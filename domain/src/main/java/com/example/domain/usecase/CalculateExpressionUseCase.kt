package com.example.domain.usecase

import com.example.core.util.AppError
import com.example.core.util.Result
import com.example.domain.model.Calculation

interface CalculateExpressionUseCase {
    suspend operator fun invoke(expression: String, isDegreeMode: Boolean = true): Result<Calculation, AppError.Calculation>
    suspend fun evaluateOnly(expression: String, isDegreeMode: Boolean = true): Result<String, AppError.Calculation>
}
