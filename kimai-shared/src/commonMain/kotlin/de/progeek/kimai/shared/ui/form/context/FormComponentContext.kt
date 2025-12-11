package de.progeek.kimai.shared.ui.form.context

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import kotlinx.serialization.KSerializer

interface FormComponentContext : ComponentContext {
    val formParams: TimesheetFormParams
}

fun <C : Any, T : Any> FormComponentContext.formChildStack(
    source: StackNavigation<C>,
    serializer: KSerializer<C>,
    initialStack: () -> List<C>,
    key: String = "DefaultStack",
    handleBackButton: Boolean = false,
    childFactory: (configuration: C, FormComponentContext) -> T
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
            DefaultFormComponentContext(
                componentContext = componentContext,
                formParams = formParams
            )
        )
    }
