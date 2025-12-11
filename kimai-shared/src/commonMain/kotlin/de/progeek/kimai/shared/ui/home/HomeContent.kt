package de.progeek.kimai.shared.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import de.progeek.kimai.shared.ui.components.BottomBar
import de.progeek.kimai.shared.ui.form.FormScreen
import de.progeek.kimai.shared.ui.settings.SettingsContent
import de.progeek.kimai.shared.ui.timesheet.TimesheetScreen

@Composable
fun HomeContent(component: HomeComponent) {
    Column(modifier = Modifier.fillMaxSize()) {
        Children(
            stack = component.childStack,
            animation = stackAnimation(fade() + scale()),
            modifier = Modifier.weight(1f)
        ) {
            when (val child = it.instance) {
                is HomeComponent.Child.Timesheet -> TimesheetScreen(child.component)
                is HomeComponent.Child.Settings -> SettingsContent(child.component)
                is HomeComponent.Child.Form -> FormScreen(child.component)
            }
        }
        BottomBar()
    }
}
