package com.example.data.repository

import android.content.Context
import com.example.domain.model.GraphHistoryItem
import com.example.domain.repository.GraphRepository

class GraphRepositoryImpl(private val context: Context? = null) : GraphRepository {

    private val sharedPrefs by lazy {
        context?.getSharedPreferences("graph_history_prefs", Context.MODE_PRIVATE)
    }

    private val inMemoryHistory = mutableListOf<GraphHistoryItem>()

    override fun saveToHistory(item: GraphHistoryItem) {
        if (sharedPrefs != null) {
            val current = getHistory().toMutableList()
            current.removeAll { it.equation == item.equation && it.type == item.type }
            current.add(0, item) // add to top
            val serialized = current.take(50).joinToString("\n") { 
                "${it.id}|${it.equation}|${it.type}|${it.color}|${it.minX}|${it.maxX}|${it.minY}|${it.maxY}"
            }
            sharedPrefs?.edit()?.putString("history_list", serialized)?.apply()
        } else {
            inMemoryHistory.removeAll { it.equation == item.equation && it.type == item.type }
            inMemoryHistory.add(0, item)
        }
    }

    override fun getHistory(): List<GraphHistoryItem> {
        if (sharedPrefs != null) {
            val raw = sharedPrefs?.getString("history_list", null) ?: return emptyList()
            if (raw.isBlank()) return emptyList()
            return raw.split("\n").mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size == 8) {
                    try {
                        GraphHistoryItem(
                            id = parts[0].toLong(),
                            equation = parts[1],
                            type = parts[2],
                            color = parts[3],
                            minX = parts[4].toDouble(),
                            maxX = parts[5].toDouble(),
                            minY = parts[6].toDouble(),
                            maxY = parts[7].toDouble()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }
        } else {
            return inMemoryHistory
        }
    }

    override fun clearHistory() {
        if (sharedPrefs != null) {
            sharedPrefs?.edit()?.remove("history_list")?.apply()
        } else {
            inMemoryHistory.clear()
        }
    }
}
