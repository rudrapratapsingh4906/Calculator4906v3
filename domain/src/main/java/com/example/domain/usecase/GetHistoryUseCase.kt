package com.example.domain.usecase

import com.example.domain.model.Calculation
import com.example.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow

class GetHistoryUseCase(
    private val repository: CalculationRepository
) {
    operator fun invoke(): Flow<List<Calculation>> {
        return repository.getCalculationHistory()
    }
}
