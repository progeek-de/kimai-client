package de.progeek.kimai.shared.ui.theme.kimai

import androidx.compose.ui.graphics.Color

/**
 * Kimai Color Palette
 * Based on the official Kimai time tracking application theme colors (Tabler Framework)
 * Adapted for Material Design 3 Color System
 *
 * Reference: https://github.com/kimai/kimai - Tabler v1.0.0-beta19
 */

// =============================================================================
// TABLER FRAMEWORK COLORS (Official Kimai UI Framework)
// =============================================================================

object TablerColors {
    // Main Status Colors
    val Primary = Color(0xFF206BC4) // Primary Blue
    val Secondary = Color(0xFF616876) // Secondary Gray
    val Success = Color(0xFF2FB344) // Success Green
    val Danger = Color(0xFFD63939) // Danger Red
    val Warning = Color(0xFFF76707) // Warning Orange
    val Info = Color(0xFF4299E1) // Info Cyan

    // Extended Palette
    val Blue = Color(0xFF206BC4)
    val Azure = Color(0xFF4299E1)
    val Indigo = Color(0xFF4263EB)
    val Purple = Color(0xFFAE3EC9)
    val Pink = Color(0xFFD6336C)
    val Red = Color(0xFFD63939)
    val Orange = Color(0xFFF76707)
    val Yellow = Color(0xFFF59F00)
    val Lime = Color(0xFF74B816)
    val Green = Color(0xFF2FB344)
    val Teal = Color(0xFF0CA678)
    val Cyan = Color(0xFF17A2B8)

    // Chart Colors
    val ChartBackground = Color(0xFF0073B7) // rgba(0,115,183,0.7)
    val ChartBorder = Color(0xFF3B8BBA)
}

// =============================================================================
// KIMAI COLOR CHOICES (from Configuration.php)
// These are the default entity colors available in Kimai
// =============================================================================

object KimaiColors {
    val Silver = Color(0xFFC0C0C0)
    val Gray = Color(0xFF808080)
    val Black = Color(0xFF000000)
    val Maroon = Color(0xFF800000)
    val Brown = Color(0xFFA52A2A)
    val Red = Color(0xFFFF0000)
    val Orange = Color(0xFFFFA500)
    val Gold = Color(0xFFFFD700)
    val Yellow = Color(0xFFFFFF00)
    val Peach = Color(0xFFFFDAB9)
    val Khaki = Color(0xFFF0E68C)
    val Olive = Color(0xFF808000)
    val Lime = Color(0xFF00FF00)
    val Jelly = Color(0xFF9ACD32)
    val Green = Color(0xFF008000)
    val Teal = Color(0xFF008080)
    val Aqua = Color(0xFF00FFFF)
    val LightBlue = Color(0xFFADD8E6)
    val DeepSky = Color(0xFF00BFFF)
    val Dodger = Color(0xFF1E90FF)
    val Blue = Color(0xFF0000FF)
    val Navy = Color(0xFF000080)
    val Purple = Color(0xFF800080)
    val Fuchsia = Color(0xFFFF00FF)
    val Violet = Color(0xFFEE82EE)
    val Rose = Color(0xFFFFE4E1)
    val Lavender = Color(0xFFE6E6FA)

    // Default neutral color from Kimai
    val Default = Color(0xFFD2D6DE)

    val all = listOf(
        Silver, Gray, Black, Maroon, Brown, Red, Orange,
        Gold, Yellow, Peach, Khaki, Olive, Lime, Jelly, Green, Teal,
        Aqua, LightBlue, DeepSky, Dodger, Blue, Navy,
        Purple, Fuchsia, Violet, Rose, Lavender
    )
}

// =============================================================================
// LIGHT THEME COLORS
// Material Design 3 Color Scheme based on Tabler/Kimai branding
// Primary: Tabler Blue #206bc4
// =============================================================================

// Primary colors (Tabler Blue)
val kimai_theme_light_primary = Color(0xFF206BC4)
val kimai_theme_light_onPrimary = Color(0xFFFFFFFF)
val kimai_theme_light_primaryContainer = Color(0xFFD6E3FF)
val kimai_theme_light_onPrimaryContainer = Color(0xFF001B3D)

// Secondary colors (Tabler Gray)
val kimai_theme_light_secondary = Color(0xFF616876)
val kimai_theme_light_onSecondary = Color(0xFFFFFFFF)
val kimai_theme_light_secondaryContainer = Color(0xFFE2E2E9)
val kimai_theme_light_onSecondaryContainer = Color(0xFF1D1D25)

// Tertiary colors (Tabler Teal)
val kimai_theme_light_tertiary = Color(0xFF0CA678)
val kimai_theme_light_onTertiary = Color(0xFFFFFFFF)
val kimai_theme_light_tertiaryContainer = Color(0xFFA8F5D8)
val kimai_theme_light_onTertiaryContainer = Color(0xFF002117)

// Error colors (Tabler Danger)
val kimai_theme_light_error = Color(0xFFD63939)
val kimai_theme_light_errorContainer = Color(0xFFFFDAD6)
val kimai_theme_light_onError = Color(0xFFFFFFFF)
val kimai_theme_light_onErrorContainer = Color(0xFF410002)

