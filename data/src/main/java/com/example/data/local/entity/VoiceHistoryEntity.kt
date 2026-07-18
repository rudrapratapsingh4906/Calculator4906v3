package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_history")
data class VoiceHistoryEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val action: String,
    val timestamp: Long
)
