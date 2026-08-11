package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.data.RoutineDay
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext

@Composable
fun RoutineCard(
    routine: PredefinedRoutine,
    onClick: () -> Unit
) {
    val context = LocalLocalizedContext.current
    val routineDayEnum = RoutineDay.entries.find { it.name.equals(routine.day, ignoreCase = true) }
    val displayDay = routineDayEnum?.getDisplayName(context) ?: routine.day

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = routine.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration row
            LabeledIconTextRow(
                icon = Icons.Default.AccessTime,
                iconTint = Color(0xFF4A90E2),
                label = context.getString(R.string.label_duration),
                value = "${routine.durationMinutes} min"
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Day row
            LabeledIconTextRow(
                icon = Icons.Default.CalendarToday,
                iconTint = Color(0xFF7ED957),
                label = context.getString(R.string.label_day),
                value = displayDay
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = routine.description,
                // TODO("trimat ili mu stavit da nesmi unit puno znakova")
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview
@Composable
fun PreviewRoutineCard() {
    val dummy = PredefinedRoutine(
        id = "1",
        name = "Cardio Blast",
        durationMinutes = 20,
        day = "Saturday",
        sets = emptyList()
    )
    MaterialTheme {
        RoutineCard(dummy, onClick = {})
    }
}
