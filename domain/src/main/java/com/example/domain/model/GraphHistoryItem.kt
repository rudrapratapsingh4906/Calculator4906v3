package com.example.domain.model

data class GraphHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val equation: String,
    val type: String,
    val color: String,
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double
)
