package de.progeek.kimai.shared.ui.home.store

import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.repositories.activity.ActivityRepository
import de.progeek.kimai.shared.core.repositories.customer.CustomerRepository
import de.progeek.kimai.shared.core.repositories.project.ProjectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStoreTest {

    private lateinit var activityRepository: ActivityRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var storeFactory: HomeStoreFactory
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        activityRepository = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)
        customerRepository = mockk(relaxed = true)

        // Setup Koin for HomeStoreFactory dependency injection
        startKoin {
            modules(
                module {
                    single { activityRepository }
                    single { projectRepository }
                    single { customerRepository }
                }
            )
        }

        storeFactory = HomeStoreFactory(DefaultStoreFactory())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        // With UnconfinedTestDispatcher, the bootstrapper runs synchronously
        // So we need to mock the repos to prevent immediate completion
        coEvery { activityRepository.invalidateCache() } coAnswers {
            // Suspend to prevent immediate completion
            kotlinx.coroutines.delay(1)
        }
        coEvery { projectRepository.invalidateCache() } coAnswers {
            kotlinx.coroutines.delay(1)
        }
        coEvery { customerRepository.invalidateCache() } coAnswers {
            kotlinx.coroutines.delay(1)
        }

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Check state before advancing - bootstrapper should have set loading to true
        assertTrue(store.stateFlow.value.isLoading, "Initial state should have isLoading = true")
    }

    @Test
    fun `sync completes and sets loading to false`() = runTest(testDispatcher) {
        coEvery { activityRepository.invalidateCache() } returns Unit
        coEvery { projectRepository.invalidateCache() } returns Unit
        coEvery { customerRepository.invalidateCache() } returns Unit

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Allow async operations to complete
        advanceUntilIdle()

        // After sync completes, isLoading should be false
        assertFalse(store.stateFlow.value.isLoading, "After sync, isLoading should be false")
    }

    @Test
    fun `sync invalidates all repository caches`() = runTest(testDispatcher) {
        coEvery { activityRepository.invalidateCache() } returns Unit
        coEvery { projectRepository.invalidateCache() } returns Unit
        coEvery { customerRepository.invalidateCache() } returns Unit

        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        // Allow async operations to complete
        advanceUntilIdle()

        // Verify cache invalidation was called on all repositories
        coVerify(exactly = 1) { activityRepository.invalidateCache() }
        coVerify(exactly = 1) { projectRepository.invalidateCache() }
        coVerify(exactly = 1) { customerRepository.invalidateCache() }
    }

    @Test
    fun `bootstrapper triggers sync on initialization`() = runTest(testDispatcher) {
        coEvery { activityRepository.invalidateCache() } returns Unit
        coEvery { projectRepository.invalidateCache() } returns Unit
        coEvery { customerRepository.invalidateCache() } returns Unit

        // Create store - bootstrapper should trigger sync
        val store = storeFactory.create(
            mainContext = testDispatcher,
            ioContext = testDispatcher
        )

        advanceUntilIdle()

        // Verify all repositories were accessed
        coVerify(exactly = 1) { activityRepository.invalidateCache() }
        coVerify(exactly = 1) { projectRepository.invalidateCache() }
        coVerify(exactly = 1) { customerRepository.invalidateCache() }

        // Final state should have loading = false
        assertFalse(store.stateFlow.value.isLoading)
    }
}
