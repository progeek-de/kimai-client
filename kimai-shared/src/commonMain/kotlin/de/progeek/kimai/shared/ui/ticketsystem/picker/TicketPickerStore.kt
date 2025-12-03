package de.progeek.kimai.shared.ui.ticketsystem.picker

import com.arkivanov.mvikotlin.core.store.Store
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig

/**
 * Store for managing ticket picker dialog state.
 * Supports issues from multiple ticket system sources.
 */
interface TicketPickerStore : Store<TicketPickerStore.Intent, TicketPickerStore.State, TicketPickerStore.Label> {

    sealed class Intent {
        data class SearchQueryUpdated(val query: String) : Intent()
        data class IssueSelected(val issue: TicketIssue) : Intent()
        data object Refresh : Intent()
        data object Dismiss : Intent()
    }

    data class State(
        val allIssues: List<TicketIssue> = emptyList(),
        val filteredIssues: List<TicketIssue> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val isOffline: Boolean = false,
        val error: String? = null,
        val lastSyncTime: Long? = null,
        val hasEnabledSources: Boolean = false,
        val ticketConfigs: List<TicketSystemConfig> = emptyList()
    )

    sealed class Label {
        data class IssueSelected(val formattedText: String) : Label()
        data object Dismissed : Label()
    }
}
