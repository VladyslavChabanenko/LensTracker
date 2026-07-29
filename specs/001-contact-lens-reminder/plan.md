# Implementation Plan: Contact Lens Reminder

**Branch**: `001-lens-tracker` | **Date**: 2026-05-04 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/001-contact-lens-reminder/spec.md`

## Summary

Build a single-screen KMP mobile app (Android + iOS) that tracks a contact lens replacement cycle: users set a start date and interval (Daily/Weekly/2-Weeks/Monthly), see a progress bar with days remaining or overdue status, and receive a local notification at a user-chosen time of day when lenses are due. All state persists in a local Room 3 database; notifications survive device reboots via platform-specific scheduling (AlarmManager on Android, UNUserNotificationCenter on iOS).

## Technical Context

**Language/Version**: Kotlin 2.3.21 (KMP), Compose Multiplatform 1.10.3  
**Primary Dependencies**: Room 3.0.0-alpha03, Koin 4.2.1, kotlinx-datetime 0.6.2 (to add), kotlinx.serialization 1.11.0  
**Storage**: Room 3 KMP alpha — single `lens_cycle` table, singleton row (id=1), auto-migration enabled from version 1  
**Testing**: `kotlin.test` in `commonTest` — unit tests for date calculation and state logic, no device/emulator required  
**Target Platform**: Android min SDK 31 + iOS 17.0 (single `:composeApp` KMP module)  
**Project Type**: Mobile app (KMP, Compose Multiplatform)  
**Performance Goals**: App state loads instantly from Room (single row); notification fires within 5 min of scheduled time (SC-002)  
**Constraints**: Offline-only (no backend); notifications must survive device reboot (FR-004); future start dates forbidden (FR-001)  
**Scale/Scope**: 1 screen, 1 entity, 1 notification slot per user

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Multiplatform-First | ✅ PASS | All domain logic, Room entities/DAOs, and ViewModels in `commonMain`. Only notification scheduling and Room builder in platform source sets. |
| II. Expect/Actual Minimalism | ✅ PASS | `expect`/`actual` used only for the Room database factory function (unavoidable). `NotificationScheduler` uses interface + Koin DI. |
| III. Library-First | ✅ PASS | Single `:composeApp` module is appropriate for this app scope. Clean package layering achieves separation of concerns without premature module splits. |
| IV. Build Must Stay Green | ✅ PASS | `kotlinx-datetime` 0.6.2 added to `libs.versions.toml`; Room 3 alpha locked at `3.0.0-alpha03`; no version auto-upgrades. |
| V. Deterministic, Testable Logic | ✅ PASS | `LensCycle` member functions (pure: nextChangeDate, daysRemaining, isOverdue, progressFraction) live in `commonMain` and are fully unit-testable without device. |

**Post-design re-check**: No violations introduced. The single `expect`/`actual` for the Room database builder is justified (no multiplatform builder API exists) and documented in Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-contact-lens-reminder/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   ├── lens-cycle-dao.md        # Room DAO contract
│   └── notification-scheduler.md # Platform notification interface contract
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
composeApp/src/
├── commonMain/kotlin/com/chb/lenstracker/
│   ├── di/
│   │   └── AppModule.kt            # Shared Koin module (DB, repository, use cases)
│   ├── domain/
│   │   └── model/
│   │       ├── LensCycle.kt        # Domain model + pure member fns: nextChangeDate, daysRemaining, isOverdue, progressFraction
│   │       └── ReminderInterval.kt # Enum: DAILY, WEEKLY, TWO_WEEKS, MONTHLY
│   ├── data/
│   │   ├── db/
│   │   │   ├── LensTrackerDatabase.kt  # @Database abstract class (commonMain)
│   │   │   ├── LensCycleEntity.kt      # @Entity (id=1 singleton)
│   │   │   ├── LensCycleDao.kt         # @Dao: observeLensCycle, getLensCycle, upsertLensCycle
│   │   │   └── DateTypeConverter.kt    # LocalDate ↔ String TypeConverter
│   │   └── repository/
│   │       ├── LensCycleRepository.kt          # Interface: observe, save, markAsChanged
│   │       └── LensCycleRepositoryImpl.kt      # Implementation: wraps DAO + schedules notification
│   ├── notification/
│   │   └── NotificationScheduler.kt    # Interface: schedule(LocalDateTime, title, body), cancel, hasPermission
│   └── ui/
│       ├── screen/
│       │   ├── HomeScreen.kt           # Single-screen UI (status + setup combined)
│       │   └── HomeViewModel.kt        # StateFlow<HomeUiState>, handles HomeUiEvent
│       └── component/
│           ├── LensProgressBar.kt      # Progress bar (green→red when overdue)
│           ├── IntervalSelector.kt     # Radio/chip group for ReminderInterval
│           └── ReminderTimePicker.kt   # Time picker for hour/minute
├── androidMain/kotlin/com/chb/lenstracker/
│   ├── di/
│   │   └── AndroidAppModule.kt         # Android Koin module: DB builder, AndroidNotificationScheduler
│   ├── data/db/
│   │   └── DatabaseFactory.android.kt  # actual createDatabase(): uses AndroidSQLiteDriver + Context
│   └── notification/
│       ├── AndroidNotificationScheduler.kt  # AlarmManager.setExactAndAllowWhileIdle()
│       ├── NotificationReceiver.kt          # BroadcastReceiver: posts notification + chains next alarm
│       └── BootReceiver.kt                  # BroadcastReceiver: BOOT_COMPLETED → reschedule
└── iosMain/kotlin/com/chb/lenstracker/
    ├── di/
    │   └── IosAppModule.kt             # iOS Koin module: DB builder, IosNotificationScheduler
    ├── data/db/
    │   └── DatabaseFactory.ios.kt      # actual createDatabase(): uses BundledSQLiteDriver + NSHomeDirectory
    └── notification/
        └── IosNotificationScheduler.kt # UNUserNotificationCenter + UNCalendarNotificationTrigger
```

**Structure Decision**: Single `:composeApp` KMP module. All shared logic in `commonMain` packages; platform-specific code confined to narrow `androidMain`/`iosMain` packages (`di/`, `data/db/`, `notification/`). No new Gradle modules created.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| `expect`/`actual` for `createDatabase()` | Room's `databaseBuilder` on Android requires a `Context` parameter; on iOS it requires `NSHomeDirectory()` path. There is no platform-agnostic builder. | Interface + Koin DI for this specific factory would require wrapping the Room builder in a factory interface — same amount of platform code, more indirection for no benefit. |
