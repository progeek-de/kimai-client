package de.progeek.kimai.shared.core.database.datasource.timesheet

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import de.progeek.kimai.shared.TimesheetEntity
import de.progeek.kimai.shared.core.database.KimaiDatabase
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.kimaiDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TimesheetDatasource(val database: KimaiDatabase) {

    private val query get() = database.timesheetEntityQueries

    fun getAll(): Result<Flow<List<Timesheet>>> =
        kotlin.runCatching {
            query.getAll(::toTimesheet).asFlow().mapToList(kimaiDispatchers.io)
        }

    suspend fun getById(id: Long): Result<Timesheet?> =
        withContext(kimaiDispatchers.io) {
            kotlin.runCatching {
                query.getById(id, ::toTimesheet).executeAsOneOrNull()
            }
        }

    fun getActive(): Result<Flow<Timesheet?>> =
        kotlin.runCatching {
            query.getActive(::toTimesheet).asFlow().mapToOneOrNull(kimaiDispatchers.io)
        }

    suspend fun insert(entity: TimesheetEntity): Result<Timesheet> =
        withContext(kimaiDispatchers.io) {
            kotlin.runCatching {
                query.insert(
                    id = entity.id,
                    begin = entity.begin,
                    end = entity.end,
                    duration = entity.duration,
                    description = entity.description,
                    project = entity.project,
                    activity = entity.activity,
                    exported = entity.exported
                )
                query.getById(entity.id, ::toTimesheet).executeAsOne()
            }
        }

    suspend fun insert(list: List<TimesheetEntity>): Result<Unit> =
        withContext(kimaiDispatchers.io) {
            kotlin.runCatching {
                query.transaction {
                    list.forEach { item ->
                        query.insert(
                            id = item.id,
                            begin = item.begin,
                            end = item.end,
                            duration = item.duration,
                            description = item.description,
                            project = item.project,
                            activity = item.activity,
                            exported = item.exported
                        )
                    }
                }
            }
        }

    suspend fun update(entity: TimesheetEntity): Result<Timesheet> =
        withContext(kimaiDispatchers.io) {
            kotlin.runCatching {
                query.update(
                    id = entity.id,
                    begin = entity.begin,
                    end = entity.end,
                    duration = entity.duration,
                    description = entity.description,
                    project = entity.project,
                    activity = entity.activity,
                    exported = entity.exported
                )
                query.getById(entity.id, ::toTimesheet).executeAsOne()
            }
        }

    suspend fun delete(id: Long): Result<Unit> = withContext(kimaiDispatchers.io) {
        kotlin.runCatching {
            query.deleteById(id)
        }
    }

    suspend fun delete(list: List<Long>): Result<Unit> = withContext(kimaiDispatchers.io) {
        kotlin.runCatching {
            query.transaction {
                list.forEach { item ->
                    query.deleteById(
                        id = item
                    )
                }
            }
        }
    }

    fun deleteAll() {
        query.deleteAll()
    }
}
