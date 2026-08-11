package com.tbasic.fitnesstracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.DateField
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.ui.components.rememberDatePickerLauncher
import com.tbasic.fitnesstracker.vm.MealPlanViewModel
import com.tbasic.fitnesstracker.vm.SectionId
import com.tbasic.fitnesstracker.vm.UserViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MealPlanSelectionScreen(
    mealPlanViewModel: MealPlanViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onStartGenerating: () -> Unit
) {
    val sections by mealPlanViewModel.sections.collectAsState()
    var currentSectionIndex by remember { mutableStateOf(0) }
    val isLoading by mealPlanViewModel.isLoading.collectAsState()
    val isPlanReady by mealPlanViewModel.isPlanReady.collectAsState()
    val currentSection = sections.getOrNull(currentSectionIndex)
    val progress = if (sections.isNotEmpty()) (currentSectionIndex + 1f) / sections.size else 0f
    val userData by userViewModel.currentUser

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val startDate by mealPlanViewModel.startDate.collectAsState()
    val endDate by mealPlanViewModel.endDate.collectAsState()
    val startDateMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    val endDateMillis = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val localizedContext = LocalLocalizedContext.current

    val startDatePickerLauncher = rememberDatePickerLauncher(
        onDateSelected = { millis ->
            val selected = Instant.fromEpochMilliseconds(millis)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            mealPlanViewModel.setStartDate(selected)
        },
        allowPast = false,
        allowFuture = true
    )

    val endDatePickerLauncher = rememberDatePickerLauncher(
        onDateSelected = { millis ->
            val selected = Instant.fromEpochMilliseconds(millis)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date

            val daysBetween = mealPlanViewModel.startDate.value.daysUntil(selected)
            mealPlanViewModel.setEndDate(selected)

            if (!(daysBetween in 0..6)) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Plan can contain 7 days maximum")
                }
            }
        },
        allowPast = false,
        allowFuture = true
    )
    val error by mealPlanViewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = localizedContext.getString(R.string.meal_plan_setup),
                onBack = onBack
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Step ${currentSectionIndex + 1} of ${sections.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            DateField(
                label = localizedContext.getString(R.string.meal_plan_filter_start_date),
                date = startDateMillis,
                onClick = { startDatePickerLauncher.launch(startDateMillis) },
                dateFormatter = dateFormatter
            )

            Spacer(modifier = Modifier.height(16.dp))

            DateField(
                label = localizedContext.getString(R.string.meal_plan_filter_end_date),
                date = endDateMillis,
                onClick = { endDatePickerLauncher.launch(endDateMillis) },
                dateFormatter = dateFormatter
            )

            error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = Color.Red, modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            currentSection?.let { section ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if ( section.icon != null) {
                        Icon(section.icon, contentDescription = "Previous Month")
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(text = getSectionTitle(section.id), style = MaterialTheme.typography.titleMedium)
                }
                Text(text = getSectionDescription(section.id), style = MaterialTheme.typography.bodyMedium)


                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    section.options.forEach { option ->
                        FilterChip(
                            selected = option.isSelected,
                            onClick = { mealPlanViewModel.toggleOption(section.id, option.id) },
                            label = { Text(getOptionLabel(section.id, option.id)) },
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (currentSectionIndex > 0) {
                    OutlinedButton(
                        onClick = { currentSectionIndex-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localizedContext.getString(R.string.button_back))
                    }
                }
                if (currentSectionIndex < sections.lastIndex) {
                    Button(
                        onClick = { currentSectionIndex++ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localizedContext.getString(R.string.button_next))
                    }
                } else {
                    Button(
                        onClick = {
                            onStartGenerating()
                            mealPlanViewModel.updateUserInfo(
                                userData
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localizedContext.getString(R.string.button_generate_meal_plan))
                    }
                }
            }
        }
    }
}

@Composable
fun getSectionTitle(sectionId: String): String {
    val localizedContext = LocalLocalizedContext.current
    return when (sectionId) {
        SectionId.GOAL.id -> localizedContext.getString(R.string.section_goal_title)
        SectionId.DIET.id -> localizedContext.getString(R.string.section_diet_title)
        SectionId.ALLERGIES.id -> localizedContext.getString(R.string.section_allergies_title)
        SectionId.MEAL_FREQUENCY.id -> localizedContext.getString(R.string.section_meal_frequency_title)
        SectionId.PROTEIN_SOURCE.id -> localizedContext.getString(R.string.section_protein_source_title)
        SectionId.PREP_DIFFICULTY.id -> localizedContext.getString(R.string.section_prep_difficulty_title)
        SectionId.PREP_TIME.id -> localizedContext.getString(R.string.section_prep_time_title)
        SectionId.ADDITIONAL_REQUIREMENTS.id -> localizedContext.getString(R.string.section_additional_requirements_title)
        else -> ""
    }
}

@Composable
fun getSectionDescription(sectionId: String): String {
    val localizedContext = LocalLocalizedContext.current
    return when (sectionId) {
        SectionId.GOAL.id -> localizedContext.getString(R.string.section_goal_description)
        SectionId.DIET.id -> localizedContext.getString(R.string.section_diet_description)
        SectionId.ALLERGIES.id -> localizedContext.getString(R.string.section_allergies_description)
        SectionId.MEAL_FREQUENCY.id -> localizedContext.getString(R.string.section_meal_frequency_description)
        SectionId.PROTEIN_SOURCE.id -> localizedContext.getString(R.string.section_protein_source_description)
        SectionId.PREP_DIFFICULTY.id -> localizedContext.getString(R.string.section_prep_difficulty_description)
        SectionId.PREP_TIME.id -> localizedContext.getString(R.string.section_prep_time_description)
        SectionId.ADDITIONAL_REQUIREMENTS.id -> localizedContext.getString(R.string.section_additional_requirements_description)
        else -> ""
    }
}

// Lokalizacija labela opcija
@Composable
fun getOptionLabel(sectionId: String, optionId: String): String {
    val localizedContext = LocalLocalizedContext.current
    return when (sectionId) {
        SectionId.GOAL.id -> when (optionId) {
            "lose_weight" -> localizedContext.getString(R.string.goal_lose_weight)
            "build_muscle" -> localizedContext.getString(R.string.goal_build_muscle)
            "increase_protein" -> localizedContext.getString(R.string.goal_increase_protein)
            "maintain_weight" -> localizedContext.getString(R.string.goal_maintain_weight)
            "improve_endurance" -> localizedContext.getString(R.string.goal_improve_endurance)
            else -> optionId
        }

        SectionId.DIET.id -> when (optionId) {
            "vegan" -> localizedContext.getString(R.string.diet_vegan)
            "vegetarian" -> localizedContext.getString(R.string.diet_vegetarian)
            "pescatarian" -> localizedContext.getString(R.string.diet_pescatarian)
            "keto" -> localizedContext.getString(R.string.diet_keto)
            "low_carb" -> localizedContext.getString(R.string.diet_low_carb)
            "gluten_free" -> localizedContext.getString(R.string.diet_gluten_free)
            "dairy_free" -> localizedContext.getString(R.string.diet_dairy_free)
            else -> optionId
        }

        SectionId.ALLERGIES.id -> when (optionId) {
            "nuts" -> localizedContext.getString(R.string.allergy_nuts)
            "gluten" -> localizedContext.getString(R.string.allergy_gluten)
            "dairy" -> localizedContext.getString(R.string.allergy_dairy)
            "soy" -> localizedContext.getString(R.string.allergy_soy)
            "shellfish" -> localizedContext.getString(R.string.allergy_shellfish)
            "eggs" -> localizedContext.getString(R.string.allergy_eggs)
            else -> optionId
        }

        SectionId.MEAL_FREQUENCY.id -> when (optionId) {
            "3_meals" -> localizedContext.getString(R.string.meal_frequency_3_meals)
            "4_meals" -> localizedContext.getString(R.string.meal_frequency_4_meals)
            "5_meals" -> localizedContext.getString(R.string.meal_frequency_5_meals)
            "6_meals" -> localizedContext.getString(R.string.meal_frequency_6_meals)
            else -> optionId
        }

        SectionId.PROTEIN_SOURCE.id -> when (optionId) {
            "plant_based" -> localizedContext.getString(R.string.protein_source_plant_based)
            "animal_based" -> localizedContext.getString(R.string.protein_source_animal_based)
            "mixed" -> localizedContext.getString(R.string.protein_source_mixed)
            else -> optionId
        }

        SectionId.PREP_DIFFICULTY.id -> when (optionId) {
            "easy" -> localizedContext.getString(R.string.prep_difficulty_easy)
            "moderate" -> localizedContext.getString(R.string.prep_difficulty_moderate)
            "advanced" -> localizedContext.getString(R.string.prep_difficulty_advanced)
            else -> optionId
        }

        SectionId.PREP_TIME.id -> when (optionId) {
            "under_15" -> localizedContext.getString(R.string.prep_time_under_15)
            "15_to_30" -> localizedContext.getString(R.string.prep_time_15_to_30)
            "30_to_60" -> localizedContext.getString(R.string.prep_time_30_to_60)
            "over_60" -> localizedContext.getString(R.string.prep_time_over_60)
            else -> optionId
        }

        SectionId.ADDITIONAL_REQUIREMENTS.id -> when (optionId) {
            "low_sodium" -> localizedContext.getString(R.string.additional_low_sodium)
            "high_fiber" -> localizedContext.getString(R.string.additional_high_fiber)
            "low_sugar" -> localizedContext.getString(R.string.additional_low_sugar)
            "organic" -> localizedContext.getString(R.string.additional_organic)
            "budget_friendly" -> localizedContext.getString(R.string.additional_budget_friendly)
            else -> optionId
        }

        else -> optionId
    }
}
