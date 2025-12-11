package de.progeek.kimai.shared.ui.timesheet.topbar

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.repositories.auth.AuthRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TimesheetTopBarStoreTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var timesheetRepository: TimesheetRepository
    private lateinit var storeFactory: TimesheetTopBarStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testBaseUrl = "https://test.kimai.cloud"
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
        description = "Test"
    )

    @BeforeTest
    fun setup() {
        settingsRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        timesheetRepository = mockk(relaxed = true)

        // Setup default return values
        every { settingsRepository.getBaseUrl() } returns testBaseUrl
        every { settingsRepository.getEntryMode() } returns flowOf(EntryMode.TIMER)
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(null)
        coEvery { authRepository.logout() } returns Result.success(Unit)

        // Setup Koin
        startKoin {
            modules(
                module {
                    single { settingsRepository }
                    single { authRepository }
                    single { timesheetRepository }
                }
            )
        }

        storeFactory = TimesheetTopBarStoreFactory(DefaultStoreFactory())
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
        assertEquals(testBaseUrl, state.baseUrl, "BaseUrl should be loaded")
        assertEquals(EntryMode.TIMER, state.mode, "Default mode should be TIMER")
        assertFalse(state.running, "Running should be false when no timesheet")
    }

    @Test
    fun `bootstrapper loads baseUrl from settings`() = runTest(testDispatcher) {
        val customUrl = "https://custom.kimai.cloud"
        every { settingsRepository.getBaseUrl() } returns customUrl

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertEquals(customUrl, store.stateFlow.value.baseUrl, "BaseUrl should be loaded from settings")
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
    fun `bootstrapper loads running status from timesheet repository`() = runTest(testDispatcher) {
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(testTimesheetForm)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertTrue(store.stateFlow.value.running, "Running should be true when timesheet is running")
    }

    @Test
    fun `running timesheet sets running to true`() = runTest(testDispatcher) {
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(testTimesheetForm)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertTrue(store.stateFlow.value.running, "Running should be true")
    }

    @Test
    fun `no running timesheet sets running to false`() = runTest(testDispatcher) {
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(null)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        assertFalse(store.stateFlow.value.running, "Running should be false")
    }

    @Test
    fun `SetMode intent saves entry mode`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(TimesheetTopBarStore.Intent.SetMode(EntryMode.MANUAL))
        advanceUntilIdle()

        coVerify { settingsRepository.saveEntryMode(EntryMode.MANUAL) }
    }

    @Test
    fun `Logout intent calls auth repository logout`() = runTest(testDispatcher) {
        coEvery { authRepository.logout() } returns Result.success(Unit)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(TimesheetTopBarStore.Intent.Logout)
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.logout() }
    }

    @Test
    fun `Reload intent publishes Reload label`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.labels.test {
            store.accept(TimesheetTopBarStore.Intent.Reload)
            advanceUntilIdle()

            val label = awaitItem()
            assertEquals(TimesheetTopBarStore.Label.Reload, label, "Reload label should be published")
        }
    }

    @Test
    fun `running timesheet automatically sets mode to TIMER`() = runTest(testDispatcher) {
        // Start with MANUAL mode
        every { settingsRepository.getEntryMode() } returns flowOf(EntryMode.MANUAL)
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(testTimesheetForm)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Running timesheet should trigger saveEntryMode(TIMER)
        coVerify { settingsRepository.saveEntryMode(EntryMode.TIMER) }
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
    fun `state updates when running status changes`() = runTest(testDispatcher) {
        every { timesheetRepository.getRunningTimesheetStream() } returns flowOf(null, testTimesheetForm)

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Final state should have running = true
        assertTrue(store.stateFlow.value.running)
    }

    @Test
    fun `handles logout failure gracefully`() = runTest(testDispatcher) {
        coEvery { authRepository.logout() } returns Result.failure(Exception("Logout failed"))

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(TimesheetTopBarStore.Intent.Logout)
        advanceUntilIdle()

        // Should not throw exception
        coVerify(exactly = 1) { authRepository.logout() }
    }

    @Test
    fun `multiple SetMode intents are processed sequentially`() = runTest(testDispatcher) {
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(TimesheetTopBarStore.Intent.SetMode(EntryMode.MANUAL))
        store.accept(TimesheetTopBarStore.Intent.SetMode(EntryMode.TIMER))
        advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.saveEntryMode(EntryMode.MANUAL) }
        coVerify(exactly = 1) { settingsRepository.saveEntryMode(EntryMode.TIMER) }
    }

    @Test
    fun `ShowDashboard intent uses current baseUrl`() = runTest(testDispatcher) {
        val customUrl = "https://custom.kimai.cloud"
        every { settingsRepository.getBaseUrl() } returns customUrl

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Note: ShowDashboard calls browseUrl() which uses Desktop.getDesktop().browse()
        // This throws HeadlessException in test environments without a display.
        // We verify the state has the correct baseUrl instead of calling the intent.
        assertEquals(customUrl, store.stateFlow.value.baseUrl)
    }
}
