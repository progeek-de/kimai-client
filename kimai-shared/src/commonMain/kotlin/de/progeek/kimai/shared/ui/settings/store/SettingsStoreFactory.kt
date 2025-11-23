package de.progeek.kimai.shared.ui.settings.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.utils.Language
import de.progeek.kimai.shared.utils.getLanguages
import de.progeek.kimai.shared.utils.notNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

class SettingsStoreFactory(
    private val storeFactory: StoreFactory
) : KoinComponent {

    private val settingsRepository by inject<SettingsRepository>()
    private val credentialsRepository by inject<CredentialsRepository>()
    private val projectRepository by inject<ProjectRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): SettingsStore =
        object : SettingsStore, Store<SettingsStore.Intent, SettingsStore.State, Nothing> by storeFactory.create(
            name = "SettingsStore",
            initialState = SettingsStore.State(
                email = "",
                theme = ThemeEnum.SYSTEM,
                defaultProject = null,
                projects = emptyList(),
                language = getLanguages().first()
            ),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(mainContext, ioContext) },
            reducer = ReducerImpl
        ) {}

    private sealed class Msg {
        data class SettingsEmail(val email: String) : Msg()
        data class Theme(val theme: ThemeEnum): Msg()
        data class DefaultProject(val project: Project): Msg()
        data class ProjectsUpdated(val projects: List<Project>): Msg()
        data class ClearDefaultProject(val project: Project?): Msg()
        data class LanguageUpdated(val language: Language): Msg()
    }

    private inner class ExecutorImpl(
        mainContext: CoroutineContext,
        private val ioContext: CoroutineContext,
    ) : CoroutineExecutor<SettingsStore.Intent, Unit, SettingsStore.State, Msg, Nothing>(mainContext) {

        override fun executeAction(action: Unit, getState: () -> SettingsStore.State) {
            loadCredentialsEmail()
            loadTheme()
            loadProjects()
            loadLanguage()
        }

        override fun executeIntent(intent: SettingsStore.Intent, getState: () -> SettingsStore.State): Unit =
            when (intent) {
                is SettingsStore.Intent.ChangeTheme -> {
                    changeTheme(intent.theme)
                }
                is SettingsStore.Intent.UpdateDefaultProject -> {
                    updateDefaultProject(intent.defaultProject)
                }
                is SettingsStore.Intent.ClearDefaultProject -> {
                    clearDefaultProject()
                }

                is SettingsStore.Intent.ChangeLanguage -> {
                    changeLanguage(intent.language)
                }
            }

        private fun changeLanguage(language: Language) {
            scope.launch {
                withContext(ioContext) {
                    settingsRepository.saveLanguage(language)
                }

                dispatch(Msg.LanguageUpdated(language))
            }
        }

        private fun loadLanguage() {
            scope.launch {
                settingsRepository.getLanguage().flowOn(ioContext).collect { language ->
                    getLanguages().find { it.languageCode ==  language }.notNull {
                        dispatch(Msg.LanguageUpdated(it))
                    }
                }
            }
        }
        private fun loadCredentialsEmail() {
            val credentials = credentialsRepository.getCredentials()
                credentials?.let {
                dispatch(Msg.SettingsEmail(credentials.email)) }
        }

        private fun loadTheme () {
            scope.launch {
                settingsRepository.getTheme().flowOn(ioContext).collectLatest {
                    dispatch(Msg.Theme(it))
                }
            }
        }

        private fun loadProjects(){
            scope.launch {
                settingsRepository.getDefaultProject().flowOn(ioContext).collectLatest { projectId ->
                    projectRepository.getProjects().flowOn(ioContext).collectLatest { projects ->
                        val filteredProjects = projects.filter { it.id == projectId }
                        filteredProjects.firstOrNull()?.let { project ->
                            dispatch(Msg.DefaultProject(project))
                        }

                        dispatch(Msg.ProjectsUpdated(projects = projects))
                    }
                }
            }
        }

        private fun changeTheme(theme: ThemeEnum) {
            scope.launch {
                val res = withContext(ioContext) {
                    settingsRepository.saveTheme(theme)
                }

                dispatch(Msg.Theme(res))
            }
        }

        private fun updateDefaultProject(defaultProject: Project) {
            scope.launch {
                withContext(ioContext) {
                    settingsRepository.saveDefaultProject(defaultProject)
                }

                dispatch(Msg.DefaultProject(defaultProject))
            }
        }

        private fun clearDefaultProject() {
            scope.launch {
                withContext(ioContext) {
                    settingsRepository.clearDefaultProject()
                }
                dispatch(Msg.ClearDefaultProject(null))
            }
        }
    }

    private object ReducerImpl : Reducer<SettingsStore.State, Msg> {
        override fun SettingsStore.State.reduce(msg: Msg): SettingsStore.State =
            when (msg) {
                is Msg.SettingsEmail -> copy(email = msg.email)
                is Msg.Theme -> copy(theme = msg.theme)
                is Msg.DefaultProject -> copy(defaultProject = msg.project)
                is Msg.ProjectsUpdated -> copy(projects = msg.projects)
                is Msg.ClearDefaultProject -> copy(defaultProject = msg.project)
                is Msg.LanguageUpdated -> copy(language = msg.language)
            }
    }
}