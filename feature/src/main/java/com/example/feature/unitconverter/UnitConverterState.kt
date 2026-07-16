package com.example.feature.unitconverter

import com.example.domain.model.ConversionCategory
import com.example.domain.model.ConversionUnit

data class UnitConverterState(
    val categories: List<ConversionCategory> = ConversionCategory.values().toList(),
    val selectedCategory: ConversionCategory = ConversionCategory.LENGTH,
    val units: List<ConversionUnit> = emptyList(),
    val fromUnit: ConversionUnit? = null,
    val toUnit: ConversionUnit? = null,
    val inputValue: String = "",
    val resultValue: String = "",
    val error: String? = null
)
