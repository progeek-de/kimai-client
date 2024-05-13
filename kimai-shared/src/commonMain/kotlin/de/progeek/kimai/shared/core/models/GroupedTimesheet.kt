package de.progeek.kimai.shared.core.models

import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

data class GroupedTimesheet(
    val date: LocalDateTime,
    val list: List<Timesheet>,
    val total: Duration?,
)