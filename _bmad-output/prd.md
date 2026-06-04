---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
inputDocuments:
  - "_bmad-output/project-planning-artifacts/research/technical-plugin-architecture-research-2025-12-26.md"
  - "_bmad-output/project-planning-artifacts/research/market-time-tracking-research-2025-12-26.md"
  - "_bmad-output/analysis/brainstorming-session-2025-12-26.md"
  - "docs/index.md"
  - "docs/project-overview.md"
  - "docs/architecture.md"
  - "docs/development-guide.md"
  - "docs/state-management.md"
  - "docs/component-inventory.md"
  - "docs/source-tree-analysis.md"
documentCounts:
  briefs: 0
  research: 2
  brainstorming: 1
  projectDocs: 7
workflowType: 'prd'
lastStep: 11
project_name: 'kimai-client'
user_name: 'Dmitri'
date: '2025-12-26'
---

# Product Requirements Document - kimai-client

**Author:** Dmitri
**Date:** 2025-12-26

## Executive Summary

Kimai Client is an existing Kotlin Multiplatform desktop application for time tracking, currently supporting Windows, macOS, and Linux with Kimai Server integration. This PRD defines the evolution into a **universal, privacy-first time tracking platform** with plugin-based monetization and multi-platform support.

### Vision

> "The only Privacy-First Time Tracker that works with ANY backend - on EVERY platform"

### What We're Building

Transform the existing desktop client into:

1. **Plugin-Enabled Platform** - Extensible architecture allowing paid feature modules
2. **Multi-Platform Application** - Expand from Desktop to iOS & Android (App Stores)
3. **Universal Time Tracker** - Backend-agnostic design supporting multiple time tracking systems
4. **Monetization Engine** - License key-based plugin activation for sustainable business model

### Target Market

- **Primary:** KMU (Small/Medium Business, 5-50 employees)
- **Secondary:** Enterprise (50+) with hardware terminals
- **Freemium Entry:** Freelancers

### Business Model: Open Core

| Layer | License | Repository | Description |
|-------|---------|------------|-------------|
| **Core App** | AGPLv3 (Open Source) | kimai-client | Base time tracking, Kimai sync, offline mode |
| **Plugin API** | AGPLv3 (Open Source) | kimai-client | Extension points, interfaces, SDK |
| **Premium Plugins** | Proprietary (Closed Source) | Separate repos | Task Integration, Reports, HR Connectors |

**Strategie:**
- Core bleibt Open Source → Community, Vertrauen, Transparenz
- Neue Premium-Features nur als Plugins → Monetarisierung
- Plugin API ist offen → Drittanbieter können eigene Plugins entwickeln

### What Makes This Special

| Differentiator | Description | Competitive Advantage |
|----------------|-------------|----------------------|
| **Open Core Model** | Core app is AGPLv3 open source, premium features via closed-source plugins | Trust + Monetization balance |
| **Privacy-First** | No window/app monitoring - only Git & Calendar integration | Unique positioning vs. surveillance-based competitors |
| **Universal Backend** | Works with any time tracking system, not vendor-locked | Only solution offering true backend freedom |
| **Plugin Ecosystem** | Open, extensible architecture for community & paid extensions | Flexibility vs. closed competitor systems |
| **Hardware Integration** | Raspberry Pi terminals for physical check-in/out | Completely unique market offering |
| **Self-Hosted Option** | Full data sovereignty for privacy-conscious organizations | Enterprise compliance advantage |

### MVP Phases

| Phase | Scope | Outcome |
|-------|-------|---------|
| **Phase 1** | Plugin Foundation | Interface, Loader, License System, Extension Points |
| **Phase 2** | First Plugin | Task Integration (Jira/GitHub/GitLab) |
| **Phase 3** | Mobile Expansion | Android & iOS App Store releases |

## Project Classification

**Technical Type:** Desktop App → Multi-Platform (Desktop + Mobile)
**Domain:** General (Productivity / Time Tracking)
**Complexity:** Medium (Plugin architecture + KMP multi-platform)
**Project Context:** Brownfield - extending existing Kotlin Multiplatform system

### Technical Foundation

- **Existing Stack:** Kotlin 2.2.21, Compose Multiplatform 1.9.3, MVIKotlin 4.3.0, Decompose 3.4.0
- **Architecture:** MVI with Clean Architecture, Component-based Navigation
- **Recommended Plugin Framework:** PF4J with ServiceLoader pattern
- **License System:** ED25519/ECDSA signing with hybrid online/offline validation

## Success Criteria

### User Success

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Plugin Activation** | Automatic after payment | < 30 seconds from payment to active plugin |
| **Time to Value** | First tracked entry with plugin | < 5 minutes after activation |
| **Cross-Platform Stability** | Works on all platforms | Zero platform-specific crashes |
| **Aha-Moment** | Task linked to time entry | User sees Jira/GitHub issue connected to timesheet |

**User Success Statement:**
> "Users realize the value when they link their first Jira ticket to a time entry and see their work automatically tracked across platforms without manual data entry."

### Business Success

| Metric | 3 Months | 12 Months |
|--------|----------|-----------|
| **Paying Users** | 100 | 1,000 |
| **Plugin Revenue** | €500/mo | €5,000-8,000/mo |
| **Conversion Rate** | 5% free→paid | 10% free→paid |
| **Churn Rate** | < 10%/mo | < 5%/mo |

