@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

inline fun Boolean.ok(action: () -> Unit): Boolean {
    if (this) action()
    return this
}

inline fun Boolean.not(action: () -> Unit): Boolean {
    if (!this) action()
    return this
}

fun<T> Boolean.then(first: T): T? {
    return if (this) first else null
}

inline fun <T> T?.isNull(block: () -> Unit): T? {
    if (this == null) block()
    return this@isNull
}

inline fun <T> T?.notNull(block: (T) -> Unit): T? {
    this?.let(block)
    return this@notNull
}

inline fun <E : Any, T : Collection<E>> T.withEmpty(func: () -> T): T {
    if (this.isEmpty()) {
        return func()
    }

    return this
}

fun Long.toLocalDatetime(): LocalDateTime {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}

fun LocalDateTime.removeSeconds(): LocalDateTime {
    return LocalDateTime(year, month, day, hour, minute)
}

@Composable
expect fun rememberImeState(): State<Boolean?>
