package com.example.habittracker

import com.example.habittracker.shared.Habit
import com.example.habittracker.shared.currentStreak
import com.example.habittracker.shared.calendarDate
import com.example.habittracker.shared.completedInWeek
import com.example.habittracker.shared.dayOfMonth
import com.example.habittracker.shared.epochDayOf
import com.example.habittracker.shared.startOfWeek
import com.example.habittracker.shared.weeklyProgress
import com.example.habittracker.shared.ExchangeRates
import com.example.habittracker.shared.MoneyCurrency
import com.example.habittracker.shared.convertMinor
import com.example.habittracker.shared.isScheduledOn
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitCalculationsTest {
    @Test
    fun streakCountsConsecutiveDaysEndingToday() {
        val habit = Habit(
            id = "test",
            title = "Test",
            emoji = "✓",
            colorIndex = 0,
            createdAtEpochDay = 90,
            completedEpochDays = setOf(96, 98, 99, 100),
        )

        assertEquals(3, habit.currentStreak(today = 100))
    }

    @Test
    fun epochDayUsesGregorianCalendar() {
        assertEquals(1, dayOfMonth(0))
        assertEquals(29, dayOfMonth(19_782)) // 2024-02-29
        assertEquals(19_782L, epochDayOf(2024, 2, 29))
        assertEquals(2024, calendarDate(19_782).year)
    }

    @Test
    fun weeklyProgressUsesHabitTarget() {
        val today = 20_000L
        val weekStart = startOfWeek(today)
        val habit = Habit(
            id = "gym",
            title = "Gym",
            emoji = "🏋️‍♂️",
            colorIndex = 0,
            createdAtEpochDay = weekStart - 30,
            weeklyTargetDays = 3,
            completedEpochDays = setOf(weekStart, weekStart + 2, weekStart - 1),
        )

        assertEquals(2, habit.completedInWeek(today))
        assertEquals(2f / 3f, habit.weeklyProgress(today), 0.001f)
    }

    @Test
    fun exactWeekdaysControlVisibilityAndProgress() {
        val monday = startOfWeek(20_000L)
        val habit = Habit(
            id = "gym",
            title = "Gym",
            emoji = "fitness",
            colorIndex = 0,
            createdAtEpochDay = monday - 10,
            scheduledWeekdays = setOf(1, 3, 5),
            completedEpochDays = setOf(monday, monday + 1, monday + 2),
        )

        assertEquals(true, habit.isScheduledOn(monday))
        assertEquals(false, habit.isScheduledOn(monday + 1))
        assertEquals(2, habit.completedInWeek(monday + 3))
        assertEquals(2f / 3f, habit.weeklyProgress(monday + 3), 0.001f)
    }

    @Test
    fun debtConvertsThroughUsdRate() {
        val rates = ExchangeRates(rubPerUsd = 100.0, arsPerUsd = 1_500.0)
        assertEquals(1_00L, convertMinor(150_000, MoneyCurrency.ARS, MoneyCurrency.USD, rates))
        assertEquals(100_000L, convertMinor(10_00, MoneyCurrency.USD, MoneyCurrency.RUB, rates))
    }
}
