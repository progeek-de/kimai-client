package de.progeek.kimai.shared.ui.login.store

import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.core.repositories.auth.AuthRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginStoreTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var storeFactory: LoginStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testBaseUrl = "https://test.kimai.cloud"
    private val testEmail = "test@example.com"
    private val testPassword = "password123"

    @BeforeTest
    fun setup() {
        authRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)

        // Setup default return value for getBaseUrl
        every { settingsRepository.getBaseUrl() } returns testBaseUrl

        // Setup Koin for LoginStoreFactory dependency injection
        startKoin {
            modules(
                module {
                    single { authRepository }
                    single { settingsRepository }
                }
            )
        }

        storeFactory = LoginStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `initial state is not logged in and not loading`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertFalse(state.isLoggedIn, "Initial state should not be logged in")
        assertFalse(state.isLoading, "Initial state should not be loading")
        assertFalse(state.isError, "Initial state should not have error")
    }

    @Test
    fun `bootstrapper loads baseUrl from settings repository`() = runTest(testDispatcher) {
        val customBaseUrl = "https://custom.kimai.cloud"
        every { settingsRepository.getBaseUrl() } returns customBaseUrl

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(customBaseUrl, store.stateFlow.value.baseUrl, "BaseUrl should be loaded from settings")
    }

    @Test
    fun `Login intent with valid credentials updates state to logged in`() = runTest(testDispatcher) {
        val credentials = Credentials(email = testEmail, password = testPassword)
        coEvery { authRepository.login(testEmail, testPassword, testBaseUrl) } returns credentials

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Initial state
        assertFalse(store.stateFlow.value.isLoggedIn)

        // Accept login intent
        store.accept(LoginStore.Intent.Login(testEmail, testPassword))

        // Wait for async operations
        advanceUntilIdle()

        // Success state
        val state = store.stateFlow.value
        assertTrue(state.isLoggedIn, "Should be logged in after successful login")
        assertFalse(state.isLoading, "Should not be loading after success")
        assertFalse(state.isError, "Should not have error after success")
    }

    @Test
    fun `Login intent with invalid credentials updates state to error`() = runTest(testDispatcher) {
        coEvery { authRepository.login(testEmail, testPassword, testBaseUrl) } returns null

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Accept login intent
        store.accept(LoginStore.Intent.Login(testEmail, testPassword))

        // Wait for async operations
        advanceUntilIdle()

        // Error state
        val state = store.stateFlow.value
        assertFalse(state.isLoggedIn, "Should not be logged in after failed login")
        assertFalse(state.isLoading, "Should not be loading after failure")
        assertTrue(state.isError, "Should have error after failed login")
    }

    @Test
    fun `BaseUrl intent updates baseUrl in state`() = runTest(testDispatcher) {
        val newBaseUrl = "https://newserver.kimai.cloud"

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Accept baseUrl intent
        store.accept(LoginStore.Intent.BaseUrl(newBaseUrl))

        // Wait for state update
        advanceUntilIdle()

        // State after intent
        assertEquals(newBaseUrl, store.stateFlow.value.baseUrl, "BaseUrl should be updated")
    }

    @Test
    fun `Login intent clears error flag when starting new login`() = runTest(testDispatcher) {
        // First login fails
        coEvery { authRepository.login(testEmail, testPassword, testBaseUrl) } returns null

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // First login attempt (fails)
        store.accept(LoginStore.Intent.Login(testEmail, testPassword))
        advanceUntilIdle()

        assertTrue(store.stateFlow.value.isError, "Should have error after failed login")

        // Setup second login to succeed
        val credentials = Credentials(testEmail, "newpassword")
        coEvery { authRepository.login(testEmail, "newpassword", testBaseUrl) } returns credentials

        // Second login attempt (succeeds)
        store.accept(LoginStore.Intent.Login(testEmail, "newpassword"))
        advanceUntilIdle()

        val finalState = store.stateFlow.value
        assertFalse(finalState.isError, "Should not have error after successful login")
        assertTrue(finalState.isLoggedIn, "Should be logged in")
    }

    @Test
    fun `state preserves version field throughout login flow`() = runTest(testDispatcher) {
        val credentials = Credentials(testEmail, testPassword)
        coEvery { authRepository.login(any(), any(), any()) } returns credentials

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val initialVersion = store.stateFlow.value.version

        store.accept(LoginStore.Intent.Login(testEmail, testPassword))
        advanceUntilIdle()

        val finalVersion = store.stateFlow.value.version
        assertEquals(initialVersion, finalVersion, "Version should be preserved after login")
    }

    @Test
    fun `BaseUrl intent does not affect other state fields`() = runTest(testDispatcher) {
        val newBaseUrl = "https://newserver.kimai.cloud"

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val initialState = store.stateFlow.value

        store.accept(LoginStore.Intent.BaseUrl(newBaseUrl))
        advanceUntilIdle()

        val updatedState = store.stateFlow.value

        // Only baseUrl and isBaseUrlValid should change
        assertEquals(newBaseUrl, updatedState.baseUrl)
        assertTrue(updatedState.isBaseUrlValid, "Valid URL should set isBaseUrlValid to true")
        assertEquals(initialState.isLoggedIn, updatedState.isLoggedIn)
        assertEquals(initialState.isLoading, updatedState.isLoading)
        assertEquals(initialState.isError, updatedState.isError)
        assertEquals(initialState.version, updatedState.version)
    }

    @Test
    fun `Login intent uses current baseUrl from state`() = runTest(testDispatcher) {
        val customBaseUrl = "https://custom.kimai.cloud"
        val credentials = Credentials(testEmail, testPassword)

        coEvery { authRepository.login(testEmail, testPassword, customBaseUrl) } returns credentials

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // First set custom baseUrl
        store.accept(LoginStore.Intent.BaseUrl(customBaseUrl))
        advanceUntilIdle()

        // Then login with custom baseUrl
        store.accept(LoginStore.Intent.Login(testEmail, testPassword))
        advanceUntilIdle()

        assertTrue(store.stateFlow.value.isLoggedIn, "Login should succeed with custom baseUrl")
    }

    @Test
    fun `initial state has valid baseUrl`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertTrue(store.stateFlow.value.isBaseUrlValid, "Initial state should have valid baseUrl")
    }

    @Test
    fun `BaseUrl intent with valid url sets isBaseUrlValid to true`() = runTest(testDispatcher) {
        val validUrl = "https://valid.kimai.cloud"

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(LoginStore.Intent.BaseUrl(validUrl))
        advanceUntilIdle()

        assertTrue(store.stateFlow.value.isBaseUrlValid, "isBaseUrlValid should be true for valid URL")
        assertEquals(validUrl, store.stateFlow.value.baseUrl)
    }

    @Test
    fun `BaseUrl intent with invalid url sets isBaseUrlValid to false`() = runTest(testDispatcher) {
        val invalidUrl = "not-a-valid-url"

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(LoginStore.Intent.BaseUrl(invalidUrl))
        advanceUntilIdle()

        assertFalse(store.stateFlow.value.isBaseUrlValid, "isBaseUrlValid should be false for invalid URL")
        assertEquals(invalidUrl, store.stateFlow.value.baseUrl)
    }

    @Test
    fun `BaseUrl intent with empty url sets isBaseUrlValid to false`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(LoginStore.Intent.BaseUrl(""))
        advanceUntilIdle()

        assertFalse(store.stateFlow.value.isBaseUrlValid, "isBaseUrlValid should be false for empty URL")
    }

    @Test
    fun `BaseUrl intent with url missing protocol sets isBaseUrlValid to false`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(LoginStore.Intent.BaseUrl("example.com"))
        advanceUntilIdle()

        assertFalse(store.stateFlow.value.isBaseUrlValid, "isBaseUrlValid should be false for URL without protocol")
    }

    @Test
    fun `BaseUrl intent with localhost url sets isBaseUrlValid to true`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(LoginStore.Intent.BaseUrl("http://localhost:8080"))
        advanceUntilIdle()

        assertTrue(store.stateFlow.value.isBaseUrlValid, "isBaseUrlValid should be true for localhost URL")
    }

    @Test
    fun `BaseUrl intent with IP address url sets isBaseUrlValid to true`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(LoginStore.Intent.BaseUrl("http://192.168.1.100:8080"))
        advanceUntilIdle()

        assertTrue(store.stateFlow.value.isBaseUrlValid, "isBaseUrlValid should be true for IP address URL")
    }

    @Test
    fun `isBaseUrlValid updates correctly when switching between valid and invalid urls`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Start with valid URL
        store.accept(LoginStore.Intent.BaseUrl("https://valid.kimai.cloud"))
        advanceUntilIdle()
        assertTrue(store.stateFlow.value.isBaseUrlValid, "Should be valid after setting valid URL")

        // Switch to invalid URL
        store.accept(LoginStore.Intent.BaseUrl("invalid"))
        advanceUntilIdle()
        assertFalse(store.stateFlow.value.isBaseUrlValid, "Should be invalid after setting invalid URL")

        // Switch back to valid URL
        store.accept(LoginStore.Intent.BaseUrl("https://another-valid.com"))
        advanceUntilIdle()
        assertTrue(store.stateFlow.value.isBaseUrlValid, "Should be valid after setting valid URL again")
    }

    @Test
    fun `bootstrapper validates baseUrl from settings repository`() = runTest(testDispatcher) {
        val invalidBaseUrl = "not-a-url"
        every { settingsRepository.getBaseUrl() } returns invalidBaseUrl

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(invalidBaseUrl, store.stateFlow.value.baseUrl)
        assertFalse(store.stateFlow.value.isBaseUrlValid, "isBaseUrlValid should be false for invalid URL from settings")
    }
}
