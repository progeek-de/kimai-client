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
        single { TimesheetRepository(timesheetsClient = get(), timesheetDatasource = get()) }
        single { ProjectRepository(projectDataSource = get(), projectClient = get()) }
        single { ActivityRepository(activityClient = get(), activityDataSource = get()) }
        single { SettingsRepository(get(), get()) }
        single { CustomerRepository(customerClient = get(), customerDataSource = get()) }
    }
}
