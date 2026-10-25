package com.example.habittracker.shared

import android.content.Context
import java.util.Calendar
import java.util.UUID

class AndroidHabitStorage(context: Context) : HabitStorage {
    private val preferences = context.getSharedPreferences("habit_tracker", Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString("habits", null)

    override fun write(value: String) {
        preferences.edit().putString("habits", value).apply()
    }
}

actual fun currentEpochDay(): Long {
    val calendar = Calendar.getInstance()
    val localMillis = calendar.timeInMillis + calendar.timeZone.getOffset(calendar.timeInMillis)
    return localMillis / 86_400_000L
}

actual fun newHabitId(): String = UUID.randomUUID().toString()
