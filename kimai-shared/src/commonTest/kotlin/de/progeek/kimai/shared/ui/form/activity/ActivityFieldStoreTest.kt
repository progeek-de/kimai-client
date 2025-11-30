package de.progeek.kimai.shared.ui.form.activity

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.repositories.activity.ActivityRepository
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
 * Test suite for ActivityFieldStore.
 *
 * Tests the following functionality:
 * - Loading activities from repository on bootstrap
 * - Filtering activities based on selected project
 * - Handling global activities (activities with no project)
 * - Selecting an activity and emitting labels
 * - Updating project and re-filtering activities
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ActivityFieldStoreTest {

    private lateinit var activityRepository: ActivityRepository
    private lateinit var storeFactory: ActivityFieldStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testProject1 = Project(
        id = 1,
        name = "Project 1",
        parent = "",
        globalActivities = true,
        customer = null
    )

    private val testProject2 = Project(
        id = 2,
        name = "Project 2",
        parent = "",
        globalActivities = false,
        customer = null
    )

    private val globalActivity = Activity(id = 1, name = "Global Activity", project = null)
    private val project1Activity = Activity(id = 2, name = "Project 1 Activity", project = testProject1.id)
    private val project2Activity = Activity(id = 3, name = "Project 2 Activity", project = testProject2.id)

    private val allActivities = listOf(globalActivity, project1Activity, project2Activity)

    @BeforeTest
    fun setup() {
        activityRepository = mockk(relaxed = true)

        // Setup Koin for ActivityFieldStoreFactory dependency injection
        startKoin {
            modules(
                module {
                    single { activityRepository }
                }
            )
        }

        storeFactory = ActivityFieldStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        clearAllMocks()
    }

    // Helper function to create a test timesheet
    private fun createTestTimesheet(
        project: Project = testProject1,
        activity: Activity = project1Activity
    ) = Timesheet(
        id = 1L,
        begin = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        end = Clock.System.now().plus(2, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault()),
        duration = 2.hours,
        description = "Test",
        project = project,
        activity = activity,
        exported = false
    )

    // ===== Bootstrap and Initial State Tests =====

    @Test
    fun `initial state with no timesheet has null project and no selected activity`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(emptyList())

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertNull(state.project)
        assertNull(state.selectedActivity)
        assertTrue(state.activities.isEmpty())
        assertTrue(state.filteredActivities.isEmpty())
    }

    @Test
    fun `initial state with timesheet loads project and activity from timesheet`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(emptyList())

        val timesheet = createTestTimesheet()
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(testProject1, state.project)
        assertEquals(project1Activity, state.selectedActivity)
    }

    @Test
    fun `bootstrap loads activities from repository`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(allActivities)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(3, state.activities.size)
        assertTrue(state.activities.contains(globalActivity))
        assertTrue(state.activities.contains(project1Activity))
        assertTrue(state.activities.contains(project2Activity))
    }

    // ===== Activity Filtering Tests =====

    @Test
    fun `activities are filtered by project id when project is set`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(allActivities)

        val timesheet = createTestTimesheet(project = testProject1)
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        // Should include project1Activity and globalActivity (since project1 has globalActivities=true)
        assertEquals(2, state.filteredActivities.size)
        assertTrue(state.filteredActivities.contains(project1Activity))
        assertTrue(state.filteredActivities.contains(globalActivity))
    }

    @Test
    fun `global activities are included when project has globalActivities true`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(allActivities)

        val timesheet = createTestTimesheet(project = testProject1) // globalActivities = true
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertTrue(state.filteredActivities.contains(globalActivity))
    }

    @Test
    fun `global activities are excluded when project has globalActivities false`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(allActivities)

        val timesheet = createTestTimesheet(project = testProject2) // globalActivities = false
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        // Should only include project2Activity
        assertEquals(1, state.filteredActivities.size)
        assertTrue(state.filteredActivities.contains(project2Activity))
        assertFalse(state.filteredActivities.contains(globalActivity))
    }

    // ===== Intent: SelectedActivity Tests =====

    @Test
    fun `SelectedActivity intent updates selected activity in state`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(allActivities)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(ActivityFieldStore.Intent.SelectedActivity(project1Activity))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(project1Activity, state.selectedActivity)
    }

    @Test
    fun `SelectedActivity intent emits ActivityChanged label`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(allActivities)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        var labelEmitted = false
        var emittedActivity: Activity? = null
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is ActivityFieldStore.Label.ActivityChanged) {
                    labelEmitted = true
                    emittedActivity = label.activity
                }
            }
        }

        store.accept(ActivityFieldStore.Intent.SelectedActivity(project1Activity))
        advanceUntilIdle()

        assertTrue(labelEmitted, "ActivityChanged label should be emitted")
        assertEquals(project1Activity, emittedActivity)

        job.cancel()
    }

    // ===== Intent: UpdatedProject Tests =====

    @Test
    fun `UpdatedProject intent clears selected activity`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(allActivities)

        val timesheet = createTestTimesheet()
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Verify activity is selected initially
        assertEquals(project1Activity, store.stateFlow.value.selectedActivity)

        // Update project
        store.accept(ActivityFieldStore.Intent.UpdatedProject(testProject2))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertNull(state.selectedActivity, "Selected activity should be cleared when project changes")
    }

    @Test
    fun `UpdatedProject intent re-filters activities for new project`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(allActivities)

        val timesheet = createTestTimesheet(project = testProject1)
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Initial filter should show project1Activity and globalActivity
        assertEquals(2, store.stateFlow.value.filteredActivities.size)

        // Update to project2 (which has globalActivities=false)
        store.accept(ActivityFieldStore.Intent.UpdatedProject(testProject2))
        advanceUntilIdle()

        val state = store.stateFlow.value
        // Should only show project2Activity (no global activities)
        assertEquals(1, state.filteredActivities.size)
        assertTrue(state.filteredActivities.contains(project2Activity))
        assertFalse(state.filteredActivities.contains(globalActivity))
    }

    // ===== Integration Tests =====

    @Test
    fun `full workflow - load activities, update project, select activity`() = runTest(testDispatcher) {
        every { activityRepository.getActivities() } returns flowOf(allActivities)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Step 1: Activities loaded
        assertEquals(3, store.stateFlow.value.activities.size)

        // Step 2: Update project
        store.accept(ActivityFieldStore.Intent.UpdatedProject(testProject1))
        advanceUntilIdle()

        // Step 3: Verify filtered activities
        assertEquals(2, store.stateFlow.value.filteredActivities.size)

        // Step 4: Select an activity
        store.accept(ActivityFieldStore.Intent.SelectedActivity(project1Activity))
        advanceUntilIdle()

        val finalState = store.stateFlow.value
        assertEquals(project1Activity, finalState.selectedActivity)
    }
}
