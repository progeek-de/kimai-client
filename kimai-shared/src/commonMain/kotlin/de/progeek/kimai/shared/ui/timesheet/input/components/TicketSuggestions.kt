package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig

/**
 * Formats a ticket issue according to its configuration.
 * Single Responsibility: Ticket formatting logic.
 */
internal fun formatTicketIssue(issue: TicketIssue, configs: List<TicketSystemConfig>): String {
    val config = configs.find { it.id == issue.sourceId }
    val formatPattern = config?.issueFormat ?: IssueInsertFormat.DEFAULT_FORMAT
    return issue.format(formatPattern)
}

/**
 * Popup displaying ticket suggestions for autocomplete.
 * Single Responsibility: Ticket suggestion display and selection.
 */
@Composable
internal fun TicketSuggestionsPopup(
    visible: Boolean,
    suggestions: List<TicketIssue>,
    ticketConfigs: List<TicketSystemConfig>,
    selectedIndex: Int,
    textFieldWidth: Int,
    onDismiss: () -> Unit,
    onSuggestionSelected: (String) -> Unit
) {
    if (!visible) return

    val density = LocalDensity.current

    Popup(
        offset = IntOffset(0, 48),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false)
    ) {
        Card(
            modifier = Modifier
                .width(with(density) { textFieldWidth.toDp() })
                .shadow(4.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Column {
                suggestions.forEachIndexed { index, issue ->
                    TicketSuggestionItem(
                        issue = issue,
                        isSelected = index == selectedIndex,
                        onClick = {
                            val formattedText = formatTicketIssue(issue, ticketConfigs)
                            onSuggestionSelected(formattedText)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual item in the ticket suggestions list.
 * Single Responsibility: Single suggestion item rendering.
 */
@Composable
private fun TicketSuggestionItem(
    issue: TicketIssue,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val keyColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProviderBadge(provider = issue.provider)
        Text(
            text = issue.key,
            style = MaterialTheme.typography.labelMedium,
            color = keyColor
        )
        Text(
            text = issue.summary,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = textColor
        )
    }
}

/**
 * Badge showing the ticket provider (Jira, GitHub, GitLab).
 * Single Responsibility: Provider identification UI.
 */
@Composable
private fun ProviderBadge(provider: TicketProvider) {
    val (color, text) = when (provider) {
        TicketProvider.JIRA -> MaterialTheme.colorScheme.primary to "J"
        TicketProvider.GITHUB -> MaterialTheme.colorScheme.tertiary to "GH"
        TicketProvider.GITLAB -> MaterialTheme.colorScheme.secondary to "GL"
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
