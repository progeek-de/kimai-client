package de.progeek.kimai.shared.core.ticketsystem.providers.trello

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
 * Test suite for TrelloTicketProvider.
 *
 * Tests provider validation and credential handling. Network operations are not
 * tested here as they require mocking HTTP clients.
 */
class TrelloTicketProviderTest {

    private val provider = TrelloTicketProvider()

    // ============================================================
    // providerType Tests
    // ============================================================

    @Test
    fun `providerType is TRELLO`() {
        assertEquals(TicketProvider.TRELLO, provider.providerType)
    }

    // ============================================================
    // validateCredentials() Tests
    // ============================================================

    @Test
    fun `validateCredentials returns true for valid TrelloToken`() {
        val config = createConfig(
            TicketCredentials.TrelloToken(apiKey = "key", token = "token")
        )

        assertTrue(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns true with board ids`() {
        val config = createConfig(
            TicketCredentials.TrelloToken(
                apiKey = "key",
                token = "token",
                boardIds = listOf("board-1", "board-2")
            )
        )

        assertTrue(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for empty apiKey`() {
        val config = createConfig(
            TicketCredentials.TrelloToken(apiKey = "", token = "token")
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for blank apiKey`() {
        val config = createConfig(
            TicketCredentials.TrelloToken(apiKey = "   ", token = "token")
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for empty token`() {
        val config = createConfig(
            TicketCredentials.TrelloToken(apiKey = "key", token = "")
        )

        assertFalse(provider.validateCredentials(config))
    }

    @Test
    fun `validateCredentials returns false for blank token`() {
        val config = createConfig(
            TicketCredentials.TrelloToken(apiKey = "key", token = "   ")
        )

        assertFalse(provider.validateCredentials(config))
    }

    // ============================================================
    // getErrorMessage() Tests
    // ============================================================

    @Test
    fun `getErrorMessage maps 401 to authentication hint`() {
        val config = createConfig(TicketCredentials.TrelloToken(apiKey = "key", token = "token"))
        val exception = Exception("Trello API error (401 Unauthorized)")

        val message = provider.getErrorMessage(exception, config)

        assertTrue(message.contains("API key", ignoreCase = true))
    }

    @Test
    fun `getErrorMessage falls back to exception message`() {
        val config = createConfig(TicketCredentials.TrelloToken(apiKey = "key", token = "token"))
        val exception = Exception("Some network error")

        val message = provider.getErrorMessage(exception, config)

        assertNotNull(message)
        assertEquals("Some network error", message)
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createConfig(
        credentials: TicketCredentials,
        baseUrl: String = "https://api.trello.com/1"
    ): TicketSystemConfig {
        return TicketSystemConfig(
            id = "config-uuid-123",
            displayName = "Test Trello",
            provider = TicketProvider.TRELLO,
            enabled = true,
            baseUrl = baseUrl,
            credentials = credentials,
            syncIntervalMinutes = 15,
            issueFormat = IssueInsertFormat.DEFAULT_FORMAT
        )
    }
}
