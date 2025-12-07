package de.progeek.kimai.shared.core.ticketsystem.providers.jira

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Test suite for JiraResponseMapper.
 *
 * Tests the mapping of Jira API response models to unified ticket models.
 */
class JiraResponseMapperTest {

    private val testConfig = TicketSystemConfig(
        id = "config-uuid-123",
        displayName = "Work Jira",
        provider = TicketProvider.JIRA,
        enabled = true,
        baseUrl = "https://company.atlassian.net",
        credentials = TicketCredentials.JiraApiToken(
            email = "user@example.com",
            token = "test-token"
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    // ============================================================
    // mapProject() Tests
    // ============================================================

    @Test
    fun `mapProject maps key correctly`() {
        val response = JiraProjectResponse(
            id = "10001",
            key = "PROJ",
            name = "Test Project",
            description = "A test project"
        )

        val result = JiraResponseMapper.mapProject(response)

        assertEquals("PROJ", result.key)
    }

    @Test
    fun `mapProject maps name correctly`() {
        val response = JiraProjectResponse(
            id = "10001",
            key = "PROJ",
            name = "Test Project",
            description = null
        )

        val result = JiraResponseMapper.mapProject(response)

        assertEquals("Test Project", result.name)
    }

    @Test
    fun `mapProject maps description when present`() {
        val response = JiraProjectResponse(
            id = "10001",
            key = "PROJ",
            name = "Test Project",
            description = "A test project description"
        )

        val result = JiraResponseMapper.mapProject(response)

        assertEquals("A test project description", result.description)
    }

    @Test
    fun `mapProject handles null description`() {
        val response = JiraProjectResponse(
            id = "10001",
            key = "PROJ",
            name = "Test Project",
            description = null
        )

        val result = JiraResponseMapper.mapProject(response)

        assertNull(result.description)
    }

    // ============================================================
    // mapIssue() Tests - Basic Fields
    // ============================================================

    @Test
    fun `mapIssue maps id correctly`() {
        val response = createJiraIssueResponse(id = "10001")

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("10001", result.id)
    }

    @Test
    fun `mapIssue maps key correctly`() {
        val response = createJiraIssueResponse(key = "PROJ-123")

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("PROJ-123", result.key)
    }

    @Test
    fun `mapIssue maps summary correctly`() {
        val response = createJiraIssueResponse(summary = "Fix login bug")

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("Fix login bug", result.summary)
    }

    @Test
    fun `mapIssue sets provider to JIRA`() {
        val response = createJiraIssueResponse()

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals(TicketProvider.JIRA, result.provider)
    }

    @Test
    fun `mapIssue sets sourceId from config`() {
        val response = createJiraIssueResponse()

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("config-uuid-123", result.sourceId)
    }

    // ============================================================
    // mapIssue() Tests - Status
    // ============================================================

    @Test
    fun `mapIssue maps status name correctly`() {
        val response = createJiraIssueResponse(status = JiraStatus(name = "In Progress"))

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("In Progress", result.status)
    }

    @Test
    fun `mapIssue handles null status with Unknown default`() {
        val response = createJiraIssueResponse(status = null)

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("Unknown", result.status)
    }

    // ============================================================
    // mapIssue() Tests - Project
    // ============================================================

    @Test
    fun `mapIssue maps project key correctly`() {
        val response = createJiraIssueResponse(
            project = JiraProjectShort(key = "PROJ", name = "Project Name")
        )

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("PROJ", result.projectKey)
    }

    @Test
    fun `mapIssue maps project name correctly`() {
        val response = createJiraIssueResponse(
            project = JiraProjectShort(key = "PROJ", name = "Project Name")
        )

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("Project Name", result.projectName)
    }

    @Test
    fun `mapIssue handles null project with empty strings`() {
        val response = createJiraIssueResponse(project = null)

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("", result.projectKey)
        assertEquals("", result.projectName)
    }

    // ============================================================
    // mapIssue() Tests - Issue Type
    // ============================================================

    @Test
    fun `mapIssue maps issue type correctly`() {
        val response = createJiraIssueResponse(
            issueType = JiraIssueType(name = "Bug")
        )

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("Bug", result.issueType)
    }

    @Test
    fun `mapIssue handles null issue type with Task default`() {
        val response = createJiraIssueResponse(issueType = null)

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("Task", result.issueType)
    }

    // ============================================================
    // mapIssue() Tests - Assignee
    // ============================================================

    @Test
    fun `mapIssue maps assignee displayName correctly`() {
        val response = createJiraIssueResponse(
            assignee = JiraUserResponse(
                accountId = "123",
                displayName = "John Doe",
                active = true
            )
        )

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals("John Doe", result.assignee)
    }

    @Test
    fun `mapIssue handles null assignee`() {
        val response = createJiraIssueResponse(assignee = null)

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertNull(result.assignee)
    }

    // ============================================================
    // mapIssue() Tests - Updated Timestamp
    // ============================================================

    @Test
    fun `mapIssue parses ISO 8601 timestamp correctly`() {
        val response = createJiraIssueResponse(
            updated = "2024-01-15T10:30:00.000Z"
        )

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals(Instant.parse("2024-01-15T10:30:00.000Z"), result.updated)
    }

    @Test
    fun `mapIssue parses timestamp with timezone offset using colon format`() {
        // kotlinx.datetime.Instant.parse() requires colon in timezone offset (+02:00 not +0200)
        val response = createJiraIssueResponse(
            updated = "2024-01-15T10:30:00.000+02:00"
        )

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        // Should be parsed correctly (UTC time = 08:30:00)
        assertEquals(Instant.parse("2024-01-15T08:30:00.000Z"), result.updated)
    }

    @Test
    fun `mapIssue handles non-standard timezone offset format with epoch 0`() {
        // Jira sometimes uses +0200 format without colon, which Instant.parse() doesn't support
        val response = createJiraIssueResponse(
            updated = "2024-01-15T10:30:00.000+0200"
        )

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        // Falls back to epoch 0 since this format is not supported by Instant.parse()
        assertEquals(Instant.fromEpochMilliseconds(0), result.updated)
    }

    @Test
    fun `mapIssue handles null updated with epoch 0`() {
        val response = createJiraIssueResponse(updated = null)

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals(Instant.fromEpochMilliseconds(0), result.updated)
    }

    @Test
    fun `mapIssue handles invalid timestamp with epoch 0`() {
        val response = createJiraIssueResponse(updated = "invalid-timestamp")

        val result = JiraResponseMapper.mapIssue(response, testConfig)

        assertEquals(Instant.fromEpochMilliseconds(0), result.updated)
    }

    // ============================================================
    // mapIssue() Tests - Web URL
    // ============================================================

    @Test
    fun `mapIssue builds correct web URL`() {
        val config = testConfig.copy(baseUrl = "https://company.atlassian.net")
        val response = createJiraIssueResponse(key = "PROJ-123")

        val result = JiraResponseMapper.mapIssue(response, config)

        assertEquals("https://company.atlassian.net/browse/PROJ-123", result.webUrl)
    }

    @Test
    fun `mapIssue builds web URL with trailing slash in baseUrl`() {
        val config = testConfig.copy(baseUrl = "https://company.atlassian.net/")
        val response = createJiraIssueResponse(key = "PROJ-456")

        val result = JiraResponseMapper.mapIssue(response, config)

        assertEquals("https://company.atlassian.net/browse/PROJ-456", result.webUrl)
    }

    @Test
    fun `mapIssue builds web URL for self-hosted Jira`() {
        val config = testConfig.copy(baseUrl = "https://jira.company.com")
        val response = createJiraIssueResponse(key = "INTERNAL-789")

        val result = JiraResponseMapper.mapIssue(response, config)

        assertEquals("https://jira.company.com/browse/INTERNAL-789", result.webUrl)
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createJiraIssueResponse(
        id: String = "10001",
        key: String = "PROJ-123",
        summary: String = "Test Issue",
        status: JiraStatus? = JiraStatus(name = "Open"),
        project: JiraProjectShort? = JiraProjectShort(key = "PROJ", name = "Project"),
        issueType: JiraIssueType? = JiraIssueType(name = "Task"),
        assignee: JiraUserResponse? = null,
        updated: String? = "2024-01-15T10:30:00.000Z"
    ): JiraIssueResponse {
        return JiraIssueResponse(
            id = id,
            key = key,
            fields = JiraIssueFields(
                summary = summary,
                status = status,
                project = project,
                issuetype = issueType,
                assignee = assignee,
                updated = updated
            )
        )
    }
}
