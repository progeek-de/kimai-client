# Story 1.5: Implement Plugin Lifecycle Management

Status: done

## Story

As a **plugin developer**,
I want **clear lifecycle hooks (init, dispose)**,
So that **my plugin can set up and clean up resources properly**.

## Acceptance Criteria

1. **Given** a compatible plugin is loaded
   **When** PluginManager initializes it
   **Then** `plugin.init(context)` is called with a valid PluginContext

2. **And** the plugin is marked as "initialized" (PluginState.INITIALIZED)

3. **And** when the application shuts down, `plugin.dispose()` is called

4. **And** plugins are disposed in reverse order of initialization

5. **And** PluginManager tracks plugin states (DISCOVERED, INITIALIZED, DISPOSED, FAILED)

## Tasks / Subtasks

- [x] Task 1: Create PluginManager interface (AC: #1, #5)
  - [x] Create `PluginManager.kt` interface in kimai-plugin-api commonMain
  - [x] Define `initializePlugins(context: PluginContext): List<PluginWrapper>` method
  - [x] Define `disposePlugins()` method
  - [x] Define `getPlugins(): List<PluginWrapper>` method
  - [x] Define `getPlugin(id: String): PluginWrapper?` method
  - [x] Add KDoc documentation

- [x] Task 2: Create DefaultPluginManager implementation (AC: #1, #2, #3, #4, #5)
  - [x] Create `DefaultPluginManager.kt` in kimai-plugin-api jvmMain
  - [x] Accept PluginLoader as constructor parameter
  - [x] Implement `initializePlugins()` - call `plugin.init(context)` for each DISCOVERED plugin
  - [x] Update state from DISCOVERED to INITIALIZED on success
  - [x] Update state to FAILED on init error (catch exceptions)
  - [x] Store initialized plugins in internal list (maintain order)

- [x] Task 3: Implement dispose lifecycle (AC: #3, #4)
  - [x] Implement `disposePlugins()` method
  - [x] Dispose plugins in REVERSE order of initialization
  - [x] Call `plugin.dispose()` for each INITIALIZED plugin
  - [x] Update state from INITIALIZED to DISPOSED
  - [x] Handle dispose errors gracefully (log, continue with next plugin)

- [x] Task 4: Implement state tracking and access methods (AC: #5)
  - [x] Track plugin initialization order internally
  - [x] Implement `getPlugins()` returning all known plugins with states
  - [x] Implement `getPlugin(id)` for individual plugin lookup
  - [x] Expose plugin count and state statistics

- [x] Task 5: Add comprehensive logging (AC: all)
  - [x] Log when plugin initialization starts/completes
  - [x] Log when plugin disposal starts/completes
  - [x] Log errors with plugin name and exception message
  - [x] Use Napier with consistent TAG ("PluginManager")

- [x] Task 6: Write unit tests (AC: all)
  - [x] Test successful plugin initialization
  - [x] Test init failure marks plugin as FAILED
  - [x] Test dispose is called in reverse order
  - [x] Test dispose continues on error
  - [x] Test state transitions: DISCOVERED → INITIALIZED → DISPOSED
  - [x] Test getPlugin() returns correct wrapper
  - [x] Run `./gradlew :kimai-plugin-api:jvmTest`
  - [x] Regression: `./gradlew :kimai-desktop:compileKotlinJvm`

## Dev Notes

### Architecture Context

This story implements the **PluginManager** component that manages plugin lifecycle after discovery. It is the final piece of the plugin loading pipeline:

```
JAR Discovery (1.3) → Version Check (1.4) → Lifecycle Management (1.5, THIS) → Fault Tolerance (1.6)
```

**Reference:** [Source: _bmad-output/architecture.md#Plugin Loading Framework: PF4J]

The PluginManager bridges the gap between plugin discovery (PluginLoader) and runtime plugin execution. It ensures plugins are properly initialized with context and cleanly disposed on shutdown.

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| PF4J | 3.12.0 | Plugin framework (already integrated in 1.3) |
| Napier | 2.7.1 | Logging |
| Koin | 4.1.1 | Dependency Injection |

### Existing Components (from Stories 1.1-1.4)

The following components already exist and MUST be reused:

**Plugin Interface (1.1):**
```kotlin
interface Plugin {
    val id: String
    val name: String
    val version: String
    val minAppVersion: String  // Added in 1.4
    val maxAppVersion: String? // Added in 1.4
    fun init(context: PluginContext)
    fun dispose()
}
```

**PluginState Enum (1.3):**
```kotlin
enum class PluginState {
    DISCOVERED,   // JAR found, not yet initialized
    INITIALIZED,  // init() called successfully
    DISPOSED,     // dispose() called
    FAILED        // Error during lifecycle
}
```

**PluginWrapper (1.3, extended in 1.4):**
```kotlin
data class PluginWrapper(
    val plugin: Plugin,
    val pluginId: String,
    val pluginPath: String,
    val state: PluginState = PluginState.DISCOVERED,
    val failureReason: String? = null  // Added in 1.4
)
```

**PluginLoader (1.3):**
```kotlin
interface PluginLoader {
    fun discoverPlugins(): List<PluginWrapper>
    fun getPluginsDirectory(): String
    fun pluginsDirectoryExists(): Boolean
}
```

**DefaultPluginLoader (1.3):**
- Uses PF4J's JarPluginManager
- Returns plugins in DISCOVERED state
- Has `shutdown()` method for cleanup

**PluginContext (1.2):**
```kotlin
interface PluginContext {
    val timesheetListStore: Any
    val projectStore: Any
    val activityStore: Any
    val customerStore: Any
    val mainContext: CoroutineContext
    val ioContext: CoroutineContext
    val appVersion: String  // Added in 1.4
    fun getDatabasePath(name: String): String
    fun getLicenseKey(pluginId: String): String?
}
```

### PluginManager Interface Design

```kotlin
package de.progeek.kimai.plugin.api

/**
 * Manages plugin lifecycle: initialization and disposal.
 *
 * The PluginManager is responsible for:
 * - Initializing discovered plugins with a valid PluginContext
 * - Tracking plugin states throughout their lifecycle
 * - Disposing plugins in reverse order during shutdown
 *
 * Lifecycle flow:
 * 1. PluginLoader discovers plugins (returns DISCOVERED state)
 * 2. PluginManager.initializePlugins() calls plugin.init() (DISCOVERED → INITIALIZED)
 * 3. On shutdown, PluginManager.disposePlugins() calls plugin.dispose() (INITIALIZED → DISPOSED)
 */
interface PluginManager {
    /**
     * Initialize all discovered plugins.
     *
     * Calls [Plugin.init] for each plugin with state [PluginState.DISCOVERED].
     * Successful plugins transition to [PluginState.INITIALIZED].
     * Failed plugins transition to [PluginState.FAILED] with a failure reason.
     *
     * @param context The PluginContext to pass to each plugin's init method
     * @return List of all plugins after initialization attempt (with updated states)
     */
    fun initializePlugins(context: PluginContext): List<PluginWrapper>

    /**
     * Dispose all initialized plugins.
     *
     * Calls [Plugin.dispose] for each plugin with state [PluginState.INITIALIZED].
     * Plugins are disposed in REVERSE order of initialization.
     * Each plugin transitions to [PluginState.DISPOSED] regardless of errors.
     */
    fun disposePlugins()

    /**
     * Get all managed plugins.
     *
     * @return List of all plugins with their current states
     */
    fun getPlugins(): List<PluginWrapper>

    /**
     * Get a specific plugin by its ID.
     *
     * @param id Plugin identifier (kebab-case)
     * @return The plugin wrapper, or null if not found
     */
    fun getPlugin(id: String): PluginWrapper?

    /**
     * Get count of plugins by state.
     *
     * @return Map of state to count
     */
    fun getPluginStateCounts(): Map<PluginState, Int>
}
```

### DefaultPluginManager Implementation Pattern

```kotlin
package de.progeek.kimai.plugin.api

import io.github.aakira.napier.Napier

/**
 * Default implementation of [PluginManager].
 *
 * ## Thread Safety
 * This class is NOT thread-safe. All methods should be called from the main thread
 * during application startup and shutdown.
 *
 * @param pluginLoader The loader that discovers plugins
 */
class DefaultPluginManager(
    private val pluginLoader: PluginLoader
) : PluginManager {

    // Plugins with current states, maintains initialization order
    private val plugins = mutableListOf<PluginWrapper>()

    // Track initialization order for reverse disposal
    private val initializationOrder = mutableListOf<String>()

    override fun initializePlugins(context: PluginContext): List<PluginWrapper> {
        Napier.i(tag = TAG) { "Starting plugin initialization" }

        val discovered = pluginLoader.discoverPlugins()
        Napier.d(tag = TAG) { "Found ${discovered.size} plugins to initialize" }

        discovered.forEach { wrapper ->
            val updated = initializeSinglePlugin(wrapper, context)
            plugins.add(updated)
        }

        val counts = getPluginStateCounts()
        Napier.i(tag = TAG) {
            "Plugin initialization complete: ${counts[PluginState.INITIALIZED] ?: 0} initialized, " +
            "${counts[PluginState.FAILED] ?: 0} failed"
        }

        return plugins.toList()
    }

    private fun initializeSinglePlugin(wrapper: PluginWrapper, context: PluginContext): PluginWrapper {
        // Skip already failed plugins (from compatibility check)
        if (wrapper.state == PluginState.FAILED) {
            Napier.d(tag = TAG) { "Skipping failed plugin: ${wrapper.name}" }
            return wrapper
        }

        return runCatching {
            Napier.d(tag = TAG) { "Initializing plugin: ${wrapper.name} v${wrapper.version}" }
            wrapper.plugin.init(context)
            initializationOrder.add(wrapper.pluginId)
            Napier.i(tag = TAG) { "Plugin initialized: ${wrapper.name} v${wrapper.version}" }
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

    override fun disposePlugins() {
        Napier.i(tag = TAG) { "Starting plugin disposal" }

        // Dispose in reverse order of initialization
        val reverseOrder = initializationOrder.reversed()

        reverseOrder.forEach { pluginId ->
            val index = plugins.indexOfFirst { it.pluginId == pluginId }
            if (index >= 0) {
                val wrapper = plugins[index]
                val updated = disposeSinglePlugin(wrapper)
                plugins[index] = updated
            }
        }

        initializationOrder.clear()
        Napier.i(tag = TAG) { "Plugin disposal complete" }
    }

    private fun disposeSinglePlugin(wrapper: PluginWrapper): PluginWrapper {
        if (wrapper.state != PluginState.INITIALIZED) {
            Napier.d(tag = TAG) { "Skipping non-initialized plugin: ${wrapper.name}" }
            return wrapper
        }

        return runCatching {
            Napier.d(tag = TAG) { "Disposing plugin: ${wrapper.name}" }
            wrapper.plugin.dispose()
            Napier.i(tag = TAG) { "Plugin disposed: ${wrapper.name}" }
            wrapper.copy(state = PluginState.DISPOSED)
        }.getOrElse { error ->
            Napier.e(tag = TAG, throwable = error) {
                "Plugin disposal failed: ${wrapper.name} - ${error.message}"
            }
            // Mark as DISPOSED anyway to prevent retry
            wrapper.copy(
                state = PluginState.DISPOSED,
                failureReason = "Dispose failed: ${error.message}"
            )
        }
    }

    override fun getPlugins(): List<PluginWrapper> = plugins.toList()

    override fun getPlugin(id: String): PluginWrapper? = plugins.find { it.pluginId == id }

    override fun getPluginStateCounts(): Map<PluginState, Int> =
        plugins.groupingBy { it.state }.eachCount()

    companion object {
        private const val TAG = "PluginManager"
    }
}
```

### Key Implementation Details

**1. Reverse Disposal Order (AC #4):**
- Track initialization order in `initializationOrder` list
- On dispose, iterate in reverse: `initializationOrder.reversed()`
- This ensures dependent plugins are disposed before their dependencies

**2. State Management (AC #5):**
- Always use `wrapper.copy()` to update state (immutability from 1.3/1.4)
- States: DISCOVERED → INITIALIZED → DISPOSED
- Or: DISCOVERED → FAILED (on init error)
- Or: INITIALIZED → DISPOSED (with failureReason on dispose error)

**3. Error Handling:**
- Use `runCatching` for all plugin calls (consistent with 1.3/1.4)
- Never throw exceptions - always return updated wrapper
- Log all errors via Napier with TAG
- Store failure reason in wrapper for debugging

**4. Skip Already Failed Plugins:**
- Plugins marked FAILED during compatibility check (1.4) should be skipped
- Don't attempt to initialize plugins that already failed discovery

### Previous Story Intelligence

**From Story 1.3 (PluginLoader):**
- DefaultPluginLoader uses `@Volatile discoveredPlugins` cache
- `shutdown()` method exists for PF4J cleanup
- Logging uses Napier with TAG constant
- Tests use MockK for mocking Plugin interface

**From Story 1.4 (Version Compatibility):**
- PluginWrapper.failureReason field exists for storing error info
- PluginWrapper is immutable - always use copy()
- Compatible plugins have state DISCOVERED
- Incompatible plugins have state FAILED with reason

**Code Patterns Established:**
- Interface in commonMain, implementation in jvmMain
- Use `runCatching` + `getOrElse` for error handling
- KDoc documentation on all public APIs
- Unit tests with MockK in jvmTest

### Git Intelligence

**Recent Commits:**
- `759b132` - Story 1.4 (version compatibility check) - done
- `e8f93ca` - Story 1.3 (PluginLoader with JAR discovery) - review
- `6a99542` - Story 1.2 (PluginContext API)
- `02be9e7` - Story 1.1 (Plugin API module structure)

**Files Modified in 1.3/1.4:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginLoader.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginState.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginWrapper.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginLoader.kt`

### Project Structure Notes

**New Files to Create:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginManager.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginManager.kt`
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/PluginManagerTest.kt`

**Files to NOT Modify:**
- Plugin.kt - already has init/dispose (Story 1.1)
- PluginWrapper.kt - already has state/failureReason (Story 1.3/1.4)
- PluginState.kt - already has all required states (Story 1.3)
- PluginContext.kt - already complete (Story 1.2/1.4)

### References

- [Source: _bmad-output/architecture.md#Module Structure Decision]
- [Source: _bmad-output/architecture.md#Plugin Loading Framework: PF4J]
- [Source: _bmad-output/prd.md#FR12 - Plugin interface with init/dispose]
- [Source: _bmad-output/prd.md#FR19 - Graceful plugin failure handling]
- [Source: _bmad-output/project-context.md#Plugin System Rules]

### Claude Skills to Use

- **No special skills needed** - Standard Kotlin implementation
- May use `kotlin-specialist` agent for coroutine patterns if needed

### Anti-Patterns to Avoid

- ❌ Do NOT modify PluginWrapper directly - use copy()
- ❌ Do NOT throw exceptions from initialize/dispose - catch and log
- ❌ Do NOT dispose plugins in forward order - must be reverse
- ❌ Do NOT initialize already-failed plugins
- ❌ Do NOT use static/global mutable state
- ❌ Do NOT skip logging for lifecycle events
- ❌ Do NOT forget to track initialization order

### Success Criteria

1. `./gradlew :kimai-plugin-api:jvmTest` passes all tests
2. `./gradlew :kimai-desktop:compileKotlinJvm` still works (no regression)
3. Plugins transition: DISCOVERED → INITIALIZED on success
4. Plugins transition: DISCOVERED → FAILED on init error
5. Plugins transition: INITIALIZED → DISPOSED on shutdown
6. Disposal occurs in reverse initialization order
7. Failed plugins (from 1.4) are skipped during initialization
8. All lifecycle events are logged via Napier

### Important Notes for Dev Agent

1. **REUSE EXISTING CODE:** PluginState, PluginWrapper, and failureReason already exist - do NOT recreate
2. **Immutability:** Always use wrapper.copy() to update state
3. **Order Tracking:** Maintain list of plugin IDs in initialization order for reverse disposal
4. **Error Resilience:** Never crash on plugin errors - log and continue
5. **Skip Failed:** Plugins already FAILED from version check should not be initialized
6. **Thread Safety:** Add KDoc noting class is not thread-safe
7. **Build Verification:** Run both plugin-api and desktop builds after changes

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- All tests pass: `./gradlew :kimai-plugin-api:jvmTest` - BUILD SUCCESSFUL
- Regression test pass: `./gradlew :kimai-desktop:compileKotlinJvm` - BUILD SUCCESSFUL

### Completion Notes List

- Implemented PluginManager interface with full KDoc documentation
- Implemented DefaultPluginManager with:
  - initializePlugins() - calls plugin.init() for DISCOVERED plugins
  - disposePlugins() - disposes in REVERSE order of initialization
  - getPlugins() and getPlugin() for state access
  - getPluginStateCounts() for statistics
- Error handling with runCatching - never throws, logs and continues
- State transitions: DISCOVERED → INITIALIZED → DISPOSED (or FAILED on error)
- Skips already-failed plugins (from version compatibility check in 1.4)
- Comprehensive logging with Napier TAG "PluginManager"
- 19 unit tests covering all acceptance criteria with MockK

### Change Log

| Date | Change |
|------|--------|
| 2025-12-27 | Story created by create-story workflow - ready for development |
| 2025-12-27 | Implemented PluginManager interface and DefaultPluginManager |
| 2025-12-27 | Added comprehensive unit tests - all pass |
| 2025-12-27 | Story completed - ready for review |
| 2025-12-27 | Code review: Fixed 8 issues (1 HIGH, 4 MEDIUM, 3 LOW) |

### File List

**New Files:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginManager.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginManager.kt`
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/PluginManagerTest.kt`
