package com.example.feature.percentagecgpa

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

class PercentageCgpaViewModel(
    private val calculationRepository: CalculationRepository,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("percentage_cgpa_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(PercentageCgpaState())
    val state: StateFlow<PercentageCgpaState> = _state.asStateFlow()

    init {
        // Load saved states
        val activeToolName = prefs.getString("last_tool", PercentageToolType.PERCENTAGE_OF_NUMBER.name)
        val initialTool = runCatching { PercentageToolType.valueOf(activeToolName ?: "") }
            .getOrDefault(PercentageToolType.PERCENTAGE_OF_NUMBER)
            
        val inputA = prefs.getString("input_a_$activeToolName", "") ?: ""
        val inputB = prefs.getString("input_b_$activeToolName", "") ?: ""
        val isGstInclusive = prefs.getBoolean("gst_inclusive", false)
        val cgpaFormulaMultiplier = prefs.getString("cgpa_multiplier", "9.5") ?: "9.5"
        val isCreditBased = prefs.getBoolean("is_credit_based", true)
        
        _state.update {
            it.copy(
                activeTool = initialTool,
                inputA = inputA,
                inputB = inputB,
                isGstInclusive = isGstInclusive,
                cgpaFormulaMultiplier = cgpaFormulaMultiplier,
                isCreditBased = isCreditBased,
                semesters = loadSemesters()
            )
        }
        
        calculatePercentage()
        calculateCgpa()
    }

    fun onEvent(event: PercentageCgpaEvent) {
        when (event) {
            is PercentageCgpaEvent.SelectTool -> {
                prefs.edit().putString("last_tool", event.tool.name).apply()
                val savedInputA = prefs.getString("input_a_${event.tool.name}", "") ?: ""
                val savedInputB = prefs.getString("input_b_${event.tool.name}", "") ?: ""
                _state.update {
                    it.copy(
                        activeTool = event.tool,
                        inputA = savedInputA,
                        inputB = savedInputB,
                        percentageResult = "",
                        percentageResultLabel = "",
                        percentageSecondaryResult = "",
                        percentageError = null
                    )
                }
                calculatePercentage()
            }
            is PercentageCgpaEvent.InputAChanged -> {
                prefs.edit().putString("input_a_${_state.value.activeTool.name}", event.value).apply()
                _state.update { it.copy(inputA = event.value) }
                calculatePercentage()
            }
            is PercentageCgpaEvent.InputBChanged -> {
                prefs.edit().putString("input_b_${_state.value.activeTool.name}", event.value).apply()
                _state.update { it.copy(inputB = event.value) }
                calculatePercentage()
            }
            is PercentageCgpaEvent.GstInclusiveChanged -> {
                prefs.edit().putBoolean("gst_inclusive", event.isInclusive).apply()
                _state.update { it.copy(isGstInclusive = event.isInclusive) }
                calculatePercentage()
            }
            is PercentageCgpaEvent.ClearPercentageInputs -> {
                prefs.edit()
                    .putString("input_a_${_state.value.activeTool.name}", "")
                    .putString("input_b_${_state.value.activeTool.name}", "")
                    .apply()
                _state.update {
                    it.copy(
                        inputA = "",
                        inputB = "",
                        percentageResult = "",
                        percentageResultLabel = "",
                        percentageSecondaryResult = "",
                        percentageError = null
                    )
                }
            }
            is PercentageCgpaEvent.AddSemester -> {
                val newSemester = Semester(
                    id = UUID.randomUUID().toString(),
                    name = event.name,
                    gpa = event.gpa,
                    credits = event.credits
                )
                val updated = _state.value.semesters + newSemester
                _state.update { it.copy(semesters = updated) }
                saveSemesters(updated)
                calculateCgpa()
            }
            is PercentageCgpaEvent.EditSemester -> {
                val updated = _state.value.semesters.map {
                    if (it.id == event.id) {
                        it.copy(name = event.name, gpa = event.gpa, credits = event.credits)
                    } else it
                }
                _state.update { it.copy(semesters = updated) }
                saveSemesters(updated)
                calculateCgpa()
            }
            is PercentageCgpaEvent.DeleteSemester -> {
                val updated = _state.value.semesters.filterNot { it.id == event.id }
                _state.update { it.copy(semesters = updated) }
                saveSemesters(updated)
                calculateCgpa()
            }
            is PercentageCgpaEvent.FormulaMultiplierChanged -> {
                prefs.edit().putString("cgpa_multiplier", event.multiplier).apply()
                _state.update { it.copy(cgpaFormulaMultiplier = event.multiplier) }
                calculateCgpa()
            }
            is PercentageCgpaEvent.CreditBasedToggled -> {
                prefs.edit().putBoolean("is_credit_based", event.isCreditBased).apply()
                _state.update { it.copy(isCreditBased = event.isCreditBased) }
                calculateCgpa()
            }
            is PercentageCgpaEvent.ResetCgpa -> {
                _state.update {
                    it.copy(
                        semesters = emptyList(),
                        calculatedCgpa = 0.0,
                        calculatedPercentage = 0.0,
                        totalCredits = 0.0
                    )
                }
                saveSemesters(emptyList())
            }
        }
    }

    private fun calculatePercentage() {
        val activeTool = _state.value.activeTool
        val inputAStr = _state.value.inputA
        val inputBStr = _state.value.inputB

        if (inputAStr.isEmpty() || inputBStr.isEmpty()) {
            _state.update {
                it.copy(
                    percentageResult = "",
                    percentageResultLabel = "",
                    percentageSecondaryResult = "",
                    percentageError = null
                )
            }
            return
        }

        val a = inputAStr.toDoubleOrNull()
        val b = inputBStr.toDoubleOrNull()

        if (a == null || b == null) {
            _state.update {
                it.copy(
                    percentageResult = "",
                    percentageResultLabel = "",
                    percentageSecondaryResult = "",
                    percentageError = "Please enter valid numbers"
                )
            }
            return
        }

        try {
            var result = ""
            var label = ""
            var secondaryResult = ""
            var error: String? = null

            when (activeTool) {
                PercentageToolType.PERCENTAGE_OF_NUMBER -> {
                    // a% of b
                    val value = (a / 100.0) * b
                    result = formatDouble(value)
                    label = "$a% of $b"
                }
                PercentageToolType.X_IS_WHAT_P_OF_Y -> {
                    // a is what % of b
                    if (b == 0.0) {
                        error = "Cannot divide by zero"
                    } else {
                        val value = (a / b) * 100.0
                        result = formatDouble(value) + "%"
                        label = "$a is what % of $b"
                    }
                }
                PercentageToolType.PERCENTAGE_INCREASE -> {
                    // increase from a to b
                    if (a == 0.0) {
                        error = "Initial value cannot be zero"
                    } else {
                        val value = ((b - a) / a) * 100.0
                        result = formatDouble(value) + "%"
                        label = "% Increase from $a to $b"
                    }
                }
                PercentageToolType.PERCENTAGE_DECREASE -> {
                    // decrease from a to b
                    if (a == 0.0) {
                        error = "Initial value cannot be zero"
                    } else {
                        val value = ((a - b) / a) * 100.0
                        result = formatDouble(value) + "%"
                        label = "% Decrease from $a to $b"
                    }
                }
                PercentageToolType.PERCENTAGE_DIFFERENCE -> {
                    // diff between a and b
                    val denom = (a + b) / 2.0
                    if (denom == 0.0) {
                        error = "Cannot calculate difference when sum is zero"
                    } else {
                        val value = (Math.abs(a - b) / denom) * 100.0
                        result = formatDouble(value) + "%"
                        label = "% Difference between $a and $b"
                    }
                }
                PercentageToolType.ADD_PERCENTAGE -> {
                    // add b% to a
                    val value = a + (b / 100.0) * a
                    result = formatDouble(value)
                    label = "$a + $b%"
                }
                PercentageToolType.SUBTRACT_PERCENTAGE -> {
                    // subtract b% from a
                    val value = a - (b / 100.0) * a
                    result = formatDouble(value)
                    label = "$a - $b%"
                }
                PercentageToolType.DISCOUNT -> {
                    // price a, discount b%
                    val discountVal = a * (b / 100.0)
                    val finalPrice = a - discountVal
                    result = formatDouble(finalPrice)
                    label = "Final Price"
                    secondaryResult = "Saved: " + formatDouble(discountVal)
                }
                PercentageToolType.GST_TAX -> {
                    // price a, gst b%
                    if (_state.value.isGstInclusive) {
                        // Inclusive of Tax
                        val originalPrice = a / (1.0 + (b / 100.0))
                        val gstAmount = a - originalPrice
                        result = formatDouble(originalPrice)
                        label = "Original Price (Excl. Tax)"
                        secondaryResult = "Tax Amount: " + formatDouble(gstAmount)
                    } else {
                        // Exclusive of Tax
                        val gstAmount = a * (b / 100.0)
                        val totalPrice = a + gstAmount
                        result = formatDouble(totalPrice)
                        label = "Total Price (Incl. Tax)"
                        secondaryResult = "Tax Amount: " + formatDouble(gstAmount)
                    }
                }
                PercentageToolType.MARKS -> {
                    // obtained a, total b
                    if (b == 0.0) {
                        error = "Total marks cannot be zero"
                    } else if (a > b) {
                        error = "Obtained marks cannot exceed total marks"
                    } else {
                        val value = (a / b) * 100.0
                        result = formatDouble(value) + "%"
                        label = "$a out of $b marks"
                    }
                }
            }

            _state.update {
                it.copy(
                    percentageResult = result,
                    percentageResultLabel = label,
                    percentageSecondaryResult = secondaryResult,
                    percentageError = error
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    percentageResult = "",
                    percentageResultLabel = "",
                    percentageSecondaryResult = "",
                    percentageError = "Calculation error"
                )
            }
        }
    }

    private fun calculateCgpa() {
        val sList = _state.value.semesters
        if (sList.isEmpty()) {
            _state.update {
                it.copy(
                    calculatedCgpa = 0.0,
                    calculatedPercentage = 0.0,
                    totalCredits = 0.0
                )
            }
            return
        }

        var totalCredits = 0.0
        var weightedGpaSum = 0.0
        var simpleGpaSum = 0.0

        for (s in sList) {
            totalCredits += s.credits
            weightedGpaSum += s.gpa * s.credits
            simpleGpaSum += s.gpa
        }

        val cgpa = if (_state.value.isCreditBased) {
            if (totalCredits > 0.0) weightedGpaSum / totalCredits else 0.0
        } else {
            simpleGpaSum / sList.size
        }

        val multiplier = _state.value.cgpaFormulaMultiplier.toDoubleOrNull() ?: 9.5
        val percentage = cgpa * multiplier

        _state.update {
            it.copy(
                calculatedCgpa = cgpa,
                calculatedPercentage = percentage,
                totalCredits = totalCredits
            )
        }
    }

    private fun saveSemesters(semesters: List<Semester>) {
        val serialized = semesters.joinToString(";") { "${it.id}|${it.name}|${it.gpa}|${it.credits}" }
        prefs.edit().putString("saved_semesters", serialized).apply()
    }

    private fun loadSemesters(): List<Semester> {
        val serialized = prefs.getString("saved_semesters", "") ?: ""
        if (serialized.isEmpty()) return emptyList()
        return serialized.split(";").mapNotNull { part ->
            val subParts = part.split("|")
            if (subParts.size == 4) {
                Semester(
                    id = subParts[0],
                    name = subParts[1],
                    gpa = subParts[2].toDoubleOrNull() ?: 0.0,
                    credits = subParts[3].toDoubleOrNull() ?: 0.0
                )
            } else null
        }
    }

    fun savePercentageToHistory() {
        val s = _state.value
        if (s.percentageResult.isNotEmpty() && s.percentageError == null) {
            val expression = "${s.activeTool.displayName} (${s.inputA}, ${s.inputB})"
            val resultText = s.percentageResult + if (s.percentageSecondaryResult.isNotEmpty()) " (${s.percentageSecondaryResult})" else ""
            saveCalculation(expression, resultText)
        }
    }

    fun saveCgpaToHistory() {
        val s = _state.value
        if (s.semesters.isNotEmpty()) {
            val expression = "CGPA (Semesters: ${s.semesters.size})"
            val resultText = "CGPA: ${formatDouble(s.calculatedCgpa, 2)} (${formatDouble(s.calculatedPercentage, 2)}%)"
            saveCalculation(expression, resultText)
        }
    }

    private fun saveCalculation(expression: String, result: String) {
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

    private fun formatDouble(value: Double, decimals: Int = 4): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val bd = BigDecimal(value.toString())
            .setScale(decimals, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        return bd.toPlainString()
    }
}
