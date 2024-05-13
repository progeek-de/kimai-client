package de.progeek.kimai.shared.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.time.Duration

actual fun Instant.format(pattern: String): String {
    val milliSeconds = this.toEpochMilliseconds()
    val formatter = SimpleDateFormat(pattern)

    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}

actual fun LocalDateTime.format(pattern: String): String {
    val instant = this.toInstant(TimeZone.currentSystemDefault())
    return instant.format(pattern)
}

actual fun Duration.format(pattern: String): String {
    val instant = Instant.fromEpochMilliseconds(this.inWholeMilliseconds)
    return instant.format(pattern)
}