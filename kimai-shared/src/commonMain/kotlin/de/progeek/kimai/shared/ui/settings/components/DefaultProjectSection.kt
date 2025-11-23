package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import dev.icerock.moko.resources.compose.stringResource

/**
 * Section for selecting and clearing the default project
 */
@Composable
fun DefaultProjectSection(component: SettingsComponent) {
    val state by component.state.collectAsState()

    SettingsField(
        label = stringResource(SharedRes.strings.default_project),
        modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 6.dp)
    ) {
        state.projects?.let { projects ->
            DefaultProjectDropdown(
                projects = projects.toTypedArray(),
                currentSelected = state.defaultProject,
                placeholder = stringResource(SharedRes.strings.select_default_project),
                onProjectSelected = { component.onDefaultProjectClick(it) },
                onClearProject = { component.clearDefaultProject() }
            )
        }
    }
}

@Composable
private fun DefaultProjectDropdown(
    projects: Array<Project>,
    currentSelected: Project?,
    placeholder: String,
    onProjectSelected: (Project) -> Unit,
    onClearProject: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var dropDownSize by remember { mutableStateOf(Size.Zero) }
    val closeDropdown = { expanded = false }

    DropdownField(
        currentSelected = currentSelected,
        placeholder = placeholder,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        dropDownSize = dropDownSize,
        onSizeChanged = { dropDownSize = it }
    ) {
        ProjectMenuItems(
            projects = projects,
            currentSelected = currentSelected,
            onProjectSelected = onProjectSelected,
            onClearProject = onClearProject,
            closeDropdown = closeDropdown
        )
    }
}

@Composable
private fun DropdownField(
    currentSelected: Project?,
    placeholder: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    dropDownSize: Size,
    onSizeChanged: (Size) -> Unit,
    menuContent: @Composable () -> Unit
) {
    val density = LocalDensity.current

    Surface(
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.onSecondary,
        border = BorderStroke(1.dp, Color.Transparent),
        modifier = Modifier.onGloballyPositioned { onSizeChanged(it.size.toSize()) }
    ) {
        Row(
            modifier = Modifier.clickable { onExpandedChange(!expanded) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            DropdownLabel(
                text = currentSelected?.name ?: placeholder,
                modifier = Modifier.weight(1f)
            )

            DropdownIcon(onClick = { onExpandedChange(true) })

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier
                    .width(with(density) { dropDownSize.width.toDp() })
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                menuContent()
            }
        }
    }
}

@Composable
private fun DropdownLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.surfaceTint,
        modifier = modifier.padding(start = 8.dp)
    )
}

@Composable
private fun DropdownIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceTint
        )
    }
}

@Composable
private fun ProjectMenuItems(
    projects: Array<Project>,
    currentSelected: Project?,
    onProjectSelected: (Project) -> Unit,
    onClearProject: () -> Unit,
    closeDropdown: () -> Unit
) {
    projects.forEach { project ->
        ProjectMenuItem(
            project = project,
            onClick = {
                closeDropdown()
                onProjectSelected(project)
            }
        )
    }

    currentSelected?.let {
        DeleteProjectMenuItem(
            onClick = {
                closeDropdown()
                onClearProject()
            }
        )
    }
}

@Composable
private fun ProjectMenuItem(
    project: Project,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        onClick = onClick,
        text = {
            Text(
                text = project.name,
                color = MaterialTheme.colorScheme.surfaceTint
            )
        }
    )
}

@Composable
private fun DeleteProjectMenuItem(onClick: () -> Unit) {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outline
    )

    DropdownMenuItem(
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = stringResource(SharedRes.strings.delete),
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}