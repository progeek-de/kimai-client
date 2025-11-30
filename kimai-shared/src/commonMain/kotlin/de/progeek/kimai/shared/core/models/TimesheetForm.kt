package de.progeek.kimai.shared.core.models

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

data class TimesheetForm(
    val id: Long? = null,
    val project: Project? = null,
    val activity: Activity? = null,
    val begin: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val end: LocalDateTime? = null,
    val duration: Duration? = null,
    val description: String? = null
)
