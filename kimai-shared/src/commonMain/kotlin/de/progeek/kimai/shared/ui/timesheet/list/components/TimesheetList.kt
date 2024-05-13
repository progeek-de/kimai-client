package de.progeek.kimai.shared.ui.timesheet.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.core.models.GroupedTimesheet
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.ui.timesheet.list.TimesheetListComponent
import de.progeek.kimai.shared.ui.timesheet.list.TimesheetListStore
import de.progeek.kimai.shared.utils.format
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

@Composable
fun TimesheetList(component: TimesheetListComponent) {
    val state by component.state.collectAsState()
    val lazyColumnListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val shouldStartPaginate = remember {
        derivedStateOf { !state.isLoading
                && !state.endReached
                && lazyColumnListState.layoutInfo.totalItemsCount > 0
                && (lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -9) >= (lazyColumnListState.layoutInfo.totalItemsCount - 6)
        }
    }
    /*
    LaunchedEffect(key1 = shouldStartPaginate.value) {
        if (shouldStartPaginate.value)
            component.onIntent(TimesheetListStore.Intent.LoadNextItems)
    }

     */
    Scaffold(
        snackbarHost =  {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.BottomCenter)
            )
        },
    ) {
        Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(Modifier.fillMaxWidth(), state = lazyColumnListState) {
            items(state.timesheets) { timesheet ->
                Column (modifier = Modifier.padding(bottom = 16.dp).shadow(4.dp)) {
                    TimesheetHeader(timesheet)

                    timesheet.list.forEach { item ->
                        TimesheetItem(
                            timesheet = item,
                            running = state.running,
                            onTimesheetClick = {
                                component.onOutput(TimesheetListComponent.Output.Edit(item))
                            },
                            onRestart = {
                                component.onIntent(TimesheetListStore.Intent.Restart(item.id))
                            },
                            snackbarHostState = snackbarHostState
                        )
                        Divider(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            item {
                if (state.isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }}
    }
}

@Composable
private fun TimesheetHeader(timesheet: GroupedTimesheet) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.onSecondary)
            .padding(16.dp)
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timesheet.date.format("EEE d. MMM"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val duration = timesheet.total?.inWholeMinutes
            val hours = duration?.div(60)
            val minutes = duration?.rem(60)

            Text( stringResource(
                SharedRes.strings.total), color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,)
            Text(
                "${hours}.${minutes} h",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surfaceTint
            )
        }
    }
}

@Composable
private fun TimesheetItem(
    timesheet: Timesheet,
    running: TimesheetForm?,
    onTimesheetClick: (Timesheet) -> Unit,
    onRestart: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val exportedSnackBarText = stringResource(SharedRes.strings.entry_already_invoiced)
    val snackBarCloseText = stringResource(SharedRes.strings.close)

    Row(
        modifier = Modifier.fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.inverseOnSurface)
            .clickable {
                if (timesheet.exported) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = exportedSnackBarText,
                            actionLabel = snackBarCloseText,
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    onTimesheetClick(timesheet)
                }
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = timesheet.descriptionOrActivityName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = if (timesheet.exported) "⬆️ " else "\uD83C\uDF11 ",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (timesheet.exported) Color.Green else MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = timesheet.project.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = " - ${timesheet.project.parent}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        val enabled = running == null
        if (timesheet.end != null) {
            val color = when (enabled) {
                true -> MaterialTheme.colorScheme.surfaceTint
                false -> Color.LightGray // TODO
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val duration = timesheet.duration?.inWholeMinutes
                val hours = duration?.div(60)
                val minutes = duration?.rem(60)

                Text(
                    "${hours}.${minutes} h",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.surfaceTint
                )
                IconButton(onClick = onRestart, enabled = enabled) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = color
                    )
                }
            }
        }
    }
}