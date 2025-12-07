package de.progeek.kimai.shared.core.network.di

import com.russhwolf.settings.Settings
import de.progeek.kimai.openapi.apis.*
import de.progeek.kimai.shared.BuildKonfig
import de.progeek.kimai.shared.core.network.client.*
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
            true -> Logger.Companion.SIMPLE
            false -> Logger.Companion.EMPTY
        }
    }
}
