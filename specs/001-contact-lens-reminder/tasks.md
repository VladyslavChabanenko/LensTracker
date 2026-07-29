# Tasks: Contact Lens Reminder

**Input**: Design documents from `/specs/001-contact-lens-reminder/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Unit tests included for `LensCycle`'s pure domain calculations (explicitly called out in plan.md technical context — no device/emulator required).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Exact file paths included in all descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add missing dependency required by all shared domain logic.

- [X] T001 Add `kotlinx-datetime = "0.6.2"` to `[versions]` and `[libraries]` in `gradle/libs.versions.toml`; add `implementation(libs.kotlinx.datetime)` to `commonMain.dependencies` block in `composeApp/build.gradle.kts`; sync Gradle to verify build succeeds

**Checkpoint**: Gradle sync green — `kotlinx.datetime.LocalDate` resolvable in `commonMain`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data layer, Room schema, Koin module skeletons — must be complete before any user story begins.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 [P] Create `ReminderInterval` enum with `DAILY(1)`, `WEEKLY(7)`, `TWO_WEEKS(14)`, `MONTHLY(30)` values and a `val days: Int` constructor parameter in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/domain/model/ReminderInterval.kt`
- [X] T003 [P] Create `LensCycleEntity` `@Entity(tableName = "lens_cycle")` with fields: `@PrimaryKey val id: Int = 1`, `val startDate: String`, `val interval: String`, `val reminderHour: Int`, `val reminderMinute: Int` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/db/LensCycleEntity.kt`
- [X] T004 [P] Create `DateTypeConverter` `@TypeConverter` class with `LocalDate.toString()` / `LocalDate.parse()` for LocalDate↔String and `ReminderInterval.name` / `ReminderInterval.valueOf()` for ReminderInterval↔String in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/db/DateTypeConverter.kt`
- [X] T005 Create `LensTrackerDatabase` `@Database(entities = [LensCycleEntity::class], version = 1, autoMigrations = [])` `@TypeConverters(DateTypeConverter::class)` abstract class extending `RoomDatabase` with `abstract fun lensCycleDao(): LensCycleDao` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/db/LensTrackerDatabase.kt`
- [X] T006 [P] Create `DatabaseFactory.android.kt` `actual fun createDatabase(context: Context): LensTrackerDatabase` using `Room.databaseBuilder` with `AndroidSQLiteDriver` and database name `"lens_tracker.db"` in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/data/db/DatabaseFactory.android.kt`
- [X] T007 [P] Create `DatabaseFactory.ios.kt` `actual fun createDatabase(): LensTrackerDatabase` using `BundledSQLiteDriver` with path `NSHomeDirectory() + "/Documents/lens_tracker.db"` in `composeApp/src/iosMain/kotlin/com/chb/lenstracker/data/db/DatabaseFactory.ios.kt`
- [X] T008 Create `LensCycleDao` `@Dao` interface with: `@Query("SELECT * FROM lens_cycle WHERE id = 1") fun observeLensCycle(): Flow<LensCycleEntity?>`, `@Query("SELECT * FROM lens_cycle WHERE id = 1") suspend fun getLensCycle(): LensCycleEntity?`, `@Upsert suspend fun upsertLensCycle(entity: LensCycleEntity)` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/db/LensCycleDao.kt`
- [X] T009 [P] Create `LensCycleRepository` interface with `fun observe(): Flow<LensCycleEntity?>`, `suspend fun save(entity: LensCycleEntity)`, `suspend fun markAsChanged()` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/repository/LensCycleRepository.kt`
- [X] T010 Create `AppModule.kt` Koin module (skeleton) binding `LensTrackerDatabase` via `createDatabase()` as singleton and `LensCycleDao` via `get<LensTrackerDatabase>().lensCycleDao()` as singleton in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/di/AppModule.kt`
- [X] T011 [P] Create `AndroidAppModule.kt` Koin module providing Android `Context` singleton; call `startKoin { androidContext(this@Application); modules(appModule, androidModule) }` from `Application.onCreate()` in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/di/AndroidAppModule.kt`
- [X] T012 [P] Create `IosAppModule.kt` Koin module; expose `fun initKoin()` calling `startKoin { modules(appModule, iosModule) }` to be called from Swift `@main` entry point in `composeApp/src/iosMain/kotlin/com/chb/lenstracker/di/IosAppModule.kt`

**Checkpoint**: Foundation ready — Room DB compiles, Koin initialises on both platforms, user story implementation can now begin

---

## Phase 3: User Story 1 — Initial Setup & Main Screen (Priority: P1) 🎯 MVP

**Goal**: Single screen showing lens status, date picker (no future dates), interval selector, and progress bar — state persists in Room.

