package de.progeek.kimai.shared.utils

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import de.progeek.kimai.shared.core.database.KimaiDatabase
import de.progeek.kimai.shared.core.database.datasource.activity.ActivityDatasource
import de.progeek.kimai.shared.core.database.datasource.customer.CustomerDatasource
import de.progeek.kimai.shared.core.database.datasource.project.ProjectDatasource
import de.progeek.kimai.shared.core.database.datasource.timesheet.TimesheetDatasource
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import de.progeek.kimai.shared.core.ticketsystem.datasource.TicketConfigDatasource
import de.progeek.kimai.shared.core.ticketsystem.datasource.TicketIssueDatasource

/**
 * Database test utilities for SQLDelight testing.
 * Provides functions for creating in-memory test databases.
 */

/**
 * Creates an in-memory SQLite database for testing.
 * Each test gets a fresh database instance.
 *
 * Example usage:
 * ```kotlin
 * @Test
 * fun myTest() {
 *     val database = createTestDatabase()
 *     val datasource = TimesheetDatasource(database)
 *     // use datasource in test
 * }
 * ```
 */
fun createTestDatabase(): KimaiDatabase {
    val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    KimaiDatabase.Schema.create(driver)
    return KimaiDatabase(driver)
}

/**
 * Clears all data from the database.
 */
fun clearDatabase(database: KimaiDatabase) {
    database.timesheetEntityQueries.deleteAll()
    database.projectEntityQueries.deleteAll()
    database.activityEntityQueries.deleteAll()
    database.customerEntityQueries.deleteAll()
    database.ticketIssueEntityQueries.deleteAll()
    database.ticketConfigEntityQueries.deleteAll()
}

/**
 * Creates test datasources with an in-memory database.
 * Returns a set of datasources ready for testing.
 *
 * Example usage:
 * ```kotlin
 * val datasources = createTestDatasources()
 * val timesheetDatasource = datasources.timesheetDatasource
 * ```
 */
data class TestDatasources(
    val database: KimaiDatabase,
    val timesheetDatasource: TimesheetDatasource,
    val projectDatasource: ProjectDatasource,
    val activityDatasource: ActivityDatasource,
    val customerDatasource: CustomerDatasource,
    val ticketIssueDatasource: TicketIssueDatasource,
    val ticketConfigDatasource: TicketConfigDatasource
)

/**
 * Creates a set of test datasources with an in-memory database.
 */
fun createTestDatasources(): TestDatasources {
    val database = createTestDatabase()
    val aesCipher = AesGCMCipher()

    return TestDatasources(
        database = database,
        timesheetDatasource = TimesheetDatasource(database),
        projectDatasource = ProjectDatasource(database),
        activityDatasource = ActivityDatasource(database),
        customerDatasource = CustomerDatasource(database),
        ticketIssueDatasource = TicketIssueDatasource(database),
        ticketConfigDatasource = TicketConfigDatasource(database, aesCipher)
    )
}

// Additional helper functions can be added here as needed during test implementation
