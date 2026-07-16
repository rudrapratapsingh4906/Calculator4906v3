package com.example.feature.healthcalculator

sealed interface HealthCalculatorEvent {
    data class GenderChanged(val gender: Gender) : HealthCalculatorEvent
    data class UnitSystemChanged(val system: UnitSystem) : HealthCalculatorEvent
    data class AgeChanged(val value: String) : HealthCalculatorEvent
    data class HeightCmChanged(val value: String) : HealthCalculatorEvent
    data class HeightFtChanged(val value: String) : HealthCalculatorEvent
    data class HeightInChanged(val value: String) : HealthCalculatorEvent
    data class WeightKgChanged(val value: String) : HealthCalculatorEvent
    data class WeightLbsChanged(val value: String) : HealthCalculatorEvent
    data class WaistCmChanged(val value: String) : HealthCalculatorEvent
    data class WaistInChanged(val value: String) : HealthCalculatorEvent
    data class ActivityLevelChanged(val level: ActivityLevel) : HealthCalculatorEvent
    
    object ClearAll : HealthCalculatorEvent
    object SaveToHistory : HealthCalculatorEvent
}
