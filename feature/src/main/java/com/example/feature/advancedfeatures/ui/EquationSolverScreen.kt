package com.example.feature.advancedfeatures.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquationSolverScreen(
    viewModel: EquationSolverViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equation Solver") },
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
            OutlinedTextField(
                value = state.expression,
                onValueChange = { /* Update expression */ },
                label = { Text("Enter Equation") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 2x + 5 = 10") }
            )

            Button(
                onClick = { /* Solve */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solve")
            }

            if (state.result.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Result", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(state.result, modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text("Steps", fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
                        state.steps.forEach { step ->
                            Text("• $step", modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
