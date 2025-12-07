package de.progeek.kimai.shared.core.ticketsystem.models

/**
 * Utility object for formatting issue references using customizable patterns.
 *
 * Supported placeholders:
 * - {key} - The issue key (e.g., "PROJ-123", "#456")
 * - {summary} - The issue summary/title
 * - {status} - The issue status
 * - {project} - The project key
 * - {type} - The issue type
 *
 * Example patterns:
 * - "{key}: {summary}" -> "PROJ-123: Fix login bug"
 * - "{summary} #{key}" -> "Fix login bug #PROJ-123"
 * - "#{key}" -> "#PROJ-123"
 * - "[{key}] {summary}" -> "[PROJ-123] Fix login bug"
 */
object IssueInsertFormat {

    /**
     * Default format pattern used when none is specified.
     */
    const val DEFAULT_FORMAT = "{key}: {summary}"

    /**
     * Format a ticket issue using the specified format pattern.
     *
     * @param issue The ticket issue to format
     * @param formatPattern The format pattern with placeholders
     * @return The formatted string with placeholders replaced
     */
    fun format(issue: TicketIssue, formatPattern: String): String {
        return formatPattern
            .replace("{key}", issue.key)
            .replace("{summary}", issue.summary)
            .replace("{status}", issue.status)
            .replace("{project}", issue.projectKey)
            .replace("{type}", issue.issueType)
    }

    /**
     * Generate an example output for a format pattern.
     *
     * @param formatPattern The format pattern to demonstrate
     * @return Example output showing what the pattern produces
     */
    fun generateExample(formatPattern: String): String {
        return formatPattern
            .replace("{key}", "PROJ-123")
            .replace("{summary}", "Fix login bug")
            .replace("{status}", "Open")
            .replace("{project}", "PROJ")
            .replace("{type}", "Bug")
    }

    /**
     * List of available placeholders for display in UI.
     */
    val availablePlaceholders = listOf(
        "{key}" to "Issue key (e.g., PROJ-123)",
        "{summary}" to "Issue title/summary",
        "{status}" to "Current status",
        "{project}" to "Project key",
        "{type}" to "Issue type"
    )
}
