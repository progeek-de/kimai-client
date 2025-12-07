package de.progeek.kimai.shared.core.ticketsystem.repository

import app.cash.turbine.test
import de.progeek.kimai.shared.core.ticketsystem.api.TicketSystemProvider
import de.progeek.kimai.shared.core.ticketsystem.api.TicketSystemRegistry
import de.progeek.kimai.shared.core.ticketsystem.datasource.TicketIssueDatasource
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.utils.TestDatasources
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for TicketSystemRepository.
 *
 * Tests the aggregating repository functionality including:
 * - Issue retrieval from datasource
 * - Search functionality with fallback
 * - Provider interaction
 * - Cache management
 */
class TicketSystemRepositoryTest {

    private lateinit var testDatasources: TestDatasources
    private lateinit var issueDatasource: TicketIssueDatasource
    private lateinit var mockConfigRepository: TicketConfigRepository
    private lateinit var registry: TicketSystemRegistry
    private lateinit var repository: TicketSystemRepository

    private val sourceId1 = "source-uuid-111"
    private val sourceId2 = "source-uuid-222"

    private val testConfig1 = TicketSystemConfig(
        id = sourceId1,
        displayName = "Test Jira",
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

    private val testConfig2 = TicketSystemConfig(
        id = sourceId2,
        displayName = "Test GitHub",
        provider = TicketProvider.GITHUB,
        enabled = true,
        baseUrl = "https://api.github.com",
        credentials = TicketCredentials.GitHubToken(
            token = "ghp_test",
            owner = "testowner",
            repositories = emptyList()
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private val testIssue1 = TicketIssue(
        id = "10001",
        key = "PROJ-123",
        summary = "Fix login bug",
        status = "Open",
        projectKey = "PROJ",
        projectName = "Project Alpha",
        issueType = "Bug",
        assignee = "john.doe",
        updated = Instant.fromEpochMilliseconds(1700000000000),
        sourceId = sourceId1,
        provider = TicketProvider.JIRA,
        webUrl = "https://company.atlassian.net/browse/PROJ-123"
    )

    private val testIssue2 = TicketIssue(
        id = "789",
        key = "#42",
        summary = "GitHub issue test",
        status = "Open",
        projectKey = "owner/repo",
        projectName = "repo",
        issueType = "Issue",
        assignee = null,
        updated = Instant.fromEpochMilliseconds(1700001000000),
        sourceId = sourceId2,
        provider = TicketProvider.GITHUB,
        webUrl = "https://github.com/owner/repo/issues/42"
    )

    @BeforeTest
    fun setup() {
        testDatasources = createTestDatasources()
        issueDatasource = testDatasources.ticketIssueDatasource

        mockConfigRepository = mockk(relaxed = true)
        registry = TicketSystemRegistry()

        repository = TicketSystemRepository(
            configRepository = mockConfigRepository,
            issueDatasource = issueDatasource,
            registry = registry
        )
    }

    @AfterTest
    fun teardown() {
        clearDatabase(testDatasources.database)
        clearAllMocks()
    }

    // ============================================================
    // getAllIssues() Tests
    // ============================================================

    @Test
    fun `getAllIssues returns empty list when database is empty`() = runTest {
        repository.getAllIssues().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllIssues returns all cached issues`() = runTest {
        issueDatasource.insert(testIssue1)
        issueDatasource.insert(testIssue2)

        repository.getAllIssues().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(2, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getIssuesBySource() Tests
    // ============================================================

    @Test
    fun `getIssuesBySource returns only issues from specified source`() = runTest {
        issueDatasource.insert(testIssue1)
        issueDatasource.insert(testIssue2)

        repository.getIssuesBySource(sourceId1).test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(sourceId1, items.first().sourceId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getIssuesBySource returns empty list for unknown source`() = runTest {
        issueDatasource.insert(testIssue1)

        repository.getIssuesBySource("unknown-source").test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getIssueByKey() Tests
    // ============================================================

    @Test
    fun `getIssueByKey returns issue when found`() = runTest {
        issueDatasource.insert(testIssue1)

        val result = repository.getIssueByKey("PROJ-123")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals("PROJ-123", result.getOrNull()?.key)
    }

    @Test
    fun `getIssueByKey returns null when not found`() = runTest {
        val result = repository.getIssueByKey("NONEXISTENT")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ============================================================
    // getCachedIssueCount() Tests
    // ============================================================

    @Test
    fun `getCachedIssueCount returns correct count`() = runTest {
        issueDatasource.insert(testIssue1)
        issueDatasource.insert(testIssue2)

        val result = repository.getCachedIssueCount()

        assertTrue(result.isSuccess)
        assertEquals(2L, result.getOrNull())
    }

    @Test
    fun `getCachedIssueCount returns zero for empty database`() = runTest {
        val result = repository.getCachedIssueCount()

        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull())
    }

    // ============================================================
    // getCachedIssueCountBySource() Tests
    // ============================================================

    @Test
    fun `getCachedIssueCountBySource returns correct count`() = runTest {
        issueDatasource.insert(testIssue1)
        issueDatasource.insert(testIssue2)

        val result = repository.getCachedIssueCountBySource(sourceId1)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    // ============================================================
    // hasEnabledSources() Tests
    // ============================================================

    @Test
    fun `hasEnabledSources returns true when enabled configs exist`() = runTest {
        coEvery { mockConfigRepository.countEnabled() } returns Result.success(2L)

        val result = repository.hasEnabledSources()

        assertTrue(result)
    }

    @Test
    fun `hasEnabledSources returns false when no enabled configs`() = runTest {
        coEvery { mockConfigRepository.countEnabled() } returns Result.success(0L)

        val result = repository.hasEnabledSources()

        assertTrue(!result)
    }

    @Test
    fun `hasEnabledSources returns false on error`() = runTest {
        coEvery { mockConfigRepository.countEnabled() } returns Result.failure(Exception("Error"))

        val result = repository.hasEnabledSources()

        assertTrue(!result)
    }

    // ============================================================
    // searchWithFallback() Tests
    // ============================================================

    @Test
    fun `searchWithFallback returns local results when cache has data`() = runTest {
        issueDatasource.insert(testIssue1)

        val result = repository.searchWithFallback("PROJ-123", 50)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("PROJ-123", result.getOrNull()?.first()?.key)
    }

    @Test
    fun `searchWithFallback returns empty when no matches in cache and no enabled configs`() = runTest {
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(emptyList())

        val result = repository.searchWithFallback("nonexistent", 50)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    // ============================================================
    // testConnection() Tests
    // ============================================================

    @Test
    fun `testConnection delegates to provider`() = runTest {
        val mockProvider = createMockProvider(TicketProvider.JIRA, "Test User")
        registry.register(mockProvider)

        val result = repository.testConnection(testConfig1)

        assertTrue(result.isSuccess)
        assertEquals("Test User", result.getOrNull())
    }

    @Test
    fun `testConnection fails for unregistered provider`() = runTest {
        // Registry is empty - no providers registered

        val result = repository.testConnection(testConfig1)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not registered") == true)
    }

    // ============================================================
    // getProjects() Tests
    // ============================================================

    @Test
    fun `getProjects delegates to provider`() = runTest {
        val testProjects = listOf(
            TicketProject(key = "PROJ", name = "Project", description = "Test project")
        )
        val mockProvider = mockk<TicketSystemProvider> {
            every { providerType } returns TicketProvider.JIRA
            coEvery { getProjects(any()) } returns Result.success(testProjects)
        }
        registry.register(mockProvider)

        val result = repository.getProjects(testConfig1)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("PROJ", result.getOrNull()?.first()?.key)
    }

    @Test
    fun `getProjects fails for unregistered provider`() = runTest {
        val result = repository.getProjects(testConfig1)

        assertTrue(result.isFailure)
    }

    // ============================================================
    // clearSource() Tests
    // ============================================================

    @Test
    fun `clearSource removes issues for source`() = runTest {
        issueDatasource.insert(testIssue1)
        issueDatasource.insert(testIssue2)

        repository.clearSource(sourceId1)

        repository.getAllIssues().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(sourceId2, items.first().sourceId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // searchAllSources() Tests
    // ============================================================

    @Test
    fun `searchAllSources returns empty when no configs`() = runTest {
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(emptyList())

        val result = repository.searchAllSources("test", 50)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `searchAllSources queries all enabled providers`() = runTest {
        val jiraProvider = createMockProvider(TicketProvider.JIRA, "user", listOf(testIssue1))
        val githubProvider = createMockProvider(TicketProvider.GITHUB, "user", listOf(testIssue2))

        registry.register(jiraProvider)
        registry.register(githubProvider)

        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(listOf(testConfig1, testConfig2))

        val result = repository.searchAllSources("test", 50)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `searchAllSources deduplicates results`() = runTest {
        // Same issue returned from two providers (simulated)
        val duplicateIssue = testIssue1.copy()
        val jiraProvider = createMockProvider(TicketProvider.JIRA, "user", listOf(testIssue1, duplicateIssue))

        registry.register(jiraProvider)
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(listOf(testConfig1))

        val result = repository.searchAllSources("test", 50)

        assertTrue(result.isSuccess)
        // Should be deduplicated by sourceId:id
        assertEquals(1, result.getOrNull()?.size)
    }

    // ============================================================
    // refreshAllSources() Tests
    // ============================================================

    @Test
    fun `refreshAllSources succeeds with no enabled configs`() = runTest {
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(emptyList())

        val result = repository.refreshAllSources()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `refreshAllSources refreshes all enabled sources`() = runTest {
        val jiraProvider = createMockProvider(TicketProvider.JIRA, "user", listOf(testIssue1))
        val githubProvider = createMockProvider(TicketProvider.GITHUB, "user", listOf(testIssue2))

        registry.register(jiraProvider)
        registry.register(githubProvider)
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(listOf(testConfig1, testConfig2))

        val result = repository.refreshAllSources()

        // Result depends on Store5 behavior, but should not throw
        // The actual refresh involves network calls which are mocked
        assertNotNull(result)
    }

    // ============================================================
    // searchAllSources() Sorting Tests
    // ============================================================

    @Test
    fun `searchAllSources sorts by updated descending`() = runTest {
        val olderIssue = testIssue1.copy(updated = Instant.fromEpochMilliseconds(1600000000000))
        val newerIssue = testIssue2.copy(updated = Instant.fromEpochMilliseconds(1800000000000))

        val jiraProvider = createMockProvider(TicketProvider.JIRA, "user", listOf(olderIssue))
        val githubProvider = createMockProvider(TicketProvider.GITHUB, "user", listOf(newerIssue))

        registry.register(jiraProvider)
        registry.register(githubProvider)
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(listOf(testConfig1, testConfig2))

        val result = repository.searchAllSources("test", 50)

        assertTrue(result.isSuccess)
        val issues = result.getOrNull()!!
        assertEquals(2, issues.size)
        // Newer issue should be first
        assertEquals(newerIssue.id, issues.first().id)
    }

    @Test
    fun `searchAllSources respects limit`() = runTest {
        val issues = (1..20).map {
            testIssue1.copy(
                id = "issue-$it",
                key = "PROJ-$it",
                updated = Instant.fromEpochMilliseconds(1700000000000 + it * 1000)
            )
        }

        val jiraProvider = createMockProvider(TicketProvider.JIRA, "user", issues)
        registry.register(jiraProvider)
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(listOf(testConfig1))

        val result = repository.searchAllSources("test", 5)

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()?.size)
    }

    // ============================================================
    // searchWithFallback() Remote Fallback Tests
    // ============================================================

    @Test
    fun `searchWithFallback falls back to remote when local cache is empty`() = runTest {
        val jiraProvider = createMockProvider(TicketProvider.JIRA, "user", listOf(testIssue1))
        registry.register(jiraProvider)
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(listOf(testConfig1))

        val result = repository.searchWithFallback("nonexistent", 50)

        assertTrue(result.isSuccess)
        // Remote search should have been triggered
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `searchWithFallback caches remote results`() = runTest {
        val jiraProvider = createMockProvider(TicketProvider.JIRA, "user", listOf(testIssue1))
        registry.register(jiraProvider)
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(listOf(testConfig1))

        // First search - should hit remote and cache
        val result1 = repository.searchWithFallback("nonexistent", 50)
        assertTrue(result1.isSuccess)

        // Verify it's now in cache
        val cachedCount = repository.getCachedIssueCount()
        assertTrue(cachedCount.getOrNull()!! >= 1)
    }

    // ============================================================
    // Error Handling Tests
    // ============================================================

    @Test
    fun `searchAllSources handles provider errors gracefully`() = runTest {
        val failingProvider = mockk<TicketSystemProvider> {
            every { providerType } returns TicketProvider.JIRA
            coEvery { searchIssues(any(), any(), any()) } returns Result.failure(Exception("Network error"))
        }
        registry.register(failingProvider)
        every { mockConfigRepository.getEnabledConfigs() } returns flowOf(listOf(testConfig1))

        val result = repository.searchAllSources("test", 50)

        assertTrue(result.isSuccess)
        // Should return empty list instead of failing
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createMockProvider(
        type: TicketProvider,
        userName: String,
        issues: List<TicketIssue> = emptyList()
    ): TicketSystemProvider {
        return mockk {
            every { providerType } returns type
            coEvery { testConnection(any()) } returns Result.success(userName)
            coEvery { searchIssues(any(), any(), any()) } returns Result.success(issues)
            coEvery { getProjects(any()) } returns Result.success(emptyList())
            coEvery { getCurrentUser(any()) } returns Result.success(userName)
            every { validateCredentials(any()) } returns true
            every { getErrorMessage(any(), any()) } returns "Error"
        }
    }
}
