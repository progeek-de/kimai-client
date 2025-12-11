package de.progeek.kimai.shared.core.network.client

import de.progeek.kimai.openapi.apis.DefaultApi
import io.ktor.client.statement.*
import io.ktor.http.*
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for AuthClient.
 * Verifies authentication logic, error handling, and API key configuration.
 */
class AuthClientTest {

    private lateinit var client: AuthClient

    @BeforeTest
    fun setup() {
        // Setup Koin for dependency injection testing
        startKoin {
            modules(
                module {
                    factory { (baseUrl: String) ->
                        mockk<DefaultApi>(relaxed = true)
                    }
                }
            )
        }
        client = AuthClient()
    }

    @AfterTest
    fun teardown() {
        stopKoin()
        clearAllMocks()
    }

    @Test
    fun `login returns success when credentials are valid`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val baseUrl = "https://kimai.example.com"

        val mockResponse = mockk<HttpResponse> {
            every { status } returns HttpStatusCode.OK
        }
        val mockApi = mockk<DefaultApi>(relaxed = true) {
            coEvery { getAppApiStatusPing() } returns mockk {
                every { response } returns mockResponse
            }
        }

        // Replace the Koin module with our mocked API
        stopKoin()
        startKoin {
            modules(
                module {
                    factory { (_: String) -> mockApi }
                }
            )
        }

        // Recreate client with new Koin context
        client = AuthClient()

        // When
        val result = client.login(email, password, baseUrl)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockApi.setApiKey(email, "X-AUTH-USER") }
        coVerify { mockApi.setApiKey(password, "X-AUTH-TOKEN") }
        coVerify { mockApi.getAppApiStatusPing() }
    }

    @Test
    fun `login returns failure when credentials are invalid`() = runTest {
        // Given
        val email = "invalid@example.com"
        val password = "wrongpassword"
        val baseUrl = "https://kimai.example.com"

        val mockResponse = mockk<HttpResponse> {
            every { status } returns HttpStatusCode.Unauthorized
        }
        val mockApi = mockk<DefaultApi>(relaxed = true) {
            coEvery { getAppApiStatusPing() } returns mockk {
                every { response } returns mockResponse
            }
        }

        stopKoin()
        startKoin {
            modules(
                module {
                    factory { (_: String) -> mockApi }
                }
            )
        }

        client = AuthClient()

        // When
        val result = client.login(email, password, baseUrl)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `login returns failure when server returns forbidden`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val baseUrl = "https://kimai.example.com"

        val mockResponse = mockk<HttpResponse> {
            every { status } returns HttpStatusCode.Forbidden
        }
        val mockApi = mockk<DefaultApi>(relaxed = true) {
            coEvery { getAppApiStatusPing() } returns mockk {
                every { response } returns mockResponse
            }
        }

        stopKoin()
        startKoin {
            modules(
                module {
                    factory { (_: String) -> mockApi }
                }
            )
        }

        client = AuthClient()

        // When
        val result = client.login(email, password, baseUrl)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `login returns failure when server returns internal error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val baseUrl = "https://kimai.example.com"

        val mockResponse = mockk<HttpResponse> {
            every { status } returns HttpStatusCode.InternalServerError
        }
        val mockApi = mockk<DefaultApi>(relaxed = true) {
            coEvery { getAppApiStatusPing() } returns mockk {
                every { response } returns mockResponse
            }
        }

        stopKoin()
        startKoin {
            modules(
                module {
                    factory { (_: String) -> mockApi }
                }
            )
        }

        client = AuthClient()

        // When
        val result = client.login(email, password, baseUrl)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `login handles network exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val baseUrl = "https://kimai.example.com"

        val mockApi = mockk<DefaultApi>(relaxed = true) {
            coEvery { getAppApiStatusPing() } throws Exception("Network error")
        }

        stopKoin()
        startKoin {
            modules(
                module {
                    factory { (_: String) -> mockApi }
                }
            )
        }

        client = AuthClient()

        // When
        val result = client.login(email, password, baseUrl)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login handles timeout exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val baseUrl = "https://kimai.example.com"

        val mockApi = mockk<DefaultApi>(relaxed = true) {
            coEvery { getAppApiStatusPing() } throws Exception("Timeout")
        }

        stopKoin()
        startKoin {
            modules(
                module {
                    factory { (_: String) -> mockApi }
                }
            )
        }

        client = AuthClient()

        // When
        val result = client.login(email, password, baseUrl)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login handles invalid URL exception`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val baseUrl = "invalid-url"

        val mockApi = mockk<DefaultApi>(relaxed = true) {
            coEvery { getAppApiStatusPing() } throws IllegalArgumentException("Invalid URL")
        }

        stopKoin()
        startKoin {
            modules(
                module {
                    factory { (_: String) -> mockApi }
                }
            )
        }

        client = AuthClient()

        // When
        val result = client.login(email, password, baseUrl)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invalid URL", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login sets API keys correctly`() = runTest {
        // Given
        val email = "user@test.com"
        val password = "secret123"
        val baseUrl = "https://test.kimai.cloud"

        val mockResponse = mockk<HttpResponse> {
            every { status } returns HttpStatusCode.OK
        }
        val mockApi = mockk<DefaultApi>(relaxed = true) {
            coEvery { getAppApiStatusPing() } returns mockk {
                every { response } returns mockResponse
            }
        }

        stopKoin()
        startKoin {
            modules(
                module {
                    factory { (_: String) -> mockApi }
                }
            )
        }

        client = AuthClient()

        // When
        client.login(email, password, baseUrl)

        // Then
        coVerify(exactly = 1) { mockApi.setApiKey(email, "X-AUTH-USER") }
        coVerify(exactly = 1) { mockApi.setApiKey(password, "X-AUTH-TOKEN") }
    }
}
