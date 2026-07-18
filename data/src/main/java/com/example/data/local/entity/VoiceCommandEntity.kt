package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_commands")
data class VoiceCommandEntity(
    @PrimaryKey val commandId: String,
    val commandName: String,
    val aliases: String,        // Comma separated list of custom/active aliases
    val defaultAliases: String, // Comma separated list of original aliases for resetting
    val isCustom: Boolean = false
)
