package de.progeek.kimai.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun rememberImeState(): State<Boolean?> {
    return remember { mutableStateOf(false) }
}
