package de.progeek.kimai.shared.ui.timesheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import de.progeek.kimai.shared.ui.timesheet.input.components.TimesheetInputField
import de.progeek.kimai.shared.ui.timesheet.list.components.TimesheetList
import de.progeek.kimai.shared.ui.timesheet.topbar.components.TimesheetTopBar

val TimesheetComponentLocal = compositionLocalOf<TimesheetComponent> {
    error("No TimesheetComponent provided")
}

@Composable
fun TimesheetScreen(component: TimesheetComponent) {
    CompositionLocalProvider(TimesheetComponentLocal provides component) {
        TimesheetContent(component)
    }
}

@Composable
private fun TimesheetContent(component: TimesheetComponent) {
    Scaffold(
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TimesheetTopBar(component.topBarComponent)
                TimesheetInputField(component.timesheetInputComponent)
                TimesheetList(component.timesheetListComponent)
            }
        }
    )
}
