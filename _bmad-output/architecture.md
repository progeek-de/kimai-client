---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
status: 'complete'
completedAt: '2025-12-27'
lastStep: 8
inputDocuments:
  - "_bmad-output/prd.md"
  - "_bmad-output/project-planning-artifacts/research/technical-plugin-architecture-research-2025-12-26.md"
  - "_bmad-output/project-planning-artifacts/research/market-time-tracking-research-2025-12-26.md"
  - "docs/index.md"
  - "docs/architecture.md"
  - "docs/state-management.md"
  - "docs/development-guide.md"
workflowType: 'architecture'
project_name: 'kimai-client'
user_name: 'Dmitri'
date: '2025-12-27'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**
72 requirements across 9 categories. The architecture must accommodate:
- **Core Extension**: Time tracking, sync, settings (existing patterns)
- **New Systems**: Plugin loading, license validation (new architecture)
- **First Plugin**: Task Integration as proof-of-concept

**Non-Functional Requirements:**
38 NFRs drive key architectural decisions:
- Performance: Plugin load < 2s, fault isolation required
- Security: Cryptographic license validation (ED25519)
- Reliability: 30-day offline grace period, zero data loss
- Cross-Platform: Feature parity across Windows/macOS/Linux

**Scale & Complexity:**
- Primary domain: Desktop Application with Plugin Architecture
- Complexity level: Medium-High (new architectural patterns)
- Estimated new components: ~15 modules/services
- Business model: Open Core (AGPLv3 base + proprietary plugins)

### Technical Constraints & Dependencies

**Existing Stack (Must Integrate):**

| Technology | Version | Role |
|------------|---------|------|
| Kotlin | 2.2.21 | Language |
| Compose Multiplatform | 1.9.3 | UI Framework |
| MVIKotlin | 4.3.0 | State Management |
| Decompose | 3.4.0 | Navigation |
| Koin | 4.1.1 | Dependency Injection |
| SQLDelight | 2.2.1 | Database |
| Arrow | - | Error Handling |

**Research-Driven Recommendations:**
- PF4J for plugin loading (proven at Netflix, Facebook)
- ServiceLoader + ClassLoader isolation for JAR discovery
- ED25519/ECDSA for license key cryptography
- Hexagonal Architecture for plugin boundaries

### Cross-Cutting Concerns Identified

| Concern | Impact | Architectural Pattern |
|---------|--------|----------------------|
| Plugin Lifecycle | All extensions | PluginManager with ClassLoader isolation |
| License Validation | Feature access | LicenseService with offline cache |
| Offline Capability | Sync, license, tasks | SQLDelight + Store5 caching |
| Error Isolation | Plugin failures | Fault boundaries + EventBus |

## Architecture Extension Strategy

### Brownfield Context

This is an extension of an existing Kotlin Multiplatform Desktop application, not a greenfield project. The base architecture is established and must be preserved.

### Module Structure Decision

**Open Source (kimai-client repository - AGPLv3):**

```
kimai-client/
├── kimai-desktop/           # App entry point, packaging
├── kimai-shared/            # Core time tracking, UI, business logic
├── kimai-swagger-client/    # Auto-generated Kimai API client
└── kimai-plugin-api/        # NEW: Plugin interfaces + loader
    ├── Plugin.kt            # Core plugin interface
    ├── PluginContext.kt     # API surface for plugins
    ├── PluginLoader.kt      # JAR discovery & loading (PF4J)
    ├── extensions/          # Extension point interfaces
    │   ├── SettingsExtension.kt
    │   ├── NavigationExtension.kt
    │   └── TimesheetActionExtension.kt
    └── license/
        └── LicenseValidator.kt  # Interface only, no implementation
```

**Closed Source (separate repositories - Proprietary):**

```
kimai-plugins/                    # Separate repo
├── license-common/               # Shared license validation
│   ├── Ed25519Validator.kt       # Cryptographic validation
│   ├── OfflineCache.kt           # 30-day grace period
│   └── PublicKey.kt              # Embedded public key
├── plugin-tasks/                 # Task Integration Plugin
│   ├── JiraConnector.kt
│   ├── GitHubConnector.kt
│   └── GitLabConnector.kt
└── plugin-reports/               # Reports Plugin (future)

kimai-license-server/             # Separate repo
└── License generation backend    # Private key protected
```

### Technology Decisions

**Plugin Loading Framework: PF4J**

- Rationale: Enterprise-proven (Netflix Spinnaker, Facebook Buck)
- Features: ClassLoader isolation, lifecycle management, ~100KB
- Kotlin support verified

**License Signing Algorithm: ED25519**

- Rationale: Modern, fast, small signatures (~64 bytes)
- Security: Asymmetric - public key in plugins, private key on server
- Offline: Signed license files work without network

**License Validation Location: In Plugins (not Core)**

- Rationale: Keeps core 100% open source
- Each plugin validates its own license
- Shared validation library in closed-source repo

### Dependency Flow

```
┌─────────────────────┐
│   Premium Plugin    │ (Closed Source JAR)
│   - TasksPlugin     │
└─────────┬───────────┘
          │ implements
          ▼
┌─────────────────────┐
│  kimai-plugin-api   │ (Open Source)
│  - Plugin interface │
│  - Extensions       │
└─────────┬───────────┘
          │ depends on
          ▼
┌─────────────────────┐
│   kimai-shared      │ (Open Source)
│   - Core models     │
│   - Repositories    │
└─────────────────────┘
```

## Core Architectural Decisions

