package com.example.habittracker.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.math.roundToLong

private const val FinancePaletteSize = 12

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
    val title: String = "",
    val amountMinor: Long,
    val currency: MoneyCurrency,
    val type: TransactionType,
    val category: String,
    val epochDay: Long,
    val settled: Boolean = false,
    val comment: String = "",
)

@Serializable
data class CurrencyBudget(val currency: MoneyCurrency, val amountMinor: Long)

@Serializable
data class ExchangeRates(
    val rubPerUsd: Double? = null,
    val arsPerUsd: Double? = null,
    val sourceLabel: String = "Автокурс",
    val automatic: Boolean = true,
    val updatedAtLabel: String? = null,
)

@Serializable
private data class CriptoYaQuote(val bid: Double, val time: Long)

@Serializable
private data class SaikyoQuote(
    val buy_usdt_via_sbp_qr_rub: Double,
    val updated_at: String,
)

data class AutomaticExchangeRates(
    val arsSalePerUsd: Double,
    val rubPurchasePerUsd: Double,
    val updatedAtLabel: String,
)

class PublicExchangeRateService(
    private val client: HttpClient = HttpClient(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetch(): AutomaticExchangeRates {
        val ars = json.decodeFromString<CriptoYaQuote>(
            client.get("https://criptoya.com/api/bybitp2p/USDT/ARS/100").bodyAsText(),
        )
        val rub = json.decodeFromString<SaikyoQuote>(
            client.get("https://saikyo.exchange/api/public/usdt-rub").bodyAsText(),
        )
        require(ars.bid > 0 && rub.buy_usdt_via_sbp_qr_rub > 0) { "Exchange rate is unavailable" }
        return AutomaticExchangeRates(
            arsSalePerUsd = ars.bid,
            rubPurchasePerUsd = rub.buy_usdt_via_sbp_qr_rub,
            updatedAtLabel = rub.updated_at.takeIf { it.length >= 16 }?.substring(11, 16)?.let { "$it UTC" }
                ?: rub.updated_at,
        )
    }
}

@Serializable
data class FinanceData(
    val displayCurrency: MoneyCurrency = MoneyCurrency.USD,
    val budgets: List<CurrencyBudget> = emptyList(),
    val transactions: List<MoneyTransaction> = emptyList(),
    val rates: ExchangeRates = ExchangeRates(),
    val categories: List<String> = listOf(
        "ЖКХ",
        "Коммунальные услуги",
        "Аренда",
        "Продукты",
        "Транспорт",
        "Здоровье",
        "Развлечения",
        "Прочее",
    ),
    val categoryColors: Map<String, Int> = emptyMap(),
    val friends: List<String> = listOf("Бак", "Коля"),
    val friendColors: Map<String, Int> = mapOf("Бак" to 4, "Коля" to 2),
)

class FinanceRepository(private val storage: HabitStorage) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun load(): FinanceData = storage.readFinance()?.let {
        runCatching { json.decodeFromString<FinanceData>(it) }.getOrNull()
    } ?: FinanceData()

    fun save(data: FinanceData) = storage.writeFinance(json.encodeToString(data))
}

data class FinanceState(val data: FinanceData = FinanceData(), val isLoading: Boolean = true) {
    fun budgetMinor(): Long {
        val budget = data.budgets.firstOrNull() ?: return 0
        return convertMinor(budget.amountMinor, budget.currency, data.displayCurrency, data.rates) ?: 0
    }

    fun totalMinor(type: TransactionType): Long = data.transactions
        .filter { it.type == type && (!it.settled || type == TransactionType.EXPENSE || type == TransactionType.INCOME) }
        .mapNotNull { convertMinor(it.amountMinor, it.currency, data.displayCurrency, data.rates) }
        .sum()
}

