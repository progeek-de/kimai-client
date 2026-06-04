---
project_name: 'kimai-client'
user_name: 'Dmitri'
date: '2025-12-27'
sections_completed: ['technology_stack', 'claude_skills', 'mvikotlin_pattern', 'decompose_pattern', 'store5_pattern', 'arrow_pattern', 'package_structure', 'naming_conventions', 'code_quality', 'testing_rules', 'plugin_system', 'anti_patterns', 'open_core_model']
status: 'complete'
rule_count: 45
optimized_for_llm: true
---

# Project Context for AI Agents

_This file contains critical rules and patterns that AI agents must follow when implementing code in this project. Focus on unobvious details that agents might otherwise miss._

---

## Technology Stack & Versions

**CRITICAL: Use these exact versions. Do not upgrade without explicit approval.**

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Language |
| Compose Multiplatform | 1.9.3 | UI Framework |
| MVIKotlin | 4.3.0 | State Management |
| Decompose | 3.4.0 | Navigation |
| Koin | 4.1.1 | Dependency Injection |
| SQLDelight | 2.2.1 | Database |
| Store5 | 5.0.0 | Caching |
| Arrow | 2.2.0 | Error Handling |
| Ktor | 3.3.3 | HTTP Client |
| Coroutines | 1.10.2 | Async |
| Cryptography | 0.5.0 | Security |
| Java Target | 21 | Runtime |

---

## Claude Skills - MANDATORY

**AI Agents MUST invoke these skills when implementing:**

| Skill | When to Use | Command |
|-------|-------------|---------|
| `store5-kotlin` | Store5 caching, Fetcher, SourceOfTruth, MutableStore | `/store5-kotlin` |
| `decompose-mvikotlin` | Decompose navigation, MVIKotlin integration | `/decompose-mvikotlin` |
| `mvikotlin` | Creating new MVIKotlin stores | `/mvikotlin` |
| `mvi-create-store` | Generating new Store implementations | `/mvi-create-store` |
| `mvi-review` | Reviewing MVIKotlin code | `/mvi-review` |

---

## Critical Implementation Rules

### MVIKotlin Store Pattern

```kotlin
// CORRECT: Store interface with sealed Intent
interface XxxStore : Store<Intent, State, Label> {
    sealed class Intent {
        data object Load : Intent()
        data class Update(val value: String) : Intent()
    }

    data class State(
        val isLoading: Boolean = false,
        val data: List<Item> = emptyList(),
        val error: String? = null
    )

    sealed class Label {
        data object NavigateBack : Label()
    }
}

// CORRECT: StoreFactory pattern
class XxxStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: XxxRepository
) {
    fun create(): XxxStore = object : XxxStore, Store<...> by storeFactory.create(...) {}
}
```

### Decompose Component Pattern

```kotlin
// CORRECT: Component with delegation
class XxxComponent(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getOrCreate {
        XxxStoreFactory(storeFactory, ...).create()
    }

    val state: StateFlow<XxxStore.State> = store.stateFlow

    fun onIntent(intent: XxxStore.Intent) = store.accept(intent)

    // Navigation with childStack
    private val navigation = StackNavigation<Configuration>()
    val childStack = childStack(
        source = navigation,
        initialConfiguration = Configuration.Default,
        handleBackButton = true,
        childFactory = ::createChild
    )

    sealed class Output { ... }
    sealed class Configuration : Parcelable { ... }
}
```

### Store5 Caching Pattern

```kotlin
// CORRECT: Store5 with Fetcher + SourceOfTruth
private val store = StoreBuilder.from(
    fetcher = Fetcher.of { key -> api.fetch(key) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key -> dataSource.observe(key) },
        writer = { key, value -> dataSource.insert(value) }
    )
).build()

// CORRECT: Stream with refresh
fun observe(): Flow<StoreResponse<Data>> =
    store.stream(StoreRequest.cached(key = Unit, refresh = true))
```

### Arrow Error Handling

```kotlin
// CORRECT: Either for repository errors
suspend fun getData(): Either<RepositoryError, List<Data>>

// CORRECT: fold() for handling
repository.getData().fold(
    ifLeft = { error -> handleError(error) },
    ifRight = { data -> updateState(data) }
)
```

---

## Package Structure

```
de.progeek.kimai.shared.
├── core/
│   ├── database/      # DataSources (XxxDataSource.kt)
│   ├── di/            # Koin modules (XxxModule.kt)
│   ├── models/        # Domain models
│   ├── network/       # API clients
│   ├── repositories/  # Repository implementations
│   └── storage/       # Local storage
└── ui/
    └── {feature}/
        ├── {Feature}Component.kt
        └── store/
            ├── {Feature}Store.kt
            └── {Feature}StoreFactory.kt
```

