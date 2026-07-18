package com.example.domain.model

data class VoiceHistory(
    val id: String,
    val prompt: String,
    val action: String,
    val timestamp: Long
)
