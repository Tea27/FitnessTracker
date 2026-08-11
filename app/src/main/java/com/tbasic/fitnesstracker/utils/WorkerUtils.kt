package com.tbasic.fitnesstracker.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.tbasic.fitnesstracker.worker.DailyTrainingNotificationWorker
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import java.util.concurrent.TimeUnit

fun scheduleDailyTrainingNotificationWorker(context: Context, languageCode: String) {
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    val nowDateTime = now.toLocalDateTime(timeZone)

    val delayTime = nowDateTime.date.atTime(8, 17)
    val targetDateTime = if (nowDateTime < delayTime) delayTime else delayTime.date.plus(1, DateTimeUnit.DAY).atTime(8, 17)
    val targetInstant = targetDateTime.toInstant(timeZone)

    val initialDelay = now.until(targetInstant, DateTimeUnit.MILLISECOND)

    val inputData = workDataOf("languageCode" to languageCode)

    val request = PeriodicWorkRequestBuilder<DailyTrainingNotificationWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .setInputData(inputData)
        .addTag("daily_notification")
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_training_notification",
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        request
    )
}

fun storeUserId(context: Context, userId: String) {
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("userId", userId).apply()
}
