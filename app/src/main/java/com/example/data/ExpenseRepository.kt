package com.example.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpensesFlow()

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun markAsSynced(id: Long) {
        expenseDao.markAsSynced(id, System.currentTimeMillis())
    }

    suspend fun getUnsyncedExpenses(): List<ExpenseEntity> {
        return expenseDao.getUnsyncedExpenses()
    }
}
