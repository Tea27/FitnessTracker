package com.tbasic.fitnesstracker.ui.navigation

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tbasic.fitnesstracker.data.mapper.toPredefinedFormat
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.navigation.CaloriesOverviewScreen
import com.tbasic.fitnesstracker.navigation.ChangePassword
import com.tbasic.fitnesstracker.navigation.ExerciseDetail
import com.tbasic.fitnesstracker.navigation.Exercises
import com.tbasic.fitnesstracker.navigation.FitnessGoalScreen
import com.tbasic.fitnesstracker.navigation.GoalsOverviewScreen
import com.tbasic.fitnesstracker.navigation.Home
import com.tbasic.fitnesstracker.navigation.Login
import com.tbasic.fitnesstracker.navigation.Main
import com.tbasic.fitnesstracker.navigation.MakeMealPlan
import com.tbasic.fitnesstracker.navigation.MealPlanDetail
import com.tbasic.fitnesstracker.navigation.MealPlanView
import com.tbasic.fitnesstracker.navigation.NewRoutine
import com.tbasic.fitnesstracker.navigation.PhysicalData
import com.tbasic.fitnesstracker.navigation.RoutineDetail
import com.tbasic.fitnesstracker.navigation.RoutinePlayer
import com.tbasic.fitnesstracker.navigation.Routines
import com.tbasic.fitnesstracker.navigation.TrackCaloriesScreen
import com.tbasic.fitnesstracker.navigation.User
import com.tbasic.fitnesstracker.ui.auth.ChangePasswordScreen
import com.tbasic.fitnesstracker.ui.components.MealPlanLoadingScreen
import com.tbasic.fitnesstracker.ui.screens.CalorieTrackScreen
import com.tbasic.fitnesstracker.ui.screens.CaloriesOverviewScreen
import com.tbasic.fitnesstracker.ui.screens.GoalsOverviewScreen
import com.tbasic.fitnesstracker.ui.screens.HomeScreen
import com.tbasic.fitnesstracker.ui.screens.MealPlanDetailScreen
import com.tbasic.fitnesstracker.ui.screens.MealPlanScreen
import com.tbasic.fitnesstracker.ui.screens.MealPlanSelectionScreen
import com.tbasic.fitnesstracker.ui.screens.MealPlanViewScreen
import com.tbasic.fitnesstracker.ui.screens.UserFitnessGoalScreen
import com.tbasic.fitnesstracker.ui.screens.UserScreen
import com.tbasic.fitnesstracker.ui.screens.exercise.ExerciseDetailScreen
import com.tbasic.fitnesstracker.ui.screens.exercise.ExerciseListScreen
import com.tbasic.fitnesstracker.ui.screens.routine.NewRoutineScreen
import com.tbasic.fitnesstracker.ui.screens.routine.PredefinedRoutineDetailScreen
import com.tbasic.fitnesstracker.ui.screens.routine.RoutinePlayerScreen
import com.tbasic.fitnesstracker.ui.screens.routine.RoutinesScreen
import com.tbasic.fitnesstracker.utils.atStartOfDayInMillis
import com.tbasic.fitnesstracker.vm.AuthState
import com.tbasic.fitnesstracker.vm.AuthViewModel
import com.tbasic.fitnesstracker.vm.CalorieTrackViewModel
import com.tbasic.fitnesstracker.vm.ExerciseViewModel
import com.tbasic.fitnesstracker.vm.MealPlanViewModel
import com.tbasic.fitnesstracker.vm.RoutinePlayerViewModel
import com.tbasic.fitnesstracker.vm.RoutineViewModel
import com.tbasic.fitnesstracker.vm.UserViewModel

