package de.progeek.kimai.shared.core.jira.repositories

import app.cash.turbine.test
import de.progeek.kimai.shared.core.database.datasource.jira.JiraDatasource
import de.progeek.kimai.shared.core.jira.client.JiraClient
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import de.progeek.kimai.shared.core.jira.models.JiraProject
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for JiraRepository.
 *
 * Tests Store5-based caching repository with the following methods:
 * 1. searchIssues(jql) - Returns Flow<List<JiraIssue>> with Store5 caching
 * 2. getAllCachedIssues() - Returns all cached issues without network fetch
 * 3. getIssueByKey(key) - Returns specific issue from cache
 * 4. getIssuesByProject(projectKey) - Returns issues for a project
 * 5. getIssuesByAssignee(assignee) - Returns issues for an assignee
 * 6. searchCached(query, limit) - Local search without network
 * 7. invalidateCache(jql) - Clears cache and fetches fresh data
 * 8. getProjects() - Returns list of Jira projects
 * 9. testConnection() - Tests Jira connection
 * 10. getCurrentUser() - Returns current user info
 * 11. hasCredentials() - Checks if credentials exist
 * 12. getBaseUrl() - Returns configured base URL
 * 13. getCachedIssueCount() - Returns count of cached issues
 *
 * Note: Integration-style tests using real datasources with in-memory databases
 * and mocked JiraClient. Store5 behavior testing focuses on basic functionality.
 */
class JiraRepositoryTest {

    private lateinit var mockClient: JiraClient
    private lateinit var datasource: JiraDatasource
    private lateinit var repository: JiraRepository
    private lateinit var testDatasources: de.progeek.kimai.shared.utils.TestDatasources

    private fun createTestIssue(
        key: String = "PROJ-123",
        summary: String = "Test Issue",
        status: String = "In Progress",
        projectKey: String = "PROJ",
        projectName: String = "Test Project",
        issueType: String = "Task",
        assignee: String? = "john@example.com",
        updated: Instant = Instant.fromEpochMilliseconds(1704067200000)
    ) = JiraIssue(
        key = key,
        summary = summary,
        status = status,
        projectKey = projectKey,
        projectName = projectName,
        issueType = issueType,
        assignee = assignee,
        updated = updated
    )

    private val testIssues = listOf(
        JiraIssue(
            key = "PROJ-1",
            summary = "First Issue",
            status = "In Progress",
            projectKey = "PROJ",
            projectName = "Test Project",
            issueType = "Task",
            assignee = "john@example.com",
            updated = Instant.fromEpochMilliseconds(1704067200000)
        ),
        JiraIssue(
            key = "PROJ-2",
            summary = "Second Issue",
            status = "Done",
            projectKey = "PROJ",
            projectName = "Test Project",
            issueType = "Bug",
            assignee = "jane@example.com",
            updated = Instant.fromEpochMilliseconds(1704153600000)
        )
    )

    private val testProjects = listOf(
        JiraProject(key = "PROJ", name = "Test Project", description = "A test project"),
        JiraProject(key = "OTHER", name = "Other Project", description = null)
    )

    @BeforeTest
    fun setup() {
        // Use real datasource with in-memory database
        testDatasources = createTestDatasources()
        datasource = testDatasources.jiraDatasource

        mockClient = mockk(relaxed = true)
        repository = JiraRepository(datasource, mockClient)
    }

    @AfterTest
    fun teardown() {
        clearDatabase(testDatasources.database)
        clearAllMocks()
    }

    // ============================================================
    // searchIssues() Tests
    // ============================================================

    @Test
    fun `searchIssues returns flow that can be collected`() = runTest {
        // Given
        coEvery { mockClient.searchIssues(any(), any()) } returns Result.success(testIssues)

        // When
        val flow = repository.searchIssues("project = PROJ")

        // Then - basic smoke test
        assertNotNull(flow)
    }

