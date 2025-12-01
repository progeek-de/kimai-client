package de.progeek.kimai.shared.core.jira.client

import de.progeek.kimai.shared.core.jira.client.models.JiraIssueResponse
import de.progeek.kimai.shared.core.jira.client.models.JiraProjectResponse
import de.progeek.kimai.shared.core.jira.client.models.JiraSearchResponse
import de.progeek.kimai.shared.core.jira.client.models.JiraUserResponse
import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import de.progeek.kimai.shared.core.jira.models.JiraProject
import de.progeek.kimai.shared.core.jira.models.SerializableAuthMethod
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
 * Client for interacting with Atlassian Jira Cloud REST API v3.
 *
 * This client provides a high-level interface for common Jira operations including:
 * - Testing connection and authentication
 * - Fetching projects
 * - Searching issues with JQL
 * - Retrieving user information
 * - Getting specific issues by key
 *
 * ## Authentication
 * Supports two authentication methods:
 * - **API Token**: For Jira Cloud (*.atlassian.net) - Basic Auth with email:token
 * - **Personal Access Token**: For Jira Server/Data Center - Bearer token auth
 *
 * ## Usage Example
 * ```kotlin
 * val client = JiraClient(credentialsProvider)
 *
 * // Test connection
 * val result = client.testConnection()
 * result.onSuccess { message ->
 *     println("Connected: $message")
 * }
 * ```
 *
 * @property credentialsProvider Provider for Jira credentials (single source of truth)
 */
