package com.example.domain.usecase

import com.example.domain.model.ComplexNumber
import kotlin.math.atan2
import kotlin.math.sqrt

class ComplexUseCase {

    fun add(c1: ComplexNumber, c2: ComplexNumber): ComplexNumber {
        return ComplexNumber(c1.real + c2.real, c1.imaginary + c2.imaginary)
    }

    fun subtract(c1: ComplexNumber, c2: ComplexNumber): ComplexNumber {
        return ComplexNumber(c1.real - c2.real, c1.imaginary - c2.imaginary)
    }

    fun multiply(c1: ComplexNumber, c2: ComplexNumber): ComplexNumber {
        val real = c1.real * c2.real - c1.imaginary * c2.imaginary
        val imaginary = c1.real * c2.imaginary + c1.imaginary * c2.real
        return ComplexNumber(real, imaginary)
    }

    fun divide(c1: ComplexNumber, c2: ComplexNumber): ComplexNumber? {
        val denominator = c2.real * c2.real + c2.imaginary * c2.imaginary
        if (denominator == 0.0) return null
        val real = (c1.real * c2.real + c1.imaginary * c2.imaginary) / denominator
        val imaginary = (c1.imaginary * c2.real - c1.real * c2.imaginary) / denominator
        return ComplexNumber(real, imaginary)
    }

    fun magnitude(c: ComplexNumber): Double {
        return sqrt(c.real * c.real + c.imaginary * c.imaginary)
    }

    fun argument(c: ComplexNumber): Double {
        return atan2(c.imaginary, c.real)
    }

    fun toPolar(c: ComplexNumber): Pair<Double, Double> {
        return magnitude(c) to argument(c)
    }

    fun fromPolar(magnitude: Double, argumentRadians: Double): ComplexNumber {
        return ComplexNumber(
            magnitude * kotlin.math.cos(argumentRadians),
            magnitude * kotlin.math.sin(argumentRadians)
        )
    }

    fun conjugate(c: ComplexNumber): ComplexNumber {
        return ComplexNumber(c.real, -c.imaginary)
    }

    fun reciprocal(c: ComplexNumber): ComplexNumber? {
        val denominator = c.real * c.real + c.imaginary * c.imaginary
        if (denominator == 0.0) return null
        return ComplexNumber(c.real / denominator, -c.imaginary / denominator)
    }
}
