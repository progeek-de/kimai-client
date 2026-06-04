@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.form.time.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestData
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.testutils.createTestComponentContext
import de.progeek.kimai.shared.testutils.createTestDispatchers
import de.progeek.kimai.shared.testutils.createTestStoreFactory
import de.progeek.kimai.shared.ui.form.context.DefaultFormComponentContext
import de.progeek.kimai.shared.ui.form.time.TimeFieldComponent
import de.progeek.kimai.shared.ui.form.time.store.TimeFieldStore
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class TimeFieldComponentsTest {

    @Before
    fun setUp() {
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
    }

    private fun createComponent(
        formParams: TimesheetFormParams,
        output: (TimeFieldComponent.Output) -> Unit = {}
    ): TimeFieldComponent {
        val context = DefaultFormComponentContext(
            componentContext = createTestComponentContext(),
            formParams = formParams
        )
        return TimeFieldComponent(
            componentContext = context,
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = output
        )
    }

    // region EditTimeField (covers BeginTimeField, EndTimeField, CalendarButton)

    @Test
    fun `edit time field renders begin end and duration`() = runComposeUiTest {
        val component = createComponent(TimesheetFormParams.EditTimesheet(TestData.timesheet1))

        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                EditTimeField(component, snackbar)
            }
        }

        waitForIdle()

        onNode(hasSetTextAction() and hasText("09:00")).assertExists()
        onNode(hasSetTextAction() and hasText("12:00")).assertExists()
        onNodeWithText("03:00:00").assertExists()
    }

    @Test
    fun `calendar button opens date picker in edit mode`() = runComposeUiTest {
        val component = createComponent(TimesheetFormParams.EditTimesheet(TestData.timesheet1))

        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                EditTimeField(component, snackbar)
            }
        }

        waitForIdle()

        onNode(hasContentDescription("Refresh")).performClick()
        waitForIdle()

        onNodeWithText("OK", ignoreCase = true).assertExists()
    }

    @Test
    fun `confirming date picker emits begin and end changes`() = runComposeUiTest {
        var begin: LocalDateTime? = null
        var end: LocalDateTime? = null
        val component = createComponent(TimesheetFormParams.EditTimesheet(TestData.timesheet1)) { output ->
            when (output) {
                is TimeFieldComponent.Output.BeginChanged -> begin = output.begin
                is TimeFieldComponent.Output.EndChanged -> end = output.end
            }
        }

        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                EditTimeField(component, snackbar)
            }
        }

        waitForIdle()

        onNode(hasContentDescription("Refresh")).performClick()
        waitForIdle()
        onNodeWithText("OK", ignoreCase = true).performClick()
        waitForIdle()

        // The picker confirm keeps the original times while pushing changes through the store.
        assertEquals(TestData.timesheet1.begin.time, begin?.time)
        assertEquals(TestData.timesheet1.end?.time, end?.time)
    }

    @Test
    fun `add mode renders with now defaults`() = runComposeUiTest {
        val component = createComponent(TimesheetFormParams.AddTimesheet(description = null))

        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                EditTimeField(component, snackbar)
            }
        }

        waitForIdle()

        // Two editable time fields and the separator should be present.
        onNodeWithText("-").assertExists()
    }

    // endregion

    // region store interaction

    @Test
    fun `begin changed intent updates state`() {
        val component = createComponent(TimesheetFormParams.EditTimesheet(TestData.timesheet1))

        val newBegin = LocalDateTime(2025, 1, 1, 10, 0)
        component.onIntent(TimeFieldStore.Intent.BeginChanged(newBegin))

        // The reducer updates state synchronously; the Output emission is exercised by the
        // date-picker confirm test above.
        assertEquals(newBegin, component.state.value.begin)
    }

    @Test
    fun `end changed intent updates state`() {
        val component = createComponent(TimesheetFormParams.EditTimesheet(TestData.timesheet1))

        val newEnd = LocalDateTime(2025, 1, 1, 13, 0)
        component.onIntent(TimeFieldStore.Intent.EndChanged(newEnd))

        assertEquals(newEnd, component.state.value.end)
    }

    // endregion

    // region RunningTimeField

    @Test
    fun `running time field renders start and today labels`() = runComposeUiTest {
        // The running field starts an endless one-second timer loop. Freezing the test
        // clock keeps the loop suspended so the first composition can be asserted.
        mainClock.autoAdvance = false

        val component = createComponent(TimesheetFormParams.EditRunningTimesheet(TestData.runningTimesheet))

        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                RunningTimeField(component, snackbar)
            }
        }

        // The begin time (08:00) is editable; "Start:" and "Today" labels are shown.
        onNode(hasSetTextAction() and hasText("08:00")).assertExists()
        onNodeWithText("Start:").assertExists()
        onNodeWithText("Today").assertExists()
    }

    @Test
    fun `running time field initialises from start mode`() = runComposeUiTest {
        mainClock.autoAdvance = false

        val component = createComponent(TimesheetFormParams.StartTimesheet(description = "Timer"))

        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                RunningTimeField(component, snackbar)
            }
        }

        onNodeWithText("Start:").assertExists()
        onNodeWithText("Today").assertExists()
    }

    // endregion
}
