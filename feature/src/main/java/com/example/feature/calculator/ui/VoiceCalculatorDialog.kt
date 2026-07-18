package com.example.feature.calculator.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.feature.calculator.CalculatorEvent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val PREFS_NAME = "voice_calc_prefs"
private const val KEY_HISTORY = "voice_history_items"
private const val KEY_CONTINUOUS = "voice_continuous_enabled"
private const val KEY_AUTO_SOLVE = "voice_auto_solve_enabled"
private const val KEY_LANGUAGE = "voice_selected_language"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceCalculatorDialog(
    onDismissRequest: () -> Unit,
    onEvent: (CalculatorEvent) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // Settings persisted locally
    var isContinuous by remember { mutableStateOf(sharedPrefs.getBoolean(KEY_CONTINUOUS, false)) }
    var isAutoSolve by remember { mutableStateOf(sharedPrefs.getBoolean(KEY_AUTO_SOLVE, true)) }
    var selectedLanguage by remember { mutableStateOf(sharedPrefs.getString(KEY_LANGUAGE, "auto") ?: "auto") }

    // Voice History loaded locally
    var voiceHistoryList by remember {
        mutableStateOf(
            sharedPrefs.getStringSet(KEY_HISTORY, emptySet())
                ?.toList()
                ?.sortedByDescending { it.substringBefore("|||").toLongOrNull() ?: 0L }
                ?: emptyList()
        )
    }

    // Speech states
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var processedMath by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rmsLevel by remember { mutableStateOf(0f) }
    var confidenceScore by remember { mutableStateOf<Int?>(null) }
    val isSpeechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    // Resolve SpeechRecognizer on main thread
    val speechRecognizer = remember {
        if (isSpeechAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }

    // Helper functions
    fun saveVoiceHistory(newList: List<String>) {
        voiceHistoryList = newList
        sharedPrefs.edit().putStringSet(KEY_HISTORY, newList.toSet()).apply()
    }

    fun addHistoryItem(spoken: String, parsed: String) {
        val timestamp = System.currentTimeMillis()
        val newItem = "$timestamp|||$spoken|||$parsed"
        val filteredList = voiceHistoryList.filterNot { it.substringAfter("|||") == "$spoken|||$parsed" }
        val updatedList = (listOf(newItem) + filteredList).take(10)
        saveVoiceHistory(updatedList)
    }

    fun clearVoiceHistory() {
        saveVoiceHistory(emptyList())
    }

    // Determine target locale for recognizer
    val selectedLangCode = when (selectedLanguage) {
        "en-US" -> "en-US"
        "en-IN" -> "en-IN"
        "hi-IN" -> "hi-IN"
        "hinglish" -> "en-IN" // en-IN is highly accurate for Hinglish/mixed phonetic speech
        else -> Locale.getDefault().toString()
    }

    // Safe starts & stops
    fun safeStartListening() {
        if (speechRecognizer == null) return
        try {
            speechRecognizer.cancel()
            errorMessage = "Ready... Speak now"
            processedMath = ""
            recognizedText = ""
            confidenceScore = null
            rmsLevel = 0f
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLangCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5) // Fetch multiple matches for highest accuracy
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            Log.d("VoiceCalc", "Starting speech recognition with locale: $selectedLangCode")
            speechRecognizer.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e("VoiceCalc", "Failed to start listening", e)
            errorMessage = "Could not initialize mic: ${e.localizedMessage}"
            isListening = false
        }
    }

    fun safeStopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceCalc", "Failed to stop listening", e)
        }
        isListening = false
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceCalc", "onReadyForSpeech")
                isListening = true
                errorMessage = "Listening closely..."
            }

            override fun onBeginningOfSpeech() {
                Log.d("VoiceCalc", "onBeginningOfSpeech")
                errorMessage = "Hearing speech..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                rmsLevel = rmsdB
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("VoiceCalc", "onEndOfSpeech")
                isListening = false
                errorMessage = "Processing your math..."
            }

            override fun onError(error: Int) {
                val errorDesc = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network issue. Offline packs missing."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout. Try again."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard"
                    else -> "No speech heard (Tap mic)"
                }
                Log.e("VoiceCalc", "Recognizer error: $error ($errorDesc)")

                if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    coroutineScope.launch {
                        try {
                            speechRecognizer?.cancel()
                            delay(500)
                            if (isListening) safeStartListening()
                        } catch (e: Exception) {
                            Log.e("VoiceCalc", "Busy recovery failed", e)
                        }
                    }
                } else {
                    errorMessage = errorDesc
                    isListening = false
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                Log.d("VoiceCalc", "onResults: matches=${matches?.size}")
                
                if (!matches.isNullOrEmpty()) {
                    // Score all alternative recognition matches to pick the one that is most mathematical!
                    var bestMatchText = matches[0]
                    var bestMathExpression = ""
                    var highestScore = -1

                    for (match in matches) {
                        val parsed = processMathExpression(match, selectedLanguage)
                        val score = scoreExpression(parsed)
                        if (score > highestScore) {
                            highestScore = score
                            bestMatchText = match
                            bestMathExpression = parsed
                        }
                    }

                    recognizedText = bestMatchText
                    processedMath = bestMathExpression

                    val topConfidence = confidences?.firstOrNull() ?: -1f
                    if (topConfidence >= 0f) {
                        confidenceScore = (topConfidence * 100).toInt()
                    }

                    if (bestMathExpression.isEmpty()) {
                        errorMessage = "No mathematical expression parsed."
                    } else {
                        errorMessage = "Parsed successfully!"
                        
                        // Action on the calculator
                        onEvent(CalculatorEvent.Clear)
                        onEvent(CalculatorEvent.InputString(bestMathExpression))
                        
                        if (isAutoSolve) {
                            onEvent(CalculatorEvent.Calculate)
                        }

                        // Persist to history
                        addHistoryItem(bestMatchText, bestMathExpression)
                    }
                } else {
                    errorMessage = "No matches detected"
                }

                // Handle continuous mode loop
                if (isContinuous) {
                    coroutineScope.launch {
                        delay(1200)
                        if (isListening) safeStartListening()
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recognizedText = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    DisposableEffect(Unit) {
        speechRecognizer?.setRecognitionListener(recognitionListener)
        onDispose {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.e("VoiceCalc", "Error during release", e)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Voice Calculator", style = MaterialTheme.typography.titleLarge)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Offline Mode",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!permissionState.status.isGranted) {
                    Text(
                        text = if (permissionState.status.shouldShowRationale) {
                            "Microphone permission is needed to recognize speech formulas offline."
                        } else {
                            "Microphone permission is currently denied. Please enable it in Settings."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { permissionState.launchPermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Permission")
                    }
                } else if (!isSpeechAvailable) {
                    Text(
                        "Speech recognition engine is not ready or available on this device. Make sure Google Play Services is installed.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Settings configuration section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Language choice
                            Text(
                                "Recognition Language",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val languages = listOf(
                                    "auto" to "Auto",
                                    "en-US" to "EN (US)",
                                    "en-IN" to "Hinglish",
                                    "hi-IN" to "हिंदी"
                                )
                                languages.forEach { (langKey, label) ->
                                    val isSelected = selectedLanguage == langKey
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedLanguage = langKey
                                                sharedPrefs.edit().putString(KEY_LANGUAGE, langKey).apply()
                                                if (isListening) {
                                                    safeStartListening()
                                                }
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = label,
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Switch options
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto Solve", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Instantly evaluate recognized formulas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isAutoSolve,
                                    onCheckedChange = {
                                        isAutoSolve = it
                                        sharedPrefs.edit().putBoolean(KEY_AUTO_SOLVE, it).apply()
                                    },
                                    thumbContent = if (isAutoSolve) {
                                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp)) }
                                    } else null
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Continuous Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Keep listening for sequential calculations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isContinuous,
                                    onCheckedChange = {
                                        isContinuous = it
                                        sharedPrefs.edit().putBoolean(KEY_CONTINUOUS, it).apply()
                                        if (!it && isListening) {
                                            safeStopListening()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Main recording wave button representation
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening) {
                            // Pulsing concentric rings mapped directly to sound RMS levels
                            val pulseScale1 by rememberInfiniteTransition("ring1").animateFloat(
                                initialValue = 0.9f,
                                targetValue = 1.3f,
                                animationSpec = infiniteRepeatable(tween(800, easing = EaseOutQuad), RepeatMode.Reverse),
                                label = "scale1"
                            )
                            val pulseScale2 by rememberInfiniteTransition("ring2").animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.5f,
                                animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutQuad), RepeatMode.Reverse),
                                label = "scale2"
                            )
                            val rmsFactor = (rmsLevel.coerceIn(0f, 10f) / 10f)

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(pulseScale2 * (1f + rmsFactor * 0.4f))
                                    .alpha(0.15f)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(105.dp)
                                    .scale(pulseScale1 * (1f + rmsFactor * 0.2f))
                                    .alpha(0.25f)
                                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isListening) {
                                    safeStopListening()
                                } else {
                                    safeStartListening()
                                }
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    if (isListening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop speech recognition" else "Start speech recognition",
                                modifier = Modifier.size(36.dp),
                                tint = if (isListening) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = if (isListening) "Listening... Speak now" else "Tap microphone to speak",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    // Results output cards
                    if (recognizedText.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Spoken Text",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    confidenceScore?.let { score ->
                                        Text(
                                            "Confidence: $score%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (score > 80) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                        )
                                    }
                                }
                                Text(
                                    text = recognizedText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (processedMath.isNotEmpty()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                    Text(
                                        "Corrected Formula",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = processedMath,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = if (msg.contains("error") || msg.contains("issue") || msg.contains("No")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Voice history list
                    if (voiceHistoryList.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Voice History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { clearVoiceHistory() },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp))
                                    Text("Clear All", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // Scrolling Box to hold recent items properly without nesting scroll weights
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .padding(4.dp)
                            ) {
                                voiceHistoryList.take(5).forEach { item ->
                                    val parts = item.split("|||")
                                    if (parts.size >= 3) {
                                        val spoken = parts[1]
                                        val parsed = parts[2]

                                        Surface(
                                            onClick = {
                                                onEvent(CalculatorEvent.Clear)
                                                onEvent(CalculatorEvent.InputString(parsed))
                                                if (isAutoSolve) {
                                                    onEvent(CalculatorEvent.Calculate)
                                                }
                                                onDismissRequest()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            tonalElevation = 1.dp
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1.5f)) {
                                                    Text(
                                                        text = spoken,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontStyle = FontStyle.Italic,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = parsed,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Quick add formula button (copies/adds without closing)
                                                    IconButton(
                                                        onClick = {
                                                            onEvent(CalculatorEvent.InputString(parsed))
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = "Add to input",
                                                            modifier = Modifier.size(16.dp),
                                                            tint = MaterialTheme.colorScheme.secondary
                                                        )
                                                    }
                                                    // Quick solve button
                                                    IconButton(
                                                        onClick = {
                                                            onEvent(CalculatorEvent.Clear)
                                                            onEvent(CalculatorEvent.InputString(parsed))
                                                            onEvent(CalculatorEvent.Calculate)
                                                            onDismissRequest()
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Solve formula",
                                                            modifier = Modifier.size(18.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}

/**
 * Score evaluated math output by character utility and structure.
 * Gives higher scores to matches containing clear digit-operator structures.
 */
private fun scoreExpression(expr: String): Int {
    if (expr.isEmpty()) return -1
    var score = 0
    var hasDigit = false
    var hasOperator = false
    
    for (char in expr) {
        when {
            char.isDigit() -> {
                score += 3
                hasDigit = true
            }
            char in "+-×÷.^%" -> {
                score += 5
                hasOperator = true
            }
            char in "()" -> {
                score += 2
            }
        }
    }
    
    // Penalty if it contains text/symbols that didn't resolve well
    val invalidLetters = expr.filter { it.isLetter() && it !in "sincostanloglsqrt" }
    score -= (invalidLetters.length * 10)
    
    // Bonus for proper math formula patterns
    if (hasDigit && hasOperator) {
        score += 15
    }
    
    return score
}

/**
 * Convert spoken English numbers (like "twenty five", "one hundred and three") to digit representations
 */
private fun convertNumberWordsToDigits(input: String): String {
    val tokens = input.split(Regex("\\s+"))
    val result = mutableListOf<String>()
    
    val units = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
        "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13,
        "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
        "eighteen" to 18, "nineteen" to 19
    )
    val tens = mapOf(
        "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50,
        "sixty" to 60, "seventy" to 70, "eighty" to 80, "ninety" to 90
    )
    val scales = mapOf(
        "hundred" to 100, "thousand" to 1000
    )
    
    var currentNumber = 0
    var partNumber = 0
    var inNumberSequence = false
    
    for (token in tokens) {
        val word = token.lowercase(Locale.ROOT).trim().replace(",", "")
        
        when {
            units.containsKey(word) -> {
                partNumber += units[word]!!
                inNumberSequence = true
            }
            tens.containsKey(word) -> {
                partNumber += tens[word]!!
                inNumberSequence = true
            }
            scales.containsKey(word) -> {
                val scale = scales[word]!!
                if (partNumber == 0) partNumber = 1
                partNumber *= scale
                currentNumber += partNumber
                partNumber = 0
                inNumberSequence = true
            }
            word == "and" && inNumberSequence -> {
                // Ignore connector in numbers e.g. "one hundred and five"
            }
            else -> {
                if (inNumberSequence) {
                    currentNumber += partNumber
                    result.add(currentNumber.toString())
                    currentNumber = 0
                    partNumber = 0
                    inNumberSequence = false
                }
                result.add(token)
            }
        }
    }
    
    if (inNumberSequence) {
        currentNumber += partNumber
        result.add(currentNumber.toString())
    }
    
    return result.joinToString(" ")
}

/**
 * Normalizes input voice matches into correct arithmetic expressions.
 */
private fun processMathExpression(rawText: String, selectedLanguage: String): String {
    var processed = rawText.lowercase(Locale.ROOT)
    
    // 1. Direct word translation for common hindi/phonetic written numbers to digits
    val hindiDigits = mapOf(
        "shunya" to "0", "zero" to "0", "ek" to "1", "do" to "2", "teen" to "3", "char" to "4",
        "panch" to "5", "chhe" to "6", "chhah" to "6", "saat" to "7", "aath" to "8", "nau" to "9", "das" to "10",
        "शून्य" to "0", "जीरो" to "0", "एक" to "1", "दो" to "2", "तीन" to "3", "चार" to "4",
        "पाँच" to "5", "पांच" to "5", "छह" to "6", "छः" to "6", "सात" to "7", "आठ" to "8", "नौ" to "9", "दस" to "10"
    )
    
    hindiDigits.forEach { (word, replacement) ->
        processed = processed.replace(word, replacement)
    }

    // 2. English spoken word conversions
    processed = convertNumberWordsToDigits(processed)

    // 3. Spoken mathematical terms mapping
    val operatorMappings = listOf(
        // English Math Terms
        "plus" to "+", "add" to "+", "sum of" to "+", "addition" to "+",
        "minus" to "-", "subtract" to "-", "less" to "-", "substract" to "-", "take away" to "-",
        "times" to "×", "multiplied by" to "×", "multiply by" to "×", "multiply" to "×", "into" to "×", "product of" to "×",
        "divided by" to "÷", "divide by" to "÷", "divided" to "÷", "divide" to "÷", "over" to "÷", "by" to "÷", "slash" to "÷", "oblique" to "÷",
        "point" to ".", "dot" to ".", "decimal" to ".",
        "percent" to "%", "percentage" to "%",
        "power of" to "^", "raised to" to "^", "raise to" to "^", "to the power of" to "^", "power" to "^",
        "squared" to "^2", "cubed" to "^3",
        "square root of" to "sqrt(", "square root" to "sqrt(", "root of" to "sqrt(", "root" to "sqrt(",
        "bracket open" to "(", "open bracket" to "(", "parenthesis open" to "(", "open parenthesis" to "(",
        "bracket close" to ")", "closed bracket" to ")", "bracket closed" to ")", "parenthesis close" to ")", "close parenthesis" to ")",
        "sin of" to "sin(", "sine of" to "sin(", "sine" to "sin(", "sin" to "sin(",
        "cos of" to "cos(", "cosine of" to "cos(", "cosine" to "cos(", "cos" to "cos(",
        "tan of" to "tan(", "tangent of" to "tan(", "tangent" to "tan(", "tan" to "tan(",
        "log of" to "log(", "logarithm of" to "log(", "log" to "log(",
        "ln of" to "ln(", "natural log of" to "ln(", "ln" to "ln(",
        
        // Hindi Math Terms
        "जमा" to "+", "धन" to "+", "जोड़" to "+", "जोड़ें" to "+",
        "घटा" to "-", "घटाएं" to "-", "ऋण" to "-",
        "गुणा" to "×", "गुणे" to "×", "गुना" to "×",
        "भाग" to "÷", "भागे" to "÷", "बटा" to "÷",
        "दशमलव" to ".", "बिंदु" to ".",
        "प्रतिशत" to "%", "फीसदी" to "%",
        "बराबर" to "=",
        
        // Hinglish/Phonetic Terms
        "jama" to "+", "jodein" to "+", "jod" to "+",
        "ghata" to "-", "ghatayein" to "-",
        "gune" to "×",
        "bhag" to "÷", "bhage" to "÷", "bata" to "÷",
        "pratishat" to "%"
    ).sortedByDescending { it.first.length }

    operatorMappings.forEach { (phrase, symbol) ->
        processed = processed.replace(phrase, symbol)
    }

    // 4. Construct clean formula filtering out trash words
    processed = processed
        .replace("*", "×")
        .replace("/", "÷")
        .replace("=", "")

    val sb = java.lang.StringBuilder()
    var i = 0
    while (i < processed.length) {
        val char = processed[i]
        if (char in "0123456789+-×÷.%^()") {
            sb.append(char)
            i++
        } else if (processed.startsWith("sin(", i)) {
            sb.append("sin(")
            i += 4
        } else if (processed.startsWith("cos(", i)) {
            sb.append("cos(")
            i += 4
        } else if (processed.startsWith("tan(", i)) {
            sb.append("tan(")
            i += 4
        } else if (processed.startsWith("log(", i)) {
            sb.append("log(")
            i += 4
        } else if (processed.startsWith("ln(", i)) {
            sb.append("ln(")
            i += 3
        } else if (processed.startsWith("sqrt(", i)) {
            sb.append("sqrt(")
            i += 5
        } else {
            // Join digits split by empty space (e.g., "5 0 0" -> "500")
            if (char.isWhitespace()) {
                if (i > 0 && i < processed.length - 1 && processed[i - 1].isDigit() && processed[i + 1].isDigit()) {
                    // skip whitespace to merge
                }
            }
            i++
        }
    }

    var resultExpr = sb.toString()

    // 5. Run auto-correct optimizations
    resultExpr = autoCorrectExpression(resultExpr)

    return resultExpr
}

/**
 * Balanced parenthesis and smart mathematical symbol cleanup
 */
private fun autoCorrectExpression(expr: String): String {
    var cleaned = expr.trim()
    
    // Strip incorrect leading operators
    while (cleaned.isNotEmpty() && (cleaned.startsWith("+") || cleaned.startsWith("×") || cleaned.startsWith("÷") || cleaned.startsWith("%") || cleaned.startsWith("^"))) {
        cleaned = cleaned.substring(1)
    }
    
    // Strip incorrect trailing operators
    while (cleaned.isNotEmpty() && (cleaned.endsWith("+") || cleaned.endsWith("-") || cleaned.endsWith("×") || cleaned.endsWith("÷") || cleaned.endsWith("^") || cleaned.endsWith("."))) {
        cleaned = cleaned.substring(0, cleaned.length - 1)
    }
    
    // Remove duplicate consecutive symbols
    cleaned = cleaned
        .replace(Regex("\\++"), "+")
        .replace(Regex("-+"), "-")
        .replace(Regex("[×*]+"), "×")
        .replace(Regex("[÷/]+"), "÷")
        .replace(Regex("\\++"), "+")
        .replace(Regex("\\.+"), ".")
        .replace(Regex("\\^+"), "^")
        .replace(Regex("%+"), "%")

    // Automatic parentheses balancer
    val openCount = cleaned.count { it == '(' }
    val closeCount = cleaned.count { it == ')' }
    if (openCount > closeCount) {
        cleaned += ")".repeat(openCount - closeCount)
    } else if (closeCount > openCount) {
        cleaned = "(".repeat(closeCount - openCount) + cleaned
    }
    
    return cleaned
}
