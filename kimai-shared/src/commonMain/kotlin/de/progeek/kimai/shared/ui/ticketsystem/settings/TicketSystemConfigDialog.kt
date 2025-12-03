@file:OptIn(ExperimentalMaterial3Api::class)

package de.progeek.kimai.shared.ui.ticketsystem.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketSystemRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.compose.koinInject
import kotlin.random.Random

/**
 * Dialog for configuring a ticket system instance.
 */
@Composable
fun TicketSystemConfigDialog(
    existingConfig: TicketSystemConfig?,
    provider: TicketProvider,
    onSave: (TicketSystemConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val ticketRepository: TicketSystemRepository = koinInject()
    val scope = rememberCoroutineScope()

    var displayName by remember { mutableStateOf(existingConfig?.displayName ?: "") }
    var baseUrl by remember { mutableStateOf(existingConfig?.baseUrl ?: getDefaultBaseUrl(provider)) }
    var syncInterval by remember { mutableStateOf(existingConfig?.syncIntervalMinutes?.toString() ?: "15") }
    var issueFormat by remember {
        mutableStateOf(existingConfig?.issueFormat ?: de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat.DEFAULT_FORMAT)
    }

    // Jira fields
    var jiraEmail by remember { mutableStateOf("") }
    var jiraToken by remember { mutableStateOf("") }
    var useJiraPat by remember { mutableStateOf(existingConfig?.credentials is TicketCredentials.JiraPersonalAccessToken) }

    // GitHub fields
    var githubToken by remember { mutableStateOf("") }
    var githubOwner by remember { mutableStateOf("") }
    var githubRepos by remember { mutableStateOf("") }

    // GitLab fields
    var gitlabToken by remember { mutableStateOf("") }
    var gitlabProjectIds by remember { mutableStateOf("") }

    // UI state
    var showToken by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    // Initialize from existing credentials
    LaunchedEffect(existingConfig) {
        existingConfig?.credentials?.let { creds ->
            when (creds) {
                is TicketCredentials.JiraApiToken -> {
                    jiraEmail = creds.email
                    jiraToken = creds.token
                    useJiraPat = false
                }
                is TicketCredentials.JiraPersonalAccessToken -> {
                    jiraToken = creds.token
                    useJiraPat = true
                }
                is TicketCredentials.GitHubToken -> {
                    githubToken = creds.token
                    githubOwner = creds.owner
                    githubRepos = creds.repositories.joinToString(", ")
                }
                is TicketCredentials.GitLabToken -> {
                    gitlabToken = creds.token
                    gitlabProjectIds = creds.projectIds.joinToString(", ")
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingConfig != null) "Edit ${provider.displayName}"
                else "Add ${provider.displayName}"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Display Name
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g., Work Jira, Personal GitHub") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Base URL
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    placeholder = { Text(getUrlPlaceholder(provider)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Provider-specific fields
                when (provider) {
                    TicketProvider.JIRA -> JiraCredentialFields(
                        email = jiraEmail,
                        onEmailChange = { jiraEmail = it },
                        token = jiraToken,
                        onTokenChange = { jiraToken = it },
                        usePat = useJiraPat,
                        onUsePatChange = { useJiraPat = it },
                        showToken = showToken,
                        onShowTokenChange = { showToken = it }
                    )
                    TicketProvider.GITHUB -> GitHubCredentialFields(
                        token = githubToken,
                        onTokenChange = { githubToken = it },
                        owner = githubOwner,
                        onOwnerChange = { githubOwner = it },
                        repos = githubRepos,
                        onReposChange = { githubRepos = it },
                        showToken = showToken,
                        onShowTokenChange = { showToken = it }
                    )
                    TicketProvider.GITLAB -> GitLabCredentialFields(
                        token = gitlabToken,
                        onTokenChange = { gitlabToken = it },
                        projectIds = gitlabProjectIds,
                        onProjectIdsChange = { gitlabProjectIds = it },
                        showToken = showToken,
                        onShowTokenChange = { showToken = it }
                    )
                }

                // Sync Interval
                OutlinedTextField(
                    value = syncInterval,
                    onValueChange = { syncInterval = it.filter { c -> c.isDigit() } },
                    label = { Text("Sync Interval (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Issue Format
                IssueFormatField(
                    value = issueFormat,
                    onValueChange = { issueFormat = it }
                )

                // Test Connection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isTesting = true
                                testResult = null
                                val config = buildConfig(
                                    id = existingConfig?.id ?: generateUuid(),
                                    displayName = displayName,
                                    provider = provider,
                                    baseUrl = baseUrl,
                                    syncInterval = syncInterval.toIntOrNull() ?: 15,
                                    jiraEmail = jiraEmail,
                                    jiraToken = jiraToken,
                                    useJiraPat = useJiraPat,
                                    githubToken = githubToken,
                                    githubOwner = githubOwner,
                                    githubRepos = githubRepos,
                                    gitlabToken = gitlabToken,
                                    gitlabProjectIds = gitlabProjectIds,
                                    issueFormat = issueFormat,
                                    existingConfig = existingConfig
                                )
                                if (config != null) {
                                    ticketRepository.testConnection(config)
                                        .onSuccess {
                                            testResult = it
                                            isSuccess = true
                                        }
                                        .onFailure {
                                            testResult = it.message
                                            isSuccess = false
                                        }
                                } else {
                                    testResult = "Please fill in all required fields"
                                    isSuccess = false
                                }
                                isTesting = false
                            }
                        },
                        enabled = !isTesting
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Test Connection")
                        }
                    }

                    testResult?.let { result ->
                        Text(
                            result,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSuccess) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val config = buildConfig(
                        id = existingConfig?.id ?: generateUuid(),
                        displayName = displayName,
                        provider = provider,
                        baseUrl = baseUrl,
                        syncInterval = syncInterval.toIntOrNull() ?: 15,
                        jiraEmail = jiraEmail,
                        jiraToken = jiraToken,
                        useJiraPat = useJiraPat,
                        githubToken = githubToken,
                        githubOwner = githubOwner,
                        githubRepos = githubRepos,
                        gitlabToken = gitlabToken,
                        gitlabProjectIds = gitlabProjectIds,
                        issueFormat = issueFormat,
                        existingConfig = existingConfig
                    )
                    if (config != null) {
                        onSave(config)
                    }
                },
                enabled = displayName.isNotBlank() && baseUrl.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun JiraCredentialFields(
    email: String,
    onEmailChange: (String) -> Unit,
    token: String,
    onTokenChange: (String) -> Unit,
    usePat: Boolean,
    onUsePatChange: (Boolean) -> Unit,
    showToken: Boolean,
    onShowTokenChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Authentication:", style = MaterialTheme.typography.bodySmall)
            FilterChip(
                selected = !usePat,
                onClick = { onUsePatChange(false) },
                label = { Text("API Token") }
            )
            FilterChip(
                selected = usePat,
                onClick = { onUsePatChange(true) },
                label = { Text("PAT") }
            )
        }

        if (!usePat) {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            label = { Text(if (usePat) "Personal Access Token" else "API Token") },
            singleLine = true,
            visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { onShowTokenChange(!showToken) }) {
                    Icon(
                        if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle visibility"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GitHubCredentialFields(
    token: String,
    onTokenChange: (String) -> Unit,
    owner: String,
    onOwnerChange: (String) -> Unit,
    repos: String,
    onReposChange: (String) -> Unit,
    showToken: Boolean,
    onShowTokenChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            label = { Text("Personal Access Token") },
            singleLine = true,
            visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { onShowTokenChange(!showToken) }) {
                    Icon(
                        if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle visibility"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = owner,
            onValueChange = onOwnerChange,
            label = { Text("Owner/Organization") },
            placeholder = { Text("e.g., octocat") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = repos,
            onValueChange = onReposChange,
            label = { Text("Repositories (optional)") },
            placeholder = { Text("e.g., repo1, repo2") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            "Leave repositories empty to search all accessible repos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GitLabCredentialFields(
    token: String,
    onTokenChange: (String) -> Unit,
    projectIds: String,
    onProjectIdsChange: (String) -> Unit,
    showToken: Boolean,
    onShowTokenChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            label = { Text("Personal Access Token") },
            singleLine = true,
            visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { onShowTokenChange(!showToken) }) {
                    Icon(
                        if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle visibility"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = projectIds,
            onValueChange = onProjectIdsChange,
            label = { Text("Project IDs or Paths (optional)") },
            placeholder = { Text("e.g., 123, group/project") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            "Leave empty to search all accessible projects",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IssueFormatField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Issue Format") },
            placeholder = { Text("{key}: {summary}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Show preview
        Text(
            "Preview: ${de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat.generateExample(value)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Show available placeholders
        Text(
            "Placeholders: {key}, {summary}, {status}, {project}, {type}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getDefaultBaseUrl(provider: TicketProvider): String = when (provider) {
    TicketProvider.JIRA -> ""
    TicketProvider.GITHUB -> "https://api.github.com"
    TicketProvider.GITLAB -> "https://gitlab.com"
}

private fun getUrlPlaceholder(provider: TicketProvider): String = when (provider) {
    TicketProvider.JIRA -> "https://your-domain.atlassian.net"
    TicketProvider.GITHUB -> "https://api.github.com"
    TicketProvider.GITLAB -> "https://gitlab.com"
}

private fun buildConfig(
    id: String,
    displayName: String,
    provider: TicketProvider,
    baseUrl: String,
    syncInterval: Int,
    jiraEmail: String,
    jiraToken: String,
    useJiraPat: Boolean,
    githubToken: String,
    githubOwner: String,
    githubRepos: String,
    gitlabToken: String,
    gitlabProjectIds: String,
    issueFormat: String,
    existingConfig: TicketSystemConfig?
): TicketSystemConfig? {
    val credentials = when (provider) {
        TicketProvider.JIRA -> {
            if (useJiraPat) {
                if (jiraToken.isBlank()) return null
                TicketCredentials.JiraPersonalAccessToken(jiraToken)
            } else {
                if (jiraEmail.isBlank() || jiraToken.isBlank()) return null
                TicketCredentials.JiraApiToken(jiraEmail, jiraToken)
            }
        }
        TicketProvider.GITHUB -> {
            if (githubToken.isBlank() || githubOwner.isBlank()) return null
            TicketCredentials.GitHubToken(
                token = githubToken,
                owner = githubOwner,
                repositories = githubRepos.split(",").map { it.trim() }.filter { it.isNotBlank() }
            )
        }
        TicketProvider.GITLAB -> {
            if (gitlabToken.isBlank()) return null
            TicketCredentials.GitLabToken(
                token = gitlabToken,
                projectIds = gitlabProjectIds.split(",").map { it.trim() }.filter { it.isNotBlank() }
            )
        }
    }

    return TicketSystemConfig(
        id = id,
        displayName = displayName.ifBlank { provider.displayName },
        provider = provider,
        enabled = existingConfig?.enabled ?: true,
        baseUrl = baseUrl,
        credentials = credentials,
        syncIntervalMinutes = syncInterval,
        createdAt = existingConfig?.createdAt ?: Clock.System.now(),
        updatedAt = Clock.System.now(),
        issueFormat = issueFormat.ifBlank { de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat.DEFAULT_FORMAT }
    )
}

/**
 * Generates a random UUID-like string.
 */
private fun generateUuid(): String {
    val chars = "0123456789abcdef"
    fun randomHex(length: Int) = (1..length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    return "${randomHex(8)}-${randomHex(4)}-${randomHex(4)}-${randomHex(4)}-${randomHex(12)}"
}
