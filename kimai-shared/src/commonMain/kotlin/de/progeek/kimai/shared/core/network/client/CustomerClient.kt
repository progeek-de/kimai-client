package de.progeek.kimai.shared.core.network.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.openapi.apis.CustomerApi
import de.progeek.kimai.shared.core.mapper.map
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher

class CustomerClient(
    settings: ObservableSettings,
    aesCipher: AesGCMCipher,
    private val client: CustomerApi
) : CredentialsListener(settings, aesCipher, client) {
    suspend fun getCustomers(): Result<List<Customer>> {
        return kotlin.runCatching {
            val response = client.getGetCustomers()
            if(!response.success)
                throw Throwable("Error while getting customers")

            response.body().map { it.map() }
        }
    }
}