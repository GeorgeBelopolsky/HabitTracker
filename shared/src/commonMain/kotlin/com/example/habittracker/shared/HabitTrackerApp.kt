package com.example.habittracker.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BarChart
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
import kotlin.math.abs

internal val AppGreen = Color(0xFF4F9B58)
internal val AppBlue = Color(0xFF2F7DFF)
internal val AccentOrange = Color(0xFFC45F2A)
internal val AppLavender = Color(0xFF9B87F5)
internal val AppBackground = Color(0xFF080B10)
internal val AppSurface = Color(0xFF121821)
internal val AppSurfaceElevated = Color(0xFF1A222D)
internal val HabitColors = listOf(
    AppBlue,
    Color(0xFF245CC7),
    Color(0xFF5598F6),
    AppGreen,
    Color(0xFF347842),
    Color(0xFF70AE72),
    AccentOrange,
    Color(0xFFD9822B),
    AppLavender,
    Color(0xFF7964C8),
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
    var analyticsKind by remember { mutableStateOf(AnalyticsKind.FINANCE) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }

    LaunchedEffect(store) { store.load() }
    LaunchedEffect(financeStore) {
        financeStore.load()
        financeStore.refreshRates()
    }

    HabitTrackerTheme {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = section,
                transitionSpec = {
                    val movingForward = targetState.ordinal > initialState.ordinal
                    val enter = slideInHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        initialOffsetX = { width -> if (movingForward) width / 5 else -width / 5 },
                    ) + fadeIn()
                    val exit = slideOutHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        targetOffsetX = { width -> if (movingForward) -width / 7 else width / 7 },
                    ) + fadeOut()
                    enter.togetherWith(exit).using(SizeTransform(clip = false))
                },
                label = "section-transition",
            ) { targetSection ->
                when (targetSection) {
                    AppSection.HABITS -> HabitTrackerScreen(
                        state = state,
                        onSelectDay = store::selectDay,
                        onToggle = { id -> scope.launch { store.toggle(id) } },
                        onHabitClick = store::openHabit,
                        onAddClick = store::openAddDialog,
                        onOpenAnalytics = {
                            analyticsKind = AnalyticsKind.HABITS
                            section = AppSection.ANALYTICS
                        },
                    )
                    AppSection.FINANCE -> FinanceScreen(
                        state = financeState,
                        onCurrencyChange = { scope.launch { financeStore.selectCurrency(it) } },
                        onBudgetChange = { currency, amount -> scope.launch { financeStore.setBudget(currency, amount) } },
                        onRatesChange = { rub, ars -> scope.launch { financeStore.setRates(rub, ars) } },
                        onRefreshRates = { scope.launch { financeStore.refreshRates(force = true) } },
                        onAdd = { amount, currency, type, target, comment ->
                            scope.launch { financeStore.addTransaction(amount, currency, type, target, comment) }
                        },
                        onDelete = { scope.launch { financeStore.deleteTransaction(it) } },
                        onUpdate = { id, amount, currency, target, comment ->
                            scope.launch { financeStore.updateTransaction(id, amount, currency, target, comment) }
                        },
                        onToggleSettled = { scope.launch { financeStore.toggleTransactionSettled(it) } },
                        onAddCategory = { scope.launch { financeStore.addCategory(it) } },
                        onDeleteCategory = { scope.launch { financeStore.deleteCategory(it) } },
                        onCategoryColorChange = { name, color ->
                            scope.launch { financeStore.updateCategoryColor(name, color) }
                        },
                        onAddFriend = { scope.launch { financeStore.addFriend(it) } },
                        onDeleteFriend = { scope.launch { financeStore.deleteFriend(it) } },
                        onFriendColorChange = { name, color ->
                            scope.launch { financeStore.updateFriendColor(name, color) }
                        },
                    )
                    AppSection.ANALYTICS -> AnalyticsScreen(
                        habitState = state,
                        financeState = financeState,
                        kind = analyticsKind,
                        onKindChange = { analyticsKind = it },
                        onHabitClick = store::openHabit,
                    )
                }
            }
            AppBottomBar(
                selected = section,
                onSelect = { target ->
                    if (target == AppSection.ANALYTICS && section != AppSection.ANALYTICS) {
                        analyticsKind = if (section == AppSection.HABITS) AnalyticsKind.HABITS else AnalyticsKind.FINANCE
                    }
                    section = target
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
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
                onEdit = { editingHabit = habit },
                onDelete = { scope.launch { store.delete(habit.id) } },
            )
        }

        editingHabit?.let { habit ->
            EditHabitDialog(
                habit = habit,
                onDismiss = { editingHabit = null },
                onConfirm = { title, emoji, color, scheduledDays ->
                    scope.launch { store.updateHabit(habit.id, title, emoji, color, scheduledDays) }
                    editingHabit = null
                },
            )
        }
    }
}

private enum class AppSection { HABITS, FINANCE, ANALYTICS }

