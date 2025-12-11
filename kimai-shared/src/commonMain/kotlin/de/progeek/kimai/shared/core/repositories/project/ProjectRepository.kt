package de.progeek.kimai.shared.core.repositories.project

import de.progeek.kimai.shared.core.database.datasource.project.ProjectDatasource
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.network.client.ProjectClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

class ProjectRepository(
    private val projectDataSource: ProjectDatasource,
    private val projectClient: ProjectClient
) {

    private val store = StoreBuilder
        .from(
            fetcher = Fetcher.of {
                projectClient.getProjects().getOrThrow()
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _ -> projectDataSource.getAll() },
                writer = { _, list -> projectDataSource.insert(list) },
                deleteAll = { projectDataSource.deleteAll() }
            )
        ).build()

    fun getProjects(): Flow<List<Project>> = store.stream(
        StoreReadRequest.cached("projects::all", false)
    ).map { it.dataOrNull() ?: emptyList() }

    @OptIn(ExperimentalStoreApi::class)
    suspend fun invalidateCache() {
        store.clear()
        store.fresh("projects::all")
    }
}
