package de.progeek.kimai.shared.ui.ticketsystem.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider

/**
 * Dialog for selecting a ticket system provider type.
 */
@Composable
fun ProviderSelectionDialog(
    onProviderSelected: (TicketProvider) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ticket System") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Select a provider:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                TicketProvider.entries.forEach { provider ->
                    ProviderOption(
                        provider = provider,
                        onClick = { onProviderSelected(provider) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = MaterialTheme.shapes.small
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ProviderOption(
    provider: TicketProvider,
    onClick: () -> Unit
) {
    val (color, description) = when (provider) {
        TicketProvider.JIRA -> MaterialTheme.colorScheme.primary to "Atlassian Jira Cloud or Server"
        TicketProvider.GITHUB -> MaterialTheme.colorScheme.tertiary to "GitHub Issues from repositories"
        TicketProvider.GITLAB -> MaterialTheme.colorScheme.secondary to "GitLab Issues from projects"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        when (provider) {
                            TicketProvider.JIRA -> "J"
                            TicketProvider.GITHUB -> "GH"
                            TicketProvider.GITLAB -> "GL"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = color
                    )
                }
            }

            Column {
                Text(
                    provider.displayName,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
