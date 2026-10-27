package com.example.habittracker.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
internal fun FinanceScreen(
    state: FinanceState,
    onCurrencyChange: (MoneyCurrency) -> Unit,
    onBudgetChange: (MoneyCurrency, Long) -> Unit,
    onRatesChange: (Double?, Double?) -> Unit,
    onRefreshRates: () -> Unit,
    onAdd: (Long, MoneyCurrency, TransactionType, String, String) -> Unit,
    onDelete: (String) -> Unit,
    onUpdate: (String, Long, MoneyCurrency, String, String) -> Unit,
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
    var editingTransaction by remember { mutableStateOf<MoneyTransaction?>(null) }
    var analyticsType by remember { mutableStateOf<TransactionType?>(null) }
    val currency = state.data.displayCurrency
    val expense = state.totalMinor(TransactionType.EXPENSE)
    val income = state.totalMinor(TransactionType.INCOME)
    val budget = state.budgetMinor()
    val balance = budget + income - expense
    val currentDate = calendarDate(currentEpochDay())
    fun currentMonthTotal(type: TransactionType): Long = state.data.transactions.filter { transaction ->
        val date = calendarDate(transaction.epochDay)
        transaction.type == type && date.year == currentDate.year && date.month == currentDate.month
    }.mapNotNull { transaction ->
        convertMinor(transaction.amountMinor, transaction.currency, currency, state.data.rates)
    }.sum()
    val monthChange = currentMonthTotal(TransactionType.INCOME) - currentMonthTotal(TransactionType.EXPENSE)

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
                containerColor = AppGreen,
                contentColor = Color.White,
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
                    AppScreenHeader("Финансы", "Мои деньги", "Остаток, операции и долги", AppBlue, Modifier.weight(1f))
                    TextButton(onClick = { showRates = true }) {
                        Icon(Icons.Rounded.CurrencyExchange, null, tint = AccentOrange)
                        Spacer(Modifier.size(5.dp))
                        Text("Курсы", color = AccentOrange)
                    }
                }
            }
            item { CurrencySelector(currency, onCurrencyChange) }
            item { ExchangeRateSummary(state.data.rates, onEdit = { showRates = true }) }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppSurface),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, AppBlue.copy(alpha = 0.20f)),
                ) {
                    Column(
                        Modifier.fillMaxWidth().background(
                            Brush.linearGradient(listOf(Color(0xFF14233A), Color(0xFF171A21), AccentOrange.copy(alpha = 0.18f))),
                        ).padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Общий остаток", color = AccentOrange, fontWeight = FontWeight.SemiBold)
                        Text(formatMoney(balance, currency), color = Color(0xFF2F7DFF), fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                        Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.06f)) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Исходный остаток", color = Color.White.copy(alpha = 0.56f), style = MaterialTheme.typography.labelMedium)
                                    Text(formatMoney(budget, currency), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("За ${monthName(currentDate.month).lowercase()}", color = Color.White.copy(alpha = 0.56f), style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        (if (monthChange > 0) "+" else "") + formatMoney(monthChange, currency),
                                        color = when { monthChange > 0 -> AppGreen; monthChange < 0 -> AccentOrange; else -> Color.White },
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                TextButton(onClick = { showBudget = true }) {
                                    Icon(Icons.Rounded.Edit, null, tint = Color(0xFF2F7DFF))
                            }
                        }
                    }
                        Text(
                            when {
                                monthChange > 0 -> "Сбережения в этом месяце увеличились."
                                monthChange < 0 -> "Расходы выше доходов — стоит проверить категории трат."
                                else -> "В этом месяце сбережения пока не изменились."
                            },
                            color = Color.White.copy(alpha = 0.68f),
                            style = MaterialTheme.typography.bodySmall,
                        )
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
                    AppSectionHeading("История", "${state.data.transactions.size}", AccentOrange)
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
    ) { amount, itemCurrency, type, target, comment ->
        onAdd(amount, itemCurrency, type, target, comment)
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
    if (showBudget) AmountDialog("Исходный остаток", currency, state.budgetMinor(), { showBudget = false }) {
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
                        (if (transaction.comment.isNotBlank()) "\n${transaction.comment}" else "") +
                        (if (transaction.settled) "\nДолг погашен" else ""),
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = {
                            editingTransaction = transaction
                            selectedTransaction = null
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F7DFF), contentColor = Color.White),
                    ) {
                        Icon(Icons.Rounded.Edit, null)
                        Spacer(Modifier.size(6.dp))
                        Text("Изменить", fontWeight = FontWeight.Bold)
                    }
                    if (transaction.type == TransactionType.OWED_TO_ME || transaction.type == TransactionType.I_OWE) {
                        Button(
                            onClick = { onToggleSettled(transaction.id); selectedTransaction = null },
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (transaction.settled) MaterialTheme.colorScheme.surfaceVariant else AppGreen,
                                contentColor = Color.White,
                            ),
                        ) { Text(if (transaction.settled) "Вернуть" else "Погасить") }
                    }
                    Surface(
                        onClick = { onDelete(transaction.id); selectedTransaction = null },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            },
            dismissButton = {},
        )
    }
    editingTransaction?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            data = state.data,
            onDismiss = { editingTransaction = null },
            onConfirm = { amount, itemCurrency, target, comment ->
                onUpdate(transaction.id, amount, itemCurrency, target, comment)
                editingTransaction = null
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
            AppSectionHeading("Категории", accent = AppBlue, modifier = Modifier.weight(1f))
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
                color = AppGreen,
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
            AppSectionHeading("Друзья", accent = AccentOrange, modifier = Modifier.weight(1f))
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
                color = AppGreen,
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
        color = AppSurface,
        border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.20f)),
    ) {
        Column(
            Modifier.background(
                Brush.horizontalGradient(
                    listOf(AppBlue.copy(alpha = 0.14f), Color.Transparent, AccentOrange.copy(alpha = 0.16f)),
                ),
            ).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CurrencyExchange, null, tint = AccentOrange)
                Spacer(Modifier.size(8.dp))
                Text("Текущий курс", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(rates.sourceLabel, style = MaterialTheme.typography.labelSmall, color = AccentOrange)
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
                color = if (selected) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(
                    modifier = if (selected) Modifier.background(
                        Brush.horizontalGradient(listOf(AppBlue, AccentOrange)),
                    ) else Modifier,
                    contentAlignment = Alignment.Center,
                ) {
                    Text(currency.code, fontWeight = FontWeight.Bold, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun FinanceAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    currency: MoneyCurrency,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Сумма",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("0.00", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)) },
            suffix = { Text(currency.symbol, color = AccentOrange, fontWeight = FontWeight.ExtraBold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = AppBlue.copy(alpha = 0.07f),
                unfocusedContainerColor = AppSurfaceElevated,
                cursorColor = AppGreen,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
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
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f)),
    ) {
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
                if (item.comment.isNotBlank()) {
                    Text(
                        item.comment,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                    )
                }
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
    onConfirm: (Long, MoneyCurrency, TransactionType, String, String) -> Unit,
) {
    val categories = data.categories
    val friends = data.friends
    var amount by remember { mutableStateOf("") }
    var target by remember(initialCategory) { mutableStateOf(initialCategory) }
    var currency by remember { mutableStateOf(MoneyCurrency.USD) }
    var type by remember(initialType) { mutableStateOf(initialType) }
    var comment by remember { mutableStateOf("") }
    val amountMinor = parseAmountMinor(amount)
    val needsTarget = type != TransactionType.INCOME
    val targets = when (type) {
        TransactionType.INCOME -> emptyList()
        TransactionType.EXPENSE -> categories
        TransactionType.OWED_TO_ME, TransactionType.I_OWE -> friends
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая операция", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FinanceAmountField(amount, { amount = it }, currency)
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
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий") },
                    placeholder = { Text("Например: счёт за воду") },
                    supportingText = { Text("Необязательно") },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2F7DFF)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amountMinor, currency, type, if (type == TransactionType.INCOME) type.title else target, comment) },
                enabled = (!needsTarget || target.isNotBlank()) && amountMinor > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AppGreen, contentColor = Color.White),
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun EditTransactionDialog(
    transaction: MoneyTransaction,
    data: FinanceData,
    onDismiss: () -> Unit,
    onConfirm: (Long, MoneyCurrency, String, String) -> Unit,
) {
    var amount by remember(transaction.id) {
        mutableStateOf("${transaction.amountMinor / 100}.${(transaction.amountMinor % 100).toString().padStart(2, '0')}")
    }
    var currency by remember(transaction.id) { mutableStateOf(transaction.currency) }
    var target by remember(transaction.id) { mutableStateOf(transaction.category) }
    var comment by remember(transaction.id) { mutableStateOf(transaction.comment) }
    val amountMinor = parseAmountMinor(amount)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить операцию", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FinanceAmountField(amount, { amount = it }, currency)
                CurrencySelector(currency) { currency = it }
                val needsTarget = transaction.type != TransactionType.INCOME
                val targets = if (transaction.type == TransactionType.EXPENSE) data.categories else data.friends
                if (needsTarget) {
                    Text(
                        when (transaction.type) {
                            TransactionType.EXPENSE -> "Категория"
                            TransactionType.I_OWE -> "Кому"
                            else -> "Кто"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    targets.forEach { item ->
                        val color = FinanceCategoryPalette[
                            if (transaction.type == TransactionType.EXPENSE) data.categoryColorIndex(item) else data.friendColorIndex(item)
                        ]
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = color.copy(alpha = if (target == item) 0.24f else 0.10f),
                            modifier = Modifier.border(
                                if (target == item) 1.dp else 0.dp,
                                if (target == item) color else Color.Transparent,
                                RoundedCornerShape(13.dp),
                            ).clickable { target = item },
                        ) {
                            Text(
                                item,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                color = if (target == item) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (target == item) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий") },
                    placeholder = { Text("Например: счёт за воду") },
                    supportingText = { Text("Необязательно") },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2F7DFF)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amountMinor, currency, if (transaction.type == TransactionType.INCOME) transaction.type.title else target, comment) },
                enabled = amountMinor > 0 && (transaction.type == TransactionType.INCOME || target.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F7DFF), contentColor = Color.White),
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
                                if (transaction.comment.isNotBlank()) {
                                    Text(
                                        transaction.comment,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                    )
                                }
                                if (transaction.settled) Text("Погашен", color = AppGreen, style = MaterialTheme.typography.labelSmall)
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
                colors = ButtonDefaults.buttonColors(containerColor = AppGreen, contentColor = Color.White),
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
                        FinanceColorGrid(data.categoryColorIndex(category)) { onColorChange(category, it) }
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
                        FinanceColorGrid(data.friendColorIndex(friend)) { onColorChange(friend, it) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } },
    )
}

