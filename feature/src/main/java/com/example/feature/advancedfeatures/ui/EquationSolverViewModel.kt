package com.example.feature.advancedfeatures.ui

import androidx.lifecycle.ViewModel
import com.example.domain.model.Equation
import com.example.domain.usecase.SolveEquationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class EquationSolverState(
    val expression: String = "",
    val result: String = "",
    val steps: List<String> = emptyList()
)

class EquationSolverViewModel(
    private val solveEquationUseCase: SolveEquationUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(EquationSolverState())
    val state: StateFlow<EquationSolverState> = _state

    fun solve(equation: Equation) {
        val steps = solveEquationUseCase.solve(equation)
        _state.update { it.copy(steps = steps, result = steps.last()) }
    }
}
