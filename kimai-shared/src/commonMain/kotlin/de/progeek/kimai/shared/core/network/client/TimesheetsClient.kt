package de.progeek.kimai.shared.core.network.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.openapi.apis.TimesheetApi
import de.progeek.kimai.openapi.models.*
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.network.NetworkConstants.datetimeFormat
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import de.progeek.kimai.shared.utils.format
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TimesheetsClient(
    settings: ObservableSettings,
    aesCipher: AesGCMCipher,
    private val client: TimesheetApi,
): CredentialsListener(settings, aesCipher, client) {

    suspend fun getTimeSheets(beforeDate: Instant, pageSize: Int): Result<List<TimesheetCollection>> =
        kotlin.runCatching {
            val end = beforeDate.minus(1L.toDuration(DurationUnit.SECONDS)).format(datetimeFormat)
            val response = client.getGetTimesheets(end = end, size = "$pageSize")
            when(response.success) {
                true -> response.body()
                false -> throw Throwable("Error while getting timesheets")
            }
        }

    suspend fun getTimesheetById(id: Long): Result<TimesheetEntity> = kotlin.runCatching {
        val response = client.getGetTimesheet("$id")
        when (response.status == HttpStatusCode.OK.value) {
            true -> response.body()
            false -> throw Throwable("Error fetching timesheet by id: $id")
        }
    }

    suspend fun updateTimesheet(newTimesheet: TimesheetForm): Result<TimesheetEntity> =
        kotlin.runCatching {
            val response = client.patchPatchTimesheet(newTimesheet.id.toString(),
                TimesheetEditForm(
                    begin = newTimesheet.begin.format(datetimeFormat),
                    end = newTimesheet.end?.format(datetimeFormat),
                    project = newTimesheet.project!!.id.toInt(),
                    activity = newTimesheet.activity!!.id.toInt(),
                    description = newTimesheet.description,
                )
            )

            when(response.success) {
                true -> response.body()
                false -> throw Throwable("Error while updating timesheet")
            }
        }

    suspend fun deleteTimesheet(id: Long): Result<Unit> = kotlin.runCatching {
        val response = client.deleteDeleteTimesheet("$id")
        if (!response.success) {
            throw Throwable("Error while deleting timesheet")
        }
    }

    suspend fun restartTimesheet(id: Long): Result<TimesheetEntity> = kotlin.runCatching {
        val response = client.patchRestartTimesheet("$id", GetRestartTimesheetGetRequest(copy = "all"))
        when(response.success) {
            true -> response.body()
            false -> throw Throwable("Error while restarting timesheet")
        }
    }

    suspend fun stopTimesheet(id: Long): Result<TimesheetEntity> = kotlin.runCatching {
        val response = client.patchStopTimesheet("$id")
        when(response.success) {
            true -> response.body()
            false -> throw Throwable("Error while stopping timesheet")
        }
    }

    suspend fun createTimesheet(timesheet: TimesheetForm): Result<TimesheetEntity> = kotlin.runCatching {
        if(timesheet.activity == null || timesheet.project == null) {
            throw Throwable("Activity and Project can't be null")
        }

        val response = client.postPostTimesheet(
            TimesheetEditForm(
                begin = timesheet.begin.format(datetimeFormat),
                end = timesheet.end?.format(datetimeFormat),
                project = timesheet.project.id.toInt(),
                activity = timesheet.activity.id.toInt(),
                description = timesheet.description,
            )
        )

        return when(response.success) {
            true -> Result.success(response.body())
            false -> Result.failure(Throwable("Error while updating timesheet"))
        }
    }

    suspend fun getActiveTimesheets(): Result<List<TimesheetCollectionExpanded>> {
        val response = client.getActiveTimesheet()

        return when(response.success) {
            true -> Result.success(response.body())
            false -> Result.failure(Throwable("Error while getting active timesheets"))
        }
    }
}