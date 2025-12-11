package de.progeek.kimai.shared.ui.form.project

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import kotlin.time.Clock
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.*
import kotlin.time.Duration.Companion.hours

/**
 * Test suite for ProjectFieldStore.
 *
 * Tests the following functionality:
 * - Loading projects from repository on bootstrap
 * - Loading default project from settings when no project is selected
 * - Filtering projects based on selected customer
 * - Selecting a project and emitting labels
 * - Updating customer and re-filtering projects
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectFieldStoreTest {

    private lateinit var projectRepository: ProjectRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var storeFactory: ProjectFieldStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCustomer1 = Customer(id = 1, name = "Customer 1")
    private val testCustomer2 = Customer(id = 2, name = "Customer 2")

    private val testProject1 = Project(
        id = 1,
        name = "Project 1",
        parent = "",
        globalActivities = true,
        customer = testCustomer1
    )

    private val testProject2 = Project(
        id = 2,
        name = "Project 2",
        parent = "",
        globalActivities = true,
        customer = testCustomer1
    )

    private val testProject3 = Project(
        id = 3,
        name = "Project 3",
        parent = "",
        globalActivities = false,
        customer = testCustomer2
    )

    private val allProjects = listOf(testProject1, testProject2, testProject3)

    private val testActivity = Activity(id = 1, name = "Development", project = testProject1.id)

    @BeforeTest
    fun setup() {
        projectRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)

        // Setup default behavior - no default project
        every { settingsRepository.getDefaultProject() } returns flowOf(null)

        // Setup Koin for ProjectFieldStoreFactory dependency injection
        startKoin {
            modules(
                module {
                    single { projectRepository }
                    single { settingsRepository }
                }
            )
        }

        storeFactory = ProjectFieldStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        clearAllMocks()
    }

    // Helper function to create a test timesheet
    private fun createTestTimesheet(project: Project = testProject1) = Timesheet(
        id = 1L,
        begin = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        end = Clock.System.now().plus(2, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault()),
        duration = 2.hours,
        description = "Test",
        project = project,
        activity = testActivity,
        exported = false
    )

    // ===== Bootstrap and Initial State Tests =====

    @Test
    fun `initial state with no timesheet has null selected project`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(emptyList())

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertNull(state.selectedProject)
        assertTrue(state.projects.isEmpty())
        assertTrue(state.filteredProjects.isEmpty())
    }

    @Test
    fun `initial state with timesheet loads project from timesheet`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(emptyList())

        val timesheet = createTestTimesheet(project = testProject1)
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(testProject1, state.selectedProject)
    }

    @Test
    fun `bootstrap loads projects from repository`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(allProjects)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(3, state.projects.size)
        assertEquals(3, state.filteredProjects.size)
        assertTrue(state.projects.contains(testProject1))
        assertTrue(state.projects.contains(testProject2))
        assertTrue(state.projects.contains(testProject3))
    }

    @Test
    fun `bootstrap loads default project when no project is selected`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(allProjects)
        every { settingsRepository.getDefaultProject() } returns flowOf(2L) // testProject2

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testProject2, state.selectedProject, "Default project should be loaded from settings")
    }

    @Test
    fun `bootstrap does not load default project when timesheet has project`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(allProjects)
        every { settingsRepository.getDefaultProject() } returns flowOf(2L) // testProject2

        val timesheet = createTestTimesheet(project = testProject1)
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testProject1, state.selectedProject, "Should keep timesheet project, not load default")
    }

    // ===== Intent: SelectedProject Tests =====

    @Test
    fun `SelectedProject intent updates selected project in state`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(allProjects)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(ProjectFieldStore.Intent.SelectedProject(testProject1))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testProject1, state.selectedProject)
    }

    @Test
    fun `SelectedProject intent emits ProjectChanged label`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(allProjects)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        var labelEmitted = false
        var emittedProject: Project? = null
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is ProjectFieldStore.Label.ProjectChanged) {
                    labelEmitted = true
                    emittedProject = label.project
                }
            }
        }

        store.accept(ProjectFieldStore.Intent.SelectedProject(testProject1))
        advanceUntilIdle()

        assertTrue(labelEmitted, "ProjectChanged label should be emitted")
        assertEquals(testProject1, emittedProject)

        job.cancel()
    }

    // ===== Intent: CustomerUpdated Tests =====

    @Test
    fun `CustomerUpdated intent clears selected project`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(allProjects)

        val timesheet = createTestTimesheet(project = testProject1)
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Verify project is selected initially
        assertEquals(testProject1, store.stateFlow.value.selectedProject)

        // Update customer
        store.accept(ProjectFieldStore.Intent.CustomerUpdated(testCustomer2))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertNull(state.selectedProject, "Selected project should be cleared when customer changes")
    }

    @Test
    fun `CustomerUpdated intent filters projects by customer`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(allProjects)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Initially all projects should be shown
        assertEquals(3, store.stateFlow.value.filteredProjects.size)

        // Update to customer1
        store.accept(ProjectFieldStore.Intent.CustomerUpdated(testCustomer1))
        advanceUntilIdle()

        val state = store.stateFlow.value
        // Should only show testProject1 and testProject2 (both belong to customer1)
        assertEquals(2, state.filteredProjects.size)
        assertTrue(state.filteredProjects.contains(testProject1))
        assertTrue(state.filteredProjects.contains(testProject2))
        assertFalse(state.filteredProjects.contains(testProject3))
    }

    @Test
    fun `CustomerUpdated intent filters projects correctly for different customers`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(allProjects)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Update to customer2
        store.accept(ProjectFieldStore.Intent.CustomerUpdated(testCustomer2))
        advanceUntilIdle()

        val state = store.stateFlow.value
        // Should only show testProject3 (belongs to customer2)
        assertEquals(1, state.filteredProjects.size)
        assertTrue(state.filteredProjects.contains(testProject3))
    }

    // ===== Integration Tests =====

    @Test
    fun `full workflow - load projects with default, update customer, select project`() = runTest(testDispatcher) {
        every { projectRepository.getProjects() } returns flowOf(allProjects)
        every { settingsRepository.getDefaultProject() } returns flowOf(1L)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Step 1: Projects loaded and default project selected
        assertEquals(3, store.stateFlow.value.projects.size)
        assertEquals(testProject1, store.stateFlow.value.selectedProject)

        // Step 2: Update customer
        store.accept(ProjectFieldStore.Intent.CustomerUpdated(testCustomer2))
        advanceUntilIdle()

        // Step 3: Verify filtered projects and cleared selection
        assertEquals(1, store.stateFlow.value.filteredProjects.size)
        assertNull(store.stateFlow.value.selectedProject)

        // Step 4: Select a project from filtered list
        store.accept(ProjectFieldStore.Intent.SelectedProject(testProject3))
        advanceUntilIdle()

        val finalState = store.stateFlow.value
        assertEquals(testProject3, finalState.selectedProject)
    }
}
