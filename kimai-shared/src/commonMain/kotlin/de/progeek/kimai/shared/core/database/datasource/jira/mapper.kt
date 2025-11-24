package de.progeek.kimai.shared.core.database.datasource.jira

import de.progeek.kimai.shared.core.jira.models.JiraIssue
import kotlinx.datetime.Instant

/**
 * Map JiraIssueEntity query result to JiraIssue domain model.
 *
 * This mapper is used by SQLDelight queries to transform database rows
 * into domain objects.
 */
internal inline fun toJiraIssue(
    key: String,
    summary: String,
    status: String,
    projectKey: String,
    projectName: String,
    issueType: String,
    assignee: String?,
    updated: Long
): JiraIssue {
    return JiraIssue(
        key = key,
        summary = summary,
        status = status,
        projectKey = projectKey,
        projectName = projectName,
        issueType = issueType,
        assignee = assignee,
        updated = Instant.fromEpochMilliseconds(updated)
    )
}
