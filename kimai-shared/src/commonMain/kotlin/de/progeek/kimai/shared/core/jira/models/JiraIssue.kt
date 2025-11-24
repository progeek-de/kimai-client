package de.progeek.kimai.shared.core.jira.models

import kotlinx.datetime.Instant

/**
 * Domain model representing a Jira issue.
 *
 * @property key Issue key (e.g., "PROJ-123")
 * @property summary Issue summary/title
 * @property status Current status (e.g., "In Progress", "Done")
 * @property projectKey Project key (e.g., "PROJ")
 * @property projectName Project name
 * @property issueType Type of issue (e.g., "Story", "Bug", "Task")
 * @property assignee Assignee email/username (nullable)
 * @property updated Last updated timestamp
 */
data class JiraIssue(
    val key: String,
    val summary: String,
    val status: String,
    val projectKey: String,
    val projectName: String,
    val issueType: String,
    val assignee: String?,
    val updated: Instant
)
