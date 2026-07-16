package com.example.feature.agecalculator.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.agecalculator.AgeCalculatorEvent
import com.example.feature.agecalculator.AgeCalculatorViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
    viewModel: AgeCalculatorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val showDobPicker = remember { mutableStateOf(false) }
    val showCurrentPicker = remember { mutableStateOf(false) }

    if (showDobPicker.value) {
        val calendar = state.dob ?: Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDob = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                viewModel.onEvent(AgeCalculatorEvent.SetDob(newDob))
                showDobPicker.value = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDobPicker.value = false }
            show()
        }
    }

    if (showCurrentPicker.value) {
        val calendar = state.currentDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCurrent = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                viewModel.onEvent(AgeCalculatorEvent.SetCurrentDate(newCurrent))
                showCurrentPicker.value = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showCurrentPicker.value = false }
            show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Age Calculator") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DateSelectionBox(
                    title = "Date of Birth",
                    dateText = state.dob?.let { dateFormat.format(it.time) } ?: "Select Date",
                    onClick = { showDobPicker.value = true },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                DateSelectionBox(
                    title = "Current Date",
                    dateText = dateFormat.format(state.currentDate.time),
                    onClick = { showCurrentPicker.value = true },
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
            }

            state.result?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Age", fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            "${result.years} Years, ${result.months} Months, ${result.days} Days",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Next Birthday", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${result.nextBirthdayMonths} Months, ${result.nextBirthdayDays} Days")
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Years:")
                            Text("${result.years}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Months:")
                            Text("${result.totalMonths}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Weeks:")
                            Text("${result.totalWeeks}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Days:")
                            Text("${result.totalDays}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Hours:")
                            Text("${result.totalHours}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Minutes:")
                            Text("${result.totalMinutes}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionBox(
    title: String,
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = dateText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
