package com.example.feature.emicalculator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Calculation
import com.example.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class EmiCalculatorViewModel(
    private val calculationRepository: CalculationRepository,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("emi_calculator_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(EmiCalculatorState())
    val state: StateFlow<EmiCalculatorState> = _state.asStateFlow()

    init {
        // Load saved states
        val loanAmount = prefs.getString("emi_loan_amount", "") ?: ""
        val interestRate = prefs.getString("emi_interest_rate", "") ?: ""
        val tenureValue = prefs.getString("emi_tenure_value", "") ?: ""
        val tenureUnitStr = prefs.getString("emi_tenure_unit", TenureUnit.YEARS.name) ?: TenureUnit.YEARS.name
        val tenureUnit = runCatching { TenureUnit.valueOf(tenureUnitStr) }.getOrDefault(TenureUnit.YEARS)
        
        val downPayment = prefs.getString("emi_down_payment", "") ?: ""
        val processingFeeValue = prefs.getString("emi_processing_fee_value", "") ?: ""
        val processingFeeTypeStr = prefs.getString("emi_processing_fee_type", FeeType.PERCENTAGE.name) ?: FeeType.PERCENTAGE.name
        val processingFeeType = runCatching { FeeType.valueOf(processingFeeTypeStr) }.getOrDefault(FeeType.PERCENTAGE)
        val extraMonthlyPayment = prefs.getString("emi_extra_monthly_payment", "") ?: ""
        
        _state.update {
            it.copy(
                loanAmount = loanAmount,
                interestRate = interestRate,
                tenureValue = tenureValue,
                tenureUnit = tenureUnit,
                downPayment = downPayment,
                processingFeeValue = processingFeeValue,
                processingFeeType = processingFeeType,
                extraMonthlyPayment = extraMonthlyPayment,
                partPayments = loadPartPayments()
            )
        }
        
        calculateEmi()
    }

    fun onEvent(event: EmiCalculatorEvent) {
        when (event) {
            is EmiCalculatorEvent.LoanAmountChanged -> {
                prefs.edit().putString("emi_loan_amount", event.value).apply()
                _state.update { it.copy(loanAmount = event.value) }
                calculateEmi()
            }
            is EmiCalculatorEvent.InterestRateChanged -> {
                prefs.edit().putString("emi_interest_rate", event.value).apply()
                _state.update { it.copy(interestRate = event.value) }
                calculateEmi()
            }
            is EmiCalculatorEvent.TenureValueChanged -> {
                prefs.edit().putString("emi_tenure_value", event.value).apply()
                _state.update { it.copy(tenureValue = event.value) }
                calculateEmi()
            }
            is EmiCalculatorEvent.TenureUnitChanged -> {
                prefs.edit().putString("emi_tenure_unit", event.unit.name).apply()
                _state.update { it.copy(tenureUnit = event.unit) }
                calculateEmi()
            }
            is EmiCalculatorEvent.DownPaymentChanged -> {
                prefs.edit().putString("emi_down_payment", event.value).apply()
                _state.update { it.copy(downPayment = event.value) }
                calculateEmi()
            }
            is EmiCalculatorEvent.ProcessingFeeValueChanged -> {
                prefs.edit().putString("emi_processing_fee_value", event.value).apply()
                _state.update { it.copy(processingFeeValue = event.value) }
                calculateEmi()
            }
            is EmiCalculatorEvent.ProcessingFeeTypeChanged -> {
                prefs.edit().putString("emi_processing_fee_type", event.type.name).apply()
                _state.update { it.copy(processingFeeType = event.type) }
                calculateEmi()
            }
            is EmiCalculatorEvent.ExtraMonthlyPaymentChanged -> {
                prefs.edit().putString("emi_extra_monthly_payment", event.value).apply()
                _state.update { it.copy(extraMonthlyPayment = event.value) }
                calculateEmi()
            }
            is EmiCalculatorEvent.AddPartPayment -> {
                val newPart = PartPayment(
                    id = UUID.randomUUID().toString(),
                    month = event.month,
                    amount = event.amount
                )
                val updated = _state.value.partPayments + newPart
                _state.update { it.copy(partPayments = updated) }
                savePartPayments(updated)
                calculateEmi()
            }
            is EmiCalculatorEvent.DeletePartPayment -> {
                val updated = _state.value.partPayments.filterNot { it.id == event.id }
                _state.update { it.copy(partPayments = updated) }
                savePartPayments(updated)
                calculateEmi()
            }
            EmiCalculatorEvent.ToggleAdvancedExpanded -> {
                _state.update { it.copy(isAdvancedExpanded = !it.isAdvancedExpanded) }
            }
            EmiCalculatorEvent.ClearAll -> {
                prefs.edit()
                    .putString("emi_loan_amount", "")
                    .putString("emi_interest_rate", "")
                    .putString("emi_tenure_value", "")
                    .putString("emi_down_payment", "")
                    .putString("emi_processing_fee_value", "")
                    .putString("emi_extra_monthly_payment", "")
                    .putString("emi_part_payments", "")
                    .apply()
                _state.update {
                    it.copy(
                        loanAmount = "",
                        interestRate = "",
                        tenureValue = "",
                        downPayment = "",
                        processingFeeValue = "",
                        extraMonthlyPayment = "",
                        partPayments = emptyList(),
                        monthlyEmi = 0.0,
                        totalInterest = 0.0,
                        totalPayment = 0.0,
                        actualTenureMonths = 0,
                        processingFeeCalculated = 0.0,
                        savingsInterest = 0.0,
                        savingsMonths = 0,
                        standardTotalInterest = 0.0,
                        standardTotalPayment = 0.0,
                        standardMonthlyEmi = 0.0,
                        schedule = emptyList(),
                        error = null
                    )
                }
            }
            EmiCalculatorEvent.SaveToHistory -> {
                saveToHistory()
            }
        }
    }

    private fun calculateEmi() {
        val s = _state.value
        val loanAmountVal = s.loanAmount.toDoubleOrNull()
        val interestRateVal = s.interestRate.toDoubleOrNull()
        val tenureValueVal = s.tenureValue.toDoubleOrNull()

        if (loanAmountVal == null || interestRateVal == null || tenureValueVal == null) {
            _state.update {
                it.copy(
                    monthlyEmi = 0.0,
                    totalInterest = 0.0,
                    totalPayment = 0.0,
                    actualTenureMonths = 0,
                    processingFeeCalculated = 0.0,
                    savingsInterest = 0.0,
                    savingsMonths = 0,
                    standardTotalInterest = 0.0,
                    standardTotalPayment = 0.0,
                    standardMonthlyEmi = 0.0,
                    schedule = emptyList(),
                    error = null
                )
            }
            return
        }

        if (loanAmountVal <= 0 || interestRateVal < 0 || tenureValueVal <= 0) {
            _state.update {
                it.copy(
                    error = "Please enter positive values for Loan, Interest, and Tenure"
                )
            }
            return
        }

        val downPaymentVal = s.downPayment.toDoubleOrNull() ?: 0.0
        val principal = loanAmountVal - downPaymentVal

        if (principal <= 0) {
            _state.update {
                it.copy(
                    error = "Down payment cannot exceed or equal the Loan Amount"
                )
            }
            return
        }

        val tenureMonths = if (s.tenureUnit == TenureUnit.YEARS) {
            (tenureValueVal * 12).toInt()
        } else {
            tenureValueVal.toInt()
        }

        if (tenureMonths <= 0) {
            _state.update { it.copy(error = "Tenure must be at least 1 month") }
            return
        }

        val monthlyRate = interestRateVal / 1200.0

        // 1. Processing Fee
        val processingFeeVal = s.processingFeeValue.toDoubleOrNull() ?: 0.0
        val calcedProcessingFee = if (s.processingFeeType == FeeType.PERCENTAGE) {
            principal * (processingFeeVal / 100.0)
        } else {
            processingFeeVal
        }

        // 2. Standard EMI (No Prepayments)
        val stdEmi = calculateEmiFormula(principal, monthlyRate, tenureMonths)
        var stdTotalInterest = 0.0
        var stdTotalPayment = 0.0
        val standardSchedule = mutableListOf<AmortizationRow>()
        
        var tempBalance = principal
        var mCount = 0
        while (tempBalance > 0.01 && mCount < 1200) {
            mCount++
            val startBal = tempBalance
            val interest = startBal * monthlyRate
            
            var principalPaid = stdEmi - interest
            if (principalPaid > startBal) {
                principalPaid = startBal
            }
            
            val totalPaid = interest + principalPaid
            val endBal = startBal - principalPaid
            
            stdTotalInterest += interest
            stdTotalPayment += totalPaid
            tempBalance = endBal
        }

        // 3. Actual EMI with Prepayments
        val actualSchedule = mutableListOf<AmortizationRow>()
        val extraMonthly = s.extraMonthlyPayment.toDoubleOrNull() ?: 0.0
        val partPayMap = s.partPayments.associateBy({ it.month }, { it.amount })
        
        var currentBalance = principal
        var actualTotalInterest = 0.0
        var actualTotalPayment = 0.0
        var actualMCount = 0

        while (currentBalance > 0.01 && actualMCount < 1200) {
            actualMCount++
            val startBal = currentBalance
            val interest = startBal * monthlyRate

            // If the monthly interest is greater than standard EMI, standard payment cannot pay down principal
            if (stdEmi <= interest && interest > 0.0) {
                _state.update {
                    it.copy(
                        error = "Interest exceeds standard monthly payment. Reduce interest rate or increase tenure."
                    )
                }
                return
            }

            var stdPrincipalPaid = stdEmi - interest
            if (stdPrincipalPaid > startBal) {
                stdPrincipalPaid = startBal
            }

            // Extra prepayments this month
            val partPayThisMonth = partPayMap[actualMCount] ?: 0.0
            val extraPrepaymentVal = extraMonthly + partPayThisMonth
            
            var extraPrincipalPaid = 0.0
            val maxExtraAllowed = startBal - stdPrincipalPaid
            if (extraPrepaymentVal > 0.0 && maxExtraAllowed > 0.0) {
                extraPrincipalPaid = Math.min(extraPrepaymentVal, maxExtraAllowed)
            }

            val totalPrincipalPaid = stdPrincipalPaid + extraPrincipalPaid
            val totalPaidThisMonth = interest + totalPrincipalPaid
            val endBal = startBal - totalPrincipalPaid

            actualSchedule.add(
                AmortizationRow(
                    month = actualMCount,
                    startBalance = startBal,
                    interestPaid = interest,
                    principalPaid = stdPrincipalPaid,
                    extraPaid = extraPrincipalPaid,
                    totalPaid = totalPaidThisMonth,
                    endBalance = endBal
                )
            )

            actualTotalInterest += interest
            actualTotalPayment += totalPaidThisMonth
            currentBalance = endBal
        }

        val savingsInterest = Math.max(0.0, stdTotalInterest - actualTotalInterest)
        val savingsMonths = Math.max(0, tenureMonths - actualMCount)

        _state.update {
            it.copy(
                monthlyEmi = stdEmi,
                totalInterest = actualTotalInterest,
                totalPayment = actualTotalPayment,
                actualTenureMonths = actualMCount,
                processingFeeCalculated = calcedProcessingFee,
                savingsInterest = savingsInterest,
                savingsMonths = savingsMonths,
                standardTotalInterest = stdTotalInterest,
                standardTotalPayment = stdTotalPayment,
                standardMonthlyEmi = stdEmi,
                schedule = actualSchedule,
                error = null
            )
        }
    }

    private fun calculateEmiFormula(principal: Double, monthlyRate: Double, months: Int): Double {
        if (principal <= 0 || months <= 0) return 0.0
        if (monthlyRate <= 0.0) return principal / months
        val power = Math.pow(1 + monthlyRate, months.toDouble())
        return (principal * monthlyRate * power) / (power - 1)
    }

    private fun savePartPayments(payments: List<PartPayment>) {
        val serialized = payments.joinToString(";") { "${it.id}|${it.month}|${it.amount}" }
        prefs.edit().putString("emi_part_payments", serialized).apply()
    }

    private fun loadPartPayments(): List<PartPayment> {
        val serialized = prefs.getString("emi_part_payments", "") ?: ""
        if (serialized.isEmpty()) return emptyList()
        return serialized.split(";").mapNotNull { part ->
            val subParts = part.split("|")
            if (subParts.size == 3) {
                PartPayment(
                    id = subParts[0],
                    month = subParts[1].toIntOrNull() ?: 0,
                    amount = subParts[2].toDoubleOrNull() ?: 0.0
                )
            } else null
        }
    }

    private fun saveToHistory() {
        val s = _state.value
        if (s.loanAmount.isNotEmpty() && s.error == null) {
            viewModelScope.launch {
                val extraMsg = if (s.savingsInterest > 0.0) {
                    " (Saved ${formatDouble(s.savingsInterest, 2)} interest & ${s.savingsMonths} months)"
                } else ""
                
                val calculation = Calculation(
                    id = UUID.randomUUID().toString(),
                    expression = "EMI: Loan ${s.loanAmount}, Rate ${s.interestRate}%, Tenure ${s.tenureValue} ${s.tenureUnit.displayName}$extraMsg",
                    result = "EMI: ${formatDouble(s.monthlyEmi, 2)}, Total Interest: ${formatDouble(s.totalInterest, 2)}, Total: ${formatDouble(s.totalPayment, 2)}",
                    timestamp = System.currentTimeMillis()
                )
                calculationRepository.saveCalculation(calculation)
            }
        }
    }

    private fun formatDouble(value: Double, decimals: Int = 4): String {
        if (value.isNaN() || value.isInfinite()) return "0.0"
        val bd = BigDecimal(value.toString())
            .setScale(decimals, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        return bd.toPlainString()
    }
}
