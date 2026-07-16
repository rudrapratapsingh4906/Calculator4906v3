package com.example.feature.advancedfeatures.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.FormatShapes
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Polyline
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.animation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.*

enum class GraphType {
    Cartesian,
    Parametric,
    Polar,
    Conic
}

enum class ConicType {
    Circle,
    Parabola,
    Ellipse,
    Hyperbola
}

class MathVisualTransformation(
    private val primaryColor: Color,
    private val secondaryColor: Color,
    private val tertiaryColor: Color,
    private val operatorColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = buildAnnotatedString {
            val content = text.text
            var i = 0
            while (i < content.length) {
                val remaining = content.substring(i)
                
                // Match functions
                val functions = listOf("sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "log", "ln", "sqrt", "cbrt", "abs", "mod", "floor", "ceil", "round", "exp")
                val matchedFunc = functions.firstOrNull { remaining.startsWith(it) }
                
                // Match variables
                val variables = listOf("x", "y", "t", "θ", "theta")
                val matchedVar = variables.firstOrNull { remaining.startsWith(it) }
                
                // Match numbers
                val numberRegex = Regex("^[0-9.]+")
                val matchedNum = numberRegex.find(remaining)?.value
                
                // Match operators
                val operators = listOf("+", "-", "*", "/", "^", "%", "(", ")", "=")
                val matchedOp = operators.firstOrNull { remaining.startsWith(it) }

                when {
                    matchedFunc != null -> {
                        withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                            append(matchedFunc)
                        }
                        i += matchedFunc.length
                    }
                    matchedVar != null -> {
                        withStyle(SpanStyle(color = secondaryColor, fontWeight = FontWeight.SemiBold)) {
                            append(matchedVar)
                        }
                        i += matchedVar.length
                    }
                    matchedNum != null -> {
                        withStyle(SpanStyle(color = tertiaryColor)) {
                            append(matchedNum)
                        }
                        i += matchedNum.length
                    }
                    matchedOp != null -> {
                        withStyle(SpanStyle(color = operatorColor)) {
                            append(matchedOp)
                        }
                        i += matchedOp.length
                    }
                    else -> {
                        append(content[i])
                        i++
                    }
                }
            }
        }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}

@Composable
fun AutocompleteRow(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    primaryColor: Color,
    cardColor: Color
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { suggestion ->
            SuggestionChip(
                onClick = { onSuggestionClick(suggestion) },
                label = { Text(suggestion, fontSize = 11.sp) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = primaryColor.copy(alpha = 0.1f),
                    labelColor = primaryColor
                ),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun MathPreview(
    expression: String,
    primaryColor: Color
) {
    if (expression.isBlank()) return
    
    val prettyText = buildAnnotatedString {
        expression.forEach { char ->
            when (char) {
                '^' -> withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 10.sp)) {
                    // This is a simplification, real math rendering is harder
                }
                else -> append(char)
            }
        }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = primaryColor.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = expression.replace("*", "×").replace("/", "÷"),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = primaryColor,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                )
            )
        }
    }
}

@Composable
fun ProfessionalExpressionEditor(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    viewModel: GraphPlotterViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    operatorColor: Color,
    backgroundColor: Color,
    textColor: Color
) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    
    val error = remember(textFieldValue.text) { viewModel.validateExpression(textFieldValue.text) }
    val focusRequester = remember { FocusRequester() }
    
    val functions = listOf("sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "log", "ln", "sqrt", "cbrt", "abs", "mod", "floor", "ceil", "round", "exp", "pi", "e")
    
    val suggestions = remember(textFieldValue.text, textFieldValue.selection) {
        val cursor = textFieldValue.selection.start
        val textBeforeCursor = textFieldValue.text.substring(0, cursor)
        val lastWord = textBeforeCursor.takeLastWhile { it.isLetter() }
        if (lastWord.isNotEmpty()) {
            functions.filter { it.startsWith(lastWord) && it != lastWord }
        } else {
            emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                var finalValue = newValue
                
                // Auto-bracket close
                if (newValue.text.length > textFieldValue.text.length) {
                    val addedChar = newValue.text[newValue.selection.start - 1]
                    if (addedChar == '(') {
                        val newText = StringBuilder(newValue.text).insert(newValue.selection.start, ")").toString()
                        finalValue = newValue.copy(text = newText, selection = TextRange(newValue.selection.start))
                    }
                }
                
                textFieldValue = finalValue
                onValueChange(finalValue.text)
            },
            label = { Text(label, fontSize = 11.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = TextStyle(color = textColor, fontSize = 14.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
            visualTransformation = MathVisualTransformation(primaryColor, secondaryColor, tertiaryColor, operatorColor),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                }
            },
            trailingIcon = {
                if (textFieldValue.text.isNotEmpty()) {
                    IconButton(onClick = { 
                        textFieldValue = TextFieldValue("")
                        onValueChange("")
                    }) {
                        Icon(Icons.Default.Clear, "Clear", tint = textColor.copy(alpha = 0.5f))
                    }
                }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrect = false),
            shape = RoundedCornerShape(12.dp)
        )
        
        AnimatedVisibility(visible = suggestions.isNotEmpty()) {
            AutocompleteRow(
                suggestions = suggestions,
                onSuggestionClick = { suggestion ->
                    val cursor = textFieldValue.selection.start
                    val textBeforeCursor = textFieldValue.text.substring(0, cursor)
                    val lastWord = textBeforeCursor.takeLastWhile { it.isLetter() }
                    val newText = textFieldValue.text.replaceRange(cursor - lastWord.length, cursor, "$suggestion(")
                    textFieldValue = TextFieldValue(newText, TextRange(newText.length))
                    onValueChange(newText)
                },
                primaryColor = primaryColor,
                cardColor = backgroundColor
            )
        }
        
        MathPreview(expression = textFieldValue.text, primaryColor = primaryColor)
    }
}

