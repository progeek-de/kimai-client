package de.progeek.kimai.shared.ui.form.time.store

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.ui.form.time.store.TimeFieldStore.*
import kotlinx.datetime.LocalDateTime
import kotlin.coroutines.CoroutineContext

class TimeFieldExecutor(mainContext: CoroutineContext, ioContext: CoroutineContext)
    : CoroutineExecutor<Intent, Unit, State, Message, Label>(mainContext)
{
    override fun executeIntent(intent: Intent, getState: () -> State) {
        when (intent) {
            is Intent.BeginChanged -> beginChanged(intent.begin)
            is Intent.EndChanged -> endChanged(intent.end)
        }
    }

    private fun beginChanged(begin: LocalDateTime) {
        dispatch(Message.BeginChanged(begin))
        publish(Label.BeginChanged(begin))
    }

    private fun endChanged(end: LocalDateTime) {
        dispatch(Message.EndChanged(end))
        publish(Label.EndChanged(end))
    }
}
