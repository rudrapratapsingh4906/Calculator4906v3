package com.example.feature.currencyconverter.domain

interface CurrencyRepository {
    /**
     * Gets the full list of supported currencies.
     */
    suspend fun getCurrencies(): List<Currency>

    /**
     * Toggles the favorite status of a currency.
     */
    suspend fun toggleFavorite(currencyCode: String)

    /**
     * Gets all currently favorited currency codes.
     */
    suspend fun getFavorites(): Set<String>

    /**
     * Gets exchange rates relative to a base currency code.
     * Ready for future API integration (currently offline).
     */
    suspend fun getExchangeRates(baseCode: String): Map<String, Double>

    /**
     * Checks if online API features are enabled.
     * Currently disabled by default.
     */
    suspend fun isOnlineModeEnabled(): Boolean

    /**
     * Checks if a mock/future online sync is successful.
     */
    suspend fun syncRates(): Boolean
}
