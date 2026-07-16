package com.example.domain.scanner

interface ExpressionValidator {
    fun validate(expression: String): Boolean
}
