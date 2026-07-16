package com.example.feature.unitconverter.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ConversionCategory
import com.example.domain.model.ConversionUnit
import com.example.feature.unitconverter.UnitConverterEvent
import com.example.feature.unitconverter.UnitConverterViewModel

fun getCategoryIcon(category: ConversionCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        ConversionCategory.LENGTH -> Icons.Default.Straighten
        ConversionCategory.WEIGHT -> Icons.Default.FitnessCenter
        ConversionCategory.TEMPERATURE -> Icons.Default.DeviceThermostat
        ConversionCategory.AREA -> Icons.Default.Layers
        ConversionCategory.VOLUME -> Icons.Default.WaterDrop
        ConversionCategory.TIME -> Icons.Default.Schedule
        ConversionCategory.SPEED -> Icons.Default.Speed
        ConversionCategory.PRESSURE -> Icons.Default.Compress
        ConversionCategory.ENERGY -> Icons.Default.Bolt
        ConversionCategory.POWER -> Icons.Default.ElectricBolt
        ConversionCategory.ANGLE -> Icons.Default.Architecture
        ConversionCategory.DATA_STORAGE -> Icons.Default.Storage
        ConversionCategory.FREQUENCY -> Icons.Default.Waves
        ConversionCategory.FUEL_CONSUMPTION -> Icons.Default.LocalGasStation
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: UnitConverterViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showFromUnitDialog by remember { mutableStateOf(false) }
    var showToUnitDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Unit Converter") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCategoryDialog = true }) {
                            Icon(Icons.Default.Category, contentDescription = "All Categories")
                        }
                    }
                )
                // Horizontally Scrollable Category Selector TabRow for modern M3 sliding experience
                val activeIndex = state.categories.indexOf(state.selectedCategory).coerceAtLeast(0)
                ScrollableTabRow(
                    selectedTabIndex = activeIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = { tabPositions ->
                        if (activeIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[activeIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    state.categories.forEachIndexed { index, category ->
                        val isSelected = state.selectedCategory == category
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.onEvent(UnitConverterEvent.SelectCategory(category)) },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(category),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = category.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // From Unit (Left)
                Box(modifier = Modifier.weight(1f)) {
                    UnitSelectionSection(
                        title = "From",
                        selectedUnit = state.fromUnit,
                        value = state.inputValue,
                        onValueChange = { viewModel.onEvent(UnitConverterEvent.InputValueChange(it)) },
                        onUnitClick = { showFromUnitDialog = true },
                        isInput = true,
                        onClearClick = { viewModel.onEvent(UnitConverterEvent.ClearInput) }
                    )
                }

                // Swap Button (Middle)
                FilledIconButton(
                    onClick = { viewModel.onEvent(UnitConverterEvent.SwapUnits) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Units", modifier = Modifier.size(20.dp))
                }

                // To Unit (Right)
                Box(modifier = Modifier.weight(1f)) {
                    UnitSelectionSection(
                        title = "To",
                        selectedUnit = state.toUnit,
                        value = state.resultValue,
                        onValueChange = { },
                        onUnitClick = { showToUnitDialog = true },
                        isInput = false,
                        onCopyClick = {
                            if (state.resultValue.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(state.resultValue))
                            }
                        }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // From Unit
                UnitSelectionSection(
                    title = "From",
                    selectedUnit = state.fromUnit,
                    value = state.inputValue,
                    onValueChange = { viewModel.onEvent(UnitConverterEvent.InputValueChange(it)) },
                    onUnitClick = { showFromUnitDialog = true },
                    isInput = true,
                    onClearClick = { viewModel.onEvent(UnitConverterEvent.ClearInput) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Swap Button
                FilledIconButton(
                    onClick = { viewModel.onEvent(UnitConverterEvent.SwapUnits) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Swap Units", modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // To Unit
                UnitSelectionSection(
                    title = "To",
                    selectedUnit = state.toUnit,
                    value = state.resultValue,
                    onValueChange = { },
                    onUnitClick = { showToUnitDialog = true },
                    isInput = false,
                    onCopyClick = {
                        if (state.resultValue.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(state.resultValue))
                        }
                    }
                )
            }
        }
    }

    if (showCategoryDialog) {
        CategorySelectionDialog(
            categories = state.categories,
            onDismiss = { showCategoryDialog = false },
            onSelect = { 
                viewModel.onEvent(UnitConverterEvent.SelectCategory(it))
                showCategoryDialog = false
            }
        )
    }

    if (showFromUnitDialog) {
        UnitSelectionDialog(
            units = state.units,
            onDismiss = { showFromUnitDialog = false },
            onSelect = {
                viewModel.onEvent(UnitConverterEvent.SelectFromUnit(it))
                showFromUnitDialog = false
            }
        )
    }

    if (showToUnitDialog) {
        UnitSelectionDialog(
            units = state.units,
            onDismiss = { showToUnitDialog = false },
            onSelect = {
                viewModel.onEvent(UnitConverterEvent.SelectToUnit(it))
                showToUnitDialog = false
            }
        )
    }
}

@Composable
fun UnitSelectionSection(
    title: String,
    selectedUnit: ConversionUnit?,
    value: String,
    onValueChange: (String) -> Unit,
    onUnitClick: () -> Unit,
    isInput: Boolean,
    onClearClick: () -> Unit = {},
    onCopyClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Unit Button (Surface)
                Surface(
                    onClick = onUnitClick,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .widthIn(min = 120.dp, max = 150.dp)
                        .height(48.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedUnit?.symbol ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedUnit?.name ?: "Select",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change unit",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Numeric Input/Output Text Box
                TextField(
                    value = value,
                    onValueChange = { 
                        if (isInput) {
                            if (it.isEmpty() || it.matches(Regex("^-?[0-9]*\\.?[0-9]*$"))) {
                                onValueChange(it) 
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    readOnly = !isInput,
                    placeholder = {
                        Text(
                            text = "0",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = if (isInput) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    trailingIcon = {
                        if (isInput && value.isNotEmpty()) {
                            IconButton(onClick = onClearClick, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear input", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        } else if (!isInput && value.isNotEmpty()) {
                            IconButton(onClick = onCopyClick, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy result", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CategorySelectionDialog(
    categories: List<ConversionCategory>,
    onDismiss: () -> Unit,
    onSelect: (ConversionCategory) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Category",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(categories) { category ->
                        OutlinedButton(
                            onClick = { onSelect(category) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.displayName,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnitSelectionDialog(
    units: List<ConversionUnit>,
    onDismiss: () -> Unit,
    onSelect: (ConversionUnit) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredUnits = remember(searchQuery, units) {
        if (searchQuery.isBlank()) units
        else units.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.symbol.contains(searchQuery, ignoreCase = true) 
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Unit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search unit...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (filteredUnits.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No units found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredUnits) { unit ->
                            Surface(
                                onClick = { onSelect(unit) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                color = androidx.compose.ui.graphics.Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = unit.name,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(
                                            text = unit.symbol,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
