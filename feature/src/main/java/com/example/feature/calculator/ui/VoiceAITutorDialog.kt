package com.example.feature.calculator.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.util.Result
import com.example.data.scanner.ExpressionParserImpl
import com.example.domain.math.CalculatorEngine
import com.example.domain.scanner.AITutorMode
import com.example.domain.scanner.AITutorEngine
import com.example.domain.scanner.MathIntent
import com.example.domain.scanner.TextUnderstandingEngine
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val PREFS_NAME = "voice_ai_tutor_prefs"
private const val KEY_LANGUAGE = "tutor_selected_language"
private const val KEY_TTS_ENABLED = "tutor_tts_enabled"

enum class MessageSender { USER, TUTOR }

data class TutorChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

private object DummySettingsRepository : com.example.domain.repository.SettingsRepository {
    override val themeFlow: kotlinx.coroutines.flow.StateFlow<String> = kotlinx.coroutines.flow.MutableStateFlow("Default")
    override val themeModeFlow: kotlinx.coroutines.flow.StateFlow<String> = kotlinx.coroutines.flow.MutableStateFlow("System")
    override val vibrationEnabledFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(true)
    override val soundEnabledFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(true)
    override val orientationLockFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val backgroundImageUriFlow: kotlinx.coroutines.flow.StateFlow<String?> = kotlinx.coroutines.flow.MutableStateFlow(null)
    override val backgroundOpacityFlow: kotlinx.coroutines.flow.StateFlow<Float> = kotlinx.coroutines.flow.MutableStateFlow(0.5f)
    override val advancedVoiceModeEnabledFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val voiceAutoStartEnabledFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val continuousListeningEnabledFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(false)

    override fun setTheme(theme: String) {}
    override fun setThemeMode(themeMode: String) {}
    override fun setVibrationEnabled(enabled: Boolean) {}
    override fun setSoundEnabled(enabled: Boolean) {}
    override fun setOrientationLock(locked: Boolean) {}
    override fun setBackgroundImageUri(uri: String?) {}
    override fun setBackgroundOpacity(opacity: Float) {}
    override fun setAdvancedVoiceModeEnabled(enabled: Boolean) {}
    override fun setVoiceAutoStartEnabled(enabled: Boolean) {}
    override fun setContinuousListeningEnabled(enabled: Boolean) {}
}

