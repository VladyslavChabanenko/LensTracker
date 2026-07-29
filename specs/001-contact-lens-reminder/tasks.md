# Tasks: Contact Lens Reminder

**Input**: Design documents from `/specs/001-contact-lens-reminder/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Included throughout. The project constitution (Principle VI, Requirement-Driven TDD) makes tests mandatory for shared logic — every new pure/testable unit gets a test task written and expected to fail before its implementation task, in addition to the existing `LensCycleTest` precedent.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story. This revision supersedes the prior single-screen task list to account for spec.md's User Story 4 (Settings & Notification Management) and the navigation/modal requirements (FR-008–FR-012). Tasks T001–T018 are already implemented (see git history) and are carried forward unchanged for traceability; T019 onward reflects the current plan.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4)
- Exact file paths included in all descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add missing dependency required by all shared domain logic.

- [X] T001 Add `kotlinx-datetime = "0.7.1"` to `[versions]` and `[libraries]` in `gradle/libs.versions.toml`; add `implementation(libs.kotlinx.datetime)` to `commonMain.dependencies` block in `composeApp/build.gradle.kts`; sync Gradle to verify build succeeds

**Checkpoint**: Gradle sync green — `kotlinx.datetime.LocalDate` resolvable in `commonMain`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data layer, Room schema, Koin module skeletons — must be complete before any user story begins.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 [P] Create `ReminderInterval` enum with `DAILY(1)`, `WEEKLY(7)`, `TWO_WEEKS(14)`, `MONTHLY(30)` values in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/domain/model/ReminderInterval.kt`
- [X] T003 [P] Create `LensCycleEntity` `@Entity(tableName = "lens_cycle")` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/db/LensCycleEntity.kt`
- [X] T004 [P] Create `DateTypeConverter` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/db/DateTypeConverter.kt`
- [X] T005 Create `LensTrackerDatabase` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/db/LensTrackerDatabase.kt`
- [X] T006 [P] Create `DatabaseFactory.android.kt` in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/data/db/DatabaseFactory.android.kt`
- [X] T007 [P] Create `DatabaseFactory.ios.kt` in `composeApp/src/iosMain/kotlin/com/chb/lenstracker/data/db/DatabaseFactory.ios.kt`
- [X] T008 Create `LensCycleDao` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/db/LensCycleDao.kt`
- [X] T009 [P] Create `LensCycleRepository` interface in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/repository/LensCycleRepository.kt`
- [X] T010 Create `AppModule.kt` Koin module in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/di/AppModule.kt`
- [X] T011 [P] Create `AndroidAppModule.kt` in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/di/AndroidAppModule.kt`
- [X] T012 [P] Create `IosAppModule.kt` in `composeApp/src/iosMain/kotlin/com/chb/lenstracker/di/IosAppModule.kt`

**Checkpoint**: Foundation ready — Room DB compiles, Koin initialises on both platforms, user story implementation can now begin

---

## Phase 3: User Story 1 — Initial Setup & Main Screen (Priority: P1) 🎯 MVP

**Goal**: Main screen showing lens status, a native Material3 date picker for start-date selection (no future dates), interval selector, and progress bar — state persists in Room.

**Independent Test**: Set today as start date with "2 Weeks" interval → app shows "14 days left", correct next-change date, and green progress bar. Select "Weekly" → status updates to 7 days. Open the date picker → future dates are disabled. Verify state survives app restart.

