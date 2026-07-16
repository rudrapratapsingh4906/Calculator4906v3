package com.example.domain.usecase

import com.example.domain.repository.GraphRepository
import com.example.domain.math.CalculatorEngine
import com.example.domain.model.GraphHistoryItem

class PlotGraphUseCase(
    private val repository: GraphRepository,
    private val calculatorEngine: CalculatorEngine = CalculatorEngine()
) {
    // Logic to prepare points for plotting
    fun plot(expression: String, range: ClosedFloatingPointRange<Double>): List<Pair<Double, Double>> {
        val points = mutableListOf<Pair<Double, Double>>()
        val step = 0.1
        var x = range.start
        while (x <= range.endInclusive) {
            val formattedX = String.format(java.util.Locale.US, "%.4f", x)
            val evalExpr = expression.replace("x", "($formattedX)").replace("X", "($formattedX)")
            val result = calculatorEngine.evaluate(evalExpr, false)
            if (result is com.example.core.util.Result.Success) {
                val y = result.data
                if (!y.isNaN() && !y.isInfinite()) {
                    points.add(x to y)
                }
            }
            x += step
        }
        return points
    }

    fun saveToHistory(item: GraphHistoryItem) {
        repository.saveToHistory(item)
    }

    fun getHistory(): List<GraphHistoryItem> {
        return repository.getHistory()
    }

    fun clearHistory() {
        repository.clearHistory()
    }
}
