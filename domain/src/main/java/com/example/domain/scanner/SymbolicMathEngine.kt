package com.example.domain.scanner

import com.example.domain.math.*
import kotlin.math.*

object SymbolicMathEngine {

    fun simplify(node: ExprNode): ExprNode {
        return when (node) {
            is AddNode -> {
                val l = simplify(node.left)
                val r = simplify(node.right)
                if (l is NumberNode && l.value == 0.0) return r
                if (r is NumberNode && r.value == 0.0) return l
                if (l is NumberNode && r is NumberNode) return NumberNode(l.value + r.value)
                AddNode(l, r)
            }
            is SubNode -> {
                val l = simplify(node.left)
                val r = simplify(node.right)
                if (r is NumberNode && r.value == 0.0) return l
                if (l is NumberNode && r is NumberNode) return NumberNode(l.value - r.value)
                if (l.toSymbolic() == r.toSymbolic()) return NumberNode(0.0)
                SubNode(l, r)
            }
            is MulNode -> {
                val l = simplify(node.left)
                val r = simplify(node.right)
                if (l is NumberNode && l.value == 0.0) return NumberNode(0.0)
                if (r is NumberNode && r.value == 0.0) return NumberNode(0.0)
                if (l is NumberNode && l.value == 1.0) return r
                if (r is NumberNode && r.value == 1.0) return l
                if (l is NumberNode && r is NumberNode) return NumberNode(l.value * r.value)
                
                if (l.toSymbolic() == r.toSymbolic()) return PowNode(l, NumberNode(2.0))
                if (r is PowNode && l.toSymbolic() == r.base.toSymbolic() && r.exponent is NumberNode) {
                    return simplify(PowNode(l, NumberNode(r.exponent.value + 1)))
                }
                if (l is PowNode && r.toSymbolic() == l.base.toSymbolic() && l.exponent is NumberNode) {
                    return simplify(PowNode(r, NumberNode(l.exponent.value + 1)))
                }
                
                MulNode(l, r)
            }
            is DivNode -> {
                val l = simplify(node.left)
                val r = simplify(node.right)
                if (l is NumberNode && l.value == 0.0) return NumberNode(0.0)
                if (r is NumberNode && r.value == 1.0) return l
                if (l is NumberNode && r is NumberNode && r.value != 0.0) return NumberNode(l.value / r.value)
                DivNode(l, r)
            }
            is PowNode -> {
                val b = simplify(node.base)
                val e = simplify(node.exponent)
                if (e is NumberNode && e.value == 0.0) return NumberNode(1.0)
                if (e is NumberNode && e.value == 1.0) return b
                if (b is NumberNode && b.value == 1.0) return NumberNode(1.0)
                if (b is NumberNode && b.value == 0.0 && e is NumberNode && e.value > 0.0) return NumberNode(0.0)
                
                // (x^a)^b -> x^(a*b)
                if (b is PowNode && e is NumberNode && b.exponent is NumberNode) {
                    return simplify(PowNode(b.base, NumberNode(b.exponent.value * e.value)))
                }
                // sqrt(x)^2 -> x
                if (b is FuncNode && (b.name == "sqrt" || b.name == "√") && e is NumberNode && e.value == 2.0) return b.child
                
                if (b is NumberNode && e is NumberNode) return NumberNode(b.value.pow(e.value))
                PowNode(b, e)
            }
            is NegNode -> {
                val c = simplify(node.child)
                if (c is NumberNode) return NumberNode(-c.value)
                if (c is NegNode) return c.child
                NegNode(c)
            }
            is FuncNode -> {
                val c = simplify(node.child)
                if (node.name == "ln" && c is FuncNode && c.name == "exp") return c.child
                if (node.name == "exp" && c is FuncNode && c.name == "ln") return c.child
                if (node.name == "sqrt" && c is PowNode && c.exponent is NumberNode && c.exponent.value == 2.0) return c.base
                
                if (c is NumberNode) {
                    return try {
                        val v = node.eval(emptyMap(), false)
                        if (abs(v - v.roundToLong()) < 1e-9) NumberNode(v.roundToLong().toDouble())
                        else FuncNode(node.name, c)
                    } catch (e: Exception) {
                        FuncNode(node.name, c)
                    }
                }
                FuncNode(node.name, c)
            }
            else -> node
        }
    }

