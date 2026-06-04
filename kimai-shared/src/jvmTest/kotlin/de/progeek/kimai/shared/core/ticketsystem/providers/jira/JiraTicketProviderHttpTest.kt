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
 * Tests for [JiraTicketProvider]'s network/orchestration logic.
 *
 * The provider is exercised through [JiraTicketProvider] constructed with a real
 * [JiraTicketClient] backed by a Ktor [MockEngine], so this covers the full
 * delegate-to-client path (validation guard + client request/mapping) for every
 * non-validation method.
 *
 * Pure validation/error-message behaviour lives in the commonTest
 * [JiraTicketProviderTest] and is not duplicated here.
 *
 * Uses [runBlocking] (not runTest) because the production client installs the
 * HttpTimeout plugin whose timeout-killer relies on real delays; runTest's
 * virtual clock would fire the timeout immediately.
 */
class JiraTicketProviderHttpTest {

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

    /**
     * Builds a provider wired to a [MockEngine] that dispatches on the request's
     * encoded path so a single provider can answer the different endpoints a flow
     * touches.
     */
    private fun provider(
        status: HttpStatusCode = HttpStatusCode.OK,
        onRequest: (HttpRequestData) -> Unit = {},
        bodyFor: (String) -> String
    ): JiraTicketProvider {
        val engine = MockEngine { request ->
            onRequest(request)
            respond(content = bodyFor(request.url.encodedPath), status = status, headers = jsonHeaders)
        }
        return JiraTicketProvider(JiraTicketClient(engine))
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

    private fun searchJson(vararg issues: String) =
        """{"issues":[${issues.joinToString(",")}],"startAt":0,"maxResults":50,"total":${issues.size}}"""

    // ============================================================
    // testConnection
    // ============================================================

    @Test
    fun `testConnection delegates to client and returns greeting`() = runBlocking {
        val result = provider { userJson }.testConnection(apiTokenConfig())
        assertEquals("Connected successfully as Jane Doe", result.getOrElse { throw it })
    }

    @Test
    fun `testConnection works for personal access token credentials`() = runBlocking {
        var auth: String? = null
        val result = provider(onRequest = { auth = it.headers[HttpHeaders.Authorization] }) { userJson }
            .testConnection(patConfig())
        assertEquals("Bearer pat-token", auth)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `testConnection propagates client failure as Result failure`() = runBlocking {
        val result = provider(status = HttpStatusCode.Unauthorized) { "nope" }
            .testConnection(apiTokenConfig())
        assertTrue(result.isFailure)
    }

    // ============================================================
    // getProjects
    // ============================================================

    @Test
    fun `getProjects delegates and maps projects`() = runBlocking {
        var path: String? = null
        val projects = provider(onRequest = { path = it.url.encodedPath }) { projectJson }
            .getProjects(apiTokenConfig())
            .getOrThrow()
        assertEquals("/rest/api/3/project", path)
        assertEquals(1, projects.size)
        assertEquals("PROJ", projects.first().key)
        assertEquals("Project One", projects.first().name)
    }

    @Test
    fun `getProjects propagates client failure`() = runBlocking {
        val result = provider(status = HttpStatusCode.NotFound) { "missing" }
            .getProjects(apiTokenConfig())
        assertTrue(result.isFailure)
    }

    // ============================================================
    // getCurrentUser
    // ============================================================

    @Test
    fun `getCurrentUser delegates and returns email`() = runBlocking {
        val result = provider { userJson }.getCurrentUser(apiTokenConfig())
        assertEquals("user@company.com", result.getOrNull())
    }

    @Test
    fun `getCurrentUser works for personal access token credentials`() = runBlocking {
        var auth: String? = null
        val result = provider(onRequest = { auth = it.headers[HttpHeaders.Authorization] }) { userJson }
            .getCurrentUser(patConfig())
        assertEquals("Bearer pat-token", auth)
        assertEquals("user@company.com", result.getOrNull())
    }

    @Test
    fun `getCurrentUser propagates client failure`() = runBlocking {
        val result = provider(status = HttpStatusCode.Unauthorized) { "nope" }
            .getCurrentUser(apiTokenConfig())
        assertTrue(result.isFailure)
    }

    // ============================================================
    // searchIssues (orchestration + result mapping)
    // ============================================================

    @Test
    fun `searchIssues delegates query through to jql and maps issues`() = runBlocking {
        var jql: String? = null
        var path: String? = null
        val issues = provider(
            onRequest = {
                jql = it.url.parameters["jql"]
                path = it.url.encodedPath
            }
        ) { searchJson(issueJson()) }
            .searchIssues(apiTokenConfig(), query = "login", maxResults = 50)
            .getOrThrow()

        assertEquals("/rest/api/3/search/jql", path)
        assertEquals("text ~ \"login\" ORDER BY key ASC", jql)
        assertEquals(1, issues.size)
        assertEquals("PROJ-123", issues.first().key)
        assertEquals("Fix login bug", issues.first().summary)
        assertEquals(TicketProvider.JIRA, issues.first().provider)
        assertEquals("cfg-1", issues.first().sourceId)
    }

    @Test
    fun `searchIssues forwards maxResults to the client`() = runBlocking {
        var maxResults: String? = null
        provider(onRequest = { maxResults = it.url.parameters["maxResults"] }) {
            searchJson()
        }.searchIssues(apiTokenConfig(), query = "login", maxResults = 7)
        assertEquals("7", maxResults)
    }

    @Test
    fun `searchIssues honours default project key from config`() = runBlocking {
        var jql: String? = null
        provider(onRequest = { jql = it.url.parameters["jql"] }) {
            searchJson()
        }.searchIssues(apiTokenConfig(defaultProjectKey = "PROJ"), query = "login", maxResults = 50)
        assertEquals("project = PROJ AND (text ~ \"login\" ORDER BY key ASC)", jql)
    }

    @Test
    fun `searchIssues maps multiple issues`() = runBlocking {
        val body = searchJson(issueJson("PROJ-1"), issueJson("PROJ-2"))
        val issues = provider { body }
            .searchIssues(apiTokenConfig(), query = "", maxResults = 50)
            .getOrThrow()
        assertEquals(listOf("PROJ-1", "PROJ-2"), issues.map { it.key })
    }

    @Test
    fun `searchIssues propagates client failure`() = runBlocking {
        val result = provider(status = HttpStatusCode.InternalServerError) { "boom" }
            .searchIssues(apiTokenConfig(), query = "login", maxResults = 50)
        assertTrue(result.isFailure)
    }

    // ============================================================
    // getIssueByKey
    // ============================================================

    @Test
    fun `getIssueByKey delegates and maps issue on success`() = runBlocking {
        var path: String? = null
        val issue = provider(onRequest = { path = it.url.encodedPath }) { issueJson("PROJ-9") }
            .getIssueByKey(apiTokenConfig(), "PROJ-9")
            .getOrThrow()
        assertEquals("/rest/api/3/issue/PROJ-9", path)
        assertEquals("PROJ-9", issue?.key)
        assertEquals("Fix login bug", issue?.summary)
    }

    @Test
    fun `getIssueByKey returns success null when not found`() = runBlocking {
        val result = provider(status = HttpStatusCode.NotFound) { "not found" }
            .getIssueByKey(apiTokenConfig(), "PROJ-404")
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `getIssueByKey propagates server error as failure`() = runBlocking {
        val result = provider(status = HttpStatusCode.InternalServerError) { "boom" }
            .getIssueByKey(apiTokenConfig(), "PROJ-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `getIssueByKey works for personal access token credentials`() = runBlocking {
        var auth: String? = null
        val issue = provider(onRequest = { auth = it.headers[HttpHeaders.Authorization] }) {
            issueJson("PROJ-9")
        }.getIssueByKey(patConfig(), "PROJ-9").getOrThrow()
        assertEquals("Bearer pat-token", auth)
        assertEquals("PROJ-9", issue?.key)
    }
}
