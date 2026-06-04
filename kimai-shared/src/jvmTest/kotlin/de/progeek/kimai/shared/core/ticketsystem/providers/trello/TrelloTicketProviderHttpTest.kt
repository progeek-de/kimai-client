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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the network/orchestration logic of [TrelloTicketProvider].
 *
 * The provider is constructed with a [TrelloTicketClient] wired to a Ktor
 * [MockEngine] so the real request-building/mapping path runs while HTTP traffic
 * is faked. The MockEngine branches on [HttpRequestData.url] (encodedPath) to serve
 * the appropriate fixture for the boards, board-cards and search endpoints.
 *
 * Uses [runBlocking] (not runTest) because the production client installs the
 * HttpTimeout plugin whose timeout-killer relies on real delays; runTest's
 * virtual clock would fire the timeout immediately.
 *
 * The validation/credential/error-message behaviour is covered by the commonTest
 * TrelloTicketProviderTest and is intentionally not duplicated here.
 */
class TrelloTicketProviderHttpTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun config(
        boardIds: List<String> = emptyList()
    ) = TicketSystemConfig(
        id = "cfg-1",
        displayName = "Trello",
        provider = TicketProvider.TRELLO,
        enabled = true,
        baseUrl = "https://api.trello.com/1",
        credentials = TicketCredentials.TrelloToken(
            apiKey = "trello_key",
            token = "trello_token",
            boardIds = boardIds
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private val memberJson = """{"id":"m1","username":"octo","fullName":"The Octo","email":"o@x.io"}"""
    private val boardsJson =
        """[{"id":"b1","name":"Board One","desc":"first board"},{"id":"b2","name":"Board Two"}]"""

    /**
     * Builds a card object. [activity] drives `dateLastActivity` (and therefore the
     * mapped `updated` instant) so ordering/dedup can be asserted deterministically.
     */
    private fun cardJson(
        id: String = "c1",
        idShort: Int = 7,
        name: String = "Fix login",
        activity: String = "2024-01-01T00:00:00.000Z",
        board: Boolean = false,
        list: Boolean = true
    ): String {
        val listPart = if (list) ""","list":{"id":"l1","name":"In Progress"}""" else ""
        val boardPart = if (board) ""","board":{"id":"b1","name":"Board One","desc":"d"}""" else ""
        return """{"id":"$id","idShort":$idShort,"name":"$name","shortUrl":"https://trello.com/c/$id",""" +
            """"idBoard":"b1","idList":"l1","dateLastActivity":"$activity",""" +
            """"labels":[{"name":"Bug","color":"red"}],"idMembers":["m1"]$listPart$boardPart}"""
    }

    /**
     * Constructs a provider backed by a MockEngine that dispatches on the request path.
     * [onRequest] receives every request for assertions on params/paths.
     */
    private fun provider(
        onRequest: (HttpRequestData) -> Unit = {},
        responder: (HttpRequestData) -> Pair<HttpStatusCode, String>
    ): TrelloTicketProvider {
        val engine = MockEngine { request ->
            onRequest(request)
            val (status, body) = responder(request)
            respond(content = body, status = status, headers = jsonHeaders)
        }
        return TrelloTicketProvider(TrelloTicketClient(engine))
    }

    // ============================================================
    // testConnection / getCurrentUser / getProjects delegation
    // ============================================================

    @Test
    fun `testConnection delegates to client and returns greeting`() = runBlocking {
        val result = provider { HttpStatusCode.OK to memberJson }.testConnection(config())
        assertEquals("Connected as The Octo", result.getOrThrow())
    }

    @Test
    fun `testConnection propagates client failure`() = runBlocking {
        val result = provider { HttpStatusCode.Unauthorized to "bad creds" }.testConnection(config())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("401") == true)
    }

    @Test
    fun `getCurrentUser delegates to client and returns username`() = runBlocking {
        val result = provider { HttpStatusCode.OK to memberJson }.getCurrentUser(config())
        assertEquals("octo", result.getOrThrow())
    }

    @Test
    fun `getProjects delegates to getBoards and maps boards`() = runBlocking {
        val projects = provider { HttpStatusCode.OK to boardsJson }.getProjects(config()).getOrThrow()
        assertEquals(2, projects.size)
        assertEquals("b1", projects.first().key)
        assertEquals("Board One", projects.first().name)
    }

    @Test
    fun `getProjects propagates client failure`() = runBlocking {
        val result = provider { HttpStatusCode.InternalServerError to "boom" }.getProjects(config())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("500") == true)
    }

    // ============================================================
    // searchIssues - free-text branch (no boards, non-blank query)
    // ============================================================

    @Test
    fun `searchIssues with query and no boards uses the search endpoint`() = runBlocking {
        var path: String? = null
        var query: String? = null
        val result = provider(
            onRequest = { req ->
                path = req.url.encodedPath
                query = req.url.parameters["query"]
            }
        ) { HttpStatusCode.OK to """{"cards":[${cardJson(board = true)}]}""" }
            .searchIssues(config(), "login", maxResults = 50)

        val issues = result.getOrThrow()
        assertEquals("/1/search", path)
        assertEquals("login", query)
        assertEquals(1, issues.size)
        assertEquals("Board One", issues.first().projectName)
    }

    @Test
    fun `searchIssues free-text branch propagates client failure`() = runBlocking {
        val result = provider { HttpStatusCode.InternalServerError to "boom" }
            .searchIssues(config(), "crash", maxResults = 50)
        assertTrue(result.isFailure)
    }

    // ============================================================
    // searchIssues - all-boards branch (no boards, blank query)
    // ============================================================

    @Test
    fun `searchIssues with blank query enumerates all boards and collects cards`() = runBlocking {
        val paths = mutableListOf<String>()
        val result = provider(
            onRequest = { req -> paths.add(req.url.encodedPath) }
        ) { req ->
            when (req.url.encodedPath) {
                "/1/members/me/boards" -> HttpStatusCode.OK to boardsJson
                "/1/boards/b1/cards" -> HttpStatusCode.OK to "[${cardJson(id = "c1")}]"
                "/1/boards/b2/cards" -> HttpStatusCode.OK to "[${cardJson(id = "c2", idShort = 9)}]"
                else -> HttpStatusCode.NotFound to "unexpected ${req.url.encodedPath}"
            }
        }.searchIssues(config(), "", maxResults = 50)

        val issues = result.getOrThrow()
        // One boards request + one cards request per board.
        assertTrue(paths.contains("/1/members/me/boards"))
        assertTrue(paths.contains("/1/boards/b1/cards"))
        assertTrue(paths.contains("/1/boards/b2/cards"))
        assertEquals(2, issues.size)
        assertEquals(setOf("c1", "c2"), issues.map { it.id }.toSet())
    }

    @Test
    fun `searchIssues all-boards branch fails when boards request fails`() = runBlocking {
        val result = provider { req ->
            when (req.url.encodedPath) {
                "/1/members/me/boards" -> HttpStatusCode.Unauthorized to "nope"
                else -> HttpStatusCode.OK to "[]"
            }
        }.searchIssues(config(), "", maxResults = 50)
        assertTrue(result.isFailure)
    }

    @Test
    fun `searchIssues all-boards branch skips boards whose cards fail`() = runBlocking {
        val result = provider { req ->
            when (req.url.encodedPath) {
                "/1/members/me/boards" -> HttpStatusCode.OK to boardsJson
                "/1/boards/b1/cards" -> HttpStatusCode.OK to "[${cardJson(id = "c1")}]"
                // b2 cards fail -> getOrNull() drops it, board b1 still contributes.
                "/1/boards/b2/cards" -> HttpStatusCode.InternalServerError to "boom"
                else -> HttpStatusCode.NotFound to "unexpected"
            }
        }.searchIssues(config(), "", maxResults = 50)

        val issues = result.getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("c1", issues.first().id)
    }

    @Test
    fun `searchIssues all-boards branch dedups by id and sorts by updated descending`() = runBlocking {
        val result = provider { req ->
            when (req.url.encodedPath) {
                "/1/members/me/boards" -> HttpStatusCode.OK to boardsJson
                // b1 yields the older card; b2 yields a duplicate id plus a newer card.
                "/1/boards/b1/cards" ->
                    HttpStatusCode.OK to "[${cardJson(id = "old", activity = "2024-01-01T00:00:00.000Z")}]"
                "/1/boards/b2/cards" ->
                    HttpStatusCode.OK to "[" +
                        cardJson(id = "old", activity = "2024-01-01T00:00:00.000Z") + "," +
                        cardJson(id = "new", activity = "2024-06-01T00:00:00.000Z") + "]"
                else -> HttpStatusCode.NotFound to "unexpected"
            }
        }.searchIssues(config(), "", maxResults = 50)

        val issues = result.getOrThrow()
        // "old" appears twice but is deduped to a single entry.
        assertEquals(2, issues.size)
        // Newest first.
        assertEquals("new", issues.first().id)
        assertEquals("old", issues.last().id)
    }

    @Test
    fun `searchIssues all-boards branch caps results at maxResults`() = runBlocking {
        val result = provider { req ->
            when (req.url.encodedPath) {
                "/1/members/me/boards" -> HttpStatusCode.OK to boardsJson
                "/1/boards/b1/cards" ->
                    HttpStatusCode.OK to "[${cardJson(id = "c1")},${cardJson(id = "c2", idShort = 2)}]"
                "/1/boards/b2/cards" ->
                    HttpStatusCode.OK to "[${cardJson(id = "c3", idShort = 3)}]"
                else -> HttpStatusCode.NotFound to "unexpected"
            }
        }.searchIssues(config(), "", maxResults = 2)

        assertEquals(2, result.getOrThrow().size)
    }

    // ============================================================
    // searchIssues - configured-boards branch
    // ============================================================

    @Test
    fun `searchIssues with configured boards fetches only those boards`() = runBlocking {
        val cardPaths = mutableListOf<String>()
        val result = provider(
            onRequest = { req ->
                if (req.url.encodedPath.endsWith("/cards")) cardPaths.add(req.url.encodedPath)
            }
        ) { req ->
            when (req.url.encodedPath) {
                "/1/members/me/boards" -> HttpStatusCode.OK to boardsJson
                "/1/boards/b1/cards" -> HttpStatusCode.OK to "[${cardJson(id = "c1")}]"
                else -> HttpStatusCode.NotFound to "unexpected ${req.url.encodedPath}"
            }
        }.searchIssues(config(boardIds = listOf("b1")), "", maxResults = 50)

        val issues = result.getOrThrow()
        // Only the configured board (b1) cards endpoint is hit, not b2.
        assertEquals(listOf("/1/boards/b1/cards"), cardPaths)
        assertEquals(1, issues.size)
        // Board name is resolved from the boards listing.
        assertEquals("Board One", issues.first().projectName)
    }

    @Test
    fun `searchIssues with configured boards filters by query on summary`() = runBlocking {
        val result = provider { req ->
            when (req.url.encodedPath) {
                "/1/members/me/boards" -> HttpStatusCode.OK to boardsJson
                "/1/boards/b1/cards" ->
                    HttpStatusCode.OK to "[" +
                        cardJson(id = "c1", name = "Fix login bug") + "," +
                        cardJson(id = "c2", idShort = 2, name = "Update docs") + "]"
                else -> HttpStatusCode.NotFound to "unexpected"
            }
        }.searchIssues(config(boardIds = listOf("b1")), "login", maxResults = 50)

        val issues = result.getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("c1", issues.first().id)
    }

    @Test
    fun `searchIssues with configured boards still works when boards listing fails`() = runBlocking {
        // getBoards failure -> board-name map is empty, but cards are still fetched.
        val result = provider { req ->
            when (req.url.encodedPath) {
                "/1/members/me/boards" -> HttpStatusCode.InternalServerError to "boom"
                "/1/boards/b1/cards" -> HttpStatusCode.OK to "[${cardJson(id = "c1")}]"
                else -> HttpStatusCode.NotFound to "unexpected"
            }
        }.searchIssues(config(boardIds = listOf("b1")), "", maxResults = 50)

        val issues = result.getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("c1", issues.first().id)
    }

    @Test
    fun `searchIssues with configured boards dedups and caps at maxResults`() = runBlocking {
        val result = provider { req ->
            when (req.url.encodedPath) {
                "/1/members/me/boards" -> HttpStatusCode.OK to boardsJson
                "/1/boards/b1/cards" ->
                    HttpStatusCode.OK to "[${cardJson(id = "c1")},${cardJson(id = "c1")}]"
                "/1/boards/b2/cards" ->
                    HttpStatusCode.OK to "[${cardJson(id = "c2", idShort = 2)}]"
                else -> HttpStatusCode.NotFound to "unexpected"
            }
        }.searchIssues(config(boardIds = listOf("b1", "b2")), "", maxResults = 1)

        // c1 deduped, both boards collected, then capped to 1.
        assertEquals(1, result.getOrThrow().size)
    }

    // ============================================================
    // getIssueByKey
    // ============================================================

    @Test
    fun `getIssueByKey returns matching card on success`() = runBlocking {
        val result = provider(
            onRequest = { req -> assertEquals("7", req.url.parameters["query"]) }
        ) { HttpStatusCode.OK to """{"cards":[${cardJson(idShort = 7, board = true)}]}""" }
            .getIssueByKey(config(), "#7")

        val issue = result.getOrThrow()
        assertEquals("#7", issue?.key)
        assertEquals("c1", issue?.id)
    }

    @Test
    fun `getIssueByKey returns success null when no card matches the short id`() = runBlocking {
        // Search returns a card with a different short id -> firstOrNull yields null.
        val result = provider { HttpStatusCode.OK to """{"cards":[${cardJson(idShort = 99, board = true)}]}""" }
            .getIssueByKey(config(), "#7")
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `getIssueByKey returns success null for non-numeric key`() = runBlocking {
        // Invalid (non-numeric) input short-circuits before any HTTP call.
        var called = false
        val result = provider(
            onRequest = { called = true }
        ) { HttpStatusCode.OK to """{"cards":[]}""" }
            .getIssueByKey(config(), "ABC-123")

        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
        assertTrue(!called, "No HTTP request should be made for invalid keys")
    }

    @Test
    fun `getIssueByKey propagates client failure`() = runBlocking {
        val result = provider { HttpStatusCode.InternalServerError to "boom" }
            .getIssueByKey(config(), "#7")
        assertTrue(result.isFailure)
    }
}
