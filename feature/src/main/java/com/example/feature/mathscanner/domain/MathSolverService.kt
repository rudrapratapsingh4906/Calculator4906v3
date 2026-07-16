package com.example.feature.mathscanner.domain

interface MathSolverService {
    suspend fun solve(expression: String): String
}
