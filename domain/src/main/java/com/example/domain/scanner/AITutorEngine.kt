package com.example.domain.scanner

import com.example.domain.math.*
import kotlin.math.*

enum class AITutorMode {
    HINT,
    NEXT_STEP,
    FULL_SOLUTION,
    FORMULA_FIRST,
    THEOREM_EXPLANATION,
    MISTAKE_DETECTION,
    PRACTICE_SESSION
}

object AITutorEngine {

    fun generateTutorResponse(
        expression: String,
        conceptInfo: MathKnowledgeBase.ConceptInfo,
        mode: AITutorMode,
        currentStepIndex: Int = 0,
        userSolution: String? = null
    ): String {
        val response = StringBuilder()
        response.append("🎓 AI Tutor Mode: ${mode.name.replace("_", " ")}\n")
        
        if (mode == AITutorMode.PRACTICE_SESSION) {
            val topic = userSolution // Passed from ExpressionParserImpl
            if (topic != null) {
                response.append("Let's practice some problems related to $topic!\n\n")
            } else {
                response.append("Let's practice some problems related to ${conceptInfo.chapter.name}!\n\n")
            }
            val questions = QuestionGeneratorEngine.generateQuestions(
                conceptInfo.chapter, 
                conceptInfo.level, 
                topic = topic,
                count = 1
            )
            val q = questions.first()
            response.append("📝 Problem: ${q.text}\n")
            response.append("💡 Initial Hint: ${q.hint ?: "Try applying ${conceptInfo.formulas.firstOrNull() ?: "basic concepts"}."}")
            return response.toString()
        }

        response.append("Concept: ${conceptInfo.conceptName} (${conceptInfo.level.name.replace("_", " ")})\n")
        
        val depth = when (conceptInfo.level) {
            MathLevel.CLASS_1_5 -> "Simple/Visual"
            MathLevel.CLASS_6_8 -> "Descriptive"
            MathLevel.CLASS_9_10 -> "Standard"
            MathLevel.CLASS_11_12 -> "Technical"
            MathLevel.JEE_MAIN -> "Strategic"
            MathLevel.JEE_ADVANCED -> "Analytical"
            MathLevel.OLYMPIAD -> "Formal/Rigorous"
        }
        response.append("Explanation Level: $depth\n\n")

        when (mode) {
            AITutorMode.PRACTICE_SESSION -> {
                // Already handled above
            }
            AITutorMode.HINT -> {
                val hint = getHint(conceptInfo, currentStepIndex)
                response.append("💡 Hint:\n$hint\n")
            }
            AITutorMode.NEXT_STEP -> {
                val steps = conceptInfo.primaryApproach
                if (currentStepIndex < steps.size) {
                    response.append("➡️ Next Step:\n${steps[currentStepIndex]}\n")
                    response.append("\n*Why this step?*\n${getDetailedReasoning(conceptInfo, currentStepIndex)}")
                } else {
                    response.append("You have reached the final step!")
                }
            }
            AITutorMode.FULL_SOLUTION -> {
                response.append(MultiStepReasoningEngine.generateReasoning(expression, conceptInfo))
            }
            AITutorMode.FORMULA_FIRST -> {
                response.append("📘 Key Formulas & Identities:\n")
                conceptInfo.formulas.forEach { response.append("• $it\n") }
                response.append("\nTry applying these to solve the problem.")
            }
            AITutorMode.THEOREM_EXPLANATION -> {
                response.append("📜 Theorem & Principle:\n")
                response.append(getTheoremExplanation(conceptInfo.conceptName))
            }
            AITutorMode.MISTAKE_DETECTION -> {
                if (userSolution != null) {
                    response.append(detectMistakes(userSolution, conceptInfo))
                } else {
                    response.append("Please provide your solution to check for mistakes.")
                }
            }
        }

        return response.toString()
    }

    private fun getDetailedReasoning(concept: MathKnowledgeBase.ConceptInfo, index: Int): String {
        return when (index) {
            0 -> "Initial assessment is crucial to select the correct approach for ${concept.conceptName}."
            1 -> "Most problems in ${concept.chapter.name} rely on a central identity or theorem. Applying it now reduces the problem's complexity."
            2 -> "Calculation phase ensures that the theoretical model is accurately transformed into a numerical or symbolic result."
            else -> "Final verification ensures the answer satisfies all initial conditions and constraints."
        }
    }

    private fun getHint(conceptInfo: MathKnowledgeBase.ConceptInfo, stepIndex: Int): String {
        val hints = listOf(
            "Think about which chapter this problem belongs to. It looks like ${conceptInfo.chapter.name}.",
            "Have you looked at the standard formulas? For example: ${conceptInfo.formulas.firstOrNull() ?: "None available"}",
            "Try breaking the problem into smaller parts.",
            "Consider if there's a simpler substitution you can make."
        )
        return hints.getOrElse(stepIndex % hints.size) { hints.last() }
    }

    private fun getTheoremExplanation(concept: String): String {
        return "The fundamental principle of $concept states that we can model complex relationships using standard mathematical structures. " +
               "In competitive exams like JEE, understanding the 'why' behind the formula is more important than memorizing it. " +
               "This principle allows for rigorous logical deduction."
    }

    private fun detectMistakes(userSolution: String, conceptInfo: MathKnowledgeBase.ConceptInfo): String {
        val lower = userSolution.lowercase()
        val mistakes = mutableListOf<String>()
        
        if (conceptInfo.conceptName.contains("Integration") && !lower.contains("+ c") && !lower.contains("+c")) {
            mistakes.add("You might have forgotten the constant of integration (+ C).")
        }
        
        if (conceptInfo.conceptName.contains("Equation") && lower.contains("x =") && !lower.contains("±") && conceptInfo.formulas.any { it.contains("²") }) {
             mistakes.add("Check if you missed a negative root (±) when taking the square root.")
        }

        return if (mistakes.isEmpty()) {
            "✅ No obvious mistakes detected in your approach so far! Keep going."
        } else {
            "⚠️ Potential Issues Found:\n" + mistakes.joinToString("\n") { "• $it" }
        }
    }
}
