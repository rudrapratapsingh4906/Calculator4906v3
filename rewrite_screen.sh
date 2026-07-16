cat << 'INNER_EOF' > feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt
package com.example.feature.calculator.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.calculator.CalculatorEvent
import com.example.feature.calculator.CalculatorState
import com.example.feature.calculator.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    var showMenu by remember { mutableStateOf(false) }
    
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAdvancedDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Settings") },
            text = { Text("Basic application settings.") },
            confirmButton = { TextButton(onClick = { showSettingsDialog = false }) { Text("OK") } }
        )
    }

    if (showAdvancedDialog) {
        AlertDialog(
            onDismissRequest = { showAdvancedDialog = false },
            title = { Text("Advanced Features") },
            text = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lock Portrait Orientation")
                    Switch(
                        checked = state.orientationLock,
                        onCheckedChange = { viewModel.onEvent(CalculatorEvent.SetOrientationLock(it)) }
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showAdvancedDialog = false }) { Text("OK") } }
        )
    }

    if (showThemeDialog) {
        val themes = listOf("Default", "Ocean", "Forest")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme Customization") },
            text = {
                Column {
                    themes.forEach { themeName ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (themeName == state.theme),
                                    onClick = { viewModel.onEvent(CalculatorEvent.SetTheme(themeName)) }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (themeName == state.theme),
                                onClick = { viewModel.onEvent(CalculatorEvent.SetTheme(themeName)) }
                            )
                            Text(text = themeName, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("OK") } }
        )
    }

    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = { Text("Voice Calculator") },
            text = { Text("Coming soon!") },
            confirmButton = { TextButton(onClick = { showVoiceDialog = false }) { Text("OK") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Calculator") },
                actions = {
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
                            text = { Text("Voice Calculator") },
                            onClick = { showMenu = false; showVoiceDialog = true }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (state.showHistory) {
                HistoryScreen(
                    history = state.history,
                    onEvent = viewModel::onEvent
                )
            } else if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorDisplay(
                        state = state,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    Column(
                        modifier = Modifier.weight(0.2f).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryClear) }) { Text("MC") }
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryRecall) }) { Text("MR") }
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryAdd) }) { Text("M+") }
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemorySubtract) }) { Text("M-") }
                        IconButton(onClick = { viewModel.onEvent(CalculatorEvent.ToggleHistory) }) {
                            Icon(Icons.Default.Menu, contentDescription = "History")
                        }
                    }
                    ScientificButtonGrid(
                        onEvent = viewModel::onEvent,
                        isDegreeMode = state.isDegreeMode,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        isLandscape = true
                    )
                    CalculatorButtonGrid(
                        onEvent = viewModel::onEvent,
                        modifier = Modifier
                            .weight(1f)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryClear) }) { Text("MC") }
                            TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryRecall) }) { Text("MR") }
                            TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryAdd) }) { Text("M+") }
                            TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemorySubtract) }) { Text("M-") }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.onEvent(CalculatorEvent.ToggleHistory) }) {
                            Icon(Icons.Default.Menu, contentDescription = "History")
                        }
                        TextButton(
                            onClick = { showScientific = !showScientific }
                        ) {
                            Text(if (showScientific) "Basic" else "Scientific")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (showScientific) {
                        ScientificButtonGrid(
                            onEvent = viewModel::onEvent,
                            isDegreeMode = state.isDegreeMode,
                            modifier = Modifier.fillMaxWidth(),
                            isLandscape = false
                        )
                    } else {
                        CalculatorButtonGrid(
                            onEvent = viewModel::onEvent,
                            modifier = Modifier.fillMaxWidth(),
                            isLandscape = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorDisplay(
    state: CalculatorState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Text(
            text = state.currentExpression,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 32.sp,
            textAlign = TextAlign.End,
            maxLines = 2,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.result.ifEmpty { "0" },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            maxLines = 1
        )
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
    
    val baseButtonModifier = Modifier.fillMaxHeight()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "AC",
                color = secondaryContainer,
                textColor = onSecondaryContainer,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.Clear) }
            )
            CalculatorButton(
                symbol = "DEL",
                color = secondaryContainer,
                textColor = onSecondaryContainer,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.DeleteLast) }
            )
            CalculatorButton(
                symbol = "%",
                color = secondaryContainer,
                textColor = onSecondaryContainer,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('%')) }
            )
            CalculatorButton(
                symbol = "÷",
                color = tertiaryContainer,
                textColor = onTertiaryContainer,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('/')) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "7",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('7')) }
            )
            CalculatorButton(
                symbol = "8",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('8')) }
            )
            CalculatorButton(
                symbol = "9",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('9')) }
            )
            CalculatorButton(
                symbol = "×",
                color = tertiaryContainer,
                textColor = onTertiaryContainer,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('*')) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "4",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('4')) }
            )
            CalculatorButton(
                symbol = "5",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('5')) }
            )
            CalculatorButton(
                symbol = "6",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('6')) }
            )
            CalculatorButton(
                symbol = "-",
                color = tertiaryContainer,
                textColor = onTertiaryContainer,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('-')) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "1",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('1')) }
            )
            CalculatorButton(
                symbol = "2",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('2')) }
            )
            CalculatorButton(
                symbol = "3",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('3')) }
            )
            CalculatorButton(
                symbol = "+",
                color = tertiaryContainer,
                textColor = onTertiaryContainer,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('+')) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "+/-",
                modifier = baseButtonModifier.weight(1f),
                onClick = { 
                    onEvent(CalculatorEvent.TogglePositiveNegative)
                }
            )
            CalculatorButton(
                symbol = "0",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('0')) }
            )
            CalculatorButton(
                symbol = ".",
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('.')) }
            )
            CalculatorButton(
                symbol = "=",
                color = primaryContainer,
                textColor = onPrimaryContainer,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.Calculate) }
            )
        }
    }
}

