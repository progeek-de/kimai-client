package de.progeek.kimai.shared.ui.timesheet.input

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputStore.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

interface TimesheetInputStore : Store<Intent, State, Label> {

    sealed class Intent {
        data object Add : Intent()
        data object Start : Intent()
        data object Stop : Intent()
        data object Edit : Intent()
        data class Description(val description: String): Intent()
    }

    data class State(
        val description: String = "",
        val defaultProject: Project? = null,
        val runningTimesheet: TimesheetForm? = null,
        val mode: EntryMode = EntryMode.TIMER
    )

    sealed interface Label {
        data class AddTimesheet(val description: String?) : Label
        data class StartTimesheet(val description: String?) : Label
        data class EditTimesheet(val form: TimesheetForm) : Label
    }
}

class TimesheetInputStoreFactory(
    private val storeFactory: StoreFactory
): KoinComponent {
    private val timesheetRepository by inject<TimesheetRepository>()
    private val settingsRepository by inject<SettingsRepository>()
    private val projectRepository by inject<ProjectRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): TimesheetInputStore =
        object : TimesheetInputStore, Store<Intent, State, Label> by storeFactory.create(
            name = "TimesheetInputStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Action.LoadEntryMode, Action.LoadTimesheetForm, Action.LoadDefaultProject),
            executorFactory = { ExecutorImpl(mainContext, ioContext) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data object LoadEntryMode : Action
        data object LoadTimesheetForm : Action
        data object LoadDefaultProject : Action
    }

    private sealed class Msg {
        data class LoadedDefaultProject(val project: Project?) : Msg()
        data object ResetDefaultProject : Msg()
        data class LoadedEntryMode(val entryMode: EntryMode) : Msg()
        data class LoadedTimesheetForm(val form: TimesheetForm?) : Msg()
        data class ChangedDescription(val description: String) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext,
    ) : CoroutineExecutor<Intent, Action, State, Msg, Label>(mainContext) {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when(intent) {
                Intent.Add -> handleAdd(getState())
                Intent.Start -> handleStart(getState())
                Intent.Stop -> handleStop(getState())
                Intent.Edit -> handleEdit(getState())
                is Intent.Description -> handleDescription(intent.description)
            }
        }

        private fun handleDescription(description: String) = 
            dispatch(Msg.ChangedDescription(description))

        private fun handleAdd(state: State) {
            publish(Label.AddTimesheet(state.description))

            // reset description
            handleDescription("")
        }


        private fun handleStart(state: State) {
            publish(Label.StartTimesheet(state.description))

            // reset description
            handleDescription("")
        }

        private fun handleStop(state: State) {
            when(canStopRunningTimesheet(state.runningTimesheet)) {
                true -> {
                    scope.launch(ioContext) {
                        state.runningTimesheet?.id?.let {
                            timesheetRepository.stopTimesheet(it)
                        }
                    }
                }
                false -> handleEdit(state)
            }
        }

        private fun handleEdit(state: State) {
            state.runningTimesheet?.let {
                publish(Label.EditTimesheet(it))
            }
        }

        private fun canStopRunningTimesheet(form: TimesheetForm?): Boolean {
            return form?.project != null && form.activity != null
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when(action) {
                Action.LoadEntryMode -> loadEntryMode()
                Action.LoadTimesheetForm -> loadTimesheetForm()
                Action.LoadDefaultProject -> loadDefaultProject()
            }
        }

        private fun loadTimesheetForm() {
            scope.launch {
                timesheetRepository.getRunningTimesheetStream().flowOn(ioContext).collectLatest { form ->
                    dispatch(Msg.LoadedTimesheetForm(form))
                }
            }
        }

        private fun loadEntryMode() {
            scope.launch {
                settingsRepository.getEntryMode().flowOn(ioContext).collectLatest {
                    dispatch(Msg.LoadedEntryMode(it))
                }
            }
        }

        private fun loadDefaultProject() {
            scope.launch {
                settingsRepository.getDefaultProject().flowOn(ioContext).collectLatest { id ->
                    projectRepository.getProjects().flowOn(ioContext).collectLatest { projects ->
                        val filterProjects = projects.filter { it.id == id }
                        dispatchProject(filterProjects)
                    }
                }
            }
        }

        private fun dispatchProject(filterProjects: List<Project?>) {
            if (filterProjects.isEmpty()) {
                dispatch(Msg.ResetDefaultProject)
            } else {
                dispatch(Msg.LoadedDefaultProject(filterProjects.firstOrNull()))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(message: Msg): State {
            return when (message) {
                is Msg.LoadedEntryMode -> copy(mode = message.entryMode)
                is Msg.LoadedTimesheetForm -> copy(runningTimesheet = message.form)
                is Msg.ChangedDescription -> copy(description = message.description)
                is Msg.LoadedDefaultProject -> copy(defaultProject = message.project)
                is Msg.ResetDefaultProject -> copy(defaultProject = null)
            }
        }
    }
}
