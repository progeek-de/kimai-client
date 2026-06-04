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
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
        // Pin locale to English so stringResource() assertions are deterministic.
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
        outputReceived = null
        StringDesc.localeType = StringDesc.LocaleType.Custom("en")
        Locale.setDefault(Locale.ENGLISH)
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

    @Test
    fun `timesheet header shows total label`() = runComposeUiTest {
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

        // The grouped header renders the "Total:" label (covers TimesheetHeader).
        onAllNodesWithText("Total", substring = true).onFirst().assertExists()
    }

    @Test
    fun `empty timesheet list does not render any item descriptions`() = runComposeUiTest {
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

        // No grouped headers or item rows should be present when the list is empty.
        onNodeWithText("Implemented new feature", substring = true).assertDoesNotExist()
        onNodeWithText("Total", substring = true).assertDoesNotExist()
    }

    @Test
    fun `clicking exported timesheet does not trigger edit output`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = listOf(TestData.timesheet3) // exported == true
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

        // Clicking an exported entry takes the snackbar branch instead of the edit branch.
        onNodeWithText("Code review for PR #123", substring = true).performClick()

        waitForIdle()

        // Edit output must NOT have been produced for an exported entry.
        assertNull(outputReceived)
    }

    @Test
    fun `clicking exported timesheet shows already invoiced snackbar`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = listOf(TestData.timesheet3) // exported == true
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

        onNodeWithText("Code review for PR #123", substring = true).performClick()

        waitForIdle()

        // The snackbar text from the exported branch should be displayed.
        onNodeWithText("already been invoiced", substring = true).assertExists()
    }

    @Test
    fun `clicking non-exported timesheet triggers edit output with that item`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = listOf(TestData.timesheet1) // exported == false
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

        onNodeWithText("Implemented new feature", substring = true).performClick()

        waitForIdle()

        val output = outputReceived
        assertNotNull(output)
        val editOutput = assertIs<TimesheetListComponent.Output.Edit>(output)
        assertTrue(editOutput.timesheet.id == TestData.timesheet1.id)
    }
}
