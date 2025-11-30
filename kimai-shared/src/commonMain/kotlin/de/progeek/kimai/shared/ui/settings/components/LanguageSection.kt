package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.ItemDropDown
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import de.progeek.kimai.shared.utils.getLanguages
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

/**
 * Section for selecting the app language
 */
@Composable
fun LanguageSection(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val languages = getLanguages()

    SettingsField(label = stringResource(SharedRes.strings.languages)) {
        ItemDropDown(
            languages,
            languages.find { it.languageCode == state.language.languageCode },
            false,
            "",
            mapItemToString = { StringDesc.Resource(it.name).localized() }
        ) {
            component.onLanguageChange(it)
        }
    }
}
