package de.progeek.kimai.shared.core.ticketsystem.models

/**
 * Configurable format for inserting issue references into text fields.
 */
enum class IssueInsertFormat(
    val displayName: String,
    val example: String
) {
    /**
     * Summary followed by hashtag and key.
     * Example: "Fix login bug #PROJ-123"
     */
    SUMMARY_HASH_KEY("Summary #Key", "Fix login bug #PROJ-123"),

    /**
     * Key followed by colon and summary.
     * Example: "PROJ-123: Fix login bug"
     */
    KEY_COLON_SUMMARY("Key: Summary", "PROJ-123: Fix login bug"),

    /**
     * Only the key with hashtag.
     * Example: "#PROJ-123"
     */
    KEY_ONLY("Only Key", "#PROJ-123");

    /**
     * Format a ticket issue using this format pattern.
     */
    fun format(issue: TicketIssue): String = when (this) {
        SUMMARY_HASH_KEY -> "${issue.summary} #${issue.key}"
        KEY_COLON_SUMMARY -> "${issue.key}: ${issue.summary}"
        KEY_ONLY -> "#${issue.key}"
    }

    companion object {
        fun fromString(value: String): IssueInsertFormat =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: SUMMARY_HASH_KEY
    }
}
