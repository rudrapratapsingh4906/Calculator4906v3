package com.example

import com.example.domain.math.CalculatorEngine
import com.example.domain.scanner.StepByStepSolver
import org.junit.Assert.*
import org.junit.Test

class StepByStepSolverTest {
    @Test
    fun testLinearEquation() {
        val engine = CalculatorEngine()
        val steps = StepByStepSolver.solveEquationSteps("2*x + 3 = 11", engine)
        println(steps)
        assertTrue(steps.contains("Step 1"))
        assertTrue(steps.contains("x = 4"))
    }
    
    @Test
    fun testQuadraticEquation() {
        val engine = CalculatorEngine()
        val steps = StepByStepSolver.solveEquationSteps("x^2 - 4 = 0", engine)
        println(steps)
        assertTrue(steps.contains("Step 1"))
        assertTrue(steps.contains("x = 2") || steps.contains("x = -2"))
    }
}
