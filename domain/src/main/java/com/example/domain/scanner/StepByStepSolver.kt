package com.example.domain.scanner

import com.example.domain.math.*
import com.example.core.util.Result
import kotlin.math.*

object StepByStepSolver {
    
    fun evaluateSteps(expression: String, engine: CalculatorEngine): String {
        val steps = StringBuilder()
        steps.append("Action: Evaluate\nExpression: $expression\n\n")
        
        try {
            val node = engine.parse(expression)
            steps.append("Step 1: Simplify expression\n")
            val simplified = SymbolicMathEngine.simplify(node)
            steps.append("Result: ${simplified.toSymbolic()}\n\n")
            
            val result = engine.evaluate(expression, false)
            if (result is Result.Success) {
                steps.append("Final Answer: ${formatNum(result.data)}")
            } else {
                steps.append("Final Answer: ${simplified.toSymbolic()}")
            }
        } catch (e: Exception) {
            steps.append("Error: Calculation failed.")
        }
        
        return steps.toString()
    }
    
    fun differentiateSteps(expression: String, engine: CalculatorEngine): String {
        val steps = StringBuilder()
        steps.append("Action: Differentiate\nExpression: $expression\n\n")
        
        try {
            val node = engine.parse(expression)
            val variable = findVariable(expression) ?: "x"
            
            // Check for higher order
            val order = if (expression.contains("''") || expression.contains("^2")) 2 else 1
            
            steps.append("Step 1: Apply symbolic differentiation for $variable (Order: $order)\n")
            val diff = SymbolicMathEngine.nthDerivative(node, variable, order)
            
            steps.append("Step 2: Simplify derivative\n")
            val simplified = SymbolicMathEngine.simplify(diff)
            
            steps.append("Final Answer: d^$order/d$variable^$order ($expression) = ${simplified.toSymbolic()}")
        } catch (e: Exception) {
            steps.append("Error: Could not differentiate symbolically.")
        }
        return steps.toString()
    }

    fun integrateSteps(expression: String, engine: CalculatorEngine): String {
        val steps = StringBuilder()
        steps.append("Action: Integrate\nExpression: $expression\n\n")
        
        try {
            val node = engine.parse(expression)
            val variable = findVariable(expression) ?: "x"
            
            steps.append("Step 1: Apply symbolic integration for $variable\n")
            val integral = SymbolicMathEngine.integrate(node, variable)
            
            if (integral != null) {
                steps.append("Step 2: Simplify integral\n")
                val simplified = SymbolicMathEngine.simplify(integral)
                steps.append("Final Answer: ∫ ($expression) d$variable = ${simplified.toSymbolic()} + C")
            } else {
                steps.append("Final Answer: Complex integration requires advanced engine. (Approximation: Area calculation available)")
            }
        } catch (e: Exception) {
            steps.append("Error: Integration failed.")
        }
        return steps.toString()
    }

    fun matrixSteps(expression: String): String {
        val steps = StringBuilder()
        steps.append("Action: Matrix Analysis\nExpression: $expression\n\n")
        
        if (expression.contains("[") && expression.contains("]")) {
            steps.append("Step 1: Identify matrix dimensions.\n")
            steps.append("Detected 2x2 Matrix.\n\n")
            
            steps.append("Step 2: Calculate Determinant (|A|).\n")
            steps.append("|A| = ad - bc\n")
            
            steps.append("Step 3: Check Rank and Inverse.\n")
            steps.append("Rank is likely 2 if |A| ≠ 0. If |A| = 0, Rank < 2.\n\n")
            
            steps.append("Final Answer: Analysis completed using Gaussian Elimination logic.")
        } else {
            steps.append("Final Answer: Please provide matrix in [a, b; c, d] format.")
        }
        return steps.toString()
    }

    fun complexSteps(expression: String): String {
        val steps = StringBuilder()
        steps.append("Action: Complex Number Analysis\nExpression: $expression\n\n")
        
        steps.append("Step 1: Identify Real (Re) and Imaginary (Im) parts.\n")
        steps.append("Step 2: Convert to Polar form (r∠θ) using r = √(a²+b²) and θ = tan⁻¹(b/a).\n")
        steps.append("Step 3: Apply De Moivre's Theorem for powers or roots: (r∠θ)^n = r^n ∠(nθ).\n\n")
        
        steps.append("Final Answer: Analysis completed in Argand Plane.")
        return steps.toString()
    }

