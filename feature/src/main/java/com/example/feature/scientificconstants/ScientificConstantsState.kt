package com.example.feature.scientificconstants

import com.example.domain.model.ConstantCategory
import com.example.domain.model.ScientificConstant

data class ScientificConstantsState(
    val categories: List<ConstantCategory> = ConstantCategory.values().toList(),
    val selectedCategory: ConstantCategory? = null, // null represents "All"
    val searchQuery: String = "",
    val constants: List<ScientificConstant> = emptyList(),
    val allConstants: List<ScientificConstant> = emptyList(),
    val selectedConstant: ScientificConstant? = null,
    val showFavoritesOnly: Boolean = false
)
