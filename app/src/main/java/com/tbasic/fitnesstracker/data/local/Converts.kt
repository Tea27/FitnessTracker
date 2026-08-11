package com.tbasic.fitnesstracker.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tbasic.fitnesstracker.data.DayMealPlanWithDates
import com.tbasic.fitnesstracker.data.RoutineExercise
import com.tbasic.fitnesstracker.data.RoutineSet
import com.tbasic.fitnesstracker.data.RoutineTranslation
import com.tbasic.fitnesstracker.data.Translation
import com.tbasic.fitnesstracker.vm.FitnessGoal

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun toStringList(json: String): List<String> =
        gson.fromJson(json, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun fromTranslations(map: Map<String, Translation>): String = gson.toJson(map)

    @TypeConverter
    fun toTranslations(json: String): Map<String, Translation> =
        gson.fromJson(json, object : TypeToken<Map<String, Translation>>() {}.type)

    @TypeConverter
    fun fromRoutineExerciseList(list: List<RoutineExercise>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toRoutineExerciseList(json: String?): List<RoutineExercise>? {
        val type = object : TypeToken<List<RoutineExercise>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromRoutineSetList(list: List<RoutineSet>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toRoutineSetList(json: String?): List<RoutineSet>? {
        val type = object : TypeToken<List<RoutineSet>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromTranslationMap(map: Map<String, RoutineTranslation>?): String? {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toTranslationMap(json: String?): Map<String, RoutineTranslation>? {
        val type = object : TypeToken<Map<String, RoutineTranslation>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromBooleanList(list: List<Boolean>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toBooleanList(json: String?): List<Boolean>? {
        val type = object : TypeToken<List<Boolean>>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun fromDayMealPlanWithDatesList(value: List<DayMealPlanWithDates>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDayMealPlanWithDatesList(value: String): List<DayMealPlanWithDates> {
        val type = object : TypeToken<List<DayMealPlanWithDates>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromFitnessGoal(value: FitnessGoal?): String? {
        return value?.name
    }

    @TypeConverter
    fun toFitnessGoal(value: String?): FitnessGoal? {
        return value?.let { FitnessGoal.valueOf(it) }
    }

//    @TypeConverter
//    fun fromDayMealPlanList(value: List<DayMealPlanParsed>): String {
//        return gson.toJson(value)
//    }
//
//    @TypeConverter
//    fun toDayMealPlanList(value: String): List<DayMealPlanParsed> {
//        val type = object : TypeToken<List<DayMealPlanParsed>>() {}.type
//        return gson.fromJson(value, type)
//    }
}