@Composable
private fun FinanceColorGrid(selectedIndex: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FinanceCategoryPalette.chunked(5).forEachIndexed { rowIndex, colors ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                colors.forEachIndexed { columnIndex, color ->
                    val index = rowIndex * 5 + columnIndex
                    val selected = selectedIndex == index
                    Surface(
                        modifier = Modifier.size(30.dp).clickable { onSelect(index) },
                        shape = CircleShape,
                        color = color,
                        border = if (selected) BorderStroke(3.dp, Color.White) else null,
                    ) {
                        if (selected) Box(contentAlignment = Alignment.Center) {
                            Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

private fun transactionTypeColor(type: TransactionType): Color = when (type) {
    TransactionType.EXPENSE -> AccentOrange
    TransactionType.INCOME -> AppGreen
    TransactionType.OWED_TO_ME -> AppBlue
    TransactionType.I_OWE -> AppLavender
}

internal val FinanceCategoryPalette = listOf(
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

@Composable
private fun AmountDialog(title: String, currency: MoneyCurrency, current: Long, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var value by remember { mutableStateOf(if (current > 0) "${current / 100}.${(current % 100).toString().padStart(2, '0')}" else "") }
    val parsed = parseAmountMinor(value)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Укажите, сколько денег у вас было до добавления доходов и расходов.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FinanceAmountField(value, { value = it }, currency)
                Text(
                    "Текущая валюта: ${currency.code}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(parsed) },
                enabled = value.isNotBlank() && parsed >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = AppGreen, contentColor = Color.White),
            ) { Text("Сохранить") }
        },
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
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = AppSurface,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        ) {
            Column(
                Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF172B48),
                                AppSurface,
                                AccentOrange.copy(alpha = 0.22f),
                            ),
                        ),
                    )
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = AppBlue.copy(alpha = 0.18f)) {
                        Icon(
                            Icons.Rounded.CurrencyExchange,
                            contentDescription = null,
                            tint = AppBlue,
                            modifier = Modifier.padding(12.dp).size(25.dp),
                        )
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Курсы валют", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text("Bybit P2P или ручной курс", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = if (rates.automatic) AppGreen.copy(alpha = 0.17f) else AccentOrange.copy(alpha = 0.17f)) {
                        Text(
                            if (rates.automatic) "AUTO" else "ВРУЧНУЮ",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            color = if (rates.automatic) AppGreen else AccentOrange,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(20.dp), color = Color.Black.copy(alpha = 0.20f)) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Как считается курс", color = AccentOrange, fontWeight = FontWeight.Bold)
                        Text(
                            "Для песо используется продажа USDT, для рубля — покупка USDT. Так приложение показывает именно тот курс, который нужен для ваших расчётов.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                GradientActionButton(
                    label = if (rates.automatic) "Обновить курс автоматически" else "Включить автоматический курс",
                    brush = Brush.horizontalGradient(listOf(AppBlue, Color(0xFF1556BE))),
                    onClick = onAutomatic,
                )

                AppSectionHeading("Ручная настройка", "USD → валюта", AccentOrange)
                RateValueField("Покупка USDT", "RUB", rub) { rub = it }
                RateValueField("Продажа USDT", "ARS", ars) { ars = it }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(0.7f)) { Text("Отмена") }
                    GradientActionButton(
                        label = "Сохранить вручную",
                        brush = Brush.horizontalGradient(listOf(AccentOrange, Color(0xFF9E3D16))),
                        onClick = { onManual(parsePositiveDouble(rub), parsePositiveDouble(ars)) },
                        modifier = Modifier.weight(1.3f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RateValueField(
    operation: String,
    currencyCode: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(operation, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            Text("1 USD =", color = AppBlue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            suffix = { Text(currencyCode, color = AccentOrange, fontWeight = FontWeight.ExtraBold) },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = AppBlue.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.18f),
                cursorColor = AppGreen,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GradientActionButton(
    label: String,
    brush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(17.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = Color.Transparent,
    ) {
        Box(
            Modifier.fillMaxWidth().background(brush).padding(horizontal = 16.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, color = Color.White, fontWeight = FontWeight.ExtraBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
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
