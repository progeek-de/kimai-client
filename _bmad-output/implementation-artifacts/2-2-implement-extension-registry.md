# Story 2.2: Implement Extension Registry

Status: done

## Story

As a **plugin developer**,
I want **my extension implementations to be automatically discovered**,
So that **I don't need to manually register each extension**.

## Acceptance Criteria

1. **Given** a plugin implements extension interfaces (e.g., `SettingsExtension`)
   **When** PluginManager initializes the plugin
   **Then** it detects which extension interfaces the plugin implements

2. **And** registers the plugin with the `ExtensionRegistry` for each implemented interface

3. **And** `ExtensionRegistry` provides `getAll<T: Extension>(): List<T>` method with reified generics

4. **And** Core can query registered extensions: `registry.getAll<SettingsExtension>()`

5. **And** `ExtensionRegistry` is accessible via `PluginContext`

## Tasks / Subtasks

- [x] Task 1: Create ExtensionRegistry interface (AC: #3, #4)
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/ExtensionRegistry.kt`
  - [x] Define interface with `fun <T: Extension> getAll(extensionType: KClass<T>): List<T>`
  - [x] Add inline reified helper: `inline fun <reified T: Extension> getAll(): List<T>`
  - [x] Add KDoc explaining registry purpose and usage

- [x] Task 2: Implement DefaultExtensionRegistry (AC: #2, #3)
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/DefaultExtensionRegistry.kt`
  - [x] Implement thread-safe storage using synchronized collection
  - [x] Implement `register(plugin: Plugin)` to detect and store extensions by type
  - [x] Implement `unregister(pluginId: String)` for cleanup on plugin disposal
  - [x] Implement `getAll<T: Extension>()` with type filtering
  - [x] Add logging for registration/unregistration events

- [x] Task 3: Update PluginContext interface (AC: #5)
  - [x] Add `val extensionRegistry: ExtensionRegistry` property to `PluginContext`
  - [x] Update PluginContextImpl (in kimai-desktop) to include ExtensionRegistry instance

- [x] Task 4: Integrate ExtensionRegistry in DefaultPluginManager (AC: #1, #2)
  - [x] Update `DefaultPluginManager` to hold ExtensionRegistry instance
  - [x] Added `getExtensionRegistry()` method to PluginManager interface
  - [x] In `initializePlugins()`, after successful `plugin.init()`, call `registry.register(plugin)`
  - [x] In `disposeSinglePlugin()`, call `registry.unregister(pluginId)` before disposal
  - [x] Registration only happens for plugins that successfully initialized (state = INITIALIZED)

- [x] Task 5: Write Unit Tests (AC: all)
  - [x] Create `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/extensions/ExtensionRegistryTest.kt`
  - [x] Test registration of plugin implementing single extension
  - [x] Test registration of plugin implementing multiple extensions
  - [x] Test `getAll<SettingsExtension>()` returns correct plugins
  - [x] Test `getAll<NavigationExtension>()` returns empty when no plugins registered
  - [x] Test unregister removes plugin from all extension lists
  - [x] Test duplicate registration prevention
  - [x] Test type-safety of returned extensions
  - [x] Run `./gradlew :kimai-plugin-api:jvmTest` - PASSED
  - [x] Regression: `./gradlew :kimai-desktop:compileKotlinJvm` - PASSED
  - [x] Run `./gradlew :kimai-desktop:jvmTest` - PASSED

## Dev Notes

### Architecture Context

This is **Story 2.2 of Epic 2: Plugin Extension Points**. It builds on Story 2.1 which defined the extension interfaces.

**Extension Point Strategy:**
- Plugins implement ONE or MORE extension interfaces (`SettingsExtension`, `NavigationExtension`, `TimesheetActionExtension`)
- When a plugin is initialized, ExtensionRegistry scans for implemented interfaces
- Core queries ExtensionRegistry to find all plugins providing specific extensions
- Core renders plugin UI at designated injection points (Stories 2.3-2.5)

**Reference:** [Source: _bmad-output/architecture.md#UI Integration Architecture]

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Kotlin Reflect | 2.2.21 | KClass for type-safe queries |
| Coroutines | 1.10.2 | Thread-safe collections |

### Target Directory Structure

```
kimai-plugin-api/
└── src/
    └── commonMain/
        └── kotlin/
            └── de/progeek/kimai/plugin/api/
                ├── Plugin.kt                    # Existing
                ├── PluginContext.kt             # MODIFIED - add extensionRegistry
                ├── DefaultPluginContext.kt      # MODIFIED - include registry
                ├── DefaultPluginManager.kt      # MODIFIED - integrate registry
                └── extensions/
                    ├── Extension.kt             # Existing (Story 2.1)
                    ├── ExtensionRegistry.kt     # NEW - Interface
                    ├── DefaultExtensionRegistry.kt # NEW - Implementation
                    ├── NavigationExtension.kt   # Existing
                    ├── SettingsExtension.kt     # Existing
                    └── TimesheetActionExtension.kt # Existing
```

### Interface Templates

**ExtensionRegistry Interface:**
```kotlin
package de.progeek.kimai.plugin.api.extensions

import kotlin.reflect.KClass

/**
 * Registry for discovering plugin extensions.
 *
 * Core components query this registry to find plugins providing specific
 * extension types (Settings, Navigation, TimesheetActions).
 *
 * Example usage:
 * ```kotlin
 * val settingsExtensions = registry.getAll<SettingsExtension>()
 * settingsExtensions.forEach { ext ->
 *     ext.SettingsContent(pluginContext)
 * }
 * ```
 */
interface ExtensionRegistry {
    /**
     * Get all plugins implementing the specified extension type.
     *
     * @param extensionType The extension interface class to query
     * @return List of plugins implementing this extension (cast to T)
     */
    fun <T : Extension> getAll(extensionType: KClass<T>): List<T>

    /**
     * Register a plugin's extensions.
     * Called by PluginManager after successful plugin initialization.
     *
     * @param plugin The plugin to scan for extension interfaces
     */
    fun register(plugin: Plugin)

    /**
     * Unregister a plugin's extensions.
     * Called by PluginManager before plugin disposal.
     *
     * @param pluginId The ID of the plugin to unregister
     */
    fun unregister(pluginId: String)
}

/**
 * Type-safe inline helper for extension queries.
 */
inline fun <reified T : Extension> ExtensionRegistry.getAll(): List<T> =
    getAll(T::class)
```

**DefaultExtensionRegistry Implementation:**
```kotlin
package de.progeek.kimai.plugin.api.extensions

import de.progeek.kimai.plugin.api.Plugin
import io.github.aakira.napier.Napier
import kotlin.reflect.KClass

/**
 * Thread-safe implementation of ExtensionRegistry.
 */
class DefaultExtensionRegistry : ExtensionRegistry {
    // Map: ExtensionType -> List of (pluginId, extension instance)
    private val extensions = mutableMapOf<KClass<out Extension>, MutableList<ExtensionEntry>>()
    private val lock = Any()

    private data class ExtensionEntry(
        val pluginId: String,
        val extension: Extension
    )

    override fun <T : Extension> getAll(extensionType: KClass<T>): List<T> {
        synchronized(lock) {
            @Suppress("UNCHECKED_CAST")
            return extensions[extensionType]
                ?.map { it.extension as T }
                ?: emptyList()
        }
    }

    override fun register(plugin: Plugin) {
        synchronized(lock) {
            EXTENSION_TYPES.forEach { extensionType ->
                if (extensionType.isInstance(plugin)) {
                    val list = extensions.getOrPut(extensionType) { mutableListOf() }
                    list.add(ExtensionEntry(plugin.id, plugin as Extension))
                    Napier.d { "Registered ${plugin.id} as ${extensionType.simpleName}" }
                }
            }
        }
    }

    override fun unregister(pluginId: String) {
        synchronized(lock) {
            extensions.values.forEach { list ->
                list.removeAll { it.pluginId == pluginId }
            }
            Napier.d { "Unregistered all extensions for $pluginId" }
        }
    }

    companion object {
        private val EXTENSION_TYPES: List<KClass<out Extension>> = listOf(
            SettingsExtension::class,
            NavigationExtension::class,
            TimesheetActionExtension::class
        )
    }
}
```

### Previous Story Intelligence (Story 2.1)

**Files Created:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/Extension.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/NavigationExtension.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/SettingsExtension.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/TimesheetActionExtension.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/NavigationItem.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/SettingsItem.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/TimesheetAction.kt`

**Code Review Learnings from 2.1:**
- Add input validation for string parameters (prevent invalid IDs)
- Data classes have `init { require(...) }` blocks for validation
- Use `ImageVector?` for icon types (not `Any?`)
- Use `Any` for entry parameter to avoid circular dependencies

**Existing Files to Modify:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginContext.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginContext.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginManager.kt`

### Git Intelligence

Recent commits:
```
37649fb  2-1-define-extension-point-interfaces
73199e2 1-7-add-plugin-registry-in-desktop-module
b79d5d2 1-6-implement-plugin-fault-tolerance
32686e6 1-5-implement-plugin-lifecycle-management
759b132 1-4-implement-plugin-version-compatibility-check
```

**Patterns from Epic 1:**
- DefaultPluginManager uses PluginState enum (LOADED, ACTIVE, FAILED, DISPOSED)
- Plugin initialization: `plugin.init(context)` wrapped in try-catch
- Logging via Napier
- Thread-safety via synchronized blocks

### Critical Implementation Notes

1. **KClass Import:** Use `kotlin.reflect.KClass`, NOT Java's `Class<T>`

2. **Reified Generics:** The inline helper function must be an extension function to use reified

3. **Thread Safety:** Use synchronized blocks for mutable collections in DefaultExtensionRegistry

4. **Registration Timing:** Only register plugins that successfully initialized (state = ACTIVE)

5. **Unregistration:** Must unregister before disposing plugin to prevent stale references

6. **Extension Type Check:** Use `KClass.isInstance(plugin)` to check if plugin implements interface

7. **Plugin Interface:** Plugin already exists in `de.progeek.kimai.plugin.api.Plugin` - import correctly

### Testing Strategy

**Mock Plugin for Tests:**
```kotlin
class MockSettingsPlugin : Plugin, SettingsExtension {
    override val id = "mock-settings"
    override val name = "Mock Settings Plugin"
    override val version = "1.0.0"

    override fun init(context: PluginContext) {}
    override fun dispose() {}

    override val settingsItem = SettingsItem(id = "mock", title = "Mock")

    @Composable
    override fun SettingsContent(pluginContext: PluginContext) {}
}

class MockMultiPlugin : Plugin, SettingsExtension, NavigationExtension {
    // Implements both interfaces
}
```

### References

- [Source: _bmad-output/architecture.md#UI Integration Architecture]
- [Source: _bmad-output/architecture.md#Communication Architecture]
- [Source: _bmad-output/epics.md#Story 2.2: Implement Extension Registry]
- [Source: _bmad-output/project-context.md#Plugin System Rules]
- [Source: _bmad-output/implementation-artifacts/2-1-define-extension-point-interfaces.md]

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- All tests passed: `./gradlew :kimai-plugin-api:jvmTest :kimai-desktop:jvmTest`
- Regression check passed: `./gradlew :kimai-desktop:compileKotlinJvm`

### Completion Notes List

1. **Test Location:** Tests are in jvmTest (not commonTest) because the mock classes need to implement @Composable methods which require Compose runtime.

2. **No DefaultPluginContext:** The story mentioned `DefaultPluginContext` but this doesn't exist. The desktop module has `PluginContextImpl` which implements `PluginContext`. Updated it to include the extensionRegistry parameter.

3. **ExtensionRegistry Access Pattern:** The `ExtensionRegistry` is created inside `DefaultPluginManager` and accessed via `getExtensionRegistry()`. The `PluginContextImpl` receives it as a constructor parameter when created in Main.kt.

4. **Duplicate Registration Prevention:** Added check in `register()` to prevent registering the same plugin twice, which could happen if `register()` is called multiple times.

5. **Registration Logging:** Added both individual extension registration logs and summary log showing how many extensions were registered for each plugin.

### File List

**Created Files:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/ExtensionRegistry.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/DefaultExtensionRegistry.kt`
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/extensions/ExtensionRegistryTest.kt`

**Modified Files:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginContext.kt` - Added extensionRegistry property
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginManager.kt` - Added getExtensionRegistry() method
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginManager.kt` - Integrated registry registration/unregistration
- `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/PluginContextImpl.kt` - Added extensionRegistry parameter
- `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/Main.kt` - Pass extensionRegistry to PluginContextImpl
- `kimai-plugin-api/src/commonTest/kotlin/de/progeek/kimai/plugin/api/PluginApiTest.kt` - Updated TestPluginContext
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/PluginManagerTest.kt` - Updated test context
- `kimai-desktop/src/jvmTest/kotlin/de/progeek/kimai/desktop/PluginContextImplTest.kt` - Updated all test cases
- `kimai-desktop/src/jvmTest/kotlin/de/progeek/kimai/desktop/PluginSystemIntegrationTest.kt` - Updated test cases

### Change Log

- 2025-12-28: Story 2.2 created with comprehensive developer context from Story 2.1 learnings
- 2025-12-28: Implementation completed - all 5 tasks done, tests passing
- 2025-12-28: Code review completed - 6 issues found and fixed:
  - MEDIUM: Added input validation for blank pluginId in unregister()
  - MEDIUM: Fixed memory leak - empty lists now removed after unregistration
  - MEDIUM: Added 3 concurrency tests (registration, query, unregistration)
  - LOW: Added KDoc documenting that getAll() returns a snapshot
  - LOW: Reduced logging from INFO to DEBUG for registration events
  - LOW: Added 2 input validation tests for blank pluginId
