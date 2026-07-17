package com.example.domain.scanner

enum class MathIntent {
    SOLVE,
    EVALUATE,
    SIMPLIFY,
    DIFFERENTIATE,
    INTEGRATE,
    WORD_PROBLEM,
    MATRIX,
    COMPLEX,
    STATISTICS,
    GEOMETRY,
    VECTOR,
    ALGEBRA,
    TUTOR,
    PRACTICE,
    UNKNOWN
}

data class UnderstandingResult(
    val intent: MathIntent,
    val expression: String,
    val category: MathCategory = MathCategory.GENERAL,
    val variables: List<String> = emptyList(),
    val extractedCommand: String? = null
)

object TextUnderstandingEngine {
    
    fun process(rawText: String): UnderstandingResult {
        var text = rawText.trim()
        var intent = MathIntent.UNKNOWN
        var command: String? = null
        val lowerText = text.lowercase()
        
        var category = MathCategory.GENERAL

        // 1. Detect Intent and Category
        if (lowerText.contains("practice") || lowerText.contains("question") || lowerText.contains("problem set")) {
            intent = MathIntent.PRACTICE
            command = when {
                lowerText.contains("chapter") -> "chapter_practice"
                lowerText.contains("topic") -> "topic_practice"
                else -> "general_practice"
            }
        } else if (lowerText.contains("hint") || lowerText.contains("next step") || lowerText.contains("mistake") || lowerText.contains("tutor") || lowerText.contains("formula") || lowerText.contains("theorem")) {
            intent = MathIntent.TUTOR
            command = when {
                lowerText.contains("hint") -> "hint"
                lowerText.contains("next step") -> "next_step"
                lowerText.contains("mistake") -> "mistake"
                lowerText.contains("formula") -> "formula"
                lowerText.contains("theorem") -> "theorem"
                else -> "tutor"
            }
        } else if (lowerText.contains("matrix") || lowerText.contains("determinant") || lowerText.contains("inverse") || lowerText.contains("transpose")) {
            intent = MathIntent.MATRIX
            command = "matrix"
            category = MathCategory.MATRIX
        } else if (lowerText.contains("complex") || lowerText.contains("imaginary") || lowerText.contains("real part") || lowerText.contains("imaginary part") || lowerText.matches(Regex(".*\\d+i.*")) || lowerText.matches(Regex(".*\\+.*i.*"))) {
            intent = MathIntent.COMPLEX
            command = "complex"
            category = MathCategory.COMPLEX
        } else if (lowerText.contains("mean") || lowerText.contains("median") || lowerText.contains("mode") || lowerText.contains("standard deviation") || lowerText.contains("variance") || lowerText.contains("probability") || lowerText.contains("statistics")) {
            intent = MathIntent.STATISTICS
            command = "statistics"
            category = MathCategory.STATISTICS
        } else if (lowerText.contains("vector") || lowerText.contains("dot product") || lowerText.contains("cross product") || lowerText.contains("magnitude") || lowerText.contains("projection")) {
            intent = MathIntent.VECTOR
            command = "vector"
            category = MathCategory.VECTOR
        } else if (lowerText.contains("differentiate") || lowerText.contains("derivative") || lowerText.contains("dy/dx") || lowerText.contains("f'(x)") || lowerText.contains("differentiation")) {
            intent = MathIntent.DIFFERENTIATE
            command = "differentiate"
            category = MathCategory.CALCULUS
        } else if (lowerText.contains("integrate") || lowerText.contains("integral") || lowerText.contains("∫") || lowerText.contains("antiderivative") || lowerText.contains("integration")) {
            intent = MathIntent.INTEGRATE
            command = "integrate"
            category = MathCategory.CALCULUS
        } else if (lowerText.contains("limit") || lowerText.contains("lim ") || lowerText.contains("approaches")) {
            intent = MathIntent.EVALUATE
            command = "limit"
            category = MathCategory.CALCULUS
        } else if (lowerText.contains("area") || lowerText.contains("volume") || lowerText.contains("perimeter") || lowerText.contains("triangle") || lowerText.contains("circle") || lowerText.contains("rectangle") || lowerText.contains("geometry")) {
            category = MathCategory.GEOMETRY
            intent = MathIntent.SOLVE
        } else if (lowerText.contains("simplify") || lowerText.contains("reduce") || lowerText.contains("expand") || lowerText.contains("factor")) {
            intent = MathIntent.SIMPLIFY
            command = "simplify"
            category = MathCategory.ALGEBRA
        } else if (lowerText.contains("logarithm") || lowerText.contains("log ") || lowerText.contains("ln ")) {
            intent = MathIntent.EVALUATE
            command = "log"
            category = MathCategory.ALGEBRA
        } else if (lowerText.contains("permutation") || lowerText.contains("combination") || lowerText.contains("p(n,r)") || lowerText.contains("c(n,r)")) {
            intent = MathIntent.EVALUATE
            command = "combinatorics"
            category = MathCategory.STATISTICS
        } else if (lowerText.contains("trigonometry") || lowerText.contains("sine") || lowerText.contains("cosine") || lowerText.contains("tangent") || lowerText.contains("cosec") || lowerText.contains("secant") || lowerText.contains("cotangent")) {
            category = MathCategory.TRIGONOMETRY
            intent = MathIntent.EVALUATE
        } else if (lowerText.contains("solve") || lowerText.contains("find x") || lowerText.contains("find the value of") || lowerText.contains("solution")) {
            intent = MathIntent.SOLVE
            command = "solve"
            category = MathCategory.ALGEBRA
        } else if (lowerText.contains("calculate") || lowerText.contains("evaluate") || lowerText.contains("compute") || lowerText.contains("what is") || lowerText.contains("result of")) {
            intent = MathIntent.EVALUATE
            command = "evaluate"
        } else if (text.contains("=")) {
            intent = MathIntent.SOLVE
            category = MathCategory.ALGEBRA
        } else {
            intent = MathIntent.EVALUATE
            if (text.contains(Regex("[a-zA-Z]"))) {
                category = MathCategory.ALGEBRA
            }
        }

        // Clean up common conversational words
        var expression = text.replace(Regex("(?i)^(can you|please|help me|i need to|could you|tell me|show me)\\s+"), "")
        expression = expression.replace(Regex("(?i)\\s+(for me|please|now|quickly)$"), "")

        // 2. Extract Variables (e.g. "Solve for y")
        val variables = mutableListOf<String>()
        val varMatch = Regex("(?i)for\\s+([a-zA-Z])(\\s+|$)").find(expression)
        if (varMatch != null) {
            variables.add(varMatch.groupValues[1])
        }

        // 3. Regex to strip out command prefixes more aggressively
        val commandPrefixRegex = Regex("(?i)^\\s*(find(\\s+[a-zA-Z]+\\s+for|\\s+the\\s+value\\s+of(\\s+[a-zA-Z]+)?(\\s+for)?|\\s+derivative\\s+of|\\s+integral\\s+of|\\s+solution\\s+to)?|solve(\\s+for\\s+[a-zA-Z]+)?|calculate(\\s+the)?|simplify(\\s+the)?|reduce(\\s+the)?|expand(\\s+the)?|factor(\\s+the)?|differentiate(\\s+the)?|integrate(\\s+the)?|evaluate(\\s+the)?|compute(\\s+the)?|what\\s+is(\\s+the)?|equation|expression|result\\s+of)\\s*[:=]*\\s*")
        expression = expression.replace(commandPrefixRegex, "")

        // 4. Handle separators like "where", "when", "if"
        val separatorMatch = Regex("(?i)\\s+(where|when|if|such that|given)\\s+").find(expression)
        if (separatorMatch != null) {
            // Keep the math before the separator as primary, but if the separator introduces values (x=2), handle them
            // For now, let's just keep the main expression
            // expression = expression.substring(0, separatorMatch.range.first).trim()
            // Wait, actually "Find x^2 where x=3" -> we want the whole thing or some way to handle it.
            // Our CalculatorEngine supports variables. So "x^2 where x=3" -> "x^2, x=3"
            expression = expression.replace(Regex("(?i)\\s+(where|when|if|such that|given)\\s+"), ", ")
        }

        // Also handle "differentiate x^2" or "integrate x^2" anywhere in the text if it survived
        expression = expression.replace(Regex("(?i)(differentiate|integrate|derivative of|integral of|solve|calculate|evaluate|simplify)\\s+"), "")

        // 5. Try to detect and parse word problems first on the cleaned expression
        val wordProblemEq = parseWordProblem(expression)
        if (wordProblemEq != null) {
            return UnderstandingResult(
                intent = if (wordProblemEq.contains("=")) MathIntent.SOLVE else MathIntent.EVALUATE,
                expression = wordProblemEq,
                category = category.takeIf { it != MathCategory.GENERAL } ?: MathCategory.ALGEBRA,
                variables = if (variables.isEmpty()) listOf("x") else variables,
                extractedCommand = "word_problem"
            )
        }

        return UnderstandingResult(intent, expression.trim(), category, variables, command)
    }

