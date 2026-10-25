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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    onAdd: (Long, MoneyCurrency, TransactionType, String) -> Unit,
    onDelete: (String) -> Unit,
    onUpdate: (String, Long, MoneyCurrency, String) -> Unit,
    onToggleSettled: (String) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onCategoryColorChange: (String, Int) -> Unit,
    onAddFriend: (String) -> Unit,
    onDeleteFriend: (String) -> Unit,
    onFriendColorChange: (String, Int) -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }
    var presetCategory by remember { mutableStateOf("") }
    var showAddCategory by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    var showAddFriend by remember { mutableStateOf(false) }
    var showManageFriends by remember { mutableStateOf(false) }
    var presetType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showBudget by remember { mutableStateOf(false) }
    var showRates by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<MoneyTransaction?>(null) }
    var editingDebt by remember { mutableStateOf<MoneyTransaction?>(null) }
    var analyticsType by remember { mutableStateOf<TransactionType?>(null) }
    val currency = state.data.displayCurrency
    val expense = state.totalMinor(TransactionType.EXPENSE)
    val income = state.totalMinor(TransactionType.INCOME)
    val budget = state.budgetMinor()
    val spendable = budget + income
    val balance = spendable - expense

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    presetCategory = ""
                    presetType = TransactionType.EXPENSE
                    showAdd = true
                },
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
                        val progress = if (spendable <= 0) 0f else (expense.toFloat() / spendable).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                            color = if (progress >= 1f) Color(0xFFFF6B6B) else Color(0xFFFFDD2D),
                            trackColor = Color.White.copy(alpha = 0.16f),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (spendable > 0) "Потрачено ${formatMoney(expense, currency)} из ${formatMoney(spendable, currency)}"
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
                    FinanceMetric("Доходы", state.totalMinor(TransactionType.INCOME), currency, transactionTypeColor(TransactionType.INCOME), Modifier.weight(1f)) {
                        analyticsType = TransactionType.INCOME
                    }
                    FinanceMetric("Расходы", expense, currency, transactionTypeColor(TransactionType.EXPENSE), Modifier.weight(1f)) {
                        analyticsType = TransactionType.EXPENSE
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FinanceMetric("Мне должны", state.totalMinor(TransactionType.OWED_TO_ME), currency, transactionTypeColor(TransactionType.OWED_TO_ME), Modifier.weight(1f)) {
                        analyticsType = TransactionType.OWED_TO_ME
                    }
                    FinanceMetric("Я должен", state.totalMinor(TransactionType.I_OWE), currency, transactionTypeColor(TransactionType.I_OWE), Modifier.weight(1f)) {
                        analyticsType = TransactionType.I_OWE
                    }
                }
            }
            item {
                CategorySection(
                    data = state.data,
                    onSelect = {
                        presetCategory = it
                        presetType = TransactionType.EXPENSE
                        showAdd = true
                    },
                    onAdd = { showAddCategory = true },
                    onManage = { showManageCategories = true },
                )
            }
            item {
                FriendSection(
                    data = state.data,
                    onSelect = {
                        presetCategory = it
                        presetType = TransactionType.OWED_TO_ME
                        showAdd = true
                    },
                    onAdd = { showAddFriend = true },
                    onManage = { showManageFriends = true },
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
                        FinanceCategoryPalette[
                            if (transaction.type == TransactionType.OWED_TO_ME || transaction.type == TransactionType.I_OWE) {
                                state.data.friendColorIndex(transaction.category)
                            } else {
                                state.data.categoryColorIndex(transaction.category)
                            }
                        ],
                    ) { selectedTransaction = transaction }
                }
            }
        }
    }

    if (showAdd) AddTransactionDialog(
        data = state.data,
        initialCategory = presetCategory,
        initialType = presetType,
        onDismiss = { showAdd = false; presetCategory = ""; presetType = TransactionType.EXPENSE },
    ) { amount, itemCurrency, type, target ->
        onAdd(amount, itemCurrency, type, target)
        showAdd = false
        presetCategory = ""
        presetType = TransactionType.EXPENSE
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
    if (showAddFriend) NameDialog(
        title = "Новый друг",
        label = "Имя",
        onDismiss = { showAddFriend = false },
        onConfirm = { onAddFriend(it); showAddFriend = false },
    )
    if (showManageFriends) FriendManagerDialog(
        data = state.data,
        onDismiss = { showManageFriends = false },
        onDelete = onDeleteFriend,
        onColorChange = onFriendColorChange,
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
            title = { Text(transaction.category) },
            text = {
                Text(
                    "${transaction.type.title}\n${formatMoney(transaction.amountMinor, transaction.currency)}" +
                        if (transaction.settled) "\nДолг погашен" else "",
                )
            },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (transaction.type == TransactionType.OWED_TO_ME || transaction.type == TransactionType.I_OWE) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                editingDebt = transaction
                                selectedTransaction = null
                            }) { Icon(Icons.Rounded.Edit, null); Spacer(Modifier.size(4.dp)); Text("Изменить") }
                            Button(
                                onClick = { onToggleSettled(transaction.id); selectedTransaction = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (transaction.settled) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF52D273),
                                    contentColor = if (transaction.settled) MaterialTheme.colorScheme.onSurface else Color(0xFF102515),
                                ),
                            ) { Text(if (transaction.settled) "Вернуть" else "Погашен") }
                        }
                    }
                    Button(
                        onClick = { onDelete(transaction.id); selectedTransaction = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) { Icon(Icons.Rounded.Delete, null); Spacer(Modifier.size(6.dp)); Text("Удалить") }
                }
            },
            dismissButton = { TextButton(onClick = { selectedTransaction = null }) { Text("Готово") } },
        )
    }
    editingDebt?.let { transaction ->
        EditDebtDialog(
            transaction = transaction,
            data = state.data,
            onDismiss = { editingDebt = null },
            onConfirm = { amount, itemCurrency, target ->
                onUpdate(transaction.id, amount, itemCurrency, target)
                editingDebt = null
            },
        )
    }
    analyticsType?.let { type ->
        TransactionAnalyticsDialog(
            type = type,
            data = state.data,
            onDismiss = { analyticsType = null },
            onOpen = {
                analyticsType = null
                selectedTransaction = it
            },
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
private fun FriendSection(
    data: FinanceData,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    onManage: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Друзья", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onManage) { Text("Настроить") }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            data.friends.forEach { friend ->
                val color = FinanceCategoryPalette[data.friendColorIndex(friend)]
                Surface(
                    modifier = Modifier.clickable { onSelect(friend) },
                    shape = RoundedCornerShape(16.dp),
                    color = color.copy(alpha = 0.20f),
                ) {
                    Text(
                        friend,
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
            ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, "Добавить друга") } }
        }
        Text(
            "Выберите человека, чтобы сразу записать долг.",
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
private fun FinanceMetric(
    title: String,
    amount: Long,
    currency: MoneyCurrency,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.14f)) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatMoney(amount, currency), fontWeight = FontWeight.ExtraBold, color = color)
            Text("Подробнее  ›", style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.78f))
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
    val typeColor = if (item.settled) MaterialTheme.colorScheme.onSurfaceVariant else transactionTypeColor(item.type)
    val icon: ImageVector = when (item.type) {
        TransactionType.INCOME -> Icons.Rounded.ArrowDownward
        TransactionType.EXPENSE -> Icons.Rounded.ArrowUpward
        else -> Icons.Rounded.Handshake
    }
    Card(onClick = onClick, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = typeColor.copy(alpha = 0.18f), modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = typeColor) }
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.category, fontWeight = FontWeight.Bold, color = categoryColor)
                Text(
                    if (item.settled) "${item.type.title} · погашен" else item.type.title,
                    color = typeColor,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatMoney(item.amountMinor, item.currency), fontWeight = FontWeight.ExtraBold, color = typeColor)
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
    data: FinanceData,
    initialCategory: String,
    initialType: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (Long, MoneyCurrency, TransactionType, String) -> Unit,
) {
    val categories = data.categories
    val friends = data.friends
    var amount by remember { mutableStateOf("") }
    var target by remember(initialCategory) { mutableStateOf(initialCategory) }
    var currency by remember { mutableStateOf(MoneyCurrency.USD) }
    var type by remember(initialType) { mutableStateOf(initialType) }
    val amountMinor = parseAmountMinor(amount)
    val needsTarget = type != TransactionType.INCOME
    val targets = when (type) {
        TransactionType.INCOME -> emptyList()
        TransactionType.EXPENSE -> categories
        TransactionType.OWED_TO_ME, TransactionType.I_OWE -> friends
    }
    val selectedTypeColor = transactionTypeColor(type)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая операция", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма", color = Color(0xFFFFDD2D), fontWeight = FontWeight.SemiBold) },
                    suffix = { Text(currency.symbol, color = selectedTypeColor, fontWeight = FontWeight.ExtraBold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedTypeColor,
                        focusedContainerColor = selectedTypeColor.copy(alpha = 0.08f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                CurrencySelector(currency) { currency = it }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { item ->
                        val itemColor = transactionTypeColor(item)
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = if (type == item) itemColor.copy(alpha = 0.24f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.border(
                                if (type == item) 1.dp else 0.dp,
                                if (type == item) itemColor.copy(alpha = 0.8f) else Color.Transparent,
                                RoundedCornerShape(13.dp),
                            ).clickable {
                                type = item
                                target = when (item) {
                                    TransactionType.INCOME -> item.title
                                    TransactionType.EXPENSE -> categories.firstOrNull().orEmpty()
                                    TransactionType.OWED_TO_ME, TransactionType.I_OWE -> friends.firstOrNull().orEmpty()
                                }
                            },
                        ) {
                            Text(
                                item.title,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                fontWeight = FontWeight.Bold,
                                color = if (type == item) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (needsTarget) {
                    Text(
                        when (type) {
                            TransactionType.OWED_TO_ME -> "Кто"
                            TransactionType.I_OWE -> "Кому"
                            else -> "Категория"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        targets.forEach { item ->
                        val itemColor = FinanceCategoryPalette[
                            if (type == TransactionType.OWED_TO_ME || type == TransactionType.I_OWE) {
                                data.friendColorIndex(item)
                            } else {
                                data.categoryColorIndex(item)
                            }
                        ]
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = itemColor.copy(alpha = if (target == item) 0.24f else 0.10f),
                            modifier = Modifier
                                .border(
                                    if (target == item) 1.dp else 0.dp,
                                    if (target == item) itemColor else Color.Transparent,
                                    RoundedCornerShape(13.dp),
                                )
                                .clickable { target = item },
                        ) {
                            Text(
                                item,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                color = if (target == item) itemColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (target == item) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                        }
                    }
                }
                if (needsTarget && targets.isEmpty()) {
                    Text(
                        if (type == TransactionType.OWED_TO_ME || type == TransactionType.I_OWE) "Сначала добавьте друга на экране финансов."
                        else "Сначала добавьте категорию на экране финансов.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amountMinor, currency, type, if (type == TransactionType.INCOME) type.title else target) },
                enabled = (!needsTarget || target.isNotBlank()) && amountMinor > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8FF00), contentColor = Color(0xFF173600)),
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun EditDebtDialog(
    transaction: MoneyTransaction,
    data: FinanceData,
    onDismiss: () -> Unit,
    onConfirm: (Long, MoneyCurrency, String) -> Unit,
) {
    var amount by remember(transaction.id) {
        mutableStateOf("${transaction.amountMinor / 100}.${(transaction.amountMinor % 100).toString().padStart(2, '0')}")
    }
    var currency by remember(transaction.id) { mutableStateOf(transaction.currency) }
    var friend by remember(transaction.id) { mutableStateOf(transaction.category) }
    val amountMinor = parseAmountMinor(amount)
    val accent = transactionTypeColor(transaction.type)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить долг", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма", color = Color(0xFFFFDD2D), fontWeight = FontWeight.SemiBold) },
                    suffix = { Text(currency.symbol, color = accent, fontWeight = FontWeight.ExtraBold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent),
                    modifier = Modifier.fillMaxWidth(),
                )
                CurrencySelector(currency) { currency = it }
                Text(
                    if (transaction.type == TransactionType.I_OWE) "Кому" else "Кто",
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    data.friends.forEach { item ->
                        val color = FinanceCategoryPalette[data.friendColorIndex(item)]
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = color.copy(alpha = if (friend == item) 0.24f else 0.10f),
                            modifier = Modifier.border(
                                if (friend == item) 1.dp else 0.dp,
                                if (friend == item) color else Color.Transparent,
                                RoundedCornerShape(13.dp),
                            ).clickable { friend = item },
                        ) {
                            Text(
                                item,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                color = if (friend == item) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (friend == item) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amountMinor, currency, friend) },
                enabled = amountMinor > 0 && friend.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8FF00), contentColor = Color(0xFF173600)),
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun TransactionAnalyticsDialog(
    type: TransactionType,
    data: FinanceData,
    onDismiss: () -> Unit,
    onOpen: (MoneyTransaction) -> Unit,
) {
    val transactions = data.transactions.filter { it.type == type }
    val active = transactions.filterNot { it.settled }
    val total = active.mapNotNull {
        convertMinor(it.amountMinor, it.currency, data.displayCurrency, data.rates)
    }.sum()
    val color = transactionTypeColor(type)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(type.title, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.14f)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(if (type == TransactionType.OWED_TO_ME || type == TransactionType.I_OWE) "Активная сумма" else "Всего", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatMoney(total, data.displayCurrency), color = color, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${transactions.size} операций", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (transactions.isEmpty()) {
                    Text("Операций пока нет.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 18.dp))
                }
                transactions.forEach { transaction ->
                    val itemColor = if (type == TransactionType.OWED_TO_ME || type == TransactionType.I_OWE) {
                        FinanceCategoryPalette[data.friendColorIndex(transaction.category)]
                    } else {
                        FinanceCategoryPalette[data.categoryColorIndex(transaction.category)]
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onOpen(transaction) },
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
                    ) {
                        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(itemColor))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(transaction.category, fontWeight = FontWeight.Bold)
                                if (transaction.settled) Text("Погашен", color = Color(0xFF52D273), style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                convertMinor(transaction.amountMinor, transaction.currency, data.displayCurrency, data.rates)
                                    ?.let { formatMoney(it, data.displayCurrency) }
                                    ?: formatMoney(transaction.amountMinor, transaction.currency),
                                color = if (transaction.settled) MaterialTheme.colorScheme.onSurfaceVariant else color,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } },
    )
}

@Composable
private fun CategoryNameDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    NameDialog("Новая категория", "Название категории", onDismiss, onConfirm)
}

@Composable
private fun NameDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(value) },
                enabled = value.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8FF00), contentColor = Color(0xFF173600)),
            ) { Text("Добавить") }
        },
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

@Composable
private fun FriendManagerDialog(
    data: FinanceData,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onColorChange: (String, Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Друзья") },
        text = {
            Column(Modifier.heightIn(max = 430.dp).verticalScroll(rememberScrollState())) {
                data.friends.forEach { friend ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(friend, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { onDelete(friend) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Удалить $friend", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FinanceCategoryPalette.forEachIndexed { index, color ->
                                val selected = data.friendColorIndex(friend) == index
                                Box(
                                    Modifier.size(if (selected) 29.dp else 25.dp).clip(CircleShape).background(color)
                                        .clickable { onColorChange(friend, index) }
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

private fun transactionTypeColor(type: TransactionType): Color = when (type) {
    TransactionType.EXPENSE -> Color(0xFFFF5E6C)
    TransactionType.INCOME -> Color(0xFF52D273)
    TransactionType.OWED_TO_ME -> Color(0xFFA889FF)
    TransactionType.I_OWE -> Color(0xFFFFA95A)
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
