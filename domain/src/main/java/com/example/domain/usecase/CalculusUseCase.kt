package com.example.domain.usecase

import com.example.domain.math.CalculatorEngine
import com.example.core.util.Result

class CalculusUseCase(private val engine: CalculatorEngine) {

    fun evaluateAt(expression: String, x: Double): Double? {
        val result = engine.evaluate(expression, false, mapOf("x" to x))
        return if (result is Result.Success) result.data else null
    }

    fun calculateDerivative(expression: String, x: Double): Double? {
        val h = 1e-7
        val fxh1 = evaluateAt(expression, x + h) ?: return null
        val fxh2 = evaluateAt(expression, x - h) ?: return null
        return (fxh1 - fxh2) / (2 * h)
    }

    fun calculateIntegration(expression: String, a: Double, b: Double, steps: Int = 1000): Double? {
        // Simpson's rule
        val h = (b - a) / steps
        var sum = (evaluateAt(expression, a) ?: return null) + (evaluateAt(expression, b) ?: return null)

        for (i in 1 until steps) {
            val x = a + i * h
            val fx = evaluateAt(expression, x) ?: return null
            sum += if (i % 2 == 0) 2 * fx else 4 * fx
        }

        return (h / 3) * sum
    }

    fun calculateLimit(expression: String, x0: Double): Double? {
        val h = 1e-9
        val left = evaluateAt(expression, x0 - h) ?: return null
        val right = evaluateAt(expression, x0 + h) ?: return null
        return (left + right) / 2.0
    }

    fun getTangentLine(expression: String, a: Double): String? {
        val fa = evaluateAt(expression, a) ?: return null
        val fprimea = calculateDerivative(expression, a) ?: return null
        // y = f'(a)(x-a) + f(a)
        // y = f'(a)x - f'(a)a + f(a)
        val intercept = -fprimea * a + fa
        return String.format("y = %.4fx + %.4f", fprimea, intercept)
    }
}
