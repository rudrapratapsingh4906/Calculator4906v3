package com.example.feature.scientificconstants

import com.example.domain.model.ConstantCategory
import com.example.domain.model.ScientificConstant

sealed class ScientificConstantsEvent {
    data class SelectCategory(val category: ConstantCategory?) : ScientificConstantsEvent()
    data class SearchQueryChange(val query: String) : ScientificConstantsEvent()
    data class ToggleFavorite(val constantId: String) : ScientificConstantsEvent()
    data class SelectConstant(val constant: ScientificConstant?) : ScientificConstantsEvent()
    object ToggleFavoritesOnly : ScientificConstantsEvent()
}
