package com.example.domain.repository

import com.example.domain.model.GraphHistoryItem

interface GraphRepository {
    fun saveToHistory(item: GraphHistoryItem)
    fun getHistory(): List<GraphHistoryItem>
    fun clearHistory()
}
