package com.tbasic.fitnesstracker.vm

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.data.AppUser
import com.tbasic.fitnesstracker.data.UserRoutine
import com.tbasic.fitnesstracker.data.local.GoalEntryEntity
import com.tbasic.fitnesstracker.localization.SupportedLanguage
import com.tbasic.fitnesstracker.localization.supportedLanguages
import com.tbasic.fitnesstracker.repository.CombinedGoalEntryRepository
import com.tbasic.fitnesstracker.repository.UserRepository
import com.tbasic.fitnesstracker.utils.getTodayEpochMillis
import com.tbasic.fitnesstracker.utils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val goalSyncRepository: CombinedGoalEntryRepository
) : ViewModel() {

    // Regex helpers
    val decimalNumberRegex = Regex("^\\d*\\.?\\d*\$")
    val integerNumberRegex = Regex("^\\d*\$")

    val languages = supportedLanguages

    // User data state
    private val _currentUser = mutableStateOf<AppUser?>(null)
    val currentUser: State<AppUser?> = _currentUser

    private val _selectedLanguage = mutableStateOf<String?>(null)
    val selectedLanguage: State<String?> = _selectedLanguage

    // User input state
    var weight by mutableStateOf("")
        private set
    var height by mutableStateOf("")
        private set
    var location by mutableStateOf("")
        private set
    var gender by mutableStateOf<Gender?>(null)
        private set
    var birthDate by mutableStateOf("")
        private set
    var goal by mutableStateOf<FitnessGoal?>(null)
        private set
    var targetWeight by mutableStateOf("")
        private set
    var workoutFrequency by mutableStateOf("")
        private set
    var timePeriodWeeks by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set

    var inputError by mutableStateOf<InputError?>(null)
        private set

    private var currentUserId: String? = null

    // Goal entries state
    private val _latestGoalEntry = mutableStateOf<GoalEntryEntity?>(null)
    val latestGoalEntry: State<GoalEntryEntity?> = _latestGoalEntry

    private val _allGoals = MutableStateFlow<List<GoalEntryEntity>>(emptyList())
    val allGoals: StateFlow<List<GoalEntryEntity>> = _allGoals.asStateFlow()

    /* =========================
       OBSERVE DATA FROM REPOSITORY
       ========================= */

    fun observeGoals(userId: String) {
        goalSyncRepository.getAllGoalsForUserFlow(userId)
            .onEach { goals -> _allGoals.value = goals.sortedByDescending { it.startDate } }
            .launchIn(viewModelScope)
    }

    fun observeLatestGoal(userId: String) {
        goalSyncRepository.getLatestGoalEntryForUserFlow(userId)
            .onEach { latestGoal -> _latestGoalEntry.value = latestGoal }
            .launchIn(viewModelScope)
    }

    /* =========================
       LOADING AND SYNCING
       ========================= */

    fun setUserId(userId: String) {
        currentUserId = userId
        refreshUserData()

        viewModelScope.launch {
            loadAllGoals(userId)
            loadLatestGoalEntry() // Važno: prvo učitaj ciljeve pa onda najnoviji
        }
        observeGoals(userId)
        observeLatestGoal(userId)
    }

    fun loadAllGoals(userId: String) {
        viewModelScope.launch {
            try {
                goalSyncRepository.syncDown(userId) // Firebase -> lokalno
                val localGoals = goalSyncRepository.getAllGoalsFromLocal(userId)
                _allGoals.value = localGoals.sortedByDescending { it.startDate }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to load goals: ${e.message}")
            }
        }
    }

    fun loadLatestGoalEntry() {
        val userId = currentUserId ?: return
        goalSyncRepository.getLatestGoalEntryForUserFlow(userId)
            .onEach { latestGoal ->
                _latestGoalEntry.value = latestGoal
                Log.d("UserViewModel", "Latest goal updated: $latestGoal")
            }
            .launchIn(viewModelScope)
    }

    /* =========================
       USER DATA MANAGEMENT
       ========================= */

    fun refreshUserData() {
        viewModelScope.launch {
            try {
                val remoteUser = withTimeout(3000L) { userRepository.fetchUserFromRemote() }
                updateUserState(remoteUser)
                Log.d("UserViewModel", "Remote user data: $remoteUser")
                userRepository.cacheUser(remoteUser)
            } catch (e: Exception) {
                val cachedUser = userRepository.getCachedUser()
                updateUserState(cachedUser)
                Log.d("UserViewModel", "Loaded cached user due to error: ${e.message}")
            }
        }
    }

    private fun updateUserState(user: AppUser?) {
        _currentUser.value = user
        _selectedLanguage.value = user?.language ?: supportedLanguages.first().code
        syncFieldsFromUser(user)
        loadLatestGoalEntry()
    }

    private fun syncFieldsFromUser(user: AppUser?) {
        user?.let {
            weight = it.weight?.toString() ?: ""
            height = it.height?.toString() ?: ""
            location = it.location ?: ""
            gender = it.gender?.let { g -> Gender.valueOf(g) }
            birthDate = it.birthDate ?: ""
            goal = it.goal
            targetWeight = it.targetWeight?.toString() ?: ""
        }
    }

    /* =========================
       GOAL OPERATIONS
       ========================= */

    fun saveGoalEntry() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            val newGoal = GoalEntryEntity(
                id = generateUniqueId(),
                userId = userId,
                goalType = goal ?: FitnessGoal.WORKOUT_COUNT,
                currentWeight = weight.toFloatOrNull(),
                targetWeight = targetWeight.toFloatOrNull(),
                timePeriodWeeks = timePeriodWeeks.toIntOrNull(),
                workoutFrequency = workoutFrequency.toIntOrNull(),
                startDate = getTodayEpochMillis(),
                isCompleted = false
            )

            goalSyncRepository.saveAndSync(newGoal)
            _latestGoalEntry.value = newGoal

            // Update current user goal info
            val updatedUser = _currentUser.value?.copy(
                goal = newGoal.goalType,
                targetWeight = newGoal.targetWeight
            )
            _currentUser.value = updatedUser

            updatedUser?.let {
                userRepository.updatePhysicalData(
                    userId = it.id,
                    weight = it.weight,
                    height = it.height,
                    location = it.location,
                    gender = it.gender,
                    birthDate = it.birthDate,
                    goal = it.goal,
                    targetWeight = it.targetWeight
                )
                userRepository.cacheUser(it)
            }

            loadAllGoals(userId)
        }
    }

    fun deleteGoal(userId: String, goalId: String) {
        viewModelScope.launch {
            try {
                goalSyncRepository.deleteGoal(userId, goalId)

                if (_latestGoalEntry.value?.id == goalId) {
                    _latestGoalEntry.value = null

                    val updatedUser = _currentUser.value?.copy(
                        goal = null,
                        targetWeight = null
                    )
                    _currentUser.value = updatedUser

                    updatedUser?.let { user ->
                        userRepository.updatePhysicalData(
                            userId = user.id,
                            weight = user.weight,
                            height = user.height,
                            location = user.location,
                            gender = user.gender,
                            birthDate = user.birthDate,
                            goal = null,
                            targetWeight = null
                        )
                        userRepository.cacheUser(user)
                        userRepository.persistLanguageEverywhere(user.language)
                    }
                }

                loadAllGoals(userId)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to delete goal: ${e.message}")
            }
        }
    }


    fun checkIfGoalIsCompleted(userRoutines: List<UserRoutine>? = null) {
        viewModelScope.launch {
            val goal = _latestGoalEntry.value ?: return@launch
            if (goal.isCompleted) return@launch

            val routines = userRoutines ?: listOf() // fallback

            val shouldComplete = when (goal.goalType) {
                FitnessGoal.WEIGHT_LOSS -> {
                    val current = weight.toFloatOrNull() ?: return@launch
                    current <= (goal.targetWeight ?: Float.MAX_VALUE)
                }

                FitnessGoal.MUSCLE_GAIN -> {
                    val current = weight.toFloatOrNull() ?: return@launch
                    current >= (goal.targetWeight ?: Float.MIN_VALUE)
                }

                FitnessGoal.WORKOUT_COUNT -> {
                    val completedThisWeek = getWeeklyCompletedWorkouts(routines)
                    val required = goal.workoutFrequency ?: return@launch
                    completedThisWeek >= required
                }
            }

            if (shouldComplete) {
                try {
                    goalSyncRepository.markGoalAsCompleted(goal.id, goal.userId)
                    loadLatestGoalEntry()
                    loadAllGoals(currentUserId ?: return@launch)
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Failed to mark goal completed: ${e.message}")
                }
            }
        }
    }

    /* =========================
       VALIDATIONS
       ========================= */

    fun isProfileComplete(): Boolean {
        val user = currentUser.value
        return user?.height != null &&
            user.weight != null &&
            user.birthDate != null &&
            user.gender != null &&
            user.goal != null &&
            !user.location.isNullOrBlank()
    }

    fun isPhysicalDataValid(): Boolean {
        val w = weight.toFloatOrNull()
        val h = height.toFloatOrNull()

        if (w == null || w < 30f || w > 300f) return false
        if (h == null || h < 100f || h > 250f) return false

        return true
    }

    /* =========================
       UPDATE FUNCTIONS
       ========================= */

    fun updatePhysicalData(onResult: () -> Unit) {
        if (!isPhysicalDataValid()) return

        val w = weight.toFloatOrNull()
        val h = height.toFloatOrNull()
        val loc = location.takeIf { it.isNotBlank() }
        val g = gender?.name
        val bd = birthDate.takeIf { it.isNotBlank() }
        val tWeight = targetWeight.toFloatOrNull()
        val selectedGoal = goal
        val userId = currentUser.value?.id ?: return

        isLoading = true
        viewModelScope.launch {
            try {
                userRepository.updatePhysicalData(
                    userId = userId,
                    weight = w,
                    height = h,
                    location = loc,
                    gender = g,
                    birthDate = bd,
                    goal = selectedGoal,
                    targetWeight = tWeight
                )
                refreshUserData()
                checkIfGoalIsCompleted()
                onResult()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to update physical data", e)
            } finally {
                isLoading = false
            }
        }
    }

    /* =========================
       USER INPUT HANDLERS
       ========================= */

    fun onWeightChange(newWeight: String) {
        weight = newWeight
    }

    fun onHeightChange(newHeight: String) {
        height = newHeight
    }

    fun onLocationChange(newLocation: String) {
        location = newLocation
    }

    fun onGenderChange(newGender: Gender) {
        gender = newGender
    }

    fun onBirthDateChange(newDate: String) {
        birthDate = newDate
    }

    fun onGoalChange(newGoal: FitnessGoal) {
        goal = newGoal
    }

    fun onTargetWeightChange(newTargetWeight: String) {
        targetWeight = newTargetWeight
    }

    fun onWorkoutFrequencyChange(value: String) {
        workoutFrequency = value
    }

    fun onTimePeriodWeeksChange(value: String) {
        timePeriodWeeks = value
    }

    /* =========================
       LANGUAGE MANAGEMENT
       ========================= */

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                val updatedUser = user.copy(language = lang)
                _currentUser.value = updatedUser

                userRepository.persistLanguageEverywhere(lang)
                userRepository.cacheUser(updatedUser)
            }
        }
    }

    fun loadLanguage(isUserLoggedIn: Boolean) {
        viewModelScope.launch {
            if (isUserLoggedIn) {
                refreshUserData()
            } else {
                val localLang = userRepository.getLanguageFromLocal()
                _selectedLanguage.value = localLang
            }
        }
    }

    fun getCurrentLanguage(): SupportedLanguage {
        return supportedLanguages.find { it.code == _selectedLanguage.value }
            ?: supportedLanguages.first()
    }

    /* =========================
       UTILS
       ========================= */

    fun generateUniqueId(): String = UUID.randomUUID().toString()

    /**
     * Returns number of completed workouts this week based on UserRoutine list
     */
    fun getWeeklyCompletedWorkouts(userRoutines: List<UserRoutine>): Int {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = today.minus((today.dayOfWeek.isoDayNumber - 1).toLong(), DateTimeUnit.DAY)
        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)

        return userRoutines.count { routine ->
            routine.completed && routine.finishedAt?.toLocalDate()?.let { finishedDate ->
                finishedDate in startOfWeek..endOfWeek
            } == true
        }
    }

    fun validateGoalInputs(): Boolean {
        inputError = null
        val current = weight.toFloatOrNull()

        when (goal) {
            FitnessGoal.WEIGHT_LOSS, FitnessGoal.MUSCLE_GAIN -> {
                if (current == null) { inputError = InputError.CurrentWeightMissing; return false }

                val target = targetWeight.toFloatOrNull() ?: run { inputError = InputError.TargetWeightInvalid; return false }
                val period = timePeriodWeeks.toIntOrNull() ?: run { inputError = InputError.WeeksOutOfRange; return false }

                when (goal) {
                    FitnessGoal.WEIGHT_LOSS -> {
                        if (target >= current) { inputError = InputError.TargetWeightInvalid; return false }
                        if ((current - target) / period > 0.5f) { inputError = InputError.WeightLossTooFast; return false }
                    }
                    FitnessGoal.MUSCLE_GAIN -> {
                        if (target <= current) { inputError = InputError.TargetWeightInvalid; return false }
                        if ((target - current) / period > 0.3f) { inputError = InputError.MuscleGainTooFast; return false }
                    }
                    else -> {}
                }
            }
            FitnessGoal.WORKOUT_COUNT -> {
                val freq = workoutFrequency.toIntOrNull() ?: run { inputError = InputError.WorkoutFrequencyInvalid; return false }
                if (freq < 1 || freq > 30) { inputError = InputError.WorkoutFrequencyInvalid; return false }
            }
            null -> return false
        }

        return true
    }

    fun getBmiCategory(bmi: Float, localizedContext: Context): String {
        return when {
            bmi < 18.5 -> localizedContext.getString(R.string.underweight)
            bmi < 25 -> localizedContext.getString(R.string.normal_weight)
            bmi < 30 -> localizedContext.getString(R.string.overweight)
            else -> localizedContext.getString(R.string.obesity)
        }
    }

}

enum class FitnessGoal(val displayName: String) {
    MUSCLE_GAIN("Gain muscle mass"),
    WEIGHT_LOSS("Weight loss (e.g., lose 5 kg)"),
    WORKOUT_COUNT("Workout frequency (e.g., 12 sessions/week)")
}

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(name: String): Gender? =
            values().firstOrNull { it.displayName.equals(name, ignoreCase = true) }
    }
}


sealed class InputError {
    object CurrentWeightMissing : InputError()
    object TargetWeightInvalid : InputError()
    object WeeksOutOfRange : InputError()
    object WeightLossTooFast : InputError()
    object MuscleGainTooFast : InputError()
    object WorkoutFrequencyInvalid : InputError()
}