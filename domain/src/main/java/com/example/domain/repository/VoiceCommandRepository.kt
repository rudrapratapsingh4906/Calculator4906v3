package com.example.domain.repository

import com.example.domain.model.VoiceCommand
import kotlinx.coroutines.flow.Flow

interface VoiceCommandRepository {
    fun getVoiceCommandsFlow(): Flow<List<VoiceCommand>>
    suspend fun getVoiceCommands(): List<VoiceCommand>
    suspend fun saveVoiceCommand(command: VoiceCommand)
    suspend fun resetToDefault(commandId: String)
    suspend fun resetAllToDefault()
}
