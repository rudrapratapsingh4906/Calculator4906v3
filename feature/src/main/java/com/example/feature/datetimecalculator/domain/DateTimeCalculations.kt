package com.example.feature.datetimecalculator.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.Period
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.format.TextStyle
import java.util.Locale

object DateTimeCalculations {

    data class DateDiffResult(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalDays: Long,
        val totalWeeks: Long,
        val remainingDaysOfWeek: Long
    )

    data class AgeResult(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalDays: Long,
        val daysToNextBirthday: Long,
        val nextBirthdayDayOfWeek: String
    )

    data class CountdownResult(
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long,
        val isPast: Boolean
    )

    data class TimeDiffResult(
        val hours: Long,
        val minutes: Long,
        val seconds: Long,
        val totalMinutes: Long,
        val totalSeconds: Long
    )

    data class TimeZoneItem(
        val id: String,
        val displayName: String,
        val gmtOffset: String
    )

    // A list of popular offline-available Time Zones
    val offlineTimeZones = listOf(
        TimeZoneItem("UTC", "Coordinated Universal Time", "GMT+00:00"),
        TimeZoneItem("America/New_York", "New York (EST/EDT)", "GMT-05:00 / GMT-04:00"),
        TimeZoneItem("America/Los_Angeles", "Los Angeles (PST/PDT)", "GMT-08:00 / GMT-07:00"),
        TimeZoneItem("America/Chicago", "Chicago (CST/CDT)", "GMT-06:00 / GMT-05:00"),
        TimeZoneItem("America/Denver", "Denver (MST/MDT)", "GMT-07:00 / GMT-06:00"),
        TimeZoneItem("Europe/London", "London (GMT/BST)", "GMT+00:00 / GMT+01:00"),
        TimeZoneItem("Europe/Paris", "Paris (CET/CEST)", "GMT+01:00 / GMT+02:00"),
        TimeZoneItem("Asia/Kolkata", "India (IST)", "GMT+05:30"),
        TimeZoneItem("Asia/Tokyo", "Tokyo (JST)", "GMT+09:00"),
        TimeZoneItem("Asia/Singapore", "Singapore (SGT)", "GMT+08:00"),
        TimeZoneItem("Australia/Sydney", "Sydney (AEST/AEDT)", "GMT+10:00 / GMT+11:00"),
        TimeZoneItem("Asia/Dubai", "Dubai (GST)", "GMT+04:00"),
        TimeZoneItem("Africa/Johannesburg", "Johannesburg (SAST)", "GMT+02:00"),
        TimeZoneItem("America/Sao_Paulo", "Sao Paulo (BRT/BRST)", "GMT-03:00"),
        TimeZoneItem("Pacific/Auckland", "Auckland (NZST/NZDT)", "GMT+12:00 / GMT+13:00")
    )

    /**
     * Future ready interface for online time zone APIs.
     * Keep online functionality disabled by default.
     */
    interface OnlineTimeZoneService {
        suspend fun syncTimeZoneOffsets(): Boolean
        fun isOnlineModeEnabled(): Boolean = false
    }

