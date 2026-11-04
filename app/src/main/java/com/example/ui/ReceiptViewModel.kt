package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ExtractedReceipt
import com.example.ai.ReceiptAiService
import com.example.data.AccountEntity
import com.example.data.AppDatabase
import com.example.data.AppThemeConfig
import com.example.data.BudgetEntity
import com.example.data.BudgetProgress
import com.example.data.ExpenseCategory
import com.example.data.FinanceRepository
import com.example.data.MonthlyBudgetHelper
import com.example.data.RecurringTransactionEntity
import com.example.data.SubscriptionEntity
import com.example.data.ThemePreferences
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.ui.theme.AppBackgroundTheme
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "FinanceViewModel"

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class Scanning(val message: String = "Analisi dello scontrino con Gemini AI in corso...") : ScanUiState
    data class ReviewReady(
        val extracted: ExtractedReceipt,
        val capturedBitmap: Bitmap? = null,
        val imageUri: String? = null
    ) : ScanUiState
    data class ErrorWithFallback(
        val message: String,
        val capturedBitmap: Bitmap? = null,
        val imageUri: String? = null
    ) : ScanUiState
}

enum class TransactionFilter(val label: String) {
    ALL("Tutte"),
    EXPENSE("Spese"),
    INCOME("Entrate")
}

class ReceiptViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FinanceRepository(
        db.accountDao(),
        db.transactionDao(),
        db.subscriptionDao(),
        db.recurringTransactionDao(),
        db.budgetDao()
    )
    private val aiService = ReceiptAiService()
    private val themePreferences = ThemePreferences(application)

    // Theme & Personalizzazione
    val themeConfig: StateFlow<AppThemeConfig> = themePreferences.themeConfig

    // Scan State
    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow(TransactionFilter.ALL)
    val typeFilter: StateFlow<TransactionFilter> = _typeFilter.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategory: StateFlow<ExpenseCategory?> = _selectedCategory.asStateFlow()

    // Accounts
    val allAccounts = repository.allAccounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalNetWorth = repository.totalNetWorth.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Transactions
    val allTransactions = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredTransactions = combine(
        allTransactions,
        _searchQuery,
        _typeFilter,
        _selectedCategory
    ) { transactions, query, type, cat ->
        transactions.filter { item ->
            val matchesType = when (type) {
                TransactionFilter.ALL -> true
                TransactionFilter.EXPENSE -> item.type == "EXPENSE"
                TransactionFilter.INCOME -> item.type == "INCOME"
            }
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.notes.contains(query, ignoreCase = true) ||
                item.amount.toString().contains(query)
            val matchesCategory = cat == null || item.category == cat.name
            matchesType && matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Subscriptions
    val allSubscriptions = repository.allSubscriptions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Fixed & Recurring Transactions (Entrate e Uscite Fisse)
    val allRecurring = repository.allRecurring.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Monthly Spending Limit (Budget Mensile)
    val monthlyBudget = repository.monthlyBudget.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val budgetProgress = combine(
        monthlyBudget,
        allTransactions
    ) { budget, transactions ->
        MonthlyBudgetHelper.calculateProgress(budget?.monthlyLimit, transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onTypeFilterChanged(filter: TransactionFilter) {
        _typeFilter.value = filter
    }

    fun onCategoryFilterChanged(category: ExpenseCategory?) {
        _selectedCategory.value = category
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    fun dismissScanState() {
        _scanState.value = ScanUiState.Idle
    }

    fun proceedWithManualFallback(bitmap: Bitmap?, imageUri: String?) {
        _scanState.value = ScanUiState.ReviewReady(
            extracted = ExtractedReceipt(
                merchant = "Nuova Spesa",
                amount = 0.0,
                currency = "€",
                dateEpochMillis = System.currentTimeMillis(),
                category = ExpenseCategory.GROCERIES,
                itemsSummary = "",
                rawJson = "{}"
            ),
            capturedBitmap = bitmap,
            imageUri = imageUri
        )
    }

    /**
     * Analisi scontrino da immagine (fotocamera o galleria) con Gemini AI
     */
    fun analyzeReceipt(bitmap: Bitmap, imageUri: Uri? = null) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Scanning()
            try {
                val result = aiService.extractReceiptData(bitmap)
                result.fold(
                    onSuccess = { extracted ->
                        _scanState.value = ScanUiState.ReviewReady(
                            extracted = extracted,
                            capturedBitmap = bitmap,
                            imageUri = imageUri?.toString()
                        )
                    },
                    onFailure = { error ->
                        _scanState.value = ScanUiState.ErrorWithFallback(
                            message = error.localizedMessage ?: "Errore durante l'analisi dello scontrino.",
                            capturedBitmap = bitmap,
                            imageUri = imageUri?.toString()
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
                _scanState.value = ScanUiState.ErrorWithFallback(
                    message = e.localizedMessage ?: "Si è verificato un errore inaspettato.",
                    capturedBitmap = bitmap,
                    imageUri = imageUri?.toString()
                )
            }
        }
    }

    /**
     * Aggiunge una transazione (Spesa o Entrata) e aggiorna automaticamente il saldo del conto
     */
    fun addTransaction(
        type: TransactionType,
        title: String,
        amount: Double,
        currency: String = "€",
        category: ExpenseCategory,
        date: Long,
        accountId: Long,
        notes: String = "",
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            try {
                val entity = TransactionEntity(
                    type = type.name,
                    title = title.ifBlank { if (type == TransactionType.INCOME) "Entrata" else "Spesa" },
                    amount = amount,
                    currency = currency,
                    category = category.name,
                    date = date,
                    accountId = accountId,
                    notes = notes,
                    imageUri = imageUri
                )
                repository.addTransaction(entity)
                _scanState.value = ScanUiState.Idle
                val symbol = if (type == TransactionType.INCOME) "+€" else "-€"
                _userFeedback.value = "${type.labelItalian} registrata: $symbol${String.format("%.2f", amount)}"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save transaction", e)
                _userFeedback.value = "Errore durante il salvataggio: ${e.localizedMessage}"
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _userFeedback.value = "Transazione eliminata e saldo conto ripristinato."
        }
    }

    /**
     * Gestione Conti
     */
    fun addAccount(name: String, initialBalance: Double, colorHex: Long, iconType: String) {
        viewModelScope.launch {
            val account = AccountEntity(
                name = name.ifBlank { "Nuovo Conto" },
                balance = initialBalance,
                initialBalance = initialBalance,
                colorHex = colorHex,
                iconType = iconType
            )
            repository.insertAccount(account)
            _userFeedback.value = "Conto '$name' aggiunto con saldo €${String.format("%.2f", initialBalance)}!"
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            _userFeedback.value = "Conto '${account.name}' eliminato."
        }
    }

    /**
     * Gestione Abbonamenti
     */
    fun addSubscription(
        name: String,
        cost: Double,
        billingCycle: String,
        nextRenewalDate: Long,
        category: String,
        colorHex: Long,
        accountId: Long
    ) {
        viewModelScope.launch {
            val sub = SubscriptionEntity(
                name = name.ifBlank { "Abbonamento" },
                cost = cost,
                billingCycle = billingCycle,
                nextRenewalDate = nextRenewalDate,
                category = category,
                colorHex = colorHex,
                accountId = accountId,
                isActive = true
            )
            repository.addSubscription(sub)
            _userFeedback.value = "Abbonamento '$name' (€${String.format("%.2f", cost)}/$billingCycle) aggiunto!"
        }
    }

    fun toggleSubscriptionActive(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            val updated = subscription.copy(isActive = !subscription.isActive)
            repository.updateSubscription(updated)
            _userFeedback.value = if (updated.isActive) "Abbonamento riattivato" else "Abbonamento messo in pausa"
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
            _userFeedback.value = "Abbonamento rimosso."
        }
    }

    /**
     * Gestione Transazioni Fisse (Entrate e Uscite Fisse)
     */
    fun addRecurring(
        type: String, // EXPENSE or INCOME
        title: String,
        amount: Double,
        frequency: String,
        dayOfMonth: Int,
        category: ExpenseCategory,
        colorHex: Long,
        accountId: Long,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val recurring = RecurringTransactionEntity(
                type = type,
                title = title.ifBlank { if (type == "INCOME") "Entrata Fissa" else "Uscita Fissa" },
                amount = amount,
                currency = "€",
                frequency = frequency,
                dayOfMonth = dayOfMonth,
                nextDate = now + (dayOfMonth.coerceIn(1, 30) * 86_400_000L),
                category = category.name,
                colorHex = colorHex,
                accountId = accountId,
                isActive = true,
                notes = notes
            )
            repository.addRecurring(recurring)
            val typeLabel = if (type == "INCOME") "Entrata fissa" else "Spesa fissa"
            _userFeedback.value = "$typeLabel '$title' (€${String.format("%.2f", amount)}) aggiunta!"
        }
    }

    fun toggleRecurringActive(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            val updated = recurring.copy(isActive = !recurring.isActive)
            repository.updateRecurring(updated)
            _userFeedback.value = if (updated.isActive) "Transazione fissa riattivata" else "Transazione fissa disattivata"
        }
    }

    fun deleteRecurring(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.deleteRecurring(recurring)
            _userFeedback.value = "Transazione fissa eliminata."
        }
    }

    fun applyRecurringNow(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.applyRecurringNow(recurring)
            val symbol = if (recurring.type == "INCOME") "+€" else "-€"
            _userFeedback.value = "✓ Registrato nel conto: '${recurring.title}' ($symbol${String.format("%.2f", recurring.amount)})"
        }
    }

    /**
     * Gestione Limite di Spesa Mensile (Budget)
     */
    fun setMonthlyBudget(limit: Double) {
        viewModelScope.launch {
            if (limit <= 0) {
                repository.clearMonthlyBudget()
                _userFeedback.value = "Limite di spesa mensile rimosso."
            } else {
                repository.setMonthlyBudget(limit)
                _userFeedback.value = "Budget mensile impostato a €${String.format(java.util.Locale.ITALIAN, "%.2f", limit)}!"
            }
        }
    }

    fun clearMonthlyBudget() {
        viewModelScope.launch {
            repository.clearMonthlyBudget()
            _userFeedback.value = "Limite di spesa mensile disattivato."
        }
    }

    /**
     * Gestione Personalizzazione Tema & Colori
     */
    fun setColorTheme(theme: AppColorTheme) {
        themePreferences.setColorTheme(theme)
        _userFeedback.value = "Colore tema: ${theme.labelItalian} (${theme.hexDescription})"
    }

    fun setBackgroundTheme(bgTheme: AppBackgroundTheme) {
        themePreferences.setBackgroundTheme(bgTheme)
        _userFeedback.value = "Sfondo aggiornato: ${bgTheme.labelItalian}"
    }

    fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
        _userFeedback.value = "Modalità display: ${mode.labelItalian}"
    }
}
