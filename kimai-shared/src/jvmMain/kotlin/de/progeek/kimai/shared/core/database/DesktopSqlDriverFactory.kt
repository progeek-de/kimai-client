package de.progeek.kimai.shared.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.scope.Scope
import java.io.File

/**
 * Returns the platform-specific application data directory.
 * - Linux: ~/.local/share/kimai-client/
 * - macOS: ~/Library/Application Support/kimai-client/
 * - Windows: %APPDATA%/kimai-client/
 */
private fun getAppDataDirectory(): File {
    val osName = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")

    val appDataDir = when {
        osName.contains("linux") -> File(userHome, ".local/share/kimai-client")
        osName.contains("mac") || osName.contains("darwin") -> File(userHome, "Library/Application Support/kimai-client")
        osName.contains("windows") -> {
            val appData = System.getenv("APPDATA") ?: File(userHome, "AppData/Roaming").path
            File(appData, "kimai-client")
        }
        else -> File(userHome, ".kimai-client") // Fallback
    }

    // Ensure the directory exists
    if (!appDataDir.exists()) {
        appDataDir.mkdirs()
    }

    return appDataDir
}

actual fun Scope.sqlDriverFactory(): SqlDriver {
    val appDataDir = getAppDataDirectory()
    val databasePath = File(appDataDir, "${DatabaseConstants.name}.db")
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
