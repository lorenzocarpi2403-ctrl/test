package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: Int = 1,
    val monthlyLimit: Double,
    val updatedAt: Long = System.currentTimeMillis()
)
