package com.example.domain.math

import com.example.core.util.Result

class RootFinder(private val calculatorEngine: CalculatorEngine) {
    /**
     * Finds approximate roots (x-axis intersections) for a given expression in the range [minX, maxX].
     */
    fun findRoots(expression: String, minX: Double, maxX: Double, steps: Int = 150): List<Double> {
        val roots = mutableListOf<Double>()
        if (minX >= maxX) return roots
        val step = (maxX - minX) / steps
        var prevX = minX
        var prevY = evaluateAt(expression, prevX)

        for (i in 1..steps) {
            val currX = minX + i * step
            val currY = evaluateAt(expression, currX)

            if (prevY != null && currY != null) {
                if (Math.abs(prevY) < 1e-9) {
                    if (roots.none { Math.abs(it - prevX) < 1e-4 }) {
                        roots.add(prevX)
                    }
                } else if (Math.abs(currY) < 1e-9) {
                    if (roots.none { Math.abs(it - currX) < 1e-4 }) {
                        roots.add(currX)
                    }
                } else if (prevY * currY < 0.0) {
                    // Sign change found, use bisection to pinpoint precise root
                    val root = findRootBisection(expression, prevX, currX)
                    if (root != null && roots.none { Math.abs(it - root) < 1e-4 }) {
                        roots.add(root)
                    }
                }
            }
            prevX = currX
            prevY = currY
        }
        return roots.sorted()
    }

    private fun evaluateAt(expression: String, x: Double): Double? {
        val result = calculatorEngine.evaluate(expression, false, mapOf("x" to x))
        return if (result is Result.Success) {
            val y = result.data
            if (y.isNaN() || y.isInfinite()) null else y
        } else null
    }

    private fun findRootBisection(expression: String, x1: Double, x2: Double, maxIterations: Int = 12): Double? {
        var low = x1
        var high = x2
        var fLow = evaluateAt(expression, low) ?: return null
        val fHigh = evaluateAt(expression, high) ?: return null

        if (fLow * fHigh > 0.0) return null

        var mid = (low + high) / 2.0
        for (i in 0 until maxIterations) {
            mid = (low + high) / 2.0
            val fMid = evaluateAt(expression, mid) ?: return mid
            if (Math.abs(fMid) < 1e-6) {
                return mid
            }
            if (fLow * fMid < 0.0) {
                high = mid
            } else {
                low = mid
                fLow = fMid
            }
        }
        return mid
    }
}
