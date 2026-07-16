import re

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "r") as f:
    content = f.read()

old_display = """@Composable
fun CalculatorDisplay(
    state: CalculatorState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        if (state.memoryValue != 0.0) {
            Text(
                text = "M",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Text(
            text = state.currentExpression,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 32.sp,
            textAlign = TextAlign.End,
            maxLines = 2,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.result.ifEmpty { "0" },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}"""

new_display = """@Composable
fun CalculatorDisplay(
    state: CalculatorState,
    onEvent: (CalculatorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.isDegreeMode) {
                    Text("DEG", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 14.sp)
                } else {
                    Text("RAD", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 14.sp)
                }
                if (state.memoryValue != 0.0) {
                    Text("M", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        BasicTextField(
            value = TextFieldValue(text = state.currentExpression, selection = TextRange(state.cursorPosition)),
            onValueChange = { 
                onEvent(CalculatorEvent.SetCursorPosition(it.selection.start))
            },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 48.sp,
                textAlign = TextAlign.End
            ),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            readOnly = true
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = if (state.liveResult.isNotEmpty()) "=" + state.liveResult else "",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 28.sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = state.result.ifEmpty { "0" },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 56.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}"""

content = content.replace(old_display, new_display)

content = content.replace("CalculatorDisplay(\n                        state = state,", 
                          "CalculatorDisplay(\n                        state = state,\n                        onEvent = viewModel::onEvent,")

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "w") as f:
    f.write(content)

