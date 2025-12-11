package de.progeek.kimai.shared.core.repositories.auth

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.core.network.client.ActivityClient
import de.progeek.kimai.shared.core.network.client.AuthClient
import de.progeek.kimai.shared.core.network.client.CustomerClient
import de.progeek.kimai.shared.core.network.client.ProjectClient
import de.progeek.kimai.shared.core.network.client.TimesheetsClient
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.storage.credentials.CredentialsConstants.BASE_URL_KEY
import org.koin.core.component.KoinComponent

class AuthRepository(
    private val authClient: AuthClient,
    private val activityClient: ActivityClient,
    private val projectClient: ProjectClient,
    private val timesheetsClient: TimesheetsClient,
    private val customerClient: CustomerClient,
    private val credentialsRepository: CredentialsRepository,
    private val settingsRepository: ObservableSettings
) : KoinComponent {

    suspend fun login(email: String, password: String, baseUrl: String): Credentials? {
        val result = authClient.login(email, password, baseUrl)
        return result.getOrNull()?.let {
            val credentials = Credentials(email, password)
            updateAllClients(baseUrl, credentials)
            settingsRepository.putString(BASE_URL_KEY, baseUrl)
            credentialsRepository.save(credentials)
            credentials
        }
    }

    private fun updateAllClients(baseUrl: String, credentials: Credentials) {
        activityClient.refresh(baseUrl, credentials)
        customerClient.refresh(baseUrl, credentials)
        projectClient.refresh(baseUrl, credentials)
        timesheetsClient.refresh(baseUrl, credentials)
    }

    suspend fun logout(): Result<Unit> =
        credentialsRepository.delete()
}
