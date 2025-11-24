package de.progeek.kimai.shared.core.jira.client

import arrow.core.Either
import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.shared.core.jira.models.JiraAuthMethod
import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import de.progeek.kimai.shared.core.jira.models.JiraProject
import de.progeek.kimai.shared.core.jira.models.toSerializable
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Client for interacting with Jira API.
 *
 * This client wraps the kotlin-jira-api library and provides a simplified interface
 * for Kimai Client's needs. It handles authentication, credential storage, and common
 * Jira operations.
 *
 * Note: The actual kotlin-jira-api integration will be implemented once we verify
 * the library is compatible with the project setup. For now, this provides the interface.
 *
 * @property settings ObservableSettings for storing configuration
 * @property aesCipher Cipher for encrypting credentials
 */
class JiraClient(
    private val settings: ObservableSettings,
    private val aesCipher: AesGCMCipher
) {
    companion object {
        private const val JIRA_BASE_URL_KEY = "JIRA_BASE_URL"
        private const val JIRA_CREDENTIALS_KEY = "JIRA_CREDENTIALS"
        private const val JIRA_ENABLED_KEY = "JIRA_ENABLED"
    }

    private var credentials: JiraCredentials? = null
    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadCredentials()
    }

    /**
     * Load encrypted credentials from settings.
     *
     * Note: Currently stores credentials unencrypted as JSON string.
     * TODO: Extend AesGCMCipher to support string encryption or create dedicated cipher for Jira.
     */
    private fun loadCredentials() {
        val jsonStr = settings.getStringOrNull(JIRA_CREDENTIALS_KEY)
        jsonStr?.let {
            credentials = try {
                json.decodeFromString<JiraCredentials>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Save credentials to settings.
     *
     * Note: Currently stores credentials unencrypted as JSON string.
     * TODO: Extend AesGCMCipher to support string encryption or create dedicated cipher for Jira.
     */
    fun saveCredentials(newCredentials: JiraCredentials) {
        credentials = newCredentials
        val jsonStr = json.encodeToString(newCredentials)
        settings.putString(JIRA_CREDENTIALS_KEY, jsonStr)
        settings.putString(JIRA_BASE_URL_KEY, newCredentials.baseUrl)
    }

    /**
     * Clear stored credentials.
     */
    fun clearCredentials() {
        credentials = null
        settings.remove(JIRA_CREDENTIALS_KEY)
        settings.remove(JIRA_BASE_URL_KEY)
    }

    /**
     * Test connection to Jira with current credentials.
     *
     * @return Result with success or error message
     */
    suspend fun testConnection(): Result<String> {
        return kotlin.runCatching {
            val creds = credentials ?: throw IllegalStateException("No credentials configured")

            // TODO: Implement actual Jira API connection test using kotlin-jira-api
            // For now, return placeholder
            // This will be implemented when integrating the actual library

            "Connection test not yet implemented - kotlin-jira-api integration pending"
        }
    }

    /**
     * Search for Jira issues using JQL query.
     *
     * @param jql JQL query string
     * @param maxResults Maximum number of results (default 50)
     * @return Result with list of issues or error
     */
    suspend fun searchIssues(jql: String, maxResults: Int = 50): Result<List<JiraIssue>> {
        return kotlin.runCatching {
            val creds = credentials ?: throw IllegalStateException("No credentials configured")

            // TODO: Implement actual Jira API search using kotlin-jira-api
            // For now, return empty list
            // This will be implemented when integrating the actual library

            emptyList()
        }
    }

    /**
     * Get Jira projects accessible to the current user.
     *
     * @return Result with list of projects or error
     */
    suspend fun getProjects(): Result<List<JiraProject>> {
        return kotlin.runCatching {
            val creds = credentials ?: throw IllegalStateException("No credentials configured")

            // TODO: Implement actual Jira API project fetch using kotlin-jira-api
            // For now, return empty list
            // This will be implemented when integrating the actual library

            emptyList()
        }
    }

    /**
     * Get current user information.
     *
     * @return Result with user email/username or error
     */
    suspend fun getCurrentUser(): Result<String> {
        return kotlin.runCatching {
            val creds = credentials ?: throw IllegalStateException("No credentials configured")

            // TODO: Implement actual Jira API user fetch using kotlin-jira-api
            // For now, return placeholder
            // This will be implemented when integrating the actual library

            when (val authMethod = creds.authMethod) {
                is de.progeek.kimai.shared.core.jira.models.SerializableAuthMethod.ApiToken -> authMethod.email
                is de.progeek.kimai.shared.core.jira.models.SerializableAuthMethod.PersonalAccessToken -> "User (PAT)"
            }
        }
    }

    /**
     * Get issue by key.
     *
     * @param key Issue key (e.g., "PROJ-123")
     * @return Result with issue or error
     */
    suspend fun getIssueByKey(key: String): Result<JiraIssue?> {
        return kotlin.runCatching {
            val creds = credentials ?: throw IllegalStateException("No credentials configured")

            // TODO: Implement actual Jira API issue fetch using kotlin-jira-api
            // For now, return null
            // This will be implemented when integrating the actual library

            null
        }
    }

    /**
     * Check if credentials are configured.
     */
    fun hasCredentials(): Boolean = credentials != null

    /**
     * Get current base URL.
     */
    fun getBaseUrl(): String? = credentials?.baseUrl
}
