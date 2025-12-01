package de.progeek.kimai.shared.core.repositories.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getLongOrNullFlow
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import de.progeek.kimai.shared.BuildKonfig
import de.progeek.kimai.shared.core.jira.models.JiraCredentials
import de.progeek.kimai.shared.core.jira.models.SerializableAuthMethod
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import de.progeek.kimai.shared.core.storage.credentials.CredentialsConstants.BASE_URL_KEY
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import de.progeek.kimai.shared.utils.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settings: ObservableSettings,
    private val aesCipher: AesGCMCipher
) {

    companion object {
        private const val JIRA_BASE_URL_KEY = "JIRA_BASE_URL"
        private const val JIRA_ENABLED_KEY = "JIRA_ENABLED"
        private const val JIRA_CREDENTIALS_KEY = "JIRA_CREDENTIALS"
        private const val JIRA_EMAIL_KEY = "JIRA_EMAIL"
        private const val JIRA_AUTH_METHOD_KEY = "JIRA_AUTH_METHOD"
        private const val JIRA_DEFAULT_PROJECT_KEY = "JIRA_DEFAULT_PROJECT"
        private const val JIRA_SYNC_INTERVAL_KEY = "JIRA_SYNC_INTERVAL"
        private const val DEFAULT_SYNC_INTERVAL = 15 // minutes
    }

    fun saveTheme(theme: ThemeEnum): ThemeEnum {
        settings.putString("THEME", theme.toString())
        return theme
    }

    fun saveDefaultProject(project: Project): Project {
        settings.putLong("DEFAULT_PROJECT", project.id)
        return project
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getTheme(): Flow<ThemeEnum> {
        return settings.getStringFlow("THEME", "LIGHT").map { ThemeEnum.valueOf(it) }
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getDefaultProject(): Flow<Long?> {
        return settings.getLongOrNullFlow("DEFAULT_PROJECT")
    }

    fun clearDefaultProject() {
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

    // Jira Configuration Methods

    fun saveJiraBaseUrl(url: String) {
        settings.putString(JIRA_BASE_URL_KEY, url)
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getJiraBaseUrl(): Flow<String?> {
        return settings.getStringOrNullFlow(JIRA_BASE_URL_KEY)
    }

    fun saveJiraCredentials(credentials: JiraCredentials) {
        val token = when (val method = credentials.authMethod) {
            is SerializableAuthMethod.ApiToken -> method.token
            is SerializableAuthMethod.PersonalAccessToken -> method.token
        }
        // Encrypt the token
        val encrypted = aesCipher.encryptString(token)

        settings.putString(JIRA_CREDENTIALS_KEY, encrypted)
        settings.putString(JIRA_AUTH_METHOD_KEY, credentials.authMethod::class.simpleName ?: "ApiToken")

        // Save email if using API Token
        when (val method = credentials.authMethod) {
            is SerializableAuthMethod.ApiToken -> {
                settings.putString(JIRA_EMAIL_KEY, method.email)
            }
            is SerializableAuthMethod.PersonalAccessToken -> {
                settings.remove(JIRA_EMAIL_KEY)
            }
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getJiraCredentials(): Flow<JiraCredentials?> {
        return settings.getStringOrNullFlow(JIRA_CREDENTIALS_KEY).map { encryptedToken ->
            encryptedToken?.let {
                val token = aesCipher.decryptString(it)
                val baseUrl = settings.getStringOrNull(JIRA_BASE_URL_KEY)

                if (token != null && baseUrl != null) {
                    val authMethodName = settings.getStringOrNull(JIRA_AUTH_METHOD_KEY) ?: "ApiToken"
                    val authMethod = if (authMethodName == "ApiToken") {
                        val email = settings.getStringOrNull(JIRA_EMAIL_KEY) ?: ""
                        SerializableAuthMethod.ApiToken(email, token)
                    } else {
                        SerializableAuthMethod.PersonalAccessToken(token)
                    }

                    JiraCredentials(baseUrl, authMethod)
                } else {
                    null
                }
            }
        }
    }

    fun clearJiraCredentials() {
        settings.remove(JIRA_CREDENTIALS_KEY)
        settings.remove(JIRA_EMAIL_KEY)
        settings.remove(JIRA_AUTH_METHOD_KEY)
    }

    fun saveJiraEnabled(enabled: Boolean) {
        settings.putBoolean(JIRA_ENABLED_KEY, enabled)
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getJiraEnabled(): Flow<Boolean> {
        return settings.getBooleanFlow(JIRA_ENABLED_KEY, false)
    }

    fun saveJiraDefaultProject(projectKey: String?) {
        if (projectKey != null) {
            settings.putString(JIRA_DEFAULT_PROJECT_KEY, projectKey)
        } else {
            settings.remove(JIRA_DEFAULT_PROJECT_KEY)
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getJiraDefaultProject(): Flow<String?> {
        return settings.getStringOrNullFlow(JIRA_DEFAULT_PROJECT_KEY)
    }

    fun saveJiraSyncInterval(minutes: Int) {
        settings.putInt(JIRA_SYNC_INTERVAL_KEY, minutes)
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getJiraSyncInterval(): Flow<Int> {
        return settings.getIntFlow(JIRA_SYNC_INTERVAL_KEY, DEFAULT_SYNC_INTERVAL)
    }
}
