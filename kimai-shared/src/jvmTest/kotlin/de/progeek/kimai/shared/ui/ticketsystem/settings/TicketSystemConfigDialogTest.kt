@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.ticketsystem.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketSystemRepository
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TicketSystemConfigDialogTest {

    private lateinit var ticketRepository: TicketSystemRepository

    @Before
    fun setUp() {
        ticketRepository = mockk(relaxed = true) {
            coEvery { testConnection(any()) } returns Result.success("Connected as tester")
        }
        TestKoinModule.startTestKoin(ticketSystemRepository = ticketRepository)
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
    }

    private fun githubConfig() = TicketSystemConfig(
        id = "cfg-gh",
        displayName = "Work GitHub",
        provider = TicketProvider.GITHUB,
        enabled = true,
        baseUrl = "https://api.github.com",
        credentials = TicketCredentials.GitHubToken("ghp_x", "octocat", listOf("a", "b")),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    @Test
    fun `add GitHub builds GitHubToken config on save`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        setContent {
            TestTheme {
                TicketSystemConfigDialog(
                    existingConfig = null,
                    provider = TicketProvider.GITHUB,
                    onSave = { saved = it },
                    onDismiss = {}
                )
            }
        }
        waitForIdle()
        onNodeWithText("Add GitHub Issues").assertExists()
        onNodeWithText("Display Name").performTextInput("My GH")
        onNodeWithText("Personal Access Token").performTextInput("ghp_token")
        onNodeWithText("Owner/Organization").performTextInput("octocat")
        onNodeWithText("Repositories (optional)").performTextInput("repo1, repo2")
        onNodeWithText("Save").performClick()
        waitForIdle()

        val config = requireNotNull(saved)
        assertEquals(TicketProvider.GITHUB, config.provider)
        val creds = config.credentials as TicketCredentials.GitHubToken
        assertEquals("octocat", creds.owner)
        assertEquals(listOf("repo1", "repo2"), creds.repositories)
    }

    @Test
    fun `add Jira with API token builds JiraApiToken`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.JIRA, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithText("Display Name").performTextInput("Jira Cloud")
        onNodeWithText("Base URL").performTextInput("https://x.atlassian.net")
        onNodeWithText("Email").performTextInput("user@example.com")
        // "API Token" also labels a FilterChip, so target the editable field explicitly
        onNode(hasSetTextAction() and hasText("API Token")).performTextInput("api-token")
        onNodeWithText("Save").performClick()
        waitForIdle()

        val creds = requireNotNull(saved).credentials
        assertTrue(creds is TicketCredentials.JiraApiToken)
        assertEquals("user@example.com", creds.email)
    }

    @Test
    fun `Jira PAT chip hides email and builds JiraPersonalAccessToken`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.JIRA, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithText("Display Name").performTextInput("Jira Server")
        onNodeWithText("Base URL").performTextInput("https://jira.local")
        onNodeWithText("PAT").performClick()
        waitForIdle()
        onNodeWithText("Email").assertDoesNotExist()
        onNodeWithText("Personal Access Token").performTextInput("pat-token")
        onNodeWithText("Save").performClick()
        waitForIdle()

        assertTrue(requireNotNull(saved).credentials is TicketCredentials.JiraPersonalAccessToken)
    }

    @Test
    fun `add GitLab builds GitLabToken`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.GITLAB, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithText("Display Name").performTextInput("GL")
        onNodeWithText("Personal Access Token").performTextInput("glpat")
        onNodeWithText("Project IDs or Paths (optional)").performTextInput("123, group/p")
        onNodeWithText("Save").performClick()
        waitForIdle()

        val creds = requireNotNull(saved).credentials as TicketCredentials.GitLabToken
        assertEquals(listOf("123", "group/p"), creds.projectIds)
    }

    @Test
    fun `add Trello builds TrelloToken`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.TRELLO, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithText("Display Name").performTextInput("Trello")
        onNodeWithText("API Key").performTextInput("key")
        onNodeWithText("Token").performTextInput("tok")
        onNodeWithText("Save").performClick()
        waitForIdle()

        assertTrue(requireNotNull(saved).credentials is TicketCredentials.TrelloToken)
    }

    @Test
    fun `save is disabled when display name blank`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.GITLAB, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        // displayName left blank -> Save disabled (baseUrl has a default for GitLab)
        onNodeWithText("Save").assertIsNotEnabled()
        assertNull(saved)
    }

    @Test
    fun `edit mode shows Edit title, prefilled fields and Delete`() = runComposeUiTest {
        var deleted = false
        setContent {
            TestTheme {
                TicketSystemConfigDialog(
                    existingConfig = githubConfig(),
                    provider = TicketProvider.GITHUB,
                    onSave = {},
                    onDelete = { deleted = true },
                    onDismiss = {}
                )
            }
        }
        waitForIdle()
        onNodeWithText("Edit GitHub Issues").assertExists()
        onNodeWithText("octocat").assertExists() // owner prefilled from LaunchedEffect
        onNodeWithText("Delete").performClick()
        assertTrue(deleted)
    }

    @Test
    fun `cancel invokes onDismiss`() = runComposeUiTest {
        var dismissed = false
        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.GITHUB, onSave = {}, onDismiss = { dismissed = true })
            }
        }
        waitForIdle()
        onNodeWithText("Cancel").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun `toggle token visibility`() = runComposeUiTest {
        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.GITHUB, onSave = {}, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithContentDescription("Toggle visibility").performClick()
        waitForIdle()
        onNodeWithContentDescription("Toggle visibility").assertExists()
    }

    @Test
    fun `test connection shows result`() = runComposeUiTest {
        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.GITHUB, onSave = {}, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithText("Personal Access Token").performTextInput("ghp_token")
        onNodeWithText("Owner/Organization").performTextInput("octocat")
        onNodeWithText("Test Connection").performClick()
        waitForIdle()
        onNodeWithText("Connected as tester").assertExists()
    }

    @Test
    fun `test connection shows failure message`() = runComposeUiTest {
        val failing: TicketSystemRepository = mockk(relaxed = true) {
            coEvery { testConnection(any()) } returns Result.failure(IllegalStateException("nope: 401"))
        }
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(ticketSystemRepository = failing)

        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.GITHUB, onSave = {}, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithText("Personal Access Token").performTextInput("ghp_token")
        onNodeWithText("Owner/Organization").performTextInput("octocat")
        onNodeWithText("Test Connection").performClick()
        waitForIdle()
        onNodeWithText("nope: 401").assertExists()
    }

    @Test
    fun `edit Jira API token prefills email and re-saves JiraApiToken`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        val existing = TicketSystemConfig(
            id = "cfg-jira",
            displayName = "Jira",
            provider = TicketProvider.JIRA,
            enabled = true,
            baseUrl = "https://x.atlassian.net",
            credentials = TicketCredentials.JiraApiToken("user@example.com", "tok"),
            syncIntervalMinutes = 15,
            issueFormat = IssueInsertFormat.DEFAULT_FORMAT
        )
        setContent {
            TestTheme {
                TicketSystemConfigDialog(existing, TicketProvider.JIRA, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        // LaunchedEffect populates the email from existing credentials.
        onNodeWithText("user@example.com").assertExists()
        onNodeWithText("Save").performClick()
        waitForIdle()
        assertTrue(requireNotNull(saved).credentials is TicketCredentials.JiraApiToken)
    }

    @Test
    fun `edit Jira PAT config prefills PAT mode`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        val existing = TicketSystemConfig(
            id = "cfg-jira-pat",
            displayName = "Jira Server",
            provider = TicketProvider.JIRA,
            enabled = true,
            baseUrl = "https://jira.local",
            credentials = TicketCredentials.JiraPersonalAccessToken("pat"),
            syncIntervalMinutes = 15,
            issueFormat = IssueInsertFormat.DEFAULT_FORMAT
        )
        setContent {
            TestTheme {
                TicketSystemConfigDialog(existing, TicketProvider.JIRA, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        // PAT mode hides the email field.
        onNodeWithText("Email").assertDoesNotExist()
        onNodeWithText("Save").performClick()
        waitForIdle()
        assertTrue(requireNotNull(saved).credentials is TicketCredentials.JiraPersonalAccessToken)
    }

    @Test
    fun `edit GitLab config prefills token and project ids`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        val existing = TicketSystemConfig(
            id = "cfg-gl",
            displayName = "GL",
            provider = TicketProvider.GITLAB,
            enabled = true,
            baseUrl = "https://gitlab.com",
            credentials = TicketCredentials.GitLabToken("glpat", listOf("123", "group/p")),
            syncIntervalMinutes = 15,
            issueFormat = IssueInsertFormat.DEFAULT_FORMAT
        )
        setContent {
            TestTheme {
                TicketSystemConfigDialog(existing, TicketProvider.GITLAB, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithText("123, group/p").assertExists()
        onNodeWithText("Save").performClick()
        waitForIdle()
        assertEquals(
            listOf("123", "group/p"),
            (requireNotNull(saved).credentials as TicketCredentials.GitLabToken).projectIds
        )
    }

    @Test
    fun `edit Trello config prefills api key and board ids`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        val existing = TicketSystemConfig(
            id = "cfg-tr",
            displayName = "Trello",
            provider = TicketProvider.TRELLO,
            enabled = true,
            baseUrl = "https://api.trello.com/1",
            credentials = TicketCredentials.TrelloToken("apikey", "tok", listOf("b1")),
            syncIntervalMinutes = 15,
            issueFormat = IssueInsertFormat.DEFAULT_FORMAT
        )
        setContent {
            TestTheme {
                TicketSystemConfigDialog(existing, TicketProvider.TRELLO, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithText("apikey").assertExists()
        onNodeWithText("Save").performClick()
        waitForIdle()
        assertTrue(requireNotNull(saved).credentials is TicketCredentials.TrelloToken)
    }

    @Test
    fun `editing sync interval filters non digits`() = runComposeUiTest {
        var saved: TicketSystemConfig? = null
        setContent {
            TestTheme {
                TicketSystemConfigDialog(null, TicketProvider.GITLAB, onSave = { saved = it }, onDismiss = {})
            }
        }
        waitForIdle()
        onNodeWithText("Display Name").performTextInput("GL")
        onNodeWithText("Personal Access Token").performTextInput("glpat")
        onNodeWithText("Sync Interval (minutes)").performTextClearance()
        onNodeWithText("Sync Interval (minutes)").performTextInput("a3b0")
        onNodeWithText("Save").performClick()
        waitForIdle()
        // Non-digits are filtered, leaving "30".
        assertEquals(30, requireNotNull(saved).syncIntervalMinutes)
    }
}
