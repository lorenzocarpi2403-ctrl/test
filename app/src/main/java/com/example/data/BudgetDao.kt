package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM monthly_budgets WHERE id = 1 LIMIT 1")
    fun getBudgetFlow(): Flow<BudgetEntity?>

    @Query("SELECT monthlyLimit FROM monthly_budgets WHERE id = 1 LIMIT 1")
    fun getMonthlyLimitFlow(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: BudgetEntity)

    @Query("DELETE FROM monthly_budgets WHERE id = 1")
    suspend fun clearBudget()
}
