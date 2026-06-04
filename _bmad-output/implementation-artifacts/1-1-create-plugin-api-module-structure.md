# Story 1.1: Create Plugin API Module Structure

Status: done

## Story

As a **developer**,
I want **a new `kimai-plugin-api` Gradle module with proper structure**,
So that **I have a foundation to build plugin interfaces on**.

## Acceptance Criteria

1. **Given** the existing kimai-client project
   **When** I create the kimai-plugin-api module
   **Then** build.gradle.kts is configured with kotlin-multiplatform plugin

2. **And** module depends only on kimai-shared (models package)

3. **And** package structure follows `de.progeek.kimai.plugin.api`

4. **And** module is included in settings.gradle.kts

5. **And** the project builds successfully with new module

## Tasks / Subtasks

- [x] Task 1: Create module directory structure (AC: #1, #3)
  - [x] Create `kimai-plugin-api/` directory in project root
  - [x] Create `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/` directory structure
  - [x] Create placeholder `.gitkeep` or empty file to ensure directory exists

- [x] Task 2: Configure build.gradle.kts (AC: #1, #2)
  - [x] Create `kimai-plugin-api/build.gradle.kts`
  - [x] Configure kotlin-multiplatform plugin
  - [x] Add dependency on kimai-shared (api dependency, models only)
  - [x] Configure source sets for commonMain

- [x] Task 3: Update settings.gradle.kts (AC: #4)
  - [x] Add `include(":kimai-plugin-api")` to settings.gradle.kts

- [x] Task 4: Create initial API placeholder files (AC: #3)
  - [x] Create `Plugin.kt` with empty interface placeholder
  - [x] Create `PluginContext.kt` with empty interface placeholder

- [x] Task 5: Verify build (AC: #5)
  - [x] Run `./gradlew :kimai-plugin-api:build`
  - [x] Ensure no compilation errors
  - [x] Verify module appears in Gradle project structure

## Dev Notes

### Architecture Context

This is the **foundational module** for the entire plugin system. It will contain:
- `Plugin.kt` - Core plugin interface
- `PluginContext.kt` - API surface for plugins to access Core
- `PluginManager.kt` - Plugin lifecycle management (future story)
- `extensions/` - Extension point interfaces (future story)
- `models/` - Plugin-specific models (future story)
- `errors/` - Plugin error types (future story)

**Reference:** [Source: _bmad-output/architecture.md#Module Structure Decision]

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Kotlin Multiplatform | - | Cross-platform support |
| Gradle | - | Build system |

### Target Directory Structure

```
kimai-plugin-api/
├── build.gradle.kts
└── src/
    └── commonMain/
        └── kotlin/
            └── de/
                └── progeek/
                    └── kimai/
                        └── plugin/
                            └── api/
                                ├── Plugin.kt           # Empty interface for now
                                └── PluginContext.kt    # Empty interface for now
```

**Reference:** [Source: _bmad-output/architecture.md#Complete Project Directory Structure]

### build.gradle.kts Template

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Only depend on models from kimai-shared
                api(project(":kimai-shared"))
            }
        }
    }
}
```

**CRITICAL:** The module should use `api` dependency on kimai-shared so that plugins transitively get access to core models (Timesheet, Project, Activity, Customer).

### Plugin Interface (Placeholder)

```kotlin
package de.progeek.kimai.plugin.api

/**
 * Core interface for all Kimai Client plugins.
 *
 * Plugins implement this interface to integrate with the application.
 * Full implementation will be added in Story 1.2.
 */
interface Plugin {
    // Full interface will be defined in Story 1.2
}
```

### PluginContext Interface (Placeholder)

```kotlin
package de.progeek.kimai.plugin.api

/**
 * Context provided to plugins for accessing Core application services.
 *
 * This interface defines the API surface available to all plugins.
 * Full implementation will be added in Story 1.2.
 */
interface PluginContext {
    // Full interface will be defined in Story 1.2
}
```

### Project Structure Notes

- **Module Location:** Root level alongside kimai-desktop, kimai-shared, kimai-swagger-client
- **Package Prefix:** `de.progeek.kimai.plugin.api` (NOT `de.progeek.kimai.shared`)
- **Dependency Direction:** kimai-plugin-api → kimai-shared (NOT the reverse)

### References

- [Source: _bmad-output/architecture.md#Module Structure Decision]
- [Source: _bmad-output/architecture.md#Complete Project Directory Structure]
- [Source: _bmad-output/architecture.md#Dependency Flow]
- [Source: _bmad-output/project-context.md#Technology Stack & Versions]

### Claude Skills to Use

- **No special skills needed for this story** - basic Gradle/Kotlin setup

### Anti-Patterns to Avoid

- ❌ DON'T add any implementation code yet - this is just structure
- ❌ DON'T add dependencies beyond kimai-shared
- ❌ DON'T create Android/iOS source sets - desktop only for now
- ❌ DON'T add tests yet - testing structure comes later

### Success Criteria

1. `./gradlew :kimai-plugin-api:build` succeeds
2. `./gradlew :kimai-desktop:run` still works (no regression)
3. Module visible in IDE project structure
4. Package structure matches architecture specification

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- Build succeeded: `./gradlew :kimai-plugin-api:build` - BUILD SUCCESSFUL in 1m 43s
- Regression check: `./gradlew :kimai-desktop:compileKotlinJvm` - BUILD SUCCESSFUL in 4s

### Completion Notes List

- Created new `kimai-plugin-api` Gradle module at project root level
- Configured Kotlin Multiplatform with JVM target (JVM 21)
- Added `api` dependency on `:kimai-shared` for transitive model access
- Created empty `Plugin` and `PluginContext` interface placeholders
- Module successfully builds and integrates with existing project
- No regressions in desktop module compilation

### Change Log

| Date | Change |
|------|--------|
| 2025-12-27 | Story created - ready for development |
| 2025-12-27 | Implementation complete - all tasks finished, builds successful |
| 2025-12-27 | Code review passed - APPROVED, status → done |

## Senior Developer Review (AI)

**Review Date:** 2025-12-27
**Reviewer:** Claude Opus 4.5
**Outcome:** ✅ APPROVED

### Validation Summary

| Check | Result |
|-------|--------|
| AC1: kotlin-multiplatform plugin | ✅ Pass |
| AC2: depends on kimai-shared | ✅ Pass |
| AC3: package structure | ✅ Pass |
| AC4: settings.gradle.kts | ✅ Pass |
| AC5: build succeeds | ✅ Pass |
| All [x] tasks verified | ✅ Pass |
| Code quality | ✅ Pass |
| File List accuracy | ✅ Pass |

### Review Notes

- All Acceptance Criteria implemented correctly
- All tasks marked [x] verified as actually complete
- Build passes: `./gradlew :kimai-plugin-api:build` SUCCESS
- Regression check: `./gradlew :kimai-desktop:compileKotlinJvm` SUCCESS
- File List accurately reflects all story-related changes
- Code follows project conventions (newlines, formatting)

### Issues Found

**None blocking.** Minor documentation notes:
- Story template uses `kotlinMultiplatform` alias but implementation correctly uses `kotlin.multiplatform` per libs.versions.toml
- `.gitignore` changes in git are BMAD infrastructure, unrelated to this story

### File List

| File | Action |
|------|--------|
| `kimai-plugin-api/build.gradle.kts` | Created |
| `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/Plugin.kt` | Created |
| `kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/PluginContext.kt` | Created |
| `settings.gradle.kts` | Modified (added include) |
