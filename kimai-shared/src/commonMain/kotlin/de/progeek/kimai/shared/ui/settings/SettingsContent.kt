package de.progeek.kimai.shared.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.shared.BuildKonfig
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.ItemDropDown
import de.progeek.kimai.shared.ui.components.KimaiTopAppBar
import de.progeek.kimai.shared.ui.theme.ColorOption
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.utils.getLanguages
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

@Composable
fun SettingsContent(component: SettingsComponent) {
    val state by component.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        KimaiTopAppBar(onBackClick = {
            component.onOutput()
        })
        LoginBar(state.email)
        Row {
            Column(modifier = Modifier.weight(1f)) {
                DropDefaultProjectList(component)
            }
                ButtonClear(component)
        }
        ColorModeDropDown(component)
        LanguageDropDown(component)
    }
    ShowVersion()
}

@Composable
fun LoginBar(userEmail: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PersonOutline,
            contentDescription = "PersonOutline",
            modifier = Modifier.size(46.dp).padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            text = userEmail,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun DropDefaultProjectList(component: SettingsComponent) {
    val state by component.state.collectAsState()

    Column(
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp).fillMaxWidth()
    ) {
        Text(
            stringResource(SharedRes.strings.default_project),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        state.projects?.let {
            ItemDropDown(
                it.toTypedArray(),
                currentSelected = state.defaultProject,
                required = false,
                placeholder = stringResource(SharedRes.strings.select_default_project),
                mapItemToString = { project -> project.name },
            )
            { selectedItem ->
                component.onDefaultProjectClick(selectedItem)
            }
        }
    }
}

@Composable
fun ColorModeDropDown(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val colorOptions = arrayOf(ColorOption(stringResource(SharedRes.strings.light_mode), ThemeEnum.LIGHT), ColorOption(stringResource(SharedRes.strings.dark_mode), ThemeEnum.DARK))

    Column(
        modifier = Modifier.padding(start = 16.dp, top = 12.dp).fillMaxWidth()
    ) {
        Text(
            stringResource(SharedRes.strings.color_mode),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        ItemDropDown(colorOptions, colorOptions.find { it.value == state.theme },
            false, stringResource(SharedRes.strings.select_theme), mapItemToString = { it.name }) {
            component.onThemeChange(it.value)
        }
    }
}

@Composable
fun LanguageDropDown(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val languages = getLanguages()

    Column(
        modifier = Modifier.padding(start = 16.dp, top = 12.dp).fillMaxWidth()
    ) {
        Text(
            stringResource(SharedRes.strings.languages),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        ItemDropDown(
            languages, languages.find { it.languageCode == state.language.languageCode },
            false, "", mapItemToString = { StringDesc.Resource(it.name).localized() }) {
            component.onLanguageChange(it)
        }
    }
}

@Composable
fun ButtonClear(component: SettingsComponent) {
    SmallFloatingActionButton(
        onClick = { component.clearDefaultProject() },
        containerColor = MaterialTheme.colorScheme.onSecondary,
        contentColor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(top = 38.dp, start = 4.dp , end = 16.dp).size(48.dp),
        shape = MaterialTheme.shapes.small,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(SharedRes.strings.delete),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun ShowVersion() {
    val projectVersion = BuildKonfig.KIMAI_VER

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(SharedRes.strings.version, projectVersion),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}