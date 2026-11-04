package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String = "EXPENSE", // EXPENSE or INCOME
    val title: String,
    val amount: Double,
    val currency: String = "€",
    val frequency: String = "MONTHLY", // MONTHLY, ANNUAL, WEEKLY
    val dayOfMonth: Int = 1, // Giorno previsto del mese (1-31)
    val nextDate: Long, // Epoch millis della prossima scadenza
    val category: String,
    val colorHex: Long = 0xFF6366F1,
    val accountId: Long = 1,
    val isActive: Boolean = true,
    val notes: String = ""
) {
    val monthlyEquivalent: Double
        get() = when (frequency) {
            "ANNUAL" -> amount / 12.0
            "WEEKLY" -> amount * 4.33
            else -> amount
        }
}
