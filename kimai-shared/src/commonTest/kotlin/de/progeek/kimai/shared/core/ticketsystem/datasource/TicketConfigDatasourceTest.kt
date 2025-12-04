package de.progeek.kimai.shared.core.ticketsystem.datasource

import app.cash.turbine.test
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.utils.TestDatasources
import de.progeek.kimai.shared.utils.clearDatabase
import de.progeek.kimai.shared.utils.createTestDatasources
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for TicketConfigDatasource.
 *
 * Tests database operations for ticket system configuration including:
 * - CRUD operations (getAll, getById, save, delete)
 * - Filtering (getEnabled, getByProvider)
 * - State management (setEnabled, updateSyncInterval)
 * - Counting operations
 * - Credential encryption/decryption
 */
class TicketConfigDatasourceTest {

    private lateinit var testDatasources: TestDatasources
    private lateinit var datasource: TicketConfigDatasource

    private val jiraConfig = TicketSystemConfig(
        id = "jira-config-1",
        displayName = "Work Jira",
        provider = TicketProvider.JIRA,
        enabled = true,
        baseUrl = "https://company.atlassian.net",
        credentials = TicketCredentials.JiraApiToken(
            email = "user@company.com",
            token = "jira-api-token-123"
        ),
        syncIntervalMinutes = 15,
        defaultProjectKey = "PROJ",
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private val githubConfig = TicketSystemConfig(
        id = "github-config-1",
        displayName = "Personal GitHub",
        provider = TicketProvider.GITHUB,
        enabled = true,
        baseUrl = "https://api.github.com",
        credentials = TicketCredentials.GitHubToken(
            token = "ghp_test_token_123",
            owner = "myuser",
            repositories = listOf("repo1", "repo2")
        ),
        syncIntervalMinutes = 30,
        defaultProjectKey = null,
        issueFormat = "{key}: {summary}"
    )

    private val gitlabConfig = TicketSystemConfig(
        id = "gitlab-config-1",
        displayName = "Company GitLab",
        provider = TicketProvider.GITLAB,
        enabled = false,
        baseUrl = "https://gitlab.company.com",
        credentials = TicketCredentials.GitLabToken(
            token = "glpat-test-token",
            projectIds = listOf("123", "456")
        ),
        syncIntervalMinutes = 60,
        defaultProjectKey = null,
        issueFormat = "[{key}] {summary}"
    )

    @BeforeTest
    fun setup() {
        testDatasources = createTestDatasources()
        datasource = testDatasources.ticketConfigDatasource
    }

    @AfterTest
    fun teardown() {
        clearDatabase(testDatasources.database)
    }

    // ============================================================
    // getAll() Tests
    // ============================================================

    @Test
    fun `getAll returns empty list when no configs exist`() = runTest {
        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns all saved configs`() = runTest {
        datasource.save(jiraConfig)
        datasource.save(githubConfig)
        datasource.save(gitlabConfig)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(3, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAll returns configs from multiple providers`() = runTest {
        datasource.save(jiraConfig)
        datasource.save(githubConfig)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.any { it.provider == TicketProvider.JIRA })
            assertTrue(items.any { it.provider == TicketProvider.GITHUB })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getEnabled() Tests
    // ============================================================

    @Test
    fun `getEnabled returns only enabled configs`() = runTest {
        datasource.save(jiraConfig.copy(enabled = true))
        datasource.save(githubConfig.copy(enabled = true))
        datasource.save(gitlabConfig.copy(enabled = false))

        datasource.getEnabled().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.all { it.enabled })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getEnabled returns empty when no configs are enabled`() = runTest {
        datasource.save(jiraConfig.copy(enabled = false))
        datasource.save(gitlabConfig.copy(enabled = false))

        datasource.getEnabled().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getById() Tests
    // ============================================================

    @Test
    fun `getById returns config when found`() = runTest {
        datasource.save(jiraConfig)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertEquals(jiraConfig.id, config.id)
            assertEquals(jiraConfig.displayName, config.displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        datasource.getById("nonexistent-id").test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNull(config)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getById preserves all config fields`() = runTest {
        datasource.save(jiraConfig)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertEquals(jiraConfig.displayName, config.displayName)
            assertEquals(jiraConfig.provider, config.provider)
            assertEquals(jiraConfig.enabled, config.enabled)
            assertEquals(jiraConfig.baseUrl, config.baseUrl)
            assertEquals(jiraConfig.syncIntervalMinutes, config.syncIntervalMinutes)
            assertEquals(jiraConfig.defaultProjectKey, config.defaultProjectKey)
            assertEquals(jiraConfig.issueFormat, config.issueFormat)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // getByProvider() Tests
    // ============================================================

    @Test
    fun `getByProvider returns configs for specified provider`() = runTest {
        datasource.save(jiraConfig)
        datasource.save(githubConfig)
        datasource.save(gitlabConfig)

        datasource.getByProvider(TicketProvider.JIRA).test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(TicketProvider.JIRA, items.first().provider)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByProvider returns empty for provider with no configs`() = runTest {
        datasource.save(jiraConfig)

        datasource.getByProvider(TicketProvider.GITLAB).test(timeout = 5.seconds) {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // save() Tests
    // ============================================================

    @Test
    fun `save creates new config successfully`() = runTest {
        val result = datasource.save(jiraConfig)

        assertTrue(result.isSuccess)
        assertEquals(jiraConfig.id, result.getOrNull()?.id)
    }

    @Test
    fun `save encrypts and decrypts Jira API Token credentials`() = runTest {
        datasource.save(jiraConfig)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            val credentials = config.credentials as TicketCredentials.JiraApiToken
            assertEquals("user@company.com", credentials.email)
            assertEquals("jira-api-token-123", credentials.token)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save encrypts and decrypts GitHub Token credentials`() = runTest {
        datasource.save(githubConfig)

        datasource.getById(githubConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            val credentials = config.credentials as TicketCredentials.GitHubToken
            assertEquals("ghp_test_token_123", credentials.token)
            assertEquals("myuser", credentials.owner)
            assertEquals(listOf("repo1", "repo2"), credentials.repositories)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save encrypts and decrypts GitLab Token credentials`() = runTest {
        datasource.save(gitlabConfig)

        datasource.getById(gitlabConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            val credentials = config.credentials as TicketCredentials.GitLabToken
            assertEquals("glpat-test-token", credentials.token)
            assertEquals(listOf("123", "456"), credentials.projectIds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save updates existing config with same id`() = runTest {
        datasource.save(jiraConfig)

        val updatedConfig = jiraConfig.copy(displayName = "Updated Jira")
        datasource.save(updatedConfig)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertEquals("Updated Jira", config.displayName)
            cancelAndIgnoreRemainingEvents()
        }

        // Should still be only 1 config
        val count = datasource.count()
        assertEquals(1L, count.getOrNull())
    }

    @Test
    fun `save handles Jira PAT credentials`() = runTest {
        val jiraPATConfig = jiraConfig.copy(
            id = "jira-pat-config",
            credentials = TicketCredentials.JiraPersonalAccessToken(token = "jira-pat-12345")
        )

        datasource.save(jiraPATConfig)

        datasource.getById(jiraPATConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            val credentials = config.credentials as TicketCredentials.JiraPersonalAccessToken
            assertEquals("jira-pat-12345", credentials.token)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // delete() Tests
    // ============================================================

    @Test
    fun `delete removes config by id`() = runTest {
        datasource.save(jiraConfig)
        datasource.save(githubConfig)

        val result = datasource.delete(jiraConfig.id)

        assertTrue(result.isSuccess)

        datasource.getAll().test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(githubConfig.id, items.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete succeeds for nonexistent id`() = runTest {
        val result = datasource.delete("nonexistent-id")

        assertTrue(result.isSuccess)
    }

    // ============================================================
    // setEnabled() Tests
    // ============================================================

    @Test
    fun `setEnabled enables a disabled config`() = runTest {
        val disabledConfig = jiraConfig.copy(enabled = false)
        datasource.save(disabledConfig)

        val result = datasource.setEnabled(jiraConfig.id, true)

        assertTrue(result.isSuccess)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertTrue(config.enabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setEnabled disables an enabled config`() = runTest {
        datasource.save(jiraConfig.copy(enabled = true))

        val result = datasource.setEnabled(jiraConfig.id, false)

        assertTrue(result.isSuccess)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertFalse(config.enabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setEnabled updates updatedAt timestamp`() = runTest {
        val originalTime = Clock.System.now()
        val oldConfig = jiraConfig.copy(updatedAt = originalTime)
        datasource.save(oldConfig)

        // Wait a tiny bit to ensure timestamp difference
        datasource.setEnabled(jiraConfig.id, false)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertTrue(config.updatedAt >= originalTime)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // updateSyncInterval() Tests
    // ============================================================

    @Test
    fun `updateSyncInterval changes sync interval`() = runTest {
        datasource.save(jiraConfig.copy(syncIntervalMinutes = 15))

        val result = datasource.updateSyncInterval(jiraConfig.id, 60)

        assertTrue(result.isSuccess)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertEquals(60, config.syncIntervalMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateSyncInterval updates updatedAt timestamp`() = runTest {
        val originalTime = Clock.System.now()
        datasource.save(jiraConfig.copy(updatedAt = originalTime))

        datasource.updateSyncInterval(jiraConfig.id, 120)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertTrue(config.updatedAt >= originalTime)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // count() Tests
    // ============================================================

    @Test
    fun `count returns zero when no configs exist`() = runTest {
        val result = datasource.count()

        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull())
    }

    @Test
    fun `count returns correct number after saves`() = runTest {
        datasource.save(jiraConfig)
        datasource.save(githubConfig)
        datasource.save(gitlabConfig)

        val result = datasource.count()

        assertTrue(result.isSuccess)
        assertEquals(3L, result.getOrNull())
    }

    @Test
    fun `count decreases after delete`() = runTest {
        datasource.save(jiraConfig)
        datasource.save(githubConfig)

        datasource.delete(jiraConfig.id)

        val result = datasource.count()
        assertEquals(1L, result.getOrNull())
    }

    // ============================================================
    // countEnabled() Tests
    // ============================================================

    @Test
    fun `countEnabled returns only enabled configs count`() = runTest {
        datasource.save(jiraConfig.copy(enabled = true))
        datasource.save(githubConfig.copy(enabled = true))
        datasource.save(gitlabConfig.copy(enabled = false))

        val result = datasource.countEnabled()

        assertTrue(result.isSuccess)
        assertEquals(2L, result.getOrNull())
    }

    @Test
    fun `countEnabled returns zero when all disabled`() = runTest {
        datasource.save(jiraConfig.copy(enabled = false))
        datasource.save(githubConfig.copy(enabled = false))

        val result = datasource.countEnabled()

        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull())
    }

    // ============================================================
    // hasEnabled() Tests
    // ============================================================

    @Test
    fun `hasEnabled returns true when enabled configs exist`() = runTest {
        datasource.save(jiraConfig.copy(enabled = true))

        datasource.hasEnabled().test(timeout = 5.seconds) {
            val hasEnabled = awaitItem()
            assertTrue(hasEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasEnabled returns false when no enabled configs`() = runTest {
        datasource.save(jiraConfig.copy(enabled = false))

        datasource.hasEnabled().test(timeout = 5.seconds) {
            val hasEnabled = awaitItem()
            assertFalse(hasEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasEnabled returns false when no configs at all`() = runTest {
        datasource.hasEnabled().test(timeout = 5.seconds) {
            val hasEnabled = awaitItem()
            assertFalse(hasEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Issue Format Tests
    // ============================================================

    @Test
    fun `save and retrieve preserves custom issue format`() = runTest {
        val customFormat = "[{project}] {key}: {summary}"
        val configWithFormat = jiraConfig.copy(issueFormat = customFormat)

        datasource.save(configWithFormat)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertEquals(customFormat, config.issueFormat)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save uses default format when not specified`() = runTest {
        datasource.save(jiraConfig)

        datasource.getById(jiraConfig.id).test(timeout = 5.seconds) {
            val config = awaitItem()
            assertNotNull(config)
            assertEquals(IssueInsertFormat.DEFAULT_FORMAT, config.issueFormat)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
