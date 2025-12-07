package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Button to open the ticket picker dialog.
 * Single Responsibility: Ticket picker trigger UI.
 */
@Composable
internal fun TicketPickerButton(
    enabled: Boolean,
    ticketSystemEnabled: Boolean,
    onClick: () -> Unit
) {
    if (!ticketSystemEnabled) return

    IconButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Select Issue",
            tint = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
    }
}
