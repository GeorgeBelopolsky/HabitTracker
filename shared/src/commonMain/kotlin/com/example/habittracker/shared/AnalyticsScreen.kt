package com.example.habittracker.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class AnalyticsKind { HABITS, FINANCE }

private val AnalyticsOrange = AccentOrange
private val AnalyticsBlue = Color(0xFF2F7DFF)
private val AnalyticsIncome = Color(0xFF52D273)
private val AnalyticsExpense = Color(0xFFFF5E6C)
private val AnalyticsCard = AppSurface
private val AnalyticsCardElevated = AppSurfaceElevated

private data class AnalyticsInsight(val title: String, val body: String)

@Composable
internal fun AnalyticsScreen(
    habitState: HabitTrackerState,
    financeState: FinanceState,
    kind: AnalyticsKind,
    onKindChange: (AnalyticsKind) -> Unit,
    onHabitClick: (String) -> Unit,
) {
    val listState = remember(kind) { LazyListState() }
    var habitPeriodDays by remember { mutableIntStateOf(30) }
    var financeMonthOffset by remember { mutableIntStateOf(0) }
    var insight by remember { mutableStateOf<AnalyticsInsight?>(null) }
    LaunchedEffect(kind) { listState.scrollToItem(0) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AppScreenHeader("Аналитика", "Картина прогресса", "Привычки и деньги в одном месте", AccentOrange)
        }
        item { AnalyticsSelector(kind, onKindChange) }
        when (kind) {
            AnalyticsKind.HABITS -> habitAnalyticsItems(
                habitState,
                habitPeriodDays,
                { habitPeriodDays = it },
                onHabitClick,
                { title, body -> insight = AnalyticsInsight(title, body) },
            )
            AnalyticsKind.FINANCE -> financeAnalyticsItems(
                financeState,
                financeMonthOffset,
                { financeMonthOffset = it },
                { title, body -> insight = AnalyticsInsight(title, body) },
            )
        }
    }
    insight?.let { selected ->
        AlertDialog(
            onDismissRequest = { insight = null },
            title = { Text(selected.title, fontWeight = FontWeight.ExtraBold) },
            text = { Text(selected.body, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = { insight = null }) { Text("Понятно") } },
        )
    }
}

