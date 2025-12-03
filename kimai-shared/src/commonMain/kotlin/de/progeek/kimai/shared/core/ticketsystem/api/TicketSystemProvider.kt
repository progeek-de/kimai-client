package de.progeek.kimai.shared.core.ticketsystem.api

import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig

/**
 * Interface for ticket system provider implementations.
 * Each provider (Jira, GitHub, GitLab) implements this interface.
 */
interface TicketSystemProvider {

    /**
     * The provider type this implementation handles.
     */
    val providerType: TicketProvider

    /**
     * Test the connection with the given configuration.
     *
     * @param config The ticket system configuration to test.
     * @return Result containing the authenticated user's display name on success,
     *         or an error on failure.
     */
    suspend fun testConnection(config: TicketSystemConfig): Result<String>

    /**
     * Search for issues matching the given query.
     *
     * @param config The ticket system configuration.
     * @param query Search query (provider-specific interpretation).
     *              - Jira: JQL or text search
     *              - GitHub: Search query with filters
     *              - GitLab: Text search
     * @param maxResults Maximum number of issues to return.
     * @return Result containing list of matching issues, or an error.
     */
    suspend fun searchIssues(
        config: TicketSystemConfig,
        query: String = "",
        maxResults: Int = 50
    ): Result<List<TicketIssue>>

    /**
     * Get a specific issue by its key/id.
     *
     * @param config The ticket system configuration.
     * @param key The issue key/id.
     *            - Jira: Issue key (e.g., "PROJ-123")
     *            - GitHub: Issue number (e.g., "456")
     *            - GitLab: Issue IID (e.g., "789")
     * @return Result containing the issue if found, null if not found, or an error.
     */
    suspend fun getIssueByKey(
        config: TicketSystemConfig,
        key: String
    ): Result<TicketIssue?>

    /**
     * Get available projects/repositories.
     *
     * @param config The ticket system configuration.
     * @return Result containing list of projects, or an error.
     */
    suspend fun getProjects(config: TicketSystemConfig): Result<List<TicketProject>>

    /**
     * Get the currently authenticated user's identifier.
     *
     * @param config The ticket system configuration.
     * @return Result containing the user identifier, or an error.
     */
    suspend fun getCurrentUser(config: TicketSystemConfig): Result<String>

    /**
     * Validate that the credentials format is correct for this provider.
     * This is a quick local check before making network calls.
     *
     * @param config The configuration to validate.
     * @return true if credentials appear valid, false otherwise.
     */
    fun validateCredentials(config: TicketSystemConfig): Boolean

    /**
     * Get a human-readable error message for provider-specific errors.
     *
     * @param exception The exception that occurred.
     * @param config The configuration used when the error occurred.
     * @return A user-friendly error message.
     */
    fun getErrorMessage(exception: Throwable, config: TicketSystemConfig): String
}
