package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.foundation.layout.*
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
import de.progeek.kimai.shared.core.jira.models.JiraAuthMethod
import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import de.progeek.kimai.shared.ui.settings.store.SettingsStore

/**
 * Settings section for configuring Jira integration
 */
@Composable
fun JiraIntegrationSection(component: SettingsComponent) {
    val state by component.state.collectAsState()

    // Local state for form fields
    var baseUrl by remember(state.jiraBaseUrl) { mutableStateOf(state.jiraBaseUrl ?: "") }
    var authMethod by remember { mutableStateOf("ApiToken") }
    var email by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var tokenVisible by remember { mutableStateOf(false) }
    var syncInterval by remember(state.jiraSyncInterval) { mutableStateOf(state.jiraSyncInterval.toString()) }
    var selectedProject by remember(state.jiraDefaultProject) { mutableStateOf(state.jiraDefaultProject) }

    // Load existing credentials
    LaunchedEffect(state.jiraCredentials) {
        state.jiraCredentials?.let { creds ->
            when (creds.authMethod) {
                is JiraAuthMethod.ApiToken -> {
                    authMethod = "ApiToken"
                    email = creds.authMethod.email
                    token = creds.authMethod.token
                }
                is JiraAuthMethod.PersonalAccessToken -> {
                    authMethod = "PAT"
                    token = creds.authMethod.token
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Enable/Disable Toggle
        SettingsField(label = "Jira Integration") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Enable Jira integration",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = state.jiraEnabled,
                    onCheckedChange = { enabled ->
                        component.onToggleJiraEnabled(enabled)
                    }
                )
            }
        }

        if (state.jiraEnabled) {
            // Base URL
            SettingsField(label = "Jira Server URL") {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    placeholder = { Text("https://your-company.atlassian.net") },
                    singleLine = true
                )
            }

            // Authentication Method
            SettingsField(label = "Authentication Method") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = authMethod == "ApiToken",
                        onClick = { authMethod = "ApiToken" },
                        label = { Text("API Token") }
                    )
                    FilterChip(
                        selected = authMethod == "PAT",
                        onClick = { authMethod = "PAT" },
                        label = { Text("Personal Access Token") }
                    )
                }
            }

            // Email field (only for API Token)
            if (authMethod == "ApiToken") {
                SettingsField(label = "Email") {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        placeholder = { Text("your.email@company.com") },
                        singleLine = true
                    )
                }
            }

            // Token field
            SettingsField(label = if (authMethod == "ApiToken") "API Token" else "Personal Access Token") {
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    placeholder = { Text("Enter your token") },
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
            }

            // Sync Interval
            SettingsField(label = "Sync Interval (minutes)") {
                OutlinedTextField(
                    value = syncInterval,
                    onValueChange = {
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            syncInterval = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    placeholder = { Text("15") },
                    singleLine = true
                )
            }

            // Default Project
            if (state.jiraProjects.isNotEmpty()) {
                SettingsField(label = "Default Project") {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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
            }

            // Connection Status
            if (state.jiraConnectionStatus != SettingsStore.JiraConnectionStatus.Unknown) {
                SettingsField(label = "Connection Status") {
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
                                    "✓ ${state.jiraConnectionMessage ?: "Connected"}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            SettingsStore.JiraConnectionStatus.Failed -> {
                                Text(
                                    "✗ ${state.jiraConnectionMessage ?: "Connection failed"}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            SettingsStore.JiraConnectionStatus.Unknown -> {}
                        }
                    }
                }
            }

            // Action Buttons
            SettingsField(label = "") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Test Connection Button
                    Button(
                        onClick = { component.onTestJiraConnection() },
                        enabled = baseUrl.isNotBlank() && token.isNotBlank() &&
                                (authMethod == "PAT" || email.isNotBlank())
                    ) {
                        Text("Test Connection")
                    }

                    // Save Button
                    Button(
                        onClick = {
                            val credentials = if (authMethod == "ApiToken") {
                                JiraCredentials(JiraAuthMethod.ApiToken(email, token))
                            } else {
                                JiraCredentials(JiraAuthMethod.PersonalAccessToken(token))
                            }

                            component.onSaveJiraConfig(
                                baseUrl = baseUrl.trim(),
                                credentials = credentials,
                                enabled = state.jiraEnabled,
                                defaultProject = selectedProject,
                                syncInterval = syncInterval.toIntOrNull() ?: 15
                            )
                        },
                        enabled = baseUrl.isNotBlank() && token.isNotBlank() &&
                                (authMethod == "PAT" || email.isNotBlank())
                    ) {
                        Text("Save")
                    }

                    // Clear Credentials Button
                    if (state.jiraCredentials != null) {
                        OutlinedButton(
                            onClick = { component.onClearJiraCredentials() }
                        ) {
                            Text("Clear Credentials")
                        }
                    }
                }
            }
        }
    }
}
