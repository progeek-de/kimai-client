package de.progeek.kimai.shared.core.repositories.auth

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.core.network.client.*
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.storage.credentials.CredentialsConstants.BASE_URL_KEY
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Test suite for AuthRepository.
 *
 * Tests the following methods:
 * 1. login(email, password, baseUrl) - Authenticates user and saves credentials
 * 2. logout() - Deletes stored credentials
 *
 * The repository coordinates authentication with the auth client, updates all API
 * clients with new credentials, and manages credential storage.
 */
class AuthRepositoryTest {

    private lateinit var mockAuthClient: AuthClient
    private lateinit var mockActivityClient: ActivityClient
    private lateinit var mockProjectClient: ProjectClient
    private lateinit var mockTimesheetsClient: TimesheetsClient
    private lateinit var mockCustomerClient: CustomerClient
    private lateinit var mockCredentialsRepo: CredentialsRepository
    private lateinit var mockSettings: ObservableSettings
    private lateinit var repository: AuthRepository

    private val testEmail = "test@example.com"
    private val testPassword = "password123"
    private val testBaseUrl = "https://kimai.example.com"
    private val testCredentials = Credentials(testEmail, testPassword)

    @BeforeTest
    fun setup() {
        mockAuthClient = mockk(relaxed = true)
        mockActivityClient = mockk(relaxed = true)
        mockProjectClient = mockk(relaxed = true)
        mockTimesheetsClient = mockk(relaxed = true)
        mockCustomerClient = mockk(relaxed = true)
        mockCredentialsRepo = mockk(relaxed = true)
        mockSettings = mockk(relaxed = true)

        repository = AuthRepository(
            authClient = mockAuthClient,
            activityClient = mockActivityClient,
            projectClient = mockProjectClient,
            timesheetsClient = mockTimesheetsClient,
            customerClient = mockCustomerClient,
            credentialsRepository = mockCredentialsRepo,
            settingsRepository = mockSettings
        )
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    // ============================================================
    // login() Tests
    // ============================================================

    @Test
    fun `login succeeds and returns credentials`() = runTest {
        // Given
        coEvery { mockAuthClient.login(testEmail, testPassword, testBaseUrl) } returns
            Result.success(Unit)
        coEvery { mockCredentialsRepo.save(any()) } returns Result.success(Unit)
        every { mockSettings.putString(BASE_URL_KEY, testBaseUrl) } just Runs

        // When
        val result = repository.login(testEmail, testPassword, testBaseUrl)

        // Then
        assertNotNull(result)
        assertEquals(testEmail, result.email)
        assertEquals(testPassword, result.password)

        // Verify all clients were updated
        verify { mockActivityClient.refresh(testBaseUrl, any()) }
        verify { mockCustomerClient.refresh(testBaseUrl, any()) }
        verify { mockProjectClient.refresh(testBaseUrl, any()) }
        verify { mockTimesheetsClient.refresh(testBaseUrl, any()) }

        // Verify credentials were saved
        coVerify { mockCredentialsRepo.save(match {
            it.email == testEmail && it.password == testPassword
        }) }

        // Verify base URL was saved
        verify { mockSettings.putString(BASE_URL_KEY, testBaseUrl) }
    }

    @Test
    fun `login fails and returns null when auth client fails`() = runTest {
        // Given
        coEvery { mockAuthClient.login(testEmail, testPassword, testBaseUrl) } returns
            Result.failure(Exception("Authentication failed"))

        // When
        val result = repository.login(testEmail, testPassword, testBaseUrl)

        // Then
        assertNull(result)

        // Verify clients were NOT updated
        verify(exactly = 0) { mockActivityClient.refresh(any(), any()) }
        verify(exactly = 0) { mockCustomerClient.refresh(any(), any()) }
        verify(exactly = 0) { mockProjectClient.refresh(any(), any()) }
        verify(exactly = 0) { mockTimesheetsClient.refresh(any(), any()) }

        // Verify credentials were NOT saved
        coVerify(exactly = 0) { mockCredentialsRepo.save(any()) }
        verify(exactly = 0) { mockSettings.putString(any(), any()) }
    }

    @Test
    fun `login updates all API clients with credentials`() = runTest {
        // Given
        coEvery { mockAuthClient.login(any(), any(), any()) } returns Result.success(Unit)
        coEvery { mockCredentialsRepo.save(any()) } returns Result.success(Unit)
        every { mockSettings.putString(any(), any()) } just Runs

        // When
        repository.login(testEmail, testPassword, testBaseUrl)

        // Then - verify each client's refresh was called exactly once
        verify(exactly = 1) { mockActivityClient.refresh(testBaseUrl, match {
            it.email == testEmail && it.password == testPassword
        }) }
        verify(exactly = 1) { mockCustomerClient.refresh(testBaseUrl, match {
            it.email == testEmail && it.password == testPassword
        }) }
        verify(exactly = 1) { mockProjectClient.refresh(testBaseUrl, match {
            it.email == testEmail && it.password == testPassword
        }) }
        verify(exactly = 1) { mockTimesheetsClient.refresh(testBaseUrl, match {
            it.email == testEmail && it.password == testPassword
        }) }
    }

    @Test
    fun `login saves base URL to settings`() = runTest {
        // Given
        coEvery { mockAuthClient.login(any(), any(), any()) } returns Result.success(Unit)
        coEvery { mockCredentialsRepo.save(any()) } returns Result.success(Unit)
        every { mockSettings.putString(any(), any()) } just Runs

        // When
        repository.login(testEmail, testPassword, testBaseUrl)

        // Then
        verify { mockSettings.putString(BASE_URL_KEY, testBaseUrl) }
    }

    @Test
    fun `login with different base URL updates clients correctly`() = runTest {
        // Given
        val differentBaseUrl = "https://different.example.com"
        coEvery { mockAuthClient.login(any(), any(), any()) } returns Result.success(Unit)
        coEvery { mockCredentialsRepo.save(any()) } returns Result.success(Unit)
        every { mockSettings.putString(any(), any()) } just Runs

        // When
        repository.login(testEmail, testPassword, differentBaseUrl)

        // Then
        verify { mockActivityClient.refresh(differentBaseUrl, any()) }
        verify { mockSettings.putString(BASE_URL_KEY, differentBaseUrl) }
    }

    @Test
    fun `login handles network timeout gracefully`() = runTest {
        // Given
        coEvery { mockAuthClient.login(any(), any(), any()) } returns
            Result.failure(Exception("Network timeout"))

        // When
        val result = repository.login(testEmail, testPassword, testBaseUrl)

        // Then
        assertNull(result)
        coVerify(exactly = 0) { mockCredentialsRepo.save(any()) }
    }

    // ============================================================
    // logout() Tests
    // ============================================================

    @Test
    fun `logout deletes credentials successfully`() = runTest {
        // Given
        coEvery { mockCredentialsRepo.delete() } returns Result.success(Unit)

        // When
        val result = repository.logout()

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockCredentialsRepo.delete() }
    }

