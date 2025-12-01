package de.progeek.kimai.shared.core.database.datasource.jira

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.progeek.kimai.shared.core.database.KimaiDatabase
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import de.progeek.kimai.shared.kimaiDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Datasource for Jira issue database operations.
 *
 * Provides reactive access to locally cached Jira issues using SQLDelight.
 */
class JiraDatasource(val database: KimaiDatabase) {

    private val query get() = database.jiraIssueEntityQueries

    /**
     * Get all cached Jira issues as a reactive Flow.
     *
     * Issues are sorted by key with natural ordering (PROJ-2 before PROJ-10).
     */
    fun getAll(): Flow<List<JiraIssue>> =
        query.getAll(::toJiraIssue)
            .asFlow()
            .mapToList(kimaiDispatchers.io)
            .map { it.sortedByKey() }

    /**
     * Get a specific Jira issue by its key.
     *
     * @param key Issue key (e.g., "PROJ-123")
     * @return Result with issue or null if not found
     */
    suspend fun getByKey(key: String): Result<JiraIssue?> = withContext(kimaiDispatchers.io) {
        runCatching {
            query.getByKey(key, ::toJiraIssue).executeAsOneOrNull()
        }
    }

    /**
     * Get all issues for a specific project.
     *
     * @param projectKey Jira project key
     * @return Flow of issues for the project, sorted by key
     */
    fun getByProject(projectKey: String): Flow<List<JiraIssue>> =
        query.getByProject(projectKey, ::toJiraIssue)
            .asFlow()
            .mapToList(kimaiDispatchers.io)
            .map { it.sortedByKey() }

    /**
     * Get all issues assigned to a specific user.
     *
     * @param assignee User email or username
     * @return Flow of issues assigned to the user, sorted by key
     */
    fun getByAssignee(assignee: String): Flow<List<JiraIssue>> =
        query.getByAssignee(assignee, ::toJiraIssue)
            .asFlow()
            .mapToList(kimaiDispatchers.io)
            .map { it.sortedByKey() }

    /**
     * Search for issues matching a query string.
     *
     * Searches both issue key and summary fields.
     *
     * @param searchQuery Query string
     * @param limit Maximum number of results (default 50)
     * @return Result with list of matching issues, sorted by key
     */
    suspend fun search(searchQuery: String, limit: Long = 50): Result<List<JiraIssue>> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.search(searchQuery, limit, ::toJiraIssue).executeAsList().sortedByKey()
            }
        }

    /**
     * Insert a single Jira issue into the database.
     *
     * Uses INSERT OR REPLACE to update existing issues.
     *
     * @param issue Issue to insert
     * @return Result with the inserted issue
     */
    suspend fun insert(issue: JiraIssue): Result<JiraIssue> = withContext(kimaiDispatchers.io) {
        runCatching {
            query.insert(
                key = issue.key,
                summary = issue.summary,
                status = issue.status,
                projectKey = issue.projectKey,
                projectName = issue.projectName,
                issueType = issue.issueType,
                assignee = issue.assignee,
                updated = issue.updated.toEpochMilliseconds()
            )
            issue
        }
    }

    /**
     * Insert multiple Jira issues in a single transaction.
     *
     * Uses INSERT OR REPLACE to update existing issues.
     *
     * @param issues List of issues to insert
     * @return Result with the inserted issues
     */
    suspend fun insert(issues: List<JiraIssue>): Result<List<JiraIssue>> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.transaction {
                    issues.forEach { issue ->
                        query.insert(
                            key = issue.key,
                            summary = issue.summary,
                            status = issue.status,
                            projectKey = issue.projectKey,
                            projectName = issue.projectName,
                            issueType = issue.issueType,
                            assignee = issue.assignee,
                            updated = issue.updated.toEpochMilliseconds()
                        )
                    }
                }
                issues
            }
        }

    /**
     * Delete a specific issue by key.
     *
     * @param key Issue key to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteByKey(key: String): Result<Unit> = withContext(kimaiDispatchers.io) {
        runCatching {
            query.deleteByKey(key)
        }
    }

    /**
     * Delete all cached Jira issues.
     */
    fun deleteAll() {
        query.deleteAll()
    }

    /**
     * Get the total count of cached issues.
     *
     * @return Result with the count
     */
    suspend fun count(): Result<Long> = withContext(kimaiDispatchers.io) {
        runCatching {
            query.count().executeAsOne()
        }
    }

    companion object {
        /**
         * Sort issues by key with natural ordering descending (PROJ-10 before PROJ-2).
         */
        private fun List<JiraIssue>.sortedByKey(): List<JiraIssue> = sortedWith(
            compareByDescending<JiraIssue> { it.key.substringBefore("-") }
                .thenByDescending { it.key.substringAfter("-").toIntOrNull() ?: 0 }
        )
    }
}
