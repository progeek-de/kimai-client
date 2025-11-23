package de.progeek.kimai.shared.core.mapper

import de.progeek.kimai.openapi.models.Customer
import de.progeek.kimai.openapi.models.ProjectCollection
import de.progeek.kimai.openapi.models.ProjectEntity
import de.progeek.kimai.openapi.models.ProjectExpanded
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Test suite for project mapper functions.
 *
 * Tests the following mapper functions:
 * 1. ProjectCollection.map()
 * 2. ProjectEntity.map()
 * 3. de.progeek.kimai.shared.ProjectEntity.map() (database entity)
 * 4. toProject() (factory function)
 * 5. ProjectExpanded.toProject()
 */
class ProjectMapperTest {

    // ============================================================
    // ProjectCollection.map() Tests
    // ============================================================

    @Test
    fun `map from ProjectCollection maps all fields correctly`() {
        // Given
        val collection = ProjectCollection(
            id = 100,
            name = "Project Alpha",
            parentTitle = "Parent Project",
            globalActivities = true,
            customer = 50,
            visible = true,
            billable = true
        )

        // When
        val project = collection.map()

        // Then
        assertEquals(100L, project.id)
        assertEquals("Project Alpha", project.name)
        assertEquals("Parent Project", project.parent)
        assertEquals(true, project.globalActivities)
        assertNotNull(project.customer)
        assertEquals(50L, project.customer?.id)
        assertEquals("Parent Project", project.customer?.name) // Uses parentTitle for customer name
    }

    @Test
    fun `map from ProjectCollection handles null id`() {
        // Given
        val collection = ProjectCollection(
            id = null,
            name = "Project Beta",
            parentTitle = "Parent",
            globalActivities = false,
            customer = null,
            visible = false,
            billable = false
        )

        // When
        val project = collection.map()

        // Then
        assertEquals(-1L, project.id)
        assertEquals("Project Beta", project.name)
    }

    @Test
    fun `map from ProjectCollection handles null parentTitle`() {
        // Given
        val collection = ProjectCollection(
            id = 100,
            name = "Project Gamma",
            parentTitle = null,
            globalActivities = true,
            customer = 30,
            visible = true,
            billable = true
        )

        // When
        val project = collection.map()

        // Then
        assertEquals("", project.parent)
        assertEquals("", project.customer?.name)
    }

    // ============================================================
    // ProjectEntity.map() (API Entity) Tests
    // ============================================================

    @Test
    fun `map from API ProjectEntity maps all fields correctly`() {
        // Given
        val entity = ProjectEntity(
            id = 200,
            name = "Entity Project",
            parentTitle = "Entity Parent",
            globalActivities = false,
            customer = 75,
            visible = true,
            billable = true,
            budget = 10000f,
            timeBudget = 100
        )

        // When
        val project = entity.map()

        // Then
        assertEquals(200L, project.id)
        assertEquals("Entity Project", project.name)
        assertEquals("Entity Parent", project.parent)
        assertEquals(false, project.globalActivities)
        assertNotNull(project.customer)
        assertEquals(75L, project.customer?.id)
    }

    @Test
    fun `map from API ProjectEntity handles null values`() {
        // Given
        val entity = ProjectEntity(
            id = null,
            name = "Null Project",
            parentTitle = null,
            globalActivities = true,
            customer = null,
            visible = false,
            billable = false,
            budget = 0f,
            timeBudget = 0
        )

        // When
        val project = entity.map()

        // Then
        assertEquals(-1L, project.id)
        assertEquals("", project.parent)
        assertEquals(-1L, project.customer?.id)
    }

    // ============================================================
    // de.progeek.kimai.shared.ProjectEntity.map() (Database Entity) Tests
    // ============================================================

