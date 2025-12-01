package de.progeek.kimai.shared.core.jira.client

import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Implementation of JiraCredentialsProvider backed by SettingsRepository.
 *
 * This provides a single source of truth for Jira credentials,
 * with proper AES-GCM encryption handled by SettingsRepository.
 *
 * The provider maintains a local cache that is automatically synced
 * when credentials change in the repository.
 */
class SettingsJiraCredentialsProvider(
    private val settingsRepository: SettingsRepository
) : JiraCredentialsProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var cachedCredentials: JiraCredentials? = null

    @Volatile
    private var initialized = false

    init {
        // Observe credentials changes and update cache automatically
        scope.launch {
            settingsRepository.getJiraCredentials().collect { credentials ->
                cachedCredentials = credentials
                initialized = true
            }
        }
    }

    override fun getCredentials(): JiraCredentials? {
        // If not yet initialized by Flow, do a blocking read
        if (!initialized) {
            cachedCredentials = runBlocking {
                settingsRepository.getJiraCredentials().first()
            }
            initialized = true
        }
        return cachedCredentials
    }

    override fun observeCredentials(): Flow<JiraCredentials?> {
        return settingsRepository.getJiraCredentials()
    }

    override fun hasCredentials(): Boolean {
        return getCredentials() != null
    }
}
