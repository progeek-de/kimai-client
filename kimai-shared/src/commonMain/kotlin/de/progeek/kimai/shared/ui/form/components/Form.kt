package de.progeek.kimai.shared.ui.form.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.ui.form.FormComponentLocal
import de.progeek.kimai.shared.ui.form.FormStore
import de.progeek.kimai.shared.ui.form.activity.ActivityField
import de.progeek.kimai.shared.ui.form.customer.CustomerField
import de.progeek.kimai.shared.ui.form.project.ProjectSelectionField
import de.progeek.kimai.shared.ui.form.time.TimeField


@Composable
fun Form(snackbarHostState: SnackbarHostState) {
    val component = FormComponentLocal.current
    val state by component.state.collectAsState()

    var description by remember { mutableStateOf(state.description) }

    LaunchedEffect(state.description) {
        description = state.description ?: ""
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(12.dp).padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TimeField(component.timeFieldComponent, snackbarHostState)
        CustomerField(component.customerFieldComponent)
        ProjectSelectionField(component.projectFieldComponent)
        ActivityField(component.activityFieldComponent)
        DescriptionInput(description) {
            description = it
            component.onIntent(FormStore.Intent.DescriptionUpdated(it))
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(top = 50.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Row {
                DeleteButton()
                SaveButton()
            }
        }
    }
}
