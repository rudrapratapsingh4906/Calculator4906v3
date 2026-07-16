package com.example.feature.advancedfeatures.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplexCalculatorScreen(
    viewModel: ComplexViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complex Calculator") },
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
            Text("First Complex Number (c1)", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.real1,
                    onValueChange = viewModel::onReal1Change,
                    label = { Text("Real") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.imaginary1,
                    onValueChange = viewModel::onImaginary1Change,
                    label = { Text("Imaginary (i)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Text("Second Complex Number (c2)", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.real2,
                    onValueChange = viewModel::onReal2Change,
                    label = { Text("Real") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.imaginary2,
                    onValueChange = viewModel::onImaginary2Change,
                    label = { Text("Imaginary (i)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Result", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.result.isEmpty()) "0" else state.result,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Text("Operations", fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::add, modifier = Modifier.weight(1f)) { Text("c1 + c2") }
                    Button(onClick = viewModel::subtract, modifier = Modifier.weight(1f)) { Text("c1 - c2") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::multiply, modifier = Modifier.weight(1f)) { Text("c1 * c2") }
                    Button(onClick = viewModel::divide, modifier = Modifier.weight(1f)) { Text("c1 / c2") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::magnitude, modifier = Modifier.weight(1f)) { Text("|c1|") }
                    Button(onClick = viewModel::argument, modifier = Modifier.weight(1f)) { Text("Arg(c1)") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::conjugate, modifier = Modifier.weight(1f)) { Text("Conj(c1)") }
                    Button(onClick = viewModel::reciprocal, modifier = Modifier.weight(1f)) { Text("1/c1") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::toPolar, modifier = Modifier.weight(1f)) { Text("Rect → Polar") }
                    Button(onClick = viewModel::fromPolar, modifier = Modifier.weight(1f)) { Text("Polar → Rect") }
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
