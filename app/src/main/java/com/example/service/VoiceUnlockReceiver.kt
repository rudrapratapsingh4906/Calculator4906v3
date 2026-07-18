package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VoiceUnlockReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "VoiceUnlockReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive: Received broadcast intent action = $action")
        
        if (action == Intent.ACTION_USER_PRESENT || action == Intent.ACTION_BOOT_COMPLETED) {
            val appContext = context.applicationContext
            val settingsRepository = SettingsRepositoryImpl(appContext)
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val advancedEnabled = settingsRepository.advancedVoiceModeEnabledFlow.first()
                    val autoStartEnabled = settingsRepository.voiceAutoStartEnabledFlow.first()
                    
                    val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        appContext,
                        android.Manifest.permission.RECORD_AUDIO
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    
                    Log.d(TAG, "Voice settings: advancedEnabled=$advancedEnabled, autoStartEnabled=$autoStartEnabled, hasMicPermission=$hasMicPermission")
                    
                    if (advancedEnabled && autoStartEnabled && hasMicPermission) {
                        Log.d(TAG, "Starting VoiceBackgroundService from receiver...")
                        val serviceIntent = Intent(appContext, VoiceBackgroundService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            appContext.startForegroundService(serviceIntent)
                        } else {
                            appContext.startService(serviceIntent)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in VoiceUnlockReceiver coroutine: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
