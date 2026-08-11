package com.tbasic.fitnesstracker.ui.screens.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.EditableExercise
import com.tbasic.fitnesstracker.data.EditableRoutine
import com.tbasic.fitnesstracker.data.EditableRoutineSet
import com.tbasic.fitnesstracker.data.Exercise
import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.data.RoutineDay
import com.tbasic.fitnesstracker.data.RoutineDay.Companion.fromDisplayName
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.data.mapper.copyForRepeat
import com.tbasic.fitnesstracker.data.mapper.localize
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.ExerciseCardExtended
import com.tbasic.fitnesstracker.ui.components.LabeledIconTextRow
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.utils.getTodayRoutineDay
import java.util.UUID

@Composable
fun PredefinedRoutineDetailScreen(
    routine: PredefinedRoutine,
    isUserRoutine: Boolean,
    onAddToMine: (PredefinedRoutine) -> Unit,
    onUpdateUserRoutine: (PredefinedRoutine) -> Unit = {},
    onDeleteUserRoutine: (PredefinedRoutine) -> Unit = {},
    onStart: (PredefinedRoutine) -> Unit = {},
    onBack: () -> Unit,
    getExerciseById: suspend (String) -> Exercise?,
    userRoutine: UserRoutine? = null,
    onExerciseClick: (Exercise) -> Unit

) {
    val context = LocalLocalizedContext.current
    val localizedRoutine = remember(routine, context) {
        routine.localize(context)
    }

    val routineDayEnum = RoutineDay.entries.find { it.name.equals(localizedRoutine.day, ignoreCase = true) }
    val displayDay = routineDayEnum?.getDisplayName(context) ?: localizedRoutine.day

    var editableRoutine by remember { mutableStateOf<EditableRoutine?>(null) }
    var showEditScreen by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isCompleted = isUserRoutine && userRoutine?.completed == true

    LaunchedEffect(localizedRoutine) {
        val editableSets = routine.sets.mapNotNull { set ->
            val editableExercises = set.exercises.mapNotNull { re ->
                getExerciseById(re.exerciseId)?.let { exercise ->
                    EditableExercise(
                        exercise = exercise,
                        reps = re.reps ?: 10,
                        durationSeconds = re.durationSeconds ?: 30
                    )
                }
            }
            if (editableExercises.isNotEmpty()) {
                EditableRoutineSet(
                    repeat = set.repeat,
                    restAfterSet = set.restAfterSet,
                    exercises = editableExercises
                )
            } else {
                null
            }
        }

        editableRoutine = EditableRoutine(
            id = routine.id,
            name = localizedRoutine.name,
            day = displayDay,
            description = localizedRoutine.description,
            durationMinutes = localizedRoutine.durationMinutes,
            sets = editableSets
        )
    }

    if (showEditScreen && editableRoutine != null) {
        EditRoutineScreen(
            initialRoutine = editableRoutine!!,
            onSave = { updatedRoutine ->
                if (isUserRoutine) {
                    onUpdateUserRoutine(updatedRoutine)
                } else {
                    onAddToMine(updatedRoutine.copy(id = UUID.randomUUID().toString()))
                }
                showEditScreen = false
            },
            onBack = { showEditScreen = false }
        )
    } else {
        val today = remember { fromDisplayName(context, getTodayRoutineDay().getDisplayName(context)) }
        val routineDay = RoutineDay.fromDisplayName(context, localizedRoutine.day) ?: localizedRoutine.day

        Scaffold(
            topBar = {
                TopAppBarWithBack(
                    title = localizedRoutine.name,
                    onBack = onBack,
                    actions = {
                        if (isUserRoutine) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = context.getString(R.string.routine_delete_title))
                            }
                        }
                    }
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when {
                        isUserRoutine && isCompleted -> {
                            Button(
                                onClick = { onAddToMine(routine.copyForRepeat()) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(context.getString(R.string.button_repeat))
                            }
                        }

                        isUserRoutine -> {
                            OutlinedButton(
                                onClick = { editableRoutine?.let { showEditScreen = true } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(context.getString(R.string.button_edit))
                            }

                            Button(
                                onClick = { onStart(routine) },
                                enabled = routineDay.equals(today, ignoreCase = true),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(context.getString(R.string.button_start))
                            }
                        }

                        else -> {
                            Button(
                                onClick = { editableRoutine?.let { showEditScreen = true } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(context.getString(R.string.button_add_to_my_routine))
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    if (localizedRoutine.description.isNotBlank()) {
                        Text(
                            text = localizedRoutine.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                item {
                    LabeledIconTextRow(
                        icon = Icons.Default.AccessTime,
                        iconTint = Color(0xFF4A90E2),
                        label = if (isUserRoutine) context.getString(R.string.label_estimated_duration) else context.getString(R.string.label_duration),
                        value = "${localizedRoutine.durationMinutes} min"
                    )
                }

                item {
                    LabeledIconTextRow(
                        icon = Icons.Default.CalendarToday,
                        iconTint = Color(0xFF7ED957),
                        label = context.getString(R.string.label_day),
                        value = displayDay
                    )
                }

                if (isCompleted) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        userRoutine?.durationPerformedMillis?.let {
                            LabeledIconTextRow(
                                icon = Icons.Default.CheckCircle,
                                iconTint = Color(0xFF66BB6A),
                                label = context.getString(R.string.label_performed_duration),
                                value = "${it / 60000} min"
                            )
                        }

                        userRoutine?.finishedAt?.let {
                            val formatted = formatDate(it)
                            LabeledIconTextRow(
                                icon = Icons.Default.EventAvailable,
                                iconTint = Color(0xFF42A5F5),
                                label = context.getString(R.string.label_finished_at),
                                value = formatted
                            )
                        }

                        val doneCount = userRoutine?.sets?.sumOf { set ->
                            set.exercises.sumOf { exercise -> exercise.done.count { it } }
                        } ?: 0

                        val totalCount = userRoutine?.sets?.sumOf { set ->
                            set.exercises.sumOf { exercise -> exercise.done.size }
                        } ?: 0

                        if (totalCount > 0) {
                            val percent = (doneCount * 100f / totalCount).toInt()
                            LabeledIconTextRow(
                                icon = Icons.Default.FitnessCenter,
                                iconTint = Color(0xFFAB47BC),
                                label = context.getString(R.string.label_exercises_completed),
                                value = "$percent%"
                            )
                            val progress = doneCount / totalCount.toFloat()
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF66BB6A)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        LabeledIconTextRow(
                            icon = Icons.Default.Check,
                            iconTint = Color(0xFF4CAF50),
                            label = context.getString(R.string.label_status),
                            value = context.getString(R.string.status_completed)
                        )
                    }
                }

                item {
                    Text(
                        text = context.getString(R.string.exercises_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }

                editableRoutine?.sets?.forEachIndexed { setIndex, set ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = context.getString(R.string.set_x_repeat, setIndex + 1, set.repeat),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                set.exercises.forEach { editableExercise ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        ExerciseCardExtended(
                                            exercise = editableExercise.exercise.localize(context),
                                            onClick = { onExerciseClick(editableExercise.exercise) },
                                            modifier = Modifier.fillMaxWidth(),
                                            reps = editableExercise.reps,
                                            durationSeconds = editableExercise.durationSeconds
                                        )

                                        if (isUserRoutine && isCompleted) {
                                            val userExercise = userRoutine?.sets
                                                ?.getOrNull(setIndex)
                                                ?.exercises
                                                ?.getOrNull(set.exercises.indexOf(editableExercise))

                                            val doneList = userExercise?.done ?: emptyList()
                                            val doneCountForExercise = doneList.count { it }
                                            val totalCountForExercise = doneList.size

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                            ) {
                                                Text(
                                                    text = "$doneCountForExercise / $totalCountForExercise ${context.getString(R.string.label_done)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )

                                                if (doneCountForExercise == totalCountForExercise && totalCountForExercise > 0) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = context.getString(R.string.label_done),
                                                        tint = Color(0xFF66BB6A),
                                                        modifier = Modifier.padding(start = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (set.restAfterSet != null && set.restAfterSet > 0) {
                                    Text(
                                        text = context.getString(R.string.rest_after_set, set.restAfterSet),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(text = context.getString(R.string.routine_delete_title))
                },
                text = {
                    Text(context.getString(R.string.routine_delete_message))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteUserRoutine(routine)
                            showDeleteDialog = false
                        }
                    ) {
                        Text(context.getString(R.string.routine_delete_yes))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(context.getString(R.string.routine_delete_no))
                    }
                }
            )
        }
    }
}
