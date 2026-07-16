package com.example.domain.model

data class AgeCalculationResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalMonths: Long,
    val totalWeeks: Long,
    val totalDays: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val nextBirthdayMonths: Int,
    val nextBirthdayDays: Int
)
