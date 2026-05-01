# Tasks: Contact Lens Reminder

**Input**: Design documents from `/specs/001-contact-lens-reminder/`
**Prerequisites**: plan.md (required), spec.md (required for user stories)

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create KMP project structure per implementation plan
- [x] T002 Initialize Kotlin Multiplatform project with Kotlin 2.2.0 or later, Compose Multiplatform, Room 3.0, Koin, and kotlinx.serialization
- [ ] T003 [P] Configure Gradle scripts (build.gradle.kts) for Android (Min SDK 31) and iOS (17.0+) targets

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

- [ ] T004 Setup Room database, entities, and DAOs in commonMain (KMP support)
- [ ] T005 [P] Setup Notification interface in commonMain and platform implementations in androidMain/iosMain (via Koin DI, avoiding expect/actual where possible)
- [ ] T006 [P] Implement date utility functions in commonMain for flexible interval calculations
- [ ] T007 [P] Setup Koin dependency injection modules in commonMain
- [ ] T008 Create base LensCycle data classes and models in commonMain

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Main Screen & Setup (Priority: P1) 🎯 MVP

**Goal**: Allow user to see status, set the start date, select replacement interval, and calculate the first reminder on a single screen.

**Independent Test**: Enter a start date, select an interval, verify it is saved, and verify the next change date and "days left" status are shown correctly on the main screen.

### Implementation for User Story 1

- [ ] T009 [US1] Create Main screen (combining status and setup) in composeApp/commonMain using Compose Multiplatform
- [ ] T010 [US1] Implement Repository in shared/commonMain to persist data via Room DAOs
- [ ] T011 [US1] Calculate and display "days left" and next change date in the main screen UI
- [ ] T012 [US1] Add basic validation (date and interval must be valid)

**Checkpoint**: User Story 1 is functional.

---

## Phase 4: User Story 2 - Bi-weekly Reminder (Priority: P1)

**Goal**: Trigger a local notification on the change date.

**Independent Test**: Set a change date for 1 minute in the future, close the app, and verify the notification appears.

### Implementation for User Story 2

- [ ] T013 [US2] Implement notification scheduling logic when start date is saved
- [ ] T014 [US2] Create notification content (title, message)
- [ ] T015 [US2] Implement notification handler to open the app to the main screen when tapped
- [ ] T016 [US2] Handle notification permissions request flow

**Checkpoint**: Reminders are now working.

---

## Phase 5: User Story 3 - Reset/Mark as Changed (Priority: P2)

**Goal**: Allow user to restart the 14-day cycle.

**Independent Test**: Tap "Mark as Changed" and verify the next change date shifts to +14 days from now.

### Implementation for User Story 3

- [ ] T017 [US3] Add "Mark as Changed" button to the main screen
- [ ] T018 [US3] Implement reset logic: update last change date to today and reschedule notification
- [ ] T019 [US3] Add confirmation dialog before resetting

**Checkpoint**: All user stories are functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T020 [P] Documentation updates in README.md
- [ ] T021 Improve UI/UX (styling, icons)
- [ ] T022 [P] Add unit tests for date calculation utilities in shared/commonTest
- [ ] T023 Handle edge case: notification permission denied (show warning UI)
