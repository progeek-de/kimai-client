# Story 1.4: Implement Plugin Version Compatibility Check

Status: done

## Story

As a **user**,
I want **incompatible plugins to be rejected before loading**,
So that **the app doesn't crash from plugin version mismatches**.

## Acceptance Criteria

1. **Given** a plugin is discovered
   **When** PluginLoader checks compatibility
   **Then** it verifies the plugin's `minAppVersion` against current app version

2. **And** if incompatible, the plugin is NOT loaded

3. **And** a warning is logged: "Plugin {name} v{version} requires app version {minAppVersion}+"

4. **And** compatible plugins proceed to initialization

5. **And** an optional `maxAppVersion` is also checked if present

## Tasks / Subtasks

- [x] Task 1: Add version properties to Plugin interface (AC: #1)
  - [x] Add `minAppVersion: String` property to Plugin interface in kimai-plugin-api
  - [x] Add optional `maxAppVersion: String?` property to Plugin interface
  - [x] Provide default values in Plugin interface (minAppVersion = "0.0.0", maxAppVersion = null)

- [x] Task 2: Create AppVersion utility for semantic version comparison (AC: #1, #5)
  - [x] Create `AppVersion.kt` in kimai-plugin-api for SemVer parsing
  - [x] Implement Comparable<AppVersion> for version comparisons
  - [x] Support major.minor.patch format (e.g., "1.2.3")
  - [x] Handle edge cases: prerelease versions, invalid formats

- [x] Task 3: Create VersionCompatibilityChecker (AC: #1, #4, #5)
  - [x] Create `VersionCompatibilityChecker.kt` interface in kimai-plugin-api
  - [x] Create `DefaultVersionCompatibilityChecker.kt` implementation
  - [x] Implement minAppVersion check: plugin.minAppVersion <= currentAppVersion
  - [x] Implement maxAppVersion check: plugin.maxAppVersion >= currentAppVersion (if set)
  - [x] Return detailed CompatibilityResult (compatible, reason, suggestion)

- [x] Task 4: Integrate compatibility check into DefaultPluginLoader (AC: #1, #2, #4)
  - [x] Call VersionCompatibilityChecker after plugin discovery
  - [x] Filter out incompatible plugins from returned list
  - [x] Update PluginWrapper state to FAILED for incompatible plugins
  - [x] Store incompatibility reason in PluginWrapper or separate property

- [x] Task 5: Implement logging for compatibility results (AC: #3)
  - [x] Log warning for incompatible plugins with clear message format
  - [x] Log info for compatible plugins proceeding to initialization
  - [x] Include plugin name, version, required app version in log messages

- [x] Task 6: Add current app version retrieval mechanism (AC: #1)
  - [x] Create mechanism to retrieve current app version (from gradle.properties or manifest)
  - [x] Inject app version into VersionCompatibilityChecker or PluginLoader
  - [x] Make app version available via PluginContext for plugins to query

- [x] Task 7: Write comprehensive unit tests (AC: all)
  - [x] Test compatible plugin (minAppVersion satisfied)
  - [x] Test incompatible plugin (minAppVersion not satisfied)
  - [x] Test maxAppVersion boundary (compatible and incompatible)
  - [x] Test edge cases: null maxAppVersion, invalid version strings
  - [x] Test logging output for different scenarios

## Dev Notes

### Architecture Context

This story adds version compatibility checking to the plugin loading pipeline. It builds on Story 1.3's PluginLoader and ensures plugins are validated before being initialized in Story 1.5.

**Plugin Loading Pipeline (Epic 1 Flow):**
```
JAR Discovery (1.3) → Version Check (1.4, THIS) → Lifecycle Init (1.5) → Fault Tolerance (1.6)
```

**Reference:** [Source: _bmad-output/architecture.md#Plugin Loading Framework: PF4J]

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| PF4J | 3.12.0 | Plugin framework (provides PluginDescriptor) |
| Napier | 2.7.1 | Logging |

### Semantic Versioning Strategy

Per SemVer 2.0.0 specification:
- Version format: `MAJOR.MINOR.PATCH` (e.g., "1.4.2")
- Comparison: Major first, then Minor, then Patch
- Pre-release: Optional suffix like `-alpha`, `-beta`, `-rc1` (compare alphabetically)

**Example Comparisons:**
```
1.0.0 < 1.0.1 < 1.1.0 < 2.0.0
1.0.0-alpha < 1.0.0-beta < 1.0.0
```

### Plugin Interface Extension

Extend the existing Plugin interface from Story 1.2:

```kotlin
// kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/Plugin.kt
interface Plugin {
    val id: String
    val name: String
    val version: String

    // NEW: Version compatibility
    val minAppVersion: String
        get() = "0.0.0"  // Default: compatible with all versions

    val maxAppVersion: String?
        get() = null  // Default: no upper limit

    fun init(context: PluginContext)
    fun dispose()
}
```

### AppVersion Data Class Design

```kotlin
// kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/AppVersion.kt
data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val prerelease: String? = null
) : Comparable<AppVersion> {

    override fun compareTo(other: AppVersion): Int {
        // Compare major, minor, patch, then prerelease
    }

    companion object {
        fun parse(version: String): AppVersion
        fun parseOrNull(version: String): AppVersion?
    }

    override fun toString(): String = buildString {
        append("$major.$minor.$patch")
        prerelease?.let { append("-$it") }
    }
}
```

### VersionCompatibilityChecker Design

```kotlin
// kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/VersionCompatibilityChecker.kt
interface VersionCompatibilityChecker {
    fun checkCompatibility(plugin: Plugin): CompatibilityResult
}

data class CompatibilityResult(
    val compatible: Boolean,
    val reason: String? = null,  // Only set if incompatible
    val suggestion: String? = null  // e.g., "Update app to 2.0.0 or higher"
)

class DefaultVersionCompatibilityChecker(
    private val currentAppVersion: AppVersion
) : VersionCompatibilityChecker {

    override fun checkCompatibility(plugin: Plugin): CompatibilityResult {
        val minVersion = AppVersion.parseOrNull(plugin.minAppVersion)
            ?: return CompatibilityResult(false, "Invalid minAppVersion format")

        if (currentAppVersion < minVersion) {
            return CompatibilityResult(
                compatible = false,
                reason = "Requires app version ${plugin.minAppVersion}+",
                suggestion = "Update app to ${plugin.minAppVersion} or higher"
            )
        }

        plugin.maxAppVersion?.let { maxStr ->
            val maxVersion = AppVersion.parseOrNull(maxStr)
                ?: return CompatibilityResult(false, "Invalid maxAppVersion format")

            if (currentAppVersion > maxVersion) {
                return CompatibilityResult(
                    compatible = false,
                    reason = "Maximum supported app version is $maxStr",
                    suggestion = "Use plugin version compatible with app ${currentAppVersion}"
                )
            }
        }

        return CompatibilityResult(compatible = true)
    }
}
```

### Integration into DefaultPluginLoader

Modify the `discoverPlugins()` method in Story 1.3's DefaultPluginLoader:

```kotlin
class DefaultPluginLoader(
    private val compatibilityChecker: VersionCompatibilityChecker
) : PluginLoader {

    override fun discoverPlugins(): List<PluginWrapper> {
        // ... existing discovery code ...

        return discovered.mapNotNull { wrapper ->
            val result = compatibilityChecker.checkCompatibility(wrapper.plugin)

            if (!result.compatible) {
                Napier.w("Plugin ${wrapper.plugin.name} v${wrapper.plugin.version} " +
                         "requires app version ${wrapper.plugin.minAppVersion}+")
                // Return wrapper with FAILED state
                wrapper.copy(
                    state = PluginState.FAILED,
                    failureReason = result.reason
                )
            } else {
                Napier.i("Plugin ${wrapper.plugin.name} v${wrapper.plugin.version} " +
                         "is compatible with current app")
                wrapper
            }
        }
    }
}
```

### PluginWrapper Extension

May need to extend PluginWrapper from Story 1.3 to include failure reason:

```kotlin
// Consider adding to PluginWrapper
data class PluginWrapper(
    val plugin: Plugin,
    val pluginId: String,
    val pluginPath: Path,
    val state: PluginState = PluginState.DISCOVERED,
    val failureReason: String? = null  // NEW: reason for FAILED state
)
```

### Current App Version Retrieval

The current app version needs to be retrieved from the build configuration:

```kotlin
// Option 1: From gradle.properties via BuildConfig
object BuildConfig {
    const val APP_VERSION = "1.0.0"  // Generated at build time
}

// Option 2: From PluginContext (add to interface)
interface PluginContext {
    // ... existing properties ...
    val appVersion: String  // NEW: current application version
}
```

For MVP, hardcode or read from a properties file. For production, generate at build time.

### Logging Format

Follow consistent Napier logging patterns from Story 1.3:

```kotlin
// Incompatible plugin warning
Napier.w(
    tag = TAG,
    message = "Plugin ${plugin.name} v${plugin.version} " +
              "requires app version ${plugin.minAppVersion}+"
)

// Compatible plugin info
Napier.i(
    tag = TAG,
    message = "Plugin ${plugin.name} v${plugin.version} compatible"
)
```

### Previous Story Intelligence

**From Story 1.3:**
- DefaultPluginLoader uses PF4J's JarPluginManager
- PluginState enum: DISCOVERED, INITIALIZED, DISPOSED, FAILED
- PluginWrapper is immutable (uses copy() for state changes)
- Logging uses Napier with TAG constant
- Discovery is idempotent with @Volatile cache
- Tests in jvmTest sourceset with MockK and JUnit

**Key Files from Story 1.3:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginLoader.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginState.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginWrapper.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginLoader.kt`

**Code Review Insights from 1.3:**
- Made PluginWrapper immutable with val state
- Added idempotency to discoverPlugins() with @Volatile cache
- Fixed logging TAG to "DefaultPluginLoader"

### Git Intelligence

**Recent Commits:**
- `e8f93ca` - Story 1.3 (PluginLoader with JAR discovery) - review status
- `6a99542` - Story 1.2 (PluginContext API)
- `02be9e7` - Story 1.1 (Plugin API module structure)

**Established Patterns:**
- Interface in commonMain, implementation in jvmMain
- Unit tests in jvmTest with MockK
- Use companion object for factory methods
- KDoc documentation on public APIs

### Project Structure Notes

**New Files to Create:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/AppVersion.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/VersionCompatibilityChecker.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/CompatibilityResult.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultVersionCompatibilityChecker.kt`
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/AppVersionTest.kt`
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/VersionCompatibilityCheckerTest.kt`

**Files to Modify:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/Plugin.kt` - Add version properties
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginWrapper.kt` - Add failureReason
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginLoader.kt` - Integrate checker

### References

- [Source: _bmad-output/architecture.md#Technology Decisions - Plugin Loading Framework: PF4J]
- [Source: _bmad-output/architecture.md#Plugin Interface Pattern]
- [Source: _bmad-output/project-context.md#Plugin System Rules]
- [Source: _bmad-output/prd.md#FR13 - Plugin compatibility validation]
- [Semantic Versioning 2.0.0](https://semver.org/)

### Claude Skills to Use

- **No special skills needed** - Standard Kotlin implementation
- May use `kotlin-specialist` agent for complex Comparable implementation if needed

### Anti-Patterns to Avoid

- Do NOT throw exceptions for incompatible plugins - return CompatibilityResult
- Do NOT use mutable state - PluginWrapper must use copy()
- Do NOT block main thread during version checks
- Do NOT parse versions without validation - use parseOrNull
- Do NOT log sensitive plugin data
- Do NOT skip logging for compatibility decisions

### Success Criteria

1. `./gradlew :kimai-plugin-api:build` succeeds
2. `./gradlew :kimai-desktop:compileKotlinJvm` still works (no regression)
3. Compatible plugins proceed to initialization (DISCOVERED state)
4. Incompatible plugins are marked FAILED with clear reason
5. Warning logs show: "Plugin {name} v{version} requires app version {minAppVersion}+"
6. AppVersion comparison handles SemVer correctly
7. Optional maxAppVersion is checked when present
8. All unit tests pass

### Important Notes for Dev Agent

1. **Extend, Don't Replace:** Add properties to existing Plugin interface, don't create new interface
2. **Immutable Wrapper:** Use copy() to update PluginWrapper state, never mutate
3. **Graceful Failures:** Never crash on version check failure - return result
4. **Log Everything:** All compatibility decisions must be logged
5. **SemVer Compliance:** Follow semantic versioning specification exactly
6. **Test Edge Cases:** Invalid versions, null maxAppVersion, prerelease versions
7. **Build Verification:** Run both plugin-api and desktop builds after changes

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- Build verified with `./gradlew :kimai-plugin-api:jvmTest` - SUCCESS (all tests passing)
- Regression check with `./gradlew :kimai-desktop:compileKotlinJvm` - SUCCESS

### Completion Notes List

- ✅ Extended Plugin interface with `minAppVersion` (default "0.0.0") and `maxAppVersion` (default null)
- ✅ Created AppVersion data class with SemVer 2.0.0 compliant parsing and comparison
- ✅ Implemented Comparable<AppVersion> for proper version ordering
- ✅ Created VersionCompatibilityChecker interface in commonMain
- ✅ Implemented DefaultVersionCompatibilityChecker in jvmMain with minAppVersion/maxAppVersion checks
- ✅ Created CompatibilityResult data class with factory methods (compatible(), incompatible())
- ✅ Extended PluginWrapper with failureReason property for FAILED state details
- ✅ Integrated compatibility checking into DefaultPluginLoader via optional constructor parameter
- ✅ Added Napier logging for compatible and incompatible plugins
- ✅ Extended PluginContext with appVersion property
- ✅ Written 40+ unit tests covering parsing, comparison, compatibility checks, and edge cases

### Change Log

| Date | Change |
|------|--------|
| 2025-12-27 | Story created by create-story workflow - ready for development |
| 2025-12-27 | Story implemented by dev-story workflow - all tasks completed |
| 2025-12-27 | Code review completed - 6 issues found and fixed |

## Senior Developer Review (AI)

**Reviewer:** Claude Opus 4.5
**Date:** 2025-12-27
**Outcome:** ✅ APPROVED (after fixes)

### Issues Found and Fixed

| # | Severity | Issue | Status |
|---|----------|-------|--------|
| 1 | HIGH | Log message for maxAppVersion incompatibility was incorrect | ✅ Fixed |
| 2 | HIGH | Missing integration tests for PluginLoader + Checker | ✅ Fixed |
| 3 | MEDIUM | SemVer prerelease comparison not fully compliant | ✅ Fixed |
| 4 | MEDIUM | Thread-safety not documented | ✅ Fixed |
| 6 | LOW | KDoc for failureReason | ✅ Already present |
| 7 | LOW | Missing debug log when no checker configured | ✅ Fixed |

### Fix Details

1. **Issue #1:** Changed hardcoded `minAppVersion` log message to use `result.reason` for accurate error reporting
2. **Issue #2:** Added 6 new integration tests for VersionCompatibilityChecker in PluginLoaderTest.kt
3. **Issue #3:** Implemented proper SemVer 2.0.0 prerelease comparison with numeric identifier support (`1.0.0-1 < 1.0.0-2 < 1.0.0-10`)
4. **Issue #4:** Added "Thread Safety" section to DefaultPluginLoader KDoc
5. **Issue #7:** Added debug log when compatibility checker is not configured

### Tests Added During Review

- `PluginLoader accepts VersionCompatibilityChecker in constructor`
- `PluginLoader works without VersionCompatibilityChecker`
- `PluginLoader with checker returns empty list for empty directory`
- `VersionCompatibilityChecker marks incompatible plugin as FAILED`
- `VersionCompatibilityChecker allows compatible plugin`
- `VersionCompatibilityChecker rejects plugin exceeding maxAppVersion`
- `compare numeric prerelease versions numerically`
- `numeric prerelease has lower precedence than alphanumeric`
- `compare dotted prerelease identifiers`
- `longer prerelease has higher precedence`

### Verification

- All 50+ unit tests passing
- Regression check: `./gradlew :kimai-desktop:compileKotlinJvm` - SUCCESS

### File List

**Created:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/AppVersion.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/CompatibilityResult.kt`
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/VersionCompatibilityChecker.kt`
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultVersionCompatibilityChecker.kt`
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/AppVersionTest.kt`
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/VersionCompatibilityCheckerTest.kt`

**Modified:**
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/Plugin.kt` - Added minAppVersion, maxAppVersion
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginWrapper.kt` - Added failureReason property
- `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginContext.kt` - Added appVersion property
- `kimai-plugin-api/src/jvmMain/kotlin/de/progeek/kimai/plugin/api/DefaultPluginLoader.kt` - Integrated compatibility checker
- `kimai-plugin-api/src/commonTest/kotlin/de/progeek/kimai/plugin/api/PluginApiTest.kt` - Updated tests
- `kimai-plugin-api/src/jvmTest/kotlin/de/progeek/kimai/plugin/api/PluginLoaderTest.kt` - Added failureReason tests

