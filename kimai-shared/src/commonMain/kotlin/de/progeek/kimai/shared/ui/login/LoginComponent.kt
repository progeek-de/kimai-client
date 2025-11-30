package de.progeek.kimai.shared.ui.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.ui.login.store.LoginStore
import de.progeek.kimai.shared.ui.login.store.LoginStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class LoginComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        LoginStoreFactory(storeFactory).create(dispatchers.main, dispatchers.io)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LoginStore.State> = store.stateFlow

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            state.bindTo {
                if (it.isLoggedIn) {
                    output(Output.Success)
                }
            }
        }
    }

    fun onLoginClick(email: String, password: String) {
        store.accept(LoginStore.Intent.Login(email, password))
    }

    fun changedBaseUrl(baseUrl: String) {
        store.accept(LoginStore.Intent.BaseUrl(baseUrl))
    }

    sealed class Output {
        data object Success : Output()
    }
}
