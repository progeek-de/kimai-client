# Story 2.3: Integrate Settings Extension Point

Status: done

## Story

As a **user**,
I want **plugin settings to appear in the Settings screen**,
So that **I can configure plugins from one place**.

## Acceptance Criteria

1. **Given** plugins implement SettingsExtension
   **When** I open the Settings screen
   **Then** plugin settings panels appear in a "Plugins" section

2. **And** each plugin's `SettingsContent()` composable is rendered

3. **And** plugin settings are visually separated from core settings

4. **And** if no plugins have settings, the "Plugins" section is hidden

5. **And** settings changes are handled by the plugin's own logic

## Tasks / Subtasks

- [x] Task 1: Create PluginSettingsSection composable (AC: #1, #2, #3)
  - [x] Create `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/settings/components/PluginSettingsSection.kt`
  - [x] Create composable that takes `ExtensionRegistry` and `PluginContext` as parameters
  - [x] Query `registry.getAll<SettingsExtension>()` to get plugins with settings
  - [x] Render section header "Plugins" with `MaterialTheme.typography.titleMedium` (like TicketSystemSettingsSection)
  - [x] Iterate through extensions and render each plugin's settings in a Card
  - [x] Sort plugins by `settingsItem.order` for consistent ordering
  - [x] Add visual separator (`Divider`) between core settings and plugins section

- [x] Task 2: Create PluginSettingsItem composable (AC: #2, #5)
  - [x] Create private composable `PluginSettingsItem` within `PluginSettingsSection.kt`
  - [x] Display plugin's `settingsItem.title` and optional `description`
  - [x] Use `Card` with `MaterialTheme.colorScheme.surfaceVariant` for visual separation
  - [x] NOTE: Try-catch around composables not supported - fault tolerance handled at plugin initialization level (Story 1.6)

- [x] Task 3: Implement conditional visibility (AC: #4)
  - [x] In `PluginSettingsSection`, check if `extensions.isEmpty()`
  - [x] If empty, return early without rendering anything
  - [x] Ensure no "Plugins" header or empty section is shown

- [x] Task 4: Update SettingsComponent with ExtensionRegistry access (AC: #1)
  - [x] Modify `SettingsComponent` constructor to accept `ExtensionRegistry?` parameter (nullable)
  - [x] Expose `val extensionRegistry: ExtensionRegistry?` property
  - [x] Modify `SettingsComponent` constructor to accept `PluginContext?` parameter (nullable)
  - [x] Expose `val pluginContext: PluginContext?` property
  - [x] Update `HomeComponent` to pass `ExtensionRegistry` when creating `SettingsComponent`
  - [x] Update `RootComponent` to pass `ExtensionRegistry` and `PluginContext` through component hierarchy

- [x] Task 5: Update SettingsContent to include PluginSettingsSection (AC: #1, #3)
  - [x] Modify `SettingsContent.kt` to include `PluginSettingsSection`
  - [x] Place after `TicketSystemSettingsSection()` with a `Divider` separator
  - [x] Pass `component.extensionRegistry` and `component.pluginContext` to `PluginSettingsSection`
  - [x] Only render when both are non-null (backward compatibility)

- [x] Task 6: Fix circular dependency and update modules
  - [x] Removed circular dependency: `kimai-plugin-api` no longer depends on `kimai-shared`
  - [x] Added `kimai-shared` dependency on `kimai-plugin-api`
  - [x] Updated `Main.kt` in `kimai-desktop` to pass `extensionRegistry` and `pluginContext` to `RootComponent`

- [x] Task 7: Write Unit Tests (AC: all)
  - [x] Create `kimai-shared/src/jvmTest/kotlin/de/progeek/kimai/shared/ui/settings/components/PluginSettingsSectionTest.kt`
  - [x] Test: Empty registry shows nothing (AC: #4)
  - [x] Test: Single plugin settings rendered correctly (AC: #2)
  - [x] Test: Multiple plugins sorted by order (AC: #1)
  - [x] Test: Plugin receives PluginContext (AC: #5)
  - [x] Run `./gradlew :kimai-shared:jvmTest` - PASSED
  - [x] Run `./gradlew :kimai-desktop:compileKotlinJvm` - PASSED

- [x] Task 8: Fix existing tests
  - [x] Updated `SettingsFlowTest.kt` to pass null for new `extensionRegistry` and `pluginContext` params
  - [x] Updated `SettingsContentTest.kt` to pass null for new `extensionRegistry` and `pluginContext` params
  - [x] All settings tests pass

## Dev Notes

### Architecture Context

This is **Story 2.3 of Epic 2: Plugin Extension Points**. It integrates the `SettingsExtension` interface (Story 2.1) and `ExtensionRegistry` (Story 2.2) into the actual Settings UI.

**Extension Point Flow:**
1. Plugin implements `SettingsExtension` interface
2. Plugin is initialized → `ExtensionRegistry.register(plugin)` is called
3. User opens Settings → `SettingsContent` renders
4. `PluginSettingsSection` queries `registry.getAll<SettingsExtension>()`
5. Each plugin's `SettingsContent(pluginContext)` composable is rendered
6. Plugin handles its own settings logic internally

**Reference:** [Source: _bmad-output/architecture.md#UI Integration Architecture]

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Compose Multiplatform | 1.9.3 | UI Framework |
| MVIKotlin | 4.3.0 | State Management |
| Decompose | 3.4.0 | Navigation |
| Koin | 4.1.1 | Dependency Injection |

### Target Directory Structure

```
kimai-shared/
└── src/
    └── commonMain/
        └── kotlin/
            └── de/progeek/kimai/shared/ui/settings/
                ├── SettingsContent.kt           # MODIFIED - add PluginSettingsSection
                ├── SettingsComponent.kt         # MODIFIED - add extensionRegistry
                └── components/
                    ├── UserProfileSection.kt    # Existing
                    ├── BrandingSection.kt       # Existing
                    ├── DefaultProjectSection.kt # Existing
                    ├── TicketSystemSettingsSection.kt (referenced, different package)
                    └── PluginSettingsSection.kt # NEW
```

### UI Design Reference

**Current SettingsContent Layout:**
```
┌─────────────────────────────────────────┐
│ ← Back                                  │ TopAppBar
├─────────────────────────────────────────┤
│ 👤 user@email.com            EN DE FR   │ UserProfileSection
├─────────────────────────────────────────┤
│ Branding: [Kimai ▼] [Custom ▼]          │ BrandingSection
│ Default Project: [Select ▼]            │ DefaultProjectSection
├─────────────────────────────────────────┤
│ Ticket Systems                      [+] │ TicketSystemSettingsSection
│ ┌─────────────────────────────────────┐ │
│ │ J  Jira Cloud                   [✓] │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤  ← NEW: Divider
│ Plugins                                 │ ← NEW: PluginSettingsSection
│ ┌─────────────────────────────────────┐ │
│ │ Task Integration                    │ │
│ │ Configure Jira, GitHub, GitLab      │ │
│ │ ┌───────────────────────────────┐   │ │
│ │ │ Plugin's SettingsContent()    │   │ │
│ │ └───────────────────────────────┘   │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

**If No Plugins Have Settings:**
The "Plugins" section header and entire section should be hidden (AC: #4).

### Implementation Templates

**PluginSettingsSection Composable:**
```kotlin
package de.progeek.kimai.shared.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.progeek.kimai.plugin.api.PluginContext
import de.progeek.kimai.plugin.api.extensions.ExtensionRegistry
import de.progeek.kimai.plugin.api.extensions.SettingsExtension
import de.progeek.kimai.plugin.api.extensions.getAll
import io.github.aakira.napier.Napier

/**
 * Settings section that renders plugin settings panels.
 *
 * Queries ExtensionRegistry for all plugins implementing SettingsExtension
 * and renders each plugin's SettingsContent() composable.
 *
 * If no plugins have settings, this composable renders nothing.
 */
@Composable
fun PluginSettingsSection(
    extensionRegistry: ExtensionRegistry,
    pluginContext: PluginContext
) {
    val settingsExtensions = extensionRegistry.getAll<SettingsExtension>()
        .sortedBy { it.settingsItem.order }

    if (settingsExtensions.isEmpty()) {
        return // Don't show section if no plugins have settings
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header
        Text(
            "Plugins",
            style = MaterialTheme.typography.titleMedium
        )

        settingsExtensions.forEach { extension ->
            PluginSettingsItem(
                extension = extension,
                pluginContext = pluginContext
            )
        }
    }
}

@Composable
private fun PluginSettingsItem(
    extension: SettingsExtension,
    pluginContext: PluginContext
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Plugin header
            Text(
                extension.settingsItem.title,
                style = MaterialTheme.typography.titleSmall
            )
            extension.settingsItem.description?.let { desc ->
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Plugin's own settings content (AC: #5)
            // Note: Compose doesn't support try-catch around composables.
            // Plugin fault tolerance is handled at initialization level (Epic 1, Story 1.6).
            // Failed plugins won't be registered in ExtensionRegistry.
            extension.SettingsContent(pluginContext)
        }
    }
}
```

**SettingsContent.kt Update:**
```kotlin
// After TicketSystemSettingsSection()
Divider()
PluginSettingsSection(
    extensionRegistry = component.extensionRegistry,
    pluginContext = component.pluginContext
)
```

**SettingsComponent.kt Update:**
```kotlin
class SettingsComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: KimaiDispatchers,
    val extensionRegistry: ExtensionRegistry,  // NEW
    val pluginContext: PluginContext,          // NEW
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {
    // ... existing code
}
```

### Previous Story Intelligence (Stories 2.1, 2.2)

**From Story 2.1 - Extension Interfaces:**
- `SettingsExtension` interface: `settingsItem: SettingsItem` + `@Composable SettingsContent(PluginContext)`
- `SettingsItem`: `id: String`, `title: String`, `description: String?`, `order: Int = 100`
- Input validation: id and title must not be blank, order >= 0

**From Story 2.2 - Extension Registry:**
- `ExtensionRegistry.getAll<SettingsExtension>()` returns list of plugins
- Thread-safe implementation with synchronized blocks
- Returns snapshot (list copy), won't reflect future changes
- Plugins auto-registered on init, auto-unregistered on dispose

**Files Created in Previous Stories:**
- `kimai-plugin-api/.../extensions/SettingsExtension.kt`
- `kimai-plugin-api/.../extensions/SettingsItem.kt`
- `kimai-plugin-api/.../extensions/ExtensionRegistry.kt`
- `kimai-plugin-api/.../extensions/DefaultExtensionRegistry.kt`

**Code Review Learnings:**
- Add error handling for plugin composables
- Use try-catch around plugin code to prevent crashes
- Log errors with Napier for debugging
- Keep logging at DEBUG level for non-critical events

### Git Intelligence

Recent commits:
```
(pending) 2-2-implement-extension-registry
37649fb  2-1-define-extension-point-interfaces
73199e2  1-7-add-plugin-registry-in-desktop-module
b79d5d2  1-6-implement-plugin-fault-tolerance
```

**Patterns from Previous Stories:**
- Settings sections use `Column` with `Arrangement.spacedBy(8.dp)`
- Section headers use `MaterialTheme.typography.titleMedium`
- Cards use `surfaceVariant` background color
- Use `Divider()` between major sections
- Use Koin `koinInject()` for dependency injection in composables

### Critical Implementation Notes

1. **Breaking Change - SettingsComponent API:** The SettingsComponent constructor signature has changed to include two new nullable parameters: `extensionRegistry: ExtensionRegistry?` and `pluginContext: PluginContext?`. These are nullable for backward compatibility. Existing code that creates SettingsComponent directly must be updated to pass these parameters (or pass `null` if plugin support is not needed).

2. **Error Handling:** Compose doesn't support try-catch around composables. Plugin fault tolerance is handled at initialization level (Epic 1, Story 1.6). Faulty plugins fail during initialization and are not registered in ExtensionRegistry.

3. **PluginContext Access:** The `PluginContext` needs to be accessible in `SettingsComponent`. Options:
   - Pass from HomeComponent → SettingsComponent
   - Inject via Koin (if registered as singleton)

4. **ExtensionRegistry Access:** Options:
   - Via `PluginManager.getExtensionRegistry()`
   - Via Koin injection
   - Pass from HomeComponent

5. **Compose Import:** Use `androidx.compose.material3.*` for Material 3 components (consistent with existing code).

6. **Sorting:** Sort by `settingsItem.order` (lower = first). Core items use 0-99, plugins use 100+.

7. **Testing Challenge:** Compose tests require additional setup. Consider using `@Test` with mock objects rather than full UI tests.

### Dependencies

**Must be complete before this story:**
- Story 2.1: Define Extension Point Interfaces ✅
- Story 2.2: Implement Extension Registry ✅

**Blocked by this story:**
- Epic 5: Task Integration Plugin (needs settings UI)

### HomeComponent Analysis

`HomeComponent` creates child components including `SettingsComponent`. Need to trace:
1. Where `HomeComponent` is defined
2. How it creates `SettingsComponent`
3. How to inject `ExtensionRegistry` and `PluginContext`

**Expected location:** `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/home/HomeComponent.kt`

### Testing Strategy

**Unit Tests:**
```kotlin
class PluginSettingsSectionTest {
    @Test
    fun `empty registry renders nothing`() {
        val registry = DefaultExtensionRegistry()
        // Assert: PluginSettingsSection returns early
    }

    @Test
    fun `single plugin settings rendered`() {
        val registry = DefaultExtensionRegistry()
        val mockPlugin = MockSettingsPlugin()
        registry.register(mockPlugin)
        // Assert: Plugin settings item visible
    }

    @Test
    fun `multiple plugins sorted by order`() {
        val registry = DefaultExtensionRegistry()
        registry.register(PluginWithOrder(order = 200))
        registry.register(PluginWithOrder(order = 100))
        // Assert: Order 100 appears first
    }

    @Test
    fun `plugin exception shows error state`() {
        val registry = DefaultExtensionRegistry()
        registry.register(CrashingPlugin())
        // Assert: Error message shown, no crash
    }
}
```

### References

- [Source: _bmad-output/architecture.md#UI Integration Architecture]
- [Source: _bmad-output/epics.md#Story 2.3: Integrate Settings Extension Point]
- [Source: _bmad-output/project-context.md#Plugin System Rules]
- [Source: _bmad-output/implementation-artifacts/2-1-define-extension-point-interfaces.md]
- [Source: _bmad-output/implementation-artifacts/2-2-implement-extension-registry.md]

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- Circular dependency detected between kimai-plugin-api and kimai-shared - resolved by removing unnecessary dependency
- Compose does not support try-catch around composables - fault tolerance handled at initialization level (Story 1.6)

### Completion Notes List

1. **PluginSettingsSection.kt created** - Composable that queries ExtensionRegistry and renders plugin settings panels
2. **SettingsComponent updated** - Added nullable `extensionRegistry` and `pluginContext` parameters for backward compatibility
3. **SettingsContent updated** - Conditionally renders PluginSettingsSection when dependencies are available
4. **HomeComponent updated** - Passes extensionRegistry and pluginContext to SettingsComponent
5. **RootComponent updated** - Propagates plugin dependencies through component hierarchy
6. **Main.kt updated** - Passes extensionRegistry and pluginContext from plugin initialization
7. **Module dependencies fixed** - Removed circular dependency by restructuring kimai-plugin-api/kimai-shared relationship
8. **Existing tests fixed** - Updated SettingsFlowTest and SettingsContentTest to use new constructor signature
9. **Unit tests created** - PluginSettingsSectionTest with 12 tests covering all acceptance criteria
10. **Error handling note** - Compose doesn't support try-catch around composables; plugin fault tolerance handled at initialization level (Story 1.6). AC #5 implemented via architecture, not runtime error boundaries.
11. **Code review fixes applied** - Added explicit AC #2 test, plugin sorting test, integration tests (SettingsContentTest), documented breaking change

### File List

**Created:**
- `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/settings/components/PluginSettingsSection.kt`
- `kimai-shared/src/jvmTest/kotlin/de/progeek/kimai/shared/ui/settings/components/PluginSettingsSectionTest.kt`

**Modified:**
- `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/settings/SettingsContent.kt`
- `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/settings/SettingsComponent.kt`
- `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/home/HomeComponent.kt`
- `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/root/RootComponent.kt`
- `kimai-desktop/src/jvmMain/kotlin/de/progeek/kimai/desktop/Main.kt`
- `kimai-plugin-api/build.gradle.kts` (removed kimai-shared dependency)
- `kimai-shared/build.gradle.kts` (added kimai-plugin-api dependency)
- `kimai-shared/src/jvmTest/kotlin/de/progeek/kimai/shared/integration/SettingsFlowTest.kt`
- `kimai-shared/src/jvmTest/kotlin/de/progeek/kimai/shared/ui/settings/SettingsContentTest.kt`

### Change Log

- 2025-12-28: Story 2.3 created with comprehensive developer context from Stories 2.1 and 2.2 learnings
- 2025-12-28: Implementation completed - all tasks done, all tests passing, ready for review
- 2025-12-29: Code review completed - fixed 3 HIGH and 4 MEDIUM issues:
  - H1: Added explicit test for AC #2 (each plugin's SettingsContent invoked)
  - H2: Updated story template code to remove try-catch (Compose limitation)
  - H3: Clarified Task 2 error handling approach (initialization-level, not runtime)
  - M1: Documented breaking change in SettingsComponent API
  - M2: Added stronger test for plugin sorting by order value
  - M3: Added integration tests for SettingsContent + PluginSettingsSection
  - All tests passing - 12 total tests in PluginSettingsSectionTest, 2 integration tests in SettingsContentTest
