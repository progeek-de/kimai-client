package de.progeek.kimai.shared.core.ticketsystem.providers.jira

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
 * Tests for [JiraTicketClient] using a Ktor [MockEngine].
 * Exercises HTTP request building, response mapping, auth (basic + bearer),
 * JQL building and error handling.
 *
 * Uses [runBlocking] (not runTest) because the production client installs the
 * HttpTimeout plugin whose timeout-killer relies on real delays; runTest's
 * virtual clock would fire the timeout immediately.
 */
class JiraTicketClientTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun apiTokenConfig(
        baseUrl: String = "https://company.atlassian.net",
        defaultProjectKey: String? = null,
        email: String = "user@company.com",
        token: String = "api-token"
    ) = TicketSystemConfig(
        id = "cfg-1",
        displayName = "Jira Cloud",
        provider = TicketProvider.JIRA,
        enabled = true,
        baseUrl = baseUrl,
        credentials = TicketCredentials.JiraApiToken(email = email, token = token),
        syncIntervalMinutes = 15,
        defaultProjectKey = defaultProjectKey,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private fun patConfig(
        baseUrl: String = "https://jira.company.com",
        defaultProjectKey: String? = null,
        token: String = "pat-token"
    ) = TicketSystemConfig(
        id = "cfg-2",
        displayName = "Jira Server",
        provider = TicketProvider.JIRA,
        enabled = true,
        baseUrl = baseUrl,
        credentials = TicketCredentials.JiraPersonalAccessToken(token = token),
        syncIntervalMinutes = 15,
        defaultProjectKey = defaultProjectKey,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private fun client(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = "{}",
        onRequest: (HttpRequestData) -> Unit = {}
    ): JiraTicketClient {
        val engine = MockEngine { request ->
            onRequest(request)
            respond(content = body, status = status, headers = jsonHeaders)
        }
        return JiraTicketClient(engine)
    }

    private val userJson =
        """{"accountId":"acc-1","emailAddress":"user@company.com","displayName":"Jane Doe","active":true}"""

    private val projectJson =
        """[{"id":"10000","key":"PROJ","name":"Project One","description":"desc","projectTypeKey":"software"}]"""

    private fun issueJson(key: String = "PROJ-123") =
        """{"id":"1001","key":"$key","fields":{""" +
            """"summary":"Fix login bug","description":"details","status":{"name":"In Progress"},""" +
            """"assignee":{"accountId":"acc-2","displayName":"John Smith","active":true},""" +
            """"project":{"key":"PROJ","name":"Project One"},""" +
            """"issuetype":{"name":"Bug"},"updated":"2024-01-02T10:00:00.000+0000"}}"""

    // --- testConnection ---

    @Test
    fun `testConnection returns greeting on success`() = runBlocking {
        val result = client(body = userJson).testConnection(apiTokenConfig())
        assertEquals("Connected successfully as Jane Doe", result.getOrElse { throw it })
    }

    @Test
    fun `testConnection sends basic auth for api token credentials`() = runBlocking {
        var auth: String? = null
        client(body = userJson) { req -> auth = req.headers[HttpHeaders.Authorization] }
            .testConnection(apiTokenConfig(email = "user@company.com", token = "api-token"))
        // Basic base64("user@company.com:api-token")
        val expected = "Basic dXNlckBjb21wYW55LmNvbTphcGktdG9rZW4="
        assertEquals(expected, auth)
    }

    @Test
    fun `testConnection sends bearer auth for personal access token credentials`() = runBlocking {
        var auth: String? = null
        client(body = userJson) { req -> auth = req.headers[HttpHeaders.Authorization] }
            .testConnection(patConfig(token = "pat-token"))
        assertEquals("Bearer pat-token", auth)
    }

    @Test
    fun `testConnection builds rest api v3 url`() = runBlocking {
        var path: String? = null
        client(body = userJson) { req -> path = req.url.encodedPath }
            .testConnection(apiTokenConfig(baseUrl = "https://company.atlassian.net/"))
        assertEquals("/rest/api/3/myself", path)
    }

    @Test
    fun `testConnection strips existing rest api suffix from base url`() = runBlocking {
        var path: String? = null
        client(body = userJson) { req -> path = req.url.encodedPath }
            .testConnection(apiTokenConfig(baseUrl = "https://jira.company.com/rest/api/2"))
        assertEquals("/rest/api/3/myself", path)
    }

    @Test
    fun `testConnection returns failure with mapped 401 message for api token`() = runBlocking {
        val result = client(status = HttpStatusCode.Unauthorized, body = "nope")
            .testConnection(apiTokenConfig())
        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message ?: ""
        assertTrue(msg.contains("Authentication failed (API Token)"))
        assertTrue(msg.contains("api-tokens"))
    }

    @Test
    fun `testConnection returns failure with mapped 401 message for pat`() = runBlocking {
        val result = client(status = HttpStatusCode.Unauthorized, body = "nope")
            .testConnection(patConfig())
        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message ?: ""
        assertTrue(msg.contains("Authentication failed (Personal Access Token)"))
        assertTrue(msg.contains("Personal Access Token is valid"))
    }

    @Test
    fun `testConnection maps 429 to rate limit message`() = runBlocking {
        val result = client(status = HttpStatusCode.TooManyRequests, body = "slow down")
            .testConnection(apiTokenConfig())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Rate limit exceeded") == true)
    }

    @Test
    fun `testConnection maps generic error with status code`() = runBlocking {
        val result = client(status = HttpStatusCode.InternalServerError, body = "boom")
            .testConnection(apiTokenConfig())
        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message ?: ""
        assertTrue(msg.contains("Jira API error (500)"))
        assertTrue(msg.contains("boom"))
    }

    @Test
    fun `testConnection maps 403 pat-on-cloud to dedicated message`() = runBlocking {
        val body = "Personal Access Tokens can only be used on Jira Data Center"
        val cfg = patConfig(baseUrl = "https://company.atlassian.net")
        val result = client(status = HttpStatusCode.Forbidden, body = body).testConnection(cfg)
        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message
                ?.contains("Personal Access Tokens cannot be used with Jira Cloud") == true
        )
    }

    // --- getProjects ---

    @Test
    fun `getProjects maps projects on success`() = runBlocking {
        val projects = client(body = projectJson).getProjects(apiTokenConfig()).getOrThrow()
        assertEquals(1, projects.size)
        assertEquals("PROJ", projects.first().key)
        assertEquals("Project One", projects.first().name)
        assertEquals("desc", projects.first().description)
    }

    @Test
    fun `getProjects hits project endpoint`() = runBlocking {
        var path: String? = null
        client(body = projectJson) { req -> path = req.url.encodedPath }
            .getProjects(apiTokenConfig())
        assertEquals("/rest/api/3/project", path)
    }

    @Test
    fun `getProjects returns failure on error`() = runBlocking {
        val result = client(status = HttpStatusCode.NotFound, body = "missing")
            .getProjects(apiTokenConfig())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Resource not found") == true)
    }

    // --- search ---

    @Test
    fun `search maps issues and fields on success`() = runBlocking {
        val body = """{"issues":[${issueJson()}],"startAt":0,"maxResults":50,"total":1}"""
        val issues = client(body = body).search(apiTokenConfig()).getOrThrow()
        assertEquals(1, issues.size)
        val issue = issues.first()
        assertEquals("1001", issue.id)
        assertEquals("PROJ-123", issue.key)
        assertEquals("Fix login bug", issue.summary)
        assertEquals("In Progress", issue.status)
        assertEquals("PROJ", issue.projectKey)
        assertEquals("Project One", issue.projectName)
        assertEquals("Bug", issue.issueType)
        assertEquals("John Smith", issue.assignee)
        assertEquals(TicketProvider.JIRA, issue.provider)
        assertEquals("cfg-1", issue.sourceId)
        assertEquals("https://company.atlassian.net/browse/PROJ-123", issue.webUrl)
    }

    @Test
    fun `search uses default jql when query is blank`() = runBlocking {
        val body = """{"issues":[],"startAt":0,"maxResults":50,"total":0}"""
        var jql: String? = null
        client(body = body) { req -> jql = req.url.parameters["jql"] }
            .search(apiTokenConfig())
        assertEquals("updated >= -30d ORDER BY updated DESC", jql)
    }

    @Test
    fun `search passes through raw jql when query contains operators`() = runBlocking {
        val body = """{"issues":[],"startAt":0,"maxResults":50,"total":0}"""
        var jql: String? = null
        client(body = body) { req -> jql = req.url.parameters["jql"] }
            .search(apiTokenConfig(), jql = "status = Done")
        assertEquals("status = Done", jql)
    }

    @Test
    fun `search wraps plain text query in text search jql`() = runBlocking {
        val body = """{"issues":[],"startAt":0,"maxResults":50,"total":0}"""
        var jql: String? = null
        client(body = body) { req -> jql = req.url.parameters["jql"] }
            .search(apiTokenConfig(), jql = "login")
        assertEquals("text ~ \"login\" ORDER BY key ASC", jql)
    }

    @Test
    fun `search prepends default project filter when configured`() = runBlocking {
        val body = """{"issues":[],"startAt":0,"maxResults":50,"total":0}"""
        var jql: String? = null
        client(body = body) { req -> jql = req.url.parameters["jql"] }
            .search(apiTokenConfig(defaultProjectKey = "PROJ"), jql = "login")
        assertEquals("project = PROJ AND (text ~ \"login\" ORDER BY key ASC)", jql)
    }

    @Test
    fun `search does not duplicate project filter when jql already references project`() = runBlocking {
        val body = """{"issues":[],"startAt":0,"maxResults":50,"total":0}"""
        var jql: String? = null
        client(body = body) { req -> jql = req.url.parameters["jql"] }
            .search(apiTokenConfig(defaultProjectKey = "PROJ"), jql = "project = OTHER")
        assertEquals("project = OTHER", jql)
    }

    @Test
    fun `search sends maxResults and fields parameters`() = runBlocking {
        val body = """{"issues":[],"startAt":0,"maxResults":50,"total":0}"""
        var maxResults: String? = null
        var fields: String? = null
        var path: String? = null
        client(body = body) { req ->
            maxResults = req.url.parameters["maxResults"]
            fields = req.url.parameters["fields"]
            path = req.url.encodedPath
        }.search(apiTokenConfig(), maxResults = 25)
        assertEquals("25", maxResults)
        assertEquals("summary,status,assignee,project,issuetype,updated", fields)
        assertEquals("/rest/api/3/search/jql", path)
    }

    @Test
    fun `search applies fallbacks for missing optional fields`() = runBlocking {
        val minimal =
            """{"issues":[{"id":"2","key":"X-1","fields":{"summary":"only summary"}}],""" +
                """"startAt":0,"maxResults":50,"total":1}"""
        val issue = client(body = minimal).search(apiTokenConfig()).getOrThrow().first()
        assertEquals("Unknown", issue.status)
        assertEquals("", issue.projectKey)
        assertEquals("", issue.projectName)
        assertEquals("Task", issue.issueType)
        assertNull(issue.assignee)
    }

    @Test
    fun `search returns failure on error`() = runBlocking {
        val result = client(status = HttpStatusCode.InternalServerError, body = "boom")
            .search(apiTokenConfig())
        assertTrue(result.isFailure)
    }

    // --- getCurrentUser ---

    @Test
    fun `getCurrentUser returns email when present`() = runBlocking {
        val result = client(body = userJson).getCurrentUser(apiTokenConfig())
        assertEquals("user@company.com", result.getOrNull())
    }

    @Test
    fun `getCurrentUser falls back to display name when email is null`() = runBlocking {
        val body = """{"accountId":"acc-1","displayName":"Jane Doe","active":true}"""
        val result = client(body = body).getCurrentUser(apiTokenConfig())
        assertEquals("Jane Doe", result.getOrNull())
    }

    @Test
    fun `getCurrentUser returns failure on error`() = runBlocking {
        val result = client(status = HttpStatusCode.Unauthorized, body = "nope")
            .getCurrentUser(apiTokenConfig())
        assertTrue(result.isFailure)
    }

    // --- getIssueByKey ---

    @Test
    fun `getIssueByKey returns mapped issue on success`() = runBlocking {
        val issue = client(body = issueJson("PROJ-9")).getIssueByKey(apiTokenConfig(), "PROJ-9").getOrThrow()
        assertEquals("PROJ-9", issue?.key)
        assertEquals("Fix login bug", issue?.summary)
    }

    @Test
    fun `getIssueByKey hits issue endpoint with fields parameter`() = runBlocking {
        var path: String? = null
        var fields: String? = null
        client(body = issueJson()) { req ->
            path = req.url.encodedPath
            fields = req.url.parameters["fields"]
        }.getIssueByKey(apiTokenConfig(), "PROJ-123")
        assertEquals("/rest/api/3/issue/PROJ-123", path)
        assertEquals("summary,status,assignee,project,issuetype,updated", fields)
    }

    @Test
    fun `getIssueByKey returns null on 404`() = runBlocking {
        val result = client(status = HttpStatusCode.NotFound, body = "not found")
            .getIssueByKey(apiTokenConfig(), "PROJ-404")
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `getIssueByKey fails on server error`() = runBlocking {
        val result = client(status = HttpStatusCode.InternalServerError, body = "boom")
            .getIssueByKey(apiTokenConfig(), "PROJ-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Jira API error (500)") == true)
    }
}
