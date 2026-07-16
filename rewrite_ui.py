import re

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "r") as f:
    content = f.read()

# Add needed imports
imports_to_add = """import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
"""
for imp in imports_to_add.strip().split("\n"):
    if imp not in content:
        content = content.replace("import androidx.compose.runtime.Composable", imp + "\nimport androidx.compose.runtime.Composable")

# Let's fix portrait layout rows (Memory and Scientific toggle)
old_portrait_controls = """                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryClear) }) { Text("MC") }
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryRecall) }) { Text("MR") }
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryAdd) }) { Text("M+") }
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemorySubtract) }) { Text("M-") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Moved History to TopAppBar, replaced with Scientific toggle positioned lower
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showScientific = !showScientific }
                        ) {
                            Text(if (showScientific) "Basic" else "Scientific")
                        }
                    }"""

new_portrait_controls = """                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showScientific = !showScientific }
                        ) {
                            Text(if (showScientific) "Basic" else "Scientific")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryClear) }) { Text("MC") }
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryRecall) }) { Text("MR") }
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemoryAdd) }) { Text("M+") }
                        TextButton(onClick = { viewModel.onEvent(CalculatorEvent.MemorySubtract) }) { Text("M-") }
                    }"""
content = content.replace(old_portrait_controls, new_portrait_controls)

# Reduce left/right margins for the grids
content = content.replace("modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, bottom = 16.dp)", 
                          "modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, bottom = 16.dp)")

content = content.replace("val buttonSpacing = 8.dp", "val buttonSpacing = 10.dp")

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "w") as f:
    f.write(content)
