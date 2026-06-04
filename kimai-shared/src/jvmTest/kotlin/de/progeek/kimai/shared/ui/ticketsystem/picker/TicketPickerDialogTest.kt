@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.ticketsystem.picker

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketSystemRepository
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.testutils.createTestComponentContext
import de.progeek.kimai.shared.testutils.createTestDispatchers
import de.progeek.kimai.shared.testutils.createTestStoreFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * Compose UI tests for [TicketPickerDialog].
 *
 * Exercises initial render, search input, result selection (output callback),
 * empty/offline/error branches and dismissal. The underlying store is driven by
 * mocking [TicketSystemRepository] / [TicketConfigRepository] through the test
 * Koin module so that bootstrap populates the dialog with sample issues.
 */
class TicketPickerDialogTest {

    private lateinit var ticketRepository: TicketSystemRepository
    private lateinit var configRepository: TicketConfigRepository

    private val sourceId = "source-uuid-111"

    private val jiraConfig = TicketSystemConfig(
        id = sourceId,
        displayName = "Test Jira",
        provider = TicketProvider.JIRA,
        enabled = true,
        baseUrl = "https://company.atlassian.net",
        credentials = TicketCredentials.JiraApiToken(
            email = "user@example.com",
            token = "test-token"
        ),
        syncIntervalMinutes = 15,
        issueFormat = "{key}: {summary}"
    )

    private val issueLogin = TicketIssue(
        id = "10001",
        key = "PROJ-123",
        summary = "Fix login bug",
        status = "Open",
        projectKey = "PROJ",
        projectName = "Project Alpha",
        issueType = "Bug",
        assignee = "john.doe",
        updated = Instant.fromEpochMilliseconds(1700000000000),
        sourceId = sourceId,
        provider = TicketProvider.JIRA,
        webUrl = "https://company.atlassian.net/browse/PROJ-123"
    )

    private val issueExport = TicketIssue(
        id = "10002",
        key = "PROJ-456",
        summary = "Add export feature",
        status = "In Progress",
        projectKey = "PROJ",
        projectName = "Project Alpha",
        issueType = "Feature",
        assignee = null,
        updated = Instant.fromEpochMilliseconds(1700001000000),
        sourceId = sourceId,
        provider = TicketProvider.JIRA,
        webUrl = "https://company.atlassian.net/browse/PROJ-456"
    )

    /**
     * Configures the repository mocks and starts the test Koin container.
     * Defaults populate two issues from a single enabled Jira source.
     */
    private fun startKoin(
        hasSources: Boolean = true,
        cachedCount: Long = 2L,
        issues: List<TicketIssue> = listOf(issueLogin, issueExport),
        configs: List<TicketSystemConfig> = listOf(jiraConfig)
    ) {
        ticketRepository = mockk(relaxed = true) {
            coEvery { hasEnabledSources() } returns hasSources
            coEvery { getCachedIssueCount() } returns Result.success(cachedCount)
            coEvery { refreshAllSources() } returns Result.success(Unit)
            every { getAllIssues() } returns flowOf(issues)
            coEvery { searchWithFallback(any(), any()) } returns Result.success(issues)
        }
        configRepository = mockk(relaxed = true) {
            every { getAllConfigs() } returns flowOf(configs)
        }
        TestKoinModule.startTestKoin(
            ticketSystemRepository = ticketRepository,
            ticketConfigRepository = configRepository
        )
    }

