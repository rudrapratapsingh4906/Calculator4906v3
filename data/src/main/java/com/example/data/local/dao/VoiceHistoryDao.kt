package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.VoiceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceHistoryDao {
    @Query("SELECT * FROM voice_history ORDER BY timestamp DESC")
    fun getVoiceHistoryFlow(): Flow<List<VoiceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceHistory(history: VoiceHistoryEntity)

    @Query("DELETE FROM voice_history")
    suspend fun clearVoiceHistory()
}
