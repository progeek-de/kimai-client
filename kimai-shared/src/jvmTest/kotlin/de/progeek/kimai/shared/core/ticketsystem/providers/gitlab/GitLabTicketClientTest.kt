package de.progeek.kimai.shared.core.ticketsystem.providers.gitlab

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
 * Tests for [GitLabTicketClient] using a Ktor [MockEngine].
 * Exercises HTTP request building, response mapping, auth and error handling.
 *
 * Uses [runBlocking] (not runTest) because the production client installs the
 * HttpTimeout plugin whose timeout-killer relies on real delays; runTest's
 * virtual clock would fire the timeout immediately.
 */
class GitLabTicketClientTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun config(
        baseUrl: String = "https://gitlab.com",
        projectIds: List<String> = emptyList()
    ) = TicketSystemConfig(
        id = "cfg-1",
        displayName = "GitLab",
        provider = TicketProvider.GITLAB,
        enabled = true,
        baseUrl = baseUrl,
        credentials = TicketCredentials.GitLabToken(
            token = "glpat_token",
            projectIds = projectIds
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private fun client(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = "{}",
        onRequest: (HttpRequestData) -> Unit = {}
    ): GitLabTicketClient {
        val engine = MockEngine { request ->
            onRequest(request)
            respond(content = body, status = status, headers = jsonHeaders)
        }
        return GitLabTicketClient(engine)
    }

    private val userJson = """{"id":1,"username":"octocat","name":"The Octocat"}"""
    private val projectJson =
        """[{"id":10,"name":"hello","path_with_namespace":"group/hello",""" +
            """"description":"d","web_url":"https://gitlab.com/group/hello"}]"""

    private fun issueJson(iid: Int = 7) =
        """{"id":100,"iid":$iid,"title":"Bug","description":"desc","state":"opened",""" +
            """"labels":["bug"],"web_url":"https://gitlab.com/group/hello/-/issues/$iid",""" +
            """"updated_at":"2024-01-01T00:00:00Z","project_id":10}"""

    @Test
    fun `testConnection returns greeting on success`() = runBlocking {
        val result = client(body = userJson).testConnection(config())
        assertEquals("Connected as The Octocat (@octocat)", result.getOrElse { throw it })
    }

    @Test
    fun `testConnection sends private-token auth header`() = runBlocking {
        var token: String? = null
        client(body = userJson) { req -> token = req.headers["PRIVATE-TOKEN"] }
            .testConnection(config())
        assertEquals("glpat_token", token)
    }

    @Test
    fun `testConnection builds url with api v4 prefix`() = runBlocking {
        var path: String? = null
        client(body = userJson) { req -> path = req.url.encodedPath }
            .testConnection(config())
        assertEquals("/api/v4/user", path)
    }

    @Test
    fun `testConnection returns failure on error status`() = runBlocking {
        val result = client(status = HttpStatusCode.Unauthorized, body = "bad creds").testConnection(config())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("401") == true)
    }

    @Test
    fun `getProjects maps projects on success`() = runBlocking {
        val projects = client(body = projectJson).getProjects(config()).getOrThrow()
        assertEquals(1, projects.size)
        assertEquals("group/hello", projects.first().key)
    }

    @Test
    fun `getProjects sends membership and paging parameters`() = runBlocking {
        var membership: String? = null
        var orderBy: String? = null
        client(body = "[]") { req ->
            membership = req.url.parameters["membership"]
            orderBy = req.url.parameters["order_by"]
        }.getProjects(config())
        assertEquals("true", membership)
        assertEquals("last_activity_at", orderBy)
    }

    @Test
    fun `searchIssues uses global search when no projectIds configured`() = runBlocking {
        var path: String? = null
        var search: String? = null
        var scope: String? = null
        client(body = "[${issueJson()}]") { req ->
            path = req.url.encodedPath
            search = req.url.parameters["search"]
            scope = req.url.parameters["scope"]
        }.searchIssues(config(projectIds = emptyList()), query = "crash")
        assertEquals("/api/v4/issues", path)
        assertEquals("all", scope)
        assertEquals("crash", search)
    }

    @Test
    fun `searchIssues global maps issues on success`() = runBlocking {
        val issues = client(body = "[${issueJson()}]")
            .searchIssues(config(projectIds = emptyList())).getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("#7", issues.first().key)
    }

    @Test
    fun `searchIssues fetches from configured projects when projectIds present`() = runBlocking {
        var path: String? = null
        client(body = "[${issueJson()}]") { req -> path = req.url.encodedPath }
            .searchIssues(config(projectIds = listOf("group/hello")), query = "bug").getOrThrow()
        // project id is URL-encoded into the path
        assertEquals("/api/v4/projects/group%2Fhello/issues", path)
    }

    @Test
    fun `searchIssues configured projects dedups and returns issues`() = runBlocking {
        val issues = client(body = "[${issueJson()}]")
            .searchIssues(config(projectIds = listOf("group/hello"))).getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("#7", issues.first().key)
    }

    @Test
    fun `getProjectIssues maps issues and encodes project id`() = runBlocking {
        var path: String? = null
        var search: String? = null
        val issues = client(body = "[${issueJson(3)}]") { req ->
            path = req.url.encodedPath
            search = req.url.parameters["search"]
        }.getProjectIssues(config(), "group/hello", query = "panic").getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("#3", issues.first().key)
        assertEquals("/api/v4/projects/group%2Fhello/issues", path)
        assertEquals("panic", search)
    }

    @Test
    fun `getProjectIssues omits search parameter when query blank`() = runBlocking {
        var search: String? = "unset"
        client(body = "[]") { req -> search = req.url.parameters["search"] }
            .getProjectIssues(config(), "10")
        assertNull(search)
    }

    @Test
    fun `getCurrentUser returns username`() = runBlocking {
        assertEquals("octocat", client(body = userJson).getCurrentUser(config()).getOrNull())
    }

    @Test
    fun `getIssueByIid returns issue on success`() = runBlocking {
        var path: String? = null
        val issue = client(body = issueJson(42)) { req -> path = req.url.encodedPath }
            .getIssueByIid(config(), "group/hello", 42).getOrThrow()
        assertEquals("#42", issue?.key)
        assertEquals("/api/v4/projects/group%2Fhello/issues/42", path)
    }

    @Test
    fun `getIssueByIid returns null on 404`() = runBlocking {
        val result = client(status = HttpStatusCode.NotFound, body = "not found")
            .getIssueByIid(config(), "group/hello", 99)
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `getIssueByIid fails on server error`() = runBlocking {
        val result = client(status = HttpStatusCode.InternalServerError, body = "boom")
            .getIssueByIid(config(), "group/hello", 1)
        assertTrue(result.isFailure)
    }

    @Test
    fun `baseUrl trailing slash is trimmed and api v4 appended once`() = runBlocking {
        var path: String? = null
        client(body = userJson) { req -> path = req.url.encodedPath }
            .testConnection(config(baseUrl = "https://gitlab.com/"))
        assertEquals("/api/v4/user", path)
    }

    @Test
    fun `baseUrl already containing api v4 is not duplicated`() = runBlocking {
        var path: String? = null
        client(body = userJson) { req -> path = req.url.encodedPath }
            .testConnection(config(baseUrl = "https://gitlab.com/api/v4"))
        assertEquals("/api/v4/user", path)
    }
}
