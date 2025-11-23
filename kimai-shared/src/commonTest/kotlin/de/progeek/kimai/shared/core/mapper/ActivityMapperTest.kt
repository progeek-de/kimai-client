package de.progeek.kimai.shared.core.mapper

import de.progeek.kimai.openapi.models.ActivityCollection
import de.progeek.kimai.openapi.models.ActivityExpanded
import de.progeek.kimai.openapi.models.Customer
import de.progeek.kimai.openapi.models.ProjectExpanded
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Test suite for activity mapper functions.
 *
 * Tests the following mapper functions:
 * 1. de.progeek.kimai.shared.ActivityEntity.map() (database entity)
 * 2. ActivityCollection.map()
 * 3. ActivityExpanded.toActivity()
 */
class ActivityMapperTest {

    // ============================================================
    // de.progeek.kimai.shared.ActivityEntity.map() (Database Entity) Tests
    // ============================================================

    @Test
    fun `map from database ActivityEntity maps all fields correctly`() {
        // Given
        val dbEntity = de.progeek.kimai.shared.ActivityEntity(
            id = 100L,
            name = "Development",
            project = 50L
        )

        // When
        val activity = dbEntity.map()

        // Then
        assertEquals(100L, activity.id)
        assertEquals("Development", activity.name)
        assertEquals(50L, activity.project)
    }

    @Test
    fun `map from database ActivityEntity handles null project`() {
        // Given
        val dbEntity = de.progeek.kimai.shared.ActivityEntity(
            id = 200L,
            name = "Global Activity",
            project = null
        )

        // When
        val activity = dbEntity.map()

        // Then
        assertEquals(200L, activity.id)
        assertEquals("Global Activity", activity.name)
        assertNull(activity.project)
    }

    // ============================================================
    // ActivityCollection.map() Tests
    // ============================================================

    @Test
    fun `map from ActivityCollection maps all fields correctly`() {
        // Given
        val collection = ActivityCollection(
            id = 300,
            name = "Testing",
            project = 75,
            visible = true,
            billable = true
        )

        // When
        val activity = collection.map()

        // Then
        assertEquals(300L, activity.id)
        assertEquals("Testing", activity.name)
        assertEquals(75L, activity.project)
    }

    @Test
    fun `map from ActivityCollection handles null id`() {
        // Given
        val collection = ActivityCollection(
            id = null,
            name = "Code Review",
            project = 80,
            visible = true,
            billable = false
        )

        // When
        val activity = collection.map()

        // Then
        assertEquals(-1L, activity.id)
        assertEquals("Code Review", activity.name)
        assertEquals(80L, activity.project)
    }

    @Test
    fun `map from ActivityCollection handles null project`() {
        // Given
        val collection = ActivityCollection(
            id = 400,
            name = "Global Activity",
            project = null,
            visible = true,
            billable = true
        )

        // When
        val activity = collection.map()

        // Then
        assertEquals(400L, activity.id)
        assertEquals("Global Activity", activity.name)
        assertNull(activity.project)
    }

    // ============================================================
    // ActivityExpanded.toActivity() Tests
    // ============================================================

    @Test
    fun `toActivity from ActivityExpanded maps all fields correctly`() {
        // Given
        val expanded = ActivityExpanded(
            id = 500,
            name = "Expanded Activity",
            project = ProjectExpanded(
                id = 90,
                name = "Expanded Project",
                customer = Customer(
                    id = 10,
                    name = "Customer",
                    visible = true,
                    billable = true
                ),
                globalActivities = true,
                visible = true,
                billable = true
            ),
            visible = true,
            billable = true
        )

        // When
        val activity = expanded.toActivity()

        // Then
        assertEquals(500L, activity.id)
        assertEquals("Expanded Activity", activity.name)
        assertEquals(90L, activity.project) // Extracts project ID from ProjectExpanded
    }

    @Test
    fun `toActivity from ActivityExpanded handles null id`() {
        // Given
        val expanded = ActivityExpanded(
            id = null,
            name = "No ID Activity",
            project = ProjectExpanded(
                id = 95,
                name = "Project",
                customer = Customer(
                    id = 15,
                    name = "Customer",
                    visible = true,
                    billable = true
                ),
                globalActivities = false,
                visible = true,
                billable = true
            ),
            visible = false,
            billable = false
        )

        // When
        val activity = expanded.toActivity()

        // Then
        assertEquals(-1L, activity.id)
        assertEquals("No ID Activity", activity.name)
        assertEquals(95L, activity.project)
    }

    @Test
    fun `toActivity from ActivityExpanded handles null project`() {
        // Given
        val expanded = ActivityExpanded(
            id = 600,
            name = "Global Activity",
            project = null,
            visible = true,
            billable = true
        )

        // When
        val activity = expanded.toActivity()

        // Then
        assertEquals(600L, activity.id)
        assertEquals("Global Activity", activity.name)
        assertNull(activity.project)
    }

    @Test
    fun `toActivity from ActivityExpanded handles null project id within ProjectExpanded`() {
        // Given
        val expanded = ActivityExpanded(
            id = 700,
            name = "Activity",
            project = ProjectExpanded(
                id = null,
                name = "Project with No ID",
                customer = Customer(
                    id = 20,
                    name = "Customer",
                    visible = true,
                    billable = true
                ),
                globalActivities = true,
                visible = true,
                billable = true
            ),
            visible = true,
            billable = true
        )

        // When
        val activity = expanded.toActivity()

        // Then
        assertEquals(700L, activity.id)
        assertEquals("Activity", activity.name)
        assertEquals(-1L, activity.project) // ProjectExpanded with null id maps to -1
    }
}
