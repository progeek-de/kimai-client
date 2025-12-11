package de.progeek.kimai.shared.ui.timesheet.topbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableRows
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.ui.components.KimaiLogo
import de.progeek.kimai.shared.ui.theme.ThemeEnum
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
        Surface(
            shadowElevation = 4.dp,
            modifier = Modifier.background(Color.Transparent).padding(bottom = 16.dp)
        ) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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

        HorizontalDivider()

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
        HorizontalDivider()
        val isDarkMode = state.theme == ThemeEnum.DARK
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = if (isDarkMode) stringResource(SharedRes.strings.light_mode) else stringResource(SharedRes.strings.dark_mode)
                )
            },
            text = {
                Text(
                    if (isDarkMode) stringResource(SharedRes.strings.light_mode) else stringResource(SharedRes.strings.dark_mode),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            onClick = {
                val newTheme = if (isDarkMode) ThemeEnum.LIGHT else ThemeEnum.DARK
                component.onIntent(Intent.ToggleTheme(newTheme))
                onDismissRequest()
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = if (isDarkMode) stringResource(SharedRes.strings.light_mode) else stringResource(SharedRes.strings.dark_mode)
                )
            },
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
            leadingIcon = {
                Icon(
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
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
