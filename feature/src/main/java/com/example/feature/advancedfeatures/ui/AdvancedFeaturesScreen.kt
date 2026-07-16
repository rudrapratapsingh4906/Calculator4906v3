package com.example.feature.advancedfeatures.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class ConverterItem(
    val title: String,
    val icon: ImageVector,
    val isImplemented: Boolean = false,
    val isCurrency: Boolean = false,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFeaturesScreen(
    onDismiss: () -> Unit,
    onNavigateToUnitConverter: () -> Unit,
    onNavigateToPercentageCgpa: () -> Unit,
    onNavigateToEmiCalculator: () -> Unit,
    onNavigateToHealthCalculator: () -> Unit,
    onNavigateToCurrencyConverter: () -> Unit,
    onNavigateToDateTimeCalculator: () -> Unit,
    onNavigateToAgeCalculator: () -> Unit,
    onNavigateToConstants: () -> Unit,
    onNavigateToCameraMathSolver: () -> Unit,
    onNavigateToGraphPlotter: () -> Unit,
    onNavigateToMatrixCalculator: () -> Unit,
    onNavigateToEquationSolver: () -> Unit,
    onNavigateToCalculus: () -> Unit,
    onNavigateToComplexCalculator: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    orientationLock: Boolean,
    onOrientationLockChange: (Boolean) -> Unit
) {
    val converters = listOf(
        ConverterItem("Unit Converter", Icons.Default.SwapHoriz, true, false, "unit_converter"),
        ConverterItem("Percentage & CGPA", Icons.Default.Percent, true, false, "percentage_cgpa"),
        ConverterItem("EMI Calculator", Icons.Default.Calculate, true, false, "emi_calculator"),
        ConverterItem("Health & BMI", Icons.Default.Favorite, true, false, "health_calculator"),
        ConverterItem("Currency", Icons.Default.MonetizationOn, true, false, "currency_calculator"),
        ConverterItem("Date & Time", Icons.Default.DateRange, true, false, "datetime_calculator"),
        ConverterItem("Age Calculator", Icons.Default.Cake, true, false, "age_calculator"),
        ConverterItem("Scientific Constants", Icons.Default.Science, true, false, "scientific_constants"),
        ConverterItem("Camera Math Solver", Icons.Default.CameraAlt, true, false, "math_scanner"),
        ConverterItem("Graph Plotter", Icons.Default.ShowChart, true, false, "graph_plotter"),
        ConverterItem("Matrix Calculator", Icons.Default.Grid3x3, true, false, "matrix_calculator"),
        ConverterItem("Equation Solver", Icons.Default.Calculate, true, false, "equation_solver"),
        ConverterItem("Calculus", Icons.Default.Functions, true, false, "calculus"),
        ConverterItem("Complex Calculator", Icons.Default.Grid4x4, true, false, "complex_calculator"),
        ConverterItem("Statistics", Icons.Default.Equalizer, true, false, "statistics_calculator")
    )

    var showComingSoonDialog by remember { mutableStateOf<ConverterItem?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Advanced Features") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
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
                    .verticalScroll(rememberScrollState())
            ) {
                // Settings
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lock Portrait Orientation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = orientationLock,
                            onCheckedChange = onOrientationLockChange
                        )
                    }
                }

                Text(
                    text = "Converters & Tools",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[0],
                                onClick = onNavigateToUnitConverter,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[1],
                                onClick = onNavigateToPercentageCgpa,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[2],
                                onClick = onNavigateToEmiCalculator,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[3],
                                onClick = onNavigateToHealthCalculator,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[4],
                                onClick = onNavigateToCurrencyConverter,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[5],
                                onClick = onNavigateToDateTimeCalculator,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[6],
                                onClick = onNavigateToAgeCalculator,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[7],
                                onClick = onNavigateToConstants,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[8],
                                onClick = onNavigateToCameraMathSolver,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[9],
                                onClick = onNavigateToGraphPlotter,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[10],
                                onClick = onNavigateToMatrixCalculator,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[11],
                                onClick = onNavigateToEquationSolver,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[12],
                                onClick = onNavigateToCalculus,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[13],
                                onClick = onNavigateToComplexCalculator,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConverterCard(
                                item = converters[14],
                                onClick = onNavigateToStatistics,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    showComingSoonDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showComingSoonDialog = null },
            title = { Text(item.title) },
            text = {
                if (item.isCurrency) {
                    Text("Coming Soon\nNo online APIs are enabled yet.")
                } else {
                    Text("Coming Soon")
                }
            },
            confirmButton = {
                TextButton(onClick = { showComingSoonDialog = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ConverterCard(
    item: ConverterItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.title,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
