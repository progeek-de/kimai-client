package de.progeek.kimai.shared.core.jira.di

import de.progeek.kimai.shared.core.database.datasource.jira.JiraDatasource
import de.progeek.kimai.shared.core.jira.client.JiraClient
import de.progeek.kimai.shared.core.jira.repositories.JiraRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val jiraModule: () -> Module get() = {
    module {
        single { JiraClient(get(), get()) }
        single { JiraDatasource(get()) }
        single { JiraRepository(get(), get()) }
    }
}
