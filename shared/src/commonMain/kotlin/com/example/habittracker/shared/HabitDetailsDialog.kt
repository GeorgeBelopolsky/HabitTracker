package com.example.habittracker.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
internal fun HabitDetailsDialog(
    habit: Habit,
    today: Long,
    onDismiss: () -> Unit,
    onToggleDay: (Long) -> Unit,
    onWeeklyTargetChange: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var monthStart by remember(habit.id) { mutableStateOf(startOfMonth(today)) }
    val color = HabitColors[habit.colorIndex.mod(HabitColors.size)]
    val completedThisWeek = habit.completedInWeek(today)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 760.dp),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp))
                            .background(color.copy(alpha = 0.24f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = habitIcon(habit.emoji),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Spacer(Modifier.size(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(habit.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            habit.streakText(today),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onDismiss) { Text("Готово") }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = color.copy(alpha = 0.13f),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Прогресс недели", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(
                                "$completedThisWeek/${habit.weeklyTargetDays}",
                                color = color,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { habit.weeklyProgress(today) },
                            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                            color = color,
                            trackColor = color.copy(alpha = 0.2f),
                        )
                    }
                }

                WeeklyTargetSelector(
                    value = habit.weeklyTargetDays,
                    onValueChange = onWeeklyTargetChange,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                MonthCalendar(
                    habit = habit,
                    today = today,
                    monthStart = monthStart,
                    color = color,
                    onPreviousMonth = { monthStart = shiftMonth(monthStart, -1) },
                    onNextMonth = { monthStart = shiftMonth(monthStart, 1) },
                    onToggleDay = onToggleDay,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Button(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Удалить привычку")
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Удалить «${habit.title}»?") },
            text = { Text("История выполнения этой привычки будет удалена с устройства.") },
            confirmButton = {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun MonthCalendar(
    habit: Habit,
    today: Long,
    monthStart: Long,
    color: Color,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleDay: (Long) -> Unit,
) {
    val date = calendarDate(monthStart)
    val totalDays = daysInMonth(date.year, date.month)
    val leadingCells = isoDayOfWeek(monthStart) - 1
    val cells = List(leadingCells) { null } + List(totalDays) { monthStart + it }
    val rows = cells.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onPreviousMonth) { Text("‹", fontSize = 28.sp) }
            Text(
                "${monthName(date.month)} ${date.year}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onNextMonth, enabled = monthStart < startOfMonth(today)) {
                Text("›", fontSize = 28.sp)
            }
        }

        Row {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { weekday ->
                Text(
                    weekday,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        rows.forEach { row ->
            Row {
                (row + List(7 - row.size) { null }).forEach { day ->
                    CalendarDay(
                        day = day,
                        habit = habit,
                        today = today,
                        color = color,
                        onToggle = onToggleDay,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot(color, "Выполнено")
            LegendDot(MaterialTheme.colorScheme.errorContainer, "Пропуск")
        }
        Text(
            "Нажмите на прошедший день, чтобы изменить отметку.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CalendarDay(
    day: Long?,
    habit: Habit,
    today: Long,
    color: Color,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (day == null) {
        Spacer(modifier.aspectRatio(1f))
        return
    }
    val completed = habit.isCompleted(day)
    val active = day in habit.createdAtEpochDay..today
    val background = when {
        completed -> color
        active && day < today -> MaterialTheme.colorScheme.errorContainer
        day == today -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val contentColor = when {
        completed -> Color(0xFF173C32)
        active && day < today -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier.aspectRatio(1f).padding(3.dp).clip(CircleShape).background(background)
            .clickable(enabled = active) { onToggle(day) },
        contentAlignment = Alignment.Center,
    ) {
        Text(dayOfMonth(day).toString(), color = contentColor, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.size(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun startOfMonth(epochDay: Long): Long {
    val date = calendarDate(epochDay)
    return epochDayOf(date.year, date.month, 1)
}

private fun shiftMonth(monthStart: Long, delta: Int): Long {
    val date = calendarDate(monthStart)
    val zeroBased = date.year * 12 + date.month - 1 + delta
    val year = zeroBased / 12
    val month = zeroBased % 12 + 1
    return epochDayOf(year, month, 1)
}
