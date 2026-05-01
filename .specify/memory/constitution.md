# [PROJECT_NAME] Constitution
<!-- Example: Spec Constitution, TaskFlow Constitution, etc. -->

## Core Principles

### I. Multiplatform-First
All business logic, data models, and networking must reside in the `shared` module's `commonMain` source set. Platform-specific code (`androidMain`, `iosMain`, etc.) is reserved for APIs that lack a multiplatform equivalent or require native integration.

### II. Expect/Actual Minimalism
The use of `expect`/`actual` declarations should be minimized in favor of interface-based dependency injection. Interfaces defined in `commonMain` should be implemented in platform-specific modules to promote testability and cleaner architecture.

### III. Library-First Architecture
The project is structured as a set of focused, independent libraries/modules. Core logic is decoupled from UI frameworks (Compose Multiplatform, SwiftUI). Each module must have a clear responsibility and maintain its own test suite.

### IV. Build Must Stay Green (NON-NEGOTIABLE)
The repository must build and tests must pass on every change:
- No merging with failing CI.
- Fix-forward immediately if main is broken.
- Prefer small, incremental PRs.

### V. Deterministic, Testable Shared Logic
Shared business logic must be unit-testable without devices/emulators:
- Unit tests for shared modules are required for non-trivial logic.
- Use fakes/mocks at boundaries; avoid direct IO in core logic.
- Keep concurrency predictable; avoid hidden global state.

## Technology Stack

### Core Requirements
- **Kotlin**: 2.2.0 or newer
- **Build System**: Gradle with Kotlin DSL (`.gradle.kts`)
- **Dependency Injection**: Koin or manual constructor injection
- **Networking**: Ktor
- **Serialization**: kotlinx.serialization

### Target Constraints
- **Android**: Min SDK 31 (Android 12.0)
- **iOS**: Deployment Target 17.0 or newer
- **Desktop/Web**: (Optional) Follow Compose Multiplatform compatibility guidelines

## Development Workflow
- Format/lint checks pass.
- All unit tests pass (shared + platform as applicable).

## Branching
- Work in short-lived branches; rebase/merge frequently.

## PR Requirements
- Description of change + affected platforms.
- Tests included/updated for shared logic changes.
- No new warnings introduced without justification.

### Quality Gates
- Code style must follow the official Kotlin Coding Conventions.
- Format/lint checks pass.
- All unit tests pass (shared + platform as applicable).

## Governance
This constitution defines the architectural boundaries and technical requirements of the project. Amendments require a consensus among lead developers and a documented migration plan for affected modules.

**Version**: 1.0.0 | **Ratified**: 2026-05-01 | **Last Amended**: 2026-05-01
