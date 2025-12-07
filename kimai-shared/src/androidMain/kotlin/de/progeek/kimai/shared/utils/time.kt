package de.progeek.kimai.shared.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun String.fromISOTime(): Instant {
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
    val date: Date = dateFormat.parse(this) ?: throw Exception("Unable to parse Time")

    return fromEpochMilliseconds(date.time)
}

actual fun String.toLocalDatetime(): LocalDateTime {
    return this.fromISOTime().toLocalDateTime(TimeZone.currentSystemDefault())
}
