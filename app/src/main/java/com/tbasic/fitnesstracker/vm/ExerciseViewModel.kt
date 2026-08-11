package com.tbasic.fitnesstracker.vm

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tbasic.fitnesstracker.data.Exercise
import com.tbasic.fitnesstracker.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val repository: ExerciseRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _noMoreData = MutableStateFlow(false)
    val noMoreData: StateFlow<Boolean> = _noMoreData
    private val pageSize = 10

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Exercise>>(emptyList())
    val searchResults: StateFlow<List<Exercise>> = _searchResults.asStateFlow()

    init {
        loadNextPage()
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _searchResults.value = emptyList()
                        return@collectLatest
                    }

                    repository.searchExercisesWithFallback(query)
                        .collectLatest { results ->
                            _searchResults.value = results
                        }
                }
        }
    }

    fun setSelectedExercise(exercise: Exercise) {
        savedStateHandle[SELECTED_EXERCISE_KEY] = exercise
    }

    fun getSelectedExercise(): Exercise? {
        return savedStateHandle[SELECTED_EXERCISE_KEY]
    }

    fun loadNextPage() {
        if (_isLoading.value || _noMoreData.value) return

        _isLoading.value = true
        val offset = _exercises.value.size

        viewModelScope.launch {
            try {
                val newExercises = repository.loadNextPage(offset)
                if (newExercises.isEmpty()) {
                    _noMoreData.value = true
                } else {
                    _exercises.update { it + newExercises }
                    if (newExercises.size < pageSize) {
                        _noMoreData.value = true
                    }
                }
            } catch (e: Exception) {
                Log.e("ExerciseViewModel", "Total failure: even fallback failed", e)
                _noMoreData.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun getExerciseById(id: String): Exercise? {
        return repository.getExerciseById(id)
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    companion object {
        const val SELECTED_EXERCISE_KEY = "selectedExercise"
    }
}
