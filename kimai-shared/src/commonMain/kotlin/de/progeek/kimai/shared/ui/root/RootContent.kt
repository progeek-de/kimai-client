package de.progeek.kimai.shared.ui.root

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import de.progeek.kimai.shared.ui.components.loading.LoadingContent
import de.progeek.kimai.shared.ui.home.HomeContent
import de.progeek.kimai.shared.ui.login.LoginScreen

@Composable
fun RootContent(component: RootComponent) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(fade() + scale())
    ) {
        when (val child = it.instance) {
            is RootComponent.Child.Login -> LoginScreen(child.component)
            is RootComponent.Child.Home -> HomeContent(child.component)
            is RootComponent.Child.Loading -> LoadingContent()
        }
    }
}
