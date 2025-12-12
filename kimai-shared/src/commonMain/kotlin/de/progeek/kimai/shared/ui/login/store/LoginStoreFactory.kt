package de.progeek.kimai.shared.ui.login.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.BuildKonfig
import de.progeek.kimai.shared.core.repositories.auth.AuthRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.ui.login.store.LoginStore.Intent
import de.progeek.kimai.shared.ui.login.store.LoginStore.State
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

internal class LoginStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    private val authRepository by inject<AuthRepository>()
    private val settingsRepository by inject<SettingsRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): LoginStore =
        object :
            LoginStore,
            Store<Intent, State, Nothing> by storeFactory.create(
                name = "LoginStore",
                initialState = State(
                    isLoggedIn = false,
                    isLoading = false,
                    isError = false,
                    baseUrl = BuildKonfig.KIMAI_SERVER,
                    version = BuildKonfig.KIMAI_VER
                ),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = {
                    ExecutorImpl(mainContext, ioContext)
                },
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data object LoginSuccess : Msg()
        data object LoginFailed : Msg()
        data object LoginLoading : Msg()
        data class BaseUrl(val baseUrl: String, val isValid: Boolean) : Msg()
    }

    companion object {
        private val URL_PATTERN = Regex(
            "^https?://([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}(:\\d{1,5})?(/.*)?$|" +
                "^https?://localhost(:\\d{1,5})?(/.*)?$|" +
                "^https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?(/.*)?$"
        )

        fun isValidUrl(url: String): Boolean {
            return url.isNotBlank() && URL_PATTERN.matches(url.trim())
        }
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<Intent, Unit, State, Msg, Nothing>(mainContext) {

        override fun executeAction(action: Unit) {
            val baseUrl = settingsRepository.getBaseUrl()
            dispatch(Msg.BaseUrl(baseUrl, isValidUrl(baseUrl)))
        }

        override fun executeIntent(intent: Intent): Unit =
            when (intent) {
                is Intent.Login -> login(intent.email, intent.password, state().baseUrl)
                is Intent.BaseUrl -> onChangeBaseUrl(intent.baseUrl)
            }

        private fun onChangeBaseUrl(baseUrl: String) {
            val url= baseUrl.replace("\\s".toRegex(), "")
            dispatch(Msg.BaseUrl(url, isValidUrl(url)))
        }

        private fun login(email: String, password: String, baseUrl: String) {
            scope.launch {
                dispatch(Msg.LoginLoading)
                val credentials = withContext(ioContext) {
                    authRepository.login(email, password, baseUrl)
                }

                when (credentials != null) {
                    true -> dispatch(Msg.LoginSuccess)
                    false -> dispatch(Msg.LoginFailed)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.LoginSuccess -> copy(isLoggedIn = true, isLoading = false, isError = false)
                is Msg.LoginFailed -> copy(isLoggedIn = false, isLoading = false, isError = true)
                is Msg.LoginLoading -> copy(isLoading = true, isError = false)
                is Msg.BaseUrl -> copy(baseUrl = msg.baseUrl, isBaseUrlValid = msg.isValid)
            }
    }
}
