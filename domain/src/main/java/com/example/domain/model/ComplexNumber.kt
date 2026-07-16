package com.example.domain.model

data class ComplexNumber(
    val real: Double,
    val imaginary: Double
) {
    override fun toString(): String {
        val sign = if (imaginary >= 0) "+" else "-"
        return "$real $sign ${kotlin.math.abs(imaginary)}i"
    }
}
