package de.progeek.kimai.shared.ui.jira

import com.arkivanov.mvikotlin.core.store.Store
import de.progeek.kimai.shared.core.jira.models.JiraIssue

/**
 * Store for managing Jira issue picker dialog state.
 *
 * Handles searching/filtering cached Jira issues and emitting selected issue text
 * for insertion into timesheet description field.
 */
interface JiraIssuePickerStore : Store<JiraIssuePickerStore.Intent, JiraIssuePickerStore.State, JiraIssuePickerStore.Label> {

    sealed class Intent {
        /**
         * Update search query to filter issues
         */
        data class SearchQueryUpdated(val query: String) : Intent()

        /**
         * User selected an issue - format and emit as label for text insertion
         */
        data class IssueSelected(val issue: JiraIssue) : Intent()

        /**
         * Manually refresh issues from Jira API
         */
        data object Refresh : Intent()

        /**
         * Close the picker dialog
         */
        data object Dismiss : Intent()
    }

    data class State(
        /**
         * All cached issues from database
         */
        val allIssues: List<JiraIssue> = emptyList(),

        /**
         * Issues filtered by search query
         */
        val filteredIssues: List<JiraIssue> = emptyList(),

        /**
         * Current search query
         */
        val searchQuery: String = "",

        /**
         * Loading state (refreshing from API)
         */
        val isLoading: Boolean = false,

        /**
         * Whether we're in offline mode (no network/Jira unavailable)
         */
        val isOffline: Boolean = false,

        /**
         * Error message if refresh failed
         */
        val error: String? = null,

        /**
         * Last successful sync timestamp
         */
        val lastSyncTime: Long? = null
    )

    sealed class Label {
        /**
         * Issue was selected - emit formatted text for insertion
         * Format: "PROJ-123: Issue Summary"
         */
        data class IssueSelected(val formattedText: String) : Label()

        /**
         * Dialog was dismissed without selection
         */
        data object Dismissed : Label()
    }
}
