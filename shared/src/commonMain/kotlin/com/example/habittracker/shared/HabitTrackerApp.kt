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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.Pool
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Accent = Color(0xFF4F8F00)
private val AccentDark = Color(0xFFA8FF00)
private val AppBackground = Color(0xFFF7F7FB)
private val DarkBackground = Color(0xFF121218)
internal val HabitColors = listOf(
    Color(0xFF70D6B4),
    Color(0xFFFFC857),
    Color(0xFFFF8B8B),
    Color(0xFF74B9FF),
    Color(0xFFC89BFF),
)
internal data class HabitIconOption(val key: String, val image: ImageVector)

private val HabitIcons = listOf(
    HabitIconOption("fitness", Icons.Rounded.FitnessCenter),
    HabitIconOption("water", Icons.Rounded.LocalDrink),
    HabitIconOption("reading", Icons.AutoMirrored.Rounded.MenuBook),
    HabitIconOption("running", Icons.AutoMirrored.Rounded.DirectionsRun),
    HabitIconOption("meditation", Icons.Rounded.SelfImprovement),
    HabitIconOption("food", Icons.Rounded.Restaurant),
    HabitIconOption("sleep", Icons.Rounded.Bedtime),
    HabitIconOption("goal", Icons.Rounded.TrackChanges),
    HabitIconOption("cycling", Icons.AutoMirrored.Rounded.DirectionsBike),
    HabitIconOption("swimming", Icons.Rounded.Pool),
    HabitIconOption("walking", Icons.AutoMirrored.Rounded.DirectionsWalk),
    HabitIconOption("sun", Icons.Rounded.WbSunny),
    HabitIconOption("nature", Icons.Rounded.Spa),
    HabitIconOption("writing", Icons.Rounded.Edit),
    HabitIconOption("music", Icons.Rounded.MusicNote),
    HabitIconOption("cleaning", Icons.Rounded.CleaningServices),
    HabitIconOption("money", Icons.Rounded.Savings),
    HabitIconOption("digital-detox", Icons.Rounded.PhoneDisabled),
    HabitIconOption("love", Icons.Rounded.Favorite),
    HabitIconOption("work", Icons.Rounded.Work),
)

internal fun habitIcon(key: String): ImageVector = HabitIcons.firstOrNull {
    it.key == key || legacyIconKey(key) == it.key
}?.image ?: Icons.Rounded.TrackChanges

private fun legacyIconKey(value: String): String = when (value) {
    "💧" -> "water"
    "📚" -> "reading"
    "🏃", "🏃‍♂️" -> "running"
    "🧘", "🧘‍♀️", "🙏" -> "meditation"
    "🥗" -> "food"
    "😴" -> "sleep"
    "🏋️‍♂️" -> "fitness"
    "🚴‍♀️" -> "cycling"
    "🏊‍♂️" -> "swimming"
    "🚶", "🚶‍♂️" -> "walking"
    "☀️" -> "sun"
    "🌿" -> "nature"
    "✍️" -> "writing"
    "🎸" -> "music"
    "🧹" -> "cleaning"
    "💰" -> "money"
    "📵" -> "digital-detox"
    "❤️" -> "love"
    else -> "goal"
}

@Composable
fun HabitTrackerApp(storage: HabitStorage) {
    val store = remember(storage) { HabitStore(LocalHabitRepository(storage)) }
    val financeStore = remember(storage) { FinanceStore(FinanceRepository(storage)) }
    val state by store.state.collectAsState()
    val financeState by financeStore.state.collectAsState()
    val scope = rememberCoroutineScope()
    var section by remember { mutableStateOf(AppSection.HABITS) }

    LaunchedEffect(store) { store.load() }
    LaunchedEffect(financeStore) { financeStore.load() }

    HabitTrackerTheme {
        Box(Modifier.fillMaxSize()) {
            when (section) {
                AppSection.HABITS -> HabitTrackerScreen(
                    state = state,
                    onSelectDay = store::selectDay,
                    onToggle = { id -> scope.launch { store.toggle(id) } },
                    onHabitClick = store::openHabit,
                    onAddClick = store::openAddDialog,
                )
                AppSection.FINANCE -> FinanceScreen(
                    state = financeState,
                    onCurrencyChange = { scope.launch { financeStore.selectCurrency(it) } },
                    onBudgetChange = { currency, amount -> scope.launch { financeStore.setBudget(currency, amount) } },
                    onRatesChange = { rub, ars -> scope.launch { financeStore.setRates(rub, ars) } },
                    onAdd = { title, amount, currency, type, category ->
                        scope.launch { financeStore.addTransaction(title, amount, currency, type, category) }
                    },
                    onDelete = { scope.launch { financeStore.deleteTransaction(it) } },
                )
            }
            AppBottomBar(section, { section = it }, Modifier.align(Alignment.BottomCenter))
        }

        if (state.showAddDialog) {
            AddHabitDialog(
                onDismiss = store::closeAddDialog,
                onConfirm = { title, emoji, color, scheduledDays ->
                    scope.launch { store.addHabit(title, emoji, color, scheduledDays) }
                },
            )
        }

        state.selectedHabit?.let { habit ->
            HabitDetailsDialog(
                habit = habit,
                today = state.today,
                onDismiss = store::closeHabit,
                onToggleDay = { day -> scope.launch { store.toggle(habit.id, day) } },
                onScheduleChange = { scheduledDays ->
                    scope.launch { store.updateSchedule(habit.id, scheduledDays) }
                },
                onDelete = { scope.launch { store.delete(habit.id) } },
            )
        }
    }
}

