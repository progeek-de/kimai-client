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

        // Only baseUrl should change
        assertEquals(newBaseUrl, updatedState.baseUrl)
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
    fun `successful login sets isLoggedIn to true and isError to false`() = runTest(testDispatcher) {
        val credentials = Credentials(testEmail, testPassword)
        coEvery { authRepository.login(testEmail, testPassword, testBaseUrl) } returns credentials

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(LoginStore.Intent.Login(testEmail, testPassword))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertTrue(state.isLoggedIn)
        assertFalse(state.isError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `failed login sets isError to true and isLoggedIn to false`() = runTest(testDispatcher) {
        coEvery { authRepository.login(testEmail, testPassword, testBaseUrl) } returns null

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(LoginStore.Intent.Login(testEmail, testPassword))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertTrue(state.isError)
        assertFalse(state.isLoggedIn)
        assertFalse(state.isLoading)
    }

    @Test
    fun `baseUrl is loaded from settings on initialization`() = runTest(testDispatcher) {
        val customUrl = "https://my-kimai.cloud"
        every { settingsRepository.getBaseUrl() } returns customUrl

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(customUrl, store.stateFlow.value.baseUrl)
    }
}
