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
    val selectedHabitId: String? = null,
) {
    val today: Long get() = currentEpochDay()

    val stats: HabitStats
        get() {
            val weekStart = startOfWeek(today)
            val week = (0L..6L).map { weekStart + it }.toSet()
            val todayHabits = habits.filter { it.isScheduledOn(today) }
            return HabitStats(
                completedToday = todayHabits.count { it.isCompleted(today) },
                total = todayHabits.size,
                weeklyCompletions = habits.sumOf { habit ->
                    habit.completedEpochDays.count { it in week && habit.isScheduledOn(it) }
                        .coerceAtMost(habit.schedule().size)
                },
                weeklyTarget = habits.sumOf { it.schedule().size },
            )
        }

    val selectedHabit: Habit? get() = habits.firstOrNull { it.id == selectedHabitId }
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

    fun openHabit(habitId: String) {
        mutableState.value = mutableState.value.copy(selectedHabitId = habitId)
    }

    fun closeHabit() {
        mutableState.value = mutableState.value.copy(selectedHabitId = null)
    }

    suspend fun addHabit(title: String, emoji: String, colorIndex: Int, scheduledWeekdays: Set<Int>) = mutex.withLock {
        if (title.isBlank()) return@withLock
        val updated = mutableState.value.habits + Habit(
            id = newHabitId(),
            title = title.trim(),
            emoji = emoji,
            colorIndex = colorIndex,
            createdAtEpochDay = currentEpochDay(),
            weeklyTargetDays = scheduledWeekdays.size.coerceIn(1, 7),
            scheduledWeekdays = scheduledWeekdays.ifEmpty { setOf(1) },
        )
        repository.save(updated)
        mutableState.value = mutableState.value.copy(habits = updated, showAddDialog = false)
    }

    suspend fun toggle(habitId: String, day: Long = mutableState.value.selectedDay) = mutex.withLock {
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


    suspend fun updateSchedule(habitId: String, scheduledWeekdays: Set<Int>) = mutex.withLock {
        val updated = mutableState.value.habits.map { habit ->
            if (habit.id == habitId) habit.copy(
                weeklyTargetDays = scheduledWeekdays.size.coerceIn(1, 7),
                scheduledWeekdays = scheduledWeekdays.ifEmpty { setOf(1) },
            )
            else habit
        }
        repository.save(updated)
        mutableState.value = mutableState.value.copy(habits = updated)
    }

    suspend fun updateHabit(
        habitId: String,
        title: String,
        emoji: String,
        colorIndex: Int,
        scheduledWeekdays: Set<Int>,
    ) = mutex.withLock {
        if (title.isBlank() || scheduledWeekdays.isEmpty()) return@withLock
        val updated = mutableState.value.habits.map { habit ->
            if (habit.id == habitId) habit.copy(
                title = title.trim(),
                emoji = emoji,
                colorIndex = colorIndex,
                weeklyTargetDays = scheduledWeekdays.size,
                scheduledWeekdays = scheduledWeekdays,
            ) else habit
        }
        repository.save(updated)
        mutableState.value = mutableState.value.copy(habits = updated)
    }

    suspend fun delete(habitId: String) = mutex.withLock {
        val updated = mutableState.value.habits.filterNot { it.id == habitId }
        repository.save(updated)
        mutableState.value = mutableState.value.copy(habits = updated, selectedHabitId = null)
    }
}