    @Test
    fun `searchIssues returns cached data when available`() = runTest {
        // Given - populate cache first
        coEvery { mockClient.searchIssues(any(), any()) } returns Result.success(testIssues)
        datasource.insert(testIssues) // Pre-populate cache

        // When
        val flow = repository.searchIssues("project = PROJ")

        // Then
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertEquals(2, item.size)
            // Issues are ordered by updated timestamp descending (PROJ-2 is newer)
            assertEquals("PROJ-2", item[0].key)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchIssues returns empty list when no data available`() = runTest {
        // Given
        coEvery { mockClient.searchIssues(any(), any()) } returns Result.success(emptyList())

        // When
        val flow = repository.searchIssues("project = EMPTY")

        // Then
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertTrue(item.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchIssues handles network error gracefully`() = runTest {
        // Given
        coEvery { mockClient.searchIssues(any(), any()) } throws Exception("Network error")

        // When
        val flow = repository.searchIssues("project = PROJ")

        // Then - Store5 handles error, returns empty
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertTrue(item.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchIssues returns cached data when network fails`() = runTest {
        // Given - cache has data, network fails
        datasource.insert(testIssues)
        coEvery { mockClient.searchIssues(any(), any()) } throws Exception("Network error")

        // When
        val flow = repository.searchIssues("project = PROJ")

        // Then - returns cached data despite network failure
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertEquals(2, item.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getAllCachedIssues() Tests
    // ============================================================

    @Test
    fun `getAllCachedIssues returns all cached issues without network call`() = runTest {
        // Given
        datasource.insert(testIssues)

        // When
        val flow = repository.getAllCachedIssues()

        // Then
        flow.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            cancelAndIgnoreRemainingEvents()
        }

        // Verify no network call was made
        coVerify(exactly = 0) { mockClient.searchIssues(any(), any()) }
    }

    @Test
    fun `getAllCachedIssues returns empty list when cache is empty`() = runTest {
        // When
        val flow = repository.getAllCachedIssues()

        // Then
        flow.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getIssueByKey() Tests
    // ============================================================

    @Test
    fun `getIssueByKey returns issue from cache`() = runTest {
        // Given
        val issue = createTestIssue(key = "FIND-123")
        datasource.insert(issue)

        // When
        val result = repository.getIssueByKey("FIND-123")

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals("FIND-123", found.key)
    }

    @Test
    fun `getIssueByKey returns null when issue not found`() = runTest {
        // When
        val result = repository.getIssueByKey("NOTFOUND-999")

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ============================================================
    // getIssuesByProject() Tests
    // ============================================================

    @Test
    fun `getIssuesByProject returns issues for specified project`() = runTest {
        // Given
        datasource.insert(testIssues)

        // When
        val flow = repository.getIssuesByProject("PROJ")

        // Then
        flow.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.all { it.projectKey == "PROJ" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getIssuesByProject returns empty list for project with no issues`() = runTest {
        // Given
        datasource.insert(testIssues)

        // When
        val flow = repository.getIssuesByProject("EMPTY")

        // Then
        flow.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getIssuesByAssignee() Tests
    // ============================================================

    @Test
    fun `getIssuesByAssignee returns issues for specified assignee`() = runTest {
        // Given
        datasource.insert(testIssues)

        // When
        val flow = repository.getIssuesByAssignee("john@example.com")

        // Then
        flow.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("PROJ-1", items[0].key)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getIssuesByAssignee returns empty list when no issues for assignee`() = runTest {
        // Given
        datasource.insert(testIssues)

        // When
        val flow = repository.getIssuesByAssignee("nobody@example.com")

        // Then
        flow.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // searchCached() Tests
    // ============================================================

    @Test
    fun `searchCached finds issues by key match`() = runTest {
        // Given
        datasource.insert(testIssues)

        // When
        val result = repository.searchCached("PROJ-1")

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals(1, found.size)
        assertEquals("PROJ-1", found[0].key)
    }

    @Test
    fun `searchCached finds issues by summary match`() = runTest {
        // Given
        datasource.insert(testIssues)

        // When
        val result = repository.searchCached("First")

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals(1, found.size)
        assertEquals("First Issue", found[0].summary)
    }

    @Test
    fun `searchCached respects limit parameter`() = runTest {
        // Given - insert more than limit
        val manyIssues = (1..10).map { createTestIssue(key = "LIMIT-$it") }
        datasource.insert(manyIssues)

        // When
        val result = repository.searchCached("LIMIT", limit = 5)

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals(5, found.size)
    }

    @Test
    fun `searchCached returns empty list when no matches`() = runTest {
        // Given
        datasource.insert(testIssues)

        // When
        val result = repository.searchCached("NOMATCH")

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertTrue(found.isEmpty())
    }

    // ============================================================
    // getProjects() Tests
    // ============================================================

    @Test
    fun `getProjects returns projects from client`() = runTest {
        // Given
        coEvery { mockClient.getProjects() } returns Result.success(testProjects)

        // When
        val result = repository.getProjects()

        // Then
        assertTrue(result.isSuccess)
        val projects = result.getOrNull()
        assertNotNull(projects)
        assertEquals(2, projects.size)
        assertEquals("PROJ", projects[0].key)

        coVerify { mockClient.getProjects() }
    }

    @Test
    fun `getProjects handles client error`() = runTest {
        // Given
        val error = Exception("Network error")
        coEvery { mockClient.getProjects() } returns Result.failure(error)

        // When
        val result = repository.getProjects()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ============================================================
    // testConnection() Tests
    // ============================================================

    @Test
    fun `testConnection returns success from client`() = runTest {
        // Given
        coEvery { mockClient.testConnection() } returns Result.success("Connection successful")

        // When
        val result = repository.testConnection()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Connection successful", result.getOrNull())

        coVerify { mockClient.testConnection() }
    }

    @Test
    fun `testConnection handles client error`() = runTest {
        // Given
        val error = Exception("Connection failed")
        coEvery { mockClient.testConnection() } returns Result.failure(error)

        // When
        val result = repository.testConnection()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Connection failed", result.exceptionOrNull()?.message)
    }

    // ============================================================
    // getCurrentUser() Tests
    // ============================================================

    @Test
    fun `getCurrentUser returns user from client`() = runTest {
        // Given
        coEvery { mockClient.getCurrentUser() } returns Result.success("john@example.com")

        // When
        val result = repository.getCurrentUser()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("john@example.com", result.getOrNull())

        coVerify { mockClient.getCurrentUser() }
    }

    @Test
    fun `getCurrentUser handles client error`() = runTest {
        // Given
        val error = Exception("No credentials")
        coEvery { mockClient.getCurrentUser() } returns Result.failure(error)

        // When
        val result = repository.getCurrentUser()

        // Then
        assertTrue(result.isFailure)
    }

    // ============================================================
    // hasCredentials() Tests
    // ============================================================

    @Test
    fun `hasCredentials returns true when credentials exist`() {
        // Given
        every { mockClient.hasCredentials() } returns true

        // When
        val result = repository.hasCredentials()

        // Then
        assertTrue(result)

        verify { mockClient.hasCredentials() }
    }

    @Test
    fun `hasCredentials returns false when no credentials`() {
        // Given
        every { mockClient.hasCredentials() } returns false

        // When
        val result = repository.hasCredentials()

        // Then
        assertFalse(result)
    }

    // ============================================================
    // getBaseUrl() Tests
    // ============================================================

    @Test
    fun `getBaseUrl returns URL from client`() {
        // Given
        val baseUrl = "https://company.atlassian.net"
        every { mockClient.getBaseUrl() } returns baseUrl

        // When
        val result = repository.getBaseUrl()

        // Then
        assertEquals(baseUrl, result)

        verify { mockClient.getBaseUrl() }
    }

    @Test
    fun `getBaseUrl returns null when not configured`() {
        // Given
        every { mockClient.getBaseUrl() } returns null

        // When
        val result = repository.getBaseUrl()

        // Then
        assertNull(result)
    }

    // ============================================================
    // getCachedIssueCount() Tests
    // ============================================================

    @Test
    fun `getCachedIssueCount returns count of cached issues`() = runTest {
        // Given
        datasource.insert(testIssues)

        // When
        val result = repository.getCachedIssueCount()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2L, result.getOrNull())
    }

    @Test
    fun `getCachedIssueCount returns zero when cache is empty`() = runTest {
        // When
        val result = repository.getCachedIssueCount()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull())
    }
}
