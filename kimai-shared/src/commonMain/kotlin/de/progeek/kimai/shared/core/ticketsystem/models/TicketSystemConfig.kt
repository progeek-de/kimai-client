@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.ticketsystem.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Configuration for a single ticket system instance.
 * Supports multiple instances of the same provider type.
 */
@Serializable
data class TicketSystemConfig(
    /**
     * Unique identifier (UUID) for this configuration.
     */
    val id: String,

    /**
     * User-defined display name (e.g., "Work Jira", "Personal GitHub").
     */
    val displayName: String,

    /**
     * The ticket system provider type.
     */
    val provider: TicketProvider,

    /**
     * Whether this configuration is enabled for issue fetching.
     */
    val enabled: Boolean = true,

    /**
     * Base URL of the ticket system.
     * - Jira: "https://company.atlassian.net" or "https://jira.company.com"
     * - GitHub: "https://api.github.com" or "https://github.company.com/api/v3"
     * - GitLab: "https://gitlab.com" or "https://gitlab.company.com"
     */
    val baseUrl: String,

    /**
     * Provider-specific authentication credentials.
     * Stored encrypted in the database.
     */
    val credentials: TicketCredentials,

    /**
     * Sync interval in minutes.
     */
    val syncIntervalMinutes: Int = 15,

    /**
     * Optional default project/repository key for filtering.
     */
    val defaultProjectKey: String? = null,

    /**
     * Timestamp when this configuration was created.
     */
    @Contextual
    val createdAt: Instant = Clock.System.now(),

    /**
     * Timestamp when this configuration was last updated.
     */
    @Contextual
    val updatedAt: Instant = Clock.System.now(),

    /**
     * Custom format pattern for inserting issue references.
     * Uses placeholders like {key}, {summary}, {status}, {project}, {type}.
     * Example: "{key}: {summary}" produces "PROJ-123: Fix login bug"
     */
    val issueFormat: String = IssueInsertFormat.DEFAULT_FORMAT
) {
    init {
        require(id.isNotBlank()) { "Configuration ID cannot be blank" }
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(baseUrl.isNotBlank()) { "Base URL cannot be blank" }
        require(syncIntervalMinutes > 0) { "Sync interval must be positive" }
        require(credentials.getProviderType() == provider) {
            "Credentials type must match provider type"
        }
    }
}
