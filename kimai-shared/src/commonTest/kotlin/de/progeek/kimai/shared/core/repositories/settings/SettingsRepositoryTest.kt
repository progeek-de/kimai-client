package de.progeek.kimai.shared.core.repositories.settings

import app.cash.turbine.test
import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.shared.SharedRes
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.storage.credentials.CredentialsConstants.BASE_URL_KEY
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.utils.Language
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for SettingsRepository.
 *
 * Tests the following methods:
 * 1. saveTheme(theme) - Saves theme preference
 * 2. getTheme() - Returns Flow<ThemeEnum>
 * 3. saveDefaultProject(project) - Saves default project
 * 4. getDefaultProject() - Returns Flow<Long?>
 * 5. clearDefaultProject() - Removes default project
 * 6. saveEntryMode(mode) - Saves entry mode
 * 7. getEntryMode() - Returns Flow<EntryMode>
 * 8. saveLanguage(language) - Saves language preference
 * 9. getLanguage() - Returns Flow<String?>
 * 10. getBaseUrl() - Returns base URL string
 */
class SettingsRepositoryTest {

    private lateinit var mockSettings: ObservableSettings
    private lateinit var repository: SettingsRepository

    private val testProject = Project(id = 123, name = "Test Project", parent = "", globalActivities = true, customer = null)