@Composable
private fun AppBottomBar(selected: AppSection, onSelect: (AppSection) -> Unit, modifier: Modifier = Modifier) {
    val glassShape = RoundedCornerShape(31.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .shadow(18.dp, glassShape)
            .border(1.dp, Color.White.copy(alpha = 0.20f), glassShape),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
    ) {
        BoxWithConstraints(
            Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xF2182433),
                            Color(0xF20D121A),
                            Color(0xF2281712),
                        ),
                    ),
                )
                .padding(6.dp)
                .height(59.dp),
        ) {
            val itemWidth = maxWidth / AppSection.entries.size
            val density = LocalDensity.current
            val itemWidthPx = with(density) { itemWidth.toPx() }
            var dragging by remember { mutableStateOf(false) }
            var dragOffsetPx by remember { mutableFloatStateOf(0f) }
            val settledOffset by animateDpAsState(
                targetValue = itemWidth * selected.ordinal,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "glass-indicator",
            )
            val baseOffsetPx = itemWidthPx * selected.ordinal
            val indicatorOffset = if (dragging) {
                with(density) {
                    (baseOffsetPx + dragOffsetPx)
                        .coerceIn(0f, itemWidthPx * (AppSection.entries.size - 1))
                        .toDp()
                }
            } else {
                settledOffset
            }
            val liquidStretch = if (dragging && itemWidthPx > 0f) {
                1f + 0.09f * (abs(dragOffsetPx) / itemWidthPx).coerceIn(0f, 1f)
            } else {
                1f
            }
            Box(
                Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .graphicsLayer {
                        scaleX = liquidStretch
                        scaleY = 1f / liquidStretch.coerceAtLeast(1f)
                    }
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AppBlue.copy(alpha = if (dragging) 0.58f else 0.42f),
                                AppGreen.copy(alpha = if (dragging) 0.34f else 0.22f),
                                AccentOrange.copy(alpha = if (dragging) 0.48f else 0.32f),
                            ),
                        ),
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(25.dp)),
            )
            Row(
                Modifier
                    .fillMaxSize()
                    .pointerInput(selected, itemWidthPx) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragging = true
                                dragOffsetPx = 0f
                            },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                dragOffsetPx += amount
                            },
                            onDragCancel = {
                                dragging = false
                                dragOffsetPx = 0f
                            },
                            onDragEnd = {
                                val threshold = itemWidthPx * 0.22f
                                when {
                                    dragOffsetPx > threshold && selected.ordinal < AppSection.entries.lastIndex -> {
                                        onSelect(AppSection.entries[selected.ordinal + 1])
                                    }
                                    dragOffsetPx < -threshold && selected.ordinal > 0 -> {
                                        onSelect(AppSection.entries[selected.ordinal - 1])
                                    }
                                }
                                dragging = false
                                dragOffsetPx = 0f
                            },
                        )
                    },
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
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
                BottomBarItem(
                    label = "Аналитика",
                    icon = Icons.Rounded.BarChart,
                    selected = selected == AppSection.ANALYTICS,
                    onClick = { onSelect(AppSection.ANALYTICS) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val color = if (selected) AppGreen else Color.White.copy(alpha = 0.58f)
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tab-scale",
    )
    Column(
        modifier = modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick).padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        )
        Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HabitTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AppBlue,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF142D59),
            onPrimaryContainer = Color(0xFFD9E7FF),
            secondary = AccentOrange,
            tertiary = AppLavender,
            error = AccentOrange,
            onError = Color.White,
            errorContainer = Color(0xFF47261A),
            onErrorContainer = Color(0xFFFFD8C8),
            background = AppBackground,
            onBackground = Color(0xFFF5F4FA),
            surface = AppSurface,
            surfaceVariant = AppSurfaceElevated,
            surfaceContainerLowest = AppBackground,
            surfaceContainerLow = Color(0xFF0E141C),
            surfaceContainer = AppSurface,
            surfaceContainerHigh = AppSurfaceElevated,
            surfaceContainerHighest = Color(0xFF222D3A),
            surfaceBright = Color(0xFF263342),
            surfaceDim = Color(0xFF090D12),
            onSurface = Color(0xFFF5F4FA),
            onSurfaceVariant = Color(0xFFAAB4C3),
            outline = Color(0xFF3A4656),
            outlineVariant = Color(0xFF26303D),
        ),
        content = content,
    )
}

