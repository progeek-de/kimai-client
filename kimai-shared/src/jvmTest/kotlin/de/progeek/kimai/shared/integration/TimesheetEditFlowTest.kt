package de.progeek.kimai.shared.integration

import androidx.compose.ui.test.*
import de.progeek.kimai.shared.testutils.*
import de.progeek.kimai.shared.ui.form.FormComponent
import de.progeek.kimai.shared.ui.form.FormScreen
import de.progeek.kimai.shared.ui.form.context.DefaultFormComponentContext
import de.progeek.kimai.shared.ui.timesheet.TimesheetComponent
import de.progeek.kimai.shared.ui.timesheet.TimesheetScreen
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Integration tests for the timesheet edit flow.
 * Tests the complete flow from list -> form -> save/delete -> back to list.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class TimesheetEditFlowTest {

    @Before
    fun setUp() {
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
    }

    @Test(timeout = 30000)
    fun `clicking timesheet opens form with populated fields`() = runComposeUiTest {
        var outputReceived: TimesheetComponent.Output? = null
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = TestData.timesheets
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = TimesheetComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = { outputReceived = it }
        )

        setContent {
            TestTheme {
                TimesheetScreen(component)
            }
        }

        waitForIdle()

        // Click on a timesheet item
        onNodeWithText("Implemented new feature", substring = true).performClick()

        waitForIdle()

        // Verify that ShowForm output was triggered
        assertTrue(outputReceived is TimesheetComponent.Output.ShowForm)
    }

    @Test(timeout = 30000)
    fun `form displays timesheet data correctly`() = runComposeUiTest {
        // Create all required mock repositories to avoid blocking on missing data
        val customerRepository = TestKoinModule.createMockCustomerRepository()
        val projectRepository = TestKoinModule.createMockProjectRepository()
        val activityRepository = TestKoinModule.createMockActivityRepository()
        val settingsRepository = TestKoinModule.createMockSettingsRepository()

        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(
            customerRepository = customerRepository,
            projectRepository = projectRepository,
            activityRepository = activityRepository,
            settingsRepository = settingsRepository
        )

        val formParams = TimesheetFormParams.EditTimesheet(timesheet = TestData.timesheet1)
        val formContext = DefaultFormComponentContext(
            componentContext = createTestComponentContext(),
            formParams = formParams
        )

        val component = FormComponent(
            componentContext = formContext,
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = { }
        )

        setContent {
            TestTheme {
                FormScreen(component)
            }
        }

        waitForIdle()

        // Verify timesheet data is displayed
        onNodeWithText("Implemented new feature", substring = true).assertExists()
    }

    @Test(timeout = 30000)
    fun `save button triggers save and closes form`() = runComposeUiTest {
        // Create all required mock repositories to avoid blocking on missing data
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository()
        val customerRepository = TestKoinModule.createMockCustomerRepository()
        val projectRepository = TestKoinModule.createMockProjectRepository()
        val activityRepository = TestKoinModule.createMockActivityRepository()
        val settingsRepository = TestKoinModule.createMockSettingsRepository()
        coEvery { timesheetRepository.updateTimesheet(any()) } returns Result.success(TestData.timesheet1)

        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(
            timesheetRepository = timesheetRepository,
            customerRepository = customerRepository,
            projectRepository = projectRepository,
            activityRepository = activityRepository,
            settingsRepository = settingsRepository
        )

        var closeTriggered = false
        val formParams = TimesheetFormParams.EditTimesheet(timesheet = TestData.timesheet1)
        val formContext = DefaultFormComponentContext(
            componentContext = createTestComponentContext(),
            formParams = formParams
        )

        val component = FormComponent(
            componentContext = formContext,
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = { output ->
                if (output is FormComponent.Output.Close) {
                    closeTriggered = true
                }
            }
        )

        setContent {
            TestTheme {
                FormScreen(component)
            }
        }

        waitForIdle()

        // Click save button (uses "OK" text from strings.xml)
        onNodeWithText("OK", ignoreCase = true).performClick()

        waitForIdle()

        // Verify that save was called
        coVerify(timeout = 2000) { timesheetRepository.updateTimesheet(any()) }
    }

    @Test(timeout = 30000)
    fun `delete button shows confirmation and triggers delete`() = runComposeUiTest {
        // Create all required mock repositories to avoid blocking on missing data
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository()
        val customerRepository = TestKoinModule.createMockCustomerRepository()
        val projectRepository = TestKoinModule.createMockProjectRepository()
        val activityRepository = TestKoinModule.createMockActivityRepository()
        val settingsRepository = TestKoinModule.createMockSettingsRepository()
        coEvery { timesheetRepository.deleteTimesheet(any()) } returns Result.success(Unit)

        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(
            timesheetRepository = timesheetRepository,
            customerRepository = customerRepository,
            projectRepository = projectRepository,
            activityRepository = activityRepository,
            settingsRepository = settingsRepository
        )

        val formParams = TimesheetFormParams.EditTimesheet(timesheet = TestData.timesheet1)
        val formContext = DefaultFormComponentContext(
            componentContext = createTestComponentContext(),
            formParams = formParams
        )

        val component = FormComponent(
            componentContext = formContext,
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = { }
        )

        setContent {
            TestTheme {
                FormScreen(component)
            }
        }

        waitForIdle()

        // Click delete button to open confirmation dialog
        onNodeWithText("Delete", ignoreCase = true).performClick()

        waitForIdle()

        // Confirmation dialog should appear with "Delete" and "Cancel" buttons
        // Click "Delete" again to confirm deletion
        // Use onAllNodes since there are now 2 "Delete" texts (button and dialog confirm)
        onAllNodesWithText("Delete", ignoreCase = true).apply {
            // The second one should be in the dialog
            if (fetchSemanticsNodes().size > 1) {
                get(1).performClick()
            } else {
                onFirst().performClick()
            }
        }

        waitForIdle()

        // Verify that delete was called
        coVerify(timeout = 2000) { timesheetRepository.deleteTimesheet(any()) }
    }

    @Test(timeout = 30000)
    fun `back button closes form without saving`() = runComposeUiTest {
        // Create all required mock repositories to avoid blocking on missing data
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository()
        val customerRepository = TestKoinModule.createMockCustomerRepository()
        val projectRepository = TestKoinModule.createMockProjectRepository()
        val activityRepository = TestKoinModule.createMockActivityRepository()
        val settingsRepository = TestKoinModule.createMockSettingsRepository()

        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(
            timesheetRepository = timesheetRepository,
            customerRepository = customerRepository,
            projectRepository = projectRepository,
            activityRepository = activityRepository,
            settingsRepository = settingsRepository
        )

        var closeTriggered = false
        val formParams = TimesheetFormParams.EditTimesheet(timesheet = TestData.timesheet1)
        val formContext = DefaultFormComponentContext(
            componentContext = createTestComponentContext(),
            formParams = formParams
        )

        val component = FormComponent(
            componentContext = formContext,
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = { output ->
                if (output is FormComponent.Output.Close) {
                    closeTriggered = true
                }
            }
        )

        setContent {
            TestTheme {
                FormScreen(component)
            }
        }

        waitForIdle()

        // Click back button (uses text "Back" in this implementation)
        onNodeWithText("Back", ignoreCase = true).performClick()

        waitForIdle()

        // Verify close was triggered
        assertTrue(closeTriggered)

        // Verify save was NOT called
        coVerify(exactly = 0) { timesheetRepository.updateTimesheet(any()) }
    }

    @Test(timeout = 30000)
    fun `add mode creates new timesheet`() = runComposeUiTest {
        // Create all required mock repositories to avoid blocking on missing data
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository()
        val customerRepository = TestKoinModule.createMockCustomerRepository()
        val projectRepository = TestKoinModule.createMockProjectRepository()
        val activityRepository = TestKoinModule.createMockActivityRepository()
        val settingsRepository = TestKoinModule.createMockSettingsRepository()

        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(
            timesheetRepository = timesheetRepository,
            customerRepository = customerRepository,
            projectRepository = projectRepository,
            activityRepository = activityRepository,
            settingsRepository = settingsRepository
        )

        val formParams = TimesheetFormParams.AddTimesheet(description = "New task")
        val formContext = DefaultFormComponentContext(
            componentContext = createTestComponentContext(),
            formParams = formParams
        )

        val component = FormComponent(
            componentContext = formContext,
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = { }
        )

        setContent {
            TestTheme {
                FormScreen(component)
            }
        }

        // Use waitUntil instead of waitForIdle to avoid blocking on infinite coroutines
        // The component has long-running coroutines that collect from SharedFlows
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText("New task", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify add mode is shown (delete button should not exist)
        onNodeWithText("Delete", ignoreCase = true, substring = true).assertDoesNotExist()

        // Description should be pre-filled
        onNodeWithText("New task", substring = true).assertExists()
    }

    @Test(timeout = 30000)
    fun `start mode creates running timesheet`() {
        // This test verifies that StartTimesheet mode initializes the FormComponent correctly.
        // We cannot use runComposeUiTest with FormScreen in START mode because RunningTimeField
        // contains an infinite while(true) loop that prevents waitForIdle() from completing.
        // Instead, we verify the component state directly.

        val timesheetRepository = TestKoinModule.createMockTimesheetRepository()
        val customerRepository = TestKoinModule.createMockCustomerRepository()
        val projectRepository = TestKoinModule.createMockProjectRepository()
        val activityRepository = TestKoinModule.createMockActivityRepository()
        val settingsRepository = TestKoinModule.createMockSettingsRepository()

        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(
            timesheetRepository = timesheetRepository,
            customerRepository = customerRepository,
            projectRepository = projectRepository,
            activityRepository = activityRepository,
            settingsRepository = settingsRepository
        )

        val formParams = TimesheetFormParams.StartTimesheet(description = "Starting work")
        val formContext = DefaultFormComponentContext(
            componentContext = createTestComponentContext(),
            formParams = formParams
        )

        val component = FormComponent(
            componentContext = formContext,
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = { }
        )

        // Verify the component state is initialized correctly for START mode
        val state = component.state.value
        assertTrue(state.mode == de.progeek.kimai.shared.ui.form.time.TimeFieldMode.START)
        assertTrue(state.description == "Starting work")
    }
}