package com.example.data.scanner

import com.example.domain.scanner.MathSolverRepository
import com.example.domain.scanner.ExpressionParser
import com.example.domain.scanner.ExpressionValidator

class MathSolverRepositoryImpl(
    private val parser: ExpressionParser,
    private val validator: ExpressionValidator
) : MathSolverRepository {
    override fun solve(expression: String): String {
        if (!validator.validate(expression)) {
            return "Error: Invalid expression"
        }
        return parser.parse(expression)
    }
}
