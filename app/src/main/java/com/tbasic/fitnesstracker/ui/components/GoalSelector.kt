package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.vm.FitnessGoal

@Composable
fun GoalSelector(
    selectedGoal: FitnessGoal?,
    onSelect: (FitnessGoal) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val localizedContext = LocalLocalizedContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(MaterialTheme.shapes.large)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large
            )
            .clickable { expanded = true }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = selectedGoal?.localizedDisplayName() ?: localizedContext.getString(R.string.goal_muscle_gain)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            FitnessGoal.entries.forEach { goal ->
                DropdownMenuItem(
                    text = { Text(goal.localizedDisplayName()) },
                    onClick = {
                        onSelect(goal)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FitnessGoal.localizedDisplayName(): String {
    val localizedContext = LocalLocalizedContext.current

    return when (this) {
        FitnessGoal.MUSCLE_GAIN -> localizedContext.getString(R.string.goal_muscle_gain)
        FitnessGoal.WEIGHT_LOSS -> localizedContext.getString(R.string.goal_weight_loss)
        FitnessGoal.WORKOUT_COUNT -> localizedContext.getString(R.string.goal_workout_count)
    }
}
