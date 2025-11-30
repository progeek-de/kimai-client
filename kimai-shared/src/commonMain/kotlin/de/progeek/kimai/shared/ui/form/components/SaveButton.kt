package de.progeek.kimai.shared.ui.form.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.form.FormComponentLocal
import de.progeek.kimai.shared.ui.form.FormStore
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SaveButton() {
    val component = FormComponentLocal.current
    val state by component.state.collectAsState()

    Button(
        modifier = Modifier.padding(horizontal = 8.dp).width(128.dp),
        enabled = ((state.project != null && state.activity != null)),
        onClick = {
            component.onIntent(FormStore.Intent.Save)
        }
    ) {
        Text(stringResource(SharedRes.strings.ok), color = Color.White)
    }
}
