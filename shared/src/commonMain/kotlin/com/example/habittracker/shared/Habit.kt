package com.example.habittracker.shared

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: String,
    val title: String,
    val emoji: String,
    val colorIndex: Int,
    val createdAtEpochDay: Long,
    val weeklyTargetDays: Int = 7,
    val completedEpochDays: Set<Long> = emptySet(),
)

data class HabitStats(
    val completedToday: Int,
    val total: Int,
    val weeklyCompletions: Int,
    val weeklyTarget: Int,
)

fun Habit.isCompleted(day: Long): Boolean = day in completedEpochDays

fun Habit.completedInWeek(day: Long): Int {
    val start = startOfWeek(day)
    return completedEpochDays.count { it in start..(start + 6) }
}

fun Habit.weeklyProgress(day: Long): Float =
    (completedInWeek(day).toFloat() / weeklyTargetDays.coerceIn(1, 7)).coerceIn(0f, 1f)

fun Habit.currentStreak(today: Long): Int {
    var day = today
    var streak = 0
    while (day in completedEpochDays) {
        streak += 1
        day -= 1
    }
    return streak
}

fun Habit.successfulWeekStreak(today: Long): Int {
    var weekStart = startOfWeek(today)
    if (completedInWeek(today) < weeklyTargetDays) weekStart -= 7
    var streak = 0
    while (weekStart + 6 >= createdAtEpochDay) {
        val completions = completedEpochDays.count { it in weekStart..(weekStart + 6) }
        if (completions < weeklyTargetDays) break
        streak += 1
        weekStart -= 7
    }
    return streak
}

fun Habit.streakText(today: Long): String = if (weeklyTargetDays == 7) {
    "Серия: ${currentStreak(today)} дн."
} else {
    "Успешных недель подряд: ${successfulWeekStreak(today)}"
}

data class CalendarDate(val year: Int, val month: Int, val day: Int)

fun calendarDate(epochDay: Long): CalendarDate {
    val zeroDay = epochDay + 719_468
    val era = zeroDay / 146_097
    val dayOfEra = zeroDay - era * 146_097
    val yearOfEra = (dayOfEra - dayOfEra / 1_460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
    var year = yearOfEra + era * 400
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthPrime = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * monthPrime + 2) / 5 + 1
    val month = monthPrime + if (monthPrime < 10) 3 else -9
    year += if (month <= 2) 1 else 0
    return CalendarDate(year.toInt(), month.toInt(), day.toInt())
}

fun dayOfMonth(epochDay: Long): Int = calendarDate(epochDay).day

fun epochDayOf(year: Int, month: Int, day: Int): Long {
    val adjustedYear = year - if (month <= 2) 1 else 0
    val era = adjustedYear / 400
    val yearOfEra = adjustedYear - era * 400
    val monthPrime = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * monthPrime + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146_097L + dayOfEra - 719_468L
}

fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (year % 400 == 0 || year % 4 == 0 && year % 100 != 0) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

fun isoDayOfWeek(epochDay: Long): Int = (((epochDay + 3) % 7 + 7) % 7 + 1).toInt()

fun startOfWeek(epochDay: Long): Long = epochDay - isoDayOfWeek(epochDay) + 1

fun monthName(month: Int): String = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь",
)[month - 1]
