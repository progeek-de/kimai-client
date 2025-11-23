package de.progeek.kimai.shared.utils

import de.progeek.kimai.shared.core.repositories.activity.ActivityRepository
import de.progeek.kimai.shared.core.repositories.auth.AuthRepository
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.repositories.customer.CustomerRepository
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import io.mockk.mockk

/**
 * Mock repository factory functions for testing.
 * These functions create MockK instances with configurable behavior.
 */

/**
 * Creates a mock TimesheetRepository with configurable behavior.
 *
 * @param timesheets The list of timesheets to return from timesheetsStream()
 * @param loadByIdResult The result to return from loadTimesheetById()
 * @param updateResult The result to return from updateTimesheet()
 * @param deleteResult The result to return from deleteTimesheet()
 * @param addResult The result to return from addTimesheet()
 * @param restartResult The result to return from restartTimesheet()
 * @param stopResult The result to return from stopTimesheet()
 */
/**
 * Creates a mock TimesheetRepository.
 * Use relaxed mocking for simplified test setup.
 */
fun mockTimesheetRepository(): TimesheetRepository = mockk(relaxed = true)

/**
 * Creates a mock ProjectRepository.
 */
fun mockProjectRepository(): ProjectRepository = mockk(relaxed = true)

/**
 * Creates a mock ActivityRepository.
 */
fun mockActivityRepository(): ActivityRepository = mockk(relaxed = true)

/**
 * Creates a mock CustomerRepository.
 */
fun mockCustomerRepository(): CustomerRepository = mockk(relaxed = true)

/**
 * Creates a mock AuthRepository.
 */
fun mockAuthRepository(): AuthRepository = mockk(relaxed = true)

/**
 * Creates a mock CredentialsRepository.
 */
fun mockCredentialsRepository(): CredentialsRepository = mockk(relaxed = true)

/**
 * Creates a mock SettingsRepository.
 */
fun mockSettingsRepository(): SettingsRepository = mockk(relaxed = true)
