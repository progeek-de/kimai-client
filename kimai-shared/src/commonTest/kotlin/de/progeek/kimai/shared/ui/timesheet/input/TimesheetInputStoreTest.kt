package de.progeek.kimai.shared.ui.timesheet.input

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class TimesheetInputStoreTest {

    private lateinit var timesheetRepository: TimesheetRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var ticketConfigRepository: TicketConfigRepository
    private lateinit var storeFactory: TimesheetInputStoreFactory
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
    private val testTimesheetForm = TimesheetForm(
        id = 1,
        project = testProject,
        activity = testActivity,
        begin = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        description = "Running timesheet"
    )

    @BeforeTest
    fun setup() {
        timesheetRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)
        ticketConfigRepository = mockk(relaxed = true)

        // Setup default return values
        every { settingsRepository.getEntryMode() } returns flowOf(EntryMode.TIMER)
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(null)
        every { settingsRepository.getDefaultProject() } returns flowOf(null)
        every { projectRepository.getProjects() } returns flowOf(emptyList())
        every { ticketConfigRepository.hasEnabledConfigs() } returns flowOf(false)

        // Setup Koin
        startKoin {
            modules(
                module {
                    single { timesheetRepository }
                    single { settingsRepository }
                    single { projectRepository }
                    single { ticketConfigRepository }
                }
            )
        }

        storeFactory = TimesheetInputStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `initial state has default values`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals("", state.description, "Initial description should be empty")
        assertNull(state.defaultProject, "Default project should be null")
        assertNull(state.runningTimesheet, "Running timesheet should be null")
        assertEquals(EntryMode.TIMER, state.mode, "Default mode should be TIMER")
    }

    @Test
    fun `bootstrapper loads entry mode from settings`() = runTest(testDispatcher) {
        every { settingsRepository.getEntryMode() } returns flowOf(EntryMode.MANUAL)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(EntryMode.MANUAL, store.stateFlow.value.mode, "Mode should be loaded from settings")
    }

    @Test
    fun `bootstrapper loads running timesheet form`() = runTest(testDispatcher) {
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(testTimesheetForm)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertNotNull(store.stateFlow.value.runningTimesheet, "Running timesheet should be loaded")
        assertEquals(testTimesheetForm, store.stateFlow.value.runningTimesheet)
    }

    @Test
    fun `bootstrapper loads default project when set`() = runTest(testDispatcher) {
        every { settingsRepository.getDefaultProject() } returns flowOf(testProject.id.toLong())
        every { projectRepository.getProjects() } returns flowOf(listOf(testProject))

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(testProject, store.stateFlow.value.defaultProject, "Default project should be loaded")
    }

    @Test
    fun `Description intent updates description in state`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val testDescription = "Test description"
        store.accept(TimesheetInputStore.Intent.Description(testDescription))
        advanceUntilIdle()

        assertEquals(testDescription, store.stateFlow.value.description, "Description should be updated")
    }

    @Test
    fun `Add intent publishes AddTimesheet label with description`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val testDescription = "Add test"
        store.accept(TimesheetInputStore.Intent.Description(testDescription))
        advanceUntilIdle()

        store.labels.test {
            store.accept(TimesheetInputStore.Intent.Add)
            advanceUntilIdle()

            val label = awaitItem() as TimesheetInputStore.Label.AddTimesheet
            assertEquals(testDescription, label.description, "Label should contain description")
        }
    }

    @Test
    fun `Add intent resets description after publishing label`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(TimesheetInputStore.Intent.Description("Test"))
        advanceUntilIdle()

        store.accept(TimesheetInputStore.Intent.Add)
        advanceUntilIdle()

        assertEquals("", store.stateFlow.value.description, "Description should be reset after Add")
    }

    @Test
    fun `Start intent publishes StartTimesheet label with description`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val testDescription = "Start test"
        store.accept(TimesheetInputStore.Intent.Description(testDescription))
        advanceUntilIdle()

        store.labels.test {
            store.accept(TimesheetInputStore.Intent.Start)
            advanceUntilIdle()

            val label = awaitItem() as TimesheetInputStore.Label.StartTimesheet
            assertEquals(testDescription, label.description, "Label should contain description")
        }
    }

    @Test
    fun `Start intent resets description after publishing label`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(TimesheetInputStore.Intent.Description("Test"))
        advanceUntilIdle()

        store.accept(TimesheetInputStore.Intent.Start)
        advanceUntilIdle()

        assertEquals("", store.stateFlow.value.description, "Description should be reset after Start")
    }

    @Test
    fun `Stop intent stops running timesheet when project and activity are set`() = runTest(testDispatcher) {
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(testTimesheetForm)
        coEvery { timesheetRepository.stopTimesheet(any()) } returns Result.success(mockk())

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        store.accept(TimesheetInputStore.Intent.Stop)
        advanceUntilIdle()

        coVerify(exactly = 1) { timesheetRepository.stopTimesheet(testTimesheetForm.id!!) }
    }

    @Test
    fun `Stop intent publishes EditTimesheet label when project is null`() = runTest(testDispatcher) {
        val incompleteForm = testTimesheetForm.copy(project = null)
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(incompleteForm)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        store.labels.test {
            store.accept(TimesheetInputStore.Intent.Stop)
            advanceUntilIdle()

            val label = awaitItem() as TimesheetInputStore.Label.EditTimesheet
            assertEquals(incompleteForm, label.form, "Label should contain incomplete form")
        }
    }

    @Test
    fun `Stop intent publishes EditTimesheet label when activity is null`() = runTest(testDispatcher) {
        val incompleteForm = testTimesheetForm.copy(activity = null)
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(incompleteForm)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        store.labels.test {
            store.accept(TimesheetInputStore.Intent.Stop)
            advanceUntilIdle()

            val label = awaitItem() as TimesheetInputStore.Label.EditTimesheet
            assertEquals(incompleteForm, label.form, "Label should contain incomplete form")
        }
    }

    @Test
    fun `Edit intent publishes EditTimesheet label with running form`() = runTest(testDispatcher) {
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(testTimesheetForm)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        store.labels.test {
            store.accept(TimesheetInputStore.Intent.Edit)
            advanceUntilIdle()

            val label = awaitItem() as TimesheetInputStore.Label.EditTimesheet
            assertEquals(testTimesheetForm, label.form, "Label should contain running form")
        }
    }

    @Test
    fun `Edit intent does nothing when no running timesheet`() = runTest(testDispatcher) {
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        store.labels.test {
            store.accept(TimesheetInputStore.Intent.Edit)
            advanceUntilIdle()

            // Should not emit any label
            expectNoEvents()
        }
    }

    @Test
    fun `default project is reset when not found in projects list`() = runTest(testDispatcher) {
        every { settingsRepository.getDefaultProject() } returns flowOf(999L) // Non-existent project ID
        every { projectRepository.getProjects() } returns flowOf(listOf(testProject))

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertNull(store.stateFlow.value.defaultProject, "Default project should be null when not found")
    }

    @Test
    fun `state updates when running timesheet changes`() = runTest(testDispatcher) {
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(null, testTimesheetForm)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Final state should have running timesheet
        assertEquals(testTimesheetForm, store.stateFlow.value.runningTimesheet)
    }

    @Test
    fun `state updates when entry mode changes`() = runTest(testDispatcher) {
        every { settingsRepository.getEntryMode() } returns flowOf(EntryMode.TIMER, EntryMode.MANUAL)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Final state should be MANUAL (last emitted value)
        assertEquals(EntryMode.MANUAL, store.stateFlow.value.mode)
    }

    @Test
    fun `handles null default project gracefully`() = runTest(testDispatcher) {
        every { settingsRepository.getDefaultProject() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertNull(store.stateFlow.value.defaultProject, "Default project should remain null")
    }

    @Test
    fun `multiple Description intents update state sequentially`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(TimesheetInputStore.Intent.Description("First"))
        advanceUntilIdle()
        assertEquals("First", store.stateFlow.value.description)

        store.accept(TimesheetInputStore.Intent.Description("Second"))
        advanceUntilIdle()
        assertEquals("Second", store.stateFlow.value.description)

        store.accept(TimesheetInputStore.Intent.Description("Third"))
        advanceUntilIdle()
        assertEquals("Third", store.stateFlow.value.description)
    }
}