// Background and Surface colors (Tabler Light Theme)
val kimai_theme_light_background = Color(0xFFFCFCFF)
val kimai_theme_light_onBackground = Color(0xFF1A1C1E)
val kimai_theme_light_surface = Color(0xFFFCFCFF)
val kimai_theme_light_onSurface = Color(0xFF1A1C1E)
val kimai_theme_light_surfaceVariant = Color(0xFFE0E2EC)
val kimai_theme_light_onSurfaceVariant = Color(0xFF44464F)

// Outline colors
val kimai_theme_light_outline = Color(0xFF74777F)
val kimai_theme_light_outlineVariant = Color(0xFFC4C6D0)

// Inverse colors
val kimai_theme_light_inverseSurface = Color(0xFF2F3033)
val kimai_theme_light_inverseOnSurface = Color(0xFFF1F0F4)
val kimai_theme_light_inversePrimary = Color(0xFFAAC7FF)

// Additional colors
val kimai_theme_light_shadow = Color(0xFF000000)
val kimai_theme_light_surfaceTint = Color(0xFF206BC4)
val kimai_theme_light_scrim = Color(0xFF000000)

// =============================================================================
// DARK THEME COLORS
// Material Design 3 Color Scheme based on Tabler/Kimai branding (dark mode)
// =============================================================================

// Primary colors (Tabler Blue - adjusted for dark theme)
val kimai_theme_dark_primary = Color(0xFFAAC7FF)
val kimai_theme_dark_onPrimary = Color(0xFF002F64)
val kimai_theme_dark_primaryContainer = Color(0xFF00458D)
val kimai_theme_dark_onPrimaryContainer = Color(0xFFD6E3FF)

// Secondary colors (Tabler Gray - adjusted for dark theme)
val kimai_theme_dark_secondary = Color(0xFFC6C6D0)
val kimai_theme_dark_onSecondary = Color(0xFF2F303A)
val kimai_theme_dark_secondaryContainer = Color(0xFF464751)
val kimai_theme_dark_onSecondaryContainer = Color(0xFFE2E2E9)

// Tertiary colors (Tabler Teal - adjusted for dark theme)
val kimai_theme_dark_tertiary = Color(0xFF8CD9BD)
val kimai_theme_dark_onTertiary = Color(0xFF00382A)
val kimai_theme_dark_tertiaryContainer = Color(0xFF00513E)
val kimai_theme_dark_onTertiaryContainer = Color(0xFFA8F5D8)

// Error colors (Tabler Danger - adjusted for dark theme)
val kimai_theme_dark_error = Color(0xFFFFB4AB)
val kimai_theme_dark_errorContainer = Color(0xFF93000A)
val kimai_theme_dark_onError = Color(0xFF690005)
val kimai_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

// Background and Surface colors (Tabler Dark Theme)
val kimai_theme_dark_background = Color(0xFF1A1C1E)
val kimai_theme_dark_onBackground = Color(0xFFE3E2E6)
val kimai_theme_dark_surface = Color(0xFF1A1C1E)
val kimai_theme_dark_onSurface = Color(0xFFE3E2E6)
val kimai_theme_dark_surfaceVariant = Color(0xFF44464F)
val kimai_theme_dark_onSurfaceVariant = Color(0xFFC4C6D0)

// Outline colors
val kimai_theme_dark_outline = Color(0xFF8E9099)
val kimai_theme_dark_outlineVariant = Color(0xFF44464F)

// Inverse colors
val kimai_theme_dark_inverseSurface = Color(0xFFE3E2E6)
val kimai_theme_dark_inverseOnSurface = Color(0xFF2F3033)
val kimai_theme_dark_inversePrimary = Color(0xFF206BC4)

// Additional colors
val kimai_theme_dark_shadow = Color(0xFF000000)
val kimai_theme_dark_surfaceTint = Color(0xFFAAC7FF)
val kimai_theme_dark_scrim = Color(0xFF000000)

// =============================================================================
// SEMANTIC COLORS
// Status and state colors matching Kimai/Tabler UI semantics
// =============================================================================

object KimaiSemanticColors {
    // Tabler Status colors
    val Success = Color(0xFF2FB344) // Tabler Success Green
    val Warning = Color(0xFFF76707) // Tabler Warning Orange
    val Error = Color(0xFFD63939) // Tabler Danger Red
    val Info = Color(0xFF4299E1) // Tabler Info Cyan

    // Activity states
    val Running = Color(0xFF2FB344) // Green - active timesheet
    val Stopped = Color(0xFF616876) // Secondary Gray - stopped timesheet
    val Billable = Color(0xFFF59F00) // Tabler Yellow - billable indicator
    val NonBillable = Color(0xFFC0C0C0) // Silver - non-billable

    // Entity type colors (matching Kimai defaults)
    val Customer = Color(0xFF4299E1) // Azure
    val Project = Color(0xFF0CA678) // Teal
    val Activity = Color(0xFF74B816) // Lime
    val Tag = Color(0xFFAE3EC9) // Purple
    val Team = Color(0xFF4263EB) // Indigo
    val User = Color(0xFF616876) // Secondary Gray
}

// Seed color for Material You dynamic theming (Tabler Primary Blue)
val kimai_seed = Color(0xFF206BC4)
