@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.ticketsystem.providers.trello

import de.progeek.kimai.shared.core.ticketsystem.api.TicketSystemProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig

/**
 * Trello implementation of TicketSystemProvider.
 *
 * Cards are mapped to issues, the card's list to the status, and the board to the project.
 */
class TrelloTicketProvider : TicketSystemProvider {

    override val providerType: TicketProvider = TicketProvider.TRELLO

    private val client = TrelloTicketClient()

    override suspend fun testConnection(config: TicketSystemConfig): Result<String> {
        validateConfig(config)
        return client.testConnection(config)
    }

    override suspend fun searchIssues(
        config: TicketSystemConfig,
        query: String,
        maxResults: Int
    ): Result<List<TicketIssue>> {
        validateConfig(config)

        val credentials = config.credentials as TicketCredentials.TrelloToken

        return when {
            // Specific boards configured: fetch their cards directly.
            credentials.boardIds.isNotEmpty() ->
                fetchFromConfiguredBoards(config, credentials, query, maxResults)
            // No query: the Trello search API cannot list "all" cards, so enumerate
            // every board the member belongs to and collect their cards instead.
            query.isBlank() ->
                fetchFromAllBoards(config, maxResults)
            // Free-text query across all accessible boards.
            else ->
                client.searchCards(config, query, maxResults)
        }
    }

    private suspend fun fetchFromAllBoards(
        config: TicketSystemConfig,
        maxResults: Int
    ): Result<List<TicketIssue>> {
        val boards = client.getBoards(config).getOrElse { return Result.failure(it) }

        val allIssues = mutableListOf<TicketIssue>()
        for (board in boards) {
            client.getCards(config, board.key, board.name, maxResults)
                .getOrNull()
                ?.let { allIssues.addAll(it) }
        }

        return Result.success(
            allIssues
                .distinctBy { it.id }
                .sortedByDescending { it.updated }
                .take(maxResults)
        )
    }

    private suspend fun fetchFromConfiguredBoards(
        config: TicketSystemConfig,
        credentials: TicketCredentials.TrelloToken,
        query: String,
        maxResults: Int
    ): Result<List<TicketIssue>> {
        val boardNames = client.getBoards(config).getOrNull()
            ?.associate { it.key to it.name }
            ?: emptyMap()

        val allIssues = mutableListOf<TicketIssue>()
        for (boardId in credentials.boardIds) {
            client.getCards(config, boardId, boardNames[boardId], maxResults)
                .getOrNull()
                ?.let { allIssues.addAll(it) }
        }

        val filtered = if (query.isBlank()) {
            allIssues
        } else {
            allIssues.filter { it.summary.contains(query, ignoreCase = true) }
        }

        return Result.success(
            filtered
                .distinctBy { it.id }
                .sortedByDescending { it.updated }
                .take(maxResults)
        )
    }

    override suspend fun getIssueByKey(
        config: TicketSystemConfig,
        key: String
    ): Result<TicketIssue?> {
        validateConfig(config)

        // Key format is "#3537" - extract the card's short id (unique per board).
        val number = key.removePrefix("#").trim()
        if (number.toIntOrNull() == null) return Result.success(null)

        // A single search request: Trello's search matches the card short id.
        return client.searchCards(config, number, MAX_LOOKUP_RESULTS).map { issues ->
            issues.firstOrNull { it.key.equals("#$number", ignoreCase = true) }
        }
    }

    override suspend fun getProjects(config: TicketSystemConfig): Result<List<TicketProject>> {
        validateConfig(config)
        return client.getBoards(config)
    }

    override suspend fun getCurrentUser(config: TicketSystemConfig): Result<String> {
        validateConfig(config)
        return client.getCurrentUser(config)
    }

    override fun validateCredentials(config: TicketSystemConfig): Boolean {
        return when (val creds = config.credentials) {
            is TicketCredentials.TrelloToken ->
                creds.apiKey.isNotBlank() && creds.token.isNotBlank()
            else -> false
        }
    }

    override fun getErrorMessage(exception: Throwable, config: TicketSystemConfig): String {
        return when {
            exception.message?.contains("401", ignoreCase = true) == true ->
                "Authentication failed. Please check your Trello API key and token."
            exception.message?.contains("403", ignoreCase = true) == true ->
                "Access denied. Your token may not have sufficient permissions."
            exception.message?.contains("404", ignoreCase = true) == true ->
                "Resource not found. Please check the board IDs."
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Connection timed out. Please check your network."
            else -> exception.message ?: "Unknown error occurred"
        }
    }

    private fun validateConfig(config: TicketSystemConfig) {
        require(config.provider == TicketProvider.TRELLO) {
            "Invalid provider type: ${config.provider}"
        }
        require(config.credentials is TicketCredentials.TrelloToken) {
            "Invalid credentials type for Trello provider"
        }
    }

    companion object {
        private const val MAX_LOOKUP_RESULTS = 100
    }
}
