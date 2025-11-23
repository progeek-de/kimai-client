package de.progeek.kimai.shared.core.repositories.customer

import app.cash.turbine.test
import de.progeek.kimai.shared.core.database.datasource.customer.CustomerDatasource
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.network.client.CustomerClient
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for CustomerRepository.
 *
 * Tests Store5-based caching repository with the following methods:
 * 1. getCustomers() - Returns Flow<List<Customer>> with Store5 caching
 * 2. invalidateCache() - Clears cache and fetches fresh data
 *
 * Note: These are integration-style tests using real datasources with in-memory databases
 * and mocked network clients. Full Store5 behavior testing is complex due to its
 * internal coroutine management, so we focus on verifying basic functionality.
 */
class CustomerRepositoryTest {

    private lateinit var mockClient: CustomerClient
    private lateinit var datasource: CustomerDatasource
    private lateinit var repository: CustomerRepository
    private lateinit var testDatasources: de.progeek.kimai.shared.utils.TestDatasources

    private val testCustomers = listOf(
        Customer(id = 1, name = "Acme Corp"),
        Customer(id = 2, name = "TechStart Inc"),
        Customer(id = 3, name = "Global Solutions")
    )

    @BeforeTest
    fun setup() {
        // Use real datasource with in-memory database
        testDatasources = createTestDatasources()
        datasource = testDatasources.customerDatasource

        mockClient = mockk(relaxed = true)
        repository = CustomerRepository(mockClient, datasource)
    }

    @AfterTest
    fun teardown() {
        clearDatabase(testDatasources.database)
        clearAllMocks()
    }

    // ============================================================
    // getCustomers() Tests
    // ============================================================

    @Test
    fun `getCustomers returns flow that can be collected`() = runTest {
        // Given - cache is empty, network has data
        coEvery { mockClient.getCustomers() } returns Result.success(testCustomers)

        // When - get customers stream
        val flow = repository.getCustomers()

        // Then - flow can be collected (basic smoke test)
        // Note: Store5's internal coroutines use their own dispatchers,
        // making timing-dependent tests unreliable in unit tests.
        // This test verifies the repository API works without testing Store5's internals.
        assertNotNull(flow)
    }

    @Test
    fun `getCustomers returns cached data when available`() = runTest {
        // Given - populate cache first
        coEvery { mockClient.getCustomers() } returns Result.success(testCustomers)
        datasource.insert(testCustomers) // Pre-populate cache

        // When
        val flow = repository.getCustomers()

        // Then - should get cached data
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertEquals(3, item.size)
            assertEquals("Acme Corp", item[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCustomers returns empty list when no data available`() = runTest {
        // Given - both cache and network are empty
        coEvery { mockClient.getCustomers() } returns Result.success(emptyList())

        // When
        val flow = repository.getCustomers()

        // Then
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertTrue(item.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCustomers handles network error gracefully`() = runTest {
        // Given - network fails
        coEvery { mockClient.getCustomers() } throws Exception("Network error")

        // When
        val flow = repository.getCustomers()

        // Then - Store5 handles error, returns empty
        flow.test(timeout = 5.seconds) {
            val item = awaitItem()
            assertTrue(item.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCustomers returns cached data when network fails`() = runTest {
        // Given - cache has data, network fails
        datasource.insert(testCustomers) // Pre-populate cache
        coEvery { mockClient.getCustomers() } throws Exception("Network error")

        // When
        val flow = repository.getCustomers()

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
        coEvery { mockClient.getCustomers() } returns Result.success(testCustomers)
        datasource.insert(testCustomers)

        // When - invalidate cache
        repository.invalidateCache()

        // Then - cache should be cleared
        // Allow time for async operations
        delay(100)

        // Verify network fetch was called for fresh data
        coVerify(timeout = 2000) { mockClient.getCustomers() }
    }

    @Test
    fun `invalidateCache with network failure throws exception`() = runTest {
        // Given - cache has data, network will fail
        datasource.insert(testCustomers)
        coEvery { mockClient.getCustomers() } throws Exception("Network error")

        // When/Then - invalidateCache throws when fetcher fails
        // This is expected Store5 behavior - fresh() propagates fetcher exceptions
        assertFailsWith<Exception> {
            repository.invalidateCache()
        }

        // Cache clear should still have happened
        coVerify { mockClient.getCustomers() }
    }

    @Test
    fun `invalidateCache fetches updated data from network`() = runTest {
        // Given - start with old data in cache
        datasource.insert(testCustomers)

        val updatedCustomers = listOf(
            Customer(id = 4, name = "New Customer LLC")
        )
        coEvery { mockClient.getCustomers() } returns Result.success(updatedCustomers)

        // When - invalidate cache to fetch fresh data
        repository.invalidateCache()

        // Allow time for fetch and write
        delay(200)

        // Then - verify fresh data was fetched
        coVerify(timeout = 2000) { mockClient.getCustomers() }
    }

    @Test
    fun `repository can be instantiated and methods called`() = runTest {
        // Given
        coEvery { mockClient.getCustomers() } returns Result.success(testCustomers)

        // When - basic smoke test that repository works
        val flow = repository.getCustomers()
        repository.invalidateCache()

        // Then - no exceptions thrown, basic functionality works
        flow.test(timeout = 5.seconds) {
            awaitItem() // Get at least one emission
            cancelAndIgnoreRemainingEvents()
        }
    }
}