**Revenue Model:**
- Pro Tier: €5-8/user/month
- 1,000 paying users × €6 avg = €6,000/month target

### Technical Success

| Metric | Target | Validation |
|--------|--------|------------|
| **Plugin Stability** | 99.9% uptime | No crashes on Windows/macOS/Linux |
| **Activation Speed** | < 30 seconds | Payment → License → Active |
| **License Validation** | Online + Offline | Works without internet after initial activation |
| **Platform Parity** | Feature-equal | All plugins work identically across platforms |
| **Plugin Load Time** | < 2 seconds | App startup with plugins enabled |

### Measurable Outcomes

**MVP Success (Phase 1-2):**
- [ ] Plugin system loads without errors on all 3 desktop platforms
- [ ] License key activates plugin within 30 seconds of purchase
- [ ] Task Integration plugin connects to Jira/GitHub/GitLab
- [ ] 100 beta users testing plugin system

**Growth Success (Phase 3):**
- [ ] Mobile apps approved in App Store & Play Store
- [ ] Plugins work on iOS and Android
- [ ] 1,000 paying plugin users achieved
- [ ] < 5% monthly churn rate

## Product Scope

### MVP - Minimum Viable Product

**Phase 1: Plugin Foundation**

| Feature | Priority | Success Criteria |
|---------|----------|------------------|
| Plugin Interface | MUST | Plugins can register and initialize |
| Plugin Loader | MUST | JAR/AAR files discovered and loaded |
| License System | MUST | Keys validate online, cache offline |
| Settings Extension | MUST | Plugins add settings panels |
| Navigation Extension | MUST | Plugins add UI tabs |
| Timesheet Actions | MUST | Plugins add entry actions |

**Phase 2: First Plugin**

| Feature | Priority | Success Criteria |
|---------|----------|------------------|
| Plugin Database | MUST | Plugins store own data |
| Jira Integration | MUST | Load issues, link to time entries |
| GitHub Integration | SHOULD | Load issues, link to time entries |
| GitLab Integration | SHOULD | Load issues, link to time entries |

### Growth Features (Post-MVP)

| Phase | Feature | Business Value |
|-------|---------|----------------|
| **Phase 3** | Mobile Apps (iOS/Android) | App Store presence, wider reach |
| **Phase 4** | Reports & Analytics Plugin | Higher-value plugin tier |
| **Phase 5** | HR Backend Connectors | Enterprise customers |
| **Phase 6** | Git Auto-Tracking Plugin | Developer productivity |

### Vision (Future)

| Feature | Target Market | Differentiator |
|---------|---------------|----------------|
| Calendar Sync Plugin | All users | Automatic meeting time capture |
| Raspberry Pi Terminal | Enterprise | Physical check-in/out hardware |
| Universal Backend API | All users | True backend freedom |
| Team Management | KMU/Enterprise | Manager dashboards, capacity planning |

## User Journeys

### Journey 1: Freelancer Max - From Free to Paying Customer

Max is a freelance web developer managing 5-8 client projects simultaneously. He uses Kimai on his own server for time tracking, but constantly switching between Jira tickets and the time tracker frustrates him. Every evening he spends 15 minutes assigning his times to the correct tickets - time he'd rather spend with his family.

While searching for a better solution, he discovers Kimai Client and installs the free desktop app. Synchronization with his Kimai server works immediately. Then he sees in the Settings menu: "Task Integration Plugin - Jira, GitHub, GitLab". He clicks "14-day free trial".

After activation, he connects his Jira account. Suddenly he sees his current tickets directly in the app. With one click, he starts the timer for "PROJ-123: Homepage Redesign". The time is automatically linked to the ticket.

At the end of the trial period, Max calculates: 15 minutes × 20 work days = 5 hours/month saved. At his hourly rate of €80, that's €400 value - for €8/month. He buys the plugin without hesitation. Three months later, he recommends the app to two freelancer colleagues.

**Revealed Requirements:** Free tier with basic Kimai sync, Plugin discovery in settings, 14-day trial activation, Jira OAuth connection flow, One-click timer start from ticket, Automatic time-ticket linking, Simple payment flow, License activation < 30 seconds

### Journey 2: KMU Employee Sarah - Daily Workflow

Sarah works as a project manager at a 25-person agency. Every morning she opens her laptop and sees the Kimai Client icon in the system tray. The app starts automatically and shows her yesterday's entries - one is still open from last night when she forgot to stop it.

She corrects the time with two clicks and opens the Task Integration Panel. Her current Jira tickets are sorted by priority. She clicks on "AGENCY-456: Prepare customer meeting" and the timer starts. The app runs minimally in the background - only the small timer widget is visible.

During the customer meeting, she pauses the timer. Afterwards, she switches to "AGENCY-457: Meeting protocol" - another click. At the end of the day, she has 7 time entries, all automatically assigned to the correct projects and customers.

On Fridays, she exports her weekly overview for the team meeting. The project manager immediately sees that the "Homepage project" needs more time than planned - information that previously wouldn't have surfaced until month-end.

**Revealed Requirements:** System tray integration with auto-start, Unfinished entry reminder/correction, Task panel with priority sorting, One-click timer switch between tasks, Minimal timer widget mode, Pause/resume functionality, Automatic project/customer assignment, Weekly time export/overview

