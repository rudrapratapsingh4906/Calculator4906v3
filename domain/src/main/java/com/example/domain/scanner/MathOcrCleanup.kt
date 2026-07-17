package com.example.domain.scanner

object MathOcrCleanup {

    /**
     * Cleans up raw OCR recognized text, correcting common mathematical notation errors,
     * normalizing symbols, and extracting the mathematical expressions/equations.
     */
    fun cleanup(rawText: String?): String {
        if (rawText.isNullOrBlank()) return ""

        try {
            // 1. Process lines, handle multi-line fraction layout detection
            val lines = rawText.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val mergedLines = mutableListOf<String>()
            var i = 0
            while (i < lines.size) {
                if (i < lines.size - 2 && isFractionBar(lines[i + 1])) {
                    val numerator = lines[i]
                    val denominator = lines[i + 2]
                    mergedLines.add("($numerator)/($denominator)")
                    i += 3
                } else {
                    mergedLines.add(lines[i])
                    i++
                }
            }

            // Combine all processed lines
            var text = mergedLines.joinToString(" ")

            // Trim trailing punctuation like "?", ".", ",", ";" that OCR might append
            text = trimTrailingPunctuation(text)

            // 2. Bracket normalization
            text = text.replace('[', '(').replace('{', '(')
                .replace(']', ')').replace('}', ')')

            // 3. Subtraction & negative symbol normalization
            text = text.replace('−', '-')
                .replace('—', '-')
                .replace('–', '-')

            // 4. Letter-to-digit corrections in numeric/operator contexts (O -> 0, l/I -> 1, S -> 5, Z -> 2)
            text = text.replace(Regex("(\\d)[oO]")) { it.groupValues[1] + "0" }
            text = text.replace(Regex("[oO](\\d)")) { "0" + it.groupValues[1] }
            text = text.replace(Regex("(\\d+\\.)[oO]")) { it.groupValues[1] + "0" }
            text = text.replace(Regex("[oO](\\.\\d)")) { "0" + it.groupValues[1] }

            text = text.replace(Regex("(\\d)[lI|]")) { it.groupValues[1] + "1" }
            text = text.replace(Regex("[lI|](\\d)")) { "1" + it.groupValues[1] }
            text = text.replace(Regex("([+\\-*/%^=])\\s*[lI|]\\s*([+\\-*/%^=])")) { it.groupValues[1] + " 1 " + it.groupValues[2] }
            text = text.replace(Regex("^\\s*[lI|]\\s*([+\\-*/%^=])")) { "1 " + it.groupValues[1] }
            text = text.replace(Regex("([+\\-*/%^=])\\s*[lI|]\\s*$")) { it.groupValues[1] + " 1" }
            
            // General | to 1 if it's likely a number (e.g. |2, 2|)
            text = text.replace(Regex("\\|(\\d)"), "1$1")
            text = text.replace(Regex("(\\d)\\|"), "$11")

            text = text.replace(Regex("(\\d)[sS]")) { it.groupValues[1] + "5" }
            text = text.replace(Regex("[sS](\\d)")) { "5" + it.groupValues[1] }
            text = text.replace(Regex("(\\d+\\.)[sS]")) { it.groupValues[1] + "5" }
            text = text.replace(Regex("[sS](\\.\\d)")) { "5" + it.groupValues[1] }

            text = text.replace(Regex("(\\d)[zZ]")) { it.groupValues[1] + "2" }
            text = text.replace(Regex("[zZ](\\d)")) { "2" + it.groupValues[1] }
            text = text.replace(Regex("(\\d+\\.)[zZ]")) { it.groupValues[1] + "2" }
            text = text.replace(Regex("[zZ](\\.\\d)")) { "2" + it.groupValues[1] }

            // 5. Multiplication & Division symbols normalization
            text = text.replace('×', '*')
                .replace('•', '*')
                .replace('·', '*')
                .replace('÷', '/')
            
            // Handle Pi
            text = text.replace(Regex("(?i)\\bpi\\b"), "π")
            // Removed erroneous π to p replacement as p is permutation in CalculatorEngine
            
            // Replaces 'x' or 'X' with '*' when positioned strictly between digits (with optional spaces)
            // Example: 5 x 3 -> 5 * 3, 5x3 -> 5*3. Keeps 'x' as algebraic variable elsewhere.
            text = text.replace(Regex("(\\d+)\\s*[xX]\\s*(\\d+)"), "$1*$2")

            // Replaces ':' with '/' when strictly positioned between digits (e.g. 6:2 -> 6/2)
            text = text.replace(Regex("(\\d+)\\s*:\\s*(\\d+)"), "$1/$2")

            // 6. Power/Exponent normalization (e.g. x2 -> x^2, y3 -> y^3, (x+1)2 -> (x+1)^2)
            text = text.replace(Regex("([a-zA-Z\\)])([2345])\\b"), "$1^$2")
            
            // 7. Root normalization
            text = text.replace(Regex("(?i)sqrt\\((.*?)\\)"), "($1)^0.5")
            text = text.replace(Regex("(?i)cbrt\\((.*?)\\)"), "($1)^(1/3)")

            // Double equals normalization
            text = text.replace("==", "=")

            // 8. Process with TextUnderstandingEngine to detect intent and strip commands
            val understanding = TextUnderstandingEngine.process(text)
            
            // For solver input, if intent is just expression, we can return expression
            // Let's add the command prefix if needed by ExpressionParserImpl or CalculusUseCase
            // But if the project expects MathOcrCleanup.cleanup to just strip things,
            // we will just return the expression.
            return understanding.expression

        } catch (e: Throwable) {
            // Safe fallback to returning trimmed raw text in case of any processing exception
            return rawText?.trim() ?: ""
        }
    }

    private fun isFractionBar(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return false
        return trimmed.all { it == '-' || it == '_' || it == '=' || it == '—' || it == '–' }
    }

    private fun trimTrailingPunctuation(str: String): String {
        var temp = str.trim()
        while (temp.endsWith(".") || temp.endsWith("?") || temp.endsWith(",") || temp.endsWith(";")) {
            temp = temp.substring(0, temp.length - 1).trim()
        }
        return temp
    }
}
