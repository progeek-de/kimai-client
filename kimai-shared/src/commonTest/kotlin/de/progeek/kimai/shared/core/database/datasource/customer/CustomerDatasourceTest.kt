package de.progeek.kimai.shared.core.database.datasource.customer

import app.cash.turbine.test
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test suite for CustomerDatasource.
 *
 * Tests the following methods:
 * 1. getAll() - Returns Flow<List<Customer>>
 * 2. insert(list: List<Customer>) - Returns Result<List<Customer>>
 * 3. deleteAll() - Returns Unit
 */
class CustomerDatasourceTest {

    private lateinit var datasources: de.progeek.kimai.shared.utils.TestDatasources
    private lateinit var customerDatasource: CustomerDatasource

    @BeforeTest
    fun setup() {
        datasources = createTestDatasources()
        customerDatasource = datasources.customerDatasource
    }

    @AfterTest
    fun teardown() {
        clearDatabase(datasources.database)
    }

    private fun createTestCustomer(
        id: Long = 1L,
        name: String = "Test Customer"
    ) = Customer(
        id = id,
        name = name
    )

    // ============================================================
    // getAll() Tests
    // ============================================================

    @Test
    fun `getAll returns empty list when no customers exist`() = runTest {
        // When
        val result = customerDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns all customers from database`() = runTest {
        // Given
        val customers = listOf(
            createTestCustomer(id = 1L, name = "Acme Corporation"),
            createTestCustomer(id = 2L, name = "Globex Inc"),
            createTestCustomer(id = 3L, name = "Initech")
        )
        customerDatasource.insert(customers)

        // When
        val result = customerDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(3, items.size)
            assertTrue(items.any { it.name == "Acme Corporation" })
            assertTrue(items.any { it.name == "Globex Inc" })
            assertTrue(items.any { it.name == "Initech" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll correctly maps customer fields`() = runTest {
        // Given
        val customer = createTestCustomer(id = 100L, name = "Special Customer")
        customerDatasource.insert(listOf(customer))

        // When
        val result = customerDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            val found = items[0]
            assertEquals(100L, found.id)
            assertEquals("Special Customer", found.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // insert(list: List<Customer>) Tests
    // ============================================================

    @Test
    fun `insert successfully stores multiple customers`() = runTest {
        // Given
        val customers = listOf(
            createTestCustomer(id = 10L, name = "Customer 1"),
            createTestCustomer(id = 11L, name = "Customer 2"),
            createTestCustomer(id = 12L, name = "Customer 3")
        )

        // When
        val result = customerDatasource.insert(customers)

        // Then
        assertTrue(result.isSuccess)
        val returned = result.getOrNull()
        assertNotNull(returned)
        assertEquals(3, returned.size)

        // Verify they were inserted
        customerDatasource.getAll().test {
            val items = awaitItem()
            assertEquals(3, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert empty list succeeds and returns empty list`() = runTest {
        // Given
        val emptyList = emptyList<Customer>()

        // When
        val result = customerDatasource.insert(emptyList)

        // Then
        assertTrue(result.isSuccess)
        val returned = result.getOrNull()
        assertNotNull(returned)
        assertTrue(returned.isEmpty())
    }

    @Test
    fun `insert replaces existing customer with same id`() = runTest {
        // Given
        val original = listOf(createTestCustomer(id = 20L, name = "Original Name"))
        customerDatasource.insert(original)

        // When - Insert with same ID but different name
        val replacement = listOf(createTestCustomer(id = 20L, name = "Updated Name"))
        val result = customerDatasource.insert(replacement)

        // Then
        assertTrue(result.isSuccess)

        // Verify it was replaced
        customerDatasource.getAll().test {
            val items = awaitItem()
            val customer = items.find { it.id == 20L }
            assertNotNull(customer)
            assertEquals("Updated Name", customer.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert preserves customer data integrity`() = runTest {
        // Given - Customer with special characters in name
        val customer = createTestCustomer(
            id = 30L,
            name = "Test & Co. (Parent Company)"
        )

        // When
        val result = customerDatasource.insert(listOf(customer))

        // Then
        assertTrue(result.isSuccess)

        // Verify data was preserved exactly
        customerDatasource.getAll().test {
            val items = awaitItem()
            val found = items.find { it.id == 30L }
            assertNotNull(found)
            assertEquals("Test & Co. (Parent Company)", found.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // deleteAll() Tests
    // ============================================================

    @Test
    fun `deleteAll removes all customers from database`() = runTest {
        // Given
        val customers = listOf(
            createTestCustomer(id = 40L, name = "Delete 1"),
            createTestCustomer(id = 41L, name = "Delete 2"),
            createTestCustomer(id = 42L, name = "Delete 3")
        )
        customerDatasource.insert(customers)

        // When
        customerDatasource.deleteAll()

        // Then
        customerDatasource.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteAll on empty database succeeds`() = runTest {
        // When - Delete from empty database
        customerDatasource.deleteAll()

        // Then - Should not throw error
        customerDatasource.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
