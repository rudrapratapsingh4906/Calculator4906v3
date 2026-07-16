package com.example.feature.percentagecgpa

sealed interface PercentageCgpaEvent {
    // Percentage Events
    data class SelectTool(val tool: PercentageToolType) : PercentageCgpaEvent
    data class InputAChanged(val value: String) : PercentageCgpaEvent
    data class InputBChanged(val value: String) : PercentageCgpaEvent
    data class GstInclusiveChanged(val isInclusive: Boolean) : PercentageCgpaEvent
    object ClearPercentageInputs : PercentageCgpaEvent
    
    // CGPA Events
    data class AddSemester(val name: String, val gpa: Double, val credits: Double) : PercentageCgpaEvent
    data class EditSemester(val id: String, val name: String, val gpa: Double, val credits: Double) : PercentageCgpaEvent
    data class DeleteSemester(val id: String) : PercentageCgpaEvent
    data class FormulaMultiplierChanged(val multiplier: String) : PercentageCgpaEvent
    data class CreditBasedToggled(val isCreditBased: Boolean) : PercentageCgpaEvent
    object ResetCgpa : PercentageCgpaEvent
}
