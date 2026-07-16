package com.example.feature.scientificconstants.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ConstantCategory
import com.example.domain.model.ScientificConstant
import com.example.feature.scientificconstants.ScientificConstantsEvent
import com.example.feature.scientificconstants.ScientificConstantsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScientificConstantsScreen(
    viewModel: ScientificConstantsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // Snackbar for feedback when copying
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Handle background toast-style action when copying
    fun copyToClipboard(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        // We can optionally trigger a brief snackbar
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scientific Constants") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Reset filters
                    if (state.selectedCategory != null || state.searchQuery.isNotEmpty() || state.showFavoritesOnly) {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(ScientificConstantsEvent.SelectCategory(null))
                                viewModel.onEvent(ScientificConstantsEvent.SearchQueryChange(""))
                                if (state.showFavoritesOnly) {
                                    viewModel.onEvent(ScientificConstantsEvent.ToggleFavoritesOnly)
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterAltOff, contentDescription = "Clear All Filters")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar & Favorites Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onEvent(ScientificConstantsEvent.SearchQueryChange(it)) },
                    placeholder = { Text("Search by name or symbol...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onEvent(ScientificConstantsEvent.SearchQueryChange("")) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                // Favorites toggle button
                FilterChip(
                    selected = state.showFavoritesOnly,
                    onClick = { viewModel.onEvent(ScientificConstantsEvent.ToggleFavoritesOnly) },
                    label = { Text("Favorites") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (state.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // Categories horizontal bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All" item
                item {
                    FilterChip(
                        selected = state.selectedCategory == null,
                        onClick = { viewModel.onEvent(ScientificConstantsEvent.SelectCategory(null)) },
                        label = { Text("All Categories") }
                    )
                }

                items(state.categories) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.onEvent(ScientificConstantsEvent.SelectCategory(category)) },
                        label = { Text(category.displayName) }
                    )
                }
            }

            // Main body - responsive layout
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Pane: Constants List (weight 1.2f)
                    Box(modifier = Modifier.weight(1.2f)) {
                        ConstantsList(
                            constants = state.constants,
                            selectedConstant = state.selectedConstant,
                            onConstantClick = { viewModel.onEvent(ScientificConstantsEvent.SelectConstant(it)) },
                            onFavoriteToggle = { viewModel.onEvent(ScientificConstantsEvent.ToggleFavorite(it.id)) }
                        )
                    }

                    // Right Pane: Constant Detail card (weight 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        val selected = state.selectedConstant
                        if (selected != null) {
                            ConstantDetailCard(
                                constant = selected,
                                onFavoriteToggle = { viewModel.onEvent(ScientificConstantsEvent.ToggleFavorite(selected.id)) },
                                onCopyClick = { text, label -> copyToClipboard(text, label) }
                            )
                        } else {
                            // Empty State / Placeholder
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Select a constant",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Click any scientific constant on the left to see detailed physical descriptors, values, and units.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Portrait Pane: Constants List
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    ConstantsList(
                        constants = state.constants,
                        selectedConstant = state.selectedConstant,
                        onConstantClick = { viewModel.onEvent(ScientificConstantsEvent.SelectConstant(it)) },
                        onFavoriteToggle = { viewModel.onEvent(ScientificConstantsEvent.ToggleFavorite(it.id)) }
                    )
                }
            }
        }
    }

    // Detail Dialog for Portrait mode
    if (!isLandscape && state.selectedConstant != null) {
        val constant = state.selectedConstant!!
        Dialog(onDismissRequest = { viewModel.onEvent(ScientificConstantsEvent.SelectConstant(null)) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = constant.field,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(onClick = { viewModel.onEvent(ScientificConstantsEvent.ToggleFavorite(constant.id)) }) {
                                Icon(
                                    imageVector = if (constant.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Toggle Favorite",
                                    tint = if (constant.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.onEvent(ScientificConstantsEvent.SelectConstant(null)) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close dialog")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ConstantDetailContent(
                        constant = constant,
                        onCopyClick = { text, label -> copyToClipboard(text, label) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConstantsList(
    constants: List<ScientificConstant>,
    selectedConstant: ScientificConstant?,
    onConstantClick: (ScientificConstant) -> Unit,
    onFavoriteToggle: (ScientificConstant) -> Unit
) {
    if (constants.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No constants found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Try clearing your search query or choosing another category.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(constants, key = { it.id }) { constant ->
                val isSelected = selectedConstant?.id == constant.id
                ConstantItemRow(
                    constant = constant,
                    isSelected = isSelected,
                    onClick = { onConstantClick(constant) },
                    onFavoriteToggle = { onFavoriteToggle(constant) }
                )
            }
        }
    }
}

@Composable
fun ConstantItemRow(
    constant: ScientificConstant,
    isSelected: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Symbol avatar box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = constant.symbol,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            // Name & category
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = constant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = constant.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = constant.field,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Short preview value with unit
                Text(
                    text = "${constant.value} ${if (constant.unit != "dimensionless") constant.unit else ""}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorite star button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (constant.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (constant.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ConstantDetailCard(
    constant: ScientificConstant,
    onFavoriteToggle: () -> Unit,
    onCopyClick: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = constant.field,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (constant.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (constant.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ConstantDetailContent(
                constant = constant,
                onCopyClick = onCopyClick
            )
        }
    }
}

@Composable
fun ConstantDetailContent(
    constant: ScientificConstant,
    onCopyClick: (String, String) -> Unit
) {
    var showCopiedText by remember { mutableStateOf(false) }

    // Start auto-dismiss block if copied is clicked
    LaunchedEffect(showCopiedText) {
        if (showCopiedText) {
            kotlinx.coroutines.delay(1500)
            showCopiedText = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Center aligned symbol & title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = constant.symbol,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = constant.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            SuggestionChip(
                onClick = {},
                label = { Text(constant.category.displayName) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    labelColor = MaterialTheme.colorScheme.secondary
                )
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Numerical Value Display Card
        Text(
            text = "Numerical Value",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = constant.value,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        onCopyClick(constant.value, constant.name)
                        showCopiedText = true
                    }
                ) {
                    Icon(
                        imageVector = if (showCopiedText) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy Value",
                        tint = if (showCopiedText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SI Unit
        if (constant.unit.isNotEmpty() && constant.unit != "dimensionless") {
            Text(
                text = "SI Unit",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = constant.unit,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Description
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = constant.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
    }
}
