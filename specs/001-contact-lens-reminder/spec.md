# Feature Specification: Contact Lens Reminder

**Feature Branch**: `001-contact-lens-reminder`  
**Created**: 2026-05-01  
**Status**: Draft  
**Input**: User description: "Build a mobile application that reminds user to change contact lenses every two weeks"

## Clarifications

### Session 2026-07-29

- Q: When the user taps "Enable" notifications in Settings, is the resulting prompt the phone's native OS permission dialog, or a custom in-app dialog the app renders? → A: Native OS permission dialog only — tapping "Enable" goes straight to the platform's own prompt; no custom in-app dialog is shown first.
- Q: Once the platform permanently blocks its own permission dialog (e.g., after repeated denial on Android) and the app can no longer trigger it, how should Settings help the user recover? → A: Show explanatory text plus a button that deep-links directly into the app's system settings page.

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

### User Story 4 - Settings & Notification Management (Priority: P2)

As a user, I want a separate Settings screen where I can adjust my reminder time and check or fix my notification permission, so my main screen stays focused on my lens status and isn't cluttered with configuration options I rarely change.

**Why this priority**: Necessary once the app has more than one preference to manage (reminder time + notification permission); keeps the primary status view uncluttered.

**Independent Test**: From the main screen, open Settings, change the reminder time, navigate back, and confirm the main screen still shows correct lens status while the new reminder time is in effect.

**Acceptance Scenarios**:

1. **Given** I am on the main screen, **When** I tap the settings control, **Then** I am taken to a Settings screen showing my current reminder time and notification permission status.
2. **Given** I am on the Settings screen, **When** I change the reminder time, **Then** the new time is saved and used for future reminders.
3. **Given** notifications are currently denied, **When** I view the Settings screen, **Then** I see a "Denied" status, a short explanation, and a control to re-request permission.
4. **Given** I am on the Settings screen, **When** I tap the back control, **Then** I return to the main screen with my lens status unchanged.
5. **Given** I re-request notification permission from Settings, **When** the platform's native permission prompt appears, **Then** I must explicitly choose "Allow" or "Don't Allow" — it does not dismiss if I tap outside it.
6. **Given** I tap "Enable" on the Settings screen, **When** I grant the permission via the native prompt, **Then** the "Denied" status clears and a brief confirmation message appears and auto-dismisses (FR-012).

---

### Edge Cases

- **Notification Permissions**: On Android 13+ (API 33), `POST_NOTIFICATIONS` is a runtime permission. The app MUST follow best practices for requesting it and show a warning if denied.
- **Leap Years/Month Boundaries**: Standard date libraries should handle this. "Monthly" is defined as exactly 30 days.
- **Device Reboot**: Reminders MUST persist after a device reboot. Missed reminders during power-off should trigger immediately upon reboot.
- **Timezone Changes**: Reminder times should be relative to the local time of the device.
- **Dismissing without choosing**: If the user dismisses the date picker or the "Mark as Changed" confirmation without making a selection, no change is applied.
- **Permission prompt persistence**: The notification-permission prompt cannot be dismissed by tapping outside it; the user must actively choose Allow or Don't Allow.
- **Denied state visibility**: Whenever notifications are denied, both the main screen (via a banner) and the Settings screen (via status + explanation) must make this clear and offer a path to re-enable.
- **Permanently denied recovery**: If the platform will not show its permission prompt again, the Settings screen MUST provide a direct shortcut to the app's system settings page rather than repeatedly retrying an in-app request.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow users to select a "start date" or "last change date" (future dates NOT allowed).
- **FR-002**: System MUST allow users to select a replacement interval: Daily, Weekly, 2 Weeks (Default), or Monthly (30 days).
- **FR-003**: System MUST calculate the next change date and "overdue" status.
- **FR-004**: System MUST schedule local notifications and ensure they persist/trigger after reboot.
- **FR-005**: System MUST allow users to reset the timer (Mark as Changed) with a confirmation dialog, and MUST show a brief confirmation message after the reset is applied.
- **FR-006**: System MUST persist the lens status using Room with support for auto-migrations.
- **FR-007**: System MUST allow users to view and change their preferred reminder time from the Settings screen.
- **FR-008**: System MUST provide a Settings screen, reachable from the main screen, showing the user's reminder time and current notification permission status.
- **FR-009**: System MUST support navigating from the main screen to the Settings screen and back without losing unsaved state on the main screen.
- **FR-010**: System MUST request notification permission via the platform's native permission prompt (not a custom in-app dialog), which requires an explicit "Allow" / "Don't Allow" choice and cannot be dismissed by tapping outside it.
- **FR-011**: System MUST allow users to re-request notification permission from the Settings screen if previously denied, and MUST display a clear explanation there when denied. If the platform will no longer show its own permission prompt (e.g., after repeated denial on Android), the Settings screen MUST instead offer a direct shortcut into the device's system settings for this app.
- **FR-012**: System MUST show a brief, non-blocking confirmation message after key actions (e.g., marking lenses as changed, enabling notifications) that dismisses on its own.

### Key Entities

- **LensCycle**: Represents the current cycle. Attributes: `startDate`, `interval` (Daily, Weekly, 14Days, Monthly), `nextChangeDate`, `reminderTime`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can set up their first reminder in under 30 seconds.
- **SC-002**: Reminders are delivered within 15 minutes of the scheduled time. Reminders are scheduled inexactly so the OS can batch them for battery; a lens change is a same-day task, so this tolerance costs the user nothing.
- **SC-003**: App state persists correctly after a device reboot or app force-close.
- **SC-004**: Users can find and update their reminder-time setting in under 20 seconds from app launch.

## Assumptions

- **Target Device**: Modern iOS and Android devices (reminders via local notifications).
- **Offline First**: The app does not require a backend/account; all data is stored locally.
- **Flexible Interval**: The interval is configurable by the user (Daily, Weekly, 2 Weeks, Monthly), defaulting to 2 weeks.
- **Screen Scope**: The app consists of a main status screen and a single Settings screen; no additional screens are introduced by this feature.
- **Overlay Interactions**: Date selection, permission requests, and reset confirmation are presented as transient overlays rather than separate screens.
- **Transient Feedback**: Brief confirmation messages (e.g., toasts) auto-dismiss and never require the user to interact with them.
- **Default Reminder Time**: Before the user completes initial setup, the Settings screen shows a sensible default reminder time (09:00) that the user can change at any time, before or after saving their first lens cycle.
