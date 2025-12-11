package de.progeek.kimai.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import de.progeek.kimai.shared.ui.theme.kimai.KimaiTheme
import de.progeek.kimai.shared.ui.theme.progeek.ProgeekTheme

data class ColorOption(val name: String, val value: ThemeEnum)

val ThemeLocal = compositionLocalOf<ThemeEnum> {
    error("No Theme provided")
}

val BrandingLocal = compositionLocalOf<BrandingEnum> {
    error("No Branding provided")
}

@Composable
fun AppTheme(
    theme: ThemeEnum,
    branding: BrandingEnum,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(BrandingLocal provides branding) {
        when (branding) {
            BrandingEnum.KIMAI -> KimaiTheme(theme = theme, content = content)
            BrandingEnum.PROGEEK -> ProgeekTheme(theme = theme, content = content)
        }
    }
}