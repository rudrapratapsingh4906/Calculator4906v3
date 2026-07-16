package com.example.feature.calculator.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.feature.calculator.CalculatorEvent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceCalculatorDialog(
    onDismissRequest: () -> Unit,
    onEvent: (CalculatorEvent) -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isSpeechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    val speechRecognizer = remember { 
        if (isSpeechAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    
    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceCalc", "onReadyForSpeech")
                isListening = true
                errorMessage = "Ready... Speak now"
                recognizedText = ""
            }

            override fun onBeginningOfSpeech() {
                Log.d("VoiceCalc", "onBeginningOfSpeech")
                errorMessage = "Hearing speech..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Log.v("VoiceCalc", "onRmsChanged: $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("VoiceCalc", "onBufferReceived")
            }
            
            override fun onEndOfSpeech() {
                Log.d("VoiceCalc", "onEndOfSpeech")
                isListening = false
                if (errorMessage == "Hearing speech...") {
                    errorMessage = "Processing..."
                }
            }

            override fun onError(error: Int) {
                val errorDesc = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                    else -> "Error code: $error"
                }
                Log.e("VoiceCalc", "Voice recognition failed")
                
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    errorMessage = "No match found. Retrying..."
                    // Try restarting automatically once
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    }
                    speechRecognizer?.startListening(intent)
                } else {
                    errorMessage = errorDesc
                    isListening = false
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("VoiceCalc", "onResults: matches=${matches?.size}, topMatch='${matches?.firstOrNull()}'")
                
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    recognizedText = text
                    errorMessage = "Analyzing: \"$text\""
                    
                    val processedExpression = processMathExpression(text)
                    Log.d("VoiceCalc", "Processed Expression: '$processedExpression'")
                    
                    if (processedExpression.isEmpty()) {
                        errorMessage = "No math found in: \"$text\""
                    } else {
                        errorMessage = "Inserting: $processedExpression"
                        Log.d("VoiceCalc", "Sending InputString: $processedExpression")
                        onEvent(CalculatorEvent.Clear)
                        onEvent(CalculatorEvent.InputString(processedExpression))
                        
                        val hasOperator = processedExpression.any { it == '+' || it == '-' || it == '×' || it == '÷' || it == '^' || it == '%' }
                        val explicitResult = text.lowercase().let { 
                            it.contains("equal") || it.contains("calculate") || it.contains("result") || it.contains("is") || it.contains("=") 
                        }
                        
                        if (hasOperator || explicitResult) {
                            Log.d("VoiceCalc", "Sending Calculate Event")
                            onEvent(CalculatorEvent.Calculate)
                        }
                    }
                } else {
                    errorMessage = "No results received"
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recognizedText = matches[0]
                    Log.d("VoiceCalc", "onPartialResults: $recognizedText")
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("VoiceCalc", "onEvent: $eventType")
            }
        }
    }

    DisposableEffect(Unit) {
        speechRecognizer?.setRecognitionListener(recognitionListener)
        onDispose {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Voice Calculator") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!permissionState.status.isGranted) {
                    Text(
                        if (permissionState.status.shouldShowRationale) {
                            "Microphone permission is needed to use the voice calculator."
                        } else {
                            "Microphone permission is required. Please grant it in settings."
                        }
                    )
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                } else if (!isSpeechAvailable) {
                    Text("Speech recognition is not available on this device.")
                } else {
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening) {
                           CircularProgressIndicator(
                               modifier = Modifier.fillMaxSize(),
                               color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                               strokeWidth = 2.dp
                           )
                        }
                        
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    speechRecognizer?.stopListening()
                                    isListening = false
                                } else {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                                        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                                    }
                                    Log.d("VoiceCalc", "Starting listening with Locale: ${Locale.getDefault()}")
                                    speechRecognizer?.startListening(intent)
                                }
                            },
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop listening" else "Start listening",
                                modifier = Modifier.size(48.dp),
                                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = if (isListening) "Listening..." else "Tap to speak",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (recognizedText.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = recognizedText,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = if (it.contains("Error") || it.contains("No")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
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

private fun processMathExpression(text: String): String {
    val numberWords = listOf(
        "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
        "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9",
        "ten" to "10", "eleven" to "11", "twelve" to "12", "thirteen" to "13",
        "fourteen" to "14", "fifteen" to "15", "sixteen" to "16", "seventeen" to "17",
        "eighteen" to "18", "nineteen" to "19", "twenty" to "20", "thirty" to "30",
        "forty" to "40", "fifty" to "50", "sixty" to "60", "seventy" to "70",
        "eighty" to "80", "ninety" to "90", "hundred" to "100"
    ).sortedByDescending { it.first.length }
    
    var processed = text.lowercase()
    
    // Replace spoken number words with digits
    numberWords.forEach { (word, replacement) ->
        processed = processed.replace(word, replacement)
    }

    processed = processed
        .replace("plus", "+")
        .replace("minus", "-")
        .replace("times", "×")
        .replace("into", "×")
        .replace("multiplied by", "×")
        .replace("multiplied", "×")
        .replace("multiply", "×")
        .replace("divided by", "÷")
        .replace("divide by", "÷")
        .replace("divided", "÷")
        .replace("divide", "÷")
        .replace("over", "/")
        .replace("point", ".")
        .replace("dot", ".")
        .replace("to the power of", "^")
        .replace("power of", "^")
        .replace("power", "^")
        .replace("percentage", "%")
        .replace("percent", "%")
        .replace("equals", "=")
        .replace("equal to", "=")
        .replace("equal", "=")
        .replace("calculate", "=")
        .replace("result", "=")
        .replace("is", "=")

    // Extract only valid calculator characters: 0-9, symbols, and operators
    val validChars = "0123456789+-*×/÷.%^="
    val filtered = processed.filter { it in validChars }
        .replace("*", "×")
        .replace("/", "÷")
        .replace("=", "") // Remove equals signs from the middle/start

    return filtered
}
