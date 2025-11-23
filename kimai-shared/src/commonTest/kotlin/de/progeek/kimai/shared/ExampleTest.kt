package de.progeek.kimai.shared

import de.progeek.kimai.shared.utils.createTestDatabase
import de.progeek.kimai.shared.utils.mockActivityRepository
import de.progeek.kimai.shared.utils.mockTimesheetsClient
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Example test demonstrating the test infrastructure setup.
 * This shows how to use the test utilities for mocking and database testing.
 */
class ExampleTest {

    @Test
    fun `test infrastructure - mock clients work`() = runTest {
        // Demonstrate MockK works with our mock utilities
        val client = mockTimesheetsClient()

        // Configure mock behavior
        coEvery { client.getTimeSheets(any(), any()) } returns Result.success(emptyList())

        // Call the mocked method
        val result = client.getTimeSheets(kotlinx.datetime.Clock.System.now(), 10)

        // Verify
        assert(result.isSuccess)
        coVerify { client.getTimeSheets(any(), any()) }
    }

    @Test
    fun `test infrastructure - mock repositories work`() {
        // Demonstrate repository mocking works
        val repository = mockActivityRepository()

        assertNotNull(repository)
    }

    @Test
    fun `test infrastructure - in-memory database works`() {
        // Demonstrate SQLDelight in-memory database works
        val database = createTestDatabase()

        assertNotNull(database)
        assertNotNull(database.timesheetEntityQueries)
        assertNotNull(database.projectEntityQueries)
        assertNotNull(database.activityEntityQueries)
        assertNotNull(database.customerEntityQueries)
    }

    @Test
    fun `test infrastructure - coroutines test works`() = runTest {
        // Demonstrate kotlinx-coroutines-test works
        var executed = false

        // Simulate async operation
        kotlinx.coroutines.delay(1)
        executed = true

        assert(executed)
    }
}