### Decision Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Plugin Interface | Minimal + Extension Interfaces | Flexibility, plugins implement only what they need |
| Plugin Database | Separate SQLite per Plugin | Data isolation, independent backup/migration |
| Plugin-Core Communication | MVIKotlin Stores | Consistency with existing patterns |
| Plugin UI Integration | Decompose Components + Compose | Native integration with existing stack |
| Plugin Distribution (MVP) | Build-time inclusion | Simplified initial implementation |
| Plugin Distribution (Post-MVP) | In-App Plugin Store | Better user experience |

### Data Architecture

**Database Strategy: Separate Database per Plugin**

```
~/.kimai-client/
├── data/
│   ├── kimai.db                    # Core: timesheets, projects, activities
│   └── plugins/
│       ├── plugin-tasks.db         # Task Integration: Jira issues, mappings
│       └── plugin-reports.db       # Reports: cached aggregations
```

- Each plugin manages its own SQLDelight database
- Core database unchanged, plugins cannot modify core tables
- Independent migration paths per plugin
- Easy plugin removal (delete plugin + its database)

**Plugin Database Access:**

```kotlin
interface PluginContext {
    fun getDatabasePath(name: String): String  // Returns plugin-specific path
    // Note: Plugins create their own SqlDriver using the path returned
}
```

### Communication Architecture

**MVIKotlin-Based Communication**

Plugins communicate with Core through MVIKotlin Store pattern:

```kotlin
interface PluginContext {
    // Read-only access to Core stores (typed as Any to avoid circular deps)
    val timesheetListStore: Any  // Cast to TimesheetListStore in plugin
    val projectStore: Any        // Cast to ProjectStore in plugin
    val activityStore: Any       // Cast to ActivityStore in plugin
    val customerStore: Any       // Cast to CustomerStore in plugin

    // Coroutine contexts
    val mainContext: CoroutineContext
    val ioContext: CoroutineContext
}

// Plugin observes Core state
class TasksPlugin : Plugin {
    override fun init(context: PluginContext) {
        scope.launch {
            context.timesheetStore.states.collect { state ->
                // React to timesheet changes
                updateJiraWorklog(state.entries)
            }
        }
    }
}

// Plugin can dispatch intents to Core (if interface allows)
context.timesheetStore.accept(TimesheetStore.Intent.Refresh)
```

**Benefits:**

- No separate Event Bus needed
- Consistent with existing codebase patterns
- Type-safe communication
- Built-in state observation via StateFlow

### UI Integration Architecture

**Decompose + Compose Extension Points**

```kotlin
// Navigation Extension - Plugin adds screens
interface NavigationExtension {
    val navigationItem: NavigationItem
    fun createComponent(
        componentContext: ComponentContext,
        pluginContext: PluginContext
    ): Any  // Plugin's Decompose Component

    @Composable
    fun Content(component: Any)
}

// Settings Extension - Plugin adds settings panel
interface SettingsExtension {
    val settingsItem: SettingsItem

    @Composable
    fun SettingsContent(pluginContext: PluginContext)
}

// Timesheet Action Extension - Plugin adds entry actions
interface TimesheetActionExtension {
    val actions: List<TimesheetAction>
    fun execute(action: TimesheetAction, entry: Timesheet)
}
```

**Integration Flow:**

```
HomeComponent (Core)
├── TimesheetComponent
├── FormComponent
├── SettingsComponent
│   └── [Plugin Settings Panels]  ← SettingsExtension
└── [Plugin Navigation Tabs]      ← NavigationExtension
    └── TasksComponent (Plugin)
        └── JiraStore (Plugin MVIKotlin Store)
```

### Distribution Architecture

**MVP: Build-Time Integration**

```kotlin
// settings.gradle.kts
include(":kimai-desktop")
include(":kimai-shared")
include(":kimai-swagger-client")
include(":kimai-plugin-api")
include(":kimai-plugin-tasks")  // Plugin included at build time

// kimai-desktop/build.gradle.kts
dependencies {
    implementation(project(":kimai-shared"))
    implementation(project(":kimai-plugin-api"))

    // Plugins compiled into app
    implementation(project(":kimai-plugin-tasks"))
}
```

**Plugin Registration (Build-Time):**

```kotlin
// PluginRegistry.kt in kimai-desktop
object PluginRegistry {
    val plugins: List<Plugin> = listOf(
        TasksPlugin(),  // Registered at compile time
        // ReportsPlugin(),  // Future plugins
    )
}
```

**Post-MVP: Dynamic Loading**

```kotlin
// Future: PF4J-based dynamic loading
class PluginManager {
    fun loadFromDirectory(pluginsDir: Path): List<Plugin>
    fun installFromStore(pluginId: String): Plugin
    fun uninstall(pluginId: String)
}
```

### Security Decisions

**License Validation (in Plugin, not Core):**

```kotlin
// Each plugin validates its own license
class TasksPlugin : Plugin {
    override fun init(context: PluginContext) {
        val licenseResult = LicenseValidator.validate(
            licenseKey = context.getLicenseKey("tasks"),
            publicKey = TASKS_PUBLIC_KEY
        )

        if (!licenseResult.isValid) {
            throw LicenseException("Invalid or expired license")
        }
    }
}
```

**API Credentials (Plugin-Managed):**

- Jira/GitHub/GitLab OAuth tokens stored in plugin database
- Encrypted using platform secure storage
- Core provides encryption utilities via PluginContext

### Decision Impact Analysis

**Implementation Sequence:**

