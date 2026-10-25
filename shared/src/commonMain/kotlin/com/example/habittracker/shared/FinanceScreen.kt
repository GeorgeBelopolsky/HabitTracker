package com.example.habittracker.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun FinanceScreen(
    state: FinanceState,
    onCurrencyChange: (MoneyCurrency) -> Unit,
    onBudgetChange: (MoneyCurrency, Long) -> Unit,
    onRatesChange: (Double?, Double?) -> Unit,
    onRefreshRates: () -> Unit,
    onAdd: (String, Long, MoneyCurrency, TransactionType, String) -> Unit,
    onDelete: (String) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onCategoryColorChange: (String, Int) -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }
    var presetCategory by remember { mutableStateOf("") }
    var showAddCategory by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    var showBudget by remember { mutableStateOf(false) }
    var showRates by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<MoneyTransaction?>(null) }
    val currency = state.data.displayCurrency
    val expense = state.totalMinor(TransactionType.EXPENSE)
    val income = state.totalMinor(TransactionType.INCOME)
    val budget = state.budgetMinor()
    val balance = income - expense

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                modifier = Modifier.padding(bottom = 72.dp),
                containerColor = Color(0xFFA8FF00),
                contentColor = Color(0xFF173600),
            ) { Icon(Icons.Rounded.Add, contentDescription = "Добавить операцию") }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Финансы", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                        Text("Бюджет, операции и долги", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { showRates = true }) {
                        Icon(Icons.Rounded.CurrencyExchange, null)
                        Spacer(Modifier.size(5.dp))
                        Text("Курсы")
                    }
                }
            }
            item { CurrencySelector(currency, onCurrencyChange) }
            item { ExchangeRateSummary(state.data.rates, onEdit = { showRates = true }) }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF20201E)),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Доступно", color = Color.White.copy(alpha = 0.7f))
                        Text(formatMoney(balance, currency), color = Color(0xFFFFDD2D), fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                        val progress = if (budget <= 0) 0f else (expense.toFloat() / budget).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                            color = if (progress >= 1f) Color(0xFFFF6B6B) else Color(0xFFFFDD2D),
                            trackColor = Color.White.copy(alpha = 0.16f),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (budget > 0) "Потрачено ${formatMoney(expense, currency)} из ${formatMoney(budget, currency)}"
                                else "Бюджет ещё не задан",
                                color = Color.White.copy(alpha = 0.78f),
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { showBudget = true }) {
                                Icon(Icons.Rounded.Edit, null, tint = Color(0xFFFFDD2D))
                                Text("Изменить", color = Color(0xFFFFDD2D))
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FinanceMetric("Доходы", state.totalMinor(TransactionType.INCOME), currency, Color(0xFF2F7DFF), Modifier.weight(1f))
                    FinanceMetric("Расходы", expense, currency, Color(0xFFFF8B8B), Modifier.weight(1f))
                }
            }
            item {
                CategorySection(
                    data = state.data,
                    onSelect = {
                        presetCategory = it
                        showAdd = true
                    },
                    onAdd = { showAddCategory = true },
                    onManage = { showManageCategories = true },
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("История", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${state.data.transactions.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.data.transactions.isEmpty()) {
                item {
                    Text(
                        "Добавьте первую покупку, доход или долг — всё хранится локально на устройстве.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 28.dp),
                    )
                }
            } else {
                items(state.data.transactions, key = { it.id }) { transaction ->
                    TransactionCard(
                        transaction,
                        currency,
                        state.data.rates,
                        FinanceCategoryPalette[state.data.categoryColorIndex(transaction.category)],
                    ) { selectedTransaction = transaction }
                }
            }
        }
    }

    if (showAdd) AddTransactionDialog(
        categories = state.data.categories,
        initialCategory = presetCategory,
        onDismiss = { showAdd = false; presetCategory = "" },
    ) { title, amount, itemCurrency, type, category ->
        onAdd(title, amount, itemCurrency, type, category)
        showAdd = false
        presetCategory = ""
    }
    if (showAddCategory) CategoryNameDialog(
        onDismiss = { showAddCategory = false },
        onConfirm = { onAddCategory(it); showAddCategory = false },
    )
    if (showManageCategories) CategoryManagerDialog(
        data = state.data,
        onDismiss = { showManageCategories = false },
        onDelete = onDeleteCategory,
        onColorChange = onCategoryColorChange,
    )
    if (showBudget) AmountDialog("Бюджет в ${currency.code}", currency, state.budgetMinor(), { showBudget = false }) {
        onBudgetChange(currency, it); showBudget = false
    }
    if (showRates) RatesDialog(
        rates = state.data.rates,
        onDismiss = { showRates = false },
        onAutomatic = { onRefreshRates(); showRates = false },
        onManual = { rub, ars -> onRatesChange(rub, ars); showRates = false },
    )
    selectedTransaction?.let { transaction ->
        AlertDialog(
            onDismissRequest = { selectedTransaction = null },
            title = { Text(transaction.title) },
            text = { Text("${transaction.type.title} · ${transaction.category}\n${formatMoney(transaction.amountMinor, transaction.currency)}") },
            confirmButton = {
                Button(
                    onClick = { onDelete(transaction.id); selectedTransaction = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Icon(Icons.Rounded.Delete, null); Spacer(Modifier.size(6.dp)); Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { selectedTransaction = null }) { Text("Готово") } },
        )
    }
}

@Composable
private fun CategorySection(
    data: FinanceData,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    onManage: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Категории", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onManage) { Text("Настроить") }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            data.categories.forEach { category ->
                val color = FinanceCategoryPalette[data.categoryColorIndex(category)]
                Surface(
                    modifier = Modifier.clickable { onSelect(category) },
                    shape = RoundedCornerShape(16.dp),
                    color = color.copy(alpha = 0.20f),
                ) {
                    Text(
                        category,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = color,
                    )
                }
            }
            Surface(
                modifier = Modifier.size(44.dp).clickable(onClick = onAdd),
                shape = CircleShape,
                color = Color(0xFFA8FF00),
            ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, "Добавить категорию") } }
        }
        Text(
            "Нажмите категорию, чтобы быстрее добавить операцию.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExchangeRateSummary(rates: ExchangeRates, onEdit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CurrencyExchange, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(8.dp))
                Text("Текущий курс", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(rates.sourceLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                rates.rubPerUsd?.let { "1 USD = ${formatRate(it)} RUB · покупка USDT" }
                    ?: "1 USD = — RUB · покупка USDT",
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                rates.arsPerUsd?.let { "1 USD = ${formatRate(it)} ARS · продажа USDT" }
                    ?: "1 USD = — ARS · продажа USDT",
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (rates.automatic) "Автоматическое обновление включено. Нажмите для настройки."
                else "Используется ручной курс. Нажмите для настройки.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            rates.updatedAtLabel?.let {
                Text("Обновлено: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CurrencySelector(value: MoneyCurrency, onChange: (MoneyCurrency) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MoneyCurrency.entries.forEach { currency ->
            val selected = value == currency
            Surface(
                modifier = Modifier.weight(1f).height(42.dp).clickable { onChange(currency) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) Color(0xFFA8FF00) else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(currency.code, fontWeight = FontWeight.Bold, color = if (selected) Color(0xFF173600) else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun FinanceMetric(title: String, amount: Long, currency: MoneyCurrency, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.14f)) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatMoney(amount, currency), fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun TransactionCard(
    item: MoneyTransaction,
    display: MoneyCurrency,
    rates: ExchangeRates,
    categoryColor: Color,
    onClick: () -> Unit,
) {
    val color = categoryColor
    val icon: ImageVector = when (item.type) {
        TransactionType.INCOME -> Icons.Rounded.ArrowDownward
        TransactionType.EXPENSE -> Icons.Rounded.ArrowUpward
        else -> Icons.Rounded.Handshake
    }
    Card(onClick = onClick, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.18f), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold)
                Text("${item.type.title} · ${item.category}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatMoney(item.amountMinor, item.currency), fontWeight = FontWeight.ExtraBold, color = color)
                if (item.currency != display) {
                    Text(
                        convertMinor(item.amountMinor, item.currency, display, rates)?.let { "≈ ${formatMoney(it, display)}" } ?: "курс не задан",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTransactionDialog(
    categories: List<String>,
    initialCategory: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, MoneyCurrency, TransactionType, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember(initialCategory) { mutableStateOf(initialCategory) }
    var currency by remember { mutableStateOf(MoneyCurrency.USD) }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    val amountMinor = parseAmountMinor(amount)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая операция", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(title, { title = it }, label = { Text("Название") }, singleLine = true)
                TextField(amount, { amount = it }, label = { Text("Сумма") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                CurrencySelector(currency) { currency = it }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = if (type == item) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { type = item },
                        ) { Text(item.title, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), fontWeight = FontWeight.SemiBold) }
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    categories.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = if (category == item) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { category = item },
                        ) { Text(item, modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) }
                    }
                }
                TextField(category, { category = it }, label = { Text("Категория или имя друга") }, singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { onConfirm(title, amountMinor, currency, type, category) }, enabled = title.isNotBlank() && amountMinor > 0) { Text("Добавить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun CategoryNameDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая категория") },
        text = { TextField(value, { value = it }, label = { Text("Название") }, singleLine = true) },
        confirmButton = { Button(onClick = { onConfirm(value) }, enabled = value.isNotBlank()) { Text("Добавить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun CategoryManagerDialog(
    data: FinanceData,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onColorChange: (String, Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Категории") },
        text = {
            Column(Modifier.heightIn(max = 430.dp).verticalScroll(rememberScrollState())) {
                data.categories.forEach { category ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(category, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { onDelete(category) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Удалить $category", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FinanceCategoryPalette.forEachIndexed { index, color ->
                                val selected = data.categoryColorIndex(category) == index
                                Box(
                                    Modifier
                                        .size(if (selected) 29.dp else 25.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { onColorChange(category, index) }
                                        .then(
                                            if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } },
    )
}

internal val FinanceCategoryPalette = listOf(
    Color(0xFFFFDD2D),
    Color(0xFF64D8CB),
    Color(0xFF7C9BFF),
    Color(0xFFFF8FA3),
    Color(0xFFA889FF),
    Color(0xFFFFA95A),
    Color(0xFF4D8DFF),
    Color(0xFFB6C1D1),
)

@Composable
private fun AmountDialog(title: String, currency: MoneyCurrency, current: Long, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var value by remember { mutableStateOf(if (current > 0) "${current / 100}.${(current % 100).toString().padStart(2, '0')}" else "") }
    val parsed = parseAmountMinor(value)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TextField(value, { value = it }, suffix = { Text(currency.symbol) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true) },
        confirmButton = { Button(onClick = { onConfirm(parsed) }, enabled = parsed >= 0) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun RatesDialog(
    rates: ExchangeRates,
    onDismiss: () -> Unit,
    onAutomatic: () -> Unit,
    onManual: (Double?, Double?) -> Unit,
) {
    var rub by remember { mutableStateOf(rates.rubPerUsd?.toString().orEmpty()) }
    var ars by remember { mutableStateOf(rates.arsPerUsd?.toString().orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Курсы валют") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Автоматически: для ARS используется продажа USDT на Bybit P2P, для RUB — покупка USDT.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onAutomatic, modifier = Modifier.fillMaxWidth()) {
                    Text(if (rates.automatic) "Обновить автоматический курс" else "Включить автоматический курс")
                }
                Text("Или задайте собственные значения:", fontWeight = FontWeight.SemiBold)
                TextField(rub, { rub = it }, label = { Text("Покупка: 1 USD = … RUB") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                TextField(ars, { ars = it }, label = { Text("Продажа: 1 USD = … ARS") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { onManual(parsePositiveDouble(rub), parsePositiveDouble(ars)) }) { Text("Сохранить вручную") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private fun parsePositiveDouble(value: String): Double? = value.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

private fun formatRate(value: Double): String {
    val rounded = kotlin.math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
}

private fun parseAmountMinor(value: String): Long {
    val normalized = value.trim().replace(',', '.')
    val parts = normalized.split('.', limit = 2)
    val major = parts.getOrNull(0)?.toLongOrNull() ?: return 0
    val cents = parts.getOrNull(1)?.take(2)?.padEnd(2, '0')?.toLongOrNull() ?: 0
    return major * 100 + cents
}
