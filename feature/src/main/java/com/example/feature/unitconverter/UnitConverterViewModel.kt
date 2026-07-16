package com.example.feature.unitconverter

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.domain.model.ConversionCategory
import com.example.domain.model.ConversionUnit
import com.example.domain.usecase.ConvertUnitUseCase
import com.example.domain.usecase.GetConversionUnitsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class UnitConverterViewModel(
    private val convertUnitUseCase: ConvertUnitUseCase,
    private val getConversionUnitsUseCase: GetConversionUnitsUseCase,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("unit_converter_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(UnitConverterState())
    val state: StateFlow<UnitConverterState> = _state.asStateFlow()

    private val allUnits: List<ConversionUnit> = getConversionUnitsUseCase()

    init {
        // Load saved category or default to LENGTH
        val savedCategoryStr = prefs.getString("last_category", ConversionCategory.LENGTH.name)
        val initialCategory = runCatching { ConversionCategory.valueOf(savedCategoryStr ?: "") }.getOrDefault(ConversionCategory.LENGTH)
        
        loadCategory(initialCategory)
    }

    fun onEvent(event: UnitConverterEvent) {
        when (event) {
            is UnitConverterEvent.SelectCategory -> {
                prefs.edit().putString("last_category", event.category.name).apply()
                loadCategory(event.category)
            }
            is UnitConverterEvent.SelectFromUnit -> {
                prefs.edit().putString("from_unit_${_state.value.selectedCategory.name}", event.unit.id).apply()
                _state.update { it.copy(fromUnit = event.unit) }
                calculate()
            }
            is UnitConverterEvent.SelectToUnit -> {
                prefs.edit().putString("to_unit_${_state.value.selectedCategory.name}", event.unit.id).apply()
                _state.update { it.copy(toUnit = event.unit) }
                calculate()
            }
            is UnitConverterEvent.InputValueChange -> {
                _state.update { it.copy(inputValue = event.value) }
                calculate()
            }
            UnitConverterEvent.SwapUnits -> {
                val currentFrom = _state.value.fromUnit
                val currentTo = _state.value.toUnit
                if (currentFrom != null && currentTo != null) {
                    prefs.edit()
                        .putString("from_unit_${_state.value.selectedCategory.name}", currentTo.id)
                        .putString("to_unit_${_state.value.selectedCategory.name}", currentFrom.id)
                        .apply()
                    _state.update { it.copy(fromUnit = currentTo, toUnit = currentFrom) }
                    calculate()
                }
            }
            UnitConverterEvent.ClearInput -> {
                _state.update { it.copy(inputValue = "", resultValue = "") }
            }
        }
    }

    private fun loadCategory(category: ConversionCategory) {
        val categoryUnits = allUnits.filter { it.category == category }
        
        val savedFromId = prefs.getString("from_unit_${category.name}", null)
        val savedToId = prefs.getString("to_unit_${category.name}", null)

        val fromUnit = categoryUnits.find { it.id == savedFromId } ?: categoryUnits.firstOrNull()
        val toUnit = categoryUnits.find { it.id == savedToId } ?: categoryUnits.getOrNull(1) ?: categoryUnits.firstOrNull()

        _state.update {
            it.copy(
                selectedCategory = category,
                units = categoryUnits,
                fromUnit = fromUnit,
                toUnit = toUnit,
                inputValue = "",
                resultValue = "",
                error = null
            )
        }
    }

    private fun calculate() {
        val currentInput = _state.value.inputValue
        val from = _state.value.fromUnit
        val to = _state.value.toUnit

        if (currentInput.isEmpty() || currentInput == "-" || currentInput == ".") {
            _state.update { it.copy(resultValue = "", error = null) }
            return
        }

        val value = currentInput.toDoubleOrNull()
        if (value == null) {
            _state.update { it.copy(resultValue = "", error = "Invalid number") }
            return
        }

        if (from == null || to == null) {
            _state.update { it.copy(resultValue = "", error = "Units not selected") }
            return
        }

        try {
            val result = convertUnitUseCase(value, from, to)
            _state.update { it.copy(resultValue = formatResult(result), error = null) }
        } catch (e: Exception) {
            _state.update { it.copy(resultValue = "", error = "Conversion error") }
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        
        // Handle very small or very large numbers with scientific notation or up to 8 decimal places
        if (value == 0.0) return "0"
        
        val absValue = Math.abs(value)
        if (absValue < 0.000001 || absValue > 1000000000) {
            val df = DecimalFormat("0.######E0")
            return df.format(value)
        }
        
        val bd = BigDecimal(value.toString()).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()
        return bd.toPlainString()
    }
}
