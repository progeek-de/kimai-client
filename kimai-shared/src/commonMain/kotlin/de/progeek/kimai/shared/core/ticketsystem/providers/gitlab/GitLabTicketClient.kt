@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.ticketsystem.providers.gitlab

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
 * HTTP client for GitLab REST API v4.
 */
internal class GitLabTicketClient {

    companion object {
        private const val TAG = "GitLabTicketClient"
        private const val REQUEST_TIMEOUT_MS = 30_000L
        private const val CONNECT_TIMEOUT_MS = 10_000L
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
     * Test connection to GitLab.
     */
    suspend fun testConnection(config: TicketSystemConfig): Result<String> {
        Napier.d(tag = TAG) { "Testing connection to GitLab: ${config.baseUrl}" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/user")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
            }

            handleResponse(response) {
                val user: GitLabUserResponse = it.body()
                Napier.i(tag = TAG) { "Connection successful: ${user.username}" }
                "Connected as ${user.name} (@${user.username})"
            }
        }
    }

    /**
     * Get projects accessible to the user.
     */
    suspend fun getProjects(config: TicketSystemConfig): Result<List<TicketProject>> {
        Napier.d(tag = TAG) { "Fetching GitLab projects" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/projects")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                parameter("membership", true)
                parameter("per_page", 100)
                parameter("order_by", "last_activity_at")
            }

            handleResponse(response) {
                val projects: List<GitLabProjectResponse> = it.body()
                Napier.i(tag = TAG) { "Fetched ${projects.size} projects" }
                projects.map(GitLabResponseMapper::mapProject)
            }
        }
    }

    /**
     * Search for issues across projects.
     */
    suspend fun searchIssues(
        config: TicketSystemConfig,
        query: String = "",
        maxResults: Int = 50
    ): Result<List<TicketIssue>> {
        val credentials = config.credentials as? TicketCredentials.GitLabToken
            ?: return Result.failure(IllegalArgumentException("Invalid credentials type"))

        Napier.d(tag = TAG) { "Searching issues: query=\"$query\"" }

        return if (credentials.projectIds.isNotEmpty()) {
            // Fetch from specific projects
            fetchFromConfiguredProjects(config, credentials, query, maxResults)
        } else {
            // Global search
            searchGlobalIssues(config, query, maxResults)
        }
    }

    private suspend fun searchGlobalIssues(
        config: TicketSystemConfig,
        query: String,
        maxResults: Int
    ): Result<List<TicketIssue>> {
        return executeRequest(config) {
            val url = buildApiUrl(config, "/issues")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                parameter("scope", "all")
                parameter("state", "all")
                parameter("per_page", maxResults)
                parameter("order_by", "updated_at")
                parameter("sort", "desc")
                if (query.isNotBlank()) {
                    parameter("search", query)
                }
            }

            handleResponse(response) {
                val issues: List<GitLabIssueResponse> = it.body()
                Napier.i(tag = TAG) { "Search returned ${issues.size} issues" }
                issues.map { issue ->
                    GitLabResponseMapper.mapIssue(issue, config, issue.projectId.toString())
                }
            }
        }
    }

    private suspend fun fetchFromConfiguredProjects(
        config: TicketSystemConfig,
        credentials: TicketCredentials.GitLabToken,
        query: String,
        maxResults: Int
    ): Result<List<TicketIssue>> {
        val allIssues = mutableListOf<TicketIssue>()

        for (projectId in credentials.projectIds) {
            val result = getProjectIssues(config, projectId, query, maxResults)
            result.getOrNull()?.let { issues ->
                allIssues.addAll(issues)
            }
        }

        return Result.success(
            allIssues
                .distinctBy { it.id }
                .sortedByDescending { it.updated }
                .take(maxResults)
        )
    }

    /**
     * Get issues for a specific project.
     */
    suspend fun getProjectIssues(
        config: TicketSystemConfig,
        projectId: String,
        query: String = "",
        maxResults: Int = 50
    ): Result<List<TicketIssue>> {
        Napier.d(tag = TAG) { "Fetching issues for project $projectId" }

        return executeRequest(config) {
            val encodedProjectId = projectId.encodeURLPathPart()
            val url = buildApiUrl(config, "/projects/$encodedProjectId/issues")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                parameter("state", "all")
                parameter("per_page", maxResults)
                parameter("order_by", "updated_at")
                parameter("sort", "desc")
                if (query.isNotBlank()) {
                    parameter("search", query)
                }
            }

            handleResponse(response) {
                val issues: List<GitLabIssueResponse> = it.body()
                Napier.i(tag = TAG) { "Fetched ${issues.size} issues from project $projectId" }
                issues.map { issue ->
                    GitLabResponseMapper.mapIssue(issue, config, projectId)
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
            }

            handleResponse(response) {
                val user: GitLabUserResponse = it.body()
                user.username
            }
        }
    }

    /**
     * Get issue by IID in a specific project.
     */
    suspend fun getIssueByIid(
        config: TicketSystemConfig,
        projectId: String,
        iid: Int
    ): Result<TicketIssue?> {
        Napier.d(tag = TAG) { "Fetching issue #$iid from project $projectId" }

        return executeRequest(config) {
            val encodedProjectId = projectId.encodeURLPathPart()
            val url = buildApiUrl(config, "/projects/$encodedProjectId/issues/$iid")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
            }

            when {
                response.status.isSuccess() -> {
                    val issue: GitLabIssueResponse = response.body()
                    GitLabResponseMapper.mapIssue(issue, config, projectId)
                }
                response.status == HttpStatusCode.NotFound -> null
                else -> {
                    val error = response.bodyAsText()
                    throw IllegalStateException("GitLab API error: $error")
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
            throw IllegalStateException("GitLab API error (${response.status}): $errorBody")
        }
    }

    private fun HttpRequestBuilder.configureAuth(credentials: TicketCredentials) {
        when (credentials) {
            is TicketCredentials.GitLabToken -> {
                header("PRIVATE-TOKEN", credentials.token)
            }
            else -> throw IllegalArgumentException("Invalid credentials type for GitLab")
        }
    }

    private fun buildApiUrl(config: TicketSystemConfig, path: String): String {
        var baseUrl = config.baseUrl.trimEnd('/')
        // Remove /api/v4 if already included
        baseUrl = baseUrl.replace(Regex("/api/v\\d+$"), "")
        return "$baseUrl/api/v4$path"
    }
}
