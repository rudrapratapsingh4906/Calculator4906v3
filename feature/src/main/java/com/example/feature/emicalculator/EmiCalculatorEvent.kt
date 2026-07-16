package com.example.feature.emicalculator

sealed interface EmiCalculatorEvent {
    data class LoanAmountChanged(val value: String) : EmiCalculatorEvent
    data class InterestRateChanged(val value: String) : EmiCalculatorEvent
    data class TenureValueChanged(val value: String) : EmiCalculatorEvent
    data class TenureUnitChanged(val unit: TenureUnit) : EmiCalculatorEvent
    
    // Advanced Options
    data class DownPaymentChanged(val value: String) : EmiCalculatorEvent
    data class ProcessingFeeValueChanged(val value: String) : EmiCalculatorEvent
    data class ProcessingFeeTypeChanged(val type: FeeType) : EmiCalculatorEvent
    data class ExtraMonthlyPaymentChanged(val value: String) : EmiCalculatorEvent
    
    // Part Prepayment
    data class AddPartPayment(val month: Int, val amount: Double) : EmiCalculatorEvent
    data class DeletePartPayment(val id: String) : EmiCalculatorEvent
    
    // Commands
    object ToggleAdvancedExpanded : EmiCalculatorEvent
    object ClearAll : EmiCalculatorEvent
    object SaveToHistory : EmiCalculatorEvent
}
