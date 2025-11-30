package de.progeek.kimai.shared.ui.form.customer

import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.ui.form.context.FormComponentContext
import de.progeek.kimai.shared.ui.form.customer.CustomerFieldStore.Label.CustomerChanged
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class CustomerFieldComponent(
    componentContext: FormComponentContext,
    val storeFactory: StoreFactory,
    private val dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit
) : FormComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        CustomerFieldStoreFactory(storeFactory).create(
            getTimesheet(componentContext.formParams),
            dispatchers.main,
            dispatchers.io
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<CustomerFieldStore.State> = store.stateFlow

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.labels.bindTo {
                when (it) {
                    is CustomerChanged -> it.customer?.apply {
                        output(Output.UpdatedCustomer(this))
                    }
                }
            }
        }
    }

    fun onIntent(intent: CustomerFieldStore.Intent) {
        store.accept(intent)
    }

    sealed class Output {
        data class UpdatedCustomer(val customer: Customer) : Output()
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
