package com.example.core.util

sealed interface AppError {
    sealed interface Calculation : AppError {
        data object DivideByZero : Calculation
        data object InvalidExpression : Calculation
        data class Unknown(val message: String) : Calculation
    }
    
    sealed interface Storage : AppError {
        data object DiskFull : Storage
        data class Unknown(val message: String) : Storage
    }
}
