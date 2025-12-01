package de.progeek.kimai.shared.core.jira.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.jira.models.SerializableAuthMethod
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for JiraClient.
 * Verifies credential management, encryption, and API operations.
 */
class JiraClientTest {

    private lateinit var settings: ObservableSettings
    private lateinit var aesCipher: AesGCMCipher
    private lateinit var client: JiraClient

    @BeforeTest
    fun setup() {
        settings = mockk(relaxed = true)
        aesCipher = mockk(relaxed = true)

        // Mock settings to return null initially (no stored credentials)
        every { settings.getStringOrNull(any()) } returns null

        client = JiraClient(settings, aesCipher)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `saveCredentials encrypts and stores credentials`() {
        // Given
        val credentials = JiraCredentials(
            baseUrl = "https://company.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = "test@example.com",
                token = "test-token-123"
            ),
            defaultProjectKey = "PROJ"
        )

        // When
        client.saveCredentials(credentials)

        // Then
        assertTrue(client.hasCredentials())
        assertEquals("https://company.atlassian.net", client.getBaseUrl())
    }

    @Test
    fun `saveCredentials stores API Token credentials correctly`() {
        // Given
        val credentials = JiraCredentials(
            baseUrl = "https://test.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = "user@test.com",
                token = "api-token-xyz"
            )
        )

        // When
        client.saveCredentials(credentials)

