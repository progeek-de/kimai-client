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
 * HTTP / orchestration tests for [GitLabTicketProvider].
 *
 * Constructs the provider with a real [GitLabTicketClient] backed by a Ktor
 * [MockEngine] so the network plumbing is exercised, while focusing on the
 * orchestration the provider adds on top of the client (key parsing,
 * project-iteration in getIssueByKey, branch selection in searchIssues and the
 * dedup/sort/take orchestration in the configured-projects branch).
 *
 * Validation and getErrorMessage behaviour is covered by the commonTest
 * GitLabTicketProviderTest and is intentionally not duplicated here.
 *
 * Uses [runBlocking] (not runTest) because the production client installs the
 * HttpTimeout plugin whose timeout-killer relies on real delays; runTest's
 * virtual clock would fire the timeout immediately.
 */
class GitLabTicketProviderHttpTest {

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

    private fun provider(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = "{}",
        onRequest: (HttpRequestData) -> Unit = {}
    ): GitLabTicketProvider {
        val engine = MockEngine { request ->
            onRequest(request)
            respond(content = body, status = status, headers = jsonHeaders)
        }
        return GitLabTicketProvider(GitLabTicketClient(engine))
    }

    /** Branch the MockEngine on the request path so multiple endpoints can be served. */
    private fun routingProvider(
        route: (HttpRequestData) -> Pair<HttpStatusCode, String>
    ): GitLabTicketProvider {
        val engine = MockEngine { request ->
            val (status, body) = route(request)
            respond(content = body, status = status, headers = jsonHeaders)
        }
        return GitLabTicketProvider(GitLabTicketClient(engine))
    }

    private val userJson = """{"id":1,"username":"octocat","name":"The Octocat"}"""

    private val projectsJson =
        """[{"id":10,"name":"hello","path_with_namespace":"group/hello",""" +
            """"description":"d","web_url":"https://gitlab.com/group/hello"}]"""

    private fun issueJson(
        id: Int = 100,
        iid: Int = 7,
        projectId: Int = 10,
        updatedAt: String = "2024-01-01T00:00:00Z"
    ) = """{"id":$id,"iid":$iid,"title":"Bug","description":"desc","state":"opened",""" +
        """"labels":["bug"],"web_url":"https://gitlab.com/group/hello/-/issues/$iid",""" +
        """"updated_at":"$updatedAt","project_id":$projectId}"""

    // ============================================================
    // testConnection
    // ============================================================

    @Test
    fun `testConnection delegates to client and returns greeting`() = runBlocking {
        val result = provider(body = userJson).testConnection(config())
        assertEquals("Connected as The Octocat (@octocat)", result.getOrThrow())
    }

    @Test
    fun `testConnection sends private-token header`() = runBlocking {
        var token: String? = null
        provider(body = userJson) { req -> token = req.headers["PRIVATE-TOKEN"] }
            .testConnection(config())
        assertEquals("glpat_token", token)
    }

    @Test
    fun `testConnection propagates failure on error status`() = runBlocking {
        val result = provider(status = HttpStatusCode.Unauthorized, body = "bad creds")
            .testConnection(config())
        assertTrue(result.isFailure)
    }

    // ============================================================
    // getProjects
    // ============================================================

    @Test
    fun `getProjects delegates and maps projects`() = runBlocking {
        val projects = provider(body = projectsJson).getProjects(config()).getOrThrow()
        assertEquals(1, projects.size)
        assertEquals("group/hello", projects.first().key)
    }

    // ============================================================
    // getCurrentUser
    // ============================================================

    @Test
    fun `getCurrentUser delegates and returns username`() = runBlocking {
        assertEquals("octocat", provider(body = userJson).getCurrentUser(config()).getOrThrow())
    }

    // ============================================================
    // searchIssues - global branch (no projectIds)
    // ============================================================

    @Test
    fun `searchIssues uses global endpoint when no projectIds configured`() = runBlocking {
        var path: String? = null
        var search: String? = null
        provider(body = "[${issueJson()}]") { req ->
            path = req.url.encodedPath
            search = req.url.parameters["search"]
        }.searchIssues(config(projectIds = emptyList()), query = "crash", maxResults = 50)
        assertEquals("/api/v4/issues", path)
        assertEquals("crash", search)
    }

    @Test
    fun `searchIssues global maps issues`() = runBlocking {
        val issues = provider(body = "[${issueJson()}]")
            .searchIssues(config(projectIds = emptyList()), query = "", maxResults = 50)
            .getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("#7", issues.first().key)
    }

    // ============================================================
    // searchIssues - configured-projects branch (dedup / sort / take)
    // ============================================================

    @Test
    fun `searchIssues hits per-project endpoint when projectIds present`() = runBlocking {
        val paths = mutableListOf<String>()
        provider(body = "[${issueJson()}]") { req -> paths.add(req.url.encodedPath) }
            .searchIssues(config(projectIds = listOf("group/hello")), query = "bug", maxResults = 50)
            .getOrThrow()
        assertEquals(listOf("/api/v4/projects/group%2Fhello/issues"), paths)
    }

