package de.progeek.kimai.shared.ui.form.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.DeleteDialog
import de.progeek.kimai.shared.ui.form.FormComponentLocal
import de.progeek.kimai.shared.ui.form.FormStore
import de.progeek.kimai.shared.utils.notNull
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun DeleteButton() {
    val component = FormComponentLocal.current
    val state by component.state.collectAsState()

    state.id.notNull {
        Button()
    }
}

@Composable
private fun Button() {
    val component = FormComponentLocal.current
    val state by component.state.collectAsState()
    var deleteDialogOpen by remember { mutableStateOf(false) }

    OutlinedButton(
        modifier = Modifier.width(128.dp),
        enabled = (state.id != null),
        onClick = { deleteDialogOpen = true },
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
    ) {
        Text(stringResource(SharedRes.strings.delete), color = MaterialTheme.colorScheme.error)
    }

    if (deleteDialogOpen) {
        DeleteDialog(onDismiss = {
            deleteDialogOpen = false
        }) {
            component.onIntent(FormStore.Intent.Delete)
            deleteDialogOpen = false
        }
    }
}
