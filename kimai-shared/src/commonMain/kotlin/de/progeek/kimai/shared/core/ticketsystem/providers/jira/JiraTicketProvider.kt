package de.progeek.kimai.shared.core.ticketsystem.providers.jira

import de.progeek.kimai.shared.core.ticketsystem.api.TicketSystemProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig

/**
 * Jira implementation of TicketSystemProvider.
 * Supports both Jira Cloud (API Token) and Jira Server/Data Center (PAT).
 */
class JiraTicketProvider : TicketSystemProvider {

    override val providerType: TicketProvider = TicketProvider.JIRA

    private val client = JiraTicketClient()

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
        return client.search(config, query, maxResults)
    }

    override suspend fun getIssueByKey(
        config: TicketSystemConfig,
        key: String
    ): Result<TicketIssue?> {
        validateConfig(config)
        return client.getIssueByKey(config, key)
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
            is TicketCredentials.JiraApiToken ->
                creds.email.isNotBlank() && creds.token.isNotBlank()
            is TicketCredentials.JiraPersonalAccessToken ->
                creds.token.isNotBlank()
            else -> false
        }
    }

    override fun getErrorMessage(exception: Throwable, config: TicketSystemConfig): String {
        return JiraErrorHandler.getErrorMessage(exception, config)
    }

    private fun validateConfig(config: TicketSystemConfig) {
        require(config.provider == TicketProvider.JIRA) {
            "Invalid provider type: ${config.provider}"
        }
        require(config.credentials is TicketCredentials.JiraApiToken ||
            config.credentials is TicketCredentials.JiraPersonalAccessToken) {
            "Invalid credentials type for Jira provider"
        }
    }
}
