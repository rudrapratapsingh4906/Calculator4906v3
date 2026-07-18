package com.example.data.scanner

import com.example.domain.scanner.ExpressionParser
import com.example.domain.scanner.TextUnderstandingEngine
import com.example.domain.scanner.MathIntent
import com.example.domain.scanner.StepByStepSolver
import com.example.domain.math.CalculatorEngine
import com.example.domain.scanner.MathKnowledgeBase
import com.example.domain.scanner.MultiStepReasoningEngine
import com.example.domain.scanner.AITutorEngine
import com.example.domain.scanner.AITutorMode
import com.example.core.util.Result

class ExpressionParserImpl(
    private val calculatorEngine: CalculatorEngine
) : ExpressionParser {
    override fun parse(expression: String): String {
        return try {
            val understanding = TextUnderstandingEngine.process(expression)
            val conceptInfo = MathKnowledgeBase.classify(understanding.expression, understanding.category)
            
            if (understanding.intent == MathIntent.TUTOR) {
                val mode = when (understanding.extractedCommand) {
                    "hint" -> AITutorMode.HINT
                    "next_step" -> AITutorMode.NEXT_STEP
                    "formula" -> AITutorMode.FORMULA_FIRST
                    "theorem" -> AITutorMode.THEOREM_EXPLANATION
                    "mistake" -> AITutorMode.MISTAKE_DETECTION
                    "explain_why" -> AITutorMode.EXPLAIN_WHY
                    "another_method" -> AITutorMode.ANOTHER_METHOD
                    "answer_only" -> AITutorMode.ANSWER_ONLY
                    else -> AITutorMode.FULL_SOLUTION
                }
                return AITutorEngine.generateTutorResponse(
                    understanding.expression,
                    conceptInfo,
                    mode,
                    userSolution = expression // Pass full text as possible user solution
                )
            }

            if (understanding.intent == MathIntent.PRACTICE) {
                val chapter = MathKnowledgeBase.findChapter(expression)
                val topic = MathKnowledgeBase.findTopic(expression)
                
                return AITutorEngine.generateTutorResponse(
                    expression,
                    conceptInfo.copy(chapter = chapter),
                    AITutorMode.PRACTICE_SESSION,
                    userSolution = topic // Abuse userSolution to pass topic for now or just let AITutorEngine handle it
                )
            }
            
            val reasoningHeader = MultiStepReasoningEngine.generateReasoning(understanding.expression, conceptInfo)
            
            val solverResult = when (understanding.intent) {
                MathIntent.SOLVE -> evaluateExpression(understanding.expression)
                MathIntent.EVALUATE -> evaluateExpression(understanding.expression)
                MathIntent.SIMPLIFY -> {
                    if (understanding.extractedCommand == "factor") {
                        StepByStepSolver.factorSteps(understanding.expression, calculatorEngine)
                    } else {
                        StepByStepSolver.simplifySteps(understanding.expression, calculatorEngine)
                    }
                }
                MathIntent.DIFFERENTIATE -> StepByStepSolver.differentiateSteps(understanding.expression, calculatorEngine)
                MathIntent.INTEGRATE -> StepByStepSolver.integrateSteps(understanding.expression, calculatorEngine)
                MathIntent.MATRIX -> StepByStepSolver.matrixSteps(understanding.expression)
                MathIntent.VECTOR -> StepByStepSolver.vectorSteps(understanding.expression)
                MathIntent.COMPLEX -> StepByStepSolver.complexSteps(understanding.expression)
                MathIntent.STATISTICS -> StepByStepSolver.statisticsSteps(understanding.expression)
                MathIntent.GEOMETRY -> StepByStepSolver.geometrySteps(understanding.expression)
                else -> {
                    when (understanding.category) {
                        com.example.domain.scanner.MathCategory.GEOMETRY -> StepByStepSolver.geometrySteps(understanding.expression)
                        com.example.domain.scanner.MathCategory.MATRIX -> StepByStepSolver.matrixSteps(understanding.expression)
                        com.example.domain.scanner.MathCategory.COMPLEX -> StepByStepSolver.complexSteps(understanding.expression)
                        com.example.domain.scanner.MathCategory.STATISTICS -> StepByStepSolver.statisticsSteps(understanding.expression)
                        else -> evaluateExpression(understanding.expression)
                    }
                }
            }
            
            reasoningHeader + solverResult
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun evaluateExpression(expression: String): String {
        return StepByStepSolver.evaluateSteps(expression, calculatorEngine)
    }
}
