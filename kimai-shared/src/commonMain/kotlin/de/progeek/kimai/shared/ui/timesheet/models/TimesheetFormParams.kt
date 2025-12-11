package de.progeek.kimai.shared.ui.timesheet.models

import de.progeek.kimai.shared.core.models.Timesheet
import kotlinx.serialization.Serializable

@Serializable
sealed class TimesheetFormParams {

    @Serializable
    data class EditTimesheet(
        val timesheet: Timesheet
    ) : TimesheetFormParams()

    @Serializable
    data class EditRunningTimesheet(
        val timesheet: Timesheet
    ) : TimesheetFormParams()

    @Serializable
    data class AddTimesheet(
        val description: String?
    ) : TimesheetFormParams()

    @Serializable
    data class StartTimesheet(
        val description: String?
    ) : TimesheetFormParams()
}
