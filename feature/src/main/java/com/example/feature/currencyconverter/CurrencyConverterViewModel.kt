package com.example.feature.currencyconverter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Calculation
import com.example.domain.repository.CalculationRepository
import com.example.feature.currencyconverter.data.OfflineCurrencyRepositoryImpl
import com.example.feature.currencyconverter.domain.Currency
import com.example.feature.currencyconverter.domain.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class CurrencyConverterViewModel(
    private val calculationRepository: CalculationRepository,
    context: Context,
    private val currencyRepository: CurrencyRepository = OfflineCurrencyRepositoryImpl(context)
) : ViewModel() {

    private val prefs = context.getSharedPreferences("currency_converter_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(CurrencyConverterState())
    val state: StateFlow<CurrencyConverterState> = _state.asStateFlow()

    init {
        val lastSource = prefs.getString("last_source_currency", "USD") ?: "USD"
        val lastTarget = prefs.getString("last_target_currency", "EUR") ?: "EUR"
        val lastAmount = prefs.getString("last_source_amount", "1") ?: "1"

        _state.update {
            it.copy(
                sourceCurrencyCode = lastSource,
                targetCurrencyCode = lastTarget,
                sourceAmount = lastAmount
            )
        }

        loadCurrencies()
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            val list = currencyRepository.getCurrencies()
            val sourceCur = list.firstOrNull { it.code == _state.value.sourceCurrencyCode }
                ?: list.firstOrNull { it.code == "USD" }
            val targetCur = list.firstOrNull { it.code == _state.value.targetCurrencyCode }
                ?: list.firstOrNull { it.code == "EUR" }

            _state.update {
                it.copy(
                    currencies = list,
                    sourceCurrency = sourceCur,
                    targetCurrency = targetCur
                )
            }
            calculateConversion()
        }
    }

    fun onEvent(event: CurrencyConverterEvent) {
        when (event) {
            is CurrencyConverterEvent.SourceAmountChanged -> {
                prefs.edit().putString("last_source_amount", event.amount).apply()
                _state.update { it.copy(sourceAmount = event.amount) }
                calculateConversion()
            }
            CurrencyConverterEvent.SwapCurrencies -> {
                val currentSource = _state.value.sourceCurrencyCode
                val currentTarget = _state.value.targetCurrencyCode
                
                prefs.edit()
                    .putString("last_source_currency", currentTarget)
                    .putString("last_target_currency", currentSource)
                    .apply()

                _state.update {
                    it.copy(
                        sourceCurrencyCode = currentTarget,
                        targetCurrencyCode = currentSource,
                        sourceCurrency = it.targetCurrency,
                        targetCurrency = it.sourceCurrency
                    )
                }
                calculateConversion()
            }
            is CurrencyConverterEvent.SelectSourceCurrency -> {
                prefs.edit().putString("last_source_currency", event.code).apply()
                _state.update {
                    it.copy(
                        sourceCurrencyCode = event.code,
                        sourceCurrency = it.currencies.firstOrNull { c -> c.code == event.code },
                        isSelectingSource = false,
                        searchQuery = ""
                    )
                }
                calculateConversion()
            }
            is CurrencyConverterEvent.SelectTargetCurrency -> {
                prefs.edit().putString("last_target_currency", event.code).apply()
                _state.update {
                    it.copy(
                        targetCurrencyCode = event.code,
                        targetCurrency = it.currencies.firstOrNull { c -> c.code == event.code },
                        isSelectingTarget = false,
                        searchQuery = ""
                    )
                }
                calculateConversion()
            }
            is CurrencyConverterEvent.ToggleFavorite -> {
                viewModelScope.launch {
                    currencyRepository.toggleFavorite(event.code)
                    loadCurrencies()
                }
            }
            is CurrencyConverterEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            CurrencyConverterEvent.OpenSourceSelection -> {
                _state.update { it.copy(isSelectingSource = true, searchQuery = "") }
            }
            CurrencyConverterEvent.OpenTargetSelection -> {
                _state.update { it.copy(isSelectingTarget = true, searchQuery = "") }
            }
            CurrencyConverterEvent.DismissSelection -> {
                _state.update { it.copy(isSelectingSource = false, isSelectingTarget = false, searchQuery = "") }
            }
            CurrencyConverterEvent.ClearAll -> {
                prefs.edit().putString("last_source_amount", "").apply()
                _state.update {
                    it.copy(
                        sourceAmount = "",
                        targetAmount = 0.0,
                        error = null
                    )
                }
            }
            CurrencyConverterEvent.SaveToHistory -> {
                saveToHistory()
            }
        }
    }

    private fun calculateConversion() {
        val s = _state.value
        val sourceCur = s.sourceCurrency
        val targetCur = s.targetCurrency

        if (sourceCur == null || targetCur == null) {
            _state.update { it.copy(targetAmount = 0.0, error = null) }
            return
        }

        if (s.sourceAmount.isEmpty()) {
            _state.update { it.copy(targetAmount = 0.0, error = null) }
            return
        }

        val amount = s.sourceAmount.toDoubleOrNull()
        if (amount == null) {
            _state.update { it.copy(targetAmount = 0.0, error = "Please enter a valid numeric amount.") }
            return
        }

        if (amount < 0) {
            _state.update { it.copy(targetAmount = 0.0, error = "Amount cannot be negative.") }
            return
        }

        // amount in USD = amount / sourceCur.rateToUsd
        // amount in target = amount in USD * targetCur.rateToUsd
        val usdAmount = amount / sourceCur.rateToUsd
        val result = usdAmount * targetCur.rateToUsd

        _state.update {
            it.copy(
                targetAmount = result,
                error = null
            )
        }
    }

    private fun saveToHistory() {
        val s = _state.value
        val sourceCur = s.sourceCurrency
        val targetCur = s.targetCurrency
        val amount = s.sourceAmount.toDoubleOrNull()

        if (sourceCur != null && targetCur != null && amount != null && amount > 0 && s.error == null) {
            val formattedSource = formatDouble(amount, 2)
            val formattedTarget = formatDouble(s.targetAmount, 2)
            
            val expression = "Convert: $formattedSource ${sourceCur.code} to ${targetCur.code}"
            val result = "$formattedTarget ${targetCur.code} (Rate: 1 ${sourceCur.code} = ${formatDouble(targetCur.rateToUsd / sourceCur.rateToUsd, 4)} ${targetCur.code})"

            viewModelScope.launch {
                val calculation = Calculation(
                    id = UUID.randomUUID().toString(),
                    expression = expression,
                    result = result,
                    timestamp = System.currentTimeMillis()
                )
                calculationRepository.saveCalculation(calculation)
            }
        }
    }

    private fun formatDouble(value: Double, decimals: Int): String {
        if (value.isNaN() || value.isInfinite()) return "0.0"
        val bd = BigDecimal(value.toString())
            .setScale(decimals, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        return bd.toPlainString()
    }
}
