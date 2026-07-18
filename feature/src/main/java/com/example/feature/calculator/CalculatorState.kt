package com.example.feature.calculator

import com.example.domain.model.Calculation

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

data class CalculatorState(
    val currentExpression: String = "",
    val textFieldValue: TextFieldValue = TextFieldValue(),
    val result: String = "",
    val liveResult: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val history: List<Calculation> = emptyList(),
    val isDegreeMode: Boolean = true,
    val showHistory: Boolean = false,
    val memoryValue: Double = 0.0,
    val theme: String = "Default",
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val themeMode: String = "System", // Light, Dark, System
    val orientationLock: Boolean = false,
    val cursorPosition: Int = 0,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val backgroundImageUri: String? = null,
    val backgroundOpacity: Float = 1.0f
)
