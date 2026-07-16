package com.example.feature.calculator

import com.example.domain.model.Calculation
import androidx.compose.ui.text.input.TextFieldValue

sealed class CalculatorEvent {
    data class InputChar(val char: Char) : CalculatorEvent()
    data class InputString(val str: String) : CalculatorEvent()
    object Calculate : CalculatorEvent()
    object Clear : CalculatorEvent()
    object DeleteLast : CalculatorEvent()
    object TogglePositiveNegative : CalculatorEvent()
    object ToggleAngleMode : CalculatorEvent()
    object ToggleHistory : CalculatorEvent()
    data class LoadCalculation(val calculation: Calculation) : CalculatorEvent()
    object ClearHistory : CalculatorEvent()
    object MemoryClear : CalculatorEvent()
    object MemoryRecall : CalculatorEvent()
    object MemoryAdd : CalculatorEvent()
    object MemorySubtract : CalculatorEvent()
    data class SetTheme(val theme: String) : CalculatorEvent()
    data class SetThemeMode(val themeMode: String) : CalculatorEvent()
    data class SetVibrationEnabled(val enabled: Boolean) : CalculatorEvent()
    data class SetSoundEnabled(val enabled: Boolean) : CalculatorEvent()
    data class SetOrientationLock(val locked: Boolean) : CalculatorEvent()
    data class SetBackgroundImageUri(val uri: String?) : CalculatorEvent()
    data class SetBackgroundOpacity(val opacity: Float) : CalculatorEvent()
    data class SetCursorPosition(val position: Int) : CalculatorEvent()
    data class SetSelection(val start: Int, val end: Int) : CalculatorEvent()
    data class UpdateExpression(val value: TextFieldValue) : CalculatorEvent()
    object NavigateToScanner : CalculatorEvent()
    object DismissError : CalculatorEvent()
}
