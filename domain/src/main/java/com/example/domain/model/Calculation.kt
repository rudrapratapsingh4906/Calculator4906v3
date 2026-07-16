package com.example.domain.model

data class Calculation(
    val id: String,
    val expression: String,
    val result: String,
    val timestamp: Long
)
