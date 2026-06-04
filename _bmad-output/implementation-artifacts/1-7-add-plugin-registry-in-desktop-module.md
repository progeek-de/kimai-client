# Story 1.7: Add Plugin Registry in Desktop Module

Status: done

## Story

As a **developer**,
I want **a central PluginRegistry for build-time plugin registration (MVP)**,
So that **plugins can be included at compile time without dynamic loading**.

## Acceptance Criteria

1. **Given** kimai-desktop module
   **When** I create PluginRegistry.kt
   **Then** it contains a list of Plugin instances for build-time inclusion

2. **And** it integrates with PluginManager for lifecycle management

3. **And** future dynamic plugins can be added alongside static plugins

4. **And** example: `val plugins: List<Plugin> = listOf(TasksPlugin())`

## Tasks / Subtasks

- [x] Task 1: Create PluginRegistry singleton (AC: #1, #4)
  - [x] Create `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginRegistry.kt`
  - [x] Define `object PluginRegistry` with `val plugins: List<Plugin>` property
  - [x] Start with empty list (no plugins registered yet for MVP)
  - [x] Add KDoc documentation explaining purpose and usage
  - [x] Add `fun discoverPlugins(): List<Plugin>` for future dynamic loading hook

- [x] Task 2: Create PluginContextImpl in desktop module (AC: #2)
  - [x] Create `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginContextImpl.kt`
  - [x] Implement `PluginContext` interface from kimai-plugin-api
  - [x] Expose read-only access to Core stores via Koin injection
  - [x] Provide `mainContext` and `ioContext` from `kimaiDispatchers`
  - [x] Implement `getDatabasePath(name: String)` returning `~/.kimai-client/data/plugins/{name}.db`
  - [x] Implement `getLicenseKey(pluginId: String)` returning null for MVP (license system not yet implemented)

- [x] Task 3: Create PluginInitializer to bootstrap plugin system (AC: #2)
  - [x] Create `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginInitializer.kt`
  - [x] Create function `initializePlugins(pluginManager: PluginManager, context: PluginContext)`
  - [x] Register build-time plugins from PluginRegistry
  - [x] Log plugin initialization results with Napier
  - [x] Handle and log any initialization failures without crashing app

- [x] Task 4: Integrate PluginManager with Main.kt (AC: #2, #3)
  - [x] Add PluginManager to Koin module (kimai-desktop or shared)
  - [x] Add DefaultPluginLoader and DefaultPluginManager to DI
  - [x] Create PluginContextImpl after Koin init, before RootComponent creation
  - [x] Call `pluginManager.initializePlugins(context)` in Main.kt
  - [x] Call `pluginManager.disposePlugins()` before `exitApplication()`
  - [x] Log plugin counts: initialized, failed, total

- [x] Task 5: Add Build-Time + Dynamic Plugin Discovery Hook (AC: #3)
  - [x] Enhance PluginRegistry with `getAllPlugins(): List<Plugin>` method
  - [x] Combine build-time plugins with dynamically loaded plugins (future)
  - [x] Add `pluginsDirectory` constant for future JAR loading: `~/.kimai-client/plugins/`
  - [x] Add TODO comment for future dynamic loading implementation

- [x] Task 6: Write Unit Tests (AC: all)
  - [x] Test PluginRegistry returns empty list initially
  - [x] Test PluginContextImpl provides correct database paths
  - [x] Test PluginContextImpl provides coroutine contexts
  - [x] Test PluginInitializer logs plugin counts
  - [x] Test integration: registry -> manager -> initialization
  - [x] Run `./gradlew :kimai-desktop:jvmTest`
  - [x] Regression: `./gradlew :kimai-desktop:compileKotlinJvm` compiles without errors

## Dev Notes

### Architecture Context

This story is the **FINAL story of Epic 1: Plugin API Foundation**. It completes the plugin loading pipeline:

```
JAR Discovery (1.3) → Version Check (1.4) → Lifecycle (1.5) → Fault Tolerance (1.6) → Registry (1.7, THIS)
```

**Purpose:** Connect all the plugin infrastructure built in Stories 1.1-1.6 to the actual desktop application.

**MVP Strategy:** Build-time plugin inclusion (no dynamic JAR loading yet). This simplifies:
- No ClassLoader complexity
- No security sandboxing needed yet
- No plugin JAR distribution mechanism needed
- Plugins compile with the app

**Post-MVP:** Add dynamic plugin loading via PF4J (separate story in future epic).

**Reference:** [Source: _bmad-output/architecture.md#Distribution Architecture - MVP: Build-Time Integration]
**Reference:** [Source: _bmad-output/epics.md#Story 1.7: Add Plugin Registry in Desktop Module]

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Koin | 4.1.1 | Dependency Injection |
| MVIKotlin | 4.3.0 | State Management (Store access) |
| Napier | 2.7.1 | Logging |
| Coroutines | 1.10.2 | Async contexts |

### Existing Components to Use

**PluginManager (from 1.5/1.6):**
```kotlin
interface PluginManager {
    fun initializePlugins(context: PluginContext): List<PluginWrapper>
    fun disposePlugins()
    fun getPlugins(): List<PluginWrapper>
    fun getPlugin(id: String): PluginWrapper?
    fun getPluginStateCounts(): Map<PluginState, Int>
    // Fault tolerance methods from 1.6...
}
```

**PluginContext Interface (from 1.2):**
```kotlin
interface PluginContext {
    val timesheetListStore: Any
    val projectStore: Any
    val activityStore: Any
    val customerStore: Any
    val mainContext: CoroutineContext
    val ioContext: CoroutineContext
    fun getDatabasePath(name: String): String
    fun getLicenseKey(pluginId: String): String?
}
```

**PluginLoader (from 1.3):**
```kotlin
interface PluginLoader {
    fun discoverPlugins(): List<PluginWrapper>
}
```

**kimaiDispatchers (existing in kimai-shared):**
```kotlin
val kimaiDispatchers = KimaiDispatchers(
    main = Dispatchers.Main,
    io = Dispatchers.IO,
    default = Dispatchers.Default
)
```

### Implementation Design

**PluginRegistry.kt:**
```kotlin
package de.progeek.kimai.desktop

import de.progeek.kimai.plugin.api.Plugin

/**
 * Central registry for build-time plugin registration.
 *
 * For MVP, plugins are compiled into the application and registered here.
 * Post-MVP will add dynamic plugin loading from JAR files.
 *
 * Usage:
 * ```kotlin
 * // Register plugins at compile time
 * object PluginRegistry {
 *     val plugins: List<Plugin> = listOf(
 *         TasksPlugin(),  // When available
 *     )
 * }
 * ```
 *
 * @see Plugin
 */
object PluginRegistry {
    /**
     * Plugins directory for future dynamic loading.
     * Path: ~/.kimai-client/plugins/
     */
    val pluginsDirectory: String = System.getProperty("user.home") +
        "/.kimai-client/plugins"

    /**
     * Build-time registered plugins.
     * Currently empty - add plugins here when implemented.
     */
    private val buildTimePlugins: List<Plugin> = listOf(
        // TasksPlugin(),  // Uncomment when Epic 5 implemented
    )

    /**
     * Get all registered plugins.
     * Combines build-time plugins with dynamically loaded plugins.
     *
     * @return List of all available plugins
     */
    fun getAllPlugins(): List<Plugin> {
        // TODO: Add dynamic plugin loading here post-MVP
        // val dynamicPlugins = loadPluginsFromDirectory(pluginsDirectory)
        // return buildTimePlugins + dynamicPlugins
        return buildTimePlugins
    }
}
```

**PluginContextImpl.kt:**
```kotlin
package de.progeek.kimai.desktop

import de.progeek.kimai.plugin.api.PluginContext
import de.progeek.kimai.shared.KimaiDispatchers
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.coroutines.CoroutineContext

/**
 * Desktop implementation of [PluginContext].
 *
 * Provides plugins with:
 * - Read-only access to Core stores (typed as Any to avoid circular deps)
 * - Coroutine contexts for async operations
 * - Database path generation for plugin-specific storage
 * - License key retrieval (MVP: always returns null)
 */
class PluginContextImpl(
    private val dispatchers: KimaiDispatchers,
    // Stores passed as Any to avoid direct dependency on store types
    override val timesheetListStore: Any,
    override val projectStore: Any,
    override val activityStore: Any,
    override val customerStore: Any
) : PluginContext {

    override val mainContext: CoroutineContext
        get() = dispatchers.main

    override val ioContext: CoroutineContext
        get() = dispatchers.io

    /**
     * Get database path for plugin-specific storage.
     *
     * Path format: ~/.kimai-client/data/plugins/{name}.db
     * Directory is created if it doesn't exist.
     *
     * @param name Database name (without extension)
     * @return Absolute path to the database file
     */
    override fun getDatabasePath(name: String): String {
        val baseDir = System.getProperty("user.home") +
            "/.kimai-client/data/plugins"
        val path = Paths.get(baseDir)
        if (!Files.exists(path)) {
            Files.createDirectories(path)
        }
        return "$baseDir/$name.db"
    }

    /**
     * Get license key for a plugin.
     *
     * MVP: Returns null (license system not yet implemented).
     * Will be implemented in Epic 3: License Validation System.
     *
     * @param pluginId Plugin identifier
     * @return License key, or null if not available
     */
    override fun getLicenseKey(pluginId: String): String? {
        // TODO: Implement license retrieval in Epic 3
        return null
    }
}
```

**Main.kt Integration:**
```kotlin
// In main() function, after initKoin():

// Initialize plugin system
val pluginManager = getKoin().get<PluginManager>()
val pluginContext = PluginContextImpl(
    dispatchers = kimaiDispatchers,
    timesheetListStore = /* from Koin */,
    projectStore = /* from Koin */,
    activityStore = /* from Koin */,
    customerStore = /* from Koin */
)

// Initialize plugins
val initializedPlugins = pluginManager.initializePlugins(pluginContext)
Napier.i(tag = "Main") {
    "Plugins initialized: ${pluginManager.getPluginStateCounts()}"
}

// ... application code ...

// In shouldExit():
pluginManager.disposePlugins()
singleInstanceManager?.close()
exitApplication()
```

### Previous Story Intelligence

**From Story 1.6 (Fault Tolerance):**
- PluginManager has `setNotificationListener()` for failure notifications
- `getFailedPlugins()` and `getPluginFailure()` for debugging
- Plugins can fail gracefully without crashing app

**From Story 1.5 (Lifecycle Management):**
- `initializePlugins()` handles DISCOVERED -> INITIALIZED transition
- `disposePlugins()` called in reverse order
- Plugin states tracked: DISCOVERED, INITIALIZED, DISPOSED, FAILED

**From Story 1.3 (Plugin Loader):**
- `DefaultPluginLoader` discovers JAR plugins (future use)
- For MVP, we bypass JAR discovery and use build-time registration

**Code Patterns Established:**
- Use `runCatching` + `getOrElse` for error handling
- Log with Napier using tag constants
- KDoc documentation on public APIs

### Git Intelligence

**Recent Commits (Epic 1):**
- `b79d5d2` - Story 1.6 (fault tolerance) - review
- `32686e6` - Story 1.5 (lifecycle management) - done
- `759b132` - Story 1.4 (version compatibility) - done
- `e8f93ca` - Story 1.3 (PluginLoader) - review
- `6a99542` - Story 1.2 (PluginContext)
- `02be9e7` - Story 1.1 (Plugin API module)

**Files from Previous Stories:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/Plugin.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginContext.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginManager.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginManager.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginLoader.kt`

### Project Structure Notes

**Files to Create:**
- `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginRegistry.kt`
- `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginContextImpl.kt`
- `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginInitializer.kt` (optional, can be inline in Main.kt)
- `kimai-desktop/src/jvmTest/kotlin/de/progeek/kimai/desktop/PluginRegistryTest.kt`
- `kimai-desktop/src/jvmTest/kotlin/de/progeek/kimai/desktop/PluginContextImplTest.kt`

**Files to Modify:**
- `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/Main.kt` - Add plugin initialization
- `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/core/di/CommonModule.kt` - Add PluginManager to Koin (or new PluginModule.kt)

**Dependencies:**
- kimai-desktop already depends on kimai-plugin-api
- May need to add dependency on kimai-shared stores for PluginContext

### Alignment with Architecture

**From architecture.md - MVP Distribution:**
```
// settings.gradle.kts
include(":kimai-desktop")
include(":kimai-shared")
include(":kimai-swagger-client")
include(":kimai-plugin-api")
include(":kimai-plugin-tasks")  // Plugin included at build time (future)
```

**Plugin Registration Pattern:**
```kotlin
// PluginRegistry.kt in kimai-desktop
object PluginRegistry {
    val plugins: List<Plugin> = listOf(
        TasksPlugin(),  // Registered at compile time
    )
}
```

### References

- [Source: _bmad-output/architecture.md#Distribution Architecture - MVP: Build-Time Integration]
- [Source: _bmad-output/architecture.md#Plugin-Core Communication]
- [Source: _bmad-output/epics.md#Story 1.7: Add Plugin Registry in Desktop Module]
- [Source: _bmad-output/project-context.md#Plugin System Rules]
- [Source: _bmad-output/implementation-artifacts/1-5-implement-plugin-lifecycle-management.md]
- [Source: _bmad-output/implementation-artifacts/1-6-implement-plugin-fault-tolerance.md]

### Claude Skills to Use

- **No special skills needed** - Standard Kotlin/Koin implementation
- May reference `/decompose-mvikotlin` for Component integration patterns if needed

### Anti-Patterns to Avoid

- Do NOT expose mutable store references to plugins
- Do NOT allow plugins to modify Core data directly
- Do NOT skip null checks for license keys (always null for MVP)
- Do NOT block main thread during plugin initialization
- Do NOT let plugin failures crash the application
- Do NOT create database directories until first access
- Do NOT hard-code paths - use System.getProperty("user.home")

### Success Criteria

1. `./gradlew :kimai-desktop:compileKotlinJvm` succeeds
2. `./gradlew :kimai-desktop:run` starts without errors
3. PluginRegistry exists and returns empty list (no plugins yet)
4. PluginContextImpl provides correct database paths
5. PluginManager is available via Koin DI
6. Plugin initialization logged at startup
7. Plugin disposal called on application exit
8. No regressions in existing functionality

### Important Notes for Dev Agent

1. **BUILD-TIME ONLY:** No JAR loading in this story - just prepare the hooks
2. **EMPTY REGISTRY:** No actual plugins to register yet (Epic 5 will add TasksPlugin)
3. **STORES AS ANY:** Pass stores typed as `Any` to avoid circular dependencies
4. **CREATE DIRECTORIES:** Ensure plugin database directories exist before returning paths
5. **NULL LICENSE:** Always return null for license keys (Epic 3 will implement)
6. **LOG EVERYTHING:** Use Napier to log plugin system initialization
7. **DISPOSE ON EXIT:** Hook disposePlugins() into application exit flow
8. **TEST ISOLATION:** Use mock stores in unit tests

### Koin Module Design

```kotlin
// PluginModule.kt (new file, or add to CommonModule.kt)
val pluginModule = module {
    single<PluginLoader> { DefaultPluginLoader() }
    single<PluginManager> { DefaultPluginManager(get()) }
}

// Usage in Main.kt
initKoin {
    modules(commonModule, pluginModule)
}
```

### Store Access Pattern

Stores need to be passed to PluginContextImpl. Options:
1. **Inject via Koin** - Get stores from Koin in Main.kt
2. **Pass from RootComponent** - Extract after RootComponent creation
3. **Lazy initialization** - Create PluginContext after app fully initialized

**Recommended:** Option 1 (Koin injection) for simplicity.

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- Build logs: `./gradlew :kimai-desktop:jvmTest` - All tests pass
- Compilation: `./gradlew :kimai-desktop:compileKotlinJvm` - SUCCESS

### Completion Notes List

- Implemented PluginRegistry singleton with build-time plugin list and discoverPlugins() hook
- Implemented PluginContextImpl providing coroutine contexts, database paths, and license key retrieval (null for MVP)
- Implemented PluginInitializer helper for bootstrapping plugin system with logging
- Created PluginModule for Koin dependency injection (PluginLoader, PluginManager)
- Integrated plugin system into Main.kt with initialization on startup and disposal on exit
- Added VERSION field to desktop BuildKonfig for app version in PluginContext
- Added kimai-plugin-api dependency to kimai-desktop module
- All unit tests pass (PluginRegistryTest, PluginContextImplTest, PluginInitializerTest, PluginSystemIntegrationTest)
- No regressions in kimai-shared tests
- Epic 1: Plugin API Foundation is now COMPLETE

### Senior Developer Review (AI)

**Review Date:** 2025-12-28
**Reviewer:** Claude Opus 4.5
**Outcome:** ✅ APPROVED (after fixes)

**Issues Found:** 0 High, 5 Medium, 2 Low

**Fixes Applied:**
- M1: Removed unused import `io.mockk.every` from PluginContextImplTest.kt
- M2: Changed PluginRegistry.pluginsDirectory to use `Paths.get()` for cross-platform compatibility
- M3: Removed redundant `Files.exists()` check before `createDirectories()` (idempotent operation)
- M4: Added input validation in `getDatabasePath()` to prevent path traversal attacks
- M5: Added test for failure logging scenario in PluginInitializerTest
- L1: Changed `discoverPlugins()` to `internal` visibility
- L2: Fixed KDoc example to reference correct `BuildKonfig.VERSION`

**New Tests Added:**
- `getDatabasePath rejects blank name`
- `getDatabasePath rejects path traversal`
- `getDatabasePath rejects forward slash`
- `getDatabasePath rejects backslash`
- `initializePlugins handles failed plugins gracefully`

**All Tests Pass:** ✅ BUILD SUCCESSFUL

### Change Log

- 2025-12-28: Code review completed - 7 issues fixed, 5 new tests added
- 2025-12-28: Story 1.7 implemented - Plugin Registry integrated into desktop module

### File List

**Created:**
- kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginRegistry.kt
- kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginContextImpl.kt
- kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginInitializer.kt
- kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginModule.kt
- kimai-desktop/src/jvmTest/kotlin/de/progeek/kimai/desktop/PluginRegistryTest.kt
- kimai-desktop/src/jvmTest/kotlin/de/progeek/kimai/desktop/PluginContextImplTest.kt
- kimai-desktop/src/jvmTest/kotlin/de/progeek/kimai/desktop/PluginInitializerTest.kt
- kimai-desktop/src/jvmTest/kotlin/de/progeek/kimai/desktop/PluginSystemIntegrationTest.kt

**Modified:**
- kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/Main.kt
- kimai-desktop/build.gradle.kts
