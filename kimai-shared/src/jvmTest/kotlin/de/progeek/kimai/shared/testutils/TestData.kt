package de.progeek.kimai.shared.testutils

import de.progeek.kimai.shared.core.models.*
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration.Companion.hours

/**
 * Test data factory for UI tests.
 * Provides consistent test data across all UI tests.
 */
object TestData {

    // Customers
    val customer1 = Customer(id = 1L, name = "Test Customer 1")
    val customer2 = Customer(id = 2L, name = "Test Customer 2")
    val customer3 = Customer(id = 3L, name = "Progeek GmbH")

    val customers = listOf(customer1, customer2, customer3)

    // Projects
    val project1 = Project(
        id = 1L,
        name = "Test Project 1",
        parent = "",
        globalActivities = true,
        customer = customer1
    )
    val project2 = Project(
        id = 2L,
        name = "Test Project 2",
        parent = "",
        globalActivities = false,
        customer = customer1
    )
    val project3 = Project(
        id = 3L,
        name = "Kimai Client",
        parent = "",
        globalActivities = true,
        customer = customer3
    )

    val projects = listOf(project1, project2, project3)

    // Activities
    val activity1 = Activity(id = 1L, name = "Development", project = 1L)
    val activity2 = Activity(id = 2L, name = "Testing", project = 1L)
    val activity3 = Activity(id = 3L, name = "Meeting", project = null) // Global activity
    val activity4 = Activity(id = 4L, name = "Code Review", project = 3L)

    val activities = listOf(activity1, activity2, activity3, activity4)

    // Timesheets
    val timesheet1 = Timesheet(
        id = 1L,
        project = project1,
        activity = activity1,
        begin = LocalDateTime(2025, 1, 1, 9, 0),
        end = LocalDateTime(2025, 1, 1, 12, 0),
        duration = 3.hours,
        description = "Implemented new feature",
        exported = false
    )

    val timesheet2 = Timesheet(
        id = 2L,
        project = project1,
        activity = activity2,
        begin = LocalDateTime(2025, 1, 1, 13, 0),
        end = LocalDateTime(2025, 1, 1, 15, 30),
        duration = 2.5.hours,
        description = "Writing unit tests",
        exported = false
    )

    val timesheet3 = Timesheet(
        id = 3L,
        project = project3,
        activity = activity4,
        begin = LocalDateTime(2025, 1, 2, 10, 0),
        end = LocalDateTime(2025, 1, 2, 11, 0),
        duration = 1.hours,
        description = "Code review for PR #123",
        exported = true
    )

    // Running timesheet (no end time)
    val runningTimesheet = Timesheet(
        id = 4L,
        project = project1,
        activity = activity1,
        begin = LocalDateTime(2025, 1, 3, 8, 0),
        end = null,
        duration = null,
        description = "Working on UI tests",
        exported = false
    )

    val timesheets = listOf(timesheet1, timesheet2, timesheet3)
    val allTimesheets = listOf(timesheet1, timesheet2, timesheet3, runningTimesheet)

    // Credentials
    val validCredentials = Credentials(
        email = "test@example.com",
        password = "testpassword123"
    )

    val invalidCredentials = Credentials(
        email = "invalid@example.com",
        password = "wrongpassword"
    )

    // Base URLs
    const val TEST_BASE_URL = "https://test.kimai.cloud"
    const val TEST_VERSION = "1.0.0-test"

    /**
     * Creates a timesheet with custom values.
     */
    fun createTimesheet(
        id: Long = 1L,
        project: Project = project1,
        activity: Activity = activity1,
        begin: LocalDateTime = LocalDateTime(2025, 1, 1, 9, 0),
        end: LocalDateTime? = LocalDateTime(2025, 1, 1, 12, 0),
        description: String? = "Test description",
        exported: Boolean = false
    ): Timesheet = Timesheet(
        id = id,
        project = project,
        activity = activity,
        begin = begin,
        end = end,
        duration = if (end != null) 3.hours else null,
        description = description,
        exported = exported
    )

    /**
     * Creates a project with custom values.
     */
    fun createProject(
        id: Long = 1L,
        name: String = "Test Project",
        parent: String = "",
        globalActivities: Boolean = true,
        customer: Customer? = customer1
    ): Project = Project(
        id = id,
        name = name,
        parent = parent,
        globalActivities = globalActivities,
        customer = customer
    )

    /**
     * Creates an activity with custom values.
     */
    fun createActivity(
        id: Long = 1L,
        name: String = "Test Activity",
        project: Long? = 1L
    ): Activity = Activity(
        id = id,
        name = name,
        project = project
    )

    /**
     * Creates a customer with custom values.
     */
    fun createCustomer(
        id: Long = 1L,
        name: String = "Test Customer"
    ): Customer = Customer(
        id = id,
        name = name
    )
}