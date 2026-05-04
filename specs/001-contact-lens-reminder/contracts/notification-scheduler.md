# Contract: NotificationScheduler

**Type**: Platform abstraction interface (commonMain)
**Implemented by**: `AndroidNotificationScheduler` (androidMain), `IosNotificationScheduler` (iosMain)
**Bound via**: Koin — platform modules provide the concrete implementation

---

## Interface

```kotlin
// commonMain: com.chb.lenstracker.notification.NotificationScheduler
interface NotificationScheduler {

    /**
     * Schedule a repeating daily notification at the given time.
     * Replaces any previously scheduled notification.
     *
     * @param nextTrigger The first date+time to fire the notification.
     * @param title       Notification title text.
     * @param body        Notification body text.
     */
    fun schedule(nextTrigger: LocalDateTime, title: String, body: String)

    /**
     * Cancel any pending scheduled notification.
     */
    fun cancel()

    /**
     * Returns true if the user has granted notification permission.
     * On Android API < 33, always returns true (no runtime permission needed).
     */
    fun hasPermission(): Boolean
}
```

---

## Android implementation notes (`AndroidNotificationScheduler`)

- Uses `AlarmManager.setExactAndAllowWhileIdle()` to fire a `PendingIntent` targeting `NotificationReceiver`.
- `NotificationReceiver` (BroadcastReceiver) posts the notification and schedules the next alarm (same time next day — repeating via chained alarms, since `setRepeating()` is inexact on API 19+).
- `BootReceiver` listens for `BOOT_COMPLETED` / `LOCKED_BOOT_COMPLETED` and calls `schedule()` again with data from Room.
- `cancel()` calls `AlarmManager.cancel()` with the same `PendingIntent`.
- `hasPermission()`: on API 33+, checks `POST_NOTIFICATIONS`; on API 31–32, returns `true`.

**Required manifest entries**:
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.USE_EXACT_ALARM"/>
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>

<receiver android:name=".receiver.NotificationReceiver" android:exported="false"/>
<receiver android:name=".receiver.BootReceiver" android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
        <action android:name="android.intent.action.LOCKED_BOOT_COMPLETED"/>
    </intent-filter>
</receiver>
```

---

## iOS implementation notes (`IosNotificationScheduler`)

- Uses `UNUserNotificationCenter.current()` from `platform.UserNotifications`.
- `schedule()`: creates a `UNMutableNotificationContent`, wraps `nextTrigger` in `NSDateComponents`, and adds a `UNCalendarNotificationTrigger` with `repeats = true`.
- iOS stores pending notifications in the OS — they survive device reboots automatically.
- `cancel()`: calls `UNUserNotificationCenter.removeAllPendingNotificationRequests()`.
- `hasPermission()`: calls `UNUserNotificationCenter.getNotificationSettingsWithCompletionHandler` — note this is async; for the synchronous interface contract, cache the result and refresh on app foreground.

**Notification request ID**: use a fixed constant `"lens_reminder"` so `schedule()` always replaces the previous request.
