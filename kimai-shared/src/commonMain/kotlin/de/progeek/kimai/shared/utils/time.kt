package de.progeek.kimai.shared.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

expect fun String.fromISOTime(): Instant

expect fun String.toLocalDatetime(): LocalDateTime