package de.progeek.kimai.shared.ui.form.customer

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.repositories.customer.CustomerRepository
import de.progeek.kimai.shared.ui.form.customer.CustomerFieldStore.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

interface CustomerFieldStore : Store<Intent, State, Label> {

    sealed class Intent {
        data class SelectedCustomer(val customer: Customer) : Intent()
    }

    data class State(
        internal val customers: List<Customer> = emptyList(),
        val selectedCustomer: Customer? = null
    )

    sealed interface Label {
        data class CustomerChanged(val customer: Customer?) : Label
    }
}

class CustomerFieldStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {
    private val customerRepository by inject<CustomerRepository>()

    fun create(timesheet: Timesheet? = null, mainContext: CoroutineContext, ioContext: CoroutineContext): CustomerFieldStore =
        object :
            CustomerFieldStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = "CustomerFieldStore",
                initialState = State(selectedCustomer = timesheet?.project?.customer),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data class LoadedCustomers(val projects: List<Customer>) : Msg()
        data class SelectedCustomer(val customer: Customer) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<Intent, Unit, State, Msg, Label>(mainContext) {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.SelectedCustomer -> handleSelectedProject(intent.customer)
            }
        }

        private fun handleSelectedProject(customer: Customer) {
            dispatch(Msg.SelectedCustomer(customer))
            publish(Label.CustomerChanged(customer))
        }

        override fun executeAction(action: Unit, getState: () -> State) {
            loadCustomers()
        }

        private fun loadCustomers() {
            scope.launch {
                customerRepository.getCustomers().flowOn(ioContext).collect {
                    dispatch(Msg.LoadedCustomers(it))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.LoadedCustomers -> copy(customers = msg.projects)
                is Msg.SelectedCustomer -> copy(
                    selectedCustomer = msg.customer
                )
            }
    }
}
