package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.ui.settings.SettingsComponent

/**
 * Settings section for configuring Jira integration
 */
@Composable
fun JiraIntegrationSection(component: SettingsComponent) {
    val state by component.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsField(label = "Jira Integration") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Enable Jira integration",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (state.jiraEnabled) {
                        FilledTonalIconButton(
                            onClick = { showDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configure Jira",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Switch(
                    checked = state.jiraEnabled,
                    onCheckedChange = { enabled ->
                        component.onToggleJiraEnabled(enabled)
                        if (enabled && state.jiraCredentials == null) {
                            showDialog = true
                        }
                    }
                )
            }

            // Show connection status summary
            if (state.jiraEnabled && state.jiraCredentials != null) {
                Text(
                    "Connected to: ${state.jiraBaseUrl ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    // Show dialog when requested
    if (showDialog) {
        JiraSettingsDialog(
            component = component,
            onDismiss = { showDialog = false }
        )
    }
}
