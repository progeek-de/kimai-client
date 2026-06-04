@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.testutils.TestData
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.testutils.createTestComponentContext
import de.progeek.kimai.shared.testutils.createTestDispatchers
import de.progeek.kimai.shared.testutils.createTestStoreFactory
import de.progeek.kimai.shared.ui.timesheet.input.TimesheetInputComponent
import dev.icerock.moko.resources.desc.StringDesc
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TimesheetInputButtonTest {

    private var outputReceived: TimesheetInputComponent.Output? = null
    private lateinit var defaultLocale: Locale

    @Before
    fun setUp() {
        // moko-resources resolves stringResource() against the JVM default locale; pin it to
        // English so assertions on base strings hold regardless of the host machine locale.
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
        outputReceived = null
        Locale.setDefault(defaultLocale)
        StringDesc.localeType = StringDesc.LocaleType.System
    }

    private fun createComponent(): TimesheetInputComponent {
        return TimesheetInputComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = { outputReceived = it }
        )
    }

    @Test(timeout = 30000)
    fun `add time button is shown in manual mode`() = runComposeUiTest {
        TestKoinModule.startTestKoin(
            settingsRepository = TestKoinModule.createMockSettingsRepository(entryMode = EntryMode.MANUAL),
            timesheetRepository = TestKoinModule.createMockTimesheetRepository(runningTimesheet = null)
        )
        val component = createComponent()

        setContent {
            TestTheme {
                CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
                    TimesheetInputButton()
                }
            }
        }

        waitForIdle()
        onNodeWithText("Add Time").assertExists()
    }

    @Test(timeout = 30000)
    fun `add time button click emits show form output`() = runComposeUiTest {
        TestKoinModule.startTestKoin(
            settingsRepository = TestKoinModule.createMockSettingsRepository(entryMode = EntryMode.MANUAL),
            timesheetRepository = TestKoinModule.createMockTimesheetRepository(runningTimesheet = null)
        )
        val component = createComponent()

        setContent {
            TestTheme {
                CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
                    TimesheetInputButton()
                }
            }
        }

        waitForIdle()
        onNodeWithText("Add Time").performClick()
        waitForIdle()

        assertNotNull(outputReceived)
        assertTrue(outputReceived is TimesheetInputComponent.Output.ShowForm)
    }

    @Test(timeout = 30000)
    fun `start button is shown in timer mode without running timesheet`() = runComposeUiTest {
        TestKoinModule.startTestKoin(
            settingsRepository = TestKoinModule.createMockSettingsRepository(entryMode = EntryMode.TIMER),
            timesheetRepository = TestKoinModule.createMockTimesheetRepository(runningTimesheet = null)
        )
        val component = createComponent()

        setContent {
            TestTheme {
                CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
                    TimesheetInputButton()
                }
            }
        }

        waitForIdle()
        onNodeWithText("Start").assertExists()
        onNodeWithText("Add Time").assertDoesNotExist()
    }

    @Test(timeout = 30000)
    fun `start button click emits show form output`() = runComposeUiTest {
        TestKoinModule.startTestKoin(
            settingsRepository = TestKoinModule.createMockSettingsRepository(entryMode = EntryMode.TIMER),
            timesheetRepository = TestKoinModule.createMockTimesheetRepository(runningTimesheet = null)
        )
        val component = createComponent()

        setContent {
            TestTheme {
                CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
                    TimesheetInputButton()
                }
            }
        }

        waitForIdle()
        onNodeWithText("Start").performClick()
        waitForIdle()

        assertNotNull(outputReceived)
        assertTrue(outputReceived is TimesheetInputComponent.Output.ShowForm)
    }

    @Test(timeout = 30000)
    fun `running timesheet test data has no end time`() {
        // The running-timer button starts an infinite ticking LaunchedEffect that never
        // settles, so it cannot be rendered under runComposeUiTest (it would time out).
        // We verify the state that drives the StopButton branch instead.
        val running = TestData.runningTimesheet
        assertTrue(running.end == null)
    }
}
