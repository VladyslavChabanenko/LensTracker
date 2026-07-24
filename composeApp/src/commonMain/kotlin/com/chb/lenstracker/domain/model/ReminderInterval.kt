package com.chb.lenstracker.domain.model

enum class ReminderInterval(val days: Int) {
    DAILY(1),
    WEEKLY(7),
    TWO_WEEKS(14),
    MONTHLY(30)
}
