package com.example.data.scanner

import com.example.domain.scanner.ExpressionValidator

class ExpressionValidatorImpl : ExpressionValidator {
    override fun validate(expression: String): Boolean {
        if (expression.isBlank()) return false
        
        // Balanced parentheses
        val openParens = expression.count { it == '(' }
        val closeParens = expression.count { it == ')' }
        if (openParens != closeParens) return false
        
        // Illegal characters (only allow digits, math operators, parens, letters for functions, '.', '=')
        val allowedChars = "0123456789+-*/%^()x.=sinco tanlogln!eπ"
        if (expression.any { it !in allowedChars && !it.isWhitespace() }) return false
        
        return true
    }
}
