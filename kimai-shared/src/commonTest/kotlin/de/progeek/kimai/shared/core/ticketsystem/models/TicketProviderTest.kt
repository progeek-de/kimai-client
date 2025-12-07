package de.progeek.kimai.shared.core.ticketsystem.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Test suite for TicketProvider enum.
 *
 * Tests the enum values and the fromString parsing functionality.
 */
class TicketProviderTest {

    // ============================================================
    // Enum Values Tests
    // ============================================================

    @Test
    fun `TicketProvider has exactly three values`() {
        assertEquals(3, TicketProvider.entries.size)
    }

    @Test
    fun `TicketProvider contains JIRA`() {
        assertNotNull(TicketProvider.entries.find { it == TicketProvider.JIRA })
    }

    @Test
    fun `TicketProvider contains GITHUB`() {
        assertNotNull(TicketProvider.entries.find { it == TicketProvider.GITHUB })
    }

    @Test
    fun `TicketProvider contains GITLAB`() {
        assertNotNull(TicketProvider.entries.find { it == TicketProvider.GITLAB })
    }

    // ============================================================
    // displayName Tests
    // ============================================================

    @Test
    fun `JIRA displayName is Jira`() {
        assertEquals("Jira", TicketProvider.JIRA.displayName)
    }

    @Test
    fun `GITHUB displayName is GitHub Issues`() {
        assertEquals("GitHub Issues", TicketProvider.GITHUB.displayName)
    }

    @Test
    fun `GITLAB displayName is GitLab Issues`() {
        assertEquals("GitLab Issues", TicketProvider.GITLAB.displayName)
    }

    // ============================================================
    // fromString() Tests - Exact match
    // ============================================================

    @Test
    fun `fromString returns JIRA for JIRA string`() {
        val result = TicketProvider.fromString("JIRA")
        assertEquals(TicketProvider.JIRA, result)
    }

    @Test
    fun `fromString returns GITHUB for GITHUB string`() {
        val result = TicketProvider.fromString("GITHUB")
        assertEquals(TicketProvider.GITHUB, result)
    }

    @Test
    fun `fromString returns GITLAB for GITLAB string`() {
        val result = TicketProvider.fromString("GITLAB")
        assertEquals(TicketProvider.GITLAB, result)
    }

    // ============================================================
    // fromString() Tests - Case insensitive
    // ============================================================

    @Test
    fun `fromString is case insensitive for jira lowercase`() {
        val result = TicketProvider.fromString("jira")
        assertEquals(TicketProvider.JIRA, result)
    }

    @Test
    fun `fromString is case insensitive for Jira mixed case`() {
        val result = TicketProvider.fromString("Jira")
        assertEquals(TicketProvider.JIRA, result)
    }

    @Test
    fun `fromString is case insensitive for github lowercase`() {
        val result = TicketProvider.fromString("github")
        assertEquals(TicketProvider.GITHUB, result)
    }

    @Test
    fun `fromString is case insensitive for GitHub mixed case`() {
        val result = TicketProvider.fromString("GitHub")
        assertEquals(TicketProvider.GITHUB, result)
    }

    @Test
    fun `fromString is case insensitive for gitlab lowercase`() {
        val result = TicketProvider.fromString("gitlab")
        assertEquals(TicketProvider.GITLAB, result)
    }

    @Test
    fun `fromString is case insensitive for GitLab mixed case`() {
        val result = TicketProvider.fromString("GitLab")
        assertEquals(TicketProvider.GITLAB, result)
    }

    // ============================================================
    // fromString() Tests - Invalid values
    // ============================================================

    @Test
    fun `fromString returns null for unknown provider`() {
        val result = TicketProvider.fromString("unknown")
        assertNull(result)
    }

    @Test
    fun `fromString returns null for empty string`() {
        val result = TicketProvider.fromString("")
        assertNull(result)
    }

    @Test
    fun `fromString returns null for blank string`() {
        val result = TicketProvider.fromString("   ")
        assertNull(result)
    }

    @Test
    fun `fromString returns null for partial match`() {
        val result = TicketProvider.fromString("JIR")
        assertNull(result)
    }

    @Test
    fun `fromString returns null for similar but wrong name`() {
        val result = TicketProvider.fromString("JIRAS")
        assertNull(result)
    }

    @Test
    fun `fromString returns null for displayName`() {
        // Should not match on displayName, only on enum name
        val result = TicketProvider.fromString("GitHub Issues")
        assertNull(result)
    }

    // ============================================================
    // name property Tests
    // ============================================================

    @Test
    fun `JIRA name property is JIRA`() {
        assertEquals("JIRA", TicketProvider.JIRA.name)
    }

    @Test
    fun `GITHUB name property is GITHUB`() {
        assertEquals("GITHUB", TicketProvider.GITHUB.name)
    }

    @Test
    fun `GITLAB name property is GITLAB`() {
        assertEquals("GITLAB", TicketProvider.GITLAB.name)
    }

    // ============================================================
    // Round-trip Tests
    // ============================================================

    @Test
    fun `fromString round trip works for all providers`() {
        TicketProvider.entries.forEach { provider ->
            val parsed = TicketProvider.fromString(provider.name)
            assertEquals(provider, parsed, "Round-trip failed for ${provider.name}")
        }
    }
}
