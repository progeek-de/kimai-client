package de.progeek.kimai.shared.ui.form.time.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.TextTimeField
import de.progeek.kimai.shared.ui.form.time.TimeFieldComponent
import de.progeek.kimai.shared.ui.form.time.store.TimeFieldStore
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningTimeField(
    component: TimeFieldComponent,
    snackbarHostState: SnackbarHostState
) {
    val state by component.state.collectAsState()
    val begin = state.begin

    var currentTime by remember { mutableStateOf(Clock.System.now()) }
    var duration by remember { mutableStateOf(currentTime.minus(begin.toInstant(TimeZone.currentSystemDefault()))) }

    val scope = rememberCoroutineScope()

    val startInFutureText = stringResource(SharedRes.strings.start_in_future)
    val closeText = stringResource(SharedRes.strings.close)

    fun onBeginChanged(begin: LocalDateTime) {
        component.onIntent(TimeFieldStore.Intent.BeginChanged(begin))
    }

    LaunchedEffect(begin) {
        while (true) {
            currentTime = Clock.System.now()

            duration =
                Clock.System.now().minus(begin.toInstant(TimeZone.currentSystemDefault()))

            delay(1000L)
        }
    }

    Surface(
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.onSecondary,
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                Text(
                    stringResource(SharedRes.strings.start),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)
            ) {
                val currentLocalTime = currentTime.toLocalDateTime(TimeZone.currentSystemDefault())

                TextTimeField(
                    time = begin,
                    onChange = {
                        onBeginChanged(
                            if (currentLocalTime < it) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        startInFutureText,
                                        actionLabel = closeText,
                                        duration = SnackbarDuration.Short
                                    )
                                }

                                currentLocalTime
                            } else {
                                it
                            }
                        )
                    }
                )
            }

            Column(
                Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(SharedRes.strings.today),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically)
            ) {
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
    }
}
