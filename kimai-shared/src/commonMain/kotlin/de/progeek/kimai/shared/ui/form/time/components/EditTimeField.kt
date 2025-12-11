package de.progeek.kimai.shared.ui.form.time.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.ui.form.time.TimeFieldComponent
import de.progeek.kimai.shared.ui.form.time.store.TimeFieldStore
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun EditTimeField(
    component: TimeFieldComponent,
    snackbarHostState: SnackbarHostState
) {
    val state by component.state.collectAsState()
    val begin = state.begin
    val end = state.end ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    fun onBeginChanged(begin: LocalDateTime) {
        component.onIntent(TimeFieldStore.Intent.BeginChanged(begin))
    }

    fun onEndChanged(end: LocalDateTime) {
        component.onIntent(TimeFieldStore.Intent.EndChanged(end))
    }

    var duration by remember {
        mutableStateOf(
            end.toInstant(TimeZone.currentSystemDefault())
                .minus(begin.toInstant(TimeZone.currentSystemDefault()))
        )
    }

    LaunchedEffect(begin, end) {
        duration =
            end.toInstant(TimeZone.currentSystemDefault())
                .minus(begin.toInstant(TimeZone.currentSystemDefault()))
    }

    Surface(
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.onSecondary,
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                BeginTimeField(begin, end, snackbarHostState) {
                    onBeginChanged(it)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                Text(
                    "-",
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                EndTimeField(begin, end, snackbarHostState) {
                    onEndChanged(it)
                }
            }

            CalendarButton(begin, end, { onBeginChanged(it) }) {
                onEndChanged(it)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                Text(
                    duration.toComponents { hours: Long, minutes, seconds, _ ->
                        "${hours.toString().padStart(2, '0')}:${
                        minutes.toString().padStart(2, '0')
                        }:${seconds.toString().padStart(2, '0')}"
                    },
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }
        }
    }
}
