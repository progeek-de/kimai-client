package de.progeek.kimai.shared.core.mapper

import de.progeek.kimai.openapi.models.Customer
import de.progeek.kimai.openapi.models.CustomerCollection
import de.progeek.kimai.openapi.models.CustomerEntity
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test suite for customer mapper functions.
 *
 * Tests the following mapper functions:
 * 1. CustomerCollection.map()
 * 2. de.progeek.kimai.shared.CustomerEntity.map() (database entity)
 * 3. Customer.toCustomer()
 */
class CustomerMapperTest {

    // ============================================================
    // CustomerCollection.map() Tests
    // ============================================================

    @Test
    fun `map from CustomerCollection maps all fields correctly`() {
        // Given
        val collection = CustomerCollection(
            id = 100,
            name = "Acme Corporation",
            visible = true,
            billable = true,
            currency = "USD"
        )

        // When
        val customer = collection.map()

        // Then
        assertEquals(100L, customer.id)
        assertEquals("Acme Corporation", customer.name)
    }

    @Test
    fun `map from CustomerCollection handles null id`() {
        // Given
        val collection = CustomerCollection(
            id = null,
            name = "Unknown Customer",
            visible = false,
            billable = false,
            currency = "EUR"
        )

        // When
        val customer = collection.map()

        // Then
        assertEquals(-1L, customer.id)
        assertEquals("Unknown Customer", customer.name)
    }

    @Test
    fun `map from CustomerCollection extracts only id and name`() {
        // Given
        val collection = CustomerCollection(
            id = 200,
            name = "Test Corp",
            visible = true,
            billable = true,
            currency = "GBP",
            number = "C-12345",
            comment = "VIP customer"
        )

        // When
        val customer = collection.map()

        // Then
        // Mapper only extracts id and name, ignoring other fields
        assertEquals(200L, customer.id)
        assertEquals("Test Corp", customer.name)
    }

    // ============================================================
    // de.progeek.kimai.shared.CustomerEntity.map() (Database Entity) Tests
    // ============================================================

    @Test
    fun `map from database CustomerEntity maps all fields correctly`() {
        // Given
        val dbEntity = de.progeek.kimai.shared.CustomerEntity(
            id = 300L,
            name = "Database Customer"
        )

        // When
        val customer = dbEntity.map()

        // Then
        assertEquals(300L, customer.id)
        assertEquals("Database Customer", customer.name)
    }

    @Test
    fun `map from database CustomerEntity preserves exact values`() {
        // Given
        val dbEntity = de.progeek.kimai.shared.CustomerEntity(
            id = 9999L,
            name = "Edge Case Customer"
        )

        // When
        val customer = dbEntity.map()

        // Then
        assertEquals(9999L, customer.id)
        assertEquals("Edge Case Customer", customer.name)
    }

    // ============================================================
    // Customer.toCustomer() (API Model) Tests
    // ============================================================

    @Test
    fun `toCustomer from API Customer maps all fields correctly`() {
        // Given
        val apiCustomer = Customer(
            id = 400,
            name = "API Customer",
            visible = true,
            billable = true
        )

        // When
        val customer = apiCustomer.toCustomer()

        // Then
        assertEquals(400L, customer.id)
        assertEquals("API Customer", customer.name)
    }

    @Test
    fun `toCustomer from API Customer handles null id`() {
        // Given
        val apiCustomer = Customer(
            id = null,
            name = "No ID Customer",
            visible = false,
            billable = false
        )

        // When
        val customer = apiCustomer.toCustomer()

        // Then
        assertEquals(-1L, customer.id)
        assertEquals("No ID Customer", customer.name)
    }

    @Test
    fun `toCustomer from API Customer extracts only id and name`() {
        // Given
        val apiCustomer = Customer(
            id = 500,
            name = "Complex Customer",
            visible = true,
            billable = true,
            number = "ABC-123",
            comment = "Important client",
            color = "#FF0000"
        )

        // When
        val customer = apiCustomer.toCustomer()

        // Then
        // Mapper only extracts id and name, ignoring other fields
        assertEquals(500L, customer.id)
        assertEquals("Complex Customer", customer.name)
    }

    @Test
    fun `all three mappers produce consistent output for same data`() {
        // Given
        val id = 600L
        val name = "Consistent Customer"

        val collection = CustomerCollection(
            id = id.toInt(),
            name = name,
            visible = true,
            billable = true,
            currency = "USD"
        )

        val dbEntity = de.progeek.kimai.shared.CustomerEntity(
            id = id,
            name = name
        )

        val apiCustomer = Customer(
            id = id.toInt(),
            name = name,
            visible = true,
            billable = true
        )

        // When
        val fromCollection = collection.map()
        val fromDbEntity = dbEntity.map()
        val fromApiCustomer = apiCustomer.toCustomer()

        // Then - All three should produce the same result
        assertEquals(id, fromCollection.id)
        assertEquals(name, fromCollection.name)

        assertEquals(id, fromDbEntity.id)
        assertEquals(name, fromDbEntity.name)

        assertEquals(id, fromApiCustomer.id)
        assertEquals(name, fromApiCustomer.name)

        // Verify they are equal
        assertEquals(fromCollection, fromDbEntity)
        assertEquals(fromDbEntity, fromApiCustomer)
        assertEquals(fromCollection, fromApiCustomer)
    }
}
