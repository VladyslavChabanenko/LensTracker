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

### VI. Requirement-Driven TDD
The development workflow must follow a strict Test-Driven Development cycle based on specifications:
- **Test First**: Unit tests must be written before implementation code, derived directly from the feature requirements and specifications.
- **Fail First**: New tests must initially fail (Red) to verify they are testing the intended behavior.
- **Implement**: Implementation code is written only to satisfy the requirements captured in the tests (Green).
- **Refactor**: Clean up and optimize the implementation while ensuring tests remain passing.

### VII. Dependency Guardrails
Architecture must enforce strict boundaries between layers:
- **Inward Only**: Dependencies must strictly flow toward the domain module. Data and UI layers
  depend on domain; domain depends on nothing.
- **Mapping Requirement**: Every boundary crossing (Data → Domain or Domain → UI) requires a
  dedicated mapper function or class.
- **No Leakage**: Third-party framework annotations (e.g., `@Entity`, `@Serializable`,
  `@Composable`) are strictly forbidden in the domain module. Domain models must remain
  framework-agnostic.

### VII. SOLID Principles
- **S - Single Responsibility**: Every class or module should have one reason to change. Separate "what the app does" (Domain) from "how it fetches data" (Data).
- **O - Open/Closed**: Software entities should be open for extension but closed for modification. Use interfaces to allow new functionality without changing existing core logic.
- **L - Liskov Substitution**: Objects of a superclass should be replaceable with objects of its subclasses without breaking the application (essential for our `expect`/`actual` and interface implementations).
- **I - Interface Segregation**: Clients should not be forced to depend on methods they do not use. Prefer small, focused interfaces over "God" interfaces.
- **D - Dependency Inversion**: Depend on abstractions, not concretions. High-level modules (Domain) must not depend on low-level modules (Data/UI). Both must depend on abstractions.

## UI Conventions

### Composable Previews
Every `@Composable` function intended for reuse (screens and components alike) must ship with at least one `@Preview` composable in the same file. Where the composable has visually distinct states (e.g. empty/filled, error/success, overdue/on-track), add one preview per state so each renders correctly without running the app.

## Naming Conventions

### Data Models

- **Database Entities**: Must use the `Entity` suffix (e.g., `UserEntity`)
- **Network Models**: API transfer objects must use the `Dto` suffix (e.g., `UserDto`)
- **Domain Models**: Must remain "pure" with no suffix (e.g., `User`)

### Interfaces

Prefer behavior-based naming over "I" prefixes (e.g., `UserRepository` instead of
`IUserRepository`).

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

**Version**: 1.1.0 | **Ratified**: 2026-05-01 | **Last Amended**: 2026-07-29
