package de.progeek.kimai.shared.ui.ticketsystem.picker

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketSystemRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

class TicketPickerStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    private val ticketRepository: TicketSystemRepository by inject()
    private val ticketConfigRepository: TicketConfigRepository by inject()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): TicketPickerStore =
        object : TicketPickerStore,
            Store<TicketPickerStore.Intent, TicketPickerStore.State, TicketPickerStore.Label>
            by storeFactory.create(
                name = "TicketPickerStore",
                initialState = TicketPickerStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data class IssuesLoaded(val issues: List<TicketIssue>) : Msg()
        data class FilteredIssuesUpdated(val issues: List<TicketIssue>) : Msg()
        data class LoadingChanged(val isLoading: Boolean) : Msg()
        data class OfflineChanged(val isOffline: Boolean) : Msg()
        data class ErrorUpdated(val error: String?) : Msg()
        data class SyncTimeUpdated(val time: Long) : Msg()
        data class HasSourcesUpdated(val hasSources: Boolean) : Msg()
        data class ConfigsLoaded(val configs: List<TicketSystemConfig>) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<TicketPickerStore.Intent, Unit, TicketPickerStore.State, Msg, TicketPickerStore.Label>(
        mainContext
    ) {
        private var searchJob: Job? = null

        override fun executeAction(action: Unit) {
            loadIssues()
            loadConfigs()
        }

        private fun loadConfigs() {
            scope.launch {
                ticketConfigRepository.getAllConfigs().collect { configs ->
                    dispatch(Msg.ConfigsLoaded(configs))
                }
            }
        }

        override fun executeIntent(intent: TicketPickerStore.Intent) {
            when (intent) {
                is TicketPickerStore.Intent.SearchQueryUpdated -> handleSearchQuery(intent.query)
                is TicketPickerStore.Intent.IssueSelected -> handleIssueSelected(intent.issue)
                is TicketPickerStore.Intent.Refresh -> refreshIssues()
                is TicketPickerStore.Intent.Dismiss -> publish(TicketPickerStore.Label.Dismissed)
            }
        }

        private fun loadIssues() {
            scope.launch {
                try {
                    val hasSources = withContext(ioContext) {
                        ticketRepository.hasEnabledSources()
                    }
                    dispatch(Msg.HasSourcesUpdated(hasSources))

                    if (!hasSources) {
                        dispatch(Msg.OfflineChanged(true))
                        return@launch
                    }

                    ticketRepository.getAllIssues().collect { issues ->
                        dispatch(Msg.IssuesLoaded(issues))
                        dispatch(Msg.FilteredIssuesUpdated(filterIssues(issues, state().searchQuery)))
                        dispatch(Msg.SyncTimeUpdated(Clock.System.now().toEpochMilliseconds()))
                    }
                } catch (e: Exception) {
                    dispatch(Msg.ErrorUpdated(e.message))
                    dispatch(Msg.OfflineChanged(true))
                }
            }
        }

        private fun handleSearchQuery(query: String) {
            searchJob?.cancel()
            searchJob = scope.launch {
                delay(300) // Debounce

                if (query.isBlank()) {
                    dispatch(Msg.FilteredIssuesUpdated(state().allIssues))
                } else {
                    val result = withContext(ioContext) {
                        ticketRepository.searchWithFallback(query, 100)
                    }
                    result.onSuccess { issues ->
                        dispatch(Msg.FilteredIssuesUpdated(issues))
                    }.onFailure {
                        dispatch(Msg.FilteredIssuesUpdated(filterIssues(state().allIssues, query)))
                    }
                }
            }
        }

        private fun handleIssueSelected(issue: TicketIssue) {
            // Get the format from the config that matches this issue's sourceId
            val config = state().ticketConfigs.find { it.id == issue.sourceId }
            val formatPattern = config?.issueFormat ?: IssueInsertFormat.DEFAULT_FORMAT
            val formattedText = issue.format(formatPattern)
            publish(TicketPickerStore.Label.IssueSelected(formattedText))
        }

        private fun refreshIssues() {
            scope.launch {
                dispatch(Msg.LoadingChanged(true))
                dispatch(Msg.ErrorUpdated(null))

                val result = withContext(ioContext) {
                    ticketRepository.refreshAllSources()
                }

                result.onSuccess {
                    dispatch(Msg.SyncTimeUpdated(Clock.System.now().toEpochMilliseconds()))
                    dispatch(Msg.OfflineChanged(false))

                    // Reload issues
                    val issues = ticketRepository.getAllIssues().first()
                    dispatch(Msg.IssuesLoaded(issues))
                    dispatch(Msg.FilteredIssuesUpdated(filterIssues(issues, state().searchQuery)))
                }.onFailure { e ->
                    dispatch(Msg.ErrorUpdated(e.message))
                }

                dispatch(Msg.LoadingChanged(false))
            }
        }

        private fun filterIssues(issues: List<TicketIssue>, query: String): List<TicketIssue> {
            if (query.isBlank()) return issues
            val lowerQuery = query.lowercase()
            return issues.filter {
                it.key.lowercase().contains(lowerQuery) ||
                    it.summary.lowercase().contains(lowerQuery)
            }
        }
    }

    private object ReducerImpl : Reducer<TicketPickerStore.State, Msg> {
        override fun TicketPickerStore.State.reduce(msg: Msg): TicketPickerStore.State =
            when (msg) {
                is Msg.IssuesLoaded -> copy(allIssues = msg.issues)
                is Msg.FilteredIssuesUpdated -> copy(filteredIssues = msg.issues)
                is Msg.LoadingChanged -> copy(isLoading = msg.isLoading)
                is Msg.OfflineChanged -> copy(isOffline = msg.isOffline)
                is Msg.ErrorUpdated -> copy(error = msg.error)
                is Msg.SyncTimeUpdated -> copy(lastSyncTime = msg.time)
                is Msg.HasSourcesUpdated -> copy(hasEnabledSources = msg.hasSources)
                is Msg.ConfigsLoaded -> copy(ticketConfigs = msg.configs)
            }
    }
}