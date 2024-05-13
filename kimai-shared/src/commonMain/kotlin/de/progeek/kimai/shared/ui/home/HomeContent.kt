package de.progeek.kimai.shared.ui.home

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation

import de.progeek.kimai.shared.ui.settings.SettingsContent
import de.progeek.kimai.shared.ui.form.FormScreen
import de.progeek.kimai.shared.ui.timesheet.TimesheetScreen

@Composable
fun HomeContent(component: HomeComponent) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(fade() + scale()),
    ) {
        when (val child = it.instance) {
            is HomeComponent.Child.Timesheet -> TimesheetScreen(child.component)
            is HomeComponent.Child.Settings -> SettingsContent(child.component)
            is HomeComponent.Child.Form -> FormScreen(child.component)
        }
    }
}