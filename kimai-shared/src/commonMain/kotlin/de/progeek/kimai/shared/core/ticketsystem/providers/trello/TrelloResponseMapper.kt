@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.ticketsystem.providers.trello

import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlin.time.Instant

/**
 * Maps Trello API response models to unified ticket models.
 */
internal object TrelloResponseMapper {

    /**
     * Maps a Trello board to a unified TicketProject.
     */
    fun mapBoard(response: TrelloBoardResponse): TicketProject {
        return TicketProject(
            key = response.id,
            name = response.name,
            description = response.desc?.takeIf { it.isNotBlank() }
        )
    }

    /**
     * Maps a Trello card to a unified TicketIssue.
     *
     * @param boardName Resolved board name (falls back to the card's embedded board).
     * @param listName Resolved list name used as the issue status.
     */
    fun mapCard(
        response: TrelloCardResponse,
        config: TicketSystemConfig,
        boardName: String?,
        listName: String?
    ): TicketIssue {
        val resolvedListName = listName ?: response.list?.name ?: "Unknown"
        val resolvedBoardName = boardName ?: response.board?.name ?: response.idBoard

        return TicketIssue(
            id = response.id,
            key = "#${response.idShort}",
            summary = response.name,
            status = resolvedListName,
            projectKey = response.idBoard,
            projectName = resolvedBoardName,
            issueType = deriveIssueType(response.labels),
            assignee = null,
            updated = parseInstant(response.dateLastActivity),
            sourceId = config.id,
            provider = TicketProvider.TRELLO,
            webUrl = response.shortUrl
        )
    }

    /**
     * Derive an issue type from the card's labels, defaulting to "Card".
     */
    private fun deriveIssueType(labels: List<TrelloLabel>): String {
        return labels.firstOrNull { !it.name.isNullOrBlank() }?.name ?: "Card"
    }

    /**
     * Safely parses an ISO 8601 timestamp string to Instant.
     */
    private fun parseInstant(timestamp: String?): Instant {
        if (timestamp.isNullOrBlank()) return Instant.fromEpochMilliseconds(0)
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            Instant.fromEpochMilliseconds(0)
        }
    }
}
