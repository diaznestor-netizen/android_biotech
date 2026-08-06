# Changelog - BioTech Android

All notable changes to the BioTech Android project.

## [1.1.0-alpha] - 2026-08-03

### Added
- **GlobalSyncManager:** Centralized synchronization engine for all industrial entities.
- **Hierarchical Sync:** Support for dependencies between entities (e.g., Machines wait for Projects).
- **Observability Framework:** Advanced integration with Firebase Crashlytics using custom industrial keys.
- **Diagnostic Panel:** New developer/support screen for real-time sync monitoring and health checks.
- **Multi-entity Handlers:** Specialized sync handlers for Projects, Machines, Inspections, Incidents, Missions, Activities, and Goals.
- **Telemetría:** Automated tracking of sync duration, HTTP status codes, and trace IDs.

### Changed
- **Unified Sync Flow:** Migrated all repositories to a single operation queue based on `sync_operations`.
- **GlobalStatusBar:** Enhanced UI indicator with specific states for pending (🕓), errors (⚠️), and conflicts (🚫).
- **Offline-First:** Full background synchronization support for Activities and Goals.

### Fixed
- **Inconsistent Sync:** Resolved critical issue where Machines and Inspections were ignored by the legacy worker.
- **Concurrency Locks:** Implemented `Mutex` and `ExistingWorkPolicy.KEEP` to prevent race conditions during sync.
- **Data Integrity:** Improved conflict resolution by capturing remote state automatically on 409 errors.

### Security
- **Strict Mode:** Enabled thread and VM policy detection in debug builds to ensure UI fluidity.
- **LeakCanary:** Integrated for automated memory leak detection in critical flows.
- **Encrypted Session:** Migrated session persistence to a more robust structure preparing for Jetpack DataStore.

## [1.0.0] - 2026-07-26

### Added
- **Industrial Premium Design System:** New palette (Primary Green #22C55E, Primary Blue #2563EB, Primary Cyan #06B6D4) and unifed typography/shapes.
- **Login Redesign:** Glassmorphism card, deep technical gradients, and fluid entrance animations (Fade + Scale + Slide).
- **BioTech UI Library:** Standardized components for buttons, cards, text fields, indicators, and states.
- **Biometric Support:** Integrated Fingerprint and Face Unlock support in the Login screen.
- **Real-time Audit Timeline:** Operational timeline for tracking actions with status badges.
- **Premium Motion:** Added micro-interactions (press scale, elevation) and staggered entrance animations for lists.
- **Android 15 Compatibility:** Updated target and compile SDK to level 35.

### Changed
- **Dashboard:** Modernized KPIs and hero summary cards with optimized recomposition logic.
- **Project & Machine Modules:** Complete visual overhaul of list and detail screens using standardized `BioTechCard` and `KPIWidget`.
- **Material Inventory:** Enhanced visual stock indicators (🟢🟡🔴) and refined search functionality.
- **User Management:** Premium list layout with avatars and role badges.
- **Navigation Shell:** Implemented `BioTechBottomBar` and `BioTechTopBar` with smooth state transitions.

### Fixed
- Migrated all deprecated Material Symbols icons to `AutoMirrored` versions.
- Cleaned up navigation placeholders, replacing them with professional `PlaceholderScreen` views.
- Optimized `LazyColumn` and `LazyGrid` performance using stable keys.
- Redacted sensitive headers in network logs for production security.

### Optimized
- Enabled R8 minification and resource shrinking for optimized APK size.
- Configured production ProGuard rules for Room, Retrofit, Hilt, and WorkManager stability.
