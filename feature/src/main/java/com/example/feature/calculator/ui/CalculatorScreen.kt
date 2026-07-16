package com.example.feature.calculator.ui

import android.util.Log
import android.content.res.Configuration
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.EditCommand
import androidx.compose.ui.text.input.PlatformTextInputService
import androidx.compose.ui.text.input.TextInputService
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.calculator.CalculatorEvent
import com.example.feature.calculator.CalculatorState
import com.example.feature.calculator.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateToAgeCalculator: () -> Unit = {},
    onNavigateToUnitConverter: () -> Unit = {},
    onNavigateToConstants: () -> Unit = {},
    onNavigateToPercentageCgpa: () -> Unit = {},
    onNavigateToEmiCalculator: () -> Unit = {},
    onNavigateToHealthCalculator: () -> Unit = {},
    onNavigateToCurrencyConverter: () -> Unit = {},
    onNavigateToDateTimeCalculator: () -> Unit = {},
    onNavigateToMathScanner: () -> Unit = {},
    onNavigateToGraphPlotter: () -> Unit = {},
    onNavigateToMatrixCalculator: () -> Unit = {},
    onNavigateToEquationSolver: () -> Unit = {},
    onNavigateToCalculus: () -> Unit = {},
    onNavigateToComplexCalculator: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    viewModel: CalculatorViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val context = LocalContext.current
    var bitmap by remember(state.backgroundImageUri) { mutableStateOf<ImageBitmap?>(null) }

    // Intercept content URI to copy locally for absolute persistence
    LaunchedEffect(state.backgroundImageUri) {
        val uriStr = state.backgroundImageUri
        if (uriStr != null) {
            if (uriStr.startsWith("content://")) {
                try {
                    val uri = Uri.parse(uriStr)
                    val fileName = "custom_background_${System.currentTimeMillis()}.png"
                    val file = File(context.filesDir, fileName)
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            file.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }
                    val localUriStr = Uri.fromFile(file).toString()
                    viewModel.onEvent(CalculatorEvent.SetBackgroundImageUri(localUriStr))
                    
                    // Clean up old custom background files
                    withContext(Dispatchers.IO) {
                        context.filesDir.listFiles { _, name ->
                            name.startsWith("custom_background_") && name != fileName
                        }?.forEach { it.delete() }
                    }
                } catch (e: Exception) {
                    Log.e("CalculatorScreen", "Failed to copy background image to local storage", e)
                }
            }
        } else {
            // Clean up all background files if background removed
            try {
                withContext(Dispatchers.IO) {
                    context.filesDir.listFiles { _, name ->
                        name.startsWith("custom_background_")
                    }?.forEach { it.delete() }
                }
            } catch (e: Exception) {
                Log.e("CalculatorScreen", "Failed to clean up background files", e)
            }
        }
    }

    // Safely load bitmap
    LaunchedEffect(state.backgroundImageUri) {
        val uriStr = state.backgroundImageUri
        if (uriStr != null) {
            try {
                val uri = Uri.parse(uriStr)
                val loadedBitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        context.contentResolver.openInputStream(uri)?.use { tempStream ->
                            BitmapFactory.decodeStream(tempStream, null, options)
                        }
                        
                        val reqWidth = 1080
                        val reqHeight = 1920
                        var inSampleSize = 1
                        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                            val halfHeight = options.outHeight / 2
                            val halfWidth = options.outWidth / 2
                            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                                inSampleSize *= 2
                            }
                        }
                        
                        val decodeOptions = BitmapFactory.Options().apply {
                            this.inSampleSize = inSampleSize
                        }
                        
                        context.contentResolver.openInputStream(uri)?.use { actualStream ->
                            BitmapFactory.decodeStream(actualStream, null, decodeOptions)?.asImageBitmap()
                        }
                    }
                }
                bitmap = loadedBitmap
            } catch (e: Exception) {
                Log.e("CalculatorScreen", "Error loading background image", e)
                bitmap = null
            }
        } else {
            bitmap = null
        }
    }
    
    var showMenu by remember { mutableStateOf(false) }
    
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAdvancedDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Settings") },
            text = {
                SettingsScreen(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showThemeDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showThemeDialog = false }
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                ThemeCustomizationScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onDismiss = { showThemeDialog = false }
                )
            }
        }
    }

    if (showAboutDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showAboutDialog = false }
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                AboutScreen(
                    onDismiss = { showAboutDialog = false }
                )
            }
        }
    }

    if (showAdvancedDialog) {
        com.example.feature.advancedfeatures.ui.AdvancedFeaturesScreen(
            onDismiss = { showAdvancedDialog = false },
            onNavigateToUnitConverter = { 
                showAdvancedDialog = false
                onNavigateToUnitConverter()
            },
            onNavigateToPercentageCgpa = {
                showAdvancedDialog = false
                onNavigateToPercentageCgpa()
            },
            onNavigateToEmiCalculator = {
                showAdvancedDialog = false
                onNavigateToEmiCalculator()
            },
            onNavigateToHealthCalculator = {
                showAdvancedDialog = false
                onNavigateToHealthCalculator()
            },
            onNavigateToCurrencyConverter = {
                showAdvancedDialog = false
                onNavigateToCurrencyConverter()
            },
            onNavigateToDateTimeCalculator = {
                showAdvancedDialog = false
                onNavigateToDateTimeCalculator()
            },
            onNavigateToAgeCalculator = {
                showAdvancedDialog = false
                onNavigateToAgeCalculator()
            },
            onNavigateToConstants = {
                showAdvancedDialog = false
                onNavigateToConstants()
            },
            onNavigateToCameraMathSolver = {
                showAdvancedDialog = false
                onNavigateToMathScanner()
            },
            onNavigateToGraphPlotter = {
                showAdvancedDialog = false
                onNavigateToGraphPlotter()
            },
            onNavigateToMatrixCalculator = {
                showAdvancedDialog = false
                onNavigateToMatrixCalculator()
            },
            onNavigateToEquationSolver = {
                showAdvancedDialog = false
                onNavigateToEquationSolver()
            },
            onNavigateToCalculus = {
                showAdvancedDialog = false
                onNavigateToCalculus()
            },
            onNavigateToComplexCalculator = {
                showAdvancedDialog = false
                onNavigateToComplexCalculator()
            },
            onNavigateToStatistics = {
                showAdvancedDialog = false
                onNavigateToStatistics()
            },
            orientationLock = state.orientationLock,
            onOrientationLockChange = { viewModel.onEvent(CalculatorEvent.SetOrientationLock(it)) }
        )
    }


    if (showVoiceDialog) {
        VoiceCalculatorDialog(
            onDismissRequest = { showVoiceDialog = false },
            onEvent = viewModel::onEvent
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!state.showHistory) {
                TopAppBar(
                    title = { Text("My Calculator 4906") },
                    actions = {
                        IconButton(onClick = { onNavigateToMathScanner() }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Math Scanner")
                        }
                        IconButton(onClick = { viewModel.onEvent(CalculatorEvent.ToggleHistory) }) {
                            Icon(Icons.Default.History, contentDescription = "History")
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { showMenu = false; showSettingsDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Advanced Features") },
                                onClick = { showMenu = false; showAdvancedDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Theme Customization") },
                                onClick = { showMenu = false; showThemeDialog = true }
                            )

                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = { showMenu = false; showAboutDialog = true }
                            )

                            DropdownMenuItem(
                                text = { Text("Voice Calculator") },
                                onClick = { showMenu = false; showVoiceDialog = true }
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = state.backgroundOpacity
                )
            }
            if (state.showHistory) {
                HistoryScreen(
                    history = state.history,
                    onEvent = viewModel::onEvent
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalculatorDisplay(
                            state = state,
                            onEvent = viewModel::onEvent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        MemoryRow(
                            onEvent = viewModel::onEvent,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        CalculatorButtonGrid(
                            onEvent = viewModel::onEvent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            isLandscape = true
                        )
                    }
                    
                    ScientificButtonGrid(
                        onEvent = viewModel::onEvent,
                        isDegreeMode = state.isDegreeMode,
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight(),
                        isLandscape = true
                    )
                }
            } else {
                var showScientific by remember { mutableStateOf(false) }
                
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    CalculatorDisplay(
                        state = state,
                        onEvent = viewModel::onEvent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    MemoryRow(
                        onEvent = viewModel::onEvent,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    
                    TextButton(
                        onClick = { showScientific = !showScientific },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 36.dp)
                            .padding(horizontal = 4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (showScientific) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showScientific) "Hide Scientific" else "Scientific Functions",
                            fontSize = 13.sp
                        )
                    }
                    
                    if (showScientific) {
                        ScientificButtonGrid(
                            onEvent = viewModel::onEvent,
                            isDegreeMode = state.isDegreeMode,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                            isLandscape = false
                        )
                    }
                    
                    CalculatorButtonGrid(
                        onEvent = viewModel::onEvent,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                        isLandscape = false
                    )
                }
            }
        }
    }
}
}
}
@Composable
fun MemoryRow(
    onEvent: (CalculatorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val buttons = listOf(
            "MC" to CalculatorEvent.MemoryClear,
            "MR" to CalculatorEvent.MemoryRecall,
            "M+" to CalculatorEvent.MemoryAdd,
            "M\u2212" to CalculatorEvent.MemorySubtract
        )
        buttons.forEach { (symbol, event) ->
            TextButton(
                onClick = { onEvent(event) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(max = 32.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                Text(
                    text = symbol,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AutoScalableText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    minFontSize: TextUnit = 24.sp
) {
    var fontSizeValue by remember(text) { mutableStateOf(style.fontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        style = style.copy(fontSize = fontSizeValue),
        maxLines = maxLines,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                if (fontSizeValue > minFontSize) {
                    fontSizeValue = (fontSizeValue.value * 0.9f).sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
fun CalculatorDisplay(
    state: CalculatorState,
    onEvent: (CalculatorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    val noOpKeyboardController = remember {
        object : SoftwareKeyboardController {
            override fun show() {
                Log.d("KeyboardDebug", "showSoftwareKeyboard requested - BLOCKING")
            }
            override fun hide() {
                Log.d("KeyboardDebug", "hideSoftwareKeyboard")
            }
        }
    }

    val noOpTextInputService = remember {
        TextInputService(object : PlatformTextInputService {
            override fun startInput(
                value: TextFieldValue,
                imeOptions: ImeOptions,
                onEditCommand: (List<EditCommand>) -> Unit,
                onImeActionPerformed: (ImeAction) -> Unit
            ) {
                Log.d("KeyboardDebug", "startInput requested - BLOCKING")
            }
            override fun stopInput() {
                Log.d("KeyboardDebug", "stopInput")
            }
            override fun showSoftwareKeyboard() {
                Log.d("KeyboardDebug", "showSoftwareKeyboard requested - BLOCKING")
            }
            override fun hideSoftwareKeyboard() {
                Log.d("KeyboardDebug", "hideSoftwareKeyboard")
            }
            override fun updateState(oldValue: TextFieldValue?, newValue: TextFieldValue) {}
        })
    }

    // Auto-scroll to the bottom when the expression updates
    LaunchedEffect(state.currentExpression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    CompositionLocalProvider(
        LocalTextInputService provides noOpTextInputService,
        LocalSoftwareKeyboardController provides noOpKeyboardController
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures {
                        Log.d("KeyboardDebug", "Display area tapped")
                        focusRequester.requestFocus()
                    }
                },
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.isDegreeMode) {
                    Text("DEG", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 14.sp)
                } else {
                    Text("RAD", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 14.sp)
                }
                if (state.memoryValue != 0.0) {
                    Text("M", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 1. Expression Area: Vertically scrollable, takes up remaining vertical space
        // As expressions grow, they grow upward naturally.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Fills the available space, pushing the result to the bottom
                .verticalScroll(scrollState),
            contentAlignment = Alignment.BottomEnd
        ) {
            BasicTextField(
                value = TextFieldValue(
                    text = state.currentExpression,
                    selection = TextRange(state.selectionStart, state.selectionEnd)
                ),
                onValueChange = { newValue ->
                    onEvent(CalculatorEvent.UpdateExpression(newValue))
                },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    fontSize = 28.sp,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 36.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        Log.d("KeyboardDebug", "Focus changed: isFocused=${it.isFocused}")
                    },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = false,
                readOnly = false,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.None,
                    keyboardType = KeyboardType.Text
                ),
                visualTransformation = VisualTransformation.None
            )
        }
        
        // Fixed Spacing/Separator between Expression and Result
        Spacer(modifier = Modifier.height(12.dp))
        
        // 2. Result Area: Fixed at the bottom of the display, never overlaps or jumps.
        val rawResult = if (state.result.isNotEmpty()) state.result else state.liveResult
        val resultText = if (rawResult.isNotEmpty()) "= $rawResult" else ""
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp), // Stable height to prevent jumping if empty
            contentAlignment = Alignment.CenterEnd
        ) {
            if (resultText.isNotEmpty()) {
                AutoScalableText(
                    text = resultText,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
    }
}
@Composable
fun CalculatorButtonGrid(
    onEvent: (CalculatorEvent) -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean
) {
    val buttonSpacing = 8.dp
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val onTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer
    
    val baseButtonModifier = if (isLandscape) Modifier.fillMaxHeight() else Modifier.aspectRatio(1.2f)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "AC", color = secondaryContainer, textColor = onSecondaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.Clear) })
            CalculatorButton(symbol = "DEL", color = secondaryContainer, textColor = onSecondaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.DeleteLast) })
            CalculatorButton(symbol = "%", color = secondaryContainer, textColor = onSecondaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('%')) })
            CalculatorButton(symbol = "÷", color = tertiaryContainer, textColor = onTertiaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('÷')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "7", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('7')) })
            CalculatorButton(symbol = "8", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('8')) })
            CalculatorButton(symbol = "9", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('9')) })
            CalculatorButton(symbol = "×", color = tertiaryContainer, textColor = onTertiaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('×')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "4", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('4')) })
            CalculatorButton(symbol = "5", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('5')) })
            CalculatorButton(symbol = "6", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('6')) })
            CalculatorButton(symbol = "-", color = tertiaryContainer, textColor = onTertiaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('-')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "1", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('1')) })
            CalculatorButton(symbol = "2", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('2')) })
            CalculatorButton(symbol = "3", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('3')) })
            CalculatorButton(symbol = "+", color = tertiaryContainer, textColor = onTertiaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('+')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "±", color = secondaryContainer, textColor = onSecondaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.TogglePositiveNegative) })
            CalculatorButton(symbol = "0", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('0')) })
            CalculatorButton(symbol = ".", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('.')) })
            CalculatorButton(symbol = "=", color = primaryContainer, textColor = onPrimaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.Calculate) })
        }
    }
}

