package com.example.feature.datetimecalculator.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.datetimecalculator.DateTimeCalculatorEvent
import com.example.feature.datetimecalculator.DateTimeCalculatorViewModel
import com.example.feature.datetimecalculator.DateTimeMode
import com.example.feature.datetimecalculator.domain.DateTimeCalculations
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeCalculatorScreen(
    viewModel: DateTimeCalculatorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    val displayTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

    // Native Date Dialog Handlers
    var activeDatePicker by remember { mutableStateOf<DatePickerTarget?>(null) }
    var activeTimePicker by remember { mutableStateOf<TimePickerTarget?>(null) }

    activeDatePicker?.let { target ->
        val currentDate = when (target) {
            DatePickerTarget.DATE_DIFF_START -> state.dateDiffStart
            DatePickerTarget.DATE_DIFF_END -> state.dateDiffEnd
            DatePickerTarget.ADD_SUB_DATE -> state.addSubDate
            DatePickerTarget.DAY_OF_WEEK -> state.dayOfWeekDate
            DatePickerTarget.AGE_BIRTH -> state.ageBirthDate
            DatePickerTarget.AGE_TARGET -> state.ageTargetDate
            DatePickerTarget.BUSINESS_START -> state.businessStart
            DatePickerTarget.BUSINESS_END -> state.businessEnd
            DatePickerTarget.COUNTDOWN -> state.countdownTargetDate
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                when (target) {
                    DatePickerTarget.DATE_DIFF_START -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeDateDiffStart(selectedDate))
                    DatePickerTarget.DATE_DIFF_END -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeDateDiffEnd(selectedDate))
                    DatePickerTarget.ADD_SUB_DATE -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeAddSubDate(selectedDate))
                    DatePickerTarget.DAY_OF_WEEK -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeDayOfWeekDate(selectedDate))
                    DatePickerTarget.AGE_BIRTH -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeAgeBirthDate(selectedDate))
                    DatePickerTarget.AGE_TARGET -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeAgeTargetDate(selectedDate))
                    DatePickerTarget.BUSINESS_START -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeBusinessStart(selectedDate))
                    DatePickerTarget.BUSINESS_END -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeBusinessEnd(selectedDate))
                    DatePickerTarget.COUNTDOWN -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeCountdownTargetDate(selectedDate))
                }
                activeDatePicker = null
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        ).apply {
            setOnCancelListener { activeDatePicker = null }
            show()
        }
    }

    activeTimePicker?.let { target ->
        val currentTime = when (target) {
            TimePickerTarget.COUNTDOWN -> state.countdownTargetTime
            TimePickerTarget.TIME_DIFF_START -> state.timeDiffStart
            TimePickerTarget.TIME_DIFF_END -> state.timeDiffEnd
            TimePickerTarget.TIME_ZONE -> state.timeZoneTime
        }
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                when (target) {
                    TimePickerTarget.COUNTDOWN -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeCountdownTargetTime(selectedTime))
                    TimePickerTarget.TIME_DIFF_START -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeTimeDiffStart(selectedTime))
                    TimePickerTarget.TIME_DIFF_END -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeTimeDiffEnd(selectedTime))
                    TimePickerTarget.TIME_ZONE -> viewModel.onEvent(DateTimeCalculatorEvent.ChangeTimeZoneTime(selectedTime))
                }
                activeTimePicker = null
            },
            currentTime.hour,
            currentTime.minute,
            false // 12-hour format display in picker
        ).apply {
            setOnCancelListener { activeTimePicker = null }
            show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Date & Time Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("datetime_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(DateTimeCalculatorEvent.ClearInputs) },
                        modifier = Modifier.testTag("datetime_clear_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset inputs")
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
            // Mode Selectors (Scrollable Tabs)
            ScrollableTabRow(
                selectedTabIndex = state.currentMode.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("datetime_mode_tabs")
            ) {
                DateTimeMode.values().forEach { mode ->
                    Tab(
                        selected = state.currentMode == mode,
                        onClick = { viewModel.onEvent(DateTimeCalculatorEvent.ChangeMode(mode)) },
                        text = {
                            Text(
                                text = mode.toFriendlyName(),
                                fontWeight = if (state.currentMode == mode) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("datetime_tab_${mode.name.lowercase()}")
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Feature Header Card with dynamic instruction
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = state.currentMode.toIcon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = state.currentMode.toFriendlyName(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = state.currentMode.toDescription(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Dynamic Mode Input View
                when (state.currentMode) {
                    DateTimeMode.DATE_DIFFERENCE -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectionBox(
                                title = "From Date",
                                valueText = state.dateDiffStart.format(displayDateFormatter),
                                onClick = { activeDatePicker = DatePickerTarget.DATE_DIFF_START },
                                modifier = Modifier.weight(1f).testTag("date_diff_start_box")
                            )
                            SelectionBox(
                                title = "To Date",
                                valueText = state.dateDiffEnd.format(displayDateFormatter),
                                onClick = { activeDatePicker = DatePickerTarget.DATE_DIFF_END },
                                modifier = Modifier.weight(1f).testTag("date_diff_end_box")
                            )
                        }
                    }

                    DateTimeMode.ADD_SUBTRACT_DATE -> {
                        SelectionBox(
                            title = "Base Date",
                            valueText = state.addSubDate.format(displayDateFormatter),
                            onClick = { activeDatePicker = DatePickerTarget.ADD_SUB_DATE },
                            modifier = Modifier.fillMaxWidth().testTag("add_sub_base_date_box")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = state.addSubAmount,
                                onValueChange = { viewModel.onEvent(DateTimeCalculatorEvent.ChangeAddSubAmount(it)) },
                                label = { Text("Amount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.2f).testTag("add_sub_amount_field"),
                                singleLine = true
                            )

                            // Unit Selector TabRow/Chips
                            Column(modifier = Modifier.weight(2f)) {
                                Text("Unit", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(ChronoUnit.DAYS, ChronoUnit.WEEKS, ChronoUnit.MONTHS, ChronoUnit.YEARS).forEach { unit ->
                                        FilterChip(
                                            selected = state.addSubUnit == unit,
                                            onClick = { viewModel.onEvent(DateTimeCalculatorEvent.ChangeAddSubUnit(unit)) },
                                            label = { Text(unit.name.substring(0, 1) + unit.name.substring(1).lowercase(), fontSize = 10.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Operation Segmented Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Operation:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            FilledTonalIconToggleButton(
                                checked = state.addSubIsAddition,
                                onCheckedChange = { viewModel.onEvent(DateTimeCalculatorEvent.ChangeAddSubIsAddition(true)) }
                            ) {
                                Text("+ Add")
                            }
                            FilledTonalIconToggleButton(
                                checked = !state.addSubIsAddition,
                                onCheckedChange = { viewModel.onEvent(DateTimeCalculatorEvent.ChangeAddSubIsAddition(false)) }
                            ) {
                                Text("- Subtract")
                            }
                        }
                    }

                    DateTimeMode.DAY_OF_WEEK -> {
                        SelectionBox(
                            title = "Select Date",
                            valueText = state.dayOfWeekDate.format(displayDateFormatter),
                            onClick = { activeDatePicker = DatePickerTarget.DAY_OF_WEEK },
                            modifier = Modifier.fillMaxWidth().testTag("day_of_week_date_box")
                        )
                    }

                    DateTimeMode.LEAP_YEAR -> {
                        OutlinedTextField(
                            value = state.leapYearValue,
                            onValueChange = { viewModel.onEvent(DateTimeCalculatorEvent.ChangeLeapYearValue(it)) },
                            label = { Text("Enter Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("leap_year_input_field"),
                            singleLine = true
                        )
                    }

                    DateTimeMode.AGE_BETWEEN_DATES -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectionBox(
                                title = "Date of Birth",
                                valueText = state.ageBirthDate.format(displayDateFormatter),
                                onClick = { activeDatePicker = DatePickerTarget.AGE_BIRTH },
                                modifier = Modifier.weight(1f).testTag("age_birth_box")
                            )
                            SelectionBox(
                                title = "Age at Date",
                                valueText = state.ageTargetDate.format(displayDateFormatter),
                                onClick = { activeDatePicker = DatePickerTarget.AGE_TARGET },
                                modifier = Modifier.weight(1f).testTag("age_target_box")
                            )
                        }
                    }

                    DateTimeMode.BUSINESS_DAYS -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectionBox(
                                title = "Start Date",
                                valueText = state.businessStart.format(displayDateFormatter),
                                onClick = { activeDatePicker = DatePickerTarget.BUSINESS_START },
                                modifier = Modifier.weight(1f).testTag("business_start_box")
                            )
                            SelectionBox(
                                title = "End Date",
                                valueText = state.businessEnd.format(displayDateFormatter),
                                onClick = { activeDatePicker = DatePickerTarget.BUSINESS_END },
                                modifier = Modifier.weight(1f).testTag("business_end_box")
                            )
                        }
                    }

                    DateTimeMode.COUNTDOWN -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectionBox(
                                title = "Target Date",
                                valueText = state.countdownTargetDate.format(displayDateFormatter),
                                onClick = { activeDatePicker = DatePickerTarget.COUNTDOWN },
                                modifier = Modifier.weight(1f).testTag("countdown_date_box")
                            )
                            SelectionBox(
                                title = "Target Time",
                                valueText = state.countdownTargetTime.format(displayTimeFormatter),
                                onClick = { activeTimePicker = TimePickerTarget.COUNTDOWN },
                                modifier = Modifier.weight(1f).testTag("countdown_time_box")
                            )
                        }
                    }

                    DateTimeMode.TIME_DIFFERENCE -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectionBox(
                                title = "Start Time",
                                valueText = state.timeDiffStart.format(displayTimeFormatter),
                                onClick = { activeTimePicker = TimePickerTarget.TIME_DIFF_START },
                                modifier = Modifier.weight(1f).testTag("time_diff_start_box")
                            )
                            SelectionBox(
                                title = "End Time",
                                valueText = state.timeDiffEnd.format(displayTimeFormatter),
                                onClick = { activeTimePicker = TimePickerTarget.TIME_DIFF_END },
                                modifier = Modifier.weight(1f).testTag("time_diff_end_box")
                            )
                        }
                    }

                    DateTimeMode.TIME_ZONE -> {
                        SelectionBox(
                            title = "Source Time",
                            valueText = state.timeZoneTime.format(displayTimeFormatter),
                            onClick = { activeTimePicker = TimePickerTarget.TIME_ZONE },
                            modifier = Modifier.fillMaxWidth().testTag("time_zone_time_box")
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectionBox(
                                title = "From Time Zone",
                                valueText = state.timeZoneSource,
                                onClick = { viewModel.onEvent(DateTimeCalculatorEvent.SetSelectingSourceZone(true)) },
                                modifier = Modifier.weight(1f).testTag("time_zone_source_box")
                            )
                            SelectionBox(
                                title = "To Time Zone",
                                valueText = state.timeZoneTarget,
                                onClick = { viewModel.onEvent(DateTimeCalculatorEvent.SetSelectingTargetZone(true)) },
                                modifier = Modifier.weight(1f).testTag("time_zone_target_box")
                            )
                        }
                        
                        Text(
                            text = "Note: Internet time zone APIs are disabled. Showing fully functional offline time zone converter using built-in system zone definitions.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                }

                // Dynamic Result Section
                val resultText = viewModel.getResultText()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("datetime_result_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.error == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CALCULATION RESULT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = if (state.error == null) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(DateTimeCalculatorEvent.SaveToHistory)
                                        Toast.makeText(context, "Saved to local calculation history", Toast.LENGTH_SHORT).show()
                                    },
                                    enabled = state.error == null
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = "Save to history",
                                        tint = if (state.error == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("DateTime Calculator Result", resultText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied result to clipboard!", Toast.LENGTH_SHORT).show()
                                        viewModel.onEvent(DateTimeCalculatorEvent.CopyResultToClipboard)
                                    },
                                    enabled = state.error == null,
                                    modifier = Modifier.testTag("datetime_copy_button")
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy result",
                                        tint = if (state.error == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = resultText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            lineHeight = 22.sp,
                            color = if (state.error == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.testTag("datetime_result_text")
                        )
                    }
                }
            }
        }
    }

    // Time Zone Picker Modal Dialog
    if (state.isSelectingSourceZone || state.isSelectingTargetZone) {
        Dialog(
            onDismissRequest = {
                viewModel.onEvent(DateTimeCalculatorEvent.SetSelectingSourceZone(false))
                viewModel.onEvent(DateTimeCalculatorEvent.SetSelectingTargetZone(false))
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (state.isSelectingSourceZone) "Select Source Zone" else "Select Target Zone",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = state.timeZoneSearchQuery,
                        onValueChange = { viewModel.onEvent(DateTimeCalculatorEvent.ChangeTimeZoneSearchQuery(it)) },
                        placeholder = { Text("Search zones e.g. London, India...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        singleLine = true
                    )

                    val filteredZones = DateTimeCalculations.offlineTimeZones.filter {
                        it.displayName.contains(state.timeZoneSearchQuery, ignoreCase = true) ||
                                it.id.contains(state.timeZoneSearchQuery, ignoreCase = true) ||
                                it.gmtOffset.contains(state.timeZoneSearchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredZones) { zone ->
                            ListItem(
                                headlineContent = { Text(zone.displayName, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text("${zone.id} | ${zone.gmtOffset}") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (state.isSelectingSourceZone) {
                                            viewModel.onEvent(DateTimeCalculatorEvent.ChangeTimeZoneSource(zone.id))
                                        } else {
                                            viewModel.onEvent(DateTimeCalculatorEvent.ChangeTimeZoneTarget(zone.id))
                                        }
                                    }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            viewModel.onEvent(DateTimeCalculatorEvent.SetSelectingSourceZone(false))
                            viewModel.onEvent(DateTimeCalculatorEvent.SetSelectingTargetZone(false))
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBox(
    title: String,
    valueText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = valueText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Target Helpers for Native Dialog Openers
enum class DatePickerTarget {
    DATE_DIFF_START,
    DATE_DIFF_END,
    ADD_SUB_DATE,
    DAY_OF_WEEK,
    AGE_BIRTH,
    AGE_TARGET,
    BUSINESS_START,
    BUSINESS_END,
    COUNTDOWN
}

enum class TimePickerTarget {
    COUNTDOWN,
    TIME_DIFF_START,
    TIME_DIFF_END,
    TIME_ZONE
}

// Friendly Helpers for UI
fun DateTimeMode.toFriendlyName(): String = when (this) {
    DateTimeMode.DATE_DIFFERENCE -> "Date Difference"
    DateTimeMode.ADD_SUBTRACT_DATE -> "Add/Sub Days"
    DateTimeMode.DAY_OF_WEEK -> "Day Finder"
    DateTimeMode.LEAP_YEAR -> "Leap Year"
    DateTimeMode.AGE_BETWEEN_DATES -> "Age Calculator"
    DateTimeMode.BUSINESS_DAYS -> "Business Days"
    DateTimeMode.COUNTDOWN -> "Countdown"
    DateTimeMode.TIME_DIFFERENCE -> "Time Difference"
    DateTimeMode.TIME_ZONE -> "Time Zone"
}

fun DateTimeMode.toDescription(): String = when (this) {
    DateTimeMode.DATE_DIFFERENCE -> "Find years, months, weeks & days between two dates."
    DateTimeMode.ADD_SUBTRACT_DATE -> "Add or subtract days, weeks, months or years to/from any date."
    DateTimeMode.DAY_OF_WEEK -> "Identify the day of the week for any past or future date."
    DateTimeMode.LEAP_YEAR -> "Check if a particular year is a leap year containing 366 days."
    DateTimeMode.AGE_BETWEEN_DATES -> "Calculate precise age in years, months, days and next birthday."
    DateTimeMode.BUSINESS_DAYS -> "Find the count of business days between dates (excluding weekends)."
    DateTimeMode.COUNTDOWN -> "Countdown timer in real-time days, hours, minutes to a milestone."
    DateTimeMode.TIME_DIFFERENCE -> "Calculate total hours, minutes and seconds between two times."
    DateTimeMode.TIME_ZONE -> "Convert hours between key offline global time zones."
}

fun DateTimeMode.toIcon() = when (this) {
    DateTimeMode.DATE_DIFFERENCE -> Icons.Default.DateRange
    DateTimeMode.ADD_SUBTRACT_DATE -> Icons.Default.EditCalendar
    DateTimeMode.DAY_OF_WEEK -> Icons.Default.CalendarViewMonth
    DateTimeMode.LEAP_YEAR -> Icons.Default.CalendarToday
    DateTimeMode.AGE_BETWEEN_DATES -> Icons.Default.Face
    DateTimeMode.BUSINESS_DAYS -> Icons.Default.WorkOutline
    DateTimeMode.COUNTDOWN -> Icons.Default.HourglassEmpty
    DateTimeMode.TIME_DIFFERENCE -> Icons.Default.AccessTime
    DateTimeMode.TIME_ZONE -> Icons.Default.Public
}
