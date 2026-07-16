package com.example.feature.advancedfeatures.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0: Descriptive Statistics, 1: Linear Regression & Correlation

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics & Regression") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab-based Operation Selector
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        viewModel.clear()
                    },
                    text = { Text("Descriptive Stats", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        viewModel.clear()
                    },
                    text = { Text("Regression & Corr", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                // Descriptive Stats
                Text(
                    text = "Enter Dataset (comma, space or newline-separated numbers)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedTextField(
                    value = state.inputX,
                    onValueChange = viewModel::onInputXChange,
                    label = { Text("Dataset Numbers") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 10, 20.5, 30, 40, 50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 5
                )

                Button(
                    onClick = viewModel::calculateStatistics,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calculate Descriptive Statistics")
                }
            } else {
                // Linear Regression & Correlation
                Text(
                    text = "Enter X Dataset (comma, space or newline-separated numbers)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedTextField(
                    value = state.inputX,
                    onValueChange = viewModel::onInputXChange,
                    label = { Text("X Dataset") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 1, 2, 3, 4, 5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Enter Y Dataset (comma, space or newline-separated numbers)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedTextField(
                    value = state.inputY,
                    onValueChange = viewModel::onInputYChange,
                    label = { Text("Y Dataset") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 2, 4, 5, 4, 5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 3
                )

                Button(
                    onClick = viewModel::calculateRegression,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calculate Linear Regression & Correlation")
                }
            }

            if (state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Results Display Card
            val hasSummary = state.summary != null
            val hasRegression = state.regressionResult != null

            if (hasSummary || hasRegression) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Calculation Results",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Copy Result Button
                            IconButton(
                                onClick = {
                                    val copyText = if (hasSummary && state.summary != null) {
                                        val s = state.summary!!
                                        buildString {
                                            appendLine("=== Descriptive Statistics ===")
                                            appendLine("Count: ${s.count}")
                                            appendLine("Sum: ${s.sum ?: "N/A"}")
                                            appendLine("Mean: ${s.mean ?: "N/A"}")
                                            appendLine("Median: ${s.median ?: "N/A"}")
                                            appendLine("Mode: ${if (s.mode.isEmpty()) "None" else s.mode.joinToString(", ")}")
                                            appendLine("Minimum: ${s.minimum ?: "N/A"}")
                                            appendLine("Maximum: ${s.maximum ?: "N/A"}")
                                            appendLine("Range: ${s.range ?: "N/A"}")
                                            appendLine("Population Variance: ${s.populationVariance ?: "N/A"}")
                                            appendLine("Sample Variance: ${s.sampleVariance ?: "N/A"}")
                                            appendLine("Population Std Dev: ${s.populationStdDev ?: "N/A"}")
                                            appendLine("Sample Std Dev: ${s.sampleStdDev ?: "N/A"}")
                                        }
                                    } else if (hasRegression && state.regressionResult != null) {
                                        val r = state.regressionResult!!
                                        buildString {
                                            appendLine("=== Linear Regression & Correlation ===")
                                            appendLine("Slope (m): ${String.format("%.6f", r.slope)}")
                                            appendLine("Intercept (c): ${String.format("%.6f", r.intercept)}")
                                            appendLine("Equation: y = ${String.format("%.4f", r.slope)}x + ${String.format("%.4f", r.intercept)}")
                                            appendLine("Pearson r: ${String.format("%.6f", r.pearsonR)}")
                                            val strength = when {
                                                kotlin.math.abs(r.pearsonR) >= 0.8 -> "Strong"
                                                kotlin.math.abs(r.pearsonR) >= 0.5 -> "Moderate"
                                                kotlin.math.abs(r.pearsonR) >= 0.3 -> "Weak"
                                                else -> "None/Very Weak"
                                            }
                                            appendLine("Correlation Strength: $strength")
                                        }
                                    } else ""

                                    if (copyText.isNotEmpty()) {
                                        clipboardManager.setText(AnnotatedString(copyText))
                                        // Simple toast representation / feedback in state is enough or snackbar
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Results"
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        if (hasSummary && state.summary != null) {
                            val s = state.summary!!
                            ResultRow("Count", s.count.toString())
                            ResultRow("Sum", formatDouble(s.sum))
                            ResultRow("Mean", formatDouble(s.mean))
                            ResultRow("Median", formatDouble(s.median))
                            ResultRow("Mode", if (s.mode.isEmpty()) "None" else s.mode.joinToString(", "))
                            ResultRow("Min", formatDouble(s.minimum))
                            ResultRow("Max", formatDouble(s.maximum))
                            ResultRow("Range", formatDouble(s.range))
                            ResultRow("Population Var", formatDouble(s.populationVariance))
                            ResultRow("Sample Var", formatDouble(s.sampleVariance))
                            ResultRow("Population SD", formatDouble(s.populationStdDev))
                            ResultRow("Sample SD", formatDouble(s.sampleStdDev))
                        }

                        if (hasRegression && state.regressionResult != null) {
                            val r = state.regressionResult!!
                            ResultRow("Slope (m)", String.format("%.6f", r.slope))
                            ResultRow("Intercept (c)", String.format("%.6f", r.intercept))
                            ResultRow("Equation", "y = ${String.format("%.4f", r.slope)}x + ${String.format("%.4f", r.intercept)}")
                            ResultRow("Pearson r", String.format("%.6f", r.pearsonR))
                            val strength = when {
                                kotlin.math.abs(r.pearsonR) >= 0.8 -> "Strong"
                                kotlin.math.abs(r.pearsonR) >= 0.5 -> "Moderate"
                                kotlin.math.abs(r.pearsonR) >= 0.3 -> "Weak"
                                else -> "None/Very Weak"
                            }
                            ResultRow("Correlation", strength)
                        }
                    }
                }
            }

            Button(
                onClick = viewModel::clear,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear")
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun formatDouble(value: Double?): String {
    if (value == null) return "N/A"
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
    // Format to 6 decimal places but remove trailing zeroes if it is a whole number or can be shorter
    val formatted = String.format("%.6f", value)
    return if (formatted.contains(".")) {
        var end = formatted.length - 1
        while (end > 0 && formatted[end] == '0') {
            end--
        }
        if (formatted[end] == '.') {
            formatted.substring(0, end)
        } else {
            formatted.substring(0, end + 1)
        }
    } else {
        formatted
    }
}
