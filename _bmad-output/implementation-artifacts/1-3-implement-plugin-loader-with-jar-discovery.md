# Story 1.3: Implement Plugin Loader with JAR Discovery

Status: review

## Story

As a **user**,
I want **the application to automatically discover and load plugins from JAR files**,
So that **I don't need to manually configure each plugin**.

## Acceptance Criteria

1. **Given** the application starts
   **When** PluginLoader scans the plugins directory (`~/.kimai-client/plugins/`)
   **Then** it discovers all JAR files in that directory

2. **And** uses ServiceLoader or PF4J to find Plugin implementations

3. **And** creates Plugin instances for each discovered plugin

4. **And** logs which plugins were discovered (plugin id, name, version)

## Tasks / Subtasks

- [x] Task 1: Add PF4J dependency to kimai-plugin-api (AC: #2)
  - [x] Add PF4J dependency to libs.versions.toml
  - [x] Add PF4J dependency to kimai-plugin-api/build.gradle.kts
  - [x] Verify build compiles successfully

- [x] Task 2: Create PluginLoader interface and implementation (AC: #1, #2, #3)
  - [x] Create `PluginLoader.kt` interface in kimai-plugin-api
  - [x] Create `DefaultPluginLoader.kt` implementation
  - [x] Implement plugins directory path resolution (`~/.kimai-client/plugins/`)
  - [x] Implement JAR file discovery using PF4J
  - [x] Implement Plugin instantiation via PF4J/ServiceLoader

- [x] Task 3: Implement Plugin discovery logging (AC: #4)
  - [x] Log discovered plugins with id, name, version using Napier
  - [x] Log plugin directory being scanned
  - [x] Log warning if plugins directory doesn't exist
  - [x] Log errors for plugins that fail to load

- [x] Task 4: Create PluginState enum for lifecycle tracking (AC: #3)
  - [x] Create `PluginState.kt` with states: DISCOVERED, INITIALIZED, DISPOSED, FAILED
  - [x] Create `PluginWrapper.kt` to hold plugin instance + state + metadata

- [x] Task 5: Update build configuration for PF4J annotation processing (AC: #2)
  - [x] Configure kapt for PF4J annotations in Kotlin (if needed)
  - [x] Ensure @Extension annotations work with Kotlin compiler

- [x] Task 6: Verify build and write tests (AC: all)
  - [x] Run `./gradlew :kimai-plugin-api:build`
  - [x] Run `./gradlew :kimai-desktop:compileKotlinJvm` (regression check)
  - [x] Write unit tests for PluginLoader

## Dev Notes

### Architecture Context

This story implements the **PluginLoader** component that discovers and loads plugins at runtime. This is a critical piece of the plugin infrastructure defined in:

**Reference:** [Source: _bmad-output/architecture.md#Module Structure Decision]

```
kimai-plugin-api/
├── Plugin.kt            # DONE in 1.1
├── PluginContext.kt     # DONE in 1.2
├── PluginLoader.kt      # THIS STORY
├── extensions/          # Future story
└── errors/              # DONE in 1.2
```

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| PF4J | 3.12.0+ | Plugin framework (JAR discovery, ClassLoader isolation) |
| Napier | 2.7.1 | Logging |
| Koin | 4.1.1 | Dependency Injection |

### PF4J Configuration

**PF4J (Plugin Framework for Java)** is the recommended plugin loading framework per architecture decision.

**Key PF4J Concepts:**
- **PluginManager**: Core class that manages plugin lifecycle
- **PluginClassLoader**: Parent-last ClassLoader per plugin (isolation)
- **@Extension**: Annotation for extension point implementations
- **META-INF/extensions.idx**: Auto-generated extension registry

**Reference:** [https://pf4j.org/doc/plugins.html](https://pf4j.org/doc/plugins.html)

**PF4J Kotlin Setup:**
Per [https://pf4j.org/doc/kotlin.html](https://pf4j.org/doc/kotlin.html), use **kapt** for annotation processing.

```kotlin
// build.gradle.kts for plugins using PF4J
plugins {
    kotlin("kapt")
}

dependencies {
    implementation("org.pf4j:pf4j:3.12.0")
    kapt("org.pf4j:pf4j:3.12.0")
}
```

### Plugin Directory Structure

```
~/.kimai-client/
├── data/
│   ├── kimai.db              # Core database
│   └── plugins/              # Plugin databases
├── plugins/                  # THIS STORY: JAR discovery location
│   ├── plugin-tasks-1.0.0.jar
│   └── plugin-reports-1.0.0.jar
└── settings.json
```

### PluginLoader Interface Design

```kotlin
package de.progeek.kimai.plugin.api

/**
 * Loads and manages plugin discovery from JAR files.
 */
interface PluginLoader {
    /**
     * Discover plugins from the plugins directory.
     * Does NOT initialize plugins - just discovers them.
     *
     * @return List of discovered plugins with their wrappers
     */
    fun discoverPlugins(): List<PluginWrapper>

    /**
     * Get the plugins directory path.
     */
    fun getPluginsDirectory(): Path
}

/**
 * Holds a plugin instance along with its metadata and state.
 */
data class PluginWrapper(
    val plugin: Plugin,
    val pluginId: String,
    val pluginPath: Path,
    var state: PluginState = PluginState.DISCOVERED
)

/**
 * Plugin lifecycle states.
 */
enum class PluginState {
    DISCOVERED,   // Found in JAR, not yet initialized
    INITIALIZED,  // init() called successfully
    DISPOSED,     // dispose() called
    FAILED        // Error during lifecycle
}
```

### DefaultPluginLoader Implementation Pattern

```kotlin
package de.progeek.kimai.plugin.api

import io.github.aakira.napier.Napier
import org.pf4j.DefaultPluginManager
import org.pf4j.JarPluginManager
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Default implementation using PF4J for JAR discovery.
 */
class DefaultPluginLoader : PluginLoader {

    private val pluginsDir: Path = resolvePluginsDirectory()
    private val pf4jManager: DefaultPluginManager

    init {
        // Ensure directory exists
        pluginsDir.toFile().mkdirs()

        // Create PF4J manager pointing to plugins directory
        pf4jManager = JarPluginManager(pluginsDir)

        Napier.i("PluginLoader initialized, scanning: $pluginsDir")
    }

    override fun discoverPlugins(): List<PluginWrapper> {
        Napier.d("Discovering plugins in $pluginsDir")

        // Load plugins (discovers JAR files)
        pf4jManager.loadPlugins()

        val discovered = pf4jManager.plugins.mapNotNull { pf4jPlugin ->
            try {
                // Get our Plugin implementation from PF4J plugin
                val extensions = pf4jManager.getExtensions(Plugin::class.java, pf4jPlugin.pluginId)
                val plugin = extensions.firstOrNull() ?: run {
                    Napier.w("Plugin ${pf4jPlugin.pluginId} has no Plugin implementation")
                    return@mapNotNull null
                }

                Napier.i("Discovered plugin: ${plugin.id} (${plugin.name}) v${plugin.version}")

                PluginWrapper(
                    plugin = plugin,
                    pluginId = plugin.id,
                    pluginPath = pf4jPlugin.pluginPath,
                    state = PluginState.DISCOVERED
                )
            } catch (e: Exception) {
                Napier.e("Failed to load plugin from ${pf4jPlugin.pluginPath}: ${e.message}", e)
                null
            }
        }

        Napier.i("Plugin discovery complete: ${discovered.size} plugins found")
        return discovered
    }

    override fun getPluginsDirectory(): Path = pluginsDir

    private fun resolvePluginsDirectory(): Path {
        val userHome = System.getProperty("user.home")
        return Paths.get(userHome, ".kimai-client", "plugins")
    }
}
```

### Alternative: Pure ServiceLoader Approach

If PF4J adds too much complexity for MVP, consider pure `ServiceLoader`:

```kotlin
// Using Java ServiceLoader directly
val plugins = ServiceLoader.load(Plugin::class.java, classLoader)
    .map { plugin ->
        Napier.i("Discovered: ${plugin.id} v${plugin.version}")
        PluginWrapper(plugin, plugin.id, path, PluginState.DISCOVERED)
    }
```

This requires `META-INF/services/de.progeek.kimai.plugin.api.Plugin` files in JARs.

### Previous Story Intelligence

**From Story 1.1:**
- Module structure at `kimai-plugin-api/` exists
- Build configuration uses `libs.plugins.kotlin.multiplatform`
- Uses `api(project(":kimai-shared"))` dependency

**From Story 1.2:**
- `Plugin` interface fully implemented with `id`, `name`, `version`, `init()`, `dispose()`
- `PluginContext` interface implemented with store access, database paths, coroutine contexts
- `PluginError` sealed class exists in `errors/` package
- Unit tests exist at `PluginApiTest.kt`

**Code Review Insights from 1.2:**
- Added `customerStore: Any` to PluginContext for architecture compliance
- Tests verify Plugin and PluginContext are implementable

### Git Intelligence

**Recent Commits:**
- `6a99542` - Story 1.2 implementation (PluginContext API)
- `02be9e7` - Story 1.1 implementation (module structure)

**Files Modified in Stories 1.1/1.2:**
- `kimai-plugin-api/build.gradle.kts`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/Plugin.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginContext.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/errors/PluginError.kt`
- `settings.gradle.kts`

### Project Structure Notes

**New Files to Create:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginLoader.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginLoader.kt` (JVM-only, PF4J)
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginWrapper.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginState.kt`

**Files to Modify:**
- `gradle/libs.versions.toml` - Add PF4J version and dependency
- `kimai-plugin-api/build.gradle.kts` - Add PF4J dependency

### Dependencies to Add

```toml
# gradle/libs.versions.toml
[versions]
pf4j = "3.12.0"

[libraries]
pf4j = { module = "org.pf4j:pf4j", version.ref = "pf4j" }
```

```kotlin
// kimai-plugin-api/build.gradle.kts
// Add to jvmMain sourceset (PF4J is JVM-only)
val jvmMain by getting {
    dependencies {
        implementation(libs.pf4j)
    }
}
```

**IMPORTANT:** PF4J is JVM-only. The PluginLoader implementation must be in `jvmMain` sourceset, while interfaces can stay in `commonMain`.

### References

- [Source: _bmad-output/architecture.md#Module Structure Decision]
- [Source: _bmad-output/architecture.md#Technology Decisions - Plugin Loading Framework: PF4J]
- [Source: _bmad-output/architecture.md#Distribution Architecture]
- [Source: _bmad-output/project-context.md#Plugin System Rules]
- [PF4J Documentation](https://pf4j.org/)
- [PF4J Kotlin Support](https://pf4j.org/doc/kotlin.html)
- [PF4J Class Loading](https://pf4j.org/doc/class-loading.html)

### Claude Skills to Use

- **No special skills needed for this story** - PF4J integration and basic Kotlin
- May use `kotlin-specialist` agent for complex coroutine patterns if needed

### Anti-Patterns to Avoid

- ❌ DON'T use blocking operations on main thread
- ❌ DON'T log sensitive plugin data (license keys)
- ❌ DON'T throw uncaught exceptions - wrap in Result/Either
- ❌ DON'T initialize plugins in loader - just discover
- ❌ DON'T use static/global mutable state
- ❌ DON'T assume plugins directory exists - create if missing

### Success Criteria

1. `./gradlew :kimai-plugin-api:build` succeeds
2. `./gradlew :kimai-desktop:compileKotlinJvm` still works (no regression)
3. PluginLoader discovers JAR files from `~/.kimai-client/plugins/`
4. Each discovered plugin is logged with id, name, version
5. Missing directory is created automatically
6. Failed plugin loads are logged as warnings (not errors that crash app)
7. Unit tests pass for PluginLoader

### Important Notes for Dev Agent

1. **JVM-Only Implementation:** PF4J is JVM-only, so implementation must go in `jvmMain` sourceset
2. **Interface in commonMain:** Keep `PluginLoader` interface in `commonMain` for multiplatform compatibility
3. **Expect/Actual Pattern:** May need expect/actual for platform-specific path resolution
4. **Directory Creation:** Always ensure plugins directory exists before scanning
5. **Error Resilience:** Never crash on plugin load failure - log and continue

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- Build verified with `./gradlew :kimai-plugin-api:build` - SUCCESS
- Regression check with `./gradlew :kimai-desktop:compileKotlinJvm` - SUCCESS
- All unit tests passing

### Completion Notes List

- ✅ Added PF4J 3.12.0 dependency to version catalog and build configuration
- ✅ Created PluginLoader interface in commonMain with discoverPlugins(), getPluginsDirectory(), and pluginsDirectoryExists()
- ✅ Created DefaultPluginLoader implementation in jvmMain using PF4J's JarPluginManager
- ✅ Implemented comprehensive Napier logging for plugin discovery lifecycle
- ✅ Created PluginState enum with DISCOVERED, INITIALIZED, DISPOSED, FAILED states
- ✅ Created PluginWrapper data class with state management and convenience properties
- ✅ Added jvmMain and jvmTest sourcesets with proper dependencies (PF4J, Napier, MockK, JUnit)
- ✅ Wrote 16 unit tests covering PluginLoader, PluginState, and PluginWrapper
- ✅ kapt not needed in core module - only required in plugin projects that use @Extension annotations

**Code Review Fixes:**
- ✅ Made PluginWrapper immutable (val state instead of var)
- ✅ Added idempotency to discoverPlugins() with @Volatile cache
- ✅ Fixed logging TAG to "DefaultPluginLoader"
- ✅ Added KDoc for companion object
- ✅ Added tests for shutdown(), idempotent discovery, DISPOSED state, and copy() pattern

### Change Log

| Date | Change |
|------|--------|
| 2025-12-27 | Story created by create-story workflow - ready for development |
| 2025-12-27 | Story implemented by dev-story workflow - all tasks completed |
| 2025-12-27 | Code review fixes applied - 7 issues resolved |

### File List

**Created:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginLoader.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginState.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginWrapper.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginLoader.kt`
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/PluginLoaderTest.kt`

**Modified:**
- `gradle/libs.versions.toml` - Added pf4j version and library
- `kimai-plugin-api/build.gradle.kts` - Added jvmMain and jvmTest sourcesets with dependencies
