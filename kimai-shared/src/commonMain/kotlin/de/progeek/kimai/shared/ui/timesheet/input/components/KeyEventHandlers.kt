package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputComponent
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputStore

/**
 * Handles keyboard events for the input field.
 * Single Responsibility: Keyboard navigation and action handling.
 * Open/Closed: Can be extended by adding new key handlers without modifying existing ones.
 *
 * @return true if the event was consumed, false otherwise
 */
internal fun handleKeyEvent(
    keyEvent: KeyEvent,
    state: TimesheetInputStore.State,
    component: TimesheetInputComponent,
    isRunning: Boolean,
    onValueUpdate: (String) -> Unit
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    return when {
        state.showTicketSuggestions -> handleTicketSuggestionKeyEvent(
            keyEvent = keyEvent,
            state = state,
            component = component,
            onValueUpdate = onValueUpdate
        )
        keyEvent.key == Key.Enter -> handleEnterKeyWithoutSuggestions(component, isRunning)
        else -> false
    }
}

/**
 * Handles keyboard events when ticket suggestions are visible.
 */
private fun handleTicketSuggestionKeyEvent(
    keyEvent: KeyEvent,
    state: TimesheetInputStore.State,
    component: TimesheetInputComponent,
    onValueUpdate: (String) -> Unit
): Boolean {
    return when (keyEvent.key) {
        Key.DirectionDown -> {
            component.onIntent(TimesheetInputStore.Intent.NavigateDown)
            true
        }
        Key.DirectionUp -> {
            component.onIntent(TimesheetInputStore.Intent.NavigateUp)
            true
        }
        Key.Enter -> handleEnterKeyWithSuggestions(state, component, onValueUpdate)
        Key.Escape -> {
            component.onIntent(TimesheetInputStore.Intent.DismissTicketSuggestions)
            true
        }
        else -> false
    }
}

/**
 * Handles Enter key when a suggestion might be selected.
 */
private fun handleEnterKeyWithSuggestions(
    state: TimesheetInputStore.State,
    component: TimesheetInputComponent,
    onValueUpdate: (String) -> Unit
): Boolean {
    if (state.selectedSuggestionIndex < 0) {
        component.onIntent(TimesheetInputStore.Intent.DismissTicketSuggestions)
        return false
    }

    component.onIntent(TimesheetInputStore.Intent.SelectSuggestion)
    val selectedIssue = state.ticketSuggestions.getOrNull(state.selectedSuggestionIndex)
    if (selectedIssue != null) {
        val formattedValue = formatTicketIssue(selectedIssue, state.ticketConfigs)
        onValueUpdate(formattedValue)
    }
    return true
}

/**
 * Handles Enter key when no suggestions are shown - starts or stops the timer.
 */
private fun handleEnterKeyWithoutSuggestions(
    component: TimesheetInputComponent,
    isRunning: Boolean
): Boolean {
    val intent = if (isRunning) {
        TimesheetInputStore.Intent.Stop
    } else {
        TimesheetInputStore.Intent.Start
    }
    component.onIntent(intent)
    return true
}
