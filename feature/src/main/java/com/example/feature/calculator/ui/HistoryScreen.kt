package com.example.feature.calculator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Calculation
import com.example.feature.calculator.CalculatorEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: List<Calculation>,
    onEvent: (CalculatorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(CalculatorEvent.ToggleHistory) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close History")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(CalculatorEvent.ClearHistory) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History")
                    }
                }
            )
            
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No history yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(history) { calc ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(CalculatorEvent.LoadCalculation(calc)) }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = calc.expression,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = calc.result,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
