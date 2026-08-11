package com.tbasic.fitnesstracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tbasic.fitnesstracker.data.local.CalorieEntryEntity
import com.tbasic.fitnesstracker.repository.CombinedCalorieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalorieTrackViewModel @Inject constructor(
    private val calorieRepository: CombinedCalorieRepository
) : ViewModel() {

    private val _calorieEntries = MutableStateFlow<List<CalorieEntryEntity>>(emptyList())
    val calorieEntries: StateFlow<List<CalorieEntryEntity>> = _calorieEntries.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _error.asStateFlow()

    var currentUserId: String? = null

    init {
        loadEntries()
    }

    fun startObservingEntries() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            // Start syncing data from Firebase once or periodically
            calorieRepository.syncFromFirebase(userId)

            // Observe local Room data changes
            calorieRepository.observeEntries(userId).collect { entries ->
                _calorieEntries.value = entries
            }
        }
    }

    fun setUserId(userId: String) {
        currentUserId = userId
        startObservingEntries()
    }

    fun loadEntries() {
        if (currentUserId == null) return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val data = calorieRepository.getAll(currentUserId!!)
                _calorieEntries.value = data.sortedByDescending { it.date }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteEntry(entry: CalorieEntryEntity) {
        if (currentUserId == null) return
        viewModelScope.launch {
            try {
                calorieRepository.deleteById(
                    userId = currentUserId!!,
                    entryId = entry.id
                )
                _calorieEntries.update { it.filterNot { e -> e.id == entry.id } }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addEntry(
        date: Long,
        calories: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (currentUserId == null) {
            onFailure("User ID not set")
            return
        }

        viewModelScope.launch {
            _loading.value = true

            try {
                val entry = CalorieEntryEntity.create(date, calories, currentUserId!!)
                calorieRepository.insert(entry)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            } finally {
                _loading.value = false
            }
        }
    }
}
