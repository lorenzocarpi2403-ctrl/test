package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.ExpenseCategory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "ReceiptAiService"

data class ExtractedReceipt(
    val merchant: String,
    val amount: Double,
    val currency: String,
    val dateEpochMillis: Long,
    val isDateDetectedFromReceipt: Boolean = false,
    val category: ExpenseCategory,
    val itemsSummary: String,
    val rawJson: String
)

class ReceiptAiService {

    // Define JSON schema with Schema.obj (SDK convention)
    private val receiptSchema = Schema.obj(
        properties = mapOf(
            "merchantName" to Schema.string("Nome del negozio, supermercato o esercente sullo scontrino"),
            "totalAmount" to Schema.double("Importo totale da pagare o pagato riportato sullo scontrino"),
            "currency" to Schema.string("Simbolo o sigla valuta (es. EUR, €, USD, $)"),
            "receiptDate" to Schema.string("Data dello scontrino in formato YYYY-MM-DD (es. converti 02/11/2026 in 2026-11-02) se leggibile, altrimenti stringa vuota"),
            "category" to Schema.string("Categoria spesa: GROCERIES, RESTAURANT, TRANSPORT, UTILITIES, SHOPPING, HEALTH, ENTERTAINMENT, OTHER"),
            "itemsSummary" to Schema.string("Breve elenco o descrizione degli articoli principali acquistati")
        ),
        optionalProperties = listOf("currency", "receiptDate", "itemsSummary")
    )

    private val config = generationConfig {
        responseMimeType = "application/json"
        responseSchema = receiptSchema
    }

    // High-speed Gemini 3.5 Flash-Lite model for ultra-low latency vision extraction
    private val fastModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = config
        )
    }

    // Full Gemini 3.8 Flash model
    private val flashModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    suspend fun extractReceiptData(bitmap: Bitmap): Result<ExtractedReceipt> = withContext(Dispatchers.IO) {
        // Scale to 800px max dimension for fast transmission over mobile/emulator connections
        val scaledBitmap = scaleBitmapIfNeeded(bitmap, 800)

        val prompt = """
            Estrai i dati chiave da questo scontrino fiscale o ricevuta:
            - merchantName: nome attività commerciale o negozio
            - totalAmount: importo totale complessivo pagato (numero decimale)
            - currency: valuta (EUR o €)
            - receiptDate: cerca accuratamente la data dello scontrino o documento commerciale (ad es. 02/11/2026, 02-11-2026, 2026-11-02) e digitalizzala in formato YYYY-MM-DD. Se non è presente o non leggibile, lascia stringa vuota.
            - category: GROCERIES, RESTAURANT, TRANSPORT, UTILITIES, SHOPPING, HEALTH, ENTERTAINMENT, OTHER
            - itemsSummary: breve riepilogo articoli principali
        """.trimIndent()

        // 1. Try high-speed gemini-3.5-flash-lite first (30s timeout)
        var jsonString: String? = null
        try {
            jsonString = withTimeout(30_000L) {
                val response = fastModel.generateContent(
                    content {
                        image(scaledBitmap)
                        text(prompt)
                    }
                )
                response.text
            }
            Log.d(TAG, "Extracted with gemini-3.5-flash-lite")
        } catch (e: Exception) {
            if (e is CancellationException && e !is TimeoutCancellationException) throw e
            Log.w(TAG, "Fast model failed or timed out: ${e.message}, trying gemini-3.8-flash...")
        }

        // 2. Fallback to gemini-3.8-flash if fast model did not return (35s timeout)
        if (jsonString.isNullOrBlank()) {
            try {
                jsonString = withTimeout(35_000L) {
                    val response = flashModel.generateContent(
                        content {
                            image(scaledBitmap)
                            text(prompt)
                        }
                    )
                    response.text
                }
                Log.d(TAG, "Extracted with gemini-3.8-flash")
            } catch (e: Exception) {
                if (e is CancellationException && e !is TimeoutCancellationException) throw e
                Log.e(TAG, "Both Gemini models failed or timed out", e)
                return@withContext Result.failure(
                    Exception(
                        if (e is TimeoutCancellationException)
                            "L'analisi ha richiesto troppo tempo. Puoi comunque rivedere e inserire i dati con la foto scansionata."
                        else e.localizedMessage ?: "Errore durante l'analisi dello scontrino."
                    )
                )
            }
        }

        if (jsonString.isNullOrBlank()) {
            return@withContext Result.failure(Exception("Gemini ha restituito una risposta vuota."))
        }

        try {
            Log.d(TAG, "Receipt JSON: $jsonString")
            val json = JSONObject(jsonString)

            val merchant = json.optString("merchantName").ifBlank { "Scontrino" }
            val amount = json.optDouble("totalAmount", 0.0)
            val currency = json.optString("currency").ifBlank { "€" }
            val dateStr = json.optString("receiptDate")
            val categoryStr = json.optString("category")
            val itemsSummary = json.optString("itemsSummary")

            val (parsedDate, isDateFound) = parseIsoDateOrCurrent(dateStr)
            val category = ExpenseCategory.fromString(categoryStr)

            Result.success(
                ExtractedReceipt(
                    merchant = merchant,
                    amount = amount,
                    currency = if (currency.contains("EUR", ignoreCase = true) || currency == "€") "€" else currency,
                    dateEpochMillis = parsedDate,
                    isDateDetectedFromReceipt = isDateFound,
                    category = category,
                    itemsSummary = itemsSummary,
                    rawJson = jsonString
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse receipt JSON", e)
            Result.failure(Exception("Impossibile interpretare il formato JSON restituito dall'IA."))
        }
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    internal fun parseIsoDateOrCurrent(dateStr: String?): Pair<Long, Boolean> {
        if (dateStr.isNullOrBlank()) return Pair(System.currentTimeMillis(), false)
        val trimmed = dateStr.trim()
        val patterns = listOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "dd.MM.yyyy",
            "d/M/yyyy",
            "d-M-yyyy",
            "dd/MM/yy",
            "dd-MM-yy",
            "yyyy/MM/dd",
            "yyyy.MM.dd"
        )
        for (pattern in patterns) {
            try {
                val format = SimpleDateFormat(pattern, Locale.ITALIAN).apply {
                    isLenient = false
                }
                val date = format.parse(trimmed)
                if (date != null) {
                    return Pair(date.time, true)
                }
            } catch (_: Exception) {}
        }
        return Pair(System.currentTimeMillis(), false)
    }
}