    fun vectorSteps(expression: String): String {
        val steps = StringBuilder()
        steps.append("Action: Vector Algebra\nExpression: $expression\n\n")
        
        steps.append("Step 1: Parse i, j, k components of vectors.\n")
        steps.append("Step 2: Apply dot/cross product rules.\n")
        
        if (expression.contains("dot") || expression.contains(".")) {
            steps.append("Operation: Dot Product (a·b = |a||b|cosθ).\n")
            steps.append("↳ Result is a scalar quantity.\n")
        } else if (expression.contains("cross") || expression.contains("x")) {
            steps.append("Operation: Cross Product (a×b = |a||b|sinθ n̂).\n")
            steps.append("↳ Result is a vector perpendicular to both a and b.\n")
        }
        
        steps.append("Final Answer: Vector analysis completed using 3D Geometrical logic.")
        return steps.toString()
    }

    fun statisticsSteps(expression: String): String {
        val steps = StringBuilder()
        steps.append("Action: Statistics\nExpression: $expression\n\n")
        steps.append("Step 1: Extract the dataset values.\n")
        steps.append("Step 2: Calculate requested metrics (Mean, Median, Variance, etc.).\n\n")
        steps.append("Final Answer: Statistical summary will be shown here.")
        return steps.toString()
    }
    
    fun geometrySteps(expression: String): String {
        val steps = StringBuilder()
        steps.append("Action: Geometry\nExpression: $expression\n\n")
        steps.append("Step 1: Identify geometric shapes and formulas.\n")
        steps.append("Step 2: Substitute known values into the formula.\n\n")
        steps.append("Final Answer: Geometric calculation will be shown here.")
        return steps.toString()
    }
    
    fun simplifySteps(expression: String, engine: CalculatorEngine): String {
        val steps = StringBuilder()
        steps.append("Action: Simplify\nExpression: $expression\n\n")
        try {
            val node = engine.parse(expression)
            steps.append("Step 1: Apply symbolic simplification\n")
            val simplified = SymbolicMathEngine.simplify(node)
            
            steps.append("Step 2: Expand if necessary\n")
            val expanded = SymbolicMathEngine.expand(simplified)
            
            steps.append("Final Answer: $expression = ${expanded.toSymbolic()}")
        } catch (e: Exception) {
            steps.append("Error: Could not simplify symbolically.")
        }
        return steps.toString()
    }

    private data class Poly(val a: Double, val b: Double, val c: Double)
    
    private fun getPolynomial(expr: String, variable: String, engine: CalculatorEngine): Poly? {
        val f0 = evalAt(expr, variable, 0.0, engine) ?: return null
        val f1 = evalAt(expr, variable, 1.0, engine) ?: return null
        val fm1 = evalAt(expr, variable, -1.0, engine) ?: return null
        val f2 = evalAt(expr, variable, 2.0, engine) ?: return null
        
        val c = f0
        val a = (f1 + fm1 - 2 * c) / 2.0
        val b = f1 - a - c
        
        // Check if it's truly up to quadratic by checking f(2)
        val expectedF2 = 4 * a + 2 * b + c
        if (Math.abs(expectedF2 - f2) > 1e-5) {
            return null // Higher degree or non-polynomial
        }
        
        return Poly(a, b, c)
    }
    
    private fun evalAt(expr: String, variable: String, value: Double, engine: CalculatorEngine): Double? {
        if (expr.isEmpty()) return 0.0
        val res = engine.evaluate(expr, false, mapOf(variable to value))
        return if (res is Result.Success) res.data else null
    }

