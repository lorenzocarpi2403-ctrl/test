package com.example

import com.example.data.MonthItem
import com.example.data.MonthlyStatsHelper
import com.example.data.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MonthlyStatsHelperTest {

    @Test
    fun testMonthItemTransitions() {
        val dec2025 = MonthItem(2025, 11) // December 2025
        val jan2026 = dec2025.nextMonth()
        assertEquals(2026, jan2026.year)
        assertEquals(0, jan2026.month)

        val backToDec = jan2026.previousMonth()
        assertEquals(2025, backToDec.year)
        assertEquals(11, backToDec.month)
    }

    @Test
    fun testMonthItemSorting() {
        val nov2026 = MonthItem(2026, 10)
        val oct2026 = MonthItem(2026, 9)
        val dec2025 = MonthItem(2025, 11)

        val list = listOf(oct2026, dec2025, nov2026).sortedDescending()
        assertEquals(listOf(nov2026, oct2026, dec2025), list)
    }

    @Test
    fun testCalculateSummary() {
        val targetMonth = MonthItem(2026, 10) // Nov 2026 (0-indexed 10)

        val calNov = Calendar.getInstance().apply {
            set(2026, 10, 15, 12, 0, 0)
        }
        val calOct = Calendar.getInstance().apply {
            set(2026, 9, 10, 12, 0, 0)
        }

        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = "EXPENSE",
                title = "Supermercato",
                amount = 45.50,
                category = "GROCERIES",
                date = calNov.timeInMillis
            ),
            TransactionEntity(
                id = 2,
                type = "INCOME",
                title = "Stipendio",
                amount = 2000.0,
                category = "SALARY",
                date = calNov.timeInMillis
            ),
            TransactionEntity(
                id = 3,
                type = "EXPENSE",
                title = "Ristorante Ottobre",
                amount = 30.0,
                category = "RESTAURANT",
                date = calOct.timeInMillis
            )
        )

        val summary = MonthlyStatsHelper.calculateSummary(targetMonth, transactions)

        assertEquals(2000.0, summary.totalIncome, 0.001)
        assertEquals(45.50, summary.totalExpense, 0.001)
        assertEquals(1954.50, summary.netBalance, 0.001)
        assertEquals(2, summary.transactionCount)
        assertEquals(1, summary.incomeCount)
        assertEquals(1, summary.expenseCount)
    }

    @Test
    fun testGenerateMonthsListIncludesCurrentAndPastMonths() {
        val months = MonthlyStatsHelper.generateMonthsList(emptyList(), minPastMonths = 5)
        assertTrue(months.size >= 6)
        assertEquals(MonthItem.current(), months.first())
    }
}
