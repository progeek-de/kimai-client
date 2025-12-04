package de.progeek.kimai.shared.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestTheme
import org.junit.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class KimaiTopAppBarTest {

    @Test
    fun `top app bar displays back button`() = runComposeUiTest {
        var backClicked = false

        setContent {
            TestTheme {
                KimaiTopAppBar(onBackClick = { backClicked = true })
            }
        }

        waitForIdle()

        // Back button should be visible (uses text "Back" from SharedRes.strings.back)
        onNodeWithText("Back", ignoreCase = true).assertExists()
    }

    @Test
    fun `clicking back button triggers callback`() = runComposeUiTest {
        var backClicked = false

        setContent {
            TestTheme {
                KimaiTopAppBar(onBackClick = { backClicked = true })
            }
        }

        waitForIdle()

        // Click back button
        onNodeWithText("Back", ignoreCase = true).performClick()

        waitForIdle()

        // Verify callback was triggered
        assertTrue(backClicked)
    }

    @Test
    fun `top app bar has proper structure`() = runComposeUiTest {
        setContent {
            TestTheme {
                KimaiTopAppBar(onBackClick = { })
            }
        }

        waitForIdle()

        // The top app bar should render without errors
        // Back button should be present with text "Back"
        onNodeWithText("Back", ignoreCase = true).assertExists()
    }

    @Test
    fun `top app bar renders correctly`() = runComposeUiTest {
        setContent {
            TestTheme {
                KimaiTopAppBar(onBackClick = { })
            }
        }

        waitForIdle()

        // The component should render without error
        // Verify the back button text is displayed
        onNodeWithText("Back", ignoreCase = true).assertExists()
    }
}
