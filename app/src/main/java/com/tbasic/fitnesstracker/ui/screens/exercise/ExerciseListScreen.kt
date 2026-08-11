package com.tbasic.fitnesstracker.ui.screens.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.Exercise
import com.tbasic.fitnesstracker.data.mapper.localize
import com.tbasic.fitnesstracker.localization.LocalLocalizedContext
import com.tbasic.fitnesstracker.ui.components.ExerciseCardExtended
import com.tbasic.fitnesstracker.vm.ExerciseViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ExerciseListScreen(
    viewModel: ExerciseViewModel = hiltViewModel(),
    onExerciseClick: (Exercise) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allExercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val noMoreData by viewModel.noMoreData.collectAsState()
    val listState = rememberLazyListState()

    val context = LocalLocalizedContext.current

    val filteredExercises = if (searchQuery.isBlank()) allExercises else searchResults
    val translatedExercises = filteredExercises.map { it.localize(context) }

    // Pagination trigger
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { index ->
                if (index == allExercises.lastIndex && searchQuery.isBlank()) {
                    viewModel.loadNextPage()
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text(context.getString(R.string.search_hint)) },
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                searchQuery.isNotBlank() && isLoading -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                searchQuery.isNotBlank() && filteredExercises.isEmpty() -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(context.getString(R.string.no_results, searchQuery))
                        }
                    }
                }

                else -> {
                    items(translatedExercises) { exercise ->
                        ExerciseCardExtended(
                            exercise = exercise,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            onClick = { onExerciseClick(exercise) }
                        )
                    }

                    if (searchQuery.isBlank()) {
                        item {
                            when {
                                isLoading -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                noMoreData -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(context.getString(R.string.no_more_exercises))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun highlightQuery(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    val builder = buildAnnotatedString {
        var start = 0
        while (true) {
            val index = lowerText.indexOf(lowerQuery, startIndex = start)
            if (index == -1) {
                append(text.substring(start))
                break
            }

            // Add text before match
            append(text.substring(start, index))

            // Add highlighted match
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(text.substring(index, index + query.length))
            pop()

            start = index + query.length
        }
    }
    return builder
}
