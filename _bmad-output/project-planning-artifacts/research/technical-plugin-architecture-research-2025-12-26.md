---
stepsCompleted: [1, 2, 3]
inputDocuments: []
workflowType: 'research'
lastStep: 1
research_type: 'technical'
research_topic: 'Plugin Architecture for Kotlin Multiplatform Time Tracker'
research_goals: 'Understand plugin loading patterns, license key systems, and extension point architectures'
user_name: 'Dmitri'
date: '2025-12-26'
web_research_enabled: true
source_verification: true
---

# Research Report: Technical - Plugin Architecture

**Date:** 2025-12-26
**Author:** Dmitri
**Research Type:** Technical

---

## Research Overview

**Topic:** Plugin Architecture for Kotlin Multiplatform Time Tracker

**Goals:**
- Analyze plugin loading patterns (JAR/AAR at runtime)
- Study license key implementation strategies
- Examine extension point architectures from successful projects
- Identify best practices for KMP plugin systems

**Reference Projects:**
- IntelliJ IDEA Plugin System
- VS Code Extensions
- Obsidian Plugins
- Gradle Plugin Architecture

---

## Technical Research Scope Confirmation

**Research Topic:** Plugin Architecture for Kotlin Multiplatform Time Tracker
**Research Goals:** Understand plugin loading patterns, license key systems, and extension point architectures

**Technical Research Scope:**

- Architecture Analysis - design patterns, frameworks, system architecture
- Implementation Approaches - development methodologies, coding patterns
- Technology Stack - languages, frameworks, tools, platforms
- Integration Patterns - APIs, protocols, interoperability
- Performance Considerations - scalability, optimization, patterns

**Reference Projects:**
- IntelliJ IDEA Plugin System
- VS Code Extensions
- Obsidian Plugins
- Gradle Plugin Architecture
- Android Dynamic Feature Modules

**Research Methodology:**

- Current web data with rigorous source verification
- Multi-source validation for critical technical claims
- Confidence level framework for uncertain information
- Comprehensive technical coverage with architecture-specific insights

**Scope Confirmed:** 2025-12-26

---

## Technology Stack Analysis

### Plugin Framework Options for JVM/Kotlin

#### PF4J - Plugin Framework for Java (Recommended)

**Overview:**
PF4J is an open-source (Apache license), lightweight (~100 KB) plugin framework for Java with minimal dependencies. It's enterprise-proven and powers Netflix Spinnaker and Facebook Buck.

**Key Features:**
- Mark any interface/abstract class as extension point with `ExtensionPoint` marker
- Use `@Extension` annotation to specify implementations
- Each plugin loaded in separate ClassLoader to avoid conflicts
- `PluginManager` handles all plugin lifecycle (loading, starting, stopping)

**Kotlin Support:**
- Native Kotlin support with demo projects available
- Fix for StackOverflow on Kotlin classes (v3.13.0)
- Gradle Kotlin DSL examples available

