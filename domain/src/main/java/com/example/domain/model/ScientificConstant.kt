package com.example.domain.model

data class ScientificConstant(
    val id: String,
    val name: String,
    val symbol: String,
    val value: String,
    val unit: String,
    val description: String,
    val category: ConstantCategory,
    val field: String,
    val isFavorite: Boolean = false
)
