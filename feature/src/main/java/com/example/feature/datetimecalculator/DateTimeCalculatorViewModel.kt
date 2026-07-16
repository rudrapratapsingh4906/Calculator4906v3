package com.example.feature.datetimecalculator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Calculation
import com.example.domain.repository.CalculationRepository
import com.example.feature.datetimecalculator.domain.DateTimeCalculations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

class DateTimeCalculatorViewModel(
    private val calculationRepository: CalculationRepository,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("date_time_calculator_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(DateTimeCalculatorState())
    val state: StateFlow<DateTimeCalculatorState> = _state.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        restoreState()
    }

    private fun restoreState() {
        try {
            val mode = DateTimeMode.valueOf(prefs.getString("last_mode", DateTimeMode.DATE_DIFFERENCE.name) ?: DateTimeMode.DATE_DIFFERENCE.name)
            
            val dateDiffStart = LocalDate.parse(prefs.getString("date_diff_start", LocalDate.now().toString()) ?: LocalDate.now().toString())
            val dateDiffEnd = LocalDate.parse(prefs.getString("date_diff_end", LocalDate.now().plusDays(1).toString()) ?: LocalDate.now().plusDays(1).toString())
            
            val addSubDate = LocalDate.parse(prefs.getString("add_sub_date", LocalDate.now().toString()) ?: LocalDate.now().toString())
            val addSubAmount = prefs.getString("add_sub_amount", "10") ?: "10"
            val addSubUnit = ChronoUnit.valueOf(prefs.getString("add_sub_unit", ChronoUnit.DAYS.name) ?: ChronoUnit.DAYS.name)
            val addSubIsAddition = prefs.getBoolean("add_sub_is_addition", true)

            val dayOfWeekDate = LocalDate.parse(prefs.getString("day_of_week_date", LocalDate.now().toString()) ?: LocalDate.now().toString())
            val leapYearValue = prefs.getString("leap_year_value", LocalDate.now().year.toString()) ?: LocalDate.now().year.toString()

            val ageBirthDate = LocalDate.parse(prefs.getString("age_birth_date", "2000-01-01") ?: "2000-01-01")
            val ageTargetDate = LocalDate.parse(prefs.getString("age_target_date", LocalDate.now().toString()) ?: LocalDate.now().toString())

            val businessStart = LocalDate.parse(prefs.getString("business_start", LocalDate.now().toString()) ?: LocalDate.now().toString())
            val businessEnd = LocalDate.parse(prefs.getString("business_end", LocalDate.now().plusDays(10).toString()) ?: LocalDate.now().plusDays(10).toString())

            val countdownTargetDate = LocalDate.parse(prefs.getString("countdown_target_date", LocalDate.now().plusMonths(1).toString()) ?: LocalDate.now().plusMonths(1).toString())
            val countdownTargetTime = LocalTime.parse(prefs.getString("countdown_target_time", "12:00") ?: "12:00")

            val timeDiffStart = LocalTime.parse(prefs.getString("time_diff_start", "09:00") ?: "09:00")
            val timeDiffEnd = LocalTime.parse(prefs.getString("time_diff_end", "17:00") ?: "17:00")

            val timeZoneTime = LocalTime.parse(prefs.getString("time_zone_time", LocalTime.now().format(timeFormatter)) ?: "12:00")
            val timeZoneSource = prefs.getString("time_zone_source", "UTC") ?: "UTC"
            val timeZoneTarget = prefs.getString("time_zone_target", "Asia/Kolkata") ?: "Asia/Kolkata"

            _state.update {
                it.copy(
                    currentMode = mode,
                    dateDiffStart = dateDiffStart,
                    dateDiffEnd = dateDiffEnd,
                    addSubDate = addSubDate,
                    addSubAmount = addSubAmount,
                    addSubUnit = addSubUnit,
                    addSubIsAddition = addSubIsAddition,
                    dayOfWeekDate = dayOfWeekDate,
                    leapYearValue = leapYearValue,
                    ageBirthDate = ageBirthDate,
                    ageTargetDate = ageTargetDate,
                    businessStart = businessStart,
                    businessEnd = businessEnd,
                    countdownTargetDate = countdownTargetDate,
                    countdownTargetTime = countdownTargetTime,
                    timeDiffStart = timeDiffStart,
                    timeDiffEnd = timeDiffEnd,
                    timeZoneTime = timeZoneTime,
                    timeZoneSource = timeZoneSource,
                    timeZoneTarget = timeZoneTarget
                )
            }
            validateAndCalculate()
        } catch (e: Exception) {
            // Rollback to default values if parse error occurs
            validateAndCalculate()
        }
    }

    private fun persist(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    private fun persistBool(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun onEvent(event: DateTimeCalculatorEvent) {
        when (event) {
            is DateTimeCalculatorEvent.ChangeMode -> {
                persist("last_mode", event.mode.name)
                _state.update { it.copy(currentMode = event.mode) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeDateDiffStart -> {
                persist("date_diff_start", event.date.toString())
                _state.update { it.copy(dateDiffStart = event.date) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeDateDiffEnd -> {
                persist("date_diff_end", event.date.toString())
                _state.update { it.copy(dateDiffEnd = event.date) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeAddSubDate -> {
                persist("add_sub_date", event.date.toString())
                _state.update { it.copy(addSubDate = event.date) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeAddSubAmount -> {
                persist("add_sub_amount", event.amount)
                _state.update { it.copy(addSubAmount = event.amount) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeAddSubUnit -> {
                persist("add_sub_unit", event.unit.name)
                _state.update { it.copy(addSubUnit = event.unit) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeAddSubIsAddition -> {
                persistBool("add_sub_is_addition", event.isAddition)
                _state.update { it.copy(addSubIsAddition = event.isAddition) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeDayOfWeekDate -> {
                persist("day_of_week_date", event.date.toString())
                _state.update { it.copy(dayOfWeekDate = event.date) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeLeapYearValue -> {
                persist("leap_year_value", event.year)
                _state.update { it.copy(leapYearValue = event.year) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeAgeBirthDate -> {
                persist("age_birth_date", event.date.toString())
                _state.update { it.copy(ageBirthDate = event.date) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeAgeTargetDate -> {
                persist("age_target_date", event.date.toString())
                _state.update { it.copy(ageTargetDate = event.date) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeBusinessStart -> {
                persist("business_start", event.date.toString())
                _state.update { it.copy(businessStart = event.date) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeBusinessEnd -> {
                persist("business_end", event.date.toString())
                _state.update { it.copy(businessEnd = event.date) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeCountdownTargetDate -> {
                persist("countdown_target_date", event.date.toString())
                _state.update { it.copy(countdownTargetDate = event.date) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeCountdownTargetTime -> {
                persist("countdown_target_time", event.time.toString())
                _state.update { it.copy(countdownTargetTime = event.time) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeTimeDiffStart -> {
                persist("time_diff_start", event.time.toString())
                _state.update { it.copy(timeDiffStart = event.time) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeTimeDiffEnd -> {
                persist("time_diff_end", event.time.toString())
                _state.update { it.copy(timeDiffEnd = event.time) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeTimeZoneTime -> {
                persist("time_zone_time", event.time.toString())
                _state.update { it.copy(timeZoneTime = event.time) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeTimeZoneSource -> {
                persist("time_zone_source", event.zoneId)
                _state.update { it.copy(timeZoneSource = event.zoneId, isSelectingSourceZone = false) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeTimeZoneTarget -> {
                persist("time_zone_target", event.zoneId)
                _state.update { it.copy(timeZoneTarget = event.zoneId, isSelectingTargetZone = false) }
                validateAndCalculate()
            }
            is DateTimeCalculatorEvent.ChangeTimeZoneSearchQuery -> {
                _state.update { it.copy(timeZoneSearchQuery = event.query) }
            }
            is DateTimeCalculatorEvent.SetSelectingSourceZone -> {
                _state.update { it.copy(isSelectingSourceZone = event.selecting, timeZoneSearchQuery = "") }
            }
            is DateTimeCalculatorEvent.SetSelectingTargetZone -> {
                _state.update { it.copy(isSelectingTargetZone = event.selecting, timeZoneSearchQuery = "") }
            }
            DateTimeCalculatorEvent.ClearInputs -> {
                resetCurrentModeInputs()
            }
            DateTimeCalculatorEvent.CopyResultToClipboard -> {
                // Done in Compose UI but triggers SaveToHistory
                saveToHistory()
            }
            DateTimeCalculatorEvent.SaveToHistory -> {
                saveToHistory()
            }
        }
    }

    private fun resetCurrentModeInputs() {
        val today = LocalDate.now()
        _state.update {
            when (it.currentMode) {
                DateTimeMode.DATE_DIFFERENCE -> it.copy(dateDiffStart = today, dateDiffEnd = today.plusDays(1), error = null)
                DateTimeMode.ADD_SUBTRACT_DATE -> it.copy(addSubDate = today, addSubAmount = "0", addSubUnit = ChronoUnit.DAYS, addSubIsAddition = true, error = null)
                DateTimeMode.DAY_OF_WEEK -> it.copy(dayOfWeekDate = today, error = null)
                DateTimeMode.LEAP_YEAR -> it.copy(leapYearValue = today.year.toString(), error = null)
                DateTimeMode.AGE_BETWEEN_DATES -> it.copy(ageBirthDate = LocalDate.of(2000, 1, 1), ageTargetDate = today, error = null)
                DateTimeMode.BUSINESS_DAYS -> it.copy(businessStart = today, businessEnd = today.plusDays(10), error = null)
                DateTimeMode.COUNTDOWN -> it.copy(countdownTargetDate = today.plusMonths(1), countdownTargetTime = LocalTime.of(12, 0), error = null)
                DateTimeMode.TIME_DIFFERENCE -> it.copy(timeDiffStart = LocalTime.of(9, 0), timeDiffEnd = LocalTime.of(17, 0), error = null)
                DateTimeMode.TIME_ZONE -> it.copy(timeZoneTime = LocalTime.of(12, 0), timeZoneSource = "UTC", timeZoneTarget = "Asia/Kolkata", error = null)
            }
        }
        validateAndCalculate()
    }

    private fun validateAndCalculate() {
        val s = _state.value
        var errorMsg: String? = null

        when (s.currentMode) {
            DateTimeMode.DATE_DIFFERENCE -> {
                if (s.dateDiffStart.isAfter(s.dateDiffEnd)) {
                    errorMsg = "Start date must be before or equal to End date."
                }
            }
            DateTimeMode.ADD_SUBTRACT_DATE -> {
                val amt = s.addSubAmount.toLongOrNull()
                if (amt == null || amt < 0) {
                    errorMsg = "Please enter a valid non-negative integer amount."
                }
            }
            DateTimeMode.LEAP_YEAR -> {
                val yr = s.leapYearValue.toIntOrNull()
                if (yr == null || yr < 1 || yr > 9999) {
                    errorMsg = "Please enter a valid year between 1 and 9999."
                }
            }
            DateTimeMode.AGE_BETWEEN_DATES -> {
                if (s.ageBirthDate.isAfter(s.ageTargetDate)) {
                    errorMsg = "Birth date must be before target/end date."
                }
            }
            DateTimeMode.BUSINESS_DAYS -> {
                if (s.businessStart.isAfter(s.businessEnd)) {
                    errorMsg = "Start date must be before or equal to End date."
                }
            }
            else -> {
                // Time difference, Zone converter, Day finder and countdown have standard safe ranges
            }
        }

        _state.update { it.copy(error = errorMsg) }
    }

    fun getResultText(): String {
        val s = _state.value
        if (s.error != null) return "Invalid Input: ${s.error}"

        return try {
            when (s.currentMode) {
                DateTimeMode.DATE_DIFFERENCE -> {
                    val result = DateTimeCalculations.calculateDateDifference(s.dateDiffStart, s.dateDiffEnd)
                    val out = StringBuilder()
                    out.append("${result.years} years, ${result.months} months, ${result.days} days\n\n")
                    out.append("Or total time equivalent:\n")
                    out.append("• ${result.totalDays} total days\n")
                    out.append("• ${result.totalWeeks} weeks and ${result.remainingDaysOfWeek} days")
                    out.toString()
                }
                DateTimeMode.ADD_SUBTRACT_DATE -> {
                    val amount = s.addSubAmount.toLongOrNull() ?: 0L
                    val finalDate = DateTimeCalculations.modifyDate(s.addSubDate, amount, s.addSubUnit, s.addSubIsAddition)
                    val op = if (s.addSubIsAddition) "Added" else "Subtracted"
                    "Result Date: ${finalDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))}\n\n($op $amount ${s.addSubUnit.name.lowercase()} from ${s.addSubDate})"
                }
                DateTimeMode.DAY_OF_WEEK -> {
                    val day = DateTimeCalculations.getDayOfWeekFinder(s.dayOfWeekDate)
                    "Day of the Week: $day\n\nDate: ${s.dayOfWeekDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}"
                }
                DateTimeMode.LEAP_YEAR -> {
                    val year = s.leapYearValue.toIntOrNull() ?: LocalDate.now().year
                    val isLeap = DateTimeCalculations.isLeapYear(year)
                    if (isLeap) {
                        "Year $year is a LEAP YEAR!\n\nIt contains 366 days instead of 365 days."
                    } else {
                        "Year $year is a COMMON YEAR.\n\nIt contains 365 days."
                    }
                }
                DateTimeMode.AGE_BETWEEN_DATES -> {
                    val result = DateTimeCalculations.calculateAge(s.ageBirthDate, s.ageTargetDate)
                    val out = StringBuilder()
                    out.append("Current Age:\n")
                    out.append("${result.years} Years, ${result.months} Months, ${result.days} Days\n\n")
                    out.append("• Total days lived: ${result.totalDays} days\n")
                    out.append("• Days to next birthday: ${result.daysToNextBirthday} days (will fall on a ${result.nextBirthdayDayOfWeek})")
                    out.toString()
                }
                DateTimeMode.BUSINESS_DAYS -> {
                    val count = DateTimeCalculations.calculateBusinessDays(s.businessStart, s.businessEnd)
                    "Total Business Days: $count\n\nExcludes Saturdays and Sundays between ${s.businessStart} and ${s.businessEnd}."
                }
                DateTimeMode.COUNTDOWN -> {
                    val target = LocalDateTime.of(s.countdownTargetDate, s.countdownTargetTime)
                    val result = DateTimeCalculations.calculateCountdown(target, LocalDateTime.now())
                    if (result.isPast) {
                        "Target date has already passed!\n\nTime elapsed since target:\n" +
                                "${result.days} days, ${result.hours} hours, ${result.minutes} minutes, ${result.seconds} seconds"
                    } else {
                        "Time remaining until target date:\n\n" +
                                "• ${result.days} Days\n" +
                                "• ${result.hours} Hours\n" +
                                "• ${result.minutes} Minutes\n" +
                                "• ${result.seconds} Seconds"
                    }
                }
                DateTimeMode.TIME_DIFFERENCE -> {
                    val result = DateTimeCalculations.calculateTimeDifference(s.timeDiffStart, s.timeDiffEnd)
                    "Time Difference:\n" +
                            "${result.hours} hours, ${result.minutes} minutes, ${result.seconds} seconds\n\n" +
                            "• Total minutes: ${result.totalMinutes}\n" +
                            "• Total seconds: ${result.totalSeconds}"
                }
                DateTimeMode.TIME_ZONE -> {
                    val result = DateTimeCalculations.convertTimeZone(s.timeZoneTime, s.timeZoneSource, s.timeZoneTarget)
                    "Converted Time: ${result.format(DateTimeFormatter.ofPattern("hh:mm a (HH:mm)"))}\n\n" +
                            "Source: ${s.timeZoneTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} inside ${s.timeZoneSource}\n" +
                            "Target: inside ${s.timeZoneTarget}"
                }
            }
        } catch (e: Exception) {
            "Error calculating date: ${e.message}"
        }
    }

    private fun saveToHistory() {
        val s = _state.value
        if (s.error != null) return

        val expression = when (s.currentMode) {
            DateTimeMode.DATE_DIFFERENCE -> "Diff between ${s.dateDiffStart} and ${s.dateDiffEnd}"
            DateTimeMode.ADD_SUBTRACT_DATE -> "${if (s.addSubIsAddition) "Add" else "Sub"} ${s.addSubAmount} ${s.addSubUnit.name.lowercase()} to ${s.addSubDate}"
            DateTimeMode.DAY_OF_WEEK -> "Day of week for ${s.dayOfWeekDate}"
            DateTimeMode.LEAP_YEAR -> "Is ${s.leapYearValue} a leap year?"
            DateTimeMode.AGE_BETWEEN_DATES -> "Age for birth ${s.ageBirthDate} at ${s.ageTargetDate}"
            DateTimeMode.BUSINESS_DAYS -> "Working days between ${s.businessStart} and ${s.businessEnd}"
            DateTimeMode.COUNTDOWN -> "Countdown to ${s.countdownTargetDate} ${s.countdownTargetTime}"
            DateTimeMode.TIME_DIFFERENCE -> "Diff between ${s.timeDiffStart} and ${s.timeDiffEnd}"
            DateTimeMode.TIME_ZONE -> "Convert ${s.timeZoneTime} from ${s.timeZoneSource} to ${s.timeZoneTarget}"
        }

        val result = getResultText().replace("\n", " | ")

        viewModelScope.launch {
            val calculation = Calculation(
                id = UUID.randomUUID().toString(),
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis()
            )
            calculationRepository.saveCalculation(calculation)
        }
    }
}
