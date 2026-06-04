package de.progeek.kimai.shared.core.ticketsystem.providers.github

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
 * Tests for [GitHubTicketClient] using a Ktor [MockEngine].
 * Exercises HTTP request building, response mapping, auth and error handling.
 *
 * Uses [runBlocking] (not runTest) because the production client installs the
 * HttpTimeout plugin whose timeout-killer relies on real delays; runTest's
 * virtual clock would fire the timeout immediately.
 */
class GitHubTicketClientTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun config(
        baseUrl: String = "https://api.github.com",
        owner: String = "octocat",
        repositories: List<String> = emptyList()
    ) = TicketSystemConfig(
        id = "cfg-1",
        displayName = "GitHub",
        provider = TicketProvider.GITHUB,
        enabled = true,
        baseUrl = baseUrl,
        credentials = TicketCredentials.GitHubToken(
            token = "ghp_token",
            owner = owner,
            repositories = repositories
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private fun client(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = "{}",
        onRequest: (HttpRequestData) -> Unit = {}
    ): GitHubTicketClient {
        val engine = MockEngine { request ->
            onRequest(request)
            respond(content = body, status = status, headers = jsonHeaders)
        }
        return GitHubTicketClient(engine)
    }

    private val userJson = """{"id":1,"login":"octocat","name":"The Octocat"}"""
    private val repoJson =
        """[{"id":10,"name":"hello","full_name":"octocat/hello","description":"d","owner":{"login":"octocat"}}]"""

    private fun issueJson(number: Int = 7) =
        """{"id":100,"number":$number,"title":"Bug","body":"desc","state":"open",""" +
            """"html_url":"https://github.com/octocat/hello/issues/$number",""" +
            """"updated_at":"2024-01-01T00:00:00Z",""" +
            """"repository_url":"https://api.github.com/repos/octocat/hello"}"""

    @Test
    fun `testConnection returns greeting on success`() = runBlocking {
        val result = client(body = userJson).testConnection(config())
        assertEquals("Connected as The Octocat", result.getOrElse { throw it })
    }

    @Test
    fun `testConnection falls back to login when name is null`() = runBlocking {
        val result = client(body = """{"id":1,"login":"octocat"}""").testConnection(config())
        assertEquals("Connected as octocat", result.getOrNull())
    }

    @Test
    fun `testConnection sends bearer auth and github headers`() = runBlocking {
        var token: String? = null
        var apiVersion: String? = null
        client(body = userJson) { req ->
            token = req.headers[HttpHeaders.Authorization]
            apiVersion = req.headers["X-GitHub-Api-Version"]
        }.testConnection(config())
        assertEquals("Bearer ghp_token", token)
        assertEquals("2022-11-28", apiVersion)
    }

    @Test
    fun `testConnection returns failure on error status`() = runBlocking {
        val result = client(status = HttpStatusCode.Unauthorized, body = "bad creds").testConnection(config())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("401") == true)
    }

    @Test
    fun `getRepositories maps repositories on success`() = runBlocking {
        val repos = client(body = repoJson).getRepositories(config()).getOrThrow()
        assertEquals(1, repos.size)
        assertEquals("octocat/hello", repos.first().key)
    }

    @Test
    fun `searchIssues maps items on success`() = runBlocking {
        val search = """{"total_count":1,"incomplete_results":false,"items":[${issueJson()}]}"""
        val issues = client(body = search).searchIssues(config(), query = "bug").getOrThrow()
        assertEquals(1, issues.size)
    }

    @Test
    fun `searchIssues builds query with repositories filter`() = runBlocking {
        val search = """{"total_count":0,"incomplete_results":false,"items":[]}"""
        var q: String? = null
        client(body = search) { req -> q = req.url.parameters["q"] }
            .searchIssues(config(repositories = listOf("hello")), query = "crash")
        assertTrue(q!!.contains("crash"))
        assertTrue(q!!.contains("repo:octocat/hello"))
        assertTrue(q!!.contains("is:issue"))
    }

    @Test
    fun `searchIssues defaults to user filter when no repositories`() = runBlocking {
        val search = """{"total_count":0,"incomplete_results":false,"items":[]}"""
        var q: String? = null
        client(body = search) { req -> q = req.url.parameters["q"] }
            .searchIssues(config(repositories = emptyList()))
        assertTrue(q!!.contains("user:octocat"))
    }

    @Test
    fun `getIssues maps issues on success`() = runBlocking {
        val issues = client(body = "[${issueJson()}]").getIssues(config(), "octocat", "hello").getOrThrow()
        assertEquals(1, issues.size)
    }

    @Test
    fun `getCurrentUser returns login`() = runBlocking {
        assertEquals("octocat", client(body = userJson).getCurrentUser(config()).getOrNull())
    }

    @Test
    fun `getIssueByNumber returns issue on success`() = runBlocking {
        val issue = client(body = issueJson(42)).getIssueByNumber(config(), "octocat", "hello", 42).getOrThrow()
        assertEquals("#42", issue?.key)
    }

    @Test
    fun `getIssueByNumber returns null on 404`() = runBlocking {
        val result = client(status = HttpStatusCode.NotFound, body = "not found")
            .getIssueByNumber(config(), "octocat", "hello", 99)
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `getIssueByNumber fails on server error`() = runBlocking {
        val result = client(status = HttpStatusCode.InternalServerError, body = "boom")
            .getIssueByNumber(config(), "octocat", "hello", 1)
        assertTrue(result.isFailure)
    }

    @Test
    fun `baseUrl trailing slash is trimmed`() = runBlocking {
        var path: String? = null
        client(body = userJson) { req -> path = req.url.encodedPath }
            .testConnection(config(baseUrl = "https://api.github.com/"))
        assertEquals("/user", path)
    }
}
