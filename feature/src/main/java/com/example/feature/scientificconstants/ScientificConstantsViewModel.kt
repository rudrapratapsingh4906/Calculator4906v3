package com.example.feature.scientificconstants

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.domain.model.ConstantCategory
import com.example.domain.model.ScientificConstant
import com.example.domain.usecase.GetScientificConstantsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScientificConstantsViewModel(
    private val getScientificConstantsUseCase: GetScientificConstantsUseCase,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("scientific_constants_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(ScientificConstantsState())
    val state: StateFlow<ScientificConstantsState> = _state.asStateFlow()

    init {
        loadConstants()
    }

    private fun loadConstants() {
        val baseConstants = getScientificConstantsUseCase()
        val favoriteIds = prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()

        val allConstantsWithFavorites = baseConstants.map { constant ->
            constant.copy(isFavorite = favoriteIds.contains(constant.id))
        }

        _state.update {
            it.copy(
                allConstants = allConstantsWithFavorites
            )
        }
        applyFiltersAndSearch()
    }

    fun onEvent(event: ScientificConstantsEvent) {
        when (event) {
            is ScientificConstantsEvent.SelectCategory -> {
                _state.update { it.copy(selectedCategory = event.category) }
                applyFiltersAndSearch()
            }
            is ScientificConstantsEvent.SearchQueryChange -> {
                _state.update { it.copy(searchQuery = event.query) }
                applyFiltersAndSearch()
            }
            is ScientificConstantsEvent.ToggleFavorite -> {
                toggleFavoriteInPrefs(event.constantId)
            }
            is ScientificConstantsEvent.SelectConstant -> {
                _state.update { it.copy(selectedConstant = event.constant) }
            }
            ScientificConstantsEvent.ToggleFavoritesOnly -> {
                _state.update { it.copy(showFavoritesOnly = !it.showFavoritesOnly) }
                applyFiltersAndSearch()
            }
        }
    }

    private fun toggleFavoriteInPrefs(constantId: String) {
        val currentFavorites = prefs.getStringSet("favorite_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentFavorites.contains(constantId)) {
            currentFavorites.remove(constantId)
        } else {
            currentFavorites.add(constantId)
        }
        prefs.edit().putStringSet("favorite_ids", currentFavorites).apply()

        // Update in state
        _state.update { currentState ->
            val updatedAll = currentState.allConstants.map { constant ->
                if (constant.id == constantId) {
                    constant.copy(isFavorite = !constant.isFavorite)
                } else {
                    constant
                }
            }
            // Update selectedConstant if it's currently showing
            val updatedSelected = currentState.selectedConstant?.let { selected ->
                if (selected.id == constantId) selected.copy(isFavorite = !selected.isFavorite) else selected
            }
            currentState.copy(
                allConstants = updatedAll,
                selectedConstant = updatedSelected
            )
        }
        applyFiltersAndSearch()
    }

    private fun applyFiltersAndSearch() {
        _state.update { currentState ->
            var filtered = currentState.allConstants

            // Category filtering
            if (currentState.selectedCategory != null) {
                filtered = filtered.filter { it.category == currentState.selectedCategory }
            }

            // Favorites filtering
            if (currentState.showFavoritesOnly) {
                filtered = filtered.filter { it.isFavorite }
            }

            // Search filtering
            val query = currentState.searchQuery.trim()
            if (query.isNotEmpty()) {
                filtered = filtered.filter { constant ->
                    constant.name.contains(query, ignoreCase = true) ||
                    constant.symbol.contains(query, ignoreCase = true) ||
                    constant.category.displayName.contains(query, ignoreCase = true) ||
                    constant.field.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(constants = filtered)
        }
    }
}
