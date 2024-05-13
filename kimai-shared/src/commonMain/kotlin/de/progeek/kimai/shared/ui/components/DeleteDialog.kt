package de.progeek.kimai.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.progeek.kimai.shared.SharedRes
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun DeleteDialog(onDismiss: () -> Unit, onDelete: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(max = 390.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                    Text(
                        stringResource(SharedRes.strings.caution),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(vertical = 16.dp))

                Row(modifier = Modifier.padding(start = 16.dp, bottom = 32.dp)) {
                    Text(
                        stringResource(SharedRes.strings.delete_entry),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        onClick = onDelete
                    ) { Text(stringResource(SharedRes.strings.delete), color = MaterialTheme.colorScheme.error) }
                    Button(modifier = Modifier.padding(start = 8.dp), onClick = onDismiss) {
                        Text(
                            stringResource(SharedRes.strings.cancel),
                            color = Color.White
                        )
                    }
                }
            }
        }

    }
}