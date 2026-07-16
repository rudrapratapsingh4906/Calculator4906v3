package com.example.feature.currencyconverter.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.currencyconverter.CurrencyConverterEvent
import com.example.feature.currencyconverter.CurrencyConverterState
import com.example.feature.currencyconverter.CurrencyConverterViewModel
import com.example.feature.currencyconverter.domain.Currency
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    viewModel: CurrencyConverterViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currency Converter") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("currency_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        ConverterInputs(state = state, onEvent = viewModel::onEvent)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        ConverterOutputsAndActions(
                            state = state,
                            onEvent = viewModel::onEvent,
                            onCopyResult = { text ->
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Copied results to clipboard", Toast.LENGTH_SHORT).show()
                                viewModel.onEvent(CurrencyConverterEvent.SaveToHistory)
                            },
                            onSaveHistory = {
                                viewModel.onEvent(CurrencyConverterEvent.SaveToHistory)
                                Toast.makeText(context, "Saved results to History", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ConverterInputs(state = state, onEvent = viewModel::onEvent)
                    ConverterOutputsAndActions(
                        state = state,
                        onEvent = viewModel::onEvent,
                        onCopyResult = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Copied results to clipboard", Toast.LENGTH_SHORT).show()
                            viewModel.onEvent(CurrencyConverterEvent.SaveToHistory)
                        },
                        onSaveHistory = {
                            viewModel.onEvent(CurrencyConverterEvent.SaveToHistory)
                            Toast.makeText(context, "Saved results to History", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Currency selection dialog
            if (state.isSelectingSource || state.isSelectingTarget) {
                CurrencySelectorDialog(
                    isSource = state.isSelectingSource,
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}

@Composable
fun ConverterInputs(
    state: CurrencyConverterState,
    onEvent: (CurrencyConverterEvent) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Conversion Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Source Currency Selection Row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("From", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
                        .clickable { onEvent(CurrencyConverterEvent.OpenSourceSelection) }
                        .padding(12.dp)
                        .testTag("currency_source_select"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = state.sourceCurrency?.symbol ?: "$",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Column {
                            Text(
                                text = state.sourceCurrency?.code ?: "USD",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = state.sourceCurrency?.name ?: "US Dollar",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Source")
                }
            }

            // Swap Button Row
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { onEvent(CurrencyConverterEvent.SwapCurrencies) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("currency_swap_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap currencies",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Target Currency Selection Row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("To", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
                        .clickable { onEvent(CurrencyConverterEvent.OpenTargetSelection) }
                        .padding(12.dp)
                        .testTag("currency_target_select"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = state.targetCurrency?.symbol ?: "€",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Column {
                            Text(
                                text = state.targetCurrency?.code ?: "EUR",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = state.targetCurrency?.name ?: "Euro",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Target")
                }
            }
        }
    }
}

@Composable
fun ConverterOutputsAndActions(
    state: CurrencyConverterState,
    onEvent: (CurrencyConverterEvent) -> Unit,
    onCopyResult: (String) -> Unit,
    onSaveHistory: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Output result card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Live Conversion Output",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                OutlinedTextField(
                    value = state.sourceAmount,
                    onValueChange = { onEvent(CurrencyConverterEvent.SourceAmountChanged(it)) },
                    label = { Text("Amount (${state.sourceCurrency?.code ?: "USD"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("currency_input_amount")
                )

                if (state.error != null) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Calculated Output
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "${formatDouble(state.targetAmount, 2)} ${state.targetCurrency?.code ?: "EUR"}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("currency_result_amount")
                    )

                    // Reference Rate text
                    if (state.sourceCurrency != null && state.targetCurrency != null) {
                        val rate = state.targetCurrency.rateToUsd / state.sourceCurrency.rateToUsd
                        val inverseRate = state.sourceCurrency.rateToUsd / state.targetCurrency.rateToUsd
                        Text(
                            text = "1 ${state.sourceCurrency.code} = ${formatDouble(rate, 4)} ${state.targetCurrency.code}  |  1 ${state.targetCurrency.code} = ${formatDouble(inverseRate, 4)} ${state.sourceCurrency.code}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Actions panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onEvent(CurrencyConverterEvent.ClearAll) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("currency_clear_button")
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear")
            }

            IconButton(
                onClick = {
                    val srcCode = state.sourceCurrency?.code ?: "USD"
                    val tarCode = state.targetCurrency?.code ?: "EUR"
                    val rate = (state.targetCurrency?.rateToUsd ?: 1.0) / (state.sourceCurrency?.rateToUsd ?: 1.0)
                    val text = "Currency Conversion Result:\n" +
                            "${state.sourceAmount} $srcCode = ${formatDouble(state.targetAmount, 2)} $tarCode\n" +
                            "Exchange Rate: 1 $srcCode = ${formatDouble(rate, 4)} $tarCode"
                    onCopyResult(text)
                },
                modifier = Modifier.testTag("currency_copy_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Conversion")
            }

            IconButton(
                onClick = onSaveHistory,
                modifier = Modifier.testTag("currency_save_history_button")
            ) {
                Icon(Icons.Default.History, contentDescription = "Save to History")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurrencySelectorDialog(
    isSource: Boolean,
    state: CurrencyConverterState,
    onEvent: (CurrencyConverterEvent) -> Unit
) {
    val filteredList = remember(state.currencies, state.searchQuery) {
        if (state.searchQuery.isEmpty()) {
            state.currencies
        } else {
            state.currencies.filter {
                it.code.contains(state.searchQuery, ignoreCase = true) ||
                        it.name.contains(state.searchQuery, ignoreCase = true) ||
                        it.country.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = { onEvent(CurrencyConverterEvent.DismissSelection) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSource) "Select Source Currency" else "Select Target Currency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { onEvent(CurrencyConverterEvent.DismissSelection) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search field
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(CurrencyConverterEvent.SearchQueryChanged(it)) },
                    placeholder = { Text("Search currency name, code, or country") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("currency_search_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No currencies match your search.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = filteredList,
                            key = { it.code }
                        ) { currency ->
                            Row(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .background(
                                        if (currency.isFavorite) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                        else Color.Transparent,
                                        MaterialTheme.shapes.medium
                                    )
                                    .clickable {
                                        if (isSource) {
                                            onEvent(CurrencyConverterEvent.SelectSourceCurrency(currency.code))
                                        } else {
                                            onEvent(CurrencyConverterEvent.SelectTargetCurrency(currency.code))
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Symbol placeholder
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = if (currency.isFavorite) MaterialTheme.colorScheme.secondaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = currency.symbol,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currency.code,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp
                                            )
                                            if (currency.isFavorite) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Favorite",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${currency.name} • ${currency.country}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Favorite Toggle Button
                                IconButton(
                                    onClick = { onEvent(CurrencyConverterEvent.ToggleFavorite(currency.code)) },
                                    modifier = Modifier.testTag("favorite_toggle_${currency.code}")
                                ) {
                                    Icon(
                                        imageVector = if (currency.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Toggle Favorite",
                                        tint = if (currency.isFavorite) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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

private fun formatDouble(value: Double, decimals: Int): String {
    if (value.isNaN() || value.isInfinite() || value <= 0.0) return "0.00"
    val bd = BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP)
    return if (decimals == 0) {
        String.format("%,d", bd.toInt())
    } else {
        String.format("%,.${decimals}f", bd.toDouble())
    }
}
