package com.tbasic.fitnesstracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.DateField
import com.tbasic.fitnesstracker.ui.components.LabeledIconTextRow
import com.tbasic.fitnesstracker.ui.components.rememberDatePickerLauncher
import com.tbasic.fitnesstracker.utils.formatDate
import com.tbasic.fitnesstracker.utils.toFormattedDateString
import com.tbasic.fitnesstracker.vm.MealPlanViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanViewScreen(
    mealPlanViewModel: MealPlanViewModel,
    onMealPlanSelected: (String) -> Unit = {},
    onAddMealPlanClick: () -> Unit = {}
) {
    val localizedContext = LocalLocalizedContext.current
    val mealPlans by mealPlanViewModel.filteredPlans.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var startDate by remember { mutableStateOf(mealPlanViewModel.filterStartDate.ifBlank { today }) }
    var endDate by remember { mutableStateOf(mealPlanViewModel.filterEndDate.ifBlank { today }) }

    fun dateStringToMillis(dateString: String): Long {
        return try {
            dateFormatter.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    val startDatePickerLauncher = rememberDatePickerLauncher(
        onDateSelected = { millis ->
            val selectedDate = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date

            startDate = selectedDate.toFormattedDateString()
        },
        allowPast = true,
        allowFuture = true
    )

    val endDatePickerLauncher = rememberDatePickerLauncher(
        onDateSelected = { millis ->
            val selectedDate = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            endDate = selectedDate.toFormattedDateString()
        },
        allowPast = true,
        allowFuture = true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizedContext.getString(R.string.meal_plan_screen_title)) },
                actions = {
                    IconButton(onClick = {
                        showFilterSheet = true
                        coroutineScope.launch { sheetState.show() }
                    }) {
                        Icon(Icons.Default.FilterList, contentDescription = localizedContext.getString(R.string.meal_plan_filter_button_description))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMealPlanClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = localizedContext.getString(R.string.meal_plan_add_button_description)
                )
            }
        }
    ) { paddingValues ->

        if (mealPlans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(localizedContext.getString(R.string.meal_plan_empty_message))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = paddingValues.calculateTopPadding(),
                        bottom = 0.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(mealPlans) { mealPlan ->
                    val totalMeals = mealPlan.meals.sumOf { it.meals.size }
                    val totalCalories = mealPlan.meals.sumOf { outerMeal ->
                        outerMeal.meals.sumOf { innerMeal -> innerMeal.calories.toIntOrNull() ?: 0 }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMealPlanSelected(mealPlan.id) }
                            .shadow(12.dp, shape = MaterialTheme.shapes.medium),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CompactMealPlanDateCard(
                                startDate = mealPlan.startDate,
                                endDate = mealPlan.endDate,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

                            Spacer(modifier = Modifier.height(4.dp))

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    LabeledIconTextRow(
                                        icon = Icons.Filled.Restaurant,
                                        iconTint = MaterialTheme.colorScheme.tertiary,
                                        label = localizedContext.getString(R.string.meal_plan_total_meals_label),
                                        value = totalMeals.toString()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    LabeledIconTextRow(
                                        icon = Icons.Filled.LocalFireDepartment,
                                        iconTint = MaterialTheme.colorScheme.error,
                                        label = localizedContext.getString(R.string.meal_plan_total_calories_label),
                                        value = totalCalories.toString()
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) showFilterSheet = false
                            }
                    },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizedContext.getString(R.string.meal_plan_filter_sheet_title),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                coroutineScope.launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!sheetState.isVisible) showFilterSheet = false
                                    }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = localizedContext.getString(R.string.action_close))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DateField(
                                label = localizedContext.getString(R.string.meal_plan_filter_start_date),
                                date = dateStringToMillis(startDate),
                                onClick = { startDatePickerLauncher.launch(dateStringToMillis(startDate)) },
                                dateFormatter = dateFormatter
                            )

                            DateField(
                                label = localizedContext.getString(R.string.meal_plan_filter_end_date),
                                date = dateStringToMillis(endDate),
                                onClick = { endDatePickerLauncher.launch(dateStringToMillis(endDate)) },
                                dateFormatter = dateFormatter
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                mealPlanViewModel.applyDateFilter(startDate, endDate)
                                coroutineScope.launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!sheetState.isVisible) showFilterSheet = false
                                    }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localizedContext.getString(R.string.meal_plan_filter_apply_button))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                // reset buttons
                                startDate = today
                                endDate = today

                                // Clear filters u ViewModelu
                                mealPlanViewModel.applyDateFilter("", "") // ili null, ovisno o tvojoj logici

                                // close sheet
                                coroutineScope.launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!sheetState.isVisible) showFilterSheet = false
                                    }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localizedContext.getString(R.string.meal_plan_filter_clear_button), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactMealPlanDateCard(
    startDate: String,
    endDate: String,
    modifier: Modifier = Modifier
) {
    val localizedContext = LocalLocalizedContext.current

    Column(modifier = modifier) {
        if (startDate == endDate) {
            CompactDateInfoBox(label = localizedContext.getString(R.string.meal_plan_single_date_label), date = formatDate(startDate))
        } else {
            CompactDateInfoBox(label = localizedContext.getString(R.string.meal_plan_date_range_start_label), date = formatDate(startDate))
            CompactDateInfoBox(label = localizedContext.getString(R.string.meal_plan_date_range_end_label), date = formatDate(endDate))
        }
    }
}

@Composable
fun CompactDateInfoBox(
    label: String,
    date: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LabeledIconTextRow(
                    icon = Icons.Filled.CalendarToday,
                    iconTint = MaterialTheme.colorScheme.primary,
                    value = formatDate(date)
                )
            }
        }
    }
}
