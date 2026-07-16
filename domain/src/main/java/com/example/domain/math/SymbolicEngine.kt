package com.example.domain.math

import kotlin.math.*

sealed class MathNode {
    abstract fun simplify(): MathNode
    abstract fun differentiate(variable: String): MathNode
    abstract fun toStringRepr(): String
    abstract fun evaluate(variables: Map<String, Double>): Double

    data class Number(val value: Double) : MathNode() {
        override fun simplify() = this
        override fun differentiate(variable: String) = Number(0.0)
        override fun toStringRepr(): String = if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString()
        override fun evaluate(variables: Map<String, Double>) = value
    }

    data class Variable(val name: String) : MathNode() {
        override fun simplify() = this
        override fun differentiate(variable: String) = if (name == variable) Number(1.0) else Number(0.0)
        override fun toStringRepr() = name
        override fun evaluate(variables: Map<String, Double>) = variables[name] ?: 0.0
    }

    data class BinaryOp(val left: MathNode, val right: MathNode, val op: Char) : MathNode() {
        override fun simplify(): MathNode {
            val sLeft = left.simplify()
            val sRight = right.simplify()
            
            if (sLeft is Number && sRight is Number) {
                return when (op) {
                    '+' -> Number(sLeft.value + sRight.value)
                    '-' -> Number(sLeft.value - sRight.value)
                    '*' -> Number(sLeft.value * sRight.value)
                    '/' -> if (sRight.value != 0.0) Number(sLeft.value / sRight.value) else this
                    '^' -> Number(sLeft.value.pow(sRight.value))
                    else -> this
                }
            }
            
            return when (op) {
                '+' -> {
                    if (sLeft is Number && sLeft.value == 0.0) sRight
                    else if (sRight is Number && sRight.value == 0.0) sLeft
                    else BinaryOp(sLeft, sRight, '+')
                }
                '-' -> {
                    if (sRight is Number && sRight.value == 0.0) sLeft
                    else if (sLeft == sRight) Number(0.0)
                    else BinaryOp(sLeft, sRight, '-')
                }
                '*' -> {
                    if (sLeft is Number && sLeft.value == 0.0) Number(0.0)
                    else if (sRight is Number && sRight.value == 0.0) Number(0.0)
                    else if (sLeft is Number && sLeft.value == 1.0) sRight
                    else if (sRight is Number && sRight.value == 1.0) sLeft
                    else BinaryOp(sLeft, sRight, '*')
                }
                '/' -> {
                    if (sLeft is Number && sLeft.value == 0.0) Number(0.0)
                    else if (sRight is Number && sRight.value == 1.0) sLeft
                    else if (sLeft == sRight) Number(1.0)
                    else BinaryOp(sLeft, sRight, '/')
                }
                '^' -> {
                    if (sRight is Number && sRight.value == 0.0) Number(1.0)
                    else if (sRight is Number && sRight.value == 1.0) sLeft
                    else if (sLeft is Number && sLeft.value == 1.0) Number(1.0)
                    else BinaryOp(sLeft, sRight, '^')
                }
                else -> BinaryOp(sLeft, sRight, op)
            }
        }

        override fun differentiate(variable: String): MathNode {
            return when (op) {
                '+' -> BinaryOp(left.differentiate(variable), right.differentiate(variable), '+')
                '-' -> BinaryOp(left.differentiate(variable), right.differentiate(variable), '-')
                '*' -> BinaryOp(
                    BinaryOp(left.differentiate(variable), right, '*'),
                    BinaryOp(left, right.differentiate(variable), '*'),
                    '+'
                )
                '/' -> BinaryOp(
                    BinaryOp(
                        BinaryOp(left.differentiate(variable), right, '*'),
                        BinaryOp(left, right.differentiate(variable), '*'),
                        '-'
                    ),
                    BinaryOp(right, Number(2.0), '^'),
                    '/'
                )
                '^' -> {
                    // (f^g)' = f^g * (g'lnf + gf'/f)
                    if (right is Number) {
                        // Special case: power rule f^n' = n * f^(n-1) * f'
                        BinaryOp(
                            BinaryOp(right, BinaryOp(left, Number(right.value - 1), '^'), '*'),
                            left.differentiate(variable),
                            '*'
                        )
                    } else {
                        // General case
                        BinaryOp(
                            this,
                            BinaryOp(
                                BinaryOp(right.differentiate(variable), Function("ln", left), '*'),
                                BinaryOp(BinaryOp(right, left.differentiate(variable), '*'), left, '/'),
                                '+'
                            ),
                            '*'
                        )
                    }
                }
                else -> Number(0.0)
            }
        }

