package com.example.feature.emicalculator.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.emicalculator.AmortizationRow
import com.example.feature.emicalculator.EmiCalculatorEvent
import com.example.feature.emicalculator.EmiCalculatorState
import com.example.feature.emicalculator.EmiCalculatorViewModel
import com.example.feature.emicalculator.FeeType
import com.example.feature.emicalculator.TenureUnit
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(
    viewModel: EmiCalculatorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    var selectedTab by remember { mutableStateOf(0) }
    var showAddPartPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMI Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Calculator") },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Schedule") },
                    icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Amortization Schedule") }
                )
            }

            if (selectedTab == 0) {
                CalculatorTabContent(
                    state = state,
                    onEvent = viewModel::onEvent,
                    isLandscape = isLandscape,
                    onAddPartPaymentClick = { showAddPartPaymentDialog = true },
                    onCopyResult = { resultText ->
                        clipboardManager.setText(AnnotatedString(resultText))
                        Toast.makeText(context, "Copied summary to clipboard", Toast.LENGTH_SHORT).show()
                        viewModel.onEvent(EmiCalculatorEvent.SaveToHistory)
                    },
                    onSaveHistory = {
                        viewModel.onEvent(EmiCalculatorEvent.SaveToHistory)
                        Toast.makeText(context, "Saved calculation to History", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                ScheduleTabContent(
                    state = state,
                    isLandscape = isLandscape
                )
            }
        }
    }

    if (showAddPartPaymentDialog) {
        PartPaymentInputDialog(
            maxMonth = state.schedule.size,
            onDismiss = { showAddPartPaymentDialog = false },
            onConfirm = { month, amount ->
                viewModel.onEvent(EmiCalculatorEvent.AddPartPayment(month, amount))
                showAddPartPaymentDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTabContent(
    state: EmiCalculatorState,
    onEvent: (EmiCalculatorEvent) -> Unit,
    isLandscape: Boolean,
    onAddPartPaymentClick: () -> Unit,
    onCopyResult: (String) -> Unit,
    onSaveHistory: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Base inputs card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Loan Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = state.loanAmount,
                            onValueChange = { onEvent(EmiCalculatorEvent.LoanAmountChanged(it)) },
                            label = { Text("Loan Amount ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("emi_input_loan_amount")
                        )
                        OutlinedTextField(
                            value = state.interestRate,
                            onValueChange = { onEvent(EmiCalculatorEvent.InterestRateChanged(it)) },
                            label = { Text("Interest Rate (% p.a.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("emi_input_interest_rate")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.tenureValue,
                            onValueChange = { onEvent(EmiCalculatorEvent.TenureValueChanged(it)) },
                            label = { Text("Tenure") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("emi_input_tenure")
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.tenureUnit == TenureUnit.YEARS,
                                onClick = { onEvent(EmiCalculatorEvent.TenureUnitChanged(TenureUnit.YEARS)) },
                                label = { Text("Years") }
                            )
                            FilterChip(
                                selected = state.tenureUnit == TenureUnit.MONTHS,
                                onClick = { onEvent(EmiCalculatorEvent.TenureUnitChanged(TenureUnit.MONTHS)) },
                                label = { Text("Months") }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = state.loanAmount,
                        onValueChange = { onEvent(EmiCalculatorEvent.LoanAmountChanged(it)) },
                        label = { Text("Loan Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emi_input_loan_amount")
                    )

                    OutlinedTextField(
                        value = state.interestRate,
                        onValueChange = { onEvent(EmiCalculatorEvent.InterestRateChanged(it)) },
                        label = { Text("Interest Rate (% per year)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emi_input_interest_rate")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.tenureValue,
                            onValueChange = { onEvent(EmiCalculatorEvent.TenureValueChanged(it)) },
                            label = { Text("Tenure") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.5f)
                                .testTag("emi_input_tenure")
                        )
                        Row(
                            modifier = Modifier.weight(0.5f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.tenureUnit == TenureUnit.YEARS,
                                onClick = { onEvent(EmiCalculatorEvent.TenureUnitChanged(TenureUnit.YEARS)) },
                                label = { Text("Years") }
                            )
                            FilterChip(
                                selected = state.tenureUnit == TenureUnit.MONTHS,
                                onClick = { onEvent(EmiCalculatorEvent.TenureUnitChanged(TenureUnit.MONTHS)) },
                                label = { Text("Months") }
                            )
                        }
                    }
                }
            }
        }

        // Advanced Options Header/Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(EmiCalculatorEvent.ToggleAdvancedExpanded) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (state.isAdvancedExpanded) Icons.Default.Settings else Icons.Default.Tune,
                        contentDescription = "Advanced Parameters",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Advanced Prepayments & Fees",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (state.isAdvancedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand Advanced Options"
                )
            }
        }

        // Advanced Options Collapsible block
        AnimatedVisibility(visible = state.isAdvancedExpanded) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = state.downPayment,
                                onValueChange = { onEvent(EmiCalculatorEvent.DownPaymentChanged(it)) },
                                label = { Text("Down Payment ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("emi_input_down_payment")
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = state.processingFeeValue,
                                    onValueChange = { onEvent(EmiCalculatorEvent.ProcessingFeeValueChanged(it)) },
                                    label = { Text("Processing Fee") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("emi_input_processing_fee")
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = state.processingFeeType == FeeType.PERCENTAGE,
                                        onClick = { onEvent(EmiCalculatorEvent.ProcessingFeeTypeChanged(FeeType.PERCENTAGE)) },
                                        label = { Text("%") }
                                    )
                                    FilterChip(
                                        selected = state.processingFeeType == FeeType.FLAT,
                                        onClick = { onEvent(EmiCalculatorEvent.ProcessingFeeTypeChanged(FeeType.FLAT)) },
                                        label = { Text("Flat ($)") }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = state.downPayment,
                            onValueChange = { onEvent(EmiCalculatorEvent.DownPaymentChanged(it)) },
                            label = { Text("Down Payment ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("emi_input_down_payment")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.processingFeeValue,
                                onValueChange = { onEvent(EmiCalculatorEvent.ProcessingFeeValueChanged(it)) },
                                label = { Text("Processing Fee") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(0.6f)
                                    .testTag("emi_input_processing_fee")
                            )
                            Row(
                                modifier = Modifier.weight(0.4f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = state.processingFeeType == FeeType.PERCENTAGE,
                                    onClick = { onEvent(EmiCalculatorEvent.ProcessingFeeTypeChanged(FeeType.PERCENTAGE)) },
                                    label = { Text("%") }
                                )
                                FilterChip(
                                    selected = state.processingFeeType == FeeType.FLAT,
                                    onClick = { onEvent(EmiCalculatorEvent.ProcessingFeeTypeChanged(FeeType.FLAT)) },
                                    label = { Text("Flat ($)") }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.extraMonthlyPayment,
                        onValueChange = { onEvent(EmiCalculatorEvent.ExtraMonthlyPaymentChanged(it)) },
                        label = { Text("Extra Monthly Payment ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emi_input_extra_monthly")
                    )

                    // One-time prepayments list
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "One-Time Part Payments",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (state.schedule.isNotEmpty()) {
                            Button(
                                onClick = onAddPartPaymentClick,
                                modifier = Modifier.testTag("add_part_payment_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Prepayment")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontSize = 12.sp)
                            }
                        }
                    }

                    if (state.partPayments.isEmpty()) {
                        Text(
                            text = "No custom part payments added yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.partPayments.forEach { part ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Month ${part.month}: $${formatCurrency(part.amount)}",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                        IconButton(
                                            onClick = { onEvent(EmiCalculatorEvent.DeletePartPayment(part.id)) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
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

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Calculation Outputs / Results Card
        AnimatedVisibility(
            visible = state.monthlyEmi > 0 && state.error == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main results display
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Monthly EMI",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )

                        Text(
                            text = "$${formatCurrency(state.monthlyEmi)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.testTag("emi_result_monthly_emi")
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Total Interest",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    "$${formatCurrency(state.totalInterest)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.testTag("emi_result_total_interest")
                                )
                            }

                            VerticalDivider(
                                modifier = Modifier.height(36.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Total Payment",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    "$${formatCurrency(state.totalPayment)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.testTag("emi_result_total_payment")
                                )
                            }
                        }

                        if (state.processingFeeCalculated > 0.0) {
                            Text(
                                text = "Est. Processing Fee: $${formatCurrency(state.processingFeeCalculated)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Prepayment / Savings Banner
                if (state.savingsInterest > 0.01 || state.savingsMonths > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "Savings",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Your Prepayment Savings!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (state.savingsInterest > 0.0) {
                                    Text(
                                        text = "Saved $${formatCurrency(state.savingsInterest)} in interest paid.",
                                        fontSize = 13.sp
                                    )
                                }
                                if (state.savingsMonths > 0) {
                                    Text(
                                        text = "Closed loan ${state.savingsMonths} months early (Actual tenure: ${state.actualTenureMonths} months).",
                                        fontSize = 13.sp
                                    )
                                }
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
                        onClick = { onEvent(EmiCalculatorEvent.ClearAll) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("emi_clear_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }

                    IconButton(
                        onClick = {
                            val summary = "EMI Loan Summary:\n" +
                                    "Loan Amount: $${formatCurrency(state.loanAmount.toDoubleOrNull() ?: 0.0)}\n" +
                                    "EMI: $${formatCurrency(state.monthlyEmi)}\n" +
                                    "Total Interest: $${formatCurrency(state.totalInterest)}\n" +
                                    "Total Payment: $${formatCurrency(state.totalPayment)}\n" +
                                    "Tenure: ${state.actualTenureMonths} months"
                            onCopyResult(summary)
                        },
                        modifier = Modifier.testTag("emi_copy_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Summary")
                    }

                    IconButton(
                        onClick = onSaveHistory,
                        modifier = Modifier.testTag("emi_save_history_button")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "Save to History")
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleTabContent(
    state: EmiCalculatorState,
    isLandscape: Boolean
) {
    if (state.schedule.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ListAlt,
                    contentDescription = "Schedule Empty",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "No Schedule Available",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Please enter loan amount, interest rate, and tenure to calculate the schedule.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Summary header table info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Amortization Table",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Total: ${state.schedule.size} Months",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.schedule) { row ->
                    AmortizationRowItem(row = row, isLandscape = isLandscape)
                }
            }
        }
    }
}

@Composable
fun AmortizationRowItem(
    row: AmortizationRow,
    isLandscape: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "Month ${row.month}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Bal: $${formatCurrency(row.endBalance)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Principal: $${formatCurrency(row.principalPaid)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Interest: $${formatCurrency(row.interestPaid)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (row.extraPaid > 0.0) {
                        Text(
                            text = "Extra Paid: $${formatCurrency(row.extraPaid)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Text(
                        text = "Total Paid: $${formatCurrency(row.totalPaid)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Paid to Principal",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$${formatCurrency(row.principalPaid)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = "Paid to Interest",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$${formatCurrency(row.interestPaid)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (row.extraPaid > 0.0) {
                        Column {
                            Text(
                                text = "Prepayment",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "$${formatCurrency(row.extraPaid)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Paid",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$${formatCurrency(row.totalPaid)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PartPaymentInputDialog(
    maxMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, amount: Double) -> Unit
) {
    var monthStr by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add One-Time Part Payment") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add an additional principal payment in a specific month to reduce outstanding tenure.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                OutlinedTextField(
                    value = monthStr,
                    onValueChange = { monthStr = it },
                    label = { Text("Month Number (1 to $maxMonth)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_part_payment_month")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Payment Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_part_payment_amount")
                )

                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mVal = monthStr.toIntOrNull()
                    val aVal = amountStr.toDoubleOrNull()

                    if (mVal == null || mVal <= 0 || mVal > maxMonth) {
                        errorText = "Please enter a valid month between 1 and $maxMonth"
                        return@Button
                    }
                    if (aVal == null || aVal <= 0) {
                        errorText = "Please enter a valid positive prepayment amount"
                        return@Button
                    }

                    onConfirm(mVal, aVal)
                },
                modifier = Modifier.testTag("dialog_part_payment_confirm")
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatCurrency(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0.00"
    val bd = BigDecimal(value).setScale(2, RoundingMode.HALF_UP)
    return String.format("%,.2f", bd.toDouble())
}
