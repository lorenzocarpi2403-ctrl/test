package com.example.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TAG = "SheetsSyncService"
private const val PREFS_NAME = "google_sheets_config"
private const val KEY_WEBHOOK_URL = "webhook_url"
private const val KEY_SHEET_ID = "sheet_id"
private const val KEY_SHEET_NAME = "sheet_name"

class GoogleSheetsSyncService(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    var webhookUrl: String
        get() = prefs.getString(KEY_WEBHOOK_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBHOOK_URL, value.trim()).apply()

    var sheetId: String
        get() = prefs.getString(KEY_SHEET_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SHEET_ID, value.trim()).apply()

    var sheetName: String
        get() = prefs.getString(KEY_SHEET_NAME, "Spese") ?: "Spese"
        set(value) = prefs.edit().putString(KEY_SHEET_NAME, value.trim()).apply()

    fun isConfigured(): Boolean {
        return webhookUrl.isNotBlank() || sheetId.isNotBlank()
    }

    /**
     * Sincronizza una singola spesa su Google Sheets tramite Webhook o endpoint configurato.
     */
    suspend fun syncExpense(expense: ExpenseEntity): Result<String> = withContext(Dispatchers.IO) {
        val targetUrl = webhookUrl.ifBlank {
            if (sheetId.isNotBlank()) {
                // Se è stato inserito l'ID del foglio ma nessun webhook, possiamo guidare l'utente
                "https://script.google.com/macros/s/AKfycbz_placeholder/exec"
            } else ""
        }

        if (targetUrl.isBlank() || targetUrl.contains("placeholder")) {
            return@withContext Result.failure(
                Exception("Configura l'URL di Google Sheets / Apps Script nelle Impostazioni per la sincronizzazione diretta.")
            )
        }

        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateFormatted = dateFormat.format(Date(expense.date))

            val payload = JSONObject().apply {
                put("date", dateFormatted)
                put("merchant", expense.merchant)
                put("category", expense.category)
                put("amount", expense.amount)
                put("currency", expense.currency)
                put("notes", expense.notes)
                put("sheetName", sheetName)
                put("sheetId", sheetId)
            }

            val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(targetUrl)
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("Spesa sincronizzata con successo su Google Sheets!")
            } else {
                val errorBody = response.body?.string().orEmpty()
                Result.failure(Exception("Errore HTTP ${response.code}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync to Google Sheets failed", e)
            Result.failure(Exception(e.localizedMessage ?: "Errore di connessione a Google Sheets."))
        }
    }

    /**
     * Esporta tutte le spese in un file CSV formattato per Google Sheets e apre la condivisione Android.
     */
    suspend fun exportToCsvAndShare(expenses: List<ExpenseEntity>): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "receipt_exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, "Spese_ReceiptSheet_${System.currentTimeMillis()}.csv")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            FileOutputStream(file).use { out ->
                val header = "Data,Esercente,Categoria,Importo,Valuta,Note,Sincronizzato\n"
                out.write(header.toByteArray(Charsets.UTF_8))

                for (item in expenses) {
                    val dateFormatted = dateFormat.format(Date(item.date))
                    val escapedMerchant = "\"${item.merchant.replace("\"", "\"\"")}\""
                    val escapedNotes = "\"${item.notes.replace("\"", "\"\"")}\""
                    val line = "$dateFormatted,$escapedMerchant,${item.category},${item.amount},${item.currency},$escapedNotes,${if (item.isSyncedToSheets) "Si" else "No"}\n"
                    out.write(line.toByteArray(Charsets.UTF_8))
                }
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Esportazione Spese per Google Sheets")
                putExtra(Intent.EXTRA_TEXT, "File CSV con le spese estratte da ReceiptSheet per l'importazione in Google Sheets.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Result.success(Intent.createChooser(shareIntent, "Apri o Condividi con Google Sheets"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create CSV export", e)
            Result.failure(Exception("Impossibile generare il file CSV per Google Sheets."))
        }
    }
}
