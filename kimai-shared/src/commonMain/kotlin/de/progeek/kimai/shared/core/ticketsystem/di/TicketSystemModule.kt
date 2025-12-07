package de.progeek.kimai.shared.core.ticketsystem.di

import de.progeek.kimai.shared.core.ticketsystem.api.TicketSystemRegistry
import de.progeek.kimai.shared.core.ticketsystem.datasource.TicketConfigDatasource
import de.progeek.kimai.shared.core.ticketsystem.datasource.TicketIssueDatasource
import de.progeek.kimai.shared.core.ticketsystem.providers.github.GitHubTicketProvider
import de.progeek.kimai.shared.core.ticketsystem.providers.gitlab.GitLabTicketProvider
import de.progeek.kimai.shared.core.ticketsystem.providers.jira.JiraTicketProvider
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketSystemRepository
import de.progeek.kimai.shared.core.ticketsystem.sync.TicketSyncScheduler
import de.progeek.kimai.shared.kimaiDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

val ticketSystemModule: () -> Module get() = {
    module {
        // Provider Registry with all implementations
        single {
            TicketSystemRegistry().apply {
                register(JiraTicketProvider())
                register(GitHubTicketProvider())
                register(GitLabTicketProvider())
            }
        }

        // Datasources
        single { TicketIssueDatasource(get()) }
        single { TicketConfigDatasource(get(), get()) }

        // Repositories
        single {
            TicketConfigRepository(
                configDatasource = get(),
                issueDatasource = get()
            )
        }

        single {
            TicketSystemRepository(
                configRepository = get(),
                issueDatasource = get(),
                registry = get()
            )
        }

        // Sync Scheduler
        single {
            val scope = CoroutineScope(SupervisorJob() + kimaiDispatchers.io)
            TicketSyncScheduler(
                configRepository = get(),
                ticketRepository = get(),
                dispatchers = kimaiDispatchers,
                scope = scope
            )
        }
    }
}
