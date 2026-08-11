package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.utils.getWeekdayInitials
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

@Composable
fun WeeklyProgressLineChart(
    data: List<Int>,
    weekStartDate: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxVal = (data.maxOrNull() ?: 1).coerceAtLeast(1)
    val pointRadius = 6.dp
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalLocalizedContext.current
    val language = context.resources.configuration.locales[0].language
    val weekdayInitials = getWeekdayInitials(language)
    Column(modifier = modifier.fillMaxWidth()) {
        // Navigacija tjednima
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousWeek) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prethodni tjedan")
            }
            val weekEndDate = weekStartDate.plus(6, DateTimeUnit.DAY)
            Text(
                text = "${weekStartDate.dayOfMonth}.${weekStartDate.monthNumber}. ${weekStartDate.year}. - ${weekEndDate.dayOfMonth}.${weekEndDate.monthNumber}. ${weekEndDate.year}.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            IconButton(onClick = onNextWeek) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Sljedeći tjedan")
            }
        }

        // Line Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val textColor = MaterialTheme.colorScheme.onSurfaceVariant

            Canvas(modifier = Modifier.fillMaxSize()) {
                val widthStep = size.width / (data.size - 1).coerceAtLeast(1)
                val heightMax = size.height * 0.8f
                val points = data.mapIndexed { i, value ->
                    Offset(
                        x = i * widthStep,
                        y = heightMax - (value / maxVal.toFloat()) * heightMax
                    )
                }

                // Crtanje linija između točkica
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = primaryColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 4f
                    )
                }

                // Crtanje točkica
                points.forEach {
                    drawCircle(
                        color = primaryColor,
                        radius = pointRadius.toPx(),
                        center = it
                    )
                }

                // Crtanje oznaka dana ispod točkica, centrirano
                val textPaint = android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 30f // prilagodi veličinu teksta
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                }

                points.forEachIndexed { i, point ->
                    drawContext.canvas.nativeCanvas.drawText(
                        weekdayInitials[i],
                        point.x,
                        heightMax + 80f, // malo ispod najniže točke
                        textPaint
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
