package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.jira.models.SerializableAuthMethod
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import de.progeek.kimai.shared.ui.settings.store.SettingsStore

/**
 * Helper function to build JiraCredentials from form fields.
 * Extracted to follow DRY principle.
 */
private fun buildCredentials(baseUrl: String, email: String, token: String): JiraCredentials =
    JiraCredentials(
        baseUrl = baseUrl.trim(),
        authMethod = SerializableAuthMethod.ApiToken(email.trim(), token)
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JiraSettingsDialog(
    component: SettingsComponent,
    onDismiss: () -> Unit
) {
    val state by component.state.collectAsState()

    // Local state for form fields - use credentials as key to update when loaded
    var baseUrl by remember(state.jiraCredentials) {
        mutableStateOf(state.jiraCredentials?.baseUrl ?: state.jiraBaseUrl ?: "")
    }
    var email by remember(state.jiraCredentials) {
        val authMethod = state.jiraCredentials?.authMethod
        mutableStateOf(
            if (authMethod is SerializableAuthMethod.ApiToken) authMethod.email else ""
        )
    }
    var token by remember(state.jiraCredentials) {
        val authMethod = state.jiraCredentials?.authMethod
        mutableStateOf(
            when (authMethod) {
                is SerializableAuthMethod.ApiToken -> authMethod.token
                is SerializableAuthMethod.PersonalAccessToken -> authMethod.token
                null -> ""
            }
        )
    }
    var tokenVisible by remember { mutableStateOf(false) }
    var syncInterval by remember(state.jiraSyncInterval) { mutableStateOf(state.jiraSyncInterval.toString()) }
    var selectedProject by remember(state.jiraDefaultProject) { mutableStateOf(state.jiraDefaultProject) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(max = 500.dp).heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Text(
                    "Jira Configuration",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Base URL
                    Column {
                        Text(
                            "Jira Server URL",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            placeholder = { Text("https://your-company.atlassian.net") },
                            singleLine = true
                        )
                        Text(
                            "Enter base URL without /rest/api/3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    // Email field
                    Column {
                        Text(
                            "Email",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            placeholder = { Text("your.email@company.com") },
                            singleLine = true
                        )
                    }

                    // Token field
                    Column {
                        Text(
                            "API Token",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = token,
                            onValueChange = { token = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            placeholder = { Text("Enter your API token") },
                            singleLine = true,
                            visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { tokenVisible = !tokenVisible }) {
                                    Icon(
                                        imageVector = if (tokenVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (tokenVisible) "Hide token" else "Show token"
                                    )
                                }
                            }
                        )
                        Text(
                            "Generate at: id.atlassian.com/manage-profile/security/api-tokens",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    // Sync Interval
                    Column {
                        Text(
                            "Sync Interval (minutes)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = syncInterval,
                            onValueChange = {
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    syncInterval = it
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            placeholder = { Text("15") },
                            singleLine = true
                        )
                    }

                    // Default Project
                    if (state.jiraProjects.isNotEmpty()) {
                        Column {
                            Text(
                                "Default Project",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            state.jiraProjects.forEach { project ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedProject == project.key,
                                        onClick = { selectedProject = project.key }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            project.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            project.key,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Connection Status
                    if (state.jiraConnectionStatus != SettingsStore.JiraConnectionStatus.Unknown) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (state.jiraConnectionStatus) {
                                SettingsStore.JiraConnectionStatus.Testing -> {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Testing connection...")
                                }
                                SettingsStore.JiraConnectionStatus.Success -> {
                                    Text(
                                        "Connected: ${state.jiraConnectionMessage ?: "Success"}",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                SettingsStore.JiraConnectionStatus.Failed -> {
                                    Text(
                                        "Failed: ${state.jiraConnectionMessage ?: "Connection failed"}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                SettingsStore.JiraConnectionStatus.Unknown -> {}
                            }
                        }
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    // Clear Credentials Button
                    if (state.jiraCredentials != null) {
                        OutlinedButton(
                            onClick = { component.onClearJiraCredentials() }
                        ) {
                            Text("Clear")
                        }
                    }

                    // Test Connection Button
                    OutlinedButton(
                        onClick = {
                            component.onTestJiraConnection(buildCredentials(baseUrl, email, token))
                        },
                        enabled = baseUrl.isNotBlank() && token.isNotBlank() && email.isNotBlank()
                    ) {
                        Text("Test")
                    }

                    // Cancel Button
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    // Save Button
                    Button(
                        onClick = {
                            component.onSaveJiraConfig(
                                baseUrl = baseUrl.trim(),
                                credentials = buildCredentials(baseUrl, email, token),
                                enabled = state.jiraEnabled,
                                defaultProject = selectedProject,
                                syncInterval = syncInterval.toIntOrNull() ?: 15
                            )
                            onDismiss()
                        },
                        enabled = baseUrl.isNotBlank() && token.isNotBlank() && email.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
