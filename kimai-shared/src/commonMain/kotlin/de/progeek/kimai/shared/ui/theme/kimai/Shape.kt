package de.progeek.kimai.shared.ui.theme.kimai

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Kimai Shape definitions
 * Based on the Tabler/Bootstrap border-radius values used in Kimai web application
 *
 * Tabler default border-radius: 4px (--tblr-border-radius)
 * Adapted for Material Design 3 shape scale
 */

val KimaiShapes = Shapes(
    // Extra small - for small components like chips, badges
    extraSmall = RoundedCornerShape(4.dp),

    // Small - for buttons, text fields, small cards
    small = RoundedCornerShape(4.dp),

    // Medium - for cards, dialogs, menus
    medium = RoundedCornerShape(8.dp),

    // Large - for bottom sheets, large cards
    large = RoundedCornerShape(12.dp),

    // Extra large - for full-screen dialogs, expanded panels
    extraLarge = RoundedCornerShape(16.dp)
)

/**
 * Additional shape definitions for specific Kimai UI components
 */
object KimaiComponentShapes {
    // Avatar shapes
    val AvatarSmall = RoundedCornerShape(50)  // Circular
    val AvatarMedium = RoundedCornerShape(50) // Circular
    val AvatarLarge = RoundedCornerShape(50)  // Circular

    // Card shapes
    val Card = RoundedCornerShape(8.dp)
    val CardElevated = RoundedCornerShape(12.dp)

    // Button shapes
    val Button = RoundedCornerShape(4.dp)
    val ButtonSmall = RoundedCornerShape(2.dp)
    val FloatingActionButton = RoundedCornerShape(16.dp)

    // Input field shapes
    val TextField = RoundedCornerShape(4.dp)
    val SearchBar = RoundedCornerShape(8.dp)

    // Modal and dialog shapes
    val Dialog = RoundedCornerShape(8.dp)
    val BottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

    // Tag/Chip shapes
    val Tag = RoundedCornerShape(4.dp)
    val Chip = RoundedCornerShape(8.dp)

    // Progress indicator shapes
    val ProgressBar = RoundedCornerShape(2.dp)

    // Timesheet entry shapes
    val TimesheetEntry = RoundedCornerShape(8.dp)
    val TimesheetEntryCompact = RoundedCornerShape(4.dp)
}
