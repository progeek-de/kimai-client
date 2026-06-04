# Story 2.1: Define Extension Point Interfaces

Status: done

## Story

As a **plugin developer**,
I want **clear extension point interfaces to implement**,
So that **I know exactly how to extend the application**.

## Acceptance Criteria

1. **Given** the kimai-plugin-api module
   **When** I define extension interfaces
   **Then** `SettingsExtension` interface exists with `settingsItem: SettingsItem` and `@Composable SettingsContent(pluginContext: PluginContext)`

2. **And** `NavigationExtension` interface exists with `navigationItem: NavigationItem`, `createComponent()`, and `@Composable Content()`

3. **And** `TimesheetActionExtension` interface exists with `actions: List<TimesheetAction>` and `execute(action, entry, context)`

4. **And** supporting data classes `SettingsItem`, `NavigationItem`, `TimesheetAction` are defined

5. **And** all interfaces are in `de.progeek.kimai.plugin.api.extensions` package

## Tasks / Subtasks

- [x] Task 1: Create base Extension marker interface (AC: #5)
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/Extension.kt`
  - [x] Define marker interface `interface Extension` (empty, for type-safe registry queries)
  - [x] Add KDoc explaining purpose and usage pattern

- [x] Task 2: Create NavigationItem data class (AC: #4)
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/NavigationItem.kt`
  - [x] Define: `data class NavigationItem(val id: String, val title: String, val icon: ImageVector?, val order: Int = 100)`
  - [x] Add KDoc with usage example
  - [x] Added compose.material3 dependency for `ImageVector` type

- [x] Task 3: Create SettingsItem data class (AC: #4)
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/SettingsItem.kt`
  - [x] Define: `data class SettingsItem(val id: String, val title: String, val description: String?, val order: Int = 100)`
  - [x] Add KDoc with usage example

- [x] Task 4: Create TimesheetAction data class (AC: #4)
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/TimesheetAction.kt`
  - [x] Define: `data class TimesheetAction(val id: String, val label: String, val icon: ImageVector?, val enabled: Boolean = true)`
  - [x] Add KDoc explaining action lifecycle

- [x] Task 5: Create SettingsExtension interface (AC: #1)
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/SettingsExtension.kt`
  - [x] Extend `Extension` marker interface
  - [x] Define `val settingsItem: SettingsItem` property
  - [x] Define `@Composable fun SettingsContent(pluginContext: PluginContext)` method
  - [x] Add KDoc with implementation example

- [x] Task 6: Create NavigationExtension interface (AC: #2)
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/NavigationExtension.kt`
  - [x] Extend `Extension` marker interface
  - [x] Define `val navigationItem: NavigationItem` property
  - [x] Define `fun createComponent(componentContext: ComponentContext, pluginContext: PluginContext): Any`
  - [x] Define `@Composable fun Content(component: Any)` method
  - [x] Add KDoc explaining Decompose component lifecycle

- [x] Task 7: Create TimesheetActionExtension interface (AC: #3)
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/TimesheetActionExtension.kt`
  - [x] Extend `Extension` marker interface
  - [x] Define `val actions: List<TimesheetAction>` property
  - [x] Define `fun execute(action: TimesheetAction, entry: Any, context: PluginContext)` (entry as Any to avoid circular deps)
  - [x] Add KDoc explaining action execution flow

- [x] Task 8: Update build.gradle.kts dependencies (AC: #5)
  - [x] Add Compose plugins (jetbrains.compose, compose.compiler)
  - [x] Add Decompose dependency for `ComponentContext`
  - [x] Add compose.runtime for `@Composable` annotation
  - [x] Verify no circular dependencies introduced

- [x] Task 9: Write Unit Tests (AC: all)
  - [x] Create `kimai-plugin-api/src/commonTest/kotlin/de/progeek/kimai/plugin/api/extensions/` directory
  - [x] Test NavigationItem data class creation and defaults
  - [x] Test SettingsItem data class creation and defaults
  - [x] Test TimesheetAction data class creation and defaults
  - [x] Run `./gradlew :kimai-plugin-api:jvmTest` - PASSED
  - [x] Regression: `./gradlew :kimai-desktop:compileKotlinJvm` - PASSED

## Dev Notes

### Architecture Context

This is the **FIRST story of Epic 2: Plugin Extension Points**. It defines the contracts that plugins implement to extend Core UI.

**Extension Point Strategy:**
- Plugins implement ONE or MORE extension interfaces
- Core queries ExtensionRegistry (Story 2.2) to find all extensions
- Core renders plugin UI at designated injection points

**Reference:** [Source: _bmad-output/architecture.md#UI Integration Architecture]

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Compose Multiplatform | 1.9.3 | `@Composable` annotation, ImageVector |
| Decompose | 3.4.0 | `ComponentContext` for navigation |
| MVIKotlin | 4.3.0 | State management (used by plugins) |

### Target Directory Structure

```
kimai-plugin-api/
└── src/
    └── commonMain/
        └── kotlin/
            └── de/progeek/kimai/plugin/api/
                ├── Plugin.kt                    # Existing
                ├── PluginContext.kt             # Existing
                ├── PluginLoader.kt              # Existing
                ├── PluginManager.kt             # Existing
                ├── PluginState.kt               # Existing
                ├── PluginWrapper.kt             # Existing
                ├── errors/
                │   └── PluginError.kt           # Existing
                └── extensions/                  # NEW - This Story
                    ├── Extension.kt             # Marker interface
                    ├── NavigationItem.kt        # Data class
                    ├── SettingsItem.kt          # Data class
                    ├── TimesheetAction.kt       # Data class
                    ├── NavigationExtension.kt   # Interface
                    ├── SettingsExtension.kt     # Interface
                    └── TimesheetActionExtension.kt # Interface
```

### Interface Templates

**Extension Marker Interface:**
```kotlin
package de.progeek.kimai.plugin.api.extensions

/**
 * Base marker interface for all plugin extensions.
 *
 * Extensions allow plugins to inject UI and functionality into Core.
 * Plugins can implement multiple extension interfaces.
 *
 * Example:
 * ```kotlin
 * class MyPlugin : Plugin, SettingsExtension, NavigationExtension {
 *     // Plugin implements both settings and navigation
 * }
 * ```
 */
interface Extension
```

**NavigationExtension Interface:**
```kotlin
package de.progeek.kimai.plugin.api.extensions

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext

/**
 * Extension point for plugins to add navigation tabs to HomeComponent.
 *
 * Plugins implementing this interface will have their navigation items
 * displayed alongside core tabs (Timesheet, Settings).
 */
interface NavigationExtension : Extension {
    /**
     * Navigation item metadata (title, icon, order).
     */
    val navigationItem: NavigationItem

    /**
     * Create the Decompose component for this navigation destination.
     *
     * @param componentContext Decompose component context for lifecycle management
     * @param pluginContext Plugin context for Core access
     * @return The plugin's Decompose component (typed as Any to avoid dependency issues)
     */
    fun createComponent(
        componentContext: ComponentContext,
        pluginContext: PluginContext
    ): Any

    /**
     * Composable content rendered when this navigation item is selected.
     *
     * @param component The component created by [createComponent]
     */
    @Composable
    fun Content(component: Any)
}
```

**SettingsExtension Interface:**
```kotlin
package de.progeek.kimai.plugin.api.extensions

import androidx.compose.runtime.Composable

/**
 * Extension point for plugins to add settings panels to SettingsComponent.
 *
 * Plugin settings appear in a "Plugins" section, visually separated from core settings.
 */
interface SettingsExtension : Extension {
    /**
     * Settings item metadata (title, description, order).
     */
    val settingsItem: SettingsItem

    /**
     * Composable content rendered in the plugin's settings panel.
     *
     * @param pluginContext Plugin context for state access and persistence
     */
    @Composable
    fun SettingsContent(pluginContext: PluginContext)
}
```

**TimesheetActionExtension Interface:**
```kotlin
package de.progeek.kimai.plugin.api.extensions

/**
 * Extension point for plugins to add actions to timesheet entries.
 *
 * Actions appear in timesheet entry context menus or action buttons.
 */
interface TimesheetActionExtension : Extension {
    /**
     * List of actions this plugin provides for timesheet entries.
     */
    val actions: List<TimesheetAction>

    /**
     * Execute an action on a timesheet entry.
     *
     * @param action The action to execute (from [actions])
     * @param entry The timesheet entry (typed as Any to avoid circular deps)
     * @param context Plugin context for API access
     */
    fun execute(action: TimesheetAction, entry: Any, context: PluginContext)
}
```

### build.gradle.kts Updates Required

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Existing dependencies...
                api(project(":kimai-shared"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.napier)

                // NEW: For Extension interfaces
                implementation(compose.runtime)        // @Composable annotation
                implementation(compose.material3)     // ImageVector
                implementation(libs.decompose)        // ComponentContext
            }
        }
    }
}
```

### Previous Story Intelligence (Epic 1)

**From Story 1.7 (Plugin Registry):**
- `PluginContext` interface is already defined with stores and contexts
- Plugin lifecycle: `init(context)` → active → `dispose()`
- All plugins go through `PluginManager.initializePlugins(context)`
- Use `Any` type for stores to avoid circular dependencies

**Code Review Learnings:**
- Add input validation for string parameters (prevent invalid IDs)
- Use `Paths.get()` for cross-platform path handling
- Follow TDD with Given-When-Then test structure

**Files Created in Epic 1:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/Plugin.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginContext.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginLoader.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginManager.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginState.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginWrapper.kt`

### Git Intelligence

Recent commits show Epic 1 completion:
```
73199e2 1-7-add-plugin-registry-in-desktop-module
b79d5d2 1-6-implement-plugin-fault-tolerance
32686e6 1-5-implement-plugin-lifecycle-management
759b132 1-4-implement-plugin-version-compatibility-check
e8f93ca 1-3-implement-plugin-loader-with-jar-discovery
6a99542 1-2-implement-plugincontext-api-surface
02be9e7 1-1-create-plugin-api-module-structure
```

### Critical Implementation Notes

1. **@Composable Import:** Use `androidx.compose.runtime.Composable`, NOT `kotlin.Composable`

2. **ComponentContext Import:** Use `com.arkivanov.decompose.ComponentContext`

3. **ImageVector:** Use `androidx.compose.ui.graphics.vector.ImageVector` - make nullable for plugins without icons

4. **Timesheet Type:** Use `Any` for the `entry` parameter in `TimesheetActionExtension.execute()` to avoid circular dependency between plugin-api and shared modules

5. **Order Field:** Default to `100` for ordering. Lower numbers appear first. Core items use `0-99`, plugins should use `100+`.

6. **Validation:** Extension IDs should follow kebab-case pattern (validated in Story 2.2's ExtensionRegistry)

### References

- [Source: _bmad-output/architecture.md#UI Integration Architecture]
- [Source: _bmad-output/architecture.md#Extension Point Patterns]
- [Source: _bmad-output/epics.md#Story 2.1: Define Extension Point Interfaces]
- [Source: _bmad-output/project-context.md#Plugin System Rules]

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- All tests passed: `./gradlew :kimai-plugin-api:jvmTest`
- Regression check passed: `./gradlew :kimai-desktop:compileKotlinJvm`

### Completion Notes List

1. **Icon Type Decision**: Used `ImageVector?` from `compose.material3` for icon parameters in `NavigationItem` and `TimesheetAction` data classes. This provides type-safe icon handling for plugins using Material Design icons.

2. **Entry Type in TimesheetActionExtension**: Used `Any` for the `entry` parameter in `execute()` method to avoid circular dependency between kimai-plugin-api and kimai-shared modules. Plugins cast to `Timesheet` at runtime.

3. **Compose Dependencies**: Added `compose.runtime` for `@Composable` annotation, `compose.material3` for `ImageVector`, and `decompose` for `ComponentContext`.

4. **Test Coverage**: Created comprehensive unit tests for all three data classes (NavigationItem, SettingsItem, TimesheetAction) covering:
   - Creation with all parameters
   - Default values
   - Nullable field handling
   - Data class equality
   - Copy operations

### File List

**Created Files:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/Extension.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/NavigationItem.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/SettingsItem.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/TimesheetAction.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/SettingsExtension.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/NavigationExtension.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/TimesheetActionExtension.kt`
- `kimai-plugin-api/src/commonTest/kotlin/de/progeek/kimai/plugin/api/extensions/ExtensionDataClassesTest.kt`

**Modified Files:**
- `kimai-plugin-api/build.gradle.kts` - Added compose.runtime, compose.material3, and decompose dependencies

### Change Log

- 2025-12-28: Story 2.1 created with comprehensive developer context
- 2025-12-28: Implementation completed - all 9 tasks done, tests passing
- 2025-12-28: Updated icon type from `Any?` to `ImageVector?` per user request
- 2025-12-28: Code review fixes - updated documentation to reflect ImageVector usage
- 2025-12-28: Code review completed - added input validation to data classes, added validation tests

## Senior Developer Review (AI)

**Review Date:** 2025-12-28
**Reviewer:** Claude Opus 4.5
**Outcome:** ✅ APPROVED

### Issues Found & Fixed

| Severity | Issue | Resolution |
|----------|-------|------------|
| HIGH | Story docs showed `Any?` but code used `ImageVector?` | Updated Task 2, Task 4, Completion Notes |
| HIGH | Completion Notes claimed wrong icon type | Corrected to reflect `ImageVector?` usage |
| MEDIUM | No ID validation in data classes | Added `init { require(...) }` blocks |
| MEDIUM | No validation tests | Added 9 validation tests |
| LOW | Change Log missing ImageVector update | Added entry |
| LOW | File List description incomplete | Updated to include compose.material3 |

### Validation Added

- `NavigationItem`: id, title must not be blank; order must be >= 0
- `SettingsItem`: id, title must not be blank; order must be >= 0
- `TimesheetAction`: id, label must not be blank

### Test Results

- All 24 tests passing
- Validation tests cover blank id, blank title/label, negative order
