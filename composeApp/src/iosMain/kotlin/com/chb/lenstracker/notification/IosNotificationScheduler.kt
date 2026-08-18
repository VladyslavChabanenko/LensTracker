package com.chb.lenstracker.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.LocalDateTime
import platform.Foundation.NSDateComponents
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

class IosNotificationScheduler : NotificationScheduler {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    /** [hasPermission] must be synchronous, but the OS API is callback-only — cache the last known status. */
    private var permissionGranted: Boolean = false

    init {
        refreshPermissionCache()
    }

    override fun schedule(nextTrigger: LocalDateTime, title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
        }
        // Only hour/minute are set so the trigger matches every day at that time, not just nextTrigger's date.
        val dateComponents = NSDateComponents().apply {
            hour = nextTrigger.hour.toLong()
            minute = nextTrigger.minute.toLong()
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = REQUEST_IDENTIFIER,
            content = content,
            trigger = trigger
        )
        center.removePendingNotificationRequestsWithIdentifiers(listOf(REQUEST_IDENTIFIER))
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override fun cancel() {
        center.removeAllPendingNotificationRequests()
    }

    override fun hasPermission(): Boolean = permissionGranted

    override suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            permissionGranted = granted
            if (continuation.isActive) continuation.resumeWith(Result.success(granted))
        }
    }

    override fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }

    private fun refreshPermissionCache() {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            permissionGranted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
        }
    }

    companion object {
        /** Fixed so every scheduled request replaces the previous one — the app has one reminder slot. */
        private const val REQUEST_IDENTIFIER = "lens_reminder"
    }
}
