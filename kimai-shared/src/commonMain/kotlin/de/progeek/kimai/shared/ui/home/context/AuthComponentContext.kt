package de.progeek.kimai.shared.ui.home.context

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.KSerializer
import de.progeek.kimai.shared.core.models.Credentials

interface AuthComponentContext : ComponentContext {
    val credentials: Credentials
    val baseUrl: String
}

fun <C : Any, T : Any> AuthComponentContext.formChildStack(
    source: StackNavigation<C>,
    serializer: KSerializer<C>,
    initialStack: () -> List<C>,
    key: String = "DefaultStack",
    handleBackButton: Boolean = false,
    childFactory: (configuration: C, AuthComponentContext) -> T
): Value<ChildStack<C, T>> =
    childStack(
        source = source,
        serializer = serializer,
        initialStack = initialStack,
        key = key,
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
