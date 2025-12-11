@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.ticketsystem.models

import kotlin.time.Instant

/**
 * Unified issue model representing a ticket from any supported provider.
 * Contains common fields that exist across Jira, GitHub, and GitLab.
 */
data class TicketIssue(
    /**
     * Provider-specific unique identifier.
     * - Jira: Issue key (e.g., "PROJ-123")
     * - GitHub: Issue number (e.g., "456")
     * - GitLab: Issue IID (e.g., "789")
     */
    val id: String,

    /**
     * Human-readable key/identifier for display.
     * - Jira: "PROJ-123"
     * - GitHub: "#456"
     * - GitLab: "#789"
     */
    val key: String,

    /**
     * Issue title/summary.
     */
    val summary: String,

    /**
     * Current status (e.g., "Open", "In Progress", "Done").
     */
    val status: String,

    /**
     * Project/repository key or identifier.
     * - Jira: Project key (e.g., "PROJ")
     * - GitHub: Repository name (e.g., "owner/repo")
     * - GitLab: Project path (e.g., "group/project")
     */
    val projectKey: String,

    /**
     * Human-readable project/repository name.
     */
    val projectName: String,

    /**
     * Issue type (e.g., "Bug", "Feature", "Task").
     * For GitHub/GitLab, this may be derived from labels.
     */
    val issueType: String,

    /**
     * Assignee username/display name (null if unassigned).
     */
    val assignee: String? = null,

    /**
     * Last update timestamp.
     */
    val updated: Instant,

    /**
     * UUID of the TicketSystemConfig this issue belongs to.
     * Used to identify which configured source this issue came from.
     */
    val sourceId: String,

    /**
     * Provider type for display purposes and styling.
     */
    val provider: TicketProvider,

    /**
     * Direct web URL to the issue (for opening in browser).
     */
    val webUrl: String? = null
) {
    /**
     * Format the issue for insertion into text fields using a format pattern.
     */
    fun format(formatPattern: String): String = IssueInsertFormat.format(this, formatPattern)
}
