package com.chb.lenstracker.notification

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Connects the Activity-free [AndroidNotificationScheduler] to the Activity-scoped permission
 * launcher. The scheduler calls [request]; whichever composable owns the launcher collects
 * [requestSignal], shows the OS prompt and reports back through [complete].
 */
class AndroidPermissionBridge {

    private val signal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var pending: CompletableDeferred<Boolean>? = null

    /** Emits once per prompt that should be shown. Collect it exactly once, from the launcher owner. */
    val requestSignal: SharedFlow<Unit> = signal.asSharedFlow()

    /**
     * Asks the launcher owner to resolve the permission and suspends until [complete] is called.
     * A request made while another is pending joins it rather than starting a second prompt.
     * Resolves `false` straight away when no launcher owner is collecting (no Activity in front).
     */
    suspend fun request(): Boolean {
        pending?.let { return it.await() }
        if (signal.subscriptionCount.value == 0) return false

        val deferred = CompletableDeferred<Boolean>()
        pending = deferred
        try {
            signal.emit(Unit)
            return deferred.await()
        } finally {
            if (pending === deferred) {
                pending = null
                // Release anyone who joined this request if its owner was cancelled mid-flight.
                deferred.complete(false)
            }
        }
    }

    /** Reports the outcome of the prompt (or `hasPermission()` when no prompt could be shown). */
    fun complete(granted: Boolean) {
        pending?.complete(granted)
    }
}