### Journey 3: IT Admin Thomas - Plugin Rollout

Thomas is IT manager at an engineering firm with 40 employees. Management has decided to buy the Task Integration Plugin for all developers - 15 licenses. Thomas must roll out the plugin without disrupting workflow.

He logs into the License Portal and purchases 15 plugin licenses. The system generates a Master License Key for his company. He copies the key and creates brief instructions for employees: "Activate plugin → Settings → Paste License Key → Done."

The first 5 developers activate their plugins on Monday. Thomas sees in the portal: "5/15 licenses activated". One developer reports - his plugin shows "Offline Mode". Thomas checks: The developer works from home without stable internet. No problem - the plugin works offline after initial activation.

After two weeks, all 15 licenses are active. Thomas receives an email: "3 more employees have requested the plugin." He purchases 3 additional licenses and expands the Master Key with two clicks.

**Revealed Requirements:** License Portal for bulk purchases, Master License Key for organizations, License activation tracking dashboard, Simple activation flow (copy-paste key), Offline mode after initial activation, License expansion workflow, Usage analytics for admins, Email notifications for license requests

### Journey 4: Team Lead Lisa - Oversight and Reporting (Post-MVP)

Lisa leads an 8-person development team. Every Monday she needs an overview: Who logged how much time on which projects? Previously, she had to export Kimai reports and merge them in Excel.

With the Reports Plugin, she opens the Team Dashboard. One click on "Last Week" shows her: Project A is at 120% of planned time, Project B at 80%. She clicks on Project A and sees that one developer logged 15 hours on a single ticket - something to address.

For the management meeting, she exports the report as PDF. The graphs show utilization per employee and project burndown. Management is impressed by the transparency.

At quarter-end, she uses the "Compare" function: Q3 vs Q4. The team has become 15% more efficient since introducing the Task Integration Plugin - measurable ROI for the plugin investment.

**Revealed Requirements:** Team Dashboard with time aggregation, Project time vs. planned comparison, Drill-down to individual entries, PDF export with visualizations, Employee utilization view, Period comparison (quarter over quarter), ROI tracking capabilities, Manager permission role

### Journey 5: New User Alex - First Run (Onboarding)

Alex just heard about Kimai Client from a colleague and downloads the app. After installation, he opens the app and sees a friendly welcome screen: "Connect your Time Tracker in 3 steps."

Step 1: Enter server URL. Alex types his Kimai URL - the app validates immediately and shows a green checkmark. Step 2: Enter API token. A link takes him directly to the Kimai page where he generates the token. Copy-paste, done. Step 3: Select first project. The app loads his projects and he chooses his main project as default.

"Done! Start your first timer." Alex clicks Play - and sees his first time entry live. He grins. That was easier than expected. The app offers: "Would you like to try the Task Integration Plugin?" - the upsell moment is naturally embedded.

**Revealed Requirements:** Step-by-step onboarding wizard, Real-time URL validation, Direct link to token generation, Default project selection, First timer celebration moment, Natural plugin upsell placement

### Journey 6: Sarah Forgets the Timer (Edge Case)

It's 2:30 PM and Sarah suddenly realizes: She hasn't had a timer running since the 11 AM meeting. Three hours of work - not recorded. Previously, she would have estimated and probably logged incorrectly.

She opens Kimai Client and immediately sees: "No active timer for 3h 24min." The app noticed. She clicks "Catch up time" and sees her calendar integrated: "Meeting with Customer X: 11:00-12:00". The app suggests booking this time automatically.

For the time after the meeting, the app shows her Git commits: "3 commits on PROJ-123 between 12:15 and 14:20". With one click, she adopts this time span. The gap is filled - accurate, not estimated.

Sarah thinks: "This app understands how I work." She activates the notification: "Remind me after 2 hours of inactivity."

**Revealed Requirements:** Inactivity detection and warning, "Catch up" time entry mode, Calendar integration for time suggestions, Git commit history for time reconstruction, Configurable inactivity reminders, One-click time gap filling

### Journey 7: CEO Klaus - Purchase Decision

Klaus runs an agency with 30 employees. His team uses Kimai, but developers complain about constantly switching between Jira and time tracking. Productivity suffers.

He googles "Kimai Jira Integration" and finds Kimai Client with Task Integration Plugin. The landing page shows: "15 minutes saved per developer per day." With 12 developers and €60 hourly rate, that's €2,700/month in recovered productivity.

Klaus starts a 14-day trial for his team. After a week, he asks the developers: "How's it going?" The response is clearly positive. He opens the admin dashboard and sees: 89% adoption, averaging 12 timer starts per person per day.

The calculation is simple: 12 licenses × €8 = €96/month for €2,700 in saved productivity. Klaus clicks "Upgrade to Pro" and enters the company credit card. Within 30 seconds, all trial accounts convert to full versions.

**Revealed Requirements:** ROI calculator on landing page, Team trial activation, Admin dashboard with adoption metrics, Usage statistics per team member, Seamless trial-to-paid conversion, Team billing (single invoice)

### Journey 8: Developer Tom - When Nothing Works (Error Recovery)

Tom starts his Monday and opens Kimai Client. Red banner: "Connection to Kimai Server failed." His heart sinks - did he lose his Friday times?

