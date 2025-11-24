package de.progeek.kimai.shared.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

actual fun Scope.sqlDriverFactory(): SqlDriver {
    return AndroidSqliteDriver(
        schema = KimaiDatabase.Schema,
        context = androidContext(),
        name = "${DatabaseConstants.name}.db",
        callback = object : AndroidSqliteDriver.Callback(KimaiDatabase.Schema) {
            override fun onOpen(db: app.cash.sqldelight.db.SqlDriver) {
                super.onOpen(db)
            }
        }
    )
}