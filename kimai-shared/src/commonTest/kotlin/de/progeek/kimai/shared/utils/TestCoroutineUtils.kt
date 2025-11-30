package de.progeek.kimai.shared.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Provides a standard test dispatcher for deterministic coroutine testing.
 * Uses UnconfinedTestDispatcher for immediate execution of coroutines.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun testDispatcher(): TestDispatcher = UnconfinedTestDispatcher()

/**
 * Provides a standard test dispatcher with a shared scheduler.
 * Useful when you need multiple dispatchers to share the same virtual time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun testDispatcher(scheduler: TestCoroutineScheduler): TestDispatcher =
    StandardTestDispatcher(scheduler)

/**
 * Creates an unconfined test dispatcher with a shared scheduler.
 * Unconfined dispatcher executes coroutines immediately without delays.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun unconfinedTestDispatcher(scheduler: TestCoroutineScheduler? = null): TestDispatcher =
    if (scheduler != null) {
        UnconfinedTestDispatcher(scheduler)
    } else {
        UnconfinedTestDispatcher()
    }

/**
 * Helper class to set up and tear down test dispatchers for Dispatchers.Main.
 * Use this in tests that interact with Dispatchers.Main.
 *
 * Example usage:
 * ```kotlin
 * @Test
 * fun myTest() = runTest {
 *     val testDispatcherRule = TestDispatcherRule()
 *     testDispatcherRule.setUp()
 *
 *     // Your test code here
 *
 *     testDispatcherRule.tearDown()
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) {
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    fun tearDown() {
        Dispatchers.resetMain()
    }
}

/**
 * Extension function for easier test dispatcher setup and teardown.
 * Automatically sets up and tears down the test dispatcher.
 *
 * Example usage:
 * ```kotlin
 * @Test
 * fun myTest() = runTestWithDispatcher {
 *     // Your test code here
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runTestWithDispatcher(
    testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    testBody: suspend TestScope.() -> Unit
) = runTest {
    val rule = TestDispatcherRule(testDispatcher)
    rule.setUp()
    try {
        testBody()
    } finally {
        rule.tearDown()
    }
}