He clicks the banner and sees: "Last successful sync: Friday 17:45. 3 entries waiting for upload." His data is safe - stored locally. The app shows details: "Server unreachable. Ping failed."

Tom continues working in offline mode. He starts timers, switches projects - everything works. At 10:30 AM, the notification comes: "Connection restored. 5 entries synchronized." He checks Kimai in the browser - everything's there, including Friday's entries.

The Jira plugin briefly shows "Offline - local cache active" then loads current tickets. Tom thinks: "The app didn't let me down."

**Revealed Requirements:** Clear error state communication, Local data persistence, Pending sync queue visibility, Graceful offline mode, Automatic reconnection, Sync confirmation notification, Plugin offline fallback (cached data)

### Journey 9: Cross-Platform - Sarah on the Go (Post-MVP Vision)

*[Phase 3 - Mobile Expansion]*

Sarah is on the train to a customer meeting. On desktop, she had started the timer for "Meeting preparation". She opens the iOS app and sees: the same timer running - synchronized in real-time.

In the meeting, she stops the timer on her iPhone and starts "Customer conversation". Back in the office, she opens the desktop client - all mobile entries are there. Seamless.

**Revealed Requirements (Post-MVP):** Real-time cross-device sync, Consistent UI across platforms, Conflict resolution for simultaneous edits, Push notifications for timer reminders

### Journey 10: Plugin Developer Nina (Future Vision)

*[Future - Open Plugin Ecosystem]*

Nina is a developer at a company using Personio for HR. She wants to build a Personio plugin for Kimai Client.

She finds the Plugin Developer Documentation and SDK. An example plugin shows the basic structure. She creates a new project, implements the plugin interface, and tests locally in a sandbox.

After two weeks, she submits the plugin to the Marketplace. After review, "Personio Integration" is live - and she earns 70% of each sale.

**Revealed Requirements (Future):** Plugin SDK with documentation, Example plugin template, Local sandbox testing, Plugin marketplace submission, Revenue sharing model

### Journey Requirements Summary

| Journey | User Type | Priority | Key Capabilities |
|---------|-----------|----------|------------------|
| Max (Freelancer) | End User | MVP | Plugin trial, Payment flow, Jira OAuth |
| Sarah (Employee) | End User | MVP | Task panel, Timer widget, Auto-assignment |
| Thomas (IT-Admin) | Admin | MVP | License Portal, Bulk purchase, Activation tracking |
| Lisa (Team Lead) | Manager | Post-MVP | Team Dashboard, Reports, PDF export |
| Alex (Onboarding) | New User | MVP | Setup wizard, First run experience |
| Sarah (Forgotten Timer) | End User | MVP | Inactivity detection, Time recovery |
| Klaus (Purchase Decision) | Decision Maker | MVP | ROI calculator, Team trial |
| Tom (Error Recovery) | End User | MVP | Offline mode, Sync queue |
| Sarah (Cross-Platform) | End User | Post-MVP | Real-time sync |
| Nina (Plugin Dev) | Developer | Future | SDK, Marketplace |

## Innovation & Novel Patterns

### Detected Innovation Areas

**1. Privacy-First Paradigm**

Kimai Client challenges the industry assumption that effective time tracking requires surveillance. While competitors build increasingly invasive monitoring (screenshots, keystrokes, activity tracking), this product takes the opposite approach: explicit, user-controlled tracking with privacy by design.

**2. Universal Backend Architecture**

Breaking vendor lock-in through a plugin-based backend abstraction. Users can switch between time tracking systems (Kimai, Personio, custom APIs) without changing their workflow or losing data. This "universal adapter" approach is unprecedented in the time tracking market.

**3. Software + Hardware Ecosystem**

Unique market position combining desktop/mobile software with physical hardware terminals (Raspberry Pi). No competitor offers integrated physical check-in/out terminals with their time tracking software.

### Validation Approach

| Innovation | Validation Method |
|------------|------------------|
| Privacy-First | User surveys comparing comfort levels; adoption rates in privacy-conscious markets (Germany, EU) |
| Universal Backend | Beta program with users switching from other systems; API compatibility testing |
| Hardware Integration | Pilot with 3-5 companies; hardware reliability testing |

### Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Privacy-First may limit features | Git/Calendar integration provides rich data without surveillance |
| Universal Backend complexity | Start with Kimai, add backends incrementally via plugins |
| Hardware development cost | Use commodity hardware (Raspberry Pi); modular design |

## Desktop App Specific Requirements

### Project-Type Overview

Kimai Client is a Kotlin Multiplatform desktop application evolving into a cross-platform time tracking solution. The desktop foundation (Windows, macOS, Linux) will expand to mobile (iOS, Android) in Phase 3, sharing core business logic through KMP.

### Platform Support

| Platform | Status | Priority | Distribution |
|----------|--------|----------|--------------|
| **Windows** | Existing | High | MSI installer, GitHub Releases |
| **macOS** | Existing | High | DMG, App Store (future) |
| **Linux** | Existing | Medium | AppImage, Flatpak |
| **Android** | Phase 3 | High | Play Store |
| **iOS** | Phase 3 | High | App Store |

**Platform Parity Principle:** All features must work identically across platforms. Plugin functionality must be consistent.

### System Integration

