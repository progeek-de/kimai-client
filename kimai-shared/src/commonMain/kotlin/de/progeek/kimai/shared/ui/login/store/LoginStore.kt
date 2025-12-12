package de.progeek.kimai.shared.ui.login.store

import com.arkivanov.mvikotlin.core.store.Store
import de.progeek.kimai.shared.ui.login.store.LoginStore.Intent
import de.progeek.kimai.shared.ui.login.store.LoginStore.State

interface LoginStore : Store<Intent, State, Nothing> {

    sealed class Intent {
        data class Login(val email: String, val password: String) : Intent()
        data class BaseUrl(val baseUrl: String) : Intent()
    }

    data class State(
        val baseUrl: String,
        val version: String,
        val isLoggedIn: Boolean,
        val isLoading: Boolean,
        val isError: Boolean,
        val isBaseUrlValid: Boolean = true
    )
}
