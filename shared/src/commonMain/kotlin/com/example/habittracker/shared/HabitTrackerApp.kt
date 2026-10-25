package com.example.habittracker.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Accent = Color(0xFF6C5CE7)
private val AccentDark = Color(0xFFA99DF4)
private val AppBackground = Color(0xFFF7F7FB)
private val DarkBackground = Color(0xFF121218)
private val HabitColors = listOf(
    Color(0xFF70D6B4),
    Color(0xFFFFC857),
    Color(0xFFFF8B8B),
    Color(0xFF74B9FF),
    Color(0xFFC89BFF),
)
private val HabitEmojis = listOf("✨", "💧", "📚", "🏃", "🧘", "🥗", "😴", "🎯")

@Composable
fun HabitTrackerApp(storage: HabitStorage) {
    val store = remember(storage) { HabitStore(LocalHabitRepository(storage)) }
    val state by store.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(store) { store.load() }

    HabitTrackerTheme {
        HabitTrackerScreen(
            state = state,
            onSelectDay = store::selectDay,
            onToggle = { id -> scope.launch { store.toggle(id) } },
            onDelete = { id -> scope.launch { store.delete(id) } },
            onAddClick = store::openAddDialog,
        )

        if (state.showAddDialog) {
            AddHabitDialog(
                onDismiss = store::closeAddDialog,
                onConfirm = { title, emoji, color ->
                    scope.launch { store.addHabit(title, emoji, color) }
                },
            )
        }
    }
}

@Composable
private fun HabitTrackerTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) {
            darkColorScheme(primary = AccentDark, background = DarkBackground, surface = Color(0xFF202028))
        } else {
            lightColorScheme(primary = Accent, background = AppBackground, surface = Color.White)
        },
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitTrackerScreen(
    state: HabitTrackerState,
    onSelectDay: (Long) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAddClick: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { Header() }
                item { ProgressSummary(state.stats) }
                item {
                    DayPicker(
                        today = state.today,
                        selectedDay = state.selectedDay,
                        onSelectDay = onSelectDay,
                    )
                }
                item {
                    Text(
                        text = if (state.selectedDay == state.today) "Сегодня" else "Привычки за день",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                if (state.habits.isEmpty()) {
                    item { EmptyState(onAddClick) }
                } else {
                    items(state.habits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            day = state.selectedDay,
                            today = state.today,
                            onToggle = { onToggle(habit.id) },
                            onDelete = { onDelete(habit.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column {
        Text(
            text = "Мой ритм",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Маленькие шаги каждый день",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressSummary(stats: HabitStats) {
    val todayProgress = if (stats.total == 0) 0f else stats.completedToday.toFloat() / stats.total
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(22.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${stats.completedToday}/${stats.total}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "выполнено сегодня",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { todayProgress },
                modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.22f),
            )
            Spacer(Modifier.height(12.dp))
            val weeklyPercent = if (stats.weeklyTarget == 0) 0 else {
                stats.weeklyCompletions * 100 / stats.weeklyTarget
            }
            Text(
                text = "За неделю — $weeklyPercent%",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun DayPicker(today: Long, selectedDay: Long, onSelectDay: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        (6 downTo 0).forEach { daysAgo ->
            val day = today - daysAgo
            val selected = day == selectedDay
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                    )
                    .clickable { onSelectDay(day) }
                    .padding(horizontal = 15.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = when (daysAgo) {
                        0 -> "СЕГ"
                        1 -> "ВЧЕ"
                        else -> "−$daysAgo"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = dayOfMonth(day).toString(),
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    day: Long,
    today: Long,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val completed = habit.isCompleted(day)
    val color = HabitColors[habit.colorIndex.mod(HabitColors.size)]
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(color.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(habit.emoji, fontSize = 25.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Серия: ${habit.currentStreak(today)} дн.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onDelete, contentPadding = PaddingValues(6.dp)) {
                Text("×", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = onToggle,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (completed) color else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (completed) Color(0xFF173C32) else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(46.dp),
            ) {
                Text(if (completed) "✓" else "", fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🌱", fontSize = 54.sp)
        Spacer(Modifier.height(12.dp))
        Text("Начните с одной привычки", fontWeight = FontWeight.Bold)
        Text(
            "Добавьте простое действие, которое хотите повторять каждый день.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp),
        )
        TextButton(onClick = onAddClick) { Text("Добавить привычку") }
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(HabitEmojis.first()) }
    var colorIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая привычка", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Иконка", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HabitEmojis.forEach { candidate ->
                        Surface(
                            modifier = Modifier.size(42.dp).clickable { emoji = candidate },
                            shape = CircleShape,
                            color = if (emoji == candidate) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Box(contentAlignment = Alignment.Center) { Text(candidate, fontSize = 21.sp) }
                        }
                    }
                }
                Text("Цвет", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HabitColors.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(if (colorIndex == index) 34.dp else 30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { colorIndex = index },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (colorIndex == index) Text("✓", color = Color(0xFF173C32))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, emoji, colorIndex) }, enabled = title.isNotBlank()) {
                Text("Добавить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}
