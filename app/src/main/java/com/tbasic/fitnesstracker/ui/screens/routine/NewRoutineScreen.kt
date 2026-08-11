package com.tbasic.fitnesstracker.ui.screens.routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tbasic.fitnesstracker.data.EditableRoutine
import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.utils.getTodayRoutineDay
import java.util.UUID

@Composable
fun NewRoutineScreen(
    onSaveRoutine: (PredefinedRoutine) -> Unit,
    onBack: () -> Unit
) {
    val localizedContext = LocalLocalizedContext.current

    val today = remember { getTodayRoutineDay().getDisplayName(localizedContext) }

    val emptyRoutine = remember {
        EditableRoutine(
            id = UUID.randomUUID().toString(),
            name = "",
            durationMinutes = 0,
            day = today,
            description = "",
            sets = emptyList()
        )
    }

    EditRoutineScreen(
        initialRoutine = emptyRoutine,
        onSave = onSaveRoutine,
        onBack = onBack
    )
}
