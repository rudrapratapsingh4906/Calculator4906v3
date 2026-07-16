package com.example.domain.model

sealed class Equation {
    data class Linear(val a: Double, val b: Double, val c: Double) : Equation() // ax + b = c
    data class Quadratic(val a: Double, val b: Double, val c: Double) : Equation() // ax^2 + bx + c = 0
    // Add others as needed
}
