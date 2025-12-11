package de.progeek.kimai.shared.core.network.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.openapi.apis.ProjectApi
import de.progeek.kimai.openapi.infrastructure.HttpResponse
import de.progeek.kimai.openapi.models.ProjectCollection
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
 * Tests for ProjectClient.
 * Verifies API interactions, error handling, and project retrieval.
 */
class ProjectClientTest {

    private lateinit var mockApi: ProjectApi
    private lateinit var mockSettings: ObservableSettings
    private lateinit var mockCipher: AesGCMCipher
    private lateinit var client: ProjectClient

    @BeforeTest
    fun setup() {
        mockApi = mockk(relaxed = true)
        mockSettings = mockk(relaxed = true)
        mockCipher = mockk(relaxed = true)
        client = ProjectClient(mockSettings, mockCipher, mockApi)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `getProjects returns success with list of projects`() = runTest {
        // Given
        val projects = listOf(
            ProjectCollection(name = "Project Alpha", visible = true, billable = true, globalActivities = true, id = 1, customer = 1),
            ProjectCollection(name = "Project Beta", visible = true, billable = true, globalActivities = false, id = 2, customer = 1),
            ProjectCollection(name = "Project Gamma", visible = true, billable = false, globalActivities = true, id = 3, customer = 2)
        )
        val response = mockk<HttpResponse<List<ProjectCollection>>> {
            every { success } returns true
            coEvery { body() } returns projects
        }
        coEvery { mockApi.getGetProjects() } returns response

        // When
        val result = client.getProjects()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        assertEquals("Project Alpha", result.getOrNull()?.get(0)?.name)
        assertEquals(1L, result.getOrNull()?.get(0)?.id)
        coVerify { mockApi.getGetProjects() }
    }

    @Test
    fun `getProjects returns empty list when no projects exist`() = runTest {
        // Given
        val response = mockk<HttpResponse<List<ProjectCollection>>> {
            every { success } returns true
            coEvery { body() } returns emptyList()
        }
        coEvery { mockApi.getGetProjects() } returns response

        // When
        val result = client.getProjects()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `getProjects returns failure when API returns error`() = runTest {
        // Given
        val response = mockk<HttpResponse<List<ProjectCollection>>> {
            every { success } returns false
        }
        coEvery { mockApi.getGetProjects() } returns response

        // When
        val result = client.getProjects()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Failed load projects", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProjects handles network exception`() = runTest {
        // Given
        coEvery { mockApi.getGetProjects() } throws Exception("Network error")

        // When
        val result = client.getProjects()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProjects handles timeout exception`() = runTest {
        // Given
        coEvery { mockApi.getGetProjects() } throws Exception("Timeout")

        // When
        val result = client.getProjects()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }
}
