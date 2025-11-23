package de.progeek.kimai.shared.core.database.datasource.project

import app.cash.turbine.test
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for ProjectDatasource.
 *
 * Tests the following methods:
 * 1. getAll() - Returns Flow<List<Project>>
 * 2. getById(id: Int) - Returns Result<Project?>
 * 3. insert(project: Project) - Returns Result<Unit>
 * 4. insert(projects: List<Project>) - Returns Result<List<Project>>
 * 5. deleteAll() - Returns Unit
 */
class ProjectDatasourceTest {

    private lateinit var datasources: de.progeek.kimai.shared.utils.TestDatasources
    private lateinit var projectDatasource: ProjectDatasource

    @BeforeTest
    fun setup() {
        datasources = createTestDatasources()
        projectDatasource = datasources.projectDatasource

        // Setup required foreign key data (customer)
        setupTestData()
    }

    @AfterTest
    fun teardown() {
        clearDatabase(datasources.database)
    }

    private fun setupTestData() = runTest {
        // Insert test customers
        datasources.database.customerEntityQueries.insertCustomer(1L, "Test Customer 1")
        datasources.database.customerEntityQueries.insertCustomer(2L, "Test Customer 2")
    }

    private fun createTestProject(
        id: Long = 1L,
        name: String = "Test Project",
        parent: String = "Parent",
        globalActivities: Boolean = false,
        customerId: Long = 1L,
        customerName: String = "Test Customer 1"
    ) = Project(
        id = id,
        name = name,
        parent = parent,
        globalActivities = globalActivities,
        customer = Customer(id = customerId, name = customerName)
    )

    // ============================================================
    // getAll() Tests
    // ============================================================

    @Test
    fun `getAll returns empty list when no projects exist`() = runTest {
        // When
        val result = projectDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns all projects from database`() = runTest {
        // Given
        val project1 = createTestProject(id = 1L, name = "Project Alpha")
        val project2 = createTestProject(id = 2L, name = "Project Beta", customerId = 2L, customerName = "Test Customer 2")
        projectDatasource.insert(project1)
        projectDatasource.insert(project2)

        // When
        val result = projectDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.any { it.name == "Project Alpha" })
            assertTrue(items.any { it.name == "Project Beta" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll includes projects with and without globalActivities`() = runTest {
        // Given
        val projectWithGlobal = createTestProject(id = 10L, globalActivities = true)
        val projectWithoutGlobal = createTestProject(id = 11L, globalActivities = false)
        projectDatasource.insert(projectWithGlobal)
        projectDatasource.insert(projectWithoutGlobal)

        // When
        val result = projectDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            val global = items.find { it.id == 10L }
            val nonGlobal = items.find { it.id == 11L }
            assertNotNull(global)
            assertNotNull(nonGlobal)
            assertTrue(global.globalActivities)
            assertFalse(nonGlobal.globalActivities)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getById() Tests
    // ============================================================

    @Test
    fun `getById returns project when it exists`() = runTest {
        // Given
        val project = createTestProject(id = 100L, name = "Find Me Project")
        projectDatasource.insert(project)

        // When
        val result = projectDatasource.getById(100)

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals(100L, found.id)
        assertEquals("Find Me Project", found.name)
    }

    @Test
    fun `getById returns null when project does not exist`() = runTest {
        // When
        val result = projectDatasource.getById(999)

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `getById correctly maps all project fields`() = runTest {
        // Given
        val project = createTestProject(
            id = 200L,
            name = "Complete Project",
            parent = "Parent Name",
            globalActivities = true,
            customerId = 2L,
            customerName = "Test Customer 2"
        )
        projectDatasource.insert(project)

        // When
        val result = projectDatasource.getById(200)

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals(200L, found.id)
        assertEquals("Complete Project", found.name)
        assertEquals("Parent Name", found.parent)
        assertTrue(found.globalActivities)
        assertNotNull(found.customer)
        assertEquals(2L, found.customer?.id)
    }

    // ============================================================
    // insert(project: Project) Tests
    // ============================================================

    @Test
    fun `insert single project successfully stores in database`() = runTest {
        // Given
        val project = createTestProject(id = 50L, name = "New Project")

        // When
        val result = projectDatasource.insert(project)

        // Then
        assertTrue(result.isSuccess)

        // Verify it was inserted
        val getResult = projectDatasource.getById(50)
        assertTrue(getResult.isSuccess)
        assertNotNull(getResult.getOrNull())
        assertEquals("New Project", getResult.getOrNull()?.name)
    }

    @Test
    fun `insert replaces existing project with same id`() = runTest {
        // Given
        val original = createTestProject(id = 60L, name = "Original")
        projectDatasource.insert(original)

        // When - Insert with same ID but different name
        val replacement = createTestProject(id = 60L, name = "Replacement")
        val result = projectDatasource.insert(replacement)

        // Then
        assertTrue(result.isSuccess)

        // Verify it was replaced
        val getResult = projectDatasource.getById(60)
        assertEquals("Replacement", getResult.getOrNull()?.name)
    }

    // ============================================================
    // insert(projects: List<Project>) Tests
    // ============================================================

    @Test
    fun `insert list successfully stores multiple projects`() = runTest {
        // Given
        val projects = listOf(
            createTestProject(id = 70L, name = "Batch 1"),
            createTestProject(id = 71L, name = "Batch 2"),
            createTestProject(id = 72L, name = "Batch 3")
        )

        // When
        val result = projectDatasource.insert(projects)

        // Then
        assertTrue(result.isSuccess)
        val returned = result.getOrNull()
        assertNotNull(returned)
        assertEquals(3, returned.size)

        // Verify all were inserted
        projectDatasource.getAll().test {
            val items = awaitItem()
            assertEquals(3, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert empty list succeeds and returns empty list`() = runTest {
        // Given
        val emptyList = emptyList<Project>()

        // When
        val result = projectDatasource.insert(emptyList)

        // Then
        assertTrue(result.isSuccess)
        val returned = result.getOrNull()
        assertNotNull(returned)
        assertTrue(returned.isEmpty())
    }

    @Test
    fun `insert list with null customer succeeds`() = runTest {
        // Given
        val projectWithoutCustomer = Project(
            id = 80L,
            name = "No Customer Project",
            parent = "",
            globalActivities = false,
            customer = null
        )

        // When
        val result = projectDatasource.insert(listOf(projectWithoutCustomer))

        // Then
        assertTrue(result.isSuccess)

        // Verify it was inserted (customer ID defaults to -1)
        val getResult = projectDatasource.getById(80)
        assertTrue(getResult.isSuccess)
        assertNotNull(getResult.getOrNull())
    }

    // ============================================================
    // deleteAll() Tests
    // ============================================================

    @Test
    fun `deleteAll removes all projects from database`() = runTest {
        // Given
        val projects = listOf(
            createTestProject(id = 90L, name = "Delete 1"),
            createTestProject(id = 91L, name = "Delete 2"),
            createTestProject(id = 92L, name = "Delete 3")
        )
        projectDatasource.insert(projects)

        // When
        projectDatasource.deleteAll()

        // Then
        projectDatasource.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteAll on empty database succeeds`() = runTest {
        // When - Delete from empty database
        projectDatasource.deleteAll()

        // Then - Should not throw error
        projectDatasource.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
