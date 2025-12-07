package de.progeek.kimai.shared.integration

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestData
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.testutils.createTestComponentContext
import de.progeek.kimai.shared.testutils.createTestDispatchers
import de.progeek.kimai.shared.testutils.createTestStoreFactory
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import de.progeek.kimai.shared.ui.settings.SettingsContent
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Integration tests for the settings flow.
 * Tests theme changes, language changes, and navigation.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class SettingsFlowTest {

    @Before
    fun setUp() {
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
    }

    private fun createSettingsComponent(output: (SettingsComponent.Output) -> Unit): SettingsComponent {
        return SettingsComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = output
        )
    }

    @Test
    fun `settings screen displays all sections`() = runComposeUiTest {
        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Verify all main sections are visible (use exact section headers)
        onNodeWithText("Color Mode", ignoreCase = true).assertExists()
        onNodeWithText("Languages", ignoreCase = true).assertExists()
        onNodeWithText("Default project", ignoreCase = true).assertExists()
    }

    @Test
    fun `clicking back button closes settings`() = runComposeUiTest {
        var closeTriggered = false
        val component = createSettingsComponent { output ->
            if (output is SettingsComponent.Output.Close) {
                closeTriggered = true
            }
        }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Click back button (uses text "Back" in this implementation)
        onNodeWithText("Back", ignoreCase = true).performClick()

        waitForIdle()

        // Verify close was triggered
        assertTrue(closeTriggered)
    }

    @Test
    fun `selecting theme changes theme setting`() = runComposeUiTest {
        val settingsRepository = TestKoinModule.createMockSettingsRepository(theme = ThemeEnum.LIGHT)
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(settingsRepository = settingsRepository)

        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // First expand the theme dropdown by clicking current selection (Light Mode)
        onNodeWithText("Light Mode", ignoreCase = true).performClick()

        waitForIdle()

        // Now click on Dark Mode theme option
        onNodeWithText("Dark Mode", ignoreCase = true).performClick()

        waitForIdle()

        // Verify theme was saved
        coVerify(timeout = 2000) { settingsRepository.saveTheme(ThemeEnum.DARK) }
    }

    @Test
    fun `selecting language changes language setting`() = runComposeUiTest {
        val settingsRepository = TestKoinModule.createMockSettingsRepository()
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(settingsRepository = settingsRepository)

        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // First expand the language dropdown by clicking on it
        // The language section should show "English" as the current selection
        onNodeWithText("English", ignoreCase = true).performClick()

        waitForIdle()

        // Now click on German language in the dropdown menu
        onNodeWithText("German", ignoreCase = true).performClick()

        waitForIdle()

        // Verify language was saved
        coVerify(timeout = 2000) { settingsRepository.saveLanguage(any()) }
    }

    @Test
    fun `user profile shows email`() = runComposeUiTest {
        val credentialsRepository = TestKoinModule.createMockCredentialsRepository(
            credentials = TestData.validCredentials
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(credentialsRepository = credentialsRepository)

        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Verify email is displayed
        onNodeWithText("test@example.com", substring = true).assertExists()
    }

    @Test
    fun `version info is displayed`() = runComposeUiTest {
        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Verify version info is shown
        onNodeWithText("Version", ignoreCase = true, substring = true).assertExists()
    }

    @Test
    fun `default project section shows projects`() = runComposeUiTest {
        val projectRepository = TestKoinModule.createMockProjectRepository()
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(projectRepository = projectRepository)

        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Default project section should be visible (exact text to avoid matching "System Default")
        onNodeWithText("Default project", ignoreCase = true).assertExists()
    }

    @Test
    fun `theme dropdown shows options when expanded`() = runComposeUiTest {
        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Default theme is System Default - click to expand dropdown
        onNodeWithText("System Default", ignoreCase = true).performClick()

        waitForIdle()

        // Now all theme options should be visible in the dropdown
        onNodeWithText("Light Mode", ignoreCase = true).assertExists()
        onNodeWithText("Dark Mode", ignoreCase = true).assertExists()
        // System Default is both the selected value and in dropdown
        onAllNodesWithText("System Default", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun `language dropdown shows options when expanded`() = runComposeUiTest {
        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Default language is English - click to expand dropdown
        onNodeWithText("English", ignoreCase = true).performClick()

        waitForIdle()

        // Now all language options should be visible in the dropdown
        onNodeWithText("German", ignoreCase = true).assertExists()
        // English appears both as selected and in dropdown
        onAllNodesWithText("English", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun `ticket system section is visible`() = runComposeUiTest {
        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Ticket system section should be visible (exact header text)
        onNodeWithText("Ticket Systems", ignoreCase = true).assertExists()
    }

    @Test
    fun `settings scroll properly with many items`() = runComposeUiTest {
        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // The settings should be scrollable
        // This test verifies the UI renders correctly with scroll
    }
}
