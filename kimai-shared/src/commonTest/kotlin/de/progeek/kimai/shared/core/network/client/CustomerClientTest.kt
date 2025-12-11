package de.progeek.kimai.shared.core.network.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.openapi.apis.CustomerApi
import de.progeek.kimai.openapi.infrastructure.HttpResponse
import de.progeek.kimai.openapi.models.CustomerCollection
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for CustomerClient.
 * Verifies API interactions, error handling, and customer retrieval.
 */
class CustomerClientTest {

    private lateinit var mockApi: CustomerApi
    private lateinit var mockSettings: ObservableSettings
    private lateinit var mockCipher: AesGCMCipher
    private lateinit var client: CustomerClient

    @BeforeTest
    fun setup() {
        mockApi = mockk(relaxed = true)
        mockSettings = mockk(relaxed = true)
        mockCipher = mockk(relaxed = true)
        client = CustomerClient(mockSettings, mockCipher, mockApi)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `getCustomers returns success with list of customers`() = runTest {
        // Given
        val customers = listOf(
            CustomerCollection(name = "Customer A", visible = true, billable = true, currency = "EUR", id = 1),
            CustomerCollection(name = "Customer B", visible = true, billable = true, currency = "USD", id = 2),
            CustomerCollection(name = "Customer C", visible = true, billable = false, currency = "GBP", id = 3)
        )
        val response = mockk<HttpResponse<List<CustomerCollection>>> {
            every { success } returns true
            coEvery { body() } returns customers
        }
        coEvery { mockApi.getGetCustomers() } returns response

        // When
        val result = client.getCustomers()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        assertEquals("Customer A", result.getOrNull()?.get(0)?.name)
        assertEquals(1L, result.getOrNull()?.get(0)?.id)
        coVerify { mockApi.getGetCustomers() }
    }

    @Test
    fun `getCustomers returns empty list when no customers exist`() = runTest {
        // Given
        val response = mockk<HttpResponse<List<CustomerCollection>>> {
            every { success } returns true
            coEvery { body() } returns emptyList()
        }
        coEvery { mockApi.getGetCustomers() } returns response

        // When
        val result = client.getCustomers()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `getCustomers returns failure when API returns error`() = runTest {
        // Given
        val response = mockk<HttpResponse<List<CustomerCollection>>> {
            every { success } returns false
        }
        coEvery { mockApi.getGetCustomers() } returns response

        // When
        val result = client.getCustomers()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error while getting customers", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCustomers handles network exception`() = runTest {
        // Given
        coEvery { mockApi.getGetCustomers() } throws Exception("Network error")

        // When
        val result = client.getCustomers()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCustomers handles timeout exception`() = runTest {
        // Given
        coEvery { mockApi.getGetCustomers() } throws Exception("Timeout")

        // When
        val result = client.getCustomers()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }
}
