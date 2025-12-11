package de.progeek.kimai.shared.ui.theme.kimai

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.ui.theme.ThemeLocal

/**
 * Kimai Theme for Compose Material 3
 * Based on the official Kimai time tracking application theme
 */

private val KimaiLightColorScheme = lightColorScheme(
    primary = kimai_theme_light_primary,
    onPrimary = kimai_theme_light_onPrimary,
    primaryContainer = kimai_theme_light_primaryContainer,
    onPrimaryContainer = kimai_theme_light_onPrimaryContainer,
    secondary = kimai_theme_light_secondary,
    onSecondary = kimai_theme_light_onSecondary,
    secondaryContainer = kimai_theme_light_secondaryContainer,
    onSecondaryContainer = kimai_theme_light_onSecondaryContainer,
    tertiary = kimai_theme_light_tertiary,
    onTertiary = kimai_theme_light_onTertiary,
    tertiaryContainer = kimai_theme_light_tertiaryContainer,
    onTertiaryContainer = kimai_theme_light_onTertiaryContainer,
    error = kimai_theme_light_error,
    errorContainer = kimai_theme_light_errorContainer,
    onError = kimai_theme_light_onError,
    onErrorContainer = kimai_theme_light_onErrorContainer,
    background = kimai_theme_light_background,
    onBackground = kimai_theme_light_onBackground,
    surface = kimai_theme_light_surface,
    onSurface = kimai_theme_light_onSurface,
    surfaceVariant = kimai_theme_light_surfaceVariant,
    onSurfaceVariant = kimai_theme_light_onSurfaceVariant,
    outline = kimai_theme_light_outline,
    inverseOnSurface = kimai_theme_light_inverseOnSurface,
    inverseSurface = kimai_theme_light_inverseSurface,
    inversePrimary = kimai_theme_light_inversePrimary,
    surfaceTint = kimai_theme_light_surfaceTint,
    outlineVariant = kimai_theme_light_outlineVariant,
    scrim = kimai_theme_light_scrim
)

private val KimaiDarkColorScheme = darkColorScheme(
    primary = kimai_theme_dark_primary,
    onPrimary = kimai_theme_dark_onPrimary,
    primaryContainer = kimai_theme_dark_primaryContainer,
    onPrimaryContainer = kimai_theme_dark_onPrimaryContainer,
    secondary = kimai_theme_dark_secondary,
    onSecondary = kimai_theme_dark_onSecondary,
    secondaryContainer = kimai_theme_dark_secondaryContainer,
    onSecondaryContainer = kimai_theme_dark_onSecondaryContainer,
    tertiary = kimai_theme_dark_tertiary,
    onTertiary = kimai_theme_dark_onTertiary,
    tertiaryContainer = kimai_theme_dark_tertiaryContainer,
    onTertiaryContainer = kimai_theme_dark_onTertiaryContainer,
    error = kimai_theme_dark_error,
    errorContainer = kimai_theme_dark_errorContainer,
    onError = kimai_theme_dark_onError,
    onErrorContainer = kimai_theme_dark_onErrorContainer,
    background = kimai_theme_dark_background,
    onBackground = kimai_theme_dark_onBackground,
    surface = kimai_theme_dark_surface,
    onSurface = kimai_theme_dark_onSurface,
    surfaceVariant = kimai_theme_dark_surfaceVariant,
    onSurfaceVariant = kimai_theme_dark_onSurfaceVariant,
    outline = kimai_theme_dark_outline,
    inverseOnSurface = kimai_theme_dark_inverseOnSurface,
    inverseSurface = kimai_theme_dark_inverseSurface,
    inversePrimary = kimai_theme_dark_inversePrimary,
    surfaceTint = kimai_theme_dark_surfaceTint,
    outlineVariant = kimai_theme_dark_outlineVariant,
    scrim = kimai_theme_dark_scrim
)

val KimaiThemeLocal = compositionLocalOf<ThemeEnum> {
    error("No Kimai Theme provided")
}

/**
 * Kimai Theme composable that applies the Kimai color scheme to Material 3 components
 *
 * @param theme The theme mode to use (LIGHT or DARK)
 * @param content The content to be themed
 */
@Composable
fun KimaiTheme(
    theme: ThemeEnum,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        ThemeEnum.DARK -> KimaiDarkColorScheme
        ThemeEnum.LIGHT -> KimaiLightColorScheme
    }

    CompositionLocalProvider(
        KimaiThemeLocal provides theme,
        ThemeLocal provides theme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KimaiTypography,
            shapes = KimaiShapes,
            content = content
        )
    }
}

/**
 * Helper function to check if the current theme is dark
 */
@Composable
fun isKimaiDarkTheme(): Boolean {
    val theme = KimaiThemeLocal.current

    return when (theme) {
        ThemeEnum.DARK -> true
        ThemeEnum.LIGHT -> false
    }
}