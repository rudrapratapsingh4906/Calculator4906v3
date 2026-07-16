package com.example.domain.math

import com.example.core.util.AppError
import com.example.core.util.Result
import kotlin.math.*

class CalculatorEngine {
    fun evaluate(
        expression: String,
        isDegreeMode: Boolean,
        variables: Map<String, Double> = emptyMap()
    ): Result<Double, AppError.Calculation> {
        return try {
            val result = eval(expression, isDegreeMode, variables)
            if (result.isNaN() || result.isInfinite()) {
                Result.Error(AppError.Calculation.InvalidExpression)
            } else {
                Result.Success(result)
            }
        } catch (e: ArithmeticException) {
            Result.Error(AppError.Calculation.DivideByZero)
        } catch (e: Exception) {
            Result.Error(AppError.Calculation.InvalidExpression)
        }
    }

    private fun eval(str: String, isDegreeMode: Boolean, variables: Map<String, Double> = emptyMap()): Double {
        val expr = str.replace(" ", "").lowercase()
            .replace("−", "-")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("³√", "cbrt")
            .replace("√", "sqrt")
            .replace("pi", "π")
            .replace("mod", "#")
            .replace("npr", "p")
            .replace("ncr", "c")

        return object : Any() {
            var pos = -1
            var ch = 0
            
            fun nextChar() {
                ch = if (++pos < expr.length) expr[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expr.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() 
                    else if (eat('-'.code)) x -= parseTerm() 
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code) || eat('×'.code)) x *= parseFactor() 
                    else if (eat('/'.code) || eat('÷'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Divide by zero")
                        x /= divisor
                    }
                    else if (eat('#'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Divide by zero")
                        x %= divisor
                    }
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() 
                if (eat('-'.code)) return -parseFactor() 
                
                var x = 0.0
                val startPos = this.pos
                if (eat('('.code)) { 
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { 
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    if (ch == 'e'.code || ch == 'E'.code) {
                        nextChar()
                        if (ch == '+'.code || ch == '-'.code) nextChar()
                        while (ch >= '0'.code && ch <= '9'.code) nextChar()
                    }
                    x = expr.substring(startPos, this.pos).toDouble()
                } else if (ch == 'π'.code) {
                    nextChar()
                    x = Math.PI
                } else if (ch == 'e'.code) {
                    // Check if it's the start of "exp"
                    if (this.pos + 1 < expr.length && expr[this.pos + 1].isLetter()) {
                        while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                        val func = expr.substring(startPos, this.pos)
                        val arg = parseFactor()
                        x = when (func) {
                            "exp" -> exp(arg)
                            else -> throw RuntimeException("Unknown function: $func")
                        }
                    } else {
                        nextChar()
                        x = Math.E
                    }
                } else if (ch == 'r'.code && expr.substring(startPos).startsWith("rand")) {
                    nextChar(); nextChar(); nextChar(); nextChar() // eat "rand"
                    x = Math.random()
                } else if (ch >= 'a'.code && ch <= 'z'.code) { 
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = expr.substring(startPos, this.pos)
                    if (variables.containsKey(func)) {
                        x = variables[func] ?: 0.0
                    } else {
                        val arg = parseFactor()
                        val argRad = if (isDegreeMode) Math.toRadians(arg) else arg
                        x = when (func) {
                            "sqrt", "√" -> sqrt(arg)
                            "cbrt" -> cbrt(arg)
                            "sin" -> sin(argRad)
                            "cos" -> cos(argRad)
                            "tan" -> tan(argRad)
                            "sinh" -> sinh(arg)
                            "cosh" -> cosh(arg)
                            "tanh" -> tanh(arg)
                            "asin" -> if (isDegreeMode) Math.toDegrees(asin(arg)) else asin(arg)
                            "acos" -> if (isDegreeMode) Math.toDegrees(acos(arg)) else acos(arg)
                            "atan" -> if (isDegreeMode) Math.toDegrees(atan(arg)) else atan(arg)
                            "log" -> log10(arg)
                            "ln" -> ln(arg)
                            "abs" -> abs(arg)
                            "floor" -> floor(arg)
                            "ceil" -> ceil(arg)
                            "exp" -> exp(arg)
                            else -> throw RuntimeException("Unknown function: $func")
                        }
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                
                if (eat('^'.code)) x = x.pow(parseFactor()) 
                
                while (eat('!'.code)) {
                    x = factorial(x)
                }
                
                while (eat('%'.code)) {
                    x /= 100.0
                }

                while (true) {
                    if (eat('p'.code)) {
                        val r = parseFactor()
                        x = permutation(x, r)
                    } else if (eat('c'.code)) {
                        val r = parseFactor()
                        x = combination(x, r)
                    } else {
                        break
                    }
                }
                
                return x
            }
            
            fun factorial(n: Double): Double {
                if (n < 0 || n != floor(n)) throw RuntimeException("Factorial only defined for non-negative integers")
                if (n > 170.0) throw RuntimeException("Factorial overflow")
                if (n == 0.0) return 1.0
                var res = 1.0
                for (i in 1..n.toInt()) res *= i
                return res
            }

            fun permutation(n: Double, r: Double): Double {
                if (n < 0 || r < 0 || n < r || n != floor(n) || r != floor(r)) {
                    throw RuntimeException("Permutation only defined for non-negative integers n >= r")
                }
                return factorial(n) / factorial(n - r)
            }

            fun combination(n: Double, r: Double): Double {
                if (n < 0 || r < 0 || n < r || n != floor(n) || r != floor(r)) {
                    throw RuntimeException("Combination only defined for non-negative integers n >= r")
                }
                return factorial(n) / (factorial(r) * factorial(n - r))
            }
        }.parse()
    }
}
