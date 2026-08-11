package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.vm.FitnessGoal
import com.tbasic.fitnesstracker.vm.RoutineViewModel
import com.tbasic.fitnesstracker.vm.UserViewModel

@Composable
fun GoalCard(
    userViewModel: UserViewModel,
    routineViewModel: RoutineViewModel,
    onChangeGoalClick: () -> Unit = {},
    onTrackCaloriesClick: () -> Unit = {},
    onSeeCaloriesClick: () -> Unit = {}
) {
    val localizedContext = LocalLocalizedContext.current
    val goal by userViewModel.latestGoalEntry
    val goalType = goal?.goalType

    val currentWeightFloat = userViewModel.weight.toFloatOrNull() ?: 0f
    val targetWeightFloat = goal?.targetWeight ?: 0f
    val timePeriodWeeks = goal?.timePeriodWeeks ?: 0

    val goalText = when (goalType) {
        FitnessGoal.WEIGHT_LOSS -> localizedContext.getString(
            R.string.aim_to_reduce_weight,
            targetWeightFloat,
            timePeriodWeeks
        )
        FitnessGoal.MUSCLE_GAIN -> localizedContext.getString(
            R.string.gain_muscle_mass,
            targetWeightFloat,
            timePeriodWeeks
        )
        FitnessGoal.WORKOUT_COUNT -> localizedContext.getString(
            R.string.complete_workouts_weekly,
            userViewModel.workoutFrequency
        )
        else -> localizedContext.getString(R.string.no_goal)
    }

    val remaining = when (goalType) {
        FitnessGoal.WEIGHT_LOSS -> (currentWeightFloat - targetWeightFloat).coerceAtLeast(0f)
        FitnessGoal.MUSCLE_GAIN -> (targetWeightFloat - currentWeightFloat).coerceAtLeast(0f)
        else -> 0f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Add Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizedContext.getString(
                        R.string.your_current_goal
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    // modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            UniformImage(
                resId = R.drawable.arrow,
                contentDescription = "arr icon",
                size = 120.dp,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = goalText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (goalType == FitnessGoal.WEIGHT_LOSS || goalType == FitnessGoal.MUSCLE_GAIN) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LabeledIconRow(
                        Icons.Default.MyLocation,
                        localizedContext.getString(R.string.target_weight),
                        "$targetWeightFloat kg"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LabeledIconRow(
                        Icons.Default.FitnessCenter,
                        localizedContext.getString(R.string.current_weight),
                        "$currentWeightFloat kg"
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LabeledIconRow(
                        Icons.Default.Timer,
                        localizedContext.getString(R.string.remaining),
                        "$remaining kg"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            if (goalType == FitnessGoal.WORKOUT_COUNT) {
                val totalThisWeek = userViewModel.getWeeklyCompletedWorkouts(routineViewModel.userRoutines)
                val goalWorkouts = userViewModel.workoutFrequency.toIntOrNull() ?: 0
                val remainingWorkouts = (goalWorkouts - totalThisWeek).coerceAtLeast(0)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LabeledIconRow(
                        Icons.Default.FitnessCenter,
                        localizedContext.getString(R.string.this_week_workouts),
                        "$totalThisWeek"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LabeledIconRow(
                        Icons.Default.Timer,
                        localizedContext.getString(R.string.remaining_this_week),
                        "$remainingWorkouts"
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onChangeGoalClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            localizedContext.getString(R.string.change_goal)
                        )
                    }

                    if (goalType == FitnessGoal.WEIGHT_LOSS || goalType == FitnessGoal.MUSCLE_GAIN) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onTrackCaloriesClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(localizedContext.getString(R.string.track_calories))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onSeeCaloriesClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = localizedContext.getString(R.string.tracked_calories))
                        }
                    }
                }
            }
        }
    }
}