        override fun toStringRepr(): String {
            val lStr = if (left is BinaryOp && precedence(left.op) < precedence(op)) "(${left.toStringRepr()})" else left.toStringRepr()
            val rStr = if (right is BinaryOp && precedence(right.op) <= precedence(op)) "(${right.toStringRepr()})" else right.toStringRepr()
            return "$lStr$op$rStr"
        }
        
        private fun precedence(op: Char): Int = when(op) {
            '+', '-' -> 1
            '*', '/' -> 2
            '^' -> 3
            else -> 0
        }

        override fun evaluate(variables: Map<String, Double>): Double {
            val l = left.evaluate(variables)
            val r = right.evaluate(variables)
            return when (op) {
                '+' -> l + r
                '-' -> l - r
                '*' -> l * r
                '/' -> l / r
                '^' -> l.pow(r)
                else -> 0.0
            }
        }
    }

    data class Function(val name: String, val arg: MathNode) : MathNode() {
        override fun simplify() = Function(name, arg.simplify())

        override fun differentiate(variable: String): MathNode {
            val darg = arg.differentiate(variable)
            val dfunc = when (name) {
                "sin" -> Function("cos", arg)
                "cos" -> BinaryOp(Number(-1.0), Function("sin", arg), '*')
                "tan" -> BinaryOp(Number(1.0), BinaryOp(Function("cos", arg), Number(2.0), '^'), '/')
                "ln" -> BinaryOp(Number(1.0), arg, '/')
                "exp" -> Function("exp", arg)
                "sqrt" -> BinaryOp(Number(0.5), BinaryOp(Function("sqrt", arg), Number(-1.0), '^'), '*')
                else -> Number(0.0)
            }
            return BinaryOp(dfunc, darg, '*')
        }

        override fun toStringRepr() = "$name(${arg.toStringRepr()})"
        
        override fun evaluate(variables: Map<String, Double>): Double {
            val a = arg.evaluate(variables)
            return when (name) {
                "sin" -> sin(a)
                "cos" -> cos(a)
                "tan" -> tan(a)
                "ln" -> ln(a)
                "exp" -> exp(a)
                "sqrt" -> sqrt(a)
                else -> 0.0
            }
        }
    }
}

class SymbolicEngine {
    fun parse(expr: String): MathNode {
        val s = expr.replace(" ", "").lowercase()
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < s.length) s[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): MathNode {
                nextChar()
                val x = parseExpression()
                if (pos < s.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): MathNode {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x = MathNode.BinaryOp(x, parseTerm(), '+')
                    else if (eat('-'.code)) x = MathNode.BinaryOp(x, parseTerm(), '-')
                    else return x
                }
            }

