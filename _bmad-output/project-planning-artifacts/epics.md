---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - "_bmad-output/prd.md"
  - "_bmad-output/architecture.md"
  - "_bmad-output/project-context.md"
  - "_bmad-output/analysis/brainstorming-session-2025-12-26.md"
  - "_bmad-output/project-planning-artifacts/research/technical-plugin-architecture-research-2025-12-26.md"
  - "_bmad-output/project-planning-artifacts/research/market-time-tracking-research-2025-12-26.md"
workflowType: 'epics-and-stories'
project_name: 'kimai-client'
user_name: 'Dmitri'
date: '2025-12-27'
---

# kimai-client - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for kimai-client, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

**Time Tracking Core (FR1-FR10):**

| ID | Requirement | Priority | Phase |
|----|-------------|----------|-------|
| FR1 | System shall allow users to start a timer with one click from the main interface | MUST | 1 |
| FR2 | System shall allow users to stop an active timer and save the time entry | MUST | 1 |
| FR3 | System shall display the current running timer with elapsed time in real-time | MUST | 1 |
| FR4 | System shall allow users to pause and resume a timer without losing data | SHOULD | 1 |
| FR5 | System shall allow users to edit existing time entries (start time, end time, description) | MUST | 1 |
| FR6 | System shall allow users to delete time entries with confirmation | MUST | 1 |
| FR7 | System shall allow users to assign activities and projects to time entries | MUST | 1 |
| FR8 | System shall automatically assign customer based on selected project | SHOULD | 1 |
| FR9 | System shall detect and warn about unfinished entries from previous sessions | SHOULD | 1 |
| FR10 | System shall provide a minimal timer widget mode for non-intrusive tracking | SHOULD | 2 |

**Plugin System (FR11-FR20):**

| ID | Requirement | Priority | Phase |
|----|-------------|----------|-------|
| FR11 | System shall load plugins from designated JAR files on application startup | MUST | 1 |
| FR12 | System shall provide a Plugin interface with `id`, `name`, `version`, `init()`, `dispose()` | MUST | 1 |
| FR13 | System shall validate plugin compatibility with current app version before loading | MUST | 1 |
| FR14 | System shall provide Settings Extension Point for plugins to register configuration panels | MUST | 1 |
| FR15 | System shall provide Navigation Extension Point for plugins to add UI tabs/panels | MUST | 1 |
| FR16 | System shall provide TimesheetInput Extension Point for plugins to add autocomplete suggestions | MUST | 1 |
| FR17 | System shall provide Database Extension Point for plugins to store custom data | MUST | 2 |
| FR18 | System shall provide HTTP Client Extension Point for plugins to make backend calls | SHOULD | 2 |
| FR19 | System shall gracefully handle plugin failures without crashing the main application | MUST | 1 |
| FR20 | System shall allow users to enable/disable individual plugins | SHOULD | 2 |

**License & Monetization (FR21-FR29):**

| ID | Requirement | Priority | Phase |
|----|-------------|----------|-------|
| FR21 | System shall validate license keys via online license server | MUST | 1 |
| FR22 | System shall cache valid licenses for offline use (30-day grace period) | MUST | 1 |
| FR23 | System shall activate plugins automatically upon valid license entry | MUST | 1 |
| FR24 | System shall support both individual and organizational (Master Key) licenses | SHOULD | 2 |
| FR25 | System shall provide 14-day trial activation for plugins | MUST | 2 |
| FR26 | System shall display clear license status (Active, Trial, Expired, Offline Mode) | MUST | 1 |
| FR27 | System shall convert trial licenses to full licenses seamlessly after payment | SHOULD | 2 |
| FR28 | System shall provide license activation tracking for administrators | SHOULD | 2 |
| FR29 | System shall use ED25519/ECDSA cryptographic signing for license validation | MUST | 1 |

**Task Integration Plugin (FR30-FR38):**

| ID | Requirement | Priority | Phase |
|----|-------------|----------|-------|
| FR30 | Plugin shall connect to Jira via OAuth authentication | MUST | 2 |
| FR31 | Plugin shall display user's current Jira issues sorted by priority | MUST | 2 |
| FR32 | Plugin shall allow starting a timer directly from a task/issue | MUST | 2 |
| FR33 | Plugin shall automatically link time entries to tasks/issues | MUST | 2 |
| FR34 | Plugin shall support GitHub Issues integration | SHOULD | 2 |
| FR35 | Plugin shall support GitLab Issues integration | SHOULD | 2 |
| FR36 | Plugin shall cache task data for offline access | MUST | 2 |
| FR37 | Plugin shall refresh task list on demand and periodically | SHOULD | 2 |
| FR38 | Plugin shall display task-to-time mapping in entry details | SHOULD | 2 |

**Data Synchronization & Offline (FR39-FR46):**

| ID | Requirement | Priority | Phase |
|----|-------------|----------|-------|
| FR39 | System shall store all time entries in local SQLite database | MUST | 1 |
| FR40 | System shall queue unsynchronized entries for upload when offline | MUST | 1 |
| FR41 | System shall automatically sync queued entries when connection restored | MUST | 1 |
| FR42 | System shall display sync status (synced, pending, failed) for each entry | MUST | 1 |
| FR43 | System shall resolve sync conflicts using server-wins strategy | MUST | 1 |
| FR44 | System shall notify users of conflicts requiring manual resolution | SHOULD | 2 |
| FR45 | System shall cache project, activity, and customer lists for offline use | MUST | 1 |
| FR46 | System shall display clear offline mode indicator in UI | MUST | 1 |

**User Settings & Preferences (FR47-FR53):**

| ID | Requirement | Priority | Phase |
|----|-------------|----------|-------|
| FR47 | System shall allow users to set default project for new time entries | MUST | 1 |
| FR48 | System shall allow users to select theme (Light/Dark/System) | MUST | 1 |
| FR49 | System shall allow users to configure auto-start on system login | SHOULD | 1 |
| FR50 | System shall allow users to configure inactivity reminder threshold | SHOULD | 2 |
| FR51 | System shall allow users to configure global keyboard shortcuts | SHOULD | 2 |
| FR52 | System shall persist all settings locally across sessions | MUST | 1 |
| FR53 | System shall allow users to configure notification preferences | SHOULD | 2 |

**Onboarding & Setup (FR54-FR59):**

| ID | Requirement | Priority | Phase |
|----|-------------|----------|-------|
| FR54 | System shall display step-by-step setup wizard on first run | MUST | 1 |
| FR55 | System shall validate Kimai server URL in real-time during setup | MUST | 1 |
| FR56 | System shall provide direct link to generate API token in Kimai | SHOULD | 1 |
| FR57 | System shall allow selection of default project during onboarding | SHOULD | 1 |
| FR58 | System shall celebrate successful first timer start | SHOULD | 1 |
| FR59 | System shall naturally present plugin upsell after successful onboarding | SHOULD | 2 |

**System Integration (FR60-FR66):**

| ID | Requirement | Priority | Phase |
|----|-------------|----------|-------|
| FR60 | System shall display persistent system tray icon on all desktop platforms | MUST | 1 |
| FR61 | System shall allow quick timer start/stop from system tray menu | MUST | 1 |
| FR62 | System shall minimize to system tray instead of closing | SHOULD | 1 |
| FR63 | System shall send desktop notifications for timer reminders | SHOULD | 2 |
| FR64 | System shall send notifications for sync status changes | SHOULD | 2 |
| FR65 | System shall support deep links (`kimai://`) for external triggers | COULD | 2 |
| FR66 | System shall check for app updates on startup (configurable) | SHOULD | 2 |

**Error Handling & Recovery (FR67-FR72):**

| ID | Requirement | Priority | Phase |
|----|-------------|----------|-------|
| FR67 | System shall display clear error messages with actionable information | MUST | 1 |
| FR68 | System shall provide connection status banner when server unreachable | MUST | 1 |
| FR69 | System shall preserve local data when connection fails | MUST | 1 |
| FR70 | System shall automatically retry failed sync operations with exponential backoff | MUST | 1 |
| FR71 | System shall log errors locally for debugging purposes | SHOULD | 1 |
| FR72 | System shall allow users to manually trigger sync retry | SHOULD | 2 |

### NonFunctional Requirements

**Performance (NFR-P1 to NFR-P7):**

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-P1 | App startup time | < 3 seconds |
| NFR-P2 | Timer start/stop response | < 100ms |
| NFR-P3 | Plugin loading time | < 2 seconds per plugin |
| NFR-P4 | Memory footprint (base app) | < 200MB |
| NFR-P5 | Memory footprint (per plugin) | < 50MB |
| NFR-P6 | Sync operation | Background, non-blocking, no UI freeze |
| NFR-P7 | UI responsiveness | 60 FPS during interactions |

**Security (NFR-S1 to NFR-S7):**

