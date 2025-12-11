@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.mapper

import de.progeek.kimai.openapi.models.TimesheetCollection
import de.progeek.kimai.openapi.models.TimesheetCollectionExpanded
import de.progeek.kimai.shared.TimesheetEntity
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.utils.fromISOTime
import de.progeek.kimai.shared.utils.toLocalDatetime
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun TimesheetCollection.toTimesheetEntity(): TimesheetEntity {
    return TimesheetEntity(
        id = this.id?.toLong() ?: -1,
        begin = this.begin.fromISOTime().toEpochMilliseconds(),
        end = this.end?.fromISOTime()?.toEpochMilliseconds(),
        duration = this.duration?.toLong(),
        description = this.description,
        project = this.project?.toLong() ?: -1,
        activity = this.activity?.toLong() ?: -1,
        exported = if (this.exported) 1L else 0L
    )
}

fun de.progeek.kimai.openapi.models.TimesheetEntity.toTimesheetEntity(): TimesheetEntity {
    return TimesheetEntity(
        id = this.id?.toLong() ?: -1,
        begin = this.begin.fromISOTime().toEpochMilliseconds(),
        end = this.end?.fromISOTime()?.toEpochMilliseconds(),
        duration = this.duration?.toLong(),
        description = this.description,
        project = this.project?.toLong() ?: -1,
        activity = this.activity?.toLong() ?: -1,
        exported = if (this.exported) 1L else 0L
    )
}

fun Timesheet.toTimesheetForm(): TimesheetForm {
    return TimesheetForm(
        id,
        project,
        activity,
        begin,
        end,
        duration,
        description
    )
}
fun TimesheetForm.toTimesheet(): Timesheet {
    if (id == null || project == null || activity == null) {
        throw Throwable("ID, Project and Activity can't be null")
    }

    return Timesheet(
        id = id,
        project = project,
        activity = activity,
        begin = begin,
        end = end,
        duration = duration,
        description = description,
        exported = false
    )
}

fun TimesheetCollectionExpanded.toTimesheetForm(): TimesheetForm {
    return TimesheetForm(
        id = id?.toLong() ?: -1,
        project = project.toProject(),
        description = description,
        activity = activity.toActivity(),
        begin = begin.toLocalDatetime(),
        end = end?.toLocalDatetime(),
        duration = duration?.toDuration(DurationUnit.SECONDS)
    )
}