    fun differentiate(node: ExprNode, variable: String): ExprNode {
        val d = when (node) {
            is NumberNode -> NumberNode(0.0)
            is ConstantNode -> NumberNode(0.0)
            is VariableNode -> if (node.name == variable) NumberNode(1.0) else NumberNode(0.0)
            is AddNode -> AddNode(differentiate(node.left, variable), differentiate(node.right, variable))
            is SubNode -> SubNode(differentiate(node.left, variable), differentiate(node.right, variable))
            is MulNode -> AddNode(
                MulNode(differentiate(node.left, variable), node.right),
                MulNode(node.left, differentiate(node.right, variable))
            )
            is DivNode -> DivNode(
                SubNode(
                    MulNode(differentiate(node.left, variable), node.right),
                    MulNode(node.left, differentiate(node.right, variable))
                ),
                PowNode(node.right, NumberNode(2.0))
            )
            is PowNode -> {
                // Simplified Power Rule: d/dx(u^n) = n*u^(n-1)*u'
                if (node.exponent is NumberNode) {
                    val n = node.exponent.value
                    MulNode(
                        MulNode(NumberNode(n), PowNode(node.base, NumberNode(n - 1))),
                        differentiate(node.base, variable)
                    )
                } else {
                    // General case: d/dx(u^v) = u^v * (v' ln u + v u' / u)
                    MulNode(node, AddNode(
                        MulNode(differentiate(node.exponent, variable), FuncNode("ln", node.base)),
                        DivNode(MulNode(node.exponent, differentiate(node.base, variable)), node.base)
                    ))
                }
            }
            is FuncNode -> {
                val u = node.child
                val du = differentiate(u, variable)
                when (node.name) {
                    "sin" -> MulNode(FuncNode("cos", u), du)
                    "cos" -> NegNode(MulNode(FuncNode("sin", u), du))
                    "tan" -> DivNode(du, PowNode(FuncNode("cos", u), NumberNode(2.0)))
                    "ln" -> DivNode(du, u)
                    "exp" -> MulNode(node, du)
                    "sqrt" -> DivNode(du, MulNode(NumberNode(2.0), node))
                    "log" -> DivNode(du, MulNode(u, FuncNode("ln", NumberNode(10.0))))
                    else -> NumberNode(0.0)
                }
            }
            is NegNode -> NegNode(differentiate(node.child, variable))
            else -> NumberNode(0.0)
        }
        return simplify(d)
    }

    fun nthDerivative(node: ExprNode, variable: String, n: Int): ExprNode {
        var result = node
        repeat(n) {
            result = differentiate(result, variable)
        }
        return result
    }

    fun partialDifferentiate(node: ExprNode, variable: String): ExprNode {
        // In this symbolic engine, partial differentiation is equivalent 
        // to regular differentiation with respect to one variable.
        return differentiate(node, variable)
    }

    fun expand(node: ExprNode): ExprNode {
        // Basic expansion: (a+b)*c -> ac + bc
        val expanded = when (node) {
            is MulNode -> {
                val l = expand(node.left)
                val r = expand(node.right)
                if (l is AddNode) {
                    expand(AddNode(MulNode(l.left, r), MulNode(l.right, r)))
                } else if (r is AddNode) {
                    expand(AddNode(MulNode(l, r.left), MulNode(l, r.right)))
                } else if (l is SubNode) {
                    expand(SubNode(MulNode(l.left, r), MulNode(l.right, r)))
                } else if (r is SubNode) {
                    expand(SubNode(MulNode(l, r.left), MulNode(l, r.right)))
                } else {
                    MulNode(l, r)
                }
            }
            is AddNode -> AddNode(expand(node.left), expand(node.right))
            is SubNode -> SubNode(expand(node.left), expand(node.right))
            is PowNode -> {
                val b = expand(node.base)
                val e = node.exponent
                if (e is NumberNode && e.value == 2.0 && b is AddNode) {
                    // (a+b)^2 -> a^2 + 2ab + b^2
                    val a = b.left
                    val bb = b.right
                    AddNode(AddNode(PowNode(a, NumberNode(2.0)), MulNode(NumberNode(2.0), MulNode(a, bb))), PowNode(bb, NumberNode(2.0)))
                } else if (e is NumberNode && e.value == 2.0 && b is SubNode) {
                    // (a-b)^2 -> a^2 - 2ab + b^2
                    val a = b.left
                    val bb = b.right
                    AddNode(SubNode(PowNode(a, NumberNode(2.0)), MulNode(NumberNode(2.0), MulNode(a, bb))), PowNode(bb, NumberNode(2.0)))
                } else {
                    PowNode(b, e)
                }
            }
            else -> node
        }
        return simplify(expanded)
    }

