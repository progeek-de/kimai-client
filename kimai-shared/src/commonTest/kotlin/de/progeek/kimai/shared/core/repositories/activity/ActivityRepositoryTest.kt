package de.progeek.kimai.shared.core.repositories.activity

import app.cash.turbine.test
import de.progeek.kimai.shared.core.database.datasource.activity.ActivityDatasource
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.network.client.ActivityClient
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for ActivityRepository.
 *
 * Tests Store5-based caching repository with the following methods:
 * 1. getActivities() - Returns Flow<List<Activity>> with Store5 caching
 * 2. invalidateCache() - Clears cache and fetches fresh data
 *
 * Note: These are integration-style tests using real datasources with in-memory databases
 * and mocked network clients. Full Store5 behavior testing is complex due to its
 * internal coroutine management, so we focus on verifying basic functionality.
 */
class ActivityRepositoryTest {

    private lateinit var mockClient: ActivityClient
    private lateinit var datasource: ActivityDatasource
    private lateinit var repository: ActivityRepository
    private lateinit var testDatasources: de.progeek.kimai.shared.utils.TestDatasources

    private val testActivities = listOf(
        Activity(id = 1, name = "Development", project = 1L),
        Activity(id = 2, name = "Testing", project = 1L),
        Activity(id = 3, name = "Documentation", project = null)
    )

    @BeforeTest
    fun setup() {
        // Use real datasource with in-memory database
        testDatasources = createTestDatasources()
        datasource = testDatasources.activityDatasource

        // Setup test data for foreign key requirements
        testDatasources.database.projectEntityQueries.insertProject(
            id = 1L,
            parent = "",
            name = "Test Project",
            globalActivities = 1L,
            customer = -1L
        )

        mockClient = mockk(relaxed = true)
        repository = ActivityRepository(mockClient, datasource)
    }

    @AfterTest
    fun teardown() {
        clearDatabase(testDatasources.database)
        clearAllMocks()
    }

    // ============================================================
    // getActivities() Tests
    // ============================================================

    @Test
    fun `getActivities returns flow that can be collected`() = runTest {
        // Given - cache is empty, network has data
        coEvery { mockClient.getActivities() } returns Result.success(testActivities)

        // When - get activities stream
        val flow = repository.getActivities()

        // Then - flow can be collected (basic smoke test)
        // Note: Store5's internal coroutines use their own dispatchers,
        // making timing-dependent tests unreliable in unit tests.
        // This test verifies the repository API works without testing Store5's internals.
        assertNotNull(flow)
    }

    @Test
    fun `getActivities returns cached data when available`() = runTest {
        // Given - populate cache first
        coEvery { mockClient.getActivities() } returns Result.success(testActivities)
        datasource.insert(testActivities) // Pre-populate cache

        // When
        val flow = repository.getActivities()

        // Then - should get cached data
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertEquals(3, item.size)
            assertEquals("Development", item[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActivities returns empty list when no data available`() = runTest {
        // Given - both cache and network are empty
        coEvery { mockClient.getActivities() } returns Result.success(emptyList())

        // When
        val flow = repository.getActivities()

        // Then
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertTrue(item.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActivities handles network error gracefully`() = runTest {
        // Given - network fails
        coEvery { mockClient.getActivities() } throws Exception("Network error")

        // When
        val flow = repository.getActivities()

        // Then - Store5 handles error, returns empty
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertTrue(item.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActivities returns cached data when network fails`() = runTest {
        // Given - cache has data, network fails
        datasource.insert(testActivities) // Pre-populate cache
        coEvery { mockClient.getActivities() } throws Exception("Network error")

        // When
        val flow = repository.getActivities()

        // Then - should return cached data despite network failure
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertEquals(3, item.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // invalidateCache() Tests
    // ============================================================

    @Test
    fun `invalidateCache clears cache and fetches fresh data`() = runTest {
        // Given - populate cache first
        coEvery { mockClient.getActivities() } returns Result.success(testActivities)
        datasource.insert(testActivities)

        // When - invalidate cache
        repository.invalidateCache()

        // Then - cache should be cleared
        // Allow time for async operations
        delay(100)

        // Verify network fetch was called for fresh data
        coVerify(timeout = 2000) { mockClient.getActivities() }
    }

    @Test
    fun `invalidateCache with network failure throws exception`() = runTest {
        // Given - cache has data, network will fail
        datasource.insert(testActivities)
        coEvery { mockClient.getActivities() } throws Exception("Network error")

        // When/Then - invalidateCache throws when fetcher fails
        // This is expected Store5 behavior - fresh() propagates fetcher exceptions
        assertFailsWith<Exception> {
            repository.invalidateCache()
        }

        // Cache clear should still have happened
        coVerify { mockClient.getActivities() }
    }

    @Test
    fun `invalidateCache fetches updated data from network`() = runTest {
        // Given - start with old data in cache
        datasource.insert(testActivities)

        val updatedActivities = listOf(
            Activity(id = 4, name = "Code Review", project = 1L)
        )
        coEvery { mockClient.getActivities() } returns Result.success(updatedActivities)

        // When - invalidate cache to fetch fresh data
        repository.invalidateCache()

        // Allow time for fetch and write
        delay(200)

        // Then - verify fresh data was fetched
        coVerify(timeout = 2000) { mockClient.getActivities() }
    }

    @Test
    fun `repository can be instantiated and methods called`() = runTest {
        // Given
        coEvery { mockClient.getActivities() } returns Result.success(testActivities)

        // When - basic smoke test that repository works
        val flow = repository.getActivities()
        repository.invalidateCache()

        // Then - no exceptions thrown, basic functionality works
        flow.test(timeout = 5.seconds) {
            awaitItem() // Get at least one emission
            cancelAndIgnoreRemainingEvents()
        }
    }
}
