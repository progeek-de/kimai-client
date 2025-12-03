package de.progeek.kimai.shared.core.ticketsystem.datasource

import app.cash.turbine.test
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.utils.TestDatasources
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
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for TicketIssueDatasource.
 *
 * Tests database operations for ticket issue caching including:
 * - CRUD operations (getAll, getByKey, insert, delete)
 * - Filtering (getBySource, getByProject, getByAssignee)
 * - Search functionality
 * - Batch operations
 * - Counting operations
 */
class TicketIssueDatasourceTest {

    private lateinit var testDatasources: TestDatasources
    private lateinit var datasource: TicketIssueDatasource

    private val sourceId1 = "source-uuid-111"
    private val sourceId2 = "source-uuid-222"

    private val testIssue1 = TicketIssue(
        id = "10001",
        key = "PROJ-123",
        summary = "Fix login bug",
        status = "Open",
        projectKey = "PROJ",
        projectName = "Project Alpha",
        issueType = "Bug",
        assignee = "john.doe",
        updated = Instant.fromEpochMilliseconds(1700000000000),
        sourceId = sourceId1,
        provider = TicketProvider.JIRA,
        webUrl = "https://company.atlassian.net/browse/PROJ-123"
    )

    private val testIssue2 = TicketIssue(
        id = "10002",
        key = "PROJ-456",
        summary = "Add dark mode",
        status = "In Progress",
        projectKey = "PROJ",
        projectName = "Project Alpha",
        issueType = "Feature",
        assignee = "jane.doe",
        updated = Instant.fromEpochMilliseconds(1700001000000),
        sourceId = sourceId1,
        provider = TicketProvider.JIRA,
        webUrl = "https://company.atlassian.net/browse/PROJ-456"
    )

    private val testIssue3 = TicketIssue(
        id = "789",
        key = "#42",
        summary = "GitHub issue test",
        status = "Open",
        projectKey = "owner/repo",
        projectName = "repo",
        issueType = "Issue",
        assignee = "john.doe",
        updated = Instant.fromEpochMilliseconds(1700002000000),
        sourceId = sourceId2,
        provider = TicketProvider.GITHUB,
        webUrl = "https://github.com/owner/repo/issues/42"
    )

    @BeforeTest
    fun setup() {
        testDatasources = createTestDatasources()
        datasource = testDatasources.ticketIssueDatasource
    }

    @AfterTest
    fun teardown() {
        clearDatabase(testDatasources.database)
    }

    // ============================================================
    // getAll() Tests
    // ============================================================

