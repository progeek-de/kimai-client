package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputComponent
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputStore
import dev.icerock.moko.resources.compose.stringResource

val TimesheetInputComponentLocal = compositionLocalOf<TimesheetInputComponent> {
    error("No TimesheetComponent provided")
}

@Composable
fun TimesheetInputField(component: TimesheetInputComponent) {
    CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
        InputField()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputField() {
    val component = TimesheetInputComponentLocal.current
    val state by component.state.collectAsState()

    var value by remember { mutableStateOf(state.runningTimesheet?.description ?: "") }

    LaunchedEffect(state.runningTimesheet) {
        value = state.runningTimesheet?.description ?: ""
    }

    val interactionSource = remember { MutableInteractionSource() }
    val running = state.runningTimesheet != null

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = {
                value = it
                component.onIntent(TimesheetInputStore.Intent.Description(it))
            },
            modifier = if (running) {Modifier.clickable { component.onIntent(TimesheetInputStore.Intent.Edit) }} else { Modifier }
                .height(48.dp)
                .fillMaxWidth(),
            singleLine = true,
            interactionSource = interactionSource,
            enabled = !running,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.surfaceTint),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
            ) { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onSecondary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSecondary,
                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                enabled = !running,
                placeholder = {
                    val placeholderText = stringResource(SharedRes.strings.working_on)
                    Text( if(interactionSource.collectIsFocusedAsState().value) {
                        ""} else placeholderText,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                },
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                    top = 0.dp,
                    bottom = 0.dp,
                ),
                trailingIcon = {
                   TimesheetInputButton()
                },
                container = {
                    Box(modifier = Modifier.shadow(4.dp)
                        .background(MaterialTheme.colorScheme.onSecondary, MaterialTheme.shapes.extraSmall)) {
                    }
                }
            )
        }
    }
}
