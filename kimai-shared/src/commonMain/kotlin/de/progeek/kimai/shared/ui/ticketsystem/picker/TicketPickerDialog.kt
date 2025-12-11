@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.ticketsystem.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Modal dialog for searching and selecting issues from multiple ticket sources.
 */
@Composable
fun TicketPickerDialog(
    component: TicketPickerComponent,
    onDismiss: () -> Unit
) {
    val state by component.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Dialog(onDismissRequest = {
        component.onDismiss()
        onDismiss()
    }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.small,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Select Issue",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = {
                        component.onDismiss()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        component.onSearchQueryChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by key or summary...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                component.onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Text("Refreshing...", style = MaterialTheme.typography.bodySmall)
                        } else if (state.isOffline) {
                            Text(
                                if (state.hasEnabledSources) "Offline Mode" else "No Sources",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            state.lastSyncTime?.let { syncTimeMs ->
                                val syncTime = remember(syncTimeMs) {
                                    Instant.fromEpochMilliseconds(syncTimeMs)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                }
                                Text(
                                    "Last sync: ${syncTime.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            "${state.filteredIssues.size} issues",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { component.onRefresh() },
                        enabled = !state.isLoading && state.hasEnabledSources
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }

                // Error message
                state.error?.let { error ->
                    Text(
                        error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Issues List
                if (state.filteredIssues.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when {
                                !state.hasEnabledSources -> "No ticket systems configured. Add one in Settings."
                                searchQuery.isNotEmpty() -> "No issues match your search"
                                else -> "No issues available"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.filteredIssues) { issue ->
                            TicketIssueItem(
                                issue = issue,
                                onClick = {
                                    component.onIssueSelected(issue)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketIssueItem(
    issue: TicketIssue,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Issue key, type, and source badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        issue.key,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Provider badge
                    ProviderBadge(provider = issue.provider)
                }
                Text(
                    issue.issueType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Summary
            Text(
                issue.summary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Project and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    issue.projectKey,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(".", style = MaterialTheme.typography.bodySmall)
                Text(
                    issue.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                issue.assignee?.let { assignee ->
                    Text(".", style = MaterialTheme.typography.bodySmall)
                    Text(
                        assignee,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderBadge(provider: TicketProvider) {
    val (color, text) = when (provider) {
        TicketProvider.JIRA -> MaterialTheme.colorScheme.primary to "Jira"
        TicketProvider.GITHUB -> MaterialTheme.colorScheme.tertiary to "GitHub"
        TicketProvider.GITLAB -> MaterialTheme.colorScheme.secondary to "GitLab"
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
