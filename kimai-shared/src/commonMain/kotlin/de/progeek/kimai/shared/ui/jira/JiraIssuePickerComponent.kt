package de.progeek.kimai.shared.ui.jira

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

/**
 * Component for managing Jira issue picker dialog.
 *
 * Wraps JiraIssuePickerStore and exposes state/intents to UI layer.
 * Handles label emission for text insertion into timesheet description.
 */
class JiraIssuePickerComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        JiraIssuePickerStoreFactory(storeFactory = storeFactory).create(dispatchers.main, dispatchers.io)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<JiraIssuePickerStore.State> = store.stateFlow

    init {
        // Listen to store labels and emit as component outputs
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.labels.bindTo {
                when (it) {
                    is JiraIssuePickerStore.Label.IssueSelected -> {
                        output(Output.IssueSelected(it.formattedText))
                    }
                    is JiraIssuePickerStore.Label.Dismissed -> {
                        output(Output.Dismissed)
                    }
                }
            }
        }
    }

    /**
     * Update search query to filter issues
     */
    fun onSearchQueryChanged(query: String) {
        store.accept(JiraIssuePickerStore.Intent.SearchQueryUpdated(query))
    }

    /**
     * User selected an issue - emit formatted text for insertion
     */
    fun onIssueSelected(issue: JiraIssue) {
        store.accept(JiraIssuePickerStore.Intent.IssueSelected(issue))
    }

    /**
     * Manually refresh issues from Jira API
     */
    fun onRefresh() {
        store.accept(JiraIssuePickerStore.Intent.Refresh)
    }

    /**
     * Close the picker dialog
     */
    fun onDismiss() {
        store.accept(JiraIssuePickerStore.Intent.Dismiss)
    }

    /**
     * Outputs emitted by the component to parent
     */
    sealed class Output {
        /**
         * Issue was selected - formatted text ready for insertion
         * Format: "PROJ-123: Issue Summary"
         */
        data class IssueSelected(val formattedText: String) : Output()

        /**
         * Dialog was dismissed without selection
         */
        data object Dismissed : Output()
    }
}
