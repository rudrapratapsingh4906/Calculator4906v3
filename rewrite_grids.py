import re

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "r") as f:
    content = f.read()

# Strip everything after CalculatorButtonGrid declaration
start = content.find("@Composable\nfun CalculatorButtonGrid(")
if start != -1:
    content = content[:start]

grids = """@Composable
fun CalculatorButtonGrid(
    onEvent: (CalculatorEvent) -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean
) {
    val buttonSpacing = 10.dp
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val onTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer
    
    val baseButtonModifier = if (isLandscape) Modifier.fillMaxHeight() else Modifier.aspectRatio(1.2f)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "AC", color = secondaryContainer, textColor = onSecondaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.Clear) })
            CalculatorButton(symbol = "DEL", color = secondaryContainer, textColor = onSecondaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.DeleteLast) })
            CalculatorButton(symbol = "%", color = secondaryContainer, textColor = onSecondaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('%')) })
            CalculatorButton(symbol = "÷", color = tertiaryContainer, textColor = onTertiaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('/')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "7", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('7')) })
            CalculatorButton(symbol = "8", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('8')) })
            CalculatorButton(symbol = "9", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('9')) })
            CalculatorButton(symbol = "×", color = tertiaryContainer, textColor = onTertiaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('*')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "4", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('4')) })
            CalculatorButton(symbol = "5", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('5')) })
            CalculatorButton(symbol = "6", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('6')) })
            CalculatorButton(symbol = "-", color = tertiaryContainer, textColor = onTertiaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('-')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "1", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('1')) })
            CalculatorButton(symbol = "2", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('2')) })
            CalculatorButton(symbol = "3", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('3')) })
            CalculatorButton(symbol = "+", color = tertiaryContainer, textColor = onTertiaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('+')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "±", color = secondaryContainer, textColor = onSecondaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.TogglePositiveNegative) })
            CalculatorButton(symbol = "0", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('0')) })
            CalculatorButton(symbol = ".", modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('.')) })
            CalculatorButton(symbol = "=", color = primaryContainer, textColor = onPrimaryContainer, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.Calculate) })
        }
    }
}

@Composable
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
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = if (isDegreeMode) "DEG" else "RAD", color = activeColor, textColor = activeTextColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.ToggleAngleMode) })
            CalculatorButton(symbol = "(", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('(')) })
            CalculatorButton(symbol = ")", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar(')')) })
            CalculatorButton(symbol = "!", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('!')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "sin", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("sin(")) })
            CalculatorButton(symbol = "cos", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("cos(")) })
            CalculatorButton(symbol = "tan", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("tan(")) })
            CalculatorButton(symbol = "%", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('%')) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "asin", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("asin(")) })
            CalculatorButton(symbol = "acos", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("acos(")) })
            CalculatorButton(symbol = "atan", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("atan(")) })
            CalculatorButton(symbol = "MOD", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("#")) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "log", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("log(")) })
            CalculatorButton(symbol = "ln", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("ln(")) })
            CalculatorButton(symbol = "√", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("sqrt(")) })
            CalculatorButton(symbol = "ABS", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("abs(")) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "x²", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("^2")) })
            CalculatorButton(symbol = "x³", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("^3")) })
            CalculatorButton(symbol = "xʸ", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('^')) })
            CalculatorButton(symbol = "1/x", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("1/(")) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "10ˣ", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("10^(")) })
            CalculatorButton(symbol = "eˣ", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("exp(")) })
            CalculatorButton(symbol = "EXP", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("E")) })
            CalculatorButton(symbol = "Rand", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("rand")) })
        }
        Row(modifier = Modifier.fillMaxWidth().let { if (isLandscape) it.weight(1f) else it }, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(symbol = "π", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('π')) })
            CalculatorButton(symbol = "e", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputChar('e')) })
            CalculatorButton(symbol = "Floor", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("floor(")) })
            CalculatorButton(symbol = "Ceil", color = color, textColor = textColor, modifier = baseButtonModifier.weight(1f), onClick = { onEvent(CalculatorEvent.InputString("ceil(")) })
        }
    }
}
"""

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "w") as f:
    f.write(content.strip() + "\n" + grids)
