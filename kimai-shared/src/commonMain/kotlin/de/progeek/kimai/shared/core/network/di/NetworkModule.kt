package de.progeek.kimai.shared.core.network.di

import com.russhwolf.settings.Settings
import de.progeek.kimai.openapi.apis.ActivityApi
import de.progeek.kimai.openapi.apis.CustomerApi
import de.progeek.kimai.openapi.apis.DefaultApi
import de.progeek.kimai.openapi.apis.ProjectApi
import de.progeek.kimai.openapi.apis.TimesheetApi
import de.progeek.kimai.shared.BuildKonfig
import de.progeek.kimai.shared.core.network.client.ActivityClient
import de.progeek.kimai.shared.core.network.client.AuthClient
import de.progeek.kimai.shared.core.network.client.CustomerClient
import de.progeek.kimai.shared.core.network.client.ProjectClient
import de.progeek.kimai.shared.core.network.client.TimesheetsClient
import de.progeek.kimai.shared.core.storage.credentials.CredentialsConstants.BASE_URL_KEY
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule: () -> Module get() = {
    val settings = Settings()
    val baseUrl = settings.getString(BASE_URL_KEY, "")

    module {
        single { AuthClient() }
        single { (baseUrl: String) -> DefaultApi(baseUrl) }

        single { TimesheetApi(baseUrl, httpClientConfig = ::httpClientConfig) }
        single { ProjectApi(baseUrl, httpClientConfig = ::httpClientConfig) }
        single { ActivityApi(baseUrl, httpClientConfig = ::httpClientConfig) }
        single { CustomerApi(baseUrl, httpClientConfig = ::httpClientConfig) }
        single { TimesheetsClient(settings = get(), aesCipher = get(), client = get()) }
        single { ProjectClient(settings = get(), aesCipher = get(), client = get()) }
        single { ActivityClient(settings = get(), aesCipher = get(), client = get()) }
        single { CustomerClient(settings = get(), aesCipher = get(), client = get()) }
    }
}

private fun httpClientConfig(config: HttpClientConfig<*>) {
    config.install(Logging) {
        level = LogLevel.INFO
        logger = when (BuildKonfig.DEBUG) {
            true -> object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            false -> object : Logger {
                override fun log(message: String) {
                    // No-op
                }
            }
        }
    }
}
