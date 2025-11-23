package de.progeek.kimai.shared.core.network.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.openapi.apis.TimesheetApi
import de.progeek.kimai.openapi.infrastructure.HttpResponse
import de.progeek.kimai.openapi.models.*
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.TimesheetForm
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import io.ktor.http.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import kotlin.test.*

/**
 * Tests for TimesheetsClient.
 * Verifies API interactions, error handling, authentication, and response mapping.
 */
class TimesheetsClientTest {

    private lateinit var mockApi: TimesheetApi
    private lateinit var mockSettings: ObservableSettings
    private lateinit var mockCipher: AesGCMCipher
    private lateinit var client: TimesheetsClient

    private val testInstant = Clock.System.now()
    private val testTimestamp = testInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    private val testTimestampPlus2Hours = testInstant.plus(2, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault())
    private val testActivity = Activity(id = 1, name = "Development", project = null)
    private val testProject = Project(id = 1, name = "Test Project", parent = "", globalActivities = true, customer = null)

    @BeforeTest
    fun setup() {
        mockApi = mockk(relaxed = true)
        mockSettings = mockk(relaxed = true)
        mockCipher = mockk(relaxed = true)
        client = TimesheetsClient(mockSettings, mockCipher, mockApi)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    // Test getTimeSheets method

    @Test
    fun `getTimeSheets returns success with list of timesheets`() = runTest {
        // Given
        val timesheets = listOf(
            TimesheetCollection(
                begin = "2024-01-01T10:00:00",
                exported = false,
                billable = true,
                id = 1,
                end = "2024-01-01T12:00:00",
                project = 1,
                activity = 1
            ),
            TimesheetCollection(
                begin = "2024-01-02T10:00:00",
                exported = false,
                billable = true,
                id = 2,
                end = "2024-01-02T12:00:00",
                project = 1,
                activity = 1
            )
        )
        val response = mockk<HttpResponse<List<TimesheetCollection>>> {
            every { success } returns true
            coEvery { body() } returns timesheets
        }
        coEvery {
            mockApi.getGetTimesheets(
                user = any(),
                users = any(),
                customer = any(),
                customers = any(),
                project = any(),
                projects = any(),
                activity = any(),
                activities = any(),
                page = any(),
                size = any(),
                tags = any(),
                orderBy = any(),
                order = any(),
                begin = any(),
                end = any(),
                exported = any(),
                active = any(),
                billable = any(),
                full = any(),
                term = any(),
                modifiedAfter = any()
            )
        } returns response

        // When
        val result = client.getTimeSheets(testInstant, 10)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getTimeSheets returns failure when API returns error`() = runTest {
        // Given
        val response = mockk<HttpResponse<List<TimesheetCollection>>> {
            every { success } returns false
        }
        coEvery { mockApi.getGetTimesheets(any(), any()) } returns response

        // When
        val result = client.getTimeSheets(testInstant, 10)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error while getting timesheets", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getTimeSheets handles network exception`() = runTest {
        // Given
        coEvery {
            mockApi.getGetTimesheets(
                user = any(),
                users = any(),
                customer = any(),
                customers = any(),
                project = any(),
                projects = any(),
                activity = any(),
                activities = any(),
                page = any(),
                size = any(),
                tags = any(),
                orderBy = any(),
                order = any(),
                begin = any(),
                end = any(),
                exported = any(),
                active = any(),
                billable = any(),
                full = any(),
                term = any(),
                modifiedAfter = any()
            )
        } throws Exception("Network error")

        // When
        val result = client.getTimeSheets(testInstant, 10)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // Test getTimesheetById method

    @Test
    fun `getTimesheetById returns success with timesheet entity`() = runTest {
        // Given
        val timesheetEntity = TimesheetEntity(
            begin = "2024-01-01T10:00:00",
            exported = false,
            billable = true,
            id = 123,
            end = "2024-01-01T12:00:00",
            project = 1,
            activity = 1,
            user = 1
        )
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { status } returns HttpStatusCode.OK.value
            every { success } returns true
            coEvery { body() } returns timesheetEntity
        }
        coEvery { mockApi.getGetTimesheet("123") } returns response

        // When
        val result = client.getTimesheetById(123)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(123, result.getOrNull()?.id)
        coVerify { mockApi.getGetTimesheet("123") }
    }

    @Test
    fun `getTimesheetById returns failure when status is not OK`() = runTest {
        // Given
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { status } returns HttpStatusCode.NotFound.value
            every { success } returns false
            coEvery { body() } returns TimesheetEntity(begin = "", exported = false, billable = true, id = 123, project = 1, activity = 1, user = 1)
        }
        coEvery { mockApi.getGetTimesheet("123") } returns response

        // When
        val result = client.getTimesheetById(123)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Error fetching timesheet by id: 123") == true)
    }

    @Test
    fun `getTimesheetById handles network exception`() = runTest {
        // Given
        coEvery { mockApi.getGetTimesheet(any()) } throws Exception("Network error")

        // When
        val result = client.getTimesheetById(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // Test updateTimesheet method

    @Test
    fun `updateTimesheet returns success with updated timesheet`() = runTest {
        // Given
        val timesheetForm = TimesheetForm(
            id = 123,
            begin = testTimestamp,
            end = testTimestampPlus2Hours,
            project = testProject,
            activity = testActivity,
            description = "Test description"
        )
        val updatedEntity = TimesheetEntity(
            begin = "2024-01-01T10:00:00",
            exported = false,
            billable = true,
            id = 123,
            end = "2024-01-01T12:00:00",
            project = 1,
            activity = 1,
            user = 1
        )
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { success } returns true
            coEvery { body() } returns updatedEntity
        }
        coEvery { mockApi.patchPatchTimesheet(any(), any()) } returns response

        // When
        val result = client.updateTimesheet(timesheetForm)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(123, result.getOrNull()?.id)
        coVerify { mockApi.patchPatchTimesheet("123", any()) }
    }

    @Test
    fun `updateTimesheet returns failure when API returns error`() = runTest {
        // Given
        val timesheetForm = TimesheetForm(
            id = 123,
            begin = testTimestamp,
            end = testTimestampPlus2Hours,
            project = testProject,
            activity = testActivity
        )
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { success } returns false
        }
        coEvery { mockApi.patchPatchTimesheet(any(), any()) } returns response

        // When
        val result = client.updateTimesheet(timesheetForm)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error while updating timesheet", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateTimesheet handles network exception`() = runTest {
        // Given
        val timesheetForm = TimesheetForm(
            id = 123,
            begin = testTimestamp,
            end = testTimestampPlus2Hours,
            project = testProject,
            activity = testActivity
        )
        coEvery { mockApi.patchPatchTimesheet(any(), any()) } throws Exception("Network error")

        // When
        val result = client.updateTimesheet(timesheetForm)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // Test deleteTimesheet method

    @Test
    fun `deleteTimesheet returns success`() = runTest {
        // Given
        val response = mockk<HttpResponse<Unit>> {
            every { success } returns true
        }
        coEvery { mockApi.deleteDeleteTimesheet("123") } returns response

        // When
        val result = client.deleteTimesheet(123)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockApi.deleteDeleteTimesheet("123") }
    }

    @Test
    fun `deleteTimesheet returns failure when API returns error`() = runTest {
        // Given
        val response = mockk<HttpResponse<Unit>> {
            every { success } returns false
        }
        coEvery { mockApi.deleteDeleteTimesheet("123") } returns response

        // When
        val result = client.deleteTimesheet(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error while deleting timesheet", result.exceptionOrNull()?.message)
    }

    // Test restartTimesheet method

    @Test
    fun `restartTimesheet returns success with restarted timesheet`() = runTest {
        // Given
        val restartedEntity = TimesheetEntity(
            begin = "2024-01-03T10:00:00",
            exported = false,
            billable = true,
            id = 124,
            project = 1,
            activity = 1,
            user = 1
        )
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { success } returns true
            coEvery { body() } returns restartedEntity
        }
        coEvery { mockApi.patchRestartTimesheet("123", any()) } returns response

        // When
        val result = client.restartTimesheet(123)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(124, result.getOrNull()?.id)
        coVerify { mockApi.patchRestartTimesheet("123", any()) }
    }

    @Test
    fun `restartTimesheet returns failure when API returns error`() = runTest {
        // Given
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { success } returns false
        }
        coEvery { mockApi.patchRestartTimesheet("123", any()) } returns response

        // When
        val result = client.restartTimesheet(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error while restarting timesheet", result.exceptionOrNull()?.message)
    }

    // Test stopTimesheet method

    @Test
    fun `stopTimesheet returns success with stopped timesheet`() = runTest {
        // Given
        val stoppedEntity = TimesheetEntity(
            begin = "2024-01-01T10:00:00",
            exported = false,
            billable = true,
            id = 123,
            end = "2024-01-01T12:00:00",
            project = 1,
            activity = 1,
            user = 1
        )
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { success } returns true
            coEvery { body() } returns stoppedEntity
        }
        coEvery { mockApi.patchStopTimesheet("123") } returns response

        // When
        val result = client.stopTimesheet(123)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull()?.end)
        coVerify { mockApi.patchStopTimesheet("123") }
    }

    @Test
    fun `stopTimesheet returns failure when API returns error`() = runTest {
        // Given
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { success } returns false
        }
        coEvery { mockApi.patchStopTimesheet("123") } returns response

        // When
        val result = client.stopTimesheet(123)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error while stopping timesheet", result.exceptionOrNull()?.message)
    }

    // Test createTimesheet method

    @Test
    fun `createTimesheet returns success with created timesheet`() = runTest {
        // Given
        val timesheetForm = TimesheetForm(
            id = null,
            begin = testTimestamp,
            end = testTimestampPlus2Hours,
            project = testProject,
            activity = testActivity,
            description = "New timesheet"
        )
        val createdEntity = TimesheetEntity(
            begin = "2024-01-01T10:00:00",
            exported = false,
            billable = true,
            id = 125,
            end = "2024-01-01T12:00:00",
            project = 1,
            activity = 1,
            user = 1
        )
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { success } returns true
            coEvery { body() } returns createdEntity
        }
        coEvery { mockApi.postPostTimesheet(any()) } returns response

        // When
        val result = client.createTimesheet(timesheetForm)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(125, result.getOrNull()?.id)
        coVerify { mockApi.postPostTimesheet(any()) }
    }

    @Test
    fun `createTimesheet returns failure when activity is null`() = runTest {
        // Given
        val timesheetForm = TimesheetForm(
            id = null,
            begin = testTimestamp,
            end = testTimestampPlus2Hours,
            project = testProject,
            activity = null
        )

        // When
        val result = client.createTimesheet(timesheetForm)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Activity and Project can't be null", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createTimesheet returns failure when project is null`() = runTest {
        // Given
        val timesheetForm = TimesheetForm(
            id = null,
            begin = testTimestamp,
            end = testTimestampPlus2Hours,
            project = null,
            activity = testActivity
        )

        // When
        val result = client.createTimesheet(timesheetForm)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Activity and Project can't be null", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createTimesheet returns failure when API returns error`() = runTest {
        // Given
        val timesheetForm = TimesheetForm(
            id = null,
            begin = testTimestamp,
            end = testTimestampPlus2Hours,
            project = testProject,
            activity = testActivity
        )
        val response = mockk<HttpResponse<TimesheetEntity>> {
            every { success } returns false
        }
        coEvery { mockApi.postPostTimesheet(any()) } returns response

        // When
        val result = client.createTimesheet(timesheetForm)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error while updating timesheet", result.exceptionOrNull()?.message)
    }

    // Test getActiveTimesheets method

    @Test
    fun `getActiveTimesheets returns success with list of active timesheets`() = runTest {
        // Given
        val activeTimesheets = listOf(
            TimesheetCollectionExpanded(
                begin = "2024-01-01T10:00:00",
                user = User(username = "testuser", id = 1, alias = "testuser"),
                activity = ActivityExpanded(name = "Development", visible = true, billable = true, id = 1),
                project = ProjectExpanded(
                    customer = Customer(name = "Test Customer", visible = true, billable = true, id = 1),
                    name = "Test Project",
                    visible = true,
                    billable = true,
                    globalActivities = true,
                    id = 1
                ),
                exported = false,
                billable = true,
                id = 1
            )
        )
        val response = mockk<HttpResponse<List<TimesheetCollectionExpanded>>> {
            every { success } returns true
            coEvery { body() } returns activeTimesheets
        }
        coEvery { mockApi.getActiveTimesheet() } returns response

        // When
        val result = client.getActiveTimesheets()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        coVerify { mockApi.getActiveTimesheet() }
    }

    @Test
    fun `getActiveTimesheets returns failure when API returns error`() = runTest {
        // Given
        val response = mockk<HttpResponse<List<TimesheetCollectionExpanded>>> {
            every { success } returns false
        }
        coEvery { mockApi.getActiveTimesheet() } returns response

        // When
        val result = client.getActiveTimesheets()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Error while getting active timesheets", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getActiveTimesheets handles network exception`() = runTest {
        // Given
        coEvery { mockApi.getActiveTimesheet() } throws Exception("Network error")

        // When
        val result = client.getActiveTimesheets()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
