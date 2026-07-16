package com.example.feature.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("About", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("App Name: My Calculator 4906", style = MaterialTheme.typography.bodyLarge)
        Text("Version: 1.0 (1)", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Developer: Placeholder", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("License", style = MaterialTheme.typography.titleMedium)
        Text("MIT License", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Open Source Libraries", style = MaterialTheme.typography.titleMedium)
        Text("- Compose", style = MaterialTheme.typography.bodyMedium)
        Text("- Room", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Privacy Policy", style = MaterialTheme.typography.titleMedium)
        Text("Placeholder", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Terms of Use", style = MaterialTheme.typography.titleMedium)
        Text("Placeholder", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Close")
        }
    }
}
