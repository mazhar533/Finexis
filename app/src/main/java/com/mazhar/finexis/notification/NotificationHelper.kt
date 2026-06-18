package com.mazhar.finexis.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mazhar.finexis.MainActivity
import com.mazhar.finexis.R
import com.mazhar.finexis.model.AppNotification
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit

object NotificationHelper {
    const val CHANNEL_BUDGET_ALERTS = "budget_alerts"
    const val CHANNEL_DAILY_REMINDERS = "daily_reminders"

    private const val PREFS_NAME = "finexis_prefs"
    private const val KEY_TODAY_DATE = "today_date"
    private const val KEY_TODAY_SPENT = "today_spent"
    private const val KEY_TODAY_COUNT = "today_count"

    private const val PREFS_NOTIFS_NAME = "finexis_notifications_prefs"
    private const val KEY_NOTIFICATIONS_LIST = "notifications_list"

    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Budget Alerts Channel
        val budgetChannel = NotificationChannel(
            CHANNEL_BUDGET_ALERTS,
            "Budget Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when budget limits are approached or exceeded"
        }

        // Daily Reminders Channel
        val reminderChannel = NotificationChannel(
            CHANNEL_DAILY_REMINDERS,
            "Daily Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily summary and expense reminders"
        }

        notificationManager.createNotificationChannel(budgetChannel)
        notificationManager.createNotificationChannel(reminderChannel)
    }

    fun showBudgetAlert(context: Context, title: String, message: String) {
        // Save notification to local store first
        saveNotification(context, title, message)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.drawable.icon_app)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun showDailyReminder(context: Context, title: String, message: String) {
        // Save notification to local store first
        saveNotification(context, title, message)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDERS)
            .setSmallIcon(R.drawable.icon_app)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1001, builder.build())
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    // Save a notification to local SharedPreferences
    fun saveNotification(context: Context, title: String, message: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NOTIFS_NAME, Context.MODE_PRIVATE) ?: return
        val listString = sharedPrefs.getString(KEY_NOTIFICATIONS_LIST, "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(listString)
            val newNotification = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("title", title)
                put("message", message)
                put("timestamp", System.currentTimeMillis())
                put("isRead", false)
            }
            jsonArray.put(newNotification)
            sharedPrefs.edit { putString(KEY_NOTIFICATIONS_LIST, jsonArray.toString()) }
        } catch (_: Exception) {
            // Ignore
        }
    }

    // Get list of saved notifications from local store
    fun getNotifications(context: Context): List<AppNotification> {
        val sharedPrefs = context.getSharedPreferences(PREFS_NOTIFS_NAME, Context.MODE_PRIVATE)
            ?: return emptyList()
        val listString = sharedPrefs.getString(KEY_NOTIFICATIONS_LIST, "[]") ?: "[]"
        val list = mutableListOf<AppNotification>()
        try {
            val jsonArray = JSONArray(listString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    AppNotification(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        message = obj.getString("message"),
                        timestamp = obj.getLong("timestamp"),
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore
        }
        return list.sortedByDescending { it.timestamp }
    }

    // Mark all notifications as read
    fun markAllAsRead(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NOTIFS_NAME, Context.MODE_PRIVATE) ?: return
        val listString = sharedPrefs.getString(KEY_NOTIFICATIONS_LIST, "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(listString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                obj.put("isRead", true)
            }
            sharedPrefs.edit { putString(KEY_NOTIFICATIONS_LIST, jsonArray.toString()) }
        } catch (_: Exception) {
            // Ignore
        }
    }

    // Clear all notifications
    fun clearNotifications(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NOTIFS_NAME, Context.MODE_PRIVATE) ?: return
        sharedPrefs.edit { putString(KEY_NOTIFICATIONS_LIST, "[]") }
    }

    // Cache the daily spent totals
    fun cacheDailySpentData(context: Context, spentAmount: Double, count: Int) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())

        sharedPrefs.edit().apply {
            putString(KEY_TODAY_DATE, todayStr)
            putFloat(KEY_TODAY_SPENT, spentAmount.toFloat())
            putInt(KEY_TODAY_COUNT, count)
            apply()
        }
    }

    // Retrieve the daily spent totals
    fun getCachedDailySpentData(context: Context): Pair<Double, Int> {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?: return Pair(0.0, 0)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())
        val savedDate = sharedPrefs.getString(KEY_TODAY_DATE, "")

        return if (savedDate == todayStr) {
            val spent = sharedPrefs.getFloat(KEY_TODAY_SPENT, 0f).toDouble()
            val count = sharedPrefs.getInt(KEY_TODAY_COUNT, 0)
            Pair(spent, count)
        } else {
            Pair(0.0, 0)
        }
    }
}
