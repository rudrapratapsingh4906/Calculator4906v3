package com.example.domain.usecase

import com.example.core.util.AppError
import com.example.core.util.Result
import com.example.domain.repository.CalculationRepository

class ClearHistoryUseCase(
    private val repository: CalculationRepository
) {
    suspend operator fun invoke(): Result<Unit, AppError.Storage> {
        return repository.clearHistory()
    }
}
