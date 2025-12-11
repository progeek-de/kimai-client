@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.ui.timesheet.list

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.models.GroupedTimesheet
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.repositories.activity.ActivityRepository
import de.progeek.kimai.shared.core.repositories.customer.CustomerRepository
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.ui.timesheet.list.TimesheetListStore.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlin.time.Clock
import kotlin.time.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

class TimesheetListStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    private val timesheetRepository by inject<TimesheetRepository>()
    private val projectRepository by inject<ProjectRepository>()
    private val activityRepository by inject<ActivityRepository>()
    private val customerRepository by inject<CustomerRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): TimesheetListStore =
        object :
            TimesheetListStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = "TimesheetListStore",
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed interface Action

    private sealed class Msg {
        data class LoadedTimesheets(val timesheets: List<GroupedTimesheet>) : Msg()
        data class LoadedRunning(val form: TimesheetForm?) : Msg()
        data class NextPage(val page: Instant?) : Msg()
        data class Loading(val isLoading: Boolean) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<Intent, Unit, State, Msg, Label>(mainContext) {
        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.LoadNextItems -> {
                    loadTimesheets(state().page)
                }
                is Intent.Refresh -> refresh()
                is Intent.Restart -> restartTimesheet(intent.id)
            }
        }

        override fun executeAction(action: Unit) {
            loadTimesheetsStream()
            loadTimesheets(state().page)
        }

        private fun loadTimesheetsStream() {
            scope.launch {
                timesheetRepository.timesheetsStream().flowOn(ioContext).collectLatest { items ->
                    val filtered = items.filter { it.end != null }
                    val grouped = groupTimesheets(filtered)
                    dispatch(Msg.LoadedTimesheets(grouped))
                }
            }
        }

        private fun loadTimesheets(page: Instant) {
            scope.launch {
                dispatch(Msg.Loading(true))
                val result = withContext(ioContext) {
                    timesheetRepository.loadNewTimesheets(page, 100)
                }

                result.onSuccess {
                    dispatch(Msg.NextPage(it))
                }

                dispatch(Msg.Loading(false))
            }
        }

        private fun groupTimesheets(items: List<Timesheet>): List<GroupedTimesheet> {
            return items.groupBy { item -> item.begin.date }.map { item ->
                GroupedTimesheet(
                    date = item.key.atTime(LocalTime(0, 0)),
                    list = item.value,
                    total = item.value.map { it.duration }.reduce { acc, item ->
                        item?.let { duration -> acc?.plus(duration) } ?: acc
                    }
                )
            }.sortedBy { it.date }.asReversed()
        }

        private fun refresh() {
            scope.launch(ioContext) {
                timesheetRepository.invalidateCache()
                projectRepository.invalidateCache()
                activityRepository.invalidateCache()
                loadTimesheets(Clock.System.now())
            }
        }

        private fun restartTimesheet(id: Long) {
            scope.launch(ioContext) {
                timesheetRepository.restartTimesheet(id)
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.Loading -> copy(isLoading = msg.isLoading)
                is Msg.NextPage -> copy(
                    page = msg.page ?: this.page,
                    endReached = msg.page == null,
                    isLoading = false
                )
                is Msg.LoadedTimesheets -> copy(
                    timesheets = msg.timesheets,
                    isLoading = msg.timesheets.isEmpty()
                )
                is Msg.LoadedRunning -> copy(running = msg.form)
            }
    }
}
