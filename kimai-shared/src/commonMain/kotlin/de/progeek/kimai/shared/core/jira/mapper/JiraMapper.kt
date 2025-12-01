package de.progeek.kimai.shared.core.jira.mapper

import de.progeek.kimai.shared.JiraIssueEntity
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import kotlinx.datetime.Instant

/**
 * Mapper functions for converting between Jira data models.
 *
 * Handles transformations between:
 * - JiraIssue (domain model)
 * - JiraIssueEntity (database model)
 * - kotlin-jira-api models (when integrated)
 */

/**
 * Convert JiraIssue domain model to JiraIssueEntity for database storage.
 */
fun JiraIssue.toJiraIssueEntity(): JiraIssueEntity {
    return JiraIssueEntity(
        key = this.key,
        summary = this.summary,
        status = this.status,
        projectKey = this.projectKey,
        projectName = this.projectName,
        issueType = this.issueType,
        assignee = this.assignee,
        updated = this.updated.toEpochMilliseconds()
    )
}

/**
 * Convert JiraIssueEntity from database to JiraIssue domain model.
 */
fun JiraIssueEntity.toJiraIssue(): JiraIssue {
    return JiraIssue(
        key = this.key,
        summary = this.summary,
        status = this.status,
        projectKey = this.projectKey,
        projectName = this.projectName,
        issueType = this.issueType,
        assignee = this.assignee,
        updated = Instant.fromEpochMilliseconds(this.updated)
    )
}

/**
 * Convert list of JiraIssues to list of JiraIssueEntities.
 */
fun List<JiraIssue>.toJiraIssueEntities(): List<JiraIssueEntity> {
    return this.map { it.toJiraIssueEntity() }
}

/**
 * Convert list of JiraIssueEntities to list of JiraIssues.
 */
fun List<JiraIssueEntity>.toJiraIssues(): List<JiraIssue> {
    return this.map { it.toJiraIssue() }
}