package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.utils.getWeekdayInitials
import kotlinx.datetime.*

@Composable
fun MiniCalendar(
    modifier: Modifier = Modifier,
    year: Int,
    month: Int,
    completedRoutinesByDay: Map<Int, List<UserRoutine>>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (newYear: Int, newMonth: Int) -> Unit
) {
    val context = LocalLocalizedContext.current

    val daysInMonth = remember(year, month) {
        val firstDayOfMonth = LocalDate(year, month, 1)
        val nextMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH)
        val lastDay = nextMonth.minus(1, DateTimeUnit.DAY)
        lastDay.dayOfMonth
    }

    val language = context.resources.configuration.locales[0].language
    val weekdayInitials = getWeekdayInitials(language)

    val firstDayOfWeekOffset = remember(year, month) {
        val firstDayOfMonth = LocalDate(year, month, 1)
        val dayOfWeek = (firstDayOfMonth.dayOfWeek.isoDayNumber + 6) % 7
        dayOfWeek
    }

    val totalGridItems = remember(firstDayOfWeekOffset, daysInMonth) {
        val total = firstDayOfWeekOffset + daysInMonth
        if (total % 7 == 0) total else total + (7 - total % 7)
    }

    Column(
        modifier = modifier
    ) {
        // Zaglavlje s mjesec/godina i strelicama
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = {
                val current = LocalDate(year, month, 1)
                val previous = current.minus(1, DateTimeUnit.MONTH)
                onMonthChanged(previous.year, previous.monthNumber)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = "${getLocalizedMonthName(month)} $year",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            IconButton(onClick = {
                val current = LocalDate(year, month, 1)
                val next = current.plus(1, DateTimeUnit.MONTH)
                onMonthChanged(next.year, next.monthNumber)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            userScrollEnabled = false
        ) {
            items(weekdayInitials.size) { index ->
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = weekdayInitials[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            items(totalGridItems) { index ->
                if (index < firstDayOfWeekOffset || index >= firstDayOfWeekOffset + daysInMonth) {
                    Spacer(modifier = Modifier.size(40.dp))
                } else {
                    val day = index - firstDayOfWeekOffset + 1
                    val completedRoutines = completedRoutinesByDay[day]
                    val completedCount = completedRoutines?.size ?: 0
                    val hasCompleted = completedCount > 0

                    val clickableModifier = if (hasCompleted) {
                        Modifier.clickable {
                            onDateSelected(LocalDate(year, month, day))
                        }
                    } else {
                        Modifier
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .then(clickableModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (hasCompleted) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasCompleted) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        if (completedCount > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = 0.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = completedCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getLocalizedMonthName(month: Int): String {
    val context = LocalLocalizedContext.current
    val months = context.resources.getStringArray(R.array.month_names_nominative)
    return months[month - 1]
}

private fun currentMonthName(month: Int): String {
    return Month(month).name.lowercase().replaceFirstChar { it.uppercase() }
}
