package com.example.feature.percentagecgpa.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.percentagecgpa.PercentageCgpaEvent
import com.example.feature.percentagecgpa.PercentageCgpaViewModel
import com.example.feature.percentagecgpa.PercentageToolType
import com.example.feature.percentagecgpa.Semester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PercentageCgpaScreen(
    viewModel: PercentageCgpaViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    var selectedTab by remember { mutableStateOf(0) }
    var showAddSemesterDialog by remember { mutableStateOf(false) }
    var editingSemester by remember { mutableStateOf<Semester?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Percentage & CGPA") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Percentage") },
                    icon = { Icon(Icons.Default.Percent, contentDescription = "Percentage") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("CGPA / GPA") },
                    icon = { Icon(Icons.Default.School, contentDescription = "CGPA") }
                )
            }

            if (selectedTab == 0) {
                PercentageTabContent(
                    state = state,
                    onEvent = viewModel::onEvent,
                    isLandscape = isLandscape,
                    onCopyResult = { resultText ->
                        clipboardManager.setText(AnnotatedString(resultText))
                        Toast.makeText(context, "Copied result: $resultText", Toast.LENGTH_SHORT).show()
                        viewModel.savePercentageToHistory()
                    },
                    onSaveHistory = {
                        viewModel.savePercentageToHistory()
                        Toast.makeText(context, "Saved to History", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                CgpaTabContent(
                    state = state,
                    onEvent = viewModel::onEvent,
                    isLandscape = isLandscape,
                    onAddSemesterClick = {
                        editingSemester = null
                        showAddSemesterDialog = true
                    },
                    onEditSemesterClick = { semester ->
                        editingSemester = semester
                        showAddSemesterDialog = true
                    },
                    onSaveHistory = {
                        viewModel.saveCgpaToHistory()
                        Toast.makeText(context, "Saved CGPA calculation to History", Toast.LENGTH_SHORT).show()
                    },
                    onCopyResult = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    if (showAddSemesterDialog) {
        SemesterInputDialog(
            semester = editingSemester,
            defaultName = "Semester ${state.semesters.size + 1}",
            isCreditBased = state.isCreditBased,
            onDismiss = { showAddSemesterDialog = false },
            onConfirm = { name, gpa, credits ->
                if (editingSemester != null) {
                    viewModel.onEvent(PercentageCgpaEvent.EditSemester(editingSemester!!.id, name, gpa, credits))
                } else {
                    viewModel.onEvent(PercentageCgpaEvent.AddSemester(name, gpa, credits))
                }
                showAddSemesterDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PercentageTabContent(
    state: com.example.feature.percentagecgpa.PercentageCgpaState,
    onEvent: (PercentageCgpaEvent) -> Unit,
    isLandscape: Boolean,
    onCopyResult: (String) -> Unit,
    onSaveHistory: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal scroll list of tools
        Text(
            text = "Select Tool",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PercentageToolType.values()) { tool ->
                FilterChip(
                    selected = state.activeTool == tool,
                    onClick = { onEvent(PercentageCgpaEvent.SelectTool(tool)) },
                    label = { Text(tool.displayName) },
                    modifier = Modifier.testTag("tool_chip_${tool.name.lowercase()}")
                )
            }
        }

        val (labelA, labelB) = when (state.activeTool) {
            PercentageToolType.PERCENTAGE_OF_NUMBER -> "Percentage (%)" to "Of Number"
            PercentageToolType.X_IS_WHAT_P_OF_Y -> "Value X" to "Value Y"
            PercentageToolType.PERCENTAGE_INCREASE -> "Initial Value (From)" to "New Value (To)"
            PercentageToolType.PERCENTAGE_DECREASE -> "Initial Value (From)" to "New Value (To)"
            PercentageToolType.PERCENTAGE_DIFFERENCE -> "Value X" to "Value Y"
            PercentageToolType.ADD_PERCENTAGE -> "Original Value" to "Percentage to Add (%)"
            PercentageToolType.SUBTRACT_PERCENTAGE -> "Original Value" to "Percentage to Subtract (%)"
            PercentageToolType.DISCOUNT -> "Original Price" to "Discount Percentage (%)"
            PercentageToolType.GST_TAX -> "Amount / Price" to "GST / Tax Rate (%)"
            PercentageToolType.MARKS -> "Obtained Marks" to "Total Marks"
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = state.activeTool.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (state.activeTool == PercentageToolType.GST_TAX) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("GST Calculation:", fontSize = 14.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !state.isGstInclusive,
                                onClick = { onEvent(PercentageCgpaEvent.GstInclusiveChanged(false)) },
                                label = { Text("Add Tax (+)") }
                            )
                            FilterChip(
                                selected = state.isGstInclusive,
                                onClick = { onEvent(PercentageCgpaEvent.GstInclusiveChanged(true)) },
                                label = { Text("Remove Tax (-)") }
                            )
                        }
                    }
                }

                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = state.inputA,
                            onValueChange = { onEvent(PercentageCgpaEvent.InputAChanged(it)) },
                            label = { Text(labelA) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("percentage_input_a")
                        )
                        OutlinedTextField(
                            value = state.inputB,
                            onValueChange = { onEvent(PercentageCgpaEvent.InputBChanged(it)) },
                            label = { Text(labelB) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("percentage_input_b")
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = state.inputA,
                        onValueChange = { onEvent(PercentageCgpaEvent.InputAChanged(it)) },
                        label = { Text(labelA) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("percentage_input_a")
                    )
                    OutlinedTextField(
                        value = state.inputB,
                        onValueChange = { onEvent(PercentageCgpaEvent.InputBChanged(it)) },
                        label = { Text(labelB) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("percentage_input_b")
                    )
                }

                if (state.percentageError != null) {
                    Text(
                        text = state.percentageError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedVisibility(
                    visible = state.percentageResult.isNotEmpty() && state.percentageError == null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.percentageResultLabel,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.percentageResult,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("percentage_result_text")
                            )
                            if (state.percentageSecondaryResult.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.percentageSecondaryResult,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onEvent(PercentageCgpaEvent.ClearPercentageInputs) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }

                    if (state.percentageResult.isNotEmpty() && state.percentageError == null) {
                        IconButton(
                            onClick = { onCopyResult(state.percentageResult) },
                            modifier = Modifier.testTag("copy_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }

                        IconButton(
                            onClick = onSaveHistory,
                            modifier = Modifier.testTag("save_history_button")
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CgpaTabContent(
    state: com.example.feature.percentagecgpa.PercentageCgpaState,
    onEvent: (PercentageCgpaEvent) -> Unit,
    isLandscape: Boolean,
    onAddSemesterClick: () -> Unit,
    onEditSemesterClick: (Semester) -> Unit,
    onSaveHistory: () -> Unit,
    onCopyResult: (String) -> Unit
) {
    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Side: Results and Configurations
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CgpaConfigurationPanel(state = state, onEvent = onEvent)
                CgpaSummaryCard(state = state, onSaveHistory = onSaveHistory, onCopyResult = onCopyResult)
            }

            // Right Side: Semester list
            Column(
                modifier = Modifier.weight(0.55f)
            ) {
                CgpaSemesterListHeader(state = state, onEvent = onEvent, onAddSemesterClick = onAddSemesterClick)
                CgpaSemesterList(state = state, onEvent = onEvent, onEditSemesterClick = onEditSemesterClick)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CgpaConfigurationPanel(state = state, onEvent = onEvent)
                }

                item {
                    CgpaSummaryCard(state = state, onSaveHistory = onSaveHistory, onCopyResult = onCopyResult)
                }

                item {
                    CgpaSemesterListHeader(state = state, onEvent = onEvent, onAddSemesterClick = onAddSemesterClick)
                }

                if (state.semesters.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "No semesters added yet. Click '+' to add a semester.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(state.semesters) { semester ->
                        SemesterItemRow(
                            semester = semester,
                            isCreditBased = state.isCreditBased,
                            onEdit = { onEditSemesterClick(semester) },
                            onDelete = { onEvent(PercentageCgpaEvent.DeleteSemester(semester.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CgpaConfigurationPanel(
    state: com.example.feature.percentagecgpa.PercentageCgpaState,
    onEvent: (PercentageCgpaEvent) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Credit-Based GPA", fontWeight = FontWeight.Medium)
                    Text("Weight semesters by credits", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = state.isCreditBased,
                    onCheckedChange = { onEvent(PercentageCgpaEvent.CreditBasedToggled(it)) },
                    modifier = Modifier.testTag("credit_based_switch")
                )
            }

            OutlinedTextField(
                value = state.cgpaFormulaMultiplier,
                onValueChange = { onEvent(PercentageCgpaEvent.FormulaMultiplierChanged(it)) },
                label = { Text("Formula Multiplier (CGPA to %)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("multiplier_input")
            )
        }
    }
}

@Composable
fun CgpaSummaryCard(
    state: com.example.feature.percentagecgpa.PercentageCgpaState,
    onSaveHistory: () -> Unit,
    onCopyResult: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Cumulative Results",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CGPA", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    val formattedCgpa = if (state.calculatedCgpa.isNaN() || state.calculatedCgpa.isInfinite()) "0.00" else String.format("%.2f", state.calculatedCgpa)
                    Text(
                        text = formattedCgpa,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("calculated_cgpa_text")
                    )
                }

                VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Percentage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    val formattedPercent = if (state.calculatedPercentage.isNaN() || state.calculatedPercentage.isInfinite()) "0.00" else String.format("%.2f", state.calculatedPercentage)
                    Text(
                        text = "$formattedPercent%",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("calculated_percentage_text")
                    )
                }
            }

            if (state.isCreditBased && state.totalCredits > 0.0) {
                val formattedCredits = String.format("%.1f", state.totalCredits)
                Text(
                    text = "Total Credits: $formattedCredits",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            if (state.semesters.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val formattedCgpa = String.format("%.2f", state.calculatedCgpa)
                            val formattedPercent = String.format("%.2f", state.calculatedPercentage)
                            onCopyResult("CGPA: $formattedCgpa, Percentage: $formattedPercent%")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer, contentColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onSaveHistory,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer, contentColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.History, contentDescription = "Save History", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CgpaSemesterListHeader(
    state: com.example.feature.percentagecgpa.PercentageCgpaState,
    onEvent: (PercentageCgpaEvent) -> Unit,
    onAddSemesterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Semesters (${state.semesters.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.semesters.isNotEmpty()) {
                TextButton(
                    onClick = { onEvent(PercentageCgpaEvent.ResetCgpa) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("reset_cgpa_button")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset All", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset All")
                }
            }

            Button(
                onClick = onAddSemesterClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_semester_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Semester", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CgpaSemesterList(
    state: com.example.feature.percentagecgpa.PercentageCgpaState,
    onEvent: (PercentageCgpaEvent) -> Unit,
    onEditSemesterClick: (Semester) -> Unit
) {
    if (state.semesters.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Text(
                text = "No semesters added yet. Click '+' to add a semester.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(state.semesters) { semester ->
                SemesterItemRow(
                    semester = semester,
                    isCreditBased = state.isCreditBased,
                    onEdit = { onEditSemesterClick(semester) },
                    onDelete = { onEvent(PercentageCgpaEvent.DeleteSemester(semester.id)) }
                )
            }
        }
    }
}

@Composable
fun SemesterItemRow(
    semester: Semester,
    isCreditBased: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(semester.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                val desc = if (isCreditBased) {
                    "GPA: ${semester.gpa} • Credits: ${semester.credits}"
                } else {
                    "GPA: ${semester.gpa}"
                }
                Text(desc, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun SemesterInputDialog(
    semester: Semester?,
    defaultName: String,
    isCreditBased: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, gpa: Double, credits: Double) -> Unit
) {
    var name by remember { mutableStateOf(semester?.name ?: defaultName) }
    var gpaStr by remember { mutableStateOf(semester?.gpa?.toString() ?: "") }
    var creditsStr by remember { mutableStateOf(semester?.credits?.toString() ?: "1.0") }

    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (semester != null) "Edit Semester" else "Add Semester") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Semester Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_semester_name")
                )

                OutlinedTextField(
                    value = gpaStr,
                    onValueChange = { gpaStr = it },
                    label = { Text("GPA / SGPA") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_semester_gpa")
                )

                if (isCreditBased) {
                    OutlinedTextField(
                        value = creditsStr,
                        onValueChange = { creditsStr = it },
                        label = { Text("Credits") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_semester_credits")
                    )
                }

                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val gpaVal = gpaStr.toDoubleOrNull()
                    val creditsVal = if (isCreditBased) creditsStr.toDoubleOrNull() else 1.0

                    if (name.trim().isEmpty()) {
                        errorText = "Please enter semester name"
                        return@Button
                    }
                    if (gpaVal == null || gpaVal < 0.0) {
                        errorText = "Please enter a valid positive GPA"
                        return@Button
                    }
                    if (isCreditBased && (creditsVal == null || creditsVal < 0.0)) {
                        errorText = "Please enter valid positive credits"
                        return@Button
                    }

                    onConfirm(name, gpaVal, creditsVal ?: 1.0)
                },
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text(if (semester != null) "Save" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
