package com.example.domain.scanner

interface MathSolverRepository {
    fun solve(expression: String): String
}
