package de.progeek.kimai.shared.ui.form.project

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.ui.form.project.ProjectFieldStore.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

interface ProjectFieldStore : Store<Intent, State, Label> {

    sealed class Intent {
        data class SelectedProject(val project: Project) : Intent()
        data class CustomerUpdated(val customer: Customer) : Intent()
    }

    data class State(
        internal val projects: List<Project> = emptyList(),
        val filteredProjects: List<Project> = emptyList(),
        val selectedProject: Project? = null
    )

    sealed interface Label {
        data class ProjectChanged(val project: Project) : Label
    }
}

class ProjectFieldStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {
    private val projectRepository by inject<ProjectRepository>()
    private val settingsRepository by inject<SettingsRepository>()

    fun create(timesheet: Timesheet? = null, mainContext: CoroutineContext, ioContext: CoroutineContext): ProjectFieldStore =
        object :
            ProjectFieldStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = "ProjectSelectionStore",
                initialState = State(selectedProject = timesheet?.project),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(mainContext, ioContext) },
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data class LoadedProjects(val projects: List<Project>) : Msg()
        data class LoadedDefaultProject(val project: Project) : Msg()
        data class SelectedProject(val project: Project) : Msg()
        data class UpdatedCustomer(val customer: Customer) : Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext
    ) : CoroutineExecutor<Intent, Unit, State, Msg, Label>(
        mainContext
    ) {
        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.SelectedProject -> handleSelectedProject(intent.project)
                is Intent.CustomerUpdated -> dispatch(Msg.UpdatedCustomer(intent.customer))
            }
        }

        private fun handleSelectedProject(project: Project) {
            dispatch(Msg.SelectedProject(project))
            publish(Label.ProjectChanged(project))
        }

        override fun executeAction(action: Unit) {
            loadProjects()
            if (state().selectedProject == null) {
                loadDefaultProject()
            }
        }

        private fun loadProjects() {
            scope.launch {
                projectRepository.getProjects().flowOn(ioContext).collect {
                    dispatch(Msg.LoadedProjects(it))
                }
            }
        }

        private fun loadDefaultProject() {
            scope.launch {
                settingsRepository.getDefaultProject().first()?.let { projectId ->
                    projectRepository.getProjects().flowOn(ioContext).collectLatest { projects ->
                        val project = projects.firstOrNull { project -> project.id == projectId }
                        project?.let {
                            dispatch(Msg.LoadedDefaultProject(it))
                            publish(Label.ProjectChanged(it))
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.LoadedProjects -> copy(
                    projects = msg.projects,
                    filteredProjects = msg.projects
                )
                is Msg.LoadedDefaultProject -> {
                    copy(selectedProject = msg.project)
                }
                is Msg.SelectedProject -> copy(
                    selectedProject = msg.project
                )
                is Msg.UpdatedCustomer -> copy(
                    selectedProject = null,
                    filteredProjects = projects.filter {
                        it.customer?.id == msg.customer.id
                    }
                )
            }
    }
}
