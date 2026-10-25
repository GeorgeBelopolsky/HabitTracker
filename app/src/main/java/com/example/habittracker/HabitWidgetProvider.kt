package com.example.habittracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.habittracker.shared.Habit
import com.example.habittracker.shared.currentEpochDay
import com.example.habittracker.shared.isCompleted
import com.example.habittracker.shared.isScheduledOn
import kotlinx.serialization.json.Json

class HabitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.habittracker.HABITS_UPDATED") {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, HabitWidgetProvider::class.java))
            onUpdate(context, manager, ids)
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
        val raw = context.getSharedPreferences("habit_tracker", Context.MODE_PRIVATE).getString("habits", null)
        val habits = raw?.let { value ->
            runCatching { Json { ignoreUnknownKeys = true }.decodeFromString<List<Habit>>(value) }.getOrNull()
        }.orEmpty()
        val today = currentEpochDay()
        val scheduled = habits.filter { it.isScheduledOn(today) }
        val completed = scheduled.count { it.isCompleted(today) }
        val views = RemoteViews(context.packageName, R.layout.habit_widget).apply {
            setTextViewText(R.id.widget_progress, "$completed/${scheduled.size}")
            setTextViewText(
                R.id.widget_subtitle,
                if (scheduled.isEmpty()) "Сегодня свободный день" else "привычек выполнено сегодня",
            )
            val openApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            setOnClickPendingIntent(R.id.widget_root, openApp)
        }
        manager.updateAppWidget(id, views)
    }
}
