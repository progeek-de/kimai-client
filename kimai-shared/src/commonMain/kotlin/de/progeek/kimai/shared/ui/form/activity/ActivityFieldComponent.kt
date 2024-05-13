package de.progeek.kimai.shared.ui.form.activity

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.kimaiDispatchers
import de.progeek.kimai.shared.ui.form.activity.ActivityFieldStore.Label.ActivityChanged
import de.progeek.kimai.shared.ui.form.context.FormComponentContext
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import de.progeek.kimai.shared.utils.coroutineScope
import io.ktor.http.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class ActivityFieldComponent(
    componentContext: FormComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    input: MutableSharedFlow<Input>,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val scope = coroutineScope(kimaiDispatchers.main + SupervisorJob())

    private val store = instanceKeeper.getStore {
        ActivityFieldStoreFactory(storeFactory).create(
            getTimesheet(componentContext.formParams),
            dispatchers.main,
            dispatchers.io
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ActivityFieldStore.State> = store.stateFlow

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            scope.launch {
                input.collectLatest {
                    when(it) {
                        is Input.UpdatedProject -> store.accept(ActivityFieldStore.Intent.UpdatedProject(it.project))
                    }
                }
            }

            store.labels.bindTo {
                when(it) {
                    is ActivityChanged -> output(Output.UpdatedActivity(it.activity))
                }
            }
        }
    }

    fun onIntent(intent: ActivityFieldStore.Intent) {
        store.accept(intent)
    }

    sealed class Input {
        data class UpdatedProject(val project: Project) : Input()
    }

    sealed class Output {
        data class UpdatedActivity(val activity: Activity) : Output()
    }

    companion object {
        private fun getTimesheet(params: TimesheetFormParams): Timesheet? {
            return when(params) {
                is TimesheetFormParams.AddTimesheet -> null
                is TimesheetFormParams.EditTimesheet -> params.timesheet
                is TimesheetFormParams.EditRunningTimesheet -> params.timesheet
                is TimesheetFormParams.StartTimesheet -> null
            }
        }
    }
}