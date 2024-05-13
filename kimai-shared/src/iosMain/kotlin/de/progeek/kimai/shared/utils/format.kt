package de.progeek.kimai.shared.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

actual fun Instant.format(pattern: String): String {
    TODO("Not yet implemented")
}

actual fun LocalDateTime.format(pattern: String): String {
    TODO("Not yet implemented")
}

actual fun Duration.format(pattern: String): String {
    val instant = Instant.fromEpochMilliseconds(this.inWholeMilliseconds)
    return instant.format(pattern)
}