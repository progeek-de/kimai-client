package de.progeek.kimai.shared.ui.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import de.progeek.kimai.shared.utils.addCharAtIndex
import de.progeek.kimai.shared.utils.format
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextTimeField(
    time: LocalDateTime,
    onChange: (time: LocalDateTime) -> Unit
) {
    var text by remember(time) { mutableStateOf(TextFieldValue(time.format("HH:mm"))) }
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    fun handleFocusChanged(state: FocusState) {
        if (state.isFocused) {
            focused = true
        } else {
            try {
                val validated = validateTimeString(text.text)
                val parsed = LocalTime.parse(validated)
                val new = time.date.atTime(parsed)

                text = TextFieldValue(time.format("HH:mm"))

                onChange(new)
            } catch (e: Exception) {
                text = TextFieldValue(time.format("HH:mm"))
            }
        }
    }

    fun handleValueChanged(value: TextFieldValue) {
        when (focused) {
            false ->
//                text = value
                {
                    val digitValue = value.text.take(5)
                    text = text.copy(text = digitValue, selection = TextRange(digitValue.length))
                }
            true -> {
                focused = false
                text = text.copy(selection = TextRange(0, text.text.length))
            }
        }
    }

    BasicTextField(
        modifier = Modifier.onFocusChanged { handleFocusChanged(it) },
        value = text,
        onValueChange = { handleValueChanged(it) },
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = MaterialTheme.colorScheme.surfaceTint
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, keyboardType = KeyboardType.Number),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
        }),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer)
    )
}

private fun validateTimeString(time: String): String {
    var newTime = time.filter { char -> char.isDigit() || char == ':' }

    if (newTime.length == 2) {
        newTime = "00:$newTime"
    }

    if (newTime.endsWith("\n")) {
        newTime = newTime.dropLast(1)
    }

    if (newTime.length > 5) {
        newTime = newTime.substring(0, 5)
    }

    if (newTime.find { it == ':' } == null) {
        newTime = newTime.addCharAtIndex(':', newTime.length / 2)
    }

    while (newTime.length < 5) {
        if (newTime.length % 2 == 0) {
            newTime = newTime.addCharAtIndex('0', 0)
        } else {
            newTime += '0'
        }
    }

    return newTime
}
