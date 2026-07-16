cat << 'INNER_EOF' > feature/src/main/java/com/example/feature/calculator/CalculatorState.kt
package com.example.feature.calculator

import com.example.domain.model.Calculation

data class CalculatorState(
    val currentExpression: String = "",
    val result: String = "",
    val error: String? = null,
    val history: List<Calculation> = emptyList(),
    val isDegreeMode: Boolean = true,
    val showHistory: Boolean = false,
    val theme: String = "Default",
    val orientationLock: Boolean = false
)
INNER_EOF
