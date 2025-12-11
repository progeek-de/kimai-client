package de.progeek.kimai.shared.testutils

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.KimaiDispatchers
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.core.models.EntryMode
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.repositories.activity.ActivityRepository
import de.progeek.kimai.shared.core.repositories.auth.AuthRepository
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.repositories.customer.CustomerRepository
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import de.progeek.kimai.shared.core.repositories.settings.SettingsRepository
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketConfigRepository
import de.progeek.kimai.shared.core.ticketsystem.repository.TicketSystemRepository
import de.progeek.kimai.shared.ui.theme.ThemeEnum
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Test Koin module configuration for UI tests.
 * Provides mock implementations of all repositories and dependencies.
 */
object TestKoinModule {

    /**
     * Creates a test dispatchers implementation using UnconfinedTestDispatcher.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun createTestDispatchers(): KimaiDispatchers {
        val testDispatcher = UnconfinedTestDispatcher()
        return object : KimaiDispatchers {
            override val main: CoroutineDispatcher = testDispatcher
            override val io: CoroutineDispatcher = testDispatcher
            override val unconfined: CoroutineDispatcher = testDispatcher
        }
    }

    /**
     * Creates a relaxed mock TimesheetRepository with default behavior.
     */
    fun createMockTimesheetRepository(
        timesheets: List<Timesheet> = TestData.timesheets,
        runningTimesheet: TimesheetForm? = null
    ): TimesheetRepository = mockk(relaxed = true) {
        every { timesheetsStream() } returns MutableStateFlow(timesheets)
        every { getRunningTimesheetStream() } returns MutableStateFlow(runningTimesheet)
        coEvery { loadNewTimesheets(any(), any()) } returns Result.success(null)
        coEvery { updateTimesheet(any()) } answers {
            val form = firstArg<TimesheetForm>()
            Result.success(TestData.createTimesheet(id = form.id ?: 1L))
        }
        coEvery { deleteTimesheet(any()) } returns Result.success(Unit)
        coEvery { createTimesheet(any()) } answers {
            val form = firstArg<TimesheetForm>()
            Result.success(form)
        }
    }

    /**
     * Creates a relaxed mock ProjectRepository with default behavior.
     */
    fun createMockProjectRepository(): ProjectRepository = mockk(relaxed = true) {
        every { getProjects() } returns flowOf(TestData.projects)
        coEvery { invalidateCache() } returns Unit
    }

    /**
     * Creates a relaxed mock ActivityRepository with default behavior.
     */
    fun createMockActivityRepository(): ActivityRepository = mockk(relaxed = true) {
        every { getActivities() } returns flowOf(TestData.activities)
        coEvery { invalidateCache() } returns Unit
    }

    /**
     * Creates a relaxed mock CustomerRepository with default behavior.
     */
    fun createMockCustomerRepository(): CustomerRepository = mockk(relaxed = true) {
        every { getCustomers() } returns flowOf(TestData.customers)
        coEvery { invalidateCache() } returns Unit
    }

    /**
     * Creates a relaxed mock AuthRepository with configurable login behavior.
     */
    fun createMockAuthRepository(
        loginSuccess: Boolean = true
    ): AuthRepository = mockk(relaxed = true) {
        coEvery { login(any(), any(), any()) } returns if (loginSuccess) {
            TestData.validCredentials
        } else {
            null
        }
        coEvery { logout() } returns Result.success(Unit)
    }

    /**
     * Creates a relaxed mock CredentialsRepository with configurable credentials.
     */
    fun createMockCredentialsRepository(
        credentials: Credentials? = null
    ): CredentialsRepository = mockk(relaxed = true) {
        every { get() } returns MutableStateFlow(credentials)
        every { getCredentials() } returns credentials
        coEvery { save(any()) } returns Result.success(Unit)
        coEvery { delete() } returns Result.success(Unit)
    }