@Composable
fun MainScreen(
    viewModel: ExerciseViewModel,
    authViewModel: AuthViewModel,
    routineViewModel: RoutineViewModel,
    playerViewModel: RoutinePlayerViewModel,
    userViewModel: UserViewModel,
    rootNavController: NavHostController,
    mealPlanViewModel: MealPlanViewModel,
    calorieTrackViewModel: CalorieTrackViewModel
) {
    val navController = rememberNavController()

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val userId = (authState as AuthState.Success).userId
            routineViewModel.setUser(userId)
            mealPlanViewModel.setUser(userId)
            userViewModel.setUserId(userId)
            calorieTrackViewModel.setUserId(userId)
        }
    }

    val localizedContext = LocalLocalizedContext.current

    CompositionLocalProvider(LocalLocalizedContext provides localizedContext) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController = navController) },
            content = { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = Home.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(Home.route) {
                        HomeScreen(
                            routineViewModel = routineViewModel,
                            userViewModel = userViewModel,
                            onRoutineClick = { routineId ->
                                navController.navigate("${RoutineDetail.route}/$routineId?isUser=true")
                            },
                            onStartRoutine = { selected ->
                                // routineViewModel.saveUserRoutine(selected)
                                navController.navigate(
                                    RoutinePlayer.createRoute(
                                        selected.id
                                    )
                                )
                            },
                            onMakeRoutineClick = {
                                navController.navigate(NewRoutine.route)
                            },
                            onExploreTemplatesClick = {
                                routineViewModel.switchToPredefinedRoutines()
                                navController.navigate(Routines.route)
                            },
                            onDateSelected = { selectedDate ->
                                val hasCompleted = routineViewModel.hasCompletedOn(selectedDate)

                                if (hasCompleted) {
                                    val millis = selectedDate.atStartOfDayInMillis()

                                    navController.navigate("${Routines.route}?filterDate=$millis")
                                }
                            },
                            onMakeMealPlanClick = {
                                navController.navigate(MakeMealPlan.route)
                            },
                            onSetGoalClick = {
                                navController.navigate(FitnessGoalScreen.route)
                            },
                            onTrackCaloriesClick = { navController.navigate(TrackCaloriesScreen.route) },
                            onSeeCaloriesClick = { navController.navigate(CaloriesOverviewScreen.route) } ,
                            navToProfile = {rootNavController.navigate(PhysicalData.createRoute(fromEdit = true))}
                        )
                    }
                    composable(Exercises.route) {
                        ExerciseListScreen(
                            viewModel,
                            onExerciseClick = { exercise ->
                                viewModel.setSelectedExercise(exercise)
                                navController.navigate(ExerciseDetail.route)
                            }
                        )
                    }

                    composable(ExerciseDetail.route) {
                        ExerciseDetailScreen(
                            viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable("${Routines.route}?filterDate={filterDate}") { backStackEntry ->
                        val filterDateString = backStackEntry.arguments?.getString("filterDate")
                        val filterDateMillis = filterDateString?.toLongOrNull()

                        if (filterDateMillis != null) {
                            routineViewModel.switchToUserRoutinesDone(
                                startDate = filterDateMillis,
                                endDate = filterDateMillis
                            )
                        }

                        RoutinesScreen(
                            routineViewModel = routineViewModel,
                            onRoutineClick = { routineId, isUser ->
                                navController.navigate("${RoutineDetail.route}/$routineId?isUser=$isUser")
                            },
                            onAddNewRoutineClick = {
                                navController.navigate(NewRoutine.route)
                            }
                        )
                    }

                    composable(
                        "${RoutineDetail.route}/{routineId}?isUser={isUser}",
                        arguments = listOf(
                            navArgument("routineId") { type = NavType.StringType },
                            navArgument("isUser") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) { backStackEntry ->
                        val routineId = backStackEntry.arguments?.getString("routineId")
                        val isUser = backStackEntry.arguments?.getBoolean("isUser") ?: false

                        val userRoutine = if (isUser) {
                            routineViewModel.getUserRoutineById(
                                routineId ?: ""
                            )
                        } else {
                            null
                        }
                        val routine = userRoutine?.toPredefinedFormat()
                            ?: routineViewModel.getPredefinedRoutineById(routineId ?: "")

                        routine?.let {
                            PredefinedRoutineDetailScreen(
                                routine = it,
                                isUserRoutine = isUser,
                                userRoutine = userRoutine,
                                onAddToMine = { selected ->
                                    val userRoutineNew =
                                        routineViewModel.convertToUserRoutine(selected, false)
                                    routineViewModel.saveUserRoutine(userRoutineNew)
                                    userViewModel.checkIfGoalIsCompleted(routineViewModel.userRoutines.plus(userRoutineNew))
                                    routineViewModel.switchToUserRoutinesTodo()
                                    navController.navigate(Routines.route) {
                                        popUpTo(Routines.route) { inclusive = true }
                                    }
                                },
                                onUpdateUserRoutine = { updated ->
                                    val userRoutineUpdated =
                                        routineViewModel.convertToUserRoutine(updated, true)
                                    routineViewModel.saveUserRoutine(userRoutineUpdated)
                                    routineViewModel.switchToUserRoutinesTodo()
                                    navController.popBackStack()
                                },
                                onDeleteUserRoutine = { toDelete ->
                                    val userRoutineToDelete =
                                        routineViewModel.convertToUserRoutine(toDelete, true)
                                    routineViewModel.deleteUserRoutine(userRoutineToDelete)
                                    when (routineViewModel.userRoutineFilter) {
                                        RoutineViewModel.Companion.UserRoutineFilter.TODO -> routineViewModel.switchToUserRoutinesTodo()
                                        RoutineViewModel.Companion.UserRoutineFilter.COMPLETED -> routineViewModel.switchToUserRoutinesDone()
                                    }
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() },
                                getExerciseById = { id -> viewModel.getExerciseById(id) },
                                onStart = { selected ->
                                    val userRoutineToStart =
                                        routineViewModel.convertToUserRoutine(selected, true)
                                    routineViewModel.saveUserRoutine(userRoutineToStart)
                                    userViewModel.checkIfGoalIsCompleted(routineViewModel.userRoutines.plus(userRoutineToStart))
                                    navController.navigate(
                                        RoutinePlayer.createRoute(
                                            userRoutineToStart.id
                                        )
                                    )
                                },
                                onExerciseClick = { exercise ->
                                    viewModel.setSelectedExercise(exercise)
                                    navController.navigate(ExerciseDetail.route)
                                }
                            )
                        } ?: Text("Routine not found")
                    }

                    composable(User.route) {
                        UserScreen(
                            authViewModel = authViewModel,
                            userViewModel = userViewModel,
                            onLogout = {
                                rootNavController.navigate(Login.route) {
                                    popUpTo(Main.route) { inclusive = true }
                                }
                            },
                            onChangePassword = {
                                navController.navigate(ChangePassword.route)
                            },
                            onEditPhysicalData = {
                                rootNavController.navigate(PhysicalData.createRoute(fromEdit = true))
                            },
                            onSeeCaloriesClick = { navController.navigate(CaloriesOverviewScreen.route) },
                            onSeePreviousGoalsClick = { navController.navigate(GoalsOverviewScreen.route) }
                        )
                    }

                    composable(NewRoutine.route) {
                        NewRoutineScreen(
                            onSaveRoutine = { editableRoutine ->
                                val userRoutine =
                                    routineViewModel.convertToUserRoutine(editableRoutine, false)
                                routineViewModel.saveUserRoutine(userRoutine)
                                routineViewModel.switchToUserRoutinesTodo()
                                navController.navigate(Routines.route) {
                                    popUpTo(Routines.route) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(RoutinePlayer.route) { backStackEntry ->
                        val routineId = backStackEntry.arguments?.getString("routineId")
                        Log.d("ovo je routineId", routineId.toString())
                        val routine = routineViewModel.userRoutines.find { it.id == routineId }
                        Log.d("ovo je routineId 2", routine?.id.toString())

                        routine?.let {
                            RoutinePlayerScreen(
                                viewModel = playerViewModel,
                                routine = it,
                                onFinish = { finishedRoutine ->
                                    Log.d("ovo je routineId 3", finishedRoutine?.id.toString())

                                    routineViewModel.saveUserRoutine(finishedRoutine) // Save with existing ID
                                    userViewModel.checkIfGoalIsCompleted(routineViewModel.userRoutines.plus(finishedRoutine))
                                    routineViewModel.switchToUserRoutinesDone() // Set done filter
                                    navController.navigate(Routines.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onBack = { navController.popBackStack() },
                                getExerciseById = { id -> viewModel.getExerciseById(id) }
                            )
                        } ?: Text("Routine not found")
                    }

                    composable(ChangePassword.route) {
                        ChangePasswordScreen(
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = MakeMealPlan.route,
                        arguments = listOf(
                            navArgument("returnRoute") {
                                type = NavType.StringType
                                defaultValue = Home.route // ako nema parametra, vrati na Home
                                nullable = true
                            }
                        )
                    ) { backStackEntry ->

                        val returnRoute = backStackEntry.arguments?.getString("returnRoute") ?: Home.route

                        val isLoading by mealPlanViewModel.isLoading.collectAsState()
                        val isPlanReady by mealPlanViewModel.isPlanReady.collectAsState()

                        when {
                            isLoading -> {
                                MealPlanLoadingScreen()
                            }
                            isPlanReady -> {
                                MealPlanScreen(
                                    mealPlanViewModel = mealPlanViewModel,
                                    onDiscard = {
                                        mealPlanViewModel.discardPlan()
                                        navController.navigate(returnRoute) {
                                            popUpTo(returnRoute) { inclusive = false }
                                        }
                                    },
                                    onSave = {
                                        mealPlanViewModel.saveMealPlan()
                                        navController.navigate(MealPlanView.route) {
                                            popUpTo(Home.route) { inclusive = false }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                            else -> {
                                MealPlanSelectionScreen(
                                    mealPlanViewModel = mealPlanViewModel,
                                    userViewModel = userViewModel,
                                    onBack = { navController.popBackStack() },
                                    onStartGenerating = {
                                        mealPlanViewModel.fetchMealPlanWithFallbackAndUpdateState()
                                    }
                                )
                            }
                        }
                    }
                    composable(MealPlanView.route) {
                        MealPlanViewScreen(
                            mealPlanViewModel = mealPlanViewModel,
                            onMealPlanSelected = { mealPlanId ->
                                navController.navigate("meal_plan_detail/$mealPlanId")
                            },
                            onAddMealPlanClick = {
                                navController.navigate("make_meal_plan?returnRoute=${MealPlanView.route}")
                            }
                        )
                    }

                    composable(FitnessGoalScreen.route) {
                        UserFitnessGoalScreen(
                            userViewModel = userViewModel,
                            onComplete = {
                                // Vraćanje na prethodni ekran ili dalje po logici
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(TrackCaloriesScreen.route) {
                        CalorieTrackScreen(
                            calorieTrackViewModel,
                            onBack = { navController.popBackStack() },
                            onSaved = { navController.popBackStack() }
                        )
                    }

                    composable(CaloriesOverviewScreen.route) {
                        CaloriesOverviewScreen(
                            calorieTrackViewModel,
                            onBack = { navController.popBackStack() },
                            onAddClick = { navController.navigate(TrackCaloriesScreen.route) }
                        )
                    }

                    composable(GoalsOverviewScreen.route) {
                        GoalsOverviewScreen(
                            userViewModel,
                            onBack = { navController.popBackStack() }
                            // onAddClick = { navController.navigate(TrackCaloriesScreen.route) },
                        )
                    }

                    composable(
                        route = MealPlanDetail.route,
                        arguments = listOf(
                            navArgument("mealPlanId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val mealPlanId = backStackEntry.arguments?.getString("mealPlanId") ?: ""

                        MealPlanDetailScreen(
                            mealPlanViewModel = mealPlanViewModel,
                            mealPlanId = mealPlanId,
                            onBack = { navController.popBackStack() },
                            onDelete = {
                                mealPlanViewModel.deleteMealPlan(mealPlanId)
                                navController.popBackStack()
                            },
                            onDownload = { context ->

                                val mealPlan = mealPlanViewModel.getMealPlanById(mealPlanId)
                                if (mealPlan != null) {
                                    val pdfBytes = mealPlanViewModel.generateMealPlanPdf(mealPlan)
                                    val fileName = "MealPlan_${mealPlan.startDate}_to_${mealPlan.endDate}.pdf"
                                    val uri = mealPlanViewModel.savePdfToFile(context, pdfBytes, fileName)
                                    if (uri != null) {
                                        // Otvori PDF ili prikaži toast
                                        Toast.makeText(context, "PDF saved!", Toast.LENGTH_SHORT).show()

                                        // Otvori PDF putem intent-a
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Meal plan not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}
