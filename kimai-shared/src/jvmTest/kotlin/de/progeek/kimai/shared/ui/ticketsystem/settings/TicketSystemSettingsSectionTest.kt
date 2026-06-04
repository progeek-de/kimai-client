@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.ticketsystem.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Test

class TicketSystemSettingsSectionTest {

    private lateinit var configRepository: TicketConfigRepository

    @Before
    fun setUp() {
        configRepository = mockk(relaxed = true) {
            every { getAllConfigs() } returns flowOf(emptyList())
            every { getEnabledConfigs() } returns flowOf(emptyList())
            every { hasEnabledConfigs() } returns flowOf(false)
            coEvery { setEnabled(any(), any()) } returns Result.success(Unit)
            coEvery { saveConfig(any()) } answers { Result.success(firstArg()) }
            coEvery { deleteConfig(any()) } returns Result.success(Unit)
        }
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
    }

    private fun startKoin() {
        TestKoinModule.startTestKoin(ticketConfigRepository = configRepository)
    }

    private fun githubConfig(
        id: String = "cfg-gh",
        displayName: String = "Work GitHub",
        enabled: Boolean = true
    ) = TicketSystemConfig(
        id = id,
        displayName = displayName,
        provider = TicketProvider.GITHUB,
        enabled = enabled,
        baseUrl = "https://api.github.com",
        credentials = TicketCredentials.GitHubToken("ghp_x", "octocat", listOf("repo1")),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private fun jiraConfig(
        id: String = "cfg-jira",
        displayName: String = "Personal Jira",
        enabled: Boolean = false
    ) = TicketSystemConfig(
        id = id,
        displayName = displayName,
        provider = TicketProvider.JIRA,
        enabled = enabled,
        baseUrl = "https://example.atlassian.net",
        credentials = TicketCredentials.JiraApiToken("user@example.com", "token"),
        syncIntervalMinutes = 30,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    @Test
    fun `empty state shows header and placeholder text`() = runComposeUiTest {
        every { configRepository.getAllConfigs() } returns flowOf(emptyList())
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNodeWithText("Ticket Systems").assertExists()
        onNodeWithText("No ticket systems configured. Click + to add one.").assertExists()
        onNodeWithContentDescription("Add Ticket System").assertExists()
    }

    @Test
    fun `populated list shows configured display names and base urls`() = runComposeUiTest {
        every { configRepository.getAllConfigs() } returns flowOf(
            listOf(githubConfig(), jiraConfig())
        )
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNodeWithText("Work GitHub").assertExists()
        onNodeWithText("Personal Jira").assertExists()
        onNodeWithText("https://api.github.com").assertExists()
        onNodeWithText("https://example.atlassian.net").assertExists()
        // Placeholder must not be shown when configs exist.
        onNodeWithText("No ticket systems configured. Click + to add one.").assertDoesNotExist()
    }

    @Test
    fun `enabled config renders switch in on state`() = runComposeUiTest {
        every { configRepository.getAllConfigs() } returns flowOf(listOf(githubConfig(enabled = true)))
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun `toggling switch calls setEnabled on repository`() = runComposeUiTest {
        val config = jiraConfig(enabled = false)
        every { configRepository.getAllConfigs() } returns flowOf(listOf(config))
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNode(isToggleable()).performClick()
        waitForIdle()

        coVerify { configRepository.setEnabled(config.id, true) }
    }

    @Test
    fun `add button opens provider selection dialog`() = runComposeUiTest {
        every { configRepository.getAllConfigs() } returns flowOf(emptyList())
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNodeWithContentDescription("Add Ticket System").performClick()
        waitForIdle()

        onNodeWithText("Select a provider:").assertExists()
        onNodeWithText("Atlassian Jira Cloud or Server").assertExists()
        onNodeWithText("GitHub Issues from repositories").assertExists()
    }

    @Test
    fun `selecting provider opens config dialog in add mode`() = runComposeUiTest {
        every { configRepository.getAllConfigs() } returns flowOf(emptyList())
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNodeWithContentDescription("Add Ticket System").performClick()
        waitForIdle()
        onNodeWithText("GitHub Issues from repositories").performClick()
        waitForIdle()

        // Config dialog opened in add mode for GitHub.
        onNodeWithText("Add GitHub Issues").assertExists()
    }

    @Test
    fun `clicking a config row opens edit dialog with delete action`() = runComposeUiTest {
        every { configRepository.getAllConfigs() } returns flowOf(listOf(githubConfig()))
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNodeWithText("Work GitHub").performClick()
        waitForIdle()

        onNodeWithText("Edit GitHub Issues").assertExists()
        onNodeWithText("Delete").assertExists()
    }

    @Test
    fun `delete from edit dialog shows confirmation and deletes on confirm`() = runComposeUiTest {
        val config = githubConfig()
        every { configRepository.getAllConfigs() } returns flowOf(listOf(config))
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        // Open edit dialog.
        onNodeWithText("Work GitHub").performClick()
        waitForIdle()

        // Trigger delete -> confirmation dialog.
        onNodeWithText("Delete").performClick()
        waitForIdle()

        onNodeWithText("Delete Configuration").assertExists()
        onNode(
            hasText("Are you sure you want to delete 'Work GitHub'?", substring = true)
        ).assertExists()

        // Confirm deletion (the confirm button in the AlertDialog).
        onNodeWithText("Delete").performClick()
        waitForIdle()

        coVerify { configRepository.deleteConfig(config.id) }
    }

    @Test
    fun `saving edited config calls saveConfig on repository`() = runComposeUiTest {
        val config = githubConfig()
        every { configRepository.getAllConfigs() } returns flowOf(listOf(config))
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNodeWithText("Work GitHub").performClick()
        waitForIdle()
        onNodeWithText("Edit GitHub Issues").assertExists()

        onNodeWithText("Save").performClick()
        waitForIdle()

        coVerify { configRepository.saveConfig(any()) }
    }

    @Test
    fun `provider selection dialog can be dismissed`() = runComposeUiTest {
        every { configRepository.getAllConfigs() } returns flowOf(emptyList())
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNodeWithContentDescription("Add Ticket System").performClick()
        waitForIdle()
        onNodeWithText("Select a provider:").assertExists()

        onNodeWithText("Cancel").performClick()
        waitForIdle()

        onNodeWithText("Select a provider:").assertDoesNotExist()
    }

    @Test
    fun `multiple configs each render their provider indicators`() = runComposeUiTest {
        every { configRepository.getAllConfigs() } returns flowOf(
            listOf(
                githubConfig(id = "a", displayName = "Alpha GitHub"),
                jiraConfig(id = "b", displayName = "Beta Jira")
            )
        )
        startKoin()

        setContent {
            TestTheme {
                TicketSystemSettingsSection()
            }
        }
        waitForIdle()

        onNodeWithText("Alpha GitHub").assertExists()
        onNodeWithText("Beta Jira").assertExists()
        // Provider badges: "GH" for GitHub, "J" for Jira.
        onNodeWithText("GH").assertExists()
        onNodeWithText("J").assertExists()
    }
}
