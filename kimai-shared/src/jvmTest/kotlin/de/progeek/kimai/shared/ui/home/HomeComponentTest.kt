@file:OptIn(ExperimentalTestApi::class, kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.home

import androidx.compose.ui.test.ExperimentalTestApi
import de.progeek.kimai.shared.testutils.TestData
import de.progeek.kimai.shared.testutils.TestKoinModule
import de.progeek.kimai.shared.testutils.createTestComponentContext
import de.progeek.kimai.shared.testutils.createTestDispatchers
import de.progeek.kimai.shared.testutils.createTestStoreFactory
import de.progeek.kimai.shared.ui.form.FormComponent
import de.progeek.kimai.shared.ui.timesheet.list.TimesheetListComponent
import de.progeek.kimai.shared.ui.timesheet.topbar.TimesheetTopBarComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Tests for [HomeComponent] navigation logic.
 *
 * Navigation inside [HomeComponent] is driven by outputs from its child components
 * (timesheet top bar, list, form, settings). These outputs are exposed via public
 * methods, so the navigation stack can be exercised without rendering Compose.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeComponentTest {

    @Before
    fun setUp() {
        TestKoinModule.startTestKoin()
    }

    @After
    fun tearDown() {
        TestKoinModule.stopTestKoin()
    }

    private fun createHomeComponent(): HomeComponent {
        return HomeComponent(
            componentContext = createTestComponentContext(),
            storeFactory = createTestStoreFactory(),
            dispatchers = createTestDispatchers()
        )
    }

    private fun HomeComponent.activeChild(): HomeComponent.Child =
        childStack.value.active.instance

    @Test
    fun `initial active child is timesheet`() {
        val component = createHomeComponent()

        assertTrue(
            component.activeChild() is HomeComponent.Child.Timesheet,
            "Initial child should be Timesheet"
        )
        assertTrue(
            component.childStack.value.backStack.isEmpty(),
            "Initial back stack should be empty"
        )
    }

    @Test
    fun `top bar show settings output navigates to settings`() {
        val component = createHomeComponent()

        val timesheet = component.activeChild() as HomeComponent.Child.Timesheet
        timesheet.component.topBarComponent.onOutput(TimesheetTopBarComponent.Output.ShowSettings)

        assertTrue(
            component.activeChild() is HomeComponent.Child.Settings,
            "Active child should be Settings after ShowSettings output"
        )
    }

    @Test
    fun `closing settings pops back to timesheet`() {
        val component = createHomeComponent()

        val timesheet = component.activeChild() as HomeComponent.Child.Timesheet
        timesheet.component.topBarComponent.onOutput(TimesheetTopBarComponent.Output.ShowSettings)

        val settings = component.activeChild() as HomeComponent.Child.Settings
        settings.component.onOutput()

        assertTrue(
            component.activeChild() is HomeComponent.Child.Timesheet,
            "Active child should be Timesheet again after settings close"
        )
    }

    @Test
    fun `editing a timesheet navigates to form`() {
        val component = createHomeComponent()

        val timesheet = component.activeChild() as HomeComponent.Child.Timesheet
        timesheet.component.timesheetListComponent.onOutput(
            TimesheetListComponent.Output.Edit(TestData.timesheet1)
        )

        assertTrue(
            component.activeChild() is HomeComponent.Child.Form,
            "Active child should be Form after Edit output"
        )
    }

    @Test
    fun `closing form pops back to timesheet`() {
        val component = createHomeComponent()

        val timesheet = component.activeChild() as HomeComponent.Child.Timesheet
        timesheet.component.timesheetListComponent.onOutput(
            TimesheetListComponent.Output.Edit(TestData.timesheet1)
        )

        val form = component.activeChild() as HomeComponent.Child.Form
        form.component.onOutput(FormComponent.Output.Close)

        assertTrue(
            component.activeChild() is HomeComponent.Child.Timesheet,
            "Active child should be Timesheet again after form close"
        )
    }
}