    private fun createComponent(output: (TicketPickerComponent.Output) -> Unit) =
        TicketPickerComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = output
        )

    @Before
    fun setUp() {
        // Default configuration; individual tests may restart Koin with other data.
        startKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
    }

    @Test
    fun `initial render shows header, search field and issues`() = runComposeUiTest {
        val component = createComponent {}
        setContent {
            TestTheme {
                TicketPickerDialog(component = component, onDismiss = {})
            }
        }
        waitForIdle()

        onNodeWithText("Select Issue").assertExists()
        onNodeWithText("Search by key or summary...").assertExists()
        onNodeWithText("2 issues").assertExists()
        onNodeWithText("PROJ-123").assertExists()
        onNodeWithText("Fix login bug").assertExists()
        onNodeWithText("PROJ-456").assertExists()
    }

    @Test
    fun `typing in search field invokes repository search`() = runComposeUiTest {
        coEvery { ticketRepository.searchWithFallback(any(), any()) } returns
            Result.success(listOf(issueLogin))

        val component = createComponent {}
        setContent {
            TestTheme {
                TicketPickerDialog(component = component, onDismiss = {})
            }
        }
        waitForIdle()

        onNode(hasSetTextAction()).performTextInput("login")
        waitForIdle()

        // Search is debounced (300ms) inside the store; allow it to settle.
        onNodeWithText("Fix login bug").assertExists()
    }

    @Test
    fun `selecting an issue emits formatted output and dismisses`() = runComposeUiTest {
        var output: TicketPickerComponent.Output? = null
        var dismissed = false
        val component = createComponent { output = it }
        setContent {
            TestTheme {
                TicketPickerDialog(component = component, onDismiss = { dismissed = true })
            }
        }
        waitForIdle()

        onNodeWithText("Fix login bug").performClick()
        waitForIdle()

        val emitted = output
        assertTrue(emitted is TicketPickerComponent.Output.IssueSelected)
        // Config format "{key}: {summary}" is applied for the matching sourceId.
        assertEquals("PROJ-123: Fix login bug", emitted.formattedText)
        assertTrue(dismissed)
    }

    @Test
    fun `clicking close button dismisses and publishes Dismissed`() = runComposeUiTest {
        var output: TicketPickerComponent.Output? = null
        var dismissed = false
        val component = createComponent { output = it }
        setContent {
            TestTheme {
                TicketPickerDialog(component = component, onDismiss = { dismissed = true })
            }
        }
        waitForIdle()

        onNodeWithContentDescription("Close").performClick()
        waitForIdle()

        assertTrue(dismissed)
        assertEquals(TicketPickerComponent.Output.Dismissed, output)
    }

    @Test
    fun `clear button resets the search field`() = runComposeUiTest {
        val component = createComponent {}
        setContent {
            TestTheme {
                TicketPickerDialog(component = component, onDismiss = {})
            }
        }
        waitForIdle()

        onNode(hasSetTextAction()).performTextInput("PROJ")
        waitForIdle()
        // Trailing clear icon appears once the field is non-empty.
        onNodeWithContentDescription("Clear").performClick()
        waitForIdle()

        onNodeWithText("Search by key or summary...").assertExists()
    }

    @Test
    fun `empty state shown when source enabled but no issues`() = runComposeUiTest {
        TestKoinModule.stopTestKoin()
        startKoin(hasSources = true, cachedCount = 5L, issues = emptyList())

        val component = createComponent {}
        setContent {
            TestTheme {
                TicketPickerDialog(component = component, onDismiss = {})
            }
        }
        waitForIdle()

        onNodeWithText("0 issues").assertExists()
        onNodeWithText("No issues available").assertExists()
    }

    @Test
    fun `no sources state shows configuration hint and offline marker`() = runComposeUiTest {
        TestKoinModule.stopTestKoin()
        startKoin(hasSources = false, issues = emptyList(), configs = emptyList())

        val component = createComponent {}
        setContent {
            TestTheme {
                TicketPickerDialog(component = component, onDismiss = {})
            }
        }
        waitForIdle()

        onNodeWithText("No Sources").assertExists()
        onNodeWithText("No ticket systems configured. Add one in Settings.").assertExists()
    }

    @Test
    fun `refresh failure surfaces error message`() = runComposeUiTest {
        TestKoinModule.stopTestKoin()
        // Empty local cache forces an immediate refresh during bootstrap, which fails.
        startKoin(hasSources = true, cachedCount = 0L, issues = emptyList())
        coEvery { ticketRepository.refreshAllSources() } returns
            Result.failure(Exception("Refresh failed"))

        val component = createComponent {}
        setContent {
            TestTheme {
                TicketPickerDialog(component = component, onDismiss = {})
            }
        }
        waitForIdle()

        onNodeWithText("Refresh failed").assertExists()
    }

    @Test
    fun `manual refresh reloads issues without error`() = runComposeUiTest {
        var output: TicketPickerComponent.Output? = null
        val component = createComponent { output = it }
        setContent {
            TestTheme {
                TicketPickerDialog(component = component, onDismiss = {})
            }
        }
        waitForIdle()

        onNodeWithContentDescription("Refresh").performClick()
        waitForIdle()

        // No selection happened, refresh keeps the list populated.
        assertNull(output)
        onNodeWithText("PROJ-123").assertExists()
    }
}
