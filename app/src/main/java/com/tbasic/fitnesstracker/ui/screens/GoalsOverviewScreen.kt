package com.tbasic.fitnesstracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.local.GoalEntryEntity
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.ui.components.localizedDisplayName
import com.tbasic.fitnesstracker.vm.FitnessGoal
import com.tbasic.fitnesstracker.vm.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GoalsOverviewScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val goals by viewModel.allGoals.collectAsState()
    val localizedContext = LocalLocalizedContext.current

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = localizedContext.getString(R.string.title_previous_goals),
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            if (goals.isEmpty()) {
                item {
                    EmptyGoalsCard(
                        localizedContext.getString(R.string.empty_goals_title),
                        localizedContext.getString(R.string.empty_goals_subtitle)
                    )
                }
            } else {
                items(goals) { goal ->
                    GoalEntryCard(
                        goal = goal,
                        localizedContext = localizedContext,
                        onDeleteClick = { viewModel.deleteGoal(goal.userId, goal.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyGoalsCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GoalEntryCard(
    goal: GoalEntryEntity,
    localizedContext: android.content.Context,
    onDeleteClick: (GoalEntryEntity) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(localizedContext.getString(R.string.dialog_delete_goal_title)) },
            text = { Text(localizedContext.getString(R.string.dialog_delete_goal_message)) },
            confirmButton = {
                Text(
                    localizedContext.getString(R.string.action_delete),
                    color = Color.Red,
                    modifier = Modifier
                        .clickable {
                            showDialog = false
                            onDeleteClick(goal)
                        }
                        .padding(8.dp)
                )
            },
            dismissButton = {
                Text(
                    localizedContext.getString(R.string.action_cancel),
                    modifier = Modifier
                        .clickable { showDialog = false }
                        .padding(8.dp)
                )
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Goal Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.goalType.localizedDisplayName(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val str = if (goal.goalType == FitnessGoal.WORKOUT_COUNT) {
                            localizedContext.getString(
                                R.string.goal_target_workouts_label,
                                goal.workoutFrequency
                            )
                        } else {
                            localizedContext.getString(
                                R.string.goal_target_label,
                                goal.targetWeight.toString()
                            )
                        }

                        Text(
                            text = str,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        val completionStatus = if (goal.isCompleted == true) localizedContext.getString(R.string.yes) else localizedContext.getString(R.string.no)

                        Text(
                            text = localizedContext.getString(R.string.goal_completed_label, completionStatus),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = localizedContext.getString(R.string.goal_date_label, dateFormat.format(Date(goal.startDate))),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showDialog = true }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = localizedContext.getString(R.string.action_delete),
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = localizedContext.getString(R.string.action_delete),
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
