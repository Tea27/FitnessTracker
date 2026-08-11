package com.tbasic.fitnesstracker.utils

import com.tbasic.fitnesstracker.data.RoutineDay
import kotlinx.datetime.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

fun getTodayRoutineDay(): RoutineDay {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> RoutineDay.MONDAY
        Calendar.TUESDAY -> RoutineDay.TUESDAY
        Calendar.WEDNESDAY -> RoutineDay.WEDNESDAY
        Calendar.THURSDAY -> RoutineDay.THURSDAY
        Calendar.FRIDAY -> RoutineDay.FRIDAY
        Calendar.SATURDAY -> RoutineDay.SATURDAY
        Calendar.SUNDAY -> RoutineDay.SUNDAY
        else -> RoutineDay.MONDAY
    }
}

fun isGoalInCurrentWeek(startDateMillis: Long): Boolean {
    val now = System.currentTimeMillis()

    val daysSinceStart = TimeUnit.MILLISECONDS.toDays(now - startDateMillis)
    return daysSinceStart in 0..6 // unutar istog tjedna od startDate
}

fun Long.toLocalDate(): LocalDate {
    return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date
}

fun String?.toLocalDateOrNull(): LocalDate? = try {
    if (this.isNullOrBlank()) null else LocalDate.parse(this)
} catch (e: Exception) {
    null
}

fun LocalDate.atStartOfDayInMillis(): Long {
    return this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

fun LocalDate.atEndOfDayInMillis(): Long {
    return this
        .plus(1, DateTimeUnit.DAY)
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .toEpochMilliseconds() - 1
}

fun getWeekOfMonth(date: LocalDate): Int {
    val firstDayOfMonth = LocalDate(date.year, date.monthNumber, 1)
    val dayOfMonth = date.dayOfMonth
    val firstWeekday = firstDayOfMonth.dayOfWeek.isoDayNumber // 1=Mon, 7=Sun
    return ((dayOfMonth + firstWeekday - 2) / 7) + 1
}

fun getStartOfWeek(date: LocalDate): LocalDate {
    val dayOfWeek = date.dayOfWeek.isoDayNumber // PON=1, NED=7
    return date.minus((dayOfWeek - 1).toLong(), DateTimeUnit.DAY)
}

fun getWeekdayInitials(language: String): List<String> {
    return when (language) {
        "hr" -> listOf("P", "U", "S", "Č", "P", "S", "N") // pon, uto, sri, čet, pet, sub, ned
        "en" -> listOf("M", "T", "W", "T", "F", "S", "S")
        "de" -> listOf("M", "D", "M", "D", "F", "S", "S")
        "fr" -> listOf("L", "M", "M", "J", "V", "S", "D")
        "it" -> listOf("L", "M", "M", "G", "V", "S", "D")
        else -> listOf("M", "T", "W", "T", "F", "S", "S")
    }
}

fun getTodayIsoDate(): String {
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    return today.toString() // ovo vraća ISO 8601 format, npr. "2025-06-17"
}

fun getTodayEpochMillis(): Long {
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    return today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

fun String.toDateLongOrNull(formatter: SimpleDateFormat): Long? {
    return try {
        formatter.parse(this)?.time
    } catch (e: Exception) {
        null
    }
}

fun Long?.toFormattedDateString(formatter: SimpleDateFormat): String {
    return this?.let { formatter.format(it) } ?: ""
}

fun LocalDate.toFormattedDateString(): String {
    return "${this.year.toString().padStart(4, '0')}-" +
        "${this.monthNumber.toString().padStart(2, '0')}-" +
        this.dayOfMonth.toString().padStart(2, '0')
}

fun LocalDate.toMillis(): Long {
    return this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

fun LocalDate.formatAsReadableDate(): String {
    val javaLocalDate = this.toJavaLocalDate()
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    return javaLocalDate.format(formatter)
}

fun String.formatAsReadableDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val date = inputFormat.parse(this)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        this
    }
}

fun formatDate(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    return try {
        java.time.LocalDate.parse(dateString).format(formatter)
    } catch (e: Exception) {
        dateString
    }
}
