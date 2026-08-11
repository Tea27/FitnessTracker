package com.tbasic.fitnesstracker.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.DateField
import com.tbasic.fitnesstracker.ui.components.GenderSelector
import com.tbasic.fitnesstracker.ui.components.LocationSearchField
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.ui.components.rememberDatePickerLauncher
import com.tbasic.fitnesstracker.utils.fetchPhotonSuggestions
import com.tbasic.fitnesstracker.utils.toDateLongOrNull
import com.tbasic.fitnesstracker.utils.toFormattedDateString
import com.tbasic.fitnesstracker.vm.UserViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPhysicalDataScreen(
    userViewModel: UserViewModel,
    onComplete: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val localizedContext = LocalLocalizedContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // State za birthDate kao Long? (timestamp)
    var birthDateLong by remember {
        mutableStateOf(userViewModel.birthDate.toDateLongOrNull(dateFormatter))
    }

    // DatePicker launcher
    val birthDatePicker = rememberDatePickerLauncher(
        onDateSelected = { selectedDate ->
            birthDateLong = selectedDate
            userViewModel.onBirthDateChange(selectedDate.toFormattedDateString(dateFormatter) ?: "")
        },
        allowPast = true,
        allowFuture = false
    )

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBarWithBack(
                    title = "Your Physical Data",
                    onBack = onBack
                )
            } else {
                TopAppBar(title = { Text("Your Physical Data") })
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)

                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Please fill in all the fields to help us improve your user experience and personalize your profile.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        OutlinedTextField(
                            value = userViewModel.weight,
                            onValueChange = { input ->
                                if (input.isEmpty() || userViewModel.decimalNumberRegex.matches(input)) {
                                    userViewModel.onWeightChange(input)
                                }
                            },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = userViewModel.weight.toFloatOrNull()?.let { it < 30 || it > 300 } == true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        )

                        OutlinedTextField(
                            value = userViewModel.height,
                            onValueChange = { input ->
                                if (input.isEmpty() || userViewModel.integerNumberRegex.matches(input)) {
                                    userViewModel.onHeightChange(input)
                                }
                            },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = userViewModel.height.toFloatOrNull()?.let { it < 100 || it > 250 } == true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        )

                        LocationSearchField(
                            query = userViewModel.location,
                            onQueryChange = { userViewModel.onLocationChange(it) },
                            onLocationSelected = { selectedLocation ->
                                userViewModel.onLocationChange(selectedLocation) // update samo sa punim odabirom
                                Log.d("LocationSearch", "User selected location: $selectedLocation")
                            },
                            fetchSuggestions = { query -> fetchPhotonSuggestions(query) }
                        )

                        Text(
                            text = "Select your gender",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        GenderSelector(
                            selectedGender = userViewModel.gender,
                            onSelect = { userViewModel.onGenderChange(it) }
                        )

                        DateField(
                            label = "Birth Date (e.g. 1990-05-20)",
                            date = birthDateLong,
                            onClick = { birthDatePicker.launch(birthDateLong) },
                            onClear = {
                                birthDateLong = null
                                userViewModel.onBirthDateChange("")
                            },
                            dateFormatter = dateFormatter
                        )

//                        Text(
//                            text = "What is your fitness goal?",
//                            style = MaterialTheme.typography.labelLarge,
//                            color = MaterialTheme.colorScheme.onSurface
//                        )
//                        GoalSelector(
//                            selectedGoal = userViewModel.goal,
//                            onSelect = { userViewModel.onGoalChange(it) }
//                        )

//                        if (userViewModel.goal == FitnessGoal.WEIGHT_LOSS || userViewModel.goal == FitnessGoal.MUSCLE_GAIN) {
//                            OutlinedTextField(
//                                value = userViewModel.targetWeight,
//                                onValueChange = { input ->
//                                    if (input.isEmpty() || userViewModel.decimalNumberRegex.matches(input)) {
//                                        userViewModel.onTargetWeightChange(input)
//                                    }
//                                },
//                                label = { Text("Target weight (kg)") },
//                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                isError = userViewModel.targetWeight.toFloatOrNull()?.let { target ->
//                                    val current = userViewModel.weight.toFloatOrNull() ?: return@let true
//                                    when (userViewModel.goal) {
//                                        FitnessGoal.WEIGHT_LOSS -> target >= current
//                                        FitnessGoal.MUSCLE_GAIN -> target <= current
//                                        else -> false
//                                    }
//                                } == true,
//                                modifier = Modifier.fillMaxWidth(),
//                                shape = MaterialTheme.shapes.large
//                            )
//                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { userViewModel.updatePhysicalData { onComplete() } },
                    enabled = userViewModel.isPhysicalDataValid(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Save")
                }
            }

            if (onBack == null) {
                item {
                    TextButton(
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Skip for now")
                    }
                }
            }
        }
    }
}