| Integration | Platform | Implementation |
|-------------|----------|----------------|
| **System Tray** | All Desktop | Persistent icon, quick timer access |
| **Auto-Start** | All Desktop | Launch on login (configurable) |
| **Notifications** | All | Timer reminders, sync status, inactivity alerts |
| **Global Hotkeys** | Desktop | Start/stop timer (configurable keys) |
| **Deep Links** | All | `kimai://` protocol for external triggers |

**Existing Implementation:**
- System tray with menu (start/stop, recent entries)
- Desktop notifications via Compose Multiplatform
- Window minimize to tray

**Planned Additions:**
- Global keyboard shortcuts (Phase 1)
- Deep link handling for plugin triggers
- Calendar app integration hooks

### Update Strategy

| Aspect | Approach |
|--------|----------|
| **Desktop Updates** | In-app update checker with user prompt |
| **Update Frequency** | Semantic versioning, monthly releases |
| **Rollback** | Keep previous version for manual rollback |
| **Plugin Updates** | Independent from app updates, version compatibility check |
| **Mobile Updates** | App Store / Play Store managed |

**Update Flow:**
1. App checks for updates on startup (configurable)
2. User sees notification: "New version available"
3. One-click download and install
4. Restart required for major updates
5. Plugin compatibility verified before app update

### Offline Capabilities

| Capability | Offline Support | Sync Behavior |
|------------|-----------------|---------------|
| **Time Tracking** | Full | Queue entries for upload |
| **Timer Start/Stop** | Full | Local state, sync on reconnect |
| **Project/Activity List** | Cached | Last-known data, refresh on connect |
| **Plugin Features** | Partial | Cached task data, no live queries |
| **License Validation** | Cached | 30-day offline grace period |

**Offline Architecture:**
- SQLite local database (existing via SQLDelight)
- Sync queue with conflict resolution (server wins)
- Visual indicator for offline status
- Automatic reconnection with exponential backoff

**Sync Conflict Resolution:**
- Server timestamp wins for same entry
- Local changes preserved if server entry unchanged
- User notification for conflicts requiring manual resolution

### Technical Architecture Considerations

**Existing Stack Leverage:**
- Compose Multiplatform for shared UI
- MVIKotlin for state management (works offline naturally)
- Decompose for navigation (stateful, preserves across reconnect)
- SQLDelight for local persistence
- Store5 for caching layer

**Plugin Architecture Integration:**
- Plugins loaded via PF4J on desktop (JAR files)
- Plugin API must handle offline gracefully
- Extension points work with cached data when offline

### Implementation Considerations

**Phase 1 (Plugin Foundation) Desktop Focus:**
- Plugin loader for JVM/Desktop first
- License validation with offline caching
- Extension points for Settings, Navigation, Timesheet Actions

**Phase 3 (Mobile) Considerations:**
- Shared business logic via KMP `commonMain`
- Platform-specific plugin loading (AAR for Android, Framework for iOS)
- App Store compliance for in-app purchases
- Push notifications for timer reminders

**Performance Targets:**

| Metric | Target |
|--------|--------|
| App startup | < 3 seconds |
| Timer start/stop | < 100ms response |
| Plugin load | < 2 seconds per plugin |
| Sync operation | Background, non-blocking |
| Memory usage | < 200MB base, +50MB per plugin |

## Project Scoping & Phased Development

### MVP Strategy & Philosophy

**MVP Approach:** Platform MVP - Build extensible foundation for plugin-based monetization

**Strategic Rationale:**
1. Plugin architecture is the core business enabler
2. First plugin validates the extension model
3. Mobile expansion leverages shared platform code
4. Revenue validation before major platform expansion

**Resource Requirements:**
- Solo developer (Dmitri) - realistic constraint acknowledged
- Phased approach to manage complexity
- Focus on one phase at a time

### MVP Feature Set (Phase 1: Plugin Foundation)

**Core User Journeys Supported:**
- Journey 5: Alex (Onboarding) - Basic app setup
- Journey 8: Tom (Error Recovery) - Offline mode
- Partial Journey 1: Max (Freelancer) - Plugin discovery

**Must-Have Capabilities:**

| Feature | Justification |
|---------|---------------|
| Plugin Interface | Core platform requirement |
| Plugin Loader | Load JAR files, lifecycle management |
| License Key System | Monetization enabler |
| Settings Extension | Plugins need configuration UI |
| Navigation Extension | Plugins need UI presence |
| Timesheet Actions | Primary integration point |

**Explicitly Out of Scope (Phase 1):**
- Mobile apps
- Multiple backend support
- Reports plugin
- Hardware integration
- Team management features

### Phase 2: First Premium Plugin (Task Integration)

**User Journeys Enabled:**
- Journey 1: Max (Freelancer) - Full conversion journey
- Journey 2: Sarah (Employee) - Daily workflow with tasks
- Journey 3: Thomas (IT-Admin) - License management
- Journey 6: Sarah (Forgotten Timer) - Time recovery
- Journey 7: Klaus (Purchase Decision) - Team trial

**Capabilities:**

| Feature | Priority |
|---------|----------|
| Plugin Database Access | MUST |
| Jira Integration | MUST |
| GitHub Integration | SHOULD |
| GitLab Integration | SHOULD |
| License Portal | MUST |
| Team Licensing | SHOULD |

### Phase 3: Mobile Expansion

**User Journeys Enabled:**
- Journey 9: Sarah (Cross-Platform) - Real-time sync

