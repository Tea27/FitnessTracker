package com.tbasic.fitnesstracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.local.CalorieEntryEntity
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.TopAppBarWithBack
import com.tbasic.fitnesstracker.vm.CalorieTrackViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaloriesOverviewScreen(
    calorieTrackViewModel: CalorieTrackViewModel,
    onBack: () -> Unit,
    onAddClick: () -> Unit
) {
    val entries = calorieTrackViewModel.calorieEntries.collectAsState()
    val isLoading = calorieTrackViewModel.isLoading.collectAsState()
    val localizedContext = LocalLocalizedContext.current

    // Stanje za dijalog brisanja i trenutno odabrani unos
    var entryToDelete by remember { mutableStateOf<CalorieEntryEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = localizedContext.getString(R.string.calories_overview_title),
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Calorie Entry")
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    if (entries.value.isEmpty()) {
                        EmptyState()
                    } else {
                        CalorieEntryList(
                            entries = entries.value,
                            onDeleteClick = { entry ->
                                entryToDelete = entry
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                // AlertDialog za potvrdu brisanja
                if (showDeleteDialog && entryToDelete != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteDialog = false
                            entryToDelete = null
                        },
                        title = { Text(localizedContext.getString(R.string.delete_entry_title)) },
                        text = { Text(localizedContext.getString(R.string.delete_entry_message)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    // Pozovi delete u ViewModelu
                                    calorieTrackViewModel.deleteEntry(entryToDelete!!)
                                    showDeleteDialog = false
                                    entryToDelete = null
                                }
                            ) {
                                Text(localizedContext.getString(R.string.action_delete))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    entryToDelete = null
                                }
                            ) {
                                Text(localizedContext.getString(R.string.action_cancel))
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun CalorieEntryList(
    entries: List<CalorieEntryEntity>,
    onDeleteClick: (CalorieEntryEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries) { entry ->
            CalorieEntryCard(entry, onDeleteClick)
        }
    }
}

@Composable
fun CalorieEntryCard(
    entry: CalorieEntryEntity,
    onDeleteClick: (CalorieEntryEntity) -> Unit
) {
    val localizedContext = LocalLocalizedContext.current

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Da razdvoji sadržaj i delete dugme
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Calories Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = dateFormat.format(Date(entry.date)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${entry.calories} kcal",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onDeleteClick(entry) }
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = localizedContext.getString(R.string.action_delete),
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = localizedContext.getString(R.string.action_delete),
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    val localizedContext = LocalLocalizedContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            localizedContext.getString(R.string.empty_state_message),
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
