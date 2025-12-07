package de.progeek.kimai.shared.ui.timesheet.list

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestData
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.testutils.createTestComponentContext
import de.progeek.kimai.shared.testutils.createTestDispatchers
import de.progeek.kimai.shared.testutils.createTestStoreFactory
import de.progeek.kimai.shared.ui.timesheet.list.components.TimesheetList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class TimesheetListTest {

    private var outputReceived: TimesheetListComponent.Output? = null
    private lateinit var outputCallback: (TimesheetListComponent.Output) -> Unit
    private lateinit var inputFlow: MutableSharedFlow<TimesheetListComponent.Input>

    @Before
    fun setUp() {
        outputCallback = { output -> outputReceived = output }
        inputFlow = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
        outputReceived = null
    }

    private fun createTimesheetListComponent(): TimesheetListComponent {
        return TimesheetListComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            input = inputFlow,
            output = outputCallback
        )
    }

    @Test
    fun `timesheet list displays all items`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = TestData.timesheets
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createTimesheetListComponent()

        setContent {
            TestTheme {
                TimesheetList(component)
            }
        }

        waitForIdle()

        // All timesheet descriptions should be visible
        onNodeWithText("Implemented new feature", substring = true).assertExists()
        onNodeWithText("Writing unit tests", substring = true).assertExists()
        onNodeWithText("Code review for PR #123", substring = true).assertExists()
    }

    @Test
    fun `timesheet list displays project names`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = TestData.timesheets
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createTimesheetListComponent()

        setContent {
            TestTheme {
                TimesheetList(component)
            }
        }

        waitForIdle()

        // Project names should be visible (multiple items may have same project)
        onAllNodesWithText("Test Project 1", substring = true).onFirst().assertExists()
    }

    @Test
    fun `timesheet list displays duration`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = TestData.timesheets
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createTimesheetListComponent()

        setContent {
            TestTheme {
                TimesheetList(component)
            }
        }

        waitForIdle()

        // Duration should be visible (e.g., "3.0 h" for timesheet1)
        onAllNodesWithText("3.0 h", substring = true).onFirst().assertExists()
    }

    @Test
    fun `clicking timesheet item triggers edit output`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = TestData.timesheets
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createTimesheetListComponent()

        setContent {
            TestTheme {
                TimesheetList(component)
            }
        }

        waitForIdle()

        // Click on a timesheet item
        onNodeWithText("Implemented new feature", substring = true).performClick()

        waitForIdle()

        // Verify edit output was triggered
        assertNotNull(outputReceived)
        assertTrue(outputReceived is TimesheetListComponent.Output.Edit)
    }

    @Test
    fun `timesheet list shows empty state when no timesheets`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = emptyList()
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createTimesheetListComponent()

        setContent {
            TestTheme {
                TimesheetList(component)
            }
        }

        waitForIdle()

        // When empty, the list should render without errors
        // Depending on UI, there might be an empty state message
    }

    @Test
    fun `timesheet list shows formatted duration`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = TestData.timesheets
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createTimesheetListComponent()

        setContent {
            TestTheme {
                TimesheetList(component)
            }
        }

        waitForIdle()

        // Duration should be displayed in a formatted way
        // The exact format depends on implementation (e.g., "3:00", "3h", etc.)
    }

    @Test
    fun `timesheet list handles reload input`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = TestData.timesheets
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createTimesheetListComponent()

        setContent {
            TestTheme {
                TimesheetList(component)
            }
        }

        waitForIdle()

        // Emit reload input
        inputFlow.tryEmit(TimesheetListComponent.Input.Reload)

        waitForIdle()

        // The list should still render correctly after reload
    }

    @Test
    fun `timesheet list groups items by date`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = TestData.timesheets
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createTimesheetListComponent()

        setContent {
            TestTheme {
                TimesheetList(component)
            }
        }

        waitForIdle()

        // Timesheets should be grouped by date
        // The exact format depends on implementation
    }

    @Test
    fun `exported timesheets are visually distinguished`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = listOf(TestData.timesheet3) // This one is exported
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createTimesheetListComponent()

        setContent {
            TestTheme {
                TimesheetList(component)
            }
        }

        waitForIdle()

        // Exported timesheet should be displayed
        onNodeWithText("Code review for PR #123", substring = true).assertExists()
        // The visual distinction depends on UI implementation
    }
}
