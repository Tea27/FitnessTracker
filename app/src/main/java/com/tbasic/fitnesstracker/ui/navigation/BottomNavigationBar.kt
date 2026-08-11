package com.tbasic.fitnesstracker.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.navigation.BottomNavScreen
import com.tbasic.fitnesstracker.navigation.Exercises
import com.tbasic.fitnesstracker.navigation.Home
import com.tbasic.fitnesstracker.navigation.MealPlanView
import com.tbasic.fitnesstracker.navigation.Routines
import com.tbasic.fitnesstracker.navigation.User

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items: List<BottomNavScreen> = listOf(Home, Exercises, Routines, MealPlanView, User)
    val localizedContext = LocalLocalizedContext.current

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?")

        items.forEach { screen ->
            val destinationRoute = screen.route

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = localizedContext.getString(screen.titleResId)) },
                label = {
                    if (currentRoute == destinationRoute) {
                        Text(
                            localizedContext.getString(screen.titleResId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                selected = currentRoute == destinationRoute,
                onClick = {
                    navController.navigate(destinationRoute) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
