package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchant: String,
    val amount: Double,
    val currency: String = "€",
    val category: String,
    val date: Long, // Epoch millis
    val notes: String = "",
    val imageUri: String? = null,
    val isSyncedToSheets: Boolean = false,
    val syncedAt: Long? = null
)
