package com.example.feature.unitconverter

import com.example.domain.model.ConversionCategory
import com.example.domain.model.ConversionUnit

sealed class UnitConverterEvent {
    data class SelectCategory(val category: ConversionCategory) : UnitConverterEvent()
    data class SelectFromUnit(val unit: ConversionUnit) : UnitConverterEvent()
    data class SelectToUnit(val unit: ConversionUnit) : UnitConverterEvent()
    data class InputValueChange(val value: String) : UnitConverterEvent()
    object SwapUnits : UnitConverterEvent()
    object ClearInput : UnitConverterEvent()
}
