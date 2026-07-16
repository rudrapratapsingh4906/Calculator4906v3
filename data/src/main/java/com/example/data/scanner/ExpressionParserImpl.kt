package com.example.data.scanner

import com.example.domain.scanner.ExpressionParser
import com.example.domain.math.CalculatorEngine
import com.example.core.util.Result

class ExpressionParserImpl(
    private val calculatorEngine: CalculatorEngine
) : ExpressionParser {
    override fun parse(expression: String): String {
        return try {
            if (expression.contains("=")) {
                solveEquation(expression)
            } else {
                evaluateExpression(expression)
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun evaluateExpression(expression: String): String {
        val result = calculatorEngine.evaluate(expression, false)
        return when (result) {
            is Result.Success -> {
                "Expression: $expression\n\nFinal Answer: ${result.data}"
            }
            is Result.Error -> "Error: Invalid expression"
        }
    }

    private fun solveEquation(expression: String): String {
        // Simple linear equation solver for: ax + b = c -> x = (c - b) / a
        // Example: 2x + 3 = 11
        
        val parts = expression.split("=")
        if (parts.size != 2) return "Error: Invalid equation"
        
        val left = parts[0].trim()
        val right = parts[1].trim()
        
        // This is simplified, just to demonstrate the flow. 
        // Real implementation would parse the expression tree.
        
        return """
            Original Equation: $expression
            
            Step 1: Simplify both sides
            Step 2: Isolate the variable x
            Step 3: Solve for x
            
            Final Answer: x = ...
        """.trimIndent()
    }
}
