package de.progeek.kimai.shared.core.ticketsystem.providers.github

import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlinx.datetime.Instant

/**
 * Maps GitHub API response models to unified ticket models.
 */
internal object GitHubResponseMapper {

    /**
     * Maps GitHub repository to unified TicketProject.
     */
    fun mapRepository(response: GitHubRepoResponse): TicketProject {
        return TicketProject(
            key = response.fullName,
            name = response.name,
            description = response.description
        )
    }

    /**
     * Maps GitHub issue to unified TicketIssue.
     */
    fun mapIssue(
        response: GitHubIssueResponse,
        config: TicketSystemConfig,
        repoFullName: String
    ): TicketIssue {
        val issueType = deriveIssueType(response.labels)

        return TicketIssue(
            id = response.id.toString(),
            key = "#${response.number}",
            summary = response.title,
            status = mapState(response.state),
            projectKey = repoFullName,
            projectName = repoFullName.substringAfter("/"),
            issueType = issueType,
            assignee = response.assignee?.login,
            updated = parseInstant(response.updatedAt),
            sourceId = config.id,
            provider = TicketProvider.GITHUB,
            webUrl = response.htmlUrl
        )
    }

    /**
     * Derive issue type from labels.
     */
    private fun deriveIssueType(labels: List<GitHubLabel>): String {
        val labelNames = labels.map { it.name.lowercase() }
        return when {
            labelNames.any { it.contains("bug") } -> "Bug"
            labelNames.any { it.contains("feature") } -> "Feature"
            labelNames.any { it.contains("enhancement") } -> "Enhancement"
            labelNames.any { it.contains("documentation") } -> "Documentation"
            labelNames.any { it.contains("question") } -> "Question"
            else -> "Issue"
        }
    }

    /**
     * Map GitHub state to display status.
     */
    private fun mapState(state: String): String {
        return when (state.lowercase()) {
            "open" -> "Open"
            "closed" -> "Closed"
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
