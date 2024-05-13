package de.progeek.kimai.shared.core.repositories.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getLongOrNullFlow
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import de.progeek.kimai.shared.BuildKonfig
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.storage.credentials.CredentialsConstants.BASE_URL_KEY
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.utils.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settings: ObservableSettings
)   {

    fun saveTheme(theme: ThemeEnum): ThemeEnum {
        settings.putString("THEME", theme.toString())
        return theme
    }

    fun saveDefaultProject(project: Project): Project {
        settings.putLong ("DEFAULT_PROJECT", project.id)
        return project
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getTheme(): Flow<ThemeEnum> {
        return settings.getStringFlow ("THEME", "LIGHT").map { ThemeEnum.valueOf(it) }
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getDefaultProject(): Flow<Long?> {
        return settings.getLongOrNullFlow ("DEFAULT_PROJECT")
    }

    fun clearDefaultProject(){
        return settings.remove("DEFAULT_PROJECT")
    }

    suspend fun saveEntryMode(mode: EntryMode): EntryMode {
        settings.putString("MODE", mode.toString())
        return mode
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getEntryMode(): Flow<EntryMode> {
        return settings.getStringOrNullFlow("MODE")
            .map { mode ->
                mode?.let { EntryMode.valueOf(mode) } ?: EntryMode.TIMER
            }
    }

    fun saveLanguage(language: Language) {
        settings.putString("LANGUAGE", language.languageCode)
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getLanguage(): Flow<String?> {
        return settings.getStringOrNullFlow("LANGUAGE")
    }

    fun getBaseUrl(): String {
        return settings.getString(BASE_URL_KEY, BuildKonfig.KIMAI_SERVER)
    }
}