@Composable
fun ScientificButton(
    symbol: String,
    label: String,
    tooltip: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("sci_btn_$symbol")
            .then(modifier)
    ) {
        Text(
            text = symbol,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun ScientificButtonGrid(
    onEvent: (CalculatorEvent) -> Unit,
    isDegreeMode: Boolean,
    modifier: Modifier = Modifier,
    isLandscape: Boolean
) {
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    
    val categories = listOf(
        "Trigonometry",
        "Inv Trig",
        "Hyperbolic",
        "Powers & Roots",
        "Logarithmic",
        "Combinatorics",
        "Constants"
    )

    val categoryDescriptions = listOf(
        "Trigonometry: Calculates sine, cosine, and tangent. Toggle DEG/RAD mode.",
        "Inverse Trig: Calculates arcsine, arccosine, and arctangent in DEG or RAD.",
        "Hyperbolic: Calculates hyperbolic sine, cosine, and tangent.",
        "Powers & Roots: Square, cube, xʸ powers, and square/cube roots.",
        "Logarithmic & Exp: Log base 10, natural log, exponential eˣ, and powers of 10.",
        "Combinatorics: Calculate Permutations (nPr), Combinations (nCr), MOD, and Factorials.",
        "Constants: Standard mathematical constants Pi (π) and Euler's number (e)."
    )

    var activeDescription by remember(selectedCategoryIndex) { 
        mutableStateOf(categoryDescriptions[selectedCategoryIndex]) 
    }

    class SciBtn(
        val symbol: String,
        val label: String,
        val desc: String,
        val onClick: () -> Unit
    )

    val buttons = remember(selectedCategoryIndex, isDegreeMode) {
        when (selectedCategoryIndex) {
            0 -> listOf(
                SciBtn("sin", "sine", "sin(x): Sine function.") {
                    activeDescription = "sin(x): Sine of angle x in ${if (isDegreeMode) "degrees" else "radians"}."
                    onEvent(CalculatorEvent.InputString("sin("))
                },
                SciBtn("cos", "cosine", "cos(x): Cosine function.") {
                    activeDescription = "cos(x): Cosine of angle x in ${if (isDegreeMode) "degrees" else "radians"}."
                    onEvent(CalculatorEvent.InputString("cos("))
                },
                SciBtn("tan", "tangent", "tan(x): Tangent function.") {
                    activeDescription = "tan(x): Tangent of angle x in ${if (isDegreeMode) "degrees" else "radians"}."
                    onEvent(CalculatorEvent.InputString("tan("))
                },
                SciBtn(if (isDegreeMode) "DEG" else "RAD", "angle mode", "Toggle between Degrees and Radians.") {
                    activeDescription = "Angle mode toggled. Now using ${if (!isDegreeMode) "Degrees (DEG)" else "Radians (RAD)"}."
                    onEvent(CalculatorEvent.ToggleAngleMode)
                }
            )
            1 -> listOf(
                SciBtn("sin⁻¹", "arcsine", "sin⁻¹(x): Inverse sine function.") {
                    activeDescription = "sin⁻¹(x): Arcsine. Returns angle in ${if (isDegreeMode) "degrees" else "radians"}."
                    onEvent(CalculatorEvent.InputString("sin⁻¹("))
                },
                SciBtn("cos⁻¹", "arccosine", "cos⁻¹(x): Inverse cosine function.") {
                    activeDescription = "cos⁻¹(x): Arccosine. Returns angle in ${if (isDegreeMode) "degrees" else "radians"}."
                    onEvent(CalculatorEvent.InputString("cos⁻¹("))
                },
                SciBtn("tan⁻¹", "arctangent", "tan⁻¹(x): Inverse tangent function.") {
                    activeDescription = "tan⁻¹(x): Arctangent. Returns angle in ${if (isDegreeMode) "degrees" else "radians"}."
                    onEvent(CalculatorEvent.InputString("tan⁻¹("))
                }
            )
            2 -> listOf(
                SciBtn("sinh", "sine h", "sinh(x): Hyperbolic sine function.") {
                    activeDescription = "sinh(x): Hyperbolic sine of x."
                    onEvent(CalculatorEvent.InputString("sinh("))
                },
                SciBtn("cosh", "cosine h", "cosh(x): Hyperbolic cosine function.") {
                    activeDescription = "cosh(x): Hyperbolic cosine of x."
                    onEvent(CalculatorEvent.InputString("cosh("))
                },
                SciBtn("tanh", "tangent h", "tanh(x): Hyperbolic tangent function.") {
                    activeDescription = "tanh(x): Hyperbolic tangent of x."
                    onEvent(CalculatorEvent.InputString("tanh("))
                }
            )
            3 -> listOf(
                SciBtn("x²", "square", "x²: Raises x to the power of 2.") {
                    activeDescription = "x²: Calculates the square of a number."
                    onEvent(CalculatorEvent.InputString("^2"))
                },
                SciBtn("x³", "cube", "x³: Raises x to the power of 3.") {
                    activeDescription = "x³: Calculates the cube of a number."
                    onEvent(CalculatorEvent.InputString("^3"))
                },
                SciBtn("xʸ", "power", "x^y: Raises base x to exponent y.") {
                    activeDescription = "xʸ: Custom exponent power. Usage: base^exponent."
                    onEvent(CalculatorEvent.InputChar('^'))
                },
                SciBtn("√", "sqrt", "√(x): Square root function.") {
                    activeDescription = "√(x): Calculates the non-negative square root of x."
                    onEvent(CalculatorEvent.InputString("√("))
                },
                SciBtn("³√", "cbrt", "³√(x): Cube root function.") {
                    activeDescription = "³√(x): Calculates the cube root of x."
                    onEvent(CalculatorEvent.InputString("³√("))
                },
                SciBtn("1/x", "reciprocal", "1/x: Multiplicative inverse.") {
                    activeDescription = "1/x: Calculates reciprocal of a number."
                    onEvent(CalculatorEvent.InputString("1/("))
                }
            )
            4 -> listOf(
                SciBtn("log₁₀", "log", "log(x): Base-10 common logarithm.") {
                    activeDescription = "log(x): Base-10 logarithm of x."
                    onEvent(CalculatorEvent.InputString("log("))
                },
                SciBtn("ln", "ln", "ln(x): Base-e natural logarithm.") {
                    activeDescription = "ln(x): Natural logarithm (base e) of x."
                    onEvent(CalculatorEvent.InputString("ln("))
                },
                SciBtn("eˣ", "exp", "e^x: Euler's number raised to x.") {
                    activeDescription = "eˣ: Exponential function exp(x)."
                    onEvent(CalculatorEvent.InputString("exp("))
                },
                SciBtn("10ˣ", "10^x", "10^x: 10 raised to the power of x.") {
                    activeDescription = "10ˣ: Calculates 10 raised to power of x."
                    onEvent(CalculatorEvent.InputString("10^("))
                },
                SciBtn("EXP", "sci exp", "EXP: Scientific notation multiplier *10^(") {
                    activeDescription = "EXP: Appends scientific exponent multiplier *10^("
                    onEvent(CalculatorEvent.InputString("*10^("))
                }
            )
            5 -> listOf(
                SciBtn("nPr", "permut", "nPr: Number of permutations.") {
                    activeDescription = "nPr: Permutations of n items taken r at a time (e.g. 5p3)."
                    onEvent(CalculatorEvent.InputChar('p'))
                },
                SciBtn("nCr", "combin", "nCr: Number of combinations.") {
                    activeDescription = "nCr: Combinations of n items taken r at a time (e.g. 5c3)."
                    onEvent(CalculatorEvent.InputChar('c'))
                },
                SciBtn("MOD", "modulo", "MOD: Remainder after division.") {
                    activeDescription = "MOD: Modulo division (e.g. 10 mod 3 = 1)."
                    onEvent(CalculatorEvent.InputString("mod"))
                },
                SciBtn("ABS", "absolute", "abs(x): Absolute value.") {
                    activeDescription = "abs(x): Returns the absolute (positive) value of a number."
                    onEvent(CalculatorEvent.InputString("abs("))
                },
                SciBtn("!", "fact", "n!: Factorial of a number.") {
                    activeDescription = "n!: Factorial of non-negative integer n. Max n is 170."
                    onEvent(CalculatorEvent.InputChar('!'))
                },
                SciBtn("Rand", "random", "rand: Random value [0, 1).") {
                    activeDescription = "Rand: Generates a pseudo-random decimal between 0 and 1."
                    onEvent(CalculatorEvent.InputString("rand"))
                }
            )
            6 -> listOf(
                SciBtn("π", "pi", "π: Mathematical constant Pi.") {
                    activeDescription = "π: Pi ratio (~3.14159265)."
                    onEvent(CalculatorEvent.InputChar('π'))
                },
                SciBtn("e", "euler", "e: Euler's number constant.") {
                    activeDescription = "e: Euler's number (~2.71828182)."
                    onEvent(CalculatorEvent.InputChar('e'))
                }
            )
            else -> emptyList()
        }
    }

    val buttonSpacing = 8.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEachIndexed { index, name ->
                val isSelected = index == selectedCategoryIndex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedCategoryIndex = index }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = activeDescription,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp,
                    maxLines = 1,
                    lineHeight = 12.sp
                )
            }
        }

        if (isLandscape) {
            val chunkedButtons = buttons.chunked(4)
            chunkedButtons.forEach { rowButtons ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    rowButtons.forEach { sciBtn ->
                        ScientificButton(
                            symbol = sciBtn.symbol,
                            label = sciBtn.label,
                            tooltip = sciBtn.desc,
                            modifier = Modifier.weight(1f),
                            onClick = sciBtn.onClick
                        )
                    }
                    val remaining = 4 - rowButtons.size
                    if (remaining > 0) {
                        repeat(remaining) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (buttons.size > 4) {
                            Modifier.horizontalScroll(rememberScrollState())
                        } else {
                            Modifier
                        }
                    ),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                buttons.forEach { sciBtn ->
                    ScientificButton(
                        symbol = sciBtn.symbol,
                        label = sciBtn.label,
                        tooltip = sciBtn.desc,
                        modifier = Modifier.then(
                            if (buttons.size > 4) {
                                Modifier.width(80.dp)
                            } else {
                                Modifier.weight(1f)
                            }
                        ),
                        onClick = sciBtn.onClick
                    )
                }
            }
        }
    }
}
