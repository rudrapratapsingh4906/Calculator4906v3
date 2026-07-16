package com.example.domain.scanner

class SolveUseCase(
    private val repository: MathSolverRepository
) {
    fun execute(expression: String): String {
        return repository.solve(expression)
    }
}
