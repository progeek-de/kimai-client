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
 * Tests for the network/orchestration logic of [GitHubTicketProvider].
 *
 * The provider is constructed with a real [GitHubTicketClient] backed by a Ktor
 * [MockEngine], so this exercises the provider's delegation and the
 * search-issues / get-issue-by-key orchestration end-to-end through the client.
 *
 * Credential/validation/error-message behaviour is covered by the commonTest
 * GitHubTicketProviderTest and is not duplicated here.
 *
 * Uses [runBlocking] (not runTest) because the production client installs the
 * HttpTimeout plugin whose timeout-killer relies on real delays; runTest's
 * virtual clock would fire the timeout immediately.
 */
class GitHubTicketProviderHttpTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun config(
        owner: String = "octocat",
        repositories: List<String> = emptyList()
    ) = TicketSystemConfig(
        id = "cfg-1",
        displayName = "GitHub",
        provider = TicketProvider.GITHUB,
        enabled = true,
        baseUrl = "https://api.github.com",
        credentials = TicketCredentials.GitHubToken(
            token = "ghp_token",
            owner = owner,
            repositories = repositories
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    /**
     * Builds a provider whose engine dispatches by request path, allowing each
     * endpoint to return a tailored body (needed for multi-repo scenarios).
     */
    private fun provider(
        onRequest: (HttpRequestData) -> Unit = {},
        handler: (path: String, request: HttpRequestData) -> Pair<HttpStatusCode, String>
    ): GitHubTicketProvider {
        val engine = MockEngine { request ->
            onRequest(request)
            val (status, body) = handler(request.url.encodedPath, request)
            respond(content = body, status = status, headers = jsonHeaders)
        }
        return GitHubTicketProvider(GitHubTicketClient(engine))
    }

    private val userJson = """{"id":1,"login":"octocat","name":"The Octocat"}"""
    private val repoJson =
        """[{"id":10,"name":"hello","full_name":"octocat/hello","description":"d","owner":{"login":"octocat"}}]"""

    private fun issueJson(
        id: Long = 100,
        number: Int = 7,
        repo: String = "hello",
        updatedAt: String = "2024-01-01T00:00:00Z"
    ) =
        """{"id":$id,"number":$number,"title":"Bug","body":"desc","state":"open",""" +
            """"html_url":"https://github.com/octocat/$repo/issues/$number",""" +
            """"updated_at":"$updatedAt",""" +
            """"repository_url":"https://api.github.com/repos/octocat/$repo"}"""

    private fun searchJson(vararg items: String) =
        """{"total_count":${items.size},"incomplete_results":false,"items":[${items.joinToString(",")}]}"""

    // ============================================================
    // Simple delegations
    // ============================================================

    @Test
    fun `testConnection delegates to client`() = runBlocking {
        val result = provider { _, _ -> HttpStatusCode.OK to userJson }
            .testConnection(config())
        assertEquals("Connected as The Octocat", result.getOrThrow())
    }

    @Test
    fun `getProjects delegates to client repositories`() = runBlocking {
        val result = provider { _, _ -> HttpStatusCode.OK to repoJson }
            .getProjects(config())
        val projects = result.getOrThrow()
        assertEquals(1, projects.size)
        assertEquals("octocat/hello", projects.first().key)
    }

    @Test
    fun `getCurrentUser delegates to client`() = runBlocking {
        val result = provider { _, _ -> HttpStatusCode.OK to userJson }
            .getCurrentUser(config())
        assertEquals("octocat", result.getOrThrow())
    }

    // ============================================================
    // searchIssues - no repositories configured (search API path)
    // ============================================================

    @Test
    fun `searchIssues with no repos uses search API`() = runBlocking {
        var path: String? = null
        val result = provider(onRequest = { path = it.url.encodedPath }) { _, _ ->
            HttpStatusCode.OK to searchJson(issueJson())
        }.searchIssues(config(repositories = emptyList()), query = "bug")

        assertEquals("/search/issues", path)
        assertEquals(1, result.getOrThrow().size)
    }

    // ============================================================
    // searchIssues - configured repos (fetchFromConfiguredRepos)
    // ============================================================

    @Test
    fun `searchIssues with repos and blank query fetches per-repo issues`() = runBlocking {
        val paths = mutableListOf<String>()
        val result = provider(onRequest = { paths.add(it.url.encodedPath) }) { path, _ ->
            when {
                path == "/repos/octocat/repo-a/issues" ->
                    HttpStatusCode.OK to "[${issueJson(id = 1, number = 1, repo = "repo-a")}]"
                path == "/repos/octocat/repo-b/issues" ->
                    HttpStatusCode.OK to "[${issueJson(id = 2, number = 2, repo = "repo-b")}]"
                else -> HttpStatusCode.OK to "[]"
            }
        }.searchIssues(
            config(repositories = listOf("repo-a", "repo-b")),
            query = "",
            maxResults = 50
        )

        val issues = result.getOrThrow()
        assertEquals(2, issues.size)
        // Blank query must hit the per-repo issues endpoint, not the search API.
        assertTrue(paths.all { it.startsWith("/repos/octocat/") && it.endsWith("/issues") })
    }

    @Test
    fun `searchIssues with repos and non-blank query uses search API per repo`() = runBlocking {
        val queries = mutableListOf<String>()
        val result = provider(onRequest = { queries.add(it.url.parameters["q"] ?: "") }) { path, _ ->
            if (path == "/search/issues") {
                HttpStatusCode.OK to searchJson(issueJson(repo = "repo-a"))
            } else {
                HttpStatusCode.OK to "[]"
            }
        }.searchIssues(
            config(repositories = listOf("repo-a")),
            query = "crash",
            maxResults = 50
        )

        assertEquals(1, result.getOrThrow().size)
        // Provider scopes each query to a specific repo before delegating to search.
        assertTrue(queries.any { it.contains("crash") && it.contains("repo:octocat/repo-a") })
    }

    @Test
    fun `searchIssues dedups by id, sorts by updated desc and applies maxResults`() = runBlocking {
        val result = provider { path, _ ->
            when (path) {
                // Shared id=2 across repos must be deduplicated.
                "/repos/octocat/repo-a/issues" -> HttpStatusCode.OK to "[" +
                    issueJson(id = 1, number = 1, repo = "repo-a", updatedAt = "2024-03-01T00:00:00Z") + "," +
                    issueJson(id = 2, number = 2, repo = "repo-a", updatedAt = "2024-01-01T00:00:00Z") +
                    "]"
                "/repos/octocat/repo-b/issues" -> HttpStatusCode.OK to "[" +
                    issueJson(id = 2, number = 2, repo = "repo-b", updatedAt = "2024-01-01T00:00:00Z") + "," +
                    issueJson(id = 3, number = 3, repo = "repo-b", updatedAt = "2024-05-01T00:00:00Z") +
                    "]"
                else -> HttpStatusCode.OK to "[]"
            }
        }.searchIssues(
            config(repositories = listOf("repo-a", "repo-b")),
            query = "",
            maxResults = 2
        )

        val issues = result.getOrThrow()
        // 4 raw issues -> dedup id=2 -> 3 unique -> take(2) of the newest.
        assertEquals(2, issues.size)
        assertEquals(listOf("3", "1"), issues.map { it.id })
    }

    // ============================================================
    // getIssueByKey
    // ============================================================

    @Test
    fun `getIssueByKey returns first match across configured repos`() = runBlocking {
        val result = provider { path, _ ->
            when (path) {
                // First repo has no such issue.
                "/repos/octocat/repo-a/issues/123" -> HttpStatusCode.NotFound to "not found"
                "/repos/octocat/repo-b/issues/123" ->
                    HttpStatusCode.OK to issueJson(id = 9, number = 123, repo = "repo-b")
                else -> HttpStatusCode.OK to "{}"
            }
        }.getIssueByKey(
            config(repositories = listOf("repo-a", "repo-b")),
            key = "#123"
        )

        val issue = result.getOrThrow()
        assertEquals("#123", issue?.key)
        assertEquals("octocat/repo-b", issue?.projectKey)
    }

    @Test
    fun `getIssueByKey fails for invalid key`() = runBlocking {
        val result = provider { _, _ -> HttpStatusCode.OK to "{}" }
            .getIssueByKey(config(repositories = listOf("repo-a")), key = "abc")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `getIssueByKey returns null when not found in any repo`() = runBlocking {
        val result = provider { _, _ -> HttpStatusCode.NotFound to "not found" }
            .getIssueByKey(
                config(repositories = listOf("repo-a", "repo-b")),
                key = "#404"
            )

        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }
}
