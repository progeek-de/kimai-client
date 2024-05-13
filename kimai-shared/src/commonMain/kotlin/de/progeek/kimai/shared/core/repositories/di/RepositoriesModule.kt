package de.progeek.kimai.shared.core.repositories.di

import com.russhwolf.settings.ExperimentalSettingsApi
import de.progeek.kimai.shared.core.repositories.activity.ActivityRepository
import de.progeek.kimai.shared.core.repositories.auth.AuthRepository
import de.progeek.kimai.shared.core.repositories.customer.CustomerRepository
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
val repositoriesModule: () -> Module get() = {
    module {
        single { AuthRepository(get(), get(), get(), get(), get(), get(), get()) }
        single { TimesheetRepository(get(), get()) }
        single { ProjectRepository(get(), get()) }
        single { ActivityRepository(get(), get()) }
        single { SettingsRepository(get()) }
        single { CustomerRepository(get(), get()) }
    }
}