@Composable
fun ScientificButtonGrid(
    onEvent: (CalculatorEvent) -> Unit,
    isDegreeMode: Boolean,
    modifier: Modifier = Modifier,
    isLandscape: Boolean
) {
    val buttonSpacing = 8.dp
    val color = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColor = MaterialTheme.colorScheme.primaryContainer
    val activeTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    
    val baseButtonModifier = Modifier.fillMaxHeight()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = if (isDegreeMode) "DEG" else "RAD",
                color = activeColor,
                textColor = activeTextColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.ToggleAngleMode) }
            )
            CalculatorButton(
                symbol = "sin",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputString("sin(")) }
            )
            CalculatorButton(
                symbol = "cos",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputString("cos(")) }
            )
            CalculatorButton(
                symbol = "tan",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputString("tan(")) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "ln",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputString("ln(")) }
            )
            CalculatorButton(
                symbol = "log",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputString("log(")) }
            )
            CalculatorButton(
                symbol = "√",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputString("sqrt(")) }
            )
            CalculatorButton(
                symbol = "^",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('^')) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "π",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('π')) }
            )
            CalculatorButton(
                symbol = "e",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('e')) }
            )
            CalculatorButton(
                symbol = "(",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('(')) }
            )
            CalculatorButton(
                symbol = ")",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar(')')) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                symbol = "x²",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputString("^2")) }
            )
            CalculatorButton(
                symbol = "!",
                color = color, textColor = textColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.InputChar('!')) }
            )
            CalculatorButton(
                symbol = "DEL",
                color = activeColor,
                textColor = activeTextColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.DeleteLast) }
            )
            CalculatorButton(
                symbol = "=",
                color = activeColor,
                textColor = activeTextColor,
                modifier = baseButtonModifier.weight(1f),
                onClick = { onEvent(CalculatorEvent.Calculate) }
            )
        }
    }
}
INNER_EOF
