package de.progeek.kimai.shared.core.ticketsystem.providers.github

import de.progeek.kimai.shared.core.ticketsystem.api.TicketSystemProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig

/**
 * GitHub implementation of TicketSystemProvider.
 */
class GitHubTicketProvider : TicketSystemProvider {

    override val providerType: TicketProvider = TicketProvider.GITHUB

    private val client = GitHubTicketClient()

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

        val credentials = config.credentials as TicketCredentials.GitHubToken

        // If specific repos are configured, fetch from each
        return if (credentials.repositories.isNotEmpty()) {
            fetchFromConfiguredRepos(config, credentials, query, maxResults)
        } else {
            // Use search API for broader search
            client.searchIssues(config, query, maxResults)
        }
    }

    private suspend fun fetchFromConfiguredRepos(
        config: TicketSystemConfig,
        credentials: TicketCredentials.GitHubToken,
        query: String,
        maxResults: Int
    ): Result<List<TicketIssue>> {
        val allIssues = mutableListOf<TicketIssue>()

        for (repo in credentials.repositories) {
            val result = if (query.isBlank()) {
                client.getIssues(config, credentials.owner, repo, maxResults)
            } else {
                client.searchIssues(config, "$query repo:${credentials.owner}/$repo", maxResults)
            }

            result.getOrNull()?.let { issues ->
                allIssues.addAll(issues)
            }
        }

        return Result.success(
            allIssues
                .distinctBy { it.id }
                .sortedByDescending { it.updated }
                .take(maxResults)
        )
    }

    override suspend fun getIssueByKey(
        config: TicketSystemConfig,
        key: String
    ): Result<TicketIssue?> {
        validateConfig(config)

        // Key format is "#123" - extract number
        val number = key.removePrefix("#").toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid issue key: $key"))

        val credentials = config.credentials as TicketCredentials.GitHubToken

        // Need to know which repo - try configured repos first
        for (repo in credentials.repositories) {
            val result = client.getIssueByNumber(config, credentials.owner, repo, number)
            if (result.isSuccess && result.getOrNull() != null) {
                return result
            }
        }

        return Result.success(null)
    }

    override suspend fun getProjects(config: TicketSystemConfig): Result<List<TicketProject>> {
        validateConfig(config)
        return client.getRepositories(config)
    }

    override suspend fun getCurrentUser(config: TicketSystemConfig): Result<String> {
        validateConfig(config)
        return client.getCurrentUser(config)
    }

    override fun validateCredentials(config: TicketSystemConfig): Boolean {
        return when (val creds = config.credentials) {
            is TicketCredentials.GitHubToken ->
                creds.token.isNotBlank() && creds.owner.isNotBlank()
            else -> false
        }
    }

    override fun getErrorMessage(exception: Throwable, config: TicketSystemConfig): String {
        return when {
            exception.message?.contains("401", ignoreCase = true) == true ->
                "Authentication failed. Please check your GitHub Personal Access Token."
            exception.message?.contains("403", ignoreCase = true) == true ->
                "Access denied. Your token may not have sufficient permissions."
            exception.message?.contains("404", ignoreCase = true) == true ->
                "Resource not found. Please check the owner and repository names."
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Connection timed out. Please check your network."
            else -> exception.message ?: "Unknown error occurred"
        }
    }

    private fun validateConfig(config: TicketSystemConfig) {
        require(config.provider == TicketProvider.GITHUB) {
            "Invalid provider type: ${config.provider}"
        }
        require(config.credentials is TicketCredentials.GitHubToken) {
            "Invalid credentials type for GitHub provider"
        }
    }
}
