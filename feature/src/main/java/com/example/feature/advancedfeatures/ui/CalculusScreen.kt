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
fun CalculusScreen(
    viewModel: CalculusViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculus Toolkit") },
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
                onValueChange = viewModel::onExpressionChange,
                label = { Text("Function f(x)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. x^2 + sin(x)") }
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Derivative & Limit", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.xValue,
                        onValueChange = viewModel::onXValueChange,
                        label = { Text("Point x") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = viewModel::calculateDerivative, modifier = Modifier.weight(1f)) {
                            Text("f'(x)")
                        }
                        Button(onClick = viewModel::calculateLimit, modifier = Modifier.weight(1f)) {
                            Text("Limit")
                        }
                    }
                    if (state.derivativeResult != null) {
                        Text("f'(${state.xValue}) = ${state.derivativeResult}", modifier = Modifier.padding(top = 8.dp))
                    }
                    if (state.tangentLineResult != null) {
                        Text("Tangent at x=${state.xValue}: ${state.tangentLineResult}", modifier = Modifier.padding(top = 8.dp))
                    }
                    if (state.limitResult != null) {
                        Text("lim x→${state.xValue} = ${state.limitResult}", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Definite Integral", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.lowerBound,
                            onValueChange = viewModel::onLowerBoundChange,
                            label = { Text("From (a)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.upperBound,
                            onValueChange = viewModel::onUpperBoundChange,
                            label = { Text("To (b)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = viewModel::calculateIntegral, modifier = Modifier.fillMaxWidth()) {
                        Text("Integrate ∫ f(x) dx")
                    }
                    if (state.integralResult != null) {
                        Text("∫ f(x) dx from ${state.lowerBound} to ${state.upperBound} ≈ ${state.integralResult}", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
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
