package de.progeek.kimai.shared.core.ticketsystem.providers.jira

import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlinx.datetime.Instant

/**
 * Maps Jira API response models to unified ticket models.
 */
internal object JiraResponseMapper {

    /**
     * Maps Jira API project response to unified TicketProject.
     */
    fun mapProject(response: JiraProjectResponse): TicketProject {
        return TicketProject(
            key = response.key,
            name = response.name,
            description = response.description
        )
    }

    /**
     * Maps Jira API issue response to unified TicketIssue.
     */
    fun mapIssue(response: JiraIssueResponse, config: TicketSystemConfig): TicketIssue {
        return TicketIssue(
            id = response.id,
            key = response.key,
            summary = response.fields.summary,
            status = response.fields.status?.name ?: "Unknown",
            projectKey = response.fields.project?.key ?: "",
            projectName = response.fields.project?.name ?: "",
            issueType = response.fields.issuetype?.name ?: "Task",
            assignee = response.fields.assignee?.displayName,
            updated = response.fields.updated?.let { parseInstant(it) }
                ?: Instant.fromEpochMilliseconds(0),
            sourceId = config.id,
            provider = TicketProvider.JIRA,
            webUrl = buildIssueUrl(config.baseUrl, response.key)
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

    /**
     * Build the web URL for a Jira issue.
     */
    private fun buildIssueUrl(baseUrl: String, issueKey: String): String {
        val normalizedBaseUrl = baseUrl.trimEnd('/')
        return "$normalizedBaseUrl/browse/$issueKey"
    }
}
