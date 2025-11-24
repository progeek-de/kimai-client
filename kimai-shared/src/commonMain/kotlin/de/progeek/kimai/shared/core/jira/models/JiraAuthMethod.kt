package de.progeek.kimai.shared.core.jira.models

/**
 * Sealed class representing Jira authentication methods.
 */
sealed class JiraAuthMethod {
    /**
     * API Token authentication for Jira Cloud.
     * Requires email and API token generated from Atlassian account settings.
     *
     * @property email User's email address
     * @property token API token
     */
    data class ApiToken(
        val email: String,
        val token: String
    ) : JiraAuthMethod()

    /**
     * Personal Access Token authentication for Jira Server/Data Center.
     * Requires PAT generated from Jira Server profile settings.
     *
     * @property token Personal Access Token
     */
    data class PersonalAccessToken(
        val token: String
    ) : JiraAuthMethod()
}

/**
 * Detect authentication method based on Jira base URL.
 *
 * @param baseUrl Jira base URL
 * @return Suggested authentication method type
 */
fun detectAuthMethodType(baseUrl: String): String {
    return when {
        baseUrl.contains(".atlassian.net") -> "API_TOKEN"
        else -> "PERSONAL_ACCESS_TOKEN"
    }
}
