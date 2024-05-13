package de.progeek.kimai.shared.ui.form.context

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigationSource
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams

interface FormComponentContext : ComponentContext {
    val formParams: TimesheetFormParams
}

inline fun <reified C : Parcelable, T : Any> FormComponentContext.formChildStack(
    source: StackNavigationSource<C>,
    noinline initialStack: () -> List<C>,
    key: String = "DefaultStack",
    persistent: Boolean = true,
    handleBackButton: Boolean = false,
    noinline childFactory: (configuration: C, FormComponentContext) -> T
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
            DefaultFormComponentContext(
                componentContext = componentContext,
                formParams = formParams
            )
        )
    }