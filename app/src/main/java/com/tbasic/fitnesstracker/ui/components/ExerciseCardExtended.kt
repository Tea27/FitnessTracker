package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.Exercise
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext

@Composable
fun ExerciseCardExtended(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    reps: Int? = null,
    durationSeconds: Int? = null,
    onClick: (() -> Unit)? = null
) {
    val context = LocalLocalizedContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            ImageWithFallback(
                exerciseId = exercise.id,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                enableGif = false
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.replaceFirstChar { it.uppercaseChar() },
                    // TODO("dodat elipsize za naslov ako je dug")
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))
                LabeledIconRow(
                    icon = Icons.Default.FitnessCenter,
                    label = context.getString(R.string.equipment),
                    value = exercise.equipment
                )
                Spacer(modifier = Modifier.height(4.dp))
                LabeledIconRow(
                    icon = Icons.Default.MyLocation,
                    label = context.getString(R.string.target),
                    value = exercise.target
                )

                val hasReps = reps != null && reps > 0
                val hasDuration = durationSeconds != null && durationSeconds > 0

                val repsLabel = context.getString(R.string.label_reps)
                val durationLabel = context.getString(R.string.label_duration)

                if (hasReps || hasDuration) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = listOfNotNull(
                            if (hasReps) "$repsLabel: $reps" else null,
                            if (hasDuration) "$durationLabel: ${durationSeconds}s" else null
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LabeledIconRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
