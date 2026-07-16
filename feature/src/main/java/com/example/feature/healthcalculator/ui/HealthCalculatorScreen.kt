package com.example.feature.healthcalculator.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
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
import com.example.feature.healthcalculator.ActivityLevel
import com.example.feature.healthcalculator.Gender
import com.example.feature.healthcalculator.HealthCalculatorEvent
import com.example.feature.healthcalculator.HealthCalculatorState
import com.example.feature.healthcalculator.HealthCalculatorViewModel
import com.example.feature.healthcalculator.UnitSystem
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCalculatorScreen(
    viewModel: HealthCalculatorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health & BMI Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("health_back_button")) {
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
                    text = { Text("BMI & Fat") },
                    icon = { Icon(Icons.Default.Accessibility, contentDescription = "BMI & Fat") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("BMR & Calories") },
                    icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "BMR & Calories") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Ideal Weight") },
                    icon = { Icon(Icons.Default.MonitorWeight, contentDescription = "Ideal Weight") }
                )
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        InputSection(state = state, onEvent = viewModel::onEvent, isLandscape = true)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutputSection(
                            tabIndex = selectedTab,
                            state = state,
                            onEvent = viewModel::onEvent,
                            onCopyResult = { text ->
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Copied results to clipboard", Toast.LENGTH_SHORT).show()
                                viewModel.onEvent(HealthCalculatorEvent.SaveToHistory)
                            },
                            onSaveHistory = {
                                viewModel.onEvent(HealthCalculatorEvent.SaveToHistory)
                                Toast.makeText(context, "Saved results to History", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InputSection(state = state, onEvent = viewModel::onEvent, isLandscape = false)
                    OutputSection(
                        tabIndex = selectedTab,
                        state = state,
                        onEvent = viewModel::onEvent,
                        onCopyResult = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Copied results to clipboard", Toast.LENGTH_SHORT).show()
                            viewModel.onEvent(HealthCalculatorEvent.SaveToHistory)
                        },
                        onSaveHistory = {
                            viewModel.onEvent(HealthCalculatorEvent.SaveToHistory)
                            Toast.makeText(context, "Saved results to History", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputSection(
    state: HealthCalculatorState,
    onEvent: (HealthCalculatorEvent) -> Unit,
    isLandscape: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Personal Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Gender selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Gender:", fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.gender == Gender.MALE,
                        onClick = { onEvent(HealthCalculatorEvent.GenderChanged(Gender.MALE)) },
                        label = { Text("Male") },
                        leadingIcon = {
                            if (state.gender == Gender.MALE) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.testTag("health_gender_male")
                    )
                    FilterChip(
                        selected = state.gender == Gender.FEMALE,
                        onClick = { onEvent(HealthCalculatorEvent.GenderChanged(Gender.FEMALE)) },
                        label = { Text("Female") },
                        leadingIcon = {
                            if (state.gender == Gender.FEMALE) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.testTag("health_gender_female")
                    )
                }
            }

            // Unit System
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("System:", fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.unitSystem == UnitSystem.METRIC,
                        onClick = { onEvent(HealthCalculatorEvent.UnitSystemChanged(UnitSystem.METRIC)) },
                        label = { Text("Metric") },
                        modifier = Modifier.testTag("health_system_metric")
                    )
                    FilterChip(
                        selected = state.unitSystem == UnitSystem.IMPERIAL,
                        onClick = { onEvent(HealthCalculatorEvent.UnitSystemChanged(UnitSystem.IMPERIAL)) },
                        label = { Text("Imperial") },
                        modifier = Modifier.testTag("health_system_imperial")
                    )
                }
            }

            // Age OutlinedTextField
            OutlinedTextField(
                value = state.age,
                onValueChange = { onEvent(HealthCalculatorEvent.AgeChanged(it)) },
                label = { Text("Age (Years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("health_input_age")
            )

            // Height OutlinedTextFields based on system
            if (state.unitSystem == UnitSystem.METRIC) {
                OutlinedTextField(
                    value = state.heightCm,
                    onValueChange = { onEvent(HealthCalculatorEvent.HeightCmChanged(it)) },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_height_cm")
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = state.heightFt,
                        onValueChange = { onEvent(HealthCalculatorEvent.HeightFtChanged(it)) },
                        label = { Text("Ft") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("health_input_height_ft")
                    )
                    OutlinedTextField(
                        value = state.heightIn,
                        onValueChange = { onEvent(HealthCalculatorEvent.HeightInChanged(it)) },
                        label = { Text("In") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("health_input_height_in")
                    )
                }
            }

            // Weight OutlinedTextFields based on system
            if (state.unitSystem == UnitSystem.METRIC) {
                OutlinedTextField(
                    value = state.weightKg,
                    onValueChange = { onEvent(HealthCalculatorEvent.WeightKgChanged(it)) },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_weight_kg")
                )
            } else {
                OutlinedTextField(
                    value = state.weightLbs,
                    onValueChange = { onEvent(HealthCalculatorEvent.WeightLbsChanged(it)) },
                    label = { Text("Weight (lbs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_weight_lbs")
                )
            }

            // Waist (Optional) OutlinedTextFields based on system
            if (state.unitSystem == UnitSystem.METRIC) {
                OutlinedTextField(
                    value = state.waistCm,
                    onValueChange = { onEvent(HealthCalculatorEvent.WaistCmChanged(it)) },
                    label = { Text("Waist Circumference (cm, optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_waist_cm")
                )
            } else {
                OutlinedTextField(
                    value = state.waistIn,
                    onValueChange = { onEvent(HealthCalculatorEvent.WaistInChanged(it)) },
                    label = { Text("Waist Circumference (in, optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_input_waist_in")
                )
            }
        }
    }
}

@Composable
fun OutputSection(
    tabIndex: Int,
    state: HealthCalculatorState,
    onEvent: (HealthCalculatorEvent) -> Unit,
    onCopyResult: (String) -> Unit,
    onSaveHistory: () -> Unit
) {
    if (state.error != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
        return
    }

    if (state.bmi <= 0.0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Awaiting Profile Inputs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Please enter your age, height, and weight to view real-time health metrics.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (tabIndex) {
            0 -> BmiTabContent(state = state)
            1 -> BmrTabContent(state = state, onEvent = onEvent)
            2 -> IdealWeightTabContent(state = state)
        }

        // Shared action panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onEvent(HealthCalculatorEvent.ClearAll) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("health_clear_button")
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear")
            }

            IconButton(
                onClick = {
                    val wUnit = if (state.unitSystem == UnitSystem.METRIC) "kg" else "lbs"
                    val wVal = if (state.unitSystem == UnitSystem.METRIC) state.weightKg else state.weightLbs
                    val hVal = if (state.unitSystem == UnitSystem.METRIC) "${state.heightCm} cm" else "${state.heightFt} ft ${state.heightIn} in"
                    
                    val text = "Health & Body Summary:\n" +
                            "Profile: ${state.gender.displayName}, ${state.age} years old\n" +
                            "Height: $hVal, Weight: $wVal $wUnit\n" +
                            "BMI: ${formatVal(state.bmi, 1)} (${state.bmiClassification})\n" +
                            "BMR (Mifflin): ${formatVal(state.bmrMifflin, 0)} kcal/day\n" +
                            "Maintenance Calories: ${formatVal(state.maintenanceCalories, 0)} kcal/day\n" +
                            "Body Fat Estimate (BMI): ${formatVal(state.bodyFatBmi, 1)}%" +
                            if (state.isYmcaAvailable) "\nBody Fat Estimate (YMCA): ${formatVal(state.bodyFatYmca, 1)}%" else ""
                    onCopyResult(text)
                },
                modifier = Modifier.testTag("health_copy_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Summary")
            }

            IconButton(
                onClick = onSaveHistory,
                modifier = Modifier.testTag("health_save_history_button")
            ) {
                Icon(Icons.Default.History, contentDescription = "Save to History")
            }
        }
    }
}

@Composable
fun BmiTabContent(state: HealthCalculatorState) {
    val bmiColor = getBmiColor(state.bmiClassification)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Body Mass Index (BMI)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Text(
                text = formatVal(state.bmi, 1),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("health_result_bmi")
            )

            Surface(
                color = bmiColor,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = state.bmiClassification,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("health_result_bmi_classification")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // BMI Slider indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                val progress = Math.max(0.0, Math.min(1.0, (state.bmi - 15.0) / 25.0)).toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = bmiColor,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("15 (Underweight)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    Text("25 (Normal)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    Text("40 (Severe)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                }
            }
        }
    }

    // Body fat estimation Card
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Body Fat Percentage Estimate",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("BMI-Based Formula", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatVal(state.bodyFatBmi, 1)}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("health_result_body_fat_bmi")
                    )
                    Text("Deurenberg Formula", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }

                if (state.isYmcaAvailable) {
                    VerticalDivider(modifier = Modifier.height(50.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Waist-Based Formula", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${formatVal(state.bodyFatYmca, 1)}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("health_result_body_fat_ymca")
                        )
                        Text("YMCA Equation", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }

            if (!state.isYmcaAvailable) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tip: Enter your waist circumference to unlock the waist-based body fat estimation.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmrTabContent(
    state: HealthCalculatorState,
    onEvent: (HealthCalculatorEvent) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Basal Metabolic Rate (BMR)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mifflin-St Jeor", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatVal(state.bmrMifflin, 0)} kcal",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("health_result_bmr_mifflin")
                    )
                    Text("Standard modern formula", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }

                VerticalDivider(modifier = Modifier.height(50.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Harris-Benedict", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatVal(state.bmrHarris, 0)} kcal",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("health_result_bmr_harris")
                    )
                    Text("Revised original formula", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        }
    }

    // Activity level dropdown & Calorie distribution
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "TDEE & Daily Energy Needs",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text("Select Activity Level for energy expenditure details:", fontSize = 12.sp, fontWeight = FontWeight.Medium)

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActivityLevel.values().forEach { level ->
                    val isSelected = state.activityLevel == level
                    val cardBgColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    val cardBorder = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

                    Card(
                        onClick = { onEvent(HealthCalculatorEvent.ActivityLevelChanged(level)) },
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("health_activity_level_${level.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(level.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(level.detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            }
                            Text(
                                "${formatVal(state.bmrMifflin * level.multiplier, 0)} kcal",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(4.dp))

            // Energy distributions: Maintenance, Weight Loss, Weight Gain based on active level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Weight Loss", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${formatVal(state.weightLossCalories, 0)} kcal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("health_result_weight_loss")
                    )
                    Text("Mild deficit (-500)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.5f)) {
                    Text("Maintenance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${formatVal(state.maintenanceCalories, 0)} kcal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("health_result_maintenance")
                    )
                    Text("TDEE base needs", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Weight Gain", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${formatVal(state.weightGainCalories, 0)} kcal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.testTag("health_result_weight_gain")
                    )
                    Text("Mild surplus (+500)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun IdealWeightTabContent(state: HealthCalculatorState) {
    val weightUnit = if (state.unitSystem == UnitSystem.METRIC) "kg" else "lbs"

    // Healthy weight range (BMI 18.5 - 24.9)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Healthy BMI Weight Range",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )

            Text(
                text = "${formatVal(state.idealWeightMin, 1)} - ${formatVal(state.idealWeightMax, 1)} $weightUnit",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.testTag("health_result_healthy_range")
            )

            Text(
                text = "Derived from standard WHO normal body weight bounds.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }

    // Mathematical formula comparisons
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Clinical Weight Formulas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "These formulas calculate the ideal weight for adults taller than 5 feet (152 cm).",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            if (state.idealWeightDevine <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clinical formulas require a height greater than 5 feet (152.4 cm).",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormulaRow(name = "Devine Formula", value = state.idealWeightDevine, unit = weightUnit, tag = "health_result_devine")
                    FormulaRow(name = "Robinson Formula", value = state.idealWeightRobinson, unit = weightUnit, tag = "health_result_robinson")
                    FormulaRow(name = "Miller Formula", value = state.idealWeightMiller, unit = weightUnit, tag = "health_result_miller")
                    FormulaRow(name = "Hamwi Formula", value = state.idealWeightHamwi, unit = weightUnit, tag = "health_result_hamwi")
                }
            }
        }
    }
}

@Composable
fun FormulaRow(
    name: String,
    value: Double,
    unit: String,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        Text(
            "${formatVal(value, 1)} $unit",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(tag)
        )
    }
}

private fun formatVal(value: Double, decimals: Int): String {
    if (value.isNaN() || value.isInfinite() || value <= 0.0) return "0.0"
    val bd = BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP)
    return if (decimals == 0) {
        String.format("%,d", bd.toInt())
    } else {
        String.format("%,.${decimals}f", bd.toDouble())
    }
}

fun getBmiColor(classification: String): Color {
    return when (classification) {
        "Underweight" -> Color(0xFF3F51B5) // Indigo
        "Normal" -> Color(0xFF4CAF50) // Green
        "Overweight" -> Color(0xFFFFC107) // Amber
        "Obesity Class I" -> Color(0xFFFF9800) // Orange
        "Obesity Class II" -> Color(0xFFFF5722) // Deep Orange
        "Obesity Class III" -> Color(0xFFF44336) // Red
        else -> Color.Gray
    }
}
