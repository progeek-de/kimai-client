package de.progeek.kimai.shared.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.scope.Scope

actual fun Scope.sqlDriverFactory(): SqlDriver {
    return NativeSqliteDriver(
        KimaiDatabase.Schema,
        "${DatabaseConstants.name}.db"
    )
}
