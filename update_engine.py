import re

with open("domain/src/main/java/com/example/domain/math/CalculatorEngine.kt", "r") as f:
    content = f.read()

old_func_block = """                    x = when (func) {
                        "sqrt" -> sqrt(arg)
                        "sin" -> sin(argRad)
                        "cos" -> cos(argRad)
                        "tan" -> tan(argRad)
                        "log" -> log10(arg)
                        "ln" -> ln(arg)
                        else -> throw RuntimeException("Unknown function: $func")
                    }"""

new_func_block = """                    x = when (func) {
                        "sqrt", "√" -> sqrt(arg)
                        "sin" -> sin(argRad)
                        "cos" -> cos(argRad)
                        "tan" -> tan(argRad)
                        "asin" -> if (isDegreeMode) Math.toDegrees(asin(arg)) else asin(arg)
                        "acos" -> if (isDegreeMode) Math.toDegrees(acos(arg)) else acos(arg)
                        "atan" -> if (isDegreeMode) Math.toDegrees(atan(arg)) else atan(arg)
                        "log" -> log10(arg)
                        "ln" -> ln(arg)
                        "abs" -> abs(arg)
                        "floor" -> floor(arg)
                        "ceil" -> ceil(arg)
                        "exp" -> exp(arg)
                        else -> throw RuntimeException("Unknown function: $func")
                    }"""

content = content.replace(old_func_block, new_func_block)

# Replace mod to % operator? Actually MOD is usually binary operator. 
# x MOD y
# Let's add mod as a binary operator.

old_term_block = """                    if (eat('*'.code) || eat('×'.code)) x *= parseFactor() 
                    else if (eat('/'.code) || eat('÷'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Divide by zero")
                        x /= divisor
                    }
                    else return x"""

new_term_block = """                    if (eat('*'.code) || eat('×'.code)) x *= parseFactor() 
                    else if (eat('/'.code) || eat('÷'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Divide by zero")
                        x /= divisor
                    }
                    else if (eat('%'.code)) {
                        // Normally % is percent as postfix, but let's see. 
                        // Actually in Calculator Engine we already have % as postfix.
                        // Let's use 'mod' as keyword. Wait, it's easier to handle mod if it's tokenized.
                        return x
                    }
                    else return x"""

# For MOD, the expression replaces "mod" with some char, say "m".
# Or let's replace "mod" with "%" in string preprocessing, but wait, "%" is postfix for percent.
# We can just replace "mod" with "#" and use "#" as mod operator.

content = content.replace('val expr = str.replace(" ", "").lowercase().replace("pi", Math.PI.toString()).replace("e", Math.E.toString())', 'val expr = str.replace(" ", "").lowercase().replace("pi", Math.PI.toString()).replace("e", Math.E.toString()).replace("mod", "#")')

new_term_block = """                    if (eat('*'.code) || eat('×'.code)) x *= parseFactor() 
                    else if (eat('/'.code) || eat('÷'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Divide by zero")
                        x /= divisor
                    }
                    else if (eat('#'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Divide by zero")
                        x %= divisor
                    }
                    else return x"""
content = content.replace(old_term_block, new_term_block)

# For rand, replace "rand" with random(). Wait, rand has no arguments.
content = content.replace('.replace("mod", "#")', '.replace("mod", "#").replace("rand", Math.random().toString())')

# 1/x -> this is just 1/(...) in string or we add inv()
content = content.replace('.replace("rand", Math.random().toString())', '.replace("rand", Math.random().toString()).replace("√", "sqrt")')


with open("domain/src/main/java/com/example/domain/math/CalculatorEngine.kt", "w") as f:
    f.write(content)
