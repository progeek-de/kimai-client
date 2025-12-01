package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.jira.JiraIssuePickerDialog
import de.progeek.kimai.shared.ui.timesheet.TimesheetComponentLocal
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
    var showJiraDialog by remember { mutableStateOf(false) }
    var textFieldWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    LaunchedEffect(state.runningTimesheet) {
        value = state.runningTimesheet?.description ?: ""
    }

    val interactionSource = remember { MutableInteractionSource() }
    val running = state.runningTimesheet != null

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                textFieldWidth = coordinates.size.width
            }
        ) {
            BasicTextField(
                value = value,
                onValueChange = {
                    value = it
                    component.onIntent(TimesheetInputStore.Intent.Description(it))
                    component.onIntent(TimesheetInputStore.Intent.SearchJira(it))
                },
                modifier = if (running) {
                    Modifier.clickable { component.onIntent(TimesheetInputStore.Intent.Edit) }
                } else {
                    Modifier
                }
                    .height(48.dp)
                    .fillMaxWidth()
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown && state.showJiraSuggestions) {
                            when (keyEvent.key) {
                                Key.DirectionDown -> {
                                    component.onIntent(TimesheetInputStore.Intent.NavigateDown)
                                    true
                                }
                                Key.DirectionUp -> {
                                    component.onIntent(TimesheetInputStore.Intent.NavigateUp)
                                    true
                                }
                                Key.Enter -> {
                                    if (state.selectedSuggestionIndex >= 0) {
                                        component.onIntent(TimesheetInputStore.Intent.SelectSuggestion)
                                        val selectedIssue = state.jiraSuggestions.getOrNull(
                                            state.selectedSuggestionIndex
                                        )
                                        if (selectedIssue != null) {
                                            value = "${selectedIssue.summary} #${selectedIssue.key}"
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.Escape -> {
                                    component.onIntent(TimesheetInputStore.Intent.DismissJiraSuggestions)
                                    true
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    },
                singleLine = true,
                interactionSource = interactionSource,
                enabled = !running,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.surfaceTint),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer)
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
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    enabled = !running,
                    placeholder = {
                        val placeholderText = stringResource(SharedRes.strings.working_on)
                        Text(
                            if (interactionSource.collectIsFocusedAsState().value) {
                                ""
                            } else {
                                placeholderText
                            },
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = MaterialTheme.colorScheme.surfaceTint
                        )
                    },
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                        top = 0.dp,
                        bottom = 0.dp
                    ),
                    leadingIcon = {
                        JiraPickerButton(
                            enabled = !running,
                            jiraEnabled = state.jiraEnabled,
                            onClick = { showJiraDialog = true }
                        )
                    },
                    trailingIcon = {
                        TimesheetInputButton()
                    },
                    container = {
                        Box(
                            modifier = Modifier.shadow(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSecondary,
                                    MaterialTheme.shapes.extraSmall
                                )
                        ) {
                        }
                    }
                )
            }

            // Jira autocomplete popup (doesn't steal focus)
            if (state.showJiraSuggestions && !running) {
                Popup(
                    offset = IntOffset(0, 48),
                    onDismissRequest = {
                        component.onIntent(TimesheetInputStore.Intent.DismissJiraSuggestions)
                    },
                    properties = PopupProperties(focusable = false)
                ) {
                    Card(
                        modifier = Modifier
                            .width(with(density) { textFieldWidth.toDp() })
                            .shadow(4.dp),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column {
                            state.jiraSuggestions.forEachIndexed { index, issue ->
                                val isSelected = index == state.selectedSuggestionIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .clickable {
                                            val formattedText = "${issue.summary} #${issue.key}"
                                            value = formattedText
                                            component.onIntent(
                                                TimesheetInputStore.Intent.Description(formattedText)
                                            )
                                            component.onIntent(
                                                TimesheetInputStore.Intent.DismissJiraSuggestions
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = issue.key,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                    Text(
                                        text = issue.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Render Jira picker dialog
    if (showJiraDialog) {
        val timesheetComponent = TimesheetComponentLocal.current
        JiraIssuePickerDialog(
            component = timesheetComponent.jiraIssuePickerComponent,
            onDismiss = { showJiraDialog = false }
        )
    }
}

@Composable
private fun JiraPickerButton(
    enabled: Boolean,
    jiraEnabled: Boolean,
    onClick: () -> Unit
) {
    if (jiraEnabled) {
        IconButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Select Jira Issue",
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}