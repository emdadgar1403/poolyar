package com.example.model

import com.example.data.local.entity.TransactionEntity

data class WeeklyFinancialSummary(
    val weekNumber: Int,
    val year: Int,
    val weekLabel: String,
    val startDatePersian: String,
    val endDatePersian: String,
    val allowanceAmount: Long,
    val isAllowancePaid: Boolean,
    val totalExpenses: Long,
    val totalSavings: Long,
    val remainingBalance: Long,
    val categoryExpenses: Map<ExpenseCategory, Long> = emptyMap()
)

data class MonthlyFinancialSummary(
    val monthName: String,
    val monthNumber: Int,
    val year: Int,
    val totalAllowance: Long,
    val totalExpenses: Long,
    val totalSavings: Long,
    val topExpenseCategory: ExpenseCategory?,
    val topExpenseAmount: Long,
    val dailyExpenseAverage: Long,
    val categoryExpenses: Map<ExpenseCategory, Long> = emptyMap()
)

data class DashboardState(
    val teenName: String = "نوجوان قهرمان",
    val teenAge: Int = 13,
    val currentBalance: Long = 0L,
    val totalSavings: Long = 0L,
    val totalBalance: Long = 0L,
    val weeklyAllowance: Long = 350_000L,
    val dailyAllowance: Long = 50_000L,
    val isWeeklyAllowancePaid: Boolean = false,
    val weeklyExpenses: Long = 0L,
    val weeklySavings: Long = 0L,
    val savingsPercentage: Int = 0,
    val weeklyExpenseLimit: Long = 300_000L,
    val isExpenseLimitExceeded: Boolean = false,
    val suggestedSavingsPercent: Int = 20,
    val categoryExpensesThisWeek: Map<ExpenseCategory, Long> = emptyMap(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val currentWeekNumber: Int = 1,
    val currentYear: Int = 1405
)
