@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.form.time.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import de.progeek.kimai.shared.ui.form.time.TimeFieldMode
import de.progeek.kimai.shared.ui.form.time.store.TimeFieldStore.*
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

interface TimeFieldStore : Store<Intent, State, Label> {
    data class State(
        val mode: TimeFieldMode,
        val begin: LocalDateTime,
        val end: LocalDateTime?
    )

    sealed class Intent {
        data class BeginChanged(val begin: LocalDateTime) : Intent()
        data class EndChanged(val end: LocalDateTime) : Intent()
    }

    sealed class Message {
        data class BeginChanged(val begin: LocalDateTime) : Message()
        data class EndChanged(val end: LocalDateTime) : Message()
    }

    sealed class Label {
        data class BeginChanged(val begin: LocalDateTime) : Label()
        data class EndChanged(val end: LocalDateTime) : Label()
    }
}

internal class TimeFieldStoreFactory(private val storeFactory: StoreFactory) : KoinComponent {

    fun create(params: TimesheetFormParams, mainContext: CoroutineContext, ioContext: CoroutineContext): TimeFieldStore {
        fun getState(params: TimesheetFormParams): State {
            return when (params) {
                is TimesheetFormParams.AddTimesheet -> {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    State(TimeFieldMode.ADD, now, now)
                }
                is TimesheetFormParams.EditTimesheet -> {
                    State(TimeFieldMode.EDIT, params.timesheet.begin, params.timesheet.end)
                }
                is TimesheetFormParams.EditRunningTimesheet -> {
                    State(TimeFieldMode.EDIT_RUNNING, params.timesheet.begin, null)
                }
                is TimesheetFormParams.StartTimesheet -> {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    State(TimeFieldMode.START, now, null)
                }
            }
        }

        return object :
            TimeFieldStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = "TimeFieldStore",
                initialState = getState(params),
                executorFactory = { TimeFieldExecutor(mainContext, ioContext) },
                reducer = TimeFieldReducer
            ) {}
    }
}
