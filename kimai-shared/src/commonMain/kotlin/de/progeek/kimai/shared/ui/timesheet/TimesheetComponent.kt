package de.progeek.kimai.shared.ui.timesheet

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.ui.ticketsystem.picker.TicketPickerComponent
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputComponent
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputStore
import de.progeek.kimai.shared.ui.timesheet.list.TimesheetListComponent
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import de.progeek.kimai.shared.ui.timesheet.topbar.TimesheetTopBarComponent
import kotlinx.coroutines.flow.MutableSharedFlow

class TimesheetComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val timesheetListInput = MutableSharedFlow<TimesheetListComponent.Input>(extraBufferCapacity = Int.MAX_VALUE)
    val timesheetListComponent = TimesheetListComponent(componentContext, storeFactory, dispatchers, timesheetListInput, ::onTimesheetListOutput)
    private fun onTimesheetListOutput(out: TimesheetListComponent.Output) {
        when (out) {
            is TimesheetListComponent.Output.Edit -> output(
                Output.ShowForm(TimesheetFormParams.EditTimesheet(out.timesheet))
            )
        }
    }

    private fun onTimesheetInputOutput(out: TimesheetInputComponent.Output) {
        when (out) {
            is TimesheetInputComponent.Output.ShowForm -> output(
                Output.ShowForm(out.data)
            )
        }
    }

    val timesheetInputComponent = TimesheetInputComponent(componentContext, storeFactory, dispatchers, ::onTimesheetInputOutput)
    val topBarComponent = TimesheetTopBarComponent(componentContext, storeFactory, dispatchers, ::onTopBarOutput)

    val ticketPickerComponent = TicketPickerComponent(componentContext, storeFactory, dispatchers, ::onTicketPickerOutput)

    private fun onTicketPickerOutput(out: TicketPickerComponent.Output) {
        when (out) {
            is TicketPickerComponent.Output.IssueSelected -> {
                // Only set description if timer is not running
                val isRunning = timesheetInputComponent.state.value.runningTimesheet != null
                if (!isRunning) {
                    timesheetInputComponent.onIntent(
                        TimesheetInputStore.Intent.Description(out.formattedText)
                    )
                }
            }
            is TicketPickerComponent.Output.Dismissed -> {
                // Dialog dismissed, no action needed
            }
        }
    }

    private fun onTopBarOutput(out: TimesheetTopBarComponent.Output) {
        when (out) {
            is TimesheetTopBarComponent.Output.Reload -> {
                timesheetListInput.tryEmit(TimesheetListComponent.Input.Reload)
            }
            is TimesheetTopBarComponent.Output.ShowSettings -> output(Output.ShowSettings)
        }
    }

    sealed interface Output {
        data object ShowSettings : Output
        data class ShowForm(val data: TimesheetFormParams) : Output
    }
}
