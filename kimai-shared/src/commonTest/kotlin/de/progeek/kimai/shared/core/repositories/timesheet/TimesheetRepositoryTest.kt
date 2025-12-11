package de.progeek.kimai.shared.core.repositories.timesheet

import app.cash.turbine.test
import de.progeek.kimai.openapi.models.TimesheetCollection
import de.progeek.kimai.shared.TimesheetEntity
import de.progeek.kimai.shared.core.database.datasource.timesheet.TimesheetDatasource
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.network.client.TimesheetsClient
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import de.progeek.kimai.openapi.models.TimesheetEntity as ApiTimesheetEntity

/**
 * Test suite for TimesheetRepository.
 *
 * Tests the following methods:
 * 1. timesheetsStream() - Returns Flow<List<Timesheet>>
 * 2. loadNewTimesheets(beforeDate, pageSize) - Returns Result<Instant?>
 * 3. loadTimesheetById(id) - Returns Result<Timesheet>
 * 4. updateTimesheet(newTimesheet) - Returns Result<Timesheet>
 * 5. deleteTimesheet(id) - Returns Result<Unit>
 * 6. restartTimesheet(id) - Returns Result<Timesheet>
 * 7. stopTimesheet(id) - Returns Result<Timesheet>
 * 8. addTimesheet(timesheet) - Returns Result<Unit>
 * 9. createTimesheet(timesheet) - Returns Result<TimesheetForm>
 * 10. getRunningTimesheetStream() - Returns Flow<TimesheetForm?>
 * 11. invalidateCache() - Returns Unit
 */
class TimesheetRepositoryTest {

    private lateinit var mockClient: TimesheetsClient
    private lateinit var mockDatasource: TimesheetDatasource
    private lateinit var repository: TimesheetRepository

    private val testInstant = Clock.System.now()
    private val testDateTime = testInstant.toLocalDateTime(TimeZone.UTC)
    private val testActivity = Activity(id = 1, name = "Development", project = null)
    private val testProject = Project(id = 1, name = "Test Project", parent = "", globalActivities = true, customer = null)

    @BeforeTest
    fun setup() {
        mockClient = mockk(relaxed = true)
        mockDatasource = mockk(relaxed = true)
        repository = TimesheetRepository(mockClient, mockDatasource)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    // Test data helpers

    private fun createTestTimesheet(
        id: Long = 1L,
        begin: LocalDateTime = testDateTime,
        end: LocalDateTime? = null,
        description: String? = "Test timesheet"
    ) = Timesheet(
        id = id,
        begin = begin,
        end = end,
        duration = end?.let { 2.hours } ?: null,
        description = description,
        project = testProject,
        activity = testActivity,
        exported = false
    )

    private fun createTestTimesheetEntity(
        id: Long = 1L,
        begin: Long = testInstant.toEpochMilliseconds(),
        end: Long? = testInstant.plus(2.hours).toEpochMilliseconds(),
        description: String? = "Test timesheet"
    ) = TimesheetEntity(
        id = id,
        begin = begin,
        end = end,
        duration = end?.let { it - begin } ?: 0L,
        description = description,
        project = 1L,
        activity = 1L,
        exported = 0L
    )

    private fun createTestTimesheetForm(
        id: Long? = null,
        begin: Instant = testInstant,
        end: Instant? = testInstant.plus(2.hours),
        description: String? = "Test timesheet"
    ) = TimesheetForm(
        id = id,
        begin = begin.toLocalDateTime(TimeZone.UTC),
        end = end?.toLocalDateTime(TimeZone.UTC),
        project = testProject,
        activity = testActivity,
        description = description
    )

    private fun createApiTimesheetCollection(
        id: Long = 1L,
        begin: String = "2025-01-15T09:00:00+0000",
        end: String? = "2025-01-15T11:00:00+0000"
    ) = TimesheetCollection(
        id = id.toInt(),
        begin = begin,
        end = end,
        exported = false,
        billable = true,
        project = 1,
        activity = 1
    )

    private fun createApiTimesheetEntity(
        id: Long = 1L,
        begin: String = "2025-01-15T09:00:00+0000",
        end: String? = "2025-01-15T11:00:00+0000"
    ) = ApiTimesheetEntity(
        id = id.toInt(),
        begin = begin,
        end = end,
        exported = false,
        billable = true,
        project = 1,
        activity = 1,
        user = 1
    )

    // ============================================================
    // timesheetsStream() Tests
    // ============================================================

    @Test
    fun `timesheetsStream returns flow from datasource`() = runTest {
        // Given
        val testTimesheets = listOf(
            createTestTimesheet(id = 1),
            createTestTimesheet(id = 2)
        )
        every { mockDatasource.getAll() } returns Result.success(flowOf(testTimesheets))

        // When
        val flow = repository.timesheetsStream()

        // Then
        flow.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals(1L, items[0].id)
            assertEquals(2L, items[1].id)
            awaitComplete()
        }
        verify { mockDatasource.getAll() }
    }

