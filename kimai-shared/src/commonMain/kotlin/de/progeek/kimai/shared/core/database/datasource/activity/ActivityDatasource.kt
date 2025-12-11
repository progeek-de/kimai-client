package de.progeek.kimai.shared.core.database.datasource.activity

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.progeek.kimai.shared.core.database.KimaiDatabase
import de.progeek.kimai.shared.core.mapper.map
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.kimaiDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ActivityDatasource(val database: KimaiDatabase) {

    private val query get() = database.activityEntityQueries

    private fun toActivity(id: Long, name: String, project: Long?): Activity =
        Activity(id, name, project)

    fun getAll(): Flow<List<Activity>> =
        query.getAllActivities(::toActivity)
            .asFlow()
            .mapToList(kimaiDispatchers.io)

    suspend fun getByProjectId(id: Long): Result<List<Activity>> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.getAllActivitiesByProjectId(id).executeAsList().map {
                    it.map()
                }
            }
        }

    suspend fun insert(list: List<Activity>): Result<List<Activity>> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.transaction {
                    list.forEach { item ->
                        query.insertActivity(
                            id = item.id,
                            name = item.name,
                            project = item.project
                        )
                    }
                }
                list
            }
        }

    fun deleteAll() = query.deleteAll()
}
