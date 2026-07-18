package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsRepositoryImpl
import com.example.data.repository.VoiceCommandRepositoryImpl
import com.example.data.repository.VoiceHistoryRepositoryImpl
import com.example.domain.model.VoiceHistory
import com.example.domain.scanner.AITutorEngine
import com.example.domain.scanner.AITutorMode
import com.example.domain.scanner.MathKnowledgeBase
import com.example.domain.scanner.TextUnderstandingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class VoiceBackgroundService : Service(), TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "VoiceBackgroundService"
        private const val CHANNEL_ID = "voice_ai_service_channel"
        private const val NOTIFICATION_ID = 4906
        
        // Actions
        const val ACTION_START_LISTENING = "com.example.service.ACTION_START_LISTENING"
        const val ACTION_STOP_LISTENING = "com.example.service.ACTION_STOP_LISTENING"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Dependencies
    private val db by lazy { AppDatabase.getInstance(applicationContext) }
    private val settingsRepository by lazy { SettingsRepositoryImpl(applicationContext) }
    private val voiceCommandRepository by lazy { VoiceCommandRepositoryImpl(db.voiceCommandDao) }
    private val voiceHistoryRepository by lazy { VoiceHistoryRepositoryImpl(db.voiceHistoryDao) }

    // Audio/Speech Engines
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private var isListening = false
    private var isSpeechRate = 1.0f

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Initializing background voice service")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Voice AI is initializing..."))

        // Init SpeechRecognizer & TTS
        initializeSpeechRecognizer()
        tts = TextToSpeech(applicationContext, this)

        // Observe Settings & Start/Stop listening based on states
        serviceScope.launch {
            val advancedEnabled = settingsRepository.advancedVoiceModeEnabledFlow.first()
            if (advancedEnabled) {
                updateNotification("Voice AI is listening for wake phrase...")
                startContinuousListening()
            } else {
                updateNotification("Voice AI background service is idle")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action = ${intent?.action}")
        when (intent?.action) {
            ACTION_START_LISTENING -> {
                startContinuousListening()
            }
            ACTION_STOP_LISTENING -> {
                stopContinuousListening()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsReady = true
                tts?.setSpeechRate(isSpeechRate)
                Log.d(TAG, "TTS Initialized successfully")
            }
        } else {
            Log.e(TAG, "Failed to initialize TTS")
        }
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(applicationContext)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "SpeechRecognizer ready")
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
                }

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech engine busy"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard"
                        else -> "Unknown speech error"
                    }
                    Log.e(TAG, "Speech recognizer error: $message ($error)")
                    isListening = false
                    
                    // Do not restart on critical errors
                    if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS || error == SpeechRecognizer.ERROR_CLIENT) {
                        Log.e(TAG, "Critical error. Stopping continuous listening.")
                        stopContinuousListening()
                        return
                    }

                    // Restart if continuous listening is enabled
                    serviceScope.launch {
                        delay(1000)
                        checkAndRestartListeningIfNeeded()
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        Log.d(TAG, "Speech recognition result: $spokenText")
                        processSpeechInput(spokenText)
                    }
                    isListening = false
                    // Restart
                    serviceScope.launch {
                        delay(500)
                        checkAndRestartListeningIfNeeded()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } else {
            Log.e(TAG, "SpeechRecognizer is not available on this device")
        }
    }

    private fun processSpeechInput(input: String) {
        val spoken = input.lowercase().trim()
        
        serviceScope.launch {
            // Check custom wake word
            val commands = voiceCommandRepository.getVoiceCommandsFlow().first()
            val wakeCommand = commands.find { it.commandId == "wake_phrase" }
            var processedSpoken = spoken
            var matchedWake = false

            if (wakeCommand != null) {
                val matchedWakeAlias = wakeCommand.aliases.find { alias ->
                    spoken.startsWith(alias) || com.example.core.util.FuzzyMatcher.isMatch(spoken.take(alias.length + 2), listOf(alias))
                }
                if (matchedWakeAlias != null) {
                    matchedWake = true
                    processedSpoken = spoken.removePrefix(matchedWakeAlias).trim()
                }
            } else {
                // Default fallback if wake command doesn't exist
                if (spoken.startsWith("hello tutor") || spoken.startsWith("hey tutor")) {
                    matchedWake = true
                    processedSpoken = spoken.removePrefix("hello tutor").removePrefix("hey tutor").trim()
                }
            }

            // If advanced voice mode is on, we require wake phrase or we can process directly if continuous is active
            val requireWake = true // we can enforce wake phrase for safety
            if (requireWake && !matchedWake) {
                // We did not hear the wake phrase, so skip processing
                Log.d(TAG, "Skipping: wake phrase not detected in '$spoken'")
                return@launch
            }

            // If wake phrase matched but text is empty, greet user
            if (processedSpoken.isEmpty()) {
                val replies = listOf(
                    "Haan ji, main sun raha hoon! Background se boliye, kya math solve karna hai?",
                    "Hello! Yes, I'm listening. Speak your math problem.",
                    "Yes, please speak! Main active hoon."
                )
                val reply = replies[Math.abs(input.hashCode()) % replies.size]
                speak(reply)
                saveHistory(input, reply)
                return@launch
            }

            // Match Custom commands
            val matchedCommand = commands.find { cmd ->
                cmd.commandId != "wake_phrase" && com.example.core.util.FuzzyMatcher.isMatch(processedSpoken, cmd.aliases)
            }

            if (matchedCommand != null) {
                when (matchedCommand.commandId) {
                    "open_graph" -> {
                        speakAndLaunch("Opening Graph Plotter", "graph_plotter")
                        saveHistory(input, "Opening Graph Plotter")
                    }
                    "open_matrix" -> {
                        speakAndLaunch("Opening Matrix Calculator", "matrix_calculator")
                        saveHistory(input, "Opening Matrix Calculator")
                    }
                    "open_scanner" -> {
                        speakAndLaunch("Opening Vision Scanner", "math_scanner")
                        saveHistory(input, "Opening Vision Scanner")
                    }
                    "open_practice" -> {
                        val reply = "Opening Practice Mode!"
                        speakAndLaunch(reply, "calculator") // can navigate to calculator or custom dialog trigger
                        saveHistory(input, reply)
                    }
                    "open_history" -> {
                        speakAndLaunch("Opening Calculation History", "calculator")
                        saveHistory(input, "Opening Calculation History")
                    }
                    "open_settings" -> {
                        speakAndLaunch("Opening Settings", "calculator")
                        saveHistory(input, "Opening Settings")
                    }
                    "unit_converter" -> {
                        speakAndLaunch("Opening Unit Converter", "unit_converter")
                        saveHistory(input, "Opening Unit Converter")
                    }
                    "open_stats" -> {
                        speakAndLaunch("Opening Statistics screen", "statistics_calculator")
                        saveHistory(input, "Opening Statistics screen")
                    }
                    "open_complex" -> {
                        speakAndLaunch("Opening Complex Numbers screen", "complex_calculator")
                        saveHistory(input, "Opening Complex Numbers screen")
                    }
                    "open_calculus" -> {
                        speakAndLaunch("Opening Calculus screen", "calculus")
                        saveHistory(input, "Opening Calculus screen")
                    }
                    "zoom_in" -> {
                        speak("Zooming in on graph view.")
                        saveHistory(input, "Zooming in on graph view.")
                    }
                    "zoom_out" -> {
                        speak("Zooming out on graph view.")
                        saveHistory(input, "Zooming out on graph view.")
                    }
                    "reset_graph" -> {
                        speak("Resetting graph viewport.")
                        saveHistory(input, "Resetting graph viewport.")
                    }
                    "stop" -> {
                        speak("Stopping background continuous mode.")
                        stopContinuousListening()
                        saveHistory(input, "Stopping background voice mode")
                    }
                    "repeat" -> {
                        speak("Nothing to repeat.")
                        saveHistory(input, "Repeat action")
                    }
                    else -> {
                        speak("Executing background command.")
                        saveHistory(input, "Executed command: ${matchedCommand.commandName}")
                    }
                }
                return@launch
            }

            // Math understanding & calculation
            val understanding = TextUnderstandingEngine.process(processedSpoken)
            if (understanding.category != com.example.domain.scanner.MathCategory.GENERAL) {
                val conceptInfo = MathKnowledgeBase.classify(processedSpoken, understanding.category)
                val response = AITutorEngine.generateTutorResponse(
                    processedSpoken,
                    conceptInfo,
                    AITutorMode.EXPLAIN_WHY,
                    0
                )
                speak(response)
                saveHistory(input, response)
            } else {
                // Simple calculation or standard reply
                val engine = com.example.domain.math.CalculatorEngine()
                val evaluationResult = engine.evaluate(processedSpoken, false)
                if (evaluationResult is com.example.core.util.Result.Success) {
                    val reply = "The result of $processedSpoken is ${evaluationResult.data}"
                    speak(reply)
                    saveHistory(input, reply)
                } else {
                    val reply = "I heard you say: $processedSpoken. Ask me a math problem, or command me to open a tool!"
                    speak(reply)
                    saveHistory(input, reply)
                }
            }
        }
    }

    private fun speak(text: String) {
        if (ttsReady) {
            Log.d(TAG, "Speaking: $text")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "BG_TTS_UTTERANCE")
        }
    }

    private fun speakAndLaunch(reply: String, targetScreen: String) {
        speak(reply)
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_screen", targetScreen)
        }
        startActivity(intent)
    }

    private suspend fun saveHistory(prompt: String, action: String) {
        val entry = VoiceHistory(
            id = UUID.randomUUID().toString(),
            prompt = prompt,
            action = action,
            timestamp = System.currentTimeMillis()
        )
        voiceHistoryRepository.saveVoiceHistory(entry)
    }

    private fun startContinuousListening() {
        if (isListening) return
        isListening = true
        Log.d(TAG, "Starting speech recognition listener...")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun stopContinuousListening() {
        isListening = false
        speechRecognizer?.stopListening()
        Log.d(TAG, "Stopped continuous listening")
    }

    private suspend fun checkAndRestartListeningIfNeeded() {
        val advancedEnabled = settingsRepository.advancedVoiceModeEnabledFlow.first()
        val continuousEnabled = settingsRepository.continuousListeningEnabledFlow.first()
        if (advancedEnabled && continuousEnabled) {
            startContinuousListening()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice AI Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Voice AI active for continuous listening and custom wake phrase."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice AI Active")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: Destroying voice service")
        serviceScope.cancel()
        
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null

        tts?.stop()
        tts?.shutdown()
        tts = null
        
        super.onDestroy()
    }
}
