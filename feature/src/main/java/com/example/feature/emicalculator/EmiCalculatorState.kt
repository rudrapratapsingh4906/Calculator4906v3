package com.example.feature.emicalculator

data class PartPayment(
    val id: String,
    val month: Int,
    val amount: Double
)

data class AmortizationRow(
    val month: Int,
    val startBalance: Double,
    val interestPaid: Double,
    val principalPaid: Double,
    val extraPaid: Double,
    val totalPaid: Double,
    val endBalance: Double
)

enum class TenureUnit(val displayName: String) {
    YEARS("Years"),
    MONTHS("Months")
}

enum class FeeType(val displayName: String) {
    PERCENTAGE("%"),
    FLAT("Flat")
}

data class EmiCalculatorState(
    // Standard Inputs
    val loanAmount: String = "",
    val interestRate: String = "",
    val tenureValue: String = "",
    val tenureUnit: TenureUnit = TenureUnit.YEARS,
    
    // Advanced Inputs
    val downPayment: String = "",
    val processingFeeValue: String = "",
    val processingFeeType: FeeType = FeeType.PERCENTAGE,
    val extraMonthlyPayment: String = "",
    val partPayments: List<PartPayment> = emptyList(),
    
    // Calculated Outputs
    val monthlyEmi: Double = 0.0,
    val totalInterest: Double = 0.0,
    val totalPayment: Double = 0.0,
    val actualTenureMonths: Int = 0,
    val processingFeeCalculated: Double = 0.0,
    
    // Savings and Comparison with Standard Loan
    val savingsInterest: Double = 0.0,
    val savingsMonths: Int = 0,
    val standardTotalInterest: Double = 0.0,
    val standardTotalPayment: Double = 0.0,
    val standardMonthlyEmi: Double = 0.0,
    
    // Amortization Schedule
    val schedule: List<AmortizationRow> = emptyList(),
    
    // UI states
    val isAdvancedExpanded: Boolean = false,
    val error: String? = null
)
