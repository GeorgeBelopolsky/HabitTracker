package com.example.habittracker.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

private val AnalyticsYellow = Color(0xFFFFDD2D)
private val AnalyticsBlue = Color(0xFF2F7DFF)
private val AnalyticsIncome = Color(0xFF52D273)
private val AnalyticsExpense = Color(0xFFFF5E6C)
private val AnalyticsCard = Color(0xFF1B1B1F)
private val AnalyticsCardElevated = Color(0xFF242429)

@Composable
internal fun AnalyticsScreen(
    habitState: HabitTrackerState,
    financeState: FinanceState,
    kind: AnalyticsKind,
    onKindChange: (AnalyticsKind) -> Unit,
) {
    val listState = remember(kind) { LazyListState() }
    LaunchedEffect(kind) { listState.scrollToItem(0) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Аналитика", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            Text("Главное за неделю и месяц", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { AnalyticsSelector(kind, onKindChange) }
        when (kind) {
            AnalyticsKind.HABITS -> habitAnalyticsItems(habitState)
            AnalyticsKind.FINANCE -> financeAnalyticsItems(financeState)
        }
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

private fun androidx.compose.foundation.lazy.LazyListScope.habitAnalyticsItems(state: HabitTrackerState) {
    val percent = if (state.stats.weeklyTarget == 0) 0 else state.stats.weeklyCompletions * 100 / state.stats.weeklyTarget
    val successful = state.habits.count { it.weeklyProgress(state.today) >= 1f }
    val active = state.habits.count { it.isScheduledOn(state.today) }

    item { HabitOverviewCard(percent, state.stats.weeklyCompletions, state.stats.weeklyTarget, successful, active) }
    item {
        SectionTitle("Ритм недели", "по запланированным дням")
        Spacer(Modifier.height(10.dp))
        HabitWeekChart(state)
    }
    item { SectionTitle("Ваши привычки", "${state.habits.size} активных") }
    if (state.habits.isEmpty()) {
        item { AnalyticsEmptyState("Добавьте привычку — здесь появится её прогресс и серии.") }
    } else {
        state.habits.forEach { habit ->
            item(key = "analytics-${habit.id}") { HabitAnalyticsRow(habit, state.today) }
        }
    }
}

@Composable
private fun HabitOverviewCard(percent: Int, completed: Int, target: Int, successful: Int, activeToday: Int) {
    Surface(shape = RoundedCornerShape(28.dp), color = AnalyticsCard) {
        Box(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFF20201E), Color(0xFF171C27))),
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
                    CompactMetric("Закрыто", "$successful", "за неделю", AnalyticsYellow, Modifier.weight(1f))
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
            drawArc(AnalyticsYellow, -90f, 360f * progress.coerceIn(0f, 1f), false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
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
private fun HabitWeekChart(state: HabitTrackerState) {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val weekStart = startOfWeek(state.today)
    Surface(shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
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
                        color = if (isToday) AnalyticsYellow else Color.White.copy(alpha = 0.55f),
                    )
                    Spacer(Modifier.height(7.dp))
                    Box(
                        Modifier.width(24.dp).height(96.dp).clip(RoundedCornerShape(9.dp)).background(Color.White.copy(alpha = 0.07f)),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        if (ratio > 0f) Box(
                            Modifier.fillMaxWidth().height((96.dp * ratio).coerceAtLeast(8.dp))
                                .background(if (isToday) AnalyticsYellow else AnalyticsBlue),
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    Surface(shape = CircleShape, color = if (isToday) AnalyticsYellow else Color.Transparent) {
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
private fun HabitAnalyticsRow(habit: Habit, today: Long) {
    val color = HabitColors[habit.colorIndex.mod(HabitColors.size)]
    val completed = habit.completedInWeek(today)
    val target = habit.schedule().size
    val progress = habit.weeklyProgress(today)
    Surface(shape = RoundedCornerShape(22.dp), color = AnalyticsCard) {
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
                Text(habit.streakText(today), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.52f))
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.financeAnalyticsItems(state: FinanceState) {
    val currentDay = currentEpochDay()
    val current = calendarDate(currentDay)
    val transactions = state.data.transactions.filter {
        val date = calendarDate(it.epochDay)
        date.year == current.year && date.month == current.month
    }
    fun total(type: TransactionType): Long = transactions.filter { it.type == type }.mapNotNull {
        convertMinor(it.amountMinor, it.currency, state.data.displayCurrency, state.data.rates)
    }.sum()
    val expenses = total(TransactionType.EXPENSE)
    val income = total(TransactionType.INCOME)
    val categories = transactions.asSequence().filter { it.type == TransactionType.EXPENSE }.mapNotNull { transaction ->
        convertMinor(transaction.amountMinor, transaction.currency, state.data.displayCurrency, state.data.rates)?.let { transaction.category to it }
    }.groupBy({ it.first }, { it.second }).map { (name, amounts) -> name to amounts.sum() }.sortedByDescending { it.second }
    val dailyExpenses = (6 downTo 0).map { offset ->
        val day = currentDay - offset
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

    item { FinanceOverviewCard(expenses, income, state.data.displayCurrency, current.month, transactions.size) }
    item {
        SectionTitle("Динамика расходов", "последние 7 дней")
        Spacer(Modifier.height(10.dp))
        FinanceWeekChart(dailyExpenses, state.data.displayCurrency)
    }
    item {
        SectionTitle("Динамика сбережений", "по месяцам")
        Spacer(Modifier.height(10.dp))
        MonthlyBalanceCard(monthly, state.data.displayCurrency)
    }
    item { SectionTitle("Категории", if (categories.isEmpty()) "пока без расходов" else "${categories.size} категорий") }
    item {
        if (categories.isEmpty()) AnalyticsEmptyState("Добавьте расход во вкладке «Финансы» — здесь появятся диаграмма и категории.")
        else FinanceCategoryBreakdown(categories, expenses, state.data)
    }
}

@Composable
private fun FinanceOverviewCard(expenses: Long, income: Long, currency: MoneyCurrency, month: Int, operationCount: Int) {
    val result = income - expenses
    Surface(shape = RoundedCornerShape(28.dp), color = AnalyticsCard) {
        Column(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFF20201E), Color(0xFF171C27))),
            ).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Расходы · ${monthName(month)}", color = Color.White.copy(alpha = 0.58f), fontWeight = FontWeight.SemiBold)
                    Text(formatMoney(expenses, currency), color = AnalyticsYellow, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                }
                Surface(shape = CircleShape, color = AnalyticsYellow) {
                    Text("$operationCount", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Color(0xFF151513), fontWeight = FontWeight.Bold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FinanceMetric("Доходы", formatMoney(income, currency), AnalyticsIncome, Modifier.weight(1f))
                FinanceMetric(
                    when { result > 0 -> "Профицит"; result < 0 -> "Дефицит"; else -> "Баланс" },
                    formatMoney(kotlin.math.abs(result), currency),
                    when { result > 0 -> AnalyticsIncome; result < 0 -> AnalyticsExpense; else -> AnalyticsYellow },
                    Modifier.weight(1f),
                )
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
private fun MonthlyBalanceCard(items: List<MonthlyFinanceSummary>, currency: MoneyCurrency) {
    Surface(shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
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
                            color = when { positive -> AnalyticsIncome; negative -> AnalyticsExpense; else -> AnalyticsYellow },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            (when { positive -> "+"; negative -> "−"; else -> "" }) + formatMoney(kotlin.math.abs(result), currency),
                            color = when { positive -> AnalyticsIncome; negative -> AnalyticsExpense; else -> AnalyticsYellow },
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
private fun FinanceWeekChart(values: List<Pair<Long, Long>>, currency: MoneyCurrency) {
    val max = values.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L
    val total = values.sumOf { it.second }
    Surface(shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
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
                                    .background(if (index == values.lastIndex) AnalyticsYellow else AnalyticsBlue),
                            )
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(dayOfMonth(day).toString(), fontSize = 11.sp, color = if (index == values.lastIndex) AnalyticsYellow else Color.White.copy(alpha = 0.55f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceCategoryBreakdown(categories: List<Pair<String, Long>>, total: Long, data: FinanceData) {
    Surface(shape = RoundedCornerShape(24.dp), color = AnalyticsCard) {
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
    Row(verticalAlignment = Alignment.Bottom) {
        Text(title, modifier = Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
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
                            .background(if (index == 2) AnalyticsYellow else AnalyticsBlue.copy(alpha = 0.7f)),
                    )
                }
            }
            Spacer(Modifier.height(17.dp))
            Text(message, color = Color.White.copy(alpha = 0.62f), textAlign = TextAlign.Center)
        }
    }
}