    /**
     * 1. Date Difference Calculator
     */
    fun calculateDateDifference(start: LocalDate, end: LocalDate): DateDiffResult {
        val period = Period.between(start, end)
        val totalDays = ChronoUnit.DAYS.between(start, end)
        val totalWeeks = totalDays / 7
        val remainingDays = totalDays % 7

        return DateDiffResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            totalWeeks = totalWeeks,
            remainingDaysOfWeek = remainingDays
        )
    }

    /**
     * 2. Add / 3. Subtract Days / Weeks / Months / Years
     */
    fun modifyDate(date: LocalDate, amount: Long, unit: ChronoUnit, isAddition: Boolean): LocalDate {
        val multiplier = if (isAddition) 1 else -1
        val signedAmount = amount * multiplier
        return when (unit) {
            ChronoUnit.DAYS -> date.plusDays(signedAmount)
            ChronoUnit.WEEKS -> date.plusWeeks(signedAmount)
            ChronoUnit.MONTHS -> date.plusMonths(signedAmount)
            ChronoUnit.YEARS -> date.plusYears(signedAmount)
            else -> date
        }
    }

    /**
     * 4. Day of Week Finder
     */
    fun getDayOfWeekFinder(date: LocalDate): String {
        return date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    /**
     * 5. Leap Year Checker
     */
    fun isLeapYear(year: Int): Boolean {
        return LocalDate.of(year, 1, 1).isLeapYear
    }

    /**
     * 6. Age Between Two Dates
     */
    fun calculateAge(birthDate: LocalDate, targetDate: LocalDate): AgeResult {
        // If targetDate is before birthDate, handle gracefully
        val start = if (birthDate.isAfter(targetDate)) targetDate else birthDate
        val end = if (birthDate.isAfter(targetDate)) birthDate else targetDate

        val period = Period.between(start, end)
        val totalDays = ChronoUnit.DAYS.between(start, end)

        // Next Birthday calculation
        val nextBirthdayYear = if (end.monthValue > start.monthValue || 
            (end.monthValue == start.monthValue && end.dayOfMonth >= start.dayOfMonth)) {
            end.year + 1
        } else {
            end.year
        }

        val nextBirthday = LocalDate.of(nextBirthdayYear, start.monthValue, start.dayOfMonth)
        val daysToNextBirthday = ChronoUnit.DAYS.between(end, nextBirthday)
        val nextBirthdayDayOfWeek = nextBirthday.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

        return AgeResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            daysToNextBirthday = daysToNextBirthday,
            nextBirthdayDayOfWeek = nextBirthdayDayOfWeek
        )
    }

    /**
     * 7. Business Days Calculator (excluding Saturday & Sunday)
     */
    fun calculateBusinessDays(start: LocalDate, end: LocalDate): Long {
        if (start.isAfter(end)) return 0
        var count = 0L
        var current = start
        while (!current.isAfter(end)) {
            val dayOfWeek = current.dayOfWeek
            if (dayOfWeek != java.time.DayOfWeek.SATURDAY && dayOfWeek != java.time.DayOfWeek.SUNDAY) {
                count++
            }
            current = current.plusDays(1)
        }
        return count
    }

    /**
     * 8. Countdown Calculator
     */
    fun calculateCountdown(targetDateTime: LocalDateTime, now: LocalDateTime): CountdownResult {
        val duration = Duration.between(now, targetDateTime)
        val isPast = duration.isNegative
        val absDuration = if (isPast) duration.abs() else duration

        val days = absDuration.toDays()
        val hours = absDuration.toHours() % 24
        val minutes = absDuration.toMinutes() % 60
        val seconds = absDuration.seconds % 60

        return CountdownResult(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            isPast = isPast
        )
    }

    /**
     * 9. Time Difference Calculator
     */
    fun calculateTimeDifference(start: LocalTime, end: LocalTime): TimeDiffResult {
        val duration = Duration.between(start, end)
        val absDuration = if (duration.isNegative) duration.plus(Duration.ofDays(1)) else duration

        val hours = absDuration.toHours()
        val minutes = absDuration.toMinutes() % 60
        val seconds = absDuration.seconds % 60

        return TimeDiffResult(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalMinutes = absDuration.toMinutes(),
            totalSeconds = absDuration.seconds
        )
    }

    /**
     * 10. Time Zone conversion prepared for future expansion (disabled/offline by default)
     */
    fun convertTimeZone(time: LocalTime, sourceZoneId: String, targetZoneId: String): LocalTime {
        val now = LocalDate.now()
        val sourceZone = ZoneId.of(sourceZoneId)
        val targetZone = ZoneId.of(targetZoneId)

        val sourceZonedDateTime = ZonedDateTime.of(now, time, sourceZone)
        val targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(targetZone)

        return targetZonedDateTime.toLocalTime()
    }
}
