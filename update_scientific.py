import re

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "r") as f:
    content = f.read()

# Replace ScientificButtonGrid entirely
old_grid_start = content.find("@Composable\nfun ScientificButtonGrid(")
old_grid_end = content.find("}", content.rfind("Row(", old_grid_start)) + 5

new_grid = """@Composable
fun ScientificButtonGrid(
    onEvent: (CalculatorEvent) -> Unit,
    isDegreeMode: Boolean,
    modifier: Modifier = Modifier,
    isLandscape: Boolean
) {
    val buttonSpacing = 10.dp
    val color = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColor = MaterialTheme.colorScheme.primaryContainer
    val activeTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    
    val baseButtonModifier = if (isLandscape) Modifier.fillMaxHeight() else Modifier.aspectRatio(1.5f)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it },
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(symbol = if (isDegreeMode) "DEG" else "RAD", color = activeColor, textColor = activeTextColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.ToggleAngleMode) })
            CalculatorButton(symbol = "(", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('(')) })
            CalculatorButton(symbol = ")", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar(')')) })
            CalculatorButton(symbol = "!", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('!')) })
        }
        Row(
            modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it },
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(symbol = "sin", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("sin(")) })
            CalculatorButton(symbol = "cos", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("cos(")) })
            CalculatorButton(symbol = "tan", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("tan(")) })
            CalculatorButton(symbol = "%", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('%')) })
        }
        Row(
            modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it },
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(symbol = "asin", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("asin(")) })
            CalculatorButton(symbol = "acos", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("acos(")) })
            CalculatorButton(symbol = "atan", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("atan(")) })
            CalculatorButton(symbol = "MOD", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString(" mod ")) })
        }
        Row(
            modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it },
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(symbol = "log", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("log(")) })
            CalculatorButton(symbol = "ln", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("ln(")) })
            CalculatorButton(symbol = "√", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("sqrt(")) })
            CalculatorButton(symbol = "ABS", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("abs(")) })
        }
        Row(
            modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it },
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(symbol = "x²", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("^2")) })
            CalculatorButton(symbol = "x³", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("^3")) })
            CalculatorButton(symbol = "xʸ", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('^')) })
            CalculatorButton(symbol = "1/x", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("1/(")) })
        }
        Row(
            modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it },
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(symbol = "10ˣ", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("10^(")) })
            CalculatorButton(symbol = "eˣ", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("exp(")) })
            CalculatorButton(symbol = "EXP", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("E")) })
            CalculatorButton(symbol = "Rand", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("rand")) })
        }
        Row(
            modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it },
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(symbol = "π", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('π')) })
            CalculatorButton(symbol = "e", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('e')) })
            CalculatorButton(symbol = "Floor", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("floor(")) })
            CalculatorButton(symbol = "Ceil", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("ceil(")) })
        }
    }
}
"""

content = content[:old_grid_start] + new_grid + content[old_grid_end:]
with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "w") as f:
    f.write(content)
