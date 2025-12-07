package de.progeek.kimai.shared

import kotlinx.coroutines.CoroutineDispatcher

interface KimaiDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

expect val kimaiDispatchers: KimaiDispatchers
