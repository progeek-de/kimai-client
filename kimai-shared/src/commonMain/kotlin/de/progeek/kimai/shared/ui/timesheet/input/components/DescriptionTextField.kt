package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import dev.icerock.moko.resources.compose.stringResource

/**
 * The main text field for entering timesheet descriptions.
 * Single Responsibility: Text input rendering and decoration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DescriptionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isRunning: Boolean,
    ticketSystemEnabled: Boolean,
    onKeyEvent: (KeyEvent) -> Boolean,
    onEditClick: () -> Unit,
    onTicketPickerClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .applyIf(isRunning) { clickable { onEditClick() } }
            .height(48.dp)
            .fillMaxWidth()
            .onPreviewKeyEvent(onKeyEvent),
        singleLine = true,
        interactionSource = interactionSource,
        enabled = !isRunning,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.surfaceTint),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer)
    ) { innerTextField ->
        TextFieldDecorationBox(
            value = value,
            innerTextField = innerTextField,
            interactionSource = interactionSource,
            isRunning = isRunning,
            ticketSystemEnabled = ticketSystemEnabled,
            onTicketPickerClick = onTicketPickerClick
        )
    }
}

/**
 * Conditional modifier extension for cleaner code.
 */
internal inline fun Modifier.applyIf(
    condition: Boolean,
    block: Modifier.() -> Modifier
): Modifier = if (condition) block() else this

/**
 * Decoration box for the text field with leading and trailing icons.
 * Single Responsibility: Visual decoration of the text field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextFieldDecorationBox(
    value: String,
    innerTextField: @Composable () -> Unit,
    interactionSource: MutableInteractionSource,
    isRunning: Boolean,
    ticketSystemEnabled: Boolean,
    onTicketPickerClick: () -> Unit
) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = value,
        innerTextField = innerTextField,
        singleLine = true,
        colors = textFieldColors(),
        enabled = !isRunning,
        placeholder = { TextFieldPlaceholder(interactionSource) },
        visualTransformation = VisualTransformation.None,
        interactionSource = interactionSource,
        contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
            top = 0.dp,
            bottom = 0.dp
        ),
        leadingIcon = ticketPickerButton(isRunning, ticketSystemEnabled, onTicketPickerClick),
        trailingIcon = { TimesheetInputButton() },
        container = { TextFieldContainer() }
    )
}

private fun ticketPickerButton(isRunning: Boolean, ticketSystemEnabled: Boolean, onTicketPickerClick: () -> Unit): @Composable (() -> Unit)? {
    if (!isRunning && ticketSystemEnabled) {
        return { TicketPickerButton(!isRunning, ticketSystemEnabled, onTicketPickerClick) }
    }

    return null
}

/**
 * Color configuration for the text field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.onSecondary,
    unfocusedContainerColor = MaterialTheme.colorScheme.onSecondary,
    disabledContainerColor = MaterialTheme.colorScheme.onSecondary,
    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
)

/**
 * Placeholder text that disappears when focused.
 */
@Composable
private fun TextFieldPlaceholder(interactionSource: MutableInteractionSource) {
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val placeholderText = stringResource(SharedRes.strings.working_on)

    Text(
        text = if (isFocused) "" else placeholderText,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
        color = MaterialTheme.colorScheme.surfaceTint
    )
}

/**
 * Container background for the text field with shadow.
 */
@Composable
private fun TextFieldContainer() {
    Box(
        modifier = Modifier
            .shadow(4.dp)
            .background(
                MaterialTheme.colorScheme.onSecondary,
                MaterialTheme.shapes.extraSmall
            )
    )
}
