@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.form.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestTheme
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FormTimeFieldsTest {

    private val begin = LocalDateTime(2025, 1, 1, 9, 0)
    private val end = LocalDateTime(2025, 1, 1, 12, 0)

    @Test
    fun `renders begin end and duration`() = runComposeUiTest {
        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                FormTimeFields(
                    begin = begin,
                    end = end,
                    snackbarHostState = snackbar,
                    onBeginChange = {},
                    onEndChange = {}
                )
            }
        }

        waitForIdle()

        // Begin (09:00) and end (12:00) time fields are visible.
        onNode(hasSetTextAction() and hasText("09:00")).assertExists()
        onNode(hasSetTextAction() and hasText("12:00")).assertExists()
        // The separator dash and the computed 3 hour duration are shown.
        onNodeWithText("-").assertExists()
        onNodeWithText("03:00:00").assertExists()
    }

    @Test
    fun `clicking calendar opens date picker`() = runComposeUiTest {
        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                FormTimeFields(
                    begin = begin,
                    end = end,
                    snackbarHostState = snackbar,
                    onBeginChange = {},
                    onEndChange = {}
                )
            }
        }

        waitForIdle()

        // The calendar icon uses the "Refresh" content description.
        onNode(hasContentDescription("Refresh")).performClick()

        waitForIdle()

        // The date picker dialog exposes an OK confirm button.
        onNodeWithText("OK", ignoreCase = true).assertExists()
    }

    @Test
    fun `confirming date picker updates begin and end`() = runComposeUiTest {
        var newBegin: LocalDateTime? = null
        var newEnd: LocalDateTime? = null

        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                FormTimeFields(
                    begin = begin,
                    end = end,
                    snackbarHostState = snackbar,
                    onBeginChange = { newBegin = it },
                    onEndChange = { newEnd = it }
                )
            }
        }

        waitForIdle()

        onNode(hasContentDescription("Refresh")).performClick()
        waitForIdle()

        onNodeWithText("OK", ignoreCase = true).performClick()
        waitForIdle()

        // Confirming the picker pushes new begin and end values keeping the original times.
        assertNotNull(newBegin)
        assertNotNull(newEnd)
        assertEquals(begin.time, newBegin!!.time)
        assertEquals(end.time, newEnd!!.time)
    }

    @Test
    fun `picker closes after confirmation`() = runComposeUiTest {
        setContent {
            TestTheme {
                val snackbar = remember { SnackbarHostState() }
                FormTimeFields(
                    begin = begin,
                    end = end,
                    snackbarHostState = snackbar,
                    onBeginChange = {},
                    onEndChange = {}
                )
            }
        }

        waitForIdle()

        onNode(hasContentDescription("Refresh")).performClick()
        waitForIdle()
        onNodeWithText("OK", ignoreCase = true).assertExists()

        onNodeWithText("OK", ignoreCase = true).performClick()
        waitForIdle()

        // After confirming, the picker (and its OK button) is dismissed.
        onNodeWithText("OK", ignoreCase = true).assertDoesNotExist()
    }
}
