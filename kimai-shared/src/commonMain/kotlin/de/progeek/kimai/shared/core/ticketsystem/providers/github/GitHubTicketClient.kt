package de.progeek.kimai.shared.core.ticketsystem.providers.github

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
 * HTTP client for GitHub REST API v3.
 */
internal class GitHubTicketClient {

    companion object {
        private const val TAG = "GitHubTicketClient"
        private const val REQUEST_TIMEOUT_MS = 30_000L
        private const val CONNECT_TIMEOUT_MS = 10_000L
        private const val GITHUB_API_VERSION = "2022-11-28"
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
     * Test connection to GitHub.
     */
    suspend fun testConnection(config: TicketSystemConfig): Result<String> {
        Napier.d(tag = TAG) { "Testing connection to GitHub" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/user")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                configureHeaders()
            }

            handleResponse(response) {
                val user: GitHubUserResponse = it.body()
                Napier.i(tag = TAG) { "Connection successful: ${user.login}" }
                "Connected as ${user.name ?: user.login}"
            }
        }
    }

    /**
     * Get repositories for the configured owner.
     */
    suspend fun getRepositories(config: TicketSystemConfig): Result<List<TicketProject>> {
        val credentials = config.credentials as? TicketCredentials.GitHubToken
            ?: return Result.failure(IllegalArgumentException("Invalid credentials type"))

        Napier.d(tag = TAG) { "Fetching repositories for ${credentials.owner}" }

        return executeRequest(config) {
            val url = buildApiUrl(config, "/users/${credentials.owner}/repos")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                configureHeaders()
                parameter("per_page", 100)
                parameter("sort", "updated")
            }

            handleResponse(response) {
                val repos: List<GitHubRepoResponse> = it.body()
                Napier.i(tag = TAG) { "Fetched ${repos.size} repositories" }
                repos.map(GitHubResponseMapper::mapRepository)
            }
        }
    }

    /**
     * Search for issues.
     */
    suspend fun searchIssues(
        config: TicketSystemConfig,
        query: String = "",
        maxResults: Int = 50
    ): Result<List<TicketIssue>> {
        val credentials = config.credentials as? TicketCredentials.GitHubToken
            ?: return Result.failure(IllegalArgumentException("Invalid credentials type"))

        val searchQuery = buildSearchQuery(credentials, query)
        Napier.d(tag = TAG) { "Searching issues: q=\"$searchQuery\"" }

        return executeRequest(config) {
            val url = buildApiUrl(config, "/search/issues")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                configureHeaders()
                parameter("q", searchQuery)
                parameter("per_page", maxResults)
                parameter("sort", "updated")
                parameter("order", "desc")
            }

            handleResponse(response) {
                val searchResponse: GitHubSearchResponse = it.body()
                Napier.i(tag = TAG) { "Search returned ${searchResponse.items.size} issues" }
                searchResponse.items.map { issue ->
                    val repoFullName = extractRepoFromUrl(issue.repositoryUrl ?: "")
                        ?: "${credentials.owner}/${credentials.repositories.firstOrNull() ?: "unknown"}"
                    GitHubResponseMapper.mapIssue(issue, config, repoFullName)
                }
            }
        }
    }

    /**
     * Get issues for a specific repository.
     */
    suspend fun getIssues(
        config: TicketSystemConfig,
        owner: String,
        repo: String,
        maxResults: Int = 50
    ): Result<List<TicketIssue>> {
        Napier.d(tag = TAG) { "Fetching issues for $owner/$repo" }

        return executeRequest(config) {
            val url = buildApiUrl(config, "/repos/$owner/$repo/issues")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                configureHeaders()
                parameter("per_page", maxResults)
                parameter("state", "all")
                parameter("sort", "updated")
                parameter("direction", "desc")
            }

            handleResponse(response) {
                val issues: List<GitHubIssueResponse> = it.body()
                Napier.i(tag = TAG) { "Fetched ${issues.size} issues" }
                issues.map { issue ->
                    GitHubResponseMapper.mapIssue(issue, config, "$owner/$repo")
                }
            }
        }
    }

    /**
     * Get current user.
     */
    suspend fun getCurrentUser(config: TicketSystemConfig): Result<String> {
        return executeRequest(config) {
            val url = buildApiUrl(config, "/user")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                configureHeaders()
            }

            handleResponse(response) {
                val user: GitHubUserResponse = it.body()
                user.login
            }
        }
    }

    /**
     * Get issue by number.
     */
    suspend fun getIssueByNumber(
        config: TicketSystemConfig,
        owner: String,
        repo: String,
        number: Int
    ): Result<TicketIssue?> {
        Napier.d(tag = TAG) { "Fetching issue #$number from $owner/$repo" }

        return executeRequest(config) {
            val url = buildApiUrl(config, "/repos/$owner/$repo/issues/$number")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                configureHeaders()
            }

            when {
                response.status.isSuccess() -> {
                    val issue: GitHubIssueResponse = response.body()
                    GitHubResponseMapper.mapIssue(issue, config, "$owner/$repo")
                }
                response.status == HttpStatusCode.NotFound -> null
                else -> {
                    val error = response.bodyAsText()
                    throw IllegalStateException("GitHub API error: $error")
                }
            }
        }
    }

    private fun buildSearchQuery(credentials: TicketCredentials.GitHubToken, query: String): String {
        val parts = mutableListOf<String>()

        // Add text query if provided
        if (query.isNotBlank()) {
            parts.add(query)
        }

        // Filter by repositories if specified
        if (credentials.repositories.isNotEmpty()) {
            credentials.repositories.forEach { repo ->
                parts.add("repo:${credentials.owner}/$repo")
            }
        } else {
            // Default to user's issues
            parts.add("user:${credentials.owner}")
        }

        // Only issues, not PRs
        parts.add("is:issue")

        return parts.joinToString(" ")
    }

    private fun extractRepoFromUrl(url: String): String? {
        // URL format: https://api.github.com/repos/owner/repo
        val regex = Regex("/repos/([^/]+/[^/]+)")
        return regex.find(url)?.groupValues?.get(1)
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
            throw IllegalStateException("GitHub API error (${response.status}): $errorBody")
        }
    }

    private fun HttpRequestBuilder.configureAuth(credentials: TicketCredentials) {
        when (credentials) {
            is TicketCredentials.GitHubToken -> {
                bearerAuth(credentials.token)
            }
            else -> throw IllegalArgumentException("Invalid credentials type for GitHub")
        }
    }

    private fun HttpRequestBuilder.configureHeaders() {
        header("Accept", "application/vnd.github+json")
        header("X-GitHub-Api-Version", GITHUB_API_VERSION)
    }

    private fun buildApiUrl(config: TicketSystemConfig, path: String): String {
        val baseUrl = config.baseUrl.trimEnd('/')
        return "$baseUrl$path"
    }
}
