package de.progeek.kimai.shared.ui.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.ui.settings.store.SettingsStore
import de.progeek.kimai.shared.ui.settings.store.SettingsStoreFactory
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.utils.Language
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow


class SettingsComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        SettingsStoreFactory(storeFactory = storeFactory).create(dispatchers.main, dispatchers.io)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<SettingsStore.State> = store.stateFlow

    fun onThemeChange(theme: ThemeEnum) {
        store.accept(SettingsStore.Intent.ChangeTheme(theme))
    }
    fun onDefaultProjectClick( defaultProject: Project) {
        store.accept(SettingsStore.Intent.UpdateDefaultProject(defaultProject))
    }
    fun clearDefaultProject() {
        store.accept(SettingsStore.Intent.ClearDefaultProject(null))
    }

    fun onLanguageChange(language: Language) {
        store.accept(SettingsStore.Intent.ChangeLanguage(language))
    }

    // Jira Integration Methods

    fun onSaveJiraConfig(
        baseUrl: String,
        credentials: JiraCredentials,
        enabled: Boolean,
        defaultProject: String?,
        syncInterval: Int
    ) {
        store.accept(
            SettingsStore.Intent.SaveJiraConfig(
                baseUrl = baseUrl,
                credentials = credentials,
                enabled = enabled,
                defaultProject = defaultProject,
                syncInterval = syncInterval
            )
        )
    }

    fun onToggleJiraEnabled(enabled: Boolean) {
        store.accept(SettingsStore.Intent.ToggleJiraEnabled(enabled))
    }

    fun onTestJiraConnection() {
        store.accept(SettingsStore.Intent.TestJiraConnection)
    }

    fun onClearJiraCredentials() {
        store.accept(SettingsStore.Intent.ClearJiraCredentials)
    }

    fun onOutput() {
        output(Output.Close)
    }

    sealed class Output {
        data object Close : Output()
    }
}


