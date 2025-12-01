package de.progeek.kimai.shared.ui.timesheet.input

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import de.progeek.kimai.shared.core.jira.repositories.JiraRepository
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputStore.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
        data class Description(val description: String) : Intent()
        data class InsertText(val text: String) : Intent()
        data class SearchJira(val query: String) : Intent()
        data object DismissJiraSuggestions : Intent()
        data object NavigateUp : Intent()
        data object NavigateDown : Intent()
        data object SelectSuggestion : Intent()
    }

    data class State(
        val description: String = "",
        val defaultProject: Project? = null,
        val runningTimesheet: TimesheetForm? = null,
        val mode: EntryMode = EntryMode.TIMER,
        val jiraEnabled: Boolean = false,
        val jiraSuggestions: List<JiraIssue> = emptyList(),
        val showJiraSuggestions: Boolean = false,
        val selectedSuggestionIndex: Int = -1
    )

    sealed interface Label {
        data class AddTimesheet(val description: String?) : Label
        data class StartTimesheet(val description: String?) : Label
        data class EditTimesheet(val form: TimesheetForm) : Label
    }
}

class TimesheetInputStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    companion object {
        private const val MIN_SEARCH_LENGTH = 2
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    private val timesheetRepository by inject<TimesheetRepository>()
    private val settingsRepository by inject<SettingsRepository>()
    private val projectRepository by inject<ProjectRepository>()
    private val jiraRepository by inject<JiraRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): TimesheetInputStore =
        object :
            TimesheetInputStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = "TimesheetInputStore",
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadEntryMode, Action.LoadTimesheetForm, Action.LoadDefaultProject, Action.LoadJiraEnabled),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed interface Action {
        data object LoadEntryMode : Action
        data object LoadTimesheetForm : Action
        data object LoadDefaultProject : Action
        data object LoadJiraEnabled : Action
    }

    private sealed class Msg {
        data class LoadedDefaultProject(val project: Project?) : Msg()
        data object ResetDefaultProject : Msg()
        data class LoadedEntryMode(val entryMode: EntryMode) : Msg()
        data class LoadedTimesheetForm(val form: TimesheetForm?) : Msg()
        data class ChangedDescription(val description: String) : Msg()
        data class LoadedJiraEnabled(val enabled: Boolean) : Msg()
        data class JiraSuggestionsLoaded(val suggestions: List<JiraIssue>) : Msg()
        data object DismissJiraSuggestions : Msg()
        data class NavigateSelection(val index: Int) : Msg()
        data class SelectSuggestion(val description: String) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<Intent, Action, State, Msg, Label>(mainContext) {
        private var jiraSearchJob: Job? = null

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.Add -> handleAdd(getState())
                Intent.Start -> handleStart(getState())
                Intent.Stop -> handleStop(getState())
                Intent.Edit -> handleEdit(getState())
                is Intent.Description -> handleDescription(intent.description)
                is Intent.InsertText -> handleInsertText(intent.text, getState())
                is Intent.SearchJira -> handleSearchJira(intent.query)
                Intent.DismissJiraSuggestions -> dispatch(Msg.DismissJiraSuggestions)
                Intent.NavigateUp -> handleNavigateUp(getState())
                Intent.NavigateDown -> handleNavigateDown(getState())
                Intent.SelectSuggestion -> handleSelectSuggestion(getState())
            }
        }

        private fun handleDescription(description: String) =
            dispatch(Msg.ChangedDescription(description))

        private fun handleInsertText(text: String, state: State) {
            val currentText = state.description
            val newDescription = when {
                currentText.isEmpty() -> text
                currentText.endsWith(" ") -> "$currentText$text"
                else -> "$currentText $text"
            }

            handleSearchJira(newDescription)
            dispatch(Msg.ChangedDescription(newDescription))
        }

        private fun handleSearchJira(query: String) {
            jiraSearchJob?.cancel()
            jiraSearchJob = scope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                jiraRepository.searchWithFallback(query, limit = 5)
                    .onSuccess { results ->
                        dispatch(Msg.JiraSuggestionsLoaded(results))
                    }
                    .onFailure {
                        dispatch(Msg.JiraSuggestionsLoaded(emptyList()))
                    }
            }
        }

        private fun handleNavigateUp(state: State) {
            if (!state.showJiraSuggestions || state.jiraSuggestions.isEmpty()) return
            val newIndex = when {
                state.selectedSuggestionIndex <= 0 -> state.jiraSuggestions.size - 1
                else -> state.selectedSuggestionIndex - 1
            }
            dispatch(Msg.NavigateSelection(newIndex))
        }

        private fun handleNavigateDown(state: State) {
            if (!state.showJiraSuggestions || state.jiraSuggestions.isEmpty()) return
            val newIndex = when {
                state.selectedSuggestionIndex >= state.jiraSuggestions.size - 1 -> 0
                else -> state.selectedSuggestionIndex + 1
            }
            dispatch(Msg.NavigateSelection(newIndex))
        }

        private fun handleSelectSuggestion(state: State) {
            if (!state.showJiraSuggestions || state.selectedSuggestionIndex < 0) return
            val selectedIssue = state.jiraSuggestions.getOrNull(state.selectedSuggestionIndex) ?: return
            val formattedText = "${selectedIssue.summary} #${selectedIssue.key}"
            dispatch(Msg.SelectSuggestion(formattedText))
        }

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
            when (canStopRunningTimesheet(state.runningTimesheet)) {
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
            when (action) {
                Action.LoadEntryMode -> loadEntryMode()
                Action.LoadTimesheetForm -> loadTimesheetForm()
                Action.LoadDefaultProject -> loadDefaultProject()
                Action.LoadJiraEnabled -> loadJiraEnabled()
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

        private fun loadJiraEnabled() {
            scope.launch {
                settingsRepository.getJiraEnabled().flowOn(ioContext).collectLatest { enabled ->
                    dispatch(Msg.LoadedJiraEnabled(enabled))
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
        override fun State.reduce(msg: Msg): State {
            return when (msg) {
                is Msg.LoadedEntryMode -> copy(mode = msg.entryMode)
                is Msg.LoadedTimesheetForm -> copy(runningTimesheet = msg.form)
                is Msg.ChangedDescription -> copy(description = msg.description)
                is Msg.LoadedDefaultProject -> copy(defaultProject = msg.project)
                is Msg.ResetDefaultProject -> copy(defaultProject = null)
                is Msg.LoadedJiraEnabled -> copy(jiraEnabled = msg.enabled)
                is Msg.JiraSuggestionsLoaded -> copy(
                    jiraSuggestions = msg.suggestions,
                    showJiraSuggestions = msg.suggestions.isNotEmpty(),
                    selectedSuggestionIndex = -1
                )
                Msg.DismissJiraSuggestions -> copy(
                    showJiraSuggestions = false,
                    selectedSuggestionIndex = -1
                )
                is Msg.NavigateSelection -> copy(selectedSuggestionIndex = msg.index)
                is Msg.SelectSuggestion -> copy(
                    description = msg.description,
                    showJiraSuggestions = false,
                    selectedSuggestionIndex = -1
                )
            }
        }
    }
}