- [X] T013 [P] [US1] Create `LensCycle` data class + `toDomain()`/`toEntity()` mappers in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/domain/model/LensCycle.kt`
- [X] T014 [P] [US1] Add pure member functions on `LensCycle`: `nextChangeDate()`, `daysRemaining()`, `baseProgressFraction()`, `overdueProgressFraction()`, `isOverdue()` in the same file
- [X] T015 [P] [US1] Write `LensCycleTest` in `composeApp/src/commonTest/kotlin/com/chb/lenstracker/domain/model/LensCycleTest.kt`
- [X] T016 [US1] Implement `LensCycleRepositoryImpl` (`observe()`, `save()`; `markAsChanged()` stubbed) in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/repository/LensCycleRepositoryImpl.kt`
- [X] T017 [US1] Create `HomeUiState`/`HomeUiEvent` and `HomeViewModel` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeViewModel.kt`
- [X] T018 [P] [US1] Create `LensProgressBar` composable with `@Preview`s in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/component/LensProgressBar.kt`
- [ ] T019 [P] [US1] Create `IntervalSelector` composable accepting `selected: ReminderInterval` and `onSelect: (ReminderInterval) -> Unit`; render a `Row` of `FilterChip` for DAILY/WEEKLY/TWO_WEEKS/MONTHLY in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/component/IntervalSelector.kt`
- [ ] T020 [US1] Update `HomeUiState` to add `showDatePicker: Boolean`; add `OpenDatePicker`, `CloseDatePicker`, `SelectDate(date: LocalDate)` to `HomeUiEvent`; `HomeViewModel` toggles `showDatePicker` and dispatches `SaveCycle` on `SelectDate` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeViewModel.kt`
- [ ] T021 [US1] Create `HomeScreen` composable wired to `HomeViewModel` via Koin: status card (`LensProgressBar`), start-date field that opens a Compose Material3 `DatePickerDialog`/`DatePicker` (native calendar widget, configured with a `SelectableDates` implementation that disables future dates per FR-001 — see research.md #9) instead of a hand-rolled calendar overlay, `IntervalSelector` (T019), next-change-date text, and an error message when no date is selected (FR-001); wire it as the content of `App()` (replacing the placeholder template UI) in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeScreen.kt` and `composeApp/src/commonMain/kotlin/com/chb/lenstracker/App.kt` (depends on T019, T020)

**Checkpoint**: User Story 1 fully functional — start date (via the native date picker) + interval saves to Room, correct days-remaining and overdue display, state survives restart

---

## Phase 4: User Story 2 — Flexible Reminders (Priority: P1)

**Goal**: Daily local notification fires at the configured time, survives device reboot; in-app permission warning shown on the main screen if denied.

**Independent Test**: Set a start date so `nextChangeDate` is ~1 minute away, background the app — notification appears within 2 minutes. Reboot emulator (cold boot) — notification reschedules automatically on Android; iOS reschedules automatically via OS. Deny notification permission — main screen shows the denied banner.

### Implementation for User Story 2

- [ ] T022 [US2] Create `NotificationScheduler` interface per `contracts/notification-scheduler.md`: `fun schedule(nextTrigger: LocalDateTime, title: String, body: String)`, `fun cancel()`, `fun hasPermission(): Boolean`, `suspend fun requestPermission(): Boolean`, `fun openAppSettings()` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/notification/NotificationScheduler.kt`
- [ ] T023 [US2] Implement `AndroidNotificationScheduler`: `schedule()`/`cancel()` via `AlarmManager.setExactAndAllowWhileIdle()` + `PendingIntent`; `hasPermission()` checks `POST_NOTIFICATIONS` on API 33+ (else `true`); `openAppSettings()` starts `Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))` with `FLAG_ACTIVITY_NEW_TASK`; `requestPermission()` temporarily returns `hasPermission()` as a placeholder (real launcher wiring lands in T043) in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/notification/AndroidNotificationScheduler.kt`
- [ ] T024 [US2] Implement `NotificationReceiver` `BroadcastReceiver`: posts the notification via `NotificationManagerCompat`, then chains the next alarm (+1 day) in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/notification/NotificationReceiver.kt`
- [ ] T025 [US2] Implement `BootReceiver` for `BOOT_COMPLETED`/`LOCKED_BOOT_COMPLETED`: reads `LensCycleEntity` from Room, fires immediately if overdue else reschedules at stored `reminderHour:reminderMinute` in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/notification/BootReceiver.kt`
- [ ] T026 [US2] Add to `composeApp/src/androidMain/AndroidManifest.xml`: `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS`, `USE_EXACT_ALARM`, `SCHEDULE_EXACT_ALARM` permissions; `<receiver>` entries for `NotificationReceiver` and `BootReceiver` (`android:exported="false"`)
- [ ] T027 [US2] Implement `IosNotificationScheduler` fully: `schedule()`/`cancel()` via `UNUserNotificationCenter` + `UNCalendarNotificationTrigger`; `hasPermission()` via cached `getNotificationSettingsWithCompletionHandler`; `requestPermission()` wraps `requestAuthorizationWithOptions` in `suspendCancellableCoroutine`; `openAppSettings()` opens `UIApplicationOpenSettingsURLString` — no Activity/launcher bridge needed on this platform, so all 5 methods are complete here (unlike Android's `requestPermission()`) in `composeApp/src/iosMain/kotlin/com/chb/lenstracker/notification/IosNotificationScheduler.kt`
- [ ] T028 [US2] Update `LensCycleRepositoryImpl` to inject `NotificationScheduler`; after `save()` call `scheduler.schedule(...)` with the new next-trigger time in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/repository/LensCycleRepositoryImpl.kt`
- [ ] T029 [US2] Wire `HomeViewModel`'s existing `hasNotificationPermission` field to `scheduler.hasPermission()`; request `POST_NOTIFICATIONS` on Android API 33+ when the user saves their first cycle; `HomeScreen` shows the persistent "Notifications are off" banner (edge case) when denied in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeViewModel.kt` and `.../ui/screen/HomeScreen.kt`
- [ ] T030 [US2] Register `AndroidNotificationScheduler`/`IosNotificationScheduler` as the `NotificationScheduler` singleton in `AndroidAppModule.kt`/`IosAppModule.kt`

**Checkpoint**: User Stories 1 AND 2 both work — notifications fire at the configured time, reboot reschedules correctly, denied banner shows on the main screen

---

## Phase 5: User Story 3 — Reset / Mark as Changed (Priority: P2)

**Goal**: "Mark as Changed" restarts the cycle from today using the same interval, behind a confirmation dialog, with a brief success toast (FR-005, FR-012).

**Independent Test**: Open the app with an active cycle; tap "Mark as Changed"; confirm in the dialog → new next-change date is `today + interval.days`, progress bar resets to 0%, and a brief confirmation message appears and auto-dismisses.

### Implementation for User Story 3

- [ ] T031 [P] [US3] Create a reusable `ConfirmDialog` composable (title, body, Cancel/Confirm actions) in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/component/ConfirmDialog.kt`
- [ ] T032 [P] [US3] Create a reusable `ConfirmationToast` composable — a transient, auto-dismissing banner (FR-012) — in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/component/ConfirmationToast.kt`
- [ ] T033 [US3] Implement `markAsChanged()` in `LensCycleRepositoryImpl` (replacing the `TODO`): create a new `LensCycleEntity` with `startDate = today` (same interval/reminder time), upsert, then `scheduler.cancel()` + `scheduler.schedule()` with the new next-change date in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/repository/LensCycleRepositoryImpl.kt` (depends on T028)
- [ ] T034 [US3] Add `showConfirmDialog: Boolean` and `toastMessage: String?` to `HomeUiState`; add `OpenConfirmDialog`, `CloseConfirmDialog` to `HomeUiEvent`; on `MarkAsChanged`, set `toastMessage` after the repository call completes and auto-clear it after a short delay in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeViewModel.kt`
- [ ] T035 [US3] Add the "Mark as Changed" button to `HomeScreen`, opening `ConfirmDialog` (T031) before dispatching `MarkAsChanged`, and rendering `ConfirmationToast` (T032) when `toastMessage` is set, in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeScreen.kt` (depends on T031, T032, T034)

**Checkpoint**: User Stories 1–3 independently functional — full single-screen reminder flow usable end-to-end

---

## Phase 6: User Story 4 — Settings & Notification Management (Priority: P2)

**Goal**: A dedicated Settings screen (reachable from the main screen and back) for reminder time and notification permission, including the native permission prompt and the permanently-denied recovery path (FR-008–FR-011).

**Independent Test**: From the main screen, tap the settings control, change the reminder time, navigate back, and confirm the main screen still shows correct lens status with the new time in effect. Deny notification permission twice on Android — Settings should stop offering the in-app prompt and instead offer a button into the app's system settings page. Grant notification permission from Settings — a brief confirmation toast appears and auto-dismisses.

### Tests for User Story 4

> **NOTE: Write this test FIRST, ensure it FAILS before implementation (Constitution VI)**

- [ ] T036 [P] [US4] Write `SettingsViewModelTest` with `kotlin.test` covering: reminder-time update maps correctly into a saved `LensCycle`, permission-status derivation from `hasPermission()`, `permissionRequestInFlight` toggling around `requestPermission()`, and `toastMessage` being set (FR-012) when `requestPermission()` resolves `true` but left `null` when it resolves `false` — expect this to fail (no `SettingsViewModel` exists yet) in `composeApp/src/commonTest/kotlin/com/chb/lenstracker/ui/screen/SettingsViewModelTest.kt`

### Implementation for User Story 4

- [ ] T037 [US4] Create `AppScreen` sealed class (`Main`, `Settings`) in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/navigation/AppScreen.kt`
- [ ] T038 [US4] Create `SettingsUiState` (`reminderHour`, `reminderMinute`, `hasNotificationPermission`, `permissionRequestInFlight`, `toastMessage`) and `SettingsViewModel` (events: `UpdateReminderTime`, `RequestPermission`, `OpenAppSettings`) backed by `LensCycleRepository` and `NotificationScheduler`, to make T036 pass; on `RequestPermission`, set `toastMessage` (FR-012) after `requestPermission()` resolves `true` and auto-clear it after a short delay, mirroring `HomeViewModel`'s existing toast handling — leave `toastMessage` unset if the result is `false` (the denied banner already covers that case) in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/SettingsViewModel.kt` (depends on T036, T022, T028)
- [ ] T039 [P] [US4] Create `ReminderTimePicker` composable accepting `hour: Int`, `minute: Int`, `onTimeChange: (Int, Int) -> Unit`: an hour dropdown (0–23) plus a minute dropdown restricted to 15-minute steps (00/15/30/45 only — no free-form minute entry) in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/component/ReminderTimePicker.kt`
- [ ] T040 [US4] Create `SettingsScreen` composable wired to `SettingsViewModel` via Koin: `ReminderTimePicker` (T039), permission status tag + "Enable" control (triggers `RequestPermission`, disabled while `permissionRequestInFlight`), denied explanation text, an "Open Settings" button (calls `OpenAppSettings`) shown only when permission is denied AND a prior `requestPermission()` call did not show the native prompt, a `ConfirmationToast` rendered when `toastMessage` is set (permission granted), and a back control in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/SettingsScreen.kt` (depends on T038, T039)
- [ ] T041 [US4] Add a settings (gear) icon to `HomeScreen`'s header that sets `AppScreen.Settings`; update `App()` to hold `var screen by remember { mutableStateOf<AppScreen>(AppScreen.Main) }` and render `HomeScreen`/`SettingsScreen` accordingly, with `SettingsScreen`'s back control setting `AppScreen.Main` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeScreen.kt` and `composeApp/src/commonMain/kotlin/com/chb/lenstracker/App.kt` (depends on T037, T040)
- [ ] T042 [US4] Create `AndroidPermissionBridge` Koin singleton: `val requestSignal: MutableSharedFlow<Unit>`, and a method to await a `CompletableDeferred<Boolean>` result, resolved by whichever composable holds the `ActivityResultLauncher` in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/notification/AndroidPermissionBridge.kt`
- [ ] T043 [US4] Update `AndroidNotificationScheduler.requestPermission()` to delegate to `AndroidPermissionBridge` instead of the T023 placeholder: on API 33+, if `shouldShowRequestPermissionRationale()` would be false after a prior denial, resolve immediately with `hasPermission()`; otherwise emit the request signal and await the bridge's deferred result in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/notification/AndroidNotificationScheduler.kt` (depends on T042)
- [ ] T044 [US4] Update `MainActivity.kt`'s `setContent` block to wrap `App()` in a small composable that collects `AndroidPermissionBridge.requestSignal` once via `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` and completes the bridge's deferred with the launcher's result in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/MainActivity.kt` (depends on T042)

**Checkpoint**: All four user stories independently functional — Settings screen reachable and navigable, reminder time editable from Settings, notification permission re-request works including the permanently-denied fallback

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Build validation and end-to-end verification.

- [ ] T045 Verify Android build compiles: `./gradlew :composeApp:assembleDebug`; verify iOS compiles: `./gradlew :composeApp:iosSimulatorArm64Test`; fix any errors
- [X] T046 [P] Room schema JSON already committed at `composeApp/schemas/com.chb.lenstracker.data.db.LensTrackerDatabase/1.json` — no schema version bump needed for this feature (Settings/navigation/modals are UI-only, no new persisted fields); verify it's still current after T045
- [ ] T047 Run `quickstart.md` validation end-to-end, including the "Test Settings & notification permission re-request" section (Main ↔ Settings navigation, reminder-time change, permission re-request, permanently-denied fallback)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — complete
- **Foundational (Phase 2)**: Depends on Phase 1 — **BLOCKS all user stories** — complete
- **User Story Phases (3, 4, 5, 6)**: All depend on Phase 2; US2 extends `LensCycleRepositoryImpl`/`HomeViewModel` from US1; US3 completes `markAsChanged()` stubbed in US1 and reuses US2's `scheduler`; US4 reuses US1's `HomeScreen`/`App.kt` (adds the gear icon + screen switch) and US2's `NotificationScheduler` (wires the real `requestPermission()`)
- **Polish (Phase 7)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Starts after Phase 2 — no dependency on US2/US3/US4
- **User Story 2 (P1)**: Starts after Phase 2 — implement US1 first (needs `LensCycleRepositoryImpl`, `HomeViewModel`)
- **User Story 3 (P2)**: Starts after US1 and US2 — completes `markAsChanged()` and reuses `scheduler`
- **User Story 4 (P2)**: Starts after US1 and US2 — reuses `HomeScreen`/`App.kt` navigation host and the `NotificationScheduler` contract; independent of US3

### Within Each User Story

- Tests (where included) before the implementation they cover
- Domain/pure logic before ViewModel
- ViewModel before UI components
- Components before screen assembly

### Parallel Opportunities

- T002, T003, T004 — simultaneously (different files, no dependencies)
- T006, T007 — simultaneously (different platform source sets)
- T011, T012 — simultaneously (different platform source sets)
- T013, T014, T015 — simultaneously (different files, depend only on T002)
- T031, T032 — simultaneously (different component files)
- T036, T039 — simultaneously (test file vs. an unrelated component, no shared dependency)

---

## Parallel Example: User Story 1 (remaining work)

```bash
# Launch simultaneously once Phase 2 is done:
Task T019: "Create IntervalSelector in .../ui/component/IntervalSelector.kt"
Task T020: "Add showDatePicker state + date-selection events to HomeUiState/HomeViewModel"

# Then, once T019 and T020 are done:
Task T021: "Create HomeScreen wiring the native DatePickerDialog, IntervalSelector, and status card"
```

## Parallel Example: User Story 4

```bash
# Launch simultaneously once Phase 2 is done:
Task T036: "Write SettingsViewModelTest in .../commonTest/.../ui/screen/SettingsViewModelTest.kt"
Task T039: "Create ReminderTimePicker in .../ui/component/ReminderTimePicker.kt"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 Only)

1. Complete Phase 1 + 2 (already done)
2. Complete Phase 3: User Story 1 remaining tasks (T019–T021) → **test independently**
3. Complete Phase 4: User Story 2 (T022–T030) → **test notifications**
4. **STOP and VALIDATE**: Full reminder flow works end-to-end
5. Complete Phase 5 (US3) and Phase 6 (US4) if time allows

### Incremental Delivery

1. Phase 1 + 2 → Foundation ready (done)
2. Phase 3 → Native date picker + status screen work, state persists → **Demo-able MVP**
3. Phase 4 → Notifications fire, reboot-safe, denied banner on main screen → **Core value delivered**
4. Phase 5 → Mark as Changed with confirmation + toast → **Single-cycle flow complete**
5. Phase 6 → Settings screen, navigation, permission re-request → **Feature complete per current spec**
6. Phase 7 → Validated, schema confirmed → **Shippable**

---

## Notes

- [P] tasks = different files, no shared dependencies — safe to parallelize
- [Story] label maps each task to its user story for traceability
- US1 is fully testable without notifications (no device permission needed)
- `Monthly` = exactly 30 days (`LocalDate.plus(30, DateTimeUnit.DAY)`) — not a calendar month
- Android `SCHEDULE_EXACT_ALARM` (API 31–32) requires user approval; fall back to `setAndAllowWhileIdle()` if denied and show the same warning banner as notification permission denial
- `openAppSettings()` is fully implementable on Android without the permission bridge (`startActivity` needs no result callback) — only `requestPermission()` needs `AndroidPermissionBridge`'s launcher wiring
- iOS never needs `AndroidPermissionBridge` — `IosNotificationScheduler` implements all 5 `NotificationScheduler` methods directly, no Activity/launcher concept exists there
