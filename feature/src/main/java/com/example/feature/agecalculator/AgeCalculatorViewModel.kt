package com.example.feature.agecalculator

import androidx.lifecycle.ViewModel
import com.example.domain.usecase.CalculateAgeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AgeCalculatorViewModel(
    private val calculateAgeUseCase: CalculateAgeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AgeCalculatorState())
    val state: StateFlow<AgeCalculatorState> = _state.asStateFlow()

    fun onEvent(event: AgeCalculatorEvent) {
        when (event) {
            is AgeCalculatorEvent.SetDob -> {
                _state.update { it.copy(dob = event.date, error = null) }
                calculate()
            }
            is AgeCalculatorEvent.SetCurrentDate -> {
                _state.update { it.copy(currentDate = event.date, error = null) }
                calculate()
            }
            AgeCalculatorEvent.Calculate -> {
                calculate()
            }
            AgeCalculatorEvent.Clear -> {
                _state.update { it.copy(dob = null, result = null, error = null) }
            }
        }
    }

    private fun calculate() {
        val currentState = _state.value
        val dob = currentState.dob
        val current = currentState.currentDate

        if (dob == null) {
            _state.update { it.copy(result = null) }
            return
        }

        val result = calculateAgeUseCase(dob, current)
        if (result == null) {
            _state.update { it.copy(error = "Date of birth cannot be after current date", result = null) }
        } else {
            _state.update { it.copy(result = result, error = null) }
        }
    }
}
