package com.tbasic.fitnesstracker.ui.components

import android.util.Log
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.utils.isGoalInCurrentWeek
import com.tbasic.fitnesstracker.vm.UserViewModel

@Composable
fun NoGoalCard(
    userViewModel: UserViewModel,
    onSetGoalClick: () -> Unit
) {
    val localizedContext = LocalLocalizedContext.current
    val goal by userViewModel.latestGoalEntry
    val goalType = goal?.goalType
    Log.d("ovo je goal", goalType.toString())
    val hasCompletedGoalThisWeek = userViewModel.allGoals.collectAsState().value
        .any { isGoalInCurrentWeek(it.startDate) && it.isCompleted }
    val isCurrentWeek = goal?.startDate?.let { isGoalInCurrentWeek(it) } ?: false

    val isCompleted = goal?.isCompleted == true
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
            // Naslov s ikonom
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Add Goal Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizedContext.getString(R.string.no_goal_set),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            UniformImage(
                resId = R.drawable.arrow,
                contentDescription = "Set Goal Icon",
                size = 120.dp,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (hasCompletedGoalThisWeek) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizedContext.getString(R.string.goal_congratulations),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = localizedContext.getString(R.string.no_goal_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dugme u zasebnoj kartici, kao kod GoalCard
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
                        onClick = onSetGoalClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = localizedContext.getString(R.string.set_your_goal))
                    }
                }
            }
        }
    }
}