**Independent Test**: Set today as start date with "2 Weeks" interval → app shows "14 days left", correct next-change date, and green progress bar. Select "Weekly" → status updates to 7 days. Verify state survives app restart.

### Implementation for User Story 1

- [X] T013 [P] [US1] Create `LensCycle` data class with `startDate: LocalDate`, `interval: ReminderInterval`, `reminderHour: Int`, `reminderMinute: Int`; add `LensCycleEntity.toDomain()` and `LensCycle.toEntity()` mappers in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/domain/model/LensCycle.kt`
- [X] T014 [P] [US1] Add pure member functions on `LensCycle`: `fun nextChangeDate(): LocalDate` (startDate + interval.days), `fun daysRemaining(today): Int` (signed, negative = overdue), `fun progressFraction(today): Float` (0.0 at start, 1.0 at due date, capped at 1.5f for display), `fun isOverdue(today): Boolean` (daysRemaining(today) < 0) in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/domain/model/LensCycle.kt`
- [X] T015 [P] [US1] Write `LensCycleTest` with `kotlin.test` covering: `nextChangeDate` for all 4 intervals, `daysRemaining` when active/due/overdue, `isOverdue` boundary (0 = not overdue), `progressFraction` at start/midpoint/due/overdue; run with `./gradlew :composeApp:testDebugUnitTest` in `composeApp/src/commonTest/kotlin/com/chb/lenstracker/domain/model/LensCycleTest.kt`
- [X] T016 [US1] Implement `LensCycleRepositoryImpl` injecting `LensCycleDao`: `observe()` delegates to `dao.observeLensCycle()`, `save(entity)` delegates to `dao.upsertLensCycle()` (interface already operates on `LensCycleEntity` per T009/contract; domain↔entity mapping via `LensCycle.toDomain()`/`toEntity()` happens at the ViewModel boundary), `markAsChanged()` stub (throws `NotImplementedError` until Phase 5); register as `LensCycleRepository` binding in `AppModule.kt` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/repository/LensCycleRepositoryImpl.kt`
- [ ] T017 [US1] Create `HomeUiState` data class (`lensCycle: LensCycle?`, `daysRemaining: Int`, `isOverdue: Boolean`, `progressFraction: Float`, `hasNotificationPermission: Boolean = true`) and `HomeUiEvent` sealed class (`SaveCycle`, `MarkAsChanged`); implement `HomeViewModel` collecting `repository.observe()`, mapping to `HomeUiState` via `LensCycle`'s extension functions, validating startDate not in future; register `HomeViewModel` in `AppModule.kt` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeViewModel.kt`
- [ ] T018 [P] [US1] Create `LensProgressBar` composable accepting `fraction: Float` and `isOverdue: Boolean`; use `LinearProgressIndicator` with `color = if (isOverdue) MaterialTheme.colorScheme.error else Color.Green`; show label "X days left" or "X days overdue" below bar in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/component/LensProgressBar.kt`
- [ ] T019 [P] [US1] Create `IntervalSelector` composable accepting `selected: ReminderInterval` and `onSelect: (ReminderInterval) -> Unit`; render a `Row` of `FilterChip` for DAILY / WEEKLY / TWO_WEEKS / MONTHLY with the selected chip highlighted in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/component/IntervalSelector.kt`
- [ ] T020 [US1] Create `HomeScreen` composable wired to `HomeViewModel` via Koin; include: `DatePicker` or `DatePickerDialog` with `selectableDates` restricting future dates (FR-001), `IntervalSelector`, `LensProgressBar`, next-change-date text, and error message when no date selected; dispatch `SaveCycle` event on valid input change in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeScreen.kt`

**Checkpoint**: User Story 1 fully functional — start date + interval saves to Room, correct days-remaining and overdue display, state survives restart

---

## Phase 4: User Story 2 — Flexible Reminders (Priority: P1)

**Goal**: Daily local notification fires at user-set time, survives device reboot; in-app permission warning shown if denied.

**Independent Test**: Set a start date so `nextChangeDate` is ~1 minute away, background the app — notification appears within 2 minutes. Reboot emulator (cold boot) — notification reschedules automatically on Android; iOS reschedules automatically via OS.

### Implementation for User Story 2

- [ ] T021 [US2] Create `NotificationScheduler` interface with `fun schedule(nextTrigger: LocalDateTime, title: String, body: String)`, `fun cancel()`, `fun hasPermission(): Boolean` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/notification/NotificationScheduler.kt`
- [ ] T022 [US2] Implement `AndroidNotificationScheduler` injecting `Context` and `AlarmManager`; `schedule()` calls `AlarmManager.setExactAndAllowWhileIdle()` with a `PendingIntent` for `NotificationReceiver`; `cancel()` cancels the same `PendingIntent`; `hasPermission()` returns `ContextCompat.checkSelfPermission(POST_NOTIFICATIONS) == GRANTED` on API 33+, `true` on API < 33 in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/notification/AndroidNotificationScheduler.kt`
- [ ] T023 [US2] Implement `NotificationReceiver` `BroadcastReceiver`: on `onReceive` post notification via `NotificationManagerCompat` using title/body from intent extras, then chain the next daily alarm by calling `AlarmManager.setExactAndAllowWhileIdle()` with same time +1 day in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/notification/NotificationReceiver.kt`
- [ ] T024 [US2] Implement `BootReceiver` `BroadcastReceiver` for `BOOT_COMPLETED` and `LOCKED_BOOT_COMPLETED`: read `LensCycleEntity` from Room via `runBlocking { dao.getLensCycle() }`; if nextChangeDate is in the past fire immediately, else schedule at stored reminderHour:reminderMinute in `composeApp/src/androidMain/kotlin/com/chb/lenstracker/notification/BootReceiver.kt`
- [ ] T025 [US2] Add to `composeApp/src/androidMain/AndroidManifest.xml`: permissions `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS`, `USE_EXACT_ALARM`, `SCHEDULE_EXACT_ALARM`; `<receiver>` declarations for `NotificationReceiver` (`android:exported="false"`) and `BootReceiver` (`android:exported="false"`) with intent-filter for both boot actions
- [ ] T026 [US2] Implement `IosNotificationScheduler` using `UNUserNotificationCenter.current()`; `schedule()` creates `UNMutableNotificationContent`, wraps `nextTrigger` in `NSDateComponents`, adds `UNNotificationRequest` with id `"lens_reminder"` and `UNCalendarNotificationTrigger(repeats: true)`; `cancel()` calls `removeAllPendingNotificationRequests()`; `hasPermission()` returns cached auth status refreshed on app foreground in `composeApp/src/iosMain/kotlin/com/chb/lenstracker/notification/IosNotificationScheduler.kt`
- [ ] T027 [US2] Update `LensCycleRepositoryImpl` to inject `NotificationScheduler`; after `save()` call `scheduler.schedule(nextTrigger = nextChangeDate at reminderHour:reminderMinute, title, body)`; update `markAsChanged()` stub to call `scheduler.cancel()` then reschedule; update `AppModule.kt` NotificationScheduler injection (or handle in platform modules) in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/repository/LensCycleRepositoryImpl.kt`
- [ ] T028 [P] [US2] Create `ReminderTimePicker` composable accepting `hour: Int`, `minute: Int`, `onTimeChange: (Int, Int) -> Unit`; use a row of dropdown or `OutlinedTextField` fields; default display `09:00` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/component/ReminderTimePicker.kt`
- [ ] T029 [US2] Update `HomeScreen` to include `ReminderTimePicker` below `IntervalSelector`; update `HomeViewModel` to request `POST_NOTIFICATIONS` permission on Android API 33+ when user first saves a cycle; show warning `Text` banner ("Notifications are disabled — enable in Settings") when `hasNotificationPermission = false` in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeScreen.kt`
- [ ] T030 [US2] Register `AndroidNotificationScheduler` as `NotificationScheduler` singleton in `AndroidAppModule.kt`; register `IosNotificationScheduler` as `NotificationScheduler` singleton in `IosAppModule.kt`

**Checkpoint**: User Stories 1 AND 2 both work — notifications fire at configured time, reboot reschedules correctly, permission denial shows warning

---

## Phase 5: User Story 3 — Reset / Mark as Changed (Priority: P2)

**Goal**: "Mark as Changed" button restarts the cycle from today using the same interval.

**Independent Test**: Open the app with an active cycle; tap "Mark as Changed"; confirm in the dialog → new next-change date is `today + interval.days`; progress bar resets to 0%.

### Implementation for User Story 3

- [ ] T031 [US3] Implement `markAsChanged()` in `LensCycleRepositoryImpl`: create `LensCycleEntity(id=1, startDate=today.toString(), interval=current.interval, reminderHour=current.reminderHour, reminderMinute=current.reminderMinute)`, call `dao.upsertLensCycle()`, then `scheduler.cancel()` and `scheduler.schedule()` with the new nextChangeDate in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/data/repository/LensCycleRepositoryImpl.kt`
- [ ] T032 [US3] Add `MarkAsChanged` to `HomeUiEvent` and handle in `HomeViewModel` by calling `repository.markAsChanged()` in a coroutine scope in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeViewModel.kt`
- [ ] T033 [US3] Add "Mark as Changed" `Button` to `HomeScreen` (visible only when `uiState.lensCycle != null`); show `AlertDialog` confirmation ("This will reset your lens cycle to today. Continue?") before dispatching `MarkAsChanged` event (FR-005) in `composeApp/src/commonMain/kotlin/com/chb/lenstracker/ui/screen/HomeScreen.kt`

**Checkpoint**: All three user stories independently functional — full app usable end-to-end

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Build validation and schema commit.

- [ ] T034 Verify Android build compiles: `./gradlew :composeApp:assembleDebug`; verify iOS compiles: `./gradlew :composeApp:iosSimulatorArm64Test`; fix any errors
- [ ] T035 [P] Commit generated Room schema JSON at `composeApp/schemas/com.chb.lenstracker.data.db.LensTrackerDatabase/1.json` (auto-generated on first compile — required for auto-migration support per FR-006)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — **BLOCKS all user stories**
- **User Story Phases (3, 4, 5)**: All depend on Phase 2; US2 extends `LensCycleRepositoryImpl` from US1; US3 completes `markAsChanged()` stubbed in US1/US2
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Starts after Phase 2 — no dependency on US2 or US3
- **User Story 2 (P1)**: Starts after Phase 2 — implement US1 first (needs `LensCycleRepositoryImpl`)
- **User Story 3 (P2)**: Starts after US1 and US2 — completes `markAsChanged()` stub

### Within Each User Story

- Domain model before use case
- Use case before repository implementation
- Repository before ViewModel
- ViewModel before UI components
- Components before Screen assembly

### Parallel Opportunities

- T002, T003, T004 — simultaneously (different files, no dependencies)
- T006, T007 — simultaneously (different platform source sets)
- T011, T012 — simultaneously (different platform source sets)
- T013, T014, T015 — simultaneously (different files, depend only on T002)
- T018, T019 — simultaneously (different component files, depend only on T017)
- T022, T023, T024 — simultaneously (different receiver files)
- T026, T028 — simultaneously (different files)

---

## Parallel Example: User Story 1

```bash
# Launch simultaneously once T002 (ReminderInterval) is done:
Task T013: "Create LensCycle domain model in .../domain/model/LensCycle.kt"
Task T014: "Add LensCycle member functions in .../domain/model/LensCycle.kt"
Task T015: "Write LensCycleTest in .../commonTest/.../domain/model/LensCycleTest.kt"

