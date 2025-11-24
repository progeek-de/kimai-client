package de.progeek.kimai.shared.core.database.datasource.jira

import app.cash.turbine.test
import de.progeek.kimai.shared.core.jira.models.JiraIssue
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for JiraDatasource.
 *
 * Tests the following methods:
 * 1. getAll() - Returns Flow<List<JiraIssue>>
 * 2. getByKey(key: String) - Returns Result<JiraIssue?>
 * 3. getByProject(projectKey: String) - Returns Flow<List<JiraIssue>>
 * 4. getByAssignee(assignee: String) - Returns Flow<List<JiraIssue>>
 * 5. search(query: String, limit: Long) - Returns Result<List<JiraIssue>>
 * 6. insert(issue: JiraIssue) - Returns Result<JiraIssue>
 * 7. insert(issues: List<JiraIssue>) - Returns Result<List<JiraIssue>>
 * 8. deleteByKey(key: String) - Returns Result<Unit>
 * 9. deleteAll() - Returns Unit
 * 10. count() - Returns Result<Long>
 */
class JiraDatasourceTest {

    private lateinit var datasources: de.progeek.kimai.shared.utils.TestDatasources
    private lateinit var jiraDatasource: JiraDatasource

    @BeforeTest
    fun setup() {
        datasources = createTestDatasources()
        jiraDatasource = datasources.jiraDatasource
    }

    @AfterTest
    fun teardown() {
        clearDatabase(datasources.database)
    }

    private fun createTestIssue(
        key: String = "PROJ-123",
        summary: String = "Test Issue",
        status: String = "In Progress",
        projectKey: String = "PROJ",
        projectName: String = "Test Project",
        issueType: String = "Task",
        assignee: String? = "john@example.com",
        updated: Instant = Instant.fromEpochMilliseconds(1704067200000) // 2024-01-01
    ) = JiraIssue(
        key = key,
        summary = summary,
        status = status,
        projectKey = projectKey,
        projectName = projectName,
        issueType = issueType,
        assignee = assignee,
        updated = updated
    )

    // ============================================================
    // getAll() Tests
    // ============================================================

