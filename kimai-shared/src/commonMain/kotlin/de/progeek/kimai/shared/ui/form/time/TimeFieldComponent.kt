package de.progeek.kimai.shared.ui.form.time

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.ui.form.context.FormComponentContext
import de.progeek.kimai.shared.ui.form.time.store.TimeFieldStore
import de.progeek.kimai.shared.ui.form.time.store.TimeFieldStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDateTime

class TimeFieldComponent(
    componentContext: FormComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit,
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        TimeFieldStoreFactory(storeFactory).create(
            componentContext.formParams,
            dispatchers.main,
            dispatchers.io
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TimeFieldStore.State> = store.stateFlow

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.labels.bindTo {
                when(it) {
                    is TimeFieldStore.Label.BeginChanged -> output(Output.BeginChanged(it.begin))
                    is TimeFieldStore.Label.EndChanged -> output(Output.EndChanged(it.end))
                }
            }
        }
    }

    fun onIntent(intent: TimeFieldStore.Intent) {
        store.accept(intent)
    }

    sealed class Output {
        data class BeginChanged(val begin: LocalDateTime) : Output()
        data class EndChanged(val end: LocalDateTime) : Output()
    }
}
