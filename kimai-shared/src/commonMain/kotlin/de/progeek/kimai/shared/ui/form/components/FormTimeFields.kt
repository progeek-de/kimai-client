package de.progeek.kimai.shared.ui.form.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.DatePicker
import de.progeek.kimai.shared.ui.components.TextTimeField
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTimeFields(
    begin: LocalDateTime,
    end: LocalDateTime,
    snackbarHostState: SnackbarHostState,
    onBeginChange: (LocalDateTime) -> Unit,
    onEndChange: (LocalDateTime) -> Unit
) {
    var duration by remember {
        mutableStateOf(
            end.toInstant(TimeZone.currentSystemDefault())
                .minus(begin.toInstant(TimeZone.currentSystemDefault()))
        )
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(begin, end) {
        duration =
            end.toInstant(TimeZone.currentSystemDefault())
                .minus(begin.toInstant(TimeZone.currentSystemDefault()))
    }

    var openDatePicker by remember { mutableStateOf(false) }

    val startAfterEndText = stringResource(SharedRes.strings.start_after_end)
    val endBeforeStartText = stringResource(SharedRes.strings.end_before_start)
    val closeText = stringResource(SharedRes.strings.close)

    Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)) {
                TextTimeField(
                    time = begin,
                    onChange = {
                        onBeginChange(
                            if (end < it) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        startAfterEndText,
                                        actionLabel = closeText,
                                        duration = SnackbarDuration.Short
                                    )
                                }

                                end
                            } else {
                                it
                            }
                        )
                    }
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)) {
                Text("-", color = MaterialTheme.colorScheme.surfaceTint, fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)) {
                TextTimeField(
                    time = end,
                    onChange = {
                        onEndChange(
                            if (begin > it) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        endBeforeStartText,
                                        actionLabel = closeText,
                                        duration = SnackbarDuration.Short
                                    )
                                }

                                begin
                            } else {
                                it
                            }
                        )
                    }
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)) {
                IconButton(
                    onClick = { openDatePicker = !openDatePicker }
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.surfaceTint,
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = stringResource(SharedRes.strings.refresh)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)) {
                Text(
                    duration.toComponents { hours, minutes, seconds, _ ->
                        "${hours.toString().padStart(2, '0')}:${
                        minutes.toString().padStart(2, '0')
                        }:${seconds.toString().padStart(2, '0')}"
                    },
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }
        }

        if (openDatePicker) {
            DatePicker(begin.toInstant(TimeZone.currentSystemDefault())) {
                val newDate = it.toLocalDateTime(TimeZone.currentSystemDefault()).date

                onBeginChange(LocalDateTime(newDate, begin.time))
                onEndChange(LocalDateTime(newDate, end.time))

                openDatePicker = false
            }
        }
    }
}
