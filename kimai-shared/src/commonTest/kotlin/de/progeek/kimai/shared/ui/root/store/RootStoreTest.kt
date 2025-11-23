package de.progeek.kimai.shared.ui.root.store

import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RootStoreTest {

    private lateinit var credentialsRepository: CredentialsRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var storeFactory: RootStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        credentialsRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)

        // Setup Koin for RootStoreFactory dependency injection
        startKoin {
            modules(
                module {
                    single { credentialsRepository }
                    single { settingsRepository }
                }
            )
        }

        storeFactory = RootStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `initial state has null credentials and isLoading true`() = runTest(testDispatcher) {
        // Use a flow that doesn't emit immediately to preserve initial state
        val credentialsFlow = kotlinx.coroutines.flow.flow<Credentials?> {
            kotlinx.coroutines.delay(1)
            emit(null)
        }
        val themeFlow = kotlinx.coroutines.flow.flow {
            kotlinx.coroutines.delay(1)
            emit(ThemeEnum.LIGHT)
        }
        every { credentialsRepository.get() } returns credentialsFlow
        every { settingsRepository.getTheme() } returns themeFlow
        every { settingsRepository.getLanguage() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Check initial state before flows emit
        val state = store.stateFlow.value
        assertNull(state.credentials, "Initial credentials should be null")
        assertTrue(state.isLoading, "Initial isLoading should be true")
        assertEquals(ThemeEnum.LIGHT, state.theme, "Initial theme should be LIGHT")
    }

    @Test
    fun `bootstrapper loads credentials from repository`() = runTest(testDispatcher) {
        val testCredentials = Credentials(
            email = "test@example.com",
            password = "test-password"
        )

        every { credentialsRepository.get() } returns flowOf(testCredentials)
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.LIGHT)
        every { settingsRepository.getLanguage() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertNotNull(state.credentials, "Credentials should be loaded")
        assertEquals(testCredentials, state.credentials, "Loaded credentials should match")
        assertFalse(state.isLoading, "isLoading should be false after credentials loaded")
    }

    @Test
    fun `bootstrapper loads theme from settings repository`() = runTest(testDispatcher) {
        every { credentialsRepository.get() } returns flowOf(null)
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.DARK)
        every { settingsRepository.getLanguage() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(ThemeEnum.DARK, state.theme, "Theme should be DARK from settings")
    }

    @Test
    fun `isLoading becomes false when credentials are loaded`() = runTest(testDispatcher) {
        every { credentialsRepository.get() } returns flowOf(null)
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.LIGHT)
        every { settingsRepository.getLanguage() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Wait for flow to emit
        advanceUntilIdle()

        // After credentials flow emits, loading should be false
        assertFalse(store.stateFlow.value.isLoading, "Should not be loading after credentials loaded")
    }

    @Test
    fun `handles null credentials gracefully`() = runTest(testDispatcher) {
        every { credentialsRepository.get() } returns flowOf(null)
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.LIGHT)
        every { settingsRepository.getLanguage() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertNull(state.credentials, "Credentials should remain null when repository returns null")
        assertFalse(state.isLoading, "Should not be loading")
    }

    @Test
    fun `handles system theme enum`() = runTest(testDispatcher) {
        every { credentialsRepository.get() } returns flowOf(null)
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.SYSTEM)
        every { settingsRepository.getLanguage() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(ThemeEnum.SYSTEM, state.theme, "Theme should be SYSTEM")
    }

    @Test
    fun `handles dark theme`() = runTest(testDispatcher) {
        every { credentialsRepository.get() } returns flowOf(null)
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.DARK)
        every { settingsRepository.getLanguage() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(ThemeEnum.DARK, store.stateFlow.value.theme, "Theme should be DARK")
    }

    @Test
    fun `loads credentials and theme together`() = runTest(testDispatcher) {
        val testCredentials = Credentials("user@test.com", "password")
        every { credentialsRepository.get() } returns flowOf(testCredentials)
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.DARK)
        every { settingsRepository.getLanguage() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testCredentials, state.credentials)
        assertEquals(ThemeEnum.DARK, state.theme)
        assertFalse(state.isLoading)
    }
}
