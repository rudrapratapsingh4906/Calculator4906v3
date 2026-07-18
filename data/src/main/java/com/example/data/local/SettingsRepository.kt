package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(context: Context) : com.example.domain.repository.SettingsRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(prefs.getString("theme", "Default") ?: "Default")
    override val themeFlow: StateFlow<String> = _themeFlow.asStateFlow()
    private val _themeModeFlow = MutableStateFlow(prefs.getString("theme_mode", "System") ?: "System")
    override val themeModeFlow: StateFlow<String> = _themeModeFlow.asStateFlow()
    private val _vibrationEnabledFlow = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    override val vibrationEnabledFlow: StateFlow<Boolean> = _vibrationEnabledFlow.asStateFlow()
    private val _soundEnabledFlow = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    override val soundEnabledFlow: StateFlow<Boolean> = _soundEnabledFlow.asStateFlow()
    private val _orientationLockFlow = MutableStateFlow(prefs.getBoolean("orientation_lock", false))
    override val orientationLockFlow: StateFlow<Boolean> = _orientationLockFlow.asStateFlow()
    private val _backgroundImageUriFlow = MutableStateFlow(prefs.getString("background_image_uri", null))
    override val backgroundImageUriFlow: StateFlow<String?> = _backgroundImageUriFlow.asStateFlow()
    private val _backgroundOpacityFlow = MutableStateFlow(prefs.getFloat("background_opacity", 1.0f))
    override val backgroundOpacityFlow: StateFlow<Float> = _backgroundOpacityFlow.asStateFlow()

    private val _advancedVoiceModeEnabledFlow = MutableStateFlow(prefs.getBoolean("advanced_voice_mode_enabled", false))
    override val advancedVoiceModeEnabledFlow: StateFlow<Boolean> = _advancedVoiceModeEnabledFlow.asStateFlow()
    private val _voiceAutoStartEnabledFlow = MutableStateFlow(prefs.getBoolean("voice_auto_start_enabled", false))
    override val voiceAutoStartEnabledFlow: StateFlow<Boolean> = _voiceAutoStartEnabledFlow.asStateFlow()
    private val _continuousListeningEnabledFlow = MutableStateFlow(prefs.getBoolean("continuous_listening_enabled", false))
    override val continuousListeningEnabledFlow: StateFlow<Boolean> = _continuousListeningEnabledFlow.asStateFlow()

    override fun setTheme(theme: String) {
        prefs.edit().putString("theme", theme).apply()
        _themeFlow.value = theme
    }
    override fun setThemeMode(themeMode: String) {
        prefs.edit().putString("theme_mode", themeMode).apply()
        _themeModeFlow.value = themeMode
    }
    override fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _vibrationEnabledFlow.value = enabled
    }
    override fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _soundEnabledFlow.value = enabled
    }
    override fun setOrientationLock(locked: Boolean) {
        prefs.edit().putBoolean("orientation_lock", locked).apply()
        _orientationLockFlow.value = locked
    }
    override fun setBackgroundImageUri(uri: String?) {
        prefs.edit().putString("background_image_uri", uri).apply()
        _backgroundImageUriFlow.value = uri
    }
    override fun setBackgroundOpacity(opacity: Float) {
        prefs.edit().putFloat("background_opacity", opacity).apply()
        _backgroundOpacityFlow.value = opacity
    }
    override fun setAdvancedVoiceModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("advanced_voice_mode_enabled", enabled).apply()
        _advancedVoiceModeEnabledFlow.value = enabled
    }
    override fun setVoiceAutoStartEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("voice_auto_start_enabled", enabled).apply()
        _voiceAutoStartEnabledFlow.value = enabled
    }
    override fun setContinuousListeningEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("continuous_listening_enabled", enabled).apply()
        _continuousListeningEnabledFlow.value = enabled
    }
}
