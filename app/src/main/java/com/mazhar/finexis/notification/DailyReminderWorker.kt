package com.mazhar.finexis.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Fetch today's progress cached by the UI
        val (spent, count) = NotificationHelper.getCachedDailySpentData(applicationContext)
        val sharedPrefs = applicationContext.getSharedPreferences("finexis_prefs", Context.MODE_PRIVATE)
        val currency = sharedPrefs.getString("currency", "PKR") ?: "PKR"

        if (count > 0) {
            val title = "Daily Expense Review 📊"
            val formattedSpent = com.mazhar.finexis.ui.utils.CurrencyHelper.format(spent, currency, showDecimal = true)
            val message = "You spent a total of $formattedSpent today. Review your $count transactions!"
            NotificationHelper.showDailyReminder(applicationContext, title, message)
        } else {
            val title = "Log Your Expenses Today! ✍️"
            val message = "You haven't logged any expenses today. Don't forget to track your spending!"
            NotificationHelper.showDailyReminder(applicationContext, title, message)
        }

        return Result.success()
    }
}
