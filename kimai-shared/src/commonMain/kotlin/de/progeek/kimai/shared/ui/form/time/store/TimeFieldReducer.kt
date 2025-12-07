package de.progeek.kimai.shared.ui.form.time.store

import com.arkivanov.mvikotlin.core.store.Reducer
import de.progeek.kimai.shared.ui.form.time.store.TimeFieldStore.Message
import de.progeek.kimai.shared.ui.form.time.store.TimeFieldStore.State

object TimeFieldReducer : Reducer<State, Message> {
    override fun State.reduce(msg: Message): State {
        return when (msg) {
            is Message.BeginChanged -> copy(begin = msg.begin)
            is Message.EndChanged -> copy(end = msg.end)
        }
    }
}