    @Test
    fun `getAll returns empty list when no issues exist`() = runTest {
        // When
        val result = jiraDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns all issues from database`() = runTest {
        // Given
        val issue1 = createTestIssue(key = "PROJ-1", summary = "First Issue")
        val issue2 = createTestIssue(key = "PROJ-2", summary = "Second Issue")
        jiraDatasource.insert(issue1)
        jiraDatasource.insert(issue2)

        // When
        val result = jiraDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.any { it.key == "PROJ-1" })
            assertTrue(items.any { it.key == "PROJ-2" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns issues ordered by updated timestamp descending`() = runTest {
        // Given
        val older = createTestIssue(
            key = "PROJ-1",
            updated = Instant.fromEpochMilliseconds(1704067200000)
        )
        val newer = createTestIssue(
            key = "PROJ-2",
            updated = Instant.fromEpochMilliseconds(1704153600000)
        )
        jiraDatasource.insert(older)
        jiraDatasource.insert(newer)

        // When
        val result = jiraDatasource.getAll()

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            // Newer issue should come first
            assertEquals("PROJ-2", items[0].key)
            assertEquals("PROJ-1", items[1].key)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getByKey() Tests
    // ============================================================

    @Test
    fun `getByKey returns issue when it exists`() = runTest {
        // Given
        val issue = createTestIssue(key = "FIND-123", summary = "Find Me")
        jiraDatasource.insert(issue)

        // When
        val result = jiraDatasource.getByKey("FIND-123")

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals("FIND-123", found.key)
        assertEquals("Find Me", found.summary)
    }

    @Test
    fun `getByKey returns null when issue does not exist`() = runTest {
        // When
        val result = jiraDatasource.getByKey("NOTFOUND-999")

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `getByKey correctly maps all issue fields`() = runTest {
        // Given
        val issue = createTestIssue(
            key = "FULL-100",
            summary = "Complete Issue",
            status = "Done",
            projectKey = "FULL",
            projectName = "Full Project",
            issueType = "Bug",
            assignee = "jane@example.com",
            updated = Instant.fromEpochMilliseconds(1704240000000)
        )
        jiraDatasource.insert(issue)

        // When
        val result = jiraDatasource.getByKey("FULL-100")

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals("FULL-100", found.key)
        assertEquals("Complete Issue", found.summary)
        assertEquals("Done", found.status)
        assertEquals("FULL", found.projectKey)
        assertEquals("Full Project", found.projectName)
        assertEquals("Bug", found.issueType)
        assertEquals("jane@example.com", found.assignee)
    }

    // ============================================================
    // getByProject() Tests
    // ============================================================

    @Test
    fun `getByProject returns only issues from specified project`() = runTest {
        // Given
        val proj1Issue1 = createTestIssue(key = "PROJ1-1", projectKey = "PROJ1", projectName = "Project 1")
        val proj1Issue2 = createTestIssue(key = "PROJ1-2", projectKey = "PROJ1", projectName = "Project 1")
        val proj2Issue = createTestIssue(key = "PROJ2-1", projectKey = "PROJ2", projectName = "Project 2")
        jiraDatasource.insert(proj1Issue1)
        jiraDatasource.insert(proj1Issue2)
        jiraDatasource.insert(proj2Issue)

        // When
        val result = jiraDatasource.getByProject("PROJ1")

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.all { it.projectKey == "PROJ1" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByProject returns empty list when no issues for project`() = runTest {
        // Given
        val issue = createTestIssue(key = "OTHER-1", projectKey = "OTHER")
        jiraDatasource.insert(issue)

        // When
        val result = jiraDatasource.getByProject("EMPTY")

        // Then
        result.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getByAssignee() Tests
    // ============================================================

    @Test
    fun `getByAssignee returns only issues assigned to specified user`() = runTest {
        // Given
        val johnIssue1 = createTestIssue(key = "TASK-1", assignee = "john@example.com")
        val johnIssue2 = createTestIssue(key = "TASK-2", assignee = "john@example.com")
        val janeIssue = createTestIssue(key = "TASK-3", assignee = "jane@example.com")
        jiraDatasource.insert(johnIssue1)
        jiraDatasource.insert(johnIssue2)
        jiraDatasource.insert(janeIssue)

        // When
        val result = jiraDatasource.getByAssignee("john@example.com")

        // Then
        result.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.all { it.assignee == "john@example.com" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByAssignee returns empty list when no issues for assignee`() = runTest {
        // Given
        val issue = createTestIssue(key = "TASK-1", assignee = "john@example.com")
        jiraDatasource.insert(issue)

        // When
        val result = jiraDatasource.getByAssignee("nobody@example.com")

        // Then
        result.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // search() Tests
    // ============================================================

    @Test
    fun `search finds issues by key match`() = runTest {
        // Given
        val issue1 = createTestIssue(key = "SEARCH-100", summary = "Test Issue")
        val issue2 = createTestIssue(key = "SEARCH-200", summary = "Another Issue")
        val issue3 = createTestIssue(key = "OTHER-100", summary = "Different Issue")
        jiraDatasource.insert(issue1)
        jiraDatasource.insert(issue2)
        jiraDatasource.insert(issue3)

        // When
        val result = jiraDatasource.search("SEARCH")

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals(2, found.size)
        assertTrue(found.all { it.key.contains("SEARCH") })
    }

    @Test
    fun `search finds issues by summary match`() = runTest {
        // Given
        val issue1 = createTestIssue(key = "TASK-1", summary = "Fix login bug")
        val issue2 = createTestIssue(key = "TASK-2", summary = "Fix logout bug")
        val issue3 = createTestIssue(key = "TASK-3", summary = "Add new feature")
        jiraDatasource.insert(issue1)
        jiraDatasource.insert(issue2)
        jiraDatasource.insert(issue3)

        // When
        val result = jiraDatasource.search("bug")

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals(2, found.size)
        assertTrue(found.all { it.summary.contains("bug") })
    }

    @Test
    fun `search respects limit parameter`() = runTest {
        // Given - Insert more than limit
        for (i in 1..10) {
            val issue = createTestIssue(key = "LIMIT-$i", summary = "Test Issue $i")
            jiraDatasource.insert(issue)
        }

        // When
        val result = jiraDatasource.search("LIMIT", limit = 5)

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertEquals(5, found.size)
    }

    @Test
    fun `search returns empty list when no matches`() = runTest {
        // Given
        val issue = createTestIssue(key = "TASK-1", summary = "Test Issue")
        jiraDatasource.insert(issue)

        // When
        val result = jiraDatasource.search("NOMATCH")

        // Then
        assertTrue(result.isSuccess)
        val found = result.getOrNull()
        assertNotNull(found)
        assertTrue(found.isEmpty())
    }

    // ============================================================
    // insert(issue: JiraIssue) Tests
    // ============================================================

    @Test
    fun `insert single issue successfully stores in database`() = runTest {
        // Given
        val issue = createTestIssue(key = "NEW-123", summary = "New Issue")

        // When
        val result = jiraDatasource.insert(issue)

        // Then
        assertTrue(result.isSuccess)
        val inserted = result.getOrNull()
        assertNotNull(inserted)
        assertEquals("NEW-123", inserted.key)

        // Verify it was inserted
        val getResult = jiraDatasource.getByKey("NEW-123")
        assertTrue(getResult.isSuccess)
        assertNotNull(getResult.getOrNull())
    }

    @Test
    fun `insert replaces existing issue with same key`() = runTest {
        // Given
        val original = createTestIssue(key = "UPDATE-1", summary = "Original", status = "To Do")
        jiraDatasource.insert(original)

        // When - Insert with same key but different data
        val updated = createTestIssue(key = "UPDATE-1", summary = "Updated", status = "Done")
        val result = jiraDatasource.insert(updated)

        // Then
        assertTrue(result.isSuccess)

        // Verify it was updated
        val getResult = jiraDatasource.getByKey("UPDATE-1")
        val found = getResult.getOrNull()
        assertNotNull(found)
        assertEquals("Updated", found.summary)
        assertEquals("Done", found.status)
    }

    @Test
    fun `insert handles null assignee correctly`() = runTest {
        // Given
        val issue = createTestIssue(key = "UNASSIGNED-1", assignee = null)

        // When
        val result = jiraDatasource.insert(issue)

        // Then
        assertTrue(result.isSuccess)

        // Verify assignee is null
        val getResult = jiraDatasource.getByKey("UNASSIGNED-1")
        val found = getResult.getOrNull()
        assertNotNull(found)
        assertNull(found.assignee)
    }

    // ============================================================
    // insert(issues: List<JiraIssue>) Tests
    // ============================================================

    @Test
    fun `insert list successfully stores multiple issues`() = runTest {
        // Given
        val issues = listOf(
            createTestIssue(key = "BATCH-1", summary = "Batch Issue 1"),
            createTestIssue(key = "BATCH-2", summary = "Batch Issue 2"),
            createTestIssue(key = "BATCH-3", summary = "Batch Issue 3")
        )

        // When
        val result = jiraDatasource.insert(issues)

        // Then
        assertTrue(result.isSuccess)
        val inserted = result.getOrNull()
        assertNotNull(inserted)
        assertEquals(3, inserted.size)

        // Verify all were inserted
        jiraDatasource.getAll().test {
            val items = awaitItem()
            assertEquals(3, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert empty list succeeds and returns empty list`() = runTest {
        // Given
        val emptyList = emptyList<JiraIssue>()

        // When
        val result = jiraDatasource.insert(emptyList)

        // Then
        assertTrue(result.isSuccess)
        val inserted = result.getOrNull()
        assertNotNull(inserted)
        assertTrue(inserted.isEmpty())
    }

    @Test
    fun `insert list with duplicates replaces existing issues`() = runTest {
        // Given - Insert original
        val original = createTestIssue(key = "DUP-1", summary = "Original")
        jiraDatasource.insert(original)

        // When - Insert list with duplicate key
        val updated = createTestIssue(key = "DUP-1", summary = "Updated")
        val new = createTestIssue(key = "DUP-2", summary = "New")
        val result = jiraDatasource.insert(listOf(updated, new))

        // Then
        assertTrue(result.isSuccess)

        // Verify only 2 issues exist (duplicate replaced)
        jiraDatasource.getAll().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            val dup1 = items.find { it.key == "DUP-1" }
            assertNotNull(dup1)
            assertEquals("Updated", dup1.summary)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // deleteByKey() Tests
    // ============================================================

    @Test
    fun `deleteByKey removes issue from database`() = runTest {
        // Given
        val issue = createTestIssue(key = "DELETE-1", summary = "To Delete")
        jiraDatasource.insert(issue)

        // When
        val result = jiraDatasource.deleteByKey("DELETE-1")

        // Then
        assertTrue(result.isSuccess)

        // Verify it was deleted
        val getResult = jiraDatasource.getByKey("DELETE-1")
        assertNull(getResult.getOrNull())
    }

    @Test
    fun `deleteByKey succeeds when issue does not exist`() = runTest {
        // When
        val result = jiraDatasource.deleteByKey("NOTEXIST-999")

        // Then
        assertTrue(result.isSuccess)
    }

    // ============================================================
    // deleteAll() Tests
    // ============================================================

    @Test
    fun `deleteAll removes all issues from database`() = runTest {
        // Given
        val issues = listOf(
            createTestIssue(key = "DEL-1"),
            createTestIssue(key = "DEL-2"),
            createTestIssue(key = "DEL-3")
        )
        jiraDatasource.insert(issues)

        // When
        jiraDatasource.deleteAll()

        // Then
        jiraDatasource.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteAll on empty database succeeds`() = runTest {
        // When - Delete from empty database
        jiraDatasource.deleteAll()

        // Then - Should not throw error
        jiraDatasource.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // count() Tests
    // ============================================================

    @Test
    fun `count returns zero when no issues exist`() = runTest {
        // When
        val result = jiraDatasource.count()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull())
    }

    @Test
    fun `count returns correct number of issues`() = runTest {
        // Given
        val issues = listOf(
            createTestIssue(key = "COUNT-1"),
            createTestIssue(key = "COUNT-2"),
            createTestIssue(key = "COUNT-3"),
            createTestIssue(key = "COUNT-4"),
            createTestIssue(key = "COUNT-5")
        )
        jiraDatasource.insert(issues)

        // When
        val result = jiraDatasource.count()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(5L, result.getOrNull())
    }

    @Test
    fun `count updates after insertions and deletions`() = runTest {
        // Given - Insert 3 issues
        val issues = listOf(
            createTestIssue(key = "DYN-1"),
            createTestIssue(key = "DYN-2"),
            createTestIssue(key = "DYN-3")
        )
        jiraDatasource.insert(issues)

        // When - Count after insert
        var result = jiraDatasource.count()
        assertTrue(result.isSuccess)
        assertEquals(3L, result.getOrNull())

        // When - Delete one and count again
        jiraDatasource.deleteByKey("DYN-2")
        result = jiraDatasource.count()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2L, result.getOrNull())
    }
}
