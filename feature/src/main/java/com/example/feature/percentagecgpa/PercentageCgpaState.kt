package com.example.feature.percentagecgpa

enum class PercentageToolType(val displayName: String) {
    PERCENTAGE_OF_NUMBER("P% of Number"),
    X_IS_WHAT_P_OF_Y("X is what % of Y"),
    PERCENTAGE_INCREASE("% Increase"),
    PERCENTAGE_DECREASE("% Decrease"),
    PERCENTAGE_DIFFERENCE("% Difference"),
    ADD_PERCENTAGE("Add Percentage"),
    SUBTRACT_PERCENTAGE("Subtract Percentage"),
    DISCOUNT("Discount"),
    GST_TAX("GST / Tax"),
    MARKS("Marks Percentage")
}

data class Semester(
    val id: String,
    val name: String,
    val gpa: Double,
    val credits: Double
)

data class PercentageCgpaState(
    // Percentage Calculator State
    val activeTool: PercentageToolType = PercentageToolType.PERCENTAGE_OF_NUMBER,
    
    // Generic Inputs for Percentage Calculator
    val inputA: String = "", 
    val inputB: String = "", 
    val isGstInclusive: Boolean = false, 
    
    // Result of Percentage Calculator
    val percentageResult: String = "",
    val percentageResultLabel: String = "",
    val percentageSecondaryResult: String = "", 
    val percentageError: String? = null,
    
    // CGPA Calculator State
    val semesters: List<Semester> = emptyList(),
    val cgpaFormulaMultiplier: String = "9.5", 
    val isCreditBased: Boolean = true, 
    
    // Calculated CGPA Results
    val calculatedCgpa: Double = 0.0,
    val calculatedPercentage: Double = 0.0,
    val totalCredits: Double = 0.0
)
