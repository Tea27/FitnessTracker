package com.tbasic.fitnesstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.vm.RoutineViewModel

@Composable
fun TabSegmentedControl(
    selectedTab: RoutineViewModel.Companion.RoutineTab,
    onTabSelected: (RoutineViewModel.Companion.RoutineTab) -> Unit
) {
    val localizedContext = LocalLocalizedContext.current

    val templatesText = localizedContext.getString(R.string.templates)
    val myRoutinesText = localizedContext.getString(R.string.my_routines)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        RoutineViewModel.Companion.RoutineTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            OutlinedButton(
                onClick = { onTabSelected(tab) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text(
                    text = if (tab == RoutineViewModel.Companion.RoutineTab.PREDEFINED) templatesText else myRoutinesText
                )
            }
        }
    }
}
