package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String = "EXPENSE", // EXPENSE or INCOME
    val title: String,
    val amount: Double,
    val currency: String = "€",
    val category: String,
    val date: Long, // Epoch millis
    val accountId: Long = 1,
    val notes: String = "",
    val imageUri: String? = null
)
