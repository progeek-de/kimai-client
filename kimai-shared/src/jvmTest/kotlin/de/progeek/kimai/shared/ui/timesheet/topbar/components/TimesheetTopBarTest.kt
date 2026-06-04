@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.timesheet.topbar.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import de.progeek.kimai.shared.core.mapper.toTimesheetForm
import de.progeek.kimai.shared.testutils.TestData
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.TestTheme
import de.progeek.kimai.shared.testutils.createTestComponentContext
import de.progeek.kimai.shared.testutils.createTestDispatchers
import de.progeek.kimai.shared.testutils.createTestStoreFactory
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.ui.timesheet.topbar.TimesheetTopBarComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Render tests for [TimesheetTopBar].
 *
 * The action menu lives in a right-aligned [androidx.compose.material3.TopAppBar] actions slot
 * and expands a [androidx.compose.material3.DropdownMenu] popup. Headless Compose Desktop cannot
 * reliably inject mouse input into the off-screen action button / popup, so these tests focus on
 * rendering the bar across theme and timer states (which exercises the composable tree); the menu
 * item click handlers are covered at the store/component level in TimesheetTopBarStore tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimesheetTopBarTest {

    private lateinit var outputCallback: (TimesheetTopBarComponent.Output) -> Unit

    @Before
    fun setUp() {
        outputCallback = { }
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
    }

    private fun createComponent(): TimesheetTopBarComponent =
        TimesheetTopBarComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers(),
            output = outputCallback
        )

    @Test
    fun `top bar renders in light theme with menu collapsed`() = runComposeUiTest {
        val component = createComponent()
        setContent { TestTheme(theme = ThemeEnum.LIGHT) { TimesheetTopBar(component) } }
        waitForIdle()
        // Collapsed: dropdown action items are not present until the menu is opened.
        onNodeWithText("SETTINGS").assertDoesNotExist()
        onNodeWithText("LOGOUT").assertDoesNotExist()
    }

    @Test
    fun `top bar renders in dark theme`() = runComposeUiTest {
        val component = createComponent()
        setContent { TestTheme(theme = ThemeEnum.DARK) { TimesheetTopBar(component) } }
        waitForIdle()
        onNodeWithText("SETTINGS").assertDoesNotExist()
    }

    @Test
    fun `top bar renders with a running timesheet`() = runComposeUiTest {
        val timesheetRepository = TestKoinModule.createMockTimesheetRepository(
            timesheets = TestData.timesheets,
            runningTimesheet = TestData.runningTimesheet.toTimesheetForm()
        )
        TestKoinModule.stopTestKoin()
        TestKoinModule.startTestKoin(timesheetRepository = timesheetRepository)

        val component = createComponent()
        setContent { TestTheme { TimesheetTopBar(component) } }
        waitForIdle()
        // Bar still renders with the collapsed menu while a timer runs.
        onNodeWithText("LOGOUT").assertDoesNotExist()
    }
}
