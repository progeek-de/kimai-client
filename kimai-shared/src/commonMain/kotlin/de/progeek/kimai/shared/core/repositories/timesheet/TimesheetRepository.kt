package de.progeek.kimai.shared.core.repositories.timesheet

import arrow.core.flatMap
import de.progeek.kimai.shared.core.database.datasource.timesheet.TimesheetDatasource
import de.progeek.kimai.shared.core.mapper.toTimesheetEntity
import de.progeek.kimai.shared.core.mapper.toTimesheetForm
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.network.client.TimesheetsClient
import de.progeek.kimai.shared.utils.fromISOTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class TimesheetRepository(
    private val timesheetsClient: TimesheetsClient,
    private val timesheetDatasource: TimesheetDatasource
) {

    fun timesheetsStream(): Flow<List<Timesheet>> =
        timesheetDatasource.getAll().getOrThrow()

    suspend fun loadNewTimesheets(beforeDate: Instant, pageSize: Int): Result<Instant?> {
        return timesheetsClient.getTimeSheets(beforeDate, pageSize)
            .flatMap { items ->
                val lastPage = items.lastOrNull()?.begin?.fromISOTime()
                timesheetDatasource.insert(
                    items.map { item -> item.toTimesheetEntity() }
                ).getOrThrow()

                Result.success(lastPage)
            }
    }

    suspend fun loadTimesheetById(id: Long): Result<Timesheet> =
        timesheetDatasource.getById(id).flatMap { cached ->
            if (cached != null) {
                Result.success(cached)
            } else {
                timesheetsClient.getTimesheetById(id).flatMap {
                    timesheetDatasource.insert(it.toTimesheetEntity())
                }
            }
        }

    suspend fun updateTimesheet(newTimesheet: TimesheetForm): Result<Timesheet> {
        return timesheetsClient.updateTimesheet(newTimesheet).flatMap {
            loadTimesheetById(newTimesheet.id!!).onSuccess { _ ->
                timesheetDatasource.update(it.toTimesheetEntity())
            }
        }
    }

    suspend fun deleteTimesheet(id: Long): Result<Unit> {
        return timesheetsClient.deleteTimesheet(id).flatMap {
            timesheetDatasource.delete(id)
        }
    }

    suspend fun restartTimesheet(id: Long): Result<Timesheet> {
        return timesheetsClient.restartTimesheet(id).flatMap {
            timesheetDatasource.insert(it.toTimesheetEntity())
        }
    }

    suspend fun stopTimesheet(id: Long): Result<Timesheet> {
        return timesheetsClient.stopTimesheet(id).flatMap {
            timesheetDatasource.insert(it.toTimesheetEntity())
        }
    }

    suspend fun addTimesheet(timesheet: TimesheetForm): Result<Unit> {
        return kotlin.runCatching {
            timesheetsClient.createTimesheet(timesheet).flatMap {
                Result.success(
                    timesheetDatasource.insert(it.toTimesheetEntity())
                )
            }
        }
    }

    suspend fun createTimesheet(timesheet: TimesheetForm): Result<TimesheetForm> {
        return kotlin.runCatching {
            timesheetsClient.createTimesheet(timesheet).flatMap {
                timesheetDatasource.insert(it.toTimesheetEntity())
            }

            // get new timesheet with correct id
            timesheetsClient.getActiveTimesheets().getOrThrow().map {
                it.toTimesheetForm()
            }.first()
        }
    }

    fun getRunningTimesheetStream(): Flow<TimesheetForm?> =
        timesheetDatasource.getActive().getOrDefault(emptyFlow()).map {
            it?.toTimesheetForm()
        }

    fun invalidateCache() = timesheetDatasource.deleteAll()
}
