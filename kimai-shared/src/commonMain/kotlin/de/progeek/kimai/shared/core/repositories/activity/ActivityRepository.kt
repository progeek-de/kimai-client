package de.progeek.kimai.shared.core.repositories.activity

import de.progeek.kimai.shared.core.database.datasource.activity.ActivityDatasource
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.network.client.ActivityClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.*
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

class ActivityRepository(
    private val activityClient: ActivityClient,
    private val activityDataSource: ActivityDatasource
) {
    private val store = StoreBuilder
        .from(
            fetcher = Fetcher.of {
                activityClient.getActivities().getOrThrow()
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _ -> activityDataSource.getAll() },
                writer = { _, list -> activityDataSource.insert(list) },
                deleteAll = { activityDataSource.deleteAll() }
            )
        ).build()

    fun getActivities(): Flow<List<Activity>> = store.stream(
        StoreReadRequest.cached("activities::all", false)
    ).map { it.dataOrNull() ?: emptyList() }

    @OptIn(ExperimentalStoreApi::class)
    suspend fun invalidateCache() {
        store.clear()
        store.fresh("activities::all")
    }
}
