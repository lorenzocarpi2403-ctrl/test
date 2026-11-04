package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val balance: Double,
    val initialBalance: Double = balance,
    val colorHex: Long = 0xFF3B82F6, // Default blue
    val iconType: String = "BANK" // BANK, CARD, CASH, SAVINGS
)
