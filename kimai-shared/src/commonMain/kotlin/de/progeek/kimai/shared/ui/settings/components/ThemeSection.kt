package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.ItemDropDown
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import de.progeek.kimai.shared.ui.theme.ColorOption
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import dev.icerock.moko.resources.compose.stringResource

/**
 * Section for selecting the app theme (light/dark/system mode)
 */
@Composable
fun ThemeSection(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val colorOptions = arrayOf(
        ColorOption(stringResource(SharedRes.strings.system_mode), ThemeEnum.SYSTEM),
        ColorOption(stringResource(SharedRes.strings.light_mode), ThemeEnum.LIGHT),
        ColorOption(stringResource(SharedRes.strings.dark_mode), ThemeEnum.DARK)
    )

    SettingsField(label = stringResource(SharedRes.strings.color_mode)) {
        ItemDropDown(
            colorOptions,
            colorOptions.find { it.value == state.theme },
            false,
            stringResource(SharedRes.strings.select_theme),
            mapItemToString = { it.name }
        ) {
            component.onThemeChange(it.value)
        }
    }
}