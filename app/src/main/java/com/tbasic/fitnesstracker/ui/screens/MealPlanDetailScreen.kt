package com.tbasic.fitnesstracker.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.MealPlanDateCard
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.vm.MealPlanViewModel

@Composable
fun MealPlanDetailScreen(
    mealPlanViewModel: MealPlanViewModel,
    mealPlanId: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onDownload: (Context) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val mealPlan = mealPlanViewModel.getMealPlanById(mealPlanId)
    val context = LocalContext.current
    val localizedContext = LocalLocalizedContext.current

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = localizedContext.getString(R.string.meal_plan_detail_screen_title),
                onBack = onBack,
                actions = {
                    TextButton(onClick = { onDownload(context) }) {
                        Text(localizedContext.getString(R.string.meal_plan_download_button), color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text(localizedContext.getString(R.string.meal_plan_delete_button), color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(localizedContext.getString(R.string.meal_plan_delete_dialog_title)) },
                text = { Text(localizedContext.getString(R.string.meal_plan_delete_dialog_message)) },
                confirmButton = {
                    Button(onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }) {
                        Text(localizedContext.getString(R.string.meal_plan_delete_dialog_confirm))
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) {
                        Text(localizedContext.getString(R.string.meal_plan_delete_dialog_dismiss))
                    }
                }
            )
        }

        if (mealPlan == null) {
            Text(
                localizedContext.getString(R.string.meal_plan_not_found_message),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                style = MaterialTheme.typography.bodyLarge
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MealPlanDateCard(
                    startDate = mealPlan.startDate,
                    endDate = mealPlan.endDate
                )
            }

            mealPlan.meals.forEach { dayMeal ->
                item {
                    ClickableDayCard(dayMeal = dayMeal)
                }
            }
        }
    }
}
