package de.progeek.kimai.shared.ui.ticketsystem.picker

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

/**
 * Component for managing ticket picker dialog.
 * Supports issues from multiple ticket system sources.
 */
class TicketPickerComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        TicketPickerStoreFactory(storeFactory = storeFactory).create(dispatchers.main, dispatchers.io)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TicketPickerStore.State> = store.stateFlow

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.labels.bindTo {
                when (it) {
                    is TicketPickerStore.Label.IssueSelected -> {
                        output(Output.IssueSelected(it.formattedText))
                    }
                    is TicketPickerStore.Label.Dismissed -> {
                        output(Output.Dismissed)
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        store.accept(TicketPickerStore.Intent.SearchQueryUpdated(query))
    }

    fun onIssueSelected(issue: TicketIssue) {
        store.accept(TicketPickerStore.Intent.IssueSelected(issue))
    }

    fun onRefresh() {
        store.accept(TicketPickerStore.Intent.Refresh)
    }

    fun onDismiss() {
        store.accept(TicketPickerStore.Intent.Dismiss)
    }

    sealed class Output {
        data class IssueSelected(val formattedText: String) : Output()
        data object Dismissed : Output()
    }
}
