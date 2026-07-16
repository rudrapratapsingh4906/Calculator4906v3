package com.example.domain.usecase

import com.example.domain.model.RegressionResult
import com.example.domain.model.StatisticsSummary
import kotlin.math.sqrt

class StatisticsUseCase {

    fun count(data: List<Double>): Int {
        return data.filter { it.isFinite() }.size
    }

    fun sum(data: List<Double>): Double? {
        val cleanData = data.filter { it.isFinite() }
        if (cleanData.isEmpty()) return null
        return cleanData.sum()
    }

    fun mean(data: List<Double>): Double? {
        val cleanData = data.filter { it.isFinite() }
        if (cleanData.isEmpty()) return null
        return cleanData.sum() / cleanData.size
    }

    fun median(data: List<Double>): Double? {
        val cleanData = data.filter { it.isFinite() }.sorted()
        if (cleanData.isEmpty()) return null
        val size = cleanData.size
        return if (size % 2 == 1) {
            cleanData[size / 2]
        } else {
            (cleanData[size / 2 - 1] + cleanData[size / 2]) / 2.0
        }
    }

    fun mode(data: List<Double>): List<Double> {
        val cleanData = data.filter { it.isFinite() }
        if (cleanData.isEmpty()) return emptyList()
        
        val frequencies = cleanData.groupBy { it }.mapValues { it.value.size }
        val maxFrequency = frequencies.values.maxOrNull() ?: return emptyList()
        
        return frequencies.filterValues { it == maxFrequency }.keys.toList().sorted()
    }

    fun minimum(data: List<Double>): Double? {
        val cleanData = data.filter { it.isFinite() }
        return cleanData.minOrNull()
    }

    fun maximum(data: List<Double>): Double? {
        val cleanData = data.filter { it.isFinite() }
        return cleanData.maxOrNull()
    }

    fun range(data: List<Double>): Double? {
        val cleanData = data.filter { it.isFinite() }
        if (cleanData.isEmpty()) return null
        val min = cleanData.minOrNull() ?: return null
        val max = cleanData.maxOrNull() ?: return null
        return max - min
    }

    fun populationVariance(data: List<Double>): Double? {
        val cleanData = data.filter { it.isFinite() }
        if (cleanData.isEmpty()) return null
        val m = mean(cleanData) ?: return null
        val sumOfSquares = cleanData.sumOf { (it - m) * (it - m) }
        return sumOfSquares / cleanData.size
    }

    fun sampleVariance(data: List<Double>): Double? {
        val cleanData = data.filter { it.isFinite() }
        if (cleanData.size <= 1) return null
        val m = mean(cleanData) ?: return null
        val sumOfSquares = cleanData.sumOf { (it - m) * (it - m) }
        return sumOfSquares / (cleanData.size - 1)
    }

    fun populationStdDev(data: List<Double>): Double? {
        val variance = populationVariance(data) ?: return null
        if (variance < 0.0) return null
        return sqrt(variance)
    }

    fun sampleStdDev(data: List<Double>): Double? {
        val variance = sampleVariance(data) ?: return null
        if (variance < 0.0) return null
        return sqrt(variance)
    }

    fun getSummary(data: List<Double>): StatisticsSummary {
        val cleanData = data.filter { it.isFinite() }
        return StatisticsSummary(
            count = count(cleanData),
            sum = sum(cleanData),
            mean = mean(cleanData),
            median = median(cleanData),
            mode = mode(cleanData),
            minimum = minimum(cleanData),
            maximum = maximum(cleanData),
            range = range(cleanData),
            populationVariance = populationVariance(cleanData),
            sampleVariance = sampleVariance(cleanData),
            populationStdDev = populationStdDev(cleanData),
            sampleStdDev = sampleStdDev(cleanData)
        )
    }

    fun linearRegression(x: List<Double>, y: List<Double>): RegressionResult? {
        val cleanX = x.map { if (it.isFinite()) it else Double.NaN }
        val cleanY = y.map { if (it.isFinite()) it else Double.NaN }
        
        val pairs = cleanX.zip(cleanY).filter { it.first.isFinite() && it.second.isFinite() }
        if (pairs.size < 2) return null
        
        val n = pairs.size.toDouble()
        val sumX = pairs.sumOf { it.first }
        val sumY = pairs.sumOf { it.second }
        val meanX = sumX / n
        val meanY = sumY / n
        
        val varX = pairs.sumOf { (it.first - meanX) * (it.first - meanX) }
        val varY = pairs.sumOf { (it.second - meanY) * (it.second - meanY) }
        val covXY = pairs.sumOf { (it.first - meanX) * (it.second - meanY) }
        
        if (varX == 0.0) {
            return null
        }
        
        val slope = covXY / varX
        val intercept = meanY - slope * meanX
        
        val stdDevX = sqrt(varX)
        val stdDevY = sqrt(varY)
        
        val pearsonR = if (stdDevX * stdDevY == 0.0) {
            0.0
        } else {
            covXY / (stdDevX * stdDevY)
        }
        
        return RegressionResult(
            slope = slope,
            intercept = intercept,
            pearsonR = pearsonR
        )
    }
}
