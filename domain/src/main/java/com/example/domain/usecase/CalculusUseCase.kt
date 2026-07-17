package com.example.domain.usecase

import com.example.domain.math.CalculatorEngine
import com.example.core.util.Result

class CalculusUseCase(private val engine: CalculatorEngine) {

    fun evaluateAt(expression: String, x: Double): Double? {
        val ast = engine.parse(expression)
        return try {
            val res = ast.eval(mapOf("x" to x), false)
            if (res.isNaN() || res.isInfinite()) null else res
        } catch (e: Exception) {
            null
        }
    }

    fun calculateDerivative(expression: String, x: Double): Double? {
        val ast = engine.parse(expression)
        val h = 1e-7
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
        
        val fxh1 = evalAst(x + h) ?: return null
        val fxh2 = evalAst(x - h) ?: return null
        return (fxh1 - fxh2) / (2 * h)
    }

    fun calculateIntegration(expression: String, a: Double, b: Double, steps: Int = 1000): Double? {
        // Simpson's rule
        val ast = engine.parse(expression)
        val h = (b - a) / steps
        val vars = HashMap<String, Double>()
        
        fun evalAst(xVal: Double): Double? {
            vars["x"] = xVal
            return try {
                val res = ast.eval(vars, false)
                if (res.isNaN() || res.isInfinite()) null else res
            } catch (e: Exception) {
                null
            }
        }

        val fa = evalAst(a) ?: return null
        val fb = evalAst(b) ?: return null
        var sum = fa + fb

        for (i in 1 until steps) {
            val x = a + i * h
            val fx = evalAst(x) ?: return null
            sum += if (i % 2 == 0) 2 * fx else 4 * fx
        }

        return (h / 3) * sum
    }

    fun calculateLimit(expression: String, x0: Double): Double? {
        val ast = engine.parse(expression)
        val h = 1e-9
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

        val left = evalAst(x0 - h) ?: return null
        val right = evalAst(x0 + h) ?: return null
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