    @Test
    fun `getAll returns empty list when database is empty`() = runTest {
        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns all inserted issues`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(2, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns issues from multiple sources`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue3)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.any { it.provider == TicketProvider.JIRA })
            assertTrue(items.any { it.provider == TicketProvider.GITHUB })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getBySource() Tests
    // ============================================================

    @Test
    fun `getBySource returns only issues from specified source`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)
        datasource.insert(testIssue3)

        datasource.getBySource(sourceId1).test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.all { it.sourceId == sourceId1 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getBySource returns empty list for unknown source`() = runTest {
        datasource.insert(testIssue1)

        datasource.getBySource("unknown-source").test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getByKey() Tests
    // ============================================================

    @Test
    fun `getByKey returns issue when found`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.getByKey("PROJ-123")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals("PROJ-123", result.getOrNull()?.key)
    }

    @Test
    fun `getByKey returns null when not found`() = runTest {
        val result = datasource.getByKey("NONEXISTENT-999")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ============================================================
    // getBySourceAndKey() Tests
    // ============================================================

    @Test
    fun `getBySourceAndKey returns issue when found`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.getBySourceAndKey(sourceId1, "PROJ-123")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals("PROJ-123", result.getOrNull()?.key)
    }

    @Test
    fun `getBySourceAndKey returns null for wrong source`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.getBySourceAndKey(sourceId2, "PROJ-123")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ============================================================
    // getByProject() Tests
    // ============================================================

    @Test
    fun `getByProject returns issues for specified project`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)
        datasource.insert(testIssue3)

        datasource.getByProject("PROJ").test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.all { it.projectKey == "PROJ" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByProject returns empty list for unknown project`() = runTest {
        datasource.insert(testIssue1)

        datasource.getByProject("UNKNOWN").test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getByAssignee() Tests
    // ============================================================

    @Test
    fun `getByAssignee returns issues for specified assignee`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)
        datasource.insert(testIssue3)

        datasource.getByAssignee("john.doe").test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.all { it.assignee == "john.doe" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByAssignee returns empty list for unknown assignee`() = runTest {
        datasource.insert(testIssue1)

        datasource.getByAssignee("unknown.user").test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // search() Tests
    // ============================================================

    @Test
    fun `search finds issues by key`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)

        val result = datasource.search("PROJ-123")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("PROJ-123", result.getOrNull()?.first()?.key)
    }

    @Test
    fun `search finds issues by summary`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)

        val result = datasource.search("dark mode")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("PROJ-456", result.getOrNull()?.first()?.key)
    }

    @Test
    fun `search returns empty list when no matches`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.search("nonexistent query")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `search respects limit parameter`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)
        datasource.insert(testIssue3)

        val result = datasource.search("", limit = 2)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    // ============================================================
    // searchBySource() Tests
    // ============================================================

    @Test
    fun `searchBySource filters by source and query`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)
        datasource.insert(testIssue3)

        val result = datasource.searchBySource(sourceId1, "PROJ")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()?.all { it.sourceId == sourceId1 } == true)
    }

    @Test
    fun `searchBySource returns empty for wrong source`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.searchBySource(sourceId2, "PROJ-123")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    // ============================================================
    // insert() Single Issue Tests
    // ============================================================

    @Test
    fun `insert single issue returns success`() = runTest {
        val result = datasource.insert(testIssue1)

        assertTrue(result.isSuccess)
        assertEquals(testIssue1.id, result.getOrNull()?.id)
    }

    @Test
    fun `insert stores all fields correctly`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.getByKey("PROJ-123")
        val retrieved = result.getOrNull()

        assertNotNull(retrieved)
        assertEquals("10001", retrieved.id)
        assertEquals("PROJ-123", retrieved.key)
        assertEquals("Fix login bug", retrieved.summary)
        assertEquals("Open", retrieved.status)
        assertEquals("PROJ", retrieved.projectKey)
        assertEquals("Project Alpha", retrieved.projectName)
        assertEquals("Bug", retrieved.issueType)
        assertEquals("john.doe", retrieved.assignee)
        assertEquals(sourceId1, retrieved.sourceId)
        assertEquals(TicketProvider.JIRA, retrieved.provider)
        assertEquals("https://company.atlassian.net/browse/PROJ-123", retrieved.webUrl)
    }

    @Test
    fun `insert handles issue with null assignee`() = runTest {
        val issueWithNullAssignee = testIssue1.copy(assignee = null)

        datasource.insert(issueWithNullAssignee)

        val result = datasource.getByKey("PROJ-123")
        assertNull(result.getOrNull()?.assignee)
    }

    @Test
    fun `insert handles issue with null webUrl`() = runTest {
        val issueWithNullUrl = testIssue1.copy(webUrl = null)

        datasource.insert(issueWithNullUrl)

        val result = datasource.getByKey("PROJ-123")
        assertNull(result.getOrNull()?.webUrl)
    }

    // ============================================================
    // insert() Batch Tests
    // ============================================================

    @Test
    fun `insert batch returns success with all issues`() = runTest {
        val issues = listOf(testIssue1, testIssue2, testIssue3)

        val result = datasource.insert(issues)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }

    @Test
    fun `insert batch stores all issues`() = runTest {
        val issues = listOf(testIssue1, testIssue2, testIssue3)

        datasource.insert(issues)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(3, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert empty batch succeeds`() = runTest {
        val result = datasource.insert(emptyList())

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    // ============================================================
    // deleteBySource() Tests
    // ============================================================

    @Test
    fun `deleteBySource removes all issues for source`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)
        datasource.insert(testIssue3)

        val result = datasource.deleteBySource(sourceId1)

        assertTrue(result.isSuccess)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(sourceId2, items.first().sourceId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteBySource does nothing for unknown source`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.deleteBySource("unknown-source")

        assertTrue(result.isSuccess)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(1, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // deleteByKey() Tests
    // ============================================================

    @Test
    fun `deleteByKey removes specific issue`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)

        val result = datasource.deleteByKey("PROJ-123")

        assertTrue(result.isSuccess)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("PROJ-456", items.first().key)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteByKey does nothing for unknown key`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.deleteByKey("NONEXISTENT-999")

        assertTrue(result.isSuccess)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(1, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // deleteAll() Tests
    // ============================================================

    @Test
    fun `deleteAll removes all issues`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)
        datasource.insert(testIssue3)

        datasource.deleteAll()

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // count() Tests
    // ============================================================

    @Test
    fun `count returns zero for empty database`() = runTest {
        val result = datasource.count()

        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull())
    }

    @Test
    fun `count returns correct number after inserts`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)
        datasource.insert(testIssue3)

        val result = datasource.count()

        assertTrue(result.isSuccess)
        assertEquals(3L, result.getOrNull())
    }

    // ============================================================
    // countBySource() Tests
    // ============================================================

    @Test
    fun `countBySource returns correct count for source`() = runTest {
        datasource.insert(testIssue1)
        datasource.insert(testIssue2)
        datasource.insert(testIssue3)

        val result = datasource.countBySource(sourceId1)

        assertTrue(result.isSuccess)
        assertEquals(2L, result.getOrNull())
    }

    @Test
    fun `countBySource returns zero for unknown source`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.countBySource("unknown-source")

        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull())
    }

    // ============================================================
    // Provider Mapping Tests
    // ============================================================

    @Test
    fun `stores and retrieves JIRA provider correctly`() = runTest {
        datasource.insert(testIssue1)

        val result = datasource.getByKey("PROJ-123")

        assertEquals(TicketProvider.JIRA, result.getOrNull()?.provider)
    }

    @Test
    fun `stores and retrieves GITHUB provider correctly`() = runTest {
        datasource.insert(testIssue3)

        val result = datasource.getByKey("#42")

        assertEquals(TicketProvider.GITHUB, result.getOrNull()?.provider)
    }

    @Test
    fun `stores and retrieves GITLAB provider correctly`() = runTest {
        val gitlabIssue = testIssue1.copy(
            id = "999",
            key = "#99",
            provider = TicketProvider.GITLAB
        )
        datasource.insert(gitlabIssue)

        val result = datasource.getByKey("#99")

        assertEquals(TicketProvider.GITLAB, result.getOrNull()?.provider)
    }
}
