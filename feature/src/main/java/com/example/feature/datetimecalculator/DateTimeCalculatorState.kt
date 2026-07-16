package com.example.feature.datetimecalculator

import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class DateTimeMode {
    DATE_DIFFERENCE,
    ADD_SUBTRACT_DATE,
    DAY_OF_WEEK,
    LEAP_YEAR,
    AGE_BETWEEN_DATES,
    BUSINESS_DAYS,
    COUNTDOWN,
    TIME_DIFFERENCE,
    TIME_ZONE
}

data class DateTimeCalculatorState(
    val currentMode: DateTimeMode = DateTimeMode.DATE_DIFFERENCE,

    // 1. Date Difference Inputs
    val dateDiffStart: LocalDate = LocalDate.now(),
    val dateDiffEnd: LocalDate = LocalDate.now().plusDays(1),

    // 2 & 3. Add/Subtract Inputs
    val addSubDate: LocalDate = LocalDate.now(),
    val addSubAmount: String = "10",
    val addSubUnit: ChronoUnit = ChronoUnit.DAYS,
    val addSubIsAddition: Boolean = true,

    // 4. Day of Week Inputs
    val dayOfWeekDate: LocalDate = LocalDate.now(),

    // 5. Leap Year Inputs
    val leapYearValue: String = LocalDate.now().year.toString(),

    // 6. Age Between Dates Inputs
    val ageBirthDate: LocalDate = LocalDate.of(2000, 1, 1),
    val ageTargetDate: LocalDate = LocalDate.now(),

    // 7. Business Days Inputs
    val businessStart: LocalDate = LocalDate.now(),
    val businessEnd: LocalDate = LocalDate.now().plusDays(10),

    // 8. Countdown Inputs
    val countdownTargetDate: LocalDate = LocalDate.now().plusMonths(1),
    val countdownTargetTime: LocalTime = LocalTime.of(12, 0),

    // 9. Time Difference Inputs
    val timeDiffStart: LocalTime = LocalTime.of(9, 0),
    val timeDiffEnd: LocalTime = LocalTime.of(17, 0),

    // 10. Time Zone Inputs
    val timeZoneTime: LocalTime = LocalTime.now(),
    val timeZoneSource: String = "UTC",
    val timeZoneTarget: String = "Asia/Kolkata",

    // Validation/Result errors
    val error: String? = null,
    
    // Loaded states or dropdown controls
    val isSelectingSourceZone: Boolean = false,
    val isSelectingTargetZone: Boolean = false,
    val timeZoneSearchQuery: String = ""
)
