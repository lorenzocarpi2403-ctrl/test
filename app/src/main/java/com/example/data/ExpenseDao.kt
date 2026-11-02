package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("UPDATE expenses SET isSyncedToSheets = 1, syncedAt = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: Long, timestamp: Long)

    @Query("SELECT * FROM expenses WHERE isSyncedToSheets = 0 ORDER BY date ASC")
    suspend fun getUnsyncedExpenses(): List<ExpenseEntity>

    @Query("DELETE FROM expenses")
    suspend fun clearAll()
}
