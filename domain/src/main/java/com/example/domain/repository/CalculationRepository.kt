package com.example.domain.repository

import com.example.core.util.AppError
import com.example.core.util.Result
import com.example.domain.model.Calculation
import kotlinx.coroutines.flow.Flow

interface CalculationRepository {
    fun getCalculationHistory(): Flow<List<Calculation>>
    suspend fun saveCalculation(calculation: Calculation): Result<Unit, AppError.Storage>
    suspend fun clearHistory(): Result<Unit, AppError.Storage>
}
