package de.progeek.kimai.shared.core.database

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.scope.Scope

expect fun Scope.sqlDriverFactory(): SqlDriver
fun createDatabase(driver: SqlDriver): KimaiDatabase =
    KimaiDatabase(driver = driver,)