package de.progeek.kimai.shared.ui.timesheet.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.kimaiDispatchers
import de.progeek.kimai.shared.utils.coroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TimesheetListComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    input: MutableSharedFlow<Input>,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val scope = coroutineScope(kimaiDispatchers.main + SupervisorJob())
    private val store = instanceKeeper.getStore {
        TimesheetListStoreFactory(storeFactory).create(dispatchers.main, dispatchers.io)
    }

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            scope.launch {
                input.collectLatest {
                    when (it) {
                        Input.Reload -> store.accept(TimesheetListStore.Intent.Refresh)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TimesheetListStore.State> = store.stateFlow

    fun onIntent(intent: TimesheetListStore.Intent) {
        store.accept(intent)
    }

    fun onOutput(output: Output) {
        output(output)
    }

    sealed class Input {
        data object Reload : Input()
    }

    sealed class Output {
        data class Edit(val timesheet: Timesheet) : Output()
    }
}
