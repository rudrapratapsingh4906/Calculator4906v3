package com.example.domain.usecase

import com.example.domain.model.Equation

class SolveEquationUseCase {
    fun solve(equation: Equation): List<String> {
        // Simple logic for demonstration. Real implementation would involve complex math solvers.
        return when (equation) {
            is Equation.Linear -> listOf("Step 1: Simplify", "Step 2: Solve", "Result: x = ${(equation.c - equation.b) / equation.a}")
            is Equation.Quadratic -> listOf("Step 1: Use quadratic formula", "Result: x = ...")
        }
    }
}
