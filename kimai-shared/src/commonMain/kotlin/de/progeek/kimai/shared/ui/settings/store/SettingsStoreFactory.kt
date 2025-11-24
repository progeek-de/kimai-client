package de.progeek.kimai.shared.ui.settings.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import de.progeek.kimai.shared.core.jira.client.JiraClient
import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.jira.models.JiraProject
import de.progeek.kimai.shared.core.jira.repositories.JiraRepository
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
    private val jiraClient by inject<JiraClient>()
    private val jiraRepository by inject<JiraRepository>()

    fun create(mainContext: CoroutineContext, ioContext: CoroutineContext): SettingsStore =
        object : SettingsStore, Store<SettingsStore.Intent, SettingsStore.State, Nothing> by storeFactory.create(
            name = "SettingsStore",
            initialState = SettingsStore.State(
                email = "",
                theme = ThemeEnum.SYSTEM,
                defaultProject = null,
                projects = emptyList(),
                language = getLanguages().first(),
                jiraEnabled = false,
                jiraBaseUrl = null,
                jiraCredentials = null,
                jiraDefaultProject = null,
                jiraSyncInterval = 15,
                jiraProjects = emptyList(),
                jiraConnectionStatus = SettingsStore.JiraConnectionStatus.Unknown,
                jiraConnectionMessage = null
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

        // Jira Messages
        data class JiraEnabledUpdated(val enabled: Boolean) : Msg()
        data class JiraBaseUrlUpdated(val url: String?) : Msg()
        data class JiraCredentialsUpdated(val credentials: JiraCredentials?) : Msg()
        data class JiraDefaultProjectUpdated(val projectKey: String?) : Msg()
        data class JiraSyncIntervalUpdated(val interval: Int) : Msg()
        data class JiraProjectsUpdated(val projects: List<JiraProject>) : Msg()
        data class JiraConnectionStatusUpdated(
            val status: SettingsStore.JiraConnectionStatus,
            val message: String?
        ) : Msg()
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
            loadJiraSettings()
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
                is SettingsStore.Intent.SaveJiraConfig -> {
                    saveJiraConfig(intent)
                }
                is SettingsStore.Intent.ToggleJiraEnabled -> {
                    toggleJiraEnabled(intent.enabled)
                }
                is SettingsStore.Intent.TestJiraConnection -> {
                    testJiraConnection()
                }
                is SettingsStore.Intent.ClearJiraCredentials -> {
                    clearJiraCredentials()
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

        // Jira Integration Methods

        private fun loadJiraSettings() {
            scope.launch {
                // Load Jira enabled state
                settingsRepository.getJiraEnabled().flowOn(ioContext).collectLatest { enabled ->
                    dispatch(Msg.JiraEnabledUpdated(enabled))
                }
            }

            scope.launch {
                // Load Jira base URL
                settingsRepository.getJiraBaseUrl().flowOn(ioContext).collectLatest { url ->
                    dispatch(Msg.JiraBaseUrlUpdated(url))
                }
            }

            scope.launch {
                // Load Jira credentials
                settingsRepository.getJiraCredentials().flowOn(ioContext).collectLatest { credentials ->
                    dispatch(Msg.JiraCredentialsUpdated(credentials))
                }
            }

            scope.launch {
                // Load Jira default project
                settingsRepository.getJiraDefaultProject().flowOn(ioContext).collectLatest { projectKey ->
                    dispatch(Msg.JiraDefaultProjectUpdated(projectKey))
                }
            }

            scope.launch {
                // Load Jira sync interval
                settingsRepository.getJiraSyncInterval().flowOn(ioContext).collectLatest { interval ->
                    dispatch(Msg.JiraSyncIntervalUpdated(interval))
                }
            }
        }

        private fun saveJiraConfig(intent: SettingsStore.Intent.SaveJiraConfig) {
            scope.launch {
                withContext(ioContext) {
                    settingsRepository.saveJiraBaseUrl(intent.baseUrl)
                    settingsRepository.saveJiraCredentials(intent.credentials)
                    settingsRepository.saveJiraEnabled(intent.enabled)
                    settingsRepository.saveJiraDefaultProject(intent.defaultProject)
                    settingsRepository.saveJiraSyncInterval(intent.syncInterval)
                }

                // Update state
                dispatch(Msg.JiraBaseUrlUpdated(intent.baseUrl))
                dispatch(Msg.JiraCredentialsUpdated(intent.credentials))
                dispatch(Msg.JiraEnabledUpdated(intent.enabled))
                dispatch(Msg.JiraDefaultProjectUpdated(intent.defaultProject))
                dispatch(Msg.JiraSyncIntervalUpdated(intent.syncInterval))

                // If enabled, load projects
                if (intent.enabled) {
                    loadJiraProjects()
                }
            }
        }

        private fun toggleJiraEnabled(enabled: Boolean) {
            scope.launch {
                withContext(ioContext) {
                    settingsRepository.saveJiraEnabled(enabled)
                }
                dispatch(Msg.JiraEnabledUpdated(enabled))
            }
        }

        private fun testJiraConnection() {
            scope.launch {
                dispatch(Msg.JiraConnectionStatusUpdated(SettingsStore.JiraConnectionStatus.Testing, null))

                val result = withContext(ioContext) {
                    jiraClient.testConnection()
                }

                result.fold(
                    onSuccess = {
                        dispatch(Msg.JiraConnectionStatusUpdated(
                            SettingsStore.JiraConnectionStatus.Success,
                            "Connection successful"
                        ))
                        // Load projects on successful connection
                        loadJiraProjects()
                    },
                    onFailure = { error ->
                        dispatch(Msg.JiraConnectionStatusUpdated(
                            SettingsStore.JiraConnectionStatus.Failed,
                            error.message ?: "Connection failed"
                        ))
                    }
                )
            }
        }

        private fun loadJiraProjects() {
            scope.launch {
                val result = withContext(ioContext) {
                    jiraClient.getProjects()
                }

                result.fold(
                    onSuccess = { projects ->
                        dispatch(Msg.JiraProjectsUpdated(projects))
                    },
                    onFailure = {
                        // Silently fail - projects will remain empty
                        dispatch(Msg.JiraProjectsUpdated(emptyList()))
                    }
                )
            }
        }

        private fun clearJiraCredentials() {
            scope.launch {
                withContext(ioContext) {
                    settingsRepository.clearJiraCredentials()
                    settingsRepository.saveJiraEnabled(false)
                }

                dispatch(Msg.JiraCredentialsUpdated(null))
                dispatch(Msg.JiraEnabledUpdated(false))
                dispatch(Msg.JiraProjectsUpdated(emptyList()))
                dispatch(Msg.JiraConnectionStatusUpdated(SettingsStore.JiraConnectionStatus.Unknown, null))
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

                // Jira Message Reducers
                is Msg.JiraEnabledUpdated -> copy(jiraEnabled = msg.enabled)
                is Msg.JiraBaseUrlUpdated -> copy(jiraBaseUrl = msg.url)
                is Msg.JiraCredentialsUpdated -> copy(jiraCredentials = msg.credentials)
                is Msg.JiraDefaultProjectUpdated -> copy(jiraDefaultProject = msg.projectKey)
                is Msg.JiraSyncIntervalUpdated -> copy(jiraSyncInterval = msg.interval)
                is Msg.JiraProjectsUpdated -> copy(jiraProjects = msg.projects)
                is Msg.JiraConnectionStatusUpdated -> copy(
                    jiraConnectionStatus = msg.status,
                    jiraConnectionMessage = msg.message
                )
            }
    }
}