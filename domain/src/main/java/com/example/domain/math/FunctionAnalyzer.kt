package com.example.domain.math

import com.example.core.util.Result
import com.example.domain.model.ExtremerPoint

class FunctionAnalyzer(private val calculatorEngine: CalculatorEngine) {
    /**
     * Finds local maxima and minima for a given expression in the range [minX, maxX].
     */
    fun findExtrema(expression: String, minX: Double, maxX: Double, steps: Int = 150): List<ExtremerPoint> {
        val extrema = mutableListOf<ExtremerPoint>()
        if (minX >= maxX) return extrema
        val step = (maxX - minX) / steps
        val h = step / 10.0 // neighborhood offset

        for (i in 1 until steps) {
            val x = minX + i * step
            val y = evaluateAt(expression, x) ?: continue
            val yLeft = evaluateAt(expression, x - h) ?: continue
            val yRight = evaluateAt(expression, x + h) ?: continue

            if (y > yLeft && y > yRight) {
                // Local Maximum identified, refine using ternary search
                val refinedX = refineExtremum(expression, x - step, x + step, isMaximum = true)
                val refinedY = evaluateAt(expression, refinedX)
                if (refinedY != null && extrema.none { Math.abs(it.x - refinedX) < 1e-4 }) {
                    extrema.add(ExtremerPoint(refinedX, refinedY, isMaximum = true))
                }
            } else if (y < yLeft && y < yRight) {
                // Local Minimum identified, refine using ternary search
                val refinedX = refineExtremum(expression, x - step, x + step, isMaximum = false)
                val refinedY = evaluateAt(expression, refinedX)
                if (refinedY != null && extrema.none { Math.abs(it.x - refinedX) < 1e-4 }) {
                    extrema.add(ExtremerPoint(refinedX, refinedY, isMaximum = false))
                }
            }
        }
        return extrema.sortedBy { it.x }
    }

    private fun evaluateAt(expression: String, x: Double): Double? {
        val result = calculatorEngine.evaluate(expression, false, mapOf("x" to x))
        return if (result is Result.Success) {
            val y = result.data
            if (y.isNaN() || y.isInfinite()) null else y
        } else null
    }

    private fun refineExtremum(expression: String, xMin: Double, xMax: Double, isMaximum: Boolean, maxIterations: Int = 12): Double {
        var low = xMin
        var high = xMax
        for (i in 0 until maxIterations) {
            val m1 = low + (high - low) / 3.0
            val m2 = high - (high - low) / 3.0
            val f1 = evaluateAt(expression, m1) ?: return (low + high) / 2.0
            val f2 = evaluateAt(expression, m2) ?: return (low + high) / 2.0

            if (isMaximum) {
                if (f1 < f2) {
                    low = m1
                } else {
                    high = m2
                }
            } else {
                if (f1 > f2) {
                    low = m1
                } else {
                    high = m2
                }
            }
        }
        return (low + high) / 2.0
    }
}
