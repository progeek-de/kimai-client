package de.progeek.kimai.shared.core.ticketsystem.providers.jira

import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig

/**
 * Error message handler for Jira API errors.
 */
internal object JiraErrorHandler {

    fun createErrorMessage(
        statusCode: Int,
        config: TicketSystemConfig,
        errorBody: String
    ): String {
        val isCloud = config.baseUrl.contains("atlassian.net")
        val authMethod = when (config.credentials) {
            is TicketCredentials.JiraApiToken -> "API Token"
            is TicketCredentials.JiraPersonalAccessToken -> "Personal Access Token"
            else -> "Unknown"
        }

        return when (statusCode) {
            401 -> buildAuthErrorMessage(isCloud, authMethod, config)
            403 -> buildForbiddenErrorMessage(isCloud, authMethod, errorBody)
            404 -> "Resource not found. Please check the URL: ${config.baseUrl}"
            429 -> "Rate limit exceeded. Please wait a moment and try again."
            else -> "Jira API error ($statusCode): $errorBody"
        }
    }

    private fun buildAuthErrorMessage(
        isCloud: Boolean,
        authMethod: String,
        config: TicketSystemConfig
    ): String {
        val baseMessage = "Authentication failed ($authMethod)"

        val suggestion = if (isCloud) {
            when (config.credentials) {
                is TicketCredentials.JiraApiToken ->
                    "Please verify your email and API token. " +
                        "Generate tokens at: https://id.atlassian.com/manage-profile/security/api-tokens"
                is TicketCredentials.JiraPersonalAccessToken ->
                    "Personal Access Tokens are not supported for Jira Cloud. " +
                        "Please use API Token authentication instead."
                else -> "Invalid authentication method for Jira Cloud."
            }
        } else {
            "Please verify your Personal Access Token is valid and has not expired."
        }

        return "$baseMessage. $suggestion"
    }

    private fun buildForbiddenErrorMessage(
        isCloud: Boolean,
        authMethod: String,
        errorBody: String
    ): String {
        val isPATOnCloudError = isCloud &&
            authMethod == "Personal Access Token" &&
            errorBody.contains("Personal Access Tokens can only be used on Jira Data Center")

        return if (isPATOnCloudError) {
            "Personal Access Tokens cannot be used with Jira Cloud. " +
                "Please use API Token authentication instead. " +
                "Generate tokens at: https://id.atlassian.com/manage-profile/security/api-tokens"
        } else {
            "Access denied. Please check your permissions or credentials. Error: $errorBody"
        }
    }

    fun getErrorMessage(exception: Throwable, config: TicketSystemConfig): String {
        return when {
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Connection timed out. Please check your network and try again."
            exception.message?.contains("host", ignoreCase = true) == true ->
                "Could not connect to ${config.baseUrl}. Please check the URL."
            else -> exception.message ?: "Unknown error occurred"
        }
    }
}
