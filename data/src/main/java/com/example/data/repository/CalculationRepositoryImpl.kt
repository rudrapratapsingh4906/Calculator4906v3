package com.example.data.repository

import com.example.core.util.AppError
import com.example.core.util.DispatcherProvider
import com.example.core.util.Result
import com.example.data.local.dao.CalculationDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.Calculation
import com.example.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CalculationRepositoryImpl(
    private val dao: CalculationDao,
    private val dispatcherProvider: DispatcherProvider
) : CalculationRepository {

    override fun getCalculationHistory(): Flow<List<Calculation>> {
        return dao.getCalculationHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCalculation(calculation: Calculation): Result<Unit, AppError.Storage> {
        return withContext(dispatcherProvider.io) {
            try {
                dao.insertCalculation(calculation.toEntity())
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(AppError.Storage.Unknown(e.message ?: "Unknown database error"))
            }
        }
    }

    override suspend fun clearHistory(): Result<Unit, AppError.Storage> {
        return withContext(dispatcherProvider.io) {
            try {
                dao.clearHistory()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(AppError.Storage.Unknown(e.message ?: "Unknown database error"))
            }
        }
    }
}
