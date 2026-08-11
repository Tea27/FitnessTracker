package com.tbasic.fitnesstracker.ui.screens.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.EditableExercise
import com.tbasic.fitnesstracker.data.EditableRoutine
import com.tbasic.fitnesstracker.data.EditableRoutineSet
import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.data.RoutineDay
import com.tbasic.fitnesstracker.data.mapper.toPredefinedRoutine
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.AddExerciseBottomSheet
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.vm.EditRoutineViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditRoutineScreen(
    initialRoutine: EditableRoutine,
    onSave: (PredefinedRoutine) -> Unit,
    onBack: () -> Unit,
    viewModel: EditRoutineViewModel = hiltViewModel()
) {
    val localizedContext = LocalLocalizedContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.setRoutine(initialRoutine)
    }

    val routine by viewModel.routine.collectAsState()
    val showSearch by viewModel.showSearch.collectAsState()
    val selectedSetIndex by viewModel.selectedSetIndex.collectAsState()
    val listState = rememberLazyListState()
    var initialLoadDone by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(routine.sets.size) {
        delay(500)
        if (initialLoadDone) {
            listState.animateScrollToItem(routine.sets.lastIndex + 1)
        } else {
            initialLoadDone = true
        }
    }

    fun isRoutineValid(): Boolean {
        if (routine.name.isBlank()) return false
        if (routine.day.isBlank()) return false
        if (routine.durationMinutes <= 0) return false
        if (routine.sets.isEmpty()) return false
        if (routine.sets.any { set ->
            (set.repeat <= 0 && (set.restAfterSet ?: 0) <= 0) ||
                set.exercises.isEmpty() ||
                set.exercises.any { ex -> ex.reps <= 0 && ex.durationSeconds <= 0 }
        }
        ) {
            return false
        }
        return true
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarWithBack(
                title = localizedContext.getString(R.string.edit_routine),
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (isRoutineValid()) {
                                val routineDay = RoutineDay.fromDisplayName(localizedContext, routine.day)
                                val converted = routine.toPredefinedRoutine(routineDay)
                                onSave(converted)
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        localizedContext.getString(R.string.validation_error_snackbar)
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = localizedContext.getString(R.string.edit_routine))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 12.dp
                )
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = listState
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = routine.name,
                                onValueChange = viewModel::updateName,
                                label = { Text(localizedContext.getString(R.string.routine_name)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                isError = routine.name.isBlank()
                            )
                            if (routine.name.isBlank()) {
                                Text(
                                    localizedContext.getString(R.string.name_empty_error),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        OutlinedTextField(
                                            value = routine.durationMinutes.takeIf { it > 0 }?.toString() ?: "",
                                            onValueChange = { viewModel.updateDuration(it.toIntOrNull() ?: 0) },
                                            label = { Text(localizedContext.getString(R.string.duration_minutes)) },
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true,
                                            isError = routine.durationMinutes <= 0
                                        )
                                        if (routine.durationMinutes <= 0) {
                                            Text(
                                                localizedContext.getString(R.string.duration_error),
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 4.dp, start = 12.dp)
                                            )
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { expanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                routine.day.takeIf { it.isNotEmpty() }
                                                    ?: localizedContext.getString(R.string.select_day)
                                            )
                                        }

                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            RoutineDay.entries.forEach { day ->
                                                DropdownMenuItem(
                                                    text = { Text(day.getDisplayName(localizedContext)) },
                                                    onClick = {
                                                        viewModel.updateDay(day.getDisplayName(localizedContext))
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = routine.description,
                                onValueChange = viewModel::updateDescription,
                                label = { Text(localizedContext.getString(R.string.description)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                shape = RoundedCornerShape(16.dp),
                                maxLines = 5
                            )
                        }
                    }
                }

                itemsIndexed(routine.sets) { index, set ->
                    SetCard(
                        set = set,
                        index = index,
                        isLast = index == routine.sets.lastIndex,
                        onMoveUp = { viewModel.moveSet(index, index - 1) },
                        onMoveDown = { viewModel.moveSet(index, index + 1) },
                        onRepeatChange = { viewModel.updateSetRepeat(index, it) },
                        onRestChange = { viewModel.updateSetRestAfter(index, it) },
                        onAddExercise = {
                            viewModel.setSelectedSetIndex(index)
                            viewModel.setShowSearch(true)
                        },
                        onExerciseMove = { from, to -> viewModel.moveExercise(index, from, to) },
                        onExerciseDelete = { exIndex -> viewModel.deleteExercise(index, exIndex) },
                        onRepsChange = { exIndex, reps -> viewModel.updateExerciseReps(index, exIndex, reps) },
                        onDurationChange = { exIndex, duration -> viewModel.updateExerciseDuration(index, exIndex, duration) },
                        onDeleteSet = { viewModel.deleteSet(index) }
                    )
                }
            }

            Button(
                onClick = { viewModel.addSet() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(localizedContext.getString(R.string.add_set))
            }
        }

        if (showSearch) {
            AddExerciseBottomSheet(
                onDismiss = {
                    viewModel.setShowSearch(false)
                    viewModel.setSelectedSetIndex(null)
                },
                onExerciseSelected = { exercise ->
                    selectedSetIndex?.let { setIndex ->
                        viewModel.addExercise(setIndex, exercise)
                    }
                    viewModel.setShowSearch(false)
                    viewModel.setSelectedSetIndex(null)
                }
            )
        }
    }
}

@Composable
private fun SetCard(
    set: EditableRoutineSet,
    index: Int,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRepeatChange: (Int) -> Unit,
    onRestChange: (Int?) -> Unit,
    onAddExercise: () -> Unit,
    onExerciseMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onExerciseDelete: (Int) -> Unit,
    onRepsChange: (Int, Int) -> Unit,
    onDurationChange: (Int, Int) -> Unit,
    onDeleteSet: () -> Unit
) {
    val localizedContext = LocalLocalizedContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = localizedContext.getString(R.string.set_x, index + 1),
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    IconButton(
                        enabled = index > 0,
                        onClick = onMoveUp
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = localizedContext.getString(R.string.move_set_up))
                    }

                    IconButton(
                        enabled = !isLast,
                        onClick = onMoveDown
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = localizedContext.getString(R.string.move_set_down))
                    }

                    IconButton(onClick = onDeleteSet) {
                        Icon(Icons.Default.Delete, contentDescription = localizedContext.getString(R.string.delete_set))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = if (set.repeat > 0) set.repeat.toString() else "",
                onValueChange = { onRepeatChange(it.toIntOrNull() ?: 0) },
                label = { Text(localizedContext.getString(R.string.repeat_count)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = set.restAfterSet?.toString() ?: "",
                onValueChange = {
                    val value = it.toIntOrNull()
                    onRestChange(value)
                },
                label = { Text(localizedContext.getString(R.string.rest_after_sec)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                set.exercises.forEachIndexed { exIndex, exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onMoveUp = { if (exIndex > 0) onExerciseMove(exIndex, exIndex - 1) },
                        onMoveDown = { if (exIndex < set.exercises.lastIndex) onExerciseMove(exIndex, exIndex + 1) },
                        onDelete = { onExerciseDelete(exIndex) },
                        onRepsChange = { reps -> onRepsChange(exIndex, reps) },
                        onDurationChange = { duration -> onDurationChange(exIndex, duration) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onAddExercise,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = localizedContext.getString(R.string.add_exercise))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizedContext.getString(R.string.add_exercise))
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: EditableExercise,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onRepsChange: (Int) -> Unit,
    onDurationChange: (Int) -> Unit
) {
    val localizedContext = LocalLocalizedContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = exercise.exercise.name, style = MaterialTheme.typography.titleSmall)

                Row {
                    IconButton(onClick = onMoveUp) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = localizedContext.getString(R.string.move_exercise_up))
                    }
                    IconButton(onClick = onMoveDown) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = localizedContext.getString(R.string.move_exercise_down))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = localizedContext.getString(R.string.delete_exercise))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = if (exercise.reps > 0) exercise.reps.toString() else "",
                    onValueChange = { onRepsChange(it.toIntOrNull() ?: 0) },
                    label = { Text(localizedContext.getString(R.string.label_reps)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = if (exercise.durationSeconds > 0) exercise.durationSeconds.toString() else "",
                    onValueChange = { onDurationChange(it.toIntOrNull() ?: 0) },
                    label = { Text(localizedContext.getString(R.string.duration_sec)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
