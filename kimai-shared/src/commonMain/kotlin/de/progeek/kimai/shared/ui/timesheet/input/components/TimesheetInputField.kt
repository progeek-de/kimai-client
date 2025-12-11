package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.ui.ticketsystem.picker.TicketPickerDialog
import de.progeek.kimai.shared.ui.timesheet.TimesheetComponentLocal
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputComponent
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputStore

val TimesheetInputComponentLocal = compositionLocalOf<TimesheetInputComponent> {
    error("No TimesheetComponent provided")
}

/**
 * Main entry point for the timesheet input field.
 * Provides the component via CompositionLocal for child composables.
 */
@Composable
fun TimesheetInputField(component: TimesheetInputComponent) {
    CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
        TimesheetInputFieldContent()
    }
}

/**
 * Container composable that orchestrates the input field and dialogs.
 * Single Responsibility: Manages the layout and state coordination.
 */
@Composable
private fun TimesheetInputFieldContent() {
    val component = TimesheetInputComponentLocal.current
    val state by component.state.collectAsState()

    var descriptionValue by remember { mutableStateOf(state.runningTimesheet?.description ?: "") }
    var showTicketDialog by remember { mutableStateOf(false) }
    var textFieldWidth by remember { mutableStateOf(0) }

    LaunchedEffect(state.runningTimesheet) {
        descriptionValue = state.runningTimesheet?.description ?: ""
    }

    // Sync description from store when not running (e.g., from ticket picker)
    LaunchedEffect(state.description) {
        if (state.runningTimesheet == null && state.description != descriptionValue) {
            descriptionValue = state.description
        }
    }

    val isRunning = state.runningTimesheet != null

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                textFieldWidth = coordinates.size.width
            }
        ) {
            DescriptionTextField(
                value = descriptionValue,
                onValueChange = { newValue ->
                    descriptionValue = newValue
                    component.onIntent(TimesheetInputStore.Intent.Description(newValue))
                    component.onIntent(TimesheetInputStore.Intent.SearchTickets(newValue))
                },
                isRunning = isRunning,
                ticketSystemEnabled = state.ticketSystemEnabled,
                onKeyEvent = { keyEvent ->
                    handleKeyEvent(
                        keyEvent = keyEvent,
                        state = state,
                        component = component,
                        isRunning = isRunning,
                        onValueUpdate = { descriptionValue = it }
                    )
                },
                onEditClick = { component.onIntent(TimesheetInputStore.Intent.Edit) },
                onTicketPickerClick = { showTicketDialog = true }
            )

            TicketSuggestionsPopup(
                visible = state.showTicketSuggestions && !isRunning,
                suggestions = state.ticketSuggestions,
                ticketConfigs = state.ticketConfigs,
                selectedIndex = state.selectedSuggestionIndex,
                textFieldWidth = textFieldWidth,
                onDismiss = {
                    component.onIntent(TimesheetInputStore.Intent.DismissTicketSuggestions)
                },
                onSuggestionSelected = { formattedText ->
                    descriptionValue = formattedText
                    component.onIntent(TimesheetInputStore.Intent.Description(formattedText))
                    component.onIntent(TimesheetInputStore.Intent.DismissTicketSuggestions)
                }
            )
        }
    }

    TicketPickerDialogWrapper(
        visible = showTicketDialog,
        onDismiss = { showTicketDialog = false }
    )
}

/**
 * Wrapper for the ticket picker dialog.
 * Single Responsibility: Dialog lifecycle management.
 */
@Composable
private fun TicketPickerDialogWrapper(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val timesheetComponent = TimesheetComponentLocal.current
    TicketPickerDialog(
        component = timesheetComponent.ticketPickerComponent,
        onDismiss = onDismiss
    )
}