1. Create `kimai-plugin-api` module with interfaces
2. Define PluginContext with Store access
3. Implement Extension Points (Settings, Navigation, Actions)
4. Create `kimai-plugin-tasks` module
5. Integrate plugin into HomeComponent navigation
6. Add license validation to plugin

**Module Dependencies:**

```
kimai-desktop
├── kimai-shared
├── kimai-plugin-api
└── kimai-plugin-tasks
    └── kimai-plugin-api
        └── kimai-shared (models only)
```

## Implementation Patterns & Consistency Rules

### Claude Skills for Implementation

**CRITICAL: AI Agents MUST use these Claude Skills when implementing:**

| Skill | Use For | Invoke With |
|-------|---------|-------------|
| `store5-kotlin` | Store5 caching, offline-first data, MutableStore CRUD | `/store5-kotlin` |
| `decompose-mvikotlin` | Decompose navigation, MVIKotlin stores, component patterns | `/decompose-mvikotlin` |
| `mvikotlin` | MVIKotlin store creation, Intent/State/Label patterns | `/mvikotlin` |
| `mvi-create-store` | Creating new MVIKotlin stores | `/mvi-create-store` |
| `mvi-review` | Reviewing MVIKotlin implementations | `/mvi-review` |
| `kotlin-specialist` | Kotlin coroutines, KMP patterns, Kotlin best practices | Task agent: `kotlin-specialist` |

**When to use skills:**

- Creating new Stores → `/mvi-create-store` or `/mvikotlin`
- Adding navigation/components → `/decompose-mvikotlin`
- Implementing data caching → `/store5-kotlin`
- Reviewing MVI code → `/mvi-review`
- Complex Kotlin patterns → `kotlin-specialist` agent

### Existing Codebase Patterns (Mandatory)

**Package Structure:**

```
de.progeek.kimai.shared.
├── core/
│   ├── database/      # DataSources
│   ├── di/            # Koin modules
│   ├── models/        # Domain models
│   ├── network/       # API clients
│   ├── repositories/  # Repository implementations
│   └── storage/       # Local storage
└── ui/
    ├── components/    # Reusable UI components
    └── {feature}/     # Feature-specific UI
        ├── {Feature}Component.kt
        └── store/
            ├── {Feature}Store.kt
            └── {Feature}StoreFactory.kt
```

**Naming Conventions:**

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `TimesheetStore`, `HomeComponent` |
| Variables | camelCase | `timesheetId`, `isLoading` |
| Constants | SCREAMING_SNAKE_CASE | `DEFAULT_TIMEOUT`, `API_VERSION` |
| Packages | lowercase | `de.progeek.kimai.shared.ui.timesheet` |
| Files | PascalCase.kt | `TimesheetStore.kt`, `HomeComponent.kt` |

**MVIKotlin Store Pattern (use `/mvikotlin` skill):**

```kotlin
// Store naming: {Feature}Store
interface TimesheetStore : Store<Intent, State, Label> {

    sealed class Intent {
        data object Load : Intent()
        data class Delete(val id: Long) : Intent()
    }

    data class State(
        val isLoading: Boolean = false,
        val entries: List<Timesheet> = emptyList(),
        val error: String? = null
    )

    sealed class Label {
        data object NavigateBack : Label()
        data class ShowError(val message: String) : Label()
    }
}
```

**Decompose Component Pattern (use `/decompose-mvikotlin` skill):**

```kotlin
// Component naming: {Feature}Component
class HomeComponent(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val output: (Output) -> Unit
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getOrCreate {
        HomeStoreFactory(storeFactory).create()
    }

    val state: StateFlow<HomeStore.State> = store.stateFlow

    fun onIntent(intent: HomeStore.Intent) = store.accept(intent)

    private val navigation = StackNavigation<Configuration>()
    val childStack = childStack(...)

    sealed class Output {
        data object Logout : Output()
    }
}
```

**Store5 Caching Pattern (use `/store5-kotlin` skill):**

```kotlin
// Repository with Store5 for caching
class TimesheetRepository(
    private val api: TimesheetApi,
    private val dataSource: TimesheetDataSource
) {
    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { api.getTimesheets() },
        sourceOfTruth = SourceOfTruth.of(
            reader = { dataSource.observeAll() },
            writer = { _, value -> dataSource.insertAll(value) }
        )
    ).build()

    fun observeTimesheets(): Flow<StoreResponse<List<Timesheet>>> =
        store.stream(StoreRequest.cached(key = Unit, refresh = true))
}
```

**Error Handling Pattern:**

```kotlin
// Arrow Either for recoverable errors
suspend fun getTimesheets(): Either<RepositoryError, List<Timesheet>>

repository.getTimesheets()
    .fold(
        ifLeft = { error -> handleError(error) },
        ifRight = { data -> updateState(data) }
    )
```

### Plugin System Patterns (New)

**Plugin Package Structure:**

```
de.progeek.kimai.plugin.{pluginname}/
├── {PluginName}Plugin.kt          # Plugin entry point
├── di/
│   └── {PluginName}Module.kt      # Koin module
├── data/
│   ├── {Entity}DataSource.kt
│   └── {Entity}Repository.kt
├── models/
│   └── {Entity}.kt
└── ui/
    ├── {Feature}Component.kt
    └── store/
        ├── {Feature}Store.kt
        └── {Feature}StoreFactory.kt
```

**Plugin Naming Conventions:**

| Element | Convention | Example |
|---------|------------|---------|
| Plugin Class | `{Feature}Plugin` | `TasksPlugin` |
| Plugin ID | kebab-case string | `"task-integration"` |
| Plugin Package | `de.progeek.kimai.plugin.{name}` | `de.progeek.kimai.plugin.tasks` |
| Plugin Database | `plugin-{id}.db` | `plugin-tasks.db` |

