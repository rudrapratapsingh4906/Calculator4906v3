package com.example.feature.advancedfeatures.ui

import androidx.lifecycle.ViewModel
import com.example.domain.model.RegressionResult
import com.example.domain.model.StatisticsSummary
import com.example.domain.usecase.StatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class StatisticsState(
    val inputX: String = "",
    val inputY: String = "",
    val summary: StatisticsSummary? = null,
    val regressionResult: RegressionResult? = null,
    val error: String? = null
)

class StatisticsViewModel(private val statisticsUseCase: StatisticsUseCase) : ViewModel() {
    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state

    fun onInputXChange(value: String) {
        _state.update { it.copy(inputX = value, error = null) }
    }

    fun onInputYChange(value: String) {
        _state.update { it.copy(inputY = value, error = null) }
    }

    private fun parseInputList(input: String): List<Double>? {
        if (input.trim().isEmpty()) return emptyList()
        val tokens = input.split(Regex("[,;\\s\\n]+")).filter { it.isNotEmpty() }
        val list = mutableListOf<Double>()
        for (token in tokens) {
            val d = token.toDoubleOrNull()
            if (d == null || !d.isFinite()) {
                return null
            }
            list.add(d)
        }
        return list
    }

    fun calculateStatistics() {
        val data = parseInputList(_state.value.inputX)
        if (data == null) {
            _state.update { 
                it.copy(
                    summary = null,
                    error = "Invalid numbers found. Please enter valid numeric values."
                ) 
            }
            return
        }

        if (data.isEmpty()) {
            _state.update { 
                it.copy(
                    summary = null,
                    error = "Dataset cannot be empty."
                ) 
            }
            return
        }

        try {
            val summary = statisticsUseCase.getSummary(data)
            _state.update { 
                it.copy(
                    summary = summary,
                    regressionResult = null,
                    error = if (data.size == 1) "Note: Sample variance and standard deviation require at least 2 data points." else null
                ) 
            }
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    summary = null,
                    error = "Calculation error: ${e.localizedMessage ?: "Unknown error"}"
                ) 
            }
        }
    }

    fun calculateRegression() {
        val x = parseInputList(_state.value.inputX)
        val y = parseInputList(_state.value.inputY)

        if (x == null || y == null) {
            _state.update { 
                it.copy(
                    regressionResult = null,
                    error = "Invalid numbers found in X or Y dataset."
                ) 
            }
            return
        }

        if (x.isEmpty() || y.isEmpty()) {
            _state.update { 
                it.copy(
                    regressionResult = null,
                    error = "Datasets X and Y cannot be empty."
                ) 
            }
            return
        }

        if (x.size != y.size) {
            _state.update { 
                it.copy(
                    regressionResult = null,
                    error = "X and Y datasets must have the same number of elements (X has ${x.size}, Y has ${y.size})."
                ) 
            }
            return
        }

        if (x.size < 2) {
            _state.update { 
                it.copy(
                    regressionResult = null,
                    error = "Linear regression requires at least 2 data points."
                ) 
            }
            return
        }

        try {
            val result = statisticsUseCase.linearRegression(x, y)
            if (result != null) {
                _state.update { 
                    it.copy(
                        regressionResult = result,
                        summary = null,
                        error = null
                    ) 
                }
            } else {
                _state.update { 
                    it.copy(
                        regressionResult = null,
                        error = "Regression calculation failed. Ensure X values are not all identical (zero variance)."
                    ) 
                }
            }
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    regressionResult = null,
                    error = "Calculation error: ${e.localizedMessage ?: "Unknown error"}"
                ) 
            }
        }
    }

    fun clear() {
        _state.update { StatisticsState() }
    }
}