**Source:** [PF4J Official](https://pf4j.org/), [PF4J Kotlin Docs](https://pf4j.org/doc/kotlin.html), [GitHub](https://github.com/pf4j/pf4j)

#### Java ServiceLoader (Built-in)

**Overview:**
Standard JVM mechanism for service discovery and loading.

**Configuration:**
- Requires `META-INF/services/` folder in classpath
- Text file named after interface containing implementation class names

**Limitations:**
- No built-in ClassLoader isolation
- No lifecycle management
- Basic discovery only

**Source:** [Oracle ServiceLoader Docs](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)

### ClassLoader Best Practices

#### Isolating ClassLoaders

**Problem:** "Dependency hell" - conflicting dependencies between plugins

**Solution:** Each plugin runs in its own ClassLoader with isolated dependencies

**Strategies:**
| Strategy | Order | Use Case |
|----------|-------|----------|
| PDA (Parent-Last) | Plugin → Dependencies → Application | Default, recommended |
| APD (Parent-First) | Application → Plugin → Dependencies | Legacy compatibility |

**Source:** [Adevinta Tech Blog](https://adevinta.com/techblog/java-plugins-with-isolating-class-loaders/), [PF4J Class Loading](https://pf4j.org/doc/class-loading.html)

#### Memory Management

**Critical:** ClassLoaders only garbage collected when ALL instances from that ClassLoader can be GC'd.

**Best Practice:** Clear all references to plugin instances before unloading.

### IntelliJ IDEA Extension Point Architecture

**Model:**
- Extensions are implementations of interfaces registered in `plugin.xml`
- Extension points defined via `interface` or `beanClass` attribute
- Runtime access via `ExtensionPointName` with FQ name

**Definition Types:**
```xml
<!-- Interface-based -->
<extensionPoint name="myExtension" interface="com.example.MyInterface"/>

<!-- Bean-based with attributes -->
<extensionPoint name="myExtension" beanClass="com.example.MyBean"/>
```

**2024 Updates:**
- Kotlin 2.x required for IntelliJ 2025.1+
- `action` and `actionId` resolve to all registered actions (2024.3+)
- Deprecation annotations properly highlighted

**Source:** [IntelliJ Extensions](https://plugins.jetbrains.com/docs/intellij/plugin-extensions.html), [Extension Points](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html)

### Android Dynamic Feature Modules

**Overview:**
Separate features from base module, download on-demand after initial install.

**Plugin Configuration:**
```groovy
plugins {
    id 'com.android.dynamic-feature'
}
```

**AAR in Dynamic Modules:**
- Library .aar can be included in on-demand module
- Access library classes via Kotlin reflection after module install
- Base module communicates via interfaces (dynamic implements)

**Limitations:**
- Base module cannot directly use dynamic module code
- Reflection required for indirect access (performance cost)
- Modules >10MB require user permission dialog

**Best Practice:** Keep dynamic modules small, use interface contracts.

**Source:** [Android Feature Delivery](https://developer.android.com/guide/playcore/feature-delivery), [Medium Deep Dive](https://medium.com/@suresh.online1/deep-dive-into-dynamic-feature-modules-on-android-part-3-ae42af93bf67)

### License Key Implementation

#### Cryptographic Best Practices

**Use Asymmetric Cryptography:**
- Sign license with private key
- Validate with public key (can be embedded in app)
- Third party cannot generate keys even with reverse engineering

**Recommended Algorithms:**
| Algorithm | Recommendation |
|-----------|----------------|
| ED25519_SIGN | Overall best when available |
| ECDSA_P256_SIGN | NIST FIPS compliance |
| RSA | Larger keys, still secure |

**Why ECC over RSA:**
- Much smaller key sizes
- Smaller signatures (better for license keys)
- Equally cryptographically strong

**Source:** [Keygen Docs](https://keygen.sh/docs/validating-licenses/), [SoftActivate](https://www.softactivate.com/licensekeygeneration)

#### Online vs. Offline Validation

| Mode | Pros | Cons |
|------|------|------|
| **Online** | Real-time revocation, usage tracking | Requires internet |
| **Offline** | Works anywhere, faster | Cannot revoke easily |
| **Hybrid** | Best of both | More complex |

**Offline Implementation:**
- License key contains signed dataset + signature
- Parse and verify cryptographically using public key
- Key carries proof of authenticity within itself

**Source:** [Keygen Offline Licenses](https://keygen.sh/docs/choosing-a-licensing-model/offline-licenses/), [Ayende Blog](https://ayende.com/blog/199617-A/using-encryption-to-verify-a-license-key)

#### Handling Failures

**Grace Period:** If offline, allow temporary usage before requiring validation

**Expiration:** Prompt renewal, don't immediately exit

**Machine Binding:** Use device fingerprint to prevent key sharing

---

## Integration Patterns Analysis

### Extension Point Design Patterns

#### Core Patterns for Plugin Systems

| Pattern | Use Case | Example |
|---------|----------|---------|
| **Observer/Mediator** | Communication between independent plugins | WordPress, Drupal hooks |
| **Strategy** | Plugin changes behavior of functionality | Payment processors |
| **Decorator** | Plugin wraps/enhances existing features | Logging, caching |
| **Chain of Responsibility** | Multiple plugins process sequentially | Filters, validators |
| **Template Method** | Plugin overrides application behavior | Custom renderers |

**Source:** [OpenStack Stevedore Docs](https://docs.openstack.org/stevedore/latest/user/essays/pycon2013.html), [Plugin Architecture Medium](https://medium.com/omarelgabrys-blog/plug-in-architecture-dec207291800)

#### Plugin Architecture Components

**Core System:**
- Plugin Loader/Manager
- Extension Point Registry
- Event Bus
- Configuration System

**Plugin Module:**
- Independent, standalone
- Specialized processing
- Minimal dependencies on other plugins

**Best Practice:** Plugins should hook into the core using the same mechanism the core uses for its own features (like Symfony bundles).

**Source:** [Codementor Design Patterns](https://www.codementor.io/@goetas/modular-application-architecture-considerations-on-design-patterns-flmx1m731)

### Event System / Publish-Subscribe

#### Pattern Overview

Publishers send messages without knowing receivers. Subscribers express interest in message classes without knowing publishers.

**Components:**
```
Event Producer → Event Bus/Broker → Event Consumer(s)
    (Plugin)      (Core System)      (Other Plugins/Core)
```

**Subscription Registration Timing:**
| Timing | Description |
|--------|-------------|
| Build time | Hardcoded handlers |
| Initialization | XML/Config defined |
| Runtime | Dynamic add/remove |

**Source:** [Wikipedia Pub-Sub](https://en.wikipedia.org/wiki/Publish–subscribe_pattern), [AltexSoft EDA](https://www.altexsoft.com/blog/event-driven-architecture-pub-sub/)

#### Benefits for Plugin Architecture

- **Loose Coupling** - Plugins don't need to know about each other
- **Scalability** - Any number of publishers/subscribers
- **Language Agnostic** - Works across different components
- **Testability** - Easy to mock events

#### Plugin Event System Example

```kotlin
// Core defines events
sealed class AppEvent {
    data class TimesheetCreated(val timesheet: Timesheet) : AppEvent()
    data class TimesheetUpdated(val timesheet: Timesheet) : AppEvent()
    data class UserLoggedIn(val user: User) : AppEvent()
}

// Event Bus in Core
interface EventBus {
    fun publish(event: AppEvent)
    fun <T : AppEvent> subscribe(eventClass: KClass<T>, handler: (T) -> Unit)
}

// Plugin subscribes
class JiraPlugin : Plugin {
    override fun init(context: PluginContext) {
        context.eventBus.subscribe(TimesheetCreated::class) { event ->
            syncToJira(event.timesheet)
        }
    }
}
```

**Source:** [nopCommerce Entity Events](https://docs.nopcommerce.com/en/developer/design/entity-events-system.html)

### Dependency Injection Integration (Koin)

#### Koin for Kotlin Multiplatform

**Why Koin:**
- No code generation or reflection
- Fast and efficient
- Kotlin DSL syntax
- KMP support (Desktop, iOS, Android)
- IDE Plugin with AI debugging

**Source:** [Koin Official](https://insert-koin.io/), [Koin GitHub](https://github.com/InsertKoinIO/koin)

#### Plugin DI Integration Strategies

**Option 1: Plugin Provides Koin Module**
```kotlin
// Plugin exports its Koin module
class JiraPlugin : Plugin {
    override val koinModule = module {
        single { JiraClient(get()) }
        single { JiraRepository(get()) }
        factory { JiraSyncService(get(), get()) }
    }
}

// Core loads plugin modules
fun loadPlugin(plugin: Plugin) {
    getKoin().loadModules(listOf(plugin.koinModule))
}
```

**Option 2: Manual Registration via Context**
```kotlin
// Plugin registers manually
class JiraPlugin : Plugin {
    override fun init(context: PluginContext) {
        context.registerService<JiraClient> { JiraClient(context.httpClient) }
        context.registerService<JiraRepository> { JiraRepository(get()) }
    }
}
```

**Recommendation for Kimai Client:** Option 1 (Koin Module) - cleaner, leverages existing Koin setup.

**Source:** [Koin KMP Docs](https://insert-koin.io/docs/reference/koin-mp/kmp/), [Auth0 Koin Guide](https://auth0.com/blog/dependency-injection-with-kotlin-and-koin/)

### Extension Point API Design

#### Fine-Grained Extension Points

```kotlin
// Separate interface per extension type
interface SettingsExtension {
    val settingsPanel: @Composable () -> Unit
    val settingsId: String
}

interface NavigationExtension {
    val navigationItem: NavigationItem
    val screen: @Composable () -> Unit
}

interface TimesheetActionExtension {
    val action: TimesheetAction
    fun execute(timesheet: Timesheet)
}

// Plugin implements what it needs
@Extension
class JiraPlugin : Plugin, SettingsExtension, TimesheetActionExtension {
    // Only implements relevant extension points
}
```

#### Extension Point Registry

```kotlin
class ExtensionRegistry {
    private val extensions = mutableMapOf<KClass<*>, MutableList<Any>>()

    fun <T : Any> register(type: KClass<T>, extension: T) {
        extensions.getOrPut(type) { mutableListOf() }.add(extension)
    }

    fun <T : Any> getAll(type: KClass<T>): List<T> {
        return extensions[type]?.filterIsInstance(type.java) ?: emptyList()
    }
}

// Usage in Core
val settingsPanels = registry.getAll(SettingsExtension::class)
    .map { it.settingsPanel }
```

**Source:** [Postman API Design](https://www.postman.com/api-platform/api-design/), [Microservice API Patterns](https://microservice-api-patterns.org/)

---

## Architectural Patterns and Design

### Hexagonal/Clean Architecture for Plugins

#### Hybrid Architecture Approach

Combines Hexagonal + Clean Architecture with plugin-style modules:

```
┌─────────────────────────────────────────────────────┐
│                   Adapters (Outer)                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │   UI/CLI    │  │  Plugins    │  │  External   │  │
│  │  Adapters   │  │  (Dynamic)  │  │    APIs     │  │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  │
├─────────┴────────────────┴────────────────┴─────────┤
│                    Ports (Interface)                 │
│  ┌─────────────────────────────────────────────────┐│
│  │  Extension Points / Plugin API Interfaces       ││
│  └─────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────┤
│                  Application Core                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │  Use Cases  │  │   Domain    │  │  Entities   │  │
│  │             │  │   Logic     │  │             │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────┘
```

**Key Principles:**
- Dependencies flow inward (Dependency Inversion)
- Plugins are adapters in the outer ring
- Core business logic isolated from plugins
- Easy to swap adapters/plugins

**Source:** [Hexagonal Architecture Wikipedia](https://en.wikipedia.org/wiki/Hexagonal_architecture_(software)), [Foojay Clean Modular Java](https://foojay.io/today/clean-and-modular-java-a-hexagonal-architecture-approach/)

#### Benefits for Kimai Plugin System

| Benefit | Application |
|---------|-------------|
| **Modularity** | Plugins as independent modules |
| **Testability** | Core logic testable without plugins |
| **Flexibility** | Easy plugin swap/update |
| **Maintainability** | Clear boundaries |

**Source:** [ARTisIVF Modular Monolith](https://blog.artisivf.com/2024/08/29/how-to-build-a-modular-monolith-with-hexagonal-architecture/)

### Plugin Security Considerations

#### JVM Security Manager (Deprecated)

**WARNING:** Java Security Manager deprecated in Java 17 (JEP-411)

**Problems:**
- Easily bypassed by malicious code
- Complex to configure correctly
- Often disabled or given AllPermission
- Developers frequently misuse it

**Source:** [Inside.java Security Post](https://inside.java/2021/04/23/security-and-sandboxing-post-securitymanager/)

#### Recommended Approaches for Plugin Security

| Approach | Security Level | Complexity |
|----------|----------------|------------|
| **Trusted Plugins** | Low (trust author) | Simple |
| **ClassLoader Isolation** | Medium | Moderate |
| **Separate JVM** | High | Complex |
| **GraalVM Sandbox** | High | Moderate |

**For Kimai Client (Trusted Plugins):**
- Plugins from your own repo = trusted
- ClassLoader isolation sufficient
- No full sandbox needed
- Focus on API boundary control

**Source:** [GraalVM Sandboxing](https://www.graalvm.org/latest/security-guide/sandboxing/), [Jayway Sandboxing Plugins](https://blog.jayway.com/2014/06/13/sandboxing-plugins-in-java/)

#### Shallow Sandbox Strategy

```kotlin
// Plugin can only access approved APIs
interface PluginContext {
    // Exposed APIs (safe)
    fun getDatabase(): PluginDatabase
    fun getHttpClient(): HttpClient
    fun getSettings(): PluginSettings

    // NOT exposed (dangerous)
    // - Direct file system access
    // - Process execution
    // - Network sockets
    // - Reflection on core classes
}
```

**Source:** [Terse Systems Sandbox](https://tersesystems.com/blog/2015/12/29/sandbox-experiment/)

### Kotlin Multiplatform Architecture

#### Code Sharing Strategy

**Typical Split:**
- 60-90% shared code (business logic, data layer)
- 10-40% platform-specific (UI, OS integrations)

**Adopters:** Shopify, Forbes, Haier, Zürcher Kantonalbank
**Google Support:** Official support announced at Google I/O 2024

**Source:** [JetBrains KMP](https://www.jetbrains.com/kotlin-multiplatform/), [Stream KMP Roadmap](https://getstream.io/blog/kotlin-multiplatform-roadmap/)

#### Expect/Actual Pattern for Plugins

```kotlin
// commonMain - shared
expect class PluginLoader {
    fun loadPlugins(): List<Plugin>
}

// jvmMain - Desktop
actual class PluginLoader {
    actual fun loadPlugins(): List<Plugin> {
        // JAR-based loading with ServiceLoader
        return ServiceLoader.load(Plugin::class.java).toList()
    }
}

// androidMain - Mobile
actual class PluginLoader {
    actual fun loadPlugins(): List<Plugin> {
        // Dynamic Feature Module loading
        return DynamicFeaturePluginLoader.load()
    }
}

// iosMain - iOS
actual class PluginLoader {
    actual fun loadPlugins(): List<Plugin> {
        // Framework-based or bundled plugins
        return BundledPluginLoader.load()
    }
}
```

**Source:** [JetBrains KMP Structure](https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-discover-project.html), [Medium KMP Package Structure](https://medium.com/@kerry.bisset/unifying-code-across-platforms-a-guide-to-kotlin-multiplatform-package-structure-1ad9fb630ddf)

#### Plugin API Module Structure

```
kimai-plugin-api/           # Shared Plugin API
├── commonMain/
│   └── kotlin/
│       └── de/progeek/kimai/plugin/
│           ├── Plugin.kt              # Core interface
│           ├── PluginContext.kt       # Context API
│           ├── extensions/            # Extension points
│           └── events/                # Event definitions
├── jvmMain/                 # Desktop-specific
│   └── PluginLoader.kt
├── androidMain/             # Android-specific
│   └── PluginLoader.kt
└── iosMain/                 # iOS-specific
    └── PluginLoader.kt
```

### Architecture Decision: Plugin Loading per Platform

| Platform | Loading Mechanism | Plugin Format |
|----------|-------------------|---------------|
| **Desktop (JVM)** | ServiceLoader + Custom ClassLoader | JAR |
| **Android** | Dynamic Feature Modules | AAR/Dynamic Module |
| **iOS** | Static linking or Framework | Framework |

**Recommendation:** Start with Desktop (JAR), add Mobile later

**Source:** [Koin KMP](https://insert-koin.io/docs/reference/koin-mp/kmp/)

---