# Launch simultaneously once T017 (HomeViewModel) is done:
Task T018: "Create LensProgressBar in .../ui/component/LensProgressBar.kt"
Task T019: "Create IntervalSelector in .../ui/component/IntervalSelector.kt"
```

## Parallel Example: User Story 2

```bash
# Launch simultaneously once T021 (NotificationScheduler interface) is done:
Task T022: "Implement AndroidNotificationScheduler in .../androidMain/.../AndroidNotificationScheduler.kt"
Task T026: "Implement IosNotificationScheduler in .../iosMain/.../IosNotificationScheduler.kt"

# Launch simultaneously:
Task T023: "Implement NotificationReceiver in .../androidMain/.../NotificationReceiver.kt"
Task T024: "Implement BootReceiver in .../androidMain/.../BootReceiver.kt"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 Only)

1. Complete Phase 1: Setup (T001)
2. Complete Phase 2: Foundational (T002–T012) — CRITICAL BLOCKER
3. Complete Phase 3: User Story 1 (T013–T020) → **test independently**
4. Complete Phase 4: User Story 2 (T021–T030) → **test notifications**
5. **STOP and VALIDATE**: Full reminder flow works end-to-end
6. Complete Phase 5: User Story 3 if time allows

### Incremental Delivery

1. Phase 1 + 2 → Foundation ready
2. Phase 3 → Status screen works, state persists → **Demo-able MVP**
3. Phase 4 → Notifications fire, reboot-safe → **Core value delivered**
4. Phase 5 → Mark as Changed → **Feature complete**
5. Phase 6 → Validated, schema committed → **Shippable**

---

## Notes

- [P] tasks = different files, no shared dependencies — safe to parallelize
- [Story] label maps each task to its user story for traceability
- US1 is fully testable without notifications (no device permission needed)
- Room schema JSON at `composeApp/schemas/` must be committed (T035) — required for auto-migration support
- `Monthly` = exactly 30 days (`LocalDate.plus(30, DateTimeUnit.DAY)`) — not a calendar month
- Android `SCHEDULE_EXACT_ALARM` (API 31–32) requires user approval; fall back to `setAndAllowWhileIdle()` if denied and show same warning banner as notification permission denial
