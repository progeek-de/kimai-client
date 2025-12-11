package de.progeek.kimai.shared.core.ticketsystem.repository

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.utils.createTestDatasources
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * Test suite for TicketConfigRepository.
 *
 * Tests cover:
 * - CRUD operations for configurations
 * - deleteConfig() coordinated deletion (issues then config)
 * - Flow-based reactive data access
 */
class TicketConfigRepositoryTest {

    private lateinit var repository: TicketConfigRepository
    private lateinit var testDatasources: de.progeek.kimai.shared.utils.TestDatasources

    private val testConfig1 = TicketSystemConfig(
        id = "config-1",
        displayName = "Jira Config",
        provider = TicketProvider.JIRA,
        enabled = true,
        baseUrl = "https://company.atlassian.net",
        credentials = TicketCredentials.JiraApiToken(
            email = "user@example.com",
            token = "test-token"
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private val testConfig2 = TicketSystemConfig(
        id = "config-2",
        displayName = "GitHub Config",
        provider = TicketProvider.GITHUB,
        enabled = false,
        baseUrl = "https://api.github.com",
        credentials = TicketCredentials.GitHubToken(
            token = "ghp_token",
            owner = "owner",
            repositories = emptyList()
        ),
        syncIntervalMinutes = 30,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private val testIssue1 = TicketIssue(
        id = "issue-1",
        key = "PROJ-123",
        summary = "Test Issue 1",
        status = "Open",
        projectKey = "PROJ",
        projectName = "Project",
        issueType = "Bug",
        assignee = null,
        updated = Instant.parse("2024-01-15T10:00:00Z"),
        sourceId = "config-1",
        provider = TicketProvider.JIRA,
        webUrl = "https://company.atlassian.net/browse/PROJ-123"
    )

    private val testIssue2 = TicketIssue(
        id = "issue-2",
        key = "PROJ-456",
        summary = "Test Issue 2",
        status = "Closed",
        projectKey = "PROJ",
        projectName = "Project",
        issueType = "Task",
        assignee = "user",
        updated = Instant.parse("2024-01-16T10:00:00Z"),
        sourceId = "config-1",
        provider = TicketProvider.JIRA,
        webUrl = "https://company.atlassian.net/browse/PROJ-456"
    )

    @BeforeTest
    fun setup() {
        testDatasources = createTestDatasources()
        repository = TicketConfigRepository(
            configDatasource = testDatasources.ticketConfigDatasource,
            issueDatasource = testDatasources.ticketIssueDatasource
        )
    }

    @AfterTest
    fun teardown() {
        de.progeek.kimai.shared.utils.clearDatabase(testDatasources.database)
    }

    // ============================================================
    // saveConfig() and getAllConfigs() Tests
    // ============================================================

    @Test
    fun `saveConfig saves new config successfully`() = runTest {
        val result = repository.saveConfig(testConfig1)

        assertTrue(result.isSuccess)
        val configs = repository.getAllConfigs().first()
        assertEquals(1, configs.size)
        assertEquals(testConfig1.id, configs[0].id)
    }

    @Test
    fun `getAllConfigs returns all saved configs`() = runTest {
        repository.saveConfig(testConfig1)
        repository.saveConfig(testConfig2)

        val configs = repository.getAllConfigs().first()

        assertEquals(2, configs.size)
    }

    @Test
    fun `getAllConfigs returns empty list when no configs`() = runTest {
        val configs = repository.getAllConfigs().first()

        assertTrue(configs.isEmpty())
    }

    // ============================================================
    // getEnabledConfigs() Tests
    // ============================================================

    @Test
    fun `getEnabledConfigs returns only enabled configs`() = runTest {
        repository.saveConfig(testConfig1) // enabled = true
        repository.saveConfig(testConfig2) // enabled = false

        val enabledConfigs = repository.getEnabledConfigs().first()

        assertEquals(1, enabledConfigs.size)
        assertEquals(testConfig1.id, enabledConfigs[0].id)
    }

    @Test
    fun `getEnabledConfigs returns empty list when all disabled`() = runTest {
        repository.saveConfig(testConfig2) // enabled = false

        val enabledConfigs = repository.getEnabledConfigs().first()

        assertTrue(enabledConfigs.isEmpty())
    }

    // ============================================================
    // getConfigById() Tests
    // ============================================================

    @Test
    fun `getConfigById returns config when exists`() = runTest {
        repository.saveConfig(testConfig1)

        val config = repository.getConfigById(testConfig1.id).first()

        assertNotNull(config)
        assertEquals(testConfig1.id, config.id)
        assertEquals(testConfig1.displayName, config.displayName)
    }

    @Test
    fun `getConfigById returns null when not exists`() = runTest {
        val config = repository.getConfigById("non-existent-id").first()

        assertNull(config)
    }

    // ============================================================
    // getConfigsByProvider() Tests
    // ============================================================

    @Test
    fun `getConfigsByProvider returns configs for specific provider`() = runTest {
        repository.saveConfig(testConfig1) // JIRA
        repository.saveConfig(testConfig2) // GITHUB

        val jiraConfigs = repository.getConfigsByProvider(TicketProvider.JIRA).first()

        assertEquals(1, jiraConfigs.size)
        assertEquals(TicketProvider.JIRA, jiraConfigs[0].provider)
    }

    @Test
    fun `getConfigsByProvider returns empty list for unused provider`() = runTest {
        repository.saveConfig(testConfig1) // JIRA

        val gitlabConfigs = repository.getConfigsByProvider(TicketProvider.GITLAB).first()

        assertTrue(gitlabConfigs.isEmpty())
    }

    // ============================================================
    // deleteConfig() Tests - CRITICAL: Coordinated Deletion
    // ============================================================

    @Test
    fun `deleteConfig removes config`() = runTest {
        repository.saveConfig(testConfig1)

        val result = repository.deleteConfig(testConfig1.id)

        assertTrue(result.isSuccess)
        val configs = repository.getAllConfigs().first()
        assertTrue(configs.isEmpty())
    }

    @Test
    fun `deleteConfig removes associated issues before config`() = runTest {
        // Save config and issues
        repository.saveConfig(testConfig1)
        testDatasources.ticketIssueDatasource.insert(listOf(testIssue1, testIssue2))

        // Verify issues exist
        val issuesBefore = testDatasources.ticketIssueDatasource.getBySource(testConfig1.id).first()
        assertEquals(2, issuesBefore.size)

        // Delete config
        val result = repository.deleteConfig(testConfig1.id)

        // Verify both config and issues are deleted
        assertTrue(result.isSuccess)
        val configs = repository.getAllConfigs().first()
        assertTrue(configs.isEmpty())

        val issuesAfter = testDatasources.ticketIssueDatasource.getBySource(testConfig1.id).first()
        assertTrue(issuesAfter.isEmpty())
    }

    @Test
    fun `deleteConfig only removes issues for specific source`() = runTest {
        // Save two configs
        repository.saveConfig(testConfig1)
        repository.saveConfig(testConfig2)

        // Add issues for both sources
        testDatasources.ticketIssueDatasource.insert(listOf(testIssue1, testIssue2))

        val issueForConfig2 = testIssue1.copy(
            id = "issue-3",
            key = "#100",
            sourceId = "config-2",
            provider = TicketProvider.GITHUB
        )
        testDatasources.ticketIssueDatasource.insert(listOf(issueForConfig2))

        // Delete config1
        repository.deleteConfig(testConfig1.id)

        // Config2's issues should remain
        val remainingIssues = testDatasources.ticketIssueDatasource.getBySource(testConfig2.id).first()
        assertEquals(1, remainingIssues.size)
        assertEquals("issue-3", remainingIssues[0].id)
    }

    // ============================================================
    // setEnabled() Tests
    // ============================================================

    @Test
    fun `setEnabled enables disabled config`() = runTest {
        repository.saveConfig(testConfig2) // enabled = false

        val result = repository.setEnabled(testConfig2.id, true)

        assertTrue(result.isSuccess)
        val config = repository.getConfigById(testConfig2.id).first()
        assertTrue(config?.enabled == true)
    }

    @Test
    fun `setEnabled disables enabled config`() = runTest {
        repository.saveConfig(testConfig1) // enabled = true

        val result = repository.setEnabled(testConfig1.id, false)

        assertTrue(result.isSuccess)
        val config = repository.getConfigById(testConfig1.id).first()
        assertFalse(config?.enabled == true)
    }

    // ============================================================
    // updateSyncInterval() Tests
    // ============================================================

    @Test
    fun `updateSyncInterval updates interval`() = runTest {
        repository.saveConfig(testConfig1) // syncIntervalMinutes = 15

        val result = repository.updateSyncInterval(testConfig1.id, 60)

        assertTrue(result.isSuccess)
        val config = repository.getConfigById(testConfig1.id).first()
        assertEquals(60, config?.syncIntervalMinutes)
    }

    // ============================================================
    // count() and countEnabled() Tests
    // ============================================================

    @Test
    fun `count returns total number of configs`() = runTest {
        repository.saveConfig(testConfig1)
        repository.saveConfig(testConfig2)

        val result = repository.count()

        assertTrue(result.isSuccess)
        assertEquals(2L, result.getOrNull())
    }

    @Test
    fun `count returns zero when no configs`() = runTest {
        val result = repository.count()

        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull())
    }

    @Test
    fun `countEnabled returns only enabled configs count`() = runTest {
        repository.saveConfig(testConfig1) // enabled = true
        repository.saveConfig(testConfig2) // enabled = false

        val result = repository.countEnabled()

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    // ============================================================
    // hasEnabledConfigs() Tests
    // ============================================================

    @Test
    fun `hasEnabledConfigs returns true when enabled configs exist`() = runTest {
        repository.saveConfig(testConfig1) // enabled = true

        val hasEnabled = repository.hasEnabledConfigs().first()

        assertTrue(hasEnabled)
    }

    @Test
    fun `hasEnabledConfigs returns false when no enabled configs`() = runTest {
        repository.saveConfig(testConfig2) // enabled = false

        val hasEnabled = repository.hasEnabledConfigs().first()

        assertFalse(hasEnabled)
    }

    @Test
    fun `hasEnabledConfigs returns false when no configs at all`() = runTest {
        val hasEnabled = repository.hasEnabledConfigs().first()

        assertFalse(hasEnabled)
    }
}