| ID | Requirement | Implementation |
|----|-------------|----------------|
| NFR-S1 | API tokens encrypted at rest | AES-256 encryption via Cryptography library |
| NFR-S2 | License keys cryptographic signing | ED25519 or ECDSA signatures |
| NFR-S3 | Network communication security | HTTPS only, TLS 1.2+ |
| NFR-S4 | Credential logging prevention | Sanitized logging, no secrets in logs |
| NFR-S5 | Plugin isolation | Plugins cannot access other plugins' data |
| NFR-S6 | License validation tamper-resistance | Signed license files, server verification |
| NFR-S7 | Secure credential storage | Platform-specific (Keychain/Credential Manager) |

**Reliability (NFR-R1 to NFR-R6):**

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-R1 | Data persistence | 100% entries recoverable after crashes |
| NFR-R2 | Offline operation | 100% time tracking features available offline |
| NFR-R3 | Sync reliability | > 99.9% successful sync after reconnection |
| NFR-R4 | Plugin fault tolerance | App must not crash from faulty plugin |
| NFR-R5 | License offline grace | 30 days of offline plugin usage |
| NFR-R6 | Auto-reconnect | Exponential backoff, max 5 minutes |

**Integration (NFR-I1 to NFR-I7):**

| ID | Requirement | Specification |
|----|-------------|---------------|
| NFR-I1 | Kimai API compatibility | Kimai API v1 (current) |
| NFR-I2 | Jira API compatibility | Jira Cloud REST API v3 |
| NFR-I3 | GitHub API compatibility | GitHub REST API v3, GraphQL v4 |
| NFR-I4 | GitLab API compatibility | GitLab REST API v4 |
| NFR-I5 | OAuth 2.0 support | Standard OAuth 2.0 flow for integrations |
| NFR-I6 | API rate limiting handling | Graceful degradation, user notification |
| NFR-I7 | Webhook support (future) | Real-time updates via webhooks |

**Accessibility (NFR-A1 to NFR-A5):**

| ID | Requirement | Standard |
|----|-------------|----------|
| NFR-A1 | Keyboard navigation | All features accessible via keyboard |
| NFR-A2 | Screen reader support | Labels and ARIA-equivalent for Compose |
| NFR-A3 | High contrast mode | Readable with system high contrast |
| NFR-A4 | Font scaling | Respect system font size settings |
| NFR-A5 | Focus indicators | Visible focus states for all interactive elements |

**Cross-Platform Compatibility (NFR-C1 to NFR-C6):**

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-C1 | Windows support | Windows 10, 11 - Full feature parity |
| NFR-C2 | macOS support | macOS 11+ (Big Sur+) - Full feature parity |
| NFR-C3 | Linux support | Ubuntu 20.04+, Fedora 35+ - Full feature parity |
| NFR-C4 | JVM requirement | Java 21+ (Bundled JRE) |
| NFR-C5 | Screen resolution | 1280x720 minimum - Responsive layout |
| NFR-C6 | HiDPI support | 4K displays - Native scaling |

### Additional Requirements

**From Architecture Document:**

- **Brownfield Project:** Extending existing Kotlin Multiplatform codebase (no starter template needed)
- **New Module:** Create `kimai-plugin-api` module with Plugin interface and PluginContext
- **Plugin Framework:** Use PF4J for plugin loading (enterprise-proven: Netflix, Facebook)
- **License Algorithm:** ED25519 for cryptographic license signing
- **License Location:** Validation logic in plugins, not Core (keeps core 100% open source)
- **Database Strategy:** Separate SQLite database per plugin for data isolation
- **Communication:** MVIKotlin Store-based communication between plugins and Core
- **Distribution (MVP):** Build-time plugin integration
- **Distribution (Post-MVP):** Dynamic plugin loading via PF4J
- **Claude Skills:** MUST use `/mvikotlin`, `/decompose-mvikotlin`, `/store5-kotlin` for implementation

**Technology Stack (from project-context.md):**

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
| Cryptography | 0.5.0 | Security |

**From Brainstorming Session:**

- **MVP Phase Strategy:** Plugin Foundation (Phase 1) → First Plugin (Phase 2) → Mobile (Phase 3)
- **Pilot Plugin:** Task Integration (Jira/GitHub/GitLab) to test all extension points
- **Go-to-Market:** KMU (5-50 employees) first, then Enterprise + Freelancer

**From Market Research:**

- **Pricing Model:** Free (Freelancer) → Pro €5-8/user/mo (KMU) → Enterprise (Custom)
- **Competitive Edge:** Privacy-First + Universal Backend + Plugin Ecosystem + Self-Hosted

**From Technical Research:**

- **Plugin Loading:** ServiceLoader + ClassLoader isolation for JAR discovery
- **Extension Point Pattern:** Fine-grained interfaces (SettingsExtension, NavigationExtension, TimesheetInputExtension)
- **Event System:** Pub/Sub via MVIKotlin Stores (no separate Event Bus needed)
- **Security:** Shallow sandbox via PluginContext API boundary

### FR Coverage Map

