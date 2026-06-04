# Story 1.2: Implement PluginContext API Surface

Status: done

## Story

As a **plugin developer**,
I want **a complete PluginContext interface that provides access to Core application services**,
So that **I can build plugins that interact with timesheets, projects, and activities without directly accessing Core internals**.

## Acceptance Criteria

1. **Given** the kimai-plugin-api module exists (from Story 1.1)
   **When** I implement the PluginContext interface
   **Then** it provides read-only access to Core stores (TimesheetListStore, ProjectFieldStore, ActivityFieldStore)

2. **And** PluginContext provides database factory methods for plugin-specific storage

3. **And** PluginContext provides coroutine contexts (mainContext, ioContext)

4. **And** Plugin interface is expanded with id, name, version properties and init/dispose lifecycle methods

5. **And** all interfaces compile without errors and the project builds successfully

## Tasks / Subtasks

- [x] Task 1: Implement Plugin interface (AC: #4)
  - [x] Add `val id: String` property (kebab-case identifier)
  - [x] Add `val name: String` property (display name)
  - [x] Add `val version: String` property (SemVer format)
  - [x] Add `fun init(context: PluginContext)` method
  - [x] Add `fun dispose()` method
  - [x] Add KDoc documentation

- [x] Task 2: Implement PluginContext interface (AC: #1, #2, #3)
  - [x] Add read-only Core store accessors (timesheetListStore, etc.)
  - [x] Add `fun getDatabasePath(name: String): String` method
  - [x] Add `val mainContext: CoroutineContext` property
  - [x] Add `val ioContext: CoroutineContext` property
  - [x] Add `fun getLicenseKey(pluginId: String): String?` method
  - [x] Add KDoc documentation

- [x] Task 3: Create PluginError sealed class (AC: #5)
  - [x] Create `errors/PluginError.kt` file
  - [x] Add LicenseInvalid, LicenseExpired error types
  - [x] Add ApiError, AuthenticationFailed error types
  - [x] Add ConfigurationMissing error type

- [x] Task 4: Update build.gradle.kts dependencies (AC: #5)
  - [x] Add kotlinx-coroutines-core dependency
  - [x] Add kotlinx-datetime dependency (for kotlin.time.Instant)
  - [x] Verify no circular dependencies

- [x] Task 5: Verify build (AC: #5)
  - [x] Run `./gradlew :kimai-plugin-api:build`
  - [x] Ensure no compilation errors
  - [x] Run `./gradlew :kimai-desktop:compileKotlinJvm` (regression check)

## Dev Notes

### Architecture Context

This story implements the **API surface** that plugins use to interact with the Core application. The PluginContext is the ONLY way plugins can access Core services.

**Key Design Decisions:**
- Read-only Store access (plugins observe, don't modify Core state directly)
- Separate database per plugin (data isolation)
- Coroutine contexts provided for proper threading
- License key retrieval for plugin-side validation

**Reference:** [Source: _bmad-output/architecture.md#Communication Architecture]

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Kotlin Coroutines | 1.10.2 | Async contexts |
| SQLDelight | 2.2.1 | Database driver reference |
| MVIKotlin | 4.3.0 | Store interfaces |

### Previous Story Intelligence

**From Story 1.1:**
- Module structure created at `kimai-plugin-api/`
- Empty `Plugin.kt` and `PluginContext.kt` placeholders exist
- Build configuration uses `libs.plugins.kotlin.multiplatform`
- Uses `api(project(":kimai-shared"))` dependency

**Files to modify:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/Plugin.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginContext.kt`
- `kimai-plugin-api/build.gradle.kts`

**New files to create:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/errors/PluginError.kt`

### Plugin Interface Template

```kotlin
package de.progeek.kimai.plugin.api

/**
 * Core interface for all Kimai Client plugins.
 *
 * Plugins implement this interface to integrate with the application.
 * Each plugin must have a unique ID, display name, and version.
 *
 * Example:
 * ```kotlin
 * class TasksPlugin : Plugin {
 *     override val id = "task-integration"
 *     override val name = "Task Integration"
 *     override val version = "1.0.0"
 *
 *     override fun init(context: PluginContext) {
 *         // Initialize plugin with provided context
 *     }
 *
 *     override fun dispose() {
 *         // Clean up resources
 *     }
 * }
 * ```
 */
interface Plugin {
    /**
     * Unique identifier for this plugin (kebab-case).
     * Used for database naming and configuration storage.
     * Example: "task-integration"
     */
    val id: String

    /**
     * Human-readable display name.
     * Shown in plugin settings and UI.
     * Example: "Task Integration"
     */
    val name: String

    /**
     * Plugin version following Semantic Versioning (SemVer).
     * Format: "MAJOR.MINOR.PATCH"
     * Example: "1.0.0"
     */
    val version: String

    /**
     * Initialize the plugin with the provided context.
     * Called once when the plugin is loaded.
     *
     * @param context Access to Core services and plugin-specific resources
     * @throws PluginError.LicenseInvalid if license validation fails
     */
    fun init(context: PluginContext)

    /**
     * Clean up plugin resources.
     * Called when the plugin is being unloaded or application is shutting down.
     */
    fun dispose()
}
```

### PluginContext Interface Template

```kotlin
package de.progeek.kimai.plugin.api

import kotlin.coroutines.CoroutineContext

/**
 * Context provided to plugins for accessing Core application services.
 *
 * This interface defines the complete API surface available to plugins.
 * Plugins MUST use this context for all interactions with Core.
 *
 * Key principles:
 * - Read-only access to Core stores (observe, don't modify directly)
 * - Separate database per plugin (data isolation)
 * - Proper coroutine contexts for threading
 */
interface PluginContext {
    // ==========================================
    // Core Store Access (Read-Only)
    // ==========================================

    /**
     * Access to timesheet list state.
     * Plugins can observe timesheets but cannot modify Core data directly.
     */
    val timesheetListStore: Any  // TODO: Define proper Store type reference

    /**
     * Access to project selection state.
     */
    val projectStore: Any  // TODO: Define proper Store type reference

    /**
     * Access to activity selection state.
     */
    val activityStore: Any  // TODO: Define proper Store type reference

    // ==========================================
    // Plugin Database
    // ==========================================

    /**
     * Get the database path for plugin-specific storage.
     * Each plugin gets its own SQLite database file.
     *
     * @param name Database name (without extension)
     * @return Path to plugin database file (e.g., ~/.kimai-client/data/plugins/plugin-tasks.db)
     */
    fun getDatabasePath(name: String): String

    // ==========================================
    // Coroutine Contexts
    // ==========================================

    /**
     * Main/UI coroutine context.
     * Use for UI updates and non-blocking operations.
     */
    val mainContext: CoroutineContext

    /**
     * IO coroutine context.
     * Use for database operations, network calls, file I/O.
     */
    val ioContext: CoroutineContext

    // ==========================================
    // License & Configuration
    // ==========================================

    /**
     * Retrieve license key for a plugin.
     * License validation is performed BY the plugin, not Core.
     *
     * @param pluginId Plugin identifier to retrieve license for
     * @return License key string, or null if not configured
     */
    fun getLicenseKey(pluginId: String): String?
}
```

### PluginError Sealed Class Template

```kotlin
package de.progeek.kimai.plugin.api.errors

import kotlin.time.Instant

/**
 * Error types that can occur during plugin operations.
 *
 * Use Arrow Either for error handling:
 * ```kotlin
 * suspend fun getJiraIssues(): Either<PluginError, List<JiraIssue>>
 * ```
 */
sealed class PluginError {
    /**
     * License is invalid or malformed.
     */
    data class LicenseInvalid(val reason: String) : PluginError()

    /**
     * License has expired.
     */
    data class LicenseExpired(val expiredAt: Instant) : PluginError()

    /**
     * API request failed.
     */
    data class ApiError(val code: Int, val message: String) : PluginError()

    /**
     * Authentication with external service failed.
     */
    data class AuthenticationFailed(val service: String) : PluginError()

    /**
     * Required configuration is missing.
     */
    data class ConfigurationMissing(val field: String) : PluginError()
}
```

### Project Structure Notes

**Package:** `de.progeek.kimai.plugin.api`

```
kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/
├── Plugin.kt              # Updated with full interface
├── PluginContext.kt       # Updated with full interface
└── errors/
    └── PluginError.kt     # NEW: Error types
```

### build.gradle.kts Update Template

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Core models and interfaces
                api(project(":kimai-shared"))

                // Coroutines for CoroutineContext
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}
```

### References

- [Source: _bmad-output/architecture.md#Communication Architecture]
- [Source: _bmad-output/architecture.md#Plugin Interface Pattern]
- [Source: _bmad-output/architecture.md#PluginContext API]
- [Source: _bmad-output/project-context.md#Plugin System Rules]

### Claude Skills to Use

- **No special skills needed for this story** - basic Kotlin interface definitions
- PluginContext references MVIKotlin stores but doesn't create new ones

### Anti-Patterns to Avoid

- **DON'T** expose mutable Core state to plugins
- **DON'T** add write methods to PluginContext
- **DON'T** allow direct plugin-to-plugin communication
- **DON'T** put license validation logic in Core (stays in plugins)
- **DON'T** forget KDoc documentation

### Success Criteria

1. `./gradlew :kimai-plugin-api:build` succeeds
2. `./gradlew :kimai-desktop:compileKotlinJvm` still works (no regression)
3. Plugin interface has id, name, version, init(), dispose()
4. PluginContext has store access, database path, coroutine contexts
5. PluginError sealed class exists with all error types
6. All interfaces have KDoc documentation

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

N/A

### Completion Notes List

- All interfaces implemented with comprehensive KDoc documentation
- Store accessors use `Any` type to avoid circular dependencies (proper typing in future stories)
- PluginError uses `kotlin.time.Instant` with `@OptIn(ExperimentalTime::class)` annotation
- Removed `sqlDriverFactory` from PluginContext (not needed - plugins create their own drivers using `getDatabasePath()`)
- Added `kotlinx.datetime` dependency for compatibility with existing codebase patterns

**Code Review Fixes (2025-12-27):**
- [H1] Added missing `customerStore: Any` to PluginContext (architecture compliance)
- [M1] Updated architecture.md to reflect sqlDriverFactory removal decision
- [M2] Added unit tests (PluginApiTest.kt) with 6 test cases verifying API implementability
- [M3] Updated project-context.md PluginContext section to match actual implementation

### Change Log

| Date | Change |
|------|--------|
| 2025-12-27 | Story created by create-story workflow - ready for development |
| 2025-12-27 | Implementation completed - all tasks done, builds passing |
| 2025-12-27 | Code review: 1 HIGH, 3 MEDIUM issues found and auto-fixed |

### File List

| File | Action | Description |
|------|--------|-------------|
| `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/Plugin.kt` | Modified | Full Plugin interface with id, name, version, init(), dispose() |
| `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginContext.kt` | Modified | Full PluginContext interface with store access (incl. customerStore), database path, coroutine contexts |
| `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/errors/PluginError.kt` | Created | Sealed class with LicenseInvalid, LicenseExpired, ApiError, AuthenticationFailed, ConfigurationMissing |
| `kimai-plugin-api/build.gradle.kts` | Modified | Added kotlinx-coroutines-core, kotlinx-datetime, and kotlin-test dependencies |
| `kimai-plugin-api/src/commonTest/kotlin/de/progeek/kimai/plugin/api/PluginApiTest.kt` | Created | Unit tests verifying API implementability |
| `_bmad-output/architecture.md` | Modified | Updated PluginContext documentation to match implementation |
| `_bmad-output/project-context.md` | Modified | Updated PluginContext API section to match implementation |
