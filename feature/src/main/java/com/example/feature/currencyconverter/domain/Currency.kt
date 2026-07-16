package com.example.feature.currencyconverter.domain

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val country: String,
    val rateToUsd: Double,
    val isFavorite: Boolean = false
)
