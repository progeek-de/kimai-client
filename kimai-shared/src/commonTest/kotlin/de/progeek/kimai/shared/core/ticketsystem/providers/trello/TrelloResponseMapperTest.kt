@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.ticketsystem.providers.trello

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/**
 * Test suite for TrelloResponseMapper.
 *
 * Tests the mapping of Trello API response models to unified ticket models.
 */
class TrelloResponseMapperTest {

    private val testConfig = TicketSystemConfig(
        id = "config-uuid-123",
        displayName = "Work Trello",
        provider = TicketProvider.TRELLO,
        enabled = true,
        baseUrl = "https://api.trello.com/1",
        credentials = TicketCredentials.TrelloToken(
            apiKey = "test-key",
            token = "test-token"
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    // ============================================================
    // mapBoard() Tests
    // ============================================================

    @Test
    fun `mapBoard maps id to key`() {
        val response = TrelloBoardResponse(id = "board-1", name = "My Board", desc = "desc")

        val result = TrelloResponseMapper.mapBoard(response)

        assertEquals("board-1", result.key)
    }

    @Test
    fun `mapBoard maps name correctly`() {
        val response = TrelloBoardResponse(id = "board-1", name = "My Board", desc = null)

        val result = TrelloResponseMapper.mapBoard(response)

        assertEquals("My Board", result.name)
    }

    @Test
    fun `mapBoard maps description when present`() {
        val response = TrelloBoardResponse(id = "board-1", name = "My Board", desc = "A board")

        val result = TrelloResponseMapper.mapBoard(response)

        assertEquals("A board", result.description)
    }

    @Test
    fun `mapBoard maps blank description to null`() {
        val response = TrelloBoardResponse(id = "board-1", name = "My Board", desc = "")

        val result = TrelloResponseMapper.mapBoard(response)

        assertNull(result.description)
    }

    // ============================================================
    // mapCard() Tests - Basic Fields
    // ============================================================

    @Test
    fun `mapCard maps id correctly`() {
        val response = createCard(id = "card-abc")

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals("card-abc", result.id)
    }

    @Test
    fun `mapCard builds key from idShort`() {
        val response = createCard(idShort = 42)

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals("#42", result.key)
    }

    @Test
    fun `mapCard maps name to summary`() {
        val response = createCard(name = "Implement feature")

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals("Implement feature", result.summary)
    }

    @Test
    fun `mapCard sets provider to TRELLO`() {
        val result = TrelloResponseMapper.mapCard(createCard(), testConfig, "Board", "To Do")

        assertEquals(TicketProvider.TRELLO, result.provider)
    }

    @Test
    fun `mapCard sets sourceId from config`() {
        val result = TrelloResponseMapper.mapCard(createCard(), testConfig, "Board", "To Do")

        assertEquals("config-uuid-123", result.sourceId)
    }

    @Test
    fun `mapCard maps idBoard to projectKey`() {
        val response = createCard(idBoard = "board-99")

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals("board-99", result.projectKey)
    }

    @Test
    fun `mapCard maps shortUrl to webUrl`() {
        val response = createCard(shortUrl = "https://trello.com/c/abc")

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals("https://trello.com/c/abc", result.webUrl)
    }

    // ============================================================
    // mapCard() Tests - Status (list name)
    // ============================================================

    @Test
    fun `mapCard uses explicit list name as status`() {
        val result = TrelloResponseMapper.mapCard(createCard(), testConfig, "Board", "In Progress")

        assertEquals("In Progress", result.status)
    }

    @Test
    fun `mapCard falls back to embedded list name`() {
        val response = createCard(list = TrelloListResponse(id = "l1", name = "Done"))

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", null)

        assertEquals("Done", result.status)
    }

    @Test
    fun `mapCard defaults status to Unknown when no list available`() {
        val result = TrelloResponseMapper.mapCard(createCard(), testConfig, "Board", null)

        assertEquals("Unknown", result.status)
    }

    // ============================================================
    // mapCard() Tests - Project name
    // ============================================================

    @Test
    fun `mapCard uses explicit board name as projectName`() {
        val result = TrelloResponseMapper.mapCard(createCard(), testConfig, "Sprint Board", "To Do")

        assertEquals("Sprint Board", result.projectName)
    }

    @Test
    fun `mapCard falls back to embedded board name`() {
        val response = createCard(
            board = TrelloBoardResponse(id = "b1", name = "Embedded Board", desc = null)
        )

        val result = TrelloResponseMapper.mapCard(response, testConfig, null, "To Do")

        assertEquals("Embedded Board", result.projectName)
    }

    @Test
    fun `mapCard falls back to idBoard when no board name available`() {
        val response = createCard(idBoard = "board-xyz")

        val result = TrelloResponseMapper.mapCard(response, testConfig, null, "To Do")

        assertEquals("board-xyz", result.projectName)
    }

    // ============================================================
    // mapCard() Tests - Issue type from labels
    // ============================================================

    @Test
    fun `mapCard derives issueType from first named label`() {
        val response = createCard(
            labels = listOf(TrelloLabel(name = "Bug", color = "red"))
        )

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals("Bug", result.issueType)
    }

    @Test
    fun `mapCard skips labels with blank names`() {
        val response = createCard(
            labels = listOf(
                TrelloLabel(name = "", color = "green"),
                TrelloLabel(name = "Feature", color = "blue")
            )
        )

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals("Feature", result.issueType)
    }

    @Test
    fun `mapCard defaults issueType to Card without labels`() {
        val result = TrelloResponseMapper.mapCard(createCard(), testConfig, "Board", "To Do")

        assertEquals("Card", result.issueType)
    }

    // ============================================================
    // mapCard() Tests - Updated timestamp
    // ============================================================

    @Test
    fun `mapCard parses ISO 8601 timestamp correctly`() {
        val response = createCard(dateLastActivity = "2024-01-15T10:30:00.000Z")

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals(Instant.parse("2024-01-15T10:30:00.000Z"), result.updated)
    }

    @Test
    fun `mapCard handles null timestamp with epoch 0`() {
        val response = createCard(dateLastActivity = null)

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals(Instant.fromEpochMilliseconds(0), result.updated)
    }

    @Test
    fun `mapCard handles invalid timestamp with epoch 0`() {
        val response = createCard(dateLastActivity = "not-a-date")

        val result = TrelloResponseMapper.mapCard(response, testConfig, "Board", "To Do")

        assertEquals(Instant.fromEpochMilliseconds(0), result.updated)
    }

    @Test
    fun `mapCard never sets an assignee`() {
        val result = TrelloResponseMapper.mapCard(createCard(), testConfig, "Board", "To Do")

        assertNull(result.assignee)
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createCard(
        id: String = "card-1",
        idShort: Int = 1,
        name: String = "Test Card",
        shortUrl: String? = "https://trello.com/c/short",
        idBoard: String = "board-1",
        idList: String = "list-1",
        dateLastActivity: String? = "2024-01-15T10:30:00.000Z",
        labels: List<TrelloLabel> = emptyList(),
        list: TrelloListResponse? = null,
        board: TrelloBoardResponse? = null
    ): TrelloCardResponse {
        return TrelloCardResponse(
            id = id,
            idShort = idShort,
            name = name,
            shortUrl = shortUrl,
            idBoard = idBoard,
            idList = idList,
            dateLastActivity = dateLastActivity,
            labels = labels,
            list = list,
            board = board
        )
    }
}