**Plugin Interface Pattern:**

```kotlin
interface Plugin {
    val id: String           // Unique identifier (kebab-case)
    val name: String         // Display name
    val version: String      // SemVer format

    fun init(context: PluginContext)
    fun dispose()
}

class TasksPlugin : Plugin, NavigationExtension, SettingsExtension {
    override val id = "task-integration"
    override val name = "Task Integration"
    override val version = "1.0.0"
}
```

**Extension Point Patterns:**

```kotlin
interface NavigationExtension {
    val navigationItem: NavigationItem
    fun createComponent(
        componentContext: ComponentContext,
        pluginContext: PluginContext
    ): Any

    @Composable
    fun Content(component: Any)
}

interface SettingsExtension {
    val settingsItem: SettingsItem

    @Composable
    fun SettingsContent(pluginContext: PluginContext)
}

interface TimesheetActionExtension {
    val actions: List<TimesheetAction>
    fun execute(action: TimesheetAction, entry: Timesheet, context: PluginContext)
}
```

**Plugin Error Pattern:**

```kotlin
sealed class PluginError {
    data class LicenseInvalid(val reason: String) : PluginError()
    data class LicenseExpired(val expiredAt: Instant) : PluginError()
    data class ApiError(val code: Int, val message: String) : PluginError()
    data class AuthenticationFailed(val service: String) : PluginError()
    data class ConfigurationMissing(val field: String) : PluginError()
}

suspend fun getJiraIssues(): Either<PluginError, List<JiraIssue>>
```

### Enforcement Guidelines

**All AI Agents MUST:**

1. **Use Claude Skills** for Store5, MVIKotlin, Decompose patterns
2. Follow existing MVIKotlin Store pattern for all new stores
3. Use Decompose Component pattern for all new screens
4. Use Arrow Either for all repository error handling
5. Place files in correct package structure
6. Follow naming conventions exactly
7. Use Koin for all dependency injection

**All Plugin Code MUST:**

1. Implement Plugin interface with correct id/name/version
2. Use PluginContext for all Core interactions
3. Create separate database file per plugin
4. Handle license validation in plugin init()
5. Clean up resources in dispose()
6. Follow same patterns as Core code

### Anti-Patterns (DO NOT)

```kotlin
// ❌ DON'T: Direct Core database access
val coreDb = context.getCoreDatabase() // NOT ALLOWED

// ❌ DON'T: Static/global state
object BadPluginState { var issues = mutableListOf() } // NOT ALLOWED

// ❌ DON'T: Direct plugin-to-plugin communication
val otherPlugin = context.getPlugin("other") // NOT ALLOWED

// ❌ DON'T: Blocking operations on main thread
val data = api.fetchSync() // NOT ALLOWED - use coroutines

// ❌ DON'T: Implement patterns without consulting Claude Skills
// ALWAYS use /mvikotlin, /decompose-mvikotlin, /store5-kotlin
```

## Project Structure & Boundaries

### Complete Project Directory Structure

**Open Source Repository (kimai-client - AGPLv3):**

