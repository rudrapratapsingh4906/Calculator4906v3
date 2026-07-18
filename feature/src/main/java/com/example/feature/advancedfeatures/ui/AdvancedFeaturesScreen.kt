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
    onBack: () -> Unit,
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
        ConverterItem("Calculus", Icons.Default.Functions, true, false, "calculus"),
        ConverterItem("Complex Calculator", Icons.Default.Grid4x4, true, false, "complex_calculator"),
        ConverterItem("Statistics", Icons.Default.Equalizer, true, false, "statistics_calculator")
    )

    var showComingSoonDialog by remember { mutableStateOf<ConverterItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Features") },
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
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lock Orientation", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = orientationLock, onCheckedChange = onOrientationLockChange)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(converters) { converter ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                if (converter.isImplemented) {
                                    when (converter.route) {
                                        "unit_converter" -> onNavigateToUnitConverter()
                                        "percentage_cgpa" -> onNavigateToPercentageCgpa()
                                        "emi_calculator" -> onNavigateToEmiCalculator()
                                        "health_calculator" -> onNavigateToHealthCalculator()
                                        "currency_calculator" -> onNavigateToCurrencyConverter()
                                        "datetime_calculator" -> onNavigateToDateTimeCalculator()
                                        "age_calculator" -> onNavigateToAgeCalculator()
                                        "scientific_constants" -> onNavigateToConstants()
                                        "math_scanner" -> onNavigateToCameraMathSolver()
                                        "graph_plotter" -> onNavigateToGraphPlotter()
                                        "matrix_calculator" -> onNavigateToMatrixCalculator()
                                        "calculus" -> onNavigateToCalculus()
                                        "complex_calculator" -> onNavigateToComplexCalculator()
                                        "statistics_calculator" -> onNavigateToStatistics()
                                    }
                                } else {
                                    showComingSoonDialog = converter
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(converter.icon, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(converter.title, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
    
    if (showComingSoonDialog != null) {
        AlertDialog(
            onDismissRequest = { showComingSoonDialog = null },
            title = { Text("Coming Soon") },
            text = { Text("${showComingSoonDialog?.title} is not yet implemented.") },
            confirmButton = {
                TextButton(onClick = { showComingSoonDialog = null }) {
                    Text("OK")
                }
            }
        )
    }
}
