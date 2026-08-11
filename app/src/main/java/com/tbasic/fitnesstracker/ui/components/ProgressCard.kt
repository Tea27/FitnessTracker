package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import kotlinx.datetime.LocalDate

@Composable
fun ProgressCard(
    modifier: Modifier = Modifier,
    showCalendar: Boolean,
    weeklyData: List<Int> = emptyList(),
    weekStartDate: LocalDate? = null,
    onPreviousWeek: (() -> Unit)? = null,
    onNextWeek: (() -> Unit)? = null,
    // Parametri za mini calendar
    year: Int = 0,
    month: Int = 0,
    completedRoutinesByDay: Map<Int, List<UserRoutine>> = emptyMap(),
    onDateSelected: ((LocalDate) -> Unit)? = null,
    onMonthChanged: ((Int, Int) -> Unit)? = null
) {
    val context = LocalLocalizedContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (showCalendar) {
                    context.getString(R.string.monthly_progress)
                } else {
                    context.getString(R.string.weekly_progress)
                },

                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showCalendar) {
                        MiniCalendar(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .heightIn(max = 400.dp),
                            year = year,
                            month = month,
                            completedRoutinesByDay = completedRoutinesByDay,
                            onDateSelected = onDateSelected ?: {},
                            onMonthChanged = onMonthChanged ?: { _, _ -> }
                        )
                    } else {
                        // Sigurno ne null parametri za chart
                        if (weekStartDate != null && onPreviousWeek != null && onNextWeek != null) {
                            WeeklyProgressLineChart(
                                data = weeklyData,
                                weekStartDate = weekStartDate,
                                onPreviousWeek = onPreviousWeek,
                                onNextWeek = onNextWeek,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // fallback ili prazan sadržaj
                            Text(text = "Podaci nisu dostupni")
                        }
                    }
                }
            }
        }
    }
}
