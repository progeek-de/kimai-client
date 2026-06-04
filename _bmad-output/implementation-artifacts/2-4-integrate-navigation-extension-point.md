# Story 2.4: Integrate Navigation Extension Point

Status: done

## Story

As a **user**,
I want **plugin screens to appear as tabs in the home screen**,
So that **I can access plugin features naturally**.

## Acceptance Criteria

1. **Given** plugins implement NavigationExtension
   **When** I view the HomeComponent
   **Then** plugin navigation items appear alongside core tabs (Timesheet, Settings)

2. **And** clicking a plugin tab navigates to the plugin's screen

3. **And** the plugin's Decompose Component is created via `createComponent()`

4. **And** the plugin's `Content()` composable is rendered in that tab

5. **And** navigation state is preserved when switching tabs

## Tasks / Subtasks

- [x] Task 1: Update HomeComponent Configuration to support plugin tabs (AC: #1, #5)
  - [x] Add `sealed class Configuration` with `PluginTab(pluginId: String, extension: NavigationExtension)` case
  - [x] Update existing Core configurations to coexist with plugin tabs (using @Serializable, @Transient)
  - [x] Ensure Configuration is `@Serializable` compatible for state restoration

- [x] Task 2: Query ExtensionRegistry for NavigationExtensions (AC: #1)
  - [x] ExtensionRegistry and PluginContext parameters already exist in HomeComponent (nullable)
  - [x] Add `navigationExtensions` property as lazy val
  - [x] Query `registry.getAll<NavigationExtension>()` during first access
  - [x] Sort extensions by `navigationItem.order` for consistent ordering

- [x] Task 3: Implement dynamic childStack with plugin tabs (AC: #1, #2, #3, #4)
  - [x] Extend `childFactory` to handle `Configuration.PluginTab` case
  - [x] Call `extension.createComponent(componentContext, pluginContext)` for plugin tabs
  - [x] Store plugin component in childStack (type-erased as `Any`)
  - [x] Add `navigateToPlugin(pluginId)` public method for navigation

- [x] Task 4: Update HomeContent UI to render plugin screens (AC: #4)
  - [x] Add `HomeComponentLocal` composition local (nullable)
  - [x] Provide HomeComponent via CompositionLocalProvider
  - [x] Add Child.PluginTab case to Children when block
  - [x] Render plugin `Content(component)` composable for plugin screens

- [x] Task 5: Update Dropdown Menu for plugin navigation items (AC: #1, #2)
  - [x] Import `HomeComponentLocal` in TimesheetTopBar
  - [x] Access `homeComponent.navigationExtensions` in TopAppBarDropdownMenu
  - [x] Add plugin items to dropdown menu with HorizontalDivider separator
  - [x] Call `homeComponent.navigateToPlugin()` on plugin item click
  - [x] Handle null homeComponent gracefully (backward compatibility)

- [x] Task 6: Verify RootComponent passes dependencies (AC: #1)
  - [x] Confirmed RootComponent already passes `extensionRegistry` and `pluginContext` to HomeComponent
  - [x] No changes needed - backward compatibility maintained

- [x] Task 7: Handle empty state (no plugins with navigation) (AC: #1)
  - [x] If `homeComponent == null` → no plugin items in dropdown (null-safe check)
  - [x] If `navigationExtensions.isEmpty()` → no plugin items section in dropdown
  - [x] UI remains functional with zero plugins

- [x] Task 8: Write Unit Tests (AC: all)
  - [x] Created NavigationExtensionsTest.kt with 6 tests
  - [x] Test: Empty registry returns empty list
  - [x] Test: Single plugin queried correctly
  - [x] Test: Multiple plugins sorted by order ascending
  - [x] Test: NavigationItem properties accessible
  - [x] Test: Same order value maintains stable sort
  - [x] Test: Unregister removes extension from list
  - [x] Run `./gradlew :kimai-shared:jvmTest` - **ALL TESTS PASSED (1012/1012)**

- [x] Task 9: Regression Testing
  - [x] Fixed HomeComponentLocal to be nullable (backward compatibility)
  - [x] Added null-safe check in TopAppBarDropdownMenu
  - [x] All existing tests passing (no regressions)
  - [x] Full test suite: 1012 tests completed, 0 failed

## Dev Notes

### Architecture Context

This is **Story 2.4 of Epic 2: Plugin Extension Points**. It integrates the `NavigationExtension` interface (Story 2.1) and `ExtensionRegistry` (Story 2.2) into the HomeComponent navigation system, allowing plugins to add their own screens as tabs.

**Extension Point Flow:**
1. Plugin implements `NavigationExtension` interface
2. Plugin is initialized → `ExtensionRegistry.register(plugin)` is called
3. HomeComponent queries `registry.getAll<NavigationExtension>()`
4. For each extension, HomeComponent adds a tab with the plugin's title/icon
5. When user clicks tab → `extension.createComponent()` creates the plugin's Decompose Component
6. Plugin's `Content(component)` composable is rendered in the tab
7. Plugin's component lifecycle is managed by Decompose (survives configuration changes)

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
            └── de/progeek/kimai/shared/ui/
                ├── home/
                │   ├── HomeComponent.kt          # MODIFIED - add plugin tab support
                │   ├── HomeContent.kt            # MODIFIED - render plugin tabs
                │   └── store/
                │       └── HomeStore.kt          # May need updates
                └── root/
                    └── RootComponent.kt          # MODIFIED - pass dependencies
```

### UI Design Reference

**Current HomeComponent Navigation:**
```
┌─────────────────────────────────────────┐
│  [📄 Timesheet] [⚙️ Settings]           │ Tab Bar (Core tabs only)
├─────────────────────────────────────────┤
│                                         │
│   Timesheet content or Settings content │
│                                         │
└─────────────────────────────────────────┘
```

**After Plugin Integration:**
```
┌─────────────────────────────────────────┐
│  [📄 Timesheet] [⚙️ Settings] [🔌 Tasks]│ Tab Bar (Core + Plugin tabs)
├─────────────────────────────────────────┤
│                                         │
│   Plugin "Tasks" screen rendered here   │
│   via extension.Content(component)      │
│                                         │
└─────────────────────────────────────────┘
```

**If No Plugins Have Navigation:**
Tab bar remains unchanged with only core tabs.

### Implementation Templates

**Updated HomeComponent Configuration:**
```kotlin
package de.progeek.kimai.shared.ui.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import de.progeek.kimai.plugin.api.PluginContext
import de.progeek.kimai.plugin.api.extensions.ExtensionRegistry
import de.progeek.kimai.plugin.api.extensions.NavigationExtension
import de.progeek.kimai.plugin.api.extensions.getAll

class HomeComponent(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    val extensionRegistry: ExtensionRegistry?,  // NEW - nullable for backward compatibility
    val pluginContext: PluginContext?,          // NEW - nullable for backward compatibility
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val navigationExtensions: List<NavigationExtension> by lazy {
        extensionRegistry?.getAll<NavigationExtension>()
            ?.sortedBy { it.navigationItem.order }
            ?: emptyList()
    }

    private val navigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = navigation,
        initialConfiguration = Configuration.Timesheet,
        handleBackButton = false,
        childFactory = ::createChild
    )

    private fun createChild(config: Configuration, context: ComponentContext): Child =
        when (config) {
            Configuration.Timesheet -> Child.Timesheet(
                component = TimesheetComponent(
                    componentContext = context,
                    storeFactory = storeFactory,
                    output = ::onTimesheetOutput
                )
            )
            Configuration.Settings -> Child.Settings(
                component = SettingsComponent(
                    componentContext = context,
                    storeFactory = storeFactory,
                    dispatchers = dispatchers,
                    extensionRegistry = extensionRegistry,
                    pluginContext = pluginContext,
                    output = ::onSettingsOutput
                )
            )
            is Configuration.PluginTab -> {
                val extension = config.extension
                val pluginComponent = extension.createComponent(
                    componentContext = context,
                    pluginContext = pluginContext
                        ?: error("PluginContext required for plugin navigation")
                )
                Child.PluginTab(
                    extension = extension,
                    component = pluginComponent
                )
            }
        }

    fun onTabSelected(config: Configuration) {
        navigation.navigate { listOf(config) }
    }

    sealed class Configuration : Parcelable {
        @Parcelize
        data object Timesheet : Configuration()

        @Parcelize
        data object Settings : Configuration()

        @Parcelize
        data class PluginTab(
            val pluginId: String,
            // Note: NavigationExtension is stored as @RawValue to avoid serialization
            val extension: @RawValue NavigationExtension
        ) : Configuration()
    }

    sealed class Child {
        data class Timesheet(val component: TimesheetComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
        data class PluginTab(
            val extension: NavigationExtension,
            val component: Any  // Plugin's Decompose Component (type erased)
        ) : Child()
    }

    sealed class Output {
        data object Logout : Output()
    }
}
```

**Updated HomeContent.kt:**
```kotlin
package de.progeek.kimai.shared.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import de.progeek.kimai.shared.ui.components.ErrorContent
import de.progeek.kimai.shared.ui.settings.SettingsContent
import de.progeek.kimai.shared.ui.timesheet.TimesheetContent
import de.progeek.kimai.shared.ui.components.tabbar.TabBar
import de.progeek.kimai.shared.ui.components.tabbar.TabItem

@Composable
fun HomeContent(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childStack.subscribeAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Bar with core + plugin tabs
        TabBar(
            selectedIndex = getSelectedTabIndex(childStack.active.configuration, component),
            onTabSelected = { index ->
                val config = getConfigurationForIndex(index, component)
                component.onTabSelected(config)
            },
            tabs = buildTabList(component)
        )

        // Content Area
        Children(
            stack = childStack,
            animation = stackAnimation(fade())
        ) { child ->
            when (val instance = child.instance) {
                is HomeComponent.Child.Timesheet -> TimesheetContent(
                    component = instance.component,
                    modifier = Modifier.fillMaxSize()
                )
                is HomeComponent.Child.Settings -> SettingsContent(
                    component = instance.component,
                    modifier = Modifier.fillMaxSize()
                )
                is HomeComponent.Child.PluginTab -> {
                    // Render plugin content via extension's Content() composable
                    instance.extension.Content(instance.component)
                }
            }
        }
    }
}

private fun buildTabList(component: HomeComponent): List<TabItem> {
    val coreTabs = listOf(
        TabItem(title = "Timesheet", icon = null),  // TODO: Add icons
        TabItem(title = "Settings", icon = null)
    )

    val pluginTabs = component.navigationExtensions.map { ext ->
        TabItem(
            title = ext.navigationItem.title,
            icon = ext.navigationItem.icon
        )
    }

    return coreTabs + pluginTabs
}

private fun getSelectedTabIndex(
    config: HomeComponent.Configuration,
    component: HomeComponent
): Int = when (config) {
    HomeComponent.Configuration.Timesheet -> 0
    HomeComponent.Configuration.Settings -> 1
    is HomeComponent.Configuration.PluginTab -> {
        2 + component.navigationExtensions.indexOfFirst { it.navigationItem.id == config.pluginId }
    }
}

private fun getConfigurationForIndex(
    index: Int,
    component: HomeComponent
): HomeComponent.Configuration = when (index) {
    0 -> HomeComponent.Configuration.Timesheet
    1 -> HomeComponent.Configuration.Settings
    else -> {
        val pluginIndex = index - 2
        val extension = component.navigationExtensions[pluginIndex]
        HomeComponent.Configuration.PluginTab(
            pluginId = extension.navigationItem.id,
            extension = extension
        )
    }
}
```

**TabBar Component (if not exists):**
```kotlin
package de.progeek.kimai.shared.ui.components.tabbar

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class TabItem(
    val title: String,
    val icon: ImageVector? = null
)

@Composable
fun TabBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<TabItem>,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
    ) {
        tabs.forEachIndexed { index, item ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(item.title) },
                icon = item.icon?.let { icon ->
                    { Icon(imageVector = icon, contentDescription = item.title) }
                }
            )
        }
    }
}
```

### Previous Story Intelligence (Stories 2.1, 2.2, 2.3)

**From Story 2.1 - Extension Interfaces:**
- `NavigationExtension` interface: `navigationItem: NavigationItem` + `createComponent()` + `@Composable Content()`
- `NavigationItem`: `id: String`, `title: String`, `icon: ImageVector?`, `order: Int = 100`
- Input validation: id and title must not be blank, order >= 0

**From Story 2.2 - Extension Registry:**
- `ExtensionRegistry.getAll<NavigationExtension>()` returns list of plugins
- Thread-safe implementation with synchronized blocks
- Returns snapshot (list copy), won't reflect future changes
- Plugins auto-registered on init, auto-unregistered on dispose

**From Story 2.3 - Settings Integration:**
- Pattern established for adding ExtensionRegistry and PluginContext to components
- Use nullable parameters for backward compatibility
- Conditional rendering when dependencies are null
- Breaking change warnings documented

**Files Created in Previous Stories:**
- `kimai-plugin-api/.../extensions/NavigationExtension.kt`
- `kimai-plugin-api/.../extensions/NavigationItem.kt`
- `kimai-plugin-api/.../extensions/ExtensionRegistry.kt`
- `kimai-plugin-api/.../extensions/DefaultExtensionRegistry.kt`

**Code Review Learnings from Story 2.3:**
- Document breaking changes clearly (constructor signatures)
- Add explicit tests for each acceptance criterion
- Use nullable parameters for backward compatibility
- Integration tests are valuable for multi-component features
- Plugin fault tolerance handled at initialization level

### Git Intelligence

Recent commits:
```
97ece24 2-3-integrate-settings-extension-point
651c414 Story 2.2: Implement Extension Registry
37649fb  2-1-define-extension-point-interfaces
73199e2  1-7-add-plugin-registry-in-desktop-module
b79d5d2  1-6-implement-plugin-fault-tolerance
```

**Patterns from Previous Stories:**
- Extension integrations modify multiple components (HomeComponent, RootComponent, Main.kt)
- Nullable parameters pattern for backward compatibility
- Use `instanceKeeper.getOrCreate` for store lifecycle management
- Use `childStack` for navigation with state preservation
- Decompose `Configuration` must be `@Parcelize` for state restoration
- Plugin components are type-erased (stored as `Any`) to avoid circular dependencies

### Critical Implementation Notes

1. **Breaking Change - HomeComponent API:** The HomeComponent constructor signature changes to include two new nullable parameters: `extensionRegistry: ExtensionRegistry?` and `pluginContext: PluginContext?`. These are nullable for backward compatibility. Existing code that creates HomeComponent directly must be updated.

2. **Decompose Navigation:** Plugins provide their own Decompose Components via `createComponent()`. These components are managed by Decompose's lifecycle, which handles state preservation and back stack management.

3. **Type Erasure:** Plugin components are stored as `Any` to avoid circular dependencies between `kimai-plugin-api` and `kimai-shared`. Plugins cast the component to their concrete type in `Content()`.

4. **Configuration Serialization:** `Configuration.PluginTab` uses `@RawValue` for the `NavigationExtension` because interfaces cannot be serialized. This means plugin tabs may not survive process death - acceptable trade-off for MVP.

5. **Tab Ordering:** Core tabs always come first (Timesheet, Settings), followed by plugin tabs sorted by `navigationItem.order` (lower = first).

6. **Error Handling:** If a plugin's `createComponent()` throws an exception, it's caught by PluginManager during init (Story 1.6). Failed plugins won't appear in `getAll()` results.

7. **Empty State:** If no plugins provide NavigationExtension, the tab bar shows only core tabs. UI remains unchanged from pre-plugin behavior.

### Dependencies

**Must be complete before this story:**
- Story 2.1: Define Extension Point Interfaces ✅
- Story 2.2: Implement Extension Registry ✅
- Story 2.3: Integrate Settings Extension Point ✅

**Blocked by this story:**
- Story 2.5: Integrate Timesheet Actions Extension Point (may need similar patterns)

### HomeComponent Current Structure

**Expected location:** `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/home/HomeComponent.kt`

**Current navigation pattern analysis needed:**
- How does HomeComponent currently handle Timesheet ↔ Settings navigation?
- Does it use childStack or simpler state-based approach?
- Are Configuration objects already defined?
- Is there a tab bar UI component, or is it embedded in HomeContent?

### Testing Strategy

**Unit Tests:**
```kotlin
class HomeComponentTest {
    @Test
    fun `empty registry shows only core tabs`() {
        val registry = DefaultExtensionRegistry()
        val component = HomeComponent(..., extensionRegistry = registry, ...)
        // Assert: only 2 tabs (Timesheet, Settings)
    }

    @Test
    fun `single plugin adds third tab`() {
        val registry = DefaultExtensionRegistry()
        val mockPlugin = MockNavigationPlugin()
        registry.register(mockPlugin)
        val component = HomeComponent(..., extensionRegistry = registry, ...)
        // Assert: 3 tabs total
    }

    @Test
    fun `multiple plugins sorted by order`() {
        val registry = DefaultExtensionRegistry()
        registry.register(PluginWithOrder(order = 200, id = "second"))
        registry.register(PluginWithOrder(order = 100, id = "first"))
        val component = HomeComponent(..., extensionRegistry = registry, ...)
        // Assert: core tabs → plugin "first" → plugin "second"
    }

    @Test
    fun `clicking plugin tab navigates to plugin screen`() {
        // Given: Plugin registered
        // When: onTabSelected(Configuration.PluginTab(...))
        // Then: childStack shows plugin component
    }

    @Test
    fun `plugin component receives PluginContext`() {
        val mockPlugin = MockNavigationPlugin()
        // Verify: createComponent() receives non-null PluginContext
    }

    @Test
    fun `state preserved across tab switches`() {
        // Navigate: Timesheet → Plugin → Timesheet
        // Assert: Timesheet component state unchanged
    }
}
```

### References

- [Source: _bmad-output/architecture.md#UI Integration Architecture]
- [Source: _bmad-output/architecture.md#Decompose + Compose Extension Points]
- [Source: _bmad-output/epics.md#Story 2.4: Integrate Navigation Extension Point]
- [Source: _bmad-output/project-context.md#Decompose Component Pattern]
- [Source: _bmad-output/implementation-artifacts/2-1-define-extension-point-interfaces.md]
- [Source: _bmad-output/implementation-artifacts/2-2-implement-extension-registry.md]
- [Source: _bmad-output/implementation-artifacts/2-3-integrate-settings-extension-point.md]

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5 (claude-sonnet-4-5-20250929[1m])

### Debug Log References

- Stack-based navigation maintained (no tab-based approach) per user requirement
- Plugin screens accessed via dropdown menu in TopAppBar (not tab bar)
- HomeComponentLocal made nullable for backward compatibility with isolated component tests
- @Serializable with @Transient used instead of @Parcelize + @RawValue

### Completion Notes List

1. **HomeComponent Configuration extended** - Added `Configuration.PluginTab` with `@Transient extension` for non-serializable NavigationExtension
2. **navigationExtensions property added** - Lazy-loaded list queries ExtensionRegistry and sorts by order
3. **childFactory extended** - Handles PluginTab configuration, creates plugin components via `extension.createComponent()`
4. **navigateToPlugin() method added** - Public API for navigating to plugin screens by pluginId
5. **navigateBack() method added** - Public API for plugins to navigate back (via HomeComponentLocal)
6. **HomeComponentLocal created** - Nullable composition local allows TimesheetTopBar to access HomeComponent
7. **HomeContent provides HomeComponent** - CompositionLocalProvider makes component available to child composables
8. **Child.PluginTab added** - Renders plugin content via `extension.Content(component)`
9. **TopAppBarDropdownMenu extended** - Plugin navigation items appear in dropdown menu after Refresh, before Manual/Timer modes
10. **HorizontalDivider added** - Visual separation between plugin navigation section and other menu items
11. **Null-safety ensured** - Backward compatibility with tests and contexts where HomeComponent unavailable
12. **createPluginChild() method added** - Separate helper for plugin child creation with try-catch error handling
13. **Napier logging added** - Error/warning logging for plugin navigation failures
14. **Unit tests created** - NavigationExtensionsTest.kt with 10 tests covering extension registry logic and edge cases
15. **Regression testing passed** - All tests continue to pass

### File List

**Created:**
- `kimai-shared/src/jvmTest/kotlin/de/progeek/kimai/shared/ui/home/NavigationExtensionsTest.kt`

**Modified:**
- `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/home/HomeComponent.kt`
- `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/home/HomeContent.kt`
- `kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/timesheet/topbar/components/TimesheetTopBar.kt`

### Change Log

- 2025-12-29: Story 2.4 created with comprehensive developer context
- 2025-12-29: Implementation completed using stack-based navigation with dropdown menu (user requirement)
  - Plugin navigation via TopAppBar dropdown menu instead of tab bar
  - All acceptance criteria met
  - 6 unit tests added (NavigationExtensionsTest.kt)
  - All 1012 tests passing (zero regressions)
  - Ready for code review
- 2025-12-29: Code Review completed (Claude Opus 4.5) - 8 issues found and fixed:
  - **Issue 1 (HIGH):** State restoration fallback now logs errors instead of crashing
  - **Issue 2 (HIGH):** Added `navigateBack()` method for plugin screens to return to previous screen
  - **Issue 3 (HIGH):** Added try-catch around `createComponent()` with fallback to Timesheet on error
  - **Issue 4 (MEDIUM):** Added 4 new edge-case tests (10 tests total now)
  - **Issue 5 (MEDIUM):** Added Napier logging for invalid pluginId in `navigateToPlugin()`
  - **Issue 6 (MEDIUM):** Verified HorizontalDivider placement is correct (false positive)
  - **Issue 7 (LOW):** Added documentation comments for DelicateDecomposeApi usage
  - **Issue 8 (LOW):** Strengthened stable sort test to verify ordering across multiple queries
  - Refactored createPluginChild() into separate method for cleaner error handling
  - All tests passing after fixes
