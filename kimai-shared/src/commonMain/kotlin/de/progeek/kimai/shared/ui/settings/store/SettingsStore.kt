package de.progeek.kimai.shared.ui.settings.store

import com.arkivanov.mvikotlin.core.store.Store
import de.progeek.kimai.shared.utils.Language
import de.progeek.kimai.shared.core.jira.models.JiraAuthMethod
import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.jira.models.JiraProject
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.ui.theme.ThemeEnum


interface SettingsStore : Store<SettingsStore.Intent, SettingsStore.State, Nothing> {

    sealed class Intent {
        data class ChangeTheme(val theme: ThemeEnum): Intent()
        data class UpdateDefaultProject(val defaultProject: Project) : Intent()
        data class ClearDefaultProject(val nothing: kotlin.Nothing?): Intent()
        data class ChangeLanguage(val language: Language): Intent()

        // Jira Integration Intents
        data class SaveJiraConfig(
            val baseUrl: String,
            val credentials: JiraCredentials,
            val enabled: Boolean,
            val defaultProject: String?,
            val syncInterval: Int
        ) : Intent()
        data class ToggleJiraEnabled(val enabled: Boolean) : Intent()
        data object TestJiraConnection : Intent()
        data object ClearJiraCredentials : Intent()
    }

    data class State(
        val email: String,
        val theme: ThemeEnum = ThemeEnum.LIGHT,
        val defaultProject: Project?,
        val projects: List<Project>?,
        val language: Language,

        // Jira Integration State
        val jiraEnabled: Boolean = false,
        val jiraBaseUrl: String? = null,
        val jiraCredentials: JiraCredentials? = null,
        val jiraDefaultProject: String? = null,
        val jiraSyncInterval: Int = 15,
        val jiraProjects: List<JiraProject> = emptyList(),
        val jiraConnectionStatus: JiraConnectionStatus = JiraConnectionStatus.Unknown,
        val jiraConnectionMessage: String? = null
    )

    enum class JiraConnectionStatus {
        Unknown,
        Testing,
        Success,
        Failed
    }
}