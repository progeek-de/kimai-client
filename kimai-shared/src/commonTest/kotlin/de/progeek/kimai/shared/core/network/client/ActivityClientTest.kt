package de.progeek.kimai.shared.core.network.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.openapi.apis.ActivityApi
import de.progeek.kimai.openapi.infrastructure.HttpResponse
import de.progeek.kimai.openapi.models.ActivityCollection
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Tests for ActivityClient.
 * Verifies API interactions, error handling, and activity retrieval.
 */
class ActivityClientTest {

    private lateinit var mockApi: ActivityApi
    private lateinit var mockSettings: ObservableSettings
    private lateinit var mockCipher: AesGCMCipher
    private lateinit var client: ActivityClient

    @BeforeTest
    fun setup() {
        mockApi = mockk(relaxed = true)
        mockSettings = mockk(relaxed = true)
        mockCipher = mockk(relaxed = true)
        client = ActivityClient(mockSettings, mockCipher, mockApi)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `getActivities returns success with list of activities`() = runTest {
        // Given
        val activities = listOf(
            ActivityCollection(name = "Development", visible = true, billable = true, id = 1, project = 1),
            ActivityCollection(name = "Testing", visible = true, billable = true, id = 2, project = 1),
            ActivityCollection(name = "Documentation", visible = true, billable = false, id = 3, project = 2)
        )
        val response = mockk<HttpResponse<List<ActivityCollection>>> {
            every { success } returns true
            coEvery { body() } returns activities
        }
        coEvery { mockApi.getGetActivities() } returns response

        // When
        val result = client.getActivities()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        assertEquals("Development", result.getOrNull()?.get(0)?.name)
        assertEquals(1L, result.getOrNull()?.get(0)?.id)
        coVerify { mockApi.getGetActivities() }
    }

    @Test
    fun `getActivities returns empty list when no activities exist`() = runTest {
        // Given
        val response = mockk<HttpResponse<List<ActivityCollection>>> {
            every { success } returns true
            coEvery { body() } returns emptyList()
        }
        coEvery { mockApi.getGetActivities() } returns response

        // When
        val result = client.getActivities()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `getActivities returns failure when API returns error`() = runTest {
        // Given
        val response = mockk<HttpResponse<List<ActivityCollection>>> {
            every { success } returns false
        }
        coEvery { mockApi.getGetActivities() } returns response

        // When
        val result = client.getActivities()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error while getting activities", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getActivities handles network exception`() = runTest {
        // Given
        coEvery { mockApi.getGetActivities() } throws Exception("Network error")

        // When
        val result = client.getActivities()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getActivities handles timeout exception`() = runTest {
        // Given
        coEvery { mockApi.getGetActivities() } throws Exception("Timeout")

        // When
        val result = client.getActivities()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }
}
