package de.progeek.kimai.shared.ui.form.time

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.progeek.kimai.shared.ui.form.time.components.EditTimeField
import de.progeek.kimai.shared.ui.form.time.components.RunningTimeField

@Composable
fun TimeField(component: TimeFieldComponent, snackbarHostState: SnackbarHostState) {
    val state by component.state.collectAsState()

    when(state.mode) {
        TimeFieldMode.ADD -> EditTimeField(component, snackbarHostState)
        TimeFieldMode.START -> RunningTimeField(component, snackbarHostState)
        TimeFieldMode.EDIT_RUNNING -> RunningTimeField(component, snackbarHostState)
        TimeFieldMode.EDIT -> EditTimeField(component, snackbarHostState)
    }
}