private object DummyVoiceHistoryRepository : com.example.domain.repository.VoiceHistoryRepository {
    override fun getVoiceHistory(): kotlinx.coroutines.flow.Flow<List<com.example.domain.model.VoiceHistory>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override suspend fun saveVoiceHistory(history: com.example.domain.model.VoiceHistory) {}
    override suspend fun clearVoiceHistory() {}
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceAITutorDialog(
    onDismissRequest: () -> Unit,
    currentScreen: String = "calculator",
    onNavigateTo: (String) -> Unit = {},
    onEvent: (com.example.feature.calculator.CalculatorEvent) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    graphViewModel: com.example.feature.advancedfeatures.ui.GraphPlotterViewModel? = null,
    matrixViewModel: com.example.feature.advancedfeatures.ui.MatrixCalculatorViewModel? = null,
    mathScannerViewModel: com.example.feature.mathscanner.ui.MathScannerViewModel? = null,
    voiceCommandRepository: com.example.domain.repository.VoiceCommandRepository? = null,
    settingsRepository: com.example.domain.repository.SettingsRepository? = null,
    voiceHistoryRepository: com.example.domain.repository.VoiceHistoryRepository? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // local preferences
    var selectedLanguage by remember { mutableStateOf(sharedPrefs.getString(KEY_LANGUAGE, "en-US") ?: "en-US") }
    var isTtsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean(KEY_TTS_ENABLED, true)) }
    var speechRate by remember { mutableStateOf(1.0f) }

    // Settings collection
    val localSettings = settingsRepository ?: DummySettingsRepository
    val advancedVoiceModeEnabled by localSettings.advancedVoiceModeEnabledFlow.collectAsState(initial = false)
    val voiceAutoStartEnabled by localSettings.voiceAutoStartEnabledFlow.collectAsState(initial = false)
    val continuousListeningEnabled by localSettings.continuousListeningEnabledFlow.collectAsState(initial = false)

    // Voice History collection
    val localVoiceHistory = voiceHistoryRepository ?: DummyVoiceHistoryRepository
    val voiceHistoryList by localVoiceHistory.getVoiceHistory().collectAsState(initial = emptyList())

    // Custom voice commands state
    val voiceCommands by if (voiceCommandRepository != null) {
        voiceCommandRepository.getVoiceCommandsFlow().collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<com.example.domain.model.VoiceCommand>()) }
    }
    var showCustomizerDialog by remember { mutableStateOf(false) }

    // TTS & SpeechRecognizer
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
        ttsInstance = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakAloud(text: String) {
        if (isTtsEnabled && ttsReady) {
            val cleanText = text
                .replace("🎓 AI Tutor Mode:", "")
                .replace("Concept:", "Let's talk about")
                .replace("Explanation Level:", "")
                .replace("💡 Hint:", "Here is a hint:")
                .replace("➡️ Next Step:", "The next step is:")
                .replace("📘 Key Formulas & Identities:", "Some key formulas to remember are:")
                .replace("📜 Theorem & Principle:", "The key theorem here is:")
                .replace("🔍 Mathematical Rationale:", "The rationale is:")
                .replace("⚡ Alternative Methodology:", "An alternative method is:")
                .replace("🎯 Direct Solution:", "The answer is:")
                .replace(Regex("[*_#]"), "") // clean markups
            
            val locale = when (selectedLanguage) {
                "hi-IN" -> Locale("hi", "IN")
                else -> Locale.US
            }
            ttsInstance?.language = locale
            ttsInstance?.setSpeechRate(speechRate)
            ttsInstance?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "tutor_speech")
        }
    }

    // Stateful interaction engines
    val parser = remember { ExpressionParserImpl(CalculatorEngine()) }
    var activeExpression by remember { mutableStateOf("") }
    var currentStepIndex by remember { mutableStateOf(0) }

    // Conversation history
    val chatMessages = remember {
        mutableStateListOf(
            TutorChatMessage(
                sender = MessageSender.TUTOR,
                text = "🎓 Namaste! I am your Voice AI Tutor. Ask me any math problem, or say commands like:\n" +
                        "• \"solve x square minus 4 equals 0\"\n" +
                        "• \"differentiate x cube\"\n" +
                        "• \"integrate sin x\"\n" +
                        "• \"factor x square minus 9\"\n" +
                        "• \"show hint\"\n" +
                        "• \"next step\"\n\n" +
                        "I support English, Hindi, and Hinglish. Talk to me!"
            )
        )
    }

    // Speech states
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rmsLevel by remember { mutableStateOf(0f) }
    val isSpeechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    val speechRecognizer = remember {
        if (isSpeechAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }

    // Language locale code
    val targetLocale = when (selectedLanguage) {
        "en-US" -> "en-US"
        "en-IN" -> "en-IN"
        "hi-IN" -> "hi-IN"
        else -> "en-US"
    }

    fun stopSpeaking() {
        ttsInstance?.stop()
    }

    fun processTutorInput(input: String) {
        stopSpeaking()
        
        // Add user chat bubble
        chatMessages.add(TutorChatMessage(sender = MessageSender.USER, text = input))
        
        val spoken = input.lowercase().trim()

        // 1. Check Custom Wake Phrase
        val wakeCommand = voiceCommands.find { it.commandId == "wake_phrase" }
        var processedSpoken = spoken
        if (wakeCommand != null) {
            val matchedWakeAlias = wakeCommand.aliases.find { alias ->
                spoken.startsWith(alias) || com.example.core.util.FuzzyMatcher.isMatch(spoken.take(alias.length + 2), listOf(alias))
            }
            if (matchedWakeAlias != null) {
                processedSpoken = spoken.removePrefix(matchedWakeAlias).trim()
                if (processedSpoken.isEmpty()) {
                    val replies = listOf(
                        "Haan ji, main sun raha hoon! Puchiye, kya math solve karna hai?",
                        "Hello! Yes, I'm listening. Speak your math problem.",
                        "Suno! Main ready hoon. Ask me any math question.",
                        "Yes, please speak! Main active hoon."
                    )
                    val reply = replies[Math.abs(input.hashCode()) % replies.size]
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    return
                }
            }
        }

        // 2. Perform Custom Command Matching via Fuzzy Matching from DB Triggers
        val matchedCommand = voiceCommands.find { command ->
            command.commandId != "wake_phrase" && com.example.core.util.FuzzyMatcher.isMatch(processedSpoken, command.aliases)
        }

        if (matchedCommand != null) {
            when (matchedCommand.commandId) {
                "open_graph" -> {
                    val reply = "Opening Graph Plotter."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    coroutineScope.launch {
                        delay(1200)
                        onNavigateTo("graph_plotter")
                        onDismissRequest()
                    }
                    return
                }
                "open_matrix" -> {
                    val reply = "Opening Matrix Calculator."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    coroutineScope.launch {
                        delay(1200)
                        onNavigateTo("matrix_calculator")
                        onDismissRequest()
                    }
                    return
                }
                "open_scanner" -> {
                    val reply = "Opening Vision Scanner."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    coroutineScope.launch {
                        delay(1200)
                        onNavigateTo("math_scanner")
                        onDismissRequest()
                    }
                    return
                }
                "open_practice" -> {
                    val reply = "Let's practice! Here is a math question for you:"
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    val chapter = com.example.domain.scanner.MathChapter.COMPLEX_NUMBERS_QUADRATIC_EQUATIONS
                    val level = com.example.domain.scanner.MathLevel.CLASS_11_12
                    val questions = com.example.domain.scanner.QuestionGeneratorEngine.generateQuestions(chapter, level, count = 1)
                    val q = questions.first()
                    val questionReply = "📝 Problem: ${q.text}\n💡 Hint: ${q.hint ?: "Try applying basic concepts."}"
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = questionReply))
                    speakAloud(questionReply)
                    return
                }
                "open_history" -> {
                    val reply = "Opening calculation history."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    onEvent(com.example.feature.calculator.CalculatorEvent.ToggleHistory)
                    onDismissRequest()
                    return
                }
                "open_settings" -> {
                    val reply = "Opening settings dialog."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    onOpenSettings()
                    onDismissRequest()
                    return
                }
                "unit_converter" -> {
                    val reply = "Opening Unit Converter."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    coroutineScope.launch {
                        delay(1200)
                        onNavigateTo("unit_converter")
                        onDismissRequest()
                    }
                    return
                }
                "open_stats" -> {
                    val reply = "Opening Statistics screen."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    coroutineScope.launch {
                        delay(1200)
                        onNavigateTo("statistics_calculator")
                        onDismissRequest()
                    }
                    return
                }
                "open_complex" -> {
                    val reply = "Opening Complex Numbers screen."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    coroutineScope.launch {
                        delay(1200)
                        onNavigateTo("complex_calculator")
                        onDismissRequest()
                    }
                    return
                }
                "open_calculus" -> {
                    val reply = "Opening Calculus screen."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    coroutineScope.launch {
                        delay(1200)
                        onNavigateTo("calculus")
                        onDismissRequest()
                    }
                    return
                }
                "zoom_in" -> {
                    val reply = "Zooming in on graph view."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    graphViewModel?.zoomIn()
                    return
                }
                "zoom_out" -> {
                    val reply = "Zooming out on graph view."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    graphViewModel?.zoomOut()
                    return
                }
                "reset_graph" -> {
                    val reply = "Resetting graph viewport layout."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    graphViewModel?.resetViewport()
                    return
                }
                "solve_matrix" -> {
                    val reply = "Solving matrix operation."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    matrixViewModel?.performOperation("+")
                    return
                }
                "find_determinant" -> {
                    val reply = "The determinant of matrix A is calculated."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    return
                }
                "find_inverse" -> {
                    val reply = "Computing inverse matrix."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    return
                }
                "transpose_matrix" -> {
                    val reply = "Transposing matrix A."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    return
                }
                "solve_quadratic" -> {
                    val reply = "Quadratic equations are of the form ax² + bx + c = 0. Speak the quadratic expression to solve, like 'solve x square minus five x plus six equals zero'."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    return
                }
                "solve_simultaneous" -> {
                    val reply = "Simultaneous linear equations can be solved using substitution or matrix methods. Speak the linear equations!"
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    return
                }
                "next_step" -> {
                    if (activeExpression.isEmpty()) {
                        val reply = "We don't have an active math equation. Please speak a problem first."
                        chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                        speakAloud(reply)
                        return
                    }
                    currentStepIndex++
                    val conceptInfo = com.example.domain.scanner.MathKnowledgeBase.classify(
                        activeExpression,
                        TextUnderstandingEngine.process(activeExpression).category
                    )
                    val tutorResponse = AITutorEngine.generateTutorResponse(
                        activeExpression,
                        conceptInfo,
                        AITutorMode.NEXT_STEP,
                        currentStepIndex
                    )
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = tutorResponse))
                    speakAloud(tutorResponse)
                    return
                }
                "previous_step" -> {
                    if (activeExpression.isEmpty()) {
                        val reply = "We don't have an active math equation."
                        chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                        speakAloud(reply)
                        return
                    }
                    currentStepIndex = maxOf(0, currentStepIndex - 1)
                    val conceptInfo = com.example.domain.scanner.MathKnowledgeBase.classify(
                        activeExpression,
                        TextUnderstandingEngine.process(activeExpression).category
                    )
                    val tutorResponse = AITutorEngine.generateTutorResponse(
                        activeExpression,
                        conceptInfo,
                        AITutorMode.NEXT_STEP,
                        currentStepIndex
                    )
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = tutorResponse))
                    speakAloud(tutorResponse)
                    return
                }
                "show_hint" -> {
                    if (activeExpression.isEmpty()) {
                        val reply = "We don't have an active math equation."
                        chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                        speakAloud(reply)
                        return
                    }
                    val conceptInfo = com.example.domain.scanner.MathKnowledgeBase.classify(
                        activeExpression,
                        TextUnderstandingEngine.process(activeExpression).category
                    )
                    val tutorResponse = AITutorEngine.generateTutorResponse(
                        activeExpression,
                        conceptInfo,
                        AITutorMode.HINT,
                        currentStepIndex
                    )
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = tutorResponse))
                    speakAloud(tutorResponse)
                    return
                }
                "explain_why" -> {
                    if (activeExpression.isEmpty()) {
                        val reply = "We don't have an active math equation."
                        chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                        speakAloud(reply)
                        return
                    }
                    val conceptInfo = com.example.domain.scanner.MathKnowledgeBase.classify(
                        activeExpression,
                        TextUnderstandingEngine.process(activeExpression).category
                    )
                    val tutorResponse = AITutorEngine.generateTutorResponse(
                        activeExpression,
                        conceptInfo,
                        AITutorMode.EXPLAIN_WHY,
                        currentStepIndex
                    )
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = tutorResponse))
                    speakAloud(tutorResponse)
                    return
                }
                "show_formula" -> {
                    if (activeExpression.isEmpty()) {
                        val reply = "We don't have an active math equation."
                        chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                        speakAloud(reply)
                        return
                    }
                    val conceptInfo = com.example.domain.scanner.MathKnowledgeBase.classify(
                        activeExpression,
                        TextUnderstandingEngine.process(activeExpression).category
                    )
                    val tutorResponse = AITutorEngine.generateTutorResponse(
                        activeExpression,
                        conceptInfo,
                        AITutorMode.FORMULA_FIRST,
                        currentStepIndex
                    )
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = tutorResponse))
                    speakAloud(tutorResponse)
                    return
                }
                "another_method" -> {
                    if (activeExpression.isEmpty()) {
                        val reply = "We don't have an active math equation."
                        chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                        speakAloud(reply)
                        return
                    }
                    val conceptInfo = com.example.domain.scanner.MathKnowledgeBase.classify(
                        activeExpression,
                        TextUnderstandingEngine.process(activeExpression).category
                    )
                    val tutorResponse = AITutorEngine.generateTutorResponse(
                        activeExpression,
                        conceptInfo,
                        AITutorMode.ANOTHER_METHOD,
                        currentStepIndex
                    )
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = tutorResponse))
                    speakAloud(tutorResponse)
                    return
                }
                "answer_only" -> {
                    if (activeExpression.isEmpty()) {
                        val reply = "We don't have an active math equation."
                        chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                        speakAloud(reply)
                        return
                    }
                    val conceptInfo = com.example.domain.scanner.MathKnowledgeBase.classify(
                        activeExpression,
                        TextUnderstandingEngine.process(activeExpression).category
                    )
                    val tutorResponse = AITutorEngine.generateTutorResponse(
                        activeExpression,
                        conceptInfo,
                        AITutorMode.ANSWER_ONLY,
                        currentStepIndex
                    )
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = tutorResponse))
                    speakAloud(tutorResponse)
                    return
                }
                "similar_question" -> {
                    val reply = "Here is another practice problem for you:"
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    val chapter = com.example.domain.scanner.MathChapter.COMPLEX_NUMBERS_QUADRATIC_EQUATIONS
                    val level = com.example.domain.scanner.MathLevel.CLASS_11_12
                    val questions = com.example.domain.scanner.QuestionGeneratorEngine.generateQuestions(chapter, level, count = 1)
                    val q = questions.first()
                    val questionReply = "📝 Problem: ${q.text}\n💡 Hint: ${q.hint ?: "Try applying basic concepts."}"
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = questionReply))
                    speakAloud(questionReply)
                    return
                }
                "stop" -> {
                    stopSpeaking()
                    return
                }
                "repeat" -> {
                    val lastTutorMessage = chatMessages.lastOrNull { m -> m.sender == MessageSender.TUTOR }
                    if (lastTutorMessage != null) {
                        speakAloud(lastTutorMessage.text)
                    } else {
                        val reply = "Nothing to repeat yet!"
                        chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                        speakAloud(reply)
                    }
                    return
                }
                "slower" -> {
                    speechRate = maxOf(0.5f, speechRate - 0.2f)
                    val reply = "Speaking slower now."
                    chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                    speakAloud(reply)
                    return
                }
            }
        }

        // 3. Check Prefix-based Plot Commands (from "plot_expression" trigger)
        val plotCommand = voiceCommands.find { it.commandId == "plot_expression" }
        if (plotCommand != null) {
            val matchedPlotAlias = plotCommand.aliases.find { alias ->
                processedSpoken.startsWith(alias)
            }
            if (matchedPlotAlias != null) {
                val exprSpoken = processedSpoken.removePrefix(matchedPlotAlias).replace("y =", "").replace("y=", "").trim()
                val expr = when (exprSpoken) {
                    "x square", "x^2", "x²" -> "x^2"
                    "sin(x)", "sin x", "sine x" -> "sin(x)"
                    "cos(x)", "cos x", "cosine x" -> "cos(x)"
                    else -> exprSpoken
                }
                val reply = "Plotting y equals $expr."
                chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                speakAloud(reply)
                graphViewModel?.addExpression(expr)
                coroutineScope.launch {
                    delay(1200)
                    onNavigateTo("graph_plotter")
                    onDismissRequest()
                }
                return
            }
        }

        // ==========================================
        // 7. SYMBOLIC MATH UNDERSTANDING ENGINE
        // ==========================================
        // 1. Phonetic voice correction
        val translatedMath = processVoiceSpeechToMath(input)
        
        // 2. Classify intent via TextUnderstandingEngine
        val understanding = TextUnderstandingEngine.process(translatedMath)
        
        Log.d("VoiceTutor", "Processed input: '$input' -> translated: '$translatedMath' -> intent: ${understanding.intent}, command: ${understanding.extractedCommand}, expr: ${understanding.expression}")
        
        if (understanding.intent == MathIntent.UNKNOWN || 
            (understanding.expression.isBlank() && understanding.extractedCommand == null && understanding.intent != MathIntent.TUTOR)) {
            // Polite fallback for general conversation
            val reply = getPoliteReply(input)
            chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
            speakAloud(reply)
            return
        }

        // 3. Stateful sequential operations handling (next step, hint, explain why, another method)
        val finalCommand = understanding.extractedCommand
        val finalExpression = understanding.expression
        
        if (finalCommand in listOf("next_step", "hint", "formula", "explain_why", "another_method") && finalExpression.isEmpty()) {
            // Context-based evaluation on active expression!
            if (activeExpression.isEmpty()) {
                val reply = "We don't have an active math equation to work on. Speak a problem first, like \"solve x square minus five x plus six equals zero\"."
                chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = reply))
                speakAloud(reply)
                return
            }
            
            // Execute conversational updates on remembered activeExpression
            if (finalCommand == "next_step") {
                currentStepIndex++
            }
            
            val requestedMode = when (finalCommand) {
                "next_step" -> AITutorMode.NEXT_STEP
                "hint" -> AITutorMode.HINT
                "formula" -> AITutorMode.FORMULA_FIRST
                "explain_why" -> AITutorMode.EXPLAIN_WHY
                "another_method" -> AITutorMode.ANOTHER_METHOD
                else -> AITutorMode.FULL_SOLUTION
            }
            
            val conceptInfo = com.example.domain.scanner.MathKnowledgeBase.classify(
                activeExpression,
                TextUnderstandingEngine.process(activeExpression).category
            )
            
            val tutorResponse = AITutorEngine.generateTutorResponse(
                activeExpression,
                conceptInfo,
                requestedMode,
                currentStepIndex
            )
            
            chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = tutorResponse))
            speakAloud(tutorResponse)
        } else {
            // This is a new equation/expression spoken! Set it active
            activeExpression = translatedMath
            currentStepIndex = 0
            
            // Resolve response via parser
            val parserResponse = parser.parse(translatedMath)
            chatMessages.add(TutorChatMessage(sender = MessageSender.TUTOR, text = parserResponse))
            speakAloud(parserResponse)
        }
    }

    // Speech functions
    fun safeStartListening() {
        if (speechRecognizer == null) return
        stopSpeaking()
        try {
            speechRecognizer.cancel()
            errorMessage = "Listening... Speak now"
            recognizedText = ""
            rmsLevel = 0f
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLocale)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            speechRecognizer.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            errorMessage = "Could not initialize mic: ${e.localizedMessage}"
            isListening = false
        }
    }

    fun safeStopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceTutor", "Failed to stop listening", e)
        }
        isListening = false
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                errorMessage = "Listening closely..."
            }

            override fun onBeginningOfSpeech() {
                errorMessage = "Hearing speech..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                rmsLevel = rmsdB
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                errorMessage = "Processing..."
            }

            override fun onError(error: Int) {
                val errorDesc = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Offline pack not configured"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout. Try again."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Tap mic again."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech engine busy"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard"
                    else -> "No speech heard (Tap mic)"
                }
                errorMessage = errorDesc
                isListening = false
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spoken = matches[0]
                    recognizedText = spoken
                    processTutorInput(spoken)
                    errorMessage = "Ready"
                } else {
                    errorMessage = "Could not process speech"
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
                Log.e("VoiceTutor", "Error releasing recognizer", e)
            }
        }
    }

    // Modal layout with M3 Styling and responsiveness for all sizes
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, "Tutor icon", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Voice AI Tutor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Fully Offline Math Pedagogist", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        actions = {
                            // Customize triggers button
                            IconButton(onClick = { showCustomizerDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Customize voice commands",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            // TTS Toggle
                            IconButton(onClick = {
                                isTtsEnabled = !isTtsEnabled
                                sharedPrefs.edit().putBoolean(KEY_TTS_ENABLED, isTtsEnabled).apply()
                                if (!isTtsEnabled) stopSpeaking()
                            }) {
                                Icon(
                                    imageVector = if (isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Toggle text to speech",
                                    tint = if (isTtsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                            IconButton(onClick = {
                                stopSpeaking()
                                onDismissRequest()
                            }) {
                                Icon(Icons.Default.Close, "Close tutor")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Chat message stack (Dynamic scrolling)
                    val listState = rememberLazyListState()
                    LaunchedEffect(chatMessages.size) {
                        if (chatMessages.isNotEmpty()) {
                            listState.animateScrollToItem(chatMessages.size - 1)
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { message ->
                            ChatBubble(message)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live transcript card
                    if (recognizedText.isNotEmpty() || isListening) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = "Spoken text",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (recognizedText.isEmpty()) "Speak now..." else recognizedText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Quick Action Helper Assist Chips
                    if (activeExpression.isNotEmpty()) {
                        Text(
                            "Contextual Commands",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val contextCommands = listOf(
                                "Hint" to "show hint",
                                "Next Step" to "next step",
                                "Formula" to "formula first",
                                "Explain Why" to "explain why",
                                "Alternative" to "another method"
                            )
                            contextCommands.forEach { (label, cmd) ->
                                AssistChip(
                                    onClick = { processTutorInput(cmd) },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Bottom Control Desk: Language Picker & Record Trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language Selection
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val languages = listOf("en-US" to "EN", "en-IN" to "Hinglish", "hi-IN" to "हिंदी")
                            languages.forEach { (code, name) ->
                                FilterChip(
                                    selected = selectedLanguage == code,
                                    onClick = {
                                        selectedLanguage = code
                                        sharedPrefs.edit().putString(KEY_LANGUAGE, code).apply()
                                    },
                                    label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        // Voice Trigger Mic Button with Pulse Concentric Circles
                        Box(
                            modifier = Modifier.size(75.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isListening) {
                                val pulse1 by rememberInfiniteTransition("tutor1").animateFloat(
                                    initialValue = 0.9f,
                                    targetValue = 1.3f,
                                    animationSpec = infiniteRepeatable(tween(800, easing = EaseOutQuad), RepeatMode.Reverse),
                                    label = "pulse1"
                                )
                                val pulse2 by rememberInfiniteTransition("tutor2").animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 1.5f,
                                    animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutQuad), RepeatMode.Reverse),
                                    label = "pulse2"
                                )
                                val factor = (rmsLevel.coerceIn(0f, 10f) / 10f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(pulse2 * (1f + factor * 0.4f))
                                        .alpha(0.12f)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .scale(pulse1 * (1f + factor * 0.2f))
                                        .alpha(0.20f)
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (!permissionState.status.isGranted) {
                                        permissionState.launchPermissionRequest()
                                    } else {
                                        if (isListening) {
                                            safeStopListening()
                                        } else {
                                            safeStartListening()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        if (isListening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    )
                                    .testTag("tutor_voice_mic_button")
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = if (isListening) "Stop recognition" else "Start recognition",
                                    tint = if (isListening) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Status Error/Prompt text
                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = if (msg.contains("error") || msg.contains("issue") || msg.contains("No")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCustomizerDialog && voiceCommandRepository != null) {
        VoiceCommandCustomizerDialog(
            voiceCommands = voiceCommands,
            onSaveCommand = { cmd ->
                coroutineScope.launch {
                    voiceCommandRepository.saveVoiceCommand(cmd)
                }
            },
            onResetCommand = { cmdId ->
                coroutineScope.launch {
                    voiceCommandRepository.resetToDefault(cmdId)
                }
            },
            onResetAll = {
                coroutineScope.launch {
                    voiceCommandRepository.resetAllToDefault()
                }
            },
            advancedVoiceModeEnabled = advancedVoiceModeEnabled,
            onSetAdvancedVoiceModeEnabled = { enabled ->
                if (enabled && !permissionState.status.isGranted) {
                    permissionState.launchPermissionRequest()
                } else {
                    coroutineScope.launch {
                        localSettings.setAdvancedVoiceModeEnabled(enabled)
                        if (enabled) {
                            val intent = android.content.Intent().apply {
                                setClassName(context.packageName, "com.example.service.VoiceBackgroundService")
                            }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        } else {
                            val intent = android.content.Intent().apply {
                                setClassName(context.packageName, "com.example.service.VoiceBackgroundService")
                            }
                            context.stopService(intent)
                        }
                    }
                }
            },
            voiceAutoStartEnabled = voiceAutoStartEnabled,
            onSetVoiceAutoStartEnabled = { enabled ->
                coroutineScope.launch {
                    localSettings.setVoiceAutoStartEnabled(enabled)
                }
            },
            continuousListeningEnabled = continuousListeningEnabled,
            onSetContinuousListeningEnabled = { enabled ->
                coroutineScope.launch {
                    localSettings.setContinuousListeningEnabled(enabled)
                    if (advancedVoiceModeEnabled) {
                        val intent = android.content.Intent().apply {
                            setClassName(context.packageName, "com.example.service.VoiceBackgroundService")
                        }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    }
                }
            },
            voiceHistoryList = voiceHistoryList,
            onClearVoiceHistory = {
                coroutineScope.launch {
                    localVoiceHistory.clearVoiceHistory()
                }
            },
            onDismiss = { showCustomizerDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCommandCustomizerDialog(
    voiceCommands: List<com.example.domain.model.VoiceCommand>,
    onSaveCommand: (com.example.domain.model.VoiceCommand) -> Unit,
    onResetCommand: (String) -> Unit,
    onResetAll: () -> Unit,
    advancedVoiceModeEnabled: Boolean,
    onSetAdvancedVoiceModeEnabled: (Boolean) -> Unit,
    voiceAutoStartEnabled: Boolean,
    onSetVoiceAutoStartEnabled: (Boolean) -> Unit,
    continuousListeningEnabled: Boolean,
    onSetContinuousListeningEnabled: (Boolean) -> Unit,
    voiceHistoryList: List<com.example.domain.model.VoiceHistory>,
    onClearVoiceHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQueries by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Voice Settings & Triggers", style = MaterialTheme.typography.titleLarge)
                if (activeTab == 0) {
                    TextButton(onClick = onResetAll) {
                        Text("Reset All")
                    }
                } else if (activeTab == 2 && voiceHistoryList.isNotEmpty()) {
                    TextButton(onClick = onClearVoiceHistory) {
                        Text("Clear History")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Tab Selection Row
                TabRow(
                    selectedTabIndex = activeTab,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                        Text("Triggers", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                        Text("Advanced", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                        Text("History", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (activeTab == 0) {
                    // Original Trigger customization tab
                    Text(
                        "Define your custom wake phrases and command triggers in English, Hindi, or Hinglish.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = searchQueries,
                        onValueChange = { searchQueries = it },
                        placeholder = { Text("Search commands...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val filtered = voiceCommands.filter {
                        it.commandName.contains(searchQueries, ignoreCase = true) ||
                        it.aliases.any { alias -> alias.contains(searchQueries, ignoreCase = true) }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { cmd ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cmd.commandName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        IconButton(
                                            onClick = { onResetCommand(cmd.commandId) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Reset command to default",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        cmd.aliases.chunked(3).forEach { rowAliases ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                rowAliases.forEach { alias ->
                                                    SuggestionChip(
                                                        onClick = {
                                                            val updatedList = cmd.aliases.filter { it != alias }
                                                            onSaveCommand(cmd.copy(aliases = updatedList))
                                                        },
                                                        label = {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(alias, style = MaterialTheme.typography.labelSmall)
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.error)
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        var showAddField by remember { mutableStateOf(false) }
                                        if (showAddField) {
                                            var textVal by remember { mutableStateOf("") }
                                            OutlinedTextField(
                                                value = textVal,
                                                onValueChange = { textVal = it },
                                                placeholder = { Text("e.g. upar jao") },
                                                modifier = Modifier.weight(1f).height(48.dp),
                                                textStyle = MaterialTheme.typography.bodySmall,
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    if (textVal.trim().isNotEmpty()) {
                                                        val cleanVal = textVal.trim().lowercase()
                                                        if (!cmd.aliases.contains(cleanVal)) {
                                                            onSaveCommand(cmd.copy(aliases = cmd.aliases + cleanVal))
                                                        }
                                                    }
                                                    showAddField = false
                                                }
                                            ) {
                                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        } else {
                                            TextButton(onClick = { showAddField = true }) {
                                                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Add Alias", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (activeTab == 1) {
                    // Advanced voice mode toggles and battery advice
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Advanced Voice Mode Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Advanced Voice Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Enables background listening service with a persistent notification.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = advancedVoiceModeEnabled,
                                onCheckedChange = onSetAdvancedVoiceModeEnabled
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Auto-start Service Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unlock Auto-Start", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Start the service automatically when you unlock your device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = voiceAutoStartEnabled,
                                onCheckedChange = onSetVoiceAutoStartEnabled,
                                enabled = advancedVoiceModeEnabled
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Continuous Listening Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Continuous Listening", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Keep background service listening continuously after executing a command.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = continuousListeningEnabled,
                                onCheckedChange = onSetContinuousListeningEnabled,
                                enabled = advancedVoiceModeEnabled
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Battery optimization guidance card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Battery Info",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Battery Optimization Advice", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Android may terminate the background listener to save power.\n" +
                                    "For best performance:\n" +
                                    "1. Long press this App icon on your home screen.\n" +
                                    "2. Go to App Info > Battery.\n" +
                                    "3. Select 'Unrestricted' instead of 'Optimized'.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else if (activeTab == 2) {
                    // Voice History logs list
                    if (voiceHistoryList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "No history",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No voice history logs yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        val timeFormat = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()) }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(voiceHistoryList) { history ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Prompt: \"${history.prompt}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = timeFormat.format(java.util.Date(history.timestamp)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Action: ${history.action}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun ChatBubble(message: TutorChatMessage) {
    val isUser = message.sender == MessageSender.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Tutor Avatar",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 0.dp,
                bottomEnd = if (isUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Highly robust phonetic speech-to-math conversion supporting Hindi/Hinglish/English
 */
fun processVoiceSpeechToMath(input: String): String {
    var text = input.lowercase(Locale.ROOT)

    // Convert Hindi written/spoken numbers to actual numbers
    val hindiDigits = mapOf(
        "shunya" to "0", "zero" to "0", "ek" to "1", "do" to "2", "teen" to "3", "char" to "4",
        "panch" to "5", "chhe" to "6", "chhah" to "6", "saat" to "7", "aath" to "8", "nau" to "9", "das" to "10",
        "शून्य" to "0", "जीरो" to "0", "एक" to "1", "दो" to "2", "तीन" to "3", "चार" to "4",
        "पाँच" to "5", "पांच" to "5", "छह" to "6", "छः" to "6", "सात" to "7", "आठ" to "8", "नौ" to "9", "दस" to "10"
    )
    hindiDigits.forEach { (word, digit) ->
        text = text.replace(word, digit)
    }

    // Number word spelling helper (English to digits)
    val words = text.split(Regex("\\s+"))
    val resultList = mutableListOf<String>()
    
    val units = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
        "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13,
        "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
        "eighteen" to 18, "nineteen" to 19, "twenty" to 20, "thirty" to 30,
        "forty" to 40, "fifty" to 50, "sixty" to 60, "seventy" to 70,
        "eighty" to 80, "ninety" to 90
    )
    
    for (w in words) {
        val mapped = units[w]
        if (mapped != null) {
            resultList.add(mapped.toString())
        } else {
            resultList.add(w)
        }
    }
    text = resultList.joinToString(" ")

    // Mathematical terms mapping
    val map = listOf(
        // Variables & Exponents
        "x square" to "x^2", "x square plus" to "x^2 +", "x cube" to "x^3",
        "square" to "^2", "cube" to "^3", "power" to "^", "ghat" to "^", "ghaat" to "^",
        
        // Operators
        "plus" to "+", "jama" to "+", "jod" to "+", "jodein" to "+",
        "minus" to "-", "ghata" to "-", "ghatayein" to "-",
        "times" to "*", "into" to "*", "multiplied by" to "*", "gune" to "*",
        "divided by" to "/", "divide" to "/", "bata" to "/", "bhag" to "/", "bhage" to "/",
        "equals" to "=", "equal to" to "=", "barabar" to "=",
        
        // Functions
        "sin of" to "sin(", "sine of" to "sin(", "sine" to "sin(", "sin" to "sin(",
        "cos of" to "cos(", "cosine of" to "cos(", "cosine" to "cos(", "cos" to "cos(",
        "tan of" to "tan(", "tangent of" to "tan(", "tangent" to "tan(", "tan" to "tan(",
        "log of" to "log(", "log" to "log(", "ln of" to "ln(", "ln" to "ln(",
        "square root of" to "sqrt(", "square root" to "sqrt(", "root" to "sqrt("
    ).sortedByDescending { it.first.length }

    map.forEach { (phrase, replacement) ->
        text = text.replace(phrase, replacement)
    }

    // Insert multiplying asterisks e.g. "5x" -> "5*x"
    text = text.replace(Regex("(\\d+)\\s*([a-zA-Z])"), "$1*$2")

    // Clean brackets
    var openBrackets = text.count { it == '(' }
    var closeBrackets = text.count { it == ')' }
    if (openBrackets > closeBrackets) {
        text += ")".repeat(openBrackets - closeBrackets)
    }

    return text.trim()
}

/**
 * General polite fallback messages when user speaks non-mathematical content
 */
fun getPoliteReply(input: String): String {
    val replies = listOf(
        "I'm specialized as an AI Math Tutor! Let's solve some equations, simplify expressions, or do derivatives. What math problem are you working on?",
        "That's interesting! However, my expertise is strictly in mathematics. Try saying 'solve x square plus 5x plus 6 equals 0' or 'integrate cos x'.",
        "I am offline and dedicated to guiding your math journey! Let's work on algebraic factoring, calculus derivatives, or symbolic solving together. Speak any equation!",
        "My symbolic processors are optimized for math tutoring! Ask me questions like 'show hint for x square minus nine' or 'differentiate x cube'."
    )
    val index = Math.abs(input.hashCode()) % replies.size
    return replies[index]
}
