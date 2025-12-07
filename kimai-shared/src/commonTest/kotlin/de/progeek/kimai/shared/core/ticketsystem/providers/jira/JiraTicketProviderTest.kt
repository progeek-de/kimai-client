package de.progeek.kimai.shared.core.ticketsystem.providers.jira

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test suite for JiraTicketProvider.
 *
 * Tests the provider validation and credential handling.
 * Network operations are not tested here as they require mocking HTTP clients.
 */
class JiraTicketProviderTest {

    private val provider = JiraTicketProvider()

    // ============================================================
    // providerType Tests
    // ============================================================

    @Test
    fun `providerType is JIRA`() {
        assertEquals(TicketProvider.JIRA, provider.providerType)
    }

    // ============================================================
    // validateCredentials() Tests - JiraApiToken
    // ============================================================

    @Test
    fun `validateCredentials returns true for valid JiraApiToken`() {
        val config = createJiraConfig(
            credentials = TicketCredentials.JiraApiToken(
                email = "user@example.com",
                token = "valid-token"
            )
        )

        assertTrue(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for empty email in JiraApiToken`() {
        val config = createJiraConfig(
            credentials = TicketCredentials.JiraApiToken(
                email = "",
                token = "valid-token"
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for blank email in JiraApiToken`() {
        val config = createJiraConfig(
            credentials = TicketCredentials.JiraApiToken(
                email = "   ",
                token = "valid-token"
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for empty token in JiraApiToken`() {
        val config = createJiraConfig(
            credentials = TicketCredentials.JiraApiToken(
                email = "user@example.com",
                token = ""
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for blank token in JiraApiToken`() {
        val config = createJiraConfig(
            credentials = TicketCredentials.JiraApiToken(
                email = "user@example.com",
                token = "   "
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    // ============================================================
    // validateCredentials() Tests - JiraPersonalAccessToken
    // ============================================================

    @Test
    fun `validateCredentials returns true for valid JiraPersonalAccessToken`() {
        val config = createJiraConfig(
            credentials = TicketCredentials.JiraPersonalAccessToken(
                token = "valid-pat-token"
            )
        )

        assertTrue(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for empty JiraPersonalAccessToken`() {
        val config = createJiraConfig(
            credentials = TicketCredentials.JiraPersonalAccessToken(
                token = ""
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for blank JiraPersonalAccessToken`() {
        val config = createJiraConfig(
            credentials = TicketCredentials.JiraPersonalAccessToken(
                token = "   "
            )
        )

        assertFalse(provider.validateCredentials(config))
    }

    // ============================================================
    // getErrorMessage() Tests
    // ============================================================

    @Test
    fun `getErrorMessage returns meaningful message for exception`() {
        val config = createJiraConfig(
            credentials = TicketCredentials.JiraApiToken(
                email = "user@example.com",
                token = "token"
            )
        )
        val exception = Exception("Network error")

        val message = provider.getErrorMessage(exception, config)

        assertNotNull(message)
        assertTrue(message.isNotBlank(), "Error message should not be blank")
        assertTrue(
            message.contains("error", ignoreCase = true) ||
            message.contains("Network error") ||
            message.contains("connection", ignoreCase = true),
            "Error message should reference the error type: $message"
        )
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createJiraConfig(
        id: String = "config-uuid-123",
        displayName: String = "Test Jira",
        baseUrl: String = "https://company.atlassian.net",
        credentials: TicketCredentials.JiraApiToken
    ): TicketSystemConfig {
        return TicketSystemConfig(
            id = id,
            displayName = displayName,
            provider = TicketProvider.JIRA,
            enabled = true,
            baseUrl = baseUrl,
            credentials = credentials,
            syncIntervalMinutes = 15,
            issueFormat = IssueInsertFormat.DEFAULT_FORMAT
        )
    }

    private fun createJiraConfig(
        id: String = "config-uuid-123",
        displayName: String = "Test Jira",
        baseUrl: String = "https://jira.company.com",
        credentials: TicketCredentials.JiraPersonalAccessToken
    ): TicketSystemConfig {
        return TicketSystemConfig(
            id = id,
            displayName = displayName,
            provider = TicketProvider.JIRA,
            enabled = true,
            baseUrl = baseUrl,
            credentials = credentials,
            syncIntervalMinutes = 15,
            issueFormat = IssueInsertFormat.DEFAULT_FORMAT
        )
    }
}
