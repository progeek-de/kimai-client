package de.progeek.kimai.shared.core.database.di

import de.progeek.kimai.shared.core.database.createDatabase
import de.progeek.kimai.shared.core.database.datasource.activity.ActivityDatasource
import de.progeek.kimai.shared.core.database.datasource.customer.CustomerDatasource
import de.progeek.kimai.shared.core.database.datasource.project.ProjectDatasource
import de.progeek.kimai.shared.core.database.datasource.timesheet.TimesheetDatasource
import de.progeek.kimai.shared.core.database.sqlDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val databaseModule: () -> Module get() = {
    module {
        factory { sqlDriverFactory() }
        single { createDatabase(driver = get()) }
        single<ProjectDatasource> { ProjectDatasource(database = get()) }
        single<ActivityDatasource> { ActivityDatasource(database = get()) }
        single<TimesheetDatasource> { TimesheetDatasource(database = get()) }
        single<CustomerDatasource> { CustomerDatasource(database = get()) }
    }
}
