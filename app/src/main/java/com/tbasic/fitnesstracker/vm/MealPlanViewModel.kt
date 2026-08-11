package com.tbasic.fitnesstracker.vm

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.tbasic.fitnesstracker.BuildConfig
import com.tbasic.fitnesstracker.data.AppUser
import com.tbasic.fitnesstracker.data.ChatCompletionResponse
import com.tbasic.fitnesstracker.data.DayMealPlanWithDates
import com.tbasic.fitnesstracker.data.MealParsed
import com.tbasic.fitnesstracker.data.MealPlanSelection
import com.tbasic.fitnesstracker.data.MealSelectionOption
import com.tbasic.fitnesstracker.data.MealSelectionSection
import com.tbasic.fitnesstracker.data.ParsedMealPlan
import com.tbasic.fitnesstracker.data.SelectionType
import com.tbasic.fitnesstracker.data.local.MealPlanEntity
import com.tbasic.fitnesstracker.localization.supportedLanguages
import com.tbasic.fitnesstracker.repository.CombinedMealPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearsUntil
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.log

// Enum za ID-eve sekcija
enum class SectionId(val id: String) {
    GOAL("goal"),
    DIET("diet"),
    ALLERGIES("allergies"),
    MEAL_FREQUENCY("meal_frequency"),
    PROTEIN_SOURCE("protein_source"),
    PREP_DIFFICULTY("prep_difficulty"),
    PREP_TIME("prep_time"),
    ADDITIONAL_REQUIREMENTS("additional_requirements")
}

// Enumovi za opcije (primjer za GOAL)
enum class GoalOption(val optionId: String, val label: String) {
    LOSE_WEIGHT("lose_weight", "Lose weight"),
    BUILD_MUSCLE("build_muscle", "Build muscle"),
    INCREASE_PROTEIN("increase_protein", "Increase protein intake"),
    MAINTAIN_WEIGHT("maintain_weight", "Maintain weight"),
    IMPROVE_ENDURANCE("improve_endurance", "Improve endurance")
}

enum class DietOption(val optionId: String, val label: String) {
    VEGAN("vegan", "Vegan"),
    VEGETARIAN("vegetarian", "Vegetarian"),
    PESCATARIAN("pescatarian", "Pescatarian"),
    KETO("keto", "Ketogenic"),
    LOW_CARB("low_carb", "Low carb"),
    GLUTEN_FREE("gluten_free", "Gluten free"),
    DAIRY_FREE("dairy_free", "Dairy free")
}

enum class AllergyOption(val optionId: String, val label: String) {
    NUTS("nuts", "Nuts"),
    GLUTEN("gluten", "Gluten"),
    DAIRY("dairy", "Dairy"),
    SOY("soy", "Soy"),
    SHELLFISH("shellfish", "Shellfish"),
    EGGS("eggs", "Eggs")
}

enum class MealFrequencyOption(val optionId: String, val label: String) {
    THREE("3_meals", "3 meals"),
    FOUR("4_meals", "4 meals"),
    FIVE("5_meals", "5 meals"),
    SIX("6_meals", "6 meals")
}

enum class ProteinSourceOption(val optionId: String, val label: String) {
    PLANT_BASED("plant_based", "Plant-based"),
    ANIMAL_BASED("animal_based", "Animal-based"),
    MIXED("mixed", "Mixed")
}

enum class PrepDifficultyOption(val optionId: String, val label: String) {
    EASY("easy", "Easy"),
    MODERATE("moderate", "Moderate"),
    ADVANCED("advanced", "Advanced")
}

enum class PrepTimeOption(val optionId: String, val label: String) {
    UNDER_15("under_15", "Under 15 minutes"),
    FROM_15_TO_30("15_to_30", "15-30 minutes"),
    FROM_30_TO_60("30_to_60", "30-60 minutes"),
    OVER_60("over_60", "Over 60 minutes")
}

