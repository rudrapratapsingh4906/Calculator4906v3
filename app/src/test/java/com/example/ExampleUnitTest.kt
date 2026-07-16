package com.example

import com.example.domain.math.CalculatorEngine
import com.example.core.util.Result
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCalculatorEngineAdvancedExpressions() {
    val engine = CalculatorEngine()

    // 1. Variable support
    // x^2 when x = 3
    val res1 = engine.evaluate("x^2", false, mapOf("x" to 3.0))
    assertTrue(res1 is Result.Success)
    assertEquals(9.0, (res1 as Result.Success).data, 1e-9)

    // sin(x) when x = Math.PI / 2
    val res2 = engine.evaluate("sin(x)", false, mapOf("x" to Math.PI / 2))
    assertTrue(res2 is Result.Success)
    assertEquals(1.0, (res2 as Result.Success).data, 1e-9)

    // cos(x)+x^2 when x = 0
    val res3 = engine.evaluate("cos(x)+x^2", false, mapOf("x" to 0.0))
    assertTrue(res3 is Result.Success)
    assertEquals(1.0, (res3 as Result.Success).data, 1e-9)

    // log(x) (base 10) when x = 100
    val res4 = engine.evaluate("log(x)", false, mapOf("x" to 100.0))
    assertTrue(res4 is Result.Success)
    assertEquals(2.0, (res4 as Result.Success).data, 1e-9)

    // ln(x) (base e) when x = Math.E
    val res5 = engine.evaluate("ln(x)", false, mapOf("x" to Math.E))
    assertTrue(res5 is Result.Success)
    assertEquals(1.0, (res5 as Result.Success).data, 1e-9)

    // abs(x) when x = -4.5
    val res6 = engine.evaluate("abs(x)", false, mapOf("x" to -4.5))
    assertTrue(res6 is Result.Success)
    assertEquals(4.5, (res6 as Result.Success).data, 1e-9)

    // 2. Modulus support
    // 5 mod 3
    val res7 = engine.evaluate("5 mod 3", false)
    assertTrue(res7 is Result.Success)
    assertEquals(2.0, (res7 as Result.Success).data, 1e-9)

    // 3. Trigonometric functions
    // tan(Math.PI / 4)
    val res8 = engine.evaluate("tan(x)", false, mapOf("x" to Math.PI / 4))
    assertTrue(res8 is Result.Success)
    assertEquals(1.0, (res8 as Result.Success).data, 1e-9)

    // asin(1)
    val res9 = engine.evaluate("asin(1)", false)
    assertTrue(res9 is Result.Success)
    assertEquals(Math.PI / 2, (res9 as Result.Success).data, 1e-9)

    // acos(0)
    val res10 = engine.evaluate("acos(0)", false)
    assertTrue(res10 is Result.Success)
    assertEquals(Math.PI / 2, (res10 as Result.Success).data, 1e-9)

    // atan(1)
    val res11 = engine.evaluate("atan(1)", false)
    assertTrue(res11 is Result.Success)
    assertEquals(Math.PI / 4, (res11 as Result.Success).data, 1e-9)

    // 4. Invalid regions
    // Division by zero
    val res12 = engine.evaluate("1/x", false, mapOf("x" to 0.0))
    assertTrue(res12 is Result.Error)

    // log negative domain
    val res13 = engine.evaluate("log(x)", false, mapOf("x" to -1.0))
    assertTrue(res13 is Result.Error)

    val res14 = engine.evaluate("ln(x)", false, mapOf("x" to -5.0))
    assertTrue(res14 is Result.Error)
  }
}
