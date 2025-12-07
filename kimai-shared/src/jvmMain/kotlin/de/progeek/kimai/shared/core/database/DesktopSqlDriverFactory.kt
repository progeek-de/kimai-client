package de.progeek.kimai.shared.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.scope.Scope
import java.io.File

actual fun Scope.sqlDriverFactory(): SqlDriver {
    val databasePath = File(System.getProperty("java.io.tmpdir"), "${DatabaseConstants.name}.db")
    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.path}")

    // For now, just create or migrate - SQLDelight 2.0 handles this automatically
    KimaiDatabase.Schema.create(driver)

    // Migration: Add issueFormat column to ticketConfigEntity if it doesn't exist
    migrateTicketConfigEntity(driver)

    return driver
}

private fun migrateTicketConfigEntity(driver: SqlDriver) {
    try {
        // Check if the column already exists by trying to select it
        driver.execute(null, "SELECT issueFormat FROM ticketConfigEntity LIMIT 1", 0, null)
    } catch (e: Exception) {
        // Column doesn't exist, add it
        driver.execute(
            null,
            "ALTER TABLE ticketConfigEntity ADD COLUMN issueFormat TEXT NOT NULL DEFAULT '{key}: {summary}'",
            0,
            null
        )
    }
}
