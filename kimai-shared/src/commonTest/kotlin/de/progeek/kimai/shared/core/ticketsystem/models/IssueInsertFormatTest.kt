package de.progeek.kimai.shared.core.ticketsystem.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * Test suite for IssueInsertFormat utility.
 *
 * Tests the formatting and placeholder replacement functionality
 * used for inserting issue references into text fields.
 */
class IssueInsertFormatTest {

    private val testIssue = TicketIssue(
        id = "10001",
        key = "PROJ-123",
        summary = "Fix login bug",
        status = "Open",
        projectKey = "PROJ",
        projectName = "Test Project",
        issueType = "Bug",
        assignee = "john.doe",
        updated = Instant.fromEpochMilliseconds(1700000000000),
        sourceId = "source-uuid-123",
        provider = TicketProvider.JIRA,
        webUrl = "https://company.atlassian.net/browse/PROJ-123"
    )

    // ============================================================
    // DEFAULT_FORMAT Tests
    // ============================================================

    @Test
    fun `DEFAULT_FORMAT contains key and summary placeholders`() {
        val format = IssueInsertFormat.DEFAULT_FORMAT
        assertTrue(format.contains("{key}"))
        assertTrue(format.contains("{summary}"))
    }

    @Test
    fun `DEFAULT_FORMAT is key colon summary`() {
        assertEquals("{key}: {summary}", IssueInsertFormat.DEFAULT_FORMAT)
    }

    // ============================================================
    // format() Tests
    // ============================================================

    @Test
    fun `format with default pattern produces key colon summary`() {
        val result = IssueInsertFormat.format(testIssue, IssueInsertFormat.DEFAULT_FORMAT)
        assertEquals("PROJ-123: Fix login bug", result)
    }

    @Test
    fun `format replaces key placeholder correctly`() {
        val result = IssueInsertFormat.format(testIssue, "{key}")
        assertEquals("PROJ-123", result)
    }

    @Test
    fun `format replaces summary placeholder correctly`() {
        val result = IssueInsertFormat.format(testIssue, "{summary}")
        assertEquals("Fix login bug", result)
    }

    @Test
    fun `format replaces status placeholder correctly`() {
        val result = IssueInsertFormat.format(testIssue, "{status}")
        assertEquals("Open", result)
    }

    @Test
    fun `format replaces project placeholder correctly`() {
        val result = IssueInsertFormat.format(testIssue, "{project}")
        assertEquals("PROJ", result)
    }

    @Test
    fun `format replaces type placeholder correctly`() {
        val result = IssueInsertFormat.format(testIssue, "{type}")
        assertEquals("Bug", result)
    }

    @Test
    fun `format handles multiple placeholders in one pattern`() {
        val result = IssueInsertFormat.format(testIssue, "[{project}] {key}: {summary}")
        assertEquals("[PROJ] PROJ-123: Fix login bug", result)
    }

    @Test
    fun `format handles all placeholders combined`() {
        val result = IssueInsertFormat.format(testIssue, "{key} - {summary} ({status}, {type}, {project})")
        assertEquals("PROJ-123 - Fix login bug (Open, Bug, PROJ)", result)
    }

    @Test
    fun `format preserves text without placeholders`() {
        val result = IssueInsertFormat.format(testIssue, "Issue: {key}")
        assertEquals("Issue: PROJ-123", result)
    }

    @Test
    fun `format handles pattern with no placeholders`() {
        val result = IssueInsertFormat.format(testIssue, "Static text")
        assertEquals("Static text", result)
    }

    @Test
    fun `format handles empty pattern`() {
        val result = IssueInsertFormat.format(testIssue, "")
        assertEquals("", result)
    }

    @Test
    fun `format handles duplicate placeholders`() {
        val result = IssueInsertFormat.format(testIssue, "{key} {key} {key}")
        assertEquals("PROJ-123 PROJ-123 PROJ-123", result)
    }

    @Test
    fun `format handles unknown placeholders by preserving them`() {
        val result = IssueInsertFormat.format(testIssue, "{key}: {unknown}")
        assertEquals("PROJ-123: {unknown}", result)
    }

    @Test
    fun `format handles GitHub style key`() {
        val githubIssue = testIssue.copy(key = "#456", provider = TicketProvider.GITHUB)
        val result = IssueInsertFormat.format(githubIssue, "{key}: {summary}")
        assertEquals("#456: Fix login bug", result)
    }

    @Test
    fun `format handles GitLab style key`() {
        val gitlabIssue = testIssue.copy(key = "#789", provider = TicketProvider.GITLAB)
        val result = IssueInsertFormat.format(gitlabIssue, "{key}: {summary}")
        assertEquals("#789: Fix login bug", result)
    }

