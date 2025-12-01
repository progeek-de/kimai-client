package de.progeek.kimai.shared.ui.root.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.core.sync.JiraSyncScheduler
import de.progeek.kimai.shared.ui.root.store.RootStore.State
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.utils.isNull
import de.progeek.kimai.shared.utils.notNull
import de.progeek.kimai.shared.utils.setDefaultLocale
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

class RootStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    private val credentialsRepository by inject<CredentialsRepository>()
    private val settingsRepository by inject<SettingsRepository>()
    private val jiraSyncScheduler by inject<JiraSyncScheduler>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): RootStore =
        object :
            RootStore,
            Store<Nothing, State, Nothing> by storeFactory.create(
                name = "RootStore",
                initialState = State(credentials = null, isLoading = true, theme = ThemeEnum.LIGHT),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data class Finished(val credentials: Credentials?) : Msg()
        data class Theme(var theme: ThemeEnum) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<Nothing, Unit, State, Msg, Nothing>(mainContext) {

        override fun executeAction(action: Unit, getState: () -> State) {
            loadCredentials()
            loadTheme()
            loadLanguage()
            startJiraSync()
        }

        private fun loadCredentials() {
            scope.launch {
                credentialsRepository.get().flowOn(ioContext).collectLatest {
                    dispatch(Msg.Finished(it))
                }
            }
        }

        private fun loadTheme() {
            scope.launch {
                settingsRepository.getTheme().flowOn(ioContext).collectLatest {
                    dispatch(Msg.Theme(it))
                }
            }
        }

        private fun loadLanguage() {
            scope.launch {
                settingsRepository.getLanguage().flowOn(ioContext).collect {
                    it.notNull {
                        StringDesc.localeType = StringDesc.LocaleType.Custom(it)
                        setDefaultLocale(it)
                    }.isNull {
                        StringDesc.localeType = StringDesc.LocaleType.System
                    }
                }
            }
        }

        private fun startJiraSync() {
            // Start the injected Jira sync scheduler
            jiraSyncScheduler.start()
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.Finished -> copy(credentials = msg.credentials, isLoading = false)
                is Msg.Theme -> copy(theme = msg.theme)
            }
    }
}
