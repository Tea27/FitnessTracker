package com.tbasic.fitnesstracker.ui.screens.routine

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.RoutineDay
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.LabeledIconTextRow

@Composable
fun RoutineCardUser(
    routine: UserRoutine,
    onClick: () -> Unit,
    showStartButton: Boolean = false,
    onStart: (() -> Unit)? = null
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
            Text(
                text = routine.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LabeledIconTextRow(
                icon = Icons.Default.AccessTime,
                iconTint = Color(0xFF4A90E2),
                label = context.getString(R.string.label_estimated_duration),
                value = "${routine.durationMinutes} min"
            )

            Spacer(modifier = Modifier.height(4.dp))

            LabeledIconTextRow(
                icon = Icons.Default.CalendarToday,
                iconTint = Color(0xFF7ED957),
                label = context.getString(R.string.label_day),
                value = displayDay
            )

            if (routine.completed) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                routine.durationPerformedMillis?.let {
                    LabeledIconTextRow(
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFF66BB6A),
                        label = context.getString(R.string.label_performed_duration),
                        value = "${it / 60000} min"
                    )
                }

                routine.finishedAt?.let {
                    val formatted = formatDate(it)
                    LabeledIconTextRow(
                        icon = Icons.Default.EventAvailable,
                        iconTint = Color(0xFF42A5F5),
                        label = context.getString(R.string.label_finished_at),
                        value = formatted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = routine.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (showStartButton && onStart != null) {
                Button(
                    onClick = { onStart() },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = context.getString(R.string.button_start))
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}
