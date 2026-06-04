@file:OptIn(ExperimentalTestApi::class)

package de.progeek.kimai.shared.ui.ticketsystem.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProviderSelectionDialogTest {

    @Before
    fun setUp() {
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
    }

    @Test
    fun `shows title and an option for every provider`() = runComposeUiTest {
        setContent {
            TestTheme {
                ProviderSelectionDialog(
                    onProviderSelected = {},
                    onDismiss = {}
                )
            }
        }
        waitForIdle()

        onNodeWithText("Add Ticket System").assertExists()
        onNodeWithText("Select a provider:").assertExists()

        TicketProvider.entries.forEach { provider ->
            onNodeWithText(provider.displayName).assertExists()
        }
    }

    @Test
    fun `clicking Jira selects JIRA`() = runComposeUiTest {
        var selected: TicketProvider? = null
        setContent {
            TestTheme {
                ProviderSelectionDialog(
                    onProviderSelected = { selected = it },
                    onDismiss = {}
                )
            }
        }
        waitForIdle()

        onNodeWithText(TicketProvider.JIRA.displayName).performClick()
        waitForIdle()

        assertEquals(TicketProvider.JIRA, selected)
    }

    @Test
    fun `clicking GitHub selects GITHUB`() = runComposeUiTest {
        var selected: TicketProvider? = null
        setContent {
            TestTheme {
                ProviderSelectionDialog(
                    onProviderSelected = { selected = it },
                    onDismiss = {}
                )
            }
        }
        waitForIdle()

        onNodeWithText(TicketProvider.GITHUB.displayName).performClick()
        waitForIdle()

        assertEquals(TicketProvider.GITHUB, selected)
    }

    @Test
    fun `clicking GitLab selects GITLAB`() = runComposeUiTest {
        var selected: TicketProvider? = null
        setContent {
            TestTheme {
                ProviderSelectionDialog(
                    onProviderSelected = { selected = it },
                    onDismiss = {}
                )
            }
        }
        waitForIdle()

        onNodeWithText(TicketProvider.GITLAB.displayName).performClick()
        waitForIdle()

        assertEquals(TicketProvider.GITLAB, selected)
    }

    @Test
    fun `clicking Trello selects TRELLO`() = runComposeUiTest {
        var selected: TicketProvider? = null
        setContent {
            TestTheme {
                ProviderSelectionDialog(
                    onProviderSelected = { selected = it },
                    onDismiss = {}
                )
            }
        }
        waitForIdle()

        onNodeWithText(TicketProvider.TRELLO.displayName).performClick()
        waitForIdle()

        assertEquals(TicketProvider.TRELLO, selected)
    }

    @Test
    fun `cancel invokes onDismiss`() = runComposeUiTest {
        var dismissed = false
        setContent {
            TestTheme {
                ProviderSelectionDialog(
                    onProviderSelected = {},
                    onDismiss = { dismissed = true }
                )
            }
        }
        waitForIdle()

        onNodeWithText("Cancel").performClick()
        waitForIdle()

        assertTrue(dismissed)
    }
}
