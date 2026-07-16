package com.example.feature.advancedfeatures.ui

import androidx.lifecycle.ViewModel
import com.example.domain.model.ComplexNumber
import com.example.domain.usecase.ComplexUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class ComplexState(
    val real1: String = "",
    val imaginary1: String = "",
    val real2: String = "",
    val imaginary2: String = "",
    val result: String = "",
    val error: String? = null
)

class ComplexViewModel(private val complexUseCase: ComplexUseCase) : ViewModel() {
    private val _state = MutableStateFlow(ComplexState())
    val state: StateFlow<ComplexState> = _state

    fun onReal1Change(value: String) {
        _state.update { it.copy(real1 = value, error = null) }
    }

    fun onImaginary1Change(value: String) {
        _state.update { it.copy(imaginary1 = value, error = null) }
    }

    fun onReal2Change(value: String) {
        _state.update { it.copy(real2 = value, error = null) }
    }

    fun onImaginary2Change(value: String) {
        _state.update { it.copy(imaginary2 = value, error = null) }
    }

    private fun parseInput(value: String): Double? {
        if (value.isEmpty()) return 0.0
        return value.toDoubleOrNull()
    }

    private fun validateAndGetInputs(needsTwo: Boolean = true): Pair<ComplexNumber?, ComplexNumber?>? {
        val r1 = parseInput(_state.value.real1)
        val i1 = parseInput(_state.value.imaginary1)
        
        if (r1 == null || i1 == null) {
            _state.update { it.copy(error = "Invalid input for first number") }
            return null
        }
        
        val c1 = ComplexNumber(r1, i1)
        
        if (!needsTwo) return c1 to null
        
        val r2 = parseInput(_state.value.real2)
        val i2 = parseInput(_state.value.imaginary2)
        
        if (r2 == null || i2 == null) {
            _state.update { it.copy(error = "Invalid input for second number") }
            return null
        }
        
        return c1 to ComplexNumber(r2, i2)
    }

    fun add() {
        val inputs = validateAndGetInputs(true) ?: return
        val res = complexUseCase.add(inputs.first!!, inputs.second!!)
        _state.update { it.copy(result = res.toString()) }
    }

    fun subtract() {
        val inputs = validateAndGetInputs(true) ?: return
        val res = complexUseCase.subtract(inputs.first!!, inputs.second!!)
        _state.update { it.copy(result = res.toString()) }
    }

    fun multiply() {
        val inputs = validateAndGetInputs(true) ?: return
        val res = complexUseCase.multiply(inputs.first!!, inputs.second!!)
        _state.update { it.copy(result = res.toString()) }
    }

    fun divide() {
        val inputs = validateAndGetInputs(true) ?: return
        val res = complexUseCase.divide(inputs.first!!, inputs.second!!)
        if (res != null) {
            _state.update { it.copy(result = res.toString()) }
        } else {
            _state.update { it.copy(error = "Division by zero") }
        }
    }

    fun magnitude() {
        val inputs = validateAndGetInputs(false) ?: return
        val res = complexUseCase.magnitude(inputs.first!!)
        _state.update { it.copy(result = String.format("%.4f", res)) }
    }

    fun argument() {
        val inputs = validateAndGetInputs(false) ?: return
        val res = complexUseCase.argument(inputs.first!!)
        _state.update { it.copy(result = String.format("%.4f rad", res)) }
    }

    fun conjugate() {
        val inputs = validateAndGetInputs(false) ?: return
        val res = complexUseCase.conjugate(inputs.first!!)
        _state.update { it.copy(result = res.toString()) }
    }

    fun reciprocal() {
        val inputs = validateAndGetInputs(false) ?: return
        val res = complexUseCase.reciprocal(inputs.first!!)
        if (res != null) {
            _state.update { it.copy(result = res.toString()) }
        } else {
            _state.update { it.copy(error = "Reciprocal of zero undefined") }
        }
    }

    fun toPolar() {
        val inputs = validateAndGetInputs(false) ?: return
        val (r, theta) = complexUseCase.toPolar(inputs.first!!)
        _state.update { it.copy(result = String.format("r=%.4f, θ=%.4f rad", r, theta)) }
    }

    fun fromPolar() {
        val r = parseInput(_state.value.real1)
        val theta = parseInput(_state.value.imaginary1)
        
        if (r == null || theta == null) {
            _state.update { it.copy(error = "Invalid input (r=Real, θ=Imaginary)") }
            return
        }
        
        val res = complexUseCase.fromPolar(r, theta)
        _state.update { it.copy(result = res.toString()) }
    }

    fun clear() {
        _state.update { ComplexState() }
    }
}
