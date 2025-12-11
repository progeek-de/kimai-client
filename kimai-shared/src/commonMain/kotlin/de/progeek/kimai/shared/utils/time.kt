@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.utils

import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant

expect fun String.fromISOTime(): Instant

expect fun String.toLocalDatetime(): LocalDateTime
