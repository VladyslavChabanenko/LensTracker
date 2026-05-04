# LensTracker

Kotlin Multiplatform mobile app (Android + iOS) for contact lens replacement reminders.

## Key files

- `composeApp/build.gradle.kts` — module dependencies (Room 3, Koin, Compose Multiplatform)
- `gradle/libs.versions.toml` — version catalog
- `composeApp/schemas/` — Room auto-migration schemas (commit these)
- `specs/` — feature specifications and implementation plans

## Build commands

```bash
# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:testDebugUnitTest

# iOS (simulator)
./gradlew :composeApp:iosSimulatorArm64Test
```

## Architecture

Single `:composeApp` KMP module. All business logic in `commonMain`; platform-specific code (Room builder, notification scheduling) in `androidMain`/`iosMain`. Koin for DI; interface-based platform abstractions (no expect/actual except for Room DB factory).

<!-- SPECKIT START -->
## Active feature plan

**Feature**: Contact Lens Reminder  
**Plan**: [specs/001-contact-lens-reminder/plan.md](specs/001-contact-lens-reminder/plan.md)  
**Branch**: `001-lens-tracker`
<!-- SPECKIT END -->
