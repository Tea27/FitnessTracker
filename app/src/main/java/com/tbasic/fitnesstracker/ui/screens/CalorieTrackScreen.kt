package com.tbasic.fitnesstracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.DateField
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.ui.components.rememberDatePickerLauncher
import com.tbasic.fitnesstracker.vm.CalorieTrackViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalorieTrackScreen(
    calorieTrackViewModel: CalorieTrackViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val localizedContext = LocalLocalizedContext.current
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var caloriesText by remember { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val datePicker = rememberDatePickerLauncher(
        onDateSelected = { millis ->
            if (millis > System.currentTimeMillis()) {
                scope.launch {
                    snackbar.showSnackbar(localizedContext.getString(R.string.future_date_error))
                }
            } else {
                dateMillis = millis
            }
        },
        allowPast = true,
        allowFuture = false
    )

    Scaffold(
        topBar = { TopAppBarWithBack(title = localizedContext.getString(R.string.track_calories), onBack = onBack) },
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = localizedContext.getString(R.string.track_calories_header),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DateField(
                        label = localizedContext.getString(R.string.meal_plan_single_date_label),
                        date = dateMillis,
                        onClick = { datePicker.launch(dateMillis) },
                        dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val caloriesInt = caloriesText.toIntOrNull()
                    val isCaloriesValid = caloriesInt != null && caloriesInt > 0

                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = { caloriesText = it },
                        label = { Text(localizedContext.getString(R.string.calories_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = caloriesText.isNotEmpty() && !isCaloriesValid,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (caloriesText.isNotEmpty() && !isCaloriesValid) {
                        Text(
                            text = when {
                                caloriesInt == 0 -> localizedContext.getString(R.string.calories_zero_error)
                                else -> localizedContext.getString(R.string.calories_invalid_error)
                            },
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (isCaloriesValid && caloriesInt != null) {
                                calorieTrackViewModel.addEntry(
                                    dateMillis,
                                    caloriesInt,
                                    onSuccess = onSaved,
                                    onFailure = {
                                        scope.launch {
                                            snackbar.showSnackbar("Error: $it")
                                        }
                                    }
                                )
                            } else {
                                scope.launch {
                                    snackbar.showSnackbar(localizedContext.getString(R.string.calorie_input_error))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(localizedContext.getString(R.string.save_entry))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = localizedContext.getString(R.string.average_burn_tip),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
