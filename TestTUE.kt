import com.example.domain.scanner.*
fun main() {
    val t = TextUnderstandingEngine.process("please find the derivative of x^2 + 2x")
    println(t)
}
