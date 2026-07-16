package com.example.feature.agecalculator

import java.util.Calendar

sealed class AgeCalculatorEvent {
    data class SetDob(val date: Calendar) : AgeCalculatorEvent()
    data class SetCurrentDate(val date: Calendar) : AgeCalculatorEvent()
    object Calculate : AgeCalculatorEvent()
    object Clear : AgeCalculatorEvent()
}
