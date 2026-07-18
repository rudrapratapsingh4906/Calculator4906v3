package com.example.domain.repository

import com.example.domain.model.VoiceHistory
import kotlinx.coroutines.flow.Flow

interface VoiceHistoryRepository {
    fun getVoiceHistory(): Flow<List<VoiceHistory>>
    suspend fun saveVoiceHistory(history: VoiceHistory)
    suspend fun clearVoiceHistory()
}
