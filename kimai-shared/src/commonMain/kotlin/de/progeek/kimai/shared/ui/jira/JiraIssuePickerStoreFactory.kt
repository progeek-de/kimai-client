package de.progeek.kimai.shared.ui.jira

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import de.progeek.kimai.shared.core.jira.repositories.JiraRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

class JiraIssuePickerStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    private val jiraRepository by inject<JiraRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): JiraIssuePickerStore =
        object :
            JiraIssuePickerStore,
            Store<JiraIssuePickerStore.Intent, JiraIssuePickerStore.State, JiraIssuePickerStore.Label> by storeFactory.create(
                name = "JiraIssuePickerStore",
                initialState = JiraIssuePickerStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data class IssuesLoaded(val issues: List<JiraIssue>) : Msg()
        data class FilteredIssuesUpdated(val issues: List<JiraIssue>) : Msg()
        data class SearchQueryUpdated(val query: String) : Msg()
        data class LoadingStateChanged(val isLoading: Boolean) : Msg()
        data class OfflineStateChanged(val isOffline: Boolean) : Msg()
        data class ErrorUpdated(val error: String?) : Msg()
        data class LastSyncTimeUpdated(val timestamp: Long) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<JiraIssuePickerStore.Intent, Unit, JiraIssuePickerStore.State, Msg, JiraIssuePickerStore.Label>(
        mainContext
    ) {

        private var searchJob: Job? = null

        override fun executeAction(action: Unit, getState: () -> JiraIssuePickerStore.State) {
            loadCachedIssues()
        }

        override fun executeIntent(
            intent: JiraIssuePickerStore.Intent,
            getState: () -> JiraIssuePickerStore.State
        ) {
            when (intent) {
                is JiraIssuePickerStore.Intent.SearchQueryUpdated -> {
                    handleSearchQueryUpdated(intent.query, getState)
                }
                is JiraIssuePickerStore.Intent.IssueSelected -> {
                    handleIssueSelected(intent.issue)
                }
                is JiraIssuePickerStore.Intent.Refresh -> {
                    refreshIssues()
                }
                is JiraIssuePickerStore.Intent.Dismiss -> {
                    publish(JiraIssuePickerStore.Label.Dismissed)
                }
            }
        }

        private fun loadCachedIssues() {
            scope.launch {
                // Check if Jira credentials are configured
                if (!jiraRepository.hasCredentials()) {
                    dispatch(Msg.OfflineStateChanged(true))
                    dispatch(Msg.ErrorUpdated("Jira not configured. Set up credentials in Settings."))
                    return@launch
                }

                jiraRepository.searchIssues("").flowOn(ioContext).collectLatest { issues ->
                    dispatch(Msg.IssuesLoaded(issues))
                    dispatch(Msg.FilteredIssuesUpdated(issues))
                    dispatch(Msg.OfflineStateChanged(false))
                    dispatch(Msg.ErrorUpdated(null))
                }
            }
        }

        private fun handleSearchQueryUpdated(query: String, getState: () -> JiraIssuePickerStore.State) {
            // Update search query state immediately for UI responsiveness
            dispatch(Msg.SearchQueryUpdated(query))

            searchJob?.cancel()

            // If query is empty, show all issues
            if (query.isBlank()) {
                dispatch(Msg.FilteredIssuesUpdated(getState().allIssues))
                return
            }

            // Debounce search by 300ms
            searchJob = scope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                dispatch(Msg.LoadingStateChanged(true))

                val result: Result<List<JiraIssue>> = withContext(ioContext) {
                    jiraRepository.searchWithFallback(query)
                }

                result.onSuccess { results ->
                    dispatch(Msg.FilteredIssuesUpdated(results))
                }

                dispatch(Msg.LoadingStateChanged(false))
            }
        }

        private fun handleIssueSelected(issue: JiraIssue) {
            // Format as "KEY: Summary" for text insertion
            val formattedText = "${issue.key}: ${issue.summary}"
            publish(JiraIssuePickerStore.Label.IssueSelected(formattedText))
        }

        private fun refreshIssues() {
            scope.launch {
                dispatch(Msg.LoadingStateChanged(true))
                dispatch(Msg.ErrorUpdated(null))

                try {
                    withContext(ioContext) {
                        jiraRepository.invalidateCache("")
                    }
                    // After invalidation, the flow will automatically fetch fresh data
                    dispatch(Msg.LoadingStateChanged(false))
                    dispatch(Msg.OfflineStateChanged(false))
                    dispatch(Msg.LastSyncTimeUpdated(Clock.System.now().toEpochMilliseconds()))
                } catch (error: Exception) {
                    dispatch(Msg.LoadingStateChanged(false))
                    dispatch(Msg.OfflineStateChanged(true))
                    dispatch(Msg.ErrorUpdated(error.message ?: "Refresh failed"))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<JiraIssuePickerStore.State, Msg> {
        override fun JiraIssuePickerStore.State.reduce(msg: Msg): JiraIssuePickerStore.State =
            when (msg) {
                is Msg.IssuesLoaded -> copy(allIssues = msg.issues)
                is Msg.FilteredIssuesUpdated -> copy(filteredIssues = msg.issues)
                is Msg.SearchQueryUpdated -> copy(searchQuery = msg.query)
                is Msg.LoadingStateChanged -> copy(isLoading = msg.isLoading)
                is Msg.OfflineStateChanged -> copy(isOffline = msg.isOffline)
                is Msg.ErrorUpdated -> copy(error = msg.error)
                is Msg.LastSyncTimeUpdated -> copy(lastSyncTime = msg.timestamp)
            }
    }
}