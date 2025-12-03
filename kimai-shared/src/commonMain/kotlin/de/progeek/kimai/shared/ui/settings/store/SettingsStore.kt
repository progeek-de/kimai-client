package de.progeek.kimai.shared.ui.settings.store

import com.arkivanov.mvikotlin.core.store.Store
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.utils.Language

interface SettingsStore : Store<SettingsStore.Intent, SettingsStore.State, Nothing> {

    sealed class Intent {
        data class ChangeTheme(val theme: ThemeEnum) : Intent()
        data class UpdateDefaultProject(val defaultProject: Project) : Intent()
        data class ClearDefaultProject(val nothing: kotlin.Nothing?) : Intent()
        data class ChangeLanguage(val language: Language) : Intent()
        data class ChangeIssueInsertFormat(val format: IssueInsertFormat) : Intent()
    }

    data class State(
        val email: String,
        val theme: ThemeEnum = ThemeEnum.LIGHT,
        val defaultProject: Project?,
        val projects: List<Project>?,
        val language: Language,
        val issueInsertFormat: IssueInsertFormat = IssueInsertFormat.SUMMARY_HASH_KEY
    )
}