    /**
     * Creates a relaxed mock SettingsRepository with default behavior.
     */
    fun createMockSettingsRepository(
        theme: ThemeEnum = ThemeEnum.LIGHT,
        defaultProjectId: Long? = null,
        baseUrl: String = TestData.TEST_BASE_URL,
        entryMode: EntryMode = EntryMode.TIMER
    ): SettingsRepository = mockk(relaxed = true) {
        every { getTheme() } returns flowOf(theme)
        every { getDefaultProject() } returns flowOf(defaultProjectId)
        every { getBaseUrl() } returns baseUrl
        every { getLanguage() } returns flowOf(null)
        every { getEntryMode() } returns flowOf(entryMode)
        coEvery { saveTheme(any()) } answers { firstArg() }
        coEvery { saveDefaultProject(any()) } answers { firstArg() }
    }

    /**
     * Creates a relaxed mock TicketSystemRepository with default behavior.
     */
    fun createMockTicketSystemRepository(): TicketSystemRepository = mockk(relaxed = true) {
        every { getAllIssues() } returns flowOf(emptyList())
        coEvery { searchWithFallback(any(), any()) } returns Result.success(emptyList())
        coEvery { hasEnabledSources() } returns false
    }

    /**
     * Creates a relaxed mock TicketConfigRepository with default behavior.
     */
    fun createMockTicketConfigRepository(): TicketConfigRepository = mockk(relaxed = true) {
        every { getAllConfigs() } returns flowOf(emptyList())
        every { getEnabledConfigs() } returns flowOf(emptyList())
        every { hasEnabledConfigs() } returns flowOf(false)
        coEvery { countEnabled() } returns Result.success(0L)
    }

    /**
     * Creates the complete test Koin module with all mock dependencies.
     */
    fun createTestModule(
        timesheetRepository: TimesheetRepository = createMockTimesheetRepository(),
        projectRepository: ProjectRepository = createMockProjectRepository(),
        activityRepository: ActivityRepository = createMockActivityRepository(),
        customerRepository: CustomerRepository = createMockCustomerRepository(),
        authRepository: AuthRepository = createMockAuthRepository(),
        credentialsRepository: CredentialsRepository = createMockCredentialsRepository(),
        settingsRepository: SettingsRepository = createMockSettingsRepository(),
        ticketSystemRepository: TicketSystemRepository = createMockTicketSystemRepository(),
        ticketConfigRepository: TicketConfigRepository = createMockTicketConfigRepository()
    ): Module = module {
        single { timesheetRepository }
        single { projectRepository }
        single { activityRepository }
        single { customerRepository }
        single { authRepository }
        single<CredentialsRepository> { credentialsRepository }
        single { settingsRepository }
        single { ticketSystemRepository }
        single { ticketConfigRepository }
        single<StoreFactory> { DefaultStoreFactory() }
    }

    /**
     * Starts Koin with the test module.
     * Call this in @BeforeTest.
     */
    fun startTestKoin(
        timesheetRepository: TimesheetRepository = createMockTimesheetRepository(),
        projectRepository: ProjectRepository = createMockProjectRepository(),
        activityRepository: ActivityRepository = createMockActivityRepository(),
        customerRepository: CustomerRepository = createMockCustomerRepository(),
        authRepository: AuthRepository = createMockAuthRepository(),
        credentialsRepository: CredentialsRepository = createMockCredentialsRepository(),
        settingsRepository: SettingsRepository = createMockSettingsRepository(),
        ticketSystemRepository: TicketSystemRepository = createMockTicketSystemRepository(),
        ticketConfigRepository: TicketConfigRepository = createMockTicketConfigRepository()
    ) {
        startKoin {
            modules(
                createTestModule(
                    timesheetRepository = timesheetRepository,
                    projectRepository = projectRepository,
                    activityRepository = activityRepository,
                    customerRepository = customerRepository,
                    authRepository = authRepository,
                    credentialsRepository = credentialsRepository,
                    settingsRepository = settingsRepository,
                    ticketSystemRepository = ticketSystemRepository,
                    ticketConfigRepository = ticketConfigRepository
                )
            )
        }
    }

    /**
     * Stops Koin.
     * Call this in @AfterTest.
     */
    fun stopTestKoin() {
        stopKoin()
    }
}
