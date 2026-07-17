package com.example.domain.scanner

import com.example.domain.math.*
import kotlin.random.Random

data class MathQuestion(
    val id: String,
    val text: String,
    val difficulty: MathLevel,
    val category: MathCategory,
    val chapter: MathChapter,
    val correctAnswer: String? = null,
    val hint: String? = null
)

object QuestionGeneratorEngine {

    fun generateQuestions(
        chapter: MathChapter,
        level: MathLevel,
        topic: String? = null,
        count: Int = 5
    ): List<MathQuestion> {
        val questions = mutableListOf<MathQuestion>()
        repeat(count) {
            val q = when {
                topic != null -> generateTopicSpecificQuestion(topic, level)
                else -> when (chapter) {
                    MathChapter.SETS_RELATIONS_FUNCTIONS -> generateCalculusQuestion(level)
                    MathChapter.MATRICES_DETERMINANTS -> generateMatrixQuestion(level)
                    MathChapter.LIMIT_CONTINUITY_DIFFERENTIABILITY -> generateCalculusQuestion(level)
                    MathChapter.INTEGRAL_CALCULUS -> generateIntegrationQuestion(level)
                    MathChapter.VECTOR_ALGEBRA -> generateVectorQuestion(level)
                    MathChapter.COMPLEX_NUMBERS_QUADRATIC_EQUATIONS -> generateComplexQuestion(level)
                    MathChapter.TRIGONOMETRY -> generateTrigQuestion(level)
                    MathChapter.SEQUENCE_SERIES -> generateSequenceQuestion(level)
                    MathChapter.STATISTICS_PROBABILITY -> generateStatsQuestion(level)
                    else -> generateGeneralQuestion(chapter, level)
                }
            }
            questions.add(q)
        }
        return questions
    }

    private fun generateTopicSpecificQuestion(topic: String, level: MathLevel): MathQuestion {
        return when (topic) {
            "Determinants" -> generateMatrixQuestion(level)
            "Dot Product" -> generateVectorQuestion(level)
            "Complex Basics" -> generateComplexQuestion(level)
            "Standard Derivatives" -> generateCalculusQuestion(level)
            "Indefinite Integrals" -> generateIntegrationQuestion(level)
            "Ratios and Identities" -> generateTrigQuestion(level)
            "Arithmetic Progression" -> generateSequenceQuestion(level)
            "Mean and Variance" -> generateStatsQuestion(level)
            else -> MathQuestion(
                id = "topic_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                text = "Practice problem for $topic at ${level.name} level.",
                difficulty = level,
                category = MathCategory.GENERAL,
                chapter = MathChapter.GENERAL,
                hint = "Use the core concepts of $topic to solve this."
            )
        }
    }

    private fun generateTrigQuestion(level: MathLevel): MathQuestion {
        val expressions = listOf("sin(30°)", "cos(60°)", "tan(45°)", "sin(90°)", "cos(0°)")
        val expr = expressions[Random.nextInt(expressions.size)]
        return MathQuestion(
            id = "trig_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            text = "Evaluate the trigonometric expression: $expr",
            difficulty = level,
            category = MathCategory.TRIGONOMETRY,
            chapter = MathChapter.TRIGONOMETRY,
            hint = "Recall the standard values of trigonometric ratios for special angles."
        )
    }

    private fun generateSequenceQuestion(level: MathLevel): MathQuestion {
        val a = Random.nextInt(1, 10)
        val d = Random.nextInt(1, 5)
        val n = Random.nextInt(5, 15)
        return MathQuestion(
            id = "seq_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            text = "In an Arithmetic Progression (AP), the first term is $a and common difference is $d. Find the ${n}th term.",
            difficulty = level,
            category = MathCategory.ALGEBRA,
            chapter = MathChapter.SEQUENCE_SERIES,
            correctAnswer = (a + (n - 1) * d).toString(),
            hint = "The nth term of an AP is given by a + (n-1)d."
        )
    }

