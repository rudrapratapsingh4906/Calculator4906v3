package com.example.feature.datetimecalculator

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

sealed interface DateTimeCalculatorEvent {
    data class ChangeMode(val mode: DateTimeMode) : DateTimeCalculatorEvent
    
    // 1. Date Difference
    data class ChangeDateDiffStart(val date: LocalDate) : DateTimeCalculatorEvent
    data class ChangeDateDiffEnd(val date: LocalDate) : DateTimeCalculatorEvent
    
    // 2 & 3. Add/Subtract
    data class ChangeAddSubDate(val date: LocalDate) : DateTimeCalculatorEvent
    data class ChangeAddSubAmount(val amount: String) : DateTimeCalculatorEvent
    data class ChangeAddSubUnit(val unit: ChronoUnit) : DateTimeCalculatorEvent
    data class ChangeAddSubIsAddition(val isAddition: Boolean) : DateTimeCalculatorEvent
    
    // 4. Day of Week
    data class ChangeDayOfWeekDate(val date: LocalDate) : DateTimeCalculatorEvent
    
    // 5. Leap Year
    data class ChangeLeapYearValue(val year: String) : DateTimeCalculatorEvent
    
    // 6. Age
    data class ChangeAgeBirthDate(val date: LocalDate) : DateTimeCalculatorEvent
    data class ChangeAgeTargetDate(val date: LocalDate) : DateTimeCalculatorEvent
    
    // 7. Business Days
    data class ChangeBusinessStart(val date: LocalDate) : DateTimeCalculatorEvent
    data class ChangeBusinessEnd(val date: LocalDate) : DateTimeCalculatorEvent
    
    // 8. Countdown
    data class ChangeCountdownTargetDate(val date: LocalDate) : DateTimeCalculatorEvent
    data class ChangeCountdownTargetTime(val time: LocalTime) : DateTimeCalculatorEvent
    
    // 9. Time Difference
    data class ChangeTimeDiffStart(val time: LocalTime) : DateTimeCalculatorEvent
    data class ChangeTimeDiffEnd(val time: LocalTime) : DateTimeCalculatorEvent
    
    // 10. Time Zone
    data class ChangeTimeZoneTime(val time: LocalTime) : DateTimeCalculatorEvent
    data class ChangeTimeZoneSource(val zoneId: String) : DateTimeCalculatorEvent
    data class ChangeTimeZoneTarget(val zoneId: String) : DateTimeCalculatorEvent
    data class ChangeTimeZoneSearchQuery(val query: String) : DateTimeCalculatorEvent
    data class SetSelectingSourceZone(val selecting: Boolean) : DateTimeCalculatorEvent
    data class SetSelectingTargetZone(val selecting: Boolean) : DateTimeCalculatorEvent
    
    // Common Actions
    object ClearInputs : DateTimeCalculatorEvent
    object CopyResultToClipboard : DateTimeCalculatorEvent
    object SaveToHistory : DateTimeCalculatorEvent
}
