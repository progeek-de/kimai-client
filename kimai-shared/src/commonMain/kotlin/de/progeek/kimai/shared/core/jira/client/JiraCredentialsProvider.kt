package de.progeek.kimai.shared.core.jira.client

import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import kotlinx.coroutines.flow.Flow

/**
 * Provider interface for Jira credentials.
 *
 * This interface abstracts credential storage from the JiraClient,
 * following the Dependency Inversion Principle (DIP).
 * The actual storage implementation is handled by SettingsRepository.
 */
interface JiraCredentialsProvider {
    /**
     * Get current credentials synchronously.
     * Returns null if no credentials are configured.
     */
    fun getCredentials(): JiraCredentials?

    /**
     * Observe credentials changes as a Flow.
     */
    fun observeCredentials(): Flow<JiraCredentials?>

    /**
     * Check if credentials are configured.
     */
    fun hasCredentials(): Boolean = getCredentials() != null

    /**
     * Get the configured base URL.
     */
    fun getBaseUrl(): String? = getCredentials()?.baseUrl
}
