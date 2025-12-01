package de.progeek.kimai.shared.core.jira.client

import de.progeek.kimai.shared.core.jira.client.models.JiraIssueResponse
import de.progeek.kimai.shared.core.jira.client.models.JiraProjectResponse
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import de.progeek.kimai.shared.core.jira.models.JiraProject
import kotlinx.datetime.Instant

/**
 * Maps Jira API response models to domain models.
 */
internal object JiraResponseMapper {

    /**
     * Maps Jira API project response to domain model.
     */
    fun mapProject(response: JiraProjectResponse): JiraProject {
        return JiraProject(
            key = response.key,
            name = response.name,
            description = response.description
        )
    }

    /**
     * Maps Jira API issue response to domain model.
     */
    fun mapIssue(response: JiraIssueResponse): JiraIssue {
        return JiraIssue(
            key = response.key,
            summary = response.fields.summary,
            status = response.fields.status?.name ?: "Unknown",
            projectKey = response.fields.project?.key ?: "",
            projectName = response.fields.project?.name ?: "",
            issueType = response.fields.issuetype?.name ?: "Task",
            assignee = response.fields.assignee?.displayName,
            updated = response.fields.updated?.let { parseInstant(it) }
                ?: Instant.fromEpochMilliseconds(0)
        )
    }

    /**
     * Safely parses ISO 8601 timestamp string to Instant.
     */
    private fun parseInstant(timestamp: String): Instant {
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            Instant.fromEpochMilliseconds(0)
        }
    }
}