    @Test
    fun `map from database ProjectEntity maps all fields correctly`() {
        // Given
        val dbEntity = de.progeek.kimai.shared.ProjectEntity(
            id = 300L,
            name = "DB Project",
            parent = "DB Parent",
            globalActivities = 1L, // 1 = true
            customer = 90L
        )

        // When
        val project = dbEntity.map()

        // Then
        assertEquals(300L, project.id)
        assertEquals("DB Project", project.name)
        assertEquals("DB Parent", project.parent)
        assertEquals(true, project.globalActivities) // 1 converted to true
        assertNotNull(project.customer)
        assertEquals(90L, project.customer?.id)
        assertEquals("DB Parent", project.customer?.name) // Uses parent for customer name
    }

    @Test
    fun `map from database ProjectEntity handles globalActivities as false`() {
        // Given
        val dbEntity = de.progeek.kimai.shared.ProjectEntity(
            id = 300L,
            name = "DB Project",
            parent = "Parent",
            globalActivities = 0L, // 0 = false
            customer = 90L
        )

        // When
        val project = dbEntity.map()

        // Then
        assertEquals(false, project.globalActivities) // 0 converted to false
    }

    @Test
    fun `map from database ProjectEntity handles empty parent`() {
        // Given
        val dbEntity = de.progeek.kimai.shared.ProjectEntity(
            id = 400L,
            name = "No Parent Project",
            parent = "",
            globalActivities = 1L,
            customer = 95L
        )

        // When
        val project = dbEntity.map()

        // Then
        assertEquals("", project.parent)
        assertEquals("", project.customer?.name) // Empty string when parent is empty
    }

    // ============================================================
    // toProject() Factory Function Tests
    // ============================================================

    @Test
    fun `toProject factory function creates project correctly`() {
        // When
        val project = toProject(
            id = 500L,
            parent = "Factory Parent",
            name = "Factory Project",
            globalActivities = 1L,
            customer = 100L
        )

        // Then
        assertEquals(500L, project.id)
        assertEquals("Factory Project", project.name)
        assertEquals("Factory Parent", project.parent)
        assertEquals(true, project.globalActivities)
        assertNotNull(project.customer)
        assertEquals(100L, project.customer?.id)
    }

    @Test
    fun `toProject factory function handles globalActivities as false`() {
        // When
        val project = toProject(
            id = 600L,
            parent = "Parent",
            name = "Project",
            globalActivities = 0L,
            customer = 100L
        )

        // Then
        assertEquals(false, project.globalActivities)
    }

    // ============================================================
    // ProjectExpanded.toProject() Tests
    // ============================================================

    @Test
    fun `toProject from ProjectExpanded maps all fields correctly`() {
        // Given
        val expanded = ProjectExpanded(
            id = 700,
            name = "Expanded Project",
            customer = Customer(
                id = 110,
                name = "Expanded Customer",
                visible = true,
                billable = true
            ),
            globalActivities = true,
            visible = true,
            billable = true
        )

        // When
        val project = expanded.toProject()

        // Then
        assertEquals(700L, project.id)
        assertEquals("Expanded Project", project.name)
        assertEquals("", project.parent) // ProjectExpanded doesn't have parent, defaults to ""
        assertEquals(true, project.globalActivities)
        assertNotNull(project.customer)
        assertEquals(110L, project.customer?.id)
        assertEquals("Expanded Customer", project.customer?.name)
    }

    @Test
    fun `toProject from ProjectExpanded handles null id`() {
        // Given
        val expanded = ProjectExpanded(
            id = null,
            name = "No ID Project",
            customer = Customer(
                id = 120,
                name = "Customer",
                visible = true,
                billable = true
            ),
            globalActivities = false,
            visible = false,
            billable = false
        )

        // When
        val project = expanded.toProject()

        // Then
        assertEquals(-1L, project.id)
        assertEquals(false, project.globalActivities)
    }

    @Test
    fun `toProject from ProjectExpanded always sets empty parent`() {
        // Given
        val expanded = ProjectExpanded(
            id = 800,
            name = "Test Project",
            customer = Customer(
                id = 130,
                name = "Test Customer",
                visible = true,
                billable = true
            ),
            globalActivities = true,
            visible = true,
            billable = true
        )

        // When
        val project = expanded.toProject()

        // Then
        assertEquals("", project.parent) // Always empty for ProjectExpanded
    }
}
