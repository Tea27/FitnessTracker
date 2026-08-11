package com.tbasic.fitnesstracker.ui.screens.routine

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tbasic.fitnesstracker.data.mapper.localize
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.DateRangePicker
import com.tbasic.fitnesstracker.ui.components.FilterSegmentedControl
import com.tbasic.fitnesstracker.ui.components.RoutineCard
import com.tbasic.fitnesstracker.ui.components.SortOrderToggle
import com.tbasic.fitnesstracker.ui.components.TabSegmentedControl
import com.tbasic.fitnesstracker.vm.RoutineViewModel
import kotlinx.coroutines.launch
import com.tbasic.fitnesstracker.R as localR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    routineViewModel: RoutineViewModel,
    onRoutineClick: (routineId: String, isUserRoutine: Boolean) -> Unit,
    onAddNewRoutineClick: () -> Unit
) {
    val selectedTab = routineViewModel.selectedTab
    val userRoutineFilter = routineViewModel.userRoutineFilter

    val predefinedRoutines = routineViewModel.predefinedRoutines
    val isLoading = routineViewModel.isLoading
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }

    val localizedContext = LocalLocalizedContext.current
    val localizedRoutines = remember(predefinedRoutines, localizedContext) {
        predefinedRoutines.map { it.localize(localizedContext) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabSegmentedControl(
                selectedTab = selectedTab,
                onTabSelected = { routineViewModel.selectedTab = it }
            )

            if (selectedTab == RoutineViewModel.Companion.RoutineTab.USER) {
                FilterSegmentedControl(
                    selectedFilter = userRoutineFilter,
                    onFilterSelected = { routineViewModel.userRoutineFilter = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedTab == RoutineViewModel.Companion.RoutineTab.USER &&
                userRoutineFilter == RoutineViewModel.Companion.UserRoutineFilter.COMPLETED
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp), // Isto kao i gore gumbi!
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SortOrderToggle(
                        descending = routineViewModel.completedSortDescending,
                        onToggle = { routineViewModel.completedSortDescending = it }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                showFilterSheet = true
                                coroutineScope.launch { sheetState.show() }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter by date")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == RoutineViewModel.Companion.RoutineTab.USER) {
                        Log.d("ovo su filtrirane", routineViewModel.filteredUserRoutines.toString())
                        items(routineViewModel.filteredUserRoutines) { routine ->
                            RoutineCardUser(
                                routine = routine,
                                onClick = { onRoutineClick(routine.id, true) }
                            )
                        }
                    } else {
                        items(localizedRoutines) { routine ->
                            RoutineCard(
                                routine = routine, // .localize(context),
                                onClick = { onRoutineClick(routine.id, false) }
                            )
                        }
                    }
                }
            }
        }

        if (
            selectedTab == RoutineViewModel.Companion.RoutineTab.USER &&
            userRoutineFilter == RoutineViewModel.Companion.UserRoutineFilter.TODO
        ) {
            FloatingActionButton(
                onClick = onAddNewRoutineClick,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Routine")
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) showFilterSheet = false
                        }
                },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header with close icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizedContext.getString(localR.string.filter_and_sort),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            coroutineScope.launch { sheetState.hide() }
                                .invokeOnCompletion {
                                    if (!sheetState.isVisible) showFilterSheet = false
                                }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = localizedContext.getString(localR.string.close))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // date filters
                    DateRangePicker(
                        startDate = routineViewModel.completedStartDate,
                        endDate = routineViewModel.completedEndDate,
                        onStartDateSelected = { routineViewModel.completedStartDate = it },
                        onEndDateSelected = { routineViewModel.completedEndDate = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
