package com.example.feature.agecalculator

import com.example.domain.model.AgeCalculationResult
import java.util.Calendar

data class AgeCalculatorState(
    val dob: Calendar? = null,
    val currentDate: Calendar = Calendar.getInstance(),
    val result: AgeCalculationResult? = null,
    val error: String? = null
)
