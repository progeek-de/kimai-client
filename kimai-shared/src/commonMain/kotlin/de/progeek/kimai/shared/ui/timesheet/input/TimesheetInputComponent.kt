package de.progeek.kimai.shared.ui.timesheet.input

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.mapper.toTimesheet
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class TimesheetInputComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        TimesheetInputStoreFactory(storeFactory).create(dispatchers.main, dispatchers.io)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TimesheetInputStore.State> = store.stateFlow

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.labels.bindTo {
                when(it) {
                    is TimesheetInputStore.Label.AddTimesheet -> output(
                        Output.ShowForm(TimesheetFormParams.AddTimesheet(it.description))
                    )
                    is TimesheetInputStore.Label.EditTimesheet -> output(
                        Output.ShowForm(TimesheetFormParams.EditRunningTimesheet(it.form.toTimesheet()))
                    )
                    is TimesheetInputStore.Label.StartTimesheet -> output(
                        Output.ShowForm(TimesheetFormParams.StartTimesheet(it.description))
                    )
                }
            }
        }
    }

    fun onIntent(intent: TimesheetInputStore.Intent) {
        store.accept(intent)
    }

    sealed class Output {
        data class ShowForm(val data: TimesheetFormParams) : Output()
    }
}