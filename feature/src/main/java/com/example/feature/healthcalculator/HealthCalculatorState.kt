package com.example.feature.healthcalculator

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female")
}

enum class UnitSystem(val displayName: String) {
    METRIC("Metric (cm, kg, cm)"),
    IMPERIAL("Imperial (ft/in, lbs, in)")
}

enum class ActivityLevel(val displayName: String, val detail: String, val multiplier: Double) {
    SEDENTARY("Sedentary", "Little or no exercise", 1.2),
    LIGHTLY_ACTIVE("Lightly Active", "Exercise 1-3 days/week", 1.375),
    MODERATELY_ACTIVE("Moderately Active", "Exercise 3-5 days/week", 1.55),
    VERY_ACTIVE("Very Active", "Exercise 6-7 days/week", 1.725),
    EXTRA_ACTIVE("Extra Active", "Hard physical work or 2x training", 1.9)
}

data class HealthCalculatorState(
    // Inputs
    val gender: Gender = Gender.MALE,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val age: String = "",
    val heightCm: String = "",
    val heightFt: String = "",
    val heightIn: String = "",
    val weightKg: String = "",
    val weightLbs: String = "",
    val waistCm: String = "",
    val waistIn: String = "",
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
    
    // Outputs - BMI
    val bmi: Double = 0.0,
    val bmiClassification: String = "",
    
    // Outputs - BMR
    val bmrMifflin: Double = 0.0,
    val bmrHarris: Double = 0.0,
    
    // Outputs - Calories
    val maintenanceCalories: Double = 0.0,
    val weightLossCalories: Double = 0.0,
    val weightGainCalories: Double = 0.0,
    
    // Outputs - Ideal Weight
    val idealWeightMin: Double = 0.0,
    val idealWeightMax: Double = 0.0,
    val idealWeightDevine: Double = 0.0,
    val idealWeightRobinson: Double = 0.0,
    val idealWeightMiller: Double = 0.0,
    val idealWeightHamwi: Double = 0.0,
    
    // Outputs - Body Fat
    val bodyFatBmi: Double = 0.0,
    val bodyFatYmca: Double = 0.0,
    val isYmcaAvailable: Boolean = false,
    
    // UI state
    val error: String? = null
)
