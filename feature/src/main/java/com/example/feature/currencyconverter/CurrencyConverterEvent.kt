package com.example.feature.currencyconverter

sealed interface CurrencyConverterEvent {
    data class SourceAmountChanged(val amount: String) : CurrencyConverterEvent
    object SwapCurrencies : CurrencyConverterEvent
    data class SelectSourceCurrency(val code: String) : CurrencyConverterEvent
    data class SelectTargetCurrency(val code: String) : CurrencyConverterEvent
    data class ToggleFavorite(val code: String) : CurrencyConverterEvent
    data class SearchQueryChanged(val query: String) : CurrencyConverterEvent
    object OpenSourceSelection : CurrencyConverterEvent
    object OpenTargetSelection : CurrencyConverterEvent
    object DismissSelection : CurrencyConverterEvent
    object ClearAll : CurrencyConverterEvent
    object SaveToHistory : CurrencyConverterEvent
}
