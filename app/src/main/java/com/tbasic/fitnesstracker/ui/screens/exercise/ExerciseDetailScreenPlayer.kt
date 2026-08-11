package com.tbasic.fitnesstracker.ui.screens.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.data.Exercise
import com.tbasic.fitnesstracker.ui.components.ImageWithFallback
import com.tbasic.fitnesstracker.ui.components.LabeledIconRow
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack

@Composable
fun ExerciseDetailScreenPlayer(
    exercise: Exercise,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopAppBarWithBack(title = exercise.name, onBack = onBackClick)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                ImageWithFallback(
                    exerciseId = exercise.id,
                    modifier = Modifier.fillMaxSize(),
                    enableGif = true
                )
            }
        }

        item {
            Text(
                text = exercise.name.replaceFirstChar { it.uppercaseChar() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LabeledIconRow(Icons.Default.FitnessCenter, "Equipment", exercise.equipment)
                LabeledIconRow(Icons.Default.MyLocation, "Target", exercise.target)
                LabeledIconRow(Icons.Default.BarChart, "Body Part", exercise.bodyPart)
            }
        }

        item {
            Text(
                text = "Instructions:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (exercise.description.isEmpty()) {
            item {
                Text("No instructions available.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(exercise.description) { step ->
                Text("• $step", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
