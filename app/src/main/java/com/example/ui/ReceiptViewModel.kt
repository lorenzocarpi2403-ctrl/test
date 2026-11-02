package com.example.ui

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ExtractedReceipt
import com.example.ai.ReceiptAiService
import com.example.data.AppDatabase
import com.example.data.ExpenseCategory
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.sync.GoogleSheetsSyncService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "ReceiptViewModel"

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

class ReceiptViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ExpenseRepository(db.expenseDao())
    private val aiService = ReceiptAiService()
    val sheetsSyncService = GoogleSheetsSyncService(application)

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategory: StateFlow<ExpenseCategory?> = _selectedCategory.asStateFlow()

    val allExpenses = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredExpenses = combine(allExpenses, _searchQuery, _selectedCategory) { list, query, cat ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.merchant.contains(query, ignoreCase = true) ||
                item.notes.contains(query, ignoreCase = true) ||
                item.amount.toString().contains(query)
            val matchesCategory = cat == null || item.category == cat.name
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
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
     * Analisi scontrino da immagine (fotocamera o galleria)
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
     * Conferma e salvataggio spesa nel database Room + eventuale sincronizzazione Sheets
     */
    fun saveExpense(
        merchant: String,
        amount: Double,
        currency: String,
        category: ExpenseCategory,
        date: Long,
        notes: String,
        imageUri: String?,
        syncToSheets: Boolean
    ) {
        viewModelScope.launch {
            try {
                val entity = ExpenseEntity(
                    merchant = merchant.ifBlank { "Spesa" },
                    amount = amount,
                    currency = currency.ifBlank { "€" },
                    category = category.name,
                    date = date,
                    notes = notes,
                    imageUri = imageUri,
                    isSyncedToSheets = false
                )

                val newId = repository.insertExpense(entity)
                val savedEntity = entity.copy(id = newId)
                _scanState.value = ScanUiState.Idle

                if (syncToSheets) {
                    syncExpenseInternal(savedEntity)
                } else {
                    _userFeedback.value = "Spesa salvata in locale (€${String.format("%.2f", amount)})"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save expense", e)
                _userFeedback.value = "Errore nel salvataggio: ${e.localizedMessage}"
            }
        }
    }

    fun syncExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            syncExpenseInternal(expense)
        }
    }

    private suspend fun syncExpenseInternal(expense: ExpenseEntity) {
        _isSyncing.value = true
        val syncResult = sheetsSyncService.syncExpense(expense)
        syncResult.fold(
            onSuccess = { msg ->
                repository.markAsSynced(expense.id)
                _userFeedback.value = "✓ $msg"
            },
            onFailure = { err ->
                _userFeedback.value = "Salvataggio locale completato. Sheets: ${err.localizedMessage}"
            }
        )
        _isSyncing.value = false
    }

    fun syncAllUnsynced() {
        viewModelScope.launch {
            _isSyncing.value = true
            val unsynced = repository.getUnsyncedExpenses()
            if (unsynced.isEmpty()) {
                _userFeedback.value = "Tutte le spese sono già sincronizzate."
                _isSyncing.value = false
                return@launch
            }

            var successCount = 0
            for (item in unsynced) {
                val res = sheetsSyncService.syncExpense(item)
                if (res.isSuccess) {
                    repository.markAsSynced(item.id)
                    successCount++
                }
            }
            _userFeedback.value = "Sincronizzate $successCount su ${unsynced.size} spese su Google Sheets."
            _isSyncing.value = false
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _userFeedback.value = "Spesa eliminata."
        }
    }

    fun exportToCsv(onIntentReady: (Intent) -> Unit) {
        viewModelScope.launch {
            val list = allExpenses.value
            if (list.isEmpty()) {
                _userFeedback.value = "Nessuna spesa da esportare."
                return@launch
            }
            val result = sheetsSyncService.exportToCsvAndShare(list)
            result.fold(
                onSuccess = { intent -> onIntentReady(intent) },
                onFailure = { err -> _userFeedback.value = err.localizedMessage }
            )
        }
    }

    fun saveSheetSettings(sheetId: String, webhookUrl: String, sheetName: String) {
        sheetsSyncService.sheetId = sheetId
        sheetsSyncService.webhookUrl = webhookUrl
        sheetsSyncService.sheetName = sheetName
        _userFeedback.value = "Impostazioni Google Sheets salvate!"
    }
}
