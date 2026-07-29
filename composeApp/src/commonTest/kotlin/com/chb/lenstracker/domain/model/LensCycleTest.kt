package com.chb.lenstracker.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LensCycleTest {

    private val startDate = LocalDate(2026, 1, 1)

    private fun cycle(interval: ReminderInterval) = LensCycle(
        startDate = startDate,
        interval = interval,
        reminderHour = 9,
        reminderMinute = 0
    )

    @Test
    fun nextChangeDateDaily() {
        assertEquals(LocalDate(2026, 1, 2), cycle(ReminderInterval.DAILY).nextChangeDate())
    }

    @Test
    fun nextChangeDateWeekly() {
        assertEquals(LocalDate(2026, 1, 8), cycle(ReminderInterval.WEEKLY).nextChangeDate())
    }

    @Test
    fun nextChangeDateTwoWeeks() {
        assertEquals(LocalDate(2026, 1, 15), cycle(ReminderInterval.TWO_WEEKS).nextChangeDate())
    }

    @Test
    fun nextChangeDateMonthly() {
        assertEquals(LocalDate(2026, 1, 31), cycle(ReminderInterval.MONTHLY).nextChangeDate())
    }

    @Test
    fun daysRemainingPositiveWhenActive() {
        val lensCycle = cycle(ReminderInterval.TWO_WEEKS)
        assertEquals(7, lensCycle.daysRemaining(LocalDate(2026, 1, 8)))
    }

    @Test
    fun daysRemainingZeroWhenDue() {
        val lensCycle = cycle(ReminderInterval.TWO_WEEKS)
        assertEquals(0, lensCycle.daysRemaining(LocalDate(2026, 1, 15)))
    }

    @Test
    fun daysRemainingNegativeWhenOverdue() {
        val lensCycle = cycle(ReminderInterval.TWO_WEEKS)
        assertEquals(-3, lensCycle.daysRemaining(LocalDate(2026, 1, 18)))
    }

    @Test
    fun isOverdueFalseWhenDue() {
        val lensCycle = cycle(ReminderInterval.TWO_WEEKS)
        assertFalse(lensCycle.isOverdue(LocalDate(2026, 1, 15)))
    }

    @Test
    fun isOverdueTrueTheDayAfterDue() {
        val lensCycle = cycle(ReminderInterval.TWO_WEEKS)
        assertTrue(lensCycle.isOverdue(LocalDate(2026, 1, 16)))
    }

    @Test
    fun progressFractionAtStart() {
        val lensCycle = cycle(ReminderInterval.TWO_WEEKS)
        assertEquals(0.0f, lensCycle.progressFraction(startDate))
    }

    @Test
    fun progressFractionAtMidpoint() {
        val lensCycle = cycle(ReminderInterval.TWO_WEEKS)
        assertEquals(0.5f, lensCycle.progressFraction(LocalDate(2026, 1, 8)))
    }

    @Test
    fun progressFractionAtDueDate() {
        val lensCycle = cycle(ReminderInterval.TWO_WEEKS)
        assertEquals(1.0f, lensCycle.progressFraction(LocalDate(2026, 1, 15)))
    }

    @Test
    fun progressFractionCappedWhenOverdue() {
        val lensCycle = cycle(ReminderInterval.TWO_WEEKS)
        assertEquals(1.5f, lensCycle.progressFraction(LocalDate(2026, 1, 25)))
    }
}
