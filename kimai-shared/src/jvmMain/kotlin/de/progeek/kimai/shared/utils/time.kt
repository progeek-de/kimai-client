@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.Instant.Companion.fromEpochMilliseconds

actual fun String.fromISOTime(): Instant {
    TimeZone.currentSystemDefault()

    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
    val date: Date = dateFormat.parse(this)
    return fromEpochMilliseconds(date.time)
}

actual fun String.toLocalDatetime(): LocalDateTime {
    return this.fromISOTime().toLocalDateTime(TimeZone.currentSystemDefault())
}
