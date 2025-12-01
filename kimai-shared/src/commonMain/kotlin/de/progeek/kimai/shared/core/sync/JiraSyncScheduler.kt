package de.progeek.kimai.shared.core.sync

import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.jira.repositories.JiraRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.minutes

/**
 * Scheduler for automatic Jira issue cache synchronization.
 *
 * Manages a background coroutine job that periodically refreshes the Jira issue cache
 * from the Jira API based on the configured sync interval in settings.
 *
 * The scheduler automatically:
 * - Starts when Jira integration is enabled
 * - Stops when Jira integration is disabled
 * - Adjusts interval when settings change
 * - Handles errors gracefully without blocking the UI
 * - Uses IO dispatcher for network operations
 *
 * @property jiraRepository Repository for Jira operations
 * @property settingsRepository Repository for accessing settings
 * @property dispatchers Coroutine dispatchers
 * @property scope Coroutine scope for scheduling (typically application-wide scope)
 */
class JiraSyncScheduler(
    private val jiraRepository: JiraRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: KimaiDispatchers,
    private val scope: CoroutineScope
) {
    private var syncJob: Job? = null
    private var monitorJob: Job? = null

    companion object {
        private const val TAG = "JiraSyncScheduler"
    }

    /**
     * Start the sync scheduler.
     *
     * This will:
     * 1. Monitor Jira enabled state
     * 2. Start background sync when enabled
     * 3. Cancel sync when disabled
     * 4. Update sync interval when settings change
     */
    fun start() {
        stop() // Ensure clean state

        monitorJob = scope.launch(dispatchers.io) {
            try {
                settingsRepository.getJiraEnabled().collect { enabled ->
                    if (enabled && jiraRepository.hasCredentials()) {
                        startSync()
                    } else {
                        stopSync()
                    }
                }
            } catch (e: Exception) {
                Napier.e(tag = TAG) { "Error monitoring Jira enabled state: ${e.message}" }
            }
        }

        Napier.d(tag = TAG) { "Jira sync scheduler started" }
    }

    /**
     * Stop the sync scheduler.
     *
     * Cancels both the monitoring job and any active sync job.
     */
    fun stop() {
        stopSync()
        monitorJob?.cancel()
        monitorJob = null
        Napier.d(tag = TAG) { "Jira sync scheduler stopped" }
    }

    /**
     * Trigger an immediate sync.
     *
     * This will perform a one-time sync regardless of the schedule.
     * Useful for manual refresh or initial sync.
     *
     * @return Result indicating success or failure
     */
    suspend fun syncNow(): Result<Unit> {
        return withContext(dispatchers.io) {
            try {
                Napier.d(tag = TAG) { "Starting immediate sync" }

                // Get default project key if configured
                val defaultProject = settingsRepository.getJiraDefaultProject().first()

                // Build JQL query
                val jql = buildJQL(defaultProject)

                // Invalidate cache and fetch fresh data
                jiraRepository.invalidateCache(jql)

                val count = jiraRepository.getCachedIssueCount().getOrNull() ?: 0
                Napier.i(tag = TAG) { "Sync completed successfully. Cached issues: $count" }

                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e // Don't catch cancellation
            } catch (e: Exception) {
                Napier.e(tag = TAG) { "Sync failed: ${e.message}" }
                Result.failure(e)
            }
        }
    }

    /**
     * Start the background sync job.
     *
     * Creates a repeating coroutine that syncs at the configured interval.
     */
    private fun startSync() {
        // Cancel existing sync if running
        stopSync()

        syncJob = scope.launch(dispatchers.io) {
            try {
                // Get initial sync interval
                var syncInterval = settingsRepository.getJiraSyncInterval().first()
                Napier.d(tag = TAG) { "Starting background sync with interval: $syncInterval minutes" }

                // Perform initial sync
                syncNow()

                // Monitor sync interval changes and perform periodic syncs
                settingsRepository.getJiraSyncInterval().collect { newInterval ->
                    if (newInterval != syncInterval) {
                        Napier.d(tag = TAG) { "Sync interval changed: $syncInterval -> $newInterval minutes" }
                        syncInterval = newInterval
                    }

                    // Wait for the interval before next sync
                    delay(syncInterval.minutes)

                    // Perform sync
                    syncNow()
                }
            } catch (e: CancellationException) {
                Napier.d(tag = TAG) { "Background sync cancelled" }
            } catch (e: Exception) {
                Napier.e(tag = TAG) { "Background sync error: ${e.message}" }
                // Continue running despite errors
            }
        }
    }

    /**
     * Stop the background sync job.
     */
    private fun stopSync() {
        syncJob?.cancel()
        syncJob = null
        Napier.d(tag = TAG) { "Background sync stopped" }
    }

    /**
     * Build JQL query for fetching issues.
     *
     * Includes:
     * - Issues assigned to current user
     * - Issues in default project (if configured)
     *
     * @param defaultProject Default project key from settings
     * @return JQL query string
     */
    private fun buildJQL(defaultProject: String?): String {
        return buildString {
            append("assignee = currentUser()")

            if (!defaultProject.isNullOrBlank()) {
                append(" OR project = ")
                append(defaultProject)
            }

            // Order by updated date descending to get most recent first
            append(" ORDER BY updated DESC")
        }
    }
}
