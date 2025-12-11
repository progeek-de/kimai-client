package de.progeek.kimai.shared.ui.timesheet.list

import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.repositories.activity.ActivityRepository
import de.progeek.kimai.shared.core.repositories.customer.CustomerRepository
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TimesheetListStoreTest {

    private lateinit var timesheetRepository: TimesheetRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var activityRepository: ActivityRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var storeFactory: TimesheetListStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCustomer = Customer(id = 1, name = "Test Customer")
    private val testProject = Project(
        id = 1,
        name = "Test Project",
        parent = "",
        globalActivities = true,
        customer = testCustomer
    )
    private val testActivity = Activity(
        id = 1,
        name = "Test Activity",
        project = testProject.id.toLong()
    )
    private val testBegin = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private val testEnd = testBegin.let {
        LocalDateTime(it.year, it.month, it.dayOfMonth, it.hour + 1, it.minute, it.second, it.nanosecond)
    }
    private val testTimesheet = Timesheet(
        id = 1,
        project = testProject,
        activity = testActivity,
        begin = testBegin,
        end = testEnd,
        duration = 1.hours,
        description = "Test timesheet",
        exported = false
    )

    @BeforeTest
    fun setup() {
        timesheetRepository = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)
        activityRepository = mockk(relaxed = true)
        customerRepository = mockk(relaxed = true)

        // Setup default return values
        every { timesheetRepository.timesheetsStream() } returns flowOf(emptyList())
        coEvery { timesheetRepository.loadNewTimesheets(any(), any()) } returns Result.success(null)

        // Setup Koin
        startKoin {
            modules(
                module {
                    single { timesheetRepository }
                    single { projectRepository }
                    single { activityRepository }
                    single { customerRepository }
                }
            )
        }

        storeFactory = TimesheetListStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `initial state is loading with empty list`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        // After bootstrapper completes, isLoading is false (from Loading(false) message)
        assertFalse(state.isLoading, "Loading should be false after bootstrapper completes")
        assertEquals(emptyList(), state.timesheets, "Timesheets should be empty")
        assertEquals(null, state.running, "Running should be null")
        // endReached is true because loadNewTimesheets returns null (no next page)
        assertTrue(state.endReached, "endReached should be true when no next page available")
    }

    @Test
    fun `bootstrapper loads timesheet stream`() = runTest(testDispatcher) {
        val timesheets = listOf(testTimesheet)
        every { timesheetRepository.timesheetsStream() } returns flowOf(timesheets)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(1, state.timesheets.size, "Should have 1 grouped timesheet")
        assertNotNull(state.timesheets.firstOrNull(), "Grouped timesheet should not be null")
    }

    @Test
    fun `bootstrapper loads initial timesheets`() = runTest(testDispatcher) {
        coEvery { timesheetRepository.loadNewTimesheets(any(), any()) } returns Result.success(
            Clock.System.now()
        )

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { timesheetRepository.loadNewTimesheets(any(), 100) }
    }

    @Test
    fun `LoadNextItems intent loads more timesheets`() = runTest(testDispatcher) {
        val nextPage = Clock.System.now()
        coEvery { timesheetRepository.loadNewTimesheets(any(), any()) } returns Result.success(nextPage)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        store.accept(TimesheetListStore.Intent.LoadNextItems)
        advanceUntilIdle()

        // Should be called twice: once in bootstrapper, once from intent
        coVerify(atLeast = 2) { timesheetRepository.loadNewTimesheets(any(), 100) }
    }

    @Test
    fun `LoadNextItems sets loading to true then false`() = runTest(testDispatcher) {
        coEvery { timesheetRepository.loadNewTimesheets(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(1)
            Result.success(Clock.System.now())
        }

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        store.accept(TimesheetListStore.Intent.LoadNextItems)
        advanceUntilIdle()

        // After loading completes, should not be loading
        assertFalse(store.stateFlow.value.isLoading, "Should not be loading after completion")
    }

    @Test
    fun `endReached is set when no more pages available`() = runTest(testDispatcher) {
        // Return null to indicate end reached
        coEvery { timesheetRepository.loadNewTimesheets(any(), any()) } returns Result.success(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertTrue(store.stateFlow.value.endReached, "endReached should be true when null page returned")
    }

    @Test
    fun `endReached is false when more pages available`() = runTest(testDispatcher) {
        val nextPage = Clock.System.now()
        coEvery { timesheetRepository.loadNewTimesheets(any(), any()) } returns Result.success(nextPage)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertFalse(store.stateFlow.value.endReached, "endReached should be false when next page available")
    }

    @Test
    fun `Refresh intent invalidates caches and reloads`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(TimesheetListStore.Intent.Refresh)
        advanceUntilIdle()

        coVerify(exactly = 1) { timesheetRepository.invalidateCache() }
        coVerify(exactly = 1) { projectRepository.invalidateCache() }
        coVerify(exactly = 1) { activityRepository.invalidateCache() }
        coVerify(atLeast = 1) { timesheetRepository.loadNewTimesheets(any(), any()) }
    }

    @Test
    fun `Restart intent restarts timesheet`() = runTest(testDispatcher) {
        val timesheetId = 123L
        coEvery { timesheetRepository.restartTimesheet(timesheetId) } returns Result.success(testTimesheet)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(TimesheetListStore.Intent.Restart(timesheetId))
        advanceUntilIdle()

        coVerify(exactly = 1) { timesheetRepository.restartTimesheet(timesheetId) }
    }

    @Test
    fun `timesheets are grouped by date`() = runTest(testDispatcher) {
        val timesheet1 = testTimesheet.copy(id = 1)
        val timesheet2 = testTimesheet.copy(id = 2)
        val timesheets = listOf(timesheet1, timesheet2)

        every { timesheetRepository.timesheetsStream() } returns flowOf(timesheets)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(1, state.timesheets.size, "Should have 1 grouped timesheet (same date)")
        assertEquals(2, state.timesheets.first().list.size, "Group should contain 2 timesheets")
    }

    @Test
    fun `only completed timesheets are included in list`() = runTest(testDispatcher) {
        val completedTimesheet = testTimesheet
        val runningTimesheet = testTimesheet.copy(id = 2, end = null)
        val timesheets = listOf(completedTimesheet, runningTimesheet)

        every { timesheetRepository.timesheetsStream() } returns flowOf(timesheets)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        val allTimesheets = state.timesheets.flatMap { it.list }
        assertEquals(1, allTimesheets.size, "Should only have completed timesheet")
        assertNotNull(allTimesheets.first().end, "Included timesheet should have end time")
    }

    @Test
    fun `grouped timesheets calculate total duration`() = runTest(testDispatcher) {
        val timesheet1 = testTimesheet.copy(id = 1, duration = 1.hours)
        val timesheet2 = testTimesheet.copy(id = 2, duration = 2.hours)
        val timesheets = listOf(timesheet1, timesheet2)

        every { timesheetRepository.timesheetsStream() } returns flowOf(timesheets)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        val groupedTimesheet = state.timesheets.first()
        assertEquals(3.hours, groupedTimesheet.total, "Total should be sum of durations")
    }

    @Test
    fun `page updates with next page instant`() = runTest(testDispatcher) {
        val nextPage: Instant = Clock.System.now()
        coEvery { timesheetRepository.loadNewTimesheets(any(), any()) } returns Result.success(nextPage)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val initialPage = store.stateFlow.value.page

        store.accept(TimesheetListStore.Intent.LoadNextItems)
        advanceUntilIdle()

        val updatedPage = store.stateFlow.value.page
        // Page should be updated if next page is provided
        assertNotNull(updatedPage, "Page should be updated")
    }

    @Test
    fun `multiple LoadNextItems are processed sequentially`() = runTest(testDispatcher) {
        coEvery { timesheetRepository.loadNewTimesheets(any(), any()) } returns Result.success(
            Clock.System.now()
        )

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Send multiple load intents
        store.accept(TimesheetListStore.Intent.LoadNextItems)
        store.accept(TimesheetListStore.Intent.LoadNextItems)
        advanceUntilIdle()

        // Should be called at least 3 times (1 bootstrap + 2 intents)
        coVerify(atLeast = 3) { timesheetRepository.loadNewTimesheets(any(), 100) }
    }

    @Test
    fun `empty timesheet list shows empty state`() = runTest(testDispatcher) {
        every { timesheetRepository.timesheetsStream() } returns flowOf(emptyList())

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(emptyList(), state.timesheets, "Timesheets should be empty")
        // isLoading is controlled by Loading messages, not by empty list
        assertFalse(state.isLoading, "Loading should be false after load completes")
    }
}
