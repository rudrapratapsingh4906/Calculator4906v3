package com.example.domain.model

data class RegressionResult(
    val slope: Double,
    val intercept: Double,
    val pearsonR: Double
)
