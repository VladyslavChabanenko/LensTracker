# Implementation Plan: Contact Lens Reminder

**Branch**: `001-lens-tracker` | **Date**: 2026-07-29 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-contact-lens-reminder/spec.md`

## Summary

Build a two-screen KMP mobile app (Android + iOS) that tracks a contact lens replacement cycle. The **Main screen** shows lens status, start date, replacement interval, and a "Mark as Changed" control; a **Settings screen** — reached via a gear icon and returned from via a back control — holds the reminder time and notification permission status. Date selection uses Compose Material3's native `DatePickerDialog` (with `selectableDates` disabling future dates) rather than a hand-rolled calendar grid; the "Mark as Changed" confirmation and notification permission requests are presented as transient dialogs rather than additional screens, plus brief auto-dismissing confirmation messages after key actions. All state persists in a local Room 3 database; notifications survive device reboots via platform-specific scheduling (AlarmManager on Android, UNUserNotificationCenter on iOS).

This plan supersedes the original single-screen plan to account for spec.md's User Story 4 (Settings & Notification Management) and the associated navigation/modal requirements (FR-008–FR-012). The core domain model (`LensCycle`, `ReminderInterval`) and data layer (Room schema, DAO, repository) are unchanged — this update is additive at the UI/navigation layer plus one new cross-platform capability (in-app notification-permission re-request). The custom `CalendarCell`/`buildCalendarCells`/`CalendarOverlay` approach from the earlier revision of this plan has been dropped in favor of Compose Material3's built-in `DatePicker`, which is already a `commonMain` dependency and needs no new pure-function test surface.

## Technical Context

**Language/Version**: Kotlin 2.3.21 (KMP), Compose Multiplatform 1.10.3
**Primary Dependencies**: Room 3.0.0-alpha03, Koin 4.2.1, kotlinx-datetime 0.7.1, kotlinx.serialization 1.11.0, androidx.activity:activity-compose 1.13.0 (already present in `androidMain`, now also backs the permission-request launcher)
**Storage**: Room 3 KMP alpha — unchanged single `lens_cycle` table, singleton row (id=1); no schema changes required for this feature (reminder hour/minute already present)
**Testing**: `kotlin.test` + `kotlinx-coroutines-test` in `commonTest` — unit tests for date/calendar calculations and ViewModel state (fake repository/scheduler, no device/emulator required)
**Target Platform**: Android min SDK 31 + iOS 17.0 (single `:composeApp` KMP module)
**Project Type**: Mobile app (KMP, Compose Multiplatform) — 2 screens (Main, Settings) with overlay/dialog interactions
**Performance Goals**: App state loads instantly from Room (single row); notification fires within 15 min of scheduled time (SC-002 — inexact alarms, no exact-alarm permission); navigating Main ↔ Settings and opening/closing overlays must feel instant (single Compose recomposition, no I/O on the navigation path)
**Constraints**: Offline-only (no backend); notifications must survive device reboot (FR-004); future start dates forbidden (FR-001); Android's "don't ask again" permission policy means `requestPermission()` cannot always show a system dialog — Settings must fall back to guiding the user to the app's system settings page (FR-011)
**Scale/Scope**: 2 screens (Main, Settings), 1 entity, 1 notification slot per user, 3 overlay/dialog interactions (date-picker calendar, mark-as-changed confirmation, notification-permission request), transient toast-style confirmation feedback

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Multiplatform-First | ✅ PASS | Navigation state (`AppScreen`), overlay/dialog state, and calendar-cell generation are all plain `commonMain` state and pure functions. Only the permission-request bridge has platform-specific wiring (behind the existing `NotificationScheduler` interface). |
| II. Expect/Actual Minimalism | ✅ PASS | No new `expect`/`actual` introduced. The new `NotificationScheduler.requestPermission()` method is implemented per-platform via the existing interface + Koin DI pattern (Android bridges to an `ActivityResultLauncher` via a Koin-scoped signal/deferred pair; iOS calls `UNUserNotificationCenter` directly) — same pattern as `hasPermission()`/`schedule()` already use. Considered an `expect`/`actual` Composable wrapper instead; rejected because it duplicates what the interface+DI pattern already does more simply. |
| III. Library-First | ✅ PASS | Still a single `:composeApp` module. Settings screen and overlays live in the existing `ui/` package; no new modules or navigation library needed for a 2-screen app with no back stack complexity. |
| IV. Build Must Stay Green | ✅ PASS | No dependency version changes. `androidx-activity-compose` is already declared and already in `androidMain.dependencies` — reused, not added. |
| V. Deterministic, Testable Logic | ✅ PASS | `HomeUiState`/`SettingsUiState` reducers are pure `commonMain` functions, unit-testable without a device, following the existing `LensCycle` pattern. Date selection itself now delegates to Compose Material3's `DatePicker`, so there is no custom calendar-cell logic to unit test. |
| VI. Requirement-Driven TDD | ✅ PASS (commitment) | Tasks for FR-008–FR-012 will follow Red→Green→Refactor: tests for calendar-cell edge cases, reminder-time update, and permission-status derivation are written from the User Story 4 acceptance scenarios before implementation. |
| VII. Dependency Guardrails | ✅ PASS | UI state classes live in `ui/`, not `domain/`; the domain model (`LensCycle`, `ReminderInterval`) is untouched by this feature. `SettingsViewModel` depends on `LensCycleRepository` and `NotificationScheduler` abstractions only, matching the existing `HomeViewModel` boundary pattern. |
| SOLID Principles | ✅ PASS | Settings gets its own `SettingsViewModel` (Single Responsibility) rather than growing `HomeViewModel`; both depend on the same repository/scheduler abstractions (Dependency Inversion), consistent with existing code. |

**Post-design re-check**: No violations introduced. The Android permission-request bridge is interface+DI, not `expect`/`actual`, so the existing Complexity Tracking entry (Room DB factory) remains the only justified `expect`/`actual` in the codebase.

## Project Structure

### Documentation (this feature)

```text
specs/001-contact-lens-reminder/
├── plan.md              # This file (/speckit-plan command output) — updated for Settings/navigation/modals
├── research.md          # Phase 0 output — updated with navigation, overlay, and permission-request research
├── data-model.md        # Phase 1 output — updated with UI-only state (AppScreen)
├── quickstart.md        # Phase 1 output — updated with Settings/permission testing steps
├── contracts/
│   ├── lens-cycle-dao.md         # Room DAO contract (unchanged)
│   └── notification-scheduler.md # Updated: adds requestPermission() to the contract
└── tasks.md             # Phase 2 output (/speckit-tasks command) — needs regeneration for this scope
```

### Source Code (repository root)

```text
composeApp/src/
├── commonMain/kotlin/com/chb/lenstracker/
│   ├── di/
│   │   └── AppModule.kt                # UPDATED: registers `SettingsViewModel` explicitly (`viewModel { SettingsViewModel(get(), get()) }`) because its message resolver has a default
│   ├── domain/
│   │   └── model/
│   │       ├── LensCycle.kt            # Unchanged
│   │       └── ReminderInterval.kt     # Unchanged
│   ├── data/                           # Unchanged (db/, repository/)
│   ├── notification/
│   │   └── NotificationScheduler.kt    # UPDATED: + `suspend fun requestPermission(): Boolean`
│   └── ui/
│       ├── navigation/
│       │   └── AppScreen.kt            # NEW: sealed class { Main, Settings } + `AppScreenSaver`; hoisted as `rememberSaveable` state in the app root (survives rotation/process restart) — no navigation library needed for 2 screens with no back stack
│       ├── screen/
│       │   ├── HomeScreen.kt           # UPDATED: centered `ScreenHeader` title; gear icon navigates to Settings; start-date field opens the native Material3 `DatePickerDialog`; "Mark as Changed" opens the confirm dialog; shows toast + denied-notifications banner
│       │   ├── HomeViewModel.kt        # UPDATED: `HomeUiState` gains `showDatePicker`, `showConfirmDialog`, `toastMessage`
│       │   ├── SettingsScreen.kt       # NEW: `ScreenHeader` (centered title + back arrow), reminder time picker, notification permission tag + Enable control, denied explanation, "Open Settings" shortcut, `ConfirmationToast` when permission is granted
│       │   └── SettingsViewModel.kt    # NEW: `SettingsUiState` (reminderHour/Minute, hasNotificationPermission, permissionRequestInFlight, toastMessage); events `UpdateReminderTime`, `RequestPermission`, `OpenAppSettings`; sets `toastMessage` when `requestPermission()` resolves `true`; toast text comes from an injectable resolver (default `getString`) so host unit tests need no resource loader
│       └── component/
│           ├── LensProgressBar.kt      # Unchanged
│           ├── IntervalSelector.kt     # Unchanged
│           ├── ReminderTimePicker.kt   # Hour dropdown (0–23) + minute dropdown restricted to 15-minute steps (00/15/30/45), now rendered on SettingsScreen instead of HomeScreen
│           ├── ScreenHeader.kt         # NEW: centered screen title with optional back arrow / settings gear (icon only — no container or press ripple), drawn 16dp from the screen edge; also exposes `screenContentPadding()` (16dp horizontal) for content below the header; shared by HomeScreen and SettingsScreen
│           ├── ConfirmDialog.kt        # NEW: reusable Yes/No confirmation dialog (used by "Mark as Changed")
│           └── ConfirmationToast.kt    # NEW: transient, auto-dismissing confirmation banner; reused by both HomeScreen (Mark as Changed) and SettingsScreen (permission granted)
├── androidMain/kotlin/com/chb/lenstracker/
│   └── notification/
│       ├── AndroidNotificationScheduler.kt  # UPDATED: `requestPermission()` delegates to `AndroidPermissionBridge`
│       └── AndroidPermissionBridge.kt       # NEW: Koin singleton — `MutableSharedFlow<Unit>` request signal + `CompletableDeferred<Boolean>` result; the app root composable collects the signal once via `rememberLauncherForActivityResult(RequestPermission())` and resolves the deferred with the result
└── iosMain/kotlin/com/chb/lenstracker/
    └── notification/
        └── IosNotificationScheduler.kt  # UPDATED: `requestPermission()` wraps `UNUserNotificationCenter.requestAuthorizationWithOptions` in `suspendCancellableCoroutine` — no bridge needed, no Activity dependency on iOS
```

**Structure Decision**: Still a single `:composeApp` KMP module — no new Gradle modules, no navigation library added. Screen switching is plain `commonMain` Compose state (`AppScreen`) because there are only two screens and no back-stack/deep-link requirements; this mirrors the imported design prototype's own approach (a screen-state field, not a navigation stack). The one new platform-specific concern — requesting notification permission from the Settings screen — reuses the established interface+DI abstraction (`NotificationScheduler`) rather than introducing new `expect`/`actual` surface.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| `expect`/`actual` for `createDatabase()` | Room's `databaseBuilder` on Android requires a `Context` parameter; on iOS it requires `NSHomeDirectory()` path. There is no platform-agnostic builder. | Interface + Koin DI for this specific factory would require wrapping the Room builder in a factory interface — same amount of platform code, more indirection for no benefit. |
