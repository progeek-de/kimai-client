package de.progeek.kimai.shared.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

actual fun String.fromISOTime(): Instant = TODO("Not yet implemented")

actual fun String.toLocalDatetime(): LocalDateTime {
    return this.fromISOTime().toSystemLocalDatetime()
}