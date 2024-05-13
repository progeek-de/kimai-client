package de.progeek.kimai.shared.core.network.client

import de.progeek.kimai.openapi.apis.DefaultApi
import io.ktor.http.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class AuthClient() : KoinComponent {
    suspend fun login(email: String, password: String, baseUrl: String): Result<Unit> {
        val client: DefaultApi by inject { parametersOf(baseUrl) }
        client.apply {
            setApiKey(email, "X-AUTH-USER")
            setApiKey(password, "X-AUTH-TOKEN")
        }

        val response = client.getAppApiStatusPing().response
        return when (response.status.value == HttpStatusCode.OK.value) {
            true -> Result.success(Unit)
            false -> Result.failure(Throwable(""))
        }
    }
}