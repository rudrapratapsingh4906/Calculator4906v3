package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.VoiceCommandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceCommandDao {
    @Query("SELECT * FROM voice_commands")
    fun getAllVoiceCommandsFlow(): Flow<List<VoiceCommandEntity>>

    @Query("SELECT * FROM voice_commands")
    suspend fun getAllVoiceCommands(): List<VoiceCommandEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceCommand(command: VoiceCommandEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceCommands(commands: List<VoiceCommandEntity>)

    @Query("SELECT * FROM voice_commands WHERE commandId = :commandId LIMIT 1")
    suspend fun getVoiceCommandById(commandId: String): VoiceCommandEntity?

    @Query("DELETE FROM voice_commands")
    suspend fun clearVoiceCommands()
}
