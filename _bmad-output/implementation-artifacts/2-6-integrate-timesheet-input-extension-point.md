# Story 2.6: Integrate TimesheetInput Extension Point

Status: review

## Story

As a **user**,
I want **plugin suggestions to appear in the description field**,
So that **I can quickly select tasks/issues while typing**.

## Acceptance Criteria

1. **Given** plugins implement TimesheetInputExtension
   **When** I type in the timesheet description field
   **Then** plugin suggestions appear in a popup below the field

2. **And** each suggestion shows: title, subtitle (if present), icon (if present)

3. **And** clicking a suggestion inserts formatted text via `formatSuggestion()`

4. **And** keyboard navigation (up/down/enter) works for selection

5. **And** if no plugins provide suggestions, no popup appears

6. **And** multiple plugins' suggestions are combined (grouped by plugin)

## Tasks / Subtasks

- [x] Task 1: Create SuggestionPopup Composable (AC: #1, #2)
  - [x] Create `SuggestionPopup.kt` in `kimai-shared/.../ui/form/components/`
  - [x] Display InputSuggestion items (title, subtitle?, icon?)
  - [x] Position popup below the description field
  - [x] Style consistent with existing dropdowns (Material3)
  - [x] Show plugin source badge/grouping for multiple plugins

- [x] Task 2: Add State Fields to FormStore (AC: #1, #5)
  - [x] Add `pluginSuggestions: List<InputSuggestion> = emptyList()` to State
  - [x] Add `showSuggestions: Boolean = false` to State
  - [x] Add `selectedSuggestionIndex: Int = 0` to State
  - [x] Add `isSuggestionsLoading: Boolean = false` to State
  - [x] Create Msg types for suggestion state updates

- [x] Task 3: Implement Suggestion Query Logic (AC: #1, #5, #6)
  - [x] Add `QuerySuggestions(query: String)` Intent
  - [x] Add `DismissSuggestions` Intent
  - [x] Implement debounced query (300ms) in ExecutorImpl
  - [x] Query all TimesheetInputExtension plugins via ExtensionRegistry
  - [x] Combine suggestions from multiple plugins
  - [x] Only show popup when suggestions exist

- [x] Task 4: Implement Suggestion Selection (AC: #3)
  - [x] Add `SelectSuggestion(index: Int)` Intent
  - [x] Call `formatSuggestion()` on selected InputSuggestion
  - [x] Update description field with formatted text
  - [x] Dismiss popup after selection

- [x] Task 5: Implement Keyboard Navigation (AC: #4)
  - [x] Add `NavigateSuggestions(direction: Int)` Intent (+1 down, -1 up)
  - [x] Add keyboard event handling in DescriptionInput
  - [x] Arrow Up/Down: Navigate suggestions
  - [x] Enter: Select current suggestion
  - [x] Escape: Dismiss suggestions

- [x] Task 6: Update DescriptionInput Integration (AC: #1, #2, #3, #4)
  - [x] Modify `DescriptionInput.kt` to accept ExtensionRegistry
  - [x] Wire description changes to QuerySuggestions intent
  - [x] Render SuggestionPopup below input field
  - [x] Pass keyboard events for navigation

- [x] Task 7: Inject ExtensionRegistry into FormComponent (AC: all)
  - [x] Add `extensionRegistry: ExtensionRegistry?` parameter to FormComponent
  - [x] Pass through from HomeComponent
  - [x] Update FormStoreFactory to accept extensionRegistry
  - [x] Update DefaultFormComponentContext if needed

- [x] Task 8: Write Unit Tests (AC: all)
  - [x] Test: Suggestions appear for matching query
  - [x] Test: Suggestions dismissed on empty query
  - [x] Test: formatSuggestion updates description
  - [x] Test: Keyboard navigation cycles through suggestions
  - [x] Test: Multiple plugin suggestions combined correctly
  - [x] Test: No popup when no plugins registered
  - [x] Run `./gradlew :kimai-shared:jvmTest`

## Dev Notes

### Architecture Context

This is **Story 2.6 of Epic 2: Plugin Extension Points**. It integrates the `TimesheetInputExtension` interface (created in Story 2.5) into the Core UI, enabling plugins to provide autocomplete suggestions in the timesheet description field.

**Integration Pattern:** Follow the same pattern as Story 2.3 (SettingsExtension) and Story 2.4 (NavigationExtension):
- Query `ExtensionRegistry.getAll<TimesheetInputExtension>()`
- Sort by some criteria (plugin ID for stability)
- Combine suggestions from all plugins
- Handle gracefully when no plugins registered

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Compose Multiplatform | 1.9.3 | UI Framework |
| MVIKotlin | 4.3.0 | State Management (FormStore) |
| Coroutines | 1.10.2 | Async getSuggestions |

### Target Directory Structure

```
kimai-shared/
└── src/
    └── commonMain/
        └── kotlin/
            └── de/progeek/kimai/shared/
                └── ui/
                    └── form/
                        ├── FormStore.kt          # MODIFY - add suggestion state
                        ├── FormComponent.kt      # MODIFY - add extensionRegistry param
                        └── components/
                            ├── DescriptionInput.kt    # MODIFY - wire suggestions
                            └── SuggestionPopup.kt     # NEW - suggestion dropdown
```

### Previous Story Intelligence (Story 2.5)

**From Story 2.5 - TimesheetInputExtension Interface:**
- Interface: `TimesheetInputExtension` with `getSuggestions()` and `formatSuggestion()`
- Data class: `InputSuggestion` with `id`, `title`, `subtitle?`, `icon?`, `metadata`
- Registered in `DefaultExtensionRegistry.EXTENSION_TYPES`
- Query via `extensionRegistry.getAll<TimesheetInputExtension>()`

**Key Files Created in Story 2.5:**
- `kimai-plugin-api/.../extensions/TimesheetInputExtension.kt`
- `kimai-plugin-api/.../extensions/InputSuggestion.kt`

### Integration Pattern Reference (Story 2.3 & 2.4)

**SettingsExtension Integration (Story 2.3):**
```kotlin
// PluginSettingsSection.kt
val settingsExtensions = extensionRegistry.getAll<SettingsExtension>()
    .sortedBy { it.settingsItem.order }

if (settingsExtensions.isEmpty()) {
    return // No popup when no plugins
}

settingsExtensions.forEach { extension ->
    extension.SettingsContent(pluginContext)
}
```

**NavigationExtension Integration (Story 2.4):**
```kotlin
// HomeComponent.kt
val navigationExtensions: List<NavigationExtension> by lazy {
    extensionRegistry?.getAll<NavigationExtension>()
        ?.sortedBy { it.navigationItem.order }
        ?: emptyList()
}
```

### Current FormStore Structure

```kotlin
// FormStore.kt - current structure
interface FormStore : Store<Intent, State, Label> {
    sealed class Intent {
        data class DescriptionUpdated(val description: String) : Intent()
        // ... other intents
    }

    data class State(
        val description: String = ""
        // ... other fields
    )
}
```

**Required Additions to FormStore:**
```kotlin
// NEW State fields for suggestions
data class State(
    // ... existing fields
    val pluginSuggestions: List<InputSuggestion> = emptyList(),
    val showSuggestions: Boolean = false,
    val selectedSuggestionIndex: Int = 0,
    val isSuggestionsLoading: Boolean = false
)

// NEW Intents
sealed class Intent {
    // ... existing intents
    data class QuerySuggestions(val query: String) : Intent()
    data object DismissSuggestions : Intent()
    data class SelectSuggestion(val index: Int) : Intent()
    data class NavigateSuggestions(val direction: Int) : Intent() // +1 down, -1 up
}

// NEW Msg types
private sealed class Msg {
    // ... existing messages
    data class SuggestionsLoading(val loading: Boolean) : Msg()
    data class SuggestionsReceived(val suggestions: List<InputSuggestion>) : Msg()
    data object SuggestionsDismissed : Msg()
    data class SuggestionSelected(val description: String) : Msg()
    data class SuggestionIndexChanged(val index: Int) : Msg()
}
```

### Debouncing Implementation

```kotlin
// In ExecutorImpl - use Job to debounce
private var suggestionJob: Job? = null

private fun querySuggestions(query: String) {
    suggestionJob?.cancel()

    if (query.length < 2) {
        dispatch(Msg.SuggestionsDismissed)
        return
    }

    dispatch(Msg.SuggestionsLoading(true))

    suggestionJob = scope.launch {
        delay(300) // 300ms debounce

        val allSuggestions = mutableListOf<InputSuggestion>()
        extensionRegistry?.getAll<TimesheetInputExtension>()?.forEach { extension ->
            try {
                val suggestions = withContext(ioContext) {
                    extension.getSuggestions(query)
                }
                allSuggestions.addAll(suggestions)
            } catch (e: Exception) {
                Napier.e("Failed to get suggestions from plugin", e)
            }
        }

        dispatch(Msg.SuggestionsReceived(allSuggestions))
    }
}
```

### SuggestionPopup Composable Design

```kotlin
// SuggestionPopup.kt - suggested structure
@Composable
fun SuggestionPopup(
    suggestions: List<InputSuggestion>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return

    Popup(
        alignment = Alignment.TopStart,
        onDismissRequest = onDismiss
    ) {
        Surface(
            shadowElevation = 8.dp,
            shape = MaterialTheme.shapes.small
        ) {
            LazyColumn {
                itemsIndexed(suggestions) { index, suggestion ->
                    SuggestionItem(
                        suggestion = suggestion,
                        isSelected = index == selectedIndex,
                        onClick = { onSelect(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: InputSuggestion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Optional icon
        suggestion.icon?.let { icon ->
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
        }

        Column {
            Text(suggestion.title, style = MaterialTheme.typography.bodyMedium)
            suggestion.subtitle?.let { subtitle ->
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### Keyboard Navigation

```kotlin
// In DescriptionInput.kt - add key event handling
BasicTextField(
    // ... existing params
    modifier = Modifier
        .fillMaxWidth()
        .onKeyEvent { event ->
            if (showSuggestions && event.type == KeyEventType.KeyDown) {
                when (event.key) {
                    Key.DirectionDown -> {
                        onNavigate(1)
                        true
                    }
                    Key.DirectionUp -> {
                        onNavigate(-1)
                        true
                    }
                    Key.Enter -> {
                        onSelectCurrent()
                        true
                    }
                    Key.Escape -> {
                        onDismiss()
                        true
                    }
                    else -> false
                }
            } else false
        }
)
```

### Git Intelligence

Recent commits showing integration patterns:
```
bbeec6f 2-5-define-timesheet-input-extension-point
07dd808 2-4-integrate-navigation-extension-point
97ece24 2-3-integrate-settings-extension-point
651c414 Story 2.2: Implement Extension Registry
```

**Key Pattern from 2.4:**
- Add `extensionRegistry: ExtensionRegistry?` parameter to component
- Use `by lazy` for extension queries
- Handle null extensionRegistry gracefully (no plugins = no popup)
- Error handling with try-catch and Napier logging

### Testing Strategy

**Unit Tests:**
```kotlin
class FormStoreSuggestionsTest {
    @Test
    fun `suggestions appear for matching query`() = runTest {
        // Given: Mock plugin with TimesheetInputExtension
        val mockExtension = mockk<TimesheetInputExtension>()
        coEvery { mockExtension.getSuggestions("test") } returns listOf(
            InputSuggestion(id = "1", title = "Test Result")
        )

        // When: QuerySuggestions intent dispatched
        store.accept(Intent.QuerySuggestions("test"))
        advanceTimeBy(300) // Wait for debounce

        // Then: Suggestions in state
        store.state.pluginSuggestions.shouldNotBeEmpty()
        store.state.showSuggestions.shouldBeTrue()
    }

    @Test
    fun `formatSuggestion updates description on selection`() {
        // Given: Suggestion in state
        coEvery { mockExtension.formatSuggestion(any()) } returns "JIRA-123: Task"

        // When: SelectSuggestion intent
        store.accept(Intent.SelectSuggestion(0))

        // Then: Description updated
        store.state.description.shouldBe("JIRA-123: Task")
        store.state.showSuggestions.shouldBeFalse()
    }

    @Test
    fun `keyboard navigation cycles through suggestions`() {
        // Given: Multiple suggestions
        store.accept(Intent.QuerySuggestions("test"))
        advanceTimeBy(300)

        // When: Navigate down twice
        store.accept(Intent.NavigateSuggestions(1))
        store.accept(Intent.NavigateSuggestions(1))

        // Then: Index at 2
        store.state.selectedSuggestionIndex.shouldBe(2)
    }
}
```

### Dependencies

**Must be complete before this story:**
- Story 2.5: Define TimesheetInput Extension Point ✅

**Blocked by this story:**
- Epic 5: Task Integration Plugin (will use this extension point)

### References

- [Source: _bmad-output/project-planning-artifacts/epics.md#Story 2.6: Integrate TimesheetInput Extension Point]
- [Source: _bmad-output/project-context.md#MVIKotlin Store Pattern]
- [Source: kimai-plugin-api/.../extensions/TimesheetInputExtension.kt] - Interface definition
- [Source: kimai-plugin-api/.../extensions/InputSuggestion.kt] - Data class
- [Source: kimai-shared/.../ui/form/FormStore.kt] - Target store for modification
- [Source: kimai-shared/.../ui/form/components/DescriptionInput.kt] - Target composable
- [Source: kimai-shared/.../ui/settings/components/PluginSettingsSection.kt] - Integration pattern reference
- [Source: kimai-shared/.../ui/home/HomeComponent.kt] - ExtensionRegistry injection pattern

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- All tests passed: `./gradlew :kimai-shared:jvmTest`
- Build successful with no compilation errors

### Completion Notes List

- ✅ Created `SuggestionPopup.kt` composable with Material3 styling, loading indicator, and selection highlighting
- ✅ Added suggestion state fields to `FormStore.State` (pluginSuggestions, showSuggestions, selectedSuggestionIndex, isSuggestionsLoading)
- ✅ Added new Intents: QuerySuggestions, DismissSuggestions, SelectSuggestion, NavigateSuggestions
- ✅ Added corresponding Msg types: SuggestionsLoading, SuggestionsReceived, SuggestionsDismissed, SuggestionSelected, SuggestionIndexChanged
- ✅ Implemented debounced query logic (300ms) with Job cancellation in ExecutorImpl
- ✅ Implemented suggestion selection with formatSuggestion() call
- ✅ Implemented keyboard navigation with clamping to valid range
- ✅ Updated `DescriptionInput.kt` with full suggestion support and backward-compatible overload
- ✅ Added keyboard event handling (Arrow Up/Down, Enter, Escape)
- ✅ Updated `FormComponent.kt` to accept ExtensionRegistry parameter
- ✅ Updated `FormStoreFactory` to accept and pass ExtensionRegistry to ExecutorImpl
- ✅ Updated `HomeComponent.kt` to pass ExtensionRegistry to FormComponent
- ✅ Updated `Form.kt` to wire suggestion state and callbacks
- ✅ Added 11 unit tests covering all acceptance criteria

### File List

**New Files:**
- kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/form/components/SuggestionPopup.kt

**Modified Files:**
- kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/form/FormStore.kt
- kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/form/FormComponent.kt
- kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/form/components/DescriptionInput.kt
- kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/form/components/Form.kt
- kimai-shared/src/commonMain/kotlin/de/progeek/kimai/shared/ui/home/HomeComponent.kt
- kimai-shared/src/commonTest/kotlin/de/progeek/kimai/shared/ui/form/FormStoreTest.kt