@Composable
private fun AnalyticsSelector(selected: AnalyticsKind, onSelect: (AnalyticsKind) -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = AnalyticsCardElevated) {
        Row(Modifier.fillMaxWidth().padding(4.dp)) {
            AnalyticsKind.entries.forEach { item ->
                val active = item == selected
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                        .background(if (active) Color(0xFFA8FF00) else Color.Transparent)
                        .clickable { onSelect(item) }.padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (item == AnalyticsKind.HABITS) "Привычки" else "Финансы",
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color(0xFF161614) else Color.White.copy(alpha = 0.68f),
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.habitAnalyticsItems(
    state: HabitTrackerState,
    periodDays: Int,
    onPeriodChange: (Int) -> Unit,
    onHabitClick: (String) -> Unit,
    onInsight: (String, String) -> Unit,
) {
    val percent = if (state.stats.weeklyTarget == 0) 0 else state.stats.weeklyCompletions * 100 / state.stats.weeklyTarget
    val successful = state.habits.count { it.weeklyProgress(state.today) >= 1f }
    val active = state.habits.count { it.isScheduledOn(state.today) }

    val weekStart = startOfWeek(state.today)
    val weekdayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val weekDetails = weekdayLabels.mapIndexed { index, label ->
        val day = weekStart + index
        val planned = state.habits.count { it.isScheduledOn(day) }
        val completed = state.habits.count { it.isScheduledOn(day) && it.isCompleted(day) }
        "$label: $completed из $planned"
    }.joinToString("\n")

    item {
        HabitOverviewCard(percent, state.stats.weeklyCompletions, state.stats.weeklyTarget, successful, active) {
            onInsight("Итог недели", "Выполнено ${state.stats.weeklyCompletions} из ${state.stats.weeklyTarget}.\nУспешных привычек: $successful.\nСегодня запланировано: $active.")
        }
    }
    item {
        SectionTitle("Ритм недели", "по запланированным дням")
        Spacer(Modifier.height(10.dp))
        HabitWeekChart(state) { onInsight("Ритм недели", weekDetails) }
    }
    item {
        SectionTitle("Стабильность", "динамика выполнений")
        Spacer(Modifier.height(10.dp))
        HabitPeriodSelector(periodDays, onPeriodChange)
        Spacer(Modifier.height(10.dp))
        HabitPeriodChart(state, periodDays) {
            onInsight("Стабильность за $periodDays дней", "График показывает долю выполненных привычек среди всех запланированных. Переключайте период, чтобы сравнить короткий и месячный ритм.")
        }
    }
    item { SectionTitle("Ваши привычки", "${state.habits.size} активных") }
    if (state.habits.isEmpty()) {
        item { AnalyticsEmptyState("Добавьте привычку — здесь появится её прогресс и серии.") }
    } else {
        state.habits.forEach { habit ->
            item(key = "analytics-${habit.id}") { HabitAnalyticsRow(habit, state.today) { onHabitClick(habit.id) } }
        }
    }
}

@Composable
private fun HabitOverviewCard(percent: Int, completed: Int, target: Int, successful: Int, activeToday: Int, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(28.dp), color = AnalyticsCard) {
        Box(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFF142B4E), Color(0xFF141A24), AccentOrange.copy(alpha = 0.22f))),
            ).padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ActivityRing(percent / 100f, "$percent%", "неделя")
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Недельная цель", color = Color.White.copy(alpha = 0.62f))
                        Text(
                            when {
                                percent >= 100 -> "Отличный результат"
                                percent >= 70 -> "Почти у цели"
                                percent > 0 -> "Продолжайте ритм"
                                else -> "Начните сегодня"
                            },
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text("$completed из $target выполнений", color = Color.White.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompactMetric("Сегодня", "$activeToday", "запланировано", AnalyticsBlue, Modifier.weight(1f))
                    CompactMetric("Закрыто", "$successful", "за неделю", AnalyticsOrange, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ActivityRing(progress: Float, value: String, caption: String) {
    Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawArc(Color.White.copy(alpha = 0.10f), -90f, 360f, false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
            drawArc(AnalyticsBlue, -90f, 360f * progress.coerceIn(0f, 1f), false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(caption, fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun CompactMetric(label: String, value: String, note: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.06f)) {
        Column(Modifier.padding(13.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.58f))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(note, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.48f))
        }
    }
}

@Composable
private fun HabitWeekChart(state: HabitTrackerState, onClick: () -> Unit) {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val weekStart = startOfWeek(state.today)
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
        Row(
            Modifier.fillMaxWidth().height(205.dp).padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            labels.forEachIndexed { index, label ->
                val day = weekStart + index
                val scheduled = state.habits.filter { it.isScheduledOn(day) }
                val done = scheduled.count { it.isCompleted(day) }
                val ratio = if (scheduled.isEmpty()) 0f else done.toFloat() / scheduled.size
                val isToday = day == state.today
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (scheduled.isEmpty()) "—" else "$done/${scheduled.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) AnalyticsBlue else Color.White.copy(alpha = 0.55f),
                    )
                    Spacer(Modifier.height(7.dp))
                    Box(
                        Modifier.width(24.dp).height(96.dp).clip(RoundedCornerShape(9.dp)).background(Color.White.copy(alpha = 0.07f)),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        if (ratio > 0f) Box(
                            Modifier.fillMaxWidth().height((96.dp * ratio).coerceAtLeast(8.dp))
                                .background(if (isToday) AnalyticsOrange else AnalyticsBlue),
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    Surface(shape = CircleShape, color = if (isToday) AnalyticsBlue else Color.Transparent) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) Color(0xFF151513) else Color.White.copy(alpha = 0.62f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitPeriodSelector(selectedDays: Int, onSelect: (Int) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = AnalyticsCardElevated) {
        Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(7 to "7 дней", 30 to "30 дней").forEach { (days, label) ->
                val selected = days == selectedDays
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (selected) AnalyticsBlue else Color.Transparent)
                        .clickable { onSelect(days) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.58f),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitPeriodChart(state: HabitTrackerState, periodDays: Int, onClick: () -> Unit) {
    val groupCount = if (periodDays == 7) 7 else 5
    val groupSize = if (periodDays == 7) 1 else 6
    val firstDay = state.today - periodDays + 1
    val values = List(groupCount) { groupIndex ->
        val days = List(groupSize) { offset -> firstDay + groupIndex * groupSize + offset }
        val scheduled = days.sumOf { day -> state.habits.count { it.isScheduledOn(day) } }
        val completed = days.sumOf { day -> state.habits.count { it.isScheduledOn(day) && it.isCompleted(day) } }
        if (scheduled == 0) 0f else completed.toFloat() / scheduled
    }
    val weekLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (periodDays == 7) "Последняя неделя" else "Последние 30 дней", fontWeight = FontWeight.Bold)
                    Text("Доля выполненных запланированных привычек", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                }
                Text(
                    "${(values.average() * 100).toInt()}%",
                    color = AnalyticsBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Row(
                Modifier.fillMaxWidth().height(142.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                values.forEachIndexed { index, value ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(value * 100).toInt()}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f))
                        Spacer(Modifier.height(5.dp))
                        Box(
                            Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(9.dp))
                                .background(Color.White.copy(alpha = 0.07f)),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            if (value > 0f) Box(
                                Modifier.fillMaxWidth().height((92.dp * value).coerceAtLeast(8.dp))
                                    .background(if (index == values.lastIndex) AnalyticsOrange else AnalyticsBlue),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (periodDays == 7) weekLabels[isoDayOfWeek(firstDay + index) - 1] else "${index + 1}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.58f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitAnalyticsRow(habit: Habit, today: Long, onClick: () -> Unit) {
    val color = HabitColors[habit.colorIndex.mod(HabitColors.size)]
    val completed = habit.completedInWeek(today)
    val target = habit.schedule().size
    val progress = habit.weeklyProgress(today)
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(22.dp), color = AnalyticsCard) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.16f)) {
                    Box(Modifier.size(43.dp), contentAlignment = Alignment.Center) {
                        Icon(habitIcon(habit.emoji), null, tint = color, modifier = Modifier.size(23.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(habit.title, fontWeight = FontWeight.Bold)
                    Text(
                        if (progress >= 1f) "Цель недели выполнена" else "Осталось ${(target - completed).coerceAtLeast(0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("${(progress * 100).toInt()}%", color = color, fontWeight = FontWeight.ExtraBold)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.13f),
            )
            Row {
                Text("$completed из $target за неделю", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.52f))
                Spacer(Modifier.weight(1f))
                Text("Настроить  ›", style = MaterialTheme.typography.labelSmall, color = AnalyticsBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.financeAnalyticsItems(
    state: FinanceState,
    selectedMonthOffset: Int,
    onMonthSelect: (Int) -> Unit,
    onInsight: (String, String) -> Unit,
) {
    val currentDay = currentEpochDay()
    val current = calendarDate(currentDay)
    val selectedMonthStart = shiftMonth(startOfMonth(currentDay), -selectedMonthOffset)
    val selectedDate = calendarDate(selectedMonthStart)
    val transactions = state.data.transactions.filter {
        val date = calendarDate(it.epochDay)
        date.year == selectedDate.year && date.month == selectedDate.month
    }
    fun total(type: TransactionType): Long = transactions.filter { it.type == type }.mapNotNull {
        convertMinor(it.amountMinor, it.currency, state.data.displayCurrency, state.data.rates)
    }.sum()
    val expenses = total(TransactionType.EXPENSE)
    val income = total(TransactionType.INCOME)
    val categories = transactions.asSequence().filter { it.type == TransactionType.EXPENSE }.mapNotNull { transaction ->
        convertMinor(transaction.amountMinor, transaction.currency, state.data.displayCurrency, state.data.rates)?.let { transaction.category to it }
    }.groupBy({ it.first }, { it.second }).map { (name, amounts) -> name to amounts.sum() }.sortedByDescending { it.second }
    val selectedMonthEnd = if (selectedMonthOffset == 0) currentDay else {
        selectedMonthStart + daysInMonth(selectedDate.year, selectedDate.month) - 1
    }
    val dailyExpenses = (6 downTo 0).map { offset ->
        val day = selectedMonthEnd - offset
        day to transactions.filter { it.type == TransactionType.EXPENSE && it.epochDay == day }.mapNotNull {
            convertMinor(it.amountMinor, it.currency, state.data.displayCurrency, state.data.rates)
        }.sum()
    }
    val monthly = (0..5).map { offset ->
        val absoluteMonth = current.year * 12 + (current.month - 1) - offset
        val year = absoluteMonth / 12
        val month = absoluteMonth.mod(12) + 1
        val monthTransactions = state.data.transactions.filter {
            val date = calendarDate(it.epochDay)
            date.year == year && date.month == month
        }
        fun monthTotal(type: TransactionType): Long = monthTransactions.filter { it.type == type }.mapNotNull {
            convertMinor(it.amountMinor, it.currency, state.data.displayCurrency, state.data.rates)
        }.sum()
        MonthlyFinanceSummary(year, month, monthTotal(TransactionType.INCOME), monthTotal(TransactionType.EXPENSE))
    }

    item { FinanceMonthSelector(currentDay, selectedMonthOffset, onMonthSelect) }
    val result = income - expenses
    val categoryDetails = if (categories.isEmpty()) "Расходов по категориям пока нет." else categories.joinToString("\n") { (name, amount) ->
        "$name: ${formatMoney(amount, state.data.displayCurrency)}"
    }
    val monthlyDetails = monthly.joinToString("\n") { item ->
        val monthResult = item.income - item.expense
        "${monthName(item.month)}: ${(if (monthResult >= 0) "+" else "−")}${formatMoney(kotlin.math.abs(monthResult), state.data.displayCurrency)}"
    }

    item {
        FinanceOverviewCard(expenses, income, state.data.displayCurrency, selectedDate.month, transactions.size) {
            onInsight(
                "Сводка за ${monthName(selectedDate.month)}",
                "Поступило: ${formatMoney(income, state.data.displayCurrency)}\nПотрачено: ${formatMoney(expenses, state.data.displayCurrency)}\nИтог: ${(if (result >= 0) "+" else "−")}${formatMoney(kotlin.math.abs(result), state.data.displayCurrency)}",
            )
        }
    }
    item {
        SectionTitle("Динамика расходов", "7 последних дней месяца")
        Spacer(Modifier.height(10.dp))
        FinanceWeekChart(dailyExpenses, state.data.displayCurrency) {
            onInsight("Расходы по дням", dailyExpenses.joinToString("\n") { (day, amount) -> "${dayOfMonth(day)}: ${formatMoney(amount, state.data.displayCurrency)}" })
        }
    }
    item {
        SectionTitle("Динамика сбережений", "по месяцам")
        Spacer(Modifier.height(10.dp))
        MonthlyBalanceCard(monthly, state.data.displayCurrency) { onInsight("Динамика сбережений", monthlyDetails) }
    }
    item { SectionTitle("Категории", if (categories.isEmpty()) "пока без расходов" else "${categories.size} категорий") }
    item {
        if (categories.isEmpty()) AnalyticsEmptyState("Добавьте расход во вкладке «Финансы» — здесь появятся диаграмма и категории.")
        else FinanceCategoryBreakdown(categories, expenses, state.data) { onInsight("Расходы по категориям", categoryDetails) }
    }
}

@Composable
private fun FinanceMonthSelector(currentDay: Long, selectedOffset: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        (0..5).forEach { offset ->
            val date = calendarDate(shiftMonth(startOfMonth(currentDay), -offset))
            val selected = offset == selectedOffset
            Surface(
                modifier = Modifier.clickable { onSelect(offset) },
                shape = RoundedCornerShape(15.dp),
                color = if (selected) AnalyticsBlue else AnalyticsCardElevated,
            ) {
                Text(
                    if (offset == 0) "Этот месяц" else monthName(date.month),
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                    color = if (selected) Color.White else Color.White.copy(alpha = 0.62f),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun FinanceOverviewCard(
    expenses: Long,
    income: Long,
    currency: MoneyCurrency,
    month: Int,
    operationCount: Int,
    onClick: () -> Unit,
) {
    val result = income - expenses
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(28.dp), color = AnalyticsCard) {
        Column(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFF14233A), Color(0xFF141A22), AccentOrange.copy(alpha = 0.20f))),
            ).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Сводка · ${monthName(month)}", color = Color.White.copy(alpha = 0.58f), fontWeight = FontWeight.SemiBold)
                    Text(
                        when {
                            result > 0 -> "Сбережения выросли"
                            result < 0 -> "Расходы выше доходов"
                            else -> "Доходы равны расходам"
                        },
                        color = when { result > 0 -> AnalyticsIncome; result < 0 -> AnalyticsExpense; else -> AnalyticsBlue },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Surface(shape = CircleShape, color = AnalyticsBlue) {
                    Text("$operationCount", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FinanceMetric("Поступило", formatMoney(income, currency), AnalyticsIncome, Modifier.weight(1f))
                FinanceMetric("Потрачено", formatMoney(expenses, currency), AnalyticsExpense, Modifier.weight(1f))
            }
            Surface(shape = RoundedCornerShape(18.dp), color = when {
                result > 0 -> AnalyticsIncome.copy(alpha = 0.12f)
                result < 0 -> AnalyticsExpense.copy(alpha = 0.12f)
                else -> AnalyticsBlue.copy(alpha = 0.12f)
            }) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Итог месяца", color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.labelMedium)
                        Text(
                            if (result < 0) "Нужно скорректировать расходы" else if (result > 0) "Доходы покрыли расходы" else "Без изменений",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        if (result >= 0) "+${formatMoney(result, currency)}" else "−${formatMoney(kotlin.math.abs(result), currency)}",
                        color = when { result > 0 -> AnalyticsIncome; result < 0 -> AnalyticsExpense; else -> AnalyticsBlue },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

private data class MonthlyFinanceSummary(
    val year: Int,
    val month: Int,
    val income: Long,
    val expense: Long,
)

@Composable
private fun MonthlyBalanceCard(items: List<MonthlyFinanceSummary>, currency: MoneyCurrency, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { item ->
                val result = item.income - item.expense
                val positive = result > 0
                val negative = result < 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${monthName(item.month)} ${item.year}", fontWeight = FontWeight.Bold)
                        Text(
                            "Доходы ${formatMoney(item.income, currency)} · расходы ${formatMoney(item.expense, currency)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            when {
                                positive -> "Сбережения выросли"
                                negative -> "Сбережения снизились"
                                else -> "Без изменений"
                            },
                            color = when { positive -> AnalyticsIncome; negative -> AnalyticsExpense; else -> AnalyticsBlue },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            (when { positive -> "+"; negative -> "−"; else -> "" }) + formatMoney(kotlin.math.abs(result), currency),
                            color = when { positive -> AnalyticsIncome; negative -> AnalyticsExpense; else -> AnalyticsBlue },
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(17.dp), color = Color.White.copy(alpha = 0.06f)) {
        Column(Modifier.padding(13.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.54f), fontSize = 12.sp)
            Text(value, color = accent, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}

@Composable
private fun FinanceWeekChart(values: List<Pair<Long, Long>>, currency: MoneyCurrency, onClick: () -> Unit) {
    val max = values.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L
    val total = values.sumOf { it.second }
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("За 7 дней", color = Color.White.copy(alpha = 0.52f), fontSize = 12.sp)
                    Text(formatMoney(total, currency), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(
                    if (total == 0L) "Нет расходов" else "В среднем ${formatMoney(total / 7, currency)}",
                    color = Color.White.copy(alpha = 0.52f),
                    fontSize = 11.sp,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().height(118.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                values.forEachIndexed { index, (day, amount) ->
                    val ratio = amount.toFloat() / max
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.fillMaxWidth().height(88.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.06f)),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            if (amount > 0L) Box(
                                Modifier.fillMaxWidth().height((88.dp * ratio).coerceAtLeast(7.dp))
                                    .background(if (index == values.lastIndex) AnalyticsOrange else AnalyticsBlue),
                            )
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(dayOfMonth(day).toString(), fontSize = 11.sp, color = if (index == values.lastIndex) AnalyticsOrange else Color.White.copy(alpha = 0.55f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceCategoryBreakdown(categories: List<Pair<String, Long>>, total: Long, data: FinanceData, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth().height(178.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(154.dp)) {
                    var startAngle = -90f
                    categories.forEach { (category, amount) ->
                        val sweep = amount.toFloat() / total.coerceAtLeast(1) * 360f
                        drawArc(
                            color = FinanceCategoryPalette[data.categoryColorIndex(category)],
                            startAngle = startAngle,
                            sweepAngle = (sweep - 3f).coerceAtLeast(1f),
                            useCenter = false,
                            style = Stroke(width = 25.dp.toPx(), cap = StrokeCap.Round),
                        )
                        startAngle += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Всего", color = Color.White.copy(alpha = 0.52f), fontSize = 12.sp)
                    Text(formatMoney(total, data.displayCurrency), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
            categories.take(5).forEach { (category, amount) ->
                val color = FinanceCategoryPalette[data.categoryColorIndex(category)]
                val percent = if (total == 0L) 0 else amount * 100 / total
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(10.dp))
                    Text(category, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text("$percent%", color = Color.White.copy(alpha = 0.52f), fontSize = 12.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(formatMoney(amount, data.displayCurrency), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    val accent = if (title.contains("Стабиль") || title.contains("сбереж") || title.contains("Категор")) AccentOrange else AppBlue
    AppSectionHeading(title, subtitle, accent)
}

@Composable
private fun AnalyticsEmptyState(message: String) {
    Surface(shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.Bottom) {
                listOf(24.dp, 40.dp, 58.dp, 34.dp).forEachIndexed { index, height ->
                    Box(
                        Modifier.width(18.dp).height(height).clip(RoundedCornerShape(6.dp))
                            .background(if (index == 2) AnalyticsOrange else AnalyticsBlue.copy(alpha = 0.7f)),
                    )
                }
            }
            Spacer(Modifier.height(17.dp))
            Text(message, color = Color.White.copy(alpha = 0.62f), textAlign = TextAlign.Center)
        }
    }
}
