package de.progeek.kimai.shared.ui.theme.kimai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color definitions for Kimai-specific UI components
 * These colors complement the Material 3 color scheme with domain-specific semantics
 */

@Immutable
data class KimaiExtendedColors(
    // Timesheet status colors
    val timesheetRunning: Color,
    val timesheetStopped: Color,
    val timesheetPaused: Color,

    // Billable indicator colors
    val billable: Color,
    val nonBillable: Color,

    // Entity type colors (default suggestions)
    val customer: Color,
    val project: Color,
    val activity: Color,
    val tag: Color,
    val team: Color,
    val user: Color,

    // Budget status colors
    val budgetOk: Color,
    val budgetWarning: Color,
    val budgetExceeded: Color,

    // Calendar colors
    val calendarToday: Color,
    val calendarWeekend: Color,
    val calendarHoliday: Color,

    // Export/Invoice colors
    val exported: Color,
    val notExported: Color,
    val invoiced: Color,
    val notInvoiced: Color
)

val KimaiLightExtendedColors = KimaiExtendedColors(
    // Timesheet status (Tabler colors)
    timesheetRunning = Color(0xFF2FB344),     // Tabler Success Green
    timesheetStopped = Color(0xFF616876),     // Tabler Secondary Gray
    timesheetPaused = Color(0xFFF76707),      // Tabler Warning Orange

    // Billable indicators (Tabler colors)
    billable = Color(0xFFF59F00),             // Tabler Yellow
    nonBillable = Color(0xFFC0C0C0),          // KimaiColors Silver

    // Entity types (Tabler colors from KimaiSemanticColors)
    customer = Color(0xFF4299E1),             // Tabler Azure
    project = Color(0xFF0CA678),              // Tabler Teal
    activity = Color(0xFF74B816),             // Tabler Lime
    tag = Color(0xFFAE3EC9),                  // Tabler Purple
    team = Color(0xFF4263EB),                 // Tabler Indigo
    user = Color(0xFF616876),                 // Tabler Secondary Gray

    // Budget status (Tabler colors)
    budgetOk = Color(0xFF2FB344),             // Tabler Success Green
    budgetWarning = Color(0xFFF76707),        // Tabler Warning Orange
    budgetExceeded = Color(0xFFD63939),       // Tabler Danger Red

    // Calendar
    calendarToday = Color(0xFF206BC4),        // Tabler Primary Blue
    calendarWeekend = Color(0xFFF5F5F5),      // Light gray surface
    calendarHoliday = Color(0xFFFFE4E1),      // Rose

    // Export/Invoice (Tabler colors)
    exported = Color(0xFF2FB344),             // Tabler Success Green
    notExported = Color(0xFF616876),          // Tabler Secondary Gray
    invoiced = Color(0xFF4299E1),             // Tabler Azure
    notInvoiced = Color(0xFFC0C0C0)           // KimaiColors Silver
)

val KimaiDarkExtendedColors = KimaiExtendedColors(
    // Timesheet status (adjusted for dark theme)
    timesheetRunning = Color(0xFF4CAF50),     // Lighter Green
    timesheetStopped = Color(0xFF9E9E9E),     // Lighter Gray
    timesheetPaused = Color(0xFFFFB74D),      // Lighter Orange

    // Billable indicators
    billable = Color(0xFFFFD54F),             // Lighter Gold
    nonBillable = Color(0xFFBDBDBD),          // Lighter Silver

    // Entity types (adjusted for dark theme)
    customer = Color(0xFF64B5F6),             // Lighter Blue
    project = Color(0xFF4DB6AC),              // Lighter Teal
    activity = Color(0xFFC5E1A5),             // Lighter Jelly
    tag = Color(0xFFCE93D8),                  // Lighter Purple
    team = Color(0xFF7986CB),                 // Lighter Navy
    user = Color(0xFF9E9E9E),                 // Lighter Gray

    // Budget status (adjusted for dark theme)
    budgetOk = Color(0xFF4CAF50),             // Lighter Green
    budgetWarning = Color(0xFFFFB74D),        // Lighter Orange
    budgetExceeded = Color(0xFFEF5350),       // Lighter Red

    // Calendar
    calendarToday = Color(0xFFAAC7FF),        // Tabler Primary Blue (dark variant)
    calendarWeekend = Color(0xFF2A2A2A),      // Dark gray surface
    calendarHoliday = Color(0xFF5D4037),      // Dark rose/brown

    // Export/Invoice
    exported = Color(0xFF4CAF50),             // Lighter Green
    notExported = Color(0xFF9E9E9E),          // Lighter Gray
    invoiced = Color(0xFF64B5F6),             // Lighter Blue
    notInvoiced = Color(0xFFBDBDBD)           // Lighter Silver
)

val LocalKimaiExtendedColors = staticCompositionLocalOf {
    KimaiLightExtendedColors
}

/**
 * Access the extended Kimai colors from composition local
 */
object KimaiExtendedTheme {
    val colors: KimaiExtendedColors
        @Composable
        get() = LocalKimaiExtendedColors.current
}

/**
 * Helper function to get the appropriate extended colors based on theme
 */
@Composable
fun kimaiExtendedColors(): KimaiExtendedColors {
    return if (isKimaiDarkTheme()) {
        KimaiDarkExtendedColors
    } else {
        KimaiLightExtendedColors
    }
}

/**
 * Utility function to parse Kimai color string (e.g., "#ff0000" or "ff0000") to Color
 */
fun parseKimaiColor(colorString: String): Color {
    val cleanedColor = colorString.removePrefix("#")
    return try {
        Color(("FF$cleanedColor").toLong(16))
    } catch (e: Exception) {
        KimaiColors.Gray // Default fallback
    }
}

/**
 * Utility function to convert Color to Kimai hex string format
 */
fun Color.toKimaiHexString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "#${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}${blue.toString(16).padStart(2, '0')}"
}
