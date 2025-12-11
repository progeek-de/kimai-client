package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.ui.components.ItemDropDown
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import de.progeek.kimai.shared.ui.theme.BrandingEnum
import dev.icerock.moko.resources.compose.stringResource

data class BrandingOption(val name: String, val value: BrandingEnum)

/**
 * Section for selecting the app branding (Kimai/Progeek)
 */
@Composable
fun BrandingSection(component: SettingsComponent) {
    val state by component.state.collectAsState()
    val brandingOptions = arrayOf(
        BrandingOption(stringResource(SharedRes.strings.branding_kimai), BrandingEnum.KIMAI),
        BrandingOption(stringResource(SharedRes.strings.branding_progeek), BrandingEnum.PROGEEK)
    )

    SettingsField(label = stringResource(SharedRes.strings.branding)) {
        ItemDropDown(
            brandingOptions,
            brandingOptions.find { it.value == state.branding },
            false,
            stringResource(SharedRes.strings.select_branding),
            mapItemToString = { it.name }
        ) {
            component.onBrandingChange(it.value)
        }
    }
}