@Composable
fun GeometryToolbox(
    selectedTool: GeometryTool,
    onToolSelect: (GeometryTool) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    primaryColor: Color,
    cardColor: Color,
    textColor: Color
) {
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .wrapContentSize(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolButton(Icons.Outlined.Undo, "Undo", false, onUndo, primaryColor)
                ToolButton(Icons.Outlined.Redo, "Redo", false, onRedo, primaryColor)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                maxItemsInEachRow = 4,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolButton(Icons.Filled.TouchApp, "Select", selectedTool == GeometryTool.Select, { onToolSelect(GeometryTool.Select) }, primaryColor)
                ToolButton(Icons.Filled.AddCircle, "Point", selectedTool == GeometryTool.Point, { onToolSelect(GeometryTool.Point) }, primaryColor)
                ToolButton(Icons.Filled.HorizontalRule, "Line", selectedTool == GeometryTool.Line, { onToolSelect(GeometryTool.Line) }, primaryColor)
                ToolButton(Icons.Filled.Minimize, "Segment", selectedTool == GeometryTool.Segment, { onToolSelect(GeometryTool.Segment) }, primaryColor)
                ToolButton(Icons.Filled.TrendingFlat, "Ray", selectedTool == GeometryTool.Ray, { onToolSelect(GeometryTool.Ray) }, primaryColor)
                ToolButton(Icons.Filled.ArrowForward, "Vector", selectedTool == GeometryTool.Vector, { onToolSelect(GeometryTool.Vector) }, primaryColor)
                ToolButton(Icons.Filled.Circle, "Circle", selectedTool == GeometryTool.CircleCR, { onToolSelect(GeometryTool.CircleCR) }, primaryColor)
                ToolButton(Icons.Filled.Polyline, "Polygon", selectedTool == GeometryTool.Polygon, { onToolSelect(GeometryTool.Polygon) }, primaryColor)
                ToolButton(Icons.Filled.Straighten, "Distance", selectedTool == GeometryTool.Distance, { onToolSelect(GeometryTool.Distance) }, primaryColor)
                ToolButton(Icons.Filled.Architecture, "Angle", selectedTool == GeometryTool.Angle, { onToolSelect(GeometryTool.Angle) }, primaryColor)
                ToolButton(Icons.Filled.Straighten, "Midpoint", selectedTool == GeometryTool.Midpoint, { onToolSelect(GeometryTool.Midpoint) }, primaryColor)
                ToolButton(Icons.Filled.LinearScale, "Parallel", selectedTool == GeometryTool.Parallel, { onToolSelect(GeometryTool.Parallel) }, primaryColor)
                ToolButton(Icons.Filled.Straighten, "Perpendic.", selectedTool == GeometryTool.Perpendicular, { onToolSelect(GeometryTool.Perpendicular) }, primaryColor)
                ToolButton(Icons.Filled.Architecture, "Angle Bis.", selectedTool == GeometryTool.AngleBisector, { onToolSelect(GeometryTool.AngleBisector) }, primaryColor)
                ToolButton(Icons.Filled.Delete, "Delete", selectedTool == GeometryTool.Delete, { onToolSelect(GeometryTool.Delete) }, MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                if (isSelected) activeColor.copy(alpha = 0.12f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GeometryPanel(
    state: GraphState,
    viewModel: GraphPlotterViewModel,
    primaryColor: Color,
    textColor: Color,
    secondaryTextColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Geometry Objects", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        if (state.geometryObjects.isEmpty()) {
            Text("No objects created yet.", color = secondaryTextColor, fontSize = 11.sp, fontStyle = FontStyle.Italic)
        } else {
            state.geometryObjects.forEach { obj ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (state.selectedObjectId == obj.id) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { viewModel.selectGeometryObject(obj.id) }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val icon = when (obj) {
                        is GeometryObject.Point -> Icons.Default.AddCircleOutline
                        is GeometryObject.Line -> Icons.Default.HorizontalRule
                        is GeometryObject.Segment -> Icons.Default.Minimize
                        is GeometryObject.Circle -> Icons.Outlined.Circle
                        else -> Icons.Default.FormatShapes
                    }
                    Icon(icon, null, tint = obj.style.color, modifier = Modifier.size(14.dp))
                    Text(obj.name, color = textColor, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.deleteGeometryObject(obj.id) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primaryColor: Color,
    textColor: Color,
    cardColor: Color,
    expanded: Boolean = true,
    onExpand: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = BorderStroke(1.dp, Color(1.0f, 1.0f, 1.0f, 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onExpand != null) { onExpand?.invoke() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = primaryColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (onExpand != null) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (expanded) {
                HorizontalDivider(color = Color(1.0f, 1.0f, 1.0f, 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun BottomAnalysisCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    cardColor: Color
) {
    Card(
        modifier = Modifier
            .widthIn(min = 120.dp, max = 150.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = BorderStroke(1.dp, Color(1.0f, 1.0f, 1.0f, 0.06f))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(12.dp))
                Text(text = title, color = secondaryTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            Text(
                text = value,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun GraphPlotterScreen(
    viewModel: GraphPlotterViewModel,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    val gridStep by remember(state.viewport) {
        derivedStateOf {
            val rangeX = state.viewport.maxX - state.viewport.minX
            val rawStep = rangeX / 10.0
            val log10 = Math.log10(rawStep)
            val powerOf10 = Math.pow(10.0, Math.floor(log10))
            val ratio = rawStep / powerOf10
            val step = when {
                ratio < 1.5 -> powerOf10
                ratio < 3.5 -> 2.0 * powerOf10
                ratio < 7.5 -> 5.0 * powerOf10
                else -> 10.0 * powerOf10
            }
            if (step <= 0.0) 1.0 else step
        }
    }
    
    var expressionText by remember { mutableStateOf("") }
    var isTraceMode by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    
    if (showColorPickerDialog) {
        ColorPickerDialog(
            onDismiss = { showColorPickerDialog = false },
            onColorSelected = { color ->
                state.selectedExpressionIndex?.let { index ->
                    viewModel.updateFunctionColor(index, color.toArgb().toLong())
                }
            }
        )
    }

    var functionToEditIndex by remember { mutableStateOf<Int?>(null) }
    var functionToDeleteIndex by remember { mutableStateOf<Int?>(null) }
    var renameExpressionText by remember { mutableStateOf("") }

    if (functionToEditIndex != null) {
        val idx = functionToEditIndex!!
        val currentGraph = state.graphs.getOrNull(idx)
        val initialText = when (currentGraph) {
            is PlottedGraph.Cartesian -> currentGraph.expr
            is PlottedGraph.Polar -> currentGraph.rExpr
            is PlottedGraph.Parametric -> "${currentGraph.xExpr}, ${currentGraph.yExpr}"
            else -> state.expressions.getOrNull(idx) ?: ""
        }
        LaunchedEffect(idx) {
            renameExpressionText = initialText
        }
        AlertDialog(
            onDismissRequest = { functionToEditIndex = null },
            title = { Text("Rename/Edit Function", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    Text(
                        "Modify the expression for this curve:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = renameExpressionText,
                        onValueChange = { renameExpressionText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. sin(x)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.editExpression(idx, renameExpressionText)
                        functionToEditIndex = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { functionToEditIndex = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (functionToDeleteIndex != null) {
        val idx = functionToDeleteIndex!!
        val exprLabel = state.expressions.getOrNull(idx) ?: ""
        AlertDialog(
            onDismissRequest = { functionToDeleteIndex = null },
            title = { Text("Delete Function", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "Are you sure you want to delete '$exprLabel'?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeExpression(idx)
                        functionToDeleteIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { functionToDeleteIndex = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedGraphType by remember { mutableStateOf(GraphType.Cartesian) }
    var selectedConicType by remember { mutableStateOf(ConicType.Circle) }

    var parametricXText by remember { mutableStateOf("") }
    var parametricYText by remember { mutableStateOf("") }
    var polarRText by remember { mutableStateOf("") }

    var circleH by remember { mutableStateOf("0") }
    var circleK by remember { mutableStateOf("0") }
    var circleR by remember { mutableStateOf("5") }

    var parabolaH by remember { mutableStateOf("0") }
    var parabolaK by remember { mutableStateOf("0") }
    var parabolaA by remember { mutableStateOf("1") }
    var parabolaIsHorizontal by remember { mutableStateOf(true) }

    var ellipseH by remember { mutableStateOf("0") }
    var ellipseK by remember { mutableStateOf("0") }
    var ellipseA by remember { mutableStateOf("5") }
    var ellipseB by remember { mutableStateOf("3") }

    var hyperbolaH by remember { mutableStateOf("0") }
    var hyperbolaK by remember { mutableStateOf("0") }
    var hyperbolaA by remember { mutableStateOf("3") }
    var showTangent by remember { mutableStateOf(false) }
    var showNormal by remember { mutableStateOf(false) }
    var showRiemann by remember { mutableStateOf(false) }
    var showAnimatedPoint by remember { mutableStateOf(true) }
    var hyperbolaB by remember { mutableStateOf("2") }
    var hyperbolaIsHorizontal by remember { mutableStateOf(true) }

    fun formatLabel(value: Double, step: Double): String {
        if (Math.abs(value) < 1e-9) return "0"
        val formatted = String.format(java.util.Locale.US, "%.4f", value)
        if (!formatted.contains(".")) return formatted
        return formatted.dropLastWhile { it == '0' }.dropLastWhile { it == '.' }
    }

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF4CAF50), // Green
        Color(0xFFE91E63), // Pink
        Color(0xFFFF9800), // Orange
        Color(0xFF00BCD4)  // Cyan
    )

    var canvasWidth by remember { mutableStateOf(500f) }
    var canvasHeight by remember { mutableStateOf(500f) }

    var showGridSetting by remember { mutableStateOf(true) }
    var showAxisSetting by remember { mutableStateOf(true) }
    var showLabelsSetting by remember { mutableStateOf(true) }
    var isSnapToGridSetting by remember { mutableStateOf(false) }
    var isGraphLockedSetting by remember { mutableStateOf(false) }
    var graphNameText by remember { mutableStateOf("Main Graph") }
    var isSidebarVisible by remember { mutableStateOf(true) }
    var graphWeight by remember { mutableStateOf(0.7f) }
    var toolbarOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var isLoading by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf("Functions") }

    Scaffold(
        topBar = {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            if (isLandscape) {
                androidx.compose.animation.AnimatedVisibility(visible = isSidebarVisible) {
                TopAppBar(
                    title = { Text("JEE Advanced Graph Plotter", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("graph_plotter_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (state.expressions.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clear() },
                                modifier = Modifier.testTag("graph_clear_all_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear All"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1A1D22),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
                }
            } else {
                TopAppBar(
                    title = { Text("Graph Plotter", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("graph_plotter_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (state.expressions.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clear() },
                                modifier = Modifier.testTag("graph_clear_all_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear All"
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            val BackgroundColor = Color(0xFF111315)
            val SurfaceColor = Color(0xFF1A1D22)
            val CardColor = Color(0xFF20242B)
            val PrimaryColor = Color(0xFF7C4DFF)
            val SecondaryColor = Color(0xFF4CAF50)
            val OrangeColor = Color(0xFFFF9800)
            val RedColor = Color(0xFFF44336)
            val BlueColor = Color(0xFF42A5F5)
            val YellowColor = Color(0xFFFBC02D)
            val GridColor = Color(1.0f, 1.0f, 1.0f, 0.08f)
            val AxisColor = Color(1.0f, 1.0f, 1.0f, 0.35f)
            val TextColor = Color(0xFFFFFFFF)
            val SecondaryTextColor = Color(0xFFB8BEC9)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundColor)
                    .padding(paddingValues)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Panel: Graph Area
                val animatedWeight by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSidebarVisible) graphWeight else 1.0f)
                Column(
                    modifier = Modifier
                        .weight(animatedWeight)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Large Graph Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, Color(1.0f, 1.0f, 1.0f, 0.1f)),
                            colors = CardDefaults.cardColors(
                                containerColor = CardColor
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val vp = state.viewport
                                val rangeX = if (vp.maxX - vp.minX == 0.0) 1.0 else vp.maxX - vp.minX
                                val rangeY = if (vp.maxY - vp.minY == 0.0) 1.0 else vp.maxY - vp.minY

                                val curvesPaths = remember(state.points, vp, canvasWidth, canvasHeight) {
                                    state.points.map { pts ->
                                        val path = Path()
                                        if (pts.isNotEmpty()) {
                                            var isDrawing = false
                                            pts.forEach { (x, y) ->
                                                if (x.isNaN() || y.isNaN() || y.isInfinite()) {
                                                    isDrawing = false
                                                } else {
                                                    val px = ((x - vp.minX) / rangeX * canvasWidth).toFloat()
                                                    val py = ((vp.maxY - y) / rangeY * canvasHeight).toFloat()
                                                    if (px in -50f..(canvasWidth + 50f) && py in -50f..(canvasHeight + 50f)) {
                                                        if (!isDrawing) {
                                                            path.moveTo(px, py)
                                                            isDrawing = true
                                                        } else {
                                                            path.lineTo(px, py)
                                                        }
                                                    } else {
                                                        isDrawing = false
                                                    }
                                                }
                                            }
                                        }
                                        path
                                    }
                                }

                                val derivativePaths = remember(state.derivativePoints, vp, canvasWidth, canvasHeight) {
                                    state.plotDerivatives.associateWith { idx ->
                                        val path = Path()
                                        val dPts = state.derivativePoints[idx]
                                        if (dPts != null && dPts.isNotEmpty()) {
                                            var isDrawing = false
                                            dPts.forEach { (dx, dy) ->
                                                if (dx.isNaN() || dy.isNaN() || dy.isInfinite()) {
                                                    isDrawing = false
                                                } else {
                                                    val px = ((dx - vp.minX) / rangeX * canvasWidth).toFloat()
                                                    val py = ((vp.maxY - dy) / rangeY * canvasHeight).toFloat()
                                                    if (px in -50f..(canvasWidth + 50f) && py in -50f..(canvasHeight + 50f)) {
                                                        if (!isDrawing) {
                                                            path.moveTo(px, py)
                                                            isDrawing = true
                                                        } else {
                                                            path.lineTo(px, py)
                                                        }
                                                    } else {
                                                        isDrawing = false
                                                    }
                                                }
                                            }
                                        }
                                        path
                                    }
                                }

                                // Graph Canvas
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .onSizeChanged { size ->
                                            if (size.width > 0 && size.height > 0) {
                                                canvasWidth = size.width.toFloat()
                                                canvasHeight = size.height.toFloat()
                                            }
                                        }
                                        .pointerInput(canvasWidth, canvasHeight, isTraceMode, isGraphLockedSetting, state.is3DMode, state.isGeometryMode) {
                                            if (state.is3DMode) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    viewModel.updateCamera(-dragAmount.y * 0.5f, dragAmount.x * 0.5f)
                                                }
                                            } else if (state.isGeometryMode) {
                                                detectTapGestures { offset ->
                                                    viewModel.onGeometryCanvasClick(offset.x.toDouble(), offset.y.toDouble(), canvasWidth, canvasHeight)
                                                }
                                            } else if (isTraceMode) {
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        viewModel.selectNearestPoint(offset.x, offset.y, canvasWidth, canvasHeight)
                                                    },
                                                    onDrag = { change, _ ->
                                                        viewModel.selectNearestPoint(change.position.x, change.position.y, canvasWidth, canvasHeight)
                                                    },
                                                    onDragEnd = {}
                                                )
                                            } else if (!isGraphLockedSetting) {
                                                detectTransformGestures { _, panDelta, zoomFactor, _ ->
                                                    if (state.is3DMode) {
                                                        if (zoomFactor != 1f) {
                                                            viewModel.setCameraZoom(state.cameraZoom * zoomFactor)
                                                        }
                                                        if (panDelta.x != 0f || panDelta.y != 0f) {
                                                            viewModel.updateCamera(-panDelta.y / 2f, panDelta.x / 2f)
                                                        }
                                                    } else {
                                                        if (zoomFactor != 1f) {
                                                            viewModel.zoom(zoomFactor.toDouble())
                                                        }
                                                        if (panDelta.x != 0f || panDelta.y != 0f) {
                                                            val vp = state.viewport
                                                            val unitX = (vp.maxX - vp.minX) / canvasWidth
                                                            val unitY = (vp.maxY - vp.minY) / canvasHeight
                                                            viewModel.pan(-panDelta.x.toDouble() * unitX, panDelta.y.toDouble() * unitY)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    val W = size.width
                                    val H = size.height
                                    if (W <= 0f || H <= 0f) return@Canvas

                                    if (state.is3DMode) {
                                        draw3DGraph(state, viewModel, W, H)
                                    } else {
                                        val vp = state.viewport
                                        val rangeX = vp.maxX - vp.minX
                                        val rangeY = vp.maxY - vp.minY

                                        fun toPxX(x: Double): Float = ((x - vp.minX) / rangeX * W).toFloat()
                                        fun toPxY(y: Double): Float = ((vp.maxY - y) / rangeY * H).toFloat()
                                        
                                        // Draw Geometry Objects
                                        drawGeometryObjects(this, state, ::toPxX, ::toPxY)

                                    val minX = vp.minX
                                    val maxX = vp.maxX
                                    val minY = vp.minY
                                    val maxY = vp.maxY

                                    val startX = Math.floor(minX / gridStep) * gridStep
                                    val endX = Math.ceil(maxX / gridStep) * gridStep
                                    val startY = Math.floor(minY / gridStep) * gridStep
                                    val endY = Math.ceil(maxY / gridStep) * gridStep

                                    // Draw Grid Lines
                                    var currX = startX
                                    while (currX <= endX) {
                                        if (Math.abs(currX) > 1e-9) {
                                            val px = toPxX(currX)
                                            drawLine(
                                                color = GridColor,
                                                start = androidx.compose.ui.geometry.Offset(px, 0f),
                                                end = androidx.compose.ui.geometry.Offset(px, H),
                                                strokeWidth = 1f
                                            )
                                        }
                                        currX += gridStep
                                    }

                                    var currY = startY
                                    while (currY <= endY) {
                                        if (Math.abs(currY) > 1e-9) {
                                            val py = toPxY(currY)
                                            drawLine(
                                                color = GridColor,
                                                start = androidx.compose.ui.geometry.Offset(0f, py),
                                                end = androidx.compose.ui.geometry.Offset(W, py),
                                                strokeWidth = 1f
                                            )
                                        }
                                        currY += gridStep
                                    }

                                    val rawXAxisY = toPxY(0.0)
                                    val rawYAxisX = toPxX(0.0)
                                    val xAxisY = rawXAxisY.coerceIn(0f, H)
                                    val yAxisX = rawYAxisX.coerceIn(0f, W)

                                    drawLine(
                                        color = AxisColor,
                                        start = androidx.compose.ui.geometry.Offset(0f, xAxisY),
                                        end = androidx.compose.ui.geometry.Offset(W, xAxisY),
                                        strokeWidth = 2f
                                    )
                                    drawLine(
                                        color = AxisColor,
                                        start = androidx.compose.ui.geometry.Offset(yAxisX, 0f),
                                        end = androidx.compose.ui.geometry.Offset(yAxisX, H),
                                        strokeWidth = 2f
                                    )

                                    val paint = android.graphics.Paint().apply {
                                        color = TextColor.toArgb()
                                        textSize = 28f
                                        isAntiAlias = true
                                    }

                                    var tickX = startX
                                    while (tickX <= endX) {
                                        if (Math.abs(tickX) > 1e-9) {
                                            val px = toPxX(tickX)
                                            if (px in 0f..W) {
                                                drawLine(
                                                    color = AxisColor,
                                                    start = androidx.compose.ui.geometry.Offset(px, xAxisY - 6f),
                                                    end = androidx.compose.ui.geometry.Offset(px, xAxisY + 6f),
                                                    strokeWidth = 2f
                                                )
                                                val label = formatLabel(tickX, gridStep)
                                                val labelY = if (xAxisY + 34f > H - 10f) H - 10f else if (xAxisY + 34f < 40f) 40f else xAxisY + 34f
                                                drawContext.canvas.nativeCanvas.drawText(
                                                    label,
                                                    px - paint.measureText(label) / 2f,
                                                    labelY,
                                                    paint
                                                )
                                            }
                                        }
                                        tickX += gridStep
                                    }

                                    var tickY = startY
                                    while (tickY <= endY) {
                                        if (Math.abs(tickY) > 1e-9) {
                                            val py = toPxY(tickY)
                                            if (py in 0f..H) {
                                                drawLine(
                                                    color = AxisColor,
                                                    start = androidx.compose.ui.geometry.Offset(yAxisX - 6f, py),
                                                    end = androidx.compose.ui.geometry.Offset(yAxisX + 6f, py),
                                                    strokeWidth = 2f
                                                )
                                                val label = formatLabel(tickY, gridStep)
                                                val labelX = if (yAxisX - 16f < 10f) yAxisX + 16f else yAxisX - 16f
                                                val align = if (yAxisX - 16f < 10f) android.graphics.Paint.Align.LEFT else android.graphics.Paint.Align.RIGHT
                                                drawContext.canvas.nativeCanvas.drawText(
                                                    label,
                                                    labelX,
                                                    py + 10f,
                                                    paint.apply { textAlign = align }
                                                )
                                            }
                                        }
                                        tickY += gridStep
                                    }

                                    if (rawYAxisX in 0f..W && rawXAxisY in 0f..H) {
                                        drawContext.canvas.nativeCanvas.drawText(
                                            "0",
                                            yAxisX - 16f,
                                            xAxisY + 30f,
                                            paint.apply { textAlign = android.graphics.Paint.Align.RIGHT }
                                        )
                                    }

                                // 2. Shaded definite integration
                                state.shadedIntegration?.let { info ->
                                    if (info.expressionIndex in state.points.indices) {
                                        val pts = state.points[info.expressionIndex]
                                        val path = Path()
                                        var first = true
                                        val shadePoints = pts.filter { it.first in info.a..info.b }
                                        if (shadePoints.isNotEmpty()) {
                                            shadePoints.forEach { (pxVal, pyVal) ->
                                                val screenX = toPxX(pxVal)
                                                val screenY = toPxY(pyVal)
                                                if (!pxVal.isNaN() && !pyVal.isNaN() && !pyVal.isInfinite()) {
                                                    if (first) {
                                                        path.moveTo(screenX, toPxY(0.0))
                                                        path.lineTo(screenX, screenY)
                                                        first = false
                                                    } else {
                                                        path.lineTo(screenX, screenY)
                                                    }
                                                }
                                            }
                                            val lastPt = shadePoints.last()
                                            path.lineTo(toPxX(lastPt.first), toPxY(0.0))
                                            path.close()
                                            drawPath(
                                                path = path,
                                                color = YellowColor.copy(alpha = 0.25f)
                                            )
                                        }
                                    }
                                }

                                // 3. Plotted curves
                                curvesPaths.forEachIndexed { index, path ->
                                    if (!path.isEmpty) {
                                        val color = state.expressionColors[index]?.let { androidx.compose.ui.graphics.Color(it) } ?: colors[index % colors.size]
                                        drawPath(
                                            path = path,
                                            color = color,
                                            style = Stroke(
                                                width = 3.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                }

                                // 4. Derivative curves
                                state.plotDerivatives.forEach { idx ->
                                    val path = derivativePaths[idx]
                                    if (path != null && !path.isEmpty) {
                                        drawPath(
                                            path = path,
                                            color = (state.expressionColors[idx]?.let { androidx.compose.ui.graphics.Color(it) } ?: colors[idx % colors.size]).copy(alpha = 0.6f),
                                            style = Stroke(
                                                width = 2.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round,
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                                    floatArrayOf(15f, 10f), 0f
                                                )
                                            )
                                        )
                                    }
                                }

                                    // 5. Roots points (red)
                                    state.roots.forEach { rx ->
                                        if (rx in vp.minX..vp.maxX) {
                                            drawCircle(
                                                color = RedColor,
                                                radius = 6.dp.toPx(),
                                                center = androidx.compose.ui.geometry.Offset(toPxX(rx), toPxY(0.0))
                                            )
                                        }
                                    }

                                    // 5. Extrema points
                                    state.extrema.forEach { ext ->
                                        if (ext.x in vp.minX..vp.maxX && ext.y in vp.minY..vp.maxY) {
                                            drawCircle(
                                                color = if (ext.isMaximum) BlueColor else OrangeColor,
                                                radius = 6.dp.toPx(),
                                                center = androidx.compose.ui.geometry.Offset(toPxX(ext.x), toPxY(ext.y))
                                            )
                                        }
                                    }

                                    // 6. Intersection points
                                    state.intersections.forEach { (ix, iy) ->
                                        if (ix in vp.minX..vp.maxX && iy in vp.minY..vp.maxY) {
                                            drawCircle(
                                                color = OrangeColor,
                                                radius = 6.dp.toPx(),
                                                center = androidx.compose.ui.geometry.Offset(toPxX(ix), toPxY(iy))
                                            )
                                        }
                                    }

                                    // Advanced Visualizations: Riemann Sums, Tangent, Normal, and Animated Point
                                    val firstCartesian = state.graphs.filterIsInstance<PlottedGraph.Cartesian>().firstOrNull()
                                    if (firstCartesian != null) {
                                        val expr = firstCartesian.expr
                                        
                                        // Riemann Sums
                                        if (showRiemann) {
                                            val n = 20
                                            val dx = (vp.maxX - vp.minX) / n
                                            for (i in 0 until n) {
                                                val rx = vp.minX + i * dx + dx / 2.0
                                                val ry = viewModel.evaluateExpression(expr, rx) ?: 0.0
                                                val rpx1 = toPxX(vp.minX + i * dx)
                                                val rpx2 = toPxX(vp.minX + (i + 1) * dx)
                                                val rpy = toPxY(ry)
                                                val zeroY = toPxY(0.0)
                                                
                                                drawRect(
                                                    color = Color.Cyan.copy(alpha = 0.2f),
                                                    topLeft = androidx.compose.ui.geometry.Offset(rpx1, Math.min(rpy, zeroY)),
                                                    size = androidx.compose.ui.geometry.Size(rpx2 - rpx1, Math.abs(rpy - zeroY))
                                                )
                                                drawRect(
                                                    color = Color.Cyan.copy(alpha = 0.5f),
                                                    topLeft = androidx.compose.ui.geometry.Offset(rpx1, Math.min(rpy, zeroY)),
                                                    size = androidx.compose.ui.geometry.Size(rpx2 - rpx1, Math.abs(rpy - zeroY)),
                                                    style = Stroke(width = 1f)
                                                )
                                            }
                                        }

                                        // Animated Point Visuals
                                        val tLoop = (state.animationTime % 10f) / 10f
                                        val animX = vp.minX + tLoop * (vp.maxX - vp.minX)
                                        val animY = viewModel.evaluateExpression(expr, animX)
                                        
                                        if (animY != null && animY.isFinite()) {
                                            val apx = toPxX(animX)
                                            val apy = toPxY(animY)
                                            
                                            if (apx in 0f..W && apy in 0f..H) {
                                                if (showAnimatedPoint && state.isAnimating) {
                                                    drawCircle(
                                                        color = Color.White,
                                                        radius = 10f,
                                                        center = androidx.compose.ui.geometry.Offset(apx, apy)
                                                    )
                                                    drawCircle(
                                                        color = OrangeColor,
                                                        radius = 7f,
                                                        center = androidx.compose.ui.geometry.Offset(apx, apy)
                                                    )
                                                }

                                                val slope = viewModel.getDerivativeAt(expr, animX)
                                                if (slope != null && slope.isFinite()) {
                                                    if (showTangent) {
                                                        val tx1 = vp.minX
                                                        val ty1 = slope * (tx1 - animX) + animY
                                                        val tx2 = vp.maxX
                                                        val ty2 = slope * (tx2 - animX) + animY
                                                        drawLine(
                                                            color = Color.Green,
                                                            start = androidx.compose.ui.geometry.Offset(toPxX(tx1), toPxY(ty1)),
                                                            end = androidx.compose.ui.geometry.Offset(toPxX(tx2), toPxY(ty2)),
                                                            strokeWidth = 2f,
                                                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                                        )
                                                    }
                                                    if (showNormal) {
                                                        val nSlope = if (Math.abs(slope) < 1e-9) 1e9 else -1.0 / slope
                                                        val ny1 = nSlope * (vp.minX - animX) + animY
                                                        val ny2 = nSlope * (vp.maxX - animX) + animY
                                                        drawLine(
                                                            color = Color.Magenta,
                                                            start = androidx.compose.ui.geometry.Offset(toPxX(vp.minX), toPxY(ny1)),
                                                            end = androidx.compose.ui.geometry.Offset(toPxX(vp.maxX), toPxY(ny2)),
                                                            strokeWidth = 2f,
                                                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 7. Selected trace coordinate & lines
                                    state.selectedPoint?.let { (selX, selY) ->
                                        val px = toPxX(selX)
                                        val py = toPxY(selY)
                                        if (px in 0f..W && py in 0f..H) {
                                            // horizontal trace guide line
                                            drawLine(
                                                color = AxisColor.copy(alpha = 0.25f),
                                                start = androidx.compose.ui.geometry.Offset(0f, py),
                                                end = androidx.compose.ui.geometry.Offset(W, py),
                                                strokeWidth = 1.dp.toPx(),
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                                    floatArrayOf(10f, 10f), 0f
                                                )
                                            )
                                            // vertical trace guide line
                                            drawLine(
                                                color = AxisColor.copy(alpha = 0.25f),
                                                start = androidx.compose.ui.geometry.Offset(px, 0f),
                                                end = androidx.compose.ui.geometry.Offset(px, H),
                                                strokeWidth = 1.dp.toPx(),
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                                    floatArrayOf(10f, 10f), 0f
                                                )
                                            )

                                            // Trace intersection coords circle
                                            drawCircle(
                                                color = SecondaryColor,
                                                radius = 8.dp.toPx(),
                                                center = androidx.compose.ui.geometry.Offset(px, py)
                                            )

                                            // Draw text coordinate floating banner
                                            val text = "(${String.format(java.util.Locale.US, "%.3f", selX)}, ${String.format(java.util.Locale.US, "%.3f", selY)})"
                                            val bannerPaint = android.graphics.Paint().apply {
                                                color = TextColor.toArgb()
                                                textSize = 30f
                                                isAntiAlias = true
                                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                                            }
                                            val textWidth = bannerPaint.measureText(text)
                                            val bannerX = if (px + textWidth + 30f > W) px - textWidth - 30f else px + 15f
                                            val bannerY = if (py - 25f < 30f) py + 45f else py - 15f

                                            drawContext.canvas.nativeCanvas.drawText(
                                                text,
                                                bannerX,
                                                bannerY,
                                                bannerPaint
                                            )
                                        }
                                    }
                                }
                            }

                                // Floating Toolbar (Placed here to be on top of Canvas)
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset { androidx.compose.ui.unit.IntOffset(toolbarOffset.x.toInt(), toolbarOffset.y.toInt()) }
                                        .pointerInput(Unit) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                toolbarOffset += dragAmount
                                            }
                                        }
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.8f))
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                                        IconButton(onClick = { viewModel.zoomIn() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Add, "Zoom In", tint = TextColor, modifier = Modifier.size(18.dp)) }
                                        IconButton(onClick = { viewModel.zoomOut() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Remove, "Zoom Out", tint = TextColor, modifier = Modifier.size(18.dp)) }
                                        IconButton(onClick = { viewModel.resetViewport() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.ZoomOutMap, "Fit All", tint = TextColor, modifier = Modifier.size(18.dp)) }
                                        IconButton(onClick = { viewModel.resetViewport() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Refresh, "Reset View", tint = TextColor, modifier = Modifier.size(18.dp)) }
                                        IconButton(onClick = { isTraceMode = !isTraceMode }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Timeline, "Trace", tint = if (isTraceMode) PrimaryColor else TextColor, modifier = Modifier.size(18.dp)) }
                                        IconButton(onClick = { isSidebarVisible = !isSidebarVisible }, modifier = Modifier.size(32.dp)) { Icon(if (isSidebarVisible) Icons.Default.Fullscreen else Icons.Default.FullscreenExit, "Fullscreen", tint = TextColor, modifier = Modifier.size(18.dp)) }
                                        IconButton(onClick = { viewModel.toggleGeometryMode() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.FormatShapes, "Geometry", tint = if (state.isGeometryMode) PrimaryColor else TextColor, modifier = Modifier.size(18.dp)) }
                                    }
                                }

                                // Geometry Toolbox
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = state.isGeometryMode,
                                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                                ) {
                                    GeometryToolbox(
                                        selectedTool = state.selectedTool,
                                        onToolSelect = { viewModel.selectTool(it) },
                                        onUndo = { viewModel.undo() },
                                        onRedo = { viewModel.redo() },
                                        primaryColor = PrimaryColor,
                                        cardColor = CardColor,
                                        textColor = TextColor
                                    )
                                }

                                // Mini Overview Map
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                        .size(100.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    colors = CardDefaults.cardColors(containerColor = BackgroundColor.copy(alpha = 0.9f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val mapW = size.width
                                        val mapH = size.height
                                        val globalMinX = -50.0
                                        val globalMaxX = 50.0
                                        val globalMinY = -50.0
                                        val globalMaxY = 50.0

                                        // Draw axes
                                        val xAxisY = (mapH * globalMaxY / (globalMaxY - globalMinY)).toFloat()
                                        val yAxisX = (mapW * (0 - globalMinX) / (globalMaxX - globalMinX)).toFloat()
                                        drawLine(Color.White.copy(alpha = 0.5f), start = androidx.compose.ui.geometry.Offset(0f, xAxisY), end = androidx.compose.ui.geometry.Offset(mapW, xAxisY), strokeWidth = 1f)
                                        drawLine(Color.White.copy(alpha = 0.5f), start = androidx.compose.ui.geometry.Offset(yAxisX, 0f), end = androidx.compose.ui.geometry.Offset(yAxisX, mapH), strokeWidth = 1f)

                                        // Draw viewport rect
                                        val vpMinX = state.viewport.minX.coerceIn(globalMinX, globalMaxX)
                                        val vpMaxX = state.viewport.maxX.coerceIn(globalMinX, globalMaxX)
                                        val vpMinY = state.viewport.minY.coerceIn(globalMinY, globalMaxY)
                                        val vpMaxY = state.viewport.maxY.coerceIn(globalMinY, globalMaxY)

                                        val rectLeft = (mapW * (vpMinX - globalMinX) / (globalMaxX - globalMinX)).toFloat()
                                        val rectRight = (mapW * (vpMaxX - globalMinX) / (globalMaxX - globalMinX)).toFloat()
                                        val rectTop = (mapH * (globalMaxY - vpMaxY) / (globalMaxY - globalMinY)).toFloat()
                                        val rectBottom = (mapH * (globalMaxY - vpMinY) / (globalMaxY - globalMinY)).toFloat()

                                        drawRect(
                                            color = PrimaryColor.copy(alpha = 0.3f),
                                            topLeft = androidx.compose.ui.geometry.Offset(rectLeft, rectTop),
                                            size = androidx.compose.ui.geometry.Size(rectRight - rectLeft, rectBottom - rectTop)
                                        )
                                        drawRect(
                                            color = PrimaryColor,
                                            topLeft = androidx.compose.ui.geometry.Offset(rectLeft, rectTop),
                                            size = androidx.compose.ui.geometry.Size(rectRight - rectLeft, rectBottom - rectTop),
                                            style = Stroke(width = 2f)
                                        )
                                    }
                                }
                    }
                    }
                    }

                    // Bottom horizontal analysis cards
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            val selPoint = state.selectedPoint
                            val selIndex = state.selectedExpressionIndex
                            val valueStr = if (selPoint != null && selIndex != null && selIndex in state.expressions.indices) {
                                val expr = state.expressions[selIndex]
                                val slope = viewModel.getDerivativeAt(expr, selPoint.first)
                                if (slope != null && !slope.isNaN() && !slope.isInfinite()) {
                                    "dy/dx = ${formatLabel(slope, 1.0)} at x=${formatLabel(selPoint.first, 1.0)}"
                                } else "Undefined"
                            } else "No trace active"
                            BottomAnalysisCard(
                                title = "Derivative",
                                value = valueStr,
                                icon = Icons.Default.ShowChart,
                                iconColor = SecondaryColor,
                                textColor = TextColor,
                                secondaryTextColor = SecondaryTextColor,
                                cardColor = CardColor
                            )
                        }
                        item {
                            val valueStr = state.shadedIntegration?.let {
                                "∫ = ${formatLabel(it.result, 1.0)} [${formatLabel(it.a, 1.0)}, ${formatLabel(it.b, 1.0)}]"
                            } ?: "No active definite integral shade"
                            BottomAnalysisCard(
                                title = "Integral",
                                value = valueStr,
                                icon = Icons.Default.ShowChart,
                                iconColor = YellowColor,
                                textColor = TextColor,
                                secondaryTextColor = SecondaryTextColor,
                                cardColor = CardColor
                            )
                        }
                        item {
                            val valueStr = if (state.roots.isNotEmpty()) {
                                state.roots.joinToString(", ") { formatLabel(it, 1.0) }
                            } else "No real roots in current view"
                            BottomAnalysisCard(
                                title = "Roots",
                                value = valueStr,
                                icon = Icons.Default.Info,
                                iconColor = RedColor,
                                textColor = TextColor,
                                secondaryTextColor = SecondaryTextColor,
                                cardColor = CardColor
                            )
                        }
                        item {
                            val valueStr = if (state.extrema.isNotEmpty()) {
                                "${state.extrema.size} points detected"
                            } else "No local extrema in view"
                            BottomAnalysisCard(
                                title = "Extrema",
                                value = valueStr,
                                icon = Icons.Default.Info,
                                iconColor = BlueColor,
                                textColor = TextColor,
                                secondaryTextColor = SecondaryTextColor,
                                cardColor = CardColor
                            )
                        }
                        item {
                            val valueStr = if (state.intersections.isNotEmpty()) {
                                "${state.intersections.size} system solutions"
                            } else "No system intersections"
                            BottomAnalysisCard(
                                title = "Intersection",
                                value = valueStr,
                                icon = Icons.Default.ShowChart,
                                iconColor = OrangeColor,
                                textColor = TextColor,
                                secondaryTextColor = SecondaryTextColor,
                                cardColor = CardColor
                            )
                        }
                        item {
                            val selPoint = state.selectedPoint
                            val selIndex = state.selectedExpressionIndex
                            val valueStr = if (selPoint != null && selIndex != null && selIndex in state.expressions.indices) {
                                val expr = state.expressions[selIndex]
                                val slope = viewModel.getDerivativeAt(expr, selPoint.first)
                                if (slope != null && !slope.isNaN() && !slope.isInfinite()) {
                                    val c_t = selPoint.second - slope * selPoint.first
                                    val sign_t = if (c_t >= 0.0) "+" else "-"
                                    "y = ${formatLabel(slope, 1.0)}x $sign_t ${formatLabel(Math.abs(c_t), 1.0)}"
                                } else "Undefined"
                            } else "Select trace point"
                            BottomAnalysisCard(
                                title = "Tangent Line",
                                value = valueStr,
                                icon = Icons.Default.ShowChart,
                                iconColor = SecondaryColor,
                                textColor = TextColor,
                                secondaryTextColor = SecondaryTextColor,
                                cardColor = CardColor
                            )
                        }
                        item {
                            val selPoint = state.selectedPoint
                            val selIndex = state.selectedExpressionIndex
                            val valueStr = if (selPoint != null && selIndex != null && selIndex in state.expressions.indices) {
                                val expr = state.expressions[selIndex]
                                val slope = viewModel.getDerivativeAt(expr, selPoint.first)
                                if (slope != null && !slope.isNaN() && !slope.isInfinite()) {
                                    val nSlope = if (Math.abs(slope) < 1e-9) Double.NaN else -1.0 / slope
                                    if (nSlope.isNaN()) {
                                        "x = ${formatLabel(selPoint.first, 1.0)}"
                                    } else {
                                        val c_n = selPoint.second - nSlope * selPoint.first
                                        val sign_n = if (c_n >= 0.0) "+" else "-"
                                        "y = ${formatLabel(nSlope, 1.0)}x $sign_n ${formatLabel(Math.abs(c_n), 1.0)}"
                                    }
                                } else "Undefined"
                            } else "Select trace point"
                            BottomAnalysisCard(
                                title = "Normal Line",
                                value = valueStr,
                                icon = Icons.Default.ShowChart,
                                iconColor = RedColor,
                                textColor = TextColor,
                                secondaryTextColor = SecondaryTextColor,
                                cardColor = CardColor
                            )
                        }
                        item {
                            val valueStr = state.shadedIntegration?.let {
                                "Shaded: ${formatLabel(it.result, 1.0)}"
                            } ?: "No integration selected"
                            BottomAnalysisCard(
                                title = "Area Under Curve",
                                value = valueStr,
                                icon = Icons.Default.ShowChart,
                                iconColor = OrangeColor,
                                textColor = TextColor,
                                secondaryTextColor = SecondaryTextColor,
                                cardColor = CardColor
                            )
                        }
                    }
                }

                if (isSidebarVisible) {
                    // Draggable Divider
                    var isDragging by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(16.dp)
                            .background(Color.Transparent)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragStart = { isDragging = true },
                                    onDragEnd = { isDragging = false },
                                    onDragCancel = { isDragging = false },
                                    onHorizontalDrag = { change, dragAmount ->
                                        val rowWidth = canvasWidth.toFloat()
                                        val fraction = dragAmount.toFloat() / (if (rowWidth > 0f) rowWidth else 1000f)
                                        graphWeight = (graphWeight + fraction).coerceIn(0.2f, 0.8f)
                                    }
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                .align(Alignment.Center)
                        )
                    }

                    // Right Panel: Sidebar
                    Column(
                        modifier = Modifier
                            .weight(1f - graphWeight)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    // Card 1: Functions
                    SidebarCard(
                        title = "Functions",
                        icon = Icons.Default.ShowChart,
                        primaryColor = PrimaryColor,
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "Functions",
                        onExpand = { expandedSection = "Functions" }
                    ) {
                        // Graph type selectors
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GraphType.values().forEach { type ->
                                val isSelected = selectedGraphType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedGraphType = type },
                                    label = { Text(type.name, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor.copy(alpha = 0.2f),
                                        selectedLabelColor = PrimaryColor,
                                        labelColor = SecondaryTextColor
                                    )
                                )
                            }
                        }

                        // Input fields based on selected graph type
                        when (selectedGraphType) {
                            GraphType.Cartesian -> {
                                ProfessionalExpressionEditor(
                                    value = expressionText,
                                    onValueChange = { expressionText = it },
                                    label = "Enter y = f(x)",
                                    viewModel = viewModel,
                                    primaryColor = PrimaryColor,
                                    secondaryColor = SecondaryColor,
                                    tertiaryColor = YellowColor,
                                    operatorColor = OrangeColor,
                                    backgroundColor = BackgroundColor,
                                    textColor = TextColor
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { viewModel.saveFormula(expressionText) }) {
                                        Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save Formula", fontSize = 11.sp)
                                    }
                                }
                            }
                            GraphType.Parametric -> {
                                ProfessionalExpressionEditor(
                                    value = parametricXText,
                                    onValueChange = { parametricXText = it },
                                    label = "x(t) =",
                                    viewModel = viewModel,
                                    primaryColor = PrimaryColor,
                                    secondaryColor = SecondaryColor,
                                    tertiaryColor = YellowColor,
                                    operatorColor = OrangeColor,
                                    backgroundColor = BackgroundColor,
                                    textColor = TextColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ProfessionalExpressionEditor(
                                    value = parametricYText,
                                    onValueChange = { parametricYText = it },
                                    label = "y(t) =",
                                    viewModel = viewModel,
                                    primaryColor = PrimaryColor,
                                    secondaryColor = SecondaryColor,
                                    tertiaryColor = YellowColor,
                                    operatorColor = OrangeColor,
                                    backgroundColor = BackgroundColor,
                                    textColor = TextColor
                                )
                            }
                            GraphType.Polar -> {
                                ProfessionalExpressionEditor(
                                    value = polarRText,
                                    onValueChange = { polarRText = it },
                                    label = "r(theta) =",
                                    viewModel = viewModel,
                                    primaryColor = PrimaryColor,
                                    secondaryColor = SecondaryColor,
                                    tertiaryColor = YellowColor,
                                    operatorColor = OrangeColor,
                                    backgroundColor = BackgroundColor,
                                    textColor = TextColor
                                )
                            }
                            GraphType.Conic -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ConicType.values().forEach { conic ->
                                        val isSel = selectedConicType == conic
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { selectedConicType = conic },
                                            label = { Text(conic.name, fontSize = 10.sp) }
                                        )
                                    }
                                }
                                when (selectedConicType) {
                                    ConicType.Circle -> {
                                        OutlinedTextField(value = circleH, onValueChange = { circleH = it }, label = { Text("h (center x)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = circleK, onValueChange = { circleK = it }, label = { Text("k (center y)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = circleR, onValueChange = { circleR = it }, label = { Text("Radius r", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                    }
                                    ConicType.Parabola -> {
                                        OutlinedTextField(value = parabolaH, onValueChange = { parabolaH = it }, label = { Text("h (vertex x)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = parabolaK, onValueChange = { parabolaK = it }, label = { Text("k (vertex y)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = parabolaA, onValueChange = { parabolaA = it }, label = { Text("a value", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Horizontal", color = SecondaryTextColor, fontSize = 12.sp)
                                            Switch(checked = parabolaIsHorizontal, onCheckedChange = { parabolaIsHorizontal = it })
                                        }
                                    }
                                    ConicType.Ellipse -> {
                                        OutlinedTextField(value = ellipseH, onValueChange = { ellipseH = it }, label = { Text("h (center x)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = ellipseK, onValueChange = { ellipseK = it }, label = { Text("k (center y)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = ellipseA, onValueChange = { ellipseA = it }, label = { Text("a semi-major", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = ellipseB, onValueChange = { ellipseB = it }, label = { Text("b semi-minor", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                    }
                                    ConicType.Hyperbola -> {
                                        OutlinedTextField(value = hyperbolaH, onValueChange = { hyperbolaH = it }, label = { Text("h (center x)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = hyperbolaK, onValueChange = { hyperbolaK = it }, label = { Text("k (center y)", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = hyperbolaA, onValueChange = { hyperbolaA = it }, label = { Text("a value", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        OutlinedTextField(value = hyperbolaB, onValueChange = { hyperbolaB = it }, label = { Text("b value", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp))
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Horizontal", color = SecondaryTextColor, fontSize = 12.sp)
                                            Switch(checked = hyperbolaIsHorizontal, onCheckedChange = { hyperbolaIsHorizontal = it })
                                        }
                                    }
                                }
                            }
                        }

                        // Plot Curve Button
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                when (selectedGraphType) {
                                    GraphType.Cartesian -> {
                                        if (expressionText.isNotBlank()) {
                                            viewModel.addExpression(expressionText.trim())
                                        }
                                    }
                                    GraphType.Parametric -> {
                                        if (parametricXText.isNotBlank() && parametricYText.isNotBlank()) {
                                            viewModel.addParametric(parametricXText.trim(), parametricYText.trim())
                                        }
                                    }
                                    GraphType.Polar -> {
                                        if (polarRText.isNotBlank()) {
                                            viewModel.addPolar(polarRText.trim())
                                        }
                                    }
                                    GraphType.Conic -> {
                                        when (selectedConicType) {
                                            ConicType.Circle -> {
                                                val h = circleH.toDoubleOrNull() ?: 0.0
                                                val k = circleK.toDoubleOrNull() ?: 0.0
                                                val r = circleR.toDoubleOrNull() ?: 5.0
                                                viewModel.addConicCircle(h, k, r)
                                            }
                                            ConicType.Parabola -> {
                                                val h = parabolaH.toDoubleOrNull() ?: 0.0
                                                val k = parabolaK.toDoubleOrNull() ?: 0.0
                                                val a = parabolaA.toDoubleOrNull() ?: 1.0
                                                viewModel.addConicParabola(h, k, a, parabolaIsHorizontal)
                                            }
                                            ConicType.Ellipse -> {
                                                val h = ellipseH.toDoubleOrNull() ?: 0.0
                                                val k = ellipseK.toDoubleOrNull() ?: 0.0
                                                val a = ellipseA.toDoubleOrNull() ?: 5.0
                                                val b = ellipseB.toDoubleOrNull() ?: 3.0
                                                viewModel.addConicEllipse(h, k, a, b)
                                            }
                                            ConicType.Hyperbola -> {
                                                val h = hyperbolaH.toDoubleOrNull() ?: 0.0
                                                val k = hyperbolaK.toDoubleOrNull() ?: 0.0
                                                val a = hyperbolaA.toDoubleOrNull() ?: 3.0
                                                val b = hyperbolaB.toDoubleOrNull() ?: 2.0
                                                viewModel.addConicHyperbola(h, k, a, b, hyperbolaIsHorizontal)
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Icon(imageVector = Icons.Default.ShowChart, contentDescription = "Plot", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Plot Curve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Rename graph text field and Lock switch
                        OutlinedTextField(
                            value = graphNameText,
                            onValueChange = { graphNameText = it },
                            label = { Text("Rename Graph", color = SecondaryTextColor, fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 12.sp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lock Pan & Zoom", color = SecondaryTextColor, fontSize = 12.sp)
                            Switch(
                                checked = isGraphLockedSetting,
                                onCheckedChange = { isGraphLockedSetting = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor)
                            )
                        }

                        // Plotted Expressions list (Function Cards)
                        if (state.expressions.isNotEmpty()) {
                            state.expressions.forEachIndexed { index, expr ->
                                val isSelected = state.selectedExpressionIndex == index
                                val borderWidth by androidx.compose.animation.core.animateDpAsState(targetValue = if (isSelected) 1.dp else 0.dp)
                                val borderColor by androidx.compose.animation.animateColorAsState(targetValue = if (isSelected) PrimaryColor else Color.Transparent)
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = state.expressionColors[index]?.let { androidx.compose.ui.graphics.Color(it) } ?: colors[index % colors.size],
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    viewModel.setSelectedExpressionIndex(index)
                                                    showColorPickerDialog = true
                                                }
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = expr,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        
                                        // Icons: Visibility (Toggle Derivative Plot), Rename, Delete
                                        val hasDerivative = state.plotDerivatives.contains(index)
                                        IconButton(
                                            onClick = { viewModel.toggleDerivativePlot(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (hasDerivative) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle Derivative Plot",
                                                tint = if (hasDerivative) MaterialTheme.colorScheme.primary else TextColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(2.dp))
                                        IconButton(
                                            onClick = { functionToEditIndex = index },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Rename",
                                                tint = TextColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(2.dp))
                                        IconButton(
                                            onClick = { functionToDeleteIndex = index },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = TextColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Clear Button
                        if (state.expressions.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.clear() },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, RedColor)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = RedColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Plotted Curves", color = RedColor, fontSize = 11.sp)
                            }
                        }
                    }

                    // Card 2: Analysis
                    SidebarCard(
                        title = "Analysis",
                        icon = Icons.Default.Info,
                        primaryColor = SecondaryColor,
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "Analysis",
                        onExpand = { expandedSection = "Analysis" }
                    ) {
                        val currentAnalysis = state.equationAnalysis.firstOrNull()
                        if (currentAnalysis != null) {
                            Text("Type: ${currentAnalysis.type.name}", color = SecondaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            // Details map from state analyzer
                            currentAnalysis.details.forEach { (key, valStr) ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = key, color = SecondaryTextColor, fontSize = 11.sp)
                                    Text(text = valStr, color = TextColor, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                                }
                            }

                            if (!currentAnalysis.details.containsKey("Domain")) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Domain", color = SecondaryTextColor, fontSize = 11.sp)
                                    Text(text = "(-∞, ∞)", color = TextColor, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                                }
                            }
                            if (!currentAnalysis.details.containsKey("Range")) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Range", color = SecondaryTextColor, fontSize = 11.sp)
                                    Text(text = "[-10, 10]", color = TextColor, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Increasing", color = SecondaryTextColor, fontSize = 11.sp)
                                Text(
                                    text = if (currentAnalysis.type == RecognizedEquationType.Linear) "(-∞, ∞)" else "[-0.0, ∞)",
                                    color = TextColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Decreasing", color = SecondaryTextColor, fontSize = 11.sp)
                                Text(
                                    text = if (currentAnalysis.type == RecognizedEquationType.Linear) "None" else "(-∞, 0.0]",
                                    color = TextColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Inflection Pt", color = SecondaryTextColor, fontSize = 11.sp)
                                Text(
                                    text = if (currentAnalysis.type == RecognizedEquationType.Trigonometric) "(0.0, 0.0)" else "None",
                                    color = TextColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            Text("Plot a curve to view automatic intelligence analysis details.", color = SecondaryTextColor, fontSize = 11.sp)
                        }
                    }

                    // Card 3: Calculus
                    SidebarCard(
                        title = "Calculus",
                        icon = Icons.Default.ShowChart,
                        primaryColor = OrangeColor,
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "Calculus",
                        onExpand = { expandedSection = "Calculus" }
                    ) {
                        if (state.expressions.isNotEmpty()) {
                            // Derivative curves toggles
                            Text("Plot Derivative Curves", color = SecondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            state.expressions.forEachIndexed { index, expr ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("d/dx ($expr)", color = TextColor, fontSize = 11.sp)
                                    Switch(
                                        checked = state.plotDerivatives.contains(index),
                                        onCheckedChange = { viewModel.toggleDerivativePlot(index) }
                                    )
                                }
                            }

                            // Tangent / Normal details
                            val selPoint = state.selectedPoint
                            val selIndex = state.selectedExpressionIndex
                            if (selPoint != null && selIndex != null && selIndex in state.expressions.indices) {
                                val expr = state.expressions[selIndex]
                                val slope = viewModel.getDerivativeAt(expr, selPoint.first)
                                if (slope != null && !slope.isNaN() && !slope.isInfinite()) {
                                    val c_t = selPoint.second - slope * selPoint.first
                                    val sign_t = if (c_t >= 0.0) "+" else "-"
                                    Text("Tangent: y = ${formatLabel(slope, 1.0)}x $sign_t ${formatLabel(Math.abs(c_t), 1.0)}", color = SecondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                    val nSlope = if (Math.abs(slope) < 1e-9) Double.NaN else -1.0 / slope
                                    if (nSlope.isNaN()) {
                                        Text("Normal: x = ${formatLabel(selPoint.first, 1.0)}", color = RedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        val c_n = selPoint.second - nSlope * selPoint.first
                                        val sign_n = if (c_n >= 0.0) "+" else "-"
                                        Text("Normal: y = ${formatLabel(nSlope, 1.0)}x $sign_n ${formatLabel(Math.abs(c_n), 1.0)}", color = RedColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Integral controls
                            var selectedExprIndexForIntegration by remember { mutableStateOf(0) }
                            var lowerLimitText by remember { mutableStateOf("-2.0") }
                            var upperLimitText by remember { mutableStateOf("2.0") }

                            Text("Integrate Shading (Simpson)", color = SecondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = lowerLimitText,
                                    onValueChange = { lowerLimitText = it },
                                    label = { Text("Lower a", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 11.sp)
                                )
                                OutlinedTextField(
                                    value = upperLimitText,
                                    onValueChange = { upperLimitText = it },
                                    label = { Text("Upper b", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = TextColor, fontSize = 11.sp)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.clearShadedArea() },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Clear", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = {
                                        val a = lowerLimitText.toDoubleOrNull()
                                        val b = upperLimitText.toDoubleOrNull()
                                        if (a != null && b != null) {
                                            viewModel.calculateAndShadeArea(selectedExprIndexForIntegration, a, b)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Integrate", fontSize = 10.sp)
                                }
                            }
                        } else {
                            Text("Plot a Cartesian curve to calculate integrals and derivatives.", color = SecondaryTextColor, fontSize = 11.sp)
                        }
                    }

                    // Card: Symbolic CAS
                    var symbolicResult by remember { mutableStateOf("") }
                    var functionAnalysis by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
                    
                    SidebarCard(
                        title = "Symbolic CAS",
                        icon = Icons.Default.Functions,
                        primaryColor = SecondaryColor,
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "Symbolic",
                        onExpand = { expandedSection = "Symbolic" }
                    ) {
                        val currentExpr = when(selectedGraphType) {
                            GraphType.Cartesian -> expressionText
                            GraphType.Polar -> polarRText
                            GraphType.Parametric -> "$parametricXText, $parametricYText"
                            else -> ""
                        }
                        
                        if (currentExpr.isBlank()) {
                            Text("Enter an expression to use symbolic features.", color = SecondaryTextColor, fontSize = 11.sp)
                        } else {
                            Text("Current: $currentExpr", color = PrimaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val actions = listOf("Simplify", "Expand", "Differentiate", "Integrate", "Analyze")
                                actions.forEach { action ->
                                    AssistChip(
                                        onClick = {
                                            when(action) {
                                                "Simplify" -> symbolicResult = viewModel.symbolicSimplify(currentExpr)
                                                "Expand" -> symbolicResult = viewModel.symbolicExpand(currentExpr)
                                                "Differentiate" -> symbolicResult = "dy/dx = " + viewModel.symbolicDifferentiate(currentExpr)
                                                "Integrate" -> symbolicResult = "∫ f(x) dx = " + viewModel.symbolicIntegrate(currentExpr)
                                                "Analyze" -> functionAnalysis = viewModel.analyzeFunctionProperties(currentExpr)
                                            }
                                        },
                                        label = { Text(action, fontSize = 10.sp) },
                                        colors = AssistChipDefaults.assistChipColors(labelColor = SecondaryColor)
                                    )
                                }
                            }
                            
                            if (symbolicResult.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = BackgroundColor.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Result:", color = SecondaryTextColor, fontSize = 10.sp)
                                        Text(symbolicResult, color = TextColor, fontSize = 13.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                        TextButton(onClick = { 
                                            if (symbolicResult.contains("=")) {
                                                expressionText = symbolicResult.substringAfter("=").trim()
                                            } else {
                                                expressionText = symbolicResult
                                            }
                                            selectedGraphType = GraphType.Cartesian
                                        }) {
                                            Text("Apply to Plot", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                            
                            if (functionAnalysis.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Analysis:", color = SecondaryTextColor, fontSize = 10.sp)
                                functionAnalysis.forEach { (k, v) ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(k, color = SecondaryTextColor, fontSize = 11.sp)
                                        Text(v, color = TextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    SidebarCard(
                        title = "Animation & Visuals",
                        icon = Icons.Default.Timeline,
                        primaryColor = OrangeColor,
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "Animation",
                        onExpand = { expandedSection = "Animation" }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleAnimation() }) {
                                Icon(
                                    imageVector = if (state.isAnimating) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle Animation",
                                    tint = OrangeColor
                                )
                            }
                            
                            Slider(
                                value = state.animationSpeed,
                                onValueChange = { viewModel.setAnimationSpeed(it) },
                                valueRange = 0.1f..5f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = OrangeColor, activeTrackColor = OrangeColor)
                            )
                            Text("Speed: ${String.format("%.1fx", state.animationSpeed)}", color = TextColor, fontSize = 10.sp)
                        }
                        
                        Text("Dynamic Parameters:", color = SecondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        listOf("a", "b", "c", "k").forEach { param ->
                            val value = state.animatingParams[param] ?: 1.0
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Parameter $param", color = SecondaryTextColor, fontSize = 11.sp)
                                    Text(String.format("%.2f", value), color = OrangeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = value.toFloat(),
                                    onValueChange = { viewModel.updateParam(param, it.toDouble()) },
                                    valueRange = -10f..10f,
                                    colors = SliderDefaults.colors(thumbColor = OrangeColor, activeTrackColor = OrangeColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Visualizations:", color = SecondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showTangent, onCheckedChange = { showTangent = it })
                            Text("Tangent Line", color = TextColor, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showNormal, onCheckedChange = { showNormal = it })
                            Text("Normal Line", color = TextColor, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showRiemann, onCheckedChange = { showRiemann = it })
                            Text("Riemann Sums", color = TextColor, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showAnimatedPoint, onCheckedChange = { showAnimatedPoint = it })
                            Text("Animated Point", color = TextColor, fontSize = 11.sp)
                        }

                        Text("Presets:", color = SecondaryTextColor, fontSize = 10.sp)
                        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("a*sin(b*x+c)+k", "a*x^2+b*x+c", "a*exp(b*x)+c").forEach { preset ->
                                AssistChip(
                                    onClick = { 
                                        expressionText = preset
                                        viewModel.addExpression(preset)
                                    },
                                    label = { Text(preset, fontSize = 9.sp) }
                                )
                            }
                        }
                    }

                    // Card: Geometry
                    SidebarCard(
                        title = "Geometry",
                        icon = Icons.Outlined.FormatShapes,
                        primaryColor = PrimaryColor,
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "Geometry",
                        onExpand = { expandedSection = "Geometry" }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = state.isGeometryMode, onCheckedChange = { viewModel.toggleGeometryMode() })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enable Geometry Mode", color = TextColor, fontSize = 11.sp)
                        }
                        
                        if (state.isGeometryMode) {
                            GeometryPanel(
                                state = state,
                                viewModel = viewModel,
                                primaryColor = PrimaryColor,
                                textColor = TextColor,
                                secondaryTextColor = SecondaryTextColor
                            )
                        }
                    }

                    // Card: Saved Formulas
                    SidebarCard(
                        title = "Saved Formulas",
                        icon = Icons.Default.Save,
                        primaryColor = BlueColor,
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "Saved",
                        onExpand = { expandedSection = "Saved" }
                    ) {
                        if (state.savedFormulas.isEmpty()) {
                            Text("No saved formulas.", color = SecondaryTextColor, fontSize = 11.sp)
                        } else {
                            state.savedFormulas.forEach { formula ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expressionText = formula; selectedGraphType = GraphType.Cartesian }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(formula, color = TextColor, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.deleteSavedFormula(formula) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedColor, modifier = Modifier.size(16.dp))
                                    }
                                }
                                HorizontalDivider(color = GridColor)
                            }
                        }
                    }

                    // Card 4: History
                    SidebarCard(
                        title = "History",
                        icon = Icons.Default.Refresh,
                        primaryColor = BlueColor,
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "History",
                        onExpand = { expandedSection = "History" }
                    ) {
                        if (state.history.isNotEmpty()) {
                            state.history.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.loadHistory(item) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.equation, color = TextColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(text = item.type, color = SecondaryTextColor, fontSize = 9.sp)
                                    }
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Load", tint = SecondaryColor, modifier = Modifier.size(16.dp))
                                }
                                HorizontalDivider(color = Color(1.0f, 1.0f, 1.0f, 0.05f))
                            }
                        } else {
                            Text("No recent history saved yet.", color = SecondaryTextColor, fontSize = 11.sp)
                        }
                    }

                    // Card: 3D Graphing
                    SidebarCard(
                        title = "3D Graphing",
                        icon = Icons.Default.ShowChart, // Using ShowChart as a fallback, or ViewInAr if imported
                        primaryColor = Color(0xFF9C27B0),
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "3D",
                        onExpand = { expandedSection = "3D" }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = state.is3DMode, onCheckedChange = { viewModel.toggle3DMode() })
                            Text("Enable 3D Mode", color = TextColor, fontSize = 11.sp)
                        }
                        
                        if (state.is3DMode) {
                            OutlinedTextField(
                                value = state.zExpr,
                                onValueChange = { viewModel.updateZExpr(it) },
                                label = { Text("z = f(x,y)", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 12.sp, color = TextColor),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    unfocusedBorderColor = GridColor
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Render Mode:", color = SecondaryTextColor, fontSize = 10.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                RenderMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = state.renderMode == mode,
                                        onClick = { viewModel.setRenderMode(mode) },
                                        label = { Text(mode.name, fontSize = 9.sp) }
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = state.surfaceColorGradient, onCheckedChange = { viewModel.toggleSurfaceGradient() })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Height Gradient", color = TextColor, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Camera Tilt: ${state.cameraRotationX.toInt()}°", color = SecondaryTextColor, fontSize = 9.sp)
                            Slider(
                                value = state.cameraRotationX,
                                onValueChange = { viewModel.updateCamera(it - state.cameraRotationX, 0f) },
                                valueRange = 0f..90f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9C27B0), activeTrackColor = Color(0xFF9C27B0))
                            )
                            
                            Text("Camera Rotation: ${state.cameraRotationZ.toInt()}°", color = SecondaryTextColor, fontSize = 9.sp)
                            Slider(
                                value = state.cameraRotationZ,
                                onValueChange = { viewModel.updateCamera(0f, it - state.cameraRotationZ) },
                                valueRange = 0f..360f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9C27B0), activeTrackColor = Color(0xFF9C27B0))
                            )

                            Text("Zoom: ${String.format(java.util.Locale.US, "%.1f", state.cameraZoom)}x", color = SecondaryTextColor, fontSize = 9.sp)
                            Slider(
                                value = state.cameraZoom,
                                onValueChange = { viewModel.setCameraZoom(it) },
                                valueRange = 0.5f..5.0f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9C27B0), activeTrackColor = Color(0xFF9C27B0))
                            )
                            
                            Text("Camera controls: Drag on 3D graph to rotate.", color = SecondaryTextColor, fontSize = 9.sp)
                            Button(
                                onClick = { 
                                    viewModel.updateCamera(-state.cameraRotationX + 30f, -state.cameraRotationZ + 45f)
                                    viewModel.setCameraZoom(1.0f)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                            ) {
                                Text("Reset View", fontSize = 11.sp)
                            }
                        }
                    }

                    // Card 5: Graph Settings
                    SidebarCard(
                        title = "Settings",
                        icon = Icons.Default.Settings,
                        primaryColor = YellowColor,
                        textColor = TextColor,
                        cardColor = CardColor,
                        expanded = expandedSection == "Settings",
                        onExpand = { expandedSection = "Settings" }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Show Grid Lines", color = SecondaryTextColor, fontSize = 11.sp)
                            Switch(checked = showGridSetting, onCheckedChange = { showGridSetting = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Show Axes", color = SecondaryTextColor, fontSize = 11.sp)
                            Switch(checked = showAxisSetting, onCheckedChange = { showAxisSetting = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Show Axis Labels", color = SecondaryTextColor, fontSize = 11.sp)
                            Switch(checked = showLabelsSetting, onCheckedChange = { showLabelsSetting = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Trace Cursor Points", color = SecondaryTextColor, fontSize = 11.sp)
                            Switch(checked = isTraceMode, onCheckedChange = { isTraceMode = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Snap to Grid Coordinate", color = SecondaryTextColor, fontSize = 11.sp)
                            Switch(checked = isSnapToGridSetting, onCheckedChange = { isSnapToGridSetting = it })
                        }
                    }
                }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Plot Area Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    )
                ) {
                    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    val primaryColor = MaterialTheme.colorScheme.primary

                    val density = LocalDensity.current
                    val tickStrokePx = with(density) { 1.5.dp.toPx() }
                    val axisStrokePx = with(density) { 2.dp.toPx() }
                    val curveStrokePx = with(density) { 3.dp.toPx() }
                    val dotOuterRadiusPx = with(density) { 5.dp.toPx() }
                    val dotInnerRadiusPx = with(density) { 2.dp.toPx() }
                    val traceLineStrokePx = with(density) { 1.dp.toPx() }
                    val traceOuterRadiusPx = with(density) { 12.dp.toPx() }
                    val traceInnerRadiusPx = with(density) { 6.dp.toPx() }

                    val portraitCurvesPaths = remember(state.points, state.viewport, canvasWidth, canvasHeight) {
                        val vp = state.viewport
                        val rangeX = if (vp.maxX - vp.minX == 0.0) 1.0 else vp.maxX - vp.minX
                        val rangeY = if (vp.maxY - vp.minY == 0.0) 1.0 else vp.maxY - vp.minY
                        state.points.map { pointsList ->
                            val path = Path()
                            if (pointsList.isNotEmpty()) {
                                var isFirst = true
                                var prevY: Double? = null
                                pointsList.forEach { (x, y) ->
                                    val px = ((x - vp.minX) / rangeX * canvasWidth).toFloat()
                                    val py = (canvasHeight - (y - vp.minY) / rangeY * canvasHeight).toFloat()
                                    val viewportHeight = vp.maxY - vp.minY
                                    val isDiscontinuity = prevY != null && !y.isNaN() && !prevY!!.isNaN() && Math.abs(y - prevY!!) > 5.0 * viewportHeight

                                    if (px.isFinite() && py.isFinite() && py in -canvasHeight..2*canvasHeight && !isDiscontinuity) {
                                        if (isFirst) {
                                            path.moveTo(px, py)
                                            isFirst = false
                                        } else {
                                            path.lineTo(px, py)
                                        }
                                    } else {
                                        isFirst = true
                                    }
                                    prevY = if (px.isFinite() && py.isFinite() && !y.isNaN()) y else null
                                }
                            }
                            path
                        }
                    }

                    val portraitDerivativePaths = remember(state.derivativePoints, state.viewport, canvasWidth, canvasHeight) {
                        val vp = state.viewport
                        val rangeX = if (vp.maxX - vp.minX == 0.0) 1.0 else vp.maxX - vp.minX
                        val rangeY = if (vp.maxY - vp.minY == 0.0) 1.0 else vp.maxY - vp.minY
                        state.derivativePoints.mapValues { (_, pointsList) ->
                            val path = Path()
                            if (pointsList.isNotEmpty()) {
                                var isFirst = true
                                var prevY: Double? = null
                                pointsList.forEach { (x, y) ->
                                    val px = ((x - vp.minX) / rangeX * canvasWidth).toFloat()
                                    val py = (canvasHeight - (y - vp.minY) / rangeY * canvasHeight).toFloat()
                                    val viewportHeight = vp.maxY - vp.minY
                                    val isDiscontinuity = prevY != null && !y.isNaN() && !prevY!!.isNaN() && Math.abs(y - prevY!!) > 5.0 * viewportHeight

                                    if (px.isFinite() && py.isFinite() && py in -canvasHeight..2*canvasHeight && !isDiscontinuity) {
                                        if (isFirst) {
                                            path.moveTo(px, py)
                                            isFirst = false
                                        } else {
                                            path.lineTo(px, py)
                                        }
                                    } else {
                                        isFirst = true
                                    }
                                    prevY = if (px.isFinite() && py.isFinite() && !y.isNaN()) y else null
                                }
                            }
                            path
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .testTag("graph_plotter_canvas")
                            .onSizeChanged { size ->
                                if (size.width > 0 && size.height > 0) {
                                    canvasWidth = size.width.toFloat()
                                    canvasHeight = size.height.toFloat()
                                }
                            }
                            .pointerInput(canvasWidth, canvasHeight, isTraceMode, state.isGeometryMode) {
                                if (state.isGeometryMode) {
                                    detectTapGestures { offset ->
                                        viewModel.onGeometryCanvasClick(offset.x.toDouble(), offset.y.toDouble(), canvasWidth, canvasHeight)
                                    }
                                } else if (isTraceMode) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            viewModel.selectNearestPoint(offset.x, offset.y, canvasWidth, canvasHeight)
                                        },
                                        onDrag = { change, _ ->
                                            viewModel.selectNearestPoint(change.position.x, change.position.y, canvasWidth, canvasHeight)
                                        },
                                        onDragEnd = {}
                                    )
                                } else {
                                    detectTransformGestures { _, panDelta, zoomFactor, _ ->
                                        if (zoomFactor != 1f) {
                                            viewModel.zoom(zoomFactor.toDouble())
                                        }
                                        if (panDelta.x != 0f || panDelta.y != 0f) {
                                            val vp = state.viewport
                                            val unitX = (vp.maxX - vp.minX) / canvasWidth
                                            val unitY = (vp.maxY - vp.minY) / canvasHeight
                                            val deltaX = - panDelta.x.toDouble() * unitX
                                            val deltaY = panDelta.y.toDouble() * unitY
                                            viewModel.pan(deltaX, deltaY)
                                        }
                                    }
                                }
                            }
                    ) {
                        val W = size.width
                        val H = size.height

                        val minX = state.viewport.minX
                        val maxX = state.viewport.maxX
                        val minY = state.viewport.minY
                        val maxY = state.viewport.maxY

                        fun toPxX(x: Double): Float = ((x - minX) / (maxX - minX) * W).toFloat()
                        fun toPxY(y: Double): Float = (H - (y - minY) / (maxY - minY) * H).toFloat()

                        // Draw Geometry Objects
                        drawGeometryObjects(this, state, ::toPxX, ::toPxY)

                        // Calculate step size for the dynamic grid
                        val rangeX = maxX - minX
                        val rawStep = rangeX / 10.0
                        val log10 = Math.log10(rawStep)
                        val powerOf10 = Math.pow(10.0, Math.floor(log10))
                        val ratio = rawStep / powerOf10
                        val gridStep = when {
                            ratio < 1.5 -> powerOf10
                            ratio < 3.5 -> 2.0 * powerOf10
                            ratio < 7.5 -> 5.0 * powerOf10
                            else -> 10.0 * powerOf10
                        }
                        val safeGridStep = if (gridStep <= 0.0) 1.0 else gridStep

                        val startX = Math.floor(minX / safeGridStep) * safeGridStep
                        val endX = Math.ceil(maxX / safeGridStep) * safeGridStep
                        val startY = Math.floor(minY / safeGridStep) * safeGridStep
                        val endY = Math.ceil(maxY / safeGridStep) * safeGridStep

                        // Draw Grid Lines
                        var currX = startX
                        while (currX <= endX) {
                            if (Math.abs(currX) > 1e-9) {
                                val px = toPxX(currX)
                                drawLine(
                                    color = gridColor,
                                    start = androidx.compose.ui.geometry.Offset(px, 0f),
                                    end = androidx.compose.ui.geometry.Offset(px, H),
                                    strokeWidth = 1f
                                )
                            }
                            currX += safeGridStep
                        }

                        var currY = startY
                        while (currY <= endY) {
                            if (Math.abs(currY) > 1e-9) {
                                val py = toPxY(currY)
                                drawLine(
                                    color = gridColor,
                                    start = androidx.compose.ui.geometry.Offset(0f, py),
                                    end = androidx.compose.ui.geometry.Offset(W, py),
                                    strokeWidth = 1f
                                )
                            }
                            currY += safeGridStep
                        }

                        // Dynamic axes and labels positioning
                        val rawXAxisY = toPxY(0.0)
                        val rawYAxisX = toPxX(0.0)

                        val xAxisY = rawXAxisY.coerceIn(0f, H)
                        val yAxisX = rawYAxisX.coerceIn(0f, W)

                        // Draw X-Axis
                        drawLine(
                            color = axisColor,
                            start = androidx.compose.ui.geometry.Offset(0f, xAxisY),
                            end = androidx.compose.ui.geometry.Offset(W, xAxisY),
                            strokeWidth = axisStrokePx
                        )
                        // Draw Y-Axis
                        drawLine(
                            color = axisColor,
                            start = androidx.compose.ui.geometry.Offset(yAxisX, 0f),
                            end = androidx.compose.ui.geometry.Offset(yAxisX, H),
                            strokeWidth = axisStrokePx
                        )

                        // Labels and Ticks
                        val paint = android.graphics.Paint().apply {
                            color = textColor.toArgb()
                            textSize = 28f
                            isAntiAlias = true
                        }

                        // X ticks and labels
                        var tickX = startX
                        while (tickX <= endX) {
                            if (Math.abs(tickX) > 1e-9) {
                                val px = toPxX(tickX)
                                if (px in 0f..W) {
                                    drawLine(
                                        color = axisColor,
                                        start = androidx.compose.ui.geometry.Offset(px, xAxisY - 6f),
                                        end = androidx.compose.ui.geometry.Offset(px, xAxisY + 6f),
                                        strokeWidth = tickStrokePx
                                    )
                                    val label = formatLabel(tickX, safeGridStep)
                                    val labelY = if (xAxisY + 34f > H - 10f) H - 10f else if (xAxisY + 34f < 40f) 40f else xAxisY + 34f
                                    drawContext.canvas.nativeCanvas.drawText(
                                        label,
                                        px,
                                        labelY,
                                        paint.apply { textAlign = android.graphics.Paint.Align.CENTER }
                                    )
                                }
                            }
                            tickX += safeGridStep
                        }

                        // Y ticks and labels
                        var tickY = startY
                        while (tickY <= endY) {
                            if (Math.abs(tickY) > 1e-9) {
                                val py = toPxY(tickY)
                                if (py in 0f..H) {
                                    drawLine(
                                        color = axisColor,
                                        start = androidx.compose.ui.geometry.Offset(yAxisX - 6f, py),
                                        end = androidx.compose.ui.geometry.Offset(yAxisX + 6f, py),
                                        strokeWidth = tickStrokePx
                                    )
                                    val label = formatLabel(tickY, safeGridStep)
                                    val labelX = if (yAxisX + 16f > W - 60f) yAxisX - 16f else yAxisX + 16f
                                    val align = if (yAxisX + 16f > W - 60f) android.graphics.Paint.Align.RIGHT else android.graphics.Paint.Align.LEFT
                                    drawContext.canvas.nativeCanvas.drawText(
                                        label,
                                        labelX,
                                        py + 10f,
                                        paint.apply { textAlign = align }
                                    )
                                }
                            }
                            tickY += safeGridStep
                        }

                        // Draw Origin 0 if visible
                        if (rawYAxisX in 0f..W && rawXAxisY in 0f..H) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "0",
                                yAxisX - 16f,
                                xAxisY + 30f,
                                paint.apply { textAlign = android.graphics.Paint.Align.RIGHT }
                            )
                        }

                        // Draw Shaded Area for Numerical Integration
                        state.shadedIntegration?.let { info ->
                            if (info.expressionIndex in state.expressions.indices) {
                                val expr = state.expressions[info.expressionIndex]
                                val shadePath = Path()
                                val steps = 200
                                val step = (info.b - info.a) / steps
                                val startPxX = toPxX(info.a)
                                val zeroPxY = toPxY(0.0)

                                shadePath.moveTo(startPxX, zeroPxY)

                                for (i in 0..steps) {
                                    val currX = info.a + i * step
                                    val currY = viewModel.evaluateExpression(expr, currX)
                                    if (currY != null && currY.isFinite()) {
                                        val px = toPxX(currX)
                                        val py = toPxY(currY)
                                        shadePath.lineTo(px, py)
                                    }
                                }

                                val endPxX = toPxX(info.b)
                                shadePath.lineTo(endPxX, zeroPxY)
                                shadePath.close()

                                drawPath(
                                    path = shadePath,
                                    color = (state.expressionColors[info.expressionIndex]?.let { androidx.compose.ui.graphics.Color(it) } ?: colors[info.expressionIndex % colors.size]).copy(alpha = 0.2f),
                                    style = androidx.compose.ui.graphics.drawscope.Fill
                                )
                            }
                        }

                        // Draw Plotted Curves
                        portraitCurvesPaths.forEachIndexed { index, path ->
                            if (!path.isEmpty) {
                                val color = state.expressionColors[index]?.let { androidx.compose.ui.graphics.Color(it) } ?: colors[index % colors.size]
                                drawPath(
                                    path = path,
                                    color = color,
                                    style = Stroke(
                                        width = curveStrokePx,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Draw Derivative Curves f'(x)
                        state.derivativePoints.forEach { (index, _) ->
                            val path = portraitDerivativePaths[index]
                            if (path != null && !path.isEmpty) {
                                val color = state.expressionColors[index]?.let { androidx.compose.ui.graphics.Color(it) } ?: colors[index % colors.size]
                                drawPath(
                                    path = path,
                                    color = color.copy(alpha = 0.6f),
                                    style = Stroke(
                                        width = curveStrokePx * 0.7f,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round,
                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                                    )
                                )
                            }
                        }

                        // Draw Roots
                        state.roots.forEach { rx ->
                            val px = toPxX(rx)
                            val py = toPxY(0.0)
                            if (px in 0f..W && py in 0f..H) {
                                drawCircle(
                                    color = Color(0xFFE53935),
                                    radius = dotOuterRadiusPx,
                                    center = androidx.compose.ui.geometry.Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = dotInnerRadiusPx,
                                    center = androidx.compose.ui.geometry.Offset(px, py)
                                )
                            }
                        }

                        // Draw Extrema
                        state.extrema.forEach { ep ->
                            val px = toPxX(ep.x)
                            val py = toPxY(ep.y)
                            if (px in 0f..W && py in 0f..H) {
                                val color = if (ep.isMaximum) Color(0xFF4CAF50) else Color(0xFF9C27B0)
                                drawCircle(
                                    color = color,
                                    radius = dotOuterRadiusPx,
                                    center = androidx.compose.ui.geometry.Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = dotInnerRadiusPx,
                                    center = androidx.compose.ui.geometry.Offset(px, py)
                                )
                            }
                        }

                        // Draw Intersections between multiple curves
                        state.intersections.forEach { (ix, iy) ->
                            val px = toPxX(ix)
                            val py = toPxY(iy)
                            if (px in 0f..W && py in 0f..H) {
                                drawCircle(
                                    color = Color(0xFFFF9800), // Orange for intersections
                                    radius = dotOuterRadiusPx * 1.1f,
                                    center = androidx.compose.ui.geometry.Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = dotInnerRadiusPx,
                                    center = androidx.compose.ui.geometry.Offset(px, py)
                                )
                            }
                        }

                        // Advanced Visualizations: Riemann Sums, Tangent, Normal, and Animated Point
                        val firstCartesian = state.graphs.filterIsInstance<PlottedGraph.Cartesian>().firstOrNull()
                        if (firstCartesian != null) {
                            val expr = firstCartesian.expr
                            
                            // Riemann Sums
                            if (showRiemann) {
                                val n = 20
                                val dx = (maxX - minX) / n
                                for (i in 0 until n) {
                                    val rx = minX + i * dx + dx / 2.0
                                    val ry = viewModel.evaluateExpression(expr, rx) ?: 0.0
                                    val rpx1 = toPxX(minX + i * dx)
                                    val rpx2 = toPxX(minX + (i + 1) * dx)
                                    val rpy = toPxY(ry)
                                    val zeroY = toPxY(0.0)
                                    
                                    drawRect(
                                        color = Color.Cyan.copy(alpha = 0.2f),
                                        topLeft = androidx.compose.ui.geometry.Offset(rpx1, Math.min(rpy, zeroY)),
                                        size = androidx.compose.ui.geometry.Size(rpx2 - rpx1, Math.abs(rpy - zeroY))
                                    )
                                    drawRect(
                                        color = Color.Cyan.copy(alpha = 0.5f),
                                        topLeft = androidx.compose.ui.geometry.Offset(rpx1, Math.min(rpy, zeroY)),
                                        size = androidx.compose.ui.geometry.Size(rpx2 - rpx1, Math.abs(rpy - zeroY)),
                                        style = Stroke(width = 1f)
                                    )
                                }
                            }

                            // Animated Point Visuals
                            val tLoop = (state.animationTime % 10f) / 10f
                            val animX = minX + tLoop * (maxX - minX)
                            val animY = viewModel.evaluateExpression(expr, animX)
                            
                            if (animY != null && animY.isFinite()) {
                                val apx = toPxX(animX)
                                val apy = toPxY(animY)
                                
                                if (apx in 0f..W && apy in 0f..H) {
                                    if (showAnimatedPoint && state.isAnimating) {
                                        drawCircle(
                                            color = Color.White,
                                            radius = 10f,
                                            center = androidx.compose.ui.geometry.Offset(apx, apy)
                                        )
                                        drawCircle(
                                            color = Color(0xFFFF9800),
                                            radius = 7f,
                                            center = androidx.compose.ui.geometry.Offset(apx, apy)
                                        )
                                    }

                                    val slope = viewModel.getDerivativeAt(expr, animX)
                                    if (slope != null && slope.isFinite()) {
                                        if (showTangent) {
                                            val tx1 = minX
                                            val ty1 = slope * (tx1 - animX) + animY
                                            val tx2 = maxX
                                            val ty2 = slope * (tx2 - animX) + animY
                                            drawLine(
                                                color = Color.Green,
                                                start = androidx.compose.ui.geometry.Offset(toPxX(tx1), toPxY(ty1)),
                                                end = androidx.compose.ui.geometry.Offset(toPxX(tx2), toPxY(ty2)),
                                                strokeWidth = 2f,
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                            )
                                        }
                                        if (showNormal) {
                                            val nSlope = if (Math.abs(slope) < 1e-9) 1e9 else -1.0 / slope
                                            val ny1 = nSlope * (minX - animX) + animY
                                            val ny2 = nSlope * (maxX - animX) + animY
                                            drawLine(
                                                color = Color.Magenta,
                                                start = androidx.compose.ui.geometry.Offset(toPxX(minX), toPxY(ny1)),
                                                end = androidx.compose.ui.geometry.Offset(toPxX(maxX), toPxY(ny2)),
                                                strokeWidth = 2f,
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Draw Traced Coordinate / Selected Point
                        state.selectedPoint?.let { (selX, selY) ->
                            val px = toPxX(selX)
                            val py = toPxY(selY)
                            if (px in 0f..W && py in 0f..H) {
                                val xAxisYPx = xAxisY
                                val yAxisXPx = yAxisX

                                drawLine(
                                    color = primaryColor.copy(alpha = 0.4f),
                                    start = androidx.compose.ui.geometry.Offset(px, py),
                                    end = androidx.compose.ui.geometry.Offset(px, xAxisYPx),
                                    strokeWidth = traceLineStrokePx,
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.4f),
                                    start = androidx.compose.ui.geometry.Offset(px, py),
                                    end = androidx.compose.ui.geometry.Offset(yAxisXPx, py),
                                    strokeWidth = traceLineStrokePx,
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )

                                // Draw tangent and normal lines on the graph canvas if an expression is selected
                                val selExpr = state.selectedExpressionIndex?.let { state.expressions.getOrNull(it) }
                                if (selExpr != null) {
                                    val slope = viewModel.getDerivativeAt(selExpr, selX)
                                    if (slope != null && !slope.isNaN() && !slope.isInfinite()) {
                                        // Tangent Line
                                        val txStart = state.viewport.minX
                                        val tyStart = slope * (txStart - selX) + selY
                                        val txEnd = state.viewport.maxX
                                        val tyEnd = slope * (txEnd - selX) + selY

                                        drawLine(
                                            color = Color(0xFF4CAF50), // Green for Tangent
                                            start = androidx.compose.ui.geometry.Offset(toPxX(txStart), toPxY(tyStart)),
                                            end = androidx.compose.ui.geometry.Offset(toPxX(txEnd), toPxY(tyEnd)),
                                            strokeWidth = traceLineStrokePx * 1.5f,
                                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                        )

                                        // Normal Line
                                        if (Math.abs(slope) < 1e-9) {
                                            drawLine(
                                                color = Color(0xFFE91E63), // Pink for Normal
                                                start = androidx.compose.ui.geometry.Offset(toPxX(selX), 0f),
                                                end = androidx.compose.ui.geometry.Offset(toPxX(selX), H),
                                                strokeWidth = traceLineStrokePx * 1.5f,
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                            )
                                        } else {
                                            val nSlope = -1.0 / slope
                                            val nyStart = nSlope * (txStart - selX) + selY
                                            val nyEnd = nSlope * (txEnd - selX) + selY

                                            drawLine(
                                                color = Color(0xFFE91E63), // Pink for Normal
                                                start = androidx.compose.ui.geometry.Offset(toPxX(txStart), toPxY(nyStart)),
                                                end = androidx.compose.ui.geometry.Offset(toPxX(txEnd), toPxY(nyEnd)),
                                                strokeWidth = traceLineStrokePx * 1.5f,
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                            )
                                        }
                                    }
                                }

                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.3f),
                                    radius = traceOuterRadiusPx,
                                    center = androidx.compose.ui.geometry.Offset(px, py)
                                )
                                drawCircle(
                                    color = primaryColor,
                                    radius = traceInnerRadiusPx,
                                    center = androidx.compose.ui.geometry.Offset(px, py)
                                )

                                val label = "(${formatLabel(selX, 1.0)}, ${formatLabel(selY, 1.0)})"
                                val labelPaint = android.graphics.Paint().apply {
                                    color = textColor.toArgb()
                                    textSize = 32f
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    isAntiAlias = true
                                }
                                val textY = if (py - 16f < 30f) py + 40f else py - 16f
                                val textX = if (px + 16f > W - 180f) px - 220f else px + 16f
                                drawContext.canvas.nativeCanvas.drawText(
                                    label,
                                    textX,
                                    textY,
                                    labelPaint
                                )
                            }
                        }
                    }
                }

                // Floating Toolbar (Placed here to be on top of Canvas)
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.zoomIn() },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Default.Add, "Zoom In", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) }
                        
                        IconButton(
                            onClick = { viewModel.zoomOut() },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Default.Remove, "Zoom Out", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) }
                        
                        IconButton(
                            onClick = { viewModel.resetViewport() },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Default.ZoomOutMap, "Fit All", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) }
                        
                        IconButton(
                            onClick = { viewModel.resetViewport() },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Default.Refresh, "Reset View", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) }
                        
                        IconButton(
                            onClick = { isTraceMode = !isTraceMode },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Default.Timeline, "Trace", tint = if (isTraceMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) }
                        
                        IconButton(
                            onClick = { /* Fullscreen toggle */ },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Default.Fullscreen, "Fullscreen", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) }
                    }
                }
            }

            // Mode Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTraceMode) "Mode: Coordinate Tracer" else "Mode: Zoom & Pan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trace Mode", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isTraceMode,
                        onCheckedChange = { isTraceMode = it },
                        modifier = Modifier.testTag("trace_mode_switch")
                    )
                }
            }

            // Curve Analysis Card
            if (state.expressions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "JEE Advanced Curve Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Roots Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFFE53935), CircleShape)
                            )
                            Text(
                                text = "Approximate Roots (x-intercepts):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.roots.isEmpty()) {
                             Text(
                                 text = "No roots found in current viewport range",
                                 style = MaterialTheme.typography.bodyMedium,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                                 modifier = Modifier.padding(start = 20.dp)
                             )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.roots.forEach { rx ->
                                    AssistChip(
                                        onClick = {
                                            val px = ((rx - state.viewport.minX) / (state.viewport.maxX - state.viewport.minX) * canvasWidth).toFloat()
                                            val py = (canvasHeight - (0.0 - state.viewport.minY) / (state.viewport.maxY - state.viewport.minY) * canvasHeight).toFloat()
                                            viewModel.selectNearestPoint(px, py, canvasWidth, canvasHeight)
                                        },
                                        label = { Text("x = ${formatLabel(rx, 1.0)}") },
                                        modifier = Modifier.testTag("root_chip_${formatLabel(rx, 1.0)}")
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Extrema Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFF9C27B0), CircleShape)
                            )
                            Text(
                                text = "Local Extrema (Maxima / Minima):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.extrema.isEmpty()) {
                            Text(
                                text = "No local extrema found in current viewport range",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.padding(start = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.extrema.forEach { ep ->
                                    val type = if (ep.isMaximum) "Local Max" else "Local Min"
                                    val typeColor = if (ep.isMaximum) Color(0xFF4CAF50) else Color(0xFF9C27B0)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.clickable {
                                            val px = ((ep.x - state.viewport.minX) / (state.viewport.maxX - state.viewport.minX) * canvasWidth).toFloat()
                                            val py = (canvasHeight - (ep.y - state.viewport.minY) / (state.viewport.maxY - state.viewport.minY) * canvasHeight).toFloat()
                                            viewModel.selectNearestPoint(px, py, canvasWidth, canvasHeight)
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(typeColor, CircleShape)
                                        )
                                        Text(
                                            text = "$type at (${formatLabel(ep.x, 1.0)}, ${formatLabel(ep.y, 1.0)})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Intersections Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFFFF9800), CircleShape)
                            )
                            Text(
                                text = "Graph Intersections:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.intersections.isEmpty()) {
                            Text(
                                text = "No intersections found in current range",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.intersections.forEach { (ix, iy) ->
                                    AssistChip(
                                        onClick = {
                                            val px = ((ix - state.viewport.minX) / (state.viewport.maxX - state.viewport.minX) * canvasWidth).toFloat()
                                            val py = (canvasHeight - (iy - state.viewport.minY) / (state.viewport.maxY - state.viewport.minY) * canvasHeight).toFloat()
                                            viewModel.selectNearestPoint(px, py, canvasWidth, canvasHeight)
                                        },
                                        label = { Text("(${formatLabel(ix, 1.0)}, ${formatLabel(iy, 1.0)})") },
                                        modifier = Modifier.testTag("intersection_chip_${formatLabel(ix, 1.0)}")
                                    )
                                }
                            }
                        }

                        // Tangent & Normal Display at selected point
                        val selPoint = state.selectedPoint
                        val selIndex = state.selectedExpressionIndex
                        if (selPoint != null && selIndex != null && selIndex in state.expressions.indices) {
                            val expr = state.expressions[selIndex]
                            val selX = selPoint.first
                            val selY = selPoint.second
                            val slope = viewModel.getDerivativeAt(expr, selX)

                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                )
                                Text(
                                    text = "Tangent & Normal Analyzer:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                modifier = Modifier.padding(start = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Point of Contact: P(${formatLabel(selX, 1.0)}, ${formatLabel(selY, 1.0)})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (slope != null && !slope.isNaN() && !slope.isInfinite()) {
                                    val c_t = selY - slope * selX
                                    val sign_t = if (c_t >= 0.0) "+" else "-"
                                    val abs_c_t = Math.abs(c_t)
                                    
                                    Text(
                                        text = "Tangent Slope (m): ${formatLabel(slope, 1.0)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        text = "Tangent: y = ${formatLabel(slope, 1.0)}x $sign_t ${formatLabel(abs_c_t, 1.0)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF4CAF50)
                                    )

                                    if (Math.abs(slope) < 1e-9) {
                                        Text(
                                            text = "Normal: x = ${formatLabel(selX, 1.0)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFE91E63)
                                        )
                                    } else {
                                        val nSlope = -1.0 / slope
                                        val c_n = selY - nSlope * selX
                                        val sign_n = if (c_n >= 0.0) "+" else "-"
                                        val abs_c_n = Math.abs(c_n)
                                        Text(
                                            text = "Normal: y = ${formatLabel(nSlope, 1.0)}x $sign_n ${formatLabel(abs_c_n, 1.0)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFE91E63)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Derivative undefined here",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Plotted Expressions list (Function Cards)
            if (state.expressions.isNotEmpty()) {
                state.expressions.forEachIndexed { index, expr ->
                    val isSelected = state.selectedExpressionIndex == index
                    val borderWidth by androidx.compose.animation.core.animateDpAsState(targetValue = if (isSelected) 2.dp else 0.dp)
                    val borderColor by androidx.compose.animation.animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = borderWidth,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = colors[index % colors.size],
                                        shape = CircleShape
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "y = $expr",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Visibility Toggle (Placeholder - need actual logic)
                            IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Add, "Toggle Visibility") }
                            IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Settings, "Rename") }
                            IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Delete, "Delete") }
                            IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Settings, "Settings") }
                        }
                    }
                }
            }

            // Definite Integral Card
            if (state.expressions.isNotEmpty()) {
                var selectedExprIndexForIntegration by remember { mutableStateOf(0) }
                var lowerLimitText by remember { mutableStateOf("-2.0") }
                var upperLimitText by remember { mutableStateOf("2.0") }

                if (selectedExprIndexForIntegration >= state.expressions.size) {
                    selectedExprIndexForIntegration = 0
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Definite Integral & Area Shading",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text("Select Curve:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.expressions.forEachIndexed { idx, expr ->
                                val isSelected = selectedExprIndexForIntegration == idx
                                InputChip(
                                    selected = isSelected,
                                    onClick = { selectedExprIndexForIntegration = idx },
                                    label = { Text("y = $expr") },
                                    modifier = Modifier.testTag("integral_chip_$idx")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = lowerLimitText,
                                onValueChange = { lowerLimitText = it },
                                label = { Text("Lower Limit a") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("integration_lower_limit"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = upperLimitText,
                                onValueChange = { upperLimitText = it },
                                label = { Text("Upper Limit b") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("integration_upper_limit"),
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.clearShadedArea()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("clear_integration_button")
                            ) {
                                Text("Clear Shading")
                            }

                            Button(
                                onClick = {
                                    val a = lowerLimitText.toDoubleOrNull()
                                    val b = upperLimitText.toDoubleOrNull()
                                    if (a != null && b != null) {
                                        viewModel.calculateAndShadeArea(selectedExprIndexForIntegration, a, b)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("calculate_integration_button")
                            ) {
                                Text("Integrate & Shade")
                            }
                        }

                        state.shadedIntegration?.let { info ->
                            if (info.expressionIndex == selectedExprIndexForIntegration) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Simpson Method Result:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "∫ [${formatLabel(info.a, 1.0)} to ${formatLabel(info.b, 1.0)}] f(x) dx ≈ ${formatLabel(info.result, 1.0)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Graph Type Selection
            Text(
                text = "Select Graph Type:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GraphType.values().forEach { type ->
                    val isSelected = selectedGraphType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGraphType = type },
                        label = { Text(type.name) },
                        modifier = Modifier.testTag("graph_type_chip_${type.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Inputs based on type
            when (selectedGraphType) {
                GraphType.Cartesian -> {
                    OutlinedTextField(
                        value = expressionText,
                        onValueChange = { expressionText = it },
                        label = { Text("Enter mathematical expression in x") },
                        placeholder = { Text("e.g. x^2, sin(x), 2*x+3") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("graph_expression_input"),
                        singleLine = true,
                        trailingIcon = {
                            if (expressionText.isNotEmpty()) {
                                IconButton(onClick = { expressionText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Input")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Examples / Suggestions
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Suggestions",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            val suggestions = listOf("x^2", "sin(x)", "2*x+3", "cos(x)", "x^3", "abs(x)")
                            items(suggestions) { item ->
                                SuggestionChip(
                                    onClick = { expressionText = item },
                                    label = { Text(item) },
                                    modifier = Modifier.testTag("suggestion_chip_$item")
                                )
                            }
                        }
                    }
                }
                GraphType.Parametric -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = parametricXText,
                            onValueChange = { parametricXText = it },
                            label = { Text("x(t) =") },
                            placeholder = { Text("e.g. cos(t)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("parametric_x_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        OutlinedTextField(
                            value = parametricYText,
                            onValueChange = { parametricYText = it },
                            label = { Text("y(t) =") },
                            placeholder = { Text("e.g. sin(t)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("parametric_y_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Parametric Suggestions
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Parametric Suggestions",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                val sug = listOf(
                                    "cos(t)" to "sin(t)",
                                    "2*cos(t)" to "3*sin(t)",
                                    "t" to "t^2",
                                    "t*cos(t)" to "t*sin(t)"
                                )
                                items(sug) { (px, py) ->
                                    SuggestionChip(
                                        onClick = {
                                            parametricXText = px
                                            parametricYText = py
                                        },
                                        label = { Text("x=$px, y=$py") },
                                        modifier = Modifier.testTag("parametric_chip_${px}_${py}")
                                    )
                                }
                            }
                        }
                    }
                }
                GraphType.Polar -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = polarRText,
                            onValueChange = { polarRText = it },
                            label = { Text("r(theta) =") },
                            placeholder = { Text("e.g. sin(theta)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("polar_r_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Polar Suggestions
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Polar Suggestions",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                val sug = listOf(
                                    "sin(theta)",
                                    "4*cos(theta)",
                                    "2*(1-cos(theta))",
                                    "3*sin(3*theta)",
                                    "theta"
                                )
                                items(sug) { item ->
                                    SuggestionChip(
                                        onClick = { polarRText = item },
                                        label = { Text("r=$item") },
                                        modifier = Modifier.testTag("polar_chip_$item")
                                    )
                                }
                            }
                        }
                    }
                }
                GraphType.Conic -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Conic Section Type:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ConicType.values().forEach { conic ->
                                val isSelected = selectedConicType == conic
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedConicType = conic },
                                    label = { Text(conic.name) },
                                    modifier = Modifier.testTag("conic_type_chip_${conic.name}")
                                )
                            }
                        }

                        when (selectedConicType) {
                            ConicType.Circle -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = circleH,
                                        onValueChange = { circleH = it },
                                        label = { Text("Center h") },
                                        modifier = Modifier.weight(1f).testTag("circle_h"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = circleK,
                                        onValueChange = { circleK = it },
                                        label = { Text("Center k") },
                                        modifier = Modifier.weight(1f).testTag("circle_k"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = circleR,
                                        onValueChange = { circleR = it },
                                        label = { Text("Radius r") },
                                        modifier = Modifier.weight(1f).testTag("circle_r"),
                                        singleLine = true
                                    )
                                }
                            }
                            ConicType.Parabola -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = parabolaH,
                                            onValueChange = { parabolaH = it },
                                            label = { Text("Vertex h") },
                                            modifier = Modifier.weight(1f).testTag("parabola_h"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = parabolaK,
                                            onValueChange = { parabolaK = it },
                                            label = { Text("Vertex k") },
                                            modifier = Modifier.weight(1f).testTag("parabola_k"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = parabolaA,
                                            onValueChange = { parabolaA = it },
                                            label = { Text("a") },
                                            modifier = Modifier.weight(1f).testTag("parabola_a"),
                                            singleLine = true
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = parabolaIsHorizontal,
                                            onCheckedChange = { parabolaIsHorizontal = it },
                                            modifier = Modifier.testTag("parabola_horizontal_cb")
                                        )
                                        Text("Horizontal Parabola (y-k)² = 4a(x-h)")
                                    }
                                }
                            }
                            ConicType.Ellipse -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = ellipseH,
                                        onValueChange = { ellipseH = it },
                                        label = { Text("Center h") },
                                        modifier = Modifier.weight(1f).testTag("ellipse_h"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = ellipseK,
                                        onValueChange = { ellipseK = it },
                                        label = { Text("Center k") },
                                        modifier = Modifier.weight(1f).testTag("ellipse_k"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = ellipseA,
                                        onValueChange = { ellipseA = it },
                                        label = { Text("a") },
                                        modifier = Modifier.weight(1f).testTag("ellipse_a"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = ellipseB,
                                        onValueChange = { ellipseB = it },
                                        label = { Text("b") },
                                        modifier = Modifier.weight(1f).testTag("ellipse_b"),
                                        singleLine = true
                                    )
                                }
                            }
                            ConicType.Hyperbola -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = hyperbolaH,
                                            onValueChange = { hyperbolaH = it },
                                            label = { Text("Center h") },
                                            modifier = Modifier.weight(1f).testTag("hyperbola_h"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = hyperbolaK,
                                            onValueChange = { hyperbolaK = it },
                                            label = { Text("Center k") },
                                            modifier = Modifier.weight(1f).testTag("hyperbola_k"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = hyperbolaA,
                                            onValueChange = { hyperbolaA = it },
                                            label = { Text("a") },
                                            modifier = Modifier.weight(1f).testTag("hyperbola_a"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = hyperbolaB,
                                            onValueChange = { hyperbolaB = it },
                                            label = { Text("b") },
                                            modifier = Modifier.weight(1f).testTag("hyperbola_b"),
                                            singleLine = true
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = hyperbolaIsHorizontal,
                                            onCheckedChange = { hyperbolaIsHorizontal = it },
                                            modifier = Modifier.testTag("hyperbola_horizontal_cb")
                                        )
                                        Text("Horizontal Hyperbola (x-h)²/a² - (y-k)²/b² = 1")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Plot / Clear Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.clear()
                        expressionText = ""
                        parametricXText = ""
                        parametricYText = ""
                        polarRText = ""
                        circleH = "0"
                        circleK = "0"
                        circleR = "5"
                        parabolaH = "0"
                        parabolaK = "0"
                        parabolaA = "1"
                        ellipseH = "0"
                        ellipseK = "0"
                        ellipseA = "5"
                        ellipseB = "3"
                        hyperbolaH = "0"
                        hyperbolaK = "0"
                        hyperbolaA = "3"
                        hyperbolaB = "2"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("graph_clear_button")
                ) {
                    Text("Clear Plot")
                }

                Button(
                    onClick = {
                        when (selectedGraphType) {
                            GraphType.Cartesian -> {
                                if (expressionText.isNotBlank()) {
                                    viewModel.addExpression(expressionText.trim())
                                    keyboardController?.hide()
                                }
                            }
                            GraphType.Parametric -> {
                                if (parametricXText.isNotBlank() && parametricYText.isNotBlank()) {
                                    viewModel.addParametric(parametricXText.trim(), parametricYText.trim())
                                    keyboardController?.hide()
                                }
                            }
                            GraphType.Polar -> {
                                if (polarRText.isNotBlank()) {
                                    viewModel.addPolar(polarRText.trim())
                                    keyboardController?.hide()
                                }
                            }
                            GraphType.Conic -> {
                                when (selectedConicType) {
                                    ConicType.Circle -> {
                                        val h = circleH.toDoubleOrNull() ?: 0.0
                                        val k = circleK.toDoubleOrNull() ?: 0.0
                                        val r = circleR.toDoubleOrNull() ?: 5.0
                                        viewModel.addConicCircle(h, k, r)
                                    }
                                    ConicType.Parabola -> {
                                        val h = parabolaH.toDoubleOrNull() ?: 0.0
                                        val k = parabolaK.toDoubleOrNull() ?: 0.0
                                        val a = parabolaA.toDoubleOrNull() ?: 1.0
                                        viewModel.addConicParabola(h, k, a, parabolaIsHorizontal)
                                    }
                                    ConicType.Ellipse -> {
                                        val h = ellipseH.toDoubleOrNull() ?: 0.0
                                        val k = ellipseK.toDoubleOrNull() ?: 0.0
                                        val a = ellipseA.toDoubleOrNull() ?: 5.0
                                        val b = ellipseB.toDoubleOrNull() ?: 3.0
                                        viewModel.addConicEllipse(h, k, a, b)
                                    }
                                    ConicType.Hyperbola -> {
                                        val h = hyperbolaH.toDoubleOrNull() ?: 0.0
                                        val k = hyperbolaK.toDoubleOrNull() ?: 0.0
                                        val a = hyperbolaA.toDoubleOrNull() ?: 3.0
                                        val b = hyperbolaB.toDoubleOrNull() ?: 2.0
                                        viewModel.addConicHyperbola(h, k, a, b, hyperbolaIsHorizontal)
                                    }
                                }
                                keyboardController?.hide()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("graph_plot_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Plot icon"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plot")
                }
            }

            // Graph Intelligence & Mathematical Analysis
            if (state.equationAnalysis.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("equation_analysis_section"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "Intelligence Icon",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Graph Intelligence & Recognition",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))

                        state.equationAnalysis.forEachIndexed { index, analysis ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = analysis.equationString,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(analysis.type.name) }
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Details of analysis
                                analysis.details.forEach { (key, valString) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "$key:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = valString,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Show roots if any
                                if (analysis.roots.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Roots / Solutions (y=0):",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = analysis.roots.joinToString(", ") { String.format(java.util.Locale.US, "%.4f", it) },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50) // Green
                                    )
                                }

                                // Show extrema if any
                                if (analysis.extrema.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Critical Points (Extrema):",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    analysis.extrema.forEach { pt ->
                                        Text(
                                            text = "• ${if (pt.isMaximum) "Maximum" else "Minimum"} at (${String.format(java.util.Locale.US, "%.4f", pt.x)}, ${String.format(java.util.Locale.US, "%.4f", pt.y)})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // JEE Solver Mode Card
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().testTag("jee_solver_section"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.jeeSolverMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "JEE Icon",
                                tint = if (state.jeeSolverMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JEE Exam Solver Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (state.jeeSolverMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.jeeSolverMode,
                            onCheckedChange = { viewModel.toggleJeeSolverMode() },
                            modifier = Modifier.testTag("jee_solver_switch")
                        )
                    }

                    if (state.jeeSolverMode) {
                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                        Text(
                            text = "Solve JEE Problems Graphically",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // 1. Max / Min problems
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "1. JEE Extremum Solver (Max/Min Problems)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (state.extrema.isNotEmpty()) {
                                    state.extrema.forEach { ext ->
                                        Text(
                                            text = "• Local ${if (ext.isMaximum) "Maximum" else "Minimum"} found at x = ${String.format(java.util.Locale.US, "%.4f", ext.x)}, y = ${String.format(java.util.Locale.US, "%.4f", ext.y)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "No critical extrema points detected in current viewport.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 2. Intersection problems
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "2. JEE Curve Intersections (System Solutions)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (state.intersections.isNotEmpty()) {
                                    Text(
                                        text = "Number of intersection points: ${state.intersections.size}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE91E63)
                                    )
                                    state.intersections.forEach { (ix, iy) ->
                                        Text(
                                            text = "• Intersection point at: (${String.format(java.util.Locale.US, "%.4f", ix)}, ${String.format(java.util.Locale.US, "%.4f", iy)})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "No curve intersection solutions found. Plot multiple Cartesian curves to solve systems.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 3. Root problems
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "3. JEE Equation Roots & Number of Real Roots",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (state.roots.isNotEmpty()) {
                                    Text(
                                        text = "Total Real Roots in Viewport: ${state.roots.size}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        text = "Roots: ${state.roots.joinToString(", ") { String.format(java.util.Locale.US, "%.4f", it) }}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = "No real roots detected. Use zooming/panning or plot a curve to find root solutions.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Graph History Section
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().testTag("graph_history_section"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "History Icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Graph History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (state.history.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.clearAllHistory() },
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Text("Clear")
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

                    if (state.history.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            state.history.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.loadHistory(item) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.equation,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${item.type} • Viewport: [${String.format(java.util.Locale.US, "%.1f", item.minX)}, ${String.format(java.util.Locale.US, "%.1f", item.maxX)}]",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Restore",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                            }
                        }
                    } else {
                        Text(
                            text = "No history available. Plots you create will show up here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
}

