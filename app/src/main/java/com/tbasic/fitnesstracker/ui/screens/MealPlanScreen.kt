package com.tbasic.fitnesstracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.DayMealPlanWithDates
import com.tbasic.fitnesstracker.data.MealParsed
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.utils.formatAsReadableDate
import com.tbasic.fitnesstracker.vm.MealPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    mealPlanViewModel: MealPlanViewModel,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    val multiDayPlan by mealPlanViewModel.multiDayPlan.collectAsState()
    val isLoading by mealPlanViewModel.isLoading.collectAsState()
    val startDate by mealPlanViewModel.startDate.collectAsState()
    val endDate by mealPlanViewModel.endDate.collectAsState()

    val scrollState = rememberLazyListState()
    val localizedContext = LocalLocalizedContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = localizedContext.getString(R.string.meal_plan_single_screen_title)) },
                actions = {
                    TextButton(onClick = onSave) {
                        Text(localizedContext.getString(R.string.meal_plan_save_button), color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = onDiscard) {
                        Text(localizedContext.getString(R.string.meal_plan_discard_button), color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = localizedContext.getString(R.string.meal_plan_for_period),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "${startDate.formatAsReadableDate()} - ${endDate.formatAsReadableDate()}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (isLoading) {
                item {
                    Text(localizedContext.getString(R.string.meal_plan_loading))
                }
            } else if (multiDayPlan != null) {
                items(multiDayPlan!!.days) { day ->
                    ClickableDayCard(dayMeal = day)
                }
            } else {
                item {
                    Text(localizedContext.getString(R.string.meal_plan_no_data))
                }
            }
        }
    }
}

@Composable
fun MealCard(meal: MealParsed) {
    val localizedContext = LocalLocalizedContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(meal.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))
            // Text("📝 Ingredients: ${meal.ingredients.joinToString(", ")}", fontSize = 14.sp)
            Text(localizedContext.getString(R.string.meal_ingredients_label, meal.ingredients.joinToString(", ")), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(2.dp))
            // Text("👨‍🍳 Instructions: ${meal.instructions}", fontSize = 14.sp)
            Text(localizedContext.getString(R.string.meal_instructions_label, meal.instructions), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(2.dp))
            // Text("🔥 Calories: ${meal.calories} kcal", fontSize = 14.sp)
            Text(localizedContext.getString(R.string.meal_calories_label, meal.calories), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(2.dp))
            // Text("⏱ Prep time: ${meal.prepTime}", fontSize = 14.sp)
            Text(localizedContext.getString(R.string.meal_prep_time_label, meal.prepTime), fontSize = 14.sp)
        }
    }
}

@Composable
fun ClickableDayCard(dayMeal: DayMealPlanWithDates) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dayMeal.day.formatAsReadableDate(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            dayMeal.meals.forEach { meal ->
                MealCard(meal)
            }
        }
    }
}
