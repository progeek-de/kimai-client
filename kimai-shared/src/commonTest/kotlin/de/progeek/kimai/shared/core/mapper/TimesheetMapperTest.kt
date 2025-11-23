package de.progeek.kimai.shared.core.mapper

import de.progeek.kimai.openapi.models.ActivityExpanded
import de.progeek.kimai.openapi.models.Customer
import de.progeek.kimai.openapi.models.ProjectExpanded
import de.progeek.kimai.openapi.models.TimesheetCollection
import de.progeek.kimai.openapi.models.TimesheetCollectionExpanded
import de.progeek.kimai.openapi.models.User
import de.progeek.kimai.shared.TimesheetEntity
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.models.TimesheetForm
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for timesheet mapper functions.
 *
 * Tests the following mapper functions:
 * 1. TimesheetCollection.toTimesheetEntity()
 * 2. de.progeek.kimai.openapi.models.TimesheetEntity.toTimesheetEntity()
 * 3. Timesheet.toTimesheetForm()
 * 4. TimesheetForm.toTimesheet()
 * 5. TimesheetCollectionExpanded.toTimesheetForm()
 */
class TimesheetMapperTest {

    // Test data helpers
    private val testBeginInstant = Instant.parse("2025-01-15T09:00:00Z")
    private val testEndInstant = Instant.parse("2025-01-15T17:00:00Z")
    private val testBeginDateTime = testBeginInstant.toLocalDateTime(TimeZone.UTC)
    private val testEndDateTime = testEndInstant.toLocalDateTime(TimeZone.UTC)
    private val testDuration = 8.hours

    // ============================================================
    // TimesheetCollection.toTimesheetEntity() Tests
    // ============================================================

    @Test
    fun `toTimesheetEntity from TimesheetCollection maps all fields correctly`() {
        // Given
        val collection = TimesheetCollection(
            id = 123,
            begin = "2025-01-15T09:00:00+0000",
            end = "2025-01-15T17:00:00+0000",
            duration = 28800, // 8 hours in seconds
            description = "Working on feature X",
            project = 10,
            activity = 5,
            exported = true,
            billable = true
        )

        // When
        val entity = collection.toTimesheetEntity()

        // Then
        assertEquals(123L, entity.id)
        assertNotNull(entity.begin)
        assertNotNull(entity.end)
        assertEquals(28800L, entity.duration)
        assertEquals("Working on feature X", entity.description)
        assertEquals(10L, entity.project)
        assertEquals(5L, entity.activity)
        assertEquals(1L, entity.exported) // true -> 1
    }

    @Test
    fun `toTimesheetEntity from TimesheetCollection handles null end time`() {
        // Given
        val collection = TimesheetCollection(
            id = 123,
            begin = "2025-01-15T09:00:00+0000",
            end = null,
            duration = null,
            description = "Active timesheet",
            project = 10,
            activity = 5,
            exported = false,
            billable = true
        )

        // When
        val entity = collection.toTimesheetEntity()

        // Then
        assertNull(entity.end)
        assertNull(entity.duration)
        assertEquals(0L, entity.exported) // false -> 0
    }

    @Test
    fun `toTimesheetEntity from TimesheetCollection handles null description`() {
        // Given
        val collection = TimesheetCollection(
            id = 123,
            begin = "2025-01-15T09:00:00+0000",
            end = "2025-01-15T17:00:00+0000",
            duration = 28800,
            description = null,
            project = 10,
            activity = 5,
            exported = false,
            billable = false
        )

        // When
        val entity = collection.toTimesheetEntity()

        // Then
        assertNull(entity.description)
    }

    @Test
    fun `toTimesheetEntity from TimesheetCollection uses -1 for null id`() {
        // Given
        val collection = TimesheetCollection(
            id = null,
            begin = "2025-01-15T09:00:00+0000",
            end = null,
            duration = null,
            description = null,
            project = null,
            activity = null,
            exported = false,
            billable = false
        )

        // When
        val entity = collection.toTimesheetEntity()

        // Then
        assertEquals(-1L, entity.id)
        assertEquals(-1L, entity.project)
        assertEquals(-1L, entity.activity)
    }