**Plugin Package:** `de.progeek.kimai.plugin.{pluginname}/`

---

## Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `TimesheetStore`, `HomeComponent` |
| Variables | camelCase | `timesheetId`, `isLoading` |
| Constants | SCREAMING_SNAKE | `DEFAULT_TIMEOUT` |
| Packages | lowercase | `de.progeek.kimai.shared.ui.timesheet` |
| Files | PascalCase.kt | `TimesheetStore.kt` |
| Plugin ID | kebab-case | `"task-integration"` |
| Plugin DB | plugin-{id}.db | `plugin-tasks.db` |

---

## Code Quality Rules

- **Indent:** 4 spaces (not tabs)
- **Line endings:** LF (Unix)
- **Charset:** UTF-8
- **ktlint:** Enabled with Compose rules
- **Final newline:** Required

---

## Testing Rules

| Library | Purpose |
|---------|---------|
| MockK | Mocking |
| Turbine | Flow testing |
| JUnit 4 | Test framework |
| Coroutines Test | Suspend function testing |

```kotlin
// CORRECT: Test structure
@Test
fun `should do something when condition`() = runTest {
    // Given
    val mock = mockk<Repository>()
    coEvery { mock.getData() } returns Either.Right(testData)

    // When
    val result = useCase.execute()

    // Then
    result.shouldBeRight()
}
```

---

## Plugin System Rules

### Plugin Interface

```kotlin
interface Plugin {
    val id: String           // kebab-case: "task-integration"
    val name: String         // Display: "Task Integration"
    val version: String      // SemVer: "1.0.0"

    fun init(context: PluginContext)
    fun dispose()
}
```

### Plugin Boundaries

- Plugins communicate with Core ONLY via `PluginContext`
- Each plugin has its own SQLite database
- Plugins CANNOT access other plugins
- License validation happens IN the plugin, not Core

### PluginContext API

```kotlin
interface PluginContext {
    // Read-only Core stores (typed as Any to avoid circular deps)
    val timesheetListStore: Any  // Cast to TimesheetListStore in plugin
    val projectStore: Any        // Cast to ProjectStore in plugin
    val activityStore: Any       // Cast to ActivityStore in plugin
    val customerStore: Any       // Cast to CustomerStore in plugin

    // Plugin database (plugins create their own SqlDriver)
    fun getDatabasePath(name: String): String

    // Coroutine contexts
    val mainContext: CoroutineContext
    val ioContext: CoroutineContext

    // License key retrieval
    fun getLicenseKey(pluginId: String): String?
}
```

---

## Critical Anti-Patterns (DO NOT)

```kotlin
// ❌ DON'T: Direct Core database access from plugin
val coreDb = context.getCoreDatabase() // NOT ALLOWED

// ❌ DON'T: Static/global mutable state
object BadState { var items = mutableListOf() } // NOT ALLOWED

// ❌ DON'T: Plugin-to-plugin communication
val other = context.getPlugin("other") // NOT ALLOWED

// ❌ DON'T: Blocking on main thread
val data = api.fetchSync() // NOT ALLOWED - use coroutines

// ❌ DON'T: Skip Claude Skills
// ALWAYS use /mvikotlin, /decompose-mvikotlin, /store5-kotlin

// ❌ DON'T: Upgrade dependency versions without approval
// Use exact versions from libs.versions.toml
```

---

## Open Core Model

| Layer | License | Repository |
|-------|---------|------------|
| Core App | AGPLv3 | kimai-client |
| Plugin API | AGPLv3 | kimai-client |
| Premium Plugins | Proprietary | kimai-plugins (separate) |

**License validation stays in plugins, not Core.**

---

## Quick Reference

**Create new Store:** Use `/mvi-create-store` skill
**Create new Component:** Use `/decompose-mvikotlin` skill
**Add caching:** Use `/store5-kotlin` skill
**Review MVI code:** Use `/mvi-review` skill

**Build:** `./gradlew :kimai-desktop:run`
**Test:** `./gradlew test`
**Package:** `./gradlew :kimai-desktop:packageReleaseAppImage`

---

## Usage Guidelines

**For AI Agents:**

- Read this file before implementing any code
- Follow ALL rules exactly as documented
- Use Claude Skills (`/mvikotlin`, `/decompose-mvikotlin`, `/store5-kotlin`) when implementing
- When in doubt, prefer the more restrictive option
- Respect plugin boundaries and Open Core licensing

**For Humans:**

- Keep this file lean and focused on agent needs
- Update when technology stack changes
- Review quarterly for outdated rules
- Remove rules that become obvious over time

---

_Last Updated: 2025-12-27_

