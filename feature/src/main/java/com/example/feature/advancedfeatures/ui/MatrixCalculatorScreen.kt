package com.example.feature.advancedfeatures.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixCalculatorScreen(
    viewModel: MatrixCalculatorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Matrix Calculator") },
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
            Text("Select Matrix Size")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(2, 3, 4).forEach { size ->
                    FilterChip(
                        selected = state.size == size,
                        onClick = { /* Update size logic */ },
                        label = { Text("${size}x${size}") }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Matrix A")
                    // Matrix editing UI would go here
                    Text("Placeholder for ${state.size}x${state.size} grid")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.performOperation("+") }, modifier = Modifier.weight(1f)) { Text("+") }
                Button(onClick = { viewModel.performOperation("-") }, modifier = Modifier.weight(1f)) { Text("-") }
                Button(onClick = { viewModel.performOperation("*") }, modifier = Modifier.weight(1f)) { Text("×") }
            }
            
            Button(onClick = { /* Clear */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Clear")
            }
        }
    }
}
