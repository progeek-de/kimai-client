package de.progeek.kimai.shared.core.ticketsystem.providers.jira

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test suite for JiraErrorHandler.
 *
 * Tests error message creation for various HTTP status codes
 * and Jira Cloud vs Server distinction.
 */
class JiraErrorHandlerTest {

    // ============================================================
    // Test Configurations
    // ============================================================

    private val jiraCloudApiTokenConfig = TicketSystemConfig(
        id = "cloud-api-token",
        displayName = "Jira Cloud (API Token)",
        provider = TicketProvider.JIRA,
        enabled = true,
        baseUrl = "https://company.atlassian.net",
        credentials = TicketCredentials.JiraApiToken(
            email = "user@example.com",
            token = "api-token-123"
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private val jiraCloudPATConfig = TicketSystemConfig(
        id = "cloud-pat",
        displayName = "Jira Cloud (PAT)",
        provider = TicketProvider.JIRA,
        enabled = true,
        baseUrl = "https://company.atlassian.net",
        credentials = TicketCredentials.JiraPersonalAccessToken(
            token = "pat-token-456"
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private val jiraServerPATConfig = TicketSystemConfig(
        id = "server-pat",
        displayName = "Jira Server (PAT)",
        provider = TicketProvider.JIRA,
        enabled = true,
        baseUrl = "https://jira.company.com",
        credentials = TicketCredentials.JiraPersonalAccessToken(
            token = "pat-token-789"
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    // ============================================================
    // 401 Unauthorized Tests
    // ============================================================

    @Test
    fun `createErrorMessage for 401 with Jira Cloud API Token`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 401,
            config = jiraCloudApiTokenConfig,
            errorBody = "Unauthorized"
        )

        assertTrue(result.contains("Authentication failed"), "Should contain auth failed message")
        assertTrue(result.contains("API Token"), "Should mention API Token")
        assertTrue(result.contains("email"), "Should mention email verification")
        assertTrue(result.contains("id.atlassian.com"), "Should provide token generation URL")
    }

    @Test
    fun `createErrorMessage for 401 with Jira Cloud PAT shows unsupported message`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 401,
            config = jiraCloudPATConfig,
            errorBody = "Unauthorized"
        )

        assertTrue(result.contains("Authentication failed"), "Should contain auth failed message")
        assertTrue(result.contains("Personal Access Token"), "Should mention PAT")
        assertTrue(
            result.contains("not supported for Jira Cloud"),
            "Should explain PAT not supported for Cloud"
        )
        assertTrue(result.contains("API Token"), "Should suggest API Token instead")
    }

    @Test
    fun `createErrorMessage for 401 with Jira Server PAT`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 401,
            config = jiraServerPATConfig,
            errorBody = "Unauthorized"
        )

        assertTrue(result.contains("Authentication failed"), "Should contain auth failed message")
        assertTrue(result.contains("Personal Access Token"), "Should mention PAT")
        assertTrue(result.contains("valid"), "Should mention token validity")
        assertTrue(result.contains("expired"), "Should mention expiration")
    }

    // ============================================================
    // 403 Forbidden Tests
    // ============================================================

