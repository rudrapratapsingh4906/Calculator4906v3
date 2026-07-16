package com.example.core.util

sealed interface Result<out T, out E : AppError> {
    data class Success<out T>(val data: T) : Result<T, Nothing>
    data class Error<out E : AppError>(val error: E) : Result<Nothing, E>
}