    @Test
    fun `searchIssues across projects dedups issues by id`() = runBlocking {
        // Both configured projects return the same issue id (100); expect one result.
        val provider = routingProvider { _ -> HttpStatusCode.OK to "[${issueJson(id = 100, iid = 7)}]" }
        val issues = provider
            .searchIssues(config(projectIds = listOf("a/one", "b/two")), query = "", maxResults = 50)
            .getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("100", issues.first().id)
    }

    @Test
    fun `searchIssues across projects sorts by updated descending and applies take`() = runBlocking {
        // project p1 -> older issue, project p2 -> newer issue. Distinct ids, maxResults=1
        // so only the newest survives the sort+take orchestration.
        val provider = routingProvider { req ->
            val body = if (req.url.encodedPath.contains("p1%2Fold")) {
                "[${issueJson(id = 1, iid = 1, projectId = 1, updatedAt = "2024-01-01T00:00:00Z")}]"
            } else {
                "[${issueJson(id = 2, iid = 2, projectId = 2, updatedAt = "2024-12-31T00:00:00Z")}]"
            }
            HttpStatusCode.OK to body
        }
        val issues = provider
            .searchIssues(config(projectIds = listOf("p1/old", "p2/new")), query = "", maxResults = 1)
            .getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("2", issues.first().id)
        assertEquals("#2", issues.first().key)
    }

    @Test
    fun `searchIssues across projects skips failing project and keeps successful one`() = runBlocking {
        // p1 fails (500), p2 succeeds. Per-project failures are swallowed by the
        // client's getOrNull, so the overall result still succeeds with p2's issue.
        val provider = routingProvider { req ->
            if (req.url.encodedPath.contains("p1%2Ffail")) {
                HttpStatusCode.InternalServerError to "boom"
            } else {
                HttpStatusCode.OK to "[${issueJson(id = 2, iid = 2, projectId = 2)}]"
            }
        }
        val issues = provider
            .searchIssues(config(projectIds = listOf("p1/fail", "p2/ok")), query = "", maxResults = 50)
            .getOrThrow()
        assertEquals(1, issues.size)
        assertEquals("2", issues.first().id)
    }

    // ============================================================
    // getIssueByKey - key parsing & project iteration orchestration
    // ============================================================

    @Test
    fun `getIssueByKey returns issue for valid key from configured project`() = runBlocking {
        var path: String? = null
        val result = provider(body = issueJson(iid = 42)) { req -> path = req.url.encodedPath }
            .getIssueByKey(config(projectIds = listOf("group/hello")), "#42")
        val issue = result.getOrThrow()
        assertEquals("#42", issue?.key) // iid in body fixture drives the key
        assertEquals("/api/v4/projects/group%2Fhello/issues/42", path)
    }

    @Test
    fun `getIssueByKey accepts key without hash prefix`() = runBlocking {
        var path: String? = null
        provider(body = issueJson(iid = 5)) { req -> path = req.url.encodedPath }
            .getIssueByKey(config(projectIds = listOf("group/hello")), "5")
        assertEquals("/api/v4/projects/group%2Fhello/issues/5", path)
    }

    @Test
    fun `getIssueByKey returns failure for non-numeric key`() = runBlocking {
        val result = provider(body = userJson)
            .getIssueByKey(config(projectIds = listOf("group/hello")), "#abc")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `getIssueByKey returns success null when no projectIds configured`() = runBlocking {
        // The loop over projectIds never runs, so the provider returns success(null)
        // without making any HTTP call.
        var called = false
        val result = provider(body = issueJson()) { _ -> called = true }
            .getIssueByKey(config(projectIds = emptyList()), "#7")
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
        assertTrue(!called)
    }

    @Test
    fun `getIssueByKey returns success null when not found in any project`() = runBlocking {
        val result = provider(status = HttpStatusCode.NotFound, body = "not found")
            .getIssueByKey(config(projectIds = listOf("group/hello")), "#99")
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `getIssueByKey returns first matching project and stops iterating`() = runBlocking {
        // First project (a/one) holds the issue, second project must not be queried.
        val queried = mutableListOf<String>()
        val provider = routingProvider { req ->
            queried.add(req.url.encodedPath)
            HttpStatusCode.OK to issueJson(id = 100, iid = 7)
        }
        val issue = provider
            .getIssueByKey(config(projectIds = listOf("a/one", "b/two")), "#7")
            .getOrThrow()
        assertEquals("#7", issue?.key)
        assertEquals(listOf("/api/v4/projects/a%2Fone/issues/7"), queried)
    }

    @Test
    fun `getIssueByKey skips project where issue is missing and checks next`() = runBlocking {
        // a/one -> 404 (null), b/two -> issue found. Provider must continue to b/two.
        val queried = mutableListOf<String>()
        val provider = routingProvider { req ->
            queried.add(req.url.encodedPath)
            if (req.url.encodedPath.contains("a%2Fone")) {
                HttpStatusCode.NotFound to "not found"
            } else {
                HttpStatusCode.OK to issueJson(id = 100, iid = 7)
            }
        }
        val issue = provider
            .getIssueByKey(config(projectIds = listOf("a/one", "b/two")), "#7")
            .getOrThrow()
        assertEquals("#7", issue?.key)
        assertEquals(
            listOf("/api/v4/projects/a%2Fone/issues/7", "/api/v4/projects/b%2Ftwo/issues/7"),
            queried
        )
    }
}
