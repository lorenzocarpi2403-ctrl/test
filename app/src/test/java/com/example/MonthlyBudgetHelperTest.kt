package com.example

import com.example.data.BudgetStatus
import com.example.data.ExpenseCategory
import com.example.data.MonthlyBudgetHelper
import com.example.data.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MonthlyBudgetHelperTest {

    private fun createCalendar(year: Int, month: Int, day: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    @Test
    fun testNullOrZeroLimitReturnsNull() {
        val transactions = emptyList<TransactionEntity>()
        assertNull(MonthlyBudgetHelper.calculateProgress(null, transactions))
        assertNull(MonthlyBudgetHelper.calculateProgress(0.0, transactions))
        assertNull(MonthlyBudgetHelper.calculateProgress(-100.0, transactions))
    }

    @Test
    fun testProgressWithinBudgetSafe() {
        val testCal = createCalendar(2026, 10, 10) // Nov 10, 2026
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = "EXPENSE",
                title = "Spesa Alimentari",
                amount = 250.0,
                category = ExpenseCategory.GROCERIES.name,
                date = testCal.timeInMillis
            ),
            TransactionEntity(
                id = 2,
                type = "EXPENSE",
                title = "Benzina",
                amount = 50.0,
                category = ExpenseCategory.TRANSPORT.name,
                date = testCal.timeInMillis
            ),
            TransactionEntity(
                id = 3,
                type = "INCOME",
                title = "Stipendio",
                amount = 1800.0,
                category = ExpenseCategory.SALARY.name,
                date = testCal.timeInMillis
            )
        )

        val progress = MonthlyBudgetHelper.calculateProgress(
            monthlyLimit = 1000.0,
            allTransactions = transactions,
            nowCalendar = testCal
        )

        assertNotNull(progress)
        progress?.let {
            assertEquals(1000.0, it.monthlyLimit, 0.001)
            assertEquals(300.0, it.currentSpent, 0.001)
            assertEquals(700.0, it.remaining, 0.001)
            assertEquals(0.0, it.overspentAmount, 0.001)
            assertEquals(30, it.percentage)
            assertEquals(0.30f, it.progressFraction, 0.01f)
            assertEquals(BudgetStatus.SAFE, it.status)
            assertTrue(it.dailySafeAllowance > 0.0)
        }
    }

    @Test
    fun testProgressWarningThreshold() {
        val testCal = createCalendar(2026, 10, 20) // Nov 20, 2026
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = "EXPENSE",
                title = "Affitto",
                amount = 850.0,
                category = ExpenseCategory.UTILITIES.name,
                date = testCal.timeInMillis
            )
        )

        val progress = MonthlyBudgetHelper.calculateProgress(
            monthlyLimit = 1000.0,
            allTransactions = transactions,
            nowCalendar = testCal
        )

        assertNotNull(progress)
        progress?.let {
            assertEquals(850.0, it.currentSpent, 0.001)
            assertEquals(150.0, it.remaining, 0.001)
            assertEquals(85, it.percentage)
            assertEquals(0.85f, it.progressFraction, 0.01f)
            assertEquals(BudgetStatus.WARNING, it.status)
        }
    }

    @Test
    fun testProgressExceeded() {
        val testCal = createCalendar(2026, 10, 25) // Nov 25, 2026
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = "EXPENSE",
                title = "Spesa Ingente",
                amount = 1250.0,
                category = ExpenseCategory.SHOPPING.name,
                date = testCal.timeInMillis
            )
        )

        val progress = MonthlyBudgetHelper.calculateProgress(
            monthlyLimit = 1000.0,
            allTransactions = transactions,
            nowCalendar = testCal
        )

        assertNotNull(progress)
        progress?.let {
            assertEquals(1250.0, it.currentSpent, 0.001)
            assertEquals(0.0, it.remaining, 0.001)
            assertEquals(250.0, it.overspentAmount, 0.001)
            assertEquals(125, it.percentage)
            assertEquals(1.0f, it.progressFraction, 0.001f) // Visual clamp
            assertEquals(1.25f, it.rawProgressFraction, 0.001f)
            assertEquals(BudgetStatus.EXCEEDED, it.status)
            assertEquals(0.0, it.dailySafeAllowance, 0.001)
        }
    }

    @Test
    fun testDaysRemainingAndDailyAllowance() {
        // November has 30 days. Setting day to Nov 15 means remaining days = 30 - 15 + 1 = 16 days.
        val testCal = createCalendar(2026, 10, 15)
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = "EXPENSE",
                title = "Varie",
                amount = 200.0,
                category = ExpenseCategory.OTHER.name,
                date = testCal.timeInMillis
            )
        )

        val progress = MonthlyBudgetHelper.calculateProgress(
            monthlyLimit = 1000.0,
            allTransactions = transactions,
            nowCalendar = testCal
        )

        assertNotNull(progress)
        progress?.let {
            assertEquals(16, it.daysRemainingInMonth)
            assertEquals(800.0, it.remaining, 0.001)
            // 800 / 16 = 50.0 per day
            assertEquals(50.0, it.dailySafeAllowance, 0.001)
        }
    }
}
