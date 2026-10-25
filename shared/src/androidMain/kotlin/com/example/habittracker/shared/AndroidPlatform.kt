package com.example.habittracker.shared

import android.content.Context
import android.content.Intent
import java.util.Calendar
import java.util.UUID

class AndroidHabitStorage(private val context: Context) : HabitStorage {
    private val preferences = context.getSharedPreferences("habit_tracker", Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString("habits", null)

    override fun write(value: String) {
        preferences.edit().putString("habits", value).apply()
        context.sendBroadcast(
            Intent("com.example.habittracker.HABITS_UPDATED").setPackage(context.packageName),
        )
    }

    override fun readFinance(): String? = preferences.getString("finance", null)

    override fun writeFinance(value: String) {
        preferences.edit().putString("finance", value).apply()
    }

    override fun isStarterPackInstalled(): Boolean = preferences.getBoolean("starter_pack_v2", false)

    override fun markStarterPackInstalled() {
        preferences.edit().putBoolean("starter_pack_v2", true).apply()
    }
}

actual fun currentEpochDay(): Long {
    val calendar = Calendar.getInstance()
    val localMillis = calendar.timeInMillis + calendar.timeZone.getOffset(calendar.timeInMillis)
    return localMillis / 86_400_000L
}

actual fun newHabitId(): String = UUID.randomUUID().toString()