| FR | Epic | Description |
|----|------|-------------|
| FR1 | Existing | Start timer with one click |
| FR2 | Existing | Stop timer and save entry |
| FR3 | Existing | Display running timer real-time |
| FR4 | Epic 8 | Pause and resume timer |
| FR5 | Existing | Edit existing time entries |
| FR6 | Existing | Delete time entries with confirmation |
| FR7 | Existing | Assign activities and projects |
| FR8 | Epic 8 | Auto-assign customer from project |
| FR9 | Epic 8 | Detect unfinished entries |
| FR10 | Epic 8 | Minimal timer widget mode |
| FR11 | Epic 1 | Load plugins from JAR files |
| FR12 | Epic 1 | Plugin interface (id, name, version, init, dispose) |
| FR13 | Epic 1 | Validate plugin compatibility |
| FR14 | Epic 2 | Settings Extension Point |
| FR15 | Epic 2 | Navigation Extension Point |
| FR16 | Epic 2 | TimesheetInput Extension Point (autocomplete suggestions) |
| FR17 | Epic 4 | Database Extension Point |
| FR18 | Epic 4 | HTTP Client Extension Point |
| FR19 | Epic 1 | Graceful plugin failure handling |
| FR20 | Epic 4 | Enable/disable plugins |
| FR21 | Epic 3 | Online license validation |
| FR22 | Epic 3 | Offline license cache (30-day grace) |
| FR23 | Epic 3 | Auto-activate on valid license |
| FR24 | Epic 7 | Individual + Master Key licenses |
| FR25 | Epic 7 | 14-day trial activation |
| FR26 | Epic 3 | License status display |
| FR27 | Epic 7 | Trial-to-paid conversion |
| FR28 | Epic 7 | License activation tracking |
| FR29 | Epic 3 | ED25519/ECDSA signing |
| FR30 | Epic 5 | Jira OAuth connection |
| FR31 | Epic 5 | Display Jira issues by priority |
| FR32 | Epic 5 | Start timer from task |
| FR33 | Epic 5 | Auto-link time entries to tasks |
| FR34 | Epic 6 | GitHub Issues integration |
| FR35 | Epic 6 | GitLab Issues integration |
| FR36 | Epic 5 | Cache task data offline |
| FR37 | Epic 5 | Refresh task list |
| FR38 | Epic 5 | Task-to-time mapping display |
| FR39 | Epic 9 | Local SQLite database |
| FR40 | Epic 9 | Offline queue for uploads |
| FR41 | Epic 9 | Auto-sync on reconnection |
| FR42 | Epic 9 | Sync status display |
| FR43 | Epic 9 | Server-wins conflict resolution |
| FR44 | Epic 9 | Conflict notification |
| FR45 | Epic 9 | Cache project/activity/customer lists |
| FR46 | Epic 9 | Offline mode indicator |
| FR47 | Epic 10 | Set default project |
| FR48 | Epic 10 | Theme selection |
| FR49 | Epic 10 | Auto-start configuration |
| FR50 | Epic 10 | Inactivity reminder |
| FR51 | Epic 10 | Global keyboard shortcuts |
| FR52 | Epic 10 | Persist settings locally |
| FR53 | Epic 10 | Notification preferences |
| FR54 | Epic 11 | Setup wizard on first run |
| FR55 | Epic 11 | Real-time server URL validation |
| FR56 | Epic 11 | Direct link for API token |
| FR57 | Epic 11 | Default project during onboarding |
| FR58 | Epic 11 | Celebrate first timer start |
| FR59 | Epic 11 | Plugin upsell after onboarding |
| FR60 | Epic 12 | Persistent system tray icon |
| FR61 | Epic 12 | Quick timer from tray |
| FR62 | Epic 12 | Minimize to tray |
| FR63 | Epic 12 | Desktop notifications |
| FR64 | Epic 12 | Sync status notifications |
| FR65 | Epic 12 | Deep links (kimai://) |
| FR66 | Epic 12 | Update checker |
| FR67 | Epic 13 | Actionable error messages |
| FR68 | Epic 13 | Connection status banner |
| FR69 | Epic 13 | Preserve local data on failure |
| FR70 | Epic 13 | Auto-retry with backoff |
| FR71 | Epic 13 | Local error logging |
| FR72 | Epic 13 | Manual sync retry |

## Epic List

### Epic 1: Plugin API Foundation
**Goal:** Enable the application to load and initialize plugins with proper lifecycle management and fault tolerance.

**User Outcome:** The app can discover, load, and manage plugin lifecycles safely.

**FRs covered:** FR11, FR12, FR13, FR19

**Phase:** 1 (Plugin Foundation)

---

### Epic 2: Plugin Extension Points
**Goal:** Enable plugins to extend the application's UI and functionality through well-defined extension points.

**User Outcome:** Plugins can add settings panels, navigation tabs, and timesheet entry actions.

**FRs covered:** FR14, FR15, FR16

**Phase:** 1 (Plugin Foundation)

---

### Epic 3: License Validation System
**Goal:** Enable premium plugin activation via cryptographically signed license keys with offline support.

**User Outcome:** Users can activate plugins with license keys, see license status, and work offline for up to 30 days.

**FRs covered:** FR21, FR22, FR23, FR26, FR29

**Phase:** 1 (Plugin Foundation)

---

### Epic 4: Plugin Data & Network Extensions
**Goal:** Enable plugins to store persistent data and make authenticated network calls.

**User Outcome:** Plugins have their own database storage and can communicate with external APIs.

**FRs covered:** FR17, FR18, FR20

**Phase:** 2 (First Plugin)

---

### Epic 5: Task Integration Plugin (Jira)
**Goal:** Enable users to link time entries with Jira tasks for seamless project management integration.

**User Outcome:** Users can see Jira issues, start timers from tasks, and have automatic time-to-task linking.

**FRs covered:** FR30, FR31, FR32, FR33, FR36, FR37, FR38

**Phase:** 2 (First Plugin)

---

### Epic 6: Task Integration Plugin (GitHub & GitLab)
**Goal:** Extend task integration to GitHub and GitLab for developer workflow support.

**User Outcome:** Same task integration features work with GitHub Issues and GitLab Issues.

**FRs covered:** FR34, FR35

**Phase:** 2 (First Plugin)

---

### Epic 7: Advanced License Features
**Goal:** Enable trial licenses, team licensing, and administrative tracking.

**User Outcome:** Users can try plugins for 14 days, organizations can use Master Keys, admins can track activations.

**FRs covered:** FR24, FR25, FR27, FR28

**Phase:** 2 (First Plugin)

---

### Epic 8: Time Tracking Enhancements
**Goal:** Improve the core time tracking experience with advanced features.

**User Outcome:** Users can pause/resume timers, see auto-assigned customers, get warned about unfinished entries, and use a minimal widget.

**FRs covered:** FR4, FR8, FR9, FR10

**Phase:** 1 (Plugin Foundation)

---

### Epic 9: Offline-First Sync System
**Goal:** Enable seamless offline work with reliable synchronization and conflict handling.

**User Outcome:** Full functionality offline, automatic sync on reconnection, visible sync status, and smart conflict resolution.

**FRs covered:** FR39, FR40, FR41, FR42, FR43, FR44, FR45, FR46

**Phase:** 1 (Plugin Foundation)

---

### Epic 10: Settings & Preferences
**Goal:** Enable users to customize their application experience.

**User Outcome:** Users can set themes, defaults, auto-start, shortcuts, and notification preferences.

**FRs covered:** FR47, FR48, FR49, FR50, FR51, FR52, FR53

**Phase:** 1 (Plugin Foundation)

---

### Epic 11: Onboarding Experience
**Goal:** Guide new users through app setup with a friendly wizard experience.

**User Outcome:** Easy step-by-step setup, helpful validation, celebration of first timer, and plugin discovery.

**FRs covered:** FR54, FR55, FR56, FR57, FR58, FR59

**Phase:** 1 (Plugin Foundation)

---

### Epic 12: Desktop System Integration
**Goal:** Integrate seamlessly with desktop environments on all platforms.

**User Outcome:** System tray access, notifications, deep links, and automatic update checks.

**FRs covered:** FR60, FR61, FR62, FR63, FR64, FR65, FR66

**Phase:** 1 (Plugin Foundation)

---

### Epic 13: Error Handling & Recovery
**Goal:** Provide clear error feedback and robust recovery mechanisms.

**User Outcome:** Understandable errors, preserved data during failures, automatic retries, and manual recovery options.

**FRs covered:** FR67, FR68, FR69, FR70, FR71, FR72

**Phase:** 1 (Plugin Foundation)

---

## Epic 1: Plugin API Foundation

**Goal:** Enable the application to load and initialize plugins with proper lifecycle management and fault tolerance.

**User Outcome:** The app can discover, load, and manage plugin lifecycles safely.

**FRs covered:** FR11, FR12, FR13, FR19

---

### Story 1.1: Create Plugin API Module Structure

**As a** developer,
**I want** a dedicated `kimai-plugin-api` module with the core Plugin interface,
**So that** plugins have a clear contract to implement.

**Acceptance Criteria:**

**Given** the kimai-client project
**When** I add the kimai-plugin-api module
**Then** the module compiles successfully with Kotlin 2.2.21
**And** contains the `Plugin` interface with `id: String`, `name: String`, `version: String`, `init(context: PluginContext)`, `dispose()` methods
**And** contains a basic `PluginContext` interface stub
**And** the module is added to settings.gradle.kts
**And** kimai-desktop can depend on kimai-plugin-api

---

### Story 1.2: Implement PluginContext API Surface

**As a** plugin developer,
**I want** access to Core application stores and utilities via PluginContext,
**So that** my plugin can read app state and access necessary services.

**Acceptance Criteria:**

**Given** the Plugin interface from Story 1.1
**When** I implement PluginContext
**Then** it exposes read-only access to `TimesheetStore`, `ProjectStore`, `ActivityStore`
**And** provides `mainContext` and `ioContext` CoroutineContexts
**And** provides `getDatabasePath(name: String): Path` for plugin-specific databases
**And** provides `sqlDriverFactory: SqlDriverFactory` for SQLDelight
**And** provides `getLicenseKey(pluginId: String): String?` for license retrieval

---

### Story 1.3: Implement Plugin Loader with JAR Discovery

**As a** user,
**I want** the application to automatically discover and load plugins from JAR files,
**So that** I don't need to manually configure each plugin.

**Acceptance Criteria:**

**Given** the application starts
**When** PluginLoader scans the plugins directory (`~/.kimai-client/plugins/`)
**Then** it discovers all JAR files in that directory
**And** uses ServiceLoader or PF4J to find Plugin implementations
**And** creates Plugin instances for each discovered plugin
**And** logs which plugins were discovered (plugin id, name, version)

---

### Story 1.4: Implement Plugin Version Compatibility Check

**As a** user,
**I want** incompatible plugins to be rejected before loading,
**So that** the app doesn't crash from plugin version mismatches.

**Acceptance Criteria:**

**Given** a plugin is discovered
**When** PluginLoader checks compatibility
**Then** it verifies the plugin's `minAppVersion` against current app version
**And** if incompatible, the plugin is NOT loaded
**And** a warning is logged: "Plugin {name} v{version} requires app version {minAppVersion}+"
**And** compatible plugins proceed to initialization
**And** an optional `maxAppVersion` is also checked if present

---

### Story 1.5: Implement Plugin Lifecycle Management

**As a** plugin developer,
**I want** clear lifecycle hooks (init, dispose),
**So that** my plugin can set up and clean up resources properly.

**Acceptance Criteria:**

**Given** a compatible plugin is loaded
**When** PluginManager initializes it
**Then** `plugin.init(context)` is called with a valid PluginContext
**And** the plugin is marked as "initialized"
**And** when the application shuts down, `plugin.dispose()` is called
**And** plugins are disposed in reverse order of initialization
**And** PluginManager tracks plugin states (DISCOVERED, INITIALIZED, DISPOSED, FAILED)

---

### Story 1.6: Implement Plugin Fault Tolerance

**As a** user,
**I want** a faulty plugin to not crash the entire application,
**So that** I can continue using other features and plugins.

**Acceptance Criteria:**

**Given** a plugin throws an exception during `init()` or any operation
**When** the exception occurs
**Then** the exception is caught by PluginManager
**And** the plugin is marked as FAILED
**And** an error is logged with plugin id and stack trace
**And** the user sees a notification: "Plugin {name} failed to load"
**And** other plugins continue to work normally
**And** the core application remains fully functional

---

### Story 1.7: Add Plugin Registry in Desktop Module

**As a** developer,
**I want** a central PluginRegistry for build-time plugin registration (MVP),
**So that** plugins can be included at compile time without dynamic loading.

**Acceptance Criteria:**

**Given** kimai-desktop module
**When** I create PluginRegistry.kt
**Then** it contains a list of Plugin instances for build-time inclusion
**And** it integrates with PluginManager for lifecycle management
**And** future dynamic plugins can be added alongside static plugins
**And** example: `val plugins: List<Plugin> = listOf(TasksPlugin())`

---

## Epic 2: Plugin Extension Points

**Goal:** Enable plugins to extend the application's UI and functionality through well-defined extension points.

**User Outcome:** Plugins can add settings panels, navigation tabs, and timesheet entry actions.

**FRs covered:** FR14, FR15, FR16

---

### Story 2.1: Define Extension Point Interfaces

**As a** plugin developer,
**I want** clear extension point interfaces to implement,
**So that** I know exactly how to extend the application.

**Acceptance Criteria:**

**Given** the kimai-plugin-api module
**When** I define extension interfaces
**Then** `SettingsExtension` interface exists with `settingsItem: SettingsItem` and `@Composable SettingsContent(pluginContext: PluginContext)`
**And** `NavigationExtension` interface exists with `navigationItem: NavigationItem`, `createComponent()`, and `@Composable Content()`
**And** `TimesheetInputExtension` interface exists with `getSuggestions(query)` and `formatSuggestion(suggestion)` (defined in Story 2.5)
**And** supporting data classes `SettingsItem`, `NavigationItem`, `InputSuggestion` are defined
**And** all interfaces are in `de.progeek.kimai.plugin.api.extensions` package

---

### Story 2.2: Implement Extension Registry

**As a** plugin developer,
**I want** my extension implementations to be automatically discovered,
**So that** I don't need to manually register each extension.

**Acceptance Criteria:**

**Given** a plugin implements extension interfaces
**When** PluginManager initializes the plugin
**Then** it detects which extension interfaces the plugin implements
**And** registers the plugin with the ExtensionRegistry for each interface
**And** ExtensionRegistry provides `getAll<T: Extension>(): List<T>` method
**And** Core can query registered extensions: `registry.getAll<SettingsExtension>()`

---

### Story 2.3: Integrate Settings Extension Point

**As a** user,
**I want** plugin settings to appear in the Settings screen,
**So that** I can configure plugins from one place.

**Acceptance Criteria:**

**Given** plugins implement SettingsExtension
**When** I open the Settings screen
**Then** plugin settings panels appear in a "Plugins" section
**And** each plugin's `SettingsContent()` composable is rendered
**And** plugin settings are visually separated from core settings
**And** if no plugins have settings, the "Plugins" section is hidden
**And** settings changes are handled by the plugin's own logic

---

### Story 2.4: Integrate Navigation Extension Point

**As a** user,
**I want** plugin screens to appear as tabs in the home screen,
**So that** I can access plugin features naturally.

**Acceptance Criteria:**

**Given** plugins implement NavigationExtension
**When** I view the HomeComponent
**Then** plugin navigation items appear alongside core tabs (Timesheet, Settings)
**And** clicking a plugin tab navigates to the plugin's screen
**And** the plugin's Decompose Component is created via `createComponent()`
**And** the plugin's `Content()` composable is rendered in that tab
**And** navigation state is preserved when switching tabs

---

### Story 2.5: Define TimesheetInput Extension Point

**As a** plugin developer,
**I want** an extension point for enhancing the timesheet input field,
**So that** my plugin can provide autocomplete suggestions and input enhancements.

**Acceptance Criteria:**

**Given** the kimai-plugin-api module
**When** I define the TimesheetInputExtension interface
**Then** it has `suspend fun getSuggestions(query: String): List<InputSuggestion>` for autocomplete
**And** it has `fun formatSuggestion(suggestion: InputSuggestion): String` for text formatting
**And** `InputSuggestion` data class contains: `id`, `title`, `subtitle?`, `icon?`, `metadata`
**And** the interface extends `Extension` base interface
**And** all interfaces are in `de.progeek.kimai.plugin.api.extensions` package
**And** the extension is discoverable via ExtensionRegistry

---

### Story 2.6: Integrate TimesheetInput Extension Point

**As a** user,
**I want** plugin suggestions to appear in the description field,
**So that** I can quickly select tasks/issues while typing.

**Acceptance Criteria:**

**Given** plugins implement TimesheetInputExtension
**When** I type in the timesheet description field
**Then** plugin suggestions appear in a popup below the field
**And** each suggestion shows: title, subtitle (if present), icon (if present)
**And** clicking a suggestion inserts formatted text via `formatSuggestion()`
**And** keyboard navigation (up/down/enter) works for selection
**And** if no plugins provide suggestions, no popup appears
**And** multiple plugins' suggestions are combined (grouped by plugin)

---

## Epic 3: License Validation System

**Goal:** Enable premium plugin activation via cryptographically signed license keys with offline support.

**User Outcome:** Users can activate plugins with license keys, see license status, and work offline for up to 30 days.

**FRs covered:** FR21, FR22, FR23, FR26, FR29

---

### Story 3.1: Define License Data Models

**As a** developer,
**I want** clear data models for licenses,
**So that** license information can be parsed and validated consistently.

**Acceptance Criteria:**

**Given** the license-common module (closed source)
**When** I define license models
**Then** `LicenseFile` data class contains: `pluginId`, `userId`, `email`, `expiresAt`, `features`, `signature`
**And** `LicenseStatus` enum has: `ACTIVE`, `TRIAL`, `EXPIRED`, `INVALID`, `OFFLINE_GRACE`
**And** `LicenseValidationResult` contains: `status`, `expiresAt`, `daysRemaining`, `errorMessage`
**And** models support JSON serialization for license file parsing

---

### Story 3.2: Implement ED25519 License Signing and Verification

**As a** developer,
**I want** cryptographically secure license verification,
**So that** licenses cannot be forged or tampered with.

**Acceptance Criteria:**

**Given** a license file with signature
**When** I verify using ED25519
**Then** the public key (embedded in plugin) verifies the signature
**And** tampered licenses fail verification
**And** verification uses the Cryptography 0.5.0 library
**And** signing (server-side only) uses the private key
**And** signature size is ~64 bytes (ED25519 standard)

---

### Story 3.3: Implement Online License Validation

**As a** user,
**I want** my license to be validated against the license server,
**So that** I can activate plugins securely.

**Acceptance Criteria:**

**Given** a user enters a license key
**When** the plugin validates the license
**Then** an API call is made to the license server with the key
**And** server returns license details (validity, expiration, features)
**And** if valid, the license is stored locally
**And** if invalid, user sees: "Invalid license key"
**And** network errors are handled gracefully with retry option

---

### Story 3.4: Implement Offline License Cache

**As a** user,
**I want** my validated license to work offline,
**So that** I can use plugins without constant internet.

**Acceptance Criteria:**

**Given** a license was validated online
**When** the application starts offline
**Then** the cached license is loaded from local storage
**And** the cached license signature is re-verified locally
**And** if cache is < 30 days old, plugin works normally
**And** if cache is > 30 days old, user sees: "Please connect to validate license"
**And** license status shows "Offline Mode" indicator

---

### Story 3.5: Implement Auto-Activation on Valid License

**As a** user,
**I want** plugins to activate automatically when I enter a valid license,
**So that** I don't need additional steps after payment.

**Acceptance Criteria:**

**Given** a valid license key is entered
**When** validation succeeds
**Then** the associated plugin is enabled automatically
**And** the plugin's `init()` is called if not already initialized
**And** user sees: "Plugin {name} activated successfully"
**And** activation takes < 30 seconds from key entry to active plugin
**And** no app restart is required

---

### Story 3.6: Implement License Status Display

**As a** user,
**I want** to see my license status clearly,
**So that** I know when to renew or if there are issues.

**Acceptance Criteria:**

**Given** I have plugins with licenses
**When** I view Settings > Plugins
**Then** each plugin shows its license status badge (Active, Trial, Expired, Offline)
**And** Active shows green badge with expiration date
**And** Trial shows orange badge with "X days remaining"
**And** Expired shows red badge with "Renew" button
**And** Offline shows gray badge with "Last validated: {date}"
**And** clicking a badge shows license details modal

---

## Epic 4: Plugin Data & Network Extensions

**Goal:** Enable plugins to store persistent data and make authenticated network calls.

**User Outcome:** Plugins have their own database storage and can communicate with external APIs.

**FRs covered:** FR17, FR18, FR20

---

### Story 4.1: Implement Database Extension Point

**As a** plugin developer,
**I want** my plugin to have its own SQLite database,
**So that** I can store plugin-specific data without affecting the core database.

**Acceptance Criteria:**

**Given** a plugin needs persistent storage
**When** I call `context.getDatabasePath("my-data")`
**Then** it returns a path like `~/.kimai-client/data/plugins/plugin-{id}.db`
**And** the directory is created automatically if it doesn't exist
**And** `context.sqlDriverFactory` creates a SQLDelight driver for that path
**And** plugin can use SQLDelight with its own schema
**And** plugin database is isolated from core database

---

### Story 4.2: Implement HTTP Client Extension Point

**As a** plugin developer,
**I want** access to a configured HTTP client,
**So that** my plugin can make API calls without boilerplate setup.

**Acceptance Criteria:**

**Given** a plugin needs to call external APIs
**When** I access `context.httpClient`
**Then** it provides a pre-configured Ktor HttpClient
**And** the client includes common settings (timeouts, JSON serialization)
**And** plugin can add its own authentication headers
**And** rate limiting errors are passed through to plugin for handling
**And** the client respects system proxy settings

---

### Story 4.3: Implement Plugin Enable/Disable UI

**As a** user,
**I want** to enable or disable individual plugins,
**So that** I can control which plugins are active.

**Acceptance Criteria:**

**Given** I am in Settings > Plugins
**When** I view the plugin list
**Then** each plugin has an enable/disable toggle
**And** disabling a plugin calls `plugin.dispose()` immediately
**And** disabled plugins are not initialized on next startup
**And** enabled plugins are initialized normally
**And** plugin state (enabled/disabled) persists across app restarts
**And** disabled plugins retain their data and licenses

---

### Story 4.4: Add Plugin Data Cleanup Option

**As a** user,
**I want** to delete a plugin's data when removing it,
**So that** I can free up space and remove all traces.

**Acceptance Criteria:**

**Given** I want to remove a plugin completely
**When** I click "Remove Plugin Data" in Settings > Plugins
**Then** a confirmation dialog appears: "Delete all data for {plugin}?"
**And** on confirm, the plugin database file is deleted
**And** cached data and credentials are removed
**And** license information is cleared
**And** the plugin can be reinstalled fresh later

---

## Epic 5: Task Integration Plugin (Jira)

**Goal:** Enable users to link time entries with Jira tasks for seamless project management integration.

**User Outcome:** Users can see Jira issues, start timers from tasks, and have automatic time-to-task linking.

**FRs covered:** FR30, FR31, FR32, FR33, FR36, FR37, FR38

---

### Story 5.1: Create Task Integration Plugin Structure

**As a** developer,
**I want** a proper plugin structure for Task Integration,
**So that** it follows the plugin architecture correctly.

**Acceptance Criteria:**

**Given** the kimai-plugins repository (closed source)
**When** I create plugin-tasks module
**Then** it implements the `Plugin` interface with id="task-integration"
**And** it implements `NavigationExtension`, `SettingsExtension`, `TimesheetActionExtension`
**And** package structure follows: `de.progeek.kimai.plugin.tasks`
**And** it has its own Koin module for dependency injection
**And** it compiles and can be loaded by PluginManager

---

### Story 5.2: Implement Jira OAuth Authentication

**As a** user,
**I want** to connect my Jira account securely,
**So that** the plugin can access my issues.

**Acceptance Criteria:**

**Given** I am in Task Integration settings
**When** I click "Connect Jira"
**Then** OAuth 2.0 flow opens in browser
**And** I authenticate with my Atlassian account
**And** tokens are securely stored in plugin database (encrypted)
**And** connection status shows "Connected as {email}"
**And** I can disconnect at any time
**And** expired tokens trigger re-authentication flow

---

### Story 5.3: Implement Jira Issue Fetching

**As a** user,
**I want** to see my Jira issues in the app,
**So that** I can track time against them.

**Acceptance Criteria:**

**Given** Jira is connected
**When** I open the Tasks panel
**Then** my assigned issues are fetched from Jira Cloud API v3
**And** issues are sorted by priority (Highest → Lowest)
**And** each issue shows: key, summary, status, priority icon
**And** loading state is shown while fetching
**And** errors show: "Failed to load issues" with retry button

---

### Story 5.4: Implement Task Data Caching for Offline

**As a** user,
**I want** my tasks to be available offline,
**So that** I can see and select them without internet.

**Acceptance Criteria:**

**Given** issues were fetched successfully
**When** I go offline
**Then** cached issues are displayed from local database
**And** cache shows "Last updated: {timestamp}"
**And** offline indicator appears on Tasks panel
**And** I can still select cached tasks for time entries
**And** cache is refreshed when back online

---

### Story 5.5: Implement Start Timer from Task

**As a** user,
**I want** to start a timer directly from a task,
**So that** tracking is quick and accurate.

**Acceptance Criteria:**

**Given** I see a list of tasks
**When** I click the play button on a task
**Then** a new timer starts immediately
**And** the time entry description is set to: "{JIRA-123}: {summary}"
**And** the task reference is stored with the entry
**And** if a timer is already running, it's stopped first
**And** the timer UI shows which task is being tracked

---

### Story 5.6: Implement Auto-Link Time Entry to Task

**As a** user,
**I want** my time entries to be automatically linked to tasks,
**So that** I don't need to manually associate them later.

**Acceptance Criteria:**

**Given** I started a timer from a task
**When** I stop the timer
**Then** the time entry is saved with task metadata (issueKey, issueId)
**And** the link is stored in plugin database (TaskTimeMapping table)
**And** linked entries show a Jira icon in the timesheet list
**And** clicking the icon opens the task details

---

### Story 5.7: Implement Task List Refresh

**As a** user,
**I want** to refresh my task list on demand,
**So that** I see newly assigned issues.

**Acceptance Criteria:**

**Given** I am viewing the Tasks panel
**When** I click the refresh button
**Then** issues are re-fetched from Jira API
**And** loading spinner appears during fetch
**And** new issues appear in the list
**And** removed/completed issues are updated
**And** periodic auto-refresh occurs every 15 minutes (configurable)

---

### Story 5.8: Display Task-Time Mapping

**As a** user,
**I want** to see which times are linked to which tasks,
**So that** I can verify my tracking is correct.

**Acceptance Criteria:**

**Given** I view a time entry detail
**When** the entry is linked to a task
**Then** it shows: "Linked to: JIRA-123 - {summary}"
**And** clicking opens a panel with task details (status, priority, assignee)
**And** I can unlink the entry if needed
**And** I can manually link an unlinked entry to a task

---

## Epic 6: Task Integration Plugin (GitHub & GitLab)

**Goal:** Extend task integration to GitHub and GitLab for developer workflow support.

**User Outcome:** Same task integration features work with GitHub Issues and GitLab Issues.

**FRs covered:** FR34, FR35

---

### Story 6.1: Implement GitHub OAuth Authentication

**As a** user,
**I want** to connect my GitHub account,
**So that** I can track time against GitHub Issues.

**Acceptance Criteria:**

**Given** I am in Task Integration settings
**When** I click "Connect GitHub"
**Then** OAuth 2.0 flow opens in browser
**And** I authenticate with my GitHub account
**And** tokens are securely stored (encrypted)
**And** connection status shows "Connected as {username}"
**And** I can select which repositories to track
**And** I can disconnect at any time

---

### Story 6.2: Implement GitHub Issue Fetching

**As a** user,
**I want** to see my GitHub Issues in the app,
**So that** I can track time against them.

**Acceptance Criteria:**

**Given** GitHub is connected
**When** I open the Tasks panel and select GitHub
**Then** issues from selected repositories are fetched via GitHub API v3/GraphQL v4
**And** issues show: number, title, labels, state
**And** issues are filtered to "assigned to me" or "created by me"
**And** loading and error states are handled
**And** issues integrate with the same Task panel UI as Jira

---

### Story 6.3: Implement GitLab OAuth Authentication

**As a** user,
**I want** to connect my GitLab account,
**So that** I can track time against GitLab Issues.

**Acceptance Criteria:**

**Given** I am in Task Integration settings
**When** I click "Connect GitLab"
**Then** OAuth 2.0 flow opens in browser (supports gitlab.com and self-hosted)
**And** I can enter custom GitLab instance URL
**And** tokens are securely stored (encrypted)
**And** connection status shows "Connected as {username} on {instance}"
**And** I can select which projects to track

---

### Story 6.4: Implement GitLab Issue Fetching

**As a** user,
**I want** to see my GitLab Issues in the app,
**So that** I can track time against them.

**Acceptance Criteria:**

**Given** GitLab is connected
**When** I open the Tasks panel and select GitLab
**Then** issues from selected projects are fetched via GitLab API v4
**And** issues show: IID, title, labels, state
**And** issues are filtered to "assigned to me"
**And** loading and error states are handled
**And** issues integrate with the same Task panel UI as Jira

---

### Story 6.5: Unified Task Provider Interface

**As a** user,
**I want** a unified view of all my tasks,
**So that** I can see Jira, GitHub, and GitLab issues together.

**Acceptance Criteria:**

**Given** multiple task providers are connected
**When** I view the Tasks panel
**Then** I can switch between providers via tabs or dropdown
**And** I can optionally view "All Tasks" combined view
**And** each task shows a provider icon (Jira, GitHub, GitLab)
**And** starting a timer works the same regardless of provider
**And** task linking works the same regardless of provider

---

## Epic 7: Advanced License Features

**Goal:** Enable trial licenses, team licensing, and administrative tracking.

**User Outcome:** Users can try plugins for 14 days, organizations can use Master Keys, admins can track activations.

**FRs covered:** FR24, FR25, FR27, FR28

---

### Story 7.1: Implement 14-Day Trial Activation

**As a** user,
**I want** to try a plugin for 14 days before purchasing,
**So that** I can evaluate if it meets my needs.

**Acceptance Criteria:**

**Given** I discover a premium plugin
**When** I click "Start 14-day Trial"
**Then** the plugin is activated immediately without payment
**And** a trial license is generated locally with 14-day expiration
**And** license status shows "Trial - 14 days remaining"
**And** full plugin functionality is available during trial
**And** trial countdown updates daily

---

### Story 7.2: Implement Trial Expiration Handling

**As a** user,
**I want** clear notification when my trial is expiring,
**So that** I can decide to purchase before losing access.

**Acceptance Criteria:**

**Given** I have a trial license
**When** 3 days remain
**Then** a notification appears: "Trial expires in 3 days"
**And** Settings shows prominent "Upgrade to Pro" button
**When** trial expires (0 days)
**Then** plugin features become disabled
**And** status shows "Trial Expired - Upgrade to continue"
**And** existing data is preserved (not deleted)

---

### Story 7.3: Implement Trial-to-Paid Conversion

**As a** user,
**I want** to seamlessly upgrade from trial to paid,
**So that** I don't lose any work or configuration.

**Acceptance Criteria:**

**Given** I have an active or expired trial
**When** I enter a valid license key
**Then** the trial converts to full license instantly
**And** all plugin data and settings are preserved
**And** status changes from "Trial" to "Active"
**And** no app restart is required
**And** user sees: "Successfully upgraded to Pro!"

---

### Story 7.4: Implement Master Key for Organizations

**As an** IT administrator,
**I want** to use a single Master Key for all employees,
**So that** license management is simplified.

**Acceptance Criteria:**

**Given** I purchased a team license (e.g., 15 seats)
**When** I receive a Master License Key
**Then** each employee can activate using the same key
**And** the server tracks activations against the seat limit
**And** if seats are full, new activation shows: "License limit reached"
**And** admin can deactivate devices to free seats

---

### Story 7.5: Implement License Activation Tracking

**As an** IT administrator,
**I want** to see which devices have activated licenses,
**So that** I can manage my organization's usage.

**Acceptance Criteria:**

**Given** I have a Master Key with team licenses
**When** I visit the License Portal (web)
**Then** I see a list of activated devices (hostname, user, date)
**And** I can see "X/Y seats used"
**And** I can deactivate a device to free its seat
**And** deactivated devices show "License deactivated" on next app start

---

## Epic 8: Time Tracking Enhancements

**Goal:** Improve the core time tracking experience with advanced features.

**User Outcome:** Users can pause/resume timers, see auto-assigned customers, get warned about unfinished entries, and use a minimal widget.

**FRs covered:** FR4, FR8, FR9, FR10

---

### Story 8.1: Implement Timer Pause and Resume

**As a** user,
**I want** to pause my timer during interruptions,
**So that** I don't track time when I'm not working on a task.

**Acceptance Criteria:**

**Given** I have an active running timer
**When** I click the "Pause" button
**Then** the timer stops counting but the entry remains open
**And** the UI shows "Paused" state with paused duration displayed
**And** a "Resume" button appears
**When** I click "Resume"
**Then** the timer continues from where it was paused
**And** total elapsed time excludes paused duration
**And** pause/resume events are logged for accuracy

---

### Story 8.2: Implement Auto-Assign Customer from Project

**As a** user,
**I want** the customer to be automatically assigned when I select a project,
**So that** I don't have to manually select both.

**Acceptance Criteria:**

**Given** I am creating or editing a time entry
**When** I select a project from the dropdown
**Then** the customer field is automatically populated based on project
**And** the customer field becomes read-only (greyed out)
**And** if project has no customer, the field remains empty
**And** changing project updates customer automatically

---

### Story 8.3: Detect Unfinished Entries from Previous Sessions

**As a** user,
**I want** to be warned about timers I forgot to stop,
**So that** I can correct my time entries.

**Acceptance Criteria:**

**Given** the application starts
**When** there are time entries without end time from previous sessions
**Then** a notification appears: "You have an unfinished entry from {date}"
**And** the notification shows entry details (project, activity, duration)
**And** user can choose: "Stop now", "Continue tracking", or "Discard"
**When** "Stop now" is selected
**Then** the entry is closed with end time = now
**And** user can edit the end time afterwards

---

### Story 8.4: Implement Unfinished Entry Quick Actions

**As a** user,
**I want** quick actions for unfinished entries,
**So that** I can resolve them efficiently.

**Acceptance Criteria:**

**Given** an unfinished entry notification is shown
**When** I click "Edit end time"
**Then** a time picker dialog opens pre-filled with reasonable estimate
**And** I can set the actual end time manually
**When** I click "Discard"
**Then** a confirmation appears: "Delete this entry?"
**And** on confirm, the entry is deleted
**When** I click "Continue"
**Then** the timer resumes from now (gap is acknowledged)

---

### Story 8.5: Implement Minimal Timer Widget Mode

**As a** user,
**I want** a small floating timer widget,
**So that** I can track time without the full app taking screen space.

**Acceptance Criteria:**

**Given** I have the app open with a running timer
**When** I click "Minimize to Widget" or use keyboard shortcut
**Then** the main window hides
**And** a small floating widget appears showing: elapsed time, project name
**And** the widget has Start/Stop button
**And** the widget stays on top of other windows (configurable)
**When** I click the widget
**Then** the full app window opens again

---

### Story 8.6: Widget Customization Options

**As a** user,
**I want** to customize the timer widget appearance,
**So that** it fits my workflow preferences.

**Acceptance Criteria:**

**Given** I am in Settings > Appearance
**When** I configure widget options
**Then** I can set widget position (corner of screen)
**And** I can toggle "Always on top" behavior
**And** I can choose widget size (compact/normal)
**And** I can set opacity level
**And** settings persist across app restarts

---

## Epic 9: Offline-First Sync System

**Goal:** Enable seamless offline work with reliable synchronization and conflict handling.

**User Outcome:** Full functionality offline, automatic sync on reconnection, visible sync status, and smart conflict resolution.

**FRs covered:** FR39, FR40, FR41, FR42, FR43, FR44, FR45, FR46

---

### Story 9.1: Implement Local SQLite Storage for Time Entries

**As a** user,
**I want** all my time entries stored locally,
**So that** my data is always available even without internet.

**Acceptance Criteria:**

**Given** I create, edit, or delete a time entry
**When** the operation completes
**Then** the entry is persisted in local SQLite database
**And** the database is located at `~/.kimai-client/data/kimai.db`
**And** entries include all fields (id, start, end, project, activity, description, etc.)
**And** entries include sync metadata (localId, serverId, syncStatus, lastModified)
**And** database operations use SQLDelight for type-safety

---

### Story 9.2: Implement Offline Queue for Unsynchronized Entries

**As a** user,
**I want** my changes queued when offline,
**So that** nothing is lost when I work without internet.

**Acceptance Criteria:**

**Given** I am offline (no server connection)
**When** I create, edit, or delete a time entry
**Then** the operation is saved locally with syncStatus = "PENDING"
**And** the entry is added to the sync queue
**And** I can continue working normally
**And** queued operations are stored in order (FIFO)
**And** the UI shows the entry with a "pending sync" indicator

---

### Story 9.3: Implement Automatic Sync on Reconnection

**As a** user,
**I want** my queued entries to sync automatically when back online,
**So that** I don't have to manually trigger synchronization.

**Acceptance Criteria:**

**Given** I have entries in the sync queue
**When** network connection is restored
**Then** sync starts automatically within 5 seconds
**And** queued entries are uploaded in order (oldest first)
**And** successful syncs update entry status to "SYNCED"
**And** a notification appears: "X entries synchronized"
**And** sync happens in background without blocking UI

---

### Story 9.4: Display Sync Status for Each Entry

**As a** user,
**I want** to see the sync status of each time entry,
**So that** I know which entries are saved on the server.

**Acceptance Criteria:**

**Given** I view the timesheet list
**When** entries are displayed
**Then** each entry shows a sync status icon:
**And** checkmark (green) = SYNCED
**And** arrow up = PENDING upload
**And** warning = FAILED
**And** hovering the icon shows details: "Synced at {timestamp}" or "Waiting to sync"
**And** status updates in real-time as sync occurs

---

### Story 9.5: Implement Server-Wins Conflict Resolution

**As a** user,
**I want** sync conflicts resolved automatically,
**So that** I don't lose data due to concurrent edits.

**Acceptance Criteria:**

**Given** an entry was modified both locally and on server
**When** sync attempts to upload the local version
**Then** server version timestamp is compared with local
**And** if server is newer, server version wins
**And** local changes are discarded with notification
**And** if local is newer and server unchanged, local wins
**And** conflict resolution is logged for debugging

---

### Story 9.6: Notify User of Conflicts Requiring Manual Resolution

**As a** user,
**I want** to be notified when conflicts need my attention,
**So that** I can decide how to resolve complex cases.

**Acceptance Criteria:**

**Given** a sync conflict cannot be auto-resolved
**When** the conflict is detected (e.g., both modified significantly)
**Then** a notification appears: "Sync conflict on entry {description}"
**And** user can view both versions (local vs server)
**And** user can choose: "Keep Local", "Keep Server", or "Merge"
**And** "Merge" opens edit dialog with both values visible
**And** resolved conflicts are marked as SYNCED

---

### Story 9.7: Cache Project, Activity, and Customer Lists

**As a** user,
**I want** project and activity lists available offline,
**So that** I can create entries without internet.

**Acceptance Criteria:**

**Given** the app fetches projects/activities/customers from server
**When** the fetch succeeds
**Then** data is cached in local SQLite database
**And** cache includes timestamp of last successful fetch
**When** I am offline
**Then** cached data is used for dropdowns
**And** dropdowns show: "Offline - using cached data"
**And** cache is refreshed automatically when back online

---

### Story 9.8: Display Offline Mode Indicator

**As a** user,
**I want** a clear visual indicator when I'm offline,
**So that** I understand why some features might be limited.

**Acceptance Criteria:**

**Given** the app cannot reach the Kimai server
**When** network check fails
**Then** a banner appears at the top: "Offline - changes will sync when connected"
**And** the banner is yellow/orange for visibility
**And** the system tray icon changes to show offline status
**When** connection is restored
**Then** the banner disappears
**And** a brief "Back online" notification appears
**And** tray icon returns to normal

---

## Epic 10: Settings & Preferences

**Goal:** Enable users to customize their application experience.

**User Outcome:** Users can set themes, defaults, auto-start, shortcuts, and notification preferences.

**FRs covered:** FR47, FR48, FR49, FR50, FR51, FR52, FR53

---

### Story 10.1: Implement Default Project Setting

**As a** user,
**I want** to set a default project for new time entries,
**So that** I don't have to select the same project repeatedly.

**Acceptance Criteria:**

**Given** I am in Settings > General
**When** I select a default project from the dropdown
**Then** the selection is saved locally
**And** new time entries are pre-filled with this project
**And** I can clear the default (set to "None")
**And** if the default project is deleted on server, setting is cleared
**And** default also sets the associated activity if only one exists

---

### Story 10.2: Implement Theme Selection

**As a** user,
**I want** to choose between Light, Dark, and System theme,
**So that** the app matches my visual preferences.

**Acceptance Criteria:**

**Given** I am in Settings > Appearance
**When** I select a theme option
**Then** "Light" applies light color scheme immediately
**And** "Dark" applies dark color scheme immediately
**And** "System" follows OS dark/light mode preference
**And** theme changes without app restart
**And** selected theme persists across sessions
**And** default is "System"

---

### Story 10.3: Implement Auto-Start on System Login

**As a** user,
**I want** the app to start automatically when I log in,
**So that** I don't forget to track my time.

**Acceptance Criteria:**

**Given** I am in Settings > General
**When** I enable "Start on system login"
**Then** the app registers with OS auto-start mechanism
**And** on Windows: adds to Startup folder or registry
**And** on macOS: adds to Login Items
**And** on Linux: creates .desktop autostart entry
**When** I disable the setting
**Then** auto-start registration is removed
**And** setting persists across app updates

---

### Story 10.4: Implement Inactivity Reminder

**As a** user,
**I want** to be reminded when I haven't tracked time,
**So that** I don't forget to log my work.

**Acceptance Criteria:**

**Given** I am in Settings > Notifications
**When** I enable "Inactivity reminder"
**Then** I can set threshold in minutes (default: 120)
**And** if no timer runs for {threshold} minutes during work hours
**Then** a notification appears: "No time tracked for 2 hours"
**And** clicking notification opens the app
**And** I can configure work hours (default: 9:00-17:00)
**And** reminders don't trigger outside work hours or on weekends

---

### Story 10.5: Implement Global Keyboard Shortcuts

**As a** user,
**I want** to control the timer with keyboard shortcuts,
**So that** I can start/stop without switching windows.

**Acceptance Criteria:**

**Given** I am in Settings > Keyboard Shortcuts
**When** I configure shortcuts
**Then** I can set "Start/Stop Timer" hotkey (default: Ctrl+Shift+T)
**And** I can set "Open App" hotkey (default: Ctrl+Shift+K)
**And** shortcuts work globally (even when app is minimized)
**And** conflicts with system shortcuts are detected and warned
**And** I can disable individual shortcuts
**And** shortcuts are platform-specific (Cmd on macOS)

---

### Story 10.6: Implement Settings Persistence

**As a** user,
**I want** all my settings saved automatically,
**So that** I don't have to reconfigure after restart.

**Acceptance Criteria:**

**Given** I change any setting in the app
**When** I navigate away or close the app
**Then** settings are automatically saved to local storage
**And** settings file is at `~/.kimai-client/settings.json`
**And** on app start, settings are loaded and applied
**And** corrupted settings file falls back to defaults
**And** settings include version for future migrations

---

### Story 10.7: Implement Notification Preferences

**As a** user,
**I want** to control which notifications I receive,
**So that** I'm not disturbed by unwanted alerts.

**Acceptance Criteria:**

**Given** I am in Settings > Notifications
**When** I configure notification preferences
**Then** I can toggle: "Timer reminders" (on/off)
**And** I can toggle: "Sync status notifications" (on/off)
**And** I can toggle: "Plugin updates" (on/off)
**And** I can toggle: "Sound effects" (on/off)
**And** I can set "Do not disturb" hours
**And** all toggles persist across sessions

---

## Epic 11: Onboarding Experience

**Goal:** Guide new users through app setup with a friendly wizard experience.

**User Outcome:** Easy step-by-step setup, helpful validation, celebration of first timer, and plugin discovery.

**FRs covered:** FR54, FR55, FR56, FR57, FR58, FR59

---

### Story 11.1: Implement First-Run Detection

**As a** new user,
**I want** the app to recognize it's my first time,
**So that** I'm guided through setup instead of seeing an empty screen.

**Acceptance Criteria:**

**Given** I launch the app for the first time
**When** no credentials are stored locally
**Then** the Setup Wizard screen is displayed automatically
**And** the main app is not accessible until setup completes
**And** a "first_run" flag is stored after successful setup
**And** subsequent launches go directly to login/home screen

---

### Story 11.2: Implement Setup Wizard - Server URL Step

**As a** new user,
**I want** to enter my Kimai server URL with validation,
**So that** I know immediately if the server is reachable.

**Acceptance Criteria:**

**Given** I am on Setup Wizard Step 1
**When** I enter a server URL
**Then** real-time validation occurs as I type (debounced 500ms)
**And** valid URL shows green checkmark: "Server found!"
**And** invalid URL shows red X: "Server not reachable"
**And** HTTPS is enforced (HTTP shows warning)
**And** common URL mistakes are auto-corrected (trailing slash, missing https)
**And** "Next" button is disabled until URL is valid

---

### Story 11.3: Implement Setup Wizard - API Token Step

**As a** new user,
**I want** help generating my API token,
**So that** I don't struggle to find where to create it.

**Acceptance Criteria:**

**Given** I am on Setup Wizard Step 2
**When** I need to enter my API token
**Then** I see clear instructions: "Enter your Kimai API token"
**And** a button "Generate Token" opens browser to `{server}/en/profile/api-token`
**And** a paste field accepts the token
**And** token is validated against the server immediately
**And** valid token shows: "Connected as {username}"
**And** invalid token shows: "Token invalid - please check and retry"

---

### Story 11.4: Implement Setup Wizard - Default Project Step

**As a** new user,
**I want** to select my default project during setup,
**So that** I can start tracking immediately after.

**Acceptance Criteria:**

**Given** I am on Setup Wizard Step 3
**When** my credentials are validated
**Then** my projects are loaded from the server
**And** I can select a default project from dropdown
**And** I can skip this step ("Set up later")
**And** selected project is saved as default setting
**And** if I have only one project, it's pre-selected

---

### Story 11.5: Implement Setup Wizard Completion

**As a** new user,
**I want** a clear confirmation that setup is complete,
**So that** I know I can start using the app.

**Acceptance Criteria:**

**Given** I complete all setup wizard steps
**When** I click "Finish Setup"
**Then** credentials are securely stored
**And** a success screen shows: "You're all set!"
**And** the screen shows a "Start your first timer" button
**And** initial data sync happens in background
**And** clicking the button opens the main app

---

### Story 11.6: Celebrate First Timer Start

**As a** new user,
**I want** positive feedback when I start my first timer,
**So that** I feel confident using the app.

**Acceptance Criteria:**

**Given** I have never started a timer before
**When** I start my first timer
**Then** a celebratory animation/confetti appears briefly
**And** a message shows: "You started your first timer!"
**And** the celebration only happens once (stored in settings)
**And** the celebration is subtle and non-intrusive (< 2 seconds)
**And** after celebration, normal timer UI is shown

---

### Story 11.7: Present Plugin Upsell After Onboarding

**As a** new user,
**I want** to discover available plugins naturally,
**So that** I can enhance my experience if interested.

**Acceptance Criteria:**

**Given** I have successfully completed onboarding
**When** I have used the app for a few minutes (or after first timer stop)
**Then** a non-intrusive card appears: "Boost your productivity"
**And** the card shows Task Integration plugin benefits
**And** I can click "Learn more" to see plugin details
**And** I can click "Try free for 14 days" to start trial
**And** I can dismiss with "Maybe later" (won't show again for 7 days)
**And** upsell respects "Do not disturb" notification settings

---

## Epic 12: Desktop System Integration

**Goal:** Integrate seamlessly with desktop environments on all platforms.

**User Outcome:** System tray access, notifications, deep links, and automatic update checks.

**FRs covered:** FR60, FR61, FR62, FR63, FR64, FR65, FR66

---

### Story 12.1: Implement Persistent System Tray Icon

**As a** user,
**I want** a system tray icon always visible,
**So that** I can access the app quickly from anywhere.

**Acceptance Criteria:**

**Given** the application is running
**When** it starts (or moves to background)
**Then** a system tray icon appears in the OS notification area
**And** the icon shows app logo in normal state
**And** the icon changes when timer is running (e.g., colored/animated)
**And** the icon works on Windows, macOS, and Linux
**And** right-clicking the icon shows context menu
**And** the icon persists until app is fully quit

---

### Story 12.2: Implement System Tray Quick Timer Actions

**As a** user,
**I want** to start/stop timers from the system tray,
**So that** I don't need to open the full app window.

**Acceptance Criteria:**

**Given** I right-click the system tray icon
**When** the context menu appears
**Then** I see "Start Timer" when no timer is running
**And** clicking "Start Timer" opens a quick project selector
**And** I see "Stop Timer ({elapsed})" when timer is running
**And** clicking "Stop Timer" stops and saves the entry
**And** I see "Recent Projects" submenu with last 5 projects
**And** selecting a recent project starts timer immediately

---

### Story 12.3: Implement Minimize to System Tray

**As a** user,
**I want** the app to minimize to tray instead of taskbar,
**So that** it doesn't clutter my taskbar.

**Acceptance Criteria:**

**Given** I have the app window open
**When** I click the minimize button
**Then** the window hides and only tray icon remains
**And** clicking tray icon restores the window
**When** I click the close button (X)
**Then** a setting determines behavior: "Minimize to tray" or "Quit"
**And** default is "Minimize to tray" with first-time explanation
**And** I can change this in Settings > General
**And** "Quit" option is always available in tray menu

---

### Story 12.4: Implement Desktop Notifications for Timer Reminders

**As a** user,
**I want** desktop notifications for timer events,
**So that** I stay aware of my time tracking.

**Acceptance Criteria:**

**Given** notification permissions are granted
**When** a timer reminder triggers
**Then** a native OS notification appears
**And** notification shows: title, message, and app icon
**And** clicking notification opens/focuses the app
**And** notifications work on Windows (Toast), macOS (Notification Center), Linux (libnotify)
**And** notifications respect OS "Do Not Disturb" mode
**And** notification sounds can be enabled/disabled in settings

---

### Story 12.5: Implement Sync Status Notifications

**As a** user,
**I want** notifications about sync status changes,
**So that** I know when my data is safely backed up.

**Acceptance Criteria:**

**Given** sync notifications are enabled in settings
**When** offline entries are successfully synced
**Then** notification shows: "X entries synchronized"
**When** sync fails after retries
**Then** notification shows: "Sync failed - will retry when online"
**And** clicking notification opens app with sync status visible
**When** conflict requires attention
**Then** notification shows: "Sync conflict needs your review"
**And** these notifications are less intrusive than timer reminders

---

### Story 12.6: Implement Deep Link Support

**As a** user,
**I want** to trigger app actions via URLs,
**So that** external tools can integrate with Kimai Client.

**Acceptance Criteria:**

**Given** the app registers `kimai://` protocol handler
**When** a deep link is opened (e.g., `kimai://start?project=123`)
**Then** the app opens (or focuses if already running)
**And** `kimai://start?project={id}` starts timer for project
**And** `kimai://stop` stops the current timer
**And** `kimai://open` simply opens/focuses the app
**And** invalid deep links show error notification
**And** deep links work from browser, terminal, and other apps

---

### Story 12.7: Implement Automatic Update Checker

**As a** user,
**I want** to be notified when updates are available,
**So that** I can keep the app current with latest features.

**Acceptance Criteria:**

**Given** update checking is enabled in settings (default: on)
**When** the app starts
**Then** it checks GitHub releases API for newer version
**And** if update available, notification shows: "Version X.Y available"
**And** clicking notification opens download page in browser
**And** check happens at most once per day
**And** I can disable update checks in Settings > General
**And** current version is shown in Settings > About
**And** "Check for updates" manual button is available

---

## Epic 13: Error Handling & Recovery

**Goal:** Provide clear error feedback and robust recovery mechanisms.

**User Outcome:** Understandable errors, preserved data during failures, automatic retries, and manual recovery options.

**FRs covered:** FR67, FR68, FR69, FR70, FR71, FR72

---

### Story 13.1: Implement User-Friendly Error Messages

**As a** user,
**I want** error messages that explain what went wrong and what to do,
**So that** I can resolve issues without technical knowledge.

**Acceptance Criteria:**

**Given** an error occurs in the application
**When** the error is displayed to the user
**Then** the message uses plain language (no technical jargon)
**And** the message explains what happened: "Could not save time entry"
**And** the message suggests action: "Check your internet connection and try again"
**And** a "Retry" button is shown where applicable
**And** a "Details" expandable shows technical info for support
**And** error dialogs have consistent styling across the app

---

### Story 13.2: Implement Connection Status Banner

**As a** user,
**I want** a clear indicator when the server is unreachable,
**So that** I understand why operations might fail.

**Acceptance Criteria:**

**Given** the app cannot reach the Kimai server
**When** connection check fails
**Then** a banner appears at the top of the screen
**And** banner shows: "Cannot connect to server" with yellow/orange background
**And** banner includes brief reason if known: "Network unavailable" or "Server timeout"
**And** banner has "Retry" button to manually check connection
**When** connection is restored
**Then** banner changes to green: "Connected" for 3 seconds, then disappears
**And** banner does not block UI interaction

---

### Story 13.3: Implement Local Data Preservation on Failure

**As a** user,
**I want** my data preserved when something fails,
**So that** I never lose my work.

**Acceptance Criteria:**

**Given** I perform an action (create/edit/delete entry)
**When** the operation fails (network error, server error)
**Then** my local data is NOT lost or corrupted
**And** the action is stored locally with PENDING status
**And** I can continue working with the local data
**And** failed operations are queued for retry
**And** user sees: "Saved locally - will sync when connected"
**And** app never shows empty state due to sync failures

---

### Story 13.4: Implement Automatic Retry with Exponential Backoff

**As a** user,
**I want** failed operations to retry automatically,
**So that** I don't have to manually retry each failure.

**Acceptance Criteria:**

**Given** a sync operation fails
**When** the failure is retryable (network error, timeout, 5xx)
**Then** automatic retry is scheduled with exponential backoff
**And** retry intervals: 5s, 10s, 30s, 60s, 300s (max 5 minutes)
**And** max retry attempts: 10 before giving up
**And** non-retryable errors (401, 403, 404) are not retried
**And** retry status is visible: "Retry in X seconds..."
**And** successful retry clears the error state

---

### Story 13.5: Implement Local Error Logging

**As a** user (or support),
**I want** errors logged locally for debugging,
**So that** issues can be diagnosed and fixed.

**Acceptance Criteria:**

**Given** any error or warning occurs
**When** it is logged
**Then** log entry includes: timestamp, level, message, stack trace
**And** logs are written to `~/.kimai-client/logs/kimai.log`
**And** log files are rotated daily, keeping 7 days
**And** sensitive data (tokens, passwords) are NEVER logged
**And** log level is configurable: ERROR, WARN, INFO, DEBUG
**And** "Export logs" button in Settings creates a zip for support

---

### Story 13.6: Implement Manual Sync Retry

**As a** user,
**I want** to manually trigger a sync retry,
**So that** I can force synchronization when I know the connection is back.

**Acceptance Criteria:**

**Given** I have pending or failed sync operations
**When** I click "Sync Now" button (in toolbar or Settings)
**Then** all pending operations are retried immediately
**And** a progress indicator shows: "Syncing X entries..."
**And** results are shown: "Synced X entries" or "Y entries failed"
**And** failed entries show individual error messages
**And** I can also pull-to-refresh on the timesheet list to trigger sync
**And** "Sync Now" is also available in system tray menu

---

### Story 13.7: Implement Error Recovery Suggestions

**As a** user,
**I want** the app to suggest recovery steps for common errors,
**So that** I can resolve issues independently.

**Acceptance Criteria:**

**Given** a known error pattern is detected
**When** the error is displayed
**Then** specific recovery suggestions are shown:
**And** "401 Unauthorized" shows "Your session expired. Please log in again." with Login button
**And** "Network timeout" shows "Server is slow. Check if Kimai is running." with Retry button
**And** "Disk full" shows "Not enough space. Free up disk space and retry."
**And** "Database locked" shows "Close other Kimai instances and retry."
**And** unknown errors show generic: "Something went wrong. Try restarting the app."
