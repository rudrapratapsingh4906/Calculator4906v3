package com.example.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val themeFlow: StateFlow<String>
    val themeModeFlow: StateFlow<String>
    val vibrationEnabledFlow: StateFlow<Boolean>
    val soundEnabledFlow: StateFlow<Boolean>
    val orientationLockFlow: StateFlow<Boolean>
    val backgroundImageUriFlow: StateFlow<String?>
    val backgroundOpacityFlow: StateFlow<Float>
    val advancedVoiceModeEnabledFlow: StateFlow<Boolean>
    val voiceAutoStartEnabledFlow: StateFlow<Boolean>
    val continuousListeningEnabledFlow: StateFlow<Boolean>
    fun setTheme(theme: String)
    fun setThemeMode(themeMode: String)
    fun setVibrationEnabled(enabled: Boolean)
    fun setSoundEnabled(enabled: Boolean)
    fun setOrientationLock(locked: Boolean)
    fun setBackgroundImageUri(uri: String?)
    fun setBackgroundOpacity(opacity: Float)
    fun setAdvancedVoiceModeEnabled(enabled: Boolean)
    fun setVoiceAutoStartEnabled(enabled: Boolean)
    fun setContinuousListeningEnabled(enabled: Boolean)
}
