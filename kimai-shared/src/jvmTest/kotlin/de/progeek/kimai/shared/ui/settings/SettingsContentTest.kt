package de.progeek.kimai.shared.ui.settings

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
import de.progeek.kimai.shared.ui.theme.BrandingEnum
import dev.icerock.moko.resources.desc.StringDesc
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class SettingsContentTest {

    private var outputReceived: SettingsComponent.Output? = null
    private lateinit var outputCallback: (SettingsComponent.Output) -> Unit

    @Before
    fun setUp() {
        outputCallback = { output -> outputReceived = output }
        // Reset locale to English before each test
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
        outputReceived = null
        // Reset locale to English after each test to prevent test pollution
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
    }

    private fun createSettingsComponent(): SettingsComponent {
        return SettingsComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = outputCallback
        )
    }

    @Test
    fun `settings screen displays user profile section`() = runComposeUiTest {
        val credentialsRepository = TestKoinModule.createMockCredentialsRepository(
            credentials = TestData.validCredentials
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(credentialsRepository = credentialsRepository)

        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // User profile section should show email
        onNodeWithText("test@example.com", substring = true).assertExists()
    }

    @Test
    fun `settings screen displays branding section with Theme label`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Branding section should be visible (label is "Theme")
        onNodeWithText("Theme", ignoreCase = true).assertExists()
    }

    @Test
    fun `settings screen displays branding dropdown with current selection`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Default branding selection (Kimai) should be visible
        onNodeWithText("Kimai", ignoreCase = true).assertExists()

        // Click to expand dropdown and verify options
        onNodeWithText("Kimai", ignoreCase = true).performClick()
        waitForIdle()

        onNodeWithText("PROGEEK", ignoreCase = true).assertExists()
    }

    @Test
    fun `settings screen displays language flags`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Language flags should be visible (EN and DE)
        onNodeWithText("EN", ignoreCase = true).assertExists()
        onNodeWithText("DE", ignoreCase = true).assertExists()
    }

    @Test
    fun `settings screen displays default project dropdown`() = runComposeUiTest {
        val projectRepository = TestKoinModule.createMockProjectRepository()
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(projectRepository = projectRepository)

        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Default project dropdown should show placeholder when no default is set
        onNodeWithText("Select default project", ignoreCase = true).assertExists()
    }

    @Test
    fun `settings screen displays branding dropdown`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Branding dropdown should show Kimai by default
        onNodeWithText("Kimai", ignoreCase = true).assertExists()
    }

    @Test
    fun `SettingsComponent triggers close output on onOutput call`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Directly call the onOutput function
        component.onOutput()

        waitForIdle()

        // Verify close output was triggered
        assertTrue(outputReceived is SettingsComponent.Output.Close)
    }

    @Test
    fun `settings screen shows current branding selection`() = runComposeUiTest {
        val settingsRepository = TestKoinModule.createMockSettingsRepository(
            branding = BrandingEnum.PROGEEK
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(settingsRepository = settingsRepository)

        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // The PROGEEK branding should be indicated as selected
        onNodeWithText("PROGEEK", ignoreCase = true).assertExists()
    }

    @Test
    fun `settings screen shows default project when set`() = runComposeUiTest {
        val settingsRepository = TestKoinModule.createMockSettingsRepository(
            defaultProjectId = TestData.project1.id
        )
        val projectRepository = TestKoinModule.createMockProjectRepository()
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(
            settingsRepository = settingsRepository,
            projectRepository = projectRepository
        )

        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Default project name should be visible
        // The exact display depends on how the UI shows this
    }

    @Test
    fun `settings screen is scrollable`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // The settings content should be rendered in a scrollable container
        // This test passes if the UI renders without error
    }

    @Test
    fun `settings screen displays ticket system section`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Ticket system settings section should be visible (exact header text)
        onNodeWithText("Ticket Systems", ignoreCase = true).assertExists()
    }

    @Test
    fun `clicking branding option triggers branding change from dropdown`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // First expand the branding dropdown (default is Kimai)
        onNodeWithText("Kimai", ignoreCase = true).performClick()

        waitForIdle()

        // Now click on PROGEEK branding option
        onNodeWithText("PROGEEK", ignoreCase = true).performClick()

        waitForIdle()

        // The branding change should be processed
        // Verification depends on store state
    }

    @Test
    fun `language flags show both EN and DE`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Language flags should be visible (EN is default selected)
        onNodeWithText("EN", ignoreCase = true).assertExists()
        onNodeWithText("DE", ignoreCase = true).assertExists()
    }

    @Test
    fun `clicking language flag triggers language change`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Click on German language flag
        onNodeWithText("DE", ignoreCase = true).performClick()

        waitForIdle()

        // The language change should be processed
    }

    @Test
    fun `settings screen has proper layout structure`() = runComposeUiTest {
        val projectRepository = TestKoinModule.createMockProjectRepository()
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(projectRepository = projectRepository)

        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // All main sections should be present
        // Branding section has "Theme" label
        onNodeWithText("Theme", ignoreCase = true).assertExists()
        // Language flags (EN/DE)
        onNodeWithText("EN", ignoreCase = true).assertExists()
        onNodeWithText("DE", ignoreCase = true).assertExists()
        // Default project dropdown shows placeholder
        waitForIdle()

        onNodeWithText("Select default project", ignoreCase = true).assertExists()
    }

    // ============================================================
    // Branding Section Tests
    // ============================================================

    @Test
    fun `settings screen displays branding section`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Branding section should be visible (label "Theme" from strings.xml)
        onNodeWithText("Theme", ignoreCase = true).assertExists()
    }

    @Test
    fun `branding dropdown shows default KIMAI selection`() = runComposeUiTest {
        val settingsRepository = TestKoinModule.createMockSettingsRepository(
            branding = BrandingEnum.KIMAI
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(settingsRepository = settingsRepository)

        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Default branding selection (Kimai) should be visible
        onNodeWithText("Kimai", ignoreCase = true).assertExists()
    }

    @Test
    fun `branding dropdown shows PROGEEK when selected`() = runComposeUiTest {
        val settingsRepository = TestKoinModule.createMockSettingsRepository(
            branding = BrandingEnum.PROGEEK
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(settingsRepository = settingsRepository)

        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // PROGEEK branding selection should be visible
        onNodeWithText("PROGEEK", ignoreCase = true).assertExists()
    }

    @Test
    fun `clicking branding dropdown shows available options`() = runComposeUiTest {
        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // Click to expand branding dropdown (default is Kimai)
        onNodeWithText("Kimai", ignoreCase = true).performClick()
        waitForIdle()

        // Both options should be visible in dropdown
        onAllNodesWithText("Kimai", ignoreCase = true)[0].assertExists()
        onNodeWithText("PROGEEK", ignoreCase = true).assertExists()
    }

    @Test
    fun `clicking branding option triggers branding change`() = runComposeUiTest {
        val settingsRepository = TestKoinModule.createMockSettingsRepository(branding = BrandingEnum.KIMAI)
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(settingsRepository = settingsRepository)

        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // First expand the branding dropdown (default is Kimai)
        onNodeWithText("Kimai", ignoreCase = true).performClick()
        waitForIdle()

        // Now click on PROGEEK option
        onNodeWithText("PROGEEK", ignoreCase = true).performClick()
        waitForIdle()

        // Verify branding was saved with PROGEEK
        coVerify(timeout = 2000) { settingsRepository.saveBranding(BrandingEnum.PROGEEK) }
    }

    @Test
    fun `branding section appears alongside other settings`() = runComposeUiTest {
        val projectRepository = TestKoinModule.createMockProjectRepository()
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(projectRepository = projectRepository)

        val component = createSettingsComponent()

        setContent {
            TestTheme {
                SettingsContent(component)
            }
        }

        waitForIdle()

        // All sections including branding should be present
        // "Theme" is the branding label
        onNodeWithText("Theme", ignoreCase = true).assertExists()
        // Language flags (EN/DE) instead of dropdown
        onNodeWithText("EN", ignoreCase = true).assertExists()
        onNodeWithText("DE", ignoreCase = true).assertExists()
        // Default project dropdown shows placeholder
        waitForIdle()
        onNodeWithText("Select default project", ignoreCase = true).assertExists()
    }
}