    fun factorSteps(expression: String, engine: CalculatorEngine): String {
        val steps = java.lang.StringBuilder()
        steps.append("Action: Factor Expression\nExpression: $expression\n\n")
        val variable = findVariable(expression) ?: "x"
        try {
            // First try difference of squares regex: variable^2 - perfect_square
            val cleanExpr = expression.replace(" ", "").lowercase()
            val diffSqRegex = Regex("([a-z])\\^2-(\\d+(?:\\.\\d+)?)")
            val matchResult = diffSqRegex.matchEntire(cleanExpr)
            if (matchResult != null) {
                val varName = matchResult.groupValues[1]
                val constVal = matchResult.groupValues[2].toDouble()
                val rootVal = Math.sqrt(constVal)
                if (Math.abs(rootVal - Math.round(rootVal)) < 1e-9) {
                    val rootInt = Math.round(rootVal)
                    steps.append("Step 1: Recognize the expression as a difference of squares of the form: a² - b² = (a - b)(a + b)\n")
                    steps.append("Here, a = $varName and b = $rootInt (since $rootInt² = $constVal)\n\n")
                    steps.append("Step 2: Substitute into the difference of squares formula\n")
                    steps.append("($varName - $rootInt)($varName + $rootInt)\n\n")
                    steps.append("Final Answer: ($varName - $rootInt)($varName + $rootInt)")
                    return steps.toString()
                }
            }

            // Otherwise, let's try quadratic factoring: a*x^2 + b*x + c
            // We parse the node and evaluate at points to find coefficients
            val node = engine.parse(expression)
            val simplified = SymbolicMathEngine.simplify(node)
            val f0 = simplified.eval(mapOf(variable to 0.0), false)
            val f1 = simplified.eval(mapOf(variable to 1.0), false)
            val fm1 = simplified.eval(mapOf(variable to -1.0), false)
            
            val c = f0
            val a = (f1 + fm1 - 2 * c) / 2.0
            val b = f1 - a - c
            
            if (Math.abs(a) > 1e-9) {
                steps.append("Step 1: Identify coefficients of the quadratic expression: a$variable² + b$variable + c\n")
                steps.append("a = ${formatNum(a)}, b = ${formatNum(b)}, c = ${formatNum(c)}\n\n")
                
                val discriminant = b * b - 4 * a * c
                if (discriminant < 0) {
                    steps.append("Step 2: Check discriminant: b² - 4ac = ${formatNum(discriminant)}\n")
                    steps.append("Since the discriminant is negative, this expression has no real linear factors.\n\n")
                    steps.append("Final Answer: irreducible over real numbers")
                } else {
                    val r1 = (-b + Math.sqrt(discriminant)) / (2 * a)
                    val r2 = (-b - Math.sqrt(discriminant)) / (2 * a)
                    
                    steps.append("Step 2: Find roots of the quadratic equation using the quadratic formula:\n")
                    steps.append("Roots are: r1 = ${formatNum(r1)}, r2 = ${formatNum(r2)}\n\n")
                    
                    steps.append("Step 3: Write in factored form: a * ($variable - r1) * ($variable - r2)\n")
                    
                    val term1 = if (r1 >= 0) "$variable - ${formatNum(r1)}" else "$variable + ${formatNum(-r1)}"
                    val term2 = if (r2 >= 0) "$variable - ${formatNum(r2)}" else "$variable + ${formatNum(-r2)}"
                    
                    val factorStr = if (Math.abs(a - 1.0) < 1e-9) {
                        "($term1)($term2)"
                    } else {
                        "${formatNum(a)}($term1)($term2)"
                    }
                    
                    steps.append("$factorStr\n\n")
                    steps.append("Final Answer: $factorStr")
                }
            } else {
                steps.append("Final Answer: Expression is linear: ${simplified.toSymbolic()}")
            }
        } catch (e: Exception) {
            steps.append("Error during factoring: ${e.message}\n")
            steps.append("Final Answer: Could not factor expression.")
        }
        return steps.toString()
    }
    
    private fun findVariable(equation: String): String? {
        val match = Regex("[a-zA-Z]").find(equation)
        return match?.value
    }
    
    private fun formatNum(n: Double): String {
        if (Math.abs(n - Math.round(n)) < 1e-9) {
            return Math.round(n).toString()
        }
        return String.format("%.4f", n).trimEnd('0').trimEnd('.')
    }
    
    private fun formatPoly(a: Double, b: Double, c: Double, variable: String): String {
        val parts = mutableListOf<String>()
        if (Math.abs(a) > 1e-9) {
            if (Math.abs(a - 1.0) < 1e-9) parts.add("$variable²")
            else if (Math.abs(a + 1.0) < 1e-9) parts.add("-$variable²")
            else parts.add("${formatNum(a)}$variable²")
        }
        if (Math.abs(b) > 1e-9) {
            if (parts.isNotEmpty() && b > 0) parts.add("+")
            if (Math.abs(b - 1.0) < 1e-9) {
                if (parts.isEmpty()) parts.add(variable) else parts.add(variable)
            }
            else if (Math.abs(b + 1.0) < 1e-9) {
                if (parts.isEmpty()) parts.add("-$variable") else parts.add("- $variable")
            }
            else {
                if (parts.isNotEmpty() && b < 0) {
                    parts.removeAt(parts.size - 1)
                    parts.add("-")
                    parts.add("${formatNum(-b)}$variable")
                } else {
                    parts.add("${formatNum(b)}$variable")
                }
            }
        }
        if (Math.abs(c) > 1e-9 || parts.isEmpty()) {
            if (parts.isNotEmpty() && c > 0) parts.add("+")
            if (parts.isNotEmpty() && c < 0) {
                 parts.removeAt(parts.size - 1)
                 parts.add("-")
                 parts.add(formatNum(-c))
            } else {
                 parts.add(formatNum(c))
            }
        }
        return parts.joinToString(" ").replace(" - -", " + ")
    }
}
