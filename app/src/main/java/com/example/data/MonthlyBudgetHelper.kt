package com.example.data

import java.util.Calendar

enum class BudgetStatus {
    SAFE,       // < 80%
    WARNING,    // 80% to 100%
    EXCEEDED    // > 100%
}

data class BudgetProgress(
    val monthlyLimit: Double,
    val currentSpent: Double,
    val remaining: Double,
    val overspentAmount: Double,
    val progressFraction: Float, // 0.0f to 1.0f (clamped for visual progress bar)
    val rawProgressFraction: Float, // > 1.0f if exceeded
    val percentage: Int, // e.g. 75, 110
    val status: BudgetStatus,
    val daysRemainingInMonth: Int,
    val dailySafeAllowance: Double
)

object MonthlyBudgetHelper {

    fun calculateProgress(
        monthlyLimit: Double?,
        allTransactions: List<TransactionEntity>,
        nowCalendar: Calendar = Calendar.getInstance()
    ): BudgetProgress? {
        if (monthlyLimit == null || monthlyLimit <= 0.0) return null

        val currentMonth = MonthItem(
            year = nowCalendar.get(Calendar.YEAR),
            month = nowCalendar.get(Calendar.MONTH)
        )
        val currentMonthSummary = MonthlyStatsHelper.calculateSummary(currentMonth, allTransactions)
        val currentSpent = currentMonthSummary.totalExpense

        val remaining = if (currentSpent < monthlyLimit) monthlyLimit - currentSpent else 0.0
        val overspent = if (currentSpent > monthlyLimit) currentSpent - monthlyLimit else 0.0

        val rawFraction = (currentSpent / monthlyLimit).toFloat()
        val progressFraction = rawFraction.coerceIn(0f, 1f)
        val percentage = (rawFraction * 100).toInt()

        val status = when {
            currentSpent > monthlyLimit -> BudgetStatus.EXCEEDED
            currentSpent >= monthlyLimit * 0.8 -> BudgetStatus.WARNING
            else -> BudgetStatus.SAFE
        }

        // Days remaining in month (inclusive of today)
        val totalDaysInMonth = nowCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = nowCalendar.get(Calendar.DAY_OF_MONTH)
        val daysRemaining = (totalDaysInMonth - currentDay + 1).coerceAtLeast(1)

        val dailySafe = if (remaining > 0.0) remaining / daysRemaining else 0.0

        return BudgetProgress(
            monthlyLimit = monthlyLimit,
            currentSpent = currentSpent,
            remaining = remaining,
            overspentAmount = overspent,
            progressFraction = progressFraction,
            rawProgressFraction = rawFraction,
            percentage = percentage,
            status = status,
            daysRemainingInMonth = daysRemaining,
            dailySafeAllowance = dailySafe
        )
    }
}
