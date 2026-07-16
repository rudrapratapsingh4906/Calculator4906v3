package com.example.domain.usecase

import com.example.domain.model.AgeCalculationResult
import java.util.Calendar

class CalculateAgeUseCase {
    operator fun invoke(dob: Calendar, currentDate: Calendar): AgeCalculationResult? {
        // Strip time
        val dobDate = stripTime(dob)
        val curDate = stripTime(currentDate)

        if (dobDate.after(curDate)) return null

        val birthYear = dobDate.get(Calendar.YEAR)
        val birthMonth = dobDate.get(Calendar.MONTH)
        val birthDay = dobDate.get(Calendar.DAY_OF_MONTH)

        val currentYear = curDate.get(Calendar.YEAR)
        val currentMonth = curDate.get(Calendar.MONTH)
        val currentDay = curDate.get(Calendar.DAY_OF_MONTH)

        var years = currentYear - birthYear
        var months = currentMonth - birthMonth
        var days = currentDay - birthDay

        if (days < 0) {
            months--
            val temp = curDate.clone() as Calendar
            temp.add(Calendar.MONTH, -1)
            days += temp.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        if (months < 0) {
            years--
            months += 12
        }

        val totalMonths = (years * 12L) + months
        val millisDiff = curDate.timeInMillis - dobDate.timeInMillis
        val totalDays = millisDiff / (1000 * 60 * 60 * 24)
        val totalWeeks = totalDays / 7
        val totalHours = totalDays * 24
        val totalMinutes = totalHours * 60

        // Next birthday
        val nextBirthday = Calendar.getInstance().apply {
            timeInMillis = dobDate.timeInMillis
            set(Calendar.YEAR, currentYear)
        }

        if (nextBirthday.before(curDate)) {
            nextBirthday.add(Calendar.YEAR, 1)
        }

        val tempCur = curDate.clone() as Calendar
        var nextBMonths = 0
        while (true) {
            val nextMonthCheck = tempCur.clone() as Calendar
            nextMonthCheck.add(Calendar.MONTH, 1)
            if (nextMonthCheck.timeInMillis <= nextBirthday.timeInMillis) {
                nextBMonths++
                tempCur.add(Calendar.MONTH, 1)
            } else {
                break
            }
        }
        val nextBDays = (nextBirthday.timeInMillis - tempCur.timeInMillis) / (1000 * 60 * 60 * 24)

        return AgeCalculationResult(
            years = years,
            months = months,
            days = days,
            totalMonths = totalMonths,
            totalWeeks = totalWeeks,
            totalDays = totalDays,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            nextBirthdayMonths = nextBMonths,
            nextBirthdayDays = nextBDays.toInt()
        )
    }

    private fun stripTime(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