    // ============================================================
    // de.progeek.kimai.openapi.models.TimesheetEntity.toTimesheetEntity() Tests
    // ============================================================

    @Test
    fun `toTimesheetEntity from API TimesheetEntity maps all fields correctly`() {
        // Given
        val apiEntity = de.progeek.kimai.openapi.models.TimesheetEntity(
            id = 456,
            begin = "2025-01-15T09:00:00+0000",
            end = "2025-01-15T17:00:00+0000",
            duration = 28800,
            description = "API entity test",
            project = 20,
            activity = 15,
            exported = true,
            billable = true,
            user = 1
        )

        // When
        val entity = apiEntity.toTimesheetEntity()

        // Then
        assertEquals(456L, entity.id)
        assertNotNull(entity.begin)
        assertNotNull(entity.end)
        assertEquals(28800L, entity.duration)
        assertEquals("API entity test", entity.description)
        assertEquals(20L, entity.project)
        assertEquals(15L, entity.activity)
        assertEquals(1L, entity.exported)
    }

    @Test
    fun `toTimesheetEntity from API TimesheetEntity handles null values`() {
        // Given
        val apiEntity = de.progeek.kimai.openapi.models.TimesheetEntity(
            id = null,
            begin = "2025-01-15T09:00:00+0000",
            end = null,
            duration = null,
            description = null,
            project = null,
            activity = null,
            exported = false,
            billable = false,
            user = 1
        )

        // When
        val entity = apiEntity.toTimesheetEntity()

        // Then
        assertEquals(-1L, entity.id)
        assertNull(entity.end)
        assertNull(entity.duration)
        assertNull(entity.description)
        assertEquals(-1L, entity.project)
        assertEquals(-1L, entity.activity)
        assertEquals(0L, entity.exported)
    }

    // ============================================================
    // Timesheet.toTimesheetForm() Tests
    // ============================================================

    @Test
    fun `toTimesheetForm from Timesheet copies all fields correctly`() {
        // Given
        val timesheet = Timesheet(
            id = 100L,
            project = Project(1L, "Project A", "Parent A", true, de.progeek.kimai.shared.core.models.Customer(1L, "Customer A")),
            activity = Activity(2L, "Development", 1L),
            begin = testBeginDateTime,
            end = testEndDateTime,
            duration = testDuration,
            description = "Development work",
            exported = false
        )

        // When
        val form = timesheet.toTimesheetForm()

        // Then
        assertEquals(timesheet.id, form.id)
        assertEquals(timesheet.project, form.project)
        assertEquals(timesheet.activity, form.activity)
        assertEquals(timesheet.begin, form.begin)
        assertEquals(timesheet.end, form.end)
        assertEquals(timesheet.duration, form.duration)
        assertEquals(timesheet.description, form.description)
    }

    @Test
    fun `toTimesheetForm from Timesheet handles null end and duration`() {
        // Given
        val timesheet = Timesheet(
            id = 100L,
            project = Project(1L, "Project A", "", false, null),
            activity = Activity(2L, "Development", 1L),
            begin = testBeginDateTime,
            end = null,
            duration = null,
            description = null,
            exported = false
        )

        // When
        val form = timesheet.toTimesheetForm()

        // Then
        assertNull(form.end)
        assertNull(form.duration)
        assertNull(form.description)
    }

    // ============================================================
    // TimesheetForm.toTimesheet() Tests
    // ============================================================

    @Test
    fun `toTimesheet from TimesheetForm creates valid timesheet`() {
        // Given
        val form = TimesheetForm(
            id = 200L,
            project = Project(1L, "Project B", "Parent B", false, de.progeek.kimai.shared.core.models.Customer(2L, "Customer B")),
            activity = Activity(3L, "Testing", 1L),
            begin = testBeginDateTime,
            end = testEndDateTime,
            duration = testDuration,
            description = "Testing work"
        )

        // When
        val timesheet = form.toTimesheet()

        // Then
        assertEquals(form.id, timesheet.id)
        assertEquals(form.project, timesheet.project)
        assertEquals(form.activity, timesheet.activity)
        assertEquals(form.begin, timesheet.begin)
        assertEquals(form.end, timesheet.end)
        assertEquals(form.duration, timesheet.duration)
        assertEquals(form.description, timesheet.description)
        assertEquals(false, timesheet.exported) // Default value
    }

