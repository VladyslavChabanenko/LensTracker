package com.chb.lenstracker.notification

import kotlinx.datetime.LocalDateTime

/**
 * Platform abstraction over the OS reminder-notification APIs.
 * Implemented by `AndroidNotificationScheduler` and `IosNotificationScheduler`, bound via Koin.
 */
interface NotificationScheduler {

    /** Schedules a daily notification starting at [nextTrigger], replacing any previously scheduled one. */
    fun schedule(nextTrigger: LocalDateTime, title: String, body: String)

    /** Cancels the pending scheduled notification, if any. */
    fun cancel()

    /** True once the user has granted notification permission. Always true on Android API < 33. */
    fun hasPermission(): Boolean

    /**
     * Prompts the user for notification permission, suspending until they respond. Resolves
     * immediately when permission is already granted or the OS will not show the prompt again,
     * so callers must re-check [hasPermission] rather than read a `false` result as "dialog shown".
     *
     * @return whether permission is granted once the call completes.
     */
    suspend fun requestPermission(): Boolean

    /**
     * Opens the app's system settings page — the recovery path when permission is permanently
     * denied. Fire-and-forget; re-check [hasPermission] when the app next resumes.
     */
    fun openAppSettings()
}
