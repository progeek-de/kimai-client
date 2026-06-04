# Story 2.5: Define TimesheetInput Extension Point

Status: done

## Story

As a **plugin developer**,
I want **an extension point for enhancing the timesheet input field**,
So that **my plugin can provide autocomplete suggestions and input enhancements**.

## Acceptance Criteria

1. **Given** the kimai-plugin-api module
   **When** I define the TimesheetInputExtension interface
   **Then** it has `suspend fun getSuggestions(query: String): List<InputSuggestion>` for autocomplete

2. **And** it has `fun formatSuggestion(suggestion: InputSuggestion): String` for text formatting

3. **And** `InputSuggestion` data class contains: `id`, `title`, `subtitle?`, `icon?`, `metadata`

4. **And** the interface extends `Extension` base interface

5. **And** all interfaces are in `de.progeek.kimai.plugin.api.extensions` package

6. **And** the extension is discoverable via ExtensionRegistry

## Tasks / Subtasks

- [x] Task 1: Create InputSuggestion Data Class (AC: #3)
  - [x] Create `InputSuggestion.kt` in `kimai-plugin-api/.../extensions/`
  - [x] Add properties: `id: String`, `title: String`, `subtitle: String? = null`, `icon: ImageVector? = null`, `metadata: Map<String, Any> = emptyMap()`
  - [x] Add init block validating id and title are not blank
  - [x] Add KDoc documentation with examples

- [x] Task 2: Create TimesheetInputExtension Interface (AC: #1, #2, #4)
  - [x] Create `TimesheetInputExtension.kt` in `kimai-plugin-api/.../extensions/`
  - [x] Extend `Extension` base interface
  - [x] Add `suspend fun getSuggestions(query: String): List<InputSuggestion>`
  - [x] Add `fun formatSuggestion(suggestion: InputSuggestion): String`
  - [x] Add comprehensive KDoc with lifecycle description and example implementation

- [x] Task 3: Update ExtensionRegistry for TimesheetInputExtension (AC: #6)
  - [x] Verify `ExtensionRegistry.getAll<TimesheetInputExtension>()` works
  - [x] Add test case for TimesheetInputExtension registration
  - [x] Ensure thread-safe query of input extensions

- [x] Task 4: Write Unit Tests (AC: all)
  - [x] Create `TimesheetInputExtensionTest.kt`
  - [x] Test: InputSuggestion validation (id/title not blank)
  - [x] Test: InputSuggestion defaults (subtitle null, icon null, metadata empty)
  - [x] Test: Mock plugin implementing TimesheetInputExtension
  - [x] Test: getSuggestions returns list correctly
  - [x] Test: formatSuggestion returns formatted string
  - [x] Run `./gradlew :kimai-plugin-api:jvmTest`

- [x] Task 5: Update Existing Extension Documentation (AC: #5)
  - [x] Update package-info or README in extensions package
  - [x] Document relationship between extension points
  - [x] Add migration note: "TimesheetInputExtension replaces planned TimesheetActionExtension for input field enhancements"

## Dev Notes

### Architecture Context

This is **Story 2.5 of Epic 2: Plugin Extension Points**. It defines the `TimesheetInputExtension` interface that enables plugins to provide autocomplete suggestions in the timesheet description field.

**Course Correction:** This story replaces the originally planned `TimesheetActionExtension` (context menu actions). The existing ticket system in the codebase (`kimai-shared/core/ticketsystem/`) provides AutoSuggestions functionality that should eventually be migrated to a plugin using this extension point.

**Extension Point Purpose:**
- Plugins can provide autocomplete suggestions as the user types
- Suggestions appear in a popup below the description field
- User selection triggers `formatSuggestion()` to insert text
- Designed to support Task Integration Plugin (Epic 5) after migration

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Compose Multiplatform | 1.9.3 | UI Framework (ImageVector) |
| Coroutines | 1.10.2 | Async getSuggestions |

### Target Directory Structure

```
kimai-plugin-api/
└── src/
    └── commonMain/
        └── kotlin/
            └── de/progeek/kimai/plugin/api/
                └── extensions/
                    ├── Extension.kt                    # EXISTS
                    ├── ExtensionRegistry.kt            # EXISTS
                    ├── DefaultExtensionRegistry.kt     # EXISTS
                    ├── SettingsExtension.kt            # EXISTS
                    ├── SettingsItem.kt                 # EXISTS
                    ├── NavigationExtension.kt          # EXISTS
                    ├── NavigationItem.kt               # EXISTS
                    ├── TimesheetInputExtension.kt      # NEW
                    └── InputSuggestion.kt              # NEW
```

### Interface Design

**InputSuggestion.kt:**
```kotlin
package de.progeek.kimai.plugin.api.extensions

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A suggestion item for the timesheet input field autocomplete.
 *
 * Plugins provide these suggestions via [TimesheetInputExtension.getSuggestions].
 * When the user selects a suggestion, the formatted text from
 * [TimesheetInputExtension.formatSuggestion] is inserted into the field.
 *
 * Example:
 * ```kotlin
 * val jiraSuggestion = InputSuggestion(
 *     id = "JIRA-123",
 *     title = "Implement login feature",
 *     subtitle = "JIRA-123 • In Progress",
 *     icon = Icons.Default.BugReport,
 *     metadata = mapOf("issueKey" to "JIRA-123", "projectId" to "10001")
 * )
 * ```
 *
 * @property id Unique identifier for the suggestion (e.g., issue key)
 * @property title Primary display text (e.g., issue summary)
 * @property subtitle Optional secondary text (e.g., "JIRA-123 • In Progress")
 * @property icon Optional icon displayed alongside the title
 * @property metadata Additional data for plugin use (not displayed)
 */
data class InputSuggestion(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val metadata: Map<String, Any> = emptyMap()
) {
    init {
        require(id.isNotBlank()) { "InputSuggestion id must not be blank" }
        require(title.isNotBlank()) { "InputSuggestion title must not be blank" }
    }
}
```

**TimesheetInputExtension.kt:**
```kotlin
package de.progeek.kimai.plugin.api.extensions

/**
 * Extension point for plugins to enhance the timesheet input field.
 *
 * Plugins implementing this interface can provide autocomplete suggestions
 * based on user input in the timesheet description field. This enables
 * task management integrations (Jira, GitHub, GitLab, etc.) to offer
 * quick task selection while typing.
 *
 * Suggestion lifecycle:
 * 1. User types in the timesheet description field
 * 2. Core calls [getSuggestions] with the current text (debounced)
 * 3. Suggestions from all plugins are combined and displayed
 * 4. User selects a suggestion (click or keyboard)
 * 5. [formatSuggestion] is called to get the text to insert
 * 6. Formatted text replaces the description field content
 *
 * Example implementation:
 * ```kotlin
 * class JiraIntegrationPlugin : Plugin, TimesheetInputExtension {
 *     override suspend fun getSuggestions(query: String): List<InputSuggestion> {
 *         if (query.length < 2) return emptyList()
 *
 *         return jiraRepository.searchIssues(query).map { issue ->
 *             InputSuggestion(
 *                 id = issue.key,
 *                 title = issue.summary,
 *                 subtitle = "${issue.key} • ${issue.status}",
 *                 icon = Icons.Default.BugReport,
 *                 metadata = mapOf("issueId" to issue.id)
 *             )
 *         }
 *     }
 *
 *     override fun formatSuggestion(suggestion: InputSuggestion): String {
 *         return "${suggestion.id}: ${suggestion.title}"
 *     }
 * }
 * ```
 *
 * Performance considerations:
 * - [getSuggestions] is called on IO dispatcher, safe to perform network calls
 * - Core applies debouncing (300ms) before calling getSuggestions
 * - Return empty list quickly if query is too short (e.g., < 2 chars)
 * - Consider caching recent results for instant display
 *
 * @see InputSuggestion
 * @see Extension
 */
interface TimesheetInputExtension : Extension {

    /**
     * Get autocomplete suggestions for the given query.
     *
     * Called when the user types in the timesheet description field.
     * Implement this to search your task system and return matching items.
     *
     * @param query Current text in the description field
     * @return List of suggestions to display, or empty list if none match
     */
    suspend fun getSuggestions(query: String): List<InputSuggestion>

    /**
     * Format a suggestion for insertion into the description field.
     *
     * Called when the user selects a suggestion. The returned string
     * replaces the current description field content.
     *
     * @param suggestion The selected suggestion
     * @return Formatted text to insert (e.g., "JIRA-123: Task summary")
     */
    fun formatSuggestion(suggestion: InputSuggestion): String
}
```

### Previous Story Intelligence (Stories 2.1-2.4)

**From Story 2.1 - Extension Interfaces Pattern:**
- All extensions extend `Extension` base interface
- Data classes use `init` block for validation
- KDoc with examples is standard
- Package: `de.progeek.kimai.plugin.api.extensions`

**From Story 2.2 - Extension Registry:**
- `ExtensionRegistry.getAll<T>()` pattern for querying
- Thread-safe implementation with synchronized blocks
- Automatic registration during plugin init

**From Stories 2.3/2.4 - Integration Pattern:**
- Use nullable parameters for backward compatibility
- Lazy properties for extension queries
- Error handling with try-catch and Napier logging

### Current Codebase Context (Ticket System)

The existing ticket system in `kimai-shared/core/ticketsystem/` provides similar functionality:

**Current Implementation (to be migrated later):**
```kotlin
// TimesheetInputStore.kt - current approach
data class State(
    val ticketSuggestions: List<TicketIssue> = emptyList(),
    val showTicketSuggestions: Boolean = false,
    val selectedSuggestionIndex: Int = 0,
    // ...
)

sealed class Intent {
    data class SearchTickets(val query: String) : Intent()
    data object DismissTicketSuggestions : Intent()
    // ...
}
```

**Future State (after migration to plugin):**
```kotlin
// TimesheetInputStore.kt - after plugin migration
data class State(
    val pluginSuggestions: List<InputSuggestion> = emptyList(),
    val showSuggestions: Boolean = false,
    val selectedSuggestionIndex: Int = 0,
    // ...
)

// Plugin provides suggestions via TimesheetInputExtension
```

### Testing Strategy

**Unit Tests:**
```kotlin
class InputSuggestionTest {
    @Test
    fun `id must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            InputSuggestion(id = "", title = "Test")
        }
    }

    @Test
    fun `title must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            InputSuggestion(id = "test", title = "  ")
        }
    }

    @Test
    fun `defaults are applied correctly`() {
        val suggestion = InputSuggestion(id = "test", title = "Test Title")
        assertNull(suggestion.subtitle)
        assertNull(suggestion.icon)
        assertTrue(suggestion.metadata.isEmpty())
    }
}

class TimesheetInputExtensionTest {
    @Test
    fun `extension can be registered and queried`() {
        val registry = DefaultExtensionRegistry()
        val mockPlugin = object : Plugin, TimesheetInputExtension {
            override val id = "test-plugin"
            override val name = "Test"
            override val version = "1.0.0"
            override fun init(context: PluginContext) {}
            override fun dispose() {}

            override suspend fun getSuggestions(query: String): List<InputSuggestion> {
                return if (query.contains("test")) {
                    listOf(InputSuggestion(id = "1", title = "Test Result"))
                } else emptyList()
            }

            override fun formatSuggestion(suggestion: InputSuggestion): String {
                return "${suggestion.id}: ${suggestion.title}"
            }
        }

        registry.register(mockPlugin)

        val extensions = registry.getAll<TimesheetInputExtension>()
        assertEquals(1, extensions.size)
    }

    @Test
    fun `getSuggestions returns results for matching query`() = runTest {
        val extension = createMockExtension()
        val suggestions = extension.getSuggestions("test query")
        assertTrue(suggestions.isNotEmpty())
    }

    @Test
    fun `formatSuggestion returns formatted string`() {
        val extension = createMockExtension()
        val suggestion = InputSuggestion(id = "JIRA-123", title = "My Task")
        val formatted = extension.formatSuggestion(suggestion)
        assertEquals("JIRA-123: My Task", formatted)
    }
}
```

### Git Intelligence

Recent commits showing extension point patterns:
```
07dd808 2-4-integrate-navigation-extension-point
97ece24 2-3-integrate-settings-extension-point
651c414 Story 2.2: Implement Extension Registry
37649fb 2-1-define-extension-point-interfaces
```

Files from Story 2.1 to reference:
- `SettingsExtension.kt` - interface pattern
- `SettingsItem.kt` - data class pattern
- `NavigationExtension.kt` - interface with suspend functions
- `NavigationItem.kt` - data class with validation

### Dependencies

**Must be complete before this story:**
- Story 2.1: Define Extension Point Interfaces (SettingsExtension, NavigationExtension) ✅
- Story 2.2: Implement Extension Registry ✅

**Blocked by this story:**
- Story 2.6: Integrate TimesheetInput Extension Point (UI integration)
- Epic 5: Task Integration Plugin (will use this extension point after migration)

### References

- [Source: _bmad-output/project-planning-artifacts/epics.md#Story 2.5: Define TimesheetInput Extension Point]
- [Source: _bmad-output/project-context.md#Plugin System Rules]
- [Source: kimai-plugin-api/.../extensions/SettingsExtension.kt] - Interface pattern reference
- [Source: kimai-plugin-api/.../extensions/SettingsItem.kt] - Data class pattern reference
- [Source: kimai-shared/.../ui/timesheet/input/TimesheetInputStore.kt] - Current suggestions implementation

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- All tests passed: `./gradlew :kimai-plugin-api:jvmTest`
- Build successful with no compilation errors

### Completion Notes List

- ✅ Created `InputSuggestion.kt` data class with validation (id/title not blank) and defaults
- ✅ Created `TimesheetInputExtension.kt` interface with `getSuggestions()` and `formatSuggestion()` methods
- ✅ Added `TimesheetInputExtension::class` to `EXTENSION_TYPES` in `DefaultExtensionRegistry`
- ✅ Added comprehensive unit tests in `ExtensionDataClassesTest.kt` for InputSuggestion validation
- ✅ Created `TimesheetInputExtensionTest.kt` with 7 test cases covering:
  - getSuggestions returns list for matching query
  - getSuggestions returns empty list for non-matching query
  - getSuggestions returns empty list for short query
  - formatSuggestion returns formatted string
  - Extension can be registered in DefaultExtensionRegistry
  - Extension can be unregistered from DefaultExtensionRegistry
  - Multiple plugins with TimesheetInputExtension can be registered
- ✅ Updated `Extension.kt` KDoc to include TimesheetInputExtension in the available extension types list
- ✅ Added migration note in TimesheetInputExtension KDoc

### Code Review Fixes (2025-12-30)

- ✅ [MED-01] Updated DefaultExtensionRegistry KDoc to include TimesheetInput in extension types list
- ✅ [MED-02] Added test for InputSuggestion metadata key access with different value types
- ✅ [MED-03] Added edge-case tests for whitespace-only and empty subtitle acceptance
- ✅ [LOW-01] Renamed tests in TimesheetInputExtensionTest for consistency (prefix "TimesheetInputExtension")
- ✅ [LOW-02] Added test verifying metadata supports String, Int, Boolean value types
- ✅ [LOW-03] Added subtitle assertions to getSuggestions test (verifies "TEST-1 • In Progress")

### File List

**New Files:**
- kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/InputSuggestion.kt
- kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/TimesheetInputExtension.kt
- kimai-plugin-api/src/commonTest/kotlin/de/progeek/kimai/plugin/api/extensions/TimesheetInputExtensionTest.kt

**Modified Files:**
- kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/DefaultExtensionRegistry.kt
- kimai-plugin-api/src/commonMain/kotlin/de/progeek/kimai/plugin/api/extensions/Extension.kt
- kimai-plugin-api/src/commonTest/kotlin/de/progeek/kimai/plugin/api/extensions/ExtensionDataClassesTest.kt

