@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.timesheet.input.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.testutils.TestTheme
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class TicketSuggestionsTest {

    private fun issue(
        key: String = "PROJ-1",
        summary: String = "Fix login bug",
        sourceId: String = "cfg-jira",
        provider: TicketProvider = TicketProvider.JIRA,
        status: String = "Open",
        projectKey: String = "PROJ",
        issueType: String = "Bug"
    ) = TicketIssue(
        id = key,
        key = key,
        summary = summary,
        status = status,
        projectKey = projectKey,
        projectName = "Project",
        issueType = issueType,
        assignee = null,
        updated = Instant.fromEpochMilliseconds(0),
        sourceId = sourceId,
        provider = provider,
        webUrl = null
    )

    private fun config(
        id: String = "cfg-jira",
        provider: TicketProvider = TicketProvider.JIRA,
        format: String = IssueInsertFormat.DEFAULT_FORMAT
    ) = TicketSystemConfig(
        id = id,
        displayName = "Jira",
        provider = provider,
        enabled = true,
        baseUrl = "https://example.atlassian.net",
        credentials = TicketCredentials.JiraApiToken("user@example.com", "token"),
        syncIntervalMinutes = 15,
        issueFormat = format
    )

    // ---- formatTicketIssue (pure) ----

    @Test
    fun `formatTicketIssue uses matching config format`() {
        val result = formatTicketIssue(
            issue = issue(key = "PROJ-9", summary = "Crash"),
            configs = listOf(config(format = "[{key}] {summary}"))
        )
        assertEquals("[PROJ-9] Crash", result)
    }

    @Test
    fun `formatTicketIssue falls back to default when no config matches`() {
        val result = formatTicketIssue(
            issue = issue(key = "PROJ-2", summary = "Hello", sourceId = "missing"),
            configs = listOf(config(id = "other"))
        )
        assertEquals("PROJ-2: Hello", result)
    }

    // ---- TicketSuggestionsPopup ----

    @Test
    fun `popup is not rendered when not visible`() = runComposeUiTest {
        setContent {
            TestTheme {
                TicketSuggestionsPopup(
                    visible = false,
                    suggestions = listOf(issue()),
                    ticketConfigs = listOf(config()),
                    selectedIndex = -1,
                    textFieldWidth = 400,
                    onDismiss = {},
                    onSuggestionSelected = {}
                )
            }
        }
        waitForIdle()
        onNodeWithText("Fix login bug").assertDoesNotExist()
    }

    @Test
    fun `popup renders nothing for empty suggestions`() = runComposeUiTest {
        setContent {
            TestTheme {
                TicketSuggestionsPopup(
                    visible = true,
                    suggestions = emptyList(),
                    ticketConfigs = listOf(config()),
                    selectedIndex = -1,
                    textFieldWidth = 400,
                    onDismiss = {},
                    onSuggestionSelected = {}
                )
            }
        }
        waitForIdle()
        onNodeWithText("PROJ-1").assertDoesNotExist()
    }

    @Test
    fun `popup renders suggestion key and summary when visible`() = runComposeUiTest {
        setContent {
            TestTheme {
                TicketSuggestionsPopup(
                    visible = true,
                    suggestions = listOf(issue(key = "PROJ-1", summary = "Fix login bug")),
                    ticketConfigs = listOf(config()),
                    selectedIndex = 0,
                    textFieldWidth = 400,
                    onDismiss = {},
                    onSuggestionSelected = {}
                )
            }
        }
        waitForIdle()
        onNodeWithText("PROJ-1").assertExists()
        onNodeWithText("Fix login bug").assertExists()
    }

    @Test
    fun `clicking a suggestion emits formatted text`() = runComposeUiTest {
        var selected: String? = null
        setContent {
            TestTheme {
                TicketSuggestionsPopup(
                    visible = true,
                    suggestions = listOf(issue(key = "PROJ-7", summary = "Add button")),
                    ticketConfigs = listOf(config(format = "{key} - {summary}")),
                    selectedIndex = -1,
                    textFieldWidth = 400,
                    onDismiss = {},
                    onSuggestionSelected = { selected = it }
                )
            }
        }
        waitForIdle()
        onNodeWithText("Add button").performClick()
        waitForIdle()
        assertEquals("PROJ-7 - Add button", selected)
    }

    @Test
    fun `popup renders badges for all providers`() = runComposeUiTest {
        setContent {
            TestTheme {
                TicketSuggestionsPopup(
                    visible = true,
                    suggestions = listOf(
                        issue(key = "J-1", summary = "Jira issue", sourceId = "j", provider = TicketProvider.JIRA),
                        issue(key = "GH-1", summary = "GitHub issue", sourceId = "gh", provider = TicketProvider.GITHUB),
                        issue(key = "GL-1", summary = "GitLab issue", sourceId = "gl", provider = TicketProvider.GITLAB),
                        issue(key = "TR-1", summary = "Trello issue", sourceId = "tr", provider = TicketProvider.TRELLO)
                    ),
                    ticketConfigs = emptyList(),
                    selectedIndex = 2,
                    textFieldWidth = 400,
                    onDismiss = {},
                    onSuggestionSelected = {}
                )
            }
        }
        waitForIdle()
        // Provider badge labels
        onNodeWithText("J").assertExists()
        onNodeWithText("GH").assertExists()
        onNodeWithText("GL").assertExists()
        onNodeWithText("TR").assertExists()
        // Each summary rendered
        onNodeWithText("Jira issue").assertExists()
        onNodeWithText("GitLab issue").assertExists()
    }
}
