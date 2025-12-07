package de.progeek.kimai.shared.core.network.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.openapi.apis.ActivityApi
import de.progeek.kimai.shared.core.mapper.map
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher

class ActivityClient(
    settings: ObservableSettings,
    aesCipher: AesGCMCipher,
    private val client: ActivityApi
) : CredentialsListener(settings, aesCipher, client) {

    suspend fun getActivities(): Result<List<Activity>> {
        return kotlin.runCatching {
            val response = client.getGetActivities()
            if (!response.success) {
                throw Throwable("Error while getting activities")
            }

            response.body().map { it.map() }
        }
    }
}
