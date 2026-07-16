package com.example.feature.advancedfeatures.ui

import androidx.lifecycle.ViewModel
import com.example.domain.usecase.CalculusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class CalculusState(
    val expression: String = "x^2",
    val xValue: String = "1",
    val lowerBound: String = "0",
    val upperBound: String = "1",
    val derivativeResult: String? = null,
    val integralResult: String? = null,
    val limitResult: String? = null,
    val tangentLineResult: String? = null,
    val error: String? = null
)

class CalculusViewModel(private val calculusUseCase: CalculusUseCase) : ViewModel() {
    private val _state = MutableStateFlow(CalculusState())
    val state: StateFlow<CalculusState> = _state

    fun onExpressionChange(newExpr: String) {
        _state.update { it.copy(expression = newExpr) }
    }

    fun onXValueChange(newVal: String) {
        _state.update { it.copy(xValue = newVal) }
    }

    fun onLowerBoundChange(newVal: String) {
        _state.update { it.copy(lowerBound = newVal) }
    }

    fun onUpperBoundChange(newVal: String) {
        _state.update { it.copy(upperBound = newVal) }
    }

    fun calculateDerivative() {
        val x = _state.value.xValue.toDoubleOrNull()
        if (x == null) {
            _state.update { it.copy(error = "Invalid x value") }
            return
        }
        val result = calculusUseCase.calculateDerivative(_state.value.expression, x)
        val tangent = calculusUseCase.getTangentLine(_state.value.expression, x)
        if (result != null) {
            _state.update { it.copy(derivativeResult = String.format("%.6f", result), tangentLineResult = tangent, error = null) }
        } else {
            _state.update { it.copy(error = "Calculation failed") }
        }
    }

    fun calculateIntegral() {
        val a = _state.value.lowerBound.toDoubleOrNull()
        val b = _state.value.upperBound.toDoubleOrNull()
        if (a == null || b == null) {
            _state.update { it.copy(error = "Invalid bounds") }
            return
        }
        val result = calculusUseCase.calculateIntegration(_state.value.expression, a, b)
        if (result != null) {
            _state.update { it.copy(integralResult = String.format("%.6f", result), error = null) }
        } else {
            _state.update { it.copy(error = "Calculation failed") }
        }
    }

    fun calculateLimit() {
        val x0 = _state.value.xValue.toDoubleOrNull()
        if (x0 == null) {
            _state.update { it.copy(error = "Invalid limit point") }
            return
        }
        val result = calculusUseCase.calculateLimit(_state.value.expression, x0)
        if (result != null) {
            _state.update { it.copy(limitResult = String.format("%.6f", result), error = null) }
        } else {
            _state.update { it.copy(error = "Calculation failed") }
        }
    }

    fun clear() {
        _state.update { CalculusState() }
    }
}
