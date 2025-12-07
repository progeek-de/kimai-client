package de.progeek.kimai.shared.core.ticketsystem.providers.github

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for GitHubTicketProvider.
 *
 * Tests the provider validation and credential handling.
 * Network operations are not tested here as they require mocking HTTP clients.
 */
class GitHubTicketProviderTest {

    private val provider = GitHubTicketProvider()

    // ============================================================
    // providerType Tests
    // ============================================================

    @Test
    fun `providerType is GITHUB`() {
        assertEquals(TicketProvider.GITHUB, provider.providerType)
    }

    // ============================================================
    // validateCredentials() Tests - Valid Cases
    // ============================================================

    @Test
    fun `validateCredentials returns true for valid GitHubToken`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "ghp_valid_token",
                owner = "testowner",
                repositories = emptyList()
            )
        )

        assertTrue(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns true with repositories configured`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "ghp_valid_token",
                owner = "testowner",
                repositories = listOf("repo1", "repo2")
            )
        )

        assertTrue(provider.validateCredentials(config))
    }

    // ============================================================
    // validateCredentials() Tests - Invalid Cases
    // ============================================================

    @Test
    fun `validateCredentials returns false for empty token`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "",
                owner = "testowner",
                repositories = emptyList()
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for blank token`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "   ",
                owner = "testowner",
                repositories = emptyList()
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for empty owner`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "ghp_valid_token",
                owner = "",
                repositories = emptyList()
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for blank owner`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "ghp_valid_token",
                owner = "   ",
                repositories = emptyList()
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for both empty token and owner`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "",
                owner = "",
                repositories = emptyList()
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    // ============================================================
    // getErrorMessage() Tests
    // ============================================================

    @Test
    fun `getErrorMessage returns auth message for 401 error`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "invalid",
                owner = "owner",
                repositories = emptyList()
            )
        )
        val exception = Exception("HTTP 401 Unauthorized")

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("Authentication failed", ignoreCase = true))
        assertTrue(message.contains("Personal Access Token", ignoreCase = true))
    }

    @Test
    fun `getErrorMessage returns access denied for 403 error`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "token",
                owner = "owner",
                repositories = emptyList()
            )
        )
        val exception = Exception("HTTP 403 Forbidden")

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("Access denied", ignoreCase = true))
        assertTrue(message.contains("permissions", ignoreCase = true))
    }

    @Test
    fun `getErrorMessage returns not found for 404 error`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "token",
                owner = "owner",
                repositories = emptyList()
            )
        )
        val exception = Exception("HTTP 404 Not Found")

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("not found", ignoreCase = true))
    }

    @Test
    fun `getErrorMessage returns timeout message for timeout error`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "token",
                owner = "owner",
                repositories = emptyList()
            )
        )
        val exception = Exception("Connection timeout")

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("timed out", ignoreCase = true))
    }

    @Test
    fun `getErrorMessage returns exception message for unknown error`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "token",
                owner = "owner",
                repositories = emptyList()
            )
        )
        val exception = Exception("Some unexpected error")

        val message = provider.getErrorMessage(exception, config)

        assertEquals("Some unexpected error", message)
    }

    @Test
    fun `getErrorMessage returns unknown error for null message`() {
        val config = createGitHubConfig(
            credentials = TicketCredentials.GitHubToken(
                token = "token",
                owner = "owner",
                repositories = emptyList()
            )
        )
        val exception = Exception()

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("Unknown error", ignoreCase = true))
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createGitHubConfig(
        id: String = "config-uuid-456",
        displayName: String = "Test GitHub",
        baseUrl: String = "https://api.github.com",
        credentials: TicketCredentials.GitHubToken
    ): TicketSystemConfig {
        return TicketSystemConfig(
            id = id,
            displayName = displayName,
            provider = TicketProvider.GITHUB,
            enabled = true,
            baseUrl = baseUrl,
            credentials = credentials,
            syncIntervalMinutes = 15,
            issueFormat = IssueInsertFormat.DEFAULT_FORMAT
        )
    }
}