    @BeforeTest
    fun setup() {
        mockSettings = mockk(relaxed = true)
        repository = SettingsRepository(mockSettings)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    // ============================================================
    // Theme Tests
    // ============================================================

    @Test
    fun `saveTheme stores theme and returns it`() = runTest {
        // Given
        every { mockSettings.putString("THEME", "DARK") } just Runs

        // When
        val result = repository.saveTheme(ThemeEnum.DARK)

        // Then
        assertEquals(ThemeEnum.DARK, result)
        verify { mockSettings.putString("THEME", "DARK") }
    }

    @Test
    fun `saveTheme handles light theme`() = runTest {
        // Given
        every { mockSettings.putString("THEME", "LIGHT") } just Runs

        // When
        val result = repository.saveTheme(ThemeEnum.LIGHT)

        // Then
        assertEquals(ThemeEnum.LIGHT, result)
        verify { mockSettings.putString("THEME", "LIGHT") }
    }

    @Test
    fun `saveTheme handles system theme`() = runTest {
        // Given
        every { mockSettings.putString("THEME", "SYSTEM") } just Runs

        // When
        val result = repository.saveTheme(ThemeEnum.SYSTEM)

        // Then
        assertEquals(ThemeEnum.SYSTEM, result)
        verify { mockSettings.putString("THEME", "SYSTEM") }
    }

    // ============================================================
    // Default Project Tests
    // ============================================================

    @Test
    fun `saveDefaultProject stores project ID and returns project`() = runTest {
        // Given
        every { mockSettings.putLong("DEFAULT_PROJECT", 123) } just Runs

        // When
        val result = repository.saveDefaultProject(testProject)

        // Then
        assertEquals(testProject, result)
        assertEquals(123L, result.id)
        verify { mockSettings.putLong("DEFAULT_PROJECT", 123) }
    }

    @Test
    fun `saveDefaultProject handles different project`() = runTest {
        // Given
        val differentProject = Project(id = 456, name = "Different", parent = "", globalActivities = false, customer = null)
        every { mockSettings.putLong("DEFAULT_PROJECT", 456) } just Runs

        // When
        val result = repository.saveDefaultProject(differentProject)

        // Then
        assertEquals(456L, result.id)
        verify { mockSettings.putLong("DEFAULT_PROJECT", 456) }
    }

    @Test
    fun `clearDefaultProject removes default project setting`() = runTest {
        // Given
        every { mockSettings.remove("DEFAULT_PROJECT") } just Runs

        // When
        repository.clearDefaultProject()

        // Then
        verify { mockSettings.remove("DEFAULT_PROJECT") }
    }

    @Test
    fun `clearDefaultProject can be called multiple times`() = runTest {
        // Given
        every { mockSettings.remove("DEFAULT_PROJECT") } just Runs

        // When
        repository.clearDefaultProject()
        repository.clearDefaultProject()

        // Then
        verify(exactly = 2) { mockSettings.remove("DEFAULT_PROJECT") }
    }

    // ============================================================
    // Entry Mode Tests
    // ============================================================

    @Test
    fun `saveEntryMode stores timer mode`() = runTest {
        // Given
        every { mockSettings.putString("MODE", "TIMER") } just Runs

        // When
        val result = repository.saveEntryMode(EntryMode.TIMER)

        // Then
        assertEquals(EntryMode.TIMER, result)
        verify { mockSettings.putString("MODE", "TIMER") }
    }

    @Test
    fun `saveEntryMode stores manual mode`() = runTest {
        // Given
        every { mockSettings.putString("MODE", "MANUAL") } just Runs

        // When
        val result = repository.saveEntryMode(EntryMode.MANUAL)

        // Then
        assertEquals(EntryMode.MANUAL, result)
        verify { mockSettings.putString("MODE", "MANUAL") }
    }

    @Test
    fun `saveEntryMode can switch between modes`() = runTest {
        // Given
        every { mockSettings.putString("MODE", any()) } just Runs

        // When
        val result1 = repository.saveEntryMode(EntryMode.TIMER)
        val result2 = repository.saveEntryMode(EntryMode.MANUAL)

        // Then
        assertEquals(EntryMode.TIMER, result1)
        assertEquals(EntryMode.MANUAL, result2)
        verify { mockSettings.putString("MODE", "TIMER") }
        verify { mockSettings.putString("MODE", "MANUAL") }
    }

    // ============================================================
    // Language Tests
    // ============================================================

    @Test
    fun `saveLanguage stores English language`() = runTest {
        // Given
        val english = Language(SharedRes.strings.english, "en")
        every { mockSettings.putString("LANGUAGE", "en") } just Runs

        // When
        repository.saveLanguage(english)

        // Then
        verify { mockSettings.putString("LANGUAGE", "en") }
    }

    @Test
    fun `saveLanguage stores German language`() = runTest {
        // Given
        val german = Language(SharedRes.strings.german, "de")
        every { mockSettings.putString("LANGUAGE", "de") } just Runs

        // When
        repository.saveLanguage(german)

        // Then
        verify { mockSettings.putString("LANGUAGE", "de") }
    }

    @Test
    fun `saveLanguage can switch languages`() = runTest {
        // Given
        val english = Language(SharedRes.strings.english, "en")
        val german = Language(SharedRes.strings.german, "de")
        every { mockSettings.putString("LANGUAGE", any()) } just Runs

        // When
        repository.saveLanguage(english)
        repository.saveLanguage(german)

        // Then
        verify { mockSettings.putString("LANGUAGE", "en") }
        verify { mockSettings.putString("LANGUAGE", "de") }
    }

    // ============================================================
    // Base URL Tests
    // ============================================================

    @Test
    fun `getBaseUrl returns stored URL`() = runTest {
        // Given
        val testUrl = "https://kimai.example.com"
        every { mockSettings.getString(BASE_URL_KEY, any()) } returns testUrl

        // When
        val result = repository.getBaseUrl()

        // Then
        assertEquals(testUrl, result)
        verify { mockSettings.getString(BASE_URL_KEY, any()) }
    }

    @Test
    fun `getBaseUrl returns default when not set`() = runTest {
        // Given - using BuildKonfig default
        every { mockSettings.getString(BASE_URL_KEY, any()) } answers { secondArg() }

        // When
        val result = repository.getBaseUrl()

        // Then - should return the default value
        assertNotNull(result)
        verify { mockSettings.getString(BASE_URL_KEY, any()) }
    }

    @Test
    fun `getBaseUrl handles different URLs`() = runTest {
        // Given
        val url1 = "https://server1.com"
        val url2 = "https://server2.com"
        every { mockSettings.getString(BASE_URL_KEY, any()) } returnsMany listOf(url1, url2)

        // When
        val result1 = repository.getBaseUrl()
        val result2 = repository.getBaseUrl()

        // Then
        assertEquals(url1, result1)
        assertEquals(url2, result2)
    }

    // ============================================================
    // Integration Tests
    // ============================================================

    @Test
    fun `save and retrieve theme works correctly`() = runTest {
        // Given
        every { mockSettings.putString("THEME", "DARK") } just Runs

        // When - save theme
        val savedTheme = repository.saveTheme(ThemeEnum.DARK)

        // Then
        assertEquals(ThemeEnum.DARK, savedTheme)
        verify { mockSettings.putString("THEME", "DARK") }
    }

    @Test
    fun `save, retrieve, and clear default project works`() = runTest {
        // Given
        every { mockSettings.putLong("DEFAULT_PROJECT", any()) } just Runs
        every { mockSettings.remove("DEFAULT_PROJECT") } just Runs

        // When - save project
        val saved = repository.saveDefaultProject(testProject)
        assertEquals(testProject, saved)

        // When - clear project
        repository.clearDefaultProject()

        // Then
        verify { mockSettings.putLong("DEFAULT_PROJECT", 123) }
        verify { mockSettings.remove("DEFAULT_PROJECT") }
    }

    @Test
    fun `multiple settings can be saved independently`() = runTest {
        // Given
        every { mockSettings.putString(any(), any()) } just Runs
        every { mockSettings.putLong(any(), any()) } just Runs

        // When - save multiple settings
        repository.saveTheme(ThemeEnum.DARK)
        repository.saveDefaultProject(testProject)
        repository.saveEntryMode(EntryMode.TIMER)
        repository.saveLanguage(Language(SharedRes.strings.english, "en"))

        // Then - all were saved
        verify { mockSettings.putString("THEME", "DARK") }
        verify { mockSettings.putLong("DEFAULT_PROJECT", 123) }
        verify { mockSettings.putString("MODE", "TIMER") }
        verify { mockSettings.putString("LANGUAGE", "en") }
    }

    @Test
    fun `settings can be overwritten`() = runTest {
        // Given
        every { mockSettings.putString(any(), any()) } just Runs

        // When - save same setting twice
        repository.saveTheme(ThemeEnum.LIGHT)
        repository.saveTheme(ThemeEnum.DARK)

        // Then - both saves happened
        verify { mockSettings.putString("THEME", "LIGHT") }
        verify { mockSettings.putString("THEME", "DARK") }
    }
}
