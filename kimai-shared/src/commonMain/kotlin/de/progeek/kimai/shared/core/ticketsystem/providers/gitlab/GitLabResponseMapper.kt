package de.progeek.kimai.shared.core.ticketsystem.providers.gitlab

import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlinx.datetime.Instant

/**
 * Maps GitLab API response models to unified ticket models.
 */
internal object GitLabResponseMapper {

    /**
     * Maps GitLab project to unified TicketProject.
     */
    fun mapProject(response: GitLabProjectResponse): TicketProject {
        return TicketProject(
            key = response.pathWithNamespace,
            name = response.name,
            description = response.description
        )
    }

    /**
     * Maps GitLab issue to unified TicketIssue.
     */
    fun mapIssue(
        response: GitLabIssueResponse,
        config: TicketSystemConfig,
        projectPath: String
    ): TicketIssue {
        val issueType = deriveIssueType(response.labels)
        val assignee = response.assignee?.username
            ?: response.assignees.firstOrNull()?.username

        return TicketIssue(
            id = response.id.toString(),
            key = "#${response.iid}",
            summary = response.title,
            status = mapState(response.state),
            projectKey = projectPath,
            projectName = projectPath.substringAfterLast("/"),
            issueType = issueType,
            assignee = assignee,
            updated = parseInstant(response.updatedAt),
            sourceId = config.id,
            provider = TicketProvider.GITLAB,
            webUrl = response.webUrl
        )
    }

    /**
     * Derive issue type from labels.
     */
    private fun deriveIssueType(labels: List<String>): String {
        val lowerLabels = labels.map { it.lowercase() }
        return when {
            lowerLabels.any { it.contains("bug") } -> "Bug"
            lowerLabels.any { it.contains("feature") } -> "Feature"
            lowerLabels.any { it.contains("enhancement") } -> "Enhancement"
            lowerLabels.any { it.contains("documentation") } -> "Documentation"
            lowerLabels.any { it.contains("incident") } -> "Incident"
            else -> "Issue"
        }
    }

    /**
     * Map GitLab state to display status.
     */
    private fun mapState(state: String): String {
        return when (state.lowercase()) {
            "opened" -> "Open"
            "closed" -> "Closed"
            "merged" -> "Merged"
            "locked" -> "Locked"
            else -> state.replaceFirstChar { it.uppercase() }
        }
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
