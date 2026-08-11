package com.tbasic.fitnesstracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tbasic.fitnesstracker.R
import com.tbasic.fitnesstracker.localization.LocaleManager
import com.tbasic.fitnesstracker.repository.CombinedRoutineRepository
import com.tbasic.fitnesstracker.utils.getTodayRoutineDay
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyTrainingNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: CombinedRoutineRepository
) : CoroutineWorker(appContext, params) {

    companion object {
        const val CHANNEL_ID = "daily_training_channel"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            val userId = getUserId() ?: return Result.failure()
            val languageCode = inputData.getString("languageCode") ?: "en"
            val localizedContext = LocaleManager.getLocalizedContext(applicationContext, languageCode)

            val userRoutines = repository.getUserRoutines(userId)
            val todayDay = getTodayRoutineDay().getDisplayName(applicationContext)
            val routinesForToday = userRoutines.filter { it.day == todayDay && !it.completed }

            if (routinesForToday.isNotEmpty()) {
                val message = localizedContext.getString(R.string.daily_training_reminder, routinesForToday.size)

                showNotification(message)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showNotification(message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Training Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent koji otvara aplikaciju
        val intent = applicationContext.packageManager?.getLaunchIntentForPackage(applicationContext.packageName)
        val pendingIntent = android.app.PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_my)
            .setContentTitle("Fitness Tracker")
            .setContentText(message)
            .setContentIntent(pendingIntent) // vodi u aplikaciju
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getUserId(): String? {
        val prefs = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getString("userId", null)
    }
}
