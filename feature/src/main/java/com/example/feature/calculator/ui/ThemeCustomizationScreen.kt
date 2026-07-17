package com.example.feature.calculator.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.feature.calculator.CalculatorEvent
import com.example.feature.calculator.CalculatorState

@Composable
fun ThemeCustomizationScreen(
    state: CalculatorState,
    onEvent: (CalculatorEvent) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = listOf("Default", "Ocean", "Forest", "AMOLED Black", "Blue", "Green", "Purple", "Orange", "Red", "Teal")
    val themeModes = listOf("Light", "Dark", "System")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onEvent(CalculatorEvent.SetBackgroundImageUri(uri.toString()))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Theme Customization", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Theme Mode", style = MaterialTheme.typography.titleMedium)
        Row {
            themeModes.forEach { mode ->
                FilterChip(
                    selected = state.themeMode == mode,
                    onClick = { onEvent(CalculatorEvent.SetThemeMode(mode)) },
                    label = { Text(mode) }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Themes", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(themes) { themeName ->
                Card(
                    modifier = Modifier
                        .clickable { onEvent(CalculatorEvent.SetTheme(themeName)) }
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.theme == themeName) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(themeName, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Text("Custom Background", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("select_background_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Select Background Image"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image")
                        }

                        if (state.backgroundImageUri != null) {
                            OutlinedButton(
                                onClick = { onEvent(CalculatorEvent.SetBackgroundImageUri(null)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("remove_background_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Background"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Remove Background")
                            }
                        }
                    }
                }

                if (state.backgroundImageUri != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Opacity: ${(state.backgroundOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = state.backgroundOpacity,
                        onValueChange = { onEvent(CalculatorEvent.SetBackgroundOpacity(it)) },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("background_opacity_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Close")
        }
    }
}
