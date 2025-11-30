package de.progeek.kimai.shared.ui.form.project

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.ui.form.context.FormComponentContext
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import de.progeek.kimai.shared.utils.coroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProjectFieldComponent(
    componentContext: FormComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    input: MutableSharedFlow<Input>,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val scope = coroutineScope(dispatchers.main + SupervisorJob())
    private val store = instanceKeeper.getStore {
        ProjectFieldStoreFactory(storeFactory).create(
            getTimesheet(componentContext.formParams),
            dispatchers.main,
            dispatchers.io
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ProjectFieldStore.State> = store.stateFlow

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            scope.launch {
                input.collectLatest {
                    when (it) {
                        is Input.CustomerUpdated -> store.accept(
                            ProjectFieldStore.Intent.CustomerUpdated(it.customer)
                        )
                    }
                }
            }

            store.labels.bindTo {
                when (it) {
                    is ProjectFieldStore.Label.ProjectChanged -> output(Output.UpdatedProject(it.project))
                }
            }
        }
    }

    fun onIntent(intent: ProjectFieldStore.Intent) {
        store.accept(intent)
    }

    sealed class Input() {
        data class CustomerUpdated(val customer: Customer) : Input()
    }

    sealed class Output {
        data class UpdatedProject(val project: Project) : Output()
    }

    companion object {
        private fun getTimesheet(params: TimesheetFormParams): Timesheet? {
            return when (params) {
                is TimesheetFormParams.AddTimesheet -> null
                is TimesheetFormParams.EditTimesheet -> params.timesheet
                is TimesheetFormParams.EditRunningTimesheet -> params.timesheet
                is TimesheetFormParams.StartTimesheet -> null
            }
        }
    }
}
