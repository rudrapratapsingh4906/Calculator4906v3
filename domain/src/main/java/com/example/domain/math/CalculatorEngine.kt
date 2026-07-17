package com.example.domain.math

import com.example.core.util.AppError
import com.example.core.util.Result
import kotlin.math.*
import java.util.concurrent.ConcurrentHashMap

sealed interface ExprNode {
    fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double
    fun toSymbolic(): String
}

class NumberNode(val value: Double) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = value
    override fun toSymbolic(): String = formatNum(value)
}

class VariableNode(val name: String) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = variables[name] ?: 0.0
    override fun toSymbolic(): String = name
}

class ConstantNode(val value: Double) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = value
    override fun toSymbolic(): String = when (value) {
        Math.PI -> "π"
        Math.E -> "e"
        else -> formatNum(value)
    }
}

class AddNode(val left: ExprNode, val right: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = left.eval(variables, isDegreeMode) + right.eval(variables, isDegreeMode)
    override fun toSymbolic(): String = "(${left.toSymbolic()} + ${right.toSymbolic()})"
}

class SubNode(val left: ExprNode, val right: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = left.eval(variables, isDegreeMode) - right.eval(variables, isDegreeMode)
    override fun toSymbolic(): String = "(${left.toSymbolic()} - ${right.toSymbolic()})"
}

class MulNode(val left: ExprNode, val right: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = left.eval(variables, isDegreeMode) * right.eval(variables, isDegreeMode)
    override fun toSymbolic(): String = "${left.toSymbolic()} * ${right.toSymbolic()}"
}

class DivNode(val left: ExprNode, val right: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double {
        val divisor = right.eval(variables, isDegreeMode)
        if (divisor == 0.0) throw ArithmeticException("Divide by zero")
        return left.eval(variables, isDegreeMode) / divisor
    }
    override fun toSymbolic(): String = "(${left.toSymbolic()}) / (${right.toSymbolic()})"
}

class ModNode(val left: ExprNode, val right: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double {
        val divisor = right.eval(variables, isDegreeMode)
        if (divisor == 0.0) throw ArithmeticException("Divide by zero")
        return left.eval(variables, isDegreeMode) % divisor
    }
    override fun toSymbolic(): String = "${left.toSymbolic()} mod ${right.toSymbolic()}"
}

class PowNode(val base: ExprNode, val exponent: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = base.eval(variables, isDegreeMode).pow(exponent.eval(variables, isDegreeMode))
    override fun toSymbolic(): String = "(${base.toSymbolic()})^(${exponent.toSymbolic()})"
}

class NegNode(val child: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = -child.eval(variables, isDegreeMode)
    override fun toSymbolic(): String = "-(${child.toSymbolic()})"
}

class FactorialNode(val child: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = factorial(child.eval(variables, isDegreeMode))
    override fun toSymbolic(): String = "(${child.toSymbolic()})!"
}

class PercentNode(val child: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = child.eval(variables, isDegreeMode) / 100.0
    override fun toSymbolic(): String = "(${child.toSymbolic()})%"
}

class PermutationNode(val left: ExprNode, val right: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = permutation(left.eval(variables, isDegreeMode), right.eval(variables, isDegreeMode))
    override fun toSymbolic(): String = "${left.toSymbolic()}P${right.toSymbolic()}"
}

class CombinationNode(val left: ExprNode, val right: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = combination(left.eval(variables, isDegreeMode), right.eval(variables, isDegreeMode))
    override fun toSymbolic(): String = "${left.toSymbolic()}C${right.toSymbolic()}"
}

class RandNode : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double = Math.random()
    override fun toSymbolic(): String = "rand()"
}

class FuncNode(val name: String, val child: ExprNode) : ExprNode {
    override fun eval(variables: Map<String, Double>, isDegreeMode: Boolean): Double {
        val arg = child.eval(variables, isDegreeMode)
        val argRad = if (isDegreeMode) Math.toRadians(arg) else arg
        return when (name) {
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
            else -> throw RuntimeException("Unknown function: $name")
        }
    }
    override fun toSymbolic(): String = "$name(${child.toSymbolic()})"
}

private fun formatNum(n: Double): String {
    if (abs(n - n.roundToLong()) < 1e-9) {
        return n.roundToLong().toString()
    }
    return String.format("%.4f", n).trimEnd('0').trimEnd('.')
}

