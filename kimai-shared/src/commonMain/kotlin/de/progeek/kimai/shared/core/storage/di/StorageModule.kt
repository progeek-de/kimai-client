package de.progeek.kimai.shared.core.storage.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import de.progeek.kimai.shared.core.storage.credentials.CredentialsStorageImpl
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
val storageModule: () -> Module get() = {
    module {
        single { Settings() as ObservableSettings }
        single { AesGCMCipher() }
        single<CredentialsRepository> { CredentialsStorageImpl(get(), get()) }
    }
}
