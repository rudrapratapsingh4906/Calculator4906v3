package com.example.feature.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.text.input.TextFieldValue
import com.example.core.util.AppError
import com.example.core.util.Result
import com.example.domain.usecase.CalculateExpressionUseCase
import com.example.domain.usecase.GetHistoryUseCase
import com.example.domain.usecase.ClearHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val calculateExpressionUseCase: CalculateExpressionUseCase,
    private val getHistoryUseCase: GetHistoryUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val settingsRepository: com.example.domain.repository.SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    init {
        observeSettings()
        observeHistory()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.themeFlow.collect { theme ->
                _state.update { it.copy(theme = theme) }
            }
        }
        viewModelScope.launch {
            settingsRepository.themeModeFlow.collect { themeMode ->
                _state.update { it.copy(themeMode = themeMode) }
            }
        }
        viewModelScope.launch {
            settingsRepository.vibrationEnabledFlow.collect { enabled ->
                _state.update { it.copy(isVibrationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.soundEnabledFlow.collect { enabled ->
                _state.update { it.copy(isSoundEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.orientationLockFlow.collect { locked ->
                _state.update { it.copy(orientationLock = locked) }
            }
        }
        viewModelScope.launch {
            settingsRepository.backgroundImageUriFlow.collect { uri ->
                _state.update { it.copy(backgroundImageUri = uri) }
            }
        }
        viewModelScope.launch {
            settingsRepository.backgroundOpacityFlow.collect { opacity ->
                _state.update { it.copy(backgroundOpacity = opacity) }
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            getHistoryUseCase().collect { history ->
                _state.update { it.copy(history = history) }
            }
        }
    }


    private fun updateExpressionAndEvaluate(newExpression: String, start: Int, end: Int) {
        val expr = newExpression
        val startCoerced = start.coerceIn(0, expr.length)
        val endCoerced = end.coerceIn(0, expr.length)
        _state.update { it.copy(
            currentExpression = expr,
            cursorPosition = endCoerced,
            selectionStart = startCoerced,
            selectionEnd = endCoerced,
            result = "",
            error = null
        ) }
        
        if (expr.isBlank()) {
            _state.update { it.copy(liveResult = "") }
            return
        }
        
        viewModelScope.launch {
            val isDegreeMode = _state.value.isDegreeMode
            when (val result = calculateExpressionUseCase.evaluateOnly(expr, isDegreeMode)) {
                is Result.Success -> {
                    _state.update { it.copy(liveResult = result.data) }
                }
                is Result.Error -> {
                    _state.update { it.copy(liveResult = "") }
                }
            }
        }
    }

    fun onEvent(event: CalculatorEvent) {
        when (event) {
            is CalculatorEvent.UpdateExpression -> {
                updateExpressionAndEvaluate(event.value.text, event.value.selection.start, event.value.selection.end)
            }
            is CalculatorEvent.InputChar -> {
                val hasResult = _state.value.result.isNotEmpty()
                val resultVal = _state.value.result
                val isOp = isBinaryOperator(event.char)
                val isValidForChaining = hasResult && !resultVal.contains("Error") && !resultVal.contains("Invalid") && !resultVal.any { it.isLetter() && it != 'e' && it != 'E' }

                if (hasResult) {
                    if (isOp && isValidForChaining) {
                        val newExpr = resultVal + event.char
                        updateExpressionAndEvaluate(newExpr, newExpr.length, newExpr.length)
                    } else {
                        if (event.char == '×' || event.char == '÷' || event.char == '%' || event.char == '^') return
                        val newExpr = event.char.toString()
                        updateExpressionAndEvaluate(newExpr, 1, 1)
                    }
                } else {
                    val current = _state.value.currentExpression
                    val pos = _state.value.cursorPosition
                    val start = _state.value.selectionStart
                    val end = _state.value.selectionEnd

                    if (current.isEmpty() && (event.char == '×' || event.char == '÷' || event.char == '%' || event.char == '^')) return
                    if (event.char == '.' && !canAddDecimal(current, pos, start, end)) return

                    if (isOp && start == end) {
                        if (pos > 0 && isBinaryOperator(current[pos - 1])) {
                            val newExpr = current.substring(0, pos - 1) + event.char + current.substring(pos)
                            updateExpressionAndEvaluate(newExpr, pos, pos)
                        } else if (pos >= 3 && current.substring(maxOf(0, pos - 3), pos) == "mod") {
                            val newExpr = current.substring(0, pos - 3) + event.char + current.substring(pos)
                            updateExpressionAndEvaluate(newExpr, pos - 2, pos - 2)
                        } else if (pos < current.length && isBinaryOperator(current[pos])) {
                            val newExpr = current.substring(0, pos) + event.char + current.substring(pos + 1)
                            updateExpressionAndEvaluate(newExpr, pos + 1, pos + 1)
                        } else if (pos + 3 <= current.length && current.substring(pos, minOf(current.length, pos + 3)) == "mod") {
                            val newExpr = current.substring(0, pos) + event.char + current.substring(pos + 3)
                            updateExpressionAndEvaluate(newExpr, pos + 1, pos + 1)
                        } else {
                            val newExpr = current.substring(0, pos) + event.char + current.substring(pos)
                            updateExpressionAndEvaluate(newExpr, pos + 1, pos + 1)
                        }
                    } else {
                        if (start != end) {
                            val min = minOf(start, end)
                            val max = maxOf(start, end)
                            val newExpr = current.substring(0, min) + event.char + current.substring(max)
                            updateExpressionAndEvaluate(newExpr, min + 1, min + 1)
                        } else {
                            val newExpr = current.substring(0, pos) + event.char + current.substring(pos)
                            updateExpressionAndEvaluate(newExpr, pos + 1, pos + 1)
                        }
                    }
                }
            }
            is CalculatorEvent.InputString -> {
                val hasResult = _state.value.result.isNotEmpty()
                val resultVal = _state.value.result
                val isOp = event.str == "mod" || event.str.startsWith("+") || event.str.startsWith("-") || event.str.startsWith("×") || event.str.startsWith("÷") || event.str.startsWith("^") || event.str.startsWith("%")
                val isValidForChaining = hasResult && !resultVal.contains("Error") && !resultVal.contains("Invalid") && !resultVal.any { it.isLetter() && it != 'e' && it != 'E' }

                if (hasResult) {
                    if (isOp && isValidForChaining) {
                        val newExpr = resultVal + event.str
                        updateExpressionAndEvaluate(newExpr, newExpr.length, newExpr.length)
                    } else {
                        if (event.str == "mod" || event.str == "^" || event.str == "×" || event.str == "÷" || event.str == "%") return
                        val newExpr = event.str
                        updateExpressionAndEvaluate(newExpr, newExpr.length, newExpr.length)
                    }
                } else {
                    val current = _state.value.currentExpression
                    val pos = _state.value.cursorPosition
                    val start = _state.value.selectionStart
                    val end = _state.value.selectionEnd

                    if (current.isEmpty() && (event.str == "mod" || event.str == "^" || event.str == "×" || event.str == "÷" || event.str == "%")) return

                    if (isOp && start == end) {
                        if (pos > 0 && isBinaryOperator(current[pos - 1])) {
                            val newExpr = current.substring(0, pos - 1) + event.str + current.substring(pos)
                            updateExpressionAndEvaluate(newExpr, pos - 1 + event.str.length, pos - 1 + event.str.length)
                        } else if (pos >= 3 && current.substring(maxOf(0, pos - 3), pos) == "mod") {
                            val newExpr = current.substring(0, pos - 3) + event.str + current.substring(pos)
                            updateExpressionAndEvaluate(newExpr, pos - 3 + event.str.length, pos - 3 + event.str.length)
                        } else if (pos < current.length && isBinaryOperator(current[pos])) {
                            val newExpr = current.substring(0, pos) + event.str + current.substring(pos + 1)
                            updateExpressionAndEvaluate(newExpr, pos + event.str.length, pos + event.str.length)
                        } else if (pos + 3 <= current.length && current.substring(pos, minOf(current.length, pos + 3)) == "mod") {
                            val newExpr = current.substring(0, pos) + event.str + current.substring(pos + 3)
                            updateExpressionAndEvaluate(newExpr, pos + event.str.length, pos + event.str.length)
                        } else {
                            val newExpr = current.substring(0, pos) + event.str + current.substring(pos)
                            updateExpressionAndEvaluate(newExpr, pos + event.str.length, pos + event.str.length)
                        }
                    } else {
                        if (start != end) {
                            val min = minOf(start, end)
                            val max = maxOf(start, end)
                            val newExpr = current.substring(0, min) + event.str + current.substring(max)
                            updateExpressionAndEvaluate(newExpr, min + event.str.length, min + event.str.length)
                        } else {
                            val newExpr = current.substring(0, pos) + event.str + current.substring(pos)
                            updateExpressionAndEvaluate(newExpr, pos + event.str.length, pos + event.str.length)
                        }
                    }
                }
            }
            is CalculatorEvent.Clear -> {
                _state.update { it.copy(
                    currentExpression = "",
                    result = "",
                    liveResult = "",
                    cursorPosition = 0,
                    selectionStart = 0,
                    selectionEnd = 0,
                    error = null
                ) }
            }
            is CalculatorEvent.DeleteLast -> {
                val current = _state.value.currentExpression
                val start = _state.value.selectionStart
                val end = _state.value.selectionEnd
                if (start != end) {
                    val min = minOf(start, end)
                    val max = maxOf(start, end)
                    val newExpr = current.substring(0, min) + current.substring(max)
                    updateExpressionAndEvaluate(newExpr, min, min)
                } else {
                    val pos = _state.value.cursorPosition
                    if (current.isNotEmpty() && pos > 0) {
                        val newExpr = current.substring(0, pos - 1) + current.substring(pos)
                        updateExpressionAndEvaluate(newExpr, pos - 1, pos - 1)
                    }
                }
            }
            is CalculatorEvent.Calculate -> {
                calculate()
            }
            is CalculatorEvent.ClearHistory -> {
                viewModelScope.launch {
                    clearHistoryUseCase()
                }
            }
            is CalculatorEvent.ToggleHistory -> {
                _state.update { it.copy(showHistory = !it.showHistory) }
            }
            is CalculatorEvent.LoadCalculation -> {
                val expr = event.calculation.expression
                _state.update { it.copy(
                    currentExpression = expr,
                    result = event.calculation.result,
                    liveResult = event.calculation.result,
                    cursorPosition = expr.length,
                    selectionStart = expr.length,
                    selectionEnd = expr.length,
                    showHistory = false
                ) }
            }
            is CalculatorEvent.MemoryClear -> {
                _state.update { it.copy(memoryValue = 0.0) }
            }
            is CalculatorEvent.MemoryRecall -> {
                val mem = _state.value.memoryValue
                val memStr = if (mem == mem.toLong().toDouble()) mem.toLong().toString() else mem.toString()
                val hasResult = _state.value.result.isNotEmpty()
                if (hasResult) {
                    updateExpressionAndEvaluate(memStr, memStr.length, memStr.length)
                } else {
                    val current = _state.value.currentExpression
                    val start = _state.value.selectionStart
                    val end = _state.value.selectionEnd
                    if (start != end) {
                        val min = minOf(start, end)
                        val max = maxOf(start, end)
                        val newExpr = current.substring(0, min) + memStr + current.substring(max)
                        updateExpressionAndEvaluate(newExpr, min + memStr.length, min + memStr.length)
                    } else {
                        val pos = _state.value.cursorPosition
                        val newExpr = current.substring(0, pos) + memStr + current.substring(pos)
                        updateExpressionAndEvaluate(newExpr, pos + memStr.length, pos + memStr.length)
                    }
                }
            }
            is CalculatorEvent.MemoryAdd -> {
                val currentVal = _state.value.liveResult.toDoubleOrNull() ?: _state.value.result.toDoubleOrNull() ?: 0.0
                _state.update { it.copy(memoryValue = it.memoryValue + currentVal) }
            }
            is CalculatorEvent.MemorySubtract -> {
                val currentVal = _state.value.liveResult.toDoubleOrNull() ?: _state.value.result.toDoubleOrNull() ?: 0.0
                _state.update { it.copy(memoryValue = it.memoryValue - currentVal) }
            }
            is CalculatorEvent.SetCursorPosition -> {
                val pos = event.position.coerceIn(0, _state.value.currentExpression.length)
                _state.update { it.copy(
                    cursorPosition = pos,
                    selectionStart = pos,
                    selectionEnd = pos
                ) }
            }
            is CalculatorEvent.SetSelection -> {
                val start = event.start.coerceIn(0, _state.value.currentExpression.length)
                val end = event.end.coerceIn(0, _state.value.currentExpression.length)
                _state.update { it.copy(
                    cursorPosition = end,
                    selectionStart = start,
                    selectionEnd = end
                ) }
            }
            is CalculatorEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
            is CalculatorEvent.ToggleAngleMode -> {
                _state.update { it.copy(isDegreeMode = !it.isDegreeMode) }
                updateExpressionAndEvaluate(_state.value.currentExpression, _state.value.cursorPosition, _state.value.cursorPosition)
            }
            is CalculatorEvent.SetTheme -> {
                settingsRepository.setTheme(event.theme)
            }
            is CalculatorEvent.SetThemeMode -> {
                settingsRepository.setThemeMode(event.themeMode)
            }
            is CalculatorEvent.SetVibrationEnabled -> {
                settingsRepository.setVibrationEnabled(event.enabled)
            }
            is CalculatorEvent.SetSoundEnabled -> {
                settingsRepository.setSoundEnabled(event.enabled)
            }
            is CalculatorEvent.SetOrientationLock -> {
                settingsRepository.setOrientationLock(event.locked)
            }
            is CalculatorEvent.SetBackgroundImageUri -> {
                settingsRepository.setBackgroundImageUri(event.uri)
            }
            is CalculatorEvent.SetBackgroundOpacity -> {
                settingsRepository.setBackgroundOpacity(event.opacity)
            }
            is CalculatorEvent.TogglePositiveNegative -> {
                togglePositiveNegative()
            }
            is CalculatorEvent.NavigateToScanner -> {
                // Handle navigation here if needed, or leave blank if handled by UI
            }
        }
    }

    private fun isBinaryOperator(c: Char): Boolean = c == '+' || c == '-' || c == '×' || c == '÷' || c == '%' || c == '^'

    private fun canAddDecimal(expression: String, pos: Int, start: Int, end: Int): Boolean {
        val currentExpr = if (start != end) {
            expression.substring(0, minOf(start, end)) + expression.substring(maxOf(start, end))
        } else {
            expression
        }
        val currentPos = if (start != end) minOf(start, end) else pos
        var i = currentPos - 1
        while (i >= 0 && (currentExpr[i].isDigit() || currentExpr[i] == '.')) {
            if (currentExpr[i] == '.') return false
            i--
        }
        var j = currentPos
        while (j < currentExpr.length && (currentExpr[j].isDigit() || currentExpr[j] == '.')) {
            if (currentExpr[j] == '.') return false
            j++
        }
        return true
    }

    private fun togglePositiveNegative() {
        val hasResult = _state.value.result.isNotEmpty()
        val expr = if (hasResult) _state.value.result else _state.value.currentExpression
        if (expr.isEmpty() || expr.contains("Error") || expr.contains("Invalid")) return
        val newExpr = if (expr.startsWith("-")) {
            expr.drop(1)
        } else if (expr.startsWith("(-") && expr.endsWith(")")) {
            expr.substring(2, expr.length - 1)
        } else {
            "(-$expr)"
        }
        updateExpressionAndEvaluate(newExpr, newExpr.length, newExpr.length)
    }

    private fun calculate() {
        val expression = _state.value.currentExpression
        val isDegreeMode = _state.value.isDegreeMode
        if (expression.isBlank() || (expression.isNotEmpty() && (isBinaryOperator(expression.last()) || expression.endsWith("mod")))) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val result = calculateExpressionUseCase(expression, isDegreeMode)) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            result = result.data.result,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    val errorMessage = when (val err = result.error) {
                        is AppError.Calculation.DivideByZero -> "Cannot divide by zero"
                        is AppError.Calculation.InvalidExpression -> "Invalid expression"
                        is AppError.Calculation.Unknown -> err.message
                    }
                    _state.update { 
                        it.copy(
                            error = errorMessage,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
