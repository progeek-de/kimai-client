package de.progeek.kimai.shared.ui.ticketsystem.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Settings section for managing ticket system configurations.
 * Shows a list of configured sources with add/edit/delete functionality.
 */
@Composable
fun TicketSystemSettingsSection() {
    val configRepository: TicketConfigRepository = koinInject()
    val configs by configRepository.getAllConfigs().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showProviderDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<TicketProvider?>(null) }
    var editingConfig by remember { mutableStateOf<TicketSystemConfig?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<TicketSystemConfig?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ticket Systems",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { showProviderDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Ticket System")
            }
        }

        if (configs.isEmpty()) {
            Text(
                "No ticket systems configured. Click + to add one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            configs.forEach { config ->
                TicketSystemConfigItem(
                    config = config,
                    onToggle = { enabled ->
                        scope.launch {
                            configRepository.setEnabled(config.id, enabled)
                        }
                    },
                    onEdit = {
                        editingConfig = config
                        selectedProvider = config.provider
                        showConfigDialog = true
                    },
                    onDelete = {
                        showDeleteConfirm = config
                    }
                )
            }
        }
    }

    // Provider Selection Dialog
    if (showProviderDialog) {
        ProviderSelectionDialog(
            onProviderSelected = { provider ->
                selectedProvider = provider
                editingConfig = null
                showProviderDialog = false
                showConfigDialog = true
            },
            onDismiss = { showProviderDialog = false }
        )
    }

    // Configuration Dialog
    if (showConfigDialog && selectedProvider != null) {
        TicketSystemConfigDialog(
            existingConfig = editingConfig,
            provider = selectedProvider!!,
            onSave = { config ->
                scope.launch {
                    configRepository.saveConfig(config)
                }
                showConfigDialog = false
                editingConfig = null
                selectedProvider = null
            },
            onDismiss = {
                showConfigDialog = false
                editingConfig = null
                selectedProvider = null
            }
        )
    }

    // Delete Confirmation
    showDeleteConfirm?.let { config ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Configuration") },
            text = { Text("Are you sure you want to delete '${config.displayName}'? This will also remove all cached issues from this source.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            configRepository.deleteConfig(config.id)
                        }
                        showDeleteConfirm = null
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = null },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TicketSystemConfigItem(
    config: TicketSystemConfig,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Provider icon/badge
                ProviderIndicator(config.provider)

                Column {
                    Text(
                        config.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        config.baseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Switch(
                    checked = config.enabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

@Composable
private fun ProviderIndicator(provider: TicketProvider) {
    val (color, text) = when (provider) {
        TicketProvider.JIRA -> MaterialTheme.colorScheme.primary to "J"
        TicketProvider.GITHUB -> MaterialTheme.colorScheme.tertiary to "GH"
        TicketProvider.GITLAB -> MaterialTheme.colorScheme.secondary to "GL"
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.size(36.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}
