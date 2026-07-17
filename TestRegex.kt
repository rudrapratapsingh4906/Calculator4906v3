fun main() {
    val text = "find the derivative of sin(2x)"
    val commandWords = listOf("solve", "evaluate", "calculate", "compute", "simplify", "differentiate", "integrate", "derivative", "integral")
    val words = text.split(Regex("\\s+"))
    if (words.size <= 3 && commandWords.any { text.contains(it) }) {
        println("Returned null")
        return
    }
    var eq = text
    eq = eq.replace("find the", "")
    val cleaned = eq.replace(Regex("[^0-9xX+\\-*/^=().]"), "")
    println("Cleaned: $cleaned")
}
