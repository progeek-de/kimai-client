package de.progeek.kimai.shared.ui.form.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import dev.icerock.moko.resources.compose.stringResource


@Composable
fun DescriptionInput(description: String, onDescriptionChange: (String) -> Unit) {
    DescriptionLabel()
    Input(description, onDescriptionChange)
}

@Composable
private fun DescriptionLabel() {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 12.dp)
    ){
        Text(
            stringResource(SharedRes.strings.description),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Input(description: String, onDescriptionChange: (String) -> Unit) {
    Row(modifier = Modifier.padding(top = 6.dp).fillMaxWidth()) {
        val interactionSource = remember { MutableInteractionSource() }

        Surface(
            shadowElevation = 8.dp,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.onSecondary
        ) {
            BasicTextField(
                value = description,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = onDescriptionChange,
                interactionSource = interactionSource,
                maxLines = 3,
                minLines = 3,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.surfaceTint)
            ) { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = description,
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(stringResource(SharedRes.strings.description_placeholder), color = MaterialTheme.colorScheme.surfaceTint)
                    },
                    singleLine = false,
                    interactionSource = interactionSource,
                    enabled = true,
                    visualTransformation = VisualTransformation.None,
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.onSecondary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.onSecondary,
                    )
                )
            }
        }
    }
}