@Composable
internal fun AppScreenHeader(
    label: String,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Surface(
            shape = RoundedCornerShape(11.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
        ) {
            Box(
                Modifier.background(
                    Brush.horizontalGradient(listOf(accent.copy(alpha = 0.24f), Color.White.copy(alpha = 0.04f))),
                ),
            ) {
                Text(
                    label.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = accent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun AppSectionHeading(
    title: String,
    subtitle: String? = null,
    accent: Color = AppBlue,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(5.dp).height(24.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        subtitle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitTrackerScreen(
    state: HabitTrackerState,
    onSelectDay: (Long) -> Unit,
    onToggle: (String) -> Unit,
    onHabitClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onOpenAnalytics: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.padding(bottom = 72.dp),
                containerColor = AppGreen,
                contentColor = Color.White,
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
                item { ProgressSummary(state.stats, onOpenAnalytics) }
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
    AppScreenHeader("Привычки", "Мой ритм", "Маленькие шаги каждый день", AppGreen)
}

@Composable
private fun ProgressSummary(stats: HabitStats, onClick: () -> Unit) {
    val todayProgress = if (stats.total == 0) 0f else stats.completedToday.toFloat() / stats.total
    val weeklyProgress = if (stats.weeklyTarget == 0) 0f else {
        stats.weeklyCompletions.toFloat() / stats.weeklyTarget
    }
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFF174B94), Color(0xFF17345C), AccentOrange.copy(alpha = 0.72f))),
            ).padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Прогресс", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Аналитика  ›", color = Color.White.copy(alpha = 0.78f), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
                ProgressRing("Сегодня", todayProgress, Modifier.weight(1f))
                ProgressRing("Неделя", weeklyProgress, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProgressRing(label: String, progress: Float, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(66.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.20f),
                strokeWidth = 7.dp,
            )
            Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(7.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DayPicker(today: Long, selectedDay: Long, onSelectDay: (Long) -> Unit) {
    val weekdayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        (6 downTo 0).forEach { daysAgo ->
            val day = today - daysAgo
            val selected = day == selectedDay
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (selected) AppGreen
                        else MaterialTheme.colorScheme.surface,
                    )
                    .clickable { onSelectDay(day) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = weekdayLabels[isoDayOfWeek(day) - 1],
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = dayOfMonth(day).toString(),
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White
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
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f)),
    ) {
        Column(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(
                    listOf(color.copy(alpha = 0.10f), Color.Transparent, AppSurface.copy(alpha = 0.80f)),
                ),
            ).padding(16.dp),
        ) {
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
                        contentColor = if (completed) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
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
    HabitEditorDialog(
        dialogTitle = "Новая привычка",
        actionTitle = "Добавить",
        initialTitle = "",
        initialEmoji = HabitIcons.first().key,
        initialColorIndex = 0,
        initialScheduledDays = (1..7).toSet(),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun EditHabitDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Set<Int>) -> Unit,
) {
    HabitEditorDialog(
        dialogTitle = "Изменить привычку",
        actionTitle = "Сохранить",
        initialTitle = habit.title,
        initialEmoji = habit.emoji,
        initialColorIndex = habit.colorIndex.mod(HabitColors.size),
        initialScheduledDays = habit.schedule(),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun HabitEditorDialog(
    dialogTitle: String,
    actionTitle: String,
    initialTitle: String,
    initialEmoji: String,
    initialColorIndex: Int,
    initialScheduledDays: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Set<Int>) -> Unit,
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var emoji by remember(initialEmoji) { mutableStateOf(initialEmoji) }
    var colorIndex by remember(initialColorIndex) { mutableIntStateOf(initialColorIndex) }
    var scheduledDays by remember(initialScheduledDays) { mutableStateOf(initialScheduledDays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 590.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Название", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Например: вечерняя прогулка") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HabitColors.chunked(5).forEachIndexed { rowIndex, colors ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            colors.forEachIndexed { columnIndex, color ->
                                val index = rowIndex * 5 + columnIndex
                                val selected = colorIndex == index
                                Surface(
                                    modifier = Modifier.size(34.dp).clickable { colorIndex = index },
                                    shape = CircleShape,
                                    color = color,
                                    border = if (selected) BorderStroke(3.dp, Color.White) else null,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (selected) Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
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
                Text(actionTitle)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
internal fun WeekdaySelector(value: Set<Int>, onValueChange: (Set<Int>) -> Unit) {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val allDays = (1..7).toSet()
    val allSelected = value.containsAll(allDays)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        color = if (selected) Color.Transparent else Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(
                            1.dp,
                            if (selected) AppBlue.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.09f),
                        ),
                    ) {
                        Box(
                            modifier = if (selected) Modifier.background(
                                Brush.horizontalGradient(listOf(AppBlue.copy(alpha = 0.92f), AppGreen.copy(alpha = 0.86f))),
                            ) else Modifier,
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                label,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                repeat(4 - rowLabels.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().height(44.dp).clickable { onValueChange(allDays) },
            shape = RoundedCornerShape(15.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, if (allSelected) AppGreen.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.11f)),
        ) {
            Box(
                modifier = if (allSelected) Modifier.background(
                    Brush.horizontalGradient(listOf(AppGreen.copy(alpha = 0.78f), AppBlue.copy(alpha = 0.62f))),
                ) else Modifier.background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Все дни недели",
                    color = if (allSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        Text(
            "Привычка будет показана только в выбранные дни.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