```
kimai-client/
├── README.md
├── LICENSE                           # AGPLv3
├── CHANGELOG.md
├── CLAUDE.md                         # AI agent instructions
├── api-docs.json                     # Kimai OpenAPI spec
├── build.gradle.kts                  # Root build configuration
├── settings.gradle.kts               # Module includes
├── gradle.properties                 # Build properties
├── generate-client.sh                # OpenAPI client generator
├── build-linux.sh                    # Linux packaging
├── build-windows.sh                  # Windows packaging
├── build-macos.sh                    # macOS packaging
│
├── .github/
│   └── workflows/
│       ├── build.yml                 # CI build & test
│       └── release.yml               # Release automation
│
├── gradle/
│   ├── libs.versions.toml            # Version catalog
│   └── wrapper/
│
├── kimai-desktop/                    # Desktop JVM entry point
│   ├── build.gradle.kts
│   ├── rules.pro                     # ProGuard rules
│   └── src/
│       └── jvmMain/
│           ├── kotlin/de/progeek/kimai/desktop/
│           │   ├── Main.kt           # Application entry
│           │   └── PluginRegistry.kt # Plugin registration (MVP)
│           └── resources/
│               └── icons/            # App icons
│
├── kimai-shared/                     # Core multiplatform module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/de/progeek/kimai/shared/
│       │       ├── core/
│       │       │   ├── database/
│       │       │   │   ├── DataSourceProvider.kt
│       │       │   │   ├── ActivityDataSource.kt
│       │       │   │   ├── CustomerDataSource.kt
│       │       │   │   ├── ProjectDataSource.kt
│       │       │   │   ├── TimesheetDataSource.kt
│       │       │   │   └── SettingsDataSource.kt
│       │       │   ├── di/
│       │       │   │   ├── CommonModule.kt
│       │       │   │   ├── NetworkModule.kt
│       │       │   │   └── RepositoryModule.kt
│       │       │   ├── error/
│       │       │   │   └── RepositoryError.kt
│       │       │   ├── mapper/
│       │       │   │   ├── ActivityMapper.kt
│       │       │   │   ├── CustomerMapper.kt
│       │       │   │   ├── ProjectMapper.kt
│       │       │   │   └── TimesheetMapper.kt
│       │       │   ├── models/
│       │       │   │   ├── Activity.kt
│       │       │   │   ├── Customer.kt
│       │       │   │   ├── Project.kt
│       │       │   │   ├── Timesheet.kt
│       │       │   │   └── Settings.kt
│       │       │   ├── network/
│       │       │   │   └── KimaiClient.kt
│       │       │   ├── repositories/
│       │       │   │   ├── ActivityRepository.kt
│       │       │   │   ├── AuthRepository.kt
│       │       │   │   ├── CredentialsRepository.kt
│       │       │   │   ├── CustomerRepository.kt
│       │       │   │   ├── ProjectRepository.kt
│       │       │   │   ├── SettingsRepository.kt
│       │       │   │   └── TimesheetRepository.kt
│       │       │   └── storage/
│       │       │       └── SecureStorage.kt
│       │       ├── ui/
│       │       │   ├── components/
│       │       │   │   ├── button/
│       │       │   │   ├── dropdown/
│       │       │   │   ├── loading/
│       │       │   │   └── timer/
│       │       │   ├── form/
│       │       │   │   ├── FormComponent.kt
│       │       │   │   └── store/
│       │       │   │       ├── FormStore.kt
│       │       │   │       └── FormStoreFactory.kt
│       │       │   ├── home/
│       │       │   │   ├── HomeComponent.kt
│       │       │   │   └── store/
│       │       │   │       ├── HomeStore.kt
│       │       │   │       └── HomeStoreFactory.kt
│       │       │   ├── login/
│       │       │   │   ├── LoginComponent.kt
│       │       │   │   └── store/
│       │       │   ├── root/
│       │       │   │   ├── RootComponent.kt
│       │       │   │   └── store/
│       │       │   ├── settings/
│       │       │   │   ├── SettingsComponent.kt
│       │       │   │   └── store/
│       │       │   ├── theme/
│       │       │   │   ├── Colors.kt
│       │       │   │   └── Theme.kt
│       │       │   └── timesheet/
│       │       │       ├── TimesheetComponent.kt
│       │       │       └── store/
│       │       └── utils/
│       │           └── Extensions.kt
│       ├── commonTest/
│       │   └── kotlin/
│       ├── jvmMain/
│       │   └── kotlin/de/progeek/kimai/shared/
│       │       └── PlatformSpecific.jvm.kt
│       └── sqldelight/
│           └── de/progeek/kimai/shared/
│               ├── KimaiDatabase.sq
│               ├── Activity.sq
│               ├── Customer.sq
│               ├── Project.sq
│               └── Timesheet.sq
│
├── kimai-swagger-client/             # Auto-generated API client
│   ├── build.gradle.kts
│   └── src/
│       └── commonMain/
│           └── kotlin/de/progeek/kimai/openapi/
│               ├── apis/             # Generated endpoints
│               ├── auth/             # Authentication
│               ├── infrastructure/   # HTTP client
│               └── models/           # API models
│
├── kimai-plugin-api/                 # NEW: Plugin interfaces (Open Source)
│   ├── build.gradle.kts
│   └── src/
│       └── commonMain/
│           └── kotlin/de/progeek/kimai/plugin/api/
│               ├── Plugin.kt                     # Core plugin interface
│               ├── PluginContext.kt              # API surface for plugins
│               ├── PluginManager.kt              # Plugin lifecycle management
│               ├── extensions/
│               │   ├── NavigationExtension.kt    # Add navigation tabs
│               │   ├── SettingsExtension.kt      # Add settings panels
│               │   └── TimesheetActionExtension.kt # Add entry actions
│               ├── models/
│               │   ├── NavigationItem.kt
│               │   ├── SettingsItem.kt
│               │   └── TimesheetAction.kt
│               ├── license/
│               │   └── LicenseValidator.kt       # Interface only
│               └── errors/
│                   └── PluginError.kt
│
└── docs/                             # Generated documentation
    ├── index.md
    ├── architecture.md
    ├── development-guide.md
    ├── state-management.md
    └── component-inventory.md
```

**Closed Source Repository (kimai-plugins - Proprietary):**

```
kimai-plugins/                        # Separate repository
├── README.md
├── LICENSE                           # Proprietary
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
│
├── license-common/                   # Shared license validation
│   ├── build.gradle.kts
│   └── src/
│       └── commonMain/
│           └── kotlin/de/progeek/kimai/license/
│               ├── Ed25519Validator.kt           # Cryptographic validation
│               ├── LicenseFile.kt                # License file parsing
│               ├── OfflineCache.kt               # 30-day grace period
│               └── LicenseError.kt
│
├── plugin-tasks/                     # Task Integration Plugin
│   ├── build.gradle.kts
│   └── src/
│       └── commonMain/
│           └── kotlin/de/progeek/kimai/plugin/tasks/
│               ├── TasksPlugin.kt                # Plugin entry point
│               ├── di/
│               │   └── TasksModule.kt            # Koin module
│               ├── data/
│               │   ├── TaskDataSource.kt
│               │   ├── JiraMappingDataSource.kt
│               │   └── TaskRepository.kt
│               ├── models/
│               │   ├── Task.kt
│               │   ├── JiraIssue.kt
│               │   ├── GitHubIssue.kt
│               │   └── GitLabIssue.kt
│               ├── network/
│               │   ├── JiraClient.kt
│               │   ├── GitHubClient.kt
│               │   └── GitLabClient.kt
│               └── ui/
│                   ├── tasks/
│                   │   ├── TasksComponent.kt
│                   │   └── store/
│                   │       ├── TasksStore.kt
│                   │       └── TasksStoreFactory.kt
│                   └── settings/
│                       └── TasksSettingsContent.kt
│
└── plugin-reports/                   # Reports Plugin (future)
    ├── build.gradle.kts
    └── src/
        └── commonMain/
            └── kotlin/de/progeek/kimai/plugin/reports/
                ├── ReportsPlugin.kt
                ├── di/
                ├── data/
                ├── models/
                └── ui/
```