    @Test
    fun `format handles special characters in summary`() {
        val issueWithSpecialChars = testIssue.copy(summary = "Fix \"quotes\" & <brackets>")
        val result = IssueInsertFormat.format(issueWithSpecialChars, "{key}: {summary}")
        assertEquals("PROJ-123: Fix \"quotes\" & <brackets>", result)
    }

    @Test
    fun `format handles unicode in summary`() {
        val issueWithUnicode = testIssue.copy(summary = "修复登录错误")
        val result = IssueInsertFormat.format(issueWithUnicode, "{key}: {summary}")
        assertEquals("PROJ-123: 修复登录错误", result)
    }

    @Test
    fun `format handles empty summary`() {
        val issueWithEmptySummary = testIssue.copy(summary = "")
        val result = IssueInsertFormat.format(issueWithEmptySummary, "{key}: {summary}")
        assertEquals("PROJ-123: ", result)
    }

    // ============================================================
    // generateExample() Tests
    // ============================================================

    @Test
    fun `generateExample with default pattern produces expected output`() {
        val result = IssueInsertFormat.generateExample(IssueInsertFormat.DEFAULT_FORMAT)
        assertEquals("PROJ-123: Fix login bug", result)
    }

    @Test
    fun `generateExample replaces key placeholder with example`() {
        val result = IssueInsertFormat.generateExample("{key}")
        assertEquals("PROJ-123", result)
    }

    @Test
    fun `generateExample replaces summary placeholder with example`() {
        val result = IssueInsertFormat.generateExample("{summary}")
        assertEquals("Fix login bug", result)
    }

    @Test
    fun `generateExample replaces status placeholder with example`() {
        val result = IssueInsertFormat.generateExample("{status}")
        assertEquals("Open", result)
    }

    @Test
    fun `generateExample replaces project placeholder with example`() {
        val result = IssueInsertFormat.generateExample("{project}")
        assertEquals("PROJ", result)
    }

    @Test
    fun `generateExample replaces type placeholder with example`() {
        val result = IssueInsertFormat.generateExample("{type}")
        assertEquals("Bug", result)
    }

    @Test
    fun `generateExample handles all placeholders`() {
        val result = IssueInsertFormat.generateExample("{key} - {summary} [{project}/{type}] ({status})")
        assertEquals("PROJ-123 - Fix login bug [PROJ/Bug] (Open)", result)
    }

    @Test
    fun `generateExample preserves unknown placeholders`() {
        val result = IssueInsertFormat.generateExample("{key}: {unknown}")
        assertEquals("PROJ-123: {unknown}", result)
    }

    @Test
    fun `generateExample handles empty pattern`() {
        val result = IssueInsertFormat.generateExample("")
        assertEquals("", result)
    }

    // ============================================================
    // availablePlaceholders Tests
    // ============================================================

    @Test
    fun `availablePlaceholders contains five entries`() {
        assertEquals(5, IssueInsertFormat.availablePlaceholders.size)
    }

    @Test
    fun `availablePlaceholders contains key placeholder`() {
        val keyEntry = IssueInsertFormat.availablePlaceholders.find { it.first == "{key}" }
        assertTrue(keyEntry != null)
        assertTrue(keyEntry.second.isNotBlank())
    }

    @Test
    fun `availablePlaceholders contains summary placeholder`() {
        val summaryEntry = IssueInsertFormat.availablePlaceholders.find { it.first == "{summary}" }
        assertTrue(summaryEntry != null)
        assertTrue(summaryEntry.second.isNotBlank())
    }

    @Test
    fun `availablePlaceholders contains status placeholder`() {
        val statusEntry = IssueInsertFormat.availablePlaceholders.find { it.first == "{status}" }
        assertTrue(statusEntry != null)
        assertTrue(statusEntry.second.isNotBlank())
    }

    @Test
    fun `availablePlaceholders contains project placeholder`() {
        val projectEntry = IssueInsertFormat.availablePlaceholders.find { it.first == "{project}" }
        assertTrue(projectEntry != null)
        assertTrue(projectEntry.second.isNotBlank())
    }

    @Test
    fun `availablePlaceholders contains type placeholder`() {
        val typeEntry = IssueInsertFormat.availablePlaceholders.find { it.first == "{type}" }
        assertTrue(typeEntry != null)
        assertTrue(typeEntry.second.isNotBlank())
    }

    // ============================================================
    // TicketIssue.format() extension Tests
    // ============================================================

    @Test
    fun `TicketIssue format extension delegates to IssueInsertFormat`() {
        val result = testIssue.format(IssueInsertFormat.DEFAULT_FORMAT)
        val expected = IssueInsertFormat.format(testIssue, IssueInsertFormat.DEFAULT_FORMAT)
        assertEquals(expected, result)
    }

    @Test
    fun `TicketIssue format extension works with custom pattern`() {
        val result = testIssue.format("[{key}] {summary}")
        assertEquals("[PROJ-123] Fix login bug", result)
    }
}
