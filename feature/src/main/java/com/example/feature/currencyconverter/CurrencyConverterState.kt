package com.example.feature.currencyconverter

import com.example.feature.currencyconverter.domain.Currency

data class CurrencyConverterState(
    // Selection state
    val sourceCurrencyCode: String = "USD",
    val targetCurrencyCode: String = "EUR",
    
    // Loaded currencies
    val sourceCurrency: Currency? = null,
    val targetCurrency: Currency? = null,
    val currencies: List<Currency> = emptyList(),
    
    // Input/output values
    val sourceAmount: String = "1",
    val targetAmount: Double = 0.0,
    
    // Dialog / Selection views
    val isSelectingSource: Boolean = false,
    val isSelectingTarget: Boolean = false,
    val searchQuery: String = "",
    
    // Error state
    val error: String? = null
)