    private fun generateStatsQuestion(level: MathLevel): MathQuestion {
        val data = List(5) { Random.nextInt(1, 20) }
        return MathQuestion(
            id = "stats_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            text = "Find the mean of the following data set: ${data.joinToString(", ")}",
            difficulty = level,
            category = MathCategory.STATISTICS,
            chapter = MathChapter.STATISTICS_PROBABILITY,
            correctAnswer = (data.sum().toDouble() / data.size).toString(),
            hint = "The mean is calculated by summing all data points and dividing by the count."
        )
    }

    private fun generateCalculusQuestion(level: MathLevel): MathQuestion {
        val ops = listOf("sin(x)", "cos(x)", "x^2", "exp(x)", "ln(x)", "tan(x)")
        val expr = ops[Random.nextInt(ops.size)]
        return MathQuestion(
            id = "calc_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            text = "Differentiate the following expression with respect to x: $expr",
            difficulty = level,
            category = MathCategory.CALCULUS,
            chapter = MathChapter.LIMIT_CONTINUITY_DIFFERENTIABILITY,
            hint = "Use the standard derivative formulas for elementary functions."
        )
    }

    private fun generateIntegrationQuestion(level: MathLevel): MathQuestion {
        val ops = listOf("x", "x^2", "sin(x)", "cos(x)", "exp(x)")
        val expr = ops[Random.nextInt(ops.size)]
        return MathQuestion(
            id = "int_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            text = "Find the indefinite integral of: $expr",
            difficulty = level,
            category = MathCategory.CALCULUS,
            chapter = MathChapter.INTEGRAL_CALCULUS,
            hint = "Don't forget the constant of integration (+C)."
        )
    }

    private fun generateMatrixQuestion(level: MathLevel): MathQuestion {
        val a = Random.nextInt(1, 10)
        val b = Random.nextInt(1, 10)
        val c = Random.nextInt(1, 10)
        val d = Random.nextInt(1, 10)
        return MathQuestion(
            id = "mat_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            text = "Find the determinant of the 2x2 matrix: [[$a, $b], [$c, $d]]",
            difficulty = level,
            category = MathCategory.MATRIX,
            chapter = MathChapter.MATRICES_DETERMINANTS,
            correctAnswer = (a * d - b * c).toString(),
            hint = "For a 2x2 matrix [[a, b], [c, d]], the determinant is ad - bc."
        )
    }

    private fun generateVectorQuestion(level: MathLevel): MathQuestion {
        val v1 = "${Random.nextInt(5)}i + ${Random.nextInt(5)}j + ${Random.nextInt(5)}k"
        val v2 = "${Random.nextInt(5)}i + ${Random.nextInt(5)}j + ${Random.nextInt(5)}k"
        return MathQuestion(
            id = "vec_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            text = "Find the dot product of vectors A = $v1 and B = $v2",
            difficulty = level,
            category = MathCategory.VECTOR,
            chapter = MathChapter.VECTOR_ALGEBRA,
            hint = "Multiply corresponding components (i*i, j*j, k*k) and sum them up."
        )
    }

    private fun generateComplexQuestion(level: MathLevel): MathQuestion {
        val re = Random.nextInt(1, 10)
        val im = Random.nextInt(1, 10)
        return MathQuestion(
            id = "cplx_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            text = "Find the magnitude (modulus) of the complex number z = $re + ${im}i",
            difficulty = level,
            category = MathCategory.COMPLEX,
            chapter = MathChapter.COMPLEX_NUMBERS_QUADRATIC_EQUATIONS,
            hint = "The magnitude of a + bi is sqrt(a^2 + b^2)."
        )
    }

    private fun generateGeneralQuestion(chapter: MathChapter, level: MathLevel): MathQuestion {
        return MathQuestion(
            id = "gen_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            text = "Practice problem for ${chapter.name.replace("_", " ")} at ${level.name} level.",
            difficulty = level,
            category = MathCategory.GENERAL,
            chapter = chapter,
            hint = "Review the basic definitions and properties of this topic."
        )
    }
}
