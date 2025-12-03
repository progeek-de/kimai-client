package de.progeek.kimai.shared.core.ticketsystem.sync

import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketSystemRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.minutes

/**
 * Scheduler for automatic ticket issue cache synchronization.
 * Manages background sync jobs for multiple ticket system sources.
 */
class TicketSyncScheduler(
    private val configRepository: TicketConfigRepository,
    private val ticketRepository: TicketSystemRepository,
    private val dispatchers: KimaiDispatchers,
    private val scope: CoroutineScope
) {
    /** Per-source sync jobs */
    private val syncJobs = mutableMapOf<String, Job>()

    /** Config monitor job */
    private var monitorJob: Job? = null

    companion object {
        private const val TAG = "TicketSyncScheduler"
    }

    /**
     * Start the sync scheduler.
     * Monitors enabled configurations and starts/stops sync jobs accordingly.
     */
    fun start() {
        stop() // Ensure clean state

        monitorJob = scope.launch(dispatchers.io) {
            try {
                configRepository.getEnabledConfigs().collect { configs ->
                    Napier.d(tag = TAG) { "Config change detected: ${configs.size} enabled sources" }

                    // Stop jobs for configs that are no longer enabled
                    val activeIds = configs.map { it.id }.toSet()
                    syncJobs.keys.filter { it !in activeIds }.forEach { id ->
                        stopSyncForSource(id)
                    }

                    // Start jobs for newly enabled configs
                    configs.forEach { config ->
                        if (config.id !in syncJobs) {
                            startSyncForSource(config)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Error monitoring configs: ${e.message}" }
            }
        }

        Napier.d(tag = TAG) { "Ticket sync scheduler started" }
    }

    /**
     * Stop the sync scheduler.
     */
    fun stop() {
        syncJobs.values.forEach { it.cancel() }
        syncJobs.clear()
        monitorJob?.cancel()
        monitorJob = null
        Napier.d(tag = TAG) { "Ticket sync scheduler stopped" }
    }

    /**
     * Trigger an immediate sync for all enabled sources.
     */
    suspend fun syncAllNow(): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                Napier.d(tag = TAG) { "Starting immediate sync for all sources" }
                ticketRepository.refreshAllSources()
                val count = ticketRepository.getCachedIssueCount().getOrNull() ?: 0
                Napier.i(tag = TAG) { "Sync completed. Total cached issues: $count" }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Sync failed: ${e.message}" }
                Result.failure(e)
            }
        }
    }

    /**
     * Trigger an immediate sync for a specific source.
     */
    suspend fun syncSourceNow(sourceId: String): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                val config = configRepository.getConfigById(sourceId).first()
                    ?: return@withContext Result.failure(
                        IllegalArgumentException("Config not found: $sourceId")
                    )

                Napier.d(tag = TAG) { "Starting immediate sync for ${config.displayName}" }
                ticketRepository.refreshSource(config)
                val count = ticketRepository.getCachedIssueCountBySource(sourceId).getOrNull() ?: 0
                Napier.i(tag = TAG) { "Sync completed for ${config.displayName}. Cached issues: $count" }
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Sync failed for source $sourceId: ${e.message}" }
                Result.failure(e)
            }
        }
    }

    private fun startSyncForSource(config: TicketSystemConfig) {
        if (syncJobs.containsKey(config.id)) {
            return
        }

        val job = scope.launch(dispatchers.io) {
            try {
                Napier.d(tag = TAG) {
                    "Starting sync for ${config.displayName} (interval: ${config.syncIntervalMinutes}m)"
                }

                // Initial sync
                ticketRepository.refreshSource(config)

                // Periodic sync
                while (isActive) {
                    delay(config.syncIntervalMinutes.minutes)

                    // Re-fetch config to check for interval changes
                    val currentConfig = configRepository.getConfigById(config.id).first()
                    if (currentConfig == null || !currentConfig.enabled) {
                        Napier.d(tag = TAG) { "Config ${config.id} disabled or deleted, stopping sync" }
                        break
                    }

                    ticketRepository.refreshSource(currentConfig)
                    Napier.d(tag = TAG) { "Periodic sync completed for ${currentConfig.displayName}" }
                }
            } catch (e: CancellationException) {
                Napier.d(tag = TAG) { "Sync cancelled for ${config.displayName}" }
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) {
                    "Sync error for ${config.displayName}: ${e.message}"
                }
            }
        }

        syncJobs[config.id] = job
        Napier.d(tag = TAG) { "Started sync job for ${config.displayName}" }
    }

    private fun stopSyncForSource(sourceId: String) {
        syncJobs.remove(sourceId)?.cancel()
        Napier.d(tag = TAG) { "Stopped sync job for source $sourceId" }
    }
}
