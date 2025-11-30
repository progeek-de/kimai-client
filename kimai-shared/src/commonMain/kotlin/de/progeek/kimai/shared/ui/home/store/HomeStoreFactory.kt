package de.progeek.kimai.shared.ui.home.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.repositories.activity.ActivityRepository
import de.progeek.kimai.shared.core.repositories.customer.CustomerRepository
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.ui.home.store.HomeStore.State
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

class HomeStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    private val activityRepository by inject<ActivityRepository>()
    private val projectRepository by inject<ProjectRepository>()
    private val customerRepository by inject<CustomerRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): HomeStore =
        object :
            HomeStore,
            Store<Unit, State, Unit> by storeFactory.create(
                name = "HomeStore",
                initialState = State(),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed interface Action {
        data object Sync : Action
    }

    private sealed interface Msg {
        data class Loading(val isLoading: Boolean) : Msg
    }

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.Sync)
        }
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<Unit, Action, State, Msg, Unit>(mainContext) {

        override fun executeAction(action: Action, getState: () -> State) {
            dispatch(Msg.Loading(true))
            when (action) {
                Action.Sync -> sync()
            }
        }

        private fun sync() {
            scope.launch {
                withContext(ioContext) {
                    activityRepository.invalidateCache()
                    projectRepository.invalidateCache()
                    customerRepository.invalidateCache()
                }

                dispatch(Msg.Loading(false))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.Loading -> copy(isLoading = msg.isLoading)
            }
    }
}