### Architectural Boundaries

**API Boundaries:**

| Boundary | Location | Purpose |
|----------|----------|---------|
| Kimai Server API | `kimai-swagger-client/` | Auto-generated client for Kimai REST API |
| Plugin API | `kimai-plugin-api/` | Contract between Core and Plugins |
| License Server API | External | License validation endpoint |
| Jira/GitHub/GitLab APIs | `plugin-tasks/network/` | External task provider integrations |

**Component Boundaries:**

| Layer | Boundary | Communication |
|-------|----------|---------------|
| Core ↔ Plugin | `PluginContext` interface | Read-only Store access, extension points |
| Plugin ↔ Plugin | **FORBIDDEN** | Plugins cannot communicate directly |
| UI ↔ Store | MVIKotlin Intent/State | Unidirectional data flow |
| Repository ↔ DataSource | Arrow Either | Error handling boundary |

**Data Boundaries:**

| Database | Location | Owner |
|----------|----------|-------|
| `kimai.db` | `~/.kimai-client/data/` | Core (kimai-shared) |
| `plugin-tasks.db` | `~/.kimai-client/data/plugins/` | Task Integration Plugin |
| `plugin-reports.db` | `~/.kimai-client/data/plugins/` | Reports Plugin |

**License Validation Boundary:**

```
┌─────────────────────────────────────────────────────┐
│                    Core (Open Source)                │
│  • No license validation logic                       │
│  • LicenseValidator is interface only               │
└─────────────────────────────────────────────────────┘
                         │
                         ▼ implements
┌─────────────────────────────────────────────────────┐
│                 Plugin (Closed Source)               │
│  • Ed25519Validator implementation                  │
│  • Public key embedded                              │
│  • Validates own license in init()                  │
└─────────────────────────────────────────────────────┘
```

### Requirements to Structure Mapping

**FR Categories to Modules:**

| FR Category | Primary Module | Secondary |
|-------------|----------------|-----------|
| Time Tracking Core (FR1-10) | `kimai-shared/ui/timesheet/` | `kimai-shared/core/repositories/` |
| Plugin System (FR11-20) | `kimai-plugin-api/` | `kimai-desktop/PluginRegistry.kt` |
| License & Monetization (FR21-29) | `kimai-plugins/license-common/` | Plugin `init()` methods |
| Task Integration (FR30-38) | `kimai-plugins/plugin-tasks/` | - |
| Data Sync & Offline (FR39-46) | `kimai-shared/core/repositories/` | SQLDelight schemas |
| User Settings (FR47-53) | `kimai-shared/ui/settings/` | `kimai-shared/core/storage/` |
| Onboarding (FR54-59) | `kimai-shared/ui/login/` | - |
| System Integration (FR60-66) | `kimai-desktop/Main.kt` | Platform-specific code |
| Error Handling (FR67-72) | `kimai-shared/core/error/` | All repositories |

**Cross-Cutting Concerns:**

| Concern | Files |
|---------|-------|
| **Dependency Injection** | `kimai-shared/core/di/*.kt`, Plugin `di/` folders |
| **Error Handling** | `kimai-shared/core/error/RepositoryError.kt`, `kimai-plugin-api/errors/PluginError.kt` |
| **Offline Caching** | All `*Repository.kt` with Store5, `license-common/OfflineCache.kt` |
| **State Management** | All `*/store/` directories |
| **Navigation** | `HomeComponent.kt` + `NavigationExtension.kt` |

### Integration Points

**Plugin-Core Communication:**

```kotlin
// PluginContext provided to plugins on init
interface PluginContext {
    // Read-only Store access (typed as Any to avoid circular deps)
    val timesheetListStore: Any  // Cast to TimesheetListStore in plugin
    val projectStore: Any        // Cast to ProjectStore in plugin
    val activityStore: Any       // Cast to ActivityStore in plugin
    val customerStore: Any       // Cast to CustomerStore in plugin

    // Database access (plugins create their own SqlDriver)
    fun getDatabasePath(name: String): String

    // Coroutine contexts
    val mainContext: CoroutineContext
    val ioContext: CoroutineContext

    // License key retrieval
    fun getLicenseKey(pluginId: String): String?
}
```

**Extension Point Integration:**

```
HomeComponent
├── TimesheetComponent
├── FormComponent
├── SettingsComponent
│   ├── GeneralSettings (Core)
│   └── [SettingsExtension panels] ← Plugins inject here
└── [NavigationExtension tabs]     ← Plugins inject here
    └── TasksComponent (from plugin-tasks)
```

**Data Flow:**

```
User Action
    │
    ▼
Intent → Store.Executor
    │
    ▼
Repository (Core or Plugin)
    │
    ├─► Network (API call)
    │       │
    │       ▼
    │   Mapper → Model
    │       │
    │       ▼
    └─► DataSource (SQLDelight)
            │
            ▼
        Store.State
            │
            ▼
        UI Update
```

### File Organization Patterns

**Configuration Files:**

| File | Purpose |
|------|---------|
| `gradle/libs.versions.toml` | Centralized dependency versions |
| `gradle.properties` | Kotlin/JVM build properties |
| `settings.gradle.kts` | Module includes |
| `*/build.gradle.kts` | Module-specific configuration |
| `kimai-desktop/rules.pro` | ProGuard optimization rules |