    @Test
    fun `createErrorMessage for 403 with PAT on Cloud error`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 403,
            config = jiraCloudPATConfig,
            errorBody = "Personal Access Tokens can only be used on Jira Data Center"
        )

        assertTrue(
            result.contains("cannot be used with Jira Cloud"),
            "Should explain PAT not for Cloud"
        )
        assertTrue(result.contains("API Token"), "Should suggest API Token")
        assertTrue(result.contains("id.atlassian.com"), "Should provide token generation URL")
    }

    @Test
    fun `createErrorMessage for 403 generic access denied`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 403,
            config = jiraCloudApiTokenConfig,
            errorBody = "User does not have permission"
        )

        assertTrue(result.contains("Access denied"), "Should contain access denied")
        assertTrue(result.contains("permissions"), "Should mention permissions")
        assertTrue(result.contains("User does not have permission"), "Should include error body")
    }

    @Test
    fun `createErrorMessage for 403 with Server PAT normal permissions error`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 403,
            config = jiraServerPATConfig,
            errorBody = "Insufficient permissions"
        )

        assertTrue(result.contains("Access denied"), "Should contain access denied")
        assertTrue(result.contains("Insufficient permissions"), "Should include error body")
    }

    // ============================================================
    // 404 Not Found Tests
    // ============================================================

    @Test
    fun `createErrorMessage for 404 includes URL`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 404,
            config = jiraCloudApiTokenConfig,
            errorBody = "Resource not found"
        )

        assertTrue(result.contains("not found"), "Should contain not found")
        assertTrue(result.contains(jiraCloudApiTokenConfig.baseUrl), "Should include base URL")
    }

    @Test
    fun `createErrorMessage for 404 with server URL`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 404,
            config = jiraServerPATConfig,
            errorBody = "Not Found"
        )

        assertTrue(result.contains("not found"), "Should contain not found")
        assertTrue(result.contains("jira.company.com"), "Should include server URL")
    }

    // ============================================================
    // 429 Rate Limit Tests
    // ============================================================

    @Test
    fun `createErrorMessage for 429 rate limit`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 429,
            config = jiraCloudApiTokenConfig,
            errorBody = "Too Many Requests"
        )

        assertTrue(result.contains("Rate limit"), "Should mention rate limit")
        assertTrue(result.contains("wait"), "Should suggest waiting")
    }

    // ============================================================
    // Other Error Codes Tests
    // ============================================================

    @Test
    fun `createErrorMessage for unknown status code includes code and body`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 500,
            config = jiraCloudApiTokenConfig,
            errorBody = "Internal Server Error"
        )

        assertTrue(result.contains("500"), "Should include status code")
        assertTrue(result.contains("Internal Server Error"), "Should include error body")
    }

    @Test
    fun `createErrorMessage for 502 bad gateway`() {
        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 502,
            config = jiraServerPATConfig,
            errorBody = "Bad Gateway"
        )

        assertTrue(result.contains("502"), "Should include status code")
        assertTrue(result.contains("Bad Gateway"), "Should include error body")
    }

    // ============================================================
    // getErrorMessage() Exception Tests
    // ============================================================

    @Test
    fun `getErrorMessage handles timeout exception`() {
        val exception = Exception("Connection timeout occurred")

        val result = JiraErrorHandler.getErrorMessage(exception, jiraCloudApiTokenConfig)

        assertTrue(result.contains("timed out"), "Should mention timeout")
        assertTrue(result.contains("network"), "Should mention network")
    }

    @Test
    fun `getErrorMessage handles host resolution exception`() {
        val exception = Exception("Unable to resolve host jira.company.com")

        val result = JiraErrorHandler.getErrorMessage(exception, jiraServerPATConfig)

        assertTrue(result.contains("connect"), "Should mention connection issue")
        assertTrue(result.contains(jiraServerPATConfig.baseUrl), "Should include URL")
    }

    @Test
    fun `getErrorMessage handles generic exception`() {
        val exception = Exception("Something went wrong")

        val result = JiraErrorHandler.getErrorMessage(exception, jiraCloudApiTokenConfig)

        assertEquals("Something went wrong", result, "Should return exception message")
    }

    @Test
    fun `getErrorMessage handles exception with null message`() {
        val exception = Exception()

        val result = JiraErrorHandler.getErrorMessage(exception, jiraCloudApiTokenConfig)

        assertEquals("Unknown error occurred", result, "Should return default message")
    }

    // ============================================================
    // Cloud vs Server Detection Tests
    // ============================================================

    @Test
    fun `createErrorMessage detects atlassian dot net as Cloud`() {
        val cloudConfig = jiraCloudApiTokenConfig.copy(baseUrl = "https://test.atlassian.net")

        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 401,
            config = cloudConfig,
            errorBody = "Unauthorized"
        )

        assertTrue(
            result.contains("id.atlassian.com") || result.contains("API Token"),
            "Should treat atlassian.net as Cloud"
        )
    }

    @Test
    fun `createErrorMessage detects non-atlassian URL as Server`() {
        val serverConfig = jiraServerPATConfig.copy(baseUrl = "https://jira.internal.corp")

        val result = JiraErrorHandler.createErrorMessage(
            statusCode = 401,
            config = serverConfig,
            errorBody = "Unauthorized"
        )

        assertTrue(
            result.contains("Personal Access Token") && result.contains("expired"),
            "Should treat non-atlassian URL as Server"
        )
    }
}
