@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.core.models.EntryMode
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DescriptionTextFieldTest {

    private lateinit var defaultLocale: Locale

    @Before
    fun setUp() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        TestKoinModule.startTestKoin(
            settingsRepository = TestKoinModule.createMockSettingsRepository(entryMode = EntryMode.TIMER),
            timesheetRepository = TestKoinModule.createMockTimesheetRepository(runningTimesheet = null)
        )
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
        Locale.setDefault(defaultLocale)
        StringDesc.localeType = StringDesc.LocaleType.System
    }

    private fun createComponent(): TimesheetInputComponent {
        return TimesheetInputComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = {}
        )
    }

    @Test(timeout = 30000)
    fun `placeholder is shown when value is empty`() = runComposeUiTest {
        val component = createComponent()
        setContent {
            TestTheme {
                CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
                    DescriptionTextField(
                        value = "",
                        onValueChange = {},
                        isRunning = false,
                        ticketSystemEnabled = false,
                        onKeyEvent = { false },
                        onEditClick = {},
                        onTicketPickerClick = {}
                    )
                }
            }
        }
        waitForIdle()
        onNodeWithText("What are you working on?").assertExists()
    }

    @Test(timeout = 30000)
    fun `typing invokes onValueChange`() = runComposeUiTest {
        val component = createComponent()
        var lastValue: String? = null
        setContent {
            TestTheme {
                var value by remember { mutableStateOf("") }
                CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
                    DescriptionTextField(
                        value = value,
                        onValueChange = {
                            value = it
                            lastValue = it
                        },
                        isRunning = false,
                        ticketSystemEnabled = false,
                        onKeyEvent = { false },
                        onEditClick = {},
                        onTicketPickerClick = {}
                    )
                }
            }
        }
        waitForIdle()
        onNode(hasSetTextAction()).performTextInput("Hello")
        waitForIdle()
        assertEquals("Hello", lastValue)
    }

    @Test(timeout = 30000)
    fun `existing value is displayed`() = runComposeUiTest {
        val component = createComponent()
        setContent {
            TestTheme {
                CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
                    DescriptionTextField(
                        value = "Working on tests",
                        onValueChange = {},
                        isRunning = false,
                        ticketSystemEnabled = false,
                        onKeyEvent = { false },
                        onEditClick = {},
                        onTicketPickerClick = {}
                    )
                }
            }
        }
        waitForIdle()
        onNodeWithText("Working on tests").assertExists()
    }

    @Test(timeout = 30000)
    fun `ticket picker button is shown when ticket system enabled and not running`() = runComposeUiTest {
        val component = createComponent()
        var pickerClicked = false
        setContent {
            TestTheme {
                CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
                    DescriptionTextField(
                        value = "",
                        onValueChange = {},
                        isRunning = false,
                        ticketSystemEnabled = true,
                        onKeyEvent = { false },
                        onEditClick = {},
                        onTicketPickerClick = { pickerClicked = true }
                    )
                }
            }
        }
        waitForIdle()
        onNodeWithContentDescription("Select Issue").assertExists()
        onNodeWithContentDescription("Select Issue").performClick()
        waitForIdle()
        assertTrue(pickerClicked)
    }

    @Test(timeout = 30000)
    fun `ticket picker button is hidden when ticket system disabled`() = runComposeUiTest {
        val component = createComponent()
        setContent {
            TestTheme {
                CompositionLocalProvider(TimesheetInputComponentLocal provides component) {
                    DescriptionTextField(
                        value = "",
                        onValueChange = {},
                        isRunning = false,
                        ticketSystemEnabled = false,
                        onKeyEvent = { false },
                        onEditClick = {},
                        onTicketPickerClick = {}
                    )
                }
            }
        }
        waitForIdle()
        onNodeWithContentDescription("Select Issue").assertDoesNotExist()
    }
}