private fun factorial(n: Double): Double {
    if (n < 0 || n != floor(n)) throw RuntimeException("Factorial only defined for non-negative integers")
    if (n > 170.0) throw RuntimeException("Factorial overflow")
    if (n == 0.0) return 1.0
    var res = 1.0
    for (i in 1..n.toInt()) res *= i
    return res
}

private fun permutation(n: Double, r: Double): Double {
    if (n < 0 || r < 0 || n < r || n != floor(n) || r != floor(r)) {
        throw RuntimeException("Permutation only defined for non-negative integers n >= r")
    }
    return factorial(n) / factorial(n - r)
}

private fun combination(n: Double, r: Double): Double {
    if (n < 0 || r < 0 || n < r || n != floor(n) || r != floor(r)) {
        throw RuntimeException("Combination only defined for non-negative integers n >= r")
    }
    return factorial(n) / (factorial(r) * factorial(n - r))
}

class CalculatorEngine {
    private val parseCache = ConcurrentHashMap<String, ExprNode>()

    fun evaluate(
        expression: String,
        isDegreeMode: Boolean,
        variables: Map<String, Double> = emptyMap()
    ): Result<Double, AppError.Calculation> {
        return try {
            val ast = parse(expression)
            val result = ast.eval(variables, isDegreeMode)
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

    fun parse(str: String): ExprNode {
        var expr = str.replace(" ", "").lowercase()
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

        // Support implicit multiplication (e.g., 4x -> 4*x, )( -> )*(, etc.)
        expr = expr.replace(Regex("(\\d+|[πe])([a-zA-Z\\(πe])"), "$1*$2")
        expr = expr.replace(Regex("\\)([a-zA-Z\\(\\dπe])"), ")*$1")
        expr = expr.replace(Regex("([xyt])\\("), "$1*(")

        return parseCache.getOrPut(expr) {
            ASTParser(expr).parse()
        }
    }

    private class ASTParser(val expr: String) {
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

        fun parse(): ExprNode {
            nextChar()
            val x = parseExpression()
            if (pos < expr.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): ExprNode {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x = AddNode(x, parseTerm())
                else if (eat('-'.code)) x = SubNode(x, parseTerm())
                else return x
            }
        }

        fun parseTerm(): ExprNode {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code) || eat('×'.code)) x = MulNode(x, parseFactor())
                else if (eat('/'.code) || eat('÷'.code)) {
                    x = DivNode(x, parseFactor())
                } else if (eat('#'.code)) {
                    x = ModNode(x, parseFactor())
                } else return x
            }
        }

        fun parseFactor(): ExprNode {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return NegNode(parseFactor())

            var x: ExprNode
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
                val num = expr.substring(startPos, this.pos).toDouble()
                x = NumberNode(num)
            } else if (ch == 'π'.code) {
                nextChar()
                x = ConstantNode(Math.PI)
            } else if (ch == 'e'.code) {
                if (this.pos + 1 < expr.length && expr[this.pos + 1].isLetter()) {
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = expr.substring(startPos, this.pos)
                    val arg = parseFactor()
                    x = when (func) {
                        "exp" -> FuncNode("exp", arg)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    nextChar()
                    x = ConstantNode(Math.E)
                }
            } else if (ch == 'r'.code && expr.substring(startPos).startsWith("rand")) {
                nextChar(); nextChar(); nextChar(); nextChar()
                x = RandNode()
            } else if (ch >= 'a'.code && ch <= 'z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = expr.substring(startPos, this.pos)
                val functionsList = setOf(
                    "sqrt", "cbrt", "sin", "cos", "tan", "sinh", "cosh", "tanh",
                    "asin", "acos", "atan", "log", "ln", "abs", "floor", "ceil", "exp"
                )
                if (func in functionsList) {
                    val arg = parseFactor()
                    x = FuncNode(func, arg)
                } else {
                    x = VariableNode(func)
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            if (eat('^'.code)) x = PowNode(x, parseFactor())

            while (eat('!'.code)) {
                x = FactorialNode(x)
            }

            while (eat('%'.code)) {
                x = PercentNode(x)
            }

            while (true) {
                if (eat('p'.code)) {
                    val r = parseFactor()
                    x = PermutationNode(x, r)
                } else if (eat('c'.code)) {
                    val r = parseFactor()
                    x = CombinationNode(x, r)
                } else {
                    break
                }
            }

            return x
        }
    }
}