**Test Organization:**

```
kimai-shared/src/commonTest/kotlin/de/progeek/kimai/shared/
├── core/
│   └── repositories/
│       ├── TimesheetRepositoryTest.kt
│       └── ProjectRepositoryTest.kt
└── ui/
    └── timesheet/
        └── store/
            └── TimesheetStoreTest.kt

kimai-plugins/plugin-tasks/src/commonTest/kotlin/
└── de/progeek/kimai/plugin/tasks/
    ├── data/
    │   └── TaskRepositoryTest.kt
    └── ui/store/
        └── TasksStoreTest.kt
```

### Development Workflow Integration

**Build Commands:**

```bash
# Run desktop app (development)
./gradlew :kimai-desktop:run

# Run tests
./gradlew test

# Build release packages
./gradlew :kimai-desktop:packageReleaseAppImage  # Linux
./gradlew :kimai-desktop:packageReleaseMsi       # Windows
./gradlew :kimai-desktop:packageReleaseDmg       # macOS

# Regenerate API client
./generate-client.sh
```

**Plugin Development (MVP - Build-time):**

```bash
# In kimai-plugins repo
./gradlew :plugin-tasks:build

# Copy JAR to kimai-client for build-time inclusion
# Or use composite build in settings.gradle.kts
```

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**

| Decision Area | Status | Notes |
|---------------|--------|-------|
| Kotlin 2.2.21 + Compose 1.9.3 + MVIKotlin 4.3.0 | ✅ | Compatible versions, proven stack |
| Decompose 3.4.0 + Koin 4.1.1 | ✅ | Works with MVIKotlin |
| SQLDelight 2.2.1 + Store5 | ✅ | Standard KMP caching pattern |
| PF4J + ED25519 Licensing | ✅ | JVM-compatible, no conflicts |
| Build-time plugin integration (MVP) | ✅ | Simplifies initial implementation |

**No version conflicts detected.**

**Pattern Consistency:**

| Pattern | Core | Plugin | Alignment |
|---------|------|--------|-----------|
| MVIKotlin Store | ✅ Used | ✅ Same pattern | Consistent |
| Decompose Navigation | ✅ Used | ✅ Via Extension | Consistent |
| Arrow Either | ✅ Used | ✅ Same pattern | Consistent |
| Koin DI | ✅ Used | ✅ Same pattern | Consistent |
| Store5 Caching | ✅ Used | ✅ Same pattern | Consistent |

**All patterns align across Core and Plugin code.**

**Structure Alignment:**

- Project structure supports Open Core model (AGPLv3 core, proprietary plugins)
- Separate databases per plugin enforces data isolation
- Extension points enable clean plugin UI integration
- PluginContext API enforces communication boundaries

### Requirements Coverage Validation ✅

**Functional Requirements Coverage (72 FRs):**

| FR Category | Count | Covered | Module |
|-------------|-------|---------|--------|
| Time Tracking Core (FR1-10) | 10 | ✅ 10/10 | kimai-shared |
| Plugin System (FR11-20) | 10 | ✅ 10/10 | kimai-plugin-api |
| License & Monetization (FR21-29) | 9 | ✅ 9/9 | license-common + plugins |
| Task Integration (FR30-38) | 9 | ✅ 9/9 | plugin-tasks |
| Data Sync & Offline (FR39-46) | 8 | ✅ 8/8 | kimai-shared repositories |
| User Settings (FR47-53) | 7 | ✅ 7/7 | kimai-shared/ui/settings |
| Onboarding (FR54-59) | 6 | ✅ 6/6 | kimai-shared/ui/login |
| System Integration (FR60-66) | 7 | ✅ 7/7 | kimai-desktop |
| Error Handling (FR67-72) | 6 | ✅ 6/6 | core/error + repositories |

**100% FR coverage achieved.**

**Non-Functional Requirements Coverage (38 NFRs):**

| NFR Category | Count | Covered | Approach |
|--------------|-------|---------|----------|
| Performance (NFR-P1-7) | 7 | ✅ 7/7 | Async patterns, Store5 caching |
| Security (NFR-S1-7) | 7 | ✅ 7/7 | ED25519, AES-256, TLS, secure storage |
| Reliability (NFR-R1-6) | 6 | ✅ 6/6 | SQLite offline, 30-day grace, fault isolation |
| Integration (NFR-I1-7) | 7 | ✅ 7/7 | Auto-generated clients, OAuth 2.0 |
| Accessibility (NFR-A1-5) | 5 | ✅ 5/5 | Compose accessibility support |
| Cross-Platform (NFR-C1-6) | 6 | ✅ 6/6 | KMP, bundled JRE, responsive UI |

**100% NFR coverage achieved.**

### Implementation Readiness Validation ✅

**Decision Completeness:**

| Aspect | Status | Evidence |
|--------|--------|----------|
| Technology versions specified | ✅ | All versions in table |
| Plugin interface defined | ✅ | Plugin.kt, PluginContext.kt |
| Extension points defined | ✅ | 3 extension interfaces |
| Communication patterns specified | ✅ | MVIKotlin Store-based |
| Database strategy defined | ✅ | Separate DB per plugin |
| License strategy defined | ✅ | ED25519, plugin-side validation |

**Structure Completeness:**

| Aspect | Status | Evidence |
|--------|--------|----------|
| Complete project tree | ✅ | Both repos fully specified |
| All modules defined | ✅ | 4 core + 3 plugin modules |
| File locations specified | ✅ | Package paths included |
| Test structure defined | ✅ | Test directory layout |

