package com.example.habittracker.shared

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: String,
    val title: String,
    val emoji: String,
    val colorIndex: Int,
    val createdAtEpochDay: Long,
    val completedEpochDays: Set<Long> = emptySet(),
)

data class HabitStats(
    val completedToday: Int,
    val total: Int,
    val weeklyCompletions: Int,
    val weeklyTarget: Int,
)

fun Habit.isCompleted(day: Long): Boolean = day in completedEpochDays

fun Habit.currentStreak(today: Long): Int {
    var day = today
    var streak = 0
    while (day in completedEpochDays) {
        streak += 1
        day -= 1
    }
    return streak
}

fun dayOfMonth(epochDay: Long): Int {
    val zeroDay = epochDay + 719_468
    val era = zeroDay / 146_097
    val dayOfEra = zeroDay - era * 146_097
    val yearOfEra = (dayOfEra - dayOfEra / 1_460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthPrime = (5 * dayOfYear + 2) / 153
    return (dayOfYear - (153 * monthPrime + 2) / 5 + 1).toInt()
}
