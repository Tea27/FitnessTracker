package com.tbasic.fitnesstracker.ui.screens

import com.tbasic.fitnesstracker.vm.InputError
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.GoalSelector
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.vm.FitnessGoal
import com.tbasic.fitnesstracker.vm.UserViewModel



@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun UserFitnessGoalScreen(
    userViewModel: UserViewModel,
    onComplete: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalLocalizedContext.current
    var showInfoDialog by remember { mutableStateOf(false) }

    val errorMessage = when (userViewModel.inputError) {
        InputError.CurrentWeightMissing -> context.getString(R.string.error_enter_current_weight)
        InputError.TargetWeightInvalid -> context.getString(R.string.error_valid_target_weight)
        InputError.WeeksOutOfRange -> context.getString(R.string.error_weeks_range)
        InputError.WeightLossTooFast -> context.getString(R.string.error_weight_loss_too_fast)
        InputError.MuscleGainTooFast -> context.getString(R.string.error_weight_gain_too_fast)
        InputError.WorkoutFrequencyInvalid -> context.getString(R.string.error_valid_workout_frequency)
        null -> null
    }

    val buttonText = when (userViewModel.goal) {
        FitnessGoal.WORKOUT_COUNT -> context.getString(R.string.save_workout_goal)
        FitnessGoal.WEIGHT_LOSS -> context.getString(R.string.save_weight_loss_goal)
        FitnessGoal.MUSCLE_GAIN -> context.getString(R.string.save_muscle_gain_goal)
        null -> context.getString(R.string.save_workout_goal)
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBarWithBack(
                    title = context.getString(R.string.set_your_fitness_goal),
                    onBack = onBack
                )
            } else {
                TopAppBar(title = { Text(context.getString(R.string.set_your_fitness_goal)) })
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = context.getString(R.string.select_your_goal),
                style = MaterialTheme.typography.titleMedium
            )

            GoalSelector(
                selectedGoal = userViewModel.goal ?: FitnessGoal.WORKOUT_COUNT,
                onSelect = { userViewModel.onGoalChange(it) }
            )

            AnimatedContent(
                targetState = userViewModel.goal,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { goal ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        when (goal) {
                            FitnessGoal.WORKOUT_COUNT -> {
                                OutlinedTextField(
                                    value = userViewModel.workoutFrequency,
                                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\$"))) userViewModel.onWorkoutFrequencyChange(it) },
                                    label = { Text(context.getString(R.string.workout_frequency_label)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = userViewModel.workoutFrequency.toIntOrNull() == null && userViewModel.workoutFrequency.isNotEmpty(),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
                                )
                            }

                            FitnessGoal.WEIGHT_LOSS, FitnessGoal.MUSCLE_GAIN -> {
                                OutlinedTextField(
                                    value = userViewModel.weight,
                                    onValueChange = { if (it.isEmpty() || it.matches(userViewModel.decimalNumberRegex)) userViewModel.onWeightChange(it) },
                                    label = { Text(context.getString(R.string.current_weight_label)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = userViewModel.weight.toFloatOrNull() == null && userViewModel.weight.isNotEmpty(),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = userViewModel.targetWeight,
                                    onValueChange = { if (it.isEmpty() || it.matches(userViewModel.decimalNumberRegex)) userViewModel.onTargetWeightChange(it) },
                                    label = { Text(context.getString(R.string.target_weight_label)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = userViewModel.targetWeight.toFloatOrNull() == null && userViewModel.targetWeight.isNotEmpty(),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = { Icon(Icons.Default.Timeline, contentDescription = null) }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = userViewModel.timePeriodWeeks,
                                    onValueChange = { if (it.isEmpty() || it.matches(userViewModel.integerNumberRegex)) userViewModel.onTimePeriodWeeksChange(it) },
                                    label = { Text(context.getString(R.string.time_period_label)) },
                                    trailingIcon = {
                                        IconButton(onClick = { showInfoDialog = true }) {
                                            Icon(Icons.Default.Info, contentDescription = context.getString(R.string.time_period_info_title))
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = userViewModel.timePeriodWeeks.toIntOrNull() == null && userViewModel.timePeriodWeeks.isNotEmpty(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            null -> {}
                        }
                    }
                }
            }

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Summary card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = context.getString(R.string.your_goal_summary), style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = when (userViewModel.goal) {
                            FitnessGoal.WORKOUT_COUNT -> "${userViewModel.workoutFrequency} ${context.getString(R.string.per_week)}"
                            FitnessGoal.WEIGHT_LOSS, FitnessGoal.MUSCLE_GAIN ->
                                "${userViewModel.weight} kg → ${userViewModel.targetWeight} kg in ${userViewModel.timePeriodWeeks} weeks"
                            null -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (userViewModel.validateGoalInputs()) {
                        userViewModel.saveGoalEntry()
                        onComplete()
                    }
                },
                enabled = userViewModel.validateGoalInputs(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(context.getString(R.string.time_period_info_title)) },
            text = { Text(context.getString(R.string.time_period_info_description)) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(context.getString(R.string.got_it))
                }
            }
        )
    }
}
