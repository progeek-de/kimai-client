package de.progeek.kimai.shared.ui.home.context

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigationSource
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import de.progeek.kimai.shared.core.models.Credentials

interface AuthComponentContext : ComponentContext {
    val credentials: Credentials
    val baseUrl: String
}

inline fun <reified C : Parcelable, T : Any> AuthComponentContext.formChildStack(
    source: StackNavigationSource<C>,
    noinline initialStack: () -> List<C>,
    key: String = "DefaultStack",
    persistent: Boolean = true,
    handleBackButton: Boolean = false,
    noinline childFactory: (configuration: C, AuthComponentContext) -> T
): Value<ChildStack<C, T>> =
    childStack(
        source = source,
        initialStack = initialStack,
        key = key,
        persistent = persistent,
        handleBackButton = handleBackButton
    ) { configuration, componentContext ->
        childFactory(
            configuration,
            DefaultAuthComponentContext(
                componentContext = componentContext,
                credentials = credentials,
                baseUrl = baseUrl
            )
        )
    }
