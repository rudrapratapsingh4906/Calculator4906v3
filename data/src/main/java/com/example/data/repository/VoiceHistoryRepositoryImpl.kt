package com.example.data.repository

import com.example.data.local.dao.VoiceHistoryDao
import com.example.data.local.entity.VoiceHistoryEntity
import com.example.domain.model.VoiceHistory
import com.example.domain.repository.VoiceHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VoiceHistoryRepositoryImpl(
    private val voiceHistoryDao: VoiceHistoryDao
) : VoiceHistoryRepository {

    override fun getVoiceHistory(): Flow<List<VoiceHistory>> {
        return voiceHistoryDao.getVoiceHistoryFlow().map { entities ->
            entities.map { entity ->
                VoiceHistory(
                    id = entity.id,
                    prompt = entity.prompt,
                    action = entity.action,
                    timestamp = entity.timestamp
                )
            }
        }
    }

    override suspend fun saveVoiceHistory(history: VoiceHistory) {
        voiceHistoryDao.insertVoiceHistory(
            VoiceHistoryEntity(
                id = history.id,
                prompt = history.prompt,
                action = history.action,
                timestamp = history.timestamp
            )
        )
    }

    override suspend fun clearVoiceHistory() {
        voiceHistoryDao.clearVoiceHistory()
    }
}
