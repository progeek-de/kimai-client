@file:OptIn(ExperimentalTestApi::class)

package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.testutils.TestData
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.testutils.createTestComponentContext
import de.progeek.kimai.shared.testutils.createTestDispatchers
import de.progeek.kimai.shared.testutils.createTestStoreFactory
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import dev.icerock.moko.resources.desc.StringDesc
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * UI tests for the individual settings components:
 * - ThemeSection
 * - LanguageSection
 * - DefaultProjectSection
 * - VersionInfo
 *
 * Each composable receives a [SettingsComponent], so behaviour is exercised through the
 * real component/store and verified against the mocked [SettingsRepository] via coVerify.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class SettingsComponentsTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var projectRepository: ProjectRepository

    @Before
    fun setUp() {
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
    }

    private fun startKoin(
        theme: ThemeEnum = ThemeEnum.LIGHT,
        defaultProjectId: Long? = null
    ) {
        settingsRepository = TestKoinModule.createMockSettingsRepository(
            theme = theme,
            defaultProjectId = defaultProjectId
        )
        projectRepository = TestKoinModule.createMockProjectRepository()
        TestKoinModule.startTestKoin(
            settingsRepository = settingsRepository,
            projectRepository = projectRepository
        )
    }

    private fun createComponent(): SettingsComponent =
        SettingsComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = {}
        )

    // ============================================================
    // ThemeSection
    // ============================================================

    @Test
    fun `theme section shows current light mode selection`() = runComposeUiTest {
        startKoin(theme = ThemeEnum.LIGHT)
        val component = createComponent()

        setContent {
            TestTheme {
                ThemeSection(component)
            }
        }
        waitForIdle()

        // Color Mode label and the current selection (Light Mode) should be visible
        onNodeWithText("Color Mode").assertExists()
        onNodeWithText("Light Mode").assertIsDisplayed()
    }

    @Test
    fun `theme section shows dark mode when set`() = runComposeUiTest {
        startKoin(theme = ThemeEnum.DARK)
        val component = createComponent()

        setContent {
            TestTheme {
                ThemeSection(component)
            }
        }
        waitForIdle()

        onNodeWithText("Dark Mode").assertExists()
    }

    @Test
    fun `selecting dark mode persists theme via repository`() = runComposeUiTest {
        startKoin(theme = ThemeEnum.LIGHT)
        val component = createComponent()

        setContent {
            TestTheme {
                ThemeSection(component)
            }
        }
        waitForIdle()

        // Expand the dropdown (trigger currently shows the Light Mode selection)
        onNodeWithText("Light Mode").performClick()
        waitForIdle()

        // Select the Dark Mode option from the open menu
        onAllNodesWithText("Dark Mode")[0].performClick()
        waitForIdle()

        coVerify(timeout = 2000) { settingsRepository.saveTheme(ThemeEnum.DARK) }
    }

    @Test
    fun `selecting light mode persists theme via repository`() = runComposeUiTest {
        startKoin(theme = ThemeEnum.DARK)
        val component = createComponent()

        setContent {
            TestTheme {
                ThemeSection(component)
            }
        }
        waitForIdle()

        // Trigger currently shows the Dark Mode selection
        onNodeWithText("Dark Mode").performClick()
        waitForIdle()

        onAllNodesWithText("Light Mode")[0].performClick()
        waitForIdle()

        coVerify(timeout = 2000) { settingsRepository.saveTheme(ThemeEnum.LIGHT) }
    }

    // ============================================================
    // LanguageSection
    // ============================================================

    @Test
    fun `language section shows english as default selection`() = runComposeUiTest {
        startKoin()
        val component = createComponent()

        setContent {
            TestTheme {
                LanguageSection(component)
            }
        }
        waitForIdle()

        // Languages label and the default selection (English) should be visible
        onNodeWithText("Languages").assertExists()
        onNodeWithText("English").assertIsDisplayed()
    }

    @Test
    fun `selecting german persists language via repository`() = runComposeUiTest {
        startKoin()
        val component = createComponent()

        setContent {
            TestTheme {
                LanguageSection(component)
            }
        }
        waitForIdle()

        // Open the dropdown (trigger currently shows English)
        onNodeWithText("English").performClick()
        waitForIdle()

        // Select the German option from the open menu
        onNodeWithText("German").performClick()
        waitForIdle()

        coVerify(timeout = 2000) {
            settingsRepository.saveLanguage(match { it.languageCode == "de" })
        }
    }

    // ============================================================
    // DefaultProjectSection
    // ============================================================

    @Test
    fun `default project section shows placeholder when none selected`() = runComposeUiTest {
        startKoin(defaultProjectId = null)
        val component = createComponent()

        setContent {
            TestTheme {
                DefaultProjectSection(component)
            }
        }
        waitForIdle()

        onNodeWithText("Default project").assertExists()
        onNodeWithText("Select default project").assertIsDisplayed()
    }

    @Test
    fun `selecting a project persists default project via repository`() = runComposeUiTest {
        startKoin(defaultProjectId = null)
        val component = createComponent()

        setContent {
            TestTheme {
                DefaultProjectSection(component)
            }
        }
        waitForIdle()

        // Open the dropdown via the placeholder
        onNodeWithText("Select default project").performClick()
        waitForIdle()

        // Select the first test project
        onNodeWithText(TestData.project1.name).performClick()
        waitForIdle()

        coVerify(timeout = 2000) {
            settingsRepository.saveDefaultProject(match { it.id == TestData.project1.id })
        }
    }

    @Test
    fun `clearing default project invokes repository clear`() = runComposeUiTest {
        startKoin(defaultProjectId = TestData.project1.id)
        val component = createComponent()

        setContent {
            TestTheme {
                DefaultProjectSection(component)
            }
        }
        waitForIdle()

        // With a default set, the trigger shows the project name; open the dropdown
        onNodeWithText(TestData.project1.name).performClick()
        waitForIdle()

        // The Delete entry only appears when a project is currently selected
        onNodeWithText("Delete").performClick()
        waitForIdle()

        coVerify(timeout = 2000) { settingsRepository.clearDefaultProject() }
    }

    // ============================================================
    // VersionInfo
    // ============================================================

    @Test
    fun `version info renders version label`() = runComposeUiTest {
        startKoin()

        setContent {
            TestTheme {
                VersionInfo()
            }
        }
        waitForIdle()

        // The version string is "Version: %s"; assert the prefix renders
        onNodeWithText("Version:", substring = true).assertExists()
    }
}
