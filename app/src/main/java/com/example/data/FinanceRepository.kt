package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val subscriptionDao: SubscriptionDao,
    private val recurringDao: RecurringTransactionDao,
    private val budgetDao: BudgetDao
) {
    // Accounts
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccountsFlow()
    val totalNetWorth: Flow<Double?> = accountDao.getTotalNetWorthFlow()

    suspend fun insertAccount(account: AccountEntity): Long {
        return accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.deleteAccount(account)
    }

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(transaction)
        val delta = if (transaction.type == "INCOME") transaction.amount else -transaction.amount
        accountDao.adjustBalance(transaction.accountId, delta)
        return id
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
        val revertDelta = if (transaction.type == "INCOME") -transaction.amount else transaction.amount
        accountDao.adjustBalance(transaction.accountId, revertDelta)
    }

    // Subscriptions
    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptionsFlow()
    val activeSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getActiveSubscriptionsFlow()

    suspend fun addSubscription(subscription: SubscriptionEntity): Long {
        return subscriptionDao.insertSubscription(subscription)
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.updateSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.deleteSubscription(subscription)
    }

    // Fixed & Recurring Transactions (Entrate Fisse e Uscite Fisse)
    val allRecurring: Flow<List<RecurringTransactionEntity>> = recurringDao.getAllRecurringFlow()

    suspend fun addRecurring(recurring: RecurringTransactionEntity): Long {
        return recurringDao.insertRecurring(recurring)
    }

    suspend fun updateRecurring(recurring: RecurringTransactionEntity) {
        recurringDao.updateRecurring(recurring)
    }

    suspend fun deleteRecurring(recurring: RecurringTransactionEntity) {
        recurringDao.deleteRecurring(recurring)
    }

    /**
     * Registra immediatamente un'istanza reale della transazione fissa nel conto associato.
     */
    suspend fun applyRecurringNow(recurring: RecurringTransactionEntity): Long {
        val tx = TransactionEntity(
            type = recurring.type,
            title = recurring.title,
            amount = recurring.amount,
            currency = recurring.currency,
            category = recurring.category,
            date = System.currentTimeMillis(),
            accountId = recurring.accountId,
            notes = "Transazione fissa ricorrente (${recurring.frequency})"
        )
        return addTransaction(tx)
    }

    // Monthly Spending Limit (Budget Mensile)
    val monthlyBudget: Flow<BudgetEntity?> = budgetDao.getBudgetFlow()
    val monthlyLimit: Flow<Double?> = budgetDao.getMonthlyLimitFlow()

    suspend fun setMonthlyBudget(limit: Double) {
        budgetDao.setBudget(BudgetEntity(id = 1, monthlyLimit = limit))
    }

    suspend fun clearMonthlyBudget() {
        budgetDao.clearBudget()
    }
}