    @Test
    fun `timesheetsStream returns empty flow when datasource is empty`() = runTest {
        // Given
        every { mockDatasource.getAll() } returns Result.success(flowOf(emptyList()))

        // When
        val flow = repository.timesheetsStream()

        // Then
        flow.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            awaitComplete()
        }
    }

    // ============================================================
    // loadNewTimesheets() Tests
    // ============================================================

    @Test
    fun `loadNewTimesheets fetches and stores timesheets successfully`() = runTest {
        // Given
        val apiTimesheets = listOf(
            createApiTimesheetCollection(id = 1, begin = "2025-01-15T09:00:00+0000"),
            createApiTimesheetCollection(id = 2, begin = "2025-01-14T10:00:00+0000")
        )
        coEvery { mockClient.getTimeSheets(any(), any()) } returns Result.success(apiTimesheets)
        coEvery { mockDatasource.insert(any<List<TimesheetEntity>>()) } returns Result.success(Unit)

        // When
        val result = repository.loadNewTimesheets(testInstant, 10)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        coVerify { mockClient.getTimeSheets(testInstant, 10) }
        coVerify { mockDatasource.insert(match<List<TimesheetEntity>> { it.size == 2 }) }
    }

    @Test
    fun `loadNewTimesheets returns null when no timesheets found`() = runTest {
        // Given
        coEvery { mockClient.getTimeSheets(any(), any()) } returns Result.success(emptyList())
        coEvery { mockDatasource.insert(emptyList()) } returns Result.success(Unit)

        // When
        val result = repository.loadNewTimesheets(testInstant, 10)

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `loadNewTimesheets handles client error`() = runTest {
        // Given
        val error = Exception("Network error")
        coEvery { mockClient.getTimeSheets(any(), any()) } returns Result.failure(error)

        // When
        val result = repository.loadNewTimesheets(testInstant, 10)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ============================================================
    // loadTimesheetById() Tests
    // ============================================================

    @Test
    fun `loadTimesheetById returns cached timesheet when available`() = runTest {
        // Given
        val cachedTimesheet = createTestTimesheet(id = 123)
        coEvery { mockDatasource.getById(123) } returns Result.success(cachedTimesheet)

        // When
        val result = repository.loadTimesheetById(123)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(123L, result.getOrNull()?.id)
        coVerify { mockDatasource.getById(123) }
        coVerify(exactly = 0) { mockClient.getTimesheetById(any()) }
    }

    @Test
    fun `loadTimesheetById fetches from network when not cached`() = runTest {
        // Given
        coEvery { mockDatasource.getById(123) } returns Result.success(null)
        val apiTimesheet = createApiTimesheetEntity(id = 123)
        coEvery { mockClient.getTimesheetById(123) } returns Result.success(apiTimesheet)
        val insertedTimesheet = createTestTimesheet(id = 123)
        coEvery { mockDatasource.insert(any<TimesheetEntity>()) } returns Result.success(insertedTimesheet)

        // When
        val result = repository.loadTimesheetById(123)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(123L, result.getOrNull()?.id)
        coVerify { mockDatasource.getById(123) }
        coVerify { mockClient.getTimesheetById(123) }
        coVerify { mockDatasource.insert(any<TimesheetEntity>()) }
    }

    @Test
    fun `loadTimesheetById handles datasource error`() = runTest {
        // Given
        val error = Exception("Database error")
        coEvery { mockDatasource.getById(123) } returns Result.failure(error)

        // When
        val result = repository.loadTimesheetById(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loadTimesheetById handles client error when not cached`() = runTest {
        // Given
        coEvery { mockDatasource.getById(123) } returns Result.success(null)
        val error = Exception("Network error")
        coEvery { mockClient.getTimesheetById(123) } returns Result.failure(error)

        // When
        val result = repository.loadTimesheetById(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ============================================================
    // updateTimesheet() Tests
    // ============================================================

    @Test
    fun `updateTimesheet updates timesheet successfully`() = runTest {
        // Given
        val form = createTestTimesheetForm(id = 123)
        val updatedApiEntity = createApiTimesheetEntity(id = 123)
        val updatedTimesheet = createTestTimesheet(id = 123)

        coEvery { mockClient.updateTimesheet(form) } returns Result.success(updatedApiEntity)
        coEvery { mockDatasource.getById(123) } returns Result.success(updatedTimesheet)
        coEvery { mockDatasource.update(any()) } returns Result.success(updatedTimesheet)

        // When
        val result = repository.updateTimesheet(form)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(123L, result.getOrNull()?.id)
        coVerify { mockClient.updateTimesheet(form) }
        coVerify { mockDatasource.getById(123) }
        coVerify { mockDatasource.update(any()) }
    }

    @Test
    fun `updateTimesheet handles client error`() = runTest {
        // Given
        val form = createTestTimesheetForm(id = 123)
        val error = Exception("Network error")
        coEvery { mockClient.updateTimesheet(form) } returns Result.failure(error)

        // When
        val result = repository.updateTimesheet(form)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ============================================================
    // deleteTimesheet() Tests
    // ============================================================

    @Test
    fun `deleteTimesheet deletes timesheet successfully`() = runTest {
        // Given
        coEvery { mockClient.deleteTimesheet(123) } returns Result.success(Unit)
        coEvery { mockDatasource.delete(123) } returns Result.success(Unit)

        // When
        val result = repository.deleteTimesheet(123)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockClient.deleteTimesheet(123) }
        coVerify { mockDatasource.delete(123) }
    }

    @Test
    fun `deleteTimesheet handles client error`() = runTest {
        // Given
        val error = Exception("Network error")
        coEvery { mockClient.deleteTimesheet(123) } returns Result.failure(error)

        // When
        val result = repository.deleteTimesheet(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteTimesheet handles datasource error`() = runTest {
        // Given
        coEvery { mockClient.deleteTimesheet(123) } returns Result.success(Unit)
        val error = Exception("Database error")
        coEvery { mockDatasource.delete(123) } returns Result.failure(error)

        // When
        val result = repository.deleteTimesheet(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    // ============================================================
    // restartTimesheet() Tests
    // ============================================================

    @Test
    fun `restartTimesheet restarts timesheet successfully`() = runTest {
        // Given
        val restartedEntity = createApiTimesheetEntity(id = 124)
        val restartedTimesheet = createTestTimesheet(id = 124)
        coEvery { mockClient.restartTimesheet(123) } returns Result.success(restartedEntity)
        coEvery { mockDatasource.insert(any<TimesheetEntity>()) } returns Result.success(restartedTimesheet)

        // When
        val result = repository.restartTimesheet(123)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(124L, result.getOrNull()?.id)
        coVerify { mockClient.restartTimesheet(123) }
        coVerify { mockDatasource.insert(any<TimesheetEntity>()) }
    }

    @Test
    fun `restartTimesheet handles client error`() = runTest {
        // Given
        val error = Exception("Network error")
        coEvery { mockClient.restartTimesheet(123) } returns Result.failure(error)

        // When
        val result = repository.restartTimesheet(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ============================================================
    // stopTimesheet() Tests
    // ============================================================

    @Test
    fun `stopTimesheet stops timesheet successfully`() = runTest {
        // Given
        val stoppedEntity = createApiTimesheetEntity(id = 123, end = "2025-01-15T17:00:00+0000")
        val endDateTime = LocalDateTime.parse("2025-01-15T17:00:00")
        val stoppedTimesheet = createTestTimesheet(id = 123, end = endDateTime)
        coEvery { mockClient.stopTimesheet(123) } returns Result.success(stoppedEntity)
        coEvery { mockDatasource.insert(any<TimesheetEntity>()) } returns Result.success(stoppedTimesheet)

        // When
        val result = repository.stopTimesheet(123)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull()?.end)
        coVerify { mockClient.stopTimesheet(123) }
        coVerify { mockDatasource.insert(any<TimesheetEntity>()) }
    }

    @Test
    fun `stopTimesheet handles client error`() = runTest {
        // Given
        val error = Exception("Network error")
        coEvery { mockClient.stopTimesheet(123) } returns Result.failure(error)

        // When
        val result = repository.stopTimesheet(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ============================================================
    // addTimesheet() Tests
    // ============================================================

    @Test
    fun `addTimesheet creates timesheet successfully`() = runTest {
        // Given
        val form = createTestTimesheetForm()
        val createdEntity = createApiTimesheetEntity(id = 125)
        val createdTimesheet = createTestTimesheet(id = 125)
        coEvery { mockClient.createTimesheet(form) } returns Result.success(createdEntity)
        coEvery { mockDatasource.insert(any<TimesheetEntity>()) } returns Result.success(createdTimesheet)

        // When
        val result = repository.addTimesheet(form)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockClient.createTimesheet(form) }
        coVerify { mockDatasource.insert(any<TimesheetEntity>()) }
    }

    @Test
    fun `addTimesheet handles client error`() = runTest {
        // Given
        val form = createTestTimesheetForm()
        val error = Exception("Network error")
        coEvery { mockClient.createTimesheet(any()) } returns Result.failure(error)

        // When
        val result = repository.addTimesheet(form)

        // Then
        assertTrue(result.isFailure, "Client error should propagate as failure")
        assertEquals("Network error", result.exceptionOrNull()?.message)
        // Verify datasource insert was never called since flatMap short-circuits
        coVerify(exactly = 0) { mockDatasource.insert(any<TimesheetEntity>()) }
    }

    // ============================================================
    // createTimesheet() Tests
    // ============================================================

    @Test
    fun `createTimesheet creates and returns new timesheet form`() = runTest {
        // Given
        val form = createTestTimesheetForm()
        val createdEntity = createApiTimesheetEntity(id = 125)
        val createdTimesheet = createTestTimesheet(id = 125)
        coEvery { mockClient.createTimesheet(form) } returns Result.success(createdEntity)
        coEvery { mockDatasource.insert(any<TimesheetEntity>()) } returns Result.success(createdTimesheet)

        // Mock getActiveTimesheets to return a form
        val activeTimesheets = listOf(
            de.progeek.kimai.openapi.models.TimesheetCollectionExpanded(
                id = 125,
                begin = "2025-01-15T09:00:00+0000",
                exported = false,
                billable = true,
                activity = de.progeek.kimai.openapi.models.ActivityExpanded(
                    id = 1,
                    name = "Development",
                    visible = true,
                    billable = true
                ),
                project = de.progeek.kimai.openapi.models.ProjectExpanded(
                    id = 1,
                    name = "Test Project",
                    visible = true,
                    billable = true,
                    globalActivities = true,
                    customer = de.progeek.kimai.openapi.models.Customer(
                        id = 1,
                        name = "Test Customer",
                        visible = true,
                        billable = true
                    )
                ),
                user = de.progeek.kimai.openapi.models.User(
                    id = 1,
                    username = "testuser",
                    alias = "Test User"
                )
            )
        )
        coEvery { mockClient.getActiveTimesheets() } returns Result.success(activeTimesheets)

        // When
        val result = repository.createTimesheet(form)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        coVerify { mockClient.createTimesheet(form) }
        coVerify { mockClient.getActiveTimesheets() }
    }

    @Test
    fun `createTimesheet handles client error`() = runTest {
        // Given
        val form = createTestTimesheetForm()
        val error = Exception("Network error")
        coEvery { mockClient.createTimesheet(form) } throws error

        // When
        val result = repository.createTimesheet(form)

        // Then
        assertTrue(result.isFailure)
    }

    // ============================================================
    // getRunningTimesheetStream() Tests
    // ============================================================

    @Test
    fun `getRunningTimesheetStream returns active timesheet form`() = runTest {
        // Given
        val activeTimesheet = createTestTimesheet(id = 123, end = null)
        every { mockDatasource.getActive() } returns Result.success(flowOf(activeTimesheet))

        // When
        val flow = repository.getRunningTimesheetStream()

        // Then
        flow.test {
            val form = awaitItem()
            assertNotNull(form)
            assertEquals(123L, form?.id)
            assertNull(form?.end)
            awaitComplete()
        }
    }

    @Test
    fun `getRunningTimesheetStream returns null when no active timesheet`() = runTest {
        // Given
        every { mockDatasource.getActive() } returns Result.success(flowOf(null))

        // When
        val flow = repository.getRunningTimesheetStream()

        // Then
        flow.test {
            val form = awaitItem()
            assertNull(form)
            awaitComplete()
        }
    }

    @Test
    fun `getRunningTimesheetStream returns empty flow on datasource error`() = runTest {
        // Given
        every { mockDatasource.getActive() } returns Result.failure(Exception("Database error"))

        // When
        val flow = repository.getRunningTimesheetStream()

        // Then
        flow.test {
            // Should emit nothing and complete due to emptyFlow fallback
            awaitComplete()
        }
    }

    // ============================================================
    // invalidateCache() Tests
    // ============================================================

    @Test
    fun `invalidateCache clears all timesheets from datasource`() = runTest {
        // Given
        every { mockDatasource.deleteAll() } returns Unit

        // When
        repository.invalidateCache()

        // Then
        verify { mockDatasource.deleteAll() }
    }
}
