package de.progeek.kimai.shared.ui.settings.store

import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.ui.theme.BrandingEnum
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.utils.getLanguages
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStoreTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var credentialsRepository: CredentialsRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var storeFactory: SettingsStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testEmail = "test@example.com"
    private val testCustomer = Customer(id = 1, name = "Test Customer")
    private val testProject = Project(
        id = 1,
        name = "Test Project",
        parent = "",
        globalActivities = true,
        customer = testCustomer
    )
    private val testProjects = listOf(testProject)
    private val testLanguage = getLanguages().first()

    @BeforeTest
    fun setup() {
        settingsRepository = mockk(relaxed = true)
        credentialsRepository = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)

        // Setup default return values
        every { credentialsRepository.getCredentials() } returns null
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.LIGHT)
        every { settingsRepository.getBranding() } returns flowOf(BrandingEnum.KIMAI)
        every { settingsRepository.getDefaultProject() } returns flowOf(null)
        every { settingsRepository.getLanguage() } returns flowOf(null)
        every { projectRepository.getProjects() } returns flowOf(emptyList())

        // Setup Koin
        startKoin {
            modules(
                module {
                    single { settingsRepository }
                    single { credentialsRepository }
                    single { projectRepository }
                }
            )
        }

        storeFactory = SettingsStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `initial state has default values`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals("", state.email, "Initial email should be empty")
        assertEquals(ThemeEnum.LIGHT, state.theme, "Theme should be LIGHT after loading")
        assertEquals(BrandingEnum.KIMAI, state.branding, "Branding should be KIMAI by default")
        assertNull(state.defaultProject, "Default project should be null")
        assertNotNull(state.projects, "Projects list should not be null")
        assertEquals(testLanguage, state.language, "Language should be first available language")
    }

    @Test
    fun `bootstrapper loads email from credentials`() = runTest(testDispatcher) {
        val credentials = Credentials(email = testEmail, password = "password")
        every { credentialsRepository.getCredentials() } returns credentials

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(testEmail, store.stateFlow.value.email, "Email should be loaded from credentials")
    }

    @Test
    fun `bootstrapper loads theme from settings`() = runTest(testDispatcher) {
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.DARK)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(ThemeEnum.DARK, store.stateFlow.value.theme, "Theme should be loaded from settings")
    }

    @Test
    fun `bootstrapper loads projects from repository`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(testProjects)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(testProjects, store.stateFlow.value.projects, "Projects should be loaded from repository")
    }

    @Test
    fun `bootstrapper loads default project when set`() = runTest(testDispatcher) {
        every { settingsRepository.getDefaultProject() } returns flowOf(testProject.id.toLong())
        every { projectRepository.getProjects() } returns flowOf(testProjects)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(testProject, store.stateFlow.value.defaultProject, "Default project should be loaded")
    }

    @Test
    fun `bootstrapper loads language from settings`() = runTest(testDispatcher) {
        val germanLanguage = getLanguages().find { it.languageCode == "de" }
        assertNotNull(germanLanguage, "German language should be available")

        every { settingsRepository.getLanguage() } returns flowOf("de")

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(germanLanguage, store.stateFlow.value.language, "Language should be German")
    }

    @Test
    fun `ChangeTheme intent updates theme`() = runTest(testDispatcher) {
        every { settingsRepository.saveTheme(any()) } returns ThemeEnum.DARK

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(SettingsStore.Intent.ChangeTheme(ThemeEnum.DARK))
        advanceUntilIdle()

        assertEquals(ThemeEnum.DARK, store.stateFlow.value.theme, "Theme should be updated to DARK")
        verify { settingsRepository.saveTheme(ThemeEnum.DARK) }
    }

    @Test
    fun `UpdateDefaultProject intent updates default project`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(SettingsStore.Intent.UpdateDefaultProject(testProject))
        advanceUntilIdle()

        assertEquals(testProject, store.stateFlow.value.defaultProject, "Default project should be updated")
        verify { settingsRepository.saveDefaultProject(testProject) }
    }

    @Test
    fun `ClearDefaultProject intent clears default project`() = runTest(testDispatcher) {
        // First set a default project
        every { settingsRepository.getDefaultProject() } returns flowOf(testProject.id.toLong())
        every { projectRepository.getProjects() } returns flowOf(testProjects)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()
        assertNotNull(store.stateFlow.value.defaultProject, "Default project should be set initially")

        // Clear it
        store.accept(SettingsStore.Intent.ClearDefaultProject(null))
        advanceUntilIdle()

        assertNull(store.stateFlow.value.defaultProject, "Default project should be cleared")
        verify { settingsRepository.clearDefaultProject() }
    }

    @Test
    fun `ChangeLanguage intent updates language`() = runTest(testDispatcher) {
        val germanLanguage = getLanguages().find { it.languageCode == "de" }
        assertNotNull(germanLanguage, "German language should be available")

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(SettingsStore.Intent.ChangeLanguage(germanLanguage))
        advanceUntilIdle()

        assertEquals(germanLanguage, store.stateFlow.value.language, "Language should be updated to German")
        verify { settingsRepository.saveLanguage(germanLanguage) }
    }

    @Test
    fun `handles null credentials gracefully`() = runTest(testDispatcher) {
        every { credentialsRepository.getCredentials() } returns null

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals("", store.stateFlow.value.email, "Email should remain empty when no credentials")
    }

    @Test
    fun `loads all settings on initialization`() = runTest(testDispatcher) {
        val credentials = Credentials(email = testEmail, password = "password")
        val germanLanguage = getLanguages().find { it.languageCode == "de" }!!

        every { credentialsRepository.getCredentials() } returns credentials
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.DARK)
        every { settingsRepository.getDefaultProject() } returns flowOf(testProject.id.toLong())
        every { settingsRepository.getLanguage() } returns flowOf("de")
        every { projectRepository.getProjects() } returns flowOf(testProjects)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testEmail, state.email)
        assertEquals(ThemeEnum.DARK, state.theme)
        assertEquals(testProject, state.defaultProject)
        assertEquals(testProjects, state.projects)
        assertEquals(germanLanguage, state.language)
    }

    @Test
    fun `theme changes are persisted`() = runTest(testDispatcher) {
        every { settingsRepository.saveTheme(ThemeEnum.DARK) } returns ThemeEnum.DARK

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(SettingsStore.Intent.ChangeTheme(ThemeEnum.DARK))
        advanceUntilIdle()

        assertEquals(ThemeEnum.DARK, store.stateFlow.value.theme)
        verify(exactly = 1) { settingsRepository.saveTheme(ThemeEnum.DARK) }
    }

    // ============================================================
    // Branding Tests
    // ============================================================

    @Test
    fun `bootstrapper loads branding from settings`() = runTest(testDispatcher) {
        every { settingsRepository.getBranding() } returns flowOf(BrandingEnum.PROGEEK)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(BrandingEnum.PROGEEK, store.stateFlow.value.branding, "Branding should be loaded from settings")
    }

    @Test
    fun `ChangeBranding intent updates branding to PROGEEK`() = runTest(testDispatcher) {
        every { settingsRepository.saveBranding(any()) } returns BrandingEnum.PROGEEK

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(SettingsStore.Intent.ChangeBranding(BrandingEnum.PROGEEK))
        advanceUntilIdle()

        assertEquals(BrandingEnum.PROGEEK, store.stateFlow.value.branding, "Branding should be updated to PROGEEK")
        verify { settingsRepository.saveBranding(BrandingEnum.PROGEEK) }
    }

    @Test
    fun `ChangeBranding intent updates branding to KIMAI`() = runTest(testDispatcher) {
        // Start with PROGEEK
        every { settingsRepository.getBranding() } returns flowOf(BrandingEnum.PROGEEK)
        every { settingsRepository.saveBranding(any()) } returns BrandingEnum.KIMAI

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()
        assertEquals(BrandingEnum.PROGEEK, store.stateFlow.value.branding, "Initial branding should be PROGEEK")

        store.accept(SettingsStore.Intent.ChangeBranding(BrandingEnum.KIMAI))
        advanceUntilIdle()

        assertEquals(BrandingEnum.KIMAI, store.stateFlow.value.branding, "Branding should be updated to KIMAI")
        verify { settingsRepository.saveBranding(BrandingEnum.KIMAI) }
    }

    @Test
    fun `branding changes are persisted`() = runTest(testDispatcher) {
        every { settingsRepository.saveBranding(BrandingEnum.PROGEEK) } returns BrandingEnum.PROGEEK

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(SettingsStore.Intent.ChangeBranding(BrandingEnum.PROGEEK))
        advanceUntilIdle()

        assertEquals(BrandingEnum.PROGEEK, store.stateFlow.value.branding)
        verify(exactly = 1) { settingsRepository.saveBranding(BrandingEnum.PROGEEK) }
    }

    @Test
    fun `loads all settings including branding on initialization`() = runTest(testDispatcher) {
        val credentials = Credentials(email = testEmail, password = "password")
        val germanLanguage = getLanguages().find { it.languageCode == "de" }!!

        every { credentialsRepository.getCredentials() } returns credentials
        every { settingsRepository.getTheme() } returns flowOf(ThemeEnum.DARK)
        every { settingsRepository.getBranding() } returns flowOf(BrandingEnum.PROGEEK)
        every { settingsRepository.getDefaultProject() } returns flowOf(testProject.id.toLong())
        every { settingsRepository.getLanguage() } returns flowOf("de")
        every { projectRepository.getProjects() } returns flowOf(testProjects)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testEmail, state.email)
        assertEquals(ThemeEnum.DARK, state.theme)
        assertEquals(BrandingEnum.PROGEEK, state.branding)
        assertEquals(testProject, state.defaultProject)
        assertEquals(testProjects, state.projects)
        assertEquals(germanLanguage, state.language)
    }
}