    @Test
    fun `toTimesheet from TimesheetForm throws error when id is null`() {
        // Given
        val form = TimesheetForm(
            id = null,
            project = Project(1L, "Project", "", false, null),
            activity = Activity(1L, "Activity", 1L),
            begin = testBeginDateTime,
            end = testEndDateTime,
            duration = testDuration,
            description = null
        )

        // When/Then
        val exception = assertFailsWith<Throwable> {
            form.toTimesheet()
        }
        assertEquals("ID, Project and Activity can't be null", exception.message)
    }

    @Test
    fun `toTimesheet from TimesheetForm throws error when project is null`() {
        // Given
        val form = TimesheetForm(
            id = 100L,
            project = null,
            activity = Activity(1L, "Activity", 1L),
            begin = testBeginDateTime,
            end = testEndDateTime,
            duration = testDuration,
            description = null
        )

        // When/Then
        val exception = assertFailsWith<Throwable> {
            form.toTimesheet()
        }
        assertEquals("ID, Project and Activity can't be null", exception.message)
    }

    @Test
    fun `toTimesheet from TimesheetForm throws error when activity is null`() {
        // Given
        val form = TimesheetForm(
            id = 100L,
            project = Project(1L, "Project", "", false, null),
            activity = null,
            begin = testBeginDateTime,
            end = testEndDateTime,
            duration = testDuration,
            description = null
        )

        // When/Then
        val exception = assertFailsWith<Throwable> {
            form.toTimesheet()
        }
        assertEquals("ID, Project and Activity can't be null", exception.message)
    }

    // ============================================================
    // TimesheetCollectionExpanded.toTimesheetForm() Tests
    // ============================================================

    @Test
    fun `toTimesheetForm from TimesheetCollectionExpanded maps all fields correctly`() {
        // Given
        val expanded = TimesheetCollectionExpanded(
            id = 300,
            begin = "2025-01-15T09:00:00+0000",
            end = "2025-01-15T17:00:00+0000",
            duration = 28800, // 8 hours in seconds
            description = "Expanded timesheet work",
            project = ProjectExpanded(
                id = 5,
                name = "Expanded Project",
                customer = Customer(
                    id = 10,
                    name = "Expanded Customer",
                    visible = true,
                    billable = true
                ),
                globalActivities = true,
                visible = true,
                billable = true
            ),
            activity = ActivityExpanded(
                id = 7,
                name = "Expanded Activity",
                project = null,
                visible = true,
                billable = true
            ),
            user = User(username = "testuser", id = 1),
            exported = false,
            billable = true
        )

        // When
        val form = expanded.toTimesheetForm()

        // Then
        assertEquals(300L, form.id)
        assertNotNull(form.project)
        assertEquals("Expanded Project", form.project?.name)
        assertNotNull(form.activity)
        assertEquals("Expanded Activity", form.activity?.name)
        assertNotNull(form.begin)
        assertNotNull(form.end)
        assertNotNull(form.duration)
        assertEquals(28800.seconds, form.duration)
        assertEquals("Expanded timesheet work", form.description)
    }

    @Test
    fun `toTimesheetForm from TimesheetCollectionExpanded handles null values`() {
        // Given
        val expanded = TimesheetCollectionExpanded(
            id = null,
            begin = "2025-01-15T09:00:00+0000",
            end = null,
            duration = null,
            description = null,
            project = ProjectExpanded(
                id = 5,
                name = "Project",
                customer = Customer(
                    id = 10,
                    name = "Customer",
                    visible = true,
                    billable = true
                ),
                globalActivities = false,
                visible = true,
                billable = true
            ),
            activity = ActivityExpanded(
                id = 7,
                name = "Activity",
                project = null,
                visible = true,
                billable = true
            ),
            user = User(username = "testuser", id = 1),
            exported = false,
            billable = false
        )

        // When
        val form = expanded.toTimesheetForm()

        // Then
        assertEquals(-1L, form.id)
        assertNull(form.end)
        assertNull(form.duration)
        assertNull(form.description)
    }
}
