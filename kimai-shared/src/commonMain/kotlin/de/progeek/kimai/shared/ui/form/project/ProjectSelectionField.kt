package de.progeek.kimai.shared.ui.form.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.ItemDropDown
import de.progeek.kimai.shared.ui.form.project.ProjectFieldStore.Intent
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ProjectSelectionField(component: ProjectFieldComponent) {
    val state by component.state.collectAsState()

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 6.dp)
    ){
        Text(
            stringResource(SharedRes.strings.project),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        ItemDropDown(
            state.filteredProjects.toTypedArray(),
            currentSelected = state.selectedProject,
            mapItemToString = { it.name },
            required = true,
            placeholder = stringResource(SharedRes.strings.select_project)
        ) {
            component.onIntent(Intent.SelectedProject(it))
        }
    }
}