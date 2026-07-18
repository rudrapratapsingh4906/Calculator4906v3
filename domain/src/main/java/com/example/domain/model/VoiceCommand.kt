package com.example.domain.model

data class VoiceCommand(
    val commandId: String,
    val commandName: String,
    val aliases: List<String>,
    val defaultAliases: List<String>,
    val isCustom: Boolean = false
)
