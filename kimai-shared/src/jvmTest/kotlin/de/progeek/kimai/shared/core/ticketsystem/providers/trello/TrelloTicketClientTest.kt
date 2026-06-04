package de.progeek.kimai.shared.core.ticketsystem.providers.trello

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [TrelloTicketClient] using a Ktor [MockEngine].
 * Exercises HTTP request building, response mapping, auth and error handling.
 *
 * Uses [runBlocking] (not runTest) because the production client installs the
 * HttpTimeout plugin whose timeout-killer relies on real delays; runTest's
 * virtual clock would fire the timeout immediately.
 *
 * Trello authenticates via the `key` and `token` query parameters (not headers),
 * so tests assert those parameters are present on outgoing requests.
 */
class TrelloTicketClientTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun config(
        baseUrl: String = "https://api.trello.com/1",
        apiKey: String = "trello_key",
        token: String = "trello_token",
        boardIds: List<String> = emptyList()
    ) = TicketSystemConfig(
        id = "cfg-1",
        displayName = "Trello",
        provider = TicketProvider.TRELLO,
        enabled = true,
        baseUrl = baseUrl,
        credentials = TicketCredentials.TrelloToken(
            apiKey = apiKey,
            token = token,
            boardIds = boardIds
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private fun client(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = "{}",
        onRequest: (HttpRequestData) -> Unit = {}
    ): TrelloTicketClient {
        val engine = MockEngine { request ->
            onRequest(request)
            respond(content = body, status = status, headers = jsonHeaders)
        }
        return TrelloTicketClient(engine)
    }

    private val memberJson = """{"id":"m1","username":"octo","fullName":"The Octo","email":"o@x.io"}"""
    private val boardsJson =
        """[{"id":"b1","name":"Board One","desc":"first board"},{"id":"b2","name":"Board Two"}]"""

    private fun cardJson(
        id: String = "c1",
        idShort: Int = 7,
        board: Boolean = false,
        list: Boolean = true
    ): String {
        val listPart = if (list) ""","list":{"id":"l1","name":"In Progress"}""" else ""
        val boardPart = if (board) ""","board":{"id":"b1","name":"Board One","desc":"d"}""" else ""
        return """{"id":"$id","idShort":$idShort,"name":"Fix login","shortUrl":"https://trello.com/c/abc",""" +
            """"idBoard":"b1","idList":"l1","dateLastActivity":"2024-01-01T00:00:00.000Z",""" +
            """"labels":[{"name":"Bug","color":"red"}],"idMembers":["m1"]$listPart$boardPart}"""
    }

    @Test
    fun `testConnection returns greeting on success`() = runBlocking {
        val result = client(body = memberJson).testConnection(config())
        assertEquals("Connected as The Octo", result.getOrElse { throw it })
    }

    @Test
    fun `testConnection falls back to username when fullName is null`() = runBlocking {
        val result = client(body = """{"id":"m1","username":"octo"}""").testConnection(config())
        assertEquals("Connected as octo", result.getOrNull())
    }

    @Test
    fun `testConnection sends key and token query parameters`() = runBlocking {
        var key: String? = null
        var token: String? = null
        client(body = memberJson) { req ->
            key = req.url.parameters["key"]
            token = req.url.parameters["token"]
        }.testConnection(config())
        assertEquals("trello_key", key)
        assertEquals("trello_token", token)
    }

    @Test
    fun `testConnection returns failure on error status`() = runBlocking {
        val result = client(status = HttpStatusCode.Unauthorized, body = "bad creds").testConnection(config())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("401") == true)
    }

    @Test
    fun `getCurrentUser returns username`() = runBlocking {
        assertEquals("octo", client(body = memberJson).getCurrentUser(config()).getOrNull())
    }

    @Test
    fun `getBoards maps boards on success`() = runBlocking {
        val boards = client(body = boardsJson).getBoards(config()).getOrThrow()
        assertEquals(2, boards.size)
        assertEquals("b1", boards.first().key)
        assertEquals("Board One", boards.first().name)
        assertEquals("first board", boards.first().description)
    }

    @Test
    fun `getBoards drops blank description`() = runBlocking {
        val boards = client(body = boardsJson).getBoards(config()).getOrThrow()
        assertEquals(null, boards[1].description)
    }

    @Test
    fun `getBoards requests id name desc fields with auth`() = runBlocking {
        var fields: String? = null
        var key: String? = null
        client(body = boardsJson) { req ->
            fields = req.url.parameters["fields"]
            key = req.url.parameters["key"]
        }.getBoards(config())
        assertEquals("id,name,desc", fields)
        assertEquals("trello_key", key)
    }

    @Test
    fun `getCards maps cards on success`() = runBlocking {
        val issues = client(body = "[${cardJson()}]").getCards(config(), "b1", "Board One").getOrThrow()
        assertEquals(1, issues.size)
        val issue = issues.first()
        assertEquals("c1", issue.id)
        assertEquals("#7", issue.key)
        assertEquals("Fix login", issue.summary)
        assertEquals("In Progress", issue.status)
        assertEquals("b1", issue.projectKey)
        assertEquals("Board One", issue.projectName)
        assertEquals("Bug", issue.issueType)
        assertEquals("https://trello.com/c/abc", issue.webUrl)
        assertEquals(TicketProvider.TRELLO, issue.provider)
        assertEquals("cfg-1", issue.sourceId)
    }

    @Test
    fun `getCards targets the board cards endpoint`() = runBlocking {
        var path: String? = null
        client(body = "[]") { req -> path = req.url.encodedPath }
            .getCards(config(), "b1", "Board One")
        assertEquals("/1/boards/b1/cards", path)
    }

    @Test
    fun `getCards passes the requested limit`() = runBlocking {
        var limit: String? = null
        client(body = "[]") { req -> limit = req.url.parameters["limit"] }
            .getCards(config(), "b1", "Board One", maxResults = 25)
        assertEquals("25", limit)
    }

    @Test
    fun `searchCards maps cards and resolves embedded board`() = runBlocking {
        val body = """{"cards":[${cardJson(board = true)}]}"""
        val issues = client(body = body).searchCards(config(), "login").getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("Board One", issues.first().projectName)
        assertEquals("In Progress", issues.first().status)
    }

    @Test
    fun `searchCards sends query and card params with auth`() = runBlocking {
        var query: String? = null
        var modelTypes: String? = null
        var token: String? = null
        client(body = """{"cards":[]}""") { req ->
            query = req.url.parameters["query"]
            modelTypes = req.url.parameters["modelTypes"]
            token = req.url.parameters["token"]
        }.searchCards(config(), "crash")
        assertEquals("crash", query)
        assertEquals("cards", modelTypes)
        assertEquals("trello_token", token)
    }

    @Test
    fun `searchCards returns failure on error status`() = runBlocking {
        val result = client(status = HttpStatusCode.InternalServerError, body = "boom")
            .searchCards(config(), "x")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("500") == true)
    }

    @Test
    fun `getCards falls back to embedded list and board names when args are null`() = runBlocking {
        val issues = client(body = "[${cardJson(board = true)}]")
            .getCards(config(), "b1", boardName = null).getOrThrow()
        assertEquals("Board One", issues.first().projectName)
        assertEquals("In Progress", issues.first().status)
    }

    @Test
    fun `baseUrl trailing slash is trimmed`() = runBlocking {
        var path: String? = null
        client(body = memberJson) { req -> path = req.url.encodedPath }
            .testConnection(config(baseUrl = "https://api.trello.com/1/"))
        assertEquals("/1/members/me", path)
    }
}