private enum class AppSection { HABITS, FINANCE }

@Composable
private fun AppBottomBar(selected: AppSection, onSelect: (AppSection) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 12.dp),
        shape = RoundedCornerShape(30.dp),
        color = Color(0xEE11130F),
        shadowElevation = 12.dp,
    ) {
        Row(Modifier.padding(7.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            BottomBarItem(
                label = "Привычки",
                icon = Icons.Rounded.TrackChanges,
                selected = selected == AppSection.HABITS,
                onClick = { onSelect(AppSection.HABITS) },
                modifier = Modifier.weight(1f),
            )
            BottomBarItem(
                label = "Финансы",
                icon = Icons.Rounded.Savings,
                selected = selected == AppSection.FINANCE,
                onClick = { onSelect(AppSection.FINANCE) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomBarItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val color = if (selected) Color(0xFFA8FF00) else Color.White.copy(alpha = 0.58f)
    Column(
        modifier = modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick).padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
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
    onHabitClick: (String) -> Unit,
    onAddClick: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.padding(bottom = 72.dp),
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
                val visibleHabits = state.habits.filter { it.isScheduledOn(state.selectedDay) }
                if (state.habits.isEmpty()) {
                    item { EmptyState(onAddClick) }
                } else if (visibleHabits.isEmpty()) {
                    item { DayOffState() }
                } else {
                    items(visibleHabits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            day = state.selectedDay,
                            today = state.today,
                            onToggle = { onToggle(habit.id) },
                            onOpen = { onHabitClick(habit.id) },
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
    onOpen: () -> Unit,
) {
    val completed = habit.isCompleted(day)
    val color = HabitColors[habit.colorIndex.mod(HabitColors.size)]
    val completedThisWeek = habit.completedInWeek(today)
    val goalReached = completedThisWeek >= habit.schedule().size
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(62.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { habit.weeklyProgress(today) },
                        modifier = Modifier.fillMaxSize(),
                        color = color,
                        trackColor = color.copy(alpha = 0.18f),
                        strokeWidth = 5.dp,
                    )
                    Surface(shape = CircleShape, color = color.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = habitIcon(habit.emoji),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(25.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(habit.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (goalReached) "Недельная цель выполнена" else habit.streakText(today),
                        color = if (goalReached) color else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (goalReached) FontWeight.SemiBold else FontWeight.Normal,
                    )
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
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { habit.weeklyProgress(today) },
                    modifier = Modifier.weight(1f).height(7.dp).clip(CircleShape),
                    color = color,
                    trackColor = color.copy(alpha = 0.18f),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "$completedThisWeek/${habit.schedule().size} по плану",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
private fun DayOffState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.SelfImprovement,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("На этот день ничего не запланировано", fontWeight = FontWeight.Bold)
        Text(
            "Можно отдохнуть или открыть привычку в другой день.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Set<Int>) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(HabitIcons.first().key) }
    var colorIndex by remember { mutableIntStateOf(0) }
    var scheduledDays by remember { mutableStateOf(setOf(1, 3, 5)) }

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
                    HabitIcons.forEach { candidate ->
                        Surface(
                            modifier = Modifier.size(44.dp).clickable { emoji = candidate.key },
                            shape = RoundedCornerShape(14.dp),
                            color = if (emoji == candidate.key) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = candidate.image,
                                    contentDescription = null,
                                    tint = if (emoji == candidate.key) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(23.dp),
                                )
                            }
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
                WeekdaySelector(
                    value = scheduledDays,
                    onValueChange = { scheduledDays = it },
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, emoji, colorIndex, scheduledDays) }, enabled = title.isNotBlank()) {
                Text("Добавить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
internal fun WeekdaySelector(value: Set<Int>, onValueChange: (Set<Int>) -> Unit) {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Дни недели", style = MaterialTheme.typography.labelLarge)
        labels.chunked(4).forEachIndexed { rowIndex, rowLabels ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowLabels.forEachIndexed { columnIndex, label ->
                    val day = rowIndex * 4 + columnIndex + 1
                    val selected = day in value
                Surface(
                        modifier = Modifier.weight(1f).height(42.dp).clickable {
                            val updated = if (selected) value - day else value + day
                            if (updated.isNotEmpty()) onValueChange(updated)
                        },
                        shape = RoundedCornerShape(14.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                                label,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                }
                repeat(4 - rowLabels.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Text(
            "Привычка будет показана только в выбранные дни.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
