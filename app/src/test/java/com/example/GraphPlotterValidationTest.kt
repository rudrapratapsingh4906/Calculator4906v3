package com.example

import com.example.domain.math.CalculatorEngine
import com.example.domain.repository.GraphRepository
import com.example.domain.usecase.PlotGraphUseCase
import com.example.feature.advancedfeatures.ui.GraphPlotterViewModel
import com.example.feature.advancedfeatures.ui.PlottedGraph
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

class FakeGraphRepository : GraphRepository {
    private val historyList = mutableListOf<com.example.domain.model.GraphHistoryItem>()
    override fun saveToHistory(item: com.example.domain.model.GraphHistoryItem) {
        historyList.add(item)
    }
    override fun getHistory(): List<com.example.domain.model.GraphHistoryItem> = historyList
    override fun clearHistory() {
        historyList.clear()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class GraphPlotterValidationTest {

    private val calculatorEngine = CalculatorEngine()
    private val graphRepository = FakeGraphRepository()
    private val plotGraphUseCase = PlotGraphUseCase(graphRepository, calculatorEngine)
    private val viewModel = GraphPlotterViewModel(plotGraphUseCase, calculatorEngine)

    @Test
    fun `test conic circle verification`() {
        // Test plotting a Circle with h=0, k=0, r=5
        viewModel.addConicCircle(0.0, 0.0, 5.0)
        
        val state = viewModel.state.value
        assertEquals(1, state.graphs.size)
        assertTrue(state.graphs[0] is PlottedGraph.ConicCircle)
        
        val points = state.points[0]
        assertTrue("Expected at least 300 points", points.size >= 300)
        
        // Verify standard points on circle: (5,0), (0,5), (-5,0), (0,-5)
        // Since we evaluate parametrically, let's verify distance from center is always 5.0
        points.forEach { (x, y) ->
            val distance = Math.sqrt(x * x + y * y)
            assertTrue("Distance was $distance", abs(distance - 5.0) < 1e-4)
        }
    }

    @Test
    fun `test direct calculator engine evaluation`() {
        val resultX = calculatorEngine.evaluate("cos(t)", false, mapOf("t" to 0.0))
        val resultY = calculatorEngine.evaluate("sin(t)", false, mapOf("t" to 0.0))
        println("Result of cos(t) at t=0: $resultX")
        println("Result of sin(t) at t=0: $resultY")
        
        if (resultX is com.example.core.util.Result.Success && resultY is com.example.core.util.Result.Success) {
            assertEquals(1.0, resultX.data, 1e-4)
            assertEquals(0.0, resultY.data, 1e-4)
        } else {
            assertTrue("Evaluation failed!", false)
        }
    }

    @Test
    fun `test parametric graph verification`() {
        // Test plotting x=cos(t), y=sin(t)
        viewModel.addParametric("cos(t)", "sin(t)")
        
        val state = viewModel.state.value
        assertEquals(1, state.graphs.size)
        assertTrue(state.graphs[0] is PlottedGraph.Parametric)
        
        val points = state.points[0]
        assertTrue("Expected at least 300 points", points.size >= 300)
        
        // This is a unit circle, so distance from origin should be 1.0
        points.forEachIndexed { index, (x, y) ->
            if (x.isFinite() && y.isFinite()) {
                val distance = Math.sqrt(x * x + y * y)
                if (abs(distance - 1.0) >= 1e-4) {
                    println("Failure at index $index: t was roughly ${-2 * Math.PI + index * (4 * Math.PI / 300.0)}, x=$x, y=$y, distance=$distance")
                }
                assertTrue("Distance was $distance", abs(distance - 1.0) < 1e-4)
            }
        }
    }

    @Test
    fun `test polar graph verification`() {
        // Test plotting r=sin(theta)
        viewModel.addPolar("sin(theta)")
        
        val state = viewModel.state.value
        assertEquals(1, state.graphs.size)
        assertTrue(state.graphs[0] is PlottedGraph.Polar)
        
        val points = state.points[0]
        assertTrue("Expected at least 300 points", points.size >= 300)
        
        // For r = sin(theta), the center is (0, 0.5) and radius is 0.5
        // Distance from (0, 0.5) should be 0.5
        points.forEach { (x, y) ->
            if (x.isFinite() && y.isFinite()) {
                val dx = x - 0.0
                val dy = y - 0.5
                val distance = Math.sqrt(dx * dx + dy * dy)
                assertTrue("Distance to center was $distance", abs(distance - 0.5) < 1e-4)
            }
        }
    }

    @Test
    fun `test equation recognition and analysis for quadratic`() {
        viewModel.addExpression("x^2-4x+3")
        
        val state = viewModel.state.value
        assertEquals(1, state.graphs.size)
        
        val analysis = state.equationAnalysis[0]
        assertEquals(com.example.feature.advancedfeatures.ui.RecognizedEquationType.Quadratic, analysis.type)
        assertEquals("Quadratic Equation", analysis.details["Classification"])
        assertEquals("Minimum at vertex", analysis.details["Extremum"])
        assertEquals("x = 2.0000", analysis.details["Axis of Symmetry"])
    }

    @Test
    fun `test equation recognition and analysis for implicit circle`() {
        viewModel.addExpression("x^2+y^2=25")
        
        val state = viewModel.state.value
        assertEquals(1, state.graphs.size)
        assertTrue(state.graphs[0] is PlottedGraph.ConicCircle)
        
        val analysis = state.equationAnalysis[0]
        assertEquals(com.example.feature.advancedfeatures.ui.RecognizedEquationType.Circle, analysis.type)
        assertEquals("(0.0, 0.0)", analysis.details["Center"])
        assertEquals("5.00", analysis.details["Radius"])
    }

    @Test
    fun `test equation recognition and analysis for trigonometric`() {
        viewModel.addExpression("sin(x)+cos(x)")
        
        val state = viewModel.state.value
        assertEquals(1, state.graphs.size)
        
        val analysis = state.equationAnalysis[0]
        assertEquals(com.example.feature.advancedfeatures.ui.RecognizedEquationType.Trigonometric, analysis.type)
        assertEquals("Trigonometric Expression", analysis.details["Classification"])
    }
}
