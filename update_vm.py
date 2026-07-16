import re

with open("feature/src/main/java/com/example/feature/calculator/CalculatorViewModel.kt", "r") as f:
    content = f.read()

# We need to insert text at cursor position and evaluate in real-time.
# Let's write a helper method to update expression, cursor, and live result.
helper = """
    private fun updateExpressionAndEvaluate(newExpression: String, newCursor: Int) {
        val expr = newExpression
        val cursor = newCursor.coerceIn(0, expr.length)
        _state.update { it.copy(currentExpression = expr, cursorPosition = cursor, error = null) }
        
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
"""

content = content.replace("    fun onEvent(event: CalculatorEvent) {", helper + "\n    fun onEvent(event: CalculatorEvent) {")

old_input_char = """            is CalculatorEvent.InputChar -> {
                _state.update { it.copy(
                    currentExpression = it.currentExpression + event.char,
                    error = null
                ) }
            }
            is CalculatorEvent.InputString -> {
                _state.update { it.copy(
                    currentExpression = it.currentExpression + event.str,
                    error = null
                ) }
            }"""

new_input_char = """            is CalculatorEvent.InputChar -> {
                val current = _state.value.currentExpression
                val pos = _state.value.cursorPosition
                val newExpr = current.substring(0, pos) + event.char + current.substring(pos)
                updateExpressionAndEvaluate(newExpr, pos + 1)
            }
            is CalculatorEvent.InputString -> {
                val current = _state.value.currentExpression
                val pos = _state.value.cursorPosition
                val newExpr = current.substring(0, pos) + event.str + current.substring(pos)
                updateExpressionAndEvaluate(newExpr, pos + event.str.length)
            }"""

content = content.replace(old_input_char, new_input_char)

old_clear = """            is CalculatorEvent.Clear -> {
                _state.update { it.copy(currentExpression = "", result = "", error = null) }
            }"""
new_clear = """            is CalculatorEvent.Clear -> {
                _state.update { it.copy(currentExpression = "", result = "", liveResult = "", cursorPosition = 0, error = null) }
            }"""
content = content.replace(old_clear, new_clear)

old_delete = """            is CalculatorEvent.DeleteLast -> {
                if (_state.value.currentExpression.isNotEmpty()) {
                    _state.update { 
                        it.copy(
                            currentExpression = it.currentExpression.dropLast(1),
                            error = null
                        ) 
                    }
                }
            }"""
new_delete = """            is CalculatorEvent.DeleteLast -> {
                val current = _state.value.currentExpression
                val pos = _state.value.cursorPosition
                if (current.isNotEmpty() && pos > 0) {
                    val newExpr = current.substring(0, pos - 1) + current.substring(pos)
                    updateExpressionAndEvaluate(newExpr, pos - 1)
                }
            }"""
content = content.replace(old_delete, new_delete)

old_load = """            is CalculatorEvent.LoadCalculation -> {
                _state.update { it.copy(
                    currentExpression = event.calculation.expression,
                    result = event.calculation.result,
                    showHistory = false
                ) }
            }"""
new_load = """            is CalculatorEvent.LoadCalculation -> {
                val expr = event.calculation.expression
                _state.update { it.copy(
                    currentExpression = expr,
                    result = event.calculation.result,
                    liveResult = event.calculation.result,
                    cursorPosition = expr.length,
                    showHistory = false
                ) }
            }"""
content = content.replace(old_load, new_load)

old_mem_recall = """            is CalculatorEvent.MemoryRecall -> {
                val mem = _state.value.memoryValue
                val memStr = if (mem == mem.toLong().toDouble()) mem.toLong().toString() else mem.toString()
                _state.update { it.copy(currentExpression = it.currentExpression + memStr, error = null) }
            }"""
new_mem_recall = """            is CalculatorEvent.MemoryRecall -> {
                val mem = _state.value.memoryValue
                val memStr = if (mem == mem.toLong().toDouble()) mem.toLong().toString() else mem.toString()
                val current = _state.value.currentExpression
                val pos = _state.value.cursorPosition
                val newExpr = current.substring(0, pos) + memStr + current.substring(pos)
                updateExpressionAndEvaluate(newExpr, pos + memStr.length)
            }"""
content = content.replace(old_mem_recall, new_mem_recall)


# MemoryAdd/Subtract should also check liveResult
old_mem_add = """            is CalculatorEvent.MemoryAdd -> {
                val currentVal = _state.value.result.toDoubleOrNull() ?: 0.0
                _state.update { it.copy(memoryValue = it.memoryValue + currentVal) }
            }"""
