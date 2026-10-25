package com.example.habittracker.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class HabitTrackerState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = true,
    val selectedDay: Long = currentEpochDay(),
    val showAddDialog: Boolean = false,
) {
    val today: Long get() = currentEpochDay()

    val stats: HabitStats
        get() {
            val week = (0L..6L).map { today - it }.toSet()
            return HabitStats(
                completedToday = habits.count { it.isCompleted(today) },
                total = habits.size,
                weeklyCompletions = habits.sumOf { habit ->
                    habit.completedEpochDays.count { it in week }
                },
                weeklyTarget = habits.size * 7,
            )
        }
}

class HabitStore(
    private val repository: HabitRepository,
) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(HabitTrackerState())
    val state: StateFlow<HabitTrackerState> = mutableState.asStateFlow()

    suspend fun load() {
        val habits = repository.load()
        mutableState.value = mutableState.value.copy(habits = habits, isLoading = false)
        repository.save(habits)
    }

    fun selectDay(day: Long) {
        mutableState.value = mutableState.value.copy(selectedDay = day)
    }

    fun openAddDialog() {
        mutableState.value = mutableState.value.copy(showAddDialog = true)
    }

    fun closeAddDialog() {
        mutableState.value = mutableState.value.copy(showAddDialog = false)
    }

    suspend fun addHabit(title: String, emoji: String, colorIndex: Int) = mutex.withLock {
        if (title.isBlank()) return@withLock
        val updated = mutableState.value.habits + Habit(
            id = newHabitId(),
            title = title.trim(),
            emoji = emoji,
            colorIndex = colorIndex,
            createdAtEpochDay = currentEpochDay(),
        )
        repository.save(updated)
        mutableState.value = mutableState.value.copy(habits = updated, showAddDialog = false)
    }

    suspend fun toggle(habitId: String) = mutex.withLock {
        val day = mutableState.value.selectedDay
        val updated = mutableState.value.habits.map { habit ->
            if (habit.id != habitId) habit
            else habit.copy(
                completedEpochDays = if (day in habit.completedEpochDays) {
                    habit.completedEpochDays - day
                } else {
                    habit.completedEpochDays + day
                },
            )
        }
        repository.save(updated)
        mutableState.value = mutableState.value.copy(habits = updated)
    }

    suspend fun delete(habitId: String) = mutex.withLock {
        val updated = mutableState.value.habits.filterNot { it.id == habitId }
        repository.save(updated)
        mutableState.value = mutableState.value.copy(habits = updated)
    }
}
