package de.progeek.kimai.shared.core.ticketsystem.providers.gitlab

import de.progeek.kimai.shared.core.ticketsystem.api.TicketSystemProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig

/**
 * GitLab implementation of TicketSystemProvider.
 */
class GitLabTicketProvider : TicketSystemProvider {

    override val providerType: TicketProvider = TicketProvider.GITLAB

    private val client = GitLabTicketClient()

    override suspend fun testConnection(config: TicketSystemConfig): Result<String> {
        validateConfig(config)
        return client.testConnection(config)
    }

    override suspend fun searchIssues(
        config: TicketSystemConfig,
        query: String,
        maxResults: Int
    ): Result<List<TicketIssue>> {
        validateConfig(config)
        return client.searchIssues(config, query, maxResults)
    }

    override suspend fun getIssueByKey(
        config: TicketSystemConfig,
        key: String
    ): Result<TicketIssue?> {
        validateConfig(config)

        // Key format is "#123" - extract IID
        val iid = key.removePrefix("#").toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid issue key: $key"))

        val credentials = config.credentials as TicketCredentials.GitLabToken

        // Try configured projects first
        for (projectId in credentials.projectIds) {
            val result = client.getIssueByIid(config, projectId, iid)
            if (result.isSuccess && result.getOrNull() != null) {
                return result
            }
        }

        return Result.success(null)
    }

    override suspend fun getProjects(config: TicketSystemConfig): Result<List<TicketProject>> {
        validateConfig(config)
        return client.getProjects(config)
    }

    override suspend fun getCurrentUser(config: TicketSystemConfig): Result<String> {
        validateConfig(config)
        return client.getCurrentUser(config)
    }

    override fun validateCredentials(config: TicketSystemConfig): Boolean {
        return when (val creds = config.credentials) {
            is TicketCredentials.GitLabToken -> creds.token.isNotBlank()
            else -> false
        }
    }

    override fun getErrorMessage(exception: Throwable, config: TicketSystemConfig): String {
        return when {
            exception.message?.contains("401", ignoreCase = true) == true ->
                "Authentication failed. Please check your GitLab Personal Access Token."
            exception.message?.contains("403", ignoreCase = true) == true ->
                "Access denied. Your token may not have sufficient permissions (api or read_api scope required)."
            exception.message?.contains("404", ignoreCase = true) == true ->
                "Resource not found. Please check the project ID or URL."
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Connection timed out. Please check your network."
            else -> exception.message ?: "Unknown error occurred"
        }
    }

    private fun validateConfig(config: TicketSystemConfig) {
        require(config.provider == TicketProvider.GITLAB) {
            "Invalid provider type: ${config.provider}"
        }
        require(config.credentials is TicketCredentials.GitLabToken) {
            "Invalid credentials type for GitLab provider"
        }
    }
}
