package com.tbasic.fitnesstracker.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.BmiCardSection
import com.tbasic.fitnesstracker.ui.components.GoalCard
import com.tbasic.fitnesstracker.ui.components.NoGoalCard
import com.tbasic.fitnesstracker.ui.components.ProgressCard
import com.tbasic.fitnesstracker.ui.components.UniformImage
import com.tbasic.fitnesstracker.ui.screens.routine.RoutineCardUser
import com.tbasic.fitnesstracker.utils.getStartOfWeek
import com.tbasic.fitnesstracker.vm.RoutineViewModel
import com.tbasic.fitnesstracker.vm.UserViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@Composable
fun HomeScreen(
    routineViewModel: RoutineViewModel,
    userViewModel: UserViewModel,
    onRoutineClick: (String) -> Unit,
    onStartRoutine: (UserRoutine) -> Unit = {},
    onMakeRoutineClick: () -> Unit,
    onExploreTemplatesClick: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onMakeMealPlanClick: () -> Unit,
    onSetGoalClick: () -> Unit,
    onTrackCaloriesClick: () -> Unit,
    onSeeCaloriesClick: () -> Unit,
    navToProfile: () -> Unit
) {
    val context = LocalLocalizedContext.current
    val goal by userViewModel.latestGoalEntry
    val allGoals by userViewModel.allGoals.collectAsState()

    val userRoutinesToday = remember(routineViewModel.userRoutines) {
        routineViewModel.getUserRoutinesForToday(context)
    }

    val todayDay = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var selectedMonth by remember { mutableStateOf(todayDay.monthNumber) }
    var selectedYear by remember { mutableStateOf(todayDay.year) }

    val completedRoutines = routineViewModel.getCompletedRoutinesGroupedByDay(selectedYear, selectedMonth)
    Log.d("ovo je today", todayDay.toString())
    var currentWeekStart by remember { mutableStateOf(getStartOfWeek(todayDay)) }
    val weeklyData = remember(currentWeekStart, routineViewModel.userRoutines) {
        routineViewModel.getWeeklyCompletedRoutines(currentWeekStart)
    }
    Log.d("ovo je current week start", currentWeekStart.toString())
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding(),
                    bottom = 0.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Naslov i logotip
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color = colorResource(id = R.color.splash_background)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.splash_background)
                    )
                }
            }

            // Rutine za danas
            item {
                if (userRoutinesToday.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Icon",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = context.getString(R.string.training_today),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = context.getString(R.string.workout_subtitle),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            UniformImage(
                                resId = R.drawable.workout_plan_3,
                                contentDescription = "wp icon",
                                size = 120.dp,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            userRoutinesToday.forEach { routine ->
                                RoutineCardUser(
                                    routine = routine,
                                    onClick = { onRoutineClick(routine.id) },
                                    showStartButton = true,
                                    onStart = { onStartRoutine(routine) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                } else {
                    // Poruka kad nema rutina
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Event Icon",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = context.getString(R.string.no_training_today),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = context.getString(R.string.no_routine_subtitle),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            UniformImage(
                                resId = R.drawable.gym,
                                contentDescription = "Gym icon",
                                size = 120.dp,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(26.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(
                                        onClick = onMakeRoutineClick,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = context.getString(R.string.make_your_routine))
                                    }

                                    Button(
                                        onClick = onExploreTemplatesClick,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = context.getString(R.string.explore_templates))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable { onMakeMealPlanClick() },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Icon",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = context.getString(R.string.ai_meal_plan_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        UniformImage(
                            resId = R.drawable.robot_chef,
                            contentDescription = "robot icon",
                            size = 120.dp,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(26.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = context.getString(R.string.ai_meal_plan_description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onMakeMealPlanClick,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(context.getString(R.string.make_plan))
                                }
                            }
                        }
                    }
                }
            }

            item {
                val allCompleted = allGoals.all { it.isCompleted == true }

                if (goal == null || allCompleted) {
                    NoGoalCard(onSetGoalClick = onSetGoalClick, userViewModel = userViewModel)
                } else {
                    GoalCard(
                        userViewModel = userViewModel,
                        routineViewModel = routineViewModel,
                        onChangeGoalClick = onSetGoalClick,
                        onTrackCaloriesClick = onTrackCaloriesClick,
                        onSeeCaloriesClick = onSeeCaloriesClick
                    )
                }
            }

            // Mini kalendar
            item {
                ProgressCard(
                    showCalendar = true,
                    year = selectedYear,
                    month = selectedMonth,
                    completedRoutinesByDay = completedRoutines,
                    onDateSelected = { selectedDate -> onDateSelected(selectedDate) },
                    onMonthChanged = { newYear, newMonth ->
                        selectedYear = newYear
                        selectedMonth = newMonth
                    }
                )
            }

            item {
                ProgressCard(
                    showCalendar = false,
                    weeklyData = weeklyData,
                    weekStartDate = currentWeekStart,
                    onPreviousWeek = {
                        currentWeekStart = currentWeekStart.minus(7, DateTimeUnit.DAY)
                    },
                    onNextWeek = { currentWeekStart = currentWeekStart.plus(7, DateTimeUnit.DAY) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                BmiCardSection(viewModel = userViewModel, navToProfile = navToProfile)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
