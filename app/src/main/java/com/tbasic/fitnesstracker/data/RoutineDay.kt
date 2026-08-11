package com.tbasic.fitnesstracker.data

import android.content.Context
import com.tbasic.fitnesstracker.R

enum class RoutineDay(val stringResId: Int) {
    MONDAY(R.string.day_monday),
    TUESDAY(R.string.day_tuesday),
    WEDNESDAY(R.string.day_wednesday),
    THURSDAY(R.string.day_thursday),
    FRIDAY(R.string.day_friday),
    SATURDAY(R.string.day_saturday),
    SUNDAY(R.string.day_sunday);

    fun getDisplayName(context: Context): String {
        return context.getString(stringResId)
    }

    companion object {
        fun fromDisplayName(context: Context, displayName: String): String? {
            return entries.find { it.getDisplayName(context).equals(displayName, ignoreCase = true) }?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
        }
    }
}