        // Then
        assertTrue(client.hasCredentials())
        assertEquals("https://test.atlassian.net", client.getBaseUrl())
    }

    @Test
    fun `saveCredentials stores Personal Access Token credentials correctly`() {
        // Given
        val credentials = JiraCredentials(
            baseUrl = "https://jira.company.com",
            authMethod = SerializableAuthMethod.PersonalAccessToken(
                token = "pat-token-abc"
            ),
            defaultProjectKey = null
        )

        // When
        client.saveCredentials(credentials)

        // Then
        assertTrue(client.hasCredentials())
        assertEquals("https://jira.company.com", client.getBaseUrl())
    }

    @Test
    fun `clearCredentials removes stored credentials`() {
        // Given
        val credentials = JiraCredentials(
            baseUrl = "https://test.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = "test@example.com",
                token = "token"
            )
        )
        client.saveCredentials(credentials)
        assertTrue(client.hasCredentials())

        // When
        client.clearCredentials()

        // Then
        assertFalse(client.hasCredentials())
        assertNull(client.getBaseUrl())
    }

    @Test
    fun `hasCredentials returns false when no credentials stored`() {
        // Given - fresh client with no credentials

        // When & Then
        assertFalse(client.hasCredentials())
    }

    @Test
    fun `hasCredentials returns true after saving credentials`() {
        // Given
        val credentials = JiraCredentials(
            baseUrl = "https://test.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = "test@example.com",
                token = "token"
            )
        )

        // When
        client.saveCredentials(credentials)

        // Then
        assertTrue(client.hasCredentials())
    }

    @Test
    fun `getBaseUrl returns null when no credentials stored`() {
        // Given - fresh client

        // When & Then
        assertNull(client.getBaseUrl())
    }

    @Test
    fun `getBaseUrl returns stored base URL after saving credentials`() {
        // Given
        val baseUrl = "https://company.atlassian.net"
        val credentials = JiraCredentials(
            baseUrl = baseUrl,
            authMethod = SerializableAuthMethod.ApiToken(
                email = "test@example.com",
                token = "token"
            )
        )

        // When
        client.saveCredentials(credentials)

        // Then
        assertEquals(baseUrl, client.getBaseUrl())
    }

    @Test
    fun `testConnection returns error when no credentials configured`() = runTest {
        // Given - fresh client with no credentials

        // When
        val result = client.testConnection()

        // Then
        assertTrue(result.isFailure)
        assertEquals("No credentials configured", result.exceptionOrNull()?.message)
    }

    @Test
    fun `search returns error when no credentials configured`() = runTest {
        // Given - fresh client

        // When
        val result = client.search("project = PROJ")

        // Then
        assertTrue(result.isFailure)
        assertEquals("No credentials configured", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProjects returns error when no credentials configured`() = runTest {
        // Given - fresh client

        // When
        val result = client.getProjects()

        // Then
        assertTrue(result.isFailure)
        assertEquals("No credentials configured", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCurrentUser returns error when no credentials configured`() = runTest {
        // Given - fresh client

        // When
        val result = client.getCurrentUser()

        // Then
        assertTrue(result.isFailure)
        assertEquals("No credentials configured", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getIssueByKey returns error when no credentials configured`() = runTest {
        // Given - fresh client

        // When
        val result = client.getIssueByKey("PROJ-123")

        // Then
        assertTrue(result.isFailure)
        assertEquals("No credentials configured", result.exceptionOrNull()?.message)
    }

    // TODO: These tests require HTTP mocking with MockEngine or a test Jira server
    // The methods now make actual HTTP calls to Jira REST API instead of returning placeholders

    @Test
    @kotlin.test.Ignore("Requires HTTP mocking - getCurrentUser now makes actual API calls")
    fun `getCurrentUser returns email for API Token auth`() = runTest {
        // Given
        val email = "user@test.com"
        val credentials = JiraCredentials(
            baseUrl = "https://test.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = email,
                token = "token"
            )
        )
        client.saveCredentials(credentials)

        // When
        val result = client.getCurrentUser()

        // Then
        assertTrue(result.isSuccess)
        // Now returns actual user data from Jira API
        assertNotNull(result.getOrNull())
    }

    @Test
    @kotlin.test.Ignore("Requires HTTP mocking - getCurrentUser now makes actual API calls")
    fun `getCurrentUser returns placeholder for PAT auth`() = runTest {
        // Given
        val credentials = JiraCredentials(
            baseUrl = "https://jira.company.com",
            authMethod = SerializableAuthMethod.PersonalAccessToken(
                token = "pat-token"
            )
        )
        client.saveCredentials(credentials)

        // When
        val result = client.getCurrentUser()

        // Then
        assertTrue(result.isSuccess)
        // Now returns actual user data from Jira API
        assertNotNull(result.getOrNull())
    }

    @Test
    @kotlin.test.Ignore("Requires HTTP mocking - search now makes actual API calls")
    fun `search respects maxResults parameter`() = runTest {
        // Given
        val credentials = JiraCredentials(
            baseUrl = "https://test.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = "test@example.com",
                token = "token"
            )
        )
        client.saveCredentials(credentials)

        // When
        val result = client.search("project = PROJ", maxResults = 100)

        // Then
        assertTrue(result.isSuccess)
        // Note: Returns empty list as placeholder until kotlin-jira-api is integrated
        assertNotNull(result.getOrNull())
    }

    @Test
    fun `saveCredentials with default project key stores correctly`() {
        // Given
        val credentials = JiraCredentials(
            baseUrl = "https://test.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = "test@example.com",
                token = "token"
            ),
            defaultProjectKey = "MYPROJ"
        )

        // When
        client.saveCredentials(credentials)

        // Then
        assertTrue(client.hasCredentials())
    }

    @Test
    fun `multiple saveCredentials calls overwrite previous credentials`() {
        // Given
        val credentials1 = JiraCredentials(
            baseUrl = "https://old.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = "old@example.com",
                token = "old-token"
            )
        )
        val credentials2 = JiraCredentials(
            baseUrl = "https://new.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = "new@example.com",
                token = "new-token"
            )
        )

        // When
        client.saveCredentials(credentials1)
        client.saveCredentials(credentials2)

        // Then
        assertEquals("https://new.atlassian.net", client.getBaseUrl())
    }

    @Test
    fun `clearCredentials can be called multiple times safely`() {
        // Given
        val credentials = JiraCredentials(
            baseUrl = "https://test.atlassian.net",
            authMethod = SerializableAuthMethod.ApiToken(
                email = "test@example.com",
                token = "token"
            )
        )
        client.saveCredentials(credentials)

        // When
        client.clearCredentials()
        client.clearCredentials() // Second call should not throw

        // Then
        assertFalse(client.hasCredentials())
        assertNull(client.getBaseUrl())
    }
}
