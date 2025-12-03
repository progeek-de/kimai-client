package de.progeek.kimai.shared.core.di

import de.progeek.kimai.shared.core.database.di.databaseModule
import de.progeek.kimai.shared.core.network.di.networkModule
import de.progeek.kimai.shared.core.repositories.di.repositoriesModule
import de.progeek.kimai.shared.core.storage.di.storageModule
import de.progeek.kimai.shared.core.ticketsystem.di.ticketSystemModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            databaseModule(),
            repositoriesModule(),
            storageModule(),
            networkModule(),
            ticketSystemModule()
        )
    }
