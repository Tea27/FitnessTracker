package com.tbasic.fitnesstracker.ui.screens.routine
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.Exercise
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.ImageWithFallback
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.ui.screens.exercise.ExerciseDetailScreenPlayer
import com.tbasic.fitnesstracker.vm.RoutinePlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun RoutinePlayerScreen(
    viewModel: RoutinePlayerViewModel,
    routine: UserRoutine,
    onFinish: (UserRoutine) -> Unit,
    onBack: () -> Unit,
    getExerciseById: suspend (String) -> Exercise?
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalLocalizedContext.current

    var showExerciseDetail by remember { mutableStateOf(false) }
    val currentSet = state.routine?.sets?.getOrNull(state.currentSetIndex)
    val currentExercise = currentSet?.exercises?.getOrNull(state.currentExerciseIndex)
    val originalRoutine = remember(routine) { routine }

    val exerciseState by produceState<Exercise?>(initialValue = null, key1 = currentExercise?.exerciseId) {
        value = currentExercise?.exerciseId?.let { getExerciseById(it) }
    }

    LaunchedEffect(currentExercise?.exerciseId) {
        val duration = currentExercise?.durationSeconds
        if (duration != null && duration > 0) {
            viewModel.setTimer(duration)
        }
    }

    LaunchedEffect(routine) {
        viewModel.start(routine)
        val firstExercise = routine.sets.firstOrNull()?.exercises?.firstOrNull()
        val duration = firstExercise?.durationSeconds
        if (duration != null && duration > 0) {
            viewModel.setTimer(duration)
        }
    }

    LaunchedEffect(state.isResting) {
        while (state.isResting) {
            delay(1000)
            viewModel.tickRest()
        }
    }

    LaunchedEffect(state.showTimer, state.timerSecondsLeft) {
        if (state.showTimer && state.timerSecondsLeft > 0) {
            delay(1000)
            viewModel.tickTimer()
        }
    }

    var showCongrats by remember { mutableStateOf(false) }
    var finalRoutineToFinish by remember { mutableStateOf<UserRoutine?>(null) }

    LaunchedEffect(state.isFinished, state.isResting) {
        if (state.isFinished && !state.isResting && state.routine != null && !showCongrats) {
            val finalRoutine = originalRoutine.copy(
                sets = viewModel.rebuildOriginalSets(),
                completed = true,
                startedAt = state.startedAt,
                finishedAt = state.finishedAt,
                durationPerformedMillis = state.durationPerformedMillis
            )

            finalRoutineToFinish = finalRoutine
            showCongrats = true
        }
    }

    val totalExercises = state.routine?.sets?.sumOf { it.exercises.size } ?: 0
    val completedExercises = state.currentSetIndex.takeIf { it < (state.routine?.sets?.size ?: 0) }
        ?.let { setIndex ->
            val doneInPreviousSets = state.routine?.sets?.take(setIndex)?.sumOf { it.exercises.size } ?: 0
            val doneInCurrentSet = state.currentExerciseIndex
            doneInPreviousSets + doneInCurrentSet
        } ?: 0
    val progress = if (totalExercises > 0) completedExercises.toFloat() / totalExercises else 0f

    if (showCongrats && finalRoutineToFinish != null) {
        CongratulationsScreen(
            onDone = {
                onFinish(finalRoutineToFinish!!)
            }
        )
        return
    }

    if (showExerciseDetail && exerciseState != null) {
        ExerciseDetailScreenPlayer(
            exercise = exerciseState!!,
            onBackClick = { showExerciseDetail = false }
        )
        return
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBarWithBack(title = routine.name, onBack = onBack)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr),
                        top = padding.calculateTopPadding(),
                        end = padding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.isResting -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                context.getString(R.string.rest_with_seconds, state.restSecondsLeft),
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.skipRest() }) {
                                Text(context.getString(R.string.skip_rest))
                            }
                        }
                    }

                    exerciseState == null -> {
                        CircularProgressIndicator()
                    }

                    else -> {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            item {
                                Text(
                                    text = context.getString(
                                        R.string.set_and_exercise_progress,
                                        state.currentSetIndex + 1,
                                        state.routine?.sets?.size ?: 0,
                                        state.currentExerciseIndex + 1,
                                        currentSet?.exercises?.size ?: 0
                                    ),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                ) {
                                    ImageWithFallback(
                                        exerciseId = exerciseState!!.id,
                                        modifier = Modifier.fillMaxSize(),
                                        enableGif = true
                                    )
                                }
                            }

                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showExerciseDetail = true }
                                ) {
                                    Text(
                                        text = exerciseState!!.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(20.dp)
                                    )
                                }
                            }

                            currentExercise?.reps?.let { reps ->
                                item {
                                    Text(
                                        text = context.getString(R.string.reps, reps),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            currentExercise?.durationSeconds?.let { duration ->
                                if (duration > 0) {
                                    item {
                                        Text(
                                            text = context.getString(R.string.duration_seconds, state.timerSecondsLeft),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }

                            item {
                                Button(onClick = { viewModel.goToNextExercise() }) {
                                    Text(context.getString(R.string.next))
                                }
                            }

                            item {
                                Button(onClick = { viewModel.skipExercise() }) {
                                    Text(context.getString(R.string.skip_exercise))
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
