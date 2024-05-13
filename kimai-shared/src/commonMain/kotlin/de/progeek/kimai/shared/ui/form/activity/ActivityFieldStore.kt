package de.progeek.kimai.shared.ui.form.activity

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.repositories.activity.ActivityRepository
import de.progeek.kimai.shared.ui.form.activity.ActivityFieldStore.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

interface ActivityFieldStore : Store<Intent, State, Label> {

    sealed class Intent {
        data class SelectedActivity(val activity: Activity) : Intent()
        data class UpdatedProject(val project: Project) : Intent()
    }

    data class State(
        internal val project: Project?,
        internal val activities: List<Activity> = emptyList(),
        val filteredActivities: List<Activity> = emptyList(),
        val selectedActivity: Activity? = null,
    )

    sealed interface Label {
        data class ActivityChanged(val activity: Activity) : Label
    }
}

class ActivityFieldStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {
    private val activityRepository by inject<ActivityRepository>()

    fun create(timesheet: Timesheet? = null, mainContext: CoroutineContext, ioContext: CoroutineContext): ActivityFieldStore =
        object : ActivityFieldStore, Store<Intent, State, Label> by storeFactory.create(
            name = "ActivityFieldStore",
            initialState = State(
                project = timesheet?.project,
                selectedActivity = timesheet?.activity
            ),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(mainContext, ioContext) },
            reducer = ReducerImpl
        ) {}

    private sealed class Msg {
        data class LoadedActivities(val activities: List<Activity>) : Msg()
        data class SelectedActivity(val activity: Activity) : Msg()
        data class UpdatedProject(val project: Project) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext,
    ) : CoroutineExecutor<Intent, Unit, State, Msg, Label>(mainContext) {

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.SelectedActivity -> handleSelectedProject(intent.activity)
                is Intent.UpdatedProject -> dispatch(Msg.UpdatedProject(intent.project))
            }
        }

        private fun handleSelectedProject(activity: Activity) {
            dispatch(Msg.SelectedActivity(activity))
            publish(Label.ActivityChanged(activity))
        }

        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch {
                activityRepository.getActivities().flowOn(ioContext).collect {
                    dispatch(Msg.LoadedActivities(it))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.LoadedActivities -> copy(
                    activities = msg.activities,
                    filteredActivities = msg.activities.filter {
                        it.project == project?.id || (project?.globalActivities == true && it.project == null)
                    }
                )
                is Msg.SelectedActivity -> copy(
                    selectedActivity = msg.activity,
                )
                is Msg.UpdatedProject -> copy(
                    selectedActivity = null,
                    filteredActivities = activities.filter {
                        it.project == msg.project.id || (msg.project.globalActivities && it.project == null)
                    }
                )
            }
    }
}

