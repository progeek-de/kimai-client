package de.progeek.kimai.shared.ui.form.time.store

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.ui.form.time.TimeFieldMode
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
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
 * Test suite for TimeFieldStore.
 *
 * Tests the following functionality:
 * - Initial state for different TimeFieldMode (ADD, START, EDIT, EDIT_RUNNING)
 * - BeginChanged intent updates begin time and emits labels
 * - EndChanged intent updates end time and emits labels
 * - Mode-specific behavior (e.g., START mode has null end time)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimeFieldStoreTest {

    private lateinit var storeFactory: TimeFieldStoreFactory
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
        // Setup Koin (TimeFieldStore doesn't inject dependencies, but kept for consistency)
        startKoin {
            modules(
                module {
                    // Empty module - TimeFieldStore is self-contained
                }
            )
        }

        storeFactory = TimeFieldStoreFactory(DefaultStoreFactory())
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
    fun `initial state in ADD mode has correct mode and both times set`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = "Test"),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(TimeFieldMode.ADD, state.mode)
        assertNotNull(state.begin)
        assertNotNull(state.end, "ADD mode should have both begin and end times")
    }

    // ===== START Mode Tests =====

    @Test
    fun `initial state in START mode has correct mode and null end time`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.StartTimesheet(description = "Starting"),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(TimeFieldMode.START, state.mode)
        assertNotNull(state.begin)
        assertNull(state.end, "START mode should have null end time")
    }

    // ===== EDIT Mode Tests =====

    @Test
    fun `initial state in EDIT mode loads times from timesheet`() = runTest(testDispatcher) {
        val timesheet = createTestTimesheet(
            begin = testDateTime,
            end = testEndDateTime
        )

        val store = storeFactory.create(
            params = TimesheetFormParams.EditTimesheet(timesheet = timesheet),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(TimeFieldMode.EDIT, state.mode)
        assertEquals(testDateTime, state.begin)
        assertEquals(testEndDateTime, state.end)
    }

    // ===== EDIT_RUNNING Mode Tests =====

    @Test
    fun `initial state in EDIT_RUNNING mode has null end time`() = runTest(testDispatcher) {
        val runningTimesheet = createTestTimesheet(
            begin = testDateTime,
            end = null
        )

        val store = storeFactory.create(
            params = TimesheetFormParams.EditRunningTimesheet(timesheet = runningTimesheet),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(TimeFieldMode.EDIT_RUNNING, state.mode)
        assertEquals(testDateTime, state.begin)
        assertNull(state.end, "EDIT_RUNNING mode should have null end time")
    }

    // ===== Intent: BeginChanged Tests =====

    @Test
    fun `BeginChanged intent updates begin time in state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val newBegin = testInstant.plus(1, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(TimeFieldStore.Intent.BeginChanged(newBegin))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(newBegin, state.begin)
    }

    @Test
    fun `BeginChanged intent emits BeginChanged label`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        var labelEmitted = false
        var emittedBegin: LocalDateTime? = null
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is TimeFieldStore.Label.BeginChanged) {
                    labelEmitted = true
                    emittedBegin = label.begin
                }
            }
        }

        val newBegin = testInstant.plus(1, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(TimeFieldStore.Intent.BeginChanged(newBegin))
        advanceUntilIdle()

        assertTrue(labelEmitted, "BeginChanged label should be emitted")
        assertEquals(newBegin, emittedBegin)

        job.cancel()
    }

    // ===== Intent: EndChanged Tests =====

    @Test
    fun `EndChanged intent updates end time in state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val newEnd = testInstant.plus(3, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(TimeFieldStore.Intent.EndChanged(newEnd))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(newEnd, state.end)
    }

    @Test
    fun `EndChanged intent emits EndChanged label`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        var labelEmitted = false
        var emittedEnd: LocalDateTime? = null
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is TimeFieldStore.Label.EndChanged) {
                    labelEmitted = true
                    emittedEnd = label.end
                }
            }
        }

        val newEnd = testInstant.plus(3, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(TimeFieldStore.Intent.EndChanged(newEnd))
        advanceUntilIdle()

        assertTrue(labelEmitted, "EndChanged label should be emitted")
        assertEquals(newEnd, emittedEnd)

        job.cancel()
    }

    @Test
    fun `EndChanged intent works in START mode`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.StartTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Initially null in START mode
        assertNull(store.stateFlow.value.end)

        // Set end time
        val newEnd = testInstant.plus(2, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(TimeFieldStore.Intent.EndChanged(newEnd))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(newEnd, state.end, "EndChanged should work even in START mode")
    }

    // ===== Multiple Intent Tests =====

    @Test
    fun `multiple BeginChanged intents update state correctly`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val begin1 = testInstant.plus(1, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(TimeFieldStore.Intent.BeginChanged(begin1))
        advanceUntilIdle()
        assertEquals(begin1, store.stateFlow.value.begin)

        val begin2 = testInstant.plus(2, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(TimeFieldStore.Intent.BeginChanged(begin2))
        advanceUntilIdle()
        assertEquals(begin2, store.stateFlow.value.begin)
    }

    @Test
    fun `multiple EndChanged intents update state correctly`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val end1 = testInstant.plus(2, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(TimeFieldStore.Intent.EndChanged(end1))
        advanceUntilIdle()
        assertEquals(end1, store.stateFlow.value.end)

        val end2 = testInstant.plus(3, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        store.accept(TimeFieldStore.Intent.EndChanged(end2))
        advanceUntilIdle()
        assertEquals(end2, store.stateFlow.value.end)
    }

    @Test
    fun `both BeginChanged and EndChanged intents work together`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            params = TimesheetFormParams.AddTimesheet(description = null),
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val newBegin = testInstant.plus(1, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
        val newEnd = testInstant.plus(4, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())

        store.accept(TimeFieldStore.Intent.BeginChanged(newBegin))
        store.accept(TimeFieldStore.Intent.EndChanged(newEnd))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(newBegin, state.begin)
        assertEquals(newEnd, state.end)
    }
}
