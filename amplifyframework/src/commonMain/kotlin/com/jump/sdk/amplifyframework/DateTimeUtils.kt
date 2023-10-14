package com.jump.sdk.amplifyframework

import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun nowAsFormattedString(): String {
    val now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    return "${now.dayOfWeek.shortName} ${now.month.shortName} ${now.dayOfMonth} " +
        "${now.hour.toString().padStart(2, '0')}:" +
        "${now.minute.toString().padStart(2, '0')}:" +
        "${now.second.toString().padStart(2, '0')} " +
        "UTC ${now.year}"
}

val DayOfWeek.shortName: String
    get() = when (this) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
        else -> {
            throw IllegalArgumentException("Unknown day of week: $this")
        }
    }

val Month.shortName: String
    get() = when (this) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
        else -> {
            throw IllegalArgumentException("Unknown month: $this")
        }
    }
