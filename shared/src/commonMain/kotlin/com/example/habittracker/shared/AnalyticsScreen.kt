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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class AnalyticsKind { HABITS, FINANCE }

@Composable
internal fun AnalyticsScreen(
    habitState: HabitTrackerState,
    financeState: FinanceState,
    kind: AnalyticsKind,
    onKindChange: (AnalyticsKind) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Аналитика", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text("Понятная картина привычек и денег", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(4.dp)) {
            AnalyticsKind.entries.forEach { item ->
                val active = item == selected
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(15.dp))
                        .background(if (active) Color(0xFFFFDD2D) else Color.Transparent)
                        .clickable { onSelect(item) }.padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (item == AnalyticsKind.HABITS) "Привычки" else "Финансы",
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color(0xFF1A1A18) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.habitAnalyticsItems(state: HabitTrackerState) {
    item {
        val percent = if (state.stats.weeklyTarget == 0) 0 else state.stats.weeklyCompletions * 100 / state.stats.weeklyTarget
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF192700))) {
            Column(Modifier.fillMaxWidth().padding(22.dp)) {
                Text("Эта неделя", color = Color.White.copy(alpha = 0.68f))
                Text("$percent%", color = Color(0xFFA8FF00), fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "${state.stats.weeklyCompletions} из ${state.stats.weeklyTarget} запланированных выполнений",
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
        }
    }
    item {
        Text("Последние 7 дней", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        HabitWeekChart(state)
    }
    item { Text("По привычкам", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
    state.habits.forEach { habit ->
        item(key = "analytics-${habit.id}") {
            val color = HabitColors[habit.colorIndex.mod(HabitColors.size)]
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(habit.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text(
                            "${habit.completedInWeek(state.today)}/${habit.schedule().size}",
                            color = color,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Spacer(Modifier.height(9.dp))
                    LinearProgressIndicator(
                        progress = { habit.weeklyProgress(state.today) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = color,
                        trackColor = color.copy(alpha = 0.16f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitWeekChart(state: HabitTrackerState) {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val weekStart = startOfWeek(state.today)
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            labels.forEachIndexed { index, label ->
                val day = weekStart + index
                val scheduled = state.habits.filter { it.isScheduledOn(day) }
                val ratio = if (scheduled.isEmpty()) 0f else scheduled.count { it.isCompleted(day) }.toFloat() / scheduled.size
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(ratio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(5.dp))
                    Box(
                        Modifier.width(24.dp).height((112.dp * ratio.coerceAtLeast(0.04f))).clip(RoundedCornerShape(8.dp))
                            .background(if (ratio >= 1f) Color(0xFFA8FF00) else MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.financeAnalyticsItems(state: FinanceState) {
    val current = calendarDate(currentEpochDay())
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
        convertMinor(transaction.amountMinor, transaction.currency, state.data.displayCurrency, state.data.rates)
            ?.let { transaction.category to it }
    }.groupBy({ it.first }, { it.second }).map { (name, amounts) -> name to amounts.sum() }.sortedByDescending { it.second }

    item {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF20201E))) {
            Column(Modifier.fillMaxWidth().padding(22.dp)) {
                Text("Расходы · ${monthName(current.month)}", color = Color.White.copy(alpha = 0.68f))
                Text(formatMoney(expenses, state.data.displayCurrency), color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("Доходы", color = Color.White.copy(alpha = 0.6f))
                        Text(formatMoney(income, state.data.displayCurrency), color = Color(0xFF70D6B4), fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Баланс", color = Color.White.copy(alpha = 0.6f))
                        Text(formatMoney(income - expenses, state.data.displayCurrency), color = Color(0xFFFFDD2D), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    item {
        Text("Куда ушли деньги", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (categories.isEmpty()) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                Text("Добавьте расходы — здесь появится разбивка по категориям.", modifier = Modifier.fillMaxWidth().padding(22.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            FinanceDonut(categories, expenses, state.data)
        }
    }
    categories.forEach { (category, amount) ->
        item(key = "category-$category") {
            val color = FinanceCategoryPalette[state.data.categoryColorIndex(category)]
            val percent = if (expenses == 0L) 0 else amount * 100 / expenses
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(13.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(category, fontWeight = FontWeight.Bold)
                        Text("$percent% расходов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(formatMoney(amount, state.data.displayCurrency), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun FinanceDonut(categories: List<Pair<String, Long>>, total: Long, data: FinanceData) {
    Box(Modifier.fillMaxWidth().height(238.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(205.dp)) {
            var startAngle = -90f
            categories.forEach { (category, amount) ->
                val sweep = amount.toFloat() / total.coerceAtLeast(1) * 360f
                drawArc(
                    color = FinanceCategoryPalette[data.categoryColorIndex(category)],
                    startAngle = startAngle,
                    sweepAngle = (sweep - 2f).coerceAtLeast(0.8f),
                    useCenter = false,
                    style = Stroke(width = 34.dp.toPx()),
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Расходы", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                formatMoney(total, data.displayCurrency),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
