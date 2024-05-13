package de.progeek.kimai.shared.core.models

import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

data class Timesheet(
    val id: Long,
    val project: Project,
    val activity: Activity,
    val begin: LocalDateTime,
    val end: LocalDateTime?,
    val duration: Duration?,
    val description: String?,
    val exported: Boolean
) {
    val descriptionOrActivityName: String get() = description ?: activity.name
}