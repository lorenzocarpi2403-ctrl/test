package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        SubscriptionEntity::class,
        RecurringTransactionEntity::class,
        BudgetEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "receipt_finance.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMillis = 86_400_000L

            // Default Accounts
            val defaultAccounts = listOf(
                AccountEntity(
                    id = 1,
                    name = "Conto Principale",
                    balance = 2450.00,
                    initialBalance = 2450.00,
                    colorHex = 0xFF059669, // Emerald
                    iconType = "BANK"
                ),
                AccountEntity(
                    id = 2,
                    name = "Carta Prepagata / Revolut",
                    balance = 340.50,
                    initialBalance = 340.50,
                    colorHex = 0xFF3B82F6, // Blue
                    iconType = "CARD"
                ),
                AccountEntity(
                    id = 3,
                    name = "Portafoglio Contanti",
                    balance = 120.00,
                    initialBalance = 120.00,
                    colorHex = 0xFFF59E0B, // Amber
                    iconType = "CASH"
                ),
                AccountEntity(
                    id = 4,
                    name = "Fondo Risparmi",
                    balance = 5200.00,
                    initialBalance = 5200.00,
                    colorHex = 0xFF8B5CF6, // Purple
                    iconType = "SAVINGS"
                )
            )
            database.accountDao().insertAccounts(defaultAccounts)

            // Default Subscriptions
            val defaultSubscriptions = listOf(
                SubscriptionEntity(
                    id = 1,
                    name = "Netflix",
                    cost = 13.99,
                    billingCycle = "MONTHLY",
                    nextRenewalDate = now + (12 * dayMillis),
                    category = "Streaming",
                    colorHex = 0xFFE50914, // Netflix Red
                    accountId = 2,
                    isActive = true
                ),
                SubscriptionEntity(
                    id = 2,
                    name = "Spotify Premium",
                    cost = 10.99,
                    billingCycle = "MONTHLY",
                    nextRenewalDate = now + (5 * dayMillis),
                    category = "Musica",
                    colorHex = 0xFF1DB954, // Spotify Green
                    accountId = 2,
                    isActive = true
                ),
                SubscriptionEntity(
                    id = 3,
                    name = "Disney+",
                    cost = 9.99,
                    billingCycle = "MONTHLY",
                    nextRenewalDate = now + (19 * dayMillis),
                    category = "Streaming",
                    colorHex = 0xFF113CCF, // Disney Blue
                    accountId = 2,
                    isActive = true
                ),
                SubscriptionEntity(
                    id = 4,
                    name = "Amazon Prime",
                    cost = 49.90,
                    billingCycle = "ANNUAL",
                    nextRenewalDate = now + (90 * dayMillis),
                    category = "Servizi",
                    colorHex = 0xFFFF9900, // Amazon Orange
                    accountId = 1,
                    isActive = true
                ),
                SubscriptionEntity(
                    id = 5,
                    name = "iCloud 200GB",
                    cost = 2.99,
                    billingCycle = "MONTHLY",
                    nextRenewalDate = now + (2 * dayMillis),
                    category = "Cloud",
                    colorHex = 0xFF007AFF, // Apple Blue
                    accountId = 1,
                    isActive = true
                )
            )
            database.subscriptionDao().insertSubscriptions(defaultSubscriptions)

            // Default Recurring Transactions (Entrate Fisse e Uscite Fisse)
            val defaultRecurring = listOf(
                RecurringTransactionEntity(
                    id = 1,
                    type = "INCOME",
                    title = "Stipendio Mensile Fisso",
                    amount = 1850.00,
                    frequency = "MONTHLY",
                    dayOfMonth = 27,
                    nextDate = now + (20 * dayMillis),
                    category = ExpenseCategory.SALARY.name,
                    colorHex = 0xFF059669,
                    accountId = 1,
                    isActive = true,
                    notes = "Accredito automatico busta paga"
                ),
                RecurringTransactionEntity(
                    id = 2,
                    type = "EXPENSE",
                    title = "Affitto Appartamento",
                    amount = 650.00,
                    frequency = "MONTHLY",
                    dayOfMonth = 5,
                    nextDate = now + (2 * dayMillis),
                    category = ExpenseCategory.UTILITIES.name,
                    colorHex = 0xFFEF4444,
                    accountId = 1,
                    isActive = true,
                    notes = "Bonifico mensile locazione"
                ),
                RecurringTransactionEntity(
                    id = 3,
                    type = "EXPENSE",
                    title = "Abbonamenti Digitali (Netflix & Spotify)",
                    amount = 24.98,
                    frequency = "MONTHLY",
                    dayOfMonth = 15,
                    nextDate = now + (12 * dayMillis),
                    category = ExpenseCategory.ENTERTAINMENT.name,
                    colorHex = 0xFF6366F1,
                    accountId = 2,
                    isActive = true,
                    notes = "Addebito carta streaming"
                ),
                RecurringTransactionEntity(
                    id = 4,
                    type = "EXPENSE",
                    title = "Spese Condominiali",
                    amount = 85.00,
                    frequency = "MONTHLY",
                    dayOfMonth = 10,
                    nextDate = now + (7 * dayMillis),
                    category = ExpenseCategory.UTILITIES.name,
                    colorHex = 0xFFF59E0B,
                    accountId = 1,
                    isActive = true,
                    notes = "Quota fissa condominio"
                )
            )
            database.recurringTransactionDao().insertAll(defaultRecurring)

            // Sample Initial Transactions
            val sampleTransactions = listOf(
                TransactionEntity(
                    type = "INCOME",
                    title = "Stipendio Mensile",
                    amount = 1850.00,
                    category = ExpenseCategory.SALARY.name,
                    date = now - (2 * dayMillis),
                    accountId = 1,
                    notes = "Accredito mensile busta paga"
                ),
                TransactionEntity(
                    type = "EXPENSE",
                    title = "Supermercato Esselunga",
                    amount = 64.30,
                    category = ExpenseCategory.GROCERIES.name,
                    date = now - (1 * dayMillis),
                    accountId = 2,
                    notes = "Spesa settimanale alimentari"
                ),
                TransactionEntity(
                    type = "EXPENSE",
                    title = "Eni Station Carburante",
                    amount = 50.00,
                    category = ExpenseCategory.TRANSPORT.name,
                    date = now - (3 * dayMillis),
                    accountId = 2,
                    notes = "Pieno benzina auto"
                ),
                TransactionEntity(
                    type = "EXPENSE",
                    title = "Pizzeria Da Mario",
                    amount = 32.50,
                    category = ExpenseCategory.RESTAURANT.name,
                    date = now - (4 * dayMillis),
                    accountId = 3,
                    notes = "Cena pizza con amici"
                ),
                TransactionEntity(
                    type = "INCOME",
                    title = "Vendita Oggetto Usato",
                    amount = 80.00,
                    category = ExpenseCategory.OTHER_INCOME.name,
                    date = now - (5 * dayMillis),
                    accountId = 3,
                    notes = "Vendita Subito.it"
                )
            )
            database.transactionDao().insertTransactions(sampleTransactions)

            // Default Monthly Spending Limit (Budget Mensile)
            database.budgetDao().setBudget(
                BudgetEntity(
                    id = 1,
                    monthlyLimit = 1200.00
                )
            )
        }
    }
}
