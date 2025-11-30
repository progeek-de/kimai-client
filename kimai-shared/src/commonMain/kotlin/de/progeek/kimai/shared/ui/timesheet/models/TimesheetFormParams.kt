package de.progeek.kimai.shared.ui.timesheet.models

import de.progeek.kimai.shared.core.models.Timesheet

sealed class TimesheetFormParams {

    data class EditTimesheet(
        val timesheet: Timesheet
    ) : TimesheetFormParams()

    data class EditRunningTimesheet(
        val timesheet: Timesheet
    ) : TimesheetFormParams()

    data class AddTimesheet(
        val description: String?
    ) : TimesheetFormParams()

    data class StartTimesheet(
        val description: String?
    ) : TimesheetFormParams()
}
