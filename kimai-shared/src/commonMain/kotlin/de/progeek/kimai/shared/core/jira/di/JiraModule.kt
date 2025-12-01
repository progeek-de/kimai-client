package de.progeek.kimai.shared.core.jira.di

import de.progeek.kimai.shared.core.database.datasource.jira.JiraDatasource
import de.progeek.kimai.shared.core.jira.client.JiraClient
import de.progeek.kimai.shared.core.jira.client.JiraCredentialsProvider
import de.progeek.kimai.shared.core.jira.client.SettingsJiraCredentialsProvider
import de.progeek.kimai.shared.core.jira.repositories.JiraRepository
import de.progeek.kimai.shared.core.sync.JiraSyncScheduler
import de.progeek.kimai.shared.kimaiDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

val jiraModule: () -> Module get() = {
    module {
        // Credentials provider (single source of truth for Jira credentials)
        single<JiraCredentialsProvider> { SettingsJiraCredentialsProvider(get()) }

        // Datasource for local cache
        single { JiraDatasource(get()) }

        // API client
        single { JiraClient(credentialsProvider = get()) }

        // Repository with Store5 caching
        single { JiraRepository(jiraDatasource = get(), jiraClient = get()) }

        // Background sync scheduler (application-scoped)
        single {
            val scope = CoroutineScope(SupervisorJob() + kimaiDispatchers.io)
            JiraSyncScheduler(
                jiraRepository = get(),
                settingsRepository = get(),
                dispatchers = kimaiDispatchers,
                scope = scope
            )
        }
    }
}
