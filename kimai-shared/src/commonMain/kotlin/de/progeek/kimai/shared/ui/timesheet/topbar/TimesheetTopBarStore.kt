package de.progeek.kimai.shared.ui.timesheet.topbar

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.BuildKonfig
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.core.repositories.auth.AuthRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.ui.timesheet.topbar.TimesheetTopBarStore.*
import de.progeek.kimai.shared.utils.browseUrl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

interface TimesheetTopBarStore : Store<Intent, State, Label> {

    sealed class Intent {
        data class SetMode(val mode: EntryMode) : Intent()
        data class ToggleTheme(val theme: ThemeEnum) : Intent()
        data object Logout : Intent()
        data object ShowDashboard : Intent()
        data object Reload : Intent()
    }

    data class State(
        val baseUrl: String = BuildKonfig.KIMAI_SERVER,
        val mode: EntryMode = EntryMode.TIMER,
        val running: Boolean = false,
        val theme: ThemeEnum = ThemeEnum.LIGHT
    )

    sealed class Label {
        data object Reload : Label()
    }
}

class TimesheetTopBarStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    private val settingsRepository by inject<SettingsRepository>()
    private val authRepository by inject<AuthRepository>()
    private val timesheetRepository by inject<TimesheetRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): TimesheetTopBarStore =
        object :
            TimesheetTopBarStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = "TimesheetTopBarStore",
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data class BaseUrl(val baseUrl: String) : Msg()
        data class LoadedEntryMode(val mode: EntryMode) : Msg()
        data class RunningTimesheet(val value: Boolean) : Msg()
        data class ThemeChanged(val theme: ThemeEnum) : Msg()
    }

    private inner class ExecutorImpl(mainContext: CoroutineContext, private val ioContext: CoroutineContext) : CoroutineExecutor<Intent, Unit, State, Msg, Label>(mainContext) {
        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.Logout -> logout()
                is Intent.Reload -> publish(Label.Reload)
                is Intent.SetMode -> saveEntryMode(intent.mode)
                is Intent.ShowDashboard -> browseUrl(state().baseUrl)
                is Intent.ToggleTheme -> saveTheme(intent.theme)
            }
        }

        private fun logout() {
            scope.launch {
                val result = withContext(ioContext) {
                    authRepository.logout()
                }

                result.onSuccess {
                    println("Logged out: User credentials deleted")
                }
            }
        }

        override fun executeAction(action: Unit) {
            val baseUrl = settingsRepository.getBaseUrl()
            dispatch(Msg.BaseUrl(baseUrl))

            scope.launch {
                timesheetRepository.getRunningTimesheetStream().flowOn(ioContext).collectLatest {
                    dispatch(Msg.RunningTimesheet(value = it != null))

                    if (it != null) {
                        saveEntryMode(EntryMode.TIMER)
                    }
                }
            }

            scope.launch {
                settingsRepository.getEntryMode().flowOn(ioContext).collectLatest {
                    dispatch(Msg.LoadedEntryMode(it))
                }
            }

            scope.launch {
                settingsRepository.getTheme().flowOn(ioContext).collectLatest {
                    dispatch(Msg.ThemeChanged(it))
                }
            }
        }

        private fun saveEntryMode(mode: EntryMode) {
            scope.launch(ioContext) {
                settingsRepository.saveEntryMode(mode)
            }
        }

        private fun saveTheme(theme: ThemeEnum) {
            scope.launch {
                val result = withContext(ioContext) {
                    settingsRepository.saveTheme(theme)
                }
                dispatch(Msg.ThemeChanged(result))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.LoadedEntryMode -> copy(mode = msg.mode)
                is Msg.RunningTimesheet -> copy(running = msg.value)
                is Msg.BaseUrl -> copy(baseUrl = msg.baseUrl)
                is Msg.ThemeChanged -> copy(theme = msg.theme)
            }
    }
}
