package com.chb.lenstracker.notification

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidPermissionBridgeTest {

    private val bridge = AndroidPermissionBridge()

    @Test
    fun requestEmitsSignalAndResolvesWithTheLaunchersResult() = runTest {
        var signals = 0
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            bridge.requestSignal.collect { signals++ }
        }
        runCurrent()

        val result = async { bridge.request() }
        runCurrent()
        assertEquals(1, signals)
        assertFalse(result.isCompleted)

        bridge.complete(true)
        assertTrue(result.await())
    }

    @Test
    fun requestResolvesFalseWithoutHangingWhenNothingIsCollectingTheSignal() = runTest {
        assertFalse(bridge.request())
    }

    @Test
    fun secondRequestWhilePendingSharesTheFirstResultInsteadOfRelaunching() = runTest {
        var signals = 0
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            bridge.requestSignal.collect { signals++ }
        }
        runCurrent()

        val first = async { bridge.request() }
        val second = async { bridge.request() }
        runCurrent()
        bridge.complete(false)

        assertFalse(first.await())
        assertFalse(second.await())
        assertEquals(1, signals)
    }

    @Test
    fun cancelledRequestDoesNotBlockTheNextOne() = runTest {
        var signals = 0
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            bridge.requestSignal.collect { signals++ }
        }
        runCurrent()

        val abandoned = async { bridge.request() }
        runCurrent()
        abandoned.cancel()
        runCurrent()

        val next = async { bridge.request() }
        runCurrent()
        assertEquals(2, signals)
        bridge.complete(true)
        assertTrue(next.await())
    }
}
