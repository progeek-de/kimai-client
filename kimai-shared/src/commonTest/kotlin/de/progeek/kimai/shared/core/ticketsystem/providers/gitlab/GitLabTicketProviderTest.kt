package de.progeek.kimai.shared.core.ticketsystem.providers.gitlab

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for GitLabTicketProvider.
 *
 * Tests the provider validation and credential handling.
 * Network operations are not tested here as they require mocking HTTP clients.
 */
class GitLabTicketProviderTest {

    private val provider = GitLabTicketProvider()

    // ============================================================
    // providerType Tests
    // ============================================================

    @Test
    fun `providerType is GITLAB`() {
        assertEquals(TicketProvider.GITLAB, provider.providerType)
    }

    // ============================================================
    // validateCredentials() Tests - Valid Cases
    // ============================================================

    @Test
    fun `validateCredentials returns true for valid GitLabToken`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "glpat-valid_token",
                projectIds = emptyList()
            )
        )

        assertTrue(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns true with project IDs configured`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "glpat-valid_token",
                projectIds = listOf("123", "456")
            )
        )

        assertTrue(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns true with empty project IDs`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "glpat-valid_token",
                projectIds = emptyList()
            )
        )

        assertTrue(provider.validateCredentials(config))
    }

    // ============================================================
    // validateCredentials() Tests - Invalid Cases
    // ============================================================

    @Test
    fun `validateCredentials returns false for empty token`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "",
                projectIds = emptyList()
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for blank token`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "   ",
                projectIds = emptyList()
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    // ============================================================
    // getErrorMessage() Tests
    // ============================================================

    @Test
    fun `getErrorMessage returns auth message for 401 error`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "invalid",
                projectIds = emptyList()
            )
        )
        val exception = Exception("HTTP 401 Unauthorized")

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("Authentication failed", ignoreCase = true))
        assertTrue(message.contains("Personal Access Token", ignoreCase = true))
    }

    @Test
    fun `getErrorMessage returns access denied for 403 error`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "token",
                projectIds = emptyList()
            )
        )
        val exception = Exception("HTTP 403 Forbidden")

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("Access denied", ignoreCase = true))
        assertTrue(message.contains("api or read_api scope", ignoreCase = true))
    }

    @Test
    fun `getErrorMessage returns not found for 404 error`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "token",
                projectIds = emptyList()
            )
        )
        val exception = Exception("HTTP 404 Not Found")

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("not found", ignoreCase = true))
        assertTrue(message.contains("project ID", ignoreCase = true))
    }

    @Test
    fun `getErrorMessage returns timeout message for timeout error`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "token",
                projectIds = emptyList()
            )
        )
        val exception = Exception("Connection timeout")

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("timed out", ignoreCase = true))
    }

    @Test
    fun `getErrorMessage returns exception message for unknown error`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "token",
                projectIds = emptyList()
            )
        )
        val exception = Exception("Some unexpected error")

        val message = provider.getErrorMessage(exception, config)

        assertEquals("Some unexpected error", message)
    }

    @Test
    fun `getErrorMessage returns unknown error for null message`() {
        val config = createGitLabConfig(
            credentials = TicketCredentials.GitLabToken(
                token = "token",
                projectIds = emptyList()
            )
        )
        val exception = Exception()

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("Unknown error", ignoreCase = true))
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createGitLabConfig(
        id: String = "config-uuid-789",
        displayName: String = "Test GitLab",
        baseUrl: String = "https://gitlab.com",
        credentials: TicketCredentials.GitLabToken
    ): TicketSystemConfig {
        return TicketSystemConfig(
            id = id,
            displayName = displayName,
            provider = TicketProvider.GITLAB,
            enabled = true,
            baseUrl = baseUrl,
            credentials = credentials,
            syncIntervalMinutes = 15,
            issueFormat = IssueInsertFormat.DEFAULT_FORMAT
        )
    }
}
