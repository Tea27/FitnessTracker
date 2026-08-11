package com.tbasic.fitnesstracker.vm

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tbasic.fitnesstracker.data.PredefinedRoutine
import com.tbasic.fitnesstracker.data.RoutineDay.Companion.fromDisplayName
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.data.mapper.localize
import com.tbasic.fitnesstracker.repository.RoutineRepository
import com.tbasic.fitnesstracker.utils.atEndOfDayInMillis
import com.tbasic.fitnesstracker.utils.atStartOfDayInMillis
import com.tbasic.fitnesstracker.utils.getTodayRoutineDay
import com.tbasic.fitnesstracker.utils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val repository: RoutineRepository
) : ViewModel() {

    var predefinedRoutines by mutableStateOf<List<PredefinedRoutine>>(emptyList())
        private set

    var userRoutines by mutableStateOf<List<UserRoutine>>(emptyList())
        private set

    private var loadingCounter by mutableStateOf(0)
    var isLoading by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var currentUserId: String? = null

    var selectedTab by mutableStateOf(RoutineTab.PREDEFINED)
    var userRoutineFilter by mutableStateOf(UserRoutineFilter.TODO)

    // UI State
    var completedStartDate by mutableStateOf<Long?>(null)
    var completedEndDate by mutableStateOf<Long?>(null)
    var completedSortDescending by mutableStateOf(true) // default: najnoviji prvi

    fun List<PredefinedRoutine>.localizeAll(context: Context): List<PredefinedRoutine> {
        return this.map { it.localize(context) }
    }

    val filteredUserRoutines: List<UserRoutine>
        get() {
            return when (userRoutineFilter) {
                UserRoutineFilter.TODO -> {
                    userRoutines.filter { !it.completed }
                }

                UserRoutineFilter.COMPLETED -> {
                    val start = completedStartDate ?: Long.MIN_VALUE
                    val endExclusive = completedEndDate?.plus(TimeUnit.DAYS.toMillis(1)) ?: Long.MAX_VALUE

                    userRoutines
                        .filter { it.completed }
                        .filter { routine ->
                            val finished = routine.finishedAt ?: return@filter false
                            finished in start until endExclusive
                        }
                        .let { list ->
                            if (completedSortDescending) {
                                list.sortedByDescending { it.finishedAt ?: 0L }
                            } else {
                                list.sortedBy { it.finishedAt ?: 0L }
                            }
                        }
                }
            }
        }

    init {
        viewModelScope.launch {
            isLoading = true
            try {
                loadPredefinedRoutines()
            } catch (e: Exception) {
                Log.e("RoutineViewModel", "Failed to init", e)
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun loadPredefinedRoutines() {
        try {
            val routines = repository.getPredefinedRoutines()
            predefinedRoutines = routines.sortedBy { dayOrder[it.day.uppercase()] ?: Int.MAX_VALUE }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Unexpected error"
            Log.e("RoutineViewModel", "Failed to load predefined routines", e)
        }
    }

    suspend fun loadUserRoutines() {
        try {
            currentUserId?.let { userId ->
                val routines = repository.getUserRoutines(userId)
                userRoutines = routines.sortedBy { dayOrder[it.day.uppercase()] ?: Int.MAX_VALUE }
            } ?: Log.d("RoutineViewModel", "Loading user routines")
        } catch (e: Exception) {
            Log.e("RoutineViewModel", "Failed to load user routines", e)
        }
    }

    fun setUser(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            loadUserRoutines()
        }
    }

    fun getPredefinedRoutineById(id: String): PredefinedRoutine? {
        return predefinedRoutines.find { it.id == id }
    }

    fun convertToUserRoutine(predefined: PredefinedRoutine, keepId: Boolean = false): UserRoutine {
        return UserRoutine(
            id = if (keepId) predefined.id else UUID.randomUUID().toString(),
            name = predefined.name,
            day = predefined.day,
            durationMinutes = predefined.durationMinutes,
            description = predefined.description,
            sets = predefined.sets,
            createdAt = System.currentTimeMillis(),
            completed = false
        )
    }

    fun saveUserRoutine(userRoutine: UserRoutine) = viewModelScope.launch {
        try {
            val userId = currentUserId ?: return@launch
            repository.saveRoutine(userId, userRoutine)
            loadUserRoutines()
            delay(100)
            Log.d("RoutineViewModel", "Routine successfully saved")
        } catch (e: Exception) {
            Log.e("RoutineViewModel", "Error saving routine", e)
        }
    }

    fun deleteUserRoutine(userRoutine: UserRoutine) = viewModelScope.launch {
        try {
            val userId = currentUserId ?: return@launch
            repository.deleteRoutine(userId, userRoutine.id)
            loadUserRoutines()
            Log.d("RoutineViewModel", "Routine deleted successfully")
        } catch (e: Exception) {
            Log.e("RoutineViewModel", "Error deleting routine", e)
            errorMessage = e.localizedMessage ?: "Failed to delete routine"
        }
    }

    fun getCompletedUserRoutines(
        startDateMillis: Long? = null,
        endDateMillis: Long? = null
    ): List<UserRoutine> {
        return userRoutines.filter { routine ->
            routine.completed && routine.finishedAt != null &&
                (startDateMillis == null || routine.finishedAt!! >= startDateMillis) &&
                (endDateMillis == null || routine.finishedAt!! <= endDateMillis)
        }
    }

    fun getCompletedRoutinesGroupedByDay(year: Int, month: Int): Map<Int, List<UserRoutine>> {
        // month: 1..12
        val startOfMonth = LocalDate(year, month, 1).atStartOfDayInMillis()
        val endOfMonth = LocalDate(year, month, 1)
            .plus(1, DateTimeUnit.MONTH)
            .minus(1, DateTimeUnit.DAY)
            .atEndOfDayInMillis()

        val completed = getCompletedUserRoutines(startOfMonth, endOfMonth)

        // Grupiraj po danu u mjesecu (1..31)
        return completed.groupBy { routine ->
            routine.finishedAt?.toLocalDate()?.dayOfMonth ?: 0
        }.filterKeys { it != 0 }
    }

    fun hasCompletedOn(date: LocalDate): Boolean {
        return userRoutines.any { it.finishedAt?.toLocalDate() == date }
    }

    fun switchToUserRoutinesTodo() {
        selectedTab = RoutineTab.USER
        userRoutineFilter = UserRoutineFilter.TODO
    }

    fun switchToUserRoutinesDone(
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        selectedTab = RoutineTab.USER
        userRoutineFilter = UserRoutineFilter.COMPLETED
        completedStartDate = startDate
        completedEndDate = endDate
    }

    fun switchToPredefinedRoutines() {
        selectedTab = RoutineTab.PREDEFINED
    }

    fun getUserRoutineById(id: String): UserRoutine? {
        return userRoutines.find { it.id == id }
    }

    fun getWeeklyCompletedRoutines(startOfWeek: LocalDate): List<Int> {
        val counts = IntArray(7) { 0 }

        userRoutines
            .filter { it.completed && it.finishedAt != null }
            .forEach { routine ->
                val date = Instant
                    .fromEpochMilliseconds(routine.finishedAt!!)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date

                if (date >= startOfWeek && date <= startOfWeek.plus(6, DateTimeUnit.DAY)) {
                    val index = date.dayOfWeek.isoDayNumber - 1 // PON=1 → index=0
                    counts[index]++
                }
            }

        return counts.toList()
    }

    fun getUserRoutinesForToday(context: Context): List<UserRoutine> {
        val today = fromDisplayName(context, getTodayRoutineDay().getDisplayName(context))
        return userRoutines.filter { !it.completed && it.day == today }
    }

    companion object {
        enum class RoutineTab {
            PREDEFINED, USER
        }

        enum class UserRoutineFilter {
            TODO, COMPLETED
        }

        private val dayOrder = mapOf(
            "MONDAY" to 1,
            "TUESDAY" to 2,
            "WEDNESDAY" to 3,
            "THURSDAY" to 4,
            "FRIDAY" to 5,
            "SATURDAY" to 6,
            "SUNDAY" to 7
        )
    }
}
