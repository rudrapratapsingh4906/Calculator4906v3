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
        val ast = calculatorEngine.parse(expression)
        val vars = HashMap<String, Double>()

        fun evalAst(v: Double): Double? {
            vars["x"] = v
            return try {
                val res = ast.eval(vars, false)
                if (res.isNaN() || res.isInfinite()) null else res
            } catch (e: Exception) {
                null
            }
        }

        val step = (maxX - minX) / steps
        val h = step / 10.0 // neighborhood offset

        for (i in 1 until steps) {
            val x = minX + i * step
            val y = evalAst(x) ?: continue
            val yLeft = evalAst(x - h) ?: continue
            val yRight = evalAst(x + h) ?: continue

            if (y > yLeft && y > yRight) {
                // Local Maximum identified, refine using ternary search
                val refinedX = refineExtremum(::evalAst, x - step, x + step, isMaximum = true)
                val refinedY = evalAst(refinedX)
                if (refinedY != null && extrema.none { Math.abs(it.x - refinedX) < 1e-4 }) {
                    extrema.add(ExtremerPoint(refinedX, refinedY, isMaximum = true))
                }
            } else if (y < yLeft && y < yRight) {
                // Local Minimum identified, refine using ternary search
                val refinedX = refineExtremum(::evalAst, x - step, x + step, isMaximum = false)
                val refinedY = evalAst(refinedX)
                if (refinedY != null && extrema.none { Math.abs(it.x - refinedX) < 1e-4 }) {
                    extrema.add(ExtremerPoint(refinedX, refinedY, isMaximum = false))
                }
            }
        }
        return extrema.sortedBy { it.x }
    }

    private inline fun refineExtremum(evalAst: (Double) -> Double?, xMin: Double, xMax: Double, isMaximum: Boolean, maxIterations: Int = 12): Double {
        var low = xMin
        var high = xMax
        for (i in 0 until maxIterations) {
            val m1 = low + (high - low) / 3.0
            val m2 = high - (high - low) / 3.0
            val f1 = evalAst(m1) ?: return (low + high) / 2.0
            val f2 = evalAst(m2) ?: return (low + high) / 2.0

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
