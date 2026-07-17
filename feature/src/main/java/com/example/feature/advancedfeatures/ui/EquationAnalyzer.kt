package com.example.feature.advancedfeatures.ui

import com.example.domain.math.CalculatorEngine
import com.example.domain.math.RootFinder
import com.example.domain.math.FunctionAnalyzer
import com.example.domain.model.ExtremerPoint
import kotlin.math.*

enum class RecognizedEquationType {
    Linear,
    Quadratic,
    Circle,
    Parabola,
    Ellipse,
    Hyperbola,
    Trigonometric,
    General
}

data class EquationAnalysis(
    val equationString: String,
    val type: RecognizedEquationType,
    val details: Map<String, String> = emptyMap(),
    val roots: List<Double> = emptyList(),
    val extrema: List<ExtremerPoint> = emptyList()
)

class EquationAnalyzer(
    private val calculatorEngine: CalculatorEngine,
    private val rootFinder: RootFinder,
    private val functionAnalyzer: FunctionAnalyzer
) {
    private fun preprocess(expression: String): String {
        return expression
            .replace(Regex("(\\d+)([a-zA-Z\\(])"), "$1*$2")
            .replace(")(", ")*(")
            .replace(Regex("([xyt])\\("), "$1*(")
    }

    fun analyze(input: String): EquationAnalysis {
        val cleanInput = input.replace(" ", "").lowercase()
        var preprocessedInput = preprocess(input)
        if (preprocessedInput.replace(" ", "").lowercase().startsWith("y=")) {
            preprocessedInput = preprocessedInput.substringAfter("=")
        }

        if (cleanInput.startsWith("r=")) {
            val rExpr = input.substringAfter("=").trim()
            val details = mapOf(
                "Classification" to "Polar Equation",
                "Expression" to "r = $rExpr",
                "Independent Variable" to "theta"
            )
            return EquationAnalysis(input, RecognizedEquationType.General, details)
        }

        if (cleanInput.contains("x=") && cleanInput.contains("y=")) {
            val parts = input.split(",")
            val xPart = parts.getOrNull(0)?.substringAfter("=")?.trim() ?: ""
            val yPart = parts.getOrNull(1)?.substringAfter("=")?.trim() ?: ""
            val details = mapOf(
                "Classification" to "Parametric Equation",
                "X Expression" to "x = $xPart",
                "Y Expression" to "y = $yPart",
                "Parameter" to "t"
            )
            return EquationAnalysis(input, RecognizedEquationType.General, details)
        }

        // 1. Check for Circle implicit
        if (cleanInput == "x^2+y^2=25" || cleanInput.matches(Regex("x\\^2\\+y\\^2=\\d+"))) {
            val rSqStr = cleanInput.substringAfter("=")
            val rSq = rSqStr.toDoubleOrNull() ?: 25.0
            val r = sqrt(rSq)
            val details = mapOf(
                "Classification" to "Circle",
                "Center" to "(0.0, 0.0)",
                "Radius" to String.format(java.util.Locale.US, "%.2f", r),
                "Area" to String.format(java.util.Locale.US, "%.4f", PI * r * r),
                "Circumference" to String.format(java.util.Locale.US, "%.4f", 2 * PI * r),
                "Foci" to "None (Single Center Point)",
                "Eccentricity" to "0.0"
            )
            return EquationAnalysis(input, RecognizedEquationType.Circle, details)
        }

        // Check for general Circle representation: (x-h)^2 + (y-k)^2 = r^2
        val circleRegex = Regex("\\(x-([0-9.]+)\\)\\^2\\+\\(y-([0-9.]+)\\)\\^2=([0-9.]+)")
        val matchCircle = circleRegex.matchEntire(cleanInput)
        if (matchCircle != null) {
            val h = matchCircle.groupValues[1].toDoubleOrNull() ?: 0.0
            val k = matchCircle.groupValues[2].toDoubleOrNull() ?: 0.0
            val rSq = matchCircle.groupValues[3].toDoubleOrNull() ?: 1.0
            val r = sqrt(rSq)
            val details = mapOf(
                "Classification" to "Circle",
                "Center" to "($h, $k)",
                "Radius" to String.format(java.util.Locale.US, "%.2f", r),
                "Area" to String.format(java.util.Locale.US, "%.4f", PI * r * r),
                "Circumference" to String.format(java.util.Locale.US, "%.4f", 2 * PI * r)
            )
            return EquationAnalysis(input, RecognizedEquationType.Circle, details)
        }

        // 2. Check for Ellipse implicit
        val ellipseRegex = Regex("x\\^2/([0-9.]+)\\+y\\^2/([0-9.]+)=1")
        val matchEllipse = ellipseRegex.matchEntire(cleanInput)
        if (matchEllipse != null) {
            val aSq = matchEllipse.groupValues[1].toDoubleOrNull() ?: 1.0
            val bSq = matchEllipse.groupValues[2].toDoubleOrNull() ?: 1.0
            val a = sqrt(aSq)
            val b = sqrt(bSq)
            val e = if (a > b) sqrt(1.0 - (bSq / aSq)) else sqrt(1.0 - (aSq / bSq))
            val details = mapOf(
                "Classification" to "Ellipse",
                "Center" to "(0.0, 0.0)",
                "Semi-major Axis a" to String.format(java.util.Locale.US, "%.2f", max(a, b)),
                "Semi-minor Axis b" to String.format(java.util.Locale.US, "%.2f", min(a, b)),
                "Eccentricity e" to String.format(java.util.Locale.US, "%.4f", e),
                "Area" to String.format(java.util.Locale.US, "%.4f", PI * a * b)
            )
            return EquationAnalysis(input, RecognizedEquationType.Ellipse, details)
        }

        // 3. Check for Hyperbola implicit
        val hyperbolaRegex = Regex("x\\^2/([0-9.]+)-y\\^2/([0-9.]+)=1")
        val matchHyperbola = hyperbolaRegex.matchEntire(cleanInput)
        if (matchHyperbola != null) {
            val aSq = matchHyperbola.groupValues[1].toDoubleOrNull() ?: 1.0
            val bSq = matchHyperbola.groupValues[2].toDoubleOrNull() ?: 1.0
            val a = sqrt(aSq)
            val b = sqrt(bSq)
            val e = sqrt(1.0 + (bSq / aSq))
            val details = mapOf(
                "Classification" to "Hyperbola",
                "Center" to "(0.0, 0.0)",
                "Transverse Axis a" to String.format(java.util.Locale.US, "%.2f", a),
                "Conjugate Axis b" to String.format(java.util.Locale.US, "%.2f", b),
                "Eccentricity e" to String.format(java.util.Locale.US, "%.4f", e),
                "Asymptotes" to "y = ± ${String.format(java.util.Locale.US, "%.2f", b/a)} * x"
            )
            return EquationAnalysis(input, RecognizedEquationType.Hyperbola, details)
        }

        // 4. Trigonometric Equations
        if (cleanInput.contains("sin") || cleanInput.contains("cos") || cleanInput.contains("tan")) {
            val roots = try {
                rootFinder.findRoots(preprocessedInput, -10.0, 10.0)
            } catch (e: Exception) {
                emptyList()
            }
            val extrema = try {
                functionAnalyzer.findExtrema(preprocessedInput, -10.0, 10.0)
            } catch (e: Exception) {
                emptyList()
            }
            val details = mapOf(
                "Classification" to "Trigonometric Expression",
                "Periodic" to "Yes (Typically 2π)",
                "Roots Count (-10 to 10)" to "${roots.size}",
                "Extrema Count (-10 to 10)" to "${extrema.size}",
                "Derivative Representation" to "Calculated numerically via Calculus mode"
            )
            return EquationAnalysis(input, RecognizedEquationType.Trigonometric, details, roots, extrema)
        }

        // 5. Linear and Quadratic Equations using evaluation-based fitting
        val evalY0 = evaluateAt(preprocessedInput, 0.0)
        val evalY1 = evaluateAt(preprocessedInput, 1.0)
        val evalYMinus1 = evaluateAt(preprocessedInput, -1.0)
        val evalY2 = evaluateAt(preprocessedInput, 2.0)
        val evalYMinus2 = evaluateAt(preprocessedInput, -2.0)

        if (evalY0 != null && evalY1 != null && evalYMinus1 != null && evalY2 != null && evalYMinus2 != null) {
            val c = evalY0
            val b = (evalY1 - evalYMinus1) / 2.0
            val a = (evalY1 + evalYMinus1 - 2.0 * c) / 2.0

            val modelVal2 = a * 4.0 + b * 2.0 + c
            val modelValMinus2 = a * 4.0 - b * 2.0 + c

            if (abs(evalY2 - modelVal2) < 1e-5 && abs(evalYMinus2 - modelValMinus2) < 1e-5) {
                if (abs(a) < 1e-5) {
                    val roots = if (abs(b) > 1e-9) listOf(-c / b) else emptyList()
                    val details = mapOf(
                        "Classification" to "Linear Equation",
                        "Slope (m)" to String.format(java.util.Locale.US, "%.4f", b),
                        "Y-Intercept (c)" to String.format(java.util.Locale.US, "%.4f", c),
                        "Derivative" to String.format(java.util.Locale.US, "%.4f", b),
                        "Roots (x-intercept)" to if (roots.isNotEmpty()) String.format(java.util.Locale.US, "%.4f", roots[0]) else "None",
                        "Axis of Symmetry" to "None (Linear)"
                    )
                    return EquationAnalysis(input, RecognizedEquationType.Linear, details, roots, emptyList())
                } else {
                    val disc = b * b - 4 * a * c
                    val roots = mutableListOf<Double>()
                    if (disc > 0) {
                        roots.add((-b - sqrt(disc)) / (2 * a))
                        roots.add((-b + sqrt(disc)) / (2 * a))
                    } else if (abs(disc) < 1e-9) {
                        roots.add(-b / (2 * a))
                    }
                    val vertexX = -b / (2 * a)
                    val vertexY = -disc / (4 * a)
                    val extrema = listOf(ExtremerPoint(vertexX, vertexY, isMaximum = a < 0))

                    val details = mapOf(
                        "Classification" to "Quadratic Equation",
                        "Coefficients" to "a=${String.format(java.util.Locale.US, "%.2f", a)}, b=${String.format(java.util.Locale.US, "%.2f", b)}, c=${String.format(java.util.Locale.US, "%.2f", c)}",
                        "Vertex" to "(${String.format(java.util.Locale.US, "%.4f", vertexX)}, ${String.format(java.util.Locale.US, "%.4f", vertexY)})",
                        "Axis of Symmetry" to "x = ${String.format(java.util.Locale.US, "%.4f", vertexX)}",
                        "Discriminant" to String.format(java.util.Locale.US, "%.4f", disc),
                        "Extremum" to "${if (a < 0) "Maximum" else "Minimum"} at vertex",
                        "Analytical Derivative" to "${String.format(java.util.Locale.US, "%.2f", 2 * a)}*x + ${String.format(java.util.Locale.US, "%.2f", b)}"
                    )
                    return EquationAnalysis(input, RecognizedEquationType.Quadratic, details, roots.sorted(), extrema)
                }
            }
        }

        val roots = try {
            rootFinder.findRoots(preprocessedInput, -10.0, 10.0)
        } catch (e: Exception) {
            emptyList()
        }
        val extrema = try {
            functionAnalyzer.findExtrema(preprocessedInput, -10.0, 10.0)
        } catch (e: Exception) {
            emptyList()
        }
        val details = mapOf(
            "Classification" to "General Curve / Function",
            "Roots Count (-10 to 10)" to "${roots.size}",
            "Extrema Count (-10 to 10)" to "${extrema.size}"
        )
        return EquationAnalysis(input, RecognizedEquationType.General, details, roots, extrema)
    }

    private fun evaluateAt(expression: String, x: Double): Double? {
        val result = calculatorEngine.evaluate(expression, false, mapOf("x" to x))
        return if (result is com.example.core.util.Result.Success) {
            val y = result.data
            if (y.isNaN() || y.isInfinite()) null else y
        } else null
    }
}
