package de.progeek.kimai.shared.ui.form.customer

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.models.Activity
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.models.Timesheet
import de.progeek.kimai.shared.core.repositories.customer.CustomerRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Test suite for CustomerFieldStore.
 *
 * Tests the following functionality:
 * - Loading customers from repository on bootstrap
 * - Selecting a customer and emitting labels
 * - Initializing with customer from timesheet
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CustomerFieldStoreTest {

    private lateinit var customerRepository: CustomerRepository
    private lateinit var storeFactory: CustomerFieldStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCustomer1 = Customer(id = 1, name = "Customer 1")
    private val testCustomer2 = Customer(id = 2, name = "Customer 2")
    private val testCustomer3 = Customer(id = 3, name = "Customer 3")

    private val allCustomers = listOf(testCustomer1, testCustomer2, testCustomer3)

    private val testProject = Project(
        id = 1,
        name = "Test Project",
        parent = "",
        globalActivities = true,
        customer = testCustomer1
    )

    private val testActivity = Activity(id = 1, name = "Development", project = testProject.id)

    @BeforeTest
    fun setup() {
        customerRepository = mockk(relaxed = true)

        // Setup Koin for CustomerFieldStoreFactory dependency injection
        startKoin {
            modules(
                module {
                    single { customerRepository }
                }
            )
        }

        storeFactory = CustomerFieldStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        clearAllMocks()
    }

    // Helper function to create a test timesheet
    private fun createTestTimesheet(customer: Customer = testCustomer1) = Timesheet(
        id = 1L,
        begin = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        end = Clock.System.now().plus(2, DateTimeUnit.HOUR, TimeZone.currentSystemDefault()).toLocalDateTime(TimeZone.currentSystemDefault()),
        duration = 2.hours,
        description = "Test",
        project = testProject.copy(customer = customer),
        activity = testActivity,
        exported = false
    )

    // ===== Bootstrap and Initial State Tests =====

    @Test
    fun `initial state with no timesheet has null selected customer`() = runTest(testDispatcher) {
        every { customerRepository.getCustomers() } returns flowOf(emptyList())

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertNull(state.selectedCustomer)
        assertTrue(state.customers.isEmpty())
    }

    @Test
    fun `initial state with timesheet loads customer from timesheet project`() = runTest(testDispatcher) {
        every { customerRepository.getCustomers() } returns flowOf(emptyList())

        val timesheet = createTestTimesheet(customer = testCustomer1)
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        val state = store.stateFlow.value
        assertEquals(testCustomer1, state.selectedCustomer)
    }

    @Test
    fun `bootstrap loads customers from repository`() = runTest(testDispatcher) {
        every { customerRepository.getCustomers() } returns flowOf(allCustomers)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(3, state.customers.size)
        assertTrue(state.customers.contains(testCustomer1))
        assertTrue(state.customers.contains(testCustomer2))
        assertTrue(state.customers.contains(testCustomer3))
    }

    // ===== Intent: SelectedCustomer Tests =====

    @Test
    fun `SelectedCustomer intent updates selected customer in state`() = runTest(testDispatcher) {
        every { customerRepository.getCustomers() } returns flowOf(allCustomers)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        store.accept(CustomerFieldStore.Intent.SelectedCustomer(testCustomer1))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testCustomer1, state.selectedCustomer)
    }

    @Test
    fun `SelectedCustomer intent emits CustomerChanged label`() = runTest(testDispatcher) {
        every { customerRepository.getCustomers() } returns flowOf(allCustomers)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        var labelEmitted = false
        var emittedCustomer: Customer? = null
        val job = CoroutineScope(testDispatcher).launch {
            store.labels.collect { label ->
                if (label is CustomerFieldStore.Label.CustomerChanged) {
                    labelEmitted = true
                    emittedCustomer = label.customer
                }
            }
        }

        store.accept(CustomerFieldStore.Intent.SelectedCustomer(testCustomer1))
        advanceUntilIdle()

        assertTrue(labelEmitted, "CustomerChanged label should be emitted")
        assertEquals(testCustomer1, emittedCustomer)

        job.cancel()
    }

    @Test
    fun `SelectedCustomer intent can change customer selection`() = runTest(testDispatcher) {
        every { customerRepository.getCustomers() } returns flowOf(allCustomers)

        val timesheet = createTestTimesheet(customer = testCustomer1)
        val store = storeFactory.create(
            timesheet = timesheet,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Initially customer1 is selected
        assertEquals(testCustomer1, store.stateFlow.value.selectedCustomer)

        // Change to customer2
        store.accept(CustomerFieldStore.Intent.SelectedCustomer(testCustomer2))
        advanceUntilIdle()

        val state = store.stateFlow.value
        assertEquals(testCustomer2, state.selectedCustomer)
    }

    // ===== Integration Tests =====

    @Test
    fun `full workflow - load customers and select one`() = runTest(testDispatcher) {
        every { customerRepository.getCustomers() } returns flowOf(allCustomers)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Step 1: Customers loaded
        assertEquals(3, store.stateFlow.value.customers.size)
        assertNull(store.stateFlow.value.selectedCustomer)

        // Step 2: Select a customer
        store.accept(CustomerFieldStore.Intent.SelectedCustomer(testCustomer2))
        advanceUntilIdle()

        val finalState = store.stateFlow.value
        assertEquals(testCustomer2, finalState.selectedCustomer)
    }

    @Test
    fun `multiple customer selections update state correctly`() = runTest(testDispatcher) {
        every { customerRepository.getCustomers() } returns flowOf(allCustomers)

        val store = storeFactory.create(
            timesheet = null,
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Select customer 1
        store.accept(CustomerFieldStore.Intent.SelectedCustomer(testCustomer1))
        advanceUntilIdle()
        assertEquals(testCustomer1, store.stateFlow.value.selectedCustomer)

        // Select customer 2
        store.accept(CustomerFieldStore.Intent.SelectedCustomer(testCustomer2))
        advanceUntilIdle()
        assertEquals(testCustomer2, store.stateFlow.value.selectedCustomer)

        // Select customer 3
        store.accept(CustomerFieldStore.Intent.SelectedCustomer(testCustomer3))
        advanceUntilIdle()
        assertEquals(testCustomer3, store.stateFlow.value.selectedCustomer)
    }
}
