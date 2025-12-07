package de.progeek.kimai.shared.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.ui.components.loading.LoadingComponent
import de.progeek.kimai.shared.ui.home.HomeComponent
import de.progeek.kimai.shared.ui.login.LoginComponent
import de.progeek.kimai.shared.ui.root.store.RootStore
import de.progeek.kimai.shared.ui.root.store.RootStoreFactory
import de.progeek.kimai.shared.utils.isNull
import de.progeek.kimai.shared.utils.notNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class RootComponent(
    componentContext: ComponentContext,
    val storeFactory: StoreFactory,
    val dispatchers: KimaiDispatchers
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        RootStoreFactory(storeFactory).create(dispatchers.main, dispatchers.io)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<RootStore.State> = store.stateFlow

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            state.bindTo {
                if (!it.isLoading) {
                    it.credentials.notNull { navigation.replaceAll(Configuration.Home) }
                        .isNull { navigation.replaceAll(Configuration.Login) }
                }
            }
        }
    }

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            initialConfiguration = Configuration.Loading,
            childFactory = ::createChild
        )

    val childStack: Value<ChildStack<*, Child>> = stack

    private fun createChild(configuration: Configuration, componentContext: ComponentContext): Child =
        when (configuration) {
            is Configuration.Login -> Child.Login(LoginComponent(componentContext, storeFactory, dispatchers, ::onLoginOutput))
            is Configuration.Home -> Child.Home(HomeComponent(componentContext, storeFactory, dispatchers))
            is Configuration.Loading -> Child.Loading(LoadingComponent(componentContext))
        }

    private fun onLoginOutput(output: LoginComponent.Output): Unit =
        println("Login $output")

    private sealed class Configuration : Parcelable {
        @Parcelize
        data object Login : Configuration()

        @Parcelize
        data object Home : Configuration()

        @Parcelize
        data object Loading : Configuration()
    }

    sealed class Child {
        data class Login(val component: LoginComponent) : Child()
        data class Home(val component: HomeComponent) : Child()
        data class Loading(val component: LoadingComponent) : Child()
    }
}
