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
 * ## Store5 Design Decision
 * This repository uses a single-key store (empty string as key) because:
 * - All issues are fetched with a single JQL query
 * - The local database is the source of truth for all cached issues
 * - Local filtering is performed in JiraDatasource for search operations
 *
 * @property jiraDatasource Local database datasource for Jira issues
 * @property jiraClient Client for Jira API operations
 */
class JiraRepository(
    private val jiraDatasource: JiraDatasource,
    private val jiraClient: JiraClient
) {
    companion object {
        /**
         * Single key for the issues store.
         * We use a single-key approach since all issues are cached together.
         */
        private const val ISSUES_STORE_KEY = "all_issues"

        /**
         * Default JQL for fetching issues.
         */
        private const val DEFAULT_FETCH_JQL = "updated >= -30d ORDER BY updated DESC"

        /**
         * Maximum issues to fetch per sync.
         */
        private const val MAX_FETCH_RESULTS = 100
    }

    /**
     * Store5 configuration for Jira issues cache.
     *
     * - Fetcher: Fetches issues from Jira API using JiraClient with default JQL
     * - SourceOfTruth: Local SQLite database via JiraDatasource
     * - MemoryPolicy: 15-minute expiration for cached data
     */
    private val issuesStore = StoreBuilder
        .from(
            fetcher = Fetcher.of { _: String ->
                // Always fetch with the default JQL - local filtering handles search
                jiraClient.search(DEFAULT_FETCH_JQL, maxResults = MAX_FETCH_RESULTS).getOrThrow()
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: String -> jiraDatasource.getAll() },
                writer = { _: String, issues: List<JiraIssue> ->
                    jiraDatasource.insert(issues)
                },
                delete = { _: String -> jiraDatasource.deleteAll() },
                deleteAll = { jiraDatasource.deleteAll() }
            )
        )
        .cachePolicy(
            MemoryPolicy.builder<String, List<JiraIssue>>()
                .setExpireAfterWrite(15.minutes)
                .setMaxSize(1L) // Single key store
                .build()
        )
        .build()

    /**
     * Get all Jira issues with automatic refresh.
     *
     * Returns cached data if available and not expired, otherwise fetches from network.
     * Use this for displaying the full list of issues.
     *
     * @return Flow of all cached issues
     */
    fun getAllIssues(): Flow<List<JiraIssue>> {
        return issuesStore.stream(
            StoreReadRequest.cached(ISSUES_STORE_KEY, refresh = true)
        ).map { it.dataOrNull() ?: emptyList() }
    }

    /**
     * Search for Jira issues using JQL query.
     *
     * @param jql JQL query string (currently uses default, kept for API compatibility)
     * @return Flow of matching issues
     * @deprecated Use getAllIssues() for full list or searchCached() for filtering
     */
    @Suppress("UNUSED_PARAMETER")
    fun searchIssues(jql: String): Flow<List<JiraIssue>> {
        return getAllIssues()
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
     * Search cached issues by query string (local only).
     *
     * Searches both issue key and summary fields.
     * Useful for autocomplete in TimesheetInputField.
     *
     * @param query Search query
     * @param limit Maximum number of results (default 50)
     * @return Result with matching issues sorted by key
     */
    suspend fun searchCached(query: String, limit: Long = 50): Result<List<JiraIssue>> {
        return jiraDatasource.search(query, limit)
    }

    /**
     * Search issues with local-first strategy and remote fallback.
     *
     * First searches local cache. If no results found, queries remote Jira API.
     * Useful for JiraIssuePickerDialog where comprehensive search is needed.
     *
     * @param query Search query
     * @param limit Maximum number of results (default 50)
     * @return Result with matching issues sorted by key
     */
    suspend fun searchWithFallback(query: String, limit: Long = 100): Result<List<JiraIssue>> {
        // First: try local cache
        val localResult = jiraDatasource.search(query, limit)
        if (localResult.isSuccess && localResult.getOrNull()?.isNotEmpty() == true) {
            return localResult
        }

        // Fallback: query remote API with text search JQL
        val jql = "text ~ \"$query\" ORDER BY key ASC"
        return jiraClient.search(jql, maxResults = limit.toInt()).map { issues ->
            // Cache remote results for future local searches
            jiraDatasource.insert(issues)
            issues.sortedBy { it.key }
        }
    }

    /**
     * Force refresh of cached issues from Jira API.
     *
     * Clears cache and fetches fresh data.
     *
     * @param jql JQL query (ignored - uses default query)
     */
    @OptIn(ExperimentalStoreApi::class)
    @Suppress("UNUSED_PARAMETER")
    suspend fun invalidateCache(jql: String = "") {
        issuesStore.clear()
        issuesStore.fresh(ISSUES_STORE_KEY)
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