class JiraClient(
    private val credentialsProvider: JiraCredentialsProvider
) {
    companion object {
        private const val TAG = "JiraClient"
        private const val API_VERSION = "3"
        private const val REQUEST_TIMEOUT_MS = 30_000L
        private const val CONNECT_TIMEOUT_MS = 10_000L

        /**
         * Fields to fetch for issue queries.
         */
        private const val ISSUE_FIELDS = "summary,status,assignee,project,issuetype,updated"

        /**
         * Default JQL query when none is provided.
         * The /search/jql endpoint requires a bounded query with restrictions.
         */
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

    // ========================================
    // Public API
    // ========================================

    /**
     * Test connection to Jira with current or provided credentials.
     *
     * Tests the connection by calling the /rest/api/3/myself endpoint which returns
     * the currently authenticated user information.
     *
     * @param testCredentials Optional credentials to test (if not provided, uses stored credentials)
     * @return Result with success message or error
     */
    suspend fun testConnection(testCredentials: JiraCredentials? = null): Result<String> {
        Napier.d(tag = TAG) { "Testing connection to Jira" }
        return executeRequest(testCredentials) { creds ->
            val url = buildApiUrl(creds, "/myself")
            Napier.d(tag = TAG) { "GET $url" }
            val response = httpClient.get(url) {
                configureAuth(creds)
                accept(ContentType.Application.Json)
            }

            handleResponse(response, creds) {
                val user: JiraUserResponse = it.body()
                Napier.i(tag = TAG) { "Connection successful: ${user.displayName}" }
                "Connected successfully as ${user.displayName}"
            }
        }
    }

    /**
     * Get Jira projects accessible to the current user.
     *
     * Uses the /rest/api/3/project endpoint to get all projects the user has permission to view.
     *
     * @return Result with list of projects or error
     */
    suspend fun getProjects(): Result<List<JiraProject>> {
        Napier.d(tag = TAG) { "Fetching Jira projects" }
        return executeRequest { creds ->
            val url = buildApiUrl(creds, "/project")
            Napier.d(tag = TAG) { "GET $url" }
            val response = httpClient.get(url) {
                configureAuth(creds)
                accept(ContentType.Application.Json)
            }

            handleResponse(response, creds) {
                val projects: List<JiraProjectResponse> = it.body()
                Napier.i(tag = TAG) { "Fetched ${projects.size} projects" }
                projects.map(JiraResponseMapper::mapProject)
            }
        }
    }

    /**
     * Search for Jira issues using JQL query.
     *
     * Uses the /rest/api/3/search/jql endpoint with JQL (Jira Query Language).
     * If an empty JQL is provided, uses a default query to get recently updated issues.
     *
     * @param jql JQL query string (e.g., "project = PROJ AND status = Open")
     * @param maxResults Maximum number of results (default 50)
     * @return Result with list of issues or error
     */
    suspend fun search(jql: String = "", maxResults: Int = 50): Result<List<JiraIssue>> {
        val effectiveJql = jql.ifBlank { DEFAULT_JQL }
        Napier.d(tag = TAG) { "Searching issues: jql=\"$effectiveJql\", maxResults=$maxResults" }
        return executeRequest { creds ->
            val url = buildApiUrl(creds, "/search/jql")
            Napier.d(tag = TAG) { "GET $url" }
            val response = httpClient.get(url) {
                configureAuth(creds)
                accept(ContentType.Application.Json)
                parameter("jql", effectiveJql)
                parameter("maxResults", maxResults)
                parameter("fields", ISSUE_FIELDS)
            }

            handleResponse(response, creds) {
                val searchResponse: JiraSearchResponse = it.body()
                Napier.i(tag = TAG) { "Search returned ${searchResponse.issues.size} issues" }
                searchResponse.issues.map(JiraResponseMapper::mapIssue)
            }
        }
    }

    /**
     * Get current user information.
     *
     * Uses the /rest/api/3/myself endpoint to get the currently authenticated user's information.
     *
     * @return Result with user display name or email
     */
    suspend fun getCurrentUser(): Result<String> {
        Napier.d(tag = TAG) { "Fetching current user info" }
        return executeRequest { creds ->
            val url = buildApiUrl(creds, "/myself")
            Napier.d(tag = TAG) { "GET $url" }
            val response = httpClient.get(url) {
                configureAuth(creds)
                accept(ContentType.Application.Json)
            }

            handleResponse(response, creds) {
                val user: JiraUserResponse = it.body()
                val result = user.emailAddress ?: user.displayName
                Napier.i(tag = TAG) { "Current user: $result" }
                result
            }
        }
    }

    /**
     * Get issue by key.
     *
     * Uses the /rest/api/3/issue/{issueIdOrKey} endpoint to fetch a specific issue.
     *
     * @param key Issue key (e.g., "PROJ-123")
     * @return Result with issue or null if not found
     */
    suspend fun getIssueByKey(key: String): Result<JiraIssue?> {
        Napier.d(tag = TAG) { "Fetching issue: $key" }
        return executeRequest { creds ->
            val url = buildApiUrl(creds, "/issue/$key")
            Napier.d(tag = TAG) { "GET $url" }
            val response = httpClient.get(url) {
                configureAuth(creds)
                accept(ContentType.Application.Json)
                parameter("fields", ISSUE_FIELDS)
            }

            when {
                response.status.isSuccess() -> {
                    val issue: JiraIssueResponse = response.body()
                    val mapped = JiraResponseMapper.mapIssue(issue)
                    Napier.i(tag = TAG) { "Found issue: ${mapped.key}" }
                    mapped
                }
                response.status == HttpStatusCode.NotFound -> {
                    Napier.w(tag = TAG) { "Issue not found: $key" }
                    null
                }
                else -> {
                    val error = createApiError(response, creds)
                    Napier.e(tag = TAG) { "Failed to fetch issue $key: $error" }
                    throw IllegalStateException(error)
                }
            }
        }
    }

    /**
     * Check if credentials are configured.
     */
    fun hasCredentials(): Boolean = credentialsProvider.hasCredentials()

    /**
     * Get current base URL.
     */
    fun getBaseUrl(): String? = credentialsProvider.getBaseUrl()

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * Execute a request with error handling.
     */
    private suspend fun <T> executeRequest(
        testCredentials: JiraCredentials? = null,
        block: suspend (JiraCredentials) -> T
    ): Result<T> {
        val creds = testCredentials ?: credentialsProvider.getCredentials()
        if (creds == null) {
            Napier.w(tag = TAG) { "No credentials configured" }
            return Result.failure(IllegalStateException("No credentials configured"))
        }

        return runCatching {
            block(creds)
        }.onFailure { error ->
            Napier.e(tag = TAG, throwable = error) { "Request failed: ${error.message}" }
        }
    }

    /**
     * Handle HTTP response and convert errors to exceptions.
     */
    private suspend fun <T> handleResponse(
        response: HttpResponse,
        credentials: JiraCredentials,
        onSuccess: suspend (HttpResponse) -> T
    ): T {
        return if (response.status.isSuccess()) {
            onSuccess(response)
        } else {
            val error = createApiError(response, credentials)
            Napier.e(tag = TAG) { "API error: $error" }
            throw IllegalStateException(error)
        }
    }

    /**
     * Create a detailed error message from API error response.
     */
    private suspend fun createApiError(
        response: HttpResponse,
        credentials: JiraCredentials
    ): String {
        val errorBody = try {
            response.bodyAsText()
        } catch (_: Exception) {
            "Unable to read error body"
        }

        return JiraErrorHandler.createErrorMessage(response, credentials, errorBody)
    }

    /**
     * Configure HTTP request with authentication headers based on credentials.
     */
    private fun HttpRequestBuilder.configureAuth(creds: JiraCredentials) {
        when (val authMethod = creds.authMethod) {
            is SerializableAuthMethod.ApiToken -> {
                // Basic Auth: Base64(email:apiToken)
                basicAuth(authMethod.email, authMethod.token)
            }
            is SerializableAuthMethod.PersonalAccessToken -> {
                // Bearer token auth
                bearerAuth(authMethod.token)
            }
        }
    }

    /**
     * Build the full API URL for a given endpoint.
     */
    private fun buildApiUrl(creds: JiraCredentials, path: String): String {
        var baseUrl = creds.baseUrl.trimEnd('/')

        // Remove /rest/api/X if user accidentally included it
        baseUrl = baseUrl.replace(Regex("/rest/api/\\d+$"), "")

        return "$baseUrl/rest/api/$API_VERSION$path"
    }
}
