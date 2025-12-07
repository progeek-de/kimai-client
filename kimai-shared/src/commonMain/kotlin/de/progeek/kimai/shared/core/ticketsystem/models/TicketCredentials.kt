package de.progeek.kimai.shared.core.ticketsystem.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sealed class for provider-specific authentication credentials.
 * Each provider has different authentication requirements.
 */
@Serializable
sealed class TicketCredentials {

    /**
     * Jira Cloud authentication using email and API token.
     * Used for *.atlassian.net instances.
     */
    @Serializable
    @SerialName("jira_api_token")
    data class JiraApiToken(
        val email: String,
        val token: String
    ) : TicketCredentials()

    /**
     * Jira Server/Data Center authentication using Personal Access Token.
     * Used for self-hosted Jira instances.
     */
    @Serializable
    @SerialName("jira_pat")
    data class JiraPersonalAccessToken(
        val token: String
    ) : TicketCredentials()

    /**
     * GitHub authentication using Personal Access Token.
     * Supports filtering by owner (user/organization) and optionally a specific repository.
     */
    @Serializable
    @SerialName("github_token")
    data class GitHubToken(
        val token: String,
        val owner: String,
        val repositories: List<String> = emptyList()
    ) : TicketCredentials()

    /**
     * GitLab authentication using Personal Access Token.
     * Supports filtering by specific project IDs.
     */
    @Serializable
    @SerialName("gitlab_token")
    data class GitLabToken(
        val token: String,
        val projectIds: List<String> = emptyList()
    ) : TicketCredentials()

    /**
     * Get the provider type for these credentials.
     */
    fun getProviderType(): TicketProvider = when (this) {
        is JiraApiToken -> TicketProvider.JIRA
        is JiraPersonalAccessToken -> TicketProvider.JIRA
        is GitHubToken -> TicketProvider.GITHUB
        is GitLabToken -> TicketProvider.GITLAB
    }
}
