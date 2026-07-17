package com.example.domain.scanner

import com.example.domain.math.*
import kotlin.math.*

object AdvancedMathEngine {

    data class Vector3D(val x: Double, val y: Double, val z: Double) {
        fun magnitude() = sqrt(x * x + y * y + z * z)
        fun dot(other: Vector3D) = x * other.x + y * other.y + z * other.z
        fun cross(other: Vector3D) = Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
        override fun toString() = "${format(x)}i + ${format(y)}j + ${format(z)}k"
    }

    data class Matrix(val rows: Int, val cols: Int, val data: Array<DoubleArray>) {
        fun determinant(): Double {
            if (rows != cols) throw ArithmeticException("Non-square matrix")
            return when (rows) {
                1 -> data[0][0]
                2 -> data[0][0] * data[1][1] - data[0][1] * data[1][0]
                3 -> {
                    data[0][0] * (data[1][1] * data[2][2] - data[1][2] * data[2][1]) -
                    data[0][1] * (data[1][0] * data[2][2] - data[1][2] * data[2][0]) +
                    data[0][2] * (data[1][0] * data[2][1] - data[1][1] * data[2][0])
                }
                else -> 0.0 // Complex decomposition for higher order
            }
        }

        override fun toString(): String {
            return data.joinToString("\n") { row -> row.joinToString("  ") { format(it) } }
        }
    }

    data class Complex(val re: Double, val im: Double) {
        fun add(other: Complex) = Complex(re + other.re, im + other.im)
        fun mul(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)
        fun magnitude() = sqrt(re * re + im * im)
        override fun toString() = "${format(re)} + ${format(im)}i"
    }

    private fun format(n: Double): String {
        if (abs(n - n.roundToLong()) < 1e-9) return n.roundToLong().toString()
        return String.format("%.2f", n).trimEnd('0').trimEnd('.')
    }

    fun solveSystem2x2(a: Double, b: Double, e: Double, c: Double, d: Double, f: Double): Pair<Double, Double>? {
        val det = a * d - b * c
        if (abs(det) < 1e-9) return null
        val x = (e * d - b * f) / det
        val y = (a * f - e * c) / det
        return x to y
    }

    fun vectorAngle(a: Vector3D, b: Vector3D): Double {
        val dot = a.dot(b)
        val mags = a.magnitude() * b.magnitude()
        if (mags < 1e-9) return 0.0
        return acos(dot / mags) * 180 / PI
    }

    fun matrixInverse2x2(m: Matrix): Matrix? {
        if (m.rows != 2 || m.cols != 2) return null
        val det = m.determinant()
        if (abs(det) < 1e-9) return null
        val d = m.data
        val invData = arrayOf(
            doubleArrayOf(d[1][1] / det, -d[0][1] / det),
            doubleArrayOf(-d[1][0] / det, d[0][0] / det)
        )
        return Matrix(2, 2, invData)
    }

    fun distance3D(p1: Vector3D, p2: Vector3D): Double {
        return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2) + (p1.z - p2.z).pow(2))
    }

    fun sectionPoint(p1: Vector3D, p2: Vector3D, m: Double, n: Double): Vector3D {
        return Vector3D(
            (m * p2.x + n * p1.x) / (m + n),
            (m * p2.y + n * p1.y) / (m + n),
            (m * p2.z + n * p1.z) / (m + n)
        )
    }
}
