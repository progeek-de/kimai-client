package de.progeek.kimai.shared.core.ticketsystem.providers.jira

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
 * HTTP client for Jira REST API v3.
 */
internal class JiraTicketClient {

    companion object {
        private const val TAG = "JiraTicketClient"
        private const val API_VERSION = "3"
        private const val REQUEST_TIMEOUT_MS = 30_000L
        private const val CONNECT_TIMEOUT_MS = 10_000L
        private const val ISSUE_FIELDS = "summary,status,assignee,project,issuetype,updated"
        private const val DEFAULT_JQL = "updated >= -30d ORDER BY updated DESC"
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
     * Test connection to Jira.
     */
    suspend fun testConnection(config: TicketSystemConfig): Result<String> {
        Napier.d(tag = TAG) { "Testing connection to Jira: ${config.baseUrl}" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/myself")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                accept(ContentType.Application.Json)
            }

            handleResponse(response, config) {
                val user: JiraUserResponse = it.body()
                Napier.i(tag = TAG) { "Connection successful: ${user.displayName}" }
                "Connected successfully as ${user.displayName}"
            }
        }
    }

    /**
     * Get Jira projects.
     */
    suspend fun getProjects(config: TicketSystemConfig): Result<List<TicketProject>> {
        Napier.d(tag = TAG) { "Fetching Jira projects" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/project")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                accept(ContentType.Application.Json)
            }

            handleResponse(response, config) {
                val projects: List<JiraProjectResponse> = it.body()
                Napier.i(tag = TAG) { "Fetched ${projects.size} projects" }
                projects.map(JiraResponseMapper::mapProject)
            }
        }
    }

    /**
     * Search for Jira issues using JQL.
     */
    suspend fun search(
        config: TicketSystemConfig,
        jql: String = "",
        maxResults: Int = 50
    ): Result<List<TicketIssue>> {
        val effectiveJql = buildEffectiveJql(jql, config.defaultProjectKey)
        Napier.d(tag = TAG) { "Searching issues: jql=\"$effectiveJql\", maxResults=$maxResults" }

        return executeRequest(config) {
            val url = buildApiUrl(config, "/search/jql")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                accept(ContentType.Application.Json)
                parameter("jql", effectiveJql)
                parameter("maxResults", maxResults)
                parameter("fields", ISSUE_FIELDS)
            }

            handleResponse(response, config) {
                val searchResponse: JiraSearchResponse = it.body()
                Napier.i(tag = TAG) { "Search returned ${searchResponse.issues.size} issues" }
                searchResponse.issues.map { issue -> JiraResponseMapper.mapIssue(issue, config) }
            }
        }
    }

    /**
     * Get current user information.
     */
    suspend fun getCurrentUser(config: TicketSystemConfig): Result<String> {
        Napier.d(tag = TAG) { "Fetching current user info" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/myself")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                accept(ContentType.Application.Json)
            }

            handleResponse(response, config) {
                val user: JiraUserResponse = it.body()
                user.emailAddress ?: user.displayName
            }
        }
    }

    /**
     * Get issue by key.
     */
    suspend fun getIssueByKey(
        config: TicketSystemConfig,
        key: String
    ): Result<TicketIssue?> {
        Napier.d(tag = TAG) { "Fetching issue: $key" }
        return executeRequest(config) {
            val url = buildApiUrl(config, "/issue/$key")
            val response = httpClient.get(url) {
                configureAuth(config.credentials)
                accept(ContentType.Application.Json)
                parameter("fields", ISSUE_FIELDS)
            }

            when {
                response.status.isSuccess() -> {
                    val issue: JiraIssueResponse = response.body()
                    JiraResponseMapper.mapIssue(issue, config)
                }
                response.status == HttpStatusCode.NotFound -> null
                else -> {
                    val error = createApiError(response, config)
                    throw IllegalStateException(error)
                }
            }
        }
    }

    private fun buildEffectiveJql(query: String, defaultProject: String?): String {
        val baseJql = if (query.isBlank()) {
            DEFAULT_JQL
        } else if (query.contains("=") || query.contains("~")) {
            // Already a JQL query
            query
        } else {
            // Text search query
            "text ~ \"$query\" ORDER BY key ASC"
        }

        return if (defaultProject != null && !baseJql.contains("project", ignoreCase = true)) {
            "project = $defaultProject AND ($baseJql)"
        } else {
            baseJql
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
        config: TicketSystemConfig,
        onSuccess: suspend (HttpResponse) -> T
    ): T {
        return if (response.status.isSuccess()) {
            onSuccess(response)
        } else {
            val error = createApiError(response, config)
            Napier.e(tag = TAG) { "API error: $error" }
            throw IllegalStateException(error)
        }
    }

    private suspend fun createApiError(
        response: HttpResponse,
        config: TicketSystemConfig
    ): String {
        val errorBody = try {
            response.bodyAsText()
        } catch (_: Exception) {
            "Unable to read error body"
        }

        return JiraErrorHandler.createErrorMessage(response.status.value, config, errorBody)
    }

    private fun HttpRequestBuilder.configureAuth(credentials: TicketCredentials) {
        when (credentials) {
            is TicketCredentials.JiraApiToken -> {
                basicAuth(credentials.email, credentials.token)
            }
            is TicketCredentials.JiraPersonalAccessToken -> {
                bearerAuth(credentials.token)
            }
            else -> throw IllegalArgumentException("Invalid credentials type for Jira")
        }
    }

    private fun buildApiUrl(config: TicketSystemConfig, path: String): String {
        var baseUrl = config.baseUrl.trimEnd('/')
        baseUrl = baseUrl.replace(Regex("/rest/api/\\d+$"), "")
        return "$baseUrl/rest/api/$API_VERSION$path"
    }
}