new_mem_add = """            is CalculatorEvent.MemoryAdd -> {
                val currentVal = _state.value.liveResult.toDoubleOrNull() ?: _state.value.result.toDoubleOrNull() ?: 0.0
                _state.update { it.copy(memoryValue = it.memoryValue + currentVal) }
            }"""
content = content.replace(old_mem_add, new_mem_add)

old_mem_sub = """            is CalculatorEvent.MemorySubtract -> {
                val currentVal = _state.value.result.toDoubleOrNull() ?: 0.0
                _state.update { it.copy(memoryValue = it.memoryValue - currentVal) }
            }"""
new_mem_sub = """            is CalculatorEvent.MemorySubtract -> {
                val currentVal = _state.value.liveResult.toDoubleOrNull() ?: _state.value.result.toDoubleOrNull() ?: 0.0
                _state.update { it.copy(memoryValue = it.memoryValue - currentVal) }
            }"""
content = content.replace(old_mem_sub, new_mem_sub)

# Angle mode change
old_angle = """            is CalculatorEvent.ToggleAngleMode -> {
                _state.update { it.copy(isDegreeMode = !it.isDegreeMode) }
            }"""
new_angle = """            is CalculatorEvent.ToggleAngleMode -> {
                _state.update { it.copy(isDegreeMode = !it.isDegreeMode) }
                updateExpressionAndEvaluate(_state.value.currentExpression, _state.value.cursorPosition)
            }"""
content = content.replace(old_angle, new_angle)


# SetCursorPosition
cursor_event = """            is CalculatorEvent.SetCursorPosition -> {
                _state.update { it.copy(cursorPosition = event.position.coerceIn(0, it.currentExpression.length)) }
            }
"""
content = content.replace("            is CalculatorEvent.DismissError -> {", cursor_event + "            is CalculatorEvent.DismissError -> {")


# Update calculate() function
old_calculate = """    private fun calculate() {
        val expression = _state.value.currentExpression
        val isDegreeMode = _state.value.isDegreeMode
        if (expression.isBlank()) return
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
                    val errorMessage = when (result.error) {
                        is AppError.Calculation.DivideByZero -> "Cannot divide by zero"
                        is AppError.Calculation.InvalidExpression -> "Invalid expression"
                        is AppError.Calculation.Unknown -> (result.error as AppError.Calculation.Unknown).message
                        else -> "Unknown error"
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
    }"""

new_calculate = """    private fun calculate() {
        val expression = _state.value.currentExpression
        val isDegreeMode = _state.value.isDegreeMode
        if (expression.isBlank()) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = calculateExpressionUseCase(expression, isDegreeMode)) {
                is Result.Success -> {
                    _state.update { 
                        it.copy(
                            result = result.data.result,
                            currentExpression = result.data.result,
                            cursorPosition = result.data.result.length,
                            liveResult = "",
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    val errorMessage = when (result.error) {
                        is AppError.Calculation.DivideByZero -> "Cannot divide by zero"
                        is AppError.Calculation.InvalidExpression -> "Invalid expression"
                        is AppError.Calculation.Unknown -> (result.error as AppError.Calculation.Unknown).message
                        else -> "Unknown error"
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
    }"""
content = content.replace(old_calculate, new_calculate)

# togglePositiveNegative
old_toggle = """    private fun togglePositiveNegative() {
        val expr = _state.value.currentExpression
        if (expr.isEmpty()) return
        val newExpr = if (expr.startsWith("-")) {
            expr.drop(1)
        } else if (expr.startsWith("(-") && expr.endsWith(")")) {
            expr.substring(2, expr.length - 1)
        } else {
            "(-$expr)"
        }
        _state.update { it.copy(currentExpression = newExpr) }
    }"""

new_toggle = """    private fun togglePositiveNegative() {
        val expr = _state.value.currentExpression
        if (expr.isEmpty()) return
        val newExpr = if (expr.startsWith("-")) {
            expr.drop(1)
        } else if (expr.startsWith("(-") && expr.endsWith(")")) {
            expr.substring(2, expr.length - 1)
        } else {
            "(-$expr)"
        }
        updateExpressionAndEvaluate(newExpr, newExpr.length)
    }"""
content = content.replace(old_toggle, new_toggle)

with open("feature/src/main/java/com/example/feature/calculator/CalculatorViewModel.kt", "w") as f:
    f.write(content)
