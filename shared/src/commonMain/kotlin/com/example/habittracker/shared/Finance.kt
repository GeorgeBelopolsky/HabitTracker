package com.example.habittracker.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToLong

@Serializable
enum class MoneyCurrency(val code: String, val symbol: String) {
    RUB("RUB", "₽"), USD("USD", "$"), ARS("ARS", "AR$")
}

@Serializable
enum class TransactionType(val title: String) {
    EXPENSE("Расход"), INCOME("Доход"), OWED_TO_ME("Мне должны"), I_OWE("Я должен")
}

@Serializable
data class MoneyTransaction(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val currency: MoneyCurrency,
    val type: TransactionType,
    val category: String,
    val epochDay: Long,
)

@Serializable
data class CurrencyBudget(val currency: MoneyCurrency, val amountMinor: Long)

@Serializable
data class ExchangeRates(
    val rubPerUsd: Double? = null,
    val arsPerUsd: Double? = null,
    val sourceLabel: String = "Ручной курс",
)

@Serializable
data class FinanceData(
    val displayCurrency: MoneyCurrency = MoneyCurrency.USD,
    val budgets: List<CurrencyBudget> = emptyList(),
    val transactions: List<MoneyTransaction> = emptyList(),
    val rates: ExchangeRates = ExchangeRates(),
)

class FinanceRepository(private val storage: HabitStorage) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun load(): FinanceData = storage.readFinance()?.let {
        runCatching { json.decodeFromString<FinanceData>(it) }.getOrNull()
    } ?: FinanceData()

    fun save(data: FinanceData) = storage.writeFinance(json.encodeToString(data))
}

data class FinanceState(val data: FinanceData = FinanceData(), val isLoading: Boolean = true) {
    fun budgetMinor(): Long = data.budgets.firstOrNull { it.currency == data.displayCurrency }?.amountMinor ?: 0

    fun totalMinor(type: TransactionType): Long = data.transactions
        .filter { it.type == type }
        .mapNotNull { convertMinor(it.amountMinor, it.currency, data.displayCurrency, data.rates) }
        .sum()
}

class FinanceStore(private val repository: FinanceRepository) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(FinanceState())
    val state: StateFlow<FinanceState> = mutableState.asStateFlow()

    fun load() { mutableState.value = FinanceState(repository.load(), false) }

    suspend fun selectCurrency(currency: MoneyCurrency) = update { it.copy(displayCurrency = currency) }

    suspend fun setBudget(currency: MoneyCurrency, amountMinor: Long) = update { data ->
        data.copy(budgets = data.budgets.filterNot { it.currency == currency } + CurrencyBudget(currency, amountMinor))
    }

    suspend fun setRates(rubPerUsd: Double?, arsPerUsd: Double?) = update { data ->
        data.copy(rates = ExchangeRates(rubPerUsd?.takeIf { it > 0 }, arsPerUsd?.takeIf { it > 0 }))
    }

    suspend fun addTransaction(
        title: String,
        amountMinor: Long,
        currency: MoneyCurrency,
        type: TransactionType,
        category: String,
    ) = update { data ->
        if (title.isBlank() || amountMinor <= 0) data else data.copy(
            transactions = listOf(
                MoneyTransaction(
                    id = newHabitId(),
                    title = title.trim(),
                    amountMinor = amountMinor,
                    currency = currency,
                    type = type,
                    category = category.trim().ifBlank { type.title },
                    epochDay = currentEpochDay(),
                ),
            ) + data.transactions,
        )
    }

    suspend fun deleteTransaction(id: String) = update { data ->
        data.copy(transactions = data.transactions.filterNot { it.id == id })
    }

    private suspend fun update(transform: (FinanceData) -> FinanceData) = mutex.withLock {
        val updated = transform(mutableState.value.data)
        repository.save(updated)
        mutableState.value = FinanceState(updated, false)
    }
}

fun convertMinor(
    amountMinor: Long,
    from: MoneyCurrency,
    to: MoneyCurrency,
    rates: ExchangeRates,
): Long? {
    if (from == to) return amountMinor
    fun unitsPerUsd(currency: MoneyCurrency): Double? = when (currency) {
        MoneyCurrency.USD -> 1.0
        MoneyCurrency.RUB -> rates.rubPerUsd
        MoneyCurrency.ARS -> rates.arsPerUsd
    }
    val sourceRate = unitsPerUsd(from) ?: return null
    val targetRate = unitsPerUsd(to) ?: return null
    return (amountMinor.toDouble() / sourceRate * targetRate).roundToLong()
}

fun formatMoney(amountMinor: Long, currency: MoneyCurrency): String {
    val absolute = kotlin.math.abs(amountMinor)
    val major = absolute / 100
    val cents = (absolute % 100).toString().padStart(2, '0')
    val sign = if (amountMinor < 0) "−" else ""
    return "$sign${currency.symbol}$major.$cents"
}
