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
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketSystemRepository
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputStore.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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
        data class SearchTickets(val query: String) : Intent()
        data object DismissTicketSuggestions : Intent()
        data object NavigateUp : Intent()
        data object NavigateDown : Intent()
        data object SelectSuggestion : Intent()
    }

    data class State(
        val description: String = "",
        val defaultProject: Project? = null,
        val runningTimesheet: TimesheetForm? = null,
        val mode: EntryMode = EntryMode.TIMER,
        val ticketSystemEnabled: Boolean = false,
        val ticketSuggestions: List<TicketIssue> = emptyList(),
        val showTicketSuggestions: Boolean = false,
        val selectedSuggestionIndex: Int = -1,
        val ticketConfigs: List<TicketSystemConfig> = emptyList()
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
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    private val timesheetRepository by inject<TimesheetRepository>()
    private val settingsRepository by inject<SettingsRepository>()
    private val projectRepository by inject<ProjectRepository>()
    private val ticketSystemRepository by inject<TicketSystemRepository>()
    private val ticketConfigRepository by inject<TicketConfigRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): TimesheetInputStore =
        object :
            TimesheetInputStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = "TimesheetInputStore",
                initialState = State(),
                bootstrapper = SimpleBootstrapper(
                    Action.LoadEntryMode,
                    Action.LoadTimesheetForm,
                    Action.LoadDefaultProject,
                    Action.LoadTicketSystemEnabled,
                    Action.LoadTicketConfigs
                ),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed interface Action {
        data object LoadEntryMode : Action
        data object LoadTimesheetForm : Action
        data object LoadDefaultProject : Action
        data object LoadTicketSystemEnabled : Action
        data object LoadTicketConfigs : Action
    }

    private sealed class Msg {
        data class LoadedDefaultProject(val project: Project?) : Msg()
        data object ResetDefaultProject : Msg()
        data class LoadedEntryMode(val entryMode: EntryMode) : Msg()
        data class LoadedTimesheetForm(val form: TimesheetForm?) : Msg()
        data class ChangedDescription(val description: String) : Msg()
        data class LoadedTicketSystemEnabled(val enabled: Boolean) : Msg()
        data class LoadedTicketConfigs(val configs: List<TicketSystemConfig>) : Msg()
        data class TicketSuggestionsLoaded(val suggestions: List<TicketIssue>) : Msg()
        data object DismissTicketSuggestions : Msg()
        data class NavigateSelection(val index: Int) : Msg()
        data class SelectSuggestion(val description: String) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<Intent, Action, State, Msg, Label>(mainContext) {
        private var ticketSearchJob: Job? = null

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.Add -> handleAdd(getState())
                Intent.Start -> handleStart(getState())
                Intent.Stop -> handleStop(getState())
                Intent.Edit -> handleEdit(getState())
                is Intent.Description -> handleDescription(intent.description)
                is Intent.InsertText -> handleInsertText(intent.text, getState())
                is Intent.SearchTickets -> handleSearchTickets(intent.query)
                Intent.DismissTicketSuggestions -> dispatch(Msg.DismissTicketSuggestions)
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

            handleSearchTickets(newDescription)
            dispatch(Msg.ChangedDescription(newDescription))
        }

        private fun handleSearchTickets(query: String) {
            ticketSearchJob?.cancel()
            ticketSearchJob = scope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                ticketSystemRepository.searchWithFallback(query, limit = 5)
                    .onSuccess { results ->
                        dispatch(Msg.TicketSuggestionsLoaded(results))
                    }
                    .onFailure {
                        dispatch(Msg.TicketSuggestionsLoaded(emptyList()))
                    }
            }
        }

        private fun handleNavigateUp(state: State) {
            if (!state.showTicketSuggestions || state.ticketSuggestions.isEmpty()) return
            val newIndex = when {
                state.selectedSuggestionIndex <= 0 -> state.ticketSuggestions.size - 1
                else -> state.selectedSuggestionIndex - 1
            }
            dispatch(Msg.NavigateSelection(newIndex))
        }

        private fun handleNavigateDown(state: State) {
            if (!state.showTicketSuggestions || state.ticketSuggestions.isEmpty()) return
            val newIndex = when {
                state.selectedSuggestionIndex >= state.ticketSuggestions.size - 1 -> 0
                else -> state.selectedSuggestionIndex + 1
            }
            dispatch(Msg.NavigateSelection(newIndex))
        }

        private fun handleSelectSuggestion(state: State) {
            if (!state.showTicketSuggestions || state.selectedSuggestionIndex < 0) return
            val selectedIssue = state.ticketSuggestions.getOrNull(state.selectedSuggestionIndex) ?: return
            // Get the format from the config that matches this issue's sourceId
            val config = state.ticketConfigs.find { it.id == selectedIssue.sourceId }
            val formatPattern = config?.issueFormat ?: IssueInsertFormat.DEFAULT_FORMAT
            val formattedText = selectedIssue.format(formatPattern)
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
                Action.LoadTicketSystemEnabled -> loadTicketSystemEnabled()
                Action.LoadTicketConfigs -> loadTicketConfigs()
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

        private fun loadTicketSystemEnabled() {
            scope.launch {
                ticketConfigRepository.hasEnabledConfigs().flowOn(ioContext).collectLatest { enabled ->
                    dispatch(Msg.LoadedTicketSystemEnabled(enabled))
                }
            }
        }

        private fun loadTicketConfigs() {
            scope.launch {
                ticketConfigRepository.getAllConfigs().flowOn(ioContext).collectLatest { configs ->
                    dispatch(Msg.LoadedTicketConfigs(configs))
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
                is Msg.LoadedTicketSystemEnabled -> copy(ticketSystemEnabled = msg.enabled)
                is Msg.LoadedTicketConfigs -> copy(ticketConfigs = msg.configs)
                is Msg.TicketSuggestionsLoaded -> copy(
                    ticketSuggestions = msg.suggestions,
                    showTicketSuggestions = msg.suggestions.isNotEmpty(),
                    selectedSuggestionIndex = -1
                )
                Msg.DismissTicketSuggestions -> copy(
                    showTicketSuggestions = false,
                    selectedSuggestionIndex = -1
                )
                is Msg.NavigateSelection -> copy(selectedSuggestionIndex = msg.index)
                is Msg.SelectSuggestion -> copy(
                    description = msg.description,
                    showTicketSuggestions = false,
                    selectedSuggestionIndex = -1
                )
            }
        }
    }
}