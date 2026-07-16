package com.example.feature.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.feature.calculator.CalculatorEvent
import com.example.feature.calculator.CalculatorState

@Composable
fun SettingsScreen(
    state: CalculatorState,
    onEvent: (CalculatorEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text("Vibration", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = state.isVibrationEnabled, onCheckedChange = { onEvent(CalculatorEvent.SetVibrationEnabled(it)) })
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text("Sound", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = state.isSoundEnabled, onCheckedChange = { onEvent(CalculatorEvent.SetSoundEnabled(it)) })
        }
    }
}
