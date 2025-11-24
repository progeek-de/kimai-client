package de.progeek.kimai.shared.core.jira.models

import kotlinx.serialization.Serializable

/**
 * Domain model representing Jira connection credentials.
 *
 * @property baseUrl Jira base URL (e.g., "https://company.atlassian.net" or "https://jira.company.com")
 * @property authMethod Authentication method (API Token or PAT)
 * @property defaultProjectKey Optional default project key for filtering issues
 */
@Serializable
data class JiraCredentials(
    val baseUrl: String,
    val authMethod: SerializableAuthMethod,
    val defaultProjectKey: String? = null
)

/**
 * Serializable version of JiraAuthMethod for storage.
 */
@Serializable
sealed class SerializableAuthMethod {
    @Serializable
    data class ApiToken(
        val email: String,
        val token: String
    ) : SerializableAuthMethod()

    @Serializable
    data class PersonalAccessToken(
        val token: String
    ) : SerializableAuthMethod()
}

/**
 * Convert to domain JiraAuthMethod.
 */
fun SerializableAuthMethod.toDomain(): JiraAuthMethod = when (this) {
    is SerializableAuthMethod.ApiToken -> JiraAuthMethod.ApiToken(email, token)
    is SerializableAuthMethod.PersonalAccessToken -> JiraAuthMethod.PersonalAccessToken(token)
}

/**
 * Convert from domain JiraAuthMethod.
 */
fun JiraAuthMethod.toSerializable(): SerializableAuthMethod = when (this) {
    is JiraAuthMethod.ApiToken -> SerializableAuthMethod.ApiToken(email, token)
    is JiraAuthMethod.PersonalAccessToken -> SerializableAuthMethod.PersonalAccessToken(token)
}