**Capabilities:**
- Android app (Play Store)
- iOS app (App Store)
- Plugin support on mobile
- Real-time cross-device sync

### Future Phases (Post-MVP Roadmap)

| Phase | Focus | Key Features |
|-------|-------|--------------|
| **Phase 4** | Reports Plugin | Team Dashboard, PDF export, Analytics |
| **Phase 5** | HR Backends | Personio, BambooHR connectors |
| **Phase 6** | Auto-Tracking | Git commits, Calendar sync plugins |
| **Phase 7** | Hardware | Raspberry Pi terminal |
| **Phase 8** | Universal Backend | Backend abstraction layer |

### Risk Mitigation Strategy

**Technical Risks:**

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Plugin isolation complexity | Medium | High | Start with trusted plugins only, no sandbox |
| Cross-platform parity | Medium | Medium | KMP shared code, platform-specific adapters |
| License validation reliability | Low | High | Hybrid online/offline with 30-day cache |

**Market Risks:**

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Competition (Toggl, Clockify) | High | Medium | Privacy-First positioning differentiates |
| Limited adoption | Medium | High | Start with KMU niche, expand later |
| Plugin demand uncertain | Medium | High | Validate with Task Integration first |

**Resource Risks:**

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Solo developer burnout | High | Critical | Phased approach, one phase at a time |
| Scope creep | Medium | High | Strict MVP boundaries, defer to future phases |
| Technical debt | Medium | Medium | Clean architecture, tests before features |

### Success Gates Between Phases

| Gate | Criteria to Proceed |
|------|---------------------|
| Phase 1 → 2 | Plugin system stable, 1 example plugin works |
| Phase 2 → 3 | 100 beta users, Task Integration validated |
| Phase 3 → 4 | Mobile apps in stores, 500+ users |
| Phase 4+ | 1,000 paying users achieved |

## Functional Requirements

### Time Tracking Core (FR1-FR10)

| ID | Requirement | Priority | Phase | User Journey |
|----|-------------|----------|-------|--------------|
| **FR1** | System shall allow users to start a timer with one click from the main interface | MUST | 1 | Sarah, Max |
| **FR2** | System shall allow users to stop an active timer and save the time entry | MUST | 1 | Sarah, Max |
| **FR3** | System shall display the current running timer with elapsed time in real-time | MUST | 1 | Sarah |
| **FR4** | System shall allow users to pause and resume a timer without losing data | SHOULD | 1 | Sarah |
| **FR5** | System shall allow users to edit existing time entries (start time, end time, description) | MUST | 1 | Sarah, Alex |
| **FR6** | System shall allow users to delete time entries with confirmation | MUST | 1 | - |
| **FR7** | System shall allow users to assign activities and projects to time entries | MUST | 1 | Sarah |
| **FR8** | System shall automatically assign customer based on selected project | SHOULD | 1 | Sarah |
| **FR9** | System shall detect and warn about unfinished entries from previous sessions | SHOULD | 1 | Sarah |
| **FR10** | System shall provide a minimal timer widget mode for non-intrusive tracking | SHOULD | 2 | Sarah |

### Plugin System (FR11-FR20)

| ID | Requirement | Priority | Phase | User Journey |
|----|-------------|----------|-------|--------------|
| **FR11** | System shall load plugins from designated JAR files on application startup | MUST | 1 | - |
| **FR12** | System shall provide a Plugin interface with `id`, `name`, `version`, `init()`, `dispose()` | MUST | 1 | Nina |
| **FR13** | System shall validate plugin compatibility with current app version before loading | MUST | 1 | Thomas |
| **FR14** | System shall provide Settings Extension Point for plugins to register configuration panels | MUST | 1 | Max |
| **FR15** | System shall provide Navigation Extension Point for plugins to add UI tabs/panels | MUST | 1 | Max, Sarah |
| **FR16** | System shall provide Timesheet Actions Extension Point for plugins to add entry actions | MUST | 1 | Sarah |
| **FR17** | System shall provide Database Extension Point for plugins to store custom data | MUST | 2 | Nina |
| **FR18** | System shall provide HTTP Client Extension Point for plugins to make backend calls | SHOULD | 2 | Nina |
| **FR19** | System shall gracefully handle plugin failures without crashing the main application | MUST | 1 | Tom |
| **FR20** | System shall allow users to enable/disable individual plugins | SHOULD | 2 | Thomas |

### License & Monetization (FR21-FR29)

| ID | Requirement | Priority | Phase | User Journey |
|----|-------------|----------|-------|--------------|
| **FR21** | System shall validate license keys via online license server | MUST | 1 | Max, Thomas |
| **FR22** | System shall cache valid licenses for offline use (30-day grace period) | MUST | 1 | Thomas, Tom |
| **FR23** | System shall activate plugins automatically upon valid license entry | MUST | 1 | Max |
| **FR24** | System shall support both individual and organizational (Master Key) licenses | SHOULD | 2 | Thomas |
| **FR25** | System shall provide 14-day trial activation for plugins | MUST | 2 | Max, Klaus |
| **FR26** | System shall display clear license status (Active, Trial, Expired, Offline Mode) | MUST | 1 | Thomas |
| **FR27** | System shall convert trial licenses to full licenses seamlessly after payment | SHOULD | 2 | Max, Klaus |
| **FR28** | System shall provide license activation tracking for administrators | SHOULD | 2 | Thomas |
| **FR29** | System shall use ED25519/ECDSA cryptographic signing for license validation | MUST | 1 | - |

