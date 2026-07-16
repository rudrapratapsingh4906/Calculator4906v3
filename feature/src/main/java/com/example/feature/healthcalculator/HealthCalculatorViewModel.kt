package com.example.feature.healthcalculator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Calculation
import com.example.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class HealthCalculatorViewModel(
    private val calculationRepository: CalculationRepository,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("health_calculator_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(HealthCalculatorState())
    val state: StateFlow<HealthCalculatorState> = _state.asStateFlow()

    init {
        // Load saved inputs
        val genderStr = prefs.getString("health_gender", Gender.MALE.name) ?: Gender.MALE.name
        val gender = runCatching { Gender.valueOf(genderStr) }.getOrDefault(Gender.MALE)

        val systemStr = prefs.getString("health_system", UnitSystem.METRIC.name) ?: UnitSystem.METRIC.name
        val system = runCatching { UnitSystem.valueOf(systemStr) }.getOrDefault(UnitSystem.METRIC)

        val age = prefs.getString("health_age", "") ?: ""
        val heightCm = prefs.getString("health_height_cm", "") ?: ""
        val heightFt = prefs.getString("health_height_ft", "") ?: ""
        val heightIn = prefs.getString("health_height_in", "") ?: ""
        val weightKg = prefs.getString("health_weight_kg", "") ?: ""
        val weightLbs = prefs.getString("health_weight_lbs", "") ?: ""
        val waistCm = prefs.getString("health_waist_cm", "") ?: ""
        val waistIn = prefs.getString("health_waist_in", "") ?: ""

        val activityStr = prefs.getString("health_activity", ActivityLevel.MODERATELY_ACTIVE.name) ?: ActivityLevel.MODERATELY_ACTIVE.name
        val activityLevel = runCatching { ActivityLevel.valueOf(activityStr) }.getOrDefault(ActivityLevel.MODERATELY_ACTIVE)

        _state.update {
            it.copy(
                gender = gender,
                unitSystem = system,
                age = age,
                heightCm = heightCm,
                heightFt = heightFt,
                heightIn = heightIn,
                weightKg = weightKg,
                weightLbs = weightLbs,
                waistCm = waistCm,
                waistIn = waistIn,
                activityLevel = activityLevel
            )
        }

        calculateHealth()
    }

    fun onEvent(event: HealthCalculatorEvent) {
        when (event) {
            is HealthCalculatorEvent.GenderChanged -> {
                prefs.edit().putString("health_gender", event.gender.name).apply()
                _state.update { it.copy(gender = event.gender) }
                calculateHealth()
            }
            is HealthCalculatorEvent.UnitSystemChanged -> {
                prefs.edit().putString("health_system", event.system.name).apply()
                _state.update { it.copy(unitSystem = event.system) }
                calculateHealth()
            }
            is HealthCalculatorEvent.AgeChanged -> {
                prefs.edit().putString("health_age", event.value).apply()
                _state.update { it.copy(age = event.value) }
                calculateHealth()
            }
            is HealthCalculatorEvent.HeightCmChanged -> {
                prefs.edit().putString("health_height_cm", event.value).apply()
                _state.update { it.copy(heightCm = event.value) }
                calculateHealth()
            }
            is HealthCalculatorEvent.HeightFtChanged -> {
                prefs.edit().putString("health_height_ft", event.value).apply()
                _state.update { it.copy(heightFt = event.value) }
                calculateHealth()
            }
            is HealthCalculatorEvent.HeightInChanged -> {
                prefs.edit().putString("health_height_in", event.value).apply()
                _state.update { it.copy(heightIn = event.value) }
                calculateHealth()
            }
            is HealthCalculatorEvent.WeightKgChanged -> {
                prefs.edit().putString("health_weight_kg", event.value).apply()
                _state.update { it.copy(weightKg = event.value) }
                calculateHealth()
            }
            is HealthCalculatorEvent.WeightLbsChanged -> {
                prefs.edit().putString("health_weight_lbs", event.value).apply()
                _state.update { it.copy(weightLbs = event.value) }
                calculateHealth()
            }
            is HealthCalculatorEvent.WaistCmChanged -> {
                prefs.edit().putString("health_waist_cm", event.value).apply()
                _state.update { it.copy(waistCm = event.value) }
                calculateHealth()
            }
            is HealthCalculatorEvent.WaistInChanged -> {
                prefs.edit().putString("health_waist_in", event.value).apply()
                _state.update { it.copy(waistIn = event.value) }
                calculateHealth()
            }
            is HealthCalculatorEvent.ActivityLevelChanged -> {
                prefs.edit().putString("health_activity", event.level.name).apply()
                _state.update { it.copy(activityLevel = event.level) }
                calculateHealth()
            }
            HealthCalculatorEvent.ClearAll -> {
                prefs.edit()
                    .putString("health_age", "")
                    .putString("health_height_cm", "")
                    .putString("health_height_ft", "")
                    .putString("health_height_in", "")
                    .putString("health_weight_kg", "")
                    .putString("health_weight_lbs", "")
                    .putString("health_waist_cm", "")
                    .putString("health_waist_in", "")
                    .apply()

                _state.update {
                    it.copy(
                        age = "",
                        heightCm = "",
                        heightFt = "",
                        heightIn = "",
                        weightKg = "",
                        weightLbs = "",
                        waistCm = "",
                        waistIn = "",
                        bmi = 0.0,
                        bmiClassification = "",
                        bmrMifflin = 0.0,
                        bmrHarris = 0.0,
                        maintenanceCalories = 0.0,
                        weightLossCalories = 0.0,
                        weightGainCalories = 0.0,
                        idealWeightMin = 0.0,
                        idealWeightMax = 0.0,
                        idealWeightDevine = 0.0,
                        idealWeightRobinson = 0.0,
                        idealWeightMiller = 0.0,
                        idealWeightHamwi = 0.0,
                        bodyFatBmi = 0.0,
                        bodyFatYmca = 0.0,
                        isYmcaAvailable = false,
                        error = null
                    )
                }
            }
            HealthCalculatorEvent.SaveToHistory -> {
                saveToHistory()
            }
        }
    }

    private fun calculateHealth() {
        val s = _state.value
        val isMetric = s.unitSystem == UnitSystem.METRIC

        val ageVal = s.age.toIntOrNull()
        if (ageVal == null || ageVal <= 0) {
            resetOutputs(error = "Please enter a valid age.")
            return
        }

        // Convert inputs to metric values for standard health formulas
        val heightCmCalculated: Double
        val weightKgCalculated: Double
        val waistInCalculated: Double?

        if (isMetric) {
            val hCm = s.heightCm.toDoubleOrNull()
            val wKg = s.weightKg.toDoubleOrNull()
            if (hCm == null || hCm <= 0 || wKg == null || wKg <= 0) {
                resetOutputs()
                return
            }
            heightCmCalculated = hCm
            weightKgCalculated = wKg
            waistInCalculated = s.waistCm.toDoubleOrNull()?.let { it / 2.54 }
        } else {
            val hFt = s.heightFt.toDoubleOrNull() ?: 0.0
            val hIn = s.heightIn.toDoubleOrNull() ?: 0.0
            val wLbs = s.weightLbs.toDoubleOrNull()

            val totalInches = (hFt * 12.0) + hIn
            if (totalInches <= 0 || wLbs == null || wLbs <= 0) {
                resetOutputs()
                return
            }

            heightCmCalculated = totalInches * 2.54
            weightKgCalculated = wLbs / 2.20462
            waistInCalculated = s.waistIn.toDoubleOrNull()
        }

        val heightMeters = heightCmCalculated / 100.0
        val heightInches = heightCmCalculated / 2.54

        // 1. BMI calculation
        val bmiVal = weightKgCalculated / (heightMeters * heightMeters)
        val classification = getBmiClassification(bmiVal)

        // 2. BMR (Basal Metabolic Rate) formulas
        // Mifflin-St Jeor Formula
        val bmrMifflinVal = if (s.gender == Gender.MALE) {
            (10.0 * weightKgCalculated) + (6.25 * heightCmCalculated) - (5.0 * ageVal) + 5.0
        } else {
            (10.0 * weightKgCalculated) + (6.25 * heightCmCalculated) - (5.0 * ageVal) - 161.0
        }

        // Revised Harris-Benedict Formula
        val bmrHarrisVal = if (s.gender == Gender.MALE) {
            88.362 + (13.397 * weightKgCalculated) + (4.799 * heightCmCalculated) - (5.677 * ageVal)
        } else {
            447.593 + (9.247 * weightKgCalculated) + (3.098 * heightCmCalculated) - (4.330 * ageVal)
        }

        // 3. Daily Calories (TDEE) based on Mifflin BMR
        val tdee = bmrMifflinVal * s.activityLevel.multiplier
        val weightLoss = Math.max(1000.0, tdee - 500.0) // Safe minimum for calories
        val weightGain = tdee + 500.0

        // 4. Ideal Weight Range (Based on BMI 18.5 - 24.9)
        var idealWeightMinVal = 18.5 * heightMeters * heightMeters
        var idealWeightMaxVal = 24.9 * heightMeters * heightMeters

        // Other formulas (require Height > 60 inches)
        var devine = 0.0
        var robinson = 0.0
        var miller = 0.0
        var hamwi = 0.0

        if (heightInches > 60.0) {
            val inchesOver60 = heightInches - 60.0
            if (s.gender == Gender.MALE) {
                devine = 50.0 + (2.3 * inchesOver60)
                robinson = 52.0 + (1.9 * inchesOver60)
                miller = 56.2 + (1.41 * inchesOver60)
                hamwi = 48.0 + (2.7 * inchesOver60)
            } else {
                devine = 45.5 + (2.3 * inchesOver60)
                robinson = 49.0 + (1.7 * inchesOver60)
                miller = 53.1 + (1.36 * inchesOver60)
                hamwi = 45.5 + (2.2 * inchesOver60)
            }
        }

        // If Imperial, convert all weights to Lbs for consistent display
        if (!isMetric) {
            idealWeightMinVal *= 2.20462
            idealWeightMaxVal *= 2.20462
            if (devine > 0.0) devine *= 2.20462
            if (robinson > 0.0) robinson *= 2.20462
            if (miller > 0.0) miller *= 2.20462
            if (hamwi > 0.0) hamwi *= 2.20462
        }

        // 5. Body Fat Estimate
        // Deurenberg BMI formula
        val genderVal = if (s.gender == Gender.MALE) 1.0 else 0.0
        val bodyFatBmiVal = (1.20 * bmiVal) + (0.23 * ageVal) - (16.2 * genderVal) - 5.4

        // YMCA Formula if waist is entered
        var bodyFatYmcaVal = 0.0
        var hasYmca = false

        if (waistInCalculated != null && waistInCalculated > 0.0) {
            val weightLbsCalculated = weightKgCalculated * 2.20462
            bodyFatYmcaVal = if (s.gender == Gender.MALE) {
                (((4.15 * waistInCalculated) - (0.082 * weightLbsCalculated) - 98.42) / weightLbsCalculated) * 100.0
            } else {
                (((4.15 * waistInCalculated) - (0.082 * weightLbsCalculated) - 76.76) / weightLbsCalculated) * 100.0
            }
            if (bodyFatYmcaVal in 2.0..60.0) {
                hasYmca = true
            }
        }

        _state.update {
            it.copy(
                bmi = bmiVal,
                bmiClassification = classification,
                bmrMifflin = bmrMifflinVal,
                bmrHarris = bmrHarrisVal,
                maintenanceCalories = tdee,
                weightLossCalories = weightLoss,
                weightGainCalories = weightGain,
                idealWeightMin = idealWeightMinVal,
                idealWeightMax = idealWeightMaxVal,
                idealWeightDevine = devine,
                idealWeightRobinson = robinson,
                idealWeightMiller = miller,
                idealWeightHamwi = hamwi,
                bodyFatBmi = Math.max(2.0, Math.min(60.0, bodyFatBmiVal)),
                bodyFatYmca = bodyFatYmcaVal,
                isYmcaAvailable = hasYmca,
                error = null
            )
        }
    }

    private fun getBmiClassification(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            bmi < 35.0 -> "Obesity Class I"
            bmi < 40.0 -> "Obesity Class II"
            else -> "Obesity Class III"
        }
    }

    private fun resetOutputs(error: String? = null) {
        _state.update {
            it.copy(
                bmi = 0.0,
                bmiClassification = "",
                bmrMifflin = 0.0,
                bmrHarris = 0.0,
                maintenanceCalories = 0.0,
                weightLossCalories = 0.0,
                weightGainCalories = 0.0,
                idealWeightMin = 0.0,
                idealWeightMax = 0.0,
                idealWeightDevine = 0.0,
                idealWeightRobinson = 0.0,
                idealWeightMiller = 0.0,
                idealWeightHamwi = 0.0,
                bodyFatBmi = 0.0,
                bodyFatYmca = 0.0,
                isYmcaAvailable = false,
                error = error
            )
        }
    }

    private fun saveToHistory() {
        val s = _state.value
        if (s.bmi > 0.0 && s.error == null) {
            val weightUnit = if (s.unitSystem == UnitSystem.METRIC) "kg" else "lbs"
            val weightStr = if (s.unitSystem == UnitSystem.METRIC) s.weightKg else s.weightLbs
            val heightStr = if (s.unitSystem == UnitSystem.METRIC) "${s.heightCm} cm" else "${s.heightFt} ft ${s.heightIn} in"

            val summaryExpression = "Health: ${s.gender.displayName}, ${s.age} yrs, $heightStr, $weightStr $weightUnit"
            val summaryResult = "BMI: ${formatDouble(s.bmi, 1)} (${s.bmiClassification}), BMR: ${formatDouble(s.bmrMifflin, 0)} kcal, Body Fat: ${formatDouble(s.bodyFatBmi, 1)}%"

            viewModelScope.launch {
                val calculation = Calculation(
                    id = UUID.randomUUID().toString(),
                    expression = summaryExpression,
                    result = summaryResult,
                    timestamp = System.currentTimeMillis()
                )
                calculationRepository.saveCalculation(calculation)
            }
        }
    }

    private fun formatDouble(value: Double, decimals: Int = 1): String {
        if (value.isNaN() || value.isInfinite()) return "0.0"
        val bd = BigDecimal(value.toString())
            .setScale(decimals, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        return bd.toPlainString()
    }
}
