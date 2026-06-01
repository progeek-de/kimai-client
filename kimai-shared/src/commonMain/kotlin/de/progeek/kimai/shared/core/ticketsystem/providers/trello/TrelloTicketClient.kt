package de.progeek.kimai.shared.core.ticketsystem.providers.trello

import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * HTTP client for the Trello REST API v1.
 *
 * Authentication is performed via the `key` and `token` query parameters appended
 * to every request.
 */
internal class TrelloTicketClient {

    companion object {
        private const val TAG = "TrelloTicketClient"
        private const val REQUEST_TIMEOUT_MS = 30_000L
        private const val CONNECT_TIMEOUT_MS = 10_000L
        private const val CARD_FIELDS = "idShort,name,shortUrl,idBoard,idList,dateLastActivity,labels,idMembers"
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
        }
    }

    /**
     * Test connection to Trello.
     */
    suspend fun testConnection(config: TicketSystemConfig): Result<String> {
        Napier.d(tag = TAG) { "Testing connection to Trello" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/members/me")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
            }

            handleResponse(response) {
                val member: TrelloMemberResponse = it.body()
                Napier.i(tag = TAG) { "Connection successful: ${member.username}" }
                "Connected as ${member.fullName ?: member.username}"
            }
        }
    }

    /**
     * Get current user identifier.
     */
    suspend fun getCurrentUser(config: TicketSystemConfig): Result<String> {
        return executeRequest(config) {
            val url = buildApiUrl(config, "/members/me")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
            }

            handleResponse(response) {
                val member: TrelloMemberResponse = it.body()
                member.username
            }
        }
    }

    /**
     * Get the boards the authenticated member belongs to.
     */
    suspend fun getBoards(config: TicketSystemConfig): Result<List<TicketProject>> {
        Napier.d(tag = TAG) { "Fetching boards" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/members/me/boards")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                parameter("fields", "id,name,desc")
            }

            handleResponse(response) {
                val boards: List<TrelloBoardResponse> = it.body()
                Napier.i(tag = TAG) { "Fetched ${boards.size} boards" }
                boards.map(TrelloResponseMapper::mapBoard)
            }
        }
    }

    /**
     * Get cards for a specific board, including their list (= status).
     */
    suspend fun getCards(
        config: TicketSystemConfig,
        boardId: String,
        boardName: String?,
        maxResults: Int = 50
    ): Result<List<TicketIssue>> {
        Napier.d(tag = TAG) { "Fetching cards for board $boardId" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/boards/$boardId/cards")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                parameter("fields", CARD_FIELDS)
                parameter("list", true)
                parameter("limit", maxResults)
            }

            handleResponse(response) {
                val cards: List<TrelloCardResponse> = it.body()
                Napier.i(tag = TAG) { "Fetched ${cards.size} cards" }
                cards.map { card ->
                    TrelloResponseMapper.mapCard(card, config, boardName, card.list?.name)
                }
            }
        }
    }

    /**
     * Search for cards across all accessible boards.
     */
    suspend fun searchCards(
        config: TicketSystemConfig,
        query: String,
        maxResults: Int = 50
    ): Result<List<TicketIssue>> {
        Napier.d(tag = TAG) { "Searching cards: q=\"$query\"" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/search")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                parameter("query", query)
                parameter("modelTypes", "cards")
                parameter("partial", true)
                parameter("cards_limit", maxResults)
                parameter("card_fields", CARD_FIELDS)
                parameter("card_list", true)
                parameter("card_board", true)
            }

            handleResponse(response) {
                val searchResponse: TrelloSearchResponse = it.body()
                Napier.i(tag = TAG) { "Search returned ${searchResponse.cards.size} cards" }
                searchResponse.cards.map { card ->
                    TrelloResponseMapper.mapCard(card, config, card.board?.name, card.list?.name)
                }
            }
        }
    }

    private suspend fun <T> executeRequest(
        config: TicketSystemConfig,
        block: suspend () -> T
    ): Result<T> {
        return runCatching {
            block()
        }.onFailure { error ->
            Napier.e(tag = TAG, throwable = error) { "Request failed: ${error.message}" }
        }
    }

    private suspend fun <T> handleResponse(
        response: HttpResponse,
        onSuccess: suspend (HttpResponse) -> T
    ): T {
        return if (response.status.isSuccess()) {
            onSuccess(response)
        } else {
            val errorBody = try {
                response.bodyAsText()
            } catch (_: Exception) {
                "Unknown error"
            }
            Napier.e(tag = TAG) { "API error ${response.status}: $errorBody" }
            throw IllegalStateException("Trello API error (${response.status}): $errorBody")
        }
    }

    private fun HttpRequestBuilder.configureAuth(credentials: TicketCredentials) {
        when (credentials) {
            is TicketCredentials.TrelloToken -> {
                parameter("key", credentials.apiKey)
                parameter("token", credentials.token)
            }
            else -> throw IllegalArgumentException("Invalid credentials type for Trello")
        }
    }

    private fun buildApiUrl(config: TicketSystemConfig, path: String): String {
        val baseUrl = config.baseUrl.trimEnd('/')
        return "$baseUrl$path"
    }
}
