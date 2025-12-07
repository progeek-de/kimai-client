package de.progeek.kimai.shared.ui.home.store

import com.arkivanov.mvikotlin.core.store.Store
import de.progeek.kimai.shared.ui.home.store.HomeStore.State

interface HomeStore : Store<Unit, State, Unit> {

    data class State(
        val isLoading: Boolean = true
    )
}
