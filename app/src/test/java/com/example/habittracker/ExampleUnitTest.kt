package com.example.habittracker

import com.example.habittracker.shared.Habit
import com.example.habittracker.shared.currentStreak
import com.example.habittracker.shared.dayOfMonth
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
    }
}