            fun parseTerm(): MathNode {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x = MathNode.BinaryOp(x, parseFactor(), '*')
                    else if (eat('/'.code)) x = MathNode.BinaryOp(x, parseFactor(), '/')
                    else return x
                }
            }

            fun parseFactor(): MathNode {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return MathNode.BinaryOp(MathNode.Number(-1.0), parseFactor(), '*')

                var x: MathNode
                val startPos = this.pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = MathNode.Number(s.substring(startPos, this.pos).toDouble())
                } else if (ch >= 'a'.code && ch <= 'z'.code) {
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val name = s.substring(startPos, this.pos)
                    if (eat('('.code)) {
                        val arg = parseExpression()
                        eat(')'.code)
                        x = MathNode.Function(name, arg)
                    } else {
                        x = MathNode.Variable(name)
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = MathNode.BinaryOp(x, parseFactor(), '^')
                return x
            }
        }.parse()
    }

    fun simplify(expr: String): String {
        return try {
            var node = parse(expr)
            repeat(5) { node = node.simplify() }
            node.toStringRepr()
        } catch (e: Exception) {
            expr
        }
    }

    fun differentiate(expr: String, variable: String = "x"): String {
        return try {
            var node = parse(expr).differentiate(variable)
            repeat(5) { node = node.simplify() }
            node.toStringRepr()
        } catch (e: Exception) {
            "Error"
        }
    }
    
    fun integrate(expr: String, variable: String = "x"): String {
        // Very basic symbolic integration for CAS Lite
        // Power rule: x^n -> x^(n+1)/(n+1)
        // Trigonometric: sin(x) -> -cos(x), cos(x) -> sin(x)
        // exp(x) -> exp(x)
        
        return try {
            val node = parse(expr)
            val result = integrateNode(node, variable)
            if (result != null) {
                var simplified: MathNode = result
                repeat(5) { simplified = simplified.simplify() }
                simplified.toStringRepr() + " + C"
            } else {
                "Complex integral (Numerical only)"
            }
        } catch (e: Exception) {
            "Error"
        }
    }
    
    private fun integrateNode(node: MathNode, variable: String): MathNode? {
        return when (node) {
            is MathNode.Number -> MathNode.BinaryOp(node, MathNode.Variable(variable), '*')
            is MathNode.Variable -> if (node.name == variable) {
                MathNode.BinaryOp(MathNode.BinaryOp(node, MathNode.Number(2.0), '^'), MathNode.Number(2.0), '/')
            } else {
                MathNode.BinaryOp(node, MathNode.Variable(variable), '*')
            }
            is MathNode.BinaryOp -> {
                when (node.op) {
                    '+' -> {
                        val l = integrateNode(node.left, variable)
                        val r = integrateNode(node.right, variable)
                        if (l != null && r != null) MathNode.BinaryOp(l, r, '+') else null
                    }
                    '-' -> {
                        val l = integrateNode(node.left, variable)
                        val r = integrateNode(node.right, variable)
                        if (l != null && r != null) MathNode.BinaryOp(l, r, '-') else null
                    }
                    '*' -> {
                        if (node.left is MathNode.Number) {
                            val r = integrateNode(node.right, variable)
                            if (r != null) MathNode.BinaryOp(node.left, r, '*') else null
                        } else if (node.right is MathNode.Number) {
                            val l = integrateNode(node.left, variable)
                            if (l != null) MathNode.BinaryOp(node.right, l, '*') else null
                        } else null
                    }
                    '^' -> {
                        if (node.left is MathNode.Variable && node.left.name == variable && node.right is MathNode.Number) {
                            val n = node.right.value
                            MathNode.BinaryOp(
                                MathNode.BinaryOp(node.left, MathNode.Number(n + 1), '^'),
                                MathNode.Number(n + 1),
                                '/'
                            )
                        } else null
                    }
                    else -> null
                }
            }
            is MathNode.Function -> {
                if (node.arg is MathNode.Variable && node.arg.name == variable) {
                    when (node.name) {
                        "sin" -> MathNode.BinaryOp(MathNode.Number(-1.0), MathNode.Function("cos", node.arg), '*')
                        "cos" -> MathNode.Function("sin", node.arg)
                        "exp" -> MathNode.Function("exp", node.arg)
                        else -> null
                    }
                } else null
            }
        }
    }
    
    fun expand(expr: String): String {
        // Expand (a+b)*c -> ac + bc
        // This is a placeholder for a more complex expand logic
        return simplify(expr) // For CAS Lite, we'll keep it simple
    }
}