### Task Integration Plugin (FR30-FR38)

| ID | Requirement | Priority | Phase | User Journey |
|----|-------------|----------|-------|--------------|
| **FR30** | Plugin shall connect to Jira via OAuth authentication | MUST | 2 | Max |
| **FR31** | Plugin shall display user's current Jira issues sorted by priority | MUST | 2 | Sarah |
| **FR32** | Plugin shall allow starting a timer directly from a task/issue | MUST | 2 | Max, Sarah |
| **FR33** | Plugin shall automatically link time entries to tasks/issues | MUST | 2 | Max |
| **FR34** | Plugin shall support GitHub Issues integration | SHOULD | 2 | Max |
| **FR35** | Plugin shall support GitLab Issues integration | SHOULD | 2 | Max |
| **FR36** | Plugin shall cache task data for offline access | MUST | 2 | Tom |
| **FR37** | Plugin shall refresh task list on demand and periodically | SHOULD | 2 | Sarah |
| **FR38** | Plugin shall display task-to-time mapping in entry details | SHOULD | 2 | Lisa |

### Data Synchronization & Offline (FR39-FR46)

| ID | Requirement | Priority | Phase | User Journey |
|----|-------------|----------|-------|--------------|
| **FR39** | System shall store all time entries in local SQLite database | MUST | 1 | Tom |
| **FR40** | System shall queue unsynchronized entries for upload when offline | MUST | 1 | Tom |
| **FR41** | System shall automatically sync queued entries when connection restored | MUST | 1 | Tom |
| **FR42** | System shall display sync status (synced, pending, failed) for each entry | MUST | 1 | Tom |
| **FR43** | System shall resolve sync conflicts using server-wins strategy | MUST | 1 | - |
| **FR44** | System shall notify users of conflicts requiring manual resolution | SHOULD | 2 | Tom |
| **FR45** | System shall cache project, activity, and customer lists for offline use | MUST | 1 | Tom |
| **FR46** | System shall display clear offline mode indicator in UI | MUST | 1 | Tom |

### User Settings & Preferences (FR47-FR53)

| ID | Requirement | Priority | Phase | User Journey |
|----|-------------|----------|-------|--------------|
| **FR47** | System shall allow users to set default project for new time entries | MUST | 1 | Alex |
| **FR48** | System shall allow users to select theme (Light/Dark/System) | MUST | 1 | - |
| **FR49** | System shall allow users to configure auto-start on system login | SHOULD | 1 | Sarah |
| **FR50** | System shall allow users to configure inactivity reminder threshold | SHOULD | 2 | Sarah |
| **FR51** | System shall allow users to configure global keyboard shortcuts | SHOULD | 2 | - |
| **FR52** | System shall persist all settings locally across sessions | MUST | 1 | - |
| **FR53** | System shall allow users to configure notification preferences | SHOULD | 2 | Sarah |

### Onboarding & Setup (FR54-FR59)

| ID | Requirement | Priority | Phase | User Journey |
|----|-------------|----------|-------|--------------|
| **FR54** | System shall display step-by-step setup wizard on first run | MUST | 1 | Alex |
| **FR55** | System shall validate Kimai server URL in real-time during setup | MUST | 1 | Alex |
| **FR56** | System shall provide direct link to generate API token in Kimai | SHOULD | 1 | Alex |
| **FR57** | System shall allow selection of default project during onboarding | SHOULD | 1 | Alex |
| **FR58** | System shall celebrate successful first timer start | SHOULD | 1 | Alex |
| **FR59** | System shall naturally present plugin upsell after successful onboarding | SHOULD | 2 | Alex |

### System Integration (FR60-FR66)

| ID | Requirement | Priority | Phase | User Journey |
|----|-------------|----------|-------|--------------|
| **FR60** | System shall display persistent system tray icon on all desktop platforms | MUST | 1 | Sarah |
| **FR61** | System shall allow quick timer start/stop from system tray menu | MUST | 1 | Sarah |
| **FR62** | System shall minimize to system tray instead of closing | SHOULD | 1 | Sarah |
| **FR63** | System shall send desktop notifications for timer reminders | SHOULD | 2 | Sarah |
| **FR64** | System shall send notifications for sync status changes | SHOULD | 2 | Tom |
| **FR65** | System shall support deep links (`kimai://`) for external triggers | COULD | 2 | - |
| **FR66** | System shall check for app updates on startup (configurable) | SHOULD | 2 | - |

### Error Handling & Recovery (FR67-FR72)

| ID | Requirement | Priority | Phase | User Journey |
|----|-------------|----------|-------|--------------|
| **FR67** | System shall display clear error messages with actionable information | MUST | 1 | Tom |
| **FR68** | System shall provide connection status banner when server unreachable | MUST | 1 | Tom |
| **FR69** | System shall preserve local data when connection fails | MUST | 1 | Tom |
| **FR70** | System shall automatically retry failed sync operations with exponential backoff | MUST | 1 | Tom |
| **FR71** | System shall log errors locally for debugging purposes | SHOULD | 1 | - |
| **FR72** | System shall allow users to manually trigger sync retry | SHOULD | 2 | Tom |

