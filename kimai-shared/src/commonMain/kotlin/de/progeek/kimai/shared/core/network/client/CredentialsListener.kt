package de.progeek.kimai.shared.core.network.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.openapi.infrastructure.ApiClient
import de.progeek.kimai.shared.BuildKonfig
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.core.network.NetworkConstants
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import de.progeek.kimai.shared.core.storage.credentials.CredentialsConstants.BASE_URL_KEY
import de.progeek.kimai.shared.core.storage.credentials.CredentialsConstants.CREDENTIALS_KEY

abstract class CredentialsListener(
    settings: ObservableSettings,
    aesCipher: AesGCMCipher,
    private val client: ApiClient
) {
    init {
        val baseUrl = settings.getString(BASE_URL_KEY, BuildKonfig.KIMAI_SERVER)
        val encrypted = settings.getStringOrNull(CREDENTIALS_KEY)
        val credentials = aesCipher.decrypt(encrypted)
        credentials?.apply {
            client.apply {
                this.baseUrl = baseUrl
                this.setApiKey(credentials.email, NetworkConstants.headerAuthUser)
                this.setApiKey(credentials.password, NetworkConstants.headerAuthToken)
            }
        }
    }

    fun refresh(baseUrl: String, credentials: Credentials) {
        client.apply {
            this.baseUrl = baseUrl
            this.setApiKey(credentials.email, NetworkConstants.headerAuthUser)
            this.setApiKey(credentials.password, NetworkConstants.headerAuthToken)
        }
    }
}
