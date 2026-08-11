package com.tbasic.fitnesstracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tbasic.fitnesstracker.navigation.Login
import com.tbasic.fitnesstracker.navigation.Main
import com.tbasic.fitnesstracker.navigation.PhysicalData
import com.tbasic.fitnesstracker.navigation.Register
import com.tbasic.fitnesstracker.ui.auth.LoginScreen
import com.tbasic.fitnesstracker.ui.auth.RegisterScreen
import com.tbasic.fitnesstracker.ui.screens.UserPhysicalDataScreen
import com.tbasic.fitnesstracker.vm.AuthViewModel
import com.tbasic.fitnesstracker.vm.CalorieTrackViewModel
import com.tbasic.fitnesstracker.vm.ExerciseViewModel
import com.tbasic.fitnesstracker.vm.MealPlanViewModel
import com.tbasic.fitnesstracker.vm.RoutinePlayerViewModel
import com.tbasic.fitnesstracker.vm.RoutineViewModel
import com.tbasic.fitnesstracker.vm.UserViewModel

@Composable
fun AppNavigation(
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val exerciseViewModel: ExerciseViewModel = hiltViewModel()
    val routineViewModel: RoutineViewModel = hiltViewModel()
    val playerViewModel: RoutinePlayerViewModel = hiltViewModel()
    val mealPlanViewModel: MealPlanViewModel = hiltViewModel()
    val calorieTrackViewModel: CalorieTrackViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
    val startRoute = if (isLoggedIn) Main.route else Login.route

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    if (userViewModel.isProfileComplete()) {
                        navController.navigate(Main.route)
                    } else {
                        navController.navigate(PhysicalData.route)
                    }
                },
                onNavigateToRegister = { navController.navigate(Register.route) },
                authViewModel = authViewModel
            )
        }
        composable(Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    if (userViewModel.isProfileComplete()) {
                        navController.navigate(Main.route)
                    } else {
                        navController.navigate(PhysicalData.route)
                    }
                },
                onNavigateToLogin = { navController.navigate(Login.route) },
                authViewModel = authViewModel
            )
        }
        composable(Main.route) {
            MainScreen(
                viewModel = exerciseViewModel,
                authViewModel = authViewModel,
                routineViewModel = routineViewModel,
                playerViewModel = playerViewModel,
                userViewModel = userViewModel,
                rootNavController = navController,
                mealPlanViewModel = mealPlanViewModel,
                calorieTrackViewModel = calorieTrackViewModel
            )
        }
        composable(
            PhysicalData.routeWithArgs,
            arguments = listOf(
                navArgument("fromEdit") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val fromEdit = backStackEntry.arguments?.getBoolean("fromEdit") ?: false
            UserPhysicalDataScreen(
                userViewModel = userViewModel,
                onComplete = {
                    navController.navigate(Main.route) {
                        popUpTo(PhysicalData.route) { inclusive = true }
                    }
                },
                onBack = if (fromEdit) {
                    {
                        userViewModel.refreshUserData()
                        navController.popBackStack()
                    }
                } else {
                    null
                }
            )
        }
    }
}
