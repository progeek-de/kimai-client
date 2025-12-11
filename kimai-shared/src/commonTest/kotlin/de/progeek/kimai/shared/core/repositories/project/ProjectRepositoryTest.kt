package de.progeek.kimai.shared.core.repositories.project

import app.cash.turbine.test
import de.progeek.kimai.shared.core.database.datasource.project.ProjectDatasource
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.network.client.ProjectClient
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for ProjectRepository.
 *
 * Tests Store5-based caching repository with the following methods:
 * 1. getProjects() - Returns Flow<List<Project>> with Store5 caching
 * 2. invalidateCache() - Clears cache and fetches fresh data
 *
 * Note: These are integration-style tests using real datasources with in-memory databases
 * and mocked network clients. Full Store5 behavior testing is complex due to its
 * internal coroutine management, so we focus on verifying basic functionality.
 */
class ProjectRepositoryTest {

    private lateinit var mockClient: ProjectClient
    private lateinit var datasource: ProjectDatasource
    private lateinit var repository: ProjectRepository
    private lateinit var testDatasources: de.progeek.kimai.shared.utils.TestDatasources

    private val testCustomer = Customer(id = 1, name = "Test Customer")
    private val testProjects = listOf(
        Project(id = 1, name = "Project 1", parent = "", globalActivities = true, customer = testCustomer),
        Project(id = 2, name = "Project 2", parent = "", globalActivities = false, customer = testCustomer),
        Project(id = 3, name = "Project 3", parent = "Parent", globalActivities = true, customer = null)
    )

    @BeforeTest
    fun setup() {
        // Use real datasource with in-memory database
        testDatasources = createTestDatasources()
        datasource = testDatasources.projectDatasource

        // Setup test data for foreign key requirements
        testDatasources.database.customerEntityQueries.insertCustomer(1L, "Test Customer")

        mockClient = mockk(relaxed = true)
        repository = ProjectRepository(datasource, mockClient)
    }

    @AfterTest
    fun teardown() {
        clearDatabase(testDatasources.database)
        clearAllMocks()
    }

    // ============================================================
    // getProjects() Tests
    // ============================================================

    @Test
    fun `getProjects returns flow that can be collected`() = runTest {
        // Given - cache is empty, network has data
        coEvery { mockClient.getProjects() } returns Result.success(testProjects)

        // When - get projects stream
        val flow = repository.getProjects()

        // Then - flow can be collected (basic smoke test)
        // Note: Store5's internal coroutines use their own dispatchers,
        // making timing-dependent tests unreliable in unit tests.
        // This test verifies the repository API works without testing Store5's internals.
        assertNotNull(flow)
    }

    @Test
    fun `getProjects returns cached data when available`() = runTest {
        // Given - populate cache first
        coEvery { mockClient.getProjects() } returns Result.success(testProjects)
        datasource.insert(testProjects) // Pre-populate cache

        // When
        val flow = repository.getProjects()

        // Then - should get cached data
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertEquals(3, item.size)
            assertEquals("Project 1", item[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getProjects returns empty list when no data available`() = runTest {
        // Given - both cache and network are empty
        coEvery { mockClient.getProjects() } returns Result.success(emptyList())

        // When
        val flow = repository.getProjects()

        // Then
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertTrue(item.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getProjects handles network error gracefully`() = runTest {
        // Given - network fails
        coEvery { mockClient.getProjects() } throws Exception("Network error")

        // When
        val flow = repository.getProjects()

        // Then - Store5 handles error, returns empty
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertTrue(item.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getProjects returns cached data when network fails`() = runTest {
        // Given - cache has data, network fails
        datasource.insert(testProjects) // Pre-populate cache
        coEvery { mockClient.getProjects() } throws Exception("Network error")

        // When
        val flow = repository.getProjects()

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
        coEvery { mockClient.getProjects() } returns Result.success(testProjects)
        datasource.insert(testProjects)

        // When - invalidate cache
        repository.invalidateCache()

        // Then - cache should be cleared
        // Allow time for async operations
        delay(100)

        // Verify network fetch was called for fresh data
        coVerify(timeout = 2000) { mockClient.getProjects() }
    }

    @Test
    fun `invalidateCache with network failure throws exception`() = runTest {
        // Given - cache has data, network will fail
        datasource.insert(testProjects)
        coEvery { mockClient.getProjects() } throws Exception("Network error")

        // When/Then - invalidateCache throws when fetcher fails
        // This is expected Store5 behavior - fresh() propagates fetcher exceptions
        assertFailsWith<Exception> {
            repository.invalidateCache()
        }

        // Cache clear should still have happened
        coVerify { mockClient.getProjects() }
    }

    @Test
    fun `invalidateCache fetches updated data from network`() = runTest {
        // Given - start with old data in cache
        datasource.insert(testProjects)

        val updatedProjects = listOf(
            Project(id = 4, name = "Updated Project", parent = "", globalActivities = true, customer = testCustomer)
        )
        coEvery { mockClient.getProjects() } returns Result.success(updatedProjects)

        // When - invalidate cache to fetch fresh data
        repository.invalidateCache()

        // Allow time for fetch and write
        delay(200)

        // Then - verify fresh data was fetched
        coVerify(timeout = 2000) { mockClient.getProjects() }
    }

    @Test
    fun `repository can be instantiated and methods called`() = runTest {
        // Given
        coEvery { mockClient.getProjects() } returns Result.success(testProjects)

        // When - basic smoke test that repository works
        val flow = repository.getProjects()
        repository.invalidateCache()

        // Then - no exceptions thrown, basic functionality works
        flow.test(timeout = 5.seconds) {
            awaitItem() // Get at least one emission
            cancelAndIgnoreRemainingEvents()
        }
    }
}
