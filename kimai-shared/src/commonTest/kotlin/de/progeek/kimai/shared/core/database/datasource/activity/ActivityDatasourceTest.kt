package de.progeek.kimai.shared.core.database.datasource.activity

import app.cash.turbine.test
import de.progeek.kimai.shared.core.models.Activity
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

/**
 * Test suite for ActivityDatasource.
 *
 * Tests the following methods:
 * 1. getAll() - Returns Flow<List<Activity>>
 * 2. getByProjectId(id: Long) - Returns Result<List<Activity>>
 * 3. insert(list: List<Activity>) - Returns Result<List<Activity>>
 * 4. deleteAll() - Returns Unit
 */
class ActivityDatasourceTest {

    private lateinit var datasources: de.progeek.kimai.shared.utils.TestDatasources
    private lateinit var activityDatasource: ActivityDatasource

    @BeforeTest
    fun setup() {
        datasources = createTestDatasources()
        activityDatasource = datasources.activityDatasource
    }

    @AfterTest
    fun teardown() {
        clearDatabase(datasources.database)
    }

    private fun createTestActivity(
        id: Long = 1L,
        name: String = "Test Activity",
        project: Long? = null
    ) = Activity(
        id = id,
        name = name,
        project = project
    )

    // ============================================================
    // getAll() Tests
    // ============================================================

    @Test
    fun `getAll returns empty list when no activities exist`() = runTest {
        // When
        val result = activityDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns all activities from database`() = runTest {
        // Given
        val activities = listOf(
            createTestActivity(id = 1L, name = "Development"),
            createTestActivity(id = 2L, name = "Testing"),
            createTestActivity(id = 3L, name = "Code Review")
        )
        activityDatasource.insert(activities)

        // When
        val result = activityDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(3, items.size)
            assertTrue(items.any { it.name == "Development" })
            assertTrue(items.any { it.name == "Testing" })
            assertTrue(items.any { it.name == "Code Review" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll includes both global and project-specific activities`() = runTest {
        // Given
        val activities = listOf(
            createTestActivity(id = 10L, name = "Global Activity", project = null),
            createTestActivity(id = 11L, name = "Project Activity", project = 100L)
        )
        activityDatasource.insert(activities)

        // When
        val result = activityDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            val global = items.find { it.id == 10L }
            val projectSpecific = items.find { it.id == 11L }
            assertNotNull(global)
            assertNotNull(projectSpecific)
            assertNull(global.project)
            assertEquals(100L, projectSpecific.project)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getByProjectId() Tests
    // ============================================================

    @Test
    fun `getByProjectId returns activities for specific project`() = runTest {
        // Given
        val activities = listOf(
            createTestActivity(id = 20L, name = "Project 1 Activity", project = 1L),
            createTestActivity(id = 21L, name = "Project 2 Activity", project = 2L),
            createTestActivity(id = 22L, name = "Another Project 1 Activity", project = 1L),
            createTestActivity(id = 23L, name = "Global Activity", project = null)
        )
        activityDatasource.insert(activities)

        // When
        val result = activityDatasource.getByProjectId(1L)

        // Then
        assertTrue(result.isSuccess)
        val items = result.getOrNull()
        assertNotNull(items)
        // Should include activities for project 1 AND global activities (null project)
        assertTrue(items.size >= 2)
        assertTrue(items.any { it.id == 20L })
        assertTrue(items.any { it.id == 22L })
        assertTrue(items.any { it.id == 23L }) // Global activity included
    }

    @Test
    fun `getByProjectId includes global activities with null project`() = runTest {
        // Given
        val activities = listOf(
            createTestActivity(id = 30L, name = "Global 1", project = null),
            createTestActivity(id = 31L, name = "Global 2", project = null),
            createTestActivity(id = 32L, name = "Project Specific", project = 5L)
        )
        activityDatasource.insert(activities)

        // When - Get activities for project 5
        val result = activityDatasource.getByProjectId(5L)

        // Then - Should include project 5 activity AND global activities
        assertTrue(result.isSuccess)
        val items = result.getOrNull()
        assertNotNull(items)
        assertEquals(3, items.size) // All activities (2 global + 1 project-specific)
    }

    @Test
    fun `getByProjectId returns only global activities when project has no specific activities`() = runTest {
        // Given
        val activities = listOf(
            createTestActivity(id = 40L, name = "Global 1", project = null),
            createTestActivity(id = 41L, name = "Global 2", project = null),
            createTestActivity(id = 42L, name = "Other Project Activity", project = 99L)
        )
        activityDatasource.insert(activities)

        // When - Get activities for project that has no specific activities
        val result = activityDatasource.getByProjectId(50L)

        // Then - Should return only global activities
        assertTrue(result.isSuccess)
        val items = result.getOrNull()
        assertNotNull(items)
        assertEquals(2, items.size)
        assertTrue(items.all { it.project == null })
    }

    // ============================================================
    // insert(list: List<Activity>) Tests
    // ============================================================

    @Test
    fun `insert successfully stores multiple activities`() = runTest {
        // Given
        val activities = listOf(
            createTestActivity(id = 60L, name = "Activity 1"),
            createTestActivity(id = 61L, name = "Activity 2"),
            createTestActivity(id = 62L, name = "Activity 3", project = 10L)
        )

        // When
        val result = activityDatasource.insert(activities)

        // Then
        assertTrue(result.isSuccess)
        val returned = result.getOrNull()
        assertNotNull(returned)
        assertEquals(3, returned.size)

        // Verify they were inserted
        activityDatasource.getAll().test {
            val items = awaitItem()
            assertEquals(3, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert empty list succeeds and returns empty list`() = runTest {
        // Given
        val emptyList = emptyList<Activity>()

        // When
        val result = activityDatasource.insert(emptyList)

        // Then
        assertTrue(result.isSuccess)
        val returned = result.getOrNull()
        assertNotNull(returned)
        assertTrue(returned.isEmpty())
    }

    @Test
    fun `insert replaces existing activities with same id`() = runTest {
        // Given
        val original = listOf(createTestActivity(id = 70L, name = "Original"))
        activityDatasource.insert(original)

        // When - Insert with same ID but different name
        val replacement = listOf(createTestActivity(id = 70L, name = "Replacement"))
        val result = activityDatasource.insert(replacement)

        // Then
        assertTrue(result.isSuccess)

        // Verify it was replaced
        activityDatasource.getAll().test {
            val items = awaitItem()
            val activity = items.find { it.id == 70L }
            assertNotNull(activity)
            assertEquals("Replacement", activity.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // deleteAll() Tests
    // ============================================================

    @Test
    fun `deleteAll removes all activities from database`() = runTest {
        // Given
        val activities = listOf(
            createTestActivity(id = 80L, name = "Delete 1"),
            createTestActivity(id = 81L, name = "Delete 2"),
            createTestActivity(id = 82L, name = "Delete 3")
        )
        activityDatasource.insert(activities)

        // When
        activityDatasource.deleteAll()

        // Then
        activityDatasource.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteAll on empty database succeeds`() = runTest {
        // When - Delete from empty database
        activityDatasource.deleteAll()

        // Then - Should not throw error
        activityDatasource.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
