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

    /**
     * Request notification permission from the user (FR-010, FR-011).
     * Suspends until the user responds, or resolves immediately if permission
     * is already granted or the OS will not show the prompt again (Android's
     * "don't ask again" state) — callers must check [hasPermission] afterward
     * rather than assuming a `false` result means the dialog was shown.
     *
     * @return true if permission is granted after the call completes.
     */
    suspend fun requestPermission(): Boolean

    /**
     * Open the platform's app-specific system settings page (FR-011's
     * permanently-denied recovery path). Fire-and-forget — does not return
     * a result; the caller should re-check [hasPermission] when the app
     * next resumes.
     */
    fun openAppSettings()
}
```

---

## Android implementation notes (`AndroidNotificationScheduler`)

- Uses `AlarmManager.setExactAndAllowWhileIdle()` to fire a `PendingIntent` targeting `NotificationReceiver`.
- `NotificationReceiver` (BroadcastReceiver) posts the notification and schedules the next alarm (same time next day — repeating via chained alarms, since `setRepeating()` is inexact on API 19+).
- `BootReceiver` listens for `BOOT_COMPLETED` / `LOCKED_BOOT_COMPLETED` and calls `schedule()` again with data from Room.
- `cancel()` calls `AlarmManager.cancel()` with the same `PendingIntent`.
- `hasPermission()`: on API 33+, checks `POST_NOTIFICATIONS`; on API 31–32, returns `true`.
- `requestPermission()`: on API < 33, returns `true` immediately (no runtime permission exists). On API 33+, delegates to `AndroidPermissionBridge` — a Koin singleton holding a `MutableSharedFlow<Unit>` request signal and a `CompletableDeferred<Boolean>` result. The app's root composable collects the signal exactly once via `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` and completes the deferred with the launcher's result. If the OS won't show the dialog again (previously denied, `shouldShowRequestPermissionRationale()` is `false`), the bridge resolves immediately with the current `hasPermission()` value instead of launching.
- `openAppSettings()`: builds `Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))` with `FLAG_ACTIVITY_NEW_TASK` and calls `context.startActivity()`.

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
- `requestPermission()`: wraps `UNUserNotificationCenter.requestAuthorizationWithOptions(.alert, .sound, .badge)` in `suspendCancellableCoroutine` and returns the granted boolean; no Activity/launcher concept needed on iOS, so no bridge is required (unlike Android).
- `openAppSettings()`: calls `UIApplication.sharedApplication.openURL(NSURL(string: UIApplicationOpenSettingsURLString))` — iOS does not permanently block the permission dialog the way Android does, but the same escape hatch is offered for consistency and for users who denied via the OS Settings app directly.

**Notification request ID**: use a fixed constant `"lens_reminder"` so `schedule()` always replaces the previous request.
