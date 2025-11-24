package de.progeek.kimai.shared.core.jira.repositories

import de.progeek.kimai.shared.core.database.datasource.jira.JiraDatasource
import de.progeek.kimai.shared.core.jira.client.JiraClient
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import de.progeek.kimai.shared.core.jira.models.JiraProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.*
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import kotlin.time.Duration.Companion.minutes

/**
 * Repository for Jira issue data with offline-first caching.
 *
 * Uses Store5 for cache management with 15-minute expiration policy.
 * Data flows from JiraClient (network) → JiraDatasource (local cache) → UI
 *
 * @property jiraDatasource Local database datasource for Jira issues
 * @property jiraClient Client for Jira API operations
 */
class JiraRepository(
    private val jiraDatasource: JiraDatasource,
    private val jiraClient: JiraClient
) {

    /**
     * Store5 configuration for Jira issues cache.
     *
     * - Fetcher: Fetches issues from Jira API using JiraClient
     * - SourceOfTruth: Local SQLite database via JiraDatasource
     * - MemoryPolicy: 15-minute expiration for cached data
     */
    private val issuesStore = StoreBuilder
        .from(
            fetcher = Fetcher.of { query: String ->
                // Fetch from Jira API
                // TODO: Replace with actual JQL when kotlin-jira-api is integrated
                jiraClient.searchIssues(query, maxResults = 100).getOrThrow()
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: String -> jiraDatasource.getAll() },
                writer = { _: String, issues: List<JiraIssue> ->
                    jiraDatasource.insert(issues)
                },
                deleteAll = { jiraDatasource.deleteAll() }
            )
        )
        .cachePolicy(
            MemoryPolicy.builder<String, List<JiraIssue>>()
                .setExpireAfterWrite(15.minutes)
                .build()
        )
        .build()

    /**
     * Search for Jira issues using JQL query.
     *
     * Returns cached data if available and not expired, otherwise fetches from network.
     *
     * @param jql JQL query string (e.g., "project = PROJ AND status = 'In Progress'")
     * @return Flow of matching issues
     */
    fun searchIssues(jql: String): Flow<List<JiraIssue>> {
        return issuesStore.stream(
            StoreReadRequest.cached(jql, refresh = false)
        ).map { it.dataOrNull() ?: emptyList() }
    }

    /**
     * Get all cached Jira issues.
     *
     * Returns issues from local database without network fetch.
     * Useful for autocomplete search when offline.
     *
     * @return Flow of all cached issues
     */
    fun getAllCachedIssues(): Flow<List<JiraIssue>> {
        return jiraDatasource.getAll()
    }

    /**
     * Get a specific issue by key from cache.
     *
     * @param key Issue key (e.g., "PROJ-123")
     * @return Result with issue or null if not found
     */
    suspend fun getIssueByKey(key: String): Result<JiraIssue?> {
        return jiraDatasource.getByKey(key)
    }

    /**
     * Get issues for a specific project from cache.
     *
     * @param projectKey Jira project key
     * @return Flow of issues for the project
     */
    fun getIssuesByProject(projectKey: String): Flow<List<JiraIssue>> {
        return jiraDatasource.getByProject(projectKey)
    }

    /**
     * Get issues assigned to a specific user from cache.
     *
     * @param assignee User email or username
     * @return Flow of issues assigned to the user
     */
    fun getIssuesByAssignee(assignee: String): Flow<List<JiraIssue>> {
        return jiraDatasource.getByAssignee(assignee)
    }

    /**
     * Search cached issues by query string.
     *
     * Searches both issue key and summary fields.
     * Useful for local autocomplete when offline.
     *
     * @param query Search query
     * @param limit Maximum number of results (default 50)
     * @return Result with matching issues
     */
    suspend fun searchCached(query: String, limit: Long = 50): Result<List<JiraIssue>> {
        return jiraDatasource.search(query, limit)
    }

    /**
     * Force refresh of cached issues from Jira API.
     *
     * Clears cache and fetches fresh data.
     *
     * @param jql JQL query to refresh (default: empty string for default query)
     */
    @OptIn(ExperimentalStoreApi::class)
    suspend fun invalidateCache(jql: String = "") {
        issuesStore.clear()
        issuesStore.fresh(jql)
    }

    /**
     * Get Jira projects accessible to the current user.
     *
     * Note: This bypasses Store5 since projects are less frequently accessed.
     *
     * @return Result with list of projects
     */
    suspend fun getProjects(): Result<List<JiraProject>> {
        return jiraClient.getProjects()
    }

    /**
     * Test connection to Jira with current credentials.
     *
     * @return Result with success message or error
     */
    suspend fun testConnection(): Result<String> {
        return jiraClient.testConnection()
    }

    /**
     * Get current Jira user information.
     *
     * @return Result with user email/username
     */
    suspend fun getCurrentUser(): Result<String> {
        return jiraClient.getCurrentUser()
    }

    /**
     * Check if Jira credentials are configured.
     *
     * @return True if credentials exist
     */
    fun hasCredentials(): Boolean {
        return jiraClient.hasCredentials()
    }

    /**
     * Get configured Jira base URL.
     *
     * @return Base URL or null if not configured
     */
    fun getBaseUrl(): String? {
        return jiraClient.getBaseUrl()
    }

    /**
     * Get count of cached issues.
     *
     * @return Result with count
     */
    suspend fun getCachedIssueCount(): Result<Long> {
        return jiraDatasource.count()
    }
}