    private fun parseWordProblem(text: String): String? {
        val lowerText = text.lowercase()
        // very basic heuristic to detect if it's a word problem rather than just an equation
        val hasWords = Regex("[a-z]{3,}").containsMatchIn(lowerText)
        
        // If it doesn't have words, or is a simple command, we skip word problem parsing
        if (!hasWords) return null
        
        // Common commands that aren't word problems by themselves
        val commandWords = listOf("solve", "evaluate", "calculate", "compute", "simplify", "differentiate", "integrate", "derivative", "integral", "limit", "expand", "factor")
        val words = lowerText.split(Regex("\\s+"))
        if (words.size <= 3 && commandWords.any { lowerText.contains(it) }) return null

        var eq = lowerText

        // Handle "of" in fractions/percent
        // percent: "20 percent of 50" -> "(20/100)*50"
        eq = eq.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*(?:percent|%)\\s*of\\s*(\\d+(?:\\.\\d+)?)"), "($1/100)*$2")
        eq = eq.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*(?:percent|%)\\s*of\\s*([a-zA-Z]+)"), "($1/100)*$2")

        // fraction: "one third of x" -> "(1/3)*x"
        val fractions = mapOf(
            "half" to "0.5", "third" to "(1/3)", "fourth" to "0.25", "quarter" to "0.25", "fifth" to "0.2"
        )
        for ((word, value) in fractions) {
            eq = eq.replace(Regex("\\b$word\\b\\s*of"), "$value *")
            eq = eq.replace(Regex("\\ba\\s+$word\\b\\s*of"), "$value *")
        }

        // Number words replacements
        val numberWords = mapOf(
            "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
            "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9", "ten" to "10"
        )
        for ((word, digit) in numberWords) {
            eq = eq.replace(Regex("\\b$word\\b"), digit)
        }

        // Multi-variable identifiers
        if (eq.contains("two numbers")) {
            eq = eq.replace("two numbers", "x and y")
            eq = eq.replace("their", "x and y")
        }

        // Variable identifiers
        val variablePlaceholders = listOf(
            "a certain number", "a number", "the number", "some number", "an unknown",
            "an integer", "a value", "the value", "a quantity"
        )
        for (placeholder in variablePlaceholders) {
            eq = eq.replace(placeholder, "x")
        }
        
        // Handle names as variables (common in word problems)
        val names = listOf("ram", "shyam", "aman", "john", "mary", "bob", "alice")
        for (name in names) {
            val nameRegex = Regex("(?i)\\b$name\\b")
            if (nameRegex.containsMatchIn(eq)) {
                eq = eq.replace(nameRegex, name[0].toString()) // Use first letter as variable
            }
        }

        // Equals identifiers
        eq = eq.replace("is equal to", "=")
        eq = eq.replace("equals", "=")
        eq = eq.replace(Regex("\\bis\\b"), "=")
        eq = eq.replace("gives", "=")
        eq = eq.replace("yields", "=")
        eq = eq.replace("results in", "=")
        eq = eq.replace("will be", "=")
        eq = eq.replace("was", "=")
        eq = eq.replace("total is", "=")

        // Age Problems
        eq = eq.replace(Regex("(?i)(\\d+)\\s+years\\s+ago"), "- $1")
        eq = eq.replace(Regex("(?i)(\\d+)\\s+years\\s+(?:hence|later|from\\s+now)"), "+ $1")
        eq = eq.replace(Regex("(?i)older\\s+than"), "+")
        eq = eq.replace(Regex("(?i)younger\\s+than"), "-")

        // Geometry Relationships
        eq = eq.replace(Regex("(?i)length\\s+of\\s+(?:a|the)\\s+rectangle"), "l")
        eq = eq.replace(Regex("(?i)width\\s+of\\s+(?:a|the)\\s+rectangle"), "w")
        eq = eq.replace(Regex("(?i)perimeter\\s+of\\s+(?:a|the)\\s+rectangle"), "2*(l+w)")
        eq = eq.replace(Regex("(?i)area\\s+of\\s+(?:a|the)\\s+rectangle"), "l*w")
 
        // Operators - Multi-word expressions
        eq = eq.replace(Regex("(?i)(?:the\\s+)?sum of (.*?) and (.*?)($|\\s=|\\+|,)"), "$1 + $2$3")
        eq = eq.replace(Regex("(?i)(?:the\\s+)?difference between (.*?) and (.*?)($|\\s=|\\+|,)"), "$1 - $2$3")
        eq = eq.replace(Regex("(?i)(?:the\\s+)?product of (.*?) and (.*?)($|\\s=|\\+|,)"), "$1 * $2$3")
        eq = eq.replace(Regex("(?i)(?:the\\s+)?ratio of (.*?) to (.*?)($|\\s=|\\+|,)"), "$1 / $2$3")

        // Operators - Modifiers
        eq = eq.replace("twice", "2 *")
        eq = eq.replace("double", "2 *")
        eq = eq.replace("triple", "3 *")
        eq = eq.replace("thrice", "3 *")
        eq = eq.replace("half of", "0.5 *")
        eq = eq.replace(Regex("square of (.*?)($|\\s=|,|\\+)"), "$1^2$2")
        eq = eq.replace(Regex("cube of (.*?)($|\\s=|,|\\+)"), "$1^3$2")
        
        // Relationship modifiers
        eq = eq.replace(Regex("(\\d+)\\s+more\\s+than"), "+ $1")
        eq = eq.replace(Regex("(\\d+)\\s+less\\s+than"), "- $1")
        eq = eq.replace(Regex("is\\s+(\\d+)\\s+times"), "= $1 *")
        
        // Operators - Single word operations
        eq = eq.replace("increased by", "+")
        eq = eq.replace("plus", "+")
        eq = eq.replace("added to", "+")
        eq = eq.replace("decreased by", "-")
        eq = eq.replace("minus", "-")
        eq = eq.replace(Regex("(?i)(.*?) less than (.*?)($|\\s=|\\+)"), "$2 - $1$3")
        eq = eq.replace("subtracted from", "-") 
        eq = eq.replace("multiplied by", "*")
        eq = eq.replace("times", "*")
        eq = eq.replace("divided by", "/")
        eq = eq.replace("over", "/")
        
        // Common filler words in word problems
        val fillers = listOf("years", "old", "years old", "find", "the", "value", "of", "whose", "its", "it")
        for (filler in fillers) {
            eq = eq.replace(Regex("(?i)\\b$filler\\b"), "")
        }

        // Final cleaning: Keep only math-related chars and alphanumeric
        val cleaned = eq.replace(Regex("[^a-zA-Z0-9+\\-*/^=()., ]"), "")
        
        // Handle multiple conditions/sentences
        if (cleaned.contains(".") || cleaned.contains(",") || cleaned.contains(" and ")) {
            val parts = cleaned.split(Regex("[.,]|\\s+and\\s+"))
            val processedParts = parts.map { it.trim() }.filter { it.length > 2 && it.contains(Regex("\\d|[a-zA-Z]")) }
            if (processedParts.size > 1) {
                return processedParts.joinToString(", ")
            }
        }

        if (cleaned != lowerText && cleaned.length > 2) {
            // Must contain at least a digit or variable to be a valid expression
            if (cleaned.contains(Regex("\\d|[a-zA-Z]"))) {
                // Return cleaned math string, replacing any multi-spaces
                return cleaned.replace(Regex("\\s+"), " ").trim()
            }
        }
        
        // If we didn't transform much, but it looks like a clear expression was inside
        val clearEqMatch = Regex("([a-zA-Z0-9+\\-*/^() ]+=[a-zA-Z0-9+\\-*/^() ]+)").find(cleaned)
        if (clearEqMatch != null) {
            return clearEqMatch.groupValues[1].trim()
        }

        return null
    }
}
