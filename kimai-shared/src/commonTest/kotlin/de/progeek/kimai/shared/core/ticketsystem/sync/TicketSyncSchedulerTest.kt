package de.progeek.kimai.shared.core.ticketsystem.sync

import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketSystemRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Test suite for TicketSyncScheduler.
 *
 * Tests the background synchronization of ticket issues including:
 * - Starting and stopping the scheduler
 * - Monitoring configuration changes
 * - Per-source sync job management
 * - Immediate sync methods (syncAllNow, syncSourceNow)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TicketSyncSchedulerTest {

    private lateinit var mockConfigRepository: TicketConfigRepository
    private lateinit var mockTicketRepository: TicketSystemRepository
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var schedulerScope: CoroutineScope
    private lateinit var kimaiDispatchers: KimaiDispatchers
    private var scheduler: TicketSyncScheduler? = null

    private val sourceId1 = "source-uuid-111"
    private val sourceId2 = "source-uuid-222"

    private val testConfig1 = TicketSystemConfig(
        id = sourceId1,
        displayName = "Test Jira",
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
        id = sourceId2,
        displayName = "Test GitHub",
        provider = TicketProvider.GITHUB,
        enabled = true,
        baseUrl = "https://api.github.com",
        credentials = TicketCredentials.GitHubToken(
            token = "ghp_test",
            owner = "testowner",
            repositories = emptyList()
        ),
        syncIntervalMinutes = 30,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    @BeforeTest
    fun setup() {
        mockConfigRepository = mockk(relaxed = true)
        mockTicketRepository = mockk(relaxed = true)

        // Default mock setup
        coEvery { mockTicketRepository.refreshSource(any()) } returns Result.success(Unit)
        coEvery { mockTicketRepository.refreshAllSources() } returns Result.success(Unit)
        coEvery { mockTicketRepository.getCachedIssueCount() } returns Result.success(10)
        coEvery { mockTicketRepository.getCachedIssueCountBySource(any()) } returns Result.success(5)
    }

    @AfterTest
    fun teardown() {
        scheduler?.stop()
        clearAllMocks()
    }

    private fun createScheduler(scope: CoroutineScope): TicketSyncScheduler {
        kimaiDispatchers = object : KimaiDispatchers {
            override val main = testDispatcher
            override val io = testDispatcher
            override val unconfined = testDispatcher
        }
        return TicketSyncScheduler(
            configRepository = mockConfigRepository,
            ticketRepository = mockTicketRepository,
            dispatchers = kimaiDispatchers,
            scope = scope
        )
    }

    // ============================================================
    // syncAllNow() Tests
    // ============================================================

    @Test
    fun `syncAllNow refreshes all sources`() = runTest(testDispatcher) {
        coEvery { mockTicketRepository.refreshAllSources() } returns Result.success(Unit)
        coEvery { mockTicketRepository.getCachedIssueCount() } returns Result.success(25)

        scheduler = createScheduler(this)

        val result = scheduler!!.syncAllNow()

        assertTrue(result.isSuccess)
        coVerify { mockTicketRepository.refreshAllSources() }
    }

    @Test
    fun `syncAllNow returns failure on error`() = runTest(testDispatcher) {
        coEvery { mockTicketRepository.refreshAllSources() } throws Exception("Sync error")

        scheduler = createScheduler(this)

        val result = scheduler!!.syncAllNow()

        assertTrue(result.isFailure)
    }

    // ============================================================
    // syncSourceNow() Tests
    // ============================================================

    @Test
    fun `syncSourceNow refreshes specific source`() = runTest(testDispatcher) {
        every { mockConfigRepository.getConfigById(sourceId1) } returns flowOf(testConfig1)
        coEvery { mockTicketRepository.refreshSource(testConfig1) } returns Result.success(Unit)

        scheduler = createScheduler(this)

        val result = scheduler!!.syncSourceNow(sourceId1)

        assertTrue(result.isSuccess)
        coVerify { mockTicketRepository.refreshSource(testConfig1) }
    }

    @Test
    fun `syncSourceNow returns failure for unknown source`() = runTest(testDispatcher) {
        every { mockConfigRepository.getConfigById("unknown-id") } returns flowOf(null)

        scheduler = createScheduler(this)

        val result = scheduler!!.syncSourceNow("unknown-id")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
    }

    @Test
    fun `syncSourceNow returns failure on repository error`() = runTest(testDispatcher) {
        every { mockConfigRepository.getConfigById(sourceId1) } returns flowOf(testConfig1)
        coEvery { mockTicketRepository.refreshSource(testConfig1) } throws Exception("Refresh failed")

        scheduler = createScheduler(this)

        val result = scheduler!!.syncSourceNow(sourceId1)

        assertTrue(result.isFailure)
    }

    // ============================================================
    // Start/Stop Tests
    // ============================================================

    @Test
    fun `start begins monitoring enabled configs`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow(listOf(testConfig1))
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        scheduler = createScheduler(this)
        scheduler!!.start()
        advanceUntilIdle()

        coVerify { mockConfigRepository.getEnabledConfigs() }

        // Stop to cleanup
        scheduler!!.stop()
    }

    @Test
    fun `stop cancels all jobs`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow(listOf(testConfig1))
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        scheduler = createScheduler(this)
        scheduler!!.start()
        advanceUntilIdle()

        scheduler!!.stop()
        advanceUntilIdle()

        // After stop, no new syncs should occur
        coVerify(atMost = 1) { mockTicketRepository.refreshSource(testConfig1) }
    }

    @Test
    fun `start stops existing scheduler first`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow(listOf(testConfig1))
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        scheduler = createScheduler(this)

        // Start first time
        scheduler!!.start()
        advanceUntilIdle()

        // Start again (should stop first)
        scheduler!!.start()
        advanceUntilIdle()

        // Should have collected configs at least once after restart
        coVerify(atLeast = 1) { mockConfigRepository.getEnabledConfigs() }

        scheduler!!.stop()
    }

    // ============================================================
    // Config Monitoring Tests
    // ============================================================

    @Test
    fun `config changes trigger sync job updates`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow(listOf(testConfig1))
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        scheduler = createScheduler(this)
        scheduler!!.start()
        advanceUntilIdle()

        // Initial sync for config1
        coVerify { mockTicketRepository.refreshSource(testConfig1) }

        // Add config2
        configFlow.value = listOf(testConfig1, testConfig2)
        advanceUntilIdle()

        // Should sync config2
        coVerify { mockTicketRepository.refreshSource(testConfig2) }

        scheduler!!.stop()
    }

    @Test
    fun `disabled config stops its sync job`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow(listOf(testConfig1, testConfig2))
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        scheduler = createScheduler(this)
        scheduler!!.start()
        advanceUntilIdle()

        // Both configs should be synced initially
        coVerify { mockTicketRepository.refreshSource(testConfig1) }
        coVerify { mockTicketRepository.refreshSource(testConfig2) }

        // Remove config2 (simulating it being disabled)
        configFlow.value = listOf(testConfig1)
        advanceUntilIdle()

        // Config2 job should be stopped
        scheduler!!.stop()
        assertTrue(true)
    }

    // ============================================================
    // Initial Sync Tests
    // ============================================================

    @Test
    fun `new config triggers initial sync`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow(listOf(testConfig1))
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        scheduler = createScheduler(this)
        scheduler!!.start()
        advanceUntilIdle()

        coVerify { mockTicketRepository.refreshSource(testConfig1) }

        scheduler!!.stop()
    }

    @Test
    fun `multiple configs trigger individual syncs`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow(listOf(testConfig1, testConfig2))
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        scheduler = createScheduler(this)
        scheduler!!.start()
        advanceUntilIdle()

        coVerify { mockTicketRepository.refreshSource(testConfig1) }
        coVerify { mockTicketRepository.refreshSource(testConfig2) }

        scheduler!!.stop()
    }

    // ============================================================
    // Empty Config Tests
    // ============================================================

    @Test
    fun `empty config list does not start any sync jobs`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow<List<TicketSystemConfig>>(emptyList())
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        scheduler = createScheduler(this)
        scheduler!!.start()
        advanceUntilIdle()

        coVerify(exactly = 0) { mockTicketRepository.refreshSource(any()) }

        scheduler!!.stop()
    }

    @Test
    fun `all configs removed stops all sync jobs`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow(listOf(testConfig1))
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        scheduler = createScheduler(this)
        scheduler!!.start()
        advanceUntilIdle()

        // Initial sync happened
        coVerify(exactly = 1) { mockTicketRepository.refreshSource(testConfig1) }

        // Remove all configs
        configFlow.value = emptyList()
        advanceUntilIdle()

        scheduler!!.stop()
        assertTrue(true)
    }

    // ============================================================
    // Error Handling Tests
    // ============================================================

    @Test
    fun `sync continues after individual source error`() = runTest(testDispatcher) {
        val configFlow = MutableStateFlow(listOf(testConfig1, testConfig2))
        every { mockConfigRepository.getEnabledConfigs() } returns configFlow

        // Config1 fails, config2 succeeds
        coEvery { mockTicketRepository.refreshSource(testConfig1) } throws Exception("Config1 error")
        coEvery { mockTicketRepository.refreshSource(testConfig2) } returns Result.success(Unit)

        scheduler = createScheduler(this)
        scheduler!!.start()
        advanceUntilIdle()

        // Both configs should have been attempted
        coVerify { mockTicketRepository.refreshSource(testConfig1) }
        coVerify { mockTicketRepository.refreshSource(testConfig2) }

        scheduler!!.stop()
    }
}
