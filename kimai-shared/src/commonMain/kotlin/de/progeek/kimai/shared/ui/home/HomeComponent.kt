package de.progeek.kimai.shared.ui.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.ui.form.FormComponent
import de.progeek.kimai.shared.ui.form.context.DefaultFormComponentContext
import de.progeek.kimai.shared.ui.home.store.HomeStore
import de.progeek.kimai.shared.ui.home.store.HomeStoreFactory
import de.progeek.kimai.shared.ui.settings.SettingsComponent
import de.progeek.kimai.shared.ui.timesheet.TimesheetComponent
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class HomeComponent(
    componentContext: ComponentContext,
    val storeFactory: StoreFactory,
    val dispatchers: KimaiDispatchers,
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        HomeStoreFactory(storeFactory = storeFactory).create(dispatchers.main, dispatchers.io)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeStore.State> = store.stateFlow

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            initialConfiguration = Configuration.Timesheet,
            childFactory = ::createChild
        )

    val childStack: Value<ChildStack<*, Child>> = stack

    private fun createChild(
        configuration: Configuration,
        componentContext: ComponentContext
    ): Child =
        when (configuration) {
            is Configuration.Form -> Child.Form(
                FormComponent(DefaultFormComponentContext(componentContext, configuration.data),
                    storeFactory,
                    dispatchers,
                    ::onEditOutput
                )
            )

            is Configuration.Timesheet -> Child.Timesheet(
                TimesheetComponent(componentContext, storeFactory, dispatchers, ::onTimesheetOutput)
            )

            is Configuration.Settings -> Child.Settings(
                SettingsComponent(componentContext, storeFactory, dispatchers, ::onSettingsOutput)
            )
        }

    private fun onTimesheetOutput(out: TimesheetComponent.Output) {
        when (out) {
            is TimesheetComponent.Output.ShowSettings -> navigation.push(Configuration.Settings)
            is TimesheetComponent.Output.ShowForm -> navigation.push(
                Configuration.Form(out.data)
            )
        }
    }

    private fun onEditOutput(output: FormComponent.Output): Unit =
        when (output) {
            is FormComponent.Output.Close -> navigation.pop()
        }

    private fun onSettingsOutput(output: SettingsComponent.Output) : Unit = when (output) {
        is SettingsComponent.Output.Close -> navigation.pop()
    }

    private sealed class Configuration : Parcelable {
        @Parcelize
        data class Form(val data: TimesheetFormParams) : Configuration()

        @Parcelize
        data object Timesheet : Configuration()

        @Parcelize
        data object Settings : Configuration()
    }

    sealed class Child {
        data class Form(val component: FormComponent) : Child()
        data class Timesheet(val component: TimesheetComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
    }
}