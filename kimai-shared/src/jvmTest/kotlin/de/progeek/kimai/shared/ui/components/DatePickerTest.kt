@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.testutils.TestTheme
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

class DatePickerTest {

    private val fixedDate = Instant.fromEpochMilliseconds(1_700_000_000_000) // mid Nov 2023

    @Test
    fun `date picker dialog renders title`() = runComposeUiTest {
        setContent {
            TestTheme {
                DatePicker(date = fixedDate, onDateChange = { })
            }
        }

        waitForIdle()

        // Dialog opens in Picker display mode -> shows the "Select Date" title.
        onNodeWithText("Select Date").assertExists()
    }

    @Test
    fun `date picker dialog renders confirm button`() = runComposeUiTest {
        setContent {
            TestTheme {
                DatePicker(date = fixedDate, onDateChange = { })
            }
        }

        waitForIdle()

        onNodeWithText("OK").assertExists()
    }

    @Test
    fun `confirming selection invokes onDateChange with selected date`() = runComposeUiTest {
        var selected: Instant? = null

        setContent {
            TestTheme {
                DatePicker(date = fixedDate, onDateChange = { selected = it })
            }
        }

        waitForIdle()

        onNodeWithText("OK").performClick()

        waitForIdle()

        // The initially selected date equals the supplied date, so the callback
        // fires with a non-null instant matching the initial selection.
        assertNotNull(selected)
    }

    @Test
    fun `confirming selection returns the initially selected date`() = runComposeUiTest {
        var selected: Instant? = null

        setContent {
            TestTheme {
                DatePicker(date = fixedDate, onDateChange = { selected = it })
            }
        }

        waitForIdle()

        // No navigation/selection change -> the initial selected millis is returned.
        onNode(hasText("OK")).performClick()

        waitForIdle()

        // DatePicker truncates to day precision, so compare on day granularity.
        val expectedDay = fixedDate.toEpochMilliseconds() / 86_400_000L
        val actualDay = (selected?.toEpochMilliseconds() ?: -1L) / 86_400_000L
        assertNotNull(selected)
        assert(actualDay == expectedDay) {
            "Expected day $expectedDay but was $actualDay"
        }
    }

    @Test
    fun `callback not invoked before confirming`() = runComposeUiTest {
        var selected: Instant? = null

        setContent {
            TestTheme {
                DatePicker(date = fixedDate, onDateChange = { selected = it })
            }
        }

        waitForIdle()

        // Without interaction the dialog must not have reported a date yet.
        assertNull(selected)
    }
}
