@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestTheme
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class TextTimeFieldTest {

    private val begin = LocalDateTime(2025, 1, 1, 9, 5)

    @Test
    fun `renders formatted time`() = runComposeUiTest {
        setContent {
            TestTheme {
                TextTimeField(time = begin, onChange = {})
            }
        }

        waitForIdle()

        onNode(hasSetTextAction() and hasText("09:05")).assertExists()
    }

    @Test
    fun `typing limits to five characters`() = runComposeUiTest {
        setContent {
            TestTheme {
                TextTimeField(time = begin, onChange = {})
            }
        }

        waitForIdle()

        val field = onNode(hasSetTextAction() and hasText("09:05"))
        field.requestFocus()
        field.performTextClearance()
        field.performTextInput("123456789")

        waitForIdle()

        // The field truncates input to at most five characters (HH:mm), so the long
        // sequence above must not be shown verbatim.
        onNode(hasSetTextAction() and hasText("123456789")).assertDoesNotExist()
    }
}
