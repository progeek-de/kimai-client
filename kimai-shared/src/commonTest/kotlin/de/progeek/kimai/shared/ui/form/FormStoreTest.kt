package de.progeek.kimai.shared.ui.form

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.ui.form.time.TimeFieldMode
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.*
import kotlin.time.Duration.Companion.hours

/**
 * Test suite for FormStore.
 *
 * Tests cover 4 different modes:
 * 1. ADD mode - Adding a completed timesheet with begin and end times
 * 2. START mode - Starting a new running timesheet (no end time)
 * 3. EDIT mode - Editing a completed timesheet
 * 4. EDIT_RUNNING mode - Editing a running timesheet
 *
 * Tests the following intents:
 * - ProjectUpdated
 * - ActivityUpdated
 * - CustomerUpdated
 * - DescriptionUpdated
 * - BeginUpdated
 * - EndUpdated
 * - Save (handles all 4 modes differently)
 * - Delete
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FormStoreTest {

    private lateinit var timesheetRepository: TimesheetRepository
    private lateinit var storeFactory: FormStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testInstant = Clock.System.now()
    private val testDateTime = testInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    private val testEndDateTime = testInstant.plus(2, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())

    private val testCustomer = Customer(id = 1, name = "Test Customer")
    private val testProject = Project(
        id = 1,
        name = "Test Project",
        parent = "",
        globalActivities = true,
        customer = testCustomer
    )
    private val testActivity = Activity(id = 1, name = "Development", project = testProject.id)

    @BeforeTest
    fun setup() {
        timesheetRepository = mockk(relaxed = true)

        // Setup Koin for FormStoreFactory dependency injection
        startKoin {
            modules(
                module {
                    single { timesheetRepository }
                }
            )
        }

        storeFactory = FormStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    // Helper function to create a test timesheet
    private fun createTestTimesheet(
        id: Long = 1L,
        begin: LocalDateTime = testDateTime,
        end: LocalDateTime? = testEndDateTime,
        description: String = "Test timesheet"
    ) = Timesheet(
        id = id,
        begin = begin,
        end = end,
        duration = end?.let { 2.hours },
        description = description,
        project = testProject,
        activity = testActivity,
        exported = false
    )

    // ===== ADD Mode Tests =====

    @Test
    fun `initial state in ADD mode has correct mode and times`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = "Test"),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(TimeFieldMode.ADD, state.mode)
        assertNull(state.id)
        assertNotNull(state.begin)
        assertNotNull(state.end)
        assertEquals("Test", state.description)
    }

    @Test
    fun `ProjectUpdated intent updates project in state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(FormStore.Intent.ProjectUpdated(testProject))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testProject, state.project)
    }

    @Test
    fun `ActivityUpdated intent updates activity in state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(FormStore.Intent.ActivityUpdated(testActivity))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testActivity, state.activity)
    }

    @Test
    fun `CustomerUpdated intent updates customer in state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(FormStore.Intent.CustomerUpdated(testCustomer))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testCustomer, state.customer)
    }

    @Test
    fun `DescriptionUpdated intent updates description in state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(FormStore.Intent.DescriptionUpdated("New description"))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals("New description", state.description)
    }

    @Test
    fun `BeginUpdated intent updates begin time in state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val newBegin = testInstant.plus(1, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(FormStore.Intent.BeginUpdated(newBegin))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(newBegin, state.begin)
    }

    @Test
    fun `EndUpdated intent updates end time in state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val newEnd = testInstant.plus(3, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(FormStore.Intent.EndUpdated(newEnd))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(newEnd, state.end)
    }

    @Test
    fun `Save intent in ADD mode calls addTimesheet and emits Close label`() = runTest(testDispatcher) {
        coEvery { timesheetRepository.addTimesheet(any()) } returns Result.success(Unit)

        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = "Test"),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Update state with required fields
        store.accept(FormStore.Intent.ProjectUpdated(testProject))
        store.accept(FormStore.Intent.ActivityUpdated(testActivity))
        advanceUntilIdle()

        // Collect labels
        var labelEmitted = false
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is FormStore.Label.Close) {
                    labelEmitted = true
                }
            }
        }

        store.accept(FormStore.Intent.Save)
        advanceUntilIdle()

        assertTrue(labelEmitted, "Close label should be emitted on successful save")
        coVerify { timesheetRepository.addTimesheet(any()) }

        job.cancel()
    }

    @Test
    fun `Save intent in ADD mode does not emit Close label on failure`() = runTest(testDispatcher) {
        coEvery { timesheetRepository.addTimesheet(any()) } returns Result.failure(Exception("Save failed"))

        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = "Test"),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(FormStore.Intent.ProjectUpdated(testProject))
        store.accept(FormStore.Intent.ActivityUpdated(testActivity))
        advanceUntilIdle()

        var labelEmitted = false
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is FormStore.Label.Close) {
                    labelEmitted = true
                }
            }
        }

        store.accept(FormStore.Intent.Save)
        advanceUntilIdle()

        assertFalse(labelEmitted, "Close label should not be emitted on failure")
        coVerify { timesheetRepository.addTimesheet(any()) }

        job.cancel()
    }

    // ===== START Mode Tests =====

    @Test
    fun `initial state in START mode has correct mode and no end time`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.StartTimesheet(description = "Starting"),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(TimeFieldMode.START, state.mode)
        assertNull(state.id)
        assertNotNull(state.begin)
        assertNull(state.end)
        assertEquals("Starting", state.description)
    }

    @Test
    fun `Save intent in START mode calls createTimesheet and emits Close label`() = runTest(testDispatcher) {
        val expectedForm = TimesheetForm(
            id = null,
            project = testProject,
            activity = testActivity,
            begin = testDateTime,
            end = null,
            description = "Starting"
        )
        coEvery { timesheetRepository.createTimesheet(any()) } returns Result.success(expectedForm)

        val store = storeFactory.create(
            params = TimesheetFormParams.StartTimesheet(description = "Starting"),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(FormStore.Intent.ProjectUpdated(testProject))
        store.accept(FormStore.Intent.ActivityUpdated(testActivity))
        advanceUntilIdle()

        var labelEmitted = false
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is FormStore.Label.Close) {
                    labelEmitted = true
                }
            }
        }

        store.accept(FormStore.Intent.Save)
        advanceUntilIdle()

        assertTrue(labelEmitted, "Close label should be emitted on successful start")
        coVerify { timesheetRepository.createTimesheet(any()) }

        job.cancel()
    }

    // ===== EDIT Mode Tests =====

    @Test
    fun `initial state in EDIT mode loads timesheet data`() = runTest(testDispatcher) {
        val timesheet = createTestTimesheet(
            id = 42L,
            description = "Edit me"
        )

        val store = storeFactory.create(
            params = TimesheetFormParams.EditTimesheet(timesheet = timesheet),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(TimeFieldMode.EDIT, state.mode)
        assertEquals(42L, state.id)
        assertEquals("Edit me", state.description)
        assertEquals(testProject, state.project)
        assertEquals(testActivity, state.activity)
        assertEquals(testCustomer, state.customer)
        assertEquals(testDateTime, state.begin)
        assertEquals(testEndDateTime, state.end)
    }

    @Test
    fun `Save intent in EDIT mode calls updateTimesheet and emits Close label`() = runTest(testDispatcher) {
        val timesheet = createTestTimesheet(id = 42L)
        coEvery { timesheetRepository.updateTimesheet(any()) } returns Result.success(timesheet)

        val store = storeFactory.create(
            params = TimesheetFormParams.EditTimesheet(timesheet = timesheet),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        var labelEmitted = false
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is FormStore.Label.Close) {
                    labelEmitted = true
                }
            }
        }

        store.accept(FormStore.Intent.Save)
        advanceUntilIdle()

        assertTrue(labelEmitted, "Close label should be emitted on successful update")
        coVerify { timesheetRepository.updateTimesheet(any()) }

        job.cancel()
    }

    @Test
    fun `Delete intent in EDIT mode calls deleteTimesheet and emits Close label`() = runTest(testDispatcher) {
        val timesheet = createTestTimesheet(id = 42L)
        coEvery { timesheetRepository.deleteTimesheet(42L) } returns Result.success(Unit)

        val store = storeFactory.create(
            params = TimesheetFormParams.EditTimesheet(timesheet = timesheet),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        var labelEmitted = false
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is FormStore.Label.Close) {
                    labelEmitted = true
                }
            }
        }

        store.accept(FormStore.Intent.Delete)
        advanceUntilIdle()

        assertTrue(labelEmitted, "Close label should be emitted on successful delete")
        coVerify { timesheetRepository.deleteTimesheet(42L) }

        job.cancel()
    }

    @Test
    fun `Delete intent with null id does not call deleteTimesheet`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(FormStore.Intent.Delete)
        advanceUntilIdle()

        coVerify(exactly = 0) { timesheetRepository.deleteTimesheet(any()) }
    }

    // ===== EDIT_RUNNING Mode Tests =====

    @Test
    fun `initial state in EDIT_RUNNING mode loads running timesheet data`() = runTest(testDispatcher) {
        val runningTimesheet = createTestTimesheet(
            id = 99L,
            end = null,
            description = "Running"
        )

        val store = storeFactory.create(
            params = TimesheetFormParams.EditRunningTimesheet(timesheet = runningTimesheet),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(TimeFieldMode.EDIT_RUNNING, state.mode)
        assertEquals(99L, state.id)
        assertEquals("Running", state.description)
        assertNull(state.end)
    }

    @Test
    fun `Save intent in EDIT_RUNNING mode calls updateTimesheet`() = runTest(testDispatcher) {
        val runningTimesheet = createTestTimesheet(id = 99L, end = null)
        coEvery { timesheetRepository.updateTimesheet(any()) } returns Result.success(runningTimesheet)

        val store = storeFactory.create(
            params = TimesheetFormParams.EditRunningTimesheet(timesheet = runningTimesheet),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        var labelEmitted = false
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is FormStore.Label.Close) {
                    labelEmitted = true
                }
            }
        }

        store.accept(FormStore.Intent.Save)
        advanceUntilIdle()

        assertTrue(labelEmitted, "Close label should be emitted on successful update")
        coVerify { timesheetRepository.updateTimesheet(any()) }

        job.cancel()
    }

    // ===== Cross-Mode Integration Tests =====

    @Test
    fun `multiple field updates maintain correct state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = "Initial"),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(FormStore.Intent.ProjectUpdated(testProject))
        store.accept(FormStore.Intent.ActivityUpdated(testActivity))
        store.accept(FormStore.Intent.CustomerUpdated(testCustomer))
        store.accept(FormStore.Intent.DescriptionUpdated("Updated"))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testProject, state.project)
        assertEquals(testActivity, state.activity)
        assertEquals(testCustomer, state.customer)
        assertEquals("Updated", state.description)
    }

    @Test
    fun `toTimesheetForm extension converts state correctly`() = runTest(testDispatcher) {
        val timesheet = createTestTimesheet(id = 123L)
        val store = storeFactory.create(
            params = TimesheetFormParams.EditTimesheet(timesheet = timesheet),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        val form = state.toTimesheetForm()

        assertEquals(123L, form.id)
        assertEquals(testProject, form.project)
        assertEquals(testActivity, form.activity)
        assertEquals(testDateTime, form.begin)
        assertEquals(testEndDateTime, form.end)
        assertEquals("Test timesheet", form.description)
    }
}
