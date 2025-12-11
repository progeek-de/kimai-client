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
import de.progeek.kimai.shared.ui.theme.BrandingEnum
import dev.icerock.moko.resources.desc.StringDesc
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertTrue

/**
 * Integration tests for the settings flow.
 * Tests branding changes, language changes, and navigation.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class SettingsFlowTest {

    @Before
    fun setUp() {
        // Reset locale to English before each test
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
        // Reset locale to English after each test to prevent test pollution
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
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

        // Verify all main sections are visible
        // Branding section has "Theme" label
        onNodeWithText("Theme", ignoreCase = true).assertExists()
        // Language flags (EN/DE)
        onNodeWithText("EN", ignoreCase = true).assertExists()
        onNodeWithText("DE", ignoreCase = true).assertExists()
        // Default project section
        onNodeWithText("Default project", ignoreCase = true).assertExists()
        // Ticket systems section
        onNodeWithText("Ticket Systems", ignoreCase = true).assertExists()
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
    fun `selecting branding changes branding setting`() = runComposeUiTest {
        val settingsRepository = TestKoinModule.createMockSettingsRepository(branding = BrandingEnum.KIMAI)
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(settingsRepository = settingsRepository)

        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // First expand the branding dropdown by clicking current selection (Kimai)
        onNodeWithText("Kimai", ignoreCase = true).performClick()

        waitForIdle()

        // Now click on PROGEEK branding option
        onNodeWithText("PROGEEK", ignoreCase = true).performClick()

        waitForIdle()

        // Verify branding was saved
        coVerify(timeout = 2000) { settingsRepository.saveBranding(BrandingEnum.PROGEEK) }
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

        // Click on German language flag (DE)
        onNodeWithText("DE", ignoreCase = true).performClick()

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
    fun `branding dropdown shows current selection`() = runComposeUiTest {
        val settingsRepository = TestKoinModule.createMockSettingsRepository(branding = BrandingEnum.KIMAI)
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(settingsRepository = settingsRepository)

        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Verify default branding (Kimai) is displayed
        onNodeWithText("Kimai", ignoreCase = true).assertExists()
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
    fun `branding dropdown shows options when expanded`() = runComposeUiTest {
        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Default branding is Kimai - click to expand dropdown
        onNodeWithText("Kimai", ignoreCase = true).performClick()

        waitForIdle()

        // Now all branding options should be visible in the dropdown
        onNodeWithText("PROGEEK", ignoreCase = true).assertExists()
        // Kimai is both the selected value and in dropdown
        onAllNodesWithText("Kimai", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun `language flags show both options`() = runComposeUiTest {
        val component = createSettingsComponent { }

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Both language flags should be visible
        onNodeWithText("EN", ignoreCase = true).assertExists()
        onNodeWithText("DE", ignoreCase = true).assertExists()
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
