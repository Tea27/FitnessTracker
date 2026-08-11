package com.tbasic.fitnesstracker.ui.screens.exercise

import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.mapper.localize
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.ImageWithFallback
import com.tbasic.fitnesstracker.ui.components.LabeledIconRow
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.vm.ExerciseViewModel

@Composable
fun ExerciseDetailScreen(
    viewModel: ExerciseViewModel,
    onBackClick: () -> Unit
) {
    val localizedContext = LocalLocalizedContext.current
    val exercise = viewModel.getSelectedExercise()?.localize(localizedContext)

    if (exercise != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TopAppBarWithBack(title = "", onBack = onBackClick)
            }

            // GIF at top
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
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

            // Exercise name
            item {
                Text(
                    text = exercise.name.replaceFirstChar { it.uppercaseChar() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Equipment / Target / Body Part
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LabeledIconRow(
                        icon = Icons.Default.FitnessCenter,
                        label = localizedContext.getString(R.string.equipment),
                        value = exercise.equipment
                    )
                    LabeledIconRow(
                        icon = Icons.Default.MyLocation,
                        label = localizedContext.getString(R.string.target),
                        value = exercise.target
                    )
                    LabeledIconRow(
                        icon = Icons.Default.BarChart,
                        label = localizedContext.getString(R.string.body_part),
                        value = exercise.bodyPart
                    )
                }
            }

            // Instructions
            item {
                Text(
                    text = localizedContext.getString(R.string.instructions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (exercise.description.isEmpty()) {
                item {
                    Text(
                        text = localizedContext.getString(R.string.no_instructions),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(exercise.description) { step ->
                    Text("• $step", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
