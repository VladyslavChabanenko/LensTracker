# Feature Specification: Contact Lens Reminder

**Feature Branch**: `001-contact-lens-reminder`  
**Created**: 2026-05-01  
**Status**: Draft  
**Input**: User description: "Build a mobile application that reminds user to change contact lenses every two weeks"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Initial Setup & Main Screen (Priority: P1)

As a user, I want to see my lens status, set the date I started wearing them, and select my replacement interval on a single screen so I can easily manage my reminders.

**Why this priority**: Core functionality. Combines status visibility with setup capability.

**Independent Test**: Can be tested by entering a start date, selecting an interval, and verifying it is saved and the next change date is correctly calculated and displayed on the same screen.

**Acceptance Scenarios**:

1. **Given** the app is opened, **When** I select today as my start date and keep the default "2 weeks" interval, **Then** the app should display the "14 days left" status and next change date correctly.
2. **Given** I am on the main screen, **When** I select "Weekly" interval, **Then** the status should update to show the next change date 7 days from the start date.
3. **Given** I am on the main screen, **When** I leave the date empty, **Then** the app should prompt me to select a date.
4. **Given** I am selecting a start date, **When** I open the date picker, **Then** dates in the future should be disabled and impossible to select.
5. **Given** my lenses are past their change date, **When** I view the main screen, **Then** the app should display an overfilled progress bar in red and show "X days overdue" (e.g., "2 days overdue").

---

### User Story 2 - Flexible Reminders (Priority: P1)

As a user, I want to receive a notification according to my selected interval (daily, weekly, 2 weeks, or monthly) so I don't forget to change my lenses.

**Why this priority**: Primary value proposition. The app's main purpose is to remind the user at their preferred interval.

**Independent Test**: Can be tested by setting a change date in the near future for different intervals and verifying notifications are triggered correctly.

**Acceptance Scenarios**:

1. **Given** a "Daily" interval is selected, **When** the next day is reached at the reminder time, **Then** I should receive a push notification.
2. **Given** a "Monthly" interval is selected, **When** 30 days have passed from the start date, **Then** I should receive a push notification.
3. **Given** I have received a reminder, **When** I tap it, **Then** the app should open to the main screen.
4. **Given** the device has rebooted, **When** there is a missed reminder or a scheduled one, **Then** the notification should trigger immediately (for missed) or remain scheduled.

---

### User Story 3 - Reset/Mark as Changed (Priority: P2)

As a user, I want to mark my lenses as changed so the cycle restarts for the same selected interval.

**Why this priority**: Essential for ongoing use.

**Independent Test**: Can be tested by clicking a "Change Lenses" button and verifying the new next change date is calculated based on the current interval from the current date.

**Acceptance Scenarios**:

1. **Given** my lenses are due today and my interval is "Weekly", **When** I tap "Mark as Changed", **Then** the new next change date should be set to 7 days from today.

---

### Edge Cases

- **Notification Permissions**: On Android 13+ (API 33), `POST_NOTIFICATIONS` is a runtime permission. The app MUST follow best practices for requesting it and show a warning if denied.
- **Leap Years/Month Boundaries**: Standard date libraries should handle this. "Monthly" is defined as exactly 30 days.
- **Device Reboot**: Reminders MUST persist after a device reboot. Missed reminders during power-off should trigger immediately upon reboot.
- **Timezone Changes**: Reminder times should be relative to the local time of the device.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow users to select a "start date" or "last change date" (future dates NOT allowed).
- **FR-002**: System MUST allow users to select a replacement interval: Daily, Weekly, 2 Weeks (Default), or Monthly (30 days).
- **FR-003**: System MUST calculate the next change date and "overdue" status.
- **FR-004**: System MUST schedule local notifications and ensure they persist/trigger after reboot.
- **FR-005**: System MUST allow users to reset the timer (Mark as Changed) with a confirmation dialog.
- **FR-006**: System MUST persist the lens status using Room with support for auto-migrations.
- **FR-007**: System MUST allow users to set a preferred time of day for the reminder notification.

### Key Entities

- **LensCycle**: Represents the current cycle. Attributes: `startDate`, `interval` (Daily, Weekly, 14Days, Monthly), `nextChangeDate`, `reminderTime`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can set up their first reminder in under 30 seconds.
- **SC-002**: Reminders are delivered within 5 minutes of the scheduled time.
- **SC-003**: App state persists correctly after a device reboot or app force-close.

## Assumptions

- **Target Device**: Modern iOS and Android devices (reminders via local notifications).
- **Offline First**: The app does not require a backend/account; all data is stored locally.
- **Flexible Interval**: The interval is configurable by the user (Daily, Weekly, 2 Weeks, Monthly), defaulting to 2 weeks.
