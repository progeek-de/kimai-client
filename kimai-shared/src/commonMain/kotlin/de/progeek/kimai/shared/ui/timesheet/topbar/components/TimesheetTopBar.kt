package de.progeek.kimai.shared.ui.timesheet.topbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.TableRows
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.ui.components.KimaiLogo
import de.progeek.kimai.shared.ui.theme.ThemeLocal
import de.progeek.kimai.shared.ui.timesheet.topbar.TimesheetTopBarComponent
import de.progeek.kimai.shared.ui.timesheet.topbar.TimesheetTopBarComponent.Output
import de.progeek.kimai.shared.ui.timesheet.topbar.TimesheetTopBarStore.Intent
import dev.icerock.moko.resources.compose.stringResource

val TimesheetTopBarComponentLocal = compositionLocalOf<TimesheetTopBarComponent> {
    error("No TimesheetComponent provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimesheetTopBar(component: TimesheetTopBarComponent) {
    CompositionLocalProvider(TimesheetTopBarComponentLocal provides component) {
        val state by component.state.collectAsState()
        val theme = ThemeLocal.current

        Surface(
            shadowElevation = 4.dp,
            modifier = Modifier.background(Color.Transparent).padding(bottom = 16.dp)
        ) {
            TopAppBar(
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { },
                navigationIcon = {
                    KimaiLogo(
                        modifier = Modifier.padding(start = 16.dp)
                            .height(35.dp)
                    )
                },
                actions = {
                    TopAppBarActions()
                }
            )
        }
    }
}

@Composable
private fun TopAppBarActions() {
    var menuExpanded by remember { mutableStateOf(false) }

    IconButton(onClick = { menuExpanded = true }) {
        Icon(imageVector = Icons.Filled.Menu, contentDescription = null)
    }

    TopAppBarDropdownMenu(menuExpanded, onDismissRequest = { menuExpanded = false })
}

@Composable
private fun TopAppBarDropdownMenu(expanded: Boolean, onDismissRequest: () -> Unit) {
    val component = TimesheetTopBarComponentLocal.current

    val state by component.state.collectAsState()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(SharedRes.strings.refresh)
                )
            },
            text = {
                Text(
                    stringResource(SharedRes.strings.refresh).uppercase(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            onClick = { component.onIntent(Intent.Reload) }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    tint = if (state.mode == EntryMode.MANUAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    imageVector = Icons.Outlined.TableRows,
                    contentDescription = stringResource(SharedRes.strings.refresh)
                )
            },
            text = {
                Text(
                    stringResource(SharedRes.strings.manual_dropdown),
                    fontSize = 12.sp,
                    color = if (state.mode == EntryMode.MANUAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            },
            onClick = {
                component.onIntent(Intent.SetMode(EntryMode.MANUAL))
                onDismissRequest()
            },
            enabled = !state.running
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    tint = if (state.mode == EntryMode.TIMER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = stringResource(SharedRes.strings.refresh)
                )
            },
            text = {
                Text(
                    stringResource(SharedRes.strings.timer_dropdown),
                    fontSize = 12.sp,
                    color = if (state.mode == EntryMode.TIMER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            },
            onClick = {
                component.onIntent(Intent.SetMode(EntryMode.TIMER))
                onDismissRequest()
            }
        )
        Divider()
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(SharedRes.strings.settings),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            onClick = {
                component.onOutput(Output.ShowSettings)
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            trailingIcon = {
                Icon(
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = stringResource(SharedRes.strings.refresh)
                )
            },
            text = {
                Text(
                    stringResource(SharedRes.strings.dashboard),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            onClick = {
                component.onIntent(Intent.ShowDashboard)
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(SharedRes.strings.logout),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            onClick = { component.onIntent(Intent.Logout) }
        )
    }
}