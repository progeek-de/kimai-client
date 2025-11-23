package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputStore
import de.progeek.kimai.shared.utils.isNull
import de.progeek.kimai.shared.utils.notNull
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

@Composable
internal fun TimesheetInputButton(modifier: Modifier = Modifier) {
    val component = TimesheetInputComponentLocal.current
    val state by component.state.collectAsState()

    when (state.mode) {
        EntryMode.MANUAL -> AddTimeButton(modifier)
        EntryMode.TIMER -> StartStopButton(modifier)
    }
}

@Composable
private fun StartStopButton(modifier: Modifier) {
    val component = TimesheetInputComponentLocal.current
    val state by component.state.collectAsState()

    state.runningTimesheet
        .notNull { StopButton(modifier) }
        .isNull { StartButton(modifier) }
}

@Composable
private fun StartButton(modifier: Modifier) {
    val component = TimesheetInputComponentLocal.current

    Box(modifier) {
        InputButton(
            text = stringResource(SharedRes.strings.start_button),
            running = false,
            onClick = { component.onIntent(TimesheetInputStore.Intent.Start) }
        )
    }
}

@Composable
private fun StopButton(modifier: Modifier) {
    val component = TimesheetInputComponentLocal.current
    val state by component.state.collectAsState()

    var text by remember { mutableStateOf("") }

    val timesheet = state.runningTimesheet

    LaunchedEffect(timesheet) {
        while (timesheet != null) {
            val currentTime = Clock.System.now()
            val duration = currentTime.minus(
                timesheet.begin.toInstant(TimeZone.currentSystemDefault())
            )

            text = duration.toComponents { hours, minutes, seconds, _ ->
                "${hours.toString().padStart(2, '0')}:${
                    minutes.toString().padStart(2, '0')
                }:${seconds.toString().padStart(2, '0')}"
            }
            delay(1000L)
        }
    }

    Box(modifier) {
        InputButton(
            text = text,
            running = true,
            onClick = { component.onIntent(TimesheetInputStore.Intent.Stop) }
        )
    }
}

@Composable
private fun AddTimeButton(modifier: Modifier) {
    val component = TimesheetInputComponentLocal.current

    Box(modifier) {
        InputButton(
            text = stringResource(SharedRes.strings.add_time),
            running = false,
            onClick = { component.onIntent(TimesheetInputStore.Intent.Add) }
        )
    }
}

@Composable
private fun InputButton(
    text: String,
    running: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    if(running) {
        OutlinedButton(
            onClick = onClick,
            interactionSource = interactionSource,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            modifier = Modifier
                .padding(end = 4.dp)
                .requiredWidthIn(min = 96.dp)
                .focusProperties { canFocus = false },
        ) {
            Text(if (isHovered) stringResource(SharedRes.strings.stop) else text, color =  MaterialTheme.colorScheme.error)
        }
    } else {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor =
                MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .padding(end = 4.dp)
                .requiredWidthIn(min = 96.dp)
                .focusProperties { canFocus = false }, // crashes without this
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            Text(text, color = Color.White)
        }
    }
}