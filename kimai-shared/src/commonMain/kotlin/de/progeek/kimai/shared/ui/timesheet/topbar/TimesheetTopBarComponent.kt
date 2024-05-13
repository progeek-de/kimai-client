package de.progeek.kimai.shared.ui.timesheet.topbar

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class TimesheetTopBarComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        TimesheetTopBarStoreFactory(storeFactory).create(dispatchers.main, dispatchers.io)
    }

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.labels.bindTo {
                when(it) {
                    TimesheetTopBarStore.Label.Reload -> output(Output.Reload)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TimesheetTopBarStore.State> = store.stateFlow

    fun onIntent(intent: TimesheetTopBarStore.Intent) {
        store.accept(intent)
    }

    fun onOutput(output: Output) {
        output(output)
    }

    sealed class Output {
        data object Reload : Output()
        data object ShowSettings : Output()
    }
}