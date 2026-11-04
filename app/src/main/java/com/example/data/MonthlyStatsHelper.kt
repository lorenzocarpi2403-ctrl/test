package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MonthItem(
    val year: Int,
    val month: Int // 0-indexed: 0 = Gennaio, 11 = Dicembre
) : Comparable<MonthItem> {

    val key: String get() = "$year-$month"

    val displayName: String
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.ITALIAN)
            return sdf.format(cal.time).replaceFirstChar { it.uppercase() }
        }

    val shortMonthName: String
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val sdf = SimpleDateFormat("MMM", Locale.ITALIAN)
            return sdf.format(cal.time).uppercase()
        }

    val yearString: String get() = year.toString()

    val isCurrentMonth: Boolean
        get() {
            val now = Calendar.getInstance()
            return now.get(Calendar.YEAR) == year && now.get(Calendar.MONTH) == month
        }

    fun nextMonth(): MonthItem {
        return if (month == 11) {
            MonthItem(year + 1, 0)
        } else {
            MonthItem(year, month + 1)
        }
    }

    fun previousMonth(): MonthItem {
        return if (month == 0) {
            MonthItem(year - 1, 11)
        } else {
            MonthItem(year, month - 1)
        }
    }

    override fun compareTo(other: MonthItem): Int {
        return if (this.year != other.year) {
            this.year.compareTo(other.year)
        } else {
            this.month.compareTo(other.month)
        }
    }

    companion object {
        fun current(): MonthItem {
            val now = Calendar.getInstance()
            return MonthItem(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        }

        fun fromTimestamp(epochMillis: Long): MonthItem {
            val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
            return MonthItem(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }
    }
}

data class MonthSummary(
    val monthItem: MonthItem,
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val transactionCount: Int,
    val incomeCount: Int,
    val expenseCount: Int,
    val transactions: List<TransactionEntity>
) {
    val savingsRate: Int
        get() = if (totalIncome > 0) {
            ((netBalance / totalIncome) * 100).coerceIn(-100.0, 100.0).toInt()
        } else 0
}

object MonthlyStatsHelper {

    fun generateMonthsList(
        transactions: List<TransactionEntity>,
        minPastMonths: Int = 5
    ): List<MonthItem> {
        val result = mutableSetOf<MonthItem>()

        // 1. Always include current month and past minPastMonths
        var cursor = MonthItem.current()
        result.add(cursor)
        for (i in 1..minPastMonths) {
            cursor = cursor.previousMonth()
            result.add(cursor)
        }

        // 2. Include all months with recorded transactions
        for (tx in transactions) {
            if (tx.date > 0) {
                result.add(MonthItem.fromTimestamp(tx.date))
            }
        }

        // Sort descending (most recent month first)
        return result.sortedDescending()
    }

    fun calculateSummary(
        monthItem: MonthItem,
        allTransactions: List<TransactionEntity>
    ): MonthSummary {
        val monthTransactions = allTransactions.filter { tx ->
            if (tx.date <= 0) return@filter false
            val itemMonth = MonthItem.fromTimestamp(tx.date)
            itemMonth.year == monthItem.year && itemMonth.month == monthItem.month
        }.sortedByDescending { it.date }

        val totalIncome = monthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = monthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense
        val incomeCount = monthTransactions.count { it.type == "INCOME" }
        val expenseCount = monthTransactions.count { it.type == "EXPENSE" }

        return MonthSummary(
            monthItem = monthItem,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            transactionCount = monthTransactions.size,
            incomeCount = incomeCount,
            expenseCount = expenseCount,
            transactions = monthTransactions
        )
    }
}
