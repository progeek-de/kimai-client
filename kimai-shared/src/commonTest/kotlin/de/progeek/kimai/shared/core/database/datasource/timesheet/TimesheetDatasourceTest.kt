package de.progeek.kimai.shared.core.database.datasource.timesheet

import app.cash.turbine.test
import de.progeek.kimai.shared.TimesheetEntity
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * Test suite for TimesheetDatasource.
 *
 * Tests the following methods:
 * 1. getAll() - Returns Flow<List<Timesheet>>
 * 2. getById(id: Long) - Returns Result<Timesheet?>
 * 3. getActive() - Returns Result<Flow<Timesheet?>>
 * 4. insert(entity: TimesheetEntity) - Returns Result<Timesheet>
 * 5. insert(list: List<TimesheetEntity>) - Returns Result<Unit>
 * 6. update(entity: TimesheetEntity) - Returns Result<Timesheet>
 * 7. delete(id: Long) - Returns Result<Unit>
 * 8. delete(list: List<Long>) - Returns Result<Unit>
 * 9. deleteAll() - Returns Unit
 */
class TimesheetDatasourceTest {

    private lateinit var datasources: de.progeek.kimai.shared.utils.TestDatasources
    private lateinit var timesheetDatasource: TimesheetDatasource

    @BeforeTest
    fun setup() {
        datasources = createTestDatasources()
        timesheetDatasource = datasources.timesheetDatasource

        // Setup required foreign key data (customer, project, activity)
        setupTestData()
    }

    @AfterTest
    fun teardown() {
        clearDatabase(datasources.database)
    }

    private fun setupTestData() = runTest {
        // Insert test customer
        datasources.database.customerEntityQueries.insertCustomer(1L, "Test Customer")

        // Insert test project
        datasources.database.projectEntityQueries.insertProject(
            id = 1L,
            parent = "Parent Project",
            name = "Test Project",
            globalActivities = 1L,
            customer = 1L
        )

        // Insert test activity
        datasources.database.activityEntityQueries.insertActivity(
            id = 1L,
            name = "Test Activity",
            project = 1L
        )
    }

    private fun createTestTimesheetEntity(
        id: Long = 1L,
        begin: Long = Clock.System.now().toEpochMilliseconds(),
        end: Long? = null,
        duration: Long? = null,
        description: String? = "Test timesheet",
        project: Long = 1L,
        activity: Long = 1L,
        exported: Long = 0L
    ) = TimesheetEntity(
        id = id,
        begin = begin,
        end = end,
        duration = duration,
        description = description,
        project = project,
        activity = activity,
        exported = exported
    )

    // ============================================================
    // getAll() Tests
    // ============================================================