class FinanceStore(
    private val repository: FinanceRepository,
    private val exchangeRateService: PublicExchangeRateService = PublicExchangeRateService(),
) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(FinanceState())
    val state: StateFlow<FinanceState> = mutableState.asStateFlow()

    fun load() { mutableState.value = FinanceState(repository.load(), false) }

    suspend fun selectCurrency(currency: MoneyCurrency) = update { it.copy(displayCurrency = currency) }

    suspend fun setBudget(currency: MoneyCurrency, amountMinor: Long) = update { data ->
        data.copy(budgets = listOf(CurrencyBudget(currency, amountMinor)))
    }

    suspend fun setRates(rubPerUsd: Double?, arsPerUsd: Double?) = update { data ->
        data.copy(
            rates = ExchangeRates(
                rubPerUsd = rubPerUsd?.takeIf { it > 0 },
                arsPerUsd = arsPerUsd?.takeIf { it > 0 },
                sourceLabel = "Ручной курс",
                automatic = false,
            ),
        )
    }

    suspend fun refreshRates(force: Boolean = false): Boolean {
        if (!force && !mutableState.value.data.rates.automatic) return false
        val fresh = runCatching { exchangeRateService.fetch() }.getOrNull() ?: return false
        update { data ->
            data.copy(
                rates = ExchangeRates(
                    rubPerUsd = fresh.rubPurchasePerUsd,
                    arsPerUsd = fresh.arsSalePerUsd,
                    sourceLabel = "Bybit P2P / SAIKYO",
                    automatic = true,
                    updatedAtLabel = fresh.updatedAtLabel,
                ),
            )
        }
        return true
    }

    suspend fun addTransaction(
        amountMinor: Long,
        currency: MoneyCurrency,
        type: TransactionType,
        target: String,
        comment: String = "",
    ) = update { data ->
        val normalizedTarget = if (type == TransactionType.INCOME) type.title else target.trim()
        if (normalizedTarget.isBlank() || amountMinor <= 0) data else data.copy(
            transactions = listOf(
                MoneyTransaction(
                    id = newHabitId(),
                    title = normalizedTarget,
                    amountMinor = amountMinor,
                    currency = currency,
                    type = type,
                    category = normalizedTarget,
                    epochDay = currentEpochDay(),
                    comment = comment.trim(),
                ),
            ) + data.transactions,
        )
    }

    suspend fun deleteTransaction(id: String) = update { data ->
        data.copy(transactions = data.transactions.filterNot { it.id == id })
    }

    suspend fun updateTransaction(
        id: String,
        amountMinor: Long,
        currency: MoneyCurrency,
        target: String,
        comment: String = "",
    ) = update { data ->
        if (amountMinor <= 0 || target.isBlank()) data else data.copy(
            transactions = data.transactions.map { transaction ->
                if (transaction.id == id) transaction.copy(
                    title = target.trim(),
                    amountMinor = amountMinor,
                    currency = currency,
                    category = target.trim(),
                    comment = comment.trim(),
                ) else transaction
            },
        )
    }

    suspend fun toggleTransactionSettled(id: String) = update { data ->
        data.copy(
            transactions = data.transactions.map { transaction ->
                if (transaction.id == id &&
                    (transaction.type == TransactionType.OWED_TO_ME || transaction.type == TransactionType.I_OWE)
                ) transaction.copy(settled = !transaction.settled) else transaction
            },
        )
    }

    suspend fun addCategory(name: String) = update { data ->
        val normalized = name.trim()
        if (normalized.isBlank() || data.categories.any { it.equals(normalized, ignoreCase = true) }) data
        else data.copy(
            categories = data.categories + normalized,
            categoryColors = data.categoryColors + (normalized to data.categories.size.mod(FinancePaletteSize)),
        )
    }

    suspend fun deleteCategory(name: String) = update { data ->
        data.copy(
            categories = data.categories.filterNot { it == name },
            categoryColors = data.categoryColors - name,
        )
    }

    suspend fun updateCategoryColor(name: String, colorIndex: Int) = update { data ->
        data.copy(categoryColors = data.categoryColors + (name to colorIndex.mod(FinancePaletteSize)))
    }

    suspend fun addFriend(name: String) = update { data ->
        val normalized = name.trim()
        if (normalized.isBlank() || data.friends.any { it.equals(normalized, ignoreCase = true) }) data
        else data.copy(
            friends = data.friends + normalized,
            friendColors = data.friendColors + (normalized to data.friends.size.mod(FinancePaletteSize)),
        )
    }

    suspend fun deleteFriend(name: String) = update { data ->
        data.copy(friends = data.friends.filterNot { it == name }, friendColors = data.friendColors - name)
    }

    suspend fun updateFriendColor(name: String, colorIndex: Int) = update { data ->
        data.copy(friendColors = data.friendColors + (name to colorIndex.mod(FinancePaletteSize)))
    }

    private suspend fun update(transform: (FinanceData) -> FinanceData) = mutex.withLock {
        val updated = transform(mutableState.value.data)
        repository.save(updated)
        mutableState.value = FinanceState(updated, false)
    }
}

fun FinanceData.categoryColorIndex(name: String): Int = categoryColors[name]
    ?: categories.indexOf(name).takeIf { it >= 0 }?.mod(FinancePaletteSize)
    ?: name.fold(0) { result, char -> result + char.code }.mod(FinancePaletteSize)

fun FinanceData.friendColorIndex(name: String): Int = friendColors[name]
    ?: friends.indexOf(name).takeIf { it >= 0 }?.mod(FinancePaletteSize)
    ?: name.fold(0) { result, char -> result + char.code }.mod(FinancePaletteSize)

fun FinanceState.expensesByCategory(): List<Pair<String, Long>> = data.transactions
    .asSequence()
    .filter { it.type == TransactionType.EXPENSE }
    .mapNotNull { transaction ->
        convertMinor(transaction.amountMinor, transaction.currency, data.displayCurrency, data.rates)
            ?.let { transaction.category to it }
    }
    .groupBy({ it.first }, { it.second })
    .map { (category, values) -> category to values.sum() }
    .sortedByDescending { it.second }

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
