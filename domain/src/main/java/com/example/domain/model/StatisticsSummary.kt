package com.example.domain.model

data class StatisticsSummary(
    val count: Int,
    val sum: Double?,
    val mean: Double?,
    val median: Double?,
    val mode: List<Double>,
    val minimum: Double?,
    val maximum: Double?,
    val range: Double?,
    val populationVariance: Double?,
    val sampleVariance: Double?,
    val populationStdDev: Double?,
    val sampleStdDev: Double?
)
