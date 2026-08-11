package com.tbasic.fitnesstracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.tbasic.fitnesstracker.R

sealed class Screen(open val route: String)

object Login : Screen("login")
object Register : Screen("register")
object Main : Screen("main")

sealed class BottomNavScreen(
    override val route: String,
    val titleResId: Int,
    val icon: ImageVector
) : Screen(route)

object Exercises : BottomNavScreen(
    route = "exercises",
    titleResId = R.string.exercises_title,
    icon = Icons.Filled.FitnessCenter
)

object Routines : BottomNavScreen(
    route = "routines",
    titleResId = R.string.routines_title,
    icon = Icons.AutoMirrored.Filled.List
) {
    fun withFilter(filter: String) = "$route?filter=$filter"
}

// object Routines : BottomNavScreen(
//    route = "routines?filter={filter}", // <- dodaj query param
//    titleResId = R.string.routines_title,
//    icon = Icons.AutoMirrored.Filled.List
// ) {
//    fun withFilter(filter: String) = "routines?filter=$filter"
// }

object Home : BottomNavScreen(
    route = "home",
    titleResId = R.string.home_title,
    icon = Icons.Default.Home
)

object MealPlanView : BottomNavScreen(
    route = "meal_plan_view",
    titleResId = R.string.meal_plan_title,
    icon = Icons.AutoMirrored.Default.MenuBook
)

object RoutineDetail : Screen("routineDetail/{routineId}") {
    fun createRoute(routineId: String) = "routineDetail/$routineId"
}

object NewRoutine : Screen("newRoutine")

object RoutinePlayer : Screen("routine_player/{routineId}") {
    fun createRoute(routineId: String) = "routine_player/$routineId"
}

object User : BottomNavScreen(
    route = "profile",
    titleResId = R.string.profile_title,
    icon = Icons.Default.Person
)

object ExerciseDetail : Screen("exercise_detail")

object ChangePassword : Screen("change_password")

object MakeMealPlan : Screen("make_meal_plan") {
    override val route = "make_meal_plan?returnRoute={returnRoute}"
}

object MealPlanDetail : Screen("meal_plan_detail/{mealPlanId}") {
    fun createRoute(mealPlanId: String) = "meal_plan_detail/$mealPlanId"
}

object FitnessGoalScreen : Screen("fitness_goal_screen")
object TrackCaloriesScreen : Screen("track_calories_screen")
object CaloriesOverviewScreen : Screen("calories_overview_screen")
object GoalsOverviewScreen : Screen("goals_overview_screen")

object PhysicalData : Screen("physical_data") {
    const val routeWithArgs = "physical_data?fromEdit={fromEdit}"

    // Funkcija za generiranje rute s parametrom
    fun createRoute(fromEdit: Boolean = false): String {
        return "physical_data?fromEdit=$fromEdit"
    }
}
