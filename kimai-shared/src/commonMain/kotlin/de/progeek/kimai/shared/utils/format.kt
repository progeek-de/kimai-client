@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.utils

import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

expect fun Instant.format(pattern: String): String
expect fun LocalDateTime.format(pattern: String): String
expect fun Duration.format(pattern: String): String