### Functional Requirements Summary

| Category | Count | MVP Phase |
|----------|-------|-----------|
| Time Tracking Core | 10 | Phase 1-2 |
| Plugin System | 10 | Phase 1-2 |
| License & Monetization | 9 | Phase 1-2 |
| Task Integration | 9 | Phase 2 |
| Data Sync & Offline | 8 | Phase 1-2 |
| User Settings | 7 | Phase 1-2 |
| Onboarding | 6 | Phase 1-2 |
| System Integration | 7 | Phase 1-2 |
| Error Handling | 6 | Phase 1-2 |
| **Total** | **72** | - |

**Priority Distribution:**
- MUST: 42 requirements (58%)
- SHOULD: 26 requirements (36%)
- COULD: 4 requirements (6%)

## Non-Functional Requirements

### Performance

| NFR-ID | Requirement | Measurement | Target |
|--------|-------------|-------------|--------|
| **NFR-P1** | App startup time | Cold start to usable UI | < 3 seconds |
| **NFR-P2** | Timer start/stop response | Click to visual feedback | < 100ms |
| **NFR-P3** | Plugin loading time | Per plugin initialization | < 2 seconds |
| **NFR-P4** | Memory footprint (base app) | RAM usage without plugins | < 200MB |
| **NFR-P5** | Memory footprint (per plugin) | Additional RAM per plugin | < 50MB |
| **NFR-P6** | Sync operation | Background, non-blocking | No UI freeze |
| **NFR-P7** | UI responsiveness | Frame rate during interactions | 60 FPS |

### Security

| NFR-ID | Requirement | Implementation |
|--------|-------------|----------------|
| **NFR-S1** | API tokens must be encrypted at rest | AES-256 encryption via Cryptography library |
| **NFR-S2** | License keys must use cryptographic signing | ED25519 or ECDSA signatures |
| **NFR-S3** | All network communication must use TLS | HTTPS only, TLS 1.2+ |
| **NFR-S4** | Credentials must not be logged | Sanitized logging, no secrets in logs |
| **NFR-S5** | Plugin isolation | Plugins cannot access other plugins' data |
| **NFR-S6** | License validation must be tamper-resistant | Signed license files, server verification |
| **NFR-S7** | Secure credential storage | Platform-specific secure storage (Keychain/Credential Manager) |

**GDPR Compliance:**
- No personal data transmitted without user consent
- Data stored locally, user controls sync
- Export/delete user data functionality

### Reliability

| NFR-ID | Requirement | Measurement | Target |
|--------|-------------|-------------|--------|
| **NFR-R1** | Data persistence | Zero data loss during crashes | 100% entries recoverable |
| **NFR-R2** | Offline operation | Full time tracking without network | 100% feature availability |
| **NFR-R3** | Sync reliability | Successful sync after reconnection | > 99.9% |
| **NFR-R4** | Plugin fault tolerance | App stability with faulty plugin | App must not crash |
| **NFR-R5** | License offline grace | Days of offline plugin usage | 30 days |
| **NFR-R6** | Auto-reconnect | Automatic recovery after network loss | Exponential backoff, max 5 min |

### Integration

| NFR-ID | Requirement | Specification |
|--------|-------------|---------------|
| **NFR-I1** | Kimai API compatibility | Support Kimai API v1 (current) |
| **NFR-I2** | Jira API compatibility | Jira Cloud REST API v3 |
| **NFR-I3** | GitHub API compatibility | GitHub REST API v3, GraphQL v4 |
| **NFR-I4** | GitLab API compatibility | GitLab REST API v4 |
| **NFR-I5** | OAuth 2.0 support | Standard OAuth 2.0 flow for integrations |
| **NFR-I6** | API rate limiting handling | Graceful degradation, user notification |
| **NFR-I7** | Webhook support (future) | Support for real-time updates via webhooks |

### Accessibility

| NFR-ID | Requirement | Standard |
|--------|-------------|----------|
| **NFR-A1** | Keyboard navigation | All features accessible via keyboard |
| **NFR-A2** | Screen reader support | Labels and ARIA-equivalent for Compose |
| **NFR-A3** | High contrast mode | Readable with system high contrast |
| **NFR-A4** | Font scaling | Respect system font size settings |
| **NFR-A5** | Focus indicators | Visible focus states for all interactive elements |

### Cross-Platform Compatibility

| NFR-ID | Requirement | Platforms                 | Target |
|--------|-------------|---------------------------|--------|
| **NFR-C1** | Windows support | Windows 10, 11            | Full feature parity |
| **NFR-C2** | macOS support | macOS 11+ (Big Sur+)      | Full feature parity |
| **NFR-C3** | Linux support | Ubuntu 20.04+, Fedora 35+ | Full feature parity |
| **NFR-C4** | JVM requirement | Java 21+                   | Bundled JRE |
| **NFR-C5** | Screen resolution | 1280x720 minimum          | Responsive layout |
| **NFR-C6** | HiDPI support | 4K displays               | Native scaling |

### NFR Summary

| Category | Count | Priority |
|----------|-------|----------|
| Performance | 7 | High |
| Security | 7 | Critical |
| Reliability | 6 | Critical |
| Integration | 7 | High |
| Accessibility | 5 | Medium |
| Cross-Platform | 6 | High |
| **Total** | **38** | - |