    @Test
    fun `logout returns failure when credentials deletion fails`() = runTest {
        // Given
        val error = Exception("Failed to delete credentials")
        coEvery { mockCredentialsRepo.delete() } returns Result.failure(error)

        // When
        val result = repository.logout()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
        coVerify { mockCredentialsRepo.delete() }
    }

    @Test
    fun `logout can be called multiple times`() = runTest {
        // Given
        coEvery { mockCredentialsRepo.delete() } returns Result.success(Unit)

        // When
        val result1 = repository.logout()
        val result2 = repository.logout()

        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        coVerify(exactly = 2) { mockCredentialsRepo.delete() }
    }

    @Test
    fun `logout delegates to credentials repository`() = runTest {
        // Given
        coEvery { mockCredentialsRepo.delete() } returns Result.success(Unit)

        // When
        repository.logout()

        // Then - verify it's just delegating, not doing extra work
        coVerify(exactly = 1) { mockCredentialsRepo.delete() }
        confirmVerified(mockCredentialsRepo)
    }

    // ============================================================
    // Integration Tests
    // ============================================================

    @Test
    fun `login and logout flow works correctly`() = runTest {
        // Given - setup for login
        coEvery { mockAuthClient.login(any(), any(), any()) } returns Result.success(Unit)
        coEvery { mockCredentialsRepo.save(any()) } returns Result.success(Unit)
        coEvery { mockCredentialsRepo.delete() } returns Result.success(Unit)
        every { mockSettings.putString(any(), any()) } just Runs

        // When - login
        val loginResult = repository.login(testEmail, testPassword, testBaseUrl)

        // Then - login succeeded
        assertNotNull(loginResult)
        coVerify { mockCredentialsRepo.save(any()) }

        // When - logout
        val logoutResult = repository.logout()

        // Then - logout succeeded
        assertTrue(logoutResult.isSuccess)
        coVerify { mockCredentialsRepo.delete() }
    }

    @Test
    fun `failed login followed by logout still works`() = runTest {
        // Given - login will fail
        coEvery { mockAuthClient.login(any(), any(), any()) } returns
            Result.failure(Exception("Auth failed"))
        coEvery { mockCredentialsRepo.delete() } returns Result.success(Unit)

        // When - attempt login (fails)
        val loginResult = repository.login(testEmail, testPassword, testBaseUrl)

        // Then - login failed
        assertNull(loginResult)

        // When - logout anyway
        val logoutResult = repository.logout()

        // Then - logout still works
        assertTrue(logoutResult.isSuccess)
    }
}
