package de.progeek.kimai.shared.ui.form

import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.ui.form.FormStore.Intent.ProjectUpdated
import de.progeek.kimai.shared.ui.form.activity.ActivityFieldComponent
import de.progeek.kimai.shared.ui.form.activity.ActivityFieldComponent.Output.UpdatedActivity
import de.progeek.kimai.shared.ui.form.context.FormComponentContext
import de.progeek.kimai.shared.ui.form.customer.CustomerFieldComponent
import de.progeek.kimai.shared.ui.form.customer.CustomerFieldComponent.Output.UpdatedCustomer
import de.progeek.kimai.shared.ui.form.project.ProjectFieldComponent
import de.progeek.kimai.shared.ui.form.project.ProjectFieldComponent.Output.UpdatedProject
import de.progeek.kimai.shared.ui.form.time.TimeFieldComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class FormComponent(
    componentContext: FormComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit,
) : FormComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        FormStoreFactory(storeFactory).create(componentContext.formParams, dispatchers.main, dispatchers.io)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<FormStore.State> = store.stateFlow

    private val projectFieldInput = MutableSharedFlow<ProjectFieldComponent.Input>(extraBufferCapacity = Int.MAX_VALUE)
    val projectFieldComponent = ProjectFieldComponent(componentContext, storeFactory, dispatchers, projectFieldInput, ::onProjectFieldOutput)
    val customerFieldComponent = CustomerFieldComponent(componentContext, storeFactory, dispatchers, ::onCustomerFieldOutput)

    private val activityInput = MutableSharedFlow<ActivityFieldComponent.Input>(extraBufferCapacity = Int.MAX_VALUE)
    val activityFieldComponent = ActivityFieldComponent(componentContext, storeFactory, dispatchers, activityInput, ::onActivityFieldOutput)
    val timeFieldComponent = TimeFieldComponent(componentContext, storeFactory, dispatchers, ::onTimeFieldOutput)

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.labels.bindTo {
                when(it) {
                    FormStore.Label.Close -> output(Output.Close)
                }
            }
        }
    }

    private fun onProjectFieldOutput(output: ProjectFieldComponent.Output) {
        when(output) {
            is UpdatedProject -> {
                store.accept(ProjectUpdated(output.project))
                activityInput.tryEmit(
                    ActivityFieldComponent.Input.UpdatedProject(output.project)
                )
            }
        }
    }

    private fun onCustomerFieldOutput(output: CustomerFieldComponent.Output) {
        when(output) {
            is UpdatedCustomer -> {
                store.accept(FormStore.Intent.CustomerUpdated(output.customer))
                projectFieldInput.tryEmit(
                    ProjectFieldComponent.Input.CustomerUpdated(output.customer)
                )
            }
        }
    }

    private fun onActivityFieldOutput(output: ActivityFieldComponent.Output) {
        when(output) {
            is UpdatedActivity -> store.accept(FormStore.Intent.ActivityUpdated(output.activity))
        }
    }

    private fun onTimeFieldOutput(output: TimeFieldComponent.Output) {
        when(output) {
            is TimeFieldComponent.Output.BeginChanged -> store.accept(FormStore.Intent.BeginUpdated(output.begin))
            is TimeFieldComponent.Output.EndChanged -> store.accept(FormStore.Intent.EndUpdated(output.end))
        }
    }

    fun onIntent(intent: FormStore.Intent) {
        store.accept(intent)
    }

    fun onOutput(out: Output) {
        output(out)
    }

    sealed class Output {
        data object Close : Output()
    }
}