# Story 1.6: Implement Plugin Fault Tolerance

Status: review

## Story

As a **user**,
I want **a faulty plugin to not crash the entire application**,
So that **I can continue using other features and plugins**.

## Acceptance Criteria

1. **Given** a plugin throws an exception during `init()` or any operation
   **When** the exception occurs
   **Then** the exception is caught by PluginManager

2. **And** the plugin is marked as FAILED

3. **And** an error is logged with plugin id and stack trace

4. **And** the user sees a notification: "Plugin {name} failed to load"

5. **And** other plugins continue to work normally

6. **And** the core application remains fully functional

## Tasks / Subtasks

- [x] Task 1: Enhance Exception Handling in PluginManager (AC: #1, #2, #3, #5)
  - [x] Verify existing `runCatching` blocks in `initializeSinglePlugin()` handle all exceptions
  - [x] Add comprehensive exception handling for all `Plugin.init()` calls
  - [x] Ensure exceptions during discovery don't crash the app
  - [x] Add specific exception type handling (SecurityException, OutOfMemoryError, etc.)
  - [x] Log full stack traces with Napier.e(throwable = ...)

- [x] Task 2: Implement Plugin Error Notification System (AC: #4)
  - [x] Create `PluginNotificationListener.kt` interface for plugin failure notifications
  - [x] Implement notification callback mechanism in PluginManager
  - [x] Add `onPluginFailed(pluginId: String, name: String, reason: String)` callback
  - [x] Store failed plugin notifications for UI retrieval
  - [x] Ensure notifications are non-blocking and don't interrupt other plugins

- [x] Task 3: Add Failed Plugins Query API (AC: #2, #6)
  - [x] Add `getFailedPlugins(): List<PluginWrapper>` method to PluginManager interface
  - [x] Add `PluginFailureInfo` data class with detailed failure information
  - [x] Implement method to get failure details: `getPluginFailure(id: String): PluginFailureInfo?`
  - [x] Expose failure timestamp and exception type in PluginFailureInfo

- [x] Task 4: Implement Runtime Error Isolation (AC: #5, #6)
  - [x] Wrap all plugin method calls in try-catch in PluginContext operations
  - [x] Add error boundary pattern for plugin operations (PluginOperations.kt)
  - [x] Ensure failed plugins don't block PluginContext operations for other plugins
  - [x] Add timeout mechanism for slow plugin operations (configurable, default 5s)

- [x] Task 5: Add Plugin Health Status (AC: #2, #6)
  - [x] Create `PluginHealth` enum: HEALTHY, DEGRADED, FAILED
  - [x] Add health status to PluginWrapper or separate tracking
  - [x] Track number of errors per plugin for degraded state
  - [x] Implement `getPluginHealth(id: String): PluginHealth` method

- [x] Task 6: Write Unit Tests (AC: all)
  - [x] Test plugin init() exception is caught and logged
  - [x] Test failed plugin is marked as FAILED state
  - [x] Test other plugins continue initialization after one fails
  - [x] Test notification callback is triggered on failure
  - [x] Test getFailedPlugins() returns correct list
  - [x] Test core app functions remain accessible after plugin failure
  - [x] Test timeout mechanism for slow plugins
  - [x] Test error accumulation for DEGRADED state
  - [x] Run `./gradlew :kimai-plugin-api:jvmTest`
  - [x] Regression: `./gradlew :kimai-desktop:compileKotlinJvm`

## Dev Notes

### Architecture Context

This story completes the **Plugin Fault Tolerance** requirement (FR19) and strengthens the **Plugin Loading Pipeline** established in Stories 1.1-1.5:

```
JAR Discovery (1.3) → Version Check (1.4) → Lifecycle Management (1.5) → Fault Tolerance (1.6, THIS)
```

**Critical Requirement:** The core application MUST remain fully functional even if:
- A plugin fails during initialization
- A plugin throws exceptions during runtime operations
- A plugin hangs or runs slow operations

**Reference:** [Source: _bmad-output/architecture.md#Plugin Loading Framework: PF4J]
**Reference:** [Source: _bmad-output/prd.md#FR19 - Graceful plugin failure handling]
**Reference:** [Source: _bmad-output/prd.md#NFR-R4 - App must not crash from faulty plugin]

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Napier | 2.7.1 | Logging |
| Coroutines | 1.10.2 | Async/timeout handling |
| Koin | 4.1.1 | Dependency Injection |

### Existing Components (from Stories 1.1-1.5)

The following components already exist and provide the foundation for fault tolerance:

**PluginManager (from 1.5) - Current Error Handling:**
```kotlin
private fun initializeSinglePlugin(wrapper: PluginWrapper, context: PluginContext): PluginWrapper {
    // Already catches exceptions with runCatching
    return runCatching {
        wrapper.plugin.init(context)
        initializationOrder.add(wrapper.pluginId)
        wrapper.copy(state = PluginState.INITIALIZED)
    }.getOrElse { error ->
        Napier.e(tag = TAG, throwable = error) {
            "Plugin initialization failed: ${wrapper.name} - ${error.message}"
        }
        wrapper.copy(
            state = PluginState.FAILED,
            failureReason = "Init failed: ${error.message}"
        )
    }
}
```

**Key insight:** Basic exception handling EXISTS in 1.5. This story EXTENDS it with:
- User-facing notifications
- Runtime error isolation
- Health tracking
- Failure query API

**PluginWrapper (1.3/1.4):**
```kotlin
data class PluginWrapper(
    val plugin: Plugin,
    val pluginId: String,
    val pluginPath: String,
    val state: PluginState = PluginState.DISCOVERED,
    val failureReason: String? = null  // Already supports failure reason
) {
    val hasFailed: Boolean get() = state == PluginState.FAILED
}
```

**PluginState (1.3):**
```kotlin
enum class PluginState {
    DISCOVERED,   // JAR found, not yet initialized
    INITIALIZED,  // init() called successfully
    DISPOSED,     // dispose() called
    FAILED        // Error during lifecycle
}
```

### Notification System Design

**PluginNotificationListener Interface:**
```kotlin
package de.progeek.kimai.plugin.api

/**
 * Listener interface for plugin lifecycle events and notifications.
 *
 * Implement this interface to receive notifications about plugin failures
 * that should be shown to users.
 */
interface PluginNotificationListener {
    /**
     * Called when a plugin fails to initialize or encounters a fatal error.
     *
     * @param pluginId Plugin identifier (kebab-case)
     * @param pluginName Human-readable plugin name
     * @param reason Human-readable failure reason
     * @param exception The exception that caused the failure (may be null for non-exception failures)
     */
    fun onPluginFailed(
        pluginId: String,
        pluginName: String,
        reason: String,
        exception: Throwable? = null
    )
}
```

**PluginManager Enhancement:**
```kotlin
interface PluginManager {
    // Existing methods...
    fun initializePlugins(context: PluginContext): List<PluginWrapper>
    fun disposePlugins()
    fun getPlugins(): List<PluginWrapper>
    fun getPlugin(id: String): PluginWrapper?
    fun getPluginStateCounts(): Map<PluginState, Int>

    // NEW: Fault tolerance additions

    /**
     * Set a listener for plugin notifications.
     * Only one listener is supported at a time.
     */
    fun setNotificationListener(listener: PluginNotificationListener?)

    /**
     * Get all plugins that have failed.
     * @return List of plugins with state [PluginState.FAILED]
     */
    fun getFailedPlugins(): List<PluginWrapper>

    /**
     * Get detailed failure information for a plugin.
     * @param id Plugin identifier
     * @return Failure details, or null if plugin didn't fail
     */
    fun getPluginFailure(id: String): PluginFailureInfo?
}
```

**PluginFailureInfo Data Class:**
```kotlin
package de.progeek.kimai.plugin.api

import kotlinx.datetime.Instant

/**
 * Detailed information about a plugin failure.
 */
data class PluginFailureInfo(
    val pluginId: String,
    val pluginName: String,
    val reason: String,
    val exceptionType: String?,
    val stackTrace: String?,
    val timestamp: Instant,
    val phase: FailurePhase
)

/**
 * Phase during which the plugin failed.
 */
enum class FailurePhase {
    DISCOVERY,      // Failed during JAR loading
    COMPATIBILITY,  // Failed version check
    INITIALIZATION, // Failed during init()
    RUNTIME,        // Failed during runtime operation
    DISPOSAL        // Failed during dispose()
}
```

### Plugin Health Design

**PluginHealth Enum:**
```kotlin
package de.progeek.kimai.plugin.api

/**
 * Health status of a plugin.
 */
enum class PluginHealth {
    /**
     * Plugin is functioning normally.
     */
    HEALTHY,

    /**
     * Plugin has experienced some errors but is still operational.
     * Typically triggered after 1-3 non-fatal errors.
     */
    DEGRADED,

    /**
     * Plugin has failed and is not operational.
     */
    FAILED
}
```

### Error Isolation Pattern

**Runtime Error Wrapper (for future extension points):**
```kotlin
/**
 * Safely executes a plugin operation with error isolation.
 *
 * @param pluginId The plugin being called
 * @param operation Human-readable operation name for logging
 * @param block The operation to execute
 * @return Result of the operation, or null if it failed
 */
internal inline fun <T> safePluginCall(
    pluginId: String,
    operation: String,
    block: () -> T
): Result<T> {
    return runCatching {
        block()
    }.onFailure { error ->
        Napier.e(tag = "PluginManager", throwable = error) {
            "Plugin '$pluginId' failed during $operation: ${error.message}"
        }
    }
}

/**
 * Safely executes a plugin operation with timeout.
 *
 * @param pluginId The plugin being called
 * @param operation Human-readable operation name
 * @param timeoutMs Maximum time to wait (default 5000ms)
 * @param block The suspending operation to execute
 * @return Result of the operation
 */
internal suspend inline fun <T> safePluginCallWithTimeout(
    pluginId: String,
    operation: String,
    timeoutMs: Long = 5000L,
    crossinline block: suspend () -> T
): Result<T> {
    return runCatching {
        withTimeout(timeoutMs) {
            block()
        }
    }.onFailure { error ->
        val reason = when (error) {
            is TimeoutCancellationException -> "Operation timed out after ${timeoutMs}ms"
            else -> error.message ?: "Unknown error"
        }
        Napier.e(tag = "PluginManager", throwable = error) {
            "Plugin '$pluginId' failed during $operation: $reason"
        }
    }
}
```

### DefaultPluginManager Enhancements

```kotlin
class DefaultPluginManager(
    private val pluginLoader: PluginLoader
) : PluginManager {

    // Existing fields...
    private val plugins = mutableListOf<PluginWrapper>()
    private val initializationOrder = mutableListOf<String>()
    @Volatile private var initialized = false

    // NEW: Fault tolerance additions
    private var notificationListener: PluginNotificationListener? = null
    private val failureInfoMap = mutableMapOf<String, PluginFailureInfo>()

    override fun setNotificationListener(listener: PluginNotificationListener?) {
        notificationListener = listener
    }

    override fun getFailedPlugins(): List<PluginWrapper> =
        plugins.filter { it.state == PluginState.FAILED }

    override fun getPluginFailure(id: String): PluginFailureInfo? =
        failureInfoMap[id]

    // Enhanced initializeSinglePlugin with notification
    private fun initializeSinglePlugin(wrapper: PluginWrapper, context: PluginContext): PluginWrapper {
        if (wrapper.state == PluginState.FAILED) {
            Napier.d(tag = TAG) { "Skipping failed plugin: ${wrapper.name}" }
            return wrapper
        }

        return runCatching {
            Napier.d(tag = TAG) { "Initializing plugin: ${wrapper.name} v${wrapper.version}" }
            wrapper.plugin.init(context)
            initializationOrder.add(wrapper.pluginId)
            Napier.d(tag = TAG) { "Plugin initialized: ${wrapper.name} v${wrapper.version}" }
            wrapper.copy(state = PluginState.INITIALIZED)
        }.getOrElse { error ->
            handlePluginFailure(
                wrapper = wrapper,
                error = error,
                phase = FailurePhase.INITIALIZATION
            )
        }
    }

    private fun handlePluginFailure(
        wrapper: PluginWrapper,
        error: Throwable,
        phase: FailurePhase
    ): PluginWrapper {
        val reason = "Init failed: ${error.message}"

        // Log with full stack trace
        Napier.e(tag = TAG, throwable = error) {
            "Plugin ${phase.name.lowercase()} failed: ${wrapper.name} - ${error.message}"
        }

        // Store detailed failure info
        failureInfoMap[wrapper.pluginId] = PluginFailureInfo(
            pluginId = wrapper.pluginId,
            pluginName = wrapper.name,
            reason = reason,
            exceptionType = error::class.simpleName,
            stackTrace = error.stackTraceToString(),
            timestamp = Clock.System.now(),
            phase = phase
        )

        // Notify listener (for UI notification)
        notificationListener?.onPluginFailed(
            pluginId = wrapper.pluginId,
            pluginName = wrapper.name,
            reason = reason,
            exception = error
        )

        return wrapper.copy(
            state = PluginState.FAILED,
            failureReason = reason
        )
    }
}
```

### Previous Story Intelligence

**From Story 1.5 (Plugin Lifecycle Management):**
- `runCatching` + `getOrElse` pattern established for error handling
- Logging with Napier TAG constant
- PluginWrapper is immutable - always use `copy()`
- State transitions: DISCOVERED -> INITIALIZED or FAILED

**From Story 1.4 (Version Compatibility):**
- PluginWrapper.failureReason field exists
- Compatibility failures already mark plugins as FAILED

**From Story 1.3 (PluginLoader):**
- Discovery errors are already logged
- Plugins can fail at discovery phase

**Code Patterns Established:**
- Interface in commonMain, implementation in jvmMain
- KDoc documentation on all public APIs
- Unit tests with MockK

### Git Intelligence

**Recent Commits:**
- `32686e6` - Story 1.5 (lifecycle management) - done
- `759b132` - Story 1.4 (version compatibility) - done
- `e8f93ca` - Story 1.3 (PluginLoader) - review
- `6a99542` - Story 1.2 (PluginContext)
- `02be9e7` - Story 1.1 (Plugin API module)

**Files Modified in Previous Stories:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginManager.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginManager.kt`
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/PluginManagerTest.kt`

### Project Structure Notes

**Files to Create:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginNotificationListener.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginFailureInfo.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginHealth.kt`

**Files to Modify:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginManager.kt` - Add new methods
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginManager.kt` - Implement new methods
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/PluginManagerTest.kt` - Add new tests

**Files to NOT Modify:**
- Plugin.kt - interface is stable
- PluginWrapper.kt - already has failureReason and hasFailed
- PluginState.kt - already has FAILED state
- PluginContext.kt - no changes needed for fault tolerance

### References

- [Source: _bmad-output/architecture.md#Plugin Loading Framework: PF4J]
- [Source: _bmad-output/prd.md#FR19 - Graceful plugin failure handling]
- [Source: _bmad-output/prd.md#NFR-R4 - App must not crash from faulty plugin]
- [Source: _bmad-output/project-context.md#Plugin System Rules]
- [Source: _bmad-output/implementation-artifacts/1-5-implement-plugin-lifecycle-management.md]

### Claude Skills to Use

- **No special skills needed** - Standard Kotlin implementation
- May use `kotlin-specialist` agent for coroutine timeout patterns

### Anti-Patterns to Avoid

- Do NOT let exceptions propagate uncaught from plugin code
- Do NOT block the main thread waiting for plugin operations
- Do NOT allow one plugin's failure to affect other plugins
- Do NOT skip error logging - always log with full stack trace
- Do NOT modify PluginWrapper directly - use copy()
- Do NOT use static/global mutable state for failure tracking
- Do NOT call notification listener synchronously on main thread (can block UI)
- Do NOT retry failed plugins automatically - let user decide

### Success Criteria

1. `./gradlew :kimai-plugin-api:jvmTest` passes all tests
2. `./gradlew :kimai-desktop:compileKotlinJvm` still works (no regression)
3. Plugin throwing exception during init() is marked FAILED
4. Other plugins continue to initialize after one fails
5. Failure reason is stored and retrievable
6. Notification listener receives failure callbacks
7. Failed plugins list is queryable
8. Full stack traces are logged via Napier
9. Core application functionality unaffected by plugin failures

### Important Notes for Dev Agent

1. **BUILD ON EXISTING CODE:** Story 1.5 already has basic error handling - extend it, don't rewrite
2. **Notification is OPTIONAL:** The listener can be null - check before calling
3. **Thread Safety:** notificationListener access should be @Volatile or synchronized
4. **Test Isolation:** Each test should use fresh PluginManager instances
5. **Don't Block UI:** If future UI integration is added, ensure notifications are async
6. **Preserve Order:** Failed plugins still count for initialization order tracking
7. **Stack Trace Capture:** Use `error.stackTraceToString()` for full trace storage
8. **Timestamp:** Use `kotlinx.datetime.Clock.System.now()` for failure timestamps

### Dependencies to Add

```kotlin
// In kimai-plugin-api/build.gradle.kts
commonMain.dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
}
```

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- All tests passed: `./gradlew :kimai-plugin-api:jvmTest`
- Regression passed: `./gradlew :kimai-desktop:compileKotlinJvm`

### Completion Notes List

1. **Task 1 - Exception Handling**: Enhanced existing `runCatching` pattern with centralized `handlePluginFailure()` method. Full stack traces logged via Napier.e(throwable = ...).

2. **Task 2 - Notification System**: Created `PluginNotificationListener` interface. DefaultPluginManager calls listener.onPluginFailed() when plugins fail initialization.

3. **Task 3 - Failed Plugins API**: Added `getFailedPlugins()`, `getPluginFailure(id)` methods. Created `PluginFailureInfo` data class with phase, timestamp, exceptionType, and stackTrace.

4. **Task 4 - Runtime Error Isolation**: Created `PluginOperations.kt` with `safePluginCall()` and `safePluginCallWithTimeout()` utilities. Default timeout: 5000ms.

5. **Task 5 - Health Status**: Created `PluginHealth` enum (HEALTHY, DEGRADED, FAILED). Added `recordPluginError()` and `getPluginHealth()` methods with error count tracking.

6. **Task 6 - Unit Tests**: Added 17 new fault tolerance tests covering all acceptance criteria. All 35 tests pass.

### Change Log

| Date | Change |
|------|--------|
| 2025-12-28 | Story created by create-story workflow - ready for development |
| 2025-12-28 | Implementation complete - all 6 tasks done, all tests pass, ready for review |

### File List

**New Files:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginNotificationListener.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginFailureInfo.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginHealth.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginOperations.kt`

**Modified Files:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginManager.kt` - Added fault tolerance API
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginManager.kt` - Implemented fault tolerance
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/PluginManagerTest.kt` - Added 17 new tests