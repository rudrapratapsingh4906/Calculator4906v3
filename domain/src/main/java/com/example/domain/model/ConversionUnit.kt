package com.example.domain.model

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val category: ConversionCategory,
    val factorToBase: Double = 1.0 // multiplier to convert to a base unit
)
