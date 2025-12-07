package de.progeek.kimai.shared.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

expect fun Instant.format(pattern: String): String
expect fun LocalDateTime.format(pattern: String): String
expect fun Duration.format(pattern: String): String
