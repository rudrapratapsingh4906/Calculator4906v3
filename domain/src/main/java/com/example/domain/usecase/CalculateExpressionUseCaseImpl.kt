package com.example.domain.usecase

import com.example.core.util.AppError
import com.example.core.util.Result
import com.example.domain.math.CalculatorEngine
import com.example.domain.model.Calculation
import com.example.domain.repository.CalculationRepository
import java.util.UUID

class CalculateExpressionUseCaseImpl(
    private val calculatorEngine: CalculatorEngine,
    private val repository: CalculationRepository
) : CalculateExpressionUseCase {
    override suspend fun invoke(expression: String, isDegreeMode: Boolean): Result<Calculation, AppError.Calculation> {
        return when (val result = calculatorEngine.evaluate(expression, isDegreeMode)) {
            is Result.Success -> {
                val formattedResult = formatResult(result.data)
                val calculation = Calculation(
                    id = UUID.randomUUID().toString(),
                    expression = expression,
                    result = formattedResult,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveCalculation(calculation)
                Result.Success(calculation)
            }
            is Result.Error -> result
        }
    }
    
    override suspend fun evaluateOnly(expression: String, isDegreeMode: Boolean): Result<String, AppError.Calculation> {
        return when (val result = calculatorEngine.evaluate(expression, isDegreeMode)) {
            is Result.Success -> Result.Success(formatResult(result.data))
            is Result.Error -> result
        }
    }
    
    private fun formatResult(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}
