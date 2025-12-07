package de.progeek.kimai.shared.ui.form

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.models.*
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.ui.form.FormStore.*
import de.progeek.kimai.shared.ui.form.time.TimeFieldMode
import de.progeek.kimai.shared.ui.timesheet.models.TimesheetFormParams
import de.progeek.kimai.shared.utils.notNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

interface FormStore : Store<Intent, State, Label> {
    sealed class Intent {
        data class ProjectUpdated(val project: Project) : Intent()
        data class ActivityUpdated(val activity: Activity) : Intent()
        data class CustomerUpdated(val customer: Customer) : Intent()
        data class DescriptionUpdated(val description: String) : Intent()
        data class BeginUpdated(val begin: LocalDateTime) : Intent()
        data class EndUpdated(val end: LocalDateTime) : Intent()
        data object Save : Intent()
        data object Delete : Intent()
    }

    data class State(
        val mode: TimeFieldMode,
        val id: Long? = null,
        val begin: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        val end: LocalDateTime? = null,
        val customer: Customer? = null,
        val project: Project? = null,
        val activity: Activity? = null,
        val description: String = ""
    )

    sealed class Label {
        data object Close : Label()
    }
}

fun State.toTimesheetForm(): TimesheetForm {
    return TimesheetForm(
        id = this.id,
        project = this.project,
        activity = this.activity,
        begin = this.begin,
        end = this.end,
        description = this.description
    )
}

internal class FormStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    private val timesheetRepository by inject<TimesheetRepository>()

    fun create(params: TimesheetFormParams, mainContext: CoroutineContext, ioContext: CoroutineContext): FormStore {
        fun timesheetToState(mode: TimeFieldMode, timesheet: Timesheet): State {
            return State(
                mode = mode,
                id = timesheet.id,
                begin = timesheet.begin,
                end = timesheet.end,
                customer = timesheet.project.customer,
                project = timesheet.project,
                activity = timesheet.activity,
                description = timesheet.description ?: ""
            )
        }

        fun buildInitialState(params: TimesheetFormParams): State {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            return when (params) {
                is TimesheetFormParams.AddTimesheet -> {
                    State(mode = TimeFieldMode.ADD, begin = now, end = now, description = params.description ?: "")
                }
                is TimesheetFormParams.EditTimesheet -> timesheetToState(TimeFieldMode.EDIT, params.timesheet)
                is TimesheetFormParams.EditRunningTimesheet -> timesheetToState(TimeFieldMode.EDIT_RUNNING, params.timesheet)
                is TimesheetFormParams.StartTimesheet -> {
                    State(mode = TimeFieldMode.START, begin = now, description = params.description ?: "")
                }
            }
        }

        return object :
            FormStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = "FormStore",
                initialState = buildInitialState(params),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}
    }

    private sealed class Msg {
        data object Loading : Msg()
        data class Success(val timesheet: Timesheet) : Msg()

        data class ProjectUpdated(val project: Project) : Msg()
        data class ActivityUpdated(val activity: Activity) : Msg()
        data class CustomerUpdated(val customer: Customer) : Msg()
        data class DescriptionUpdated(val description: String) : Msg()
        data class BeginUpdated(val begin: LocalDateTime) : Msg()
        data class EndUpdated(val end: LocalDateTime) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) :
        CoroutineExecutor<Intent, Unit, State, Msg, Label>(mainContext) {

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.ActivityUpdated -> dispatch(Msg.ActivityUpdated(intent.activity))
                is Intent.BeginUpdated -> dispatch(Msg.BeginUpdated(intent.begin))
                is Intent.CustomerUpdated -> dispatch(Msg.CustomerUpdated(intent.customer))
                is Intent.Delete -> deleteTimesheet(getState().id)
                is Intent.DescriptionUpdated -> dispatch(Msg.DescriptionUpdated(intent.description))
                is Intent.EndUpdated -> dispatch(Msg.EndUpdated(intent.end))
                is Intent.ProjectUpdated -> dispatch(Msg.ProjectUpdated(intent.project))
                is Intent.Save -> save(getState())
            }
        }

        private fun save(state: State) {
            val timesheet = state.toTimesheetForm()
            when (state.mode) {
                TimeFieldMode.ADD -> addTimesheet(timesheet)
                TimeFieldMode.START -> createTimesheet(timesheet)
                TimeFieldMode.EDIT_RUNNING -> updateTimesheet(timesheet)
                TimeFieldMode.EDIT -> updateTimesheet(timesheet)
            }
        }

        private fun addTimesheet(timesheet: TimesheetForm) {
            dispatch(Msg.Loading)
            scope.launch {
                val result = withContext(ioContext) {
                    timesheetRepository.addTimesheet(timesheet)
                }

                result.onSuccess {
                    publish(Label.Close)
                }
            }
        }

        private fun createTimesheet(timesheet: TimesheetForm) {
            dispatch(Msg.Loading)
            scope.launch {
                val result = withContext(ioContext) {
                    timesheetRepository.createTimesheet(timesheet)
                }

                result.onSuccess {
                    publish(Label.Close)
                }
            }
        }

        private fun updateTimesheet(timesheet: TimesheetForm) {
            dispatch(Msg.Loading)
            scope.launch {
                val result = withContext(ioContext) {
                    timesheetRepository.updateTimesheet(timesheet)
                }

                result.onSuccess {
                    publish(Label.Close)
                }
            }
        }

        private fun deleteTimesheet(id: Long?) {
            id.notNull {
                dispatch(Msg.Loading)
                scope.launch {
                    val result = withContext(ioContext) {
                        timesheetRepository.deleteTimesheet(id!!)
                    }

                    result.onSuccess {
                        publish(Label.Close)
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State {
            return when (msg) {
                is Msg.ActivityUpdated -> copy(activity = msg.activity)
                is Msg.BeginUpdated -> copy(begin = msg.begin)
                is Msg.CustomerUpdated -> copy(customer = msg.customer)
                is Msg.DescriptionUpdated -> copy(description = msg.description)
                is Msg.EndUpdated -> copy(end = msg.end)
                is Msg.Loading -> copy()
                is Msg.ProjectUpdated -> copy(project = msg.project)
                is Msg.Success -> copy()
            }
        }
    }
}
