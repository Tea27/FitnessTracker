package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.vm.RoutineViewModel

@Composable
fun FilterSegmentedControl(
    selectedFilter: RoutineViewModel.Companion.UserRoutineFilter,
    onFilterSelected: (RoutineViewModel.Companion.UserRoutineFilter) -> Unit
) {
    val filters = RoutineViewModel.Companion.UserRoutineFilter.values()
    val localizedContext = LocalLocalizedContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor)
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (filter) {
                        RoutineViewModel.Companion.UserRoutineFilter.TODO -> localizedContext.getString(R.string.filter_todo)
                        RoutineViewModel.Companion.UserRoutineFilter.COMPLETED -> localizedContext.getString(R.string.filter_completed)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor
                )
            }
        }
    }
}