enum class AdditionalRequirementsOption(val optionId: String, val label: String) {
    LOW_SODIUM("low_sodium", "Low sodium"),
    HIGH_FIBER("high_fiber", "High fiber"),
    LOW_SUGAR("low_sugar", "Low sugar"),
    ORGANIC("organic", "Organic ingredients"),
    BUDGET_FRIENDLY("budget_friendly", "Budget friendly")
}

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val repository: CombinedMealPlanRepository
) : ViewModel() {

    private val _sections = MutableStateFlow<List<MealSelectionSection>>(emptyList())
    val sections: StateFlow<List<MealSelectionSection>> = _sections

    var gender: String = "unspecified"
    var location: String = "unspecified"
    var language: String = "en"
    var userWeight: Float? = null
    var userHeight: Float? = null
    var userAge: Int? = null
    var targetWeight: Float? = null
    var currentUserId: String? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isPlanReady = MutableStateFlow(false)
    val isPlanReady: StateFlow<Boolean> = _isPlanReady

    private val json = Json { ignoreUnknownKeys = true }

    private val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private val _startDate = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    val startDate: StateFlow<LocalDate> = _startDate

    private val _endDate = MutableStateFlow(
        Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(1, DateTimeUnit.DAY)
    )
    val endDate: StateFlow<LocalDate> = _endDate

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _multiDayPlan = MutableStateFlow<MealPlanSelection?>(null)
    val multiDayPlan: StateFlow<MealPlanSelection?> = _multiDayPlan.asStateFlow()

    private var _filteredPlans = MutableStateFlow<List<MealPlanEntity>>(emptyList())
    val filteredPlans: StateFlow<List<MealPlanEntity>> = _filteredPlans

    private val _userMealPlans = MutableStateFlow<List<MealPlanEntity>>(emptyList())
    val userMealPlans: StateFlow<List<MealPlanEntity>> = _userMealPlans

    var filterStartDate: String = ""
    var filterEndDate: String = ""

    fun applyDateFilter(startDate: String, endDate: String) {
        filterStartDate = startDate
        filterEndDate = endDate

        Log.d("Filter", "Filtering from $startDate to $endDate")
        Log.d("Filter", "Initial plans count: ${_userMealPlans.value.size}")

        if (startDate.isBlank() || endDate.isBlank()) {
            _filteredPlans.value = _userMealPlans.value
            return
        }

        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)

        val filteredList = _userMealPlans.value.filter { mealPlan ->
            val mealStart = LocalDate.parse(mealPlan.startDate)
            val mealEnd = LocalDate.parse(mealPlan.endDate)
            val overlaps = !(mealEnd < start || mealStart > end)

            Log.d("Filter", "MealPlan ${mealPlan.id}: $mealStart - $mealEnd overlaps? $overlaps")

            overlaps
        }

        Log.d("Filter", "Filtered plans count: ${filteredList.size}")
        _filteredPlans.value = filteredList
    }

    fun getMealPlanById(mealPlanId: String): MealPlanEntity? {
        return _userMealPlans.value.find { it.id == mealPlanId }
    }

    fun setStartDate(date: LocalDate) {
        _startDate.value = date

        // resetiraj endDate ako više nije valjan
        if (date.daysUntil(_endDate.value) !in 0..6) {
            _endDate.value = date.plus(1, DateTimeUnit.DAY)
        }
    }

    fun setEndDate(date: LocalDate) {
        _endDate.value = date
    }

    fun LocalDate.format(): String {
        val instant = this.atStartOfDayIn(TimeZone.currentSystemDefault())
        val javaDate = Date(instant.toEpochMilliseconds())
        return formatter.format(javaDate)
    }

    init {
        _sections.value = SectionId.entries.map { createSection(it) }
    }

    fun setUser(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            loadUserMealPlans()
        }
    }

    fun discardPlan() {
        _multiDayPlan.value = null
        _isPlanReady.value = false
        _isLoading.value = false
        _errorMessage.value = null
        _startDate.value = Clock.System.todayIn(TimeZone.currentSystemDefault())
        _endDate.value = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(1, DateTimeUnit.DAY)
        _sections.value = SectionId.entries.map { createSection(it) }
    }

    // POSTOJEĆE METODE

    fun updateUserInfo(user: AppUser?) {
        gender = user?.gender ?: "unspecified"
        location = user?.location ?: "unspecified"
        language = user?.language ?: "en"
        userWeight = user?.weight
        userHeight = user?.height
        userAge = user?.birthDate?.let { calculateAge(it) }
        targetWeight = user?.targetWeight
        currentUserId = user?.id
    }

    fun saveMealPlan() {
        Log.d("ovo je uid", currentUserId.toString())
        if (currentUserId == null) return
        val currentPlan = multiDayPlan.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveMealPlan(currentUserId!!, currentPlan, startDate.value.toString(), endDate.value.toString())
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "saveMealPlan: ", e)
            } finally {
                _isLoading.value = false
                discardPlan()
                loadUserMealPlans()
            }
        }
    }

    private suspend fun loadUserMealPlans() {
        try {
            currentUserId?.let { userId ->
                val plans = repository.getMealPlansForUser(userId)
                _userMealPlans.value = plans
                _filteredPlans.value = _userMealPlans.value
            } ?: Log.d("MealPlanViewModel", "Loading user routines")
        } catch (e: Exception) {
            Log.e("MealPlanViewModel", "Failed to load user routines", e)
        }
    }

    fun deleteMealPlan(mealPlanId: String) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteMealPlan(userId, mealPlanId)
                loadUserMealPlans()
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Failed to delete meal plan", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun ensureEndsWithPunctuation(text: String): String {
        val trimmed = text.trim()
        return if (trimmed.isNotEmpty() && !".!?".contains(trimmed.last())) "$trimmed." else trimmed
    }

    fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }

    fun savePdfToFile(context: Context, pdfBytes: ByteArray, fileName: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues) ?: return null
        resolver.openOutputStream(uri).use { it?.write(pdfBytes) }
        return uri
    }

    fun generateMealPlanPdf(mealPlan: MealPlanEntity): ByteArray {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() //veličina A4 i broj stranice
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val normalPaint = Paint().apply {
            textSize = 14f
        }

        val maxTextWidth = 440f
        var y = 40f // trenutna vertikalna pozicija na stranici

        fun addNewPageIfNeeded() {
            if (y > 780f) {
                pdfDocument.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                y = 40f
            }
        }

        fun drawWrappedText(text: String, paint: Paint, indent: Float) {
            val lines = text.split("\n")
            for (line in lines) {
                val cleanLine = ensureEndsWithPunctuation(line.trim())
                val wrapped = wrapText(cleanLine, paint, maxTextWidth)
                for (wrappedLine in wrapped) {
                    addNewPageIfNeeded()
                    canvas.drawText(wrappedLine, indent, y, paint)
                    y += 18f
                }
                y += 6f // razmak između paragrafa
            }
        }

        // Header
        canvas.drawText("Meal Plan from ${mealPlan.startDate} to ${mealPlan.endDate}", 40f, y, titlePaint)
        y += 40f

        mealPlan.meals.forEach { dayMeal ->
            addNewPageIfNeeded()
            canvas.drawText(dayMeal.day, 40f, y, titlePaint)
            y += 30f

            dayMeal.meals.forEach { meal ->
                addNewPageIfNeeded()
                canvas.drawText(meal.name, 60f, y, titlePaint)
                y += 24f
                Log.d("meal instructions", meal.instructions.toString())
                drawWrappedText("Ingredients: ${meal.ingredients.joinToString(", ")}", normalPaint, 80f)
                drawWrappedText("Instructions: ${ensureEndsWithPunctuation(meal.instructions)}", normalPaint, 80f)
                drawWrappedText("Calories: ${meal.calories}", normalPaint, 80f)
                drawWrappedText("Prep Time: ${meal.prepTime}", normalPaint, 80f)

                y += 24f // Razmak između jela
            }

            y += 32f // Razmak između dana
        }

        pdfDocument.finishPage(page)
        val outputStream = ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        return outputStream.toByteArray()
    }

    fun toggleOption(sectionId: String, optionId: String) {
        val selectedDiets = _sections.value.firstOrNull { it.id == SectionId.DIET.id }
            ?.options?.filter { it.isSelected }?.map { it.id } ?: emptyList()

        val isVeganOrVegetarian = selectedDiets.contains(DietOption.VEGAN.optionId) ||
            selectedDiets.contains(DietOption.VEGETARIAN.optionId)

        if (sectionId == SectionId.PROTEIN_SOURCE.id && isVeganOrVegetarian) {
            if (optionId == ProteinSourceOption.ANIMAL_BASED.optionId ||
                optionId == ProteinSourceOption.MIXED.optionId
            ) {
                return
            }
        }

        _sections.update { sections ->
            sections.map { section ->
                if (section.id == sectionId) {
                    when (section.selectionType) {
                        SelectionType.MULTIPLE -> {
                            section.copy(
                                options = section.options.map {
                                    if (it.id == optionId) it.copy(isSelected = !it.isSelected) else it
                                }
                            )
                        }
                        SelectionType.SINGLE -> {
                            section.copy(
                                options = section.options.map {
                                    it.copy(isSelected = it.id == optionId)
                                }
                            )
                        }
                    }
                } else {
                    section
                }
            }
        }
    }

    fun sanitizeOpenAiJsonResponse(raw: String): String {
        return raw
            .trim()
            .removePrefix("```")
            .removeSuffix("```")
            .replace("\\\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\r", "\r")
            .trim()
    }

    fun generatePartialPrompt(forDate: LocalDate, results: MutableList<DayMealPlanWithDates>): String {
        fun getSelectedLabels(sectionId: SectionId): List<String> = _sections.value
            .firstOrNull { it.id == sectionId.id }
            ?.options
            ?.filter { it.isSelected }
            ?.map { it.label }
            ?: emptyList()

        val goals = getSelectedLabels(SectionId.GOAL)
        val diets = getSelectedLabels(SectionId.DIET)
        val allergies = getSelectedLabels(SectionId.ALLERGIES)
        val mealsPerDay = getSelectedLabels(SectionId.MEAL_FREQUENCY)
        val proteinSources = getSelectedLabels(SectionId.PROTEIN_SOURCE)
        val prepDifficulty = getSelectedLabels(SectionId.PREP_DIFFICULTY)
        val prepTime = getSelectedLabels(SectionId.PREP_TIME)
        val additionalReq = getSelectedLabels(SectionId.ADDITIONAL_REQUIREMENTS)
        val previousMeals = results.flatMap { it.meals }.joinToString("; ") { it.name }
        val avoidMealsLine = if (previousMeals.isNotBlank()) {
            "- Avoid repeating the following meals from previous plans: $previousMeals"
        } else {
            ""
        }
        val profile = listOfNotNull(
            "Gender: $gender",
            userAge?.let { "Age: $it" },
            userWeight?.let { "Weight: ${it}kg" },
            userHeight?.let { "Height: ${it}cm" },
            targetWeight?.let { "Target: ${it}kg" },
            "Location: $location",
            "Language: $language"
        ).joinToString(", ")

        val lang = supportedLanguages.find { it.code == language }
            ?: supportedLanguages.first()


        val mealCount = mealsPerDay.firstOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: 3

        return """
        Create a personalized 1-day meal plan for the date ${forDate.format()} based on the following user profile and preferences:
    - $avoidMealsLine
    - $profile
    - Goals: ${goals.joinToString(", ").ifEmpty { "none" }}
    - Diet: ${diets.joinToString(", ").ifEmpty { "none" }}
    - Allergies: ${allergies.joinToString(", ").ifEmpty { "none" }}
    - Protein sources: ${proteinSources.joinToString(", ").ifEmpty { "any" }}
    - Prep difficulty: ${prepDifficulty.joinToString(", ").ifEmpty { "no preference" }}
    - Prep time: ${prepTime.joinToString(", ").ifEmpty { "no preference" }}
    - Additional: ${additionalReq.joinToString(", ").ifEmpty { "none" }}

    Requirements:
    - Output valid JSON only.
    - JSON root key: "meals"
    - Keys inside: meals[], name, ingredients[], instructions, calories, prepTime.
    - Each json must contain exactly $mealCount meals.
    - Instructions: 3–5 sentences per meal, no line breaks, use \\n if needed.
    - Escape all quotes as \\".
    - Important: Translate **all text values** strictly into $lang language, following standard $lang grammar and culinary terminology.
   
      # Sample JSON:
        # {
        #   "meals": [
        #         {
        #           "name": "Grilled Chicken Salad",
        #           "ingredients": ["Chicken breast", "Lettuce", "Tomatoes"],
        #           "instructions": "Step 1...\\nStep 2...",
        #           "calories": 450,
        #           "prepTime": "20 minutes"
        #         }
        #   ]
        # }
        
    Objective: Concise, readable, and valid JSON structure for direct app parsing.
        """.trimIndent()
    }

    fun fetchMealPlanWithFallbackAndUpdateState() {
        viewModelScope.launch {
            if (!isDateRangeValid()) {
                _errorMessage.value = "You can generate plan for 7 days most"
                return@launch
            }
            _isLoading.value = true
            _isPlanReady.value = false
            _multiDayPlan.value = null

            tryGenerateMealPlanRange()
        }
    }

    suspend fun tryGenerateMealPlanRange() {
        _errorMessage.value = null
        _isLoading.value = true
        val dates = getDatesInRange(startDate.value, endDate.value)
        val results = mutableListOf<DayMealPlanWithDates>()
        var allFallback = true
        for (date in dates) {
            val plan = fetchSingleDayMealPlanWithRetry(date, results)
            if (plan.meals.firstOrNull()?.name != "Data unavailable") {
                allFallback = false
            }
            results.add(DayMealPlanWithDates(day = date.format(), meals = plan.meals))

            delay(60000L) // drži delay da se ne spama API
        }

        _multiDayPlan.value = MealPlanSelection(days = results)
        _isLoading.value = false

        if (allFallback) {
            _errorMessage.value = "Didn't manage to fetch plan. Please try later."
        } else {
            // Barem jedan uspješan -> plan je spreman
            _isPlanReady.value = true
            _errorMessage.value = null
        }
    }

    suspend fun fetchSingleDayMealPlanWithRetry(
        date: LocalDate,
        results: MutableList<DayMealPlanWithDates>,
        maxRetries: Int = 3
    ): ParsedMealPlan {
        val prompt = generatePartialPrompt(date, results)

        var attempt = 0

        while (attempt < maxRetries) {
            attempt++
            try {
                val requestData = ChatCompletionRequest(
                    model = ModelId(BuildConfig.API_MODEL_ID),
                    messages = listOf(
                        ChatMessage(role = ChatRole.System, content = "You are a professional nutritionist."),
                        ChatMessage(role = ChatRole.User, content = prompt)
                    )
                )

                val response = fetchResponseBlocking(
                    apiUrl = BuildConfig.API_URL,
                    apiKey = BuildConfig.API_KEY,
                    requestData = requestData
                )

                val cleaned = sanitizeOpenAiJsonResponse(response)

                return Json { ignoreUnknownKeys = true }.decodeFromString<ParsedMealPlan>(cleaned)
            } catch (e: Exception) {
                Log.e("MealPlan", "Attempt $attempt failed", e)
                exponentialBackoffDelay(attempt)
            }
        }

        // fallback ako sve pokuša i ne uspije
        Log.e("MealPlan", "All attempts failed, returning fallback day")
        return ParsedMealPlan(
            meals = listOf(
                MealParsed(
                    name = "Data unavailable",
                    ingredients = listOf("Failed to generate meal plan."),
                    instructions = "Please try again later.",
                    calories = "0",
                    prepTime = "N/A"
                )
            )
        )
    }

    private suspend fun exponentialBackoffDelay(attempt: Int) {
        delay((Math.pow(2.0, attempt.toDouble()) * 1000L).toLong().coerceAtMost(60000L))
    }

    private fun calculateAge(birthDate: String): Int? {
        return try {
            val parts = birthDate.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val birth = LocalDate(year, month, day)
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            birth.yearsUntil(today)
        } catch (e: Exception) {
            Log.e("AgeCalc", "Invalid birthDate: $birthDate", e)
            null
        }
    }

    fun getDatesInRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var current = startDate
        while (current <= endDate) {
            dates.add(current)
            current = current.plus(1, DateTimeUnit.DAY)
        }
        return dates
    }

    fun isDateRangeValid(): Boolean {
        Log.d("DateCheck", "startDate: ${startDate.value}, endDate: ${endDate.value}")
        val daysDiff = startDate.value.daysUntil(endDate.value)
        Log.d("DateCheck", "daysUntil: $daysDiff")
        return daysDiff <= 6
    }

    private suspend fun fetchResponseBlocking(
        apiUrl: String,
        apiKey: String,
        requestData: ChatCompletionRequest
    ): String = withContext(Dispatchers.IO) {
        val client = HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000 /// timeout za cijeli request
                connectTimeoutMillis = 30_000 // timeout za spajanje na server
                socketTimeoutMillis = 60_000 // timeout za čitanje/pisanje s mrežom
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    }
                )
            }
        }

        client.use {
            try {
                val response = it.post(apiUrl) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $apiKey")
                    header("HTTP-Referer", "http://localhost")
                    header("X-Title", "My App")
                    setBody(json.encodeToString(requestData))
                }

                val raw = response.bodyAsText()
                Log.d("API", "Response raw: $raw")

                val chatResponse = json.decodeFromString<ChatCompletionResponse>(raw)
                chatResponse.choices.firstOrNull()?.message?.content ?: "No response"
            } catch (e: Exception) {
                Log.e("API", "API Error", e)
                "Error: ${e.localizedMessage}"
            }
        }
    }

    private fun createSection(sectionId: SectionId): MealSelectionSection {
        return when (sectionId) {
            SectionId.GOAL -> MealSelectionSection(
                id = sectionId.id,
                title = "Choose your goal",
                description = "Select your primary fitness goal to tailor your meal plan.",
                options = GoalOption.entries.map { MealSelectionOption(it.optionId, it.label) },
                selectionType = SelectionType.SINGLE,
                icon = Icons.Filled.MyLocation
            )
            SectionId.DIET -> MealSelectionSection(
                id = sectionId.id,
                title = "Diet preferences",
                description = "Pick your dietary preferences or restrictions.",
                options = DietOption.entries.map { MealSelectionOption(it.optionId, it.label) },
                selectionType = SelectionType.SINGLE,
                icon = Icons.Default.Fastfood
            )
            SectionId.ALLERGIES -> MealSelectionSection(
                id = sectionId.id,
                title = "Allergies / Intolerances",
                description = "Avoid ingredients you are allergic or intolerant to.",
                options = AllergyOption.entries.map { MealSelectionOption(it.optionId, it.label) },
                selectionType = SelectionType.MULTIPLE,
                icon = Icons.Default.Warning
            )
            SectionId.MEAL_FREQUENCY -> MealSelectionSection(
                id = sectionId.id,
                title = "Meals per day",
                description = "Choose how many meals you want daily.",
                options = MealFrequencyOption.entries.map { MealSelectionOption(it.optionId, it.label) },
                selectionType = SelectionType.SINGLE,
                icon = Icons.Default.CalendarToday
            )
            SectionId.PROTEIN_SOURCE -> MealSelectionSection(
                id = sectionId.id,
                title = "Preferred protein sources",
                description = "Select your preferred protein sources.",
                options = ProteinSourceOption.entries.map { MealSelectionOption(it.optionId, it.label) },
                selectionType = SelectionType.SINGLE,
                icon = Icons.Default.BatteryChargingFull
            )
            SectionId.PREP_DIFFICULTY -> MealSelectionSection(
                id = sectionId.id,
                title = "Meal preparation difficulty",
                description = "Select how hard you want your meal prep to be.",
                options = PrepDifficultyOption.entries.map { MealSelectionOption(it.optionId, it.label) },
                selectionType = SelectionType.SINGLE,
                icon = Icons.Default.BuildCircle
            )
            SectionId.PREP_TIME -> MealSelectionSection(
                id = sectionId.id,
                title = "Preferred meal prep time",
                description = "Select your preferred cooking time per meal.",
                options = PrepTimeOption.entries.map { MealSelectionOption(it.optionId, it.label) },
                selectionType = SelectionType.SINGLE,
                icon = Icons.Default.Timer
            )
            SectionId.ADDITIONAL_REQUIREMENTS -> MealSelectionSection(
                id = sectionId.id,
                title = "Additional requirements",
                description = "Any extra preferences or restrictions?",
                options = AdditionalRequirementsOption.entries.map { MealSelectionOption(it.optionId, it.label) },
                selectionType = SelectionType.MULTIPLE,
                icon = Icons.Default.List
            )
        }
    }
}
