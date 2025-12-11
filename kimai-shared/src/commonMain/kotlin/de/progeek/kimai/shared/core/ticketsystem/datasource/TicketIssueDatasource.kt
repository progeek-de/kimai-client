@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.ticketsystem.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.progeek.kimai.shared.core.database.KimaiDatabase
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.kimaiDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant

/**
 * Datasource for unified ticket issue database operations.
 * Supports issues from multiple providers and sources.
 */
class TicketIssueDatasource(private val database: KimaiDatabase) {

    private val query get() = database.ticketIssueEntityQueries

    /**
     * Get all cached issues from all sources.
     */
    fun getAll(): Flow<List<TicketIssue>> =
        query.getAll(::toTicketIssue)
            .asFlow()
            .mapToList(kimaiDispatchers.io)

    /**
     * Get all issues for a specific source.
     */
    fun getBySource(sourceId: String): Flow<List<TicketIssue>> =
        query.getBySource(sourceId, ::toTicketIssue)
            .asFlow()
            .mapToList(kimaiDispatchers.io)
            .map { it.sortedByKey() }

    /**
     * Get a specific issue by key.
     */
    suspend fun getByKey(key: String): Result<TicketIssue?> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.getByKey(key, ::toTicketIssue).executeAsOneOrNull()
            }
        }

    /**
     * Get a specific issue by source and key.
     */
    suspend fun getBySourceAndKey(sourceId: String, key: String): Result<TicketIssue?> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.getBySourceAndKey(sourceId, key, ::toTicketIssue).executeAsOneOrNull()
            }
        }

    /**
     * Get all issues for a specific project.
     */
    fun getByProject(projectKey: String): Flow<List<TicketIssue>> =
        query.getByProject(projectKey, ::toTicketIssue)
            .asFlow()
            .mapToList(kimaiDispatchers.io)
            .map { it.sortedByKey() }

    /**
     * Get all issues assigned to a specific user.
     */
    fun getByAssignee(assignee: String): Flow<List<TicketIssue>> =
        query.getByAssignee(assignee, ::toTicketIssue)
            .asFlow()
            .mapToList(kimaiDispatchers.io)
            .map { it.sortedByKey() }

    /**
     * Search for issues across all sources.
     */
    suspend fun search(searchQuery: String, limit: Long = 50): Result<List<TicketIssue>> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.search(searchQuery, limit, ::toTicketIssue).executeAsList()
            }
        }

    /**
     * Search for issues within a specific source.
     */
    suspend fun searchBySource(
        sourceId: String,
        searchQuery: String,
        limit: Long = 50
    ): Result<List<TicketIssue>> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.searchBySource(sourceId, searchQuery, limit, ::toTicketIssue)
                    .executeAsList()
                    .sortedByKey()
            }
        }

    /**
     * Insert a single issue.
     */
    suspend fun insert(issue: TicketIssue): Result<TicketIssue> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.insert(
                    id = issue.id,
                    sourceId = issue.sourceId,
                    key = issue.key,
                    summary = issue.summary,
                    status = issue.status,
                    projectKey = issue.projectKey,
                    projectName = issue.projectName,
                    issueType = issue.issueType,
                    assignee = issue.assignee,
                    updated = issue.updated.toEpochMilliseconds(),
                    provider = issue.provider.name,
                    webUrl = issue.webUrl
                )
                issue
            }
        }

    /**
     * Insert multiple issues in a transaction.
     */
    suspend fun insert(issues: List<TicketIssue>): Result<List<TicketIssue>> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.transaction {
                    issues.forEach { issue ->
                        query.insert(
                            id = issue.id,
                            sourceId = issue.sourceId,
                            key = issue.key,
                            summary = issue.summary,
                            status = issue.status,
                            projectKey = issue.projectKey,
                            projectName = issue.projectName,
                            issueType = issue.issueType,
                            assignee = issue.assignee,
                            updated = issue.updated.toEpochMilliseconds(),
                            provider = issue.provider.name,
                            webUrl = issue.webUrl
                        )
                    }
                }
                issues
            }
        }

    /**
     * Delete all issues for a specific source.
     */
    suspend fun deleteBySource(sourceId: String): Result<Unit> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.deleteBySource(sourceId)
                Unit
            }
        }

    /**
     * Delete a specific issue by key.
     */
    suspend fun deleteByKey(key: String): Result<Unit> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.deleteByKey(key)
                Unit
            }
        }

    /**
     * Delete all cached issues.
     */
    fun deleteAll() {
        query.deleteAll()
    }

    /**
     * Get total count of cached issues.
     */
    suspend fun count(): Result<Long> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.count().executeAsOne()
            }
        }

    /**
     * Get count of issues for a specific source.
     */
    suspend fun countBySource(sourceId: String): Result<Long> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.countBySource(sourceId).executeAsOne()
            }
        }

    companion object {
        /**
         * Sort issues by key with natural ordering (PROJ-10 after PROJ-9).
         */
        private fun List<TicketIssue>.sortedByKey(): List<TicketIssue> = sortedWith(
            compareByDescending<TicketIssue> { it.key.substringBefore("-").substringBefore("#") }
                .thenByDescending {
                    it.key.substringAfter("-").substringAfter("#").toIntOrNull() ?: 0
                }
        )

        /**
         * Map database row to TicketIssue domain model.
         */
        private fun toTicketIssue(
            id: String,
            sourceId: String,
            key: String,
            summary: String,
            status: String,
            projectKey: String,
            projectName: String,
            issueType: String,
            assignee: String?,
            updated: Long,
            provider: String,
            webUrl: String?
        ): TicketIssue = TicketIssue(
            id = id,
            sourceId = sourceId,
            key = key,
            summary = summary,
            status = status,
            projectKey = projectKey,
            projectName = projectName,
            issueType = issueType,
            assignee = assignee,
            updated = Instant.fromEpochMilliseconds(updated),
            provider = TicketProvider.fromString(provider) ?: TicketProvider.JIRA,
            webUrl = webUrl
        )
    }
}