    fun integrate(node: ExprNode, variable: String): ExprNode? {
        val simplifiedNode = simplify(node)
        return when (simplifiedNode) {
            is NumberNode -> MulNode(simplifiedNode, VariableNode(variable))
            is VariableNode -> {
                if (simplifiedNode.name == variable) {
                    MulNode(NumberNode(0.5), PowNode(simplifiedNode, NumberNode(2.0)))
                } else {
                    MulNode(simplifiedNode, VariableNode(variable))
                }
            }
            is AddNode -> {
                val l = integrate(simplifiedNode.left, variable)
                val r = integrate(simplifiedNode.right, variable)
                if (l != null && r != null) AddNode(l, r) else null
            }
            is SubNode -> {
                val l = integrate(simplifiedNode.left, variable)
                val r = integrate(simplifiedNode.right, variable)
                if (l != null && r != null) SubNode(l, r) else null
            }
            is PowNode -> {
                if (simplifiedNode.base is VariableNode && simplifiedNode.base.name == variable && simplifiedNode.exponent is NumberNode) {
                    val n = simplifiedNode.exponent.value
                    if (n == -1.0) FuncNode("ln", simplifiedNode.base)
                    else MulNode(DivNode(NumberNode(1.0), NumberNode(n + 1)), PowNode(simplifiedNode.base, NumberNode(n + 1)))
                } else {
                    null
                }
            }
            is FuncNode -> {
                if (simplifiedNode.child is VariableNode && simplifiedNode.child.name == variable) {
                    when (simplifiedNode.name) {
                        "sin" -> NegNode(FuncNode("cos", simplifiedNode.child))
                        "cos" -> FuncNode("sin", simplifiedNode.child)
                        "exp" -> simplifiedNode
                        "sec^2" -> FuncNode("tan", simplifiedNode.child)
                        else -> null
                    }
                } else {
                    null
                }
            }
            is MulNode -> {
                if (simplifiedNode.left is NumberNode) {
                    val r = integrate(simplifiedNode.right, variable)
                    if (r != null) MulNode(simplifiedNode.left, r) else null
                } else if (simplifiedNode.right is NumberNode) {
                    val l = integrate(simplifiedNode.left, variable)
                    if (l != null) MulNode(simplifiedNode.right, l) else null
                } else {
                    null
                }
            }
            is NegNode -> {
                val c = integrate(simplifiedNode.child, variable)
                if (c != null) NegNode(c) else null
            }
            else -> null
        }
    }

    fun solveLinear(equation: String, variable: String, engine: CalculatorEngine): String {
        // Basic linear solver
        val parts = equation.split("=")
        if (parts.size != 2) return "Invalid equation"
        
        try {
            val leftNode = engine.parse(parts[0])
            val rightNode = engine.parse(parts[1])
            
            // Move everything to left: (left - right) = 0
            val combined = simplify(SubNode(leftNode, rightNode))
            
            // For a linear equation ax + b = 0
            // b = f(0)
            // a = f(1) - f(0)
            val b = combined.eval(mapOf(variable to 0.0), false)
            val a = combined.eval(mapOf(variable to 1.0), false) - b
            
            if (abs(a) < 1e-9) {
                return if (abs(b) < 1e-9) "Infinite solutions" else "No solution"
            }
            
            val result = -b / a
            return "$variable = ${formatResult(result)}"
        } catch (e: Exception) {
            return "Could not solve symbolically"
        }
    }
    
    private fun formatResult(n: Double): String {
        if (abs(n - n.roundToLong()) < 1e-9) return n.roundToLong().toString()
        // Try to represent as fraction
        for (den in 1..100) {
            val num = n * den
            if (abs(num - num.roundToLong()) < 1e-7) {
                return "${num.roundToLong()}/$den"
            }
        }
        return String.format("%.4f", n).trimEnd('0').trimEnd('.')
    }
}
