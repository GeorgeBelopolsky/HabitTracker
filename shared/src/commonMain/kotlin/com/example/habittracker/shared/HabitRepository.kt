package com.example.habittracker.shared

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface HabitStorage {
    fun read(): String?
    fun write(value: String)
    fun readFinance(): String? = null
    fun writeFinance(value: String) = Unit
    fun isStarterPackInstalled(): Boolean = false
    fun markStarterPackInstalled() = Unit
}

interface HabitRepository {
    suspend fun load(): List<Habit>
    suspend fun save(habits: List<Habit>)
}

class LocalHabitRepository(
    private val storage: HabitStorage,
) : HabitRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun load(): List<Habit> {
        val decoded = storage.read()?.let { raw ->
            runCatching { json.decodeFromString<List<Habit>>(raw) }.getOrNull()
        }.orEmpty()
        val result = if (storage.isStarterPackInstalled()) {
            decoded
        } else {
            val starters = starterHabits()
            decoded + starters.filterNot { starter -> decoded.any { it.id == starter.id } }
        }
        storage.markStarterPackInstalled()
        return result
    }

    override suspend fun save(habits: List<Habit>) {
        storage.write(json.encodeToString(habits))
    }

    private fun starterHabits(): List<Habit> {
        val today = currentEpochDay()
        return listOf(
            Habit(
                id = "starter-water",
                title = "Пить воду",
                emoji = "water",
                colorIndex = 0,
                createdAtEpochDay = today - 6,
                weeklyTargetDays = 7,
                scheduledWeekdays = (1..7).toSet(),
                completedEpochDays = setOf(today - 3, today - 2, today - 1),
            ),
            Habit(
                id = "starter-reading",
                title = "Читать 20 минут",
                emoji = "reading",
                colorIndex = 1,
                createdAtEpochDay = today - 6,
                weeklyTargetDays = 5,
                scheduledWeekdays = setOf(1, 2, 3, 4, 5),
                completedEpochDays = setOf(today - 2, today - 1, today),
            ),
            Habit(
                id = "starter-walk",
                title = "Прогулка",
                emoji = "walking",
                colorIndex = 2,
                createdAtEpochDay = today - 6,
                weeklyTargetDays = 3,
                scheduledWeekdays = setOf(1, 3, 5),
                completedEpochDays = setOf(today - 4, today - 2),
            ),
        )
    }
}

expect fun currentEpochDay(): Long

expect fun newHabitId(): String