**Pattern Completeness:**

| Pattern | Core Example | Plugin Example | Complete |
|---------|--------------|----------------|----------|
| MVIKotlin Store | TimesheetStore | TasksStore | ✅ |
| Decompose Component | HomeComponent | TasksComponent | ✅ |
| Repository + Arrow | TimesheetRepository | TaskRepository | ✅ |
| Extension implementation | - | TasksPlugin implements 3 | ✅ |

**Claude Skills documented for AI agent guidance.**

### Gap Analysis Results

**Critical Gaps:** None identified ✅

**Important Gaps (Addressed):**

| Gap | Resolution |
|-----|------------|
| Plugin-to-Core Store exposure | PluginContext provides read-only access |
| License key storage | SecureStorage via PluginContext |
| Plugin compatibility checking | Version check on init() |

**Nice-to-Have Gaps (Future):**

| Gap | Phase |
|-----|-------|
| Dynamic plugin loading (PF4J) | Post-MVP |
| In-App Plugin Store | Post-MVP |
| Mobile plugin support | Phase 3 |
| Plugin marketplace | Future Vision |

### Architecture Completeness Checklist

**✅ Requirements Analysis**

- [x] Project context thoroughly analyzed
- [x] Scale and complexity assessed
- [x] Technical constraints identified
- [x] Cross-cutting concerns mapped

**✅ Architectural Decisions**

- [x] Critical decisions documented with versions
- [x] Technology stack fully specified
- [x] Integration patterns defined
- [x] Performance considerations addressed

**✅ Implementation Patterns**

- [x] Naming conventions established
- [x] Structure patterns defined
- [x] Communication patterns specified
- [x] Process patterns documented
- [x] Claude Skills for AI agents documented

**✅ Project Structure**

- [x] Complete directory structure defined
- [x] Component boundaries established
- [x] Integration points mapped
- [x] Requirements to structure mapping complete

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION

**Confidence Level:** HIGH

**Key Strengths:**

- Proven technology stack with compatible versions
- Clear separation between Open Source core and proprietary plugins
- Consistent patterns across all code (MVIKotlin, Decompose, Arrow)
- Comprehensive Claude Skills documentation for AI agents
- 100% requirements coverage (72 FRs, 38 NFRs)

**Areas for Future Enhancement:**

- Dynamic plugin loading (PF4J) in Post-MVP
- In-App Plugin Store for better UX
- Mobile platform support (Phase 3)
- Plugin developer marketplace (Future Vision)

### Implementation Handoff

**AI Agent Guidelines:**

1. **Use Claude Skills:** Always invoke `/mvikotlin`, `/decompose-mvikotlin`, `/store5-kotlin` when implementing
2. **Follow Patterns:** Use exact patterns documented in Implementation Patterns section
3. **Respect Boundaries:** Plugins communicate only through PluginContext
4. **Separate Databases:** Each plugin gets its own SQLite database
5. **License in Plugin:** License validation logic stays in closed-source plugins

**First Implementation Priority:**

1. Create `kimai-plugin-api` module with Plugin interface and PluginContext
2. Define extension point interfaces (Navigation, Settings, TimesheetAction)
3. Implement PluginManager in kimai-desktop
4. Create example plugin to validate architecture

## Architecture Completion Summary

### Workflow Completion

**Architecture Decision Workflow:** COMPLETED ✅
**Total Steps Completed:** 8
**Date Completed:** 2025-12-27
**Document Location:** `_bmad-output/architecture.md`

### Final Architecture Deliverables

**📋 Complete Architecture Document**

- All architectural decisions documented with specific versions
- Implementation patterns ensuring AI agent consistency
- Complete project structure with all files and directories
- Requirements to architecture mapping
- Validation confirming coherence and completeness

**🏗️ Implementation Ready Foundation**

- 6 core architectural decisions made
- 8 implementation patterns defined
- 7 module components specified (4 core + 3 plugin)
- 110 requirements fully supported (72 FRs + 38 NFRs)

**📚 AI Agent Implementation Guide**

- Technology stack with verified versions (Kotlin 2.2.21, Compose 1.9.3, MVIKotlin 4.3.0)
- Consistency rules that prevent implementation conflicts
- Project structure with clear boundaries (Open Core model)
- Integration patterns and communication standards (MVIKotlin Stores)
- Claude Skills documentation for implementation guidance

### Development Sequence

1. Create `kimai-plugin-api` module with Plugin interface
2. Define PluginContext with Store access
3. Implement extension point interfaces
4. Create PluginManager in kimai-desktop
5. Build first plugin (Task Integration) following established patterns
6. Validate architecture with working plugin

### Quality Assurance Checklist

**✅ Architecture Coherence**

- [x] All decisions work together without conflicts
- [x] Technology choices are compatible
- [x] Patterns support the architectural decisions
- [x] Structure aligns with all choices

**✅ Requirements Coverage**

- [x] All 72 functional requirements are supported
- [x] All 38 non-functional requirements are addressed
- [x] Cross-cutting concerns are handled
- [x] Integration points are defined

**✅ Implementation Readiness**

- [x] Decisions are specific and actionable
- [x] Patterns prevent agent conflicts
- [x] Structure is complete and unambiguous
- [x] Claude Skills documented for AI agents

---

**Architecture Status:** READY FOR IMPLEMENTATION ✅

**Next Phase:** Create Epics & Stories from PRD, then begin implementation

**Document Maintenance:** Update this architecture when major technical decisions are made during implementation.

