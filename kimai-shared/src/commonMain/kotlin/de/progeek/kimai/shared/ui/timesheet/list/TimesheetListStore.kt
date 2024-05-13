package de.progeek.kimai.shared.ui.timesheet.list

import com.arkivanov.mvikotlin.core.store.Store
import de.progeek.kimai.shared.core.models.GroupedTimesheet
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.ui.timesheet.list.TimesheetListStore.Intent
import de.progeek.kimai.shared.ui.timesheet.list.TimesheetListStore.Label
import de.progeek.kimai.shared.ui.timesheet.list.TimesheetListStore.State
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface TimesheetListStore : Store<Intent, State, Label> {

    sealed class Intent {
        data object LoadNextItems : Intent()
        data object Refresh : Intent()
        data class Restart(val id: Long) : Intent()
    }

    data class State(
        val isLoading: Boolean = true,
        val timesheets: List<GroupedTimesheet> = emptyList(),
        val running: TimesheetForm? = null,
        val page: Instant = Clock.System.now(),
        val endReached: Boolean = false,
    )

    sealed interface Label {
    }
}
