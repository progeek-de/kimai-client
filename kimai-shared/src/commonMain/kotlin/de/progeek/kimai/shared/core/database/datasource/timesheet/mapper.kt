package de.progeek.kimai.shared.core.database.datasource.timesheet

import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.utils.toLocalDatetime
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal inline fun toTimesheet(
    id: Long,
    begin: Long,
    end: Long?,
    duration: Long?,
    description: String?,
    project: Long,
    activity: Long,
    exported: Long,
    ac_id: Long,
    ac_name: String,
    pr_id: Long,
    pr_name: String,
    pr_parent: String,
    pr_globalActivities: Long,
    cu_id: Long,
    cu_name: String
): Timesheet {
    return Timesheet(
        id = id,
        begin = begin.toLocalDatetime(),
        end = end?.toLocalDatetime(),
        duration = duration?.toDuration(DurationUnit.SECONDS),
        description = description,
        project = Project(
            id = pr_id, name = pr_name, parent = pr_parent, globalActivities = pr_globalActivities == 1L,
            customer = Customer(id = cu_id, name = cu_name)
        ),
        activity = Activity(
            id = ac_id, name = ac_name, project = pr_id
        ),
        exported = exported == 1L
    )
}