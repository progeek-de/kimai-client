@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.ticketsystem.repository

import de.progeek.kimai.shared.core.ticketsystem.api.TicketSystemRegistry
import de.progeek.kimai.shared.core.ticketsystem.datasource.TicketIssueDatasource
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MemoryPolicy
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import kotlin.time.Duration.Companion.minutes

/**
 * Aggregating repository for fetching and caching issues from multiple ticket sources.
 * Each source has its own Store5 cache with independent refresh cycles.
 */
class TicketSystemRepository(
    private val configRepository: TicketConfigRepository,
    private val issueDatasource: TicketIssueDatasource,
    private val registry: TicketSystemRegistry
) {
    companion object {
        private const val DEFAULT_FETCH_JQL = "updated >= -30d ORDER BY updated DESC"
        private const val MAX_FETCH_RESULTS = 100
    }

    /**
     * Per-source Store5 instances for independent caching.
     */
    private val sourceStores = mutableMapOf<String, Store<String, List<TicketIssue>>>()

    /**
     * Get or create a Store5 instance for a specific source.
     */
    private fun getStoreForSource(config: TicketSystemConfig): Store<String, List<TicketIssue>> {
        return sourceStores.getOrPut(config.id) {
            StoreBuilder
                .from(
                    fetcher = Fetcher.of { _: String ->
                        val provider = registry.getProvider(config.provider)
                            ?: throw IllegalStateException("Provider ${config.provider} not registered")
                        provider.searchIssues(config, "", MAX_FETCH_RESULTS).getOrThrow()
                    },
                    sourceOfTruth = SourceOfTruth.of(
                        reader = { _: String -> issueDatasource.getBySource(config.id) },
                        writer = { _: String, issues: List<TicketIssue> ->
                            issueDatasource.insert(issues)
                        },
                        delete = { _: String ->
                            issueDatasource.deleteBySource(config.id)
                        },
                        deleteAll = { issueDatasource.deleteBySource(config.id) }
                    )
                )
                .cachePolicy(
                    MemoryPolicy.builder<String, List<TicketIssue>>()
                        .setExpireAfterWrite(config.syncIntervalMinutes.minutes)
                        .setMaxSize(1L)
                        .build()
                )
                .build()
        }
    }

    /**
     * Get all issues from all enabled sources.
     */
    fun getAllIssues(): Flow<List<TicketIssue>> = issueDatasource.getAll()

    /**
     * Get issues from a specific source.
     */
    fun getIssuesBySource(sourceId: String): Flow<List<TicketIssue>> =
        issueDatasource.getBySource(sourceId)

    /**
     * Search all enabled sources with aggregated results.
     * Performs parallel searches across all enabled sources.
     */
    suspend fun searchAllSources(
        query: String,
        limit: Int = 50
    ): Result<List<TicketIssue>> = coroutineScope {
        runCatching {
            val configs = configRepository.getEnabledConfigs().first()

            val results = configs.map { config ->
                async {
                    val provider = registry.getProvider(config.provider)
                    provider?.searchIssues(config, query, limit)?.getOrNull() ?: emptyList()
                }
            }.awaitAll()

            results.flatten()
                .distinctBy { "${it.sourceId}:${it.id}" }
                .sortedByDescending { it.updated }
                .take(limit)
        }
    }

    /**
     * Search with local-first strategy and remote fallback.
     * First searches local cache, then queries remotes if needed.
     */
    suspend fun searchWithFallback(query: String, limit: Int = 100): Result<List<TicketIssue>> {
        // First try local cache
        val localResult = issueDatasource.search(query, limit.toLong())
        if (localResult.isSuccess && localResult.getOrNull()?.isNotEmpty() == true) {
            return localResult
        }

        // Fallback to remote search
        return searchAllSources(query, limit).map { remoteIssues ->
            // Cache remote results
            if (remoteIssues.isNotEmpty()) {
                issueDatasource.insert(remoteIssues)
            }
            remoteIssues
        }
    }

    /**
     * Refresh cache for a specific source.
     */
    @OptIn(ExperimentalStoreApi::class)
    suspend fun refreshSource(config: TicketSystemConfig): Result<Unit> = runCatching {
        val store = getStoreForSource(config)
        store.clear()
        // Force a fresh fetch from the network
        store.stream(StoreReadRequest.fresh(config.id)).first { response ->
            response is StoreReadResponse.Data
        }
        Unit
    }

    /**
     * Refresh all enabled sources in parallel.
     */
    suspend fun refreshAllSources(): Result<Unit> = coroutineScope {
        runCatching {
            val configs = configRepository.getEnabledConfigs().first()
            configs.map { config ->
                async { refreshSource(config) }
            }.awaitAll()
            Unit
        }
    }

    /**
     * Clear cache for a specific source.
     */
    @OptIn(ExperimentalStoreApi::class)
    suspend fun clearSource(sourceId: String): Result<Unit> = runCatching {
        sourceStores[sourceId]?.clear()
        issueDatasource.deleteBySource(sourceId)
        Unit
    }

    /**
     * Test connection for a configuration.
     */
    suspend fun testConnection(config: TicketSystemConfig): Result<String> {
        val provider = registry.getProvider(config.provider)
            ?: return Result.failure(IllegalStateException("Provider ${config.provider} not registered"))
        return provider.testConnection(config)
    }

    /**
     * Get projects for a configuration.
     */
    suspend fun getProjects(config: TicketSystemConfig): Result<List<TicketProject>> {
        val provider = registry.getProvider(config.provider)
            ?: return Result.failure(IllegalStateException("Provider ${config.provider} not registered"))
        return provider.getProjects(config)
    }

    /**
     * Get an issue by key from any source.
     */
    suspend fun getIssueByKey(key: String): Result<TicketIssue?> =
        issueDatasource.getByKey(key)

    /**
     * Get total count of cached issues across all sources.
     */
    suspend fun getCachedIssueCount(): Result<Long> = issueDatasource.count()

    /**
     * Get count of cached issues for a specific source.
     */
    suspend fun getCachedIssueCountBySource(sourceId: String): Result<Long> =
        issueDatasource.countBySource(sourceId)

    /**
     * Check if any ticket systems are configured and enabled.
     */
    suspend fun hasEnabledSources(): Boolean =
        configRepository.countEnabled().getOrNull()?.let { it > 0 } ?: false
}