    @Test
    fun `getAll returns empty list when no timesheets exist`() = runTest {
        // When
        val result = timesheetDatasource.getAll()

        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns all timesheets from database`() = runTest {
        // Given
        val entity1 = createTestTimesheetEntity(id = 1L, description = "Timesheet 1")
        val entity2 = createTestTimesheetEntity(id = 2L, description = "Timesheet 2")
        timesheetDatasource.insert(entity1)
        timesheetDatasource.insert(entity2)

        // When
        val result = timesheetDatasource.getAll()

        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            // Results are ordered by id DESC
            assertEquals(2L, items[0].id)
            assertEquals(1L, items[1].id)
            assertEquals("Timesheet 2", items[0].description)
            assertEquals("Timesheet 1", items[1].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getById() Tests
    // ============================================================

    @Test
    fun `getById returns timesheet when it exists`() = runTest {
        // Given
        val entity = createTestTimesheetEntity(id = 100L, description = "Find me")
        timesheetDatasource.insert(entity)

        // When
        val result = timesheetDatasource.getById(100L)

        // Then
        assertTrue(result.isSuccess)
        val timesheet = result.getOrNull()
        assertNotNull(timesheet)
        assertEquals(100L, timesheet.id)
        assertEquals("Find me", timesheet.description)
    }

    @Test
    fun `getById returns null when timesheet does not exist`() = runTest {
        // When
        val result = timesheetDatasource.getById(999L)

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ============================================================
    // getActive() Tests
    // ============================================================

    @Test
    fun `getActive returns null when no active timesheet exists`() = runTest {
        // Given - Insert completed timesheet (with end time)
        val entity = createTestTimesheetEntity(
            id = 1L,
            begin = Clock.System.now().toEpochMilliseconds(),
            end = Clock.System.now().toEpochMilliseconds()
        )
        timesheetDatasource.insert(entity)

        // When
        val result = timesheetDatasource.getActive()

        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActive returns active timesheet when it exists`() = runTest {
        // Given - Insert active timesheet (no end time)
        val entity = createTestTimesheetEntity(
            id = 50L,
            begin = Clock.System.now().toEpochMilliseconds(),
            end = null,
            description = "Active timesheet"
        )
        timesheetDatasource.insert(entity)

        // When
        val result = timesheetDatasource.getActive()

        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.test {
            val active = awaitItem()
            assertNotNull(active)
            assertEquals(50L, active.id)
            assertEquals("Active timesheet", active.description)
            assertNull(active.end)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // insert(entity: TimesheetEntity) Tests
    // ============================================================

    @Test
    fun `insert single entity successfully inserts and returns timesheet`() = runTest {
        // Given
        val entity = createTestTimesheetEntity(id = 10L, description = "New timesheet")

        // When
        val result = timesheetDatasource.insert(entity)

        // Then
        assertTrue(result.isSuccess)
        val timesheet = result.getOrNull()
        assertNotNull(timesheet)
        assertEquals(10L, timesheet.id)
        assertEquals("New timesheet", timesheet.description)

        // Verify it was actually inserted
        val getResult = timesheetDatasource.getById(10L)
        assertTrue(getResult.isSuccess)
        assertNotNull(getResult.getOrNull())
    }

    @Test
    fun `insert single entity with all fields populated`() = runTest {
        // Given
        val now = Clock.System.now().toEpochMilliseconds()
        val entity = createTestTimesheetEntity(
            id = 20L,
            begin = now,
            end = now + 3600000, // 1 hour later
            duration = 3600L, // 1 hour in seconds
            description = "Complete timesheet",
            exported = 1L
        )

        // When
        val result = timesheetDatasource.insert(entity)

        // Then
        assertTrue(result.isSuccess)
        val timesheet = result.getOrNull()
        assertNotNull(timesheet)
        assertEquals(20L, timesheet.id)
        assertNotNull(timesheet.end)
        assertNotNull(timesheet.duration)
        assertEquals("Complete timesheet", timesheet.description)
        assertTrue(timesheet.exported)
    }

    // ============================================================
    // insert(list: List<TimesheetEntity>) Tests
    // ============================================================

    @Test
    fun `insert list successfully inserts multiple timesheets`() = runTest {
        // Given
        val entities = listOf(
            createTestTimesheetEntity(id = 30L, description = "Batch 1"),
            createTestTimesheetEntity(id = 31L, description = "Batch 2"),
            createTestTimesheetEntity(id = 32L, description = "Batch 3")
        )

        // When
        val result = timesheetDatasource.insert(entities)

        // Then
        assertTrue(result.isSuccess)

        // Verify all were inserted
        val getResult = timesheetDatasource.getAll()
        assertTrue(getResult.isSuccess)
        getResult.getOrNull()?.test {
            val items = awaitItem()
            assertEquals(3, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert empty list succeeds`() = runTest {
        // Given
        val emptyList = emptyList<TimesheetEntity>()

        // When
        val result = timesheetDatasource.insert(emptyList)

        // Then
        assertTrue(result.isSuccess)
    }

    // ============================================================
    // update(entity: TimesheetEntity) Tests
    // ============================================================

    @Test
    fun `update successfully modifies existing timesheet`() = runTest {
        // Given - Insert initial timesheet
        val original = createTestTimesheetEntity(id = 40L, description = "Original")
        timesheetDatasource.insert(original)

        // When - Update it
        val updated = createTestTimesheetEntity(id = 40L, description = "Updated")
        val result = timesheetDatasource.update(updated)

        // Then
        assertTrue(result.isSuccess)
        val timesheet = result.getOrNull()
        assertNotNull(timesheet)
        assertEquals(40L, timesheet.id)
        assertEquals("Updated", timesheet.description)

        // Verify database reflects the update
        val getResult = timesheetDatasource.getById(40L)
        assertEquals("Updated", getResult.getOrNull()?.description)
    }

    @Test
    fun `update can mark timesheet as completed by setting end time`() = runTest {
        // Given - Insert active timesheet
        val active = createTestTimesheetEntity(id = 50L, end = null, description = "Running")
        timesheetDatasource.insert(active)

        // When - Update with end time
        val completed = createTestTimesheetEntity(
            id = 50L,
            end = Clock.System.now().toEpochMilliseconds(),
            description = "Completed"
        )
        val result = timesheetDatasource.update(completed)

        // Then
        assertTrue(result.isSuccess)
        val timesheet = result.getOrNull()
        assertNotNull(timesheet)
        assertNotNull(timesheet.end)
    }

    // ============================================================
    // delete(id: Long) Tests
    // ============================================================

    @Test
    fun `delete by id removes timesheet from database`() = runTest {
        // Given
        val entity = createTestTimesheetEntity(id = 60L, description = "To be deleted")
        timesheetDatasource.insert(entity)

        // When
        val result = timesheetDatasource.delete(60L)

        // Then
        assertTrue(result.isSuccess)

        // Verify it was deleted
        val getResult = timesheetDatasource.getById(60L)
        assertNull(getResult.getOrNull())
    }

    @Test
    fun `delete by id succeeds even if timesheet does not exist`() = runTest {
        // When
        val result = timesheetDatasource.delete(999L)

        // Then - Should not throw error
        assertTrue(result.isSuccess)
    }

    // ============================================================
    // delete(list: List<Long>) Tests
    // ============================================================

    @Test
    fun `delete list removes multiple timesheets`() = runTest {
        // Given
        val entities = listOf(
            createTestTimesheetEntity(id = 70L),
            createTestTimesheetEntity(id = 71L),
            createTestTimesheetEntity(id = 72L)
        )
        timesheetDatasource.insert(entities)

        // When
        val result = timesheetDatasource.delete(listOf(70L, 71L, 72L))

        // Then
        assertTrue(result.isSuccess)

        // Verify all were deleted
        assertNull(timesheetDatasource.getById(70L).getOrNull())
        assertNull(timesheetDatasource.getById(71L).getOrNull())
        assertNull(timesheetDatasource.getById(72L).getOrNull())
    }

    @Test
    fun `delete empty list succeeds`() = runTest {
        // When
        val result = timesheetDatasource.delete(emptyList())

        // Then
        assertTrue(result.isSuccess)
    }

    // ============================================================
    // deleteAll() Tests
    // ============================================================

    @Test
    fun `deleteAll removes all timesheets from database`() = runTest {
        // Given
        val entities = listOf(
            createTestTimesheetEntity(id = 80L),
            createTestTimesheetEntity(id = 81L),
            createTestTimesheetEntity(id = 82L)
        )
        timesheetDatasource.insert(entities)

        // When
        timesheetDatasource.deleteAll()

        // Then
        val result = timesheetDatasource.getAll()
        assertTrue(result.isSuccess)
        result.getOrNull()?.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteAll on empty database succeeds`() = runTest {
        // When - Delete from empty database
        timesheetDatasource.deleteAll()

        // Then - Should not throw error
        val result = timesheetDatasource.getAll()
        assertTrue(result.isSuccess)
        result.getOrNull()?.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
