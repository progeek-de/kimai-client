package de.progeek.kimai.shared.core.jira.client

import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.jira.models.SerializableAuthMethod
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Handles error responses from Jira API and provides helpful error messages.
 */
internal object JiraErrorHandler {

    /**
     * Creates a detailed error message from HTTP response.
     *
     * @param response HTTP response with error
     * @param credentials Credentials used for the request
     * @param errorBody Raw error body from Jira
     * @return Detailed error message with helpful suggestions
     */
    suspend fun createErrorMessage(
        response: HttpResponse,
        credentials: JiraCredentials,
        errorBody: String
    ): String {
        val authMethodName = when (credentials.authMethod) {
            is SerializableAuthMethod.ApiToken -> "API Token"
            is SerializableAuthMethod.PersonalAccessToken -> "Personal Access Token"
        }

        val helpfulMessage = getHelpfulMessage(response.status.value, errorBody, credentials)

        return buildString {
            appendLine("Connection failed: ${response.status.value} ${response.status.description}")
            appendLine("Auth method: $authMethodName")
            appendLine("Base URL: ${credentials.baseUrl}")
            appendLine("Error details: $errorBody")
            if (helpfulMessage.isNotEmpty()) {
                append(helpfulMessage)
            }
        }
    }

    /**
     * Provides context-specific help messages based on error patterns.
     */
    private fun getHelpfulMessage(statusCode: Int, errorBody: String, credentials: JiraCredentials): String {
        return when {
            // PAT not supported on Jira Cloud
            statusCode == 403 &&
                errorBody.contains("Failed to parse Connect Session Auth Token", ignoreCase = true) &&
                credentials.authMethod is SerializableAuthMethod.PersonalAccessToken &&
                credentials.baseUrl.contains("atlassian.net", ignoreCase = true) -> {
                """

                💡 SOLUTION: Jira Cloud (*.atlassian.net) does NOT support Personal Access Tokens.
                   Please switch to 'API Token' authentication method.
                   Generate an API token at: https://id.atlassian.com/manage-profile/security/api-tokens
                """.trimIndent()
            }

            // General authentication errors
            statusCode == 401 || statusCode == 403 -> {
                """

                💡 Check your credentials:
                   - Verify email address is correct
                   - Generate a new API token at: https://id.atlassian.com/manage-profile/security/api-tokens
                   - Ensure your account has 'Browse Projects' permission
                """.trimIndent()
            }

            // Rate limiting
            statusCode == 429 -> {
                """

                💡 Rate limit exceeded. Please wait a moment and try again.
                """.trimIndent()
            }

            else -> ""
        }
    